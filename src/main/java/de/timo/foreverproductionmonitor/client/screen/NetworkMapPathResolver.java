package de.timo.foreverproductionmonitor.client.screen;

import de.timo.foreverproductionmonitor.network.MonitorNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Resolves navigable links shown by the Network Map.
 *
 * Keeping link-specific rules out of the renderer prevents Quantum Bridge and optional
 * integration details from growing inside NetworkMapView.
 */
final class NetworkMapPathResolver {
    enum Type {
        QUANTUM,
        WIRELESS
    }

    record Target(ResourceLocation dimension, BlockPos pos, Type type) {
    }

    private NetworkMapPathResolver() {
    }

    static Target resolve(MonitorNetwork.NetworkMapPayload snapshot,
                          MonitorNetwork.MapNode selected) {
        if (snapshot == null || selected == null) {
            return null;
        }

        ResourceLocation currentDimension = snapshot.viewDimension();

        if (isAe2QuantumBridgePart(selected)) {
            for (MonitorNetwork.QuantumLink link : snapshot.quantumLinks()) {
                if (currentDimension.equals(link.dimensionA())
                        && sameBridge(selected.pos(), link.posA())) {
                    return new Target(link.dimensionB(), link.posB(), Type.QUANTUM);
                }
                if (currentDimension.equals(link.dimensionB())
                        && sameBridge(selected.pos(), link.posB())) {
                    return new Target(link.dimensionA(), link.posA(), Type.QUANTUM);
                }
            }
        }

        if (isExtendedAeWirelessConnector(selected)) {
            for (MonitorNetwork.WirelessLink link : snapshot.wirelessLinks()) {
                if (!currentDimension.equals(link.dimension())) {
                    continue;
                }
                if (selected.pos().equals(link.posA())) {
                    return new Target(currentDimension, link.posB(), Type.WIRELESS);
                }
                if (selected.pos().equals(link.posB())) {
                    return new Target(currentDimension, link.posA(), Type.WIRELESS);
                }
            }
        }

        return null;
    }

    private static boolean isAe2QuantumBridgePart(MonitorNetwork.MapNode node) {
        BlockState state = Block.stateById(node.blockStateId());
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return "ae2".equals(blockId.getNamespace())
                && ("quantum_link".equals(blockId.getPath())
                || "quantum_ring".equals(blockId.getPath()));
    }

    private static boolean isExtendedAeWirelessConnector(MonitorNetwork.MapNode node) {
        BlockState state = Block.stateById(node.blockStateId());
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return "extendedae".equals(blockId.getNamespace())
                && "wireless_connect".equals(blockId.getPath());
    }

    private static boolean sameBridge(BlockPos selected, BlockPos center) {
        return Math.abs(selected.getX() - center.getX()) <= 1
                && Math.abs(selected.getY() - center.getY()) <= 1
                && Math.abs(selected.getZ() - center.getZ()) <= 1;
    }
}
