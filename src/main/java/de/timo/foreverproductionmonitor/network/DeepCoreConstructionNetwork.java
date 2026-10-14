package de.timo.foreverproductionmonitor.network;

import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.CraftingSubmitErrorCode;
import appeng.api.networking.crafting.UnsuitableCpus;
import appeng.api.config.Actionable;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.me.helpers.PlayerSource;
import de.timo.foreverproductionmonitor.blockentity.ProductionMonitorBlockEntity;
import de.timo.foreverproductionmonitor.client.ClientDeepCoreConstructionState;
import de.timo.foreverproductionmonitor.item.ProductionTabletItem;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Optional ForeverDeepCore construction integration.
 *
 * <p>No compile-time Deep Core dependency is required. The bill-of-materials is
 * read from ForeverDeepCore's public construction API by reflection when that
 * mod is installed. This keeps FPM independently loadable.</p>
 */
public final class DeepCoreConstructionNetwork {
    private static final String DEEP_CORE_MOD_ID = "forever_deep_core";
    private static final String PLAN_API = "de.timo.foreverdeepcore.api.DeepCoreConstructionPlans";
    private static final Map<UUID, BatchSession> BATCHES = new LinkedHashMap<>();
    private static boolean tickRegistered;

    private DeepCoreConstructionNetwork() {}

    static void register(PayloadRegistrar registrar) {
        registrar.playToServer(PlanRequest.TYPE, PlanRequest.CODEC, DeepCoreConstructionNetwork::handlePlanRequest);
        registrar.playToClient(PlanResponse.TYPE, PlanResponse.CODEC, DeepCoreConstructionNetwork::handlePlanResponse);
        registrar.playToServer(BatchRequest.TYPE, BatchRequest.CODEC, DeepCoreConstructionNetwork::handleBatchRequest);
        registrar.playToClient(BatchResult.TYPE, BatchResult.CODEC, DeepCoreConstructionNetwork::handleBatchResult);
        if (!tickRegistered) {
            tickRegistered = true;
            NeoForge.EVENT_BUS.addListener(DeepCoreConstructionNetwork::tickBatches);
            NeoForge.EVENT_BUS.addListener(DeepCoreConstructionNetwork::serverStopped);
        }
    }

    public static void requestPlan(ProductionTabletItem.MonitorLink link, int tier) {
        PacketDistributor.sendToServer(new PlanRequest(link.dimension(), link.pos(), tier));
    }

    public static void startBatch(ProductionTabletItem.MonitorLink link, int tier) {
        PacketDistributor.sendToServer(new BatchRequest(link.dimension(), link.pos(), tier));
    }

    private static void handlePlanRequest(PlanRequest request, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            PlanSnapshot snapshot = buildSnapshot(player, request.dimension(), request.pos(), request.tier());
            PacketDistributor.sendToPlayer(player,
                    new PlanResponse(request.dimension(), request.pos(), request.tier(), snapshot));
        });
    }

    private static void handlePlanResponse(PlanResponse response, IPayloadContext context) {
        ClientDeepCoreConstructionState.acceptPlan(response);
    }

    private static PlanSnapshot buildSnapshot(ServerPlayer player, ResourceLocation dimension,
                                              BlockPos pos, int tier) {
        if (tier < 1 || tier > 3) {
            return new PlanSnapshot(PlanStatus.INVALID_TIER, List.of());
        }
        if (!ModList.get().isLoaded(DEEP_CORE_MOD_ID)) {
            return new PlanSnapshot(PlanStatus.MOD_MISSING, List.of());
        }

        ProductionMonitorBlockEntity monitor = MonitorNetwork.linkedMonitor(player, dimension, pos);
        if (monitor == null) {
            return new PlanSnapshot(PlanStatus.INVALID_LINK, List.of());
        }
        if (!monitor.isMonitorOnline() || monitor.getMainNode().getGrid() == null) {
            return new PlanSnapshot(PlanStatus.NETWORK_OFFLINE, List.of());
        }

        try {
            Map<ResourceLocation, Integer> counts = readPlan(tier);
            if (counts.isEmpty()) {
                return new PlanSnapshot(PlanStatus.PLAN_ERROR, List.of());
            }

            var grid = monitor.getMainNode().getGrid();
            Map<AEItemKey, Long> stored = storedItems(grid.getStorageService().getCachedInventory());
            List<PlanEntry> entries = new ArrayList<>(counts.size());
            for (var entry : counts.entrySet()) {
                Item item = BuiltInRegistries.ITEM.getOptional(entry.getKey()).orElse(Items.AIR);
                if (item == Items.AIR || entry.getValue() <= 0) {
                    return new PlanSnapshot(PlanStatus.PLAN_ERROR, List.of());
                }
                AEItemKey key = AEItemKey.of(item);
                long required = entry.getValue();
                long available = saturatingAdd(
                        Math.max(0L, stored.getOrDefault(key, 0L)),
                        inventoryCount(player, item));
                long missing = Math.max(0L, required - available);
                boolean craftable = missing == 0L || grid.getCraftingService().isCraftable(key);
                entries.add(new PlanEntry(key, required, available, missing, craftable));
            }
            return new PlanSnapshot(PlanStatus.READY, List.copyOf(entries));
        } catch (Throwable error) {
            return new PlanSnapshot(PlanStatus.PLAN_ERROR, List.of());
        }
    }

    private static void handleBatchRequest(BatchRequest request, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            BatchSession existing = BATCHES.get(player.getUUID());
            if (existing != null) {
                sendBatchResult(player, request, BatchStatus.ERROR,
                        existing.started, existing.requirements.size(), "A build kit is already being crafted");
                return;
            }
            if (request.tier() < 1 || request.tier() > 3) {
                sendBatchResult(player, request, BatchStatus.INVALID_TIER, 0, 0, "");
                return;
            }
            if (!ModList.get().isLoaded(DEEP_CORE_MOD_ID)) {
                sendBatchResult(player, request, BatchStatus.MOD_MISSING, 0, 0, "");
                return;
            }
            ProductionMonitorBlockEntity monitor =
                    MonitorNetwork.linkedMonitor(player, request.dimension(), request.pos());
            if (monitor == null) {
                sendBatchResult(player, request, BatchStatus.INVALID_LINK, 0, 0, "");
                return;
            }
            if (!monitor.isMonitorOnline() || monitor.getMainNode().getGrid() == null) {
                sendBatchResult(player, request, BatchStatus.NETWORK_OFFLINE, 0, 0, "");
                return;
            }
            try {
                Map<AEItemKey, Long> required = new LinkedHashMap<>();
                for (var entry : readPlan(request.tier()).entrySet()) {
                    Item item = BuiltInRegistries.ITEM.getOptional(entry.getKey()).orElse(Items.AIR);
                    if (item == Items.AIR || entry.getValue() <= 0) {
                        throw new IllegalStateException("Invalid construction component");
                    }
                    required.put(AEItemKey.of(item), entry.getValue().longValue());
                }
                var crafting = monitor.getMainNode().getGrid().getCraftingService();
                boolean missing = false;
                for (var entry : required.entrySet()) {
                    if (availableFor(player, monitor, entry.getKey(), entry.getValue()) >= entry.getValue()) continue;
                    missing = true;
                    if (!crafting.isCraftable(entry.getKey())) {
                        sendBatchResult(player, request, BatchStatus.NOT_CRAFTABLE, 0, required.size(),
                                entry.getKey().getDisplayName().getString());
                        return;
                    }
                }
                if (!missing) {
                    sendBatchResult(player, request, BatchStatus.NOTHING_TO_CRAFT, 0, 0, "");
                    return;
                }
                List<AEItemKey> order = ConstructionBatchOrder.consumerFirst(required.keySet(),
                        key -> componentDependencies(crafting, key, required.keySet()));
                BATCHES.put(player.getUUID(), new BatchSession(request, required, order));
                sendBatchResult(player, request, BatchStatus.QUEUED, 0, required.size(), "");
            } catch (Throwable error) {
                sendBatchResult(player, request, BatchStatus.ERROR, 0, 0,
                        error.getClass().getSimpleName());
            }
        });
    }

    private static Set<AEItemKey> componentDependencies(
            appeng.api.networking.crafting.ICraftingService crafting,
            AEItemKey root, Set<AEItemKey> components) {
        Set<AEItemKey> dependencies = new LinkedHashSet<>();
        Set<AEKey> visited = new HashSet<>();
        ArrayDeque<AEKey> pending = new ArrayDeque<>();
        pending.add(root);
        while (!pending.isEmpty() && visited.size() < 512) {
            AEKey key = pending.removeFirst();
            if (!visited.add(key)) continue;
            int patterns = 0;
            for (var pattern : crafting.getCraftingFor(key)) {
                if (++patterns > 32) break;
                for (var input : pattern.getInputs()) {
                    for (var possible : input.getPossibleInputs()) {
                        AEKey ingredient = possible.what();
                        if (ingredient instanceof AEItemKey item && components.contains(item) && !item.equals(root)) {
                            dependencies.add(item);
                        }
                        if (!visited.contains(ingredient) && pending.size() < 2048) pending.addLast(ingredient);
                    }
                }
            }
        }
        return dependencies;
    }

    private static long availableFor(ServerPlayer player, ProductionMonitorBlockEntity monitor,
                                     AEItemKey key, long required) {
        long stored = monitor.getMainNode().getGrid().getStorageService().getInventory()
                .extract(key, required, Actionable.SIMULATE, new PlayerSource(player, monitor));
        return saturatingAdd(stored, inventoryCount(player, key.getItem()));
    }

    private static void tickBatches(ServerTickEvent.Post event) {
        var server = event.getServer();
        if (server.getTickCount() % 10 != 0) return;
        var iterator = BATCHES.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            BatchSession batch = entry.getValue();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null || player.isRemoved()) {
                batch.cancelCalculation();
                iterator.remove();
                continue;
            }
            ProductionMonitorBlockEntity monitor = MonitorNetwork.linkedMonitor(
                    player, batch.request.dimension(), batch.request.pos());
            if (monitor == null || !monitor.isMonitorOnline() || monitor.getMainNode().getGrid() == null) {
                failBatch(player, batch, BatchStatus.NETWORK_OFFLINE, "");
                iterator.remove();
                continue;
            }
            try {
                if (!advanceBatch(player, monitor, batch)) iterator.remove();
            } catch (Exception error) {
                failBatch(player, batch, BatchStatus.ERROR, error.getClass().getSimpleName());
                iterator.remove();
            }
        }
    }

    /** Each plan is calculated only after the previous job has finished. */
    private static boolean advanceBatch(ServerPlayer player, ProductionMonitorBlockEntity monitor,
                                        BatchSession batch) throws Exception {
        var crafting = monitor.getMainNode().getGrid().getCraftingService();
        if (batch.activeJob != null) {
            if (batch.activeJob.isCanceled()) {
                failBatch(player, batch, BatchStatus.SUBMIT_FAILED, "Crafting job cancelled");
                return false;
            }
            if (!batch.activeJob.isDone()) return true;
            batch.activeJob = null;
            batch.index++;
        }
        if (batch.index >= batch.order.size()) {
            boolean complete = batch.requirements.entrySet().stream().allMatch(entry ->
                    availableFor(player, monitor, entry.getKey(), entry.getValue()) >= entry.getValue());
            if (complete) {
                sendBatchResult(player, batch.request, BatchStatus.COMPLETE,
                        batch.started, batch.requirements.size(), "");
                return false;
            }
            // Alternative recipe paths or external consumers can invalidate earlier stock.
            // Reconcile the real complete kit, never declare success on stale initial counts.
            if (++batch.reconciliations > 2) {
                failBatch(player, batch, BatchStatus.SUBMIT_FAILED, "Component stock changed during crafting");
                return false;
            }
            batch.index = 0;
        }
        AEItemKey key = batch.order.get(batch.index);
        long required = batch.requirements.get(key);
        long missing = Math.max(0L, required - availableFor(player, monitor, key, required));
        if (missing == 0L) {
            batch.cancelCalculation();
            batch.index++;
            return true;
        }
        if (!crafting.isCraftable(key)) {
            failBatch(player, batch, BatchStatus.NOT_CRAFTABLE, key.getDisplayName().getString());
            return false;
        }
        var source = new PlayerSource(player, monitor);
        if (batch.calculation == null) {
            batch.calculation = crafting.beginCraftingCalculation(player.level(), () -> source,
                    key, missing, CalculationStrategy.REPORT_MISSING_ITEMS);
            return true;
        }
        if (!batch.calculation.isDone()) return true;
        ICraftingPlan plan = batch.calculation.get();
        if (plan.simulation()) {
            failBatch(player, batch, BatchStatus.MISSING_INGREDIENTS, key.getDisplayName().getString());
            return false;
        }
        List<ICraftingCPU> idleBefore = crafting.getCpus().stream().filter(cpu -> !cpu.isBusy()).toList();
        var submitted = crafting.submitJob(plan, null, null, true, source);
        if (!submitted.successful()) {
            if (submitted.errorCode() == CraftingSubmitErrorCode.NO_SUITABLE_CPU_FOUND
                    && submitted.errorDetail() instanceof UnsuitableCpus cpus && cpus.busy() > 0
                    && ++batch.cpuWaits <= 600) return true;
            if (submitted.errorCode() == CraftingSubmitErrorCode.MISSING_INGREDIENT && ++batch.stockRetries <= 2) {
                // Another request changed stock after this calculation. Recalculate, rather
                // than retrying the same already-invalid plan against different ingredients.
                batch.calculation = null;
                return true;
            }
            failBatch(player, batch, BatchStatus.SUBMIT_FAILED,
                    key.getDisplayName().getString() + " · " + submitted.errorCode());
            return false;
        }
        batch.calculation = null;
        batch.cpuWaits = 0;
        batch.stockRetries = 0;
        batch.started++;
        for (ICraftingCPU cpu : idleBefore) {
            if (cpu.isBusy() && cpu instanceof CraftingCPUCluster cluster) {
                batch.activeJob = cluster.craftingLogic.getLastLink();
                if (batch.activeJob != null) break;
            }
        }
        if (batch.activeJob == null) {
            failBatch(player, batch, BatchStatus.ERROR, "Cannot track submitted crafting CPU");
            return false;
        }
        return true;
    }

    private static void failBatch(ServerPlayer player, BatchSession batch, BatchStatus status, String detail) {
        batch.cancelCalculation();
        sendBatchResult(player, batch.request, status, batch.started, batch.requirements.size(), detail);
    }

    private static void serverStopped(ServerStoppedEvent event) {
        BATCHES.values().forEach(BatchSession::cancelCalculation);
        BATCHES.clear();
    }

    private static final class BatchSession {
        private final BatchRequest request;
        private final Map<AEItemKey, Long> requirements;
        private final List<AEItemKey> order;
        private Future<ICraftingPlan> calculation;
        private ICraftingLink activeJob;
        private int index;
        private int started;
        private int reconciliations;
        private int cpuWaits;
        private int stockRetries;

        private BatchSession(BatchRequest request, Map<AEItemKey, Long> requirements, List<AEItemKey> order) {
            this.request = request;
            this.requirements = requirements;
            this.order = order;
        }

        private void cancelCalculation() {
            if (calculation != null) calculation.cancel(true);
            calculation = null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<ResourceLocation, Integer> readPlan(int tier) throws ReflectiveOperationException {
        Class<?> api = Class.forName(PLAN_API);
        Method method = api.getMethod("itemCounts", int.class);
        Object value = method.invoke(null, tier);
        if (!(value instanceof Map<?, ?> raw)) {
            throw new IllegalStateException("Deep Core construction API returned no map");
        }

        Map<ResourceLocation, Integer> result = new LinkedHashMap<>();
        for (var entry : raw.entrySet()) {
            if (!(entry.getKey() instanceof ResourceLocation id) || !(entry.getValue() instanceof Number amount)) {
                throw new IllegalStateException("Invalid Deep Core construction component");
            }
            int count = amount.intValue();
            if (count <= 0) throw new IllegalStateException("Invalid Deep Core construction count");
            result.put(id, count);
        }
        return result;
    }

    private static long inventoryCount(ServerPlayer player, Item item) {
        long total = 0L;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && stack.is(item)) {
                total = saturatingAdd(total, stack.getCount());
            }
        }
        return total;
    }

    private static Map<AEItemKey, Long> storedItems(Iterable<? extends Object2LongMap.Entry<AEKey>> inventory) {
        Map<AEItemKey, Long> result = new LinkedHashMap<>();
        for (Object2LongMap.Entry<AEKey> entry : inventory) {
            if (entry.getKey() instanceof AEItemKey key && entry.getLongValue() > 0L) {
                result.merge(key, entry.getLongValue(), DeepCoreConstructionNetwork::saturatingAdd);
            }
        }
        return result;
    }

    private static long saturatingAdd(long a, long b) {
        if (a >= Long.MAX_VALUE - b) return Long.MAX_VALUE;
        return a + b;
    }

    private static void sendBatchResult(ServerPlayer player, BatchRequest request,
                                        BatchStatus status, int startedJobs, int totalJobs,
                                        String detail) {
        if (player.isRemoved()) return;
        PacketDistributor.sendToPlayer(player, new BatchResult(
                request.dimension(), request.pos(), request.tier(), status,
                startedJobs, totalJobs, detail == null ? "" : detail));
    }

    private static void handleBatchResult(BatchResult response, IPayloadContext context) {
        ClientDeepCoreConstructionState.acceptBatch(response);
    }

    private static void writeText(RegistryFriendlyByteBuf buf, String value) {
        String safe = value == null ? "" : value;
        buf.writeUtf(safe.length() > 160 ? safe.substring(0, 160) : safe, 160);
    }

    private static AEItemKey readItemKey(RegistryFriendlyByteBuf buf) {
        AEKey key = AEKey.STREAM_CODEC.decode(buf);
        if (!(key instanceof AEItemKey itemKey)) {
            throw new IllegalArgumentException("Deep Core construction entry is not an item");
        }
        return itemKey;
    }


    public enum PlanStatus {
        READY,
        MOD_MISSING,
        INVALID_TIER,
        INVALID_LINK,
        NETWORK_OFFLINE,
        PLAN_ERROR
    }

    public record PlanEntry(AEItemKey key, long required, long stored, long missing, boolean craftable) {
        private void write(RegistryFriendlyByteBuf buf) {
            AEKey.STREAM_CODEC.encode(buf, key);
            buf.writeVarLong(required);
            buf.writeVarLong(stored);
            buf.writeVarLong(missing);
            buf.writeBoolean(craftable);
        }

        private static PlanEntry read(RegistryFriendlyByteBuf buf) {
            return new PlanEntry(readItemKey(buf), buf.readVarLong(), buf.readVarLong(),
                    buf.readVarLong(), buf.readBoolean());
        }
    }

    public record PlanSnapshot(PlanStatus status, List<PlanEntry> entries) {
        public boolean canStart() {
            if (status != PlanStatus.READY) return false;
            boolean anyMissing = false;
            for (PlanEntry entry : entries) {
                if (entry.missing() > 0L) {
                    anyMissing = true;
                    if (!entry.craftable()) return false;
                }
            }
            return anyMissing;
        }
    }

    public record PlanRequest(ResourceLocation dimension, BlockPos pos, int tier)
            implements CustomPacketPayload {
        public static final Type<PlanRequest> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath("forever_production_monitor",
                        "request_deepcore_construction_plan"));
        public static final StreamCodec<RegistryFriendlyByteBuf, PlanRequest> CODEC =
                StreamCodec.of((buf, value) -> {
                    ResourceLocation.STREAM_CODEC.encode(buf, value.dimension());
                    buf.writeBlockPos(value.pos());
                    buf.writeVarInt(value.tier());
                }, buf -> new PlanRequest(
                        ResourceLocation.STREAM_CODEC.decode(buf),
                        buf.readBlockPos(),
                        buf.readVarInt()));

        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record PlanResponse(ResourceLocation dimension, BlockPos pos, int tier,
                               PlanSnapshot snapshot) implements CustomPacketPayload {
        public static final Type<PlanResponse> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath("forever_production_monitor",
                        "deepcore_construction_plan"));
        public static final StreamCodec<RegistryFriendlyByteBuf, PlanResponse> CODEC =
                StreamCodec.of((buf, value) -> {
                    ResourceLocation.STREAM_CODEC.encode(buf, value.dimension());
                    buf.writeBlockPos(value.pos());
                    buf.writeVarInt(value.tier());
                    buf.writeEnum(value.snapshot().status());
                    buf.writeVarInt(value.snapshot().entries().size());
                    for (PlanEntry entry : value.snapshot().entries()) entry.write(buf);
                }, buf -> {
                    ResourceLocation dimension = ResourceLocation.STREAM_CODEC.decode(buf);
                    BlockPos pos = buf.readBlockPos();
                    int tier = buf.readVarInt();
                    PlanStatus status = buf.readEnum(PlanStatus.class);
                    int size = buf.readVarInt();
                    if (size < 0 || size > 64) throw new IllegalArgumentException("Invalid construction plan size");
                    List<PlanEntry> entries = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) entries.add(PlanEntry.read(buf));
                    return new PlanResponse(dimension, pos, tier,
                            new PlanSnapshot(status, List.copyOf(entries)));
                });

        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record BatchRequest(ResourceLocation dimension, BlockPos pos, int tier)
            implements CustomPacketPayload {
        public static final Type<BatchRequest> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath("forever_production_monitor",
                        "request_deepcore_construction_batch"));
        public static final StreamCodec<RegistryFriendlyByteBuf, BatchRequest> CODEC =
                StreamCodec.of((buf, value) -> {
                    ResourceLocation.STREAM_CODEC.encode(buf, value.dimension());
                    buf.writeBlockPos(value.pos());
                    buf.writeVarInt(value.tier());
                }, buf -> new BatchRequest(
                        ResourceLocation.STREAM_CODEC.decode(buf),
                        buf.readBlockPos(),
                        buf.readVarInt()));

        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public enum BatchStatus {
        STARTED,
        NOTHING_TO_CRAFT,
        MOD_MISSING,
        INVALID_TIER,
        INVALID_LINK,
        NETWORK_OFFLINE,
        NOT_CRAFTABLE,
        MISSING_INGREDIENTS,
        SUBMIT_FAILED,
        PARTIAL,
        ERROR,
        QUEUED,
        COMPLETE
    }

    public record BatchResult(ResourceLocation dimension, BlockPos pos, int tier,
                              BatchStatus status, int startedJobs, int totalJobs,
                              String detail) implements CustomPacketPayload {
        public static final Type<BatchResult> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath("forever_production_monitor",
                        "deepcore_construction_batch_result"));
        public static final StreamCodec<RegistryFriendlyByteBuf, BatchResult> CODEC =
                StreamCodec.of((buf, value) -> {
                    ResourceLocation.STREAM_CODEC.encode(buf, value.dimension());
                    buf.writeBlockPos(value.pos());
                    buf.writeVarInt(value.tier());
                    buf.writeEnum(value.status());
                    buf.writeVarInt(value.startedJobs());
                    buf.writeVarInt(value.totalJobs());
                    writeText(buf, value.detail());
                }, buf -> new BatchResult(
                        ResourceLocation.STREAM_CODEC.decode(buf),
                        buf.readBlockPos(),
                        buf.readVarInt(),
                        buf.readEnum(BatchStatus.class),
                        buf.readVarInt(),
                        buf.readVarInt(),
                        buf.readUtf(160)));

        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}
