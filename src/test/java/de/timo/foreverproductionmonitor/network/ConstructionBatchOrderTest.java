package de.timo.foreverproductionmonitor.network;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Runs without Minecraft/bootstrap and fails the Gradle check task on regression. */
public final class ConstructionBatchOrderTest {
    public static void main(String[] args) {
        var order = ConstructionBatchOrder.consumerFirst(
                List.of("frame", "casing", "cooler"),
                key -> switch (key) {
                    case "cooler" -> List.of("frame", "casing");
                    case "casing" -> List.of("frame");
                    default -> List.of();
                });
        require(order.equals(List.of("cooler", "casing", "frame")), order.toString());

        // Four frames are final kit members, but the casing also consumes two.
        // Re-read real stock after crafting the consumer and replenish that deficit.
        Map<String, Integer> stock = new HashMap<>(Map.of("frame", 4, "casing", 0));
        for (String key : ConstructionBatchOrder.consumerFirst(
                List.of("frame", "casing"),
                key -> key.equals("casing") ? List.of("frame") : List.of())) {
            int required = key.equals("frame") ? 4 : 1;
            int missing = Math.max(0, required - stock.get(key));
            if (missing > 0 && key.equals("casing")) stock.compute("frame", (k, n) -> n - 2);
            stock.merge(key, missing, Integer::sum);
        }
        require(stock.get("frame") == 4 && stock.get("casing") == 1, stock.toString());

        var cyclic = ConstructionBatchOrder.consumerFirst(List.of("a", "b", "a"),
                key -> List.of(key.equals("a") ? "b" : "a", key));
        require(cyclic.equals(List.of("a", "b")), "Cycles/duplicates must be bounded: " + cyclic);
        require(ConstructionBatchOrder.consumerFirst(List.<String>of(), key -> List.of()).isEmpty(),
                "Empty kit");
        System.out.println("Construction batch ordering and shared-component regressions passed");
    }

    private static void require(boolean valid, String message) {
        if (!valid) throw new AssertionError(message);
    }
}
