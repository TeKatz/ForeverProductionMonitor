package de.timo.foreverproductionmonitor.client;

import de.timo.foreverproductionmonitor.blockentity.AssemblerMatrixPatternImporter;
import de.timo.foreverproductionmonitor.item.ProductionTabletItem;
import de.timo.foreverproductionmonitor.network.PatternImportNetwork;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Client-only, session-local queue for JEI pattern imports.
 *
 * <p>The queue deliberately reuses the already-tested single recipe upload packet for every entry.
 * Server-side recipe validation, duplicate detection and Matrix insertion therefore remain exactly
 * the same as for a normal one-click import.</p>
 */
public final class ClientPatternImportQueue {
    private static final int MAX_QUEUED = 128;
    private static final Set<ResourceLocation> QUEUED = new LinkedHashSet<>();
    private static final Set<ResourceLocation> PENDING = new LinkedHashSet<>();
    private static final Map<AssemblerMatrixPatternImporter.Result, Integer> RESULTS =
            new EnumMap<>(AssemblerMatrixPatternImporter.Result.class);

    private static Object sessionConnection;
    private static int batchTotal;

    private ClientPatternImportQueue() {}

    public static void toggleAndNotify(ResourceLocation recipeId) {
        syncSession();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || recipeId == null) return;

        if (QUEUED.remove(recipeId)) {
            minecraft.player.displayClientMessage(
                    Component.literal("Removed from pattern queue · " + QUEUED.size() + " queued")
                            .withStyle(ChatFormatting.YELLOW), true);
            return;
        }

        if (QUEUED.size() >= MAX_QUEUED) {
            minecraft.player.displayClientMessage(
                    Component.literal("Pattern queue is full (" + MAX_QUEUED + ")")
                            .withStyle(ChatFormatting.RED), true);
            return;
        }

        QUEUED.add(recipeId);
        minecraft.player.displayClientMessage(
                Component.literal("Added to pattern queue · " + QUEUED.size() + " queued")
                        .withStyle(ChatFormatting.AQUA), true);
    }

    public static boolean contains(ResourceLocation recipeId) {
        syncSession();
        return QUEUED.contains(recipeId);
    }

    public static int size() {
        syncSession();
        return QUEUED.size();
    }

    public static boolean isBatchActive() {
        syncSession();
        return !PENDING.isEmpty();
    }

    public static void uploadQueued(ProductionTabletItem.MonitorLink link) {
        syncSession();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || link == null) return;

        if (!PENDING.isEmpty()) {
            minecraft.player.displayClientMessage(
                    Component.literal("A pattern queue upload is already running")
                            .withStyle(ChatFormatting.YELLOW), true);
            return;
        }
        if (QUEUED.isEmpty()) {
            minecraft.player.displayClientMessage(
                    Component.literal("Pattern queue is empty").withStyle(ChatFormatting.YELLOW), true);
            return;
        }

        PENDING.addAll(QUEUED);
        QUEUED.clear();
        RESULTS.clear();
        batchTotal = PENDING.size();

        minecraft.player.displayClientMessage(
                Component.literal("Uploading " + batchTotal + " queued patterns…")
                        .withStyle(ChatFormatting.AQUA), true);

        for (ResourceLocation recipeId : Set.copyOf(PENDING)) {
            PatternImportNetwork.request(link, recipeId);
        }
    }

    /**
     * @return true when the result belonged to the currently running queue and was consumed here.
     */
    public static boolean acceptBatchResult(ResourceLocation recipeId,
                                            AssemblerMatrixPatternImporter.Result result) {
        syncSession();
        if (recipeId == null || result == null || !PENDING.remove(recipeId)) return false;

        RESULTS.merge(result, 1, Integer::sum);
        if (PENDING.isEmpty()) finishBatch();
        return true;
    }

    private static void finishBatch() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            RESULTS.clear();
            batchTotal = 0;
            return;
        }

        int uploaded = count(AssemblerMatrixPatternImporter.Result.UPLOADED);
        int duplicates = count(AssemblerMatrixPatternImporter.Result.ALREADY_EXISTS);
        int failed = Math.max(0, batchTotal - uploaded - duplicates);

        ChatFormatting summaryColor = failed > 0 ? ChatFormatting.YELLOW : ChatFormatting.GREEN;
        minecraft.player.displayClientMessage(
                Component.literal("Pattern queue complete · " + uploaded + " uploaded · "
                                + duplicates + " duplicates"
                                + (failed > 0 ? " · " + failed + " failed" : ""))
                        .withStyle(summaryColor), true);

        if (failed > 0) {
            StringBuilder detail = new StringBuilder("Pattern queue failures: ");
            boolean first = true;
            for (var entry : RESULTS.entrySet()) {
                if (entry.getKey() == AssemblerMatrixPatternImporter.Result.UPLOADED
                        || entry.getKey() == AssemblerMatrixPatternImporter.Result.ALREADY_EXISTS) continue;
                if (!first) detail.append(" · ");
                detail.append(label(entry.getKey())).append(" × ").append(entry.getValue());
                first = false;
            }
            minecraft.player.displayClientMessage(
                    Component.literal(detail.toString()).withStyle(ChatFormatting.RED), false);
        }

        RESULTS.clear();
        batchTotal = 0;
    }

    private static int count(AssemblerMatrixPatternImporter.Result result) {
        return RESULTS.getOrDefault(result, 0);
    }

    private static String label(AssemblerMatrixPatternImporter.Result result) {
        return switch (result) {
            case NO_BLANK_PATTERN -> "no Blank Pattern";
            case NO_ASSEMBLER_MATRIX -> "no Assembler Matrix";
            case MATRIX_FULL -> "Matrix full";
            case UNSUPPORTED_RECIPE -> "unsupported recipe";
            case RECIPE_NOT_FOUND -> "recipe missing";
            case INVALID_LINK -> "invalid link";
            case NETWORK_OFFLINE -> "network offline";
            case ENCODE_FAILED -> "encode failed";
            case UPLOADED -> "uploaded";
            case ALREADY_EXISTS -> "duplicate";
        };
    }

    private static void syncSession() {
        Object connection = Minecraft.getInstance().getConnection();
        if (connection == sessionConnection) return;

        sessionConnection = connection;
        QUEUED.clear();
        PENDING.clear();
        RESULTS.clear();
        batchTotal = 0;
    }
}
