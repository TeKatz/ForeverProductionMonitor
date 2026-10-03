package de.timo.foreverproductionmonitor.integration;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/**
 * Reflection-only bridge to ForeverDeepCore's optional construction API.
 * FPM therefore remains loadable without ForeverDeepCore installed.
 */
public final class DeepCoreConstructionBridge {
    private static final String API = "de.timo.foreverdeepcore.api.DeepCoreConstructionPlans";
    private static final Map<String, List<Placement>> CACHE = new LinkedHashMap<>();

    public record Placement(BlockPos offset, ResourceLocation blockId, ResourceLocation itemId) {}

    public static synchronized List<Placement> placements(int tier, Direction front)
            throws ReflectiveOperationException {
        if (front == null || front.getAxis().isVertical()) {
            throw new IllegalArgumentException("front must be horizontal");
        }
        String cacheKey = tier + ":" + front.getName();
        List<Placement> cached = CACHE.get(cacheKey);
        if (cached != null) return cached;

        Class<?> api = Class.forName(API);
        Method placements = api.getMethod("placements", int.class, Direction.class);
        Object rawResult = placements.invoke(null, tier, front);
        if (!(rawResult instanceof List<?> rawList)) {
            throw new IllegalStateException("Deep Core construction API returned no placement list");
        }

        java.util.ArrayList<Placement> result = new java.util.ArrayList<>(rawList.size());
        for (Object raw : rawList) {
            if (raw == null) throw new IllegalStateException("Null Deep Core placement");
            Method offsetAccessor = raw.getClass().getMethod("offset");
            Method blockAccessor = raw.getClass().getMethod("blockId");
            Method itemAccessor = raw.getClass().getMethod("itemId");
            Object offset = offsetAccessor.invoke(raw);
            Object blockId = blockAccessor.invoke(raw);
            Object itemId = itemAccessor.invoke(raw);
            if (!(offset instanceof BlockPos pos)
                    || !(blockId instanceof ResourceLocation block)
                    || !(itemId instanceof ResourceLocation item)) {
                throw new IllegalStateException("Invalid Deep Core placement entry");
            }
            result.add(new Placement(pos.immutable(), block, item));
        }

        List<Placement> immutable = List.copyOf(result);
        CACHE.put(cacheKey, immutable);
        return immutable;
    }

    private DeepCoreConstructionBridge() {}
}
