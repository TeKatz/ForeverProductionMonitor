package de.timo.foreverproductionmonitor.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/** Consumer components are crafted before the components they may consume. */
final class ConstructionBatchOrder {
    private ConstructionBatchOrder() {}

    static <K> List<K> consumerFirst(Collection<K> components,
                                    Function<K, ? extends Collection<K>> dependencies) {
        Set<K> keys = new LinkedHashSet<>(components);
        Map<K, Set<K>> edges = new LinkedHashMap<>();
        Map<K, Integer> incoming = new LinkedHashMap<>();
        keys.forEach(key -> incoming.put(key, 0));
        for (K key : keys) {
            Set<K> inputs = new LinkedHashSet<>(dependencies.apply(key));
            inputs.retainAll(keys);
            inputs.remove(key);
            edges.put(key, inputs);
            inputs.forEach(input -> incoming.compute(input, (ignored, count) -> count + 1));
        }
        List<K> ready = new ArrayList<>();
        incoming.forEach((key, count) -> { if (count == 0) ready.add(key); });
        List<K> result = new ArrayList<>();
        for (int index = 0; index < ready.size(); index++) {
            K key = ready.get(index);
            result.add(key);
            for (K input : edges.get(key)) {
                if (incoming.compute(input, (ignored, count) -> count - 1) == 0) ready.add(input);
            }
        }
        // Alternative/conversion recipes can form cycles. Keep a deterministic bounded
        // order; the live-stock reconciliation pass handles anything consumed later.
        keys.stream().filter(key -> !result.contains(key)).forEach(result::add);
        return List.copyOf(result);
    }
}
