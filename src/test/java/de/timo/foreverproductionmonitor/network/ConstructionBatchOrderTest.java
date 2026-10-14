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
        // Screenshot regression: the first accepted addon job exposes no native CPU.
        // It must wait for stock and then continue, rather than fail after submission.
        var opaque = new ConstructionJobProgress(1, 0, 100);
        require(opaque.observe(0, false, false, false, 110)
                == ConstructionJobProgress.State.WAITING, "Opaque job must remain queued");
        require(opaque.observe(1, false, false, false, 120)
                == ConstructionJobProgress.State.READY, "Delivered opaque job must advance");
        var immediate = new ConstructionJobProgress(1, 1, 100);
        require(immediate.observe(1, false, false, false, 100)
                == ConstructionJobProgress.State.READY, "Synchronous addon completion");
        var partial = new ConstructionJobProgress(6, 0, 0);
        require(partial.observe(2, false, false, false, 100)
                == ConstructionJobProgress.State.WAITING, "Partial output is not completion");
        require(partial.observe(2, false, false, false, 12100)
                == ConstructionJobProgress.State.STALLED, "Unobservable jobs must not wait forever");
        var nativeJob = new ConstructionJobProgress(1, 0, 0);
        require(nativeJob.observe(1, true, false, false, 100)
                == ConstructionJobProgress.State.WAITING, "Do not overlap an observable running job");
        require(nativeJob.observe(0, true, false, false, 20000)
                == ConstructionJobProgress.State.WAITING, "Observed long jobs must not time out");
        require(nativeJob.observe(0, false, false, true, 20010)
                == ConstructionJobProgress.State.CANCELLED, "Cancellation must stop the batch");
        require(nativeJob.observe(0, false, true, false, 20020)
                == ConstructionJobProgress.State.READY, "Known completion must advance");
        System.out.println("Construction batch ordering, shared-component and opaque-CPU regressions passed");
    }

    private static void require(boolean valid, String message) {
        if (!valid) throw new AssertionError(message);
    }
}
