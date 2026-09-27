package de.timo.foreverproductionmonitor.blockentity;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.CraftingJobStatus;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.AECraftingPattern;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.crafting.pattern.AESmithingTablePattern;
import appeng.crafting.pattern.AEStonecuttingPattern;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

/**
 * A bounded, read-only view of AE2's node-backed providers. All calls are made on the
 * server thread. No crafting plans, CPU internals, or external machine state are inferred.
 */
public final class CraftingDiagnostics {
    public static final int MAX_PROVIDERS = 256;
    public static final int MAX_PATTERNS = 256;
    public static final int MAX_PATTERN_SLOTS = 2048;
    public static final int MAX_CPUS = 64;

    private CraftingDiagnostics() {}

    public static Snapshot inspect(ProductionMonitorBlockEntity monitor) {
        if (!monitor.isMonitorOnline() || monitor.getMainNode().getGrid() == null) {
            return new Snapshot(false, false, List.of(), List.of(), List.of());
        }
        IGrid grid = monitor.getMainNode().getGrid();
        List<Job> jobs = new ArrayList<>();
        boolean limited = false;
        for (ICraftingCPU cpu : grid.getCraftingService().getCpus()) {
            if (!cpu.isBusy()) continue;
            if (jobs.size() >= MAX_CPUS) {
                limited = true;
                break;
            }
            CraftingJobStatus status = cpu.getJobStatus();
            jobs.add(new Job(cpu.getName() == null ? "Unnamed CPU" : cpu.getName().getString(),
                    cpu.getAvailableStorage(), cpu.getCoProcessors(),
                    status == null || status.crafting() == null ? "Job details unavailable"
                            : label(status.crafting().what()) + " × " + status.crafting().amount(),
                    status == null || status.crafting() == null ? null
                            : icon(status.crafting().what())));
        }

        List<Provider> providers = new ArrayList<>();
        LinkedHashMap<AEItemKey, MutablePattern> patterns = new LinkedHashMap<>();
        int slots = 0;
        for (IGridNode node : grid.getNodes()) {
            ICraftingProvider service = node.getService(ICraftingProvider.class);
            if (service == null) continue;
            if (providers.size() >= MAX_PROVIDERS) {
                limited = true;
                break;
            }
            ProductionMonitorBlockEntity.DeviceLocation loc =
                    ProductionMonitorBlockEntity.locationOf(node, node.getOwner());
            AEItemKey visual = node.getVisualRepresentation();
            String name = visual == null ? node.getOwner().getClass().getSimpleName()
                    : visual.getDisplayName().getString();
            int index = providers.size();
            providers.add(new Provider(name, loc == null ? null : loc.dimension(),
                    loc == null ? null : loc.pos(), node.isPowered(), node.meetsChannelRequirements(),
                    node.hasGridBooted(), node.isActive(), service.getPatternPriority(),
                    visual == null ? null : icon(visual)));
            // Offline node inventories can be observed, but are not claimed to be craftable.
            for (IPatternDetails details : service.getAvailablePatterns()) {
                if (++slots > MAX_PATTERN_SLOTS) {
                    limited = true;
                    break;
                }
                if (details == null || details.getDefinition() == null
                        || details.getOutputs().isEmpty()) continue;
                MutablePattern existing = patterns.get(details.getDefinition());
                if (existing == null) {
                    if (patterns.size() >= MAX_PATTERNS) {
                        limited = true;
                        continue;
                    }
                    existing = new MutablePattern(details);
                    patterns.put(details.getDefinition(), existing);
                }
                existing.copies++;
                if (!existing.providers.contains(index)) existing.providers.add(index);
            }
            if (slots > MAX_PATTERN_SLOTS) break;
        }

        List<MutablePattern> all = new ArrayList<>(patterns.values());
        for (MutablePattern pattern : all) limited |= pattern.truncated;
        Map<AEKey, List<Integer>> byOutput = new HashMap<>();
        for (int i = 0; i < all.size(); i++) {
            for (GenericStack output : all.get(i).outputs) {
                byOutput.computeIfAbsent(output.what(), ignored -> new ArrayList<>()).add(i);
            }
        }
        Map<AEKey, Set<List<InputSpec>>> inputVariants = new HashMap<>();
        for (MutablePattern pattern : all) {
            inputVariants.computeIfAbsent(pattern.primary.what(), ignored -> new HashSet<>())
                    .add(pattern.inputSpecs);
        }
        List<Pattern> result = new ArrayList<>();
        for (MutablePattern pattern : all) {
            Set<Integer> dependencies = new HashSet<>();
            for (InputSpec input : pattern.inputSpecs) {
                for (GenericStack option : input.options()) {
                    dependencies.addAll(byOutput.getOrDefault(option.what(), List.of()));
                }
            }
            ArrayList<Integer> ordered = new ArrayList<>(dependencies);
            ordered.sort(Integer::compareTo);
            result.add(new Pattern(pattern.type, label(pattern.primary.what()),
                    pattern.primary.amount(), pattern.inputText, pattern.outputText,
                    List.copyOf(pattern.providers), ordered, pattern.copies,
                    pattern.providers.size() > 1,
                    inputVariants.get(pattern.primary.what()).size() > 1,
                    icon(pattern.primary.what()), pattern.inputIcons, pattern.outputIcons));
        }
        return new Snapshot(true, limited, jobs, providers, result);
    }

    private static String label(AEKey key) {
        String display = key.getDisplayName().getString();
        return display.length() > 90 ? display.substring(0, 90) : display;
    }

    private static AEKey icon(AEKey key) {
        // The item/fluid type is enough for the GUI icon. Strip components so
        // large NBT payloads are never copied into a diagnostics snapshot.
        return key.dropSecondary();
    }

    private record InputSpec(List<GenericStack> options, long multiplier) {}

    private static final class MutablePattern {
        final String type;
        final GenericStack primary;
        final List<GenericStack> outputs;
        final List<InputSpec> inputSpecs = new ArrayList<>();
        final List<String> inputText = new ArrayList<>();
        final List<String> outputText = new ArrayList<>();
        final List<AEKey> inputIcons = new ArrayList<>();
        final List<AEKey> outputIcons = new ArrayList<>();
        final List<Integer> providers = new ArrayList<>();
        int copies;
        boolean truncated;

        MutablePattern(IPatternDetails details) {
            type = details instanceof AEProcessingPattern ? "Processing"
                    : details instanceof AECraftingPattern || details instanceof AESmithingTablePattern
                    || details instanceof AEStonecuttingPattern ? "Crafting" : "Other";
            primary = details.getPrimaryOutput();
            outputs = details.getOutputs();
            for (IPatternDetails.IInput input : details.getInputs()) {
                if (inputSpecs.size() >= 12) { truncated = true; break; }
                List<GenericStack> options = List.of(input.getPossibleInputs());
                inputSpecs.add(new InputSpec(options, input.getMultiplier()));
                if (!options.isEmpty()) {
                    GenericStack first = options.get(0);
                    inputText.add(label(first.what()) + " × " + (first.amount() * input.getMultiplier())
                            + (options.size() > 1 ? " (+" + (options.size() - 1) + " alternatives)" : ""));
                    inputIcons.add(icon(first.what()));
                }
            }
            for (GenericStack output : outputs) {
                if (outputText.size() >= 8) { truncated = true; break; }
                outputText.add(label(output.what()) + " × " + output.amount());
                outputIcons.add(icon(output.what()));
            }
        }
    }

    public record Snapshot(boolean online, boolean limited, List<Job> jobs,
                           List<Provider> providers, List<Pattern> patterns) {}
    public record Job(String cpuName, long storage, int coProcessors, String target, AEKey targetIcon) {}
    public record Provider(String name, ResourceLocation dimension, BlockPos pos,
                           boolean powered, boolean channel, boolean booted,
                           boolean active, int priority, AEKey visualIcon) {}
    public record Pattern(String type, String output, long amount, List<String> inputs,
                          List<String> outputs, List<Integer> providers,
                          List<Integer> dependencies, int copies, boolean multipleProviders,
                          boolean outputVariants, AEKey outputIcon,
                          List<AEKey> inputIcons, List<AEKey> outputIcons) {}
}
