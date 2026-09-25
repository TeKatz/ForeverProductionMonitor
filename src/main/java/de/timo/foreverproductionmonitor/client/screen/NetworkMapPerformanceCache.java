package de.timo.foreverproductionmonitor.client.screen;

import de.timo.foreverproductionmonitor.network.MonitorNetwork;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;

/**
 * Derived Network Map data that is expensive to rebuild every render frame.
 *
 * The snapshot itself remains the source of truth. This cache is rebuilt only when
 * the snapshot, search text or map filter changes.
 */
final class NetworkMapPerformanceCache {
    private static final int CABLE_LOAD_RADIUS = 6;

    private final ArrayList<Integer> visibleNodeIndices = new ArrayList<>();
    private final HashSet<BlockPos> visiblePositions = new HashSet<>();
    private final HashMap<Long, Integer> cableLoads = new HashMap<>();

    void rebuild(MonitorNetwork.NetworkMapPayload snapshot,
                 Predicate<MonitorNetwork.MapNode> visibilityPredicate) {
        this.visibleNodeIndices.clear();
        this.visiblePositions.clear();
        this.cableLoads.clear();

        if (snapshot == null) {
            return;
        }

        List<MonitorNetwork.MapNode> nodes = snapshot.nodes();
        for (int index = 0; index < nodes.size(); ++index) {
            MonitorNetwork.MapNode node = nodes.get(index);
            if (visibilityPredicate.test(node)) {
                this.visibleNodeIndices.add(index);
                this.visiblePositions.add(node.pos());
            }
        }

        this.rebuildCableLoads(nodes);
    }

    void clear() {
        this.visibleNodeIndices.clear();
        this.visiblePositions.clear();
        this.cableLoads.clear();
    }

    List<Integer> visibleNodeIndices() {
        return this.visibleNodeIndices;
    }

    Set<BlockPos> visiblePositions() {
        return this.visiblePositions;
    }

    int cableLoad(BlockPos pos) {
        return this.cableLoads.getOrDefault(pos.asLong(), 0);
    }

    private void rebuildCableLoads(List<MonitorNetwork.MapNode> nodes) {
        HashSet<Long> cablePositions = new HashSet<>();
        for (MonitorNetwork.MapNode node : nodes) {
            if (!isCable(node)) {
                continue;
            }
            long packed = node.pos().asLong();
            cablePositions.add(packed);
            this.cableLoads.put(packed, 0);
        }

        if (cablePositions.isEmpty()) {
            return;
        }

        // Previous implementation compared every cable with every channel-using node.
        // For large maps that becomes O(cables * devices). The radius is fixed at six
        // blocks, so walking the small Manhattan neighbourhood around each device turns
        // the work into O(devices * radius^3) hash lookups instead.
        for (MonitorNetwork.MapNode node : nodes) {
            if (node.channels() <= 0 || isCable(node)) {
                continue;
            }

            BlockPos pos = node.pos();
            int channels = node.channels();
            for (int dx = -CABLE_LOAD_RADIUS; dx <= CABLE_LOAD_RADIUS; ++dx) {
                int remainingAfterX = CABLE_LOAD_RADIUS - Math.abs(dx);
                for (int dy = -remainingAfterX; dy <= remainingAfterX; ++dy) {
                    int remainingAfterY = remainingAfterX - Math.abs(dy);
                    for (int dz = -remainingAfterY; dz <= remainingAfterY; ++dz) {
                        long packed = BlockPos.asLong(
                                pos.getX() + dx,
                                pos.getY() + dy,
                                pos.getZ() + dz);
                        if (!cablePositions.contains(packed)) {
                            continue;
                        }
                        this.cableLoads.merge(packed, channels, Integer::sum);
                    }
                }
            }
        }
    }

    private static boolean isCable(MonitorNetwork.MapNode node) {
        String path = node.visualId().getPath();
        return path.contains("cable") && !path.contains("bus");
    }
}
