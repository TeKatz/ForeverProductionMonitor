package de.timo.foreverproductionmonitor.network;

import appeng.api.config.Actionable;
import appeng.api.orientation.IOrientationStrategy;
import appeng.api.stacks.AEItemKey;
import appeng.core.definitions.AEBlocks;
import appeng.me.helpers.PlayerSource;
import de.timo.foreverproductionmonitor.blockentity.ProductionMonitorBlockEntity;
import de.timo.foreverproductionmonitor.integration.DeepCoreConstructionBridge;
import de.timo.foreverproductionmonitor.item.MultiblockConstructorItem;
import de.timo.foreverproductionmonitor.item.ProductionTabletItem;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Server-authoritative placement pipeline for the Multiblock Constructor.
 *
 * <p>Construction can consume matching block items from the player's inventory
 * and the linked ME network. Existing correct blocks are reused, foreign blocks
 * are never broken, and placement is spread across server ticks.</p>
 */
public final class DeepCoreConstructorNetwork {
    private static final String DEEP_CORE_MOD_ID = "forever_deep_core";
    private static final int BLOCKS_PER_TICK = 4;
    private static final double MAX_BUILD_DISTANCE_SQ = 48.0 * 48.0;
    private static final Map<UUID, BuildSession> SESSIONS = new LinkedHashMap<>();
    private static boolean tickRegistered;

    private DeepCoreConstructorNetwork() {}

    static void register(PayloadRegistrar registrar) {
        registrar.playToServer(BuildRequest.TYPE, BuildRequest.CODEC,
                DeepCoreConstructorNetwork::handleBuildRequest);
        if (!tickRegistered) {
            tickRegistered = true;
            NeoForge.EVENT_BUS.addListener(DeepCoreConstructorNetwork::serverTick);
        }
    }

    public static void requestBuild(ItemStack stack) {
        var link = MultiblockConstructorItem.getLink(stack).orElse(null);
        var target = MultiblockConstructorItem.getTarget(stack).orElse(null);
        if (link == null || target == null) return;
        PacketDistributor.sendToServer(new BuildRequest(
                link.dimension(), link.pos(),
                target.dimension(), target.corePos(), target.front(),
                MultiblockConstructorItem.getTier(stack)));
    }

    public static void dismantle(ServerPlayer player, ItemStack stack, BlockPos clickedPos) {
        if (MultiblockConstructorItem.getMode(stack) != MultiblockConstructorItem.Mode.DISMANTLE) {
            return;
        }
        if (!ModList.get().isLoaded(DEEP_CORE_MOD_ID)) {
            message(player, "message.forever_production_monitor.constructor.deepcore_missing");
            return;
        }
        try {
            int removed = DeepCoreConstructionBridge.dismantleAt(player, clickedPos, stack);
            if (removed <= 0) {
                message(player, "message.forever_production_monitor.constructor.dismantle_not_found");
            } else {
                message(player, "message.forever_production_monitor.constructor.dismantle_complete", removed);
            }
        } catch (Throwable error) {
            message(player, "message.forever_production_monitor.constructor.dismantle_error",
                    error.getClass().getSimpleName());
        }
    }

    private static void handleBuildRequest(BuildRequest request, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            startBuild(player, request);
        });
    }

    private static void startBuild(ServerPlayer player, BuildRequest request) {
        if (SESSIONS.containsKey(player.getUUID())) {
            message(player, "message.forever_production_monitor.constructor.in_progress");
            return;
        }
        if (!ModList.get().isLoaded(DEEP_CORE_MOD_ID)) {
            message(player, "message.forever_production_monitor.constructor.deepcore_missing");
            return;
        }
        if (request.tier() < 1 || request.tier() > 3
                || request.front() == null || request.front().getAxis().isVertical()) {
            message(player, "message.forever_production_monitor.constructor.invalid_plan");
            return;
        }

        ItemStack held = matchingHeldConstructor(player, request);
        if (held.isEmpty()) {
            message(player, "message.forever_production_monitor.constructor.invalid_tool");
            return;
        }
        if (MultiblockConstructorItem.getMode(held) != MultiblockConstructorItem.Mode.BUILD) {
            message(player, "message.forever_production_monitor.constructor.invalid_tool");
            return;
        }

        ServerLevel targetLevel = player.server.getLevel(ResourceKey.create(
                Registries.DIMENSION, request.targetDimension()));
        if (targetLevel == null || player.level() != targetLevel) {
            message(player, "message.forever_production_monitor.constructor.wrong_dimension");
            return;
        }
        if (player.distanceToSqr(
                request.corePos().getX() + 0.5,
                request.corePos().getY() + 0.5,
                request.corePos().getZ() + 0.5) > MAX_BUILD_DISTANCE_SQ) {
            message(player, "message.forever_production_monitor.constructor.too_far");
            return;
        }

        ProductionMonitorBlockEntity monitor = resolveMonitor(player.server,
                request.monitorDimension(), request.monitorPos());
        if (monitor == null) {
            message(player, "message.forever_production_monitor.constructor.invalid_link");
            return;
        }
        if (!monitor.isMonitorOnline() || monitor.getMainNode().getGrid() == null) {
            message(player, "message.forever_production_monitor.constructor.offline");
            return;
        }

        List<DeepCoreConstructionBridge.Placement> plan;
        try {
            plan = DeepCoreConstructionBridge.placements(request.tier(), request.front());
        } catch (Throwable error) {
            message(player, "message.forever_production_monitor.constructor.invalid_plan");
            return;
        }

        var grid = monitor.getMainNode().getGrid();
        var storage = grid.getStorageService().getInventory();
        var source = new PlayerSource(player, monitor);
        Deque<PlacementTask> tasks = new ArrayDeque<>();
        Map<AEItemKey, Long> required = new LinkedHashMap<>();

        for (DeepCoreConstructionBridge.Placement placement : plan) {
            Block block = BuiltInRegistries.BLOCK.getOptional(placement.blockId()).orElse(Blocks.AIR);
            Item item = BuiltInRegistries.ITEM.getOptional(placement.itemId()).orElse(Items.AIR);
            if (block == Blocks.AIR || item == Items.AIR) {
                message(player, "message.forever_production_monitor.constructor.invalid_plan");
                return;
            }

            BlockPos pos = request.corePos().offset(placement.offset());
            if (!targetLevel.hasChunkAt(pos)) {
                message(player, "message.forever_production_monitor.constructor.unloaded",
                        pos.toShortString());
                return;
            }

            BlockState existing = targetLevel.getBlockState(pos);
            if (isCorrectExisting(existing, block, request.front())) continue;
            if (existing.is(block)) {
                // Correct block, wrong orientation. Rotate it in place without
                // consuming another component from inventory or ME storage.
                tasks.addLast(new PlacementTask(pos, block, AEItemKey.of(item), true));
                continue;
            }
            if (!existing.isAir() && !existing.canBeReplaced()) {
                message(player, "message.forever_production_monitor.constructor.blocked",
                        pos.toShortString());
                return;
            }

            AEItemKey key = AEItemKey.of(item);
            tasks.addLast(new PlacementTask(pos, block, key, false));
            required.merge(key, 1L, DeepCoreConstructorNetwork::safeAdd);
        }

        if (tasks.isEmpty()) {
            message(player, "message.forever_production_monitor.constructor.already_complete");
            return;
        }

        for (var entry : required.entrySet()) {
            Item item = entry.getKey().getItem();
            long inventoryAvailable = inventoryCount(player, item);
            long remaining = Math.max(0L, entry.getValue() - inventoryAvailable);
            long meAvailable = remaining == 0L ? 0L
                    : storage.extract(entry.getKey(), remaining, Actionable.SIMULATE, source);
            long available = safeAdd(inventoryAvailable, meAvailable);
            if (available < entry.getValue()) {
                long missing = entry.getValue() - available;
                String name = entry.getKey().getDisplayName().getString();
                message(player, "message.forever_production_monitor.constructor.missing",
                        missing, name);
                return;
            }
        }

        SESSIONS.put(player.getUUID(), new BuildSession(
                player.getUUID(),
                new ProductionTabletItem.MonitorLink(
                        request.monitorDimension(), request.monitorPos()),
                request.targetDimension(),
                request.front(),
                request.tier(),
                tasks,
                tasks.size(),
                0));
        message(player, "message.forever_production_monitor.constructor.started",
                tasks.size(), roman(request.tier()));
    }

    private static void serverTick(ServerTickEvent.Post event) {
        if (SESSIONS.isEmpty()) return;
        MinecraftServer server = event.getServer();

        Iterator<Map.Entry<UUID, BuildSession>> iterator = SESSIONS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, BuildSession> entry = iterator.next();
            BuildSession session = entry.getValue();
            ServerPlayer player = server.getPlayerList().getPlayer(session.playerId());
            if (player == null || player.isRemoved()) {
                iterator.remove();
                continue;
            }

            ServerLevel targetLevel = server.getLevel(ResourceKey.create(
                    Registries.DIMENSION, session.targetDimension()));
            ProductionMonitorBlockEntity monitor = resolveMonitor(
                    server, session.link().dimension(), session.link().pos());
            if (targetLevel == null || monitor == null
                    || !monitor.isMonitorOnline() || monitor.getMainNode().getGrid() == null) {
                message(player, "message.forever_production_monitor.constructor.aborted_network");
                iterator.remove();
                continue;
            }

            var storage = monitor.getMainNode().getGrid().getStorageService().getInventory();
            var source = new PlayerSource(player, monitor);

            boolean aborted = false;
            for (int i = 0; i < BLOCKS_PER_TICK && !session.tasks().isEmpty(); i++) {
                PlacementTask task = session.tasks().removeFirst();
                BlockState existing = targetLevel.getBlockState(task.pos());

                if (isCorrectExisting(existing, task.block(), session.front())) {
                    session.incrementPlaced();
                    continue;
                }

                if (task.rotateOnly()) {
                    if (!existing.is(task.block())) {
                        message(player, "message.forever_production_monitor.constructor.aborted_blocked",
                                task.pos().toShortString());
                        aborted = true;
                        break;
                    }
                    BlockState rotated = orientedState(task.block(), session.front());
                    if (!targetLevel.setBlock(task.pos(), rotated, Block.UPDATE_ALL)) {
                        message(player, "message.forever_production_monitor.constructor.aborted_place",
                                task.pos().toShortString());
                        aborted = true;
                        break;
                    }
                    session.incrementPlaced();
                    continue;
                }

                if (!existing.isAir() && !existing.canBeReplaced()) {
                    message(player, "message.forever_production_monitor.constructor.aborted_blocked",
                            task.pos().toShortString());
                    aborted = true;
                    break;
                }

                MaterialSource materialSource = takeOne(player, storage, source, task.key());
                if (materialSource == MaterialSource.NONE) {
                    message(player, "message.forever_production_monitor.constructor.aborted_missing",
                            task.key().getDisplayName().getString());
                    aborted = true;
                    break;
                }

                BlockState state = orientedState(task.block(), session.front());

                boolean placed = targetLevel.setBlock(task.pos(), state, Block.UPDATE_ALL);
                if (!placed) {
                    refundOne(player, storage, source, task.key(), materialSource);
                    message(player, "message.forever_production_monitor.constructor.aborted_place",
                            task.pos().toShortString());
                    aborted = true;
                    break;
                }

                // Direct world placement must still run the normal post-placement
                // lifecycle. AE2 uses setPlacedBy to initialize ownership/settings
                // for block entities such as the Drive.
                try {
                    BlockState placedState = targetLevel.getBlockState(task.pos());
                    task.block().setPlacedBy(targetLevel, task.pos(), placedState,
                            player, new ItemStack(task.key().getItem()));
                } catch (Throwable error) {
                    targetLevel.removeBlock(task.pos(), false);
                    refundOne(player, storage, source, task.key(), materialSource);
                    message(player, "message.forever_production_monitor.constructor.aborted_place",
                            task.pos().toShortString());
                    aborted = true;
                    break;
                }

                session.incrementPlaced();
            }

            if (aborted) {
                iterator.remove();
                continue;
            }
            if (session.tasks().isEmpty()) {
                message(player, "message.forever_production_monitor.constructor.complete",
                        session.placed(), session.total(), roman(session.tier()));
                iterator.remove();
            }
        }
    }

    private static BlockState orientedState(Block block, Direction front) {
        BlockState state = block.defaultBlockState();
        if (block == AEBlocks.DRIVE.block()) {
            // The open face of the embedded Drive must face the Control Core.
            // In DeepCoreStructure the Control Core is exactly `front` from the Drive.
            state = IOrientationStrategy.get(state).setOrientation(state, front, Direction.UP);
        } else if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, front);
        }
        if (state.hasProperty(BlockStateProperties.LIT)) {
            state = state.setValue(BlockStateProperties.LIT, false);
        }
        return state;
    }

    private static boolean isCorrectExisting(BlockState existing, Block block, Direction front) {
        if (!existing.is(block)) return false;
        if (block == AEBlocks.DRIVE.block()) {
            return IOrientationStrategy.get(existing).getFacing(existing) == front;
        }
        if (existing.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return existing.getValue(BlockStateProperties.HORIZONTAL_FACING) == front;
        }
        return true;
    }

    private static ProductionMonitorBlockEntity resolveMonitor(
            MinecraftServer server, ResourceLocation dimension, BlockPos pos) {
        ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
        if (level == null || !level.hasChunkAt(pos)) return null;
        return level.getBlockEntity(pos) instanceof ProductionMonitorBlockEntity monitor
                ? monitor : null;
    }

    private static ItemStack matchingHeldConstructor(ServerPlayer player, BuildRequest request) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!(stack.getItem() instanceof MultiblockConstructorItem)) continue;
            var link = MultiblockConstructorItem.getLink(stack).orElse(null);
            var target = MultiblockConstructorItem.getTarget(stack).orElse(null);
            if (link == null || target == null) continue;
            if (!link.dimension().equals(request.monitorDimension())
                    || !link.pos().equals(request.monitorPos())
                    || !target.dimension().equals(request.targetDimension())
                    || !target.corePos().equals(request.corePos())
                    || target.front() != request.front()
                    || MultiblockConstructorItem.getTier(stack) != request.tier()
                    || MultiblockConstructorItem.getMode(stack) != MultiblockConstructorItem.Mode.BUILD) continue;
            return stack;
        }
        return ItemStack.EMPTY;
    }

    private static long inventoryCount(ServerPlayer player, Item item) {
        long total = 0L;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && stack.is(item)) {
                total = safeAdd(total, stack.getCount());
            }
        }
        return total;
    }

    private static MaterialSource takeOne(ServerPlayer player,
                                          appeng.api.storage.MEStorage storage,
                                          PlayerSource source, AEItemKey key) {
        Item item = key.getItem();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && stack.is(item)) {
                stack.shrink(1);
                player.getInventory().setChanged();
                return MaterialSource.INVENTORY;
            }
        }

        long simulated = storage.extract(key, 1L, Actionable.SIMULATE, source);
        if (simulated < 1L) return MaterialSource.NONE;
        long extracted = storage.extract(key, 1L, Actionable.MODULATE, source);
        return extracted >= 1L ? MaterialSource.ME : MaterialSource.NONE;
    }

    private static void refundOne(ServerPlayer player,
                                  appeng.api.storage.MEStorage storage,
                                  PlayerSource source, AEItemKey key,
                                  MaterialSource materialSource) {
        if (materialSource == MaterialSource.INVENTORY) {
            player.getInventory().placeItemBackInInventory(new ItemStack(key.getItem()));
        } else if (materialSource == MaterialSource.ME) {
            storage.insert(key, 1L, Actionable.MODULATE, source);
        }
    }

    private enum MaterialSource {
        INVENTORY,
        ME,
        NONE
    }

    private static void message(ServerPlayer player, String key, Object... args) {
        player.displayClientMessage(Component.translatable(key, args), true);
    }

    private static long safeAdd(long a, long b) {
        return a >= Long.MAX_VALUE - b ? Long.MAX_VALUE : a + b;
    }

    private static String roman(int tier) {
        return switch (tier) {
            case 2 -> "II";
            case 3 -> "III";
            default -> "I";
        };
    }

    private record PlacementTask(BlockPos pos, Block block, AEItemKey key, boolean rotateOnly) {}

    private static final class BuildSession {
        private final UUID playerId;
        private final ProductionTabletItem.MonitorLink link;
        private final ResourceLocation targetDimension;
        private final Direction front;
        private final int tier;
        private final Deque<PlacementTask> tasks;
        private final int total;
        private int placed;

        private BuildSession(UUID playerId, ProductionTabletItem.MonitorLink link,
                             ResourceLocation targetDimension, Direction front, int tier,
                             Deque<PlacementTask> tasks, int total, int placed) {
            this.playerId = playerId;
            this.link = link;
            this.targetDimension = targetDimension;
            this.front = front;
            this.tier = tier;
            this.tasks = tasks;
            this.total = total;
            this.placed = placed;
        }

        UUID playerId() { return playerId; }
        ProductionTabletItem.MonitorLink link() { return link; }
        ResourceLocation targetDimension() { return targetDimension; }
        Direction front() { return front; }
        int tier() { return tier; }
        Deque<PlacementTask> tasks() { return tasks; }
        int total() { return total; }
        int placed() { return placed; }
        void incrementPlaced() { placed++; }
    }

    public record BuildRequest(ResourceLocation monitorDimension, BlockPos monitorPos,
                               ResourceLocation targetDimension, BlockPos corePos,
                               Direction front, int tier) implements CustomPacketPayload {
        public static final Type<BuildRequest> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(
                        "forever_production_monitor", "deepcore_constructor_build"));
        public static final StreamCodec<RegistryFriendlyByteBuf, BuildRequest> CODEC =
                StreamCodec.of((buf, value) -> {
                    ResourceLocation.STREAM_CODEC.encode(buf, value.monitorDimension());
                    buf.writeBlockPos(value.monitorPos());
                    ResourceLocation.STREAM_CODEC.encode(buf, value.targetDimension());
                    buf.writeBlockPos(value.corePos());
                    buf.writeEnum(value.front());
                    buf.writeVarInt(value.tier());
                }, buf -> new BuildRequest(
                        ResourceLocation.STREAM_CODEC.decode(buf),
                        buf.readBlockPos(),
                        ResourceLocation.STREAM_CODEC.decode(buf),
                        buf.readBlockPos(),
                        buf.readEnum(Direction.class),
                        buf.readVarInt()));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
