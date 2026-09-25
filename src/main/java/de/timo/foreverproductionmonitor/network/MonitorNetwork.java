/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  appeng.api.stacks.AEFluidKey
 *  appeng.api.stacks.AEItemKey
 *  appeng.api.stacks.AEKey
 *  appeng.core.definitions.AEBlocks
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.neoforged.neoforge.network.PacketDistributor
 *  net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
 *  net.neoforged.neoforge.network.handling.IPayloadContext
 *  net.neoforged.neoforge.network.registration.PayloadRegistrar
 */
package de.timo.foreverproductionmonitor.network;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.core.definitions.AEBlocks;
import de.timo.foreverproductionmonitor.blockentity.ProductionMonitorBlockEntity;
import de.timo.foreverproductionmonitor.client.ClientMonitorState;
import de.timo.foreverproductionmonitor.client.DeviceLocator;
import de.timo.foreverproductionmonitor.integration.TabletCurios;
import de.timo.foreverproductionmonitor.item.ProductionTabletItem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class MonitorNetwork {
    public static final int MAX_PAGE_SIZE = 18;
    public static final int MAX_HUD_ENTRIES = 10;

    private MonitorNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent registerPayloadHandlersEvent) {
        PayloadRegistrar payloadRegistrar = registerPayloadHandlersEvent.registrar("2.0");
        payloadRegistrar.playToServer(RequestSnapshot.TYPE, RequestSnapshot.STREAM_CODEC, MonitorNetwork::handleRequest);
        payloadRegistrar.playToClient(MonitorSnapshot.TYPE, MonitorSnapshot.STREAM_CODEC, MonitorNetwork::handleSnapshot);
        payloadRegistrar.playToServer(RequestStatistics.TYPE, RequestStatistics.STREAM_CODEC, MonitorNetwork::handleStatisticsRequest);
        payloadRegistrar.playToClient(StatisticsSnapshot.TYPE, StatisticsSnapshot.STREAM_CODEC, MonitorNetwork::handleStatisticsSnapshot);
        payloadRegistrar.playToServer(RequestHud.TYPE, RequestHud.STREAM_CODEC, MonitorNetwork::handleHudRequest);
        payloadRegistrar.playToClient(HudSnapshot.TYPE, HudSnapshot.STREAM_CODEC, MonitorNetwork::handleHudSnapshot);
        payloadRegistrar.playToServer(RequestLocateDevice.TYPE, RequestLocateDevice.STREAM_CODEC, MonitorNetwork::handleLocateRequest);
        payloadRegistrar.playToClient(LocateDeviceResult.TYPE, LocateDeviceResult.STREAM_CODEC, MonitorNetwork::handleLocateResult);
        payloadRegistrar.playToServer(RequestDashboard.TYPE, RequestDashboard.STREAM_CODEC, MonitorNetwork::handleDashboardRequest);
        payloadRegistrar.playToClient(DashboardSnapshot.TYPE, DashboardSnapshot.STREAM_CODEC, MonitorNetwork::handleDashboardSnapshot);
        payloadRegistrar.playToServer(UpdateDashboardPin.TYPE, UpdateDashboardPin.STREAM_CODEC, MonitorNetwork::handleDashboardUpdate);
        payloadRegistrar.playToServer(RequestNetworkMap.TYPE, RequestNetworkMap.STREAM_CODEC, MonitorNetwork::handleNetworkMapRequest);
        payloadRegistrar.playToClient(NetworkMapPayload.TYPE, NetworkMapPayload.STREAM_CODEC, MonitorNetwork::handleNetworkMapSnapshot);
    }

    public static void request(ProductionTabletItem.MonitorLink monitorLink, String string, SortMode sortMode, int n, int n2, int n3) {
        PacketDistributor.sendToServer((CustomPacketPayload)new RequestSnapshot(monitorLink.dimension(), monitorLink.pos(), string, sortMode, n, n2, n3), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    public static void requestHud(HudMode hudMode, int n, int n2) {
        PacketDistributor.sendToServer((CustomPacketPayload)new RequestHud(hudMode, n, n2), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    public static void requestStatistics(ProductionTabletItem.MonitorLink monitorLink, StatisticsPage statisticsPage, String string, int n, int n2, DeviceSort deviceSort, DeviceFilter deviceFilter) {
        PacketDistributor.sendToServer((CustomPacketPayload)new RequestStatistics(monitorLink.dimension(), monitorLink.pos(), statisticsPage, string, n, n2, deviceSort, deviceFilter), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    public static void requestLocate(ProductionTabletItem.MonitorLink monitorLink, DeviceGroup deviceGroup, int n) {
        ResourceLocation resourceLocation = BuiltInRegistries.ITEM.getKey(deviceGroup.visual().getItem());
        PacketDistributor.sendToServer((CustomPacketPayload)new RequestLocateDevice(monitorLink.dimension(), monitorLink.pos(), resourceLocation, deviceGroup.name(), Math.max(0, n)), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    public static void requestDashboard(ProductionTabletItem.MonitorLink monitorLink) {
        PacketDistributor.sendToServer((CustomPacketPayload)new RequestDashboard(monitorLink.dimension(), monitorLink.pos()), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    public static void requestNetworkMap(ProductionTabletItem.MonitorLink monitorLink) {
        requestNetworkMap(monitorLink, monitorLink.dimension());
    }

    public static void requestNetworkMap(ProductionTabletItem.MonitorLink monitorLink, ResourceLocation viewDimension) {
        PacketDistributor.sendToServer((CustomPacketPayload)new RequestNetworkMap(
                monitorLink.dimension(), monitorLink.pos(), viewDimension), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    public static void updateDashboardPin(ProductionTabletItem.MonitorLink monitorLink, DashboardAction dashboardAction, EntryKind entryKind, AEKey aEKey, ProductionMonitorBlockEntity.AlarmMode alarmMode, long l, int n, long l2) {
        PacketDistributor.sendToServer((CustomPacketPayload)new UpdateDashboardPin(monitorLink.dimension(), monitorLink.pos(), dashboardAction, entryKind, aEKey, alarmMode, l, n, l2), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    private static ProductionMonitorBlockEntity linkedMonitor(ServerPlayer serverPlayer, ResourceLocation resourceLocation, BlockPos blockPos) {
        ProductionMonitorBlockEntity productionMonitorBlockEntity;
        if (!MonitorNetwork.hasMatchingTablet(serverPlayer, resourceLocation, blockPos)) {
            return null;
        }
        ServerLevel serverLevel = serverPlayer.server.getLevel(ResourceKey.create((ResourceKey)Registries.DIMENSION, (ResourceLocation)resourceLocation));
        if (serverLevel == null || !serverLevel.hasChunkAt(blockPos)) {
            return null;
        }
        BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
        return blockEntity instanceof ProductionMonitorBlockEntity ? (productionMonitorBlockEntity = (ProductionMonitorBlockEntity)blockEntity) : null;
    }

    private static void handleDashboardRequest(RequestDashboard requestDashboard, IPayloadContext iPayloadContext) {
        Object object = iPayloadContext.player();
        if (!(object instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)object;
        object = MonitorNetwork.linkedMonitor(serverPlayer, requestDashboard.dimension(), requestDashboard.pos());
        if (object == null || !((ProductionMonitorBlockEntity)((Object)object)).isMonitorOnline()) {
            PacketDistributor.sendToPlayer((ServerPlayer)serverPlayer, (CustomPacketPayload)new DashboardSnapshot(requestDashboard.dimension(), requestDashboard.pos(), object == null ? Status.MONITOR_MISSING : Status.NETWORK_OFFLINE, List.of()), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        ServerLevel serverLevel = (ServerLevel)((ProductionMonitorBlockEntity)object).getLevel();
        List<DashboardEntry> list = ((ProductionMonitorBlockEntity)((Object)object)).dashboardSnapshot(serverLevel.getGameTime()).stream().map(dashboardEntrySnapshot -> new DashboardEntry(MonitorNetwork.fromDashboardKind(dashboardEntrySnapshot.kind()), dashboardEntrySnapshot.key(), dashboardEntrySnapshot.amount(), dashboardEntrySnapshot.currentPerMinute(), dashboardEntrySnapshot.averagePerMinute(), dashboardEntrySnapshot.secondsSinceChange(), dashboardEntrySnapshot.infinite(), dashboardEntrySnapshot.rule().mode(), dashboardEntrySnapshot.rule().threshold(), dashboardEntrySnapshot.rule().delaySeconds(), dashboardEntrySnapshot.rule().hysteresis(), dashboardEntrySnapshot.state())).toList();
        Status status = ((ProductionMonitorBlockEntity)((Object)object)).isWarmingUp() ? Status.WARMING_UP : Status.ONLINE;
        PacketDistributor.sendToPlayer((ServerPlayer)serverPlayer, (CustomPacketPayload)new DashboardSnapshot(requestDashboard.dimension(), requestDashboard.pos(), status, list), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    private static void handleNetworkMapRequest(RequestNetworkMap requestNetworkMap, IPayloadContext iPayloadContext) {
        Object object = iPayloadContext.player();
        if (!(object instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)object;
        object = MonitorNetwork.linkedMonitor(serverPlayer, requestNetworkMap.dimension(), requestNetworkMap.pos());
        if (object == null || !((ProductionMonitorBlockEntity)((Object)object)).isMonitorOnline()) {
            PacketDistributor.sendToPlayer((ServerPlayer)serverPlayer,
                    (CustomPacketPayload)new NetworkMapPayload(
                            requestNetworkMap.dimension(),
                            requestNetworkMap.pos(),
                            requestNetworkMap.viewDimension(),
                            object == null ? Status.MONITOR_MISSING : Status.NETWORK_OFFLINE,
                            false,
                            List.of(requestNetworkMap.viewDimension()),
                            List.of(),
                            List.of(),
                            List.of()),
                    (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }

        ProductionMonitorBlockEntity.NetworkMapSnapshot networkMapSnapshot =
                ((ProductionMonitorBlockEntity)((Object)object)).networkMapSnapshot(requestNetworkMap.viewDimension());
        List<MapNode> nodes = networkMapSnapshot.nodes().stream().map(MapNode::from).toList();
        List<QuantumLink> quantumLinks = networkMapSnapshot.quantumLinks().stream().map(QuantumLink::from).toList();
        List<WirelessLink> wirelessLinks = networkMapSnapshot.wirelessLinks().stream().map(WirelessLink::from).toList();
        Status status = ((ProductionMonitorBlockEntity)((Object)object)).isWarmingUp()
                ? Status.WARMING_UP : Status.ONLINE;

        PacketDistributor.sendToPlayer((ServerPlayer)serverPlayer,
                (CustomPacketPayload)new NetworkMapPayload(
                        requestNetworkMap.dimension(),
                        requestNetworkMap.pos(),
                        networkMapSnapshot.viewDimension(),
                        status,
                        networkMapSnapshot.truncated(),
                        networkMapSnapshot.dimensions(),
                        quantumLinks,
                        wirelessLinks,
                        nodes),
                (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    private static void handleDashboardUpdate(UpdateDashboardPin updateDashboardPin, IPayloadContext iPayloadContext) {
        Object object = iPayloadContext.player();
        if (!(object instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)object;
        object = MonitorNetwork.linkedMonitor(serverPlayer, updateDashboardPin.dimension(), updateDashboardPin.pos());
        if (object == null) {
            return;
        }
        ProductionMonitorBlockEntity.DashboardKind dashboardKind = MonitorNetwork.toDashboardKind(updateDashboardPin.kind());
        ProductionMonitorBlockEntity.AlarmRule alarmRule = new ProductionMonitorBlockEntity.AlarmRule(updateDashboardPin.alarmMode(), updateDashboardPin.threshold(), updateDashboardPin.delaySeconds(), updateDashboardPin.hysteresis());
        switch (updateDashboardPin.action()) {
            case ADD: {
                ((ProductionMonitorBlockEntity)((Object)object)).addDashboardPin(dashboardKind, updateDashboardPin.key());
                break;
            }
            case REMOVE: {
                ((ProductionMonitorBlockEntity)((Object)object)).removeDashboardPin(dashboardKind, updateDashboardPin.key());
                break;
            }
            case UPDATE: {
                ((ProductionMonitorBlockEntity)((Object)object)).updateDashboardPin(dashboardKind, updateDashboardPin.key(), alarmRule);
                break;
            }
            case MOVE_UP: {
                ((ProductionMonitorBlockEntity)((Object)object)).moveDashboardPin(dashboardKind, updateDashboardPin.key(), -1);
                break;
            }
            case MOVE_DOWN: {
                ((ProductionMonitorBlockEntity)((Object)object)).moveDashboardPin(dashboardKind, updateDashboardPin.key(), 1);
            }
        }
        MonitorNetwork.handleDashboardRequest(new RequestDashboard(updateDashboardPin.dimension(), updateDashboardPin.pos()), iPayloadContext);
    }

    private static ProductionMonitorBlockEntity.DashboardKind toDashboardKind(EntryKind entryKind) {
        return switch (entryKind) {
            default -> throw new IncompatibleClassChangeError();
            case EntryKind.ITEM -> ProductionMonitorBlockEntity.DashboardKind.ITEM;
            case EntryKind.FLUID -> ProductionMonitorBlockEntity.DashboardKind.FLUID;
            case EntryKind.ENERGY -> ProductionMonitorBlockEntity.DashboardKind.ENERGY;
        };
    }

    private static EntryKind fromDashboardKind(ProductionMonitorBlockEntity.DashboardKind dashboardKind) {
        return switch (dashboardKind) {
            default -> throw new IncompatibleClassChangeError();
            case ProductionMonitorBlockEntity.DashboardKind.ITEM -> EntryKind.ITEM;
            case ProductionMonitorBlockEntity.DashboardKind.FLUID -> EntryKind.FLUID;
            case ProductionMonitorBlockEntity.DashboardKind.ENERGY -> EntryKind.ENERGY;
        };
    }

    private static void handleLocateRequest(RequestLocateDevice requestLocateDevice, IPayloadContext iPayloadContext) {
        ProductionMonitorBlockEntity productionMonitorBlockEntity;
        Object object;
        ServerPlayer serverPlayer;
        Player player = iPayloadContext.player();
        if (!(player instanceof ServerPlayer) || !MonitorNetwork.hasMatchingTablet(serverPlayer = (ServerPlayer)player, requestLocateDevice.dimension(), requestLocateDevice.monitorPos())) {
            return;
        }
        ResourceKey<net.minecraft.world.level.Level> dimension = ResourceKey.create(Registries.DIMENSION, requestLocateDevice.dimension());
        ServerLevel serverLevel = serverPlayer.server.getLevel(dimension);
        if (!(serverLevel != null && serverLevel.hasChunkAt(requestLocateDevice.monitorPos()) && (object = serverLevel.getBlockEntity(requestLocateDevice.monitorPos())) instanceof ProductionMonitorBlockEntity && (productionMonitorBlockEntity = (ProductionMonitorBlockEntity)((Object)object)).isMonitorOnline())) {
            return;
        }
        object = productionMonitorBlockEntity.deviceSnapshot(serverLevel.getGameTime()).groups().stream().filter(deviceGroupSnapshot -> deviceGroupSnapshot.name().equals(requestLocateDevice.name())).filter(deviceGroupSnapshot -> BuiltInRegistries.ITEM.getKey(deviceGroupSnapshot.visual().getItem()).equals((Object)requestLocateDevice.visualId())).findFirst().orElse(null);
        if (object == null || ((ProductionMonitorBlockEntity.DeviceGroupSnapshot)object).missingLocations().isEmpty()) {
            PacketDistributor.sendToPlayer((ServerPlayer)serverPlayer, (CustomPacketPayload)LocateDeviceResult.unavailable(requestLocateDevice.dimension(), requestLocateDevice.name()), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        int n = Math.floorMod(requestLocateDevice.index(), ((ProductionMonitorBlockEntity.DeviceGroupSnapshot)object).missingLocations().size());
        ProductionMonitorBlockEntity.DeviceLocation deviceLocation = ((ProductionMonitorBlockEntity.DeviceGroupSnapshot)object).missingLocations().get(n);

        // requestLocateDevice.dimension() is the dimension containing the linked monitor.
        // The missing-channel device can live in any dimension of the same AE grid, so the
        // locator result must carry the device's own recorded dimension instead.
        PacketDistributor.sendToPlayer((ServerPlayer)serverPlayer,
                (CustomPacketPayload)new LocateDeviceResult(
                        true,
                        deviceLocation.dimension(),
                        deviceLocation.pos(),
                        deviceLocation.side(),
                        requestLocateDevice.name(),
                        n,
                        ((ProductionMonitorBlockEntity.DeviceGroupSnapshot)object).missingLocations().size()),
                (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    private static void handleStatisticsRequest(RequestStatistics requestStatistics, IPayloadContext iPayloadContext) {
        Player player = iPayloadContext.player();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)player;
        if (!MonitorNetwork.hasMatchingTablet(serverPlayer, requestStatistics.dimension(), requestStatistics.pos())) {
            MonitorNetwork.sendStatisticsOffline(serverPlayer, requestStatistics, Status.INVALID_LINK);
            return;
        }
        ResourceKey<net.minecraft.world.level.Level> dimension = ResourceKey.create(Registries.DIMENSION, requestStatistics.dimension());
        ServerLevel serverLevel = serverPlayer.server.getLevel(dimension);
        if (serverLevel == null || !serverLevel.hasChunkAt(requestStatistics.pos())) {
            MonitorNetwork.sendStatisticsOffline(serverPlayer, requestStatistics, Status.CHUNK_UNLOADED);
            return;
        }
        Object object = serverLevel.getBlockEntity(requestStatistics.pos());
        if (!(object instanceof ProductionMonitorBlockEntity)) {
            MonitorNetwork.sendStatisticsOffline(serverPlayer, requestStatistics, Status.MONITOR_MISSING);
            return;
        }
        ProductionMonitorBlockEntity productionMonitorBlockEntity = (ProductionMonitorBlockEntity)((Object)object);
        if (!productionMonitorBlockEntity.isMonitorOnline()) {
            MonitorNetwork.sendStatisticsOffline(serverPlayer, requestStatistics, Status.NETWORK_OFFLINE);
            return;
        }
        Status status = productionMonitorBlockEntity.isWarmingUp() ? Status.WARMING_UP : Status.ONLINE;
        if (requestStatistics.statisticsPage() == StatisticsPage.STORAGE) {
            StorageStats storageStats = StorageStats.from(productionMonitorBlockEntity.storageCapacitySnapshot());
            PacketDistributor.sendToPlayer((ServerPlayer)serverPlayer, (CustomPacketPayload)new StatisticsSnapshot(requestStatistics.dimension(), requestStatistics.pos(), status, StatisticsPage.STORAGE, 0, 1, 0, storageStats, List.of(), DeviceTotals.EMPTY, List.of()), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (requestStatistics.statisticsPage() == StatisticsPage.DEVICES) {
            MonitorNetwork.sendDeviceStatistics(serverPlayer, requestStatistics, productionMonitorBlockEntity.deviceSnapshot(serverLevel.getGameTime()), status);
            return;
        }
        String string = requestStatistics.search().strip().toLowerCase(Locale.ROOT);
        HashMap<Item, MutableComponentGroup> hashMap = new HashMap<Item, MutableComponentGroup>();
        for (ProductionMonitorBlockEntity.SnapshotEntry snapshotEntry : productionMonitorBlockEntity.snapshot(serverLevel.getGameTime())) {
            AEKey aEKey = snapshotEntry.key();
            if (!(aEKey instanceof AEItemKey)) continue;
            AEItemKey aEItemKey = (AEItemKey)aEKey;
            if (snapshotEntry.amount() <= 0L || aEItemKey.getReadOnlyStack().getComponentsPatch().isEmpty()) continue;
            hashMap.computeIfAbsent(aEItemKey.getItem(), item -> new MutableComponentGroup(AEItemKey.of((ItemLike)item))).add(aEItemKey, snapshotEntry.amount(), MonitorNetwork.isInfinite((AEKey)aEItemKey, snapshotEntry.amount()));
        }
        List<ComponentGroup> list = hashMap.values().stream().filter(mutableComponentGroup -> string.isEmpty() || mutableComponentGroup.matches(string)).map(MutableComponentGroup::finish).sorted(Comparator.<ComponentGroup>comparingLong(componentGroup -> componentGroup.infinite() ? Long.MAX_VALUE : componentGroup.totalAmount()).reversed().thenComparing(componentGroup -> componentGroup.baseKey().getDisplayName().getString(), String.CASE_INSENSITIVE_ORDER)).toList();
        int n = Math.max(6, Math.min(18, requestStatistics.pageSize()));
        int n2 = Math.max(1, (list.size() + n - 1) / n);
        int n3 = Math.max(0, Math.min(requestStatistics.page(), n2 - 1));
        int n4 = n3 * n;
        int n5 = Math.min(list.size(), n4 + n);
        ArrayList<ComponentGroup> arrayList = new ArrayList<ComponentGroup>(list.subList(n4, n5));
        PacketDistributor.sendToPlayer((ServerPlayer)serverPlayer, (CustomPacketPayload)new StatisticsSnapshot(requestStatistics.dimension(), requestStatistics.pos(), status, StatisticsPage.COMPONENTS, n3, n2, list.size(), StorageStats.EMPTY, arrayList, DeviceTotals.EMPTY, List.of()), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    private static void sendDeviceStatistics(ServerPlayer serverPlayer, RequestStatistics requestStatistics, ProductionMonitorBlockEntity.DeviceSnapshot deviceSnapshot, Status status) {
        String string = requestStatistics.search().strip().toLowerCase(Locale.ROOT);
        Comparator<ProductionMonitorBlockEntity.DeviceGroupSnapshot> comparator = Comparator.comparing(ProductionMonitorBlockEntity.DeviceGroupSnapshot::name, String.CASE_INSENSITIVE_ORDER);
        Comparator<ProductionMonitorBlockEntity.DeviceGroupSnapshot> comparator2 = switch (requestStatistics.deviceSort()) {
            default -> throw new IncompatibleClassChangeError();
            case DeviceSort.POWER -> Comparator.comparingDouble(ProductionMonitorBlockEntity.DeviceGroupSnapshot::idlePower).reversed().thenComparing(comparator);
            case DeviceSort.COUNT -> Comparator.comparingInt(ProductionMonitorBlockEntity.DeviceGroupSnapshot::count).reversed().thenComparing(comparator);
            case DeviceSort.NAME -> comparator;
            case DeviceSort.PROBLEMS -> Comparator.comparingInt(MonitorNetwork::deviceProblemCount).reversed().thenComparing(Comparator.comparingDouble(ProductionMonitorBlockEntity.DeviceGroupSnapshot::idlePower).reversed()).thenComparing(comparator);
        };
        List<DeviceGroup> list = deviceSnapshot.groups().stream().filter(deviceGroupSnapshot -> string.isEmpty() || deviceGroupSnapshot.name().toLowerCase(Locale.ROOT).contains(string) || BuiltInRegistries.ITEM.getKey(deviceGroupSnapshot.visual().getItem()).toString().toLowerCase(Locale.ROOT).contains(string)).filter(deviceGroupSnapshot -> MonitorNetwork.matchesDeviceFilter(deviceGroupSnapshot, requestStatistics.deviceFilter())).sorted(comparator2).map(DeviceGroup::from).toList();
        int n = Math.max(6, Math.min(18, requestStatistics.pageSize()));
        int n2 = Math.max(1, (list.size() + n - 1) / n);
        int n3 = Math.max(0, Math.min(requestStatistics.page(), n2 - 1));
        int n4 = n3 * n;
        int n5 = Math.min(list.size(), n4 + n);
        DeviceTotals deviceTotals = new DeviceTotals(deviceSnapshot.totalDevices(), deviceSnapshot.activeDevices(), deviceSnapshot.missingChannels(), deviceSnapshot.unpoweredDevices(), deviceSnapshot.bootingDevices(), deviceSnapshot.assignedChannels(), deviceSnapshot.networkChannels(), deviceSnapshot.unresolvedChannels(), deviceSnapshot.idlePower());
        PacketDistributor.sendToPlayer((ServerPlayer)serverPlayer, (CustomPacketPayload)new StatisticsSnapshot(requestStatistics.dimension(), requestStatistics.pos(), status, StatisticsPage.DEVICES, n3, n2, list.size(), StorageStats.EMPTY, List.of(), deviceTotals, new ArrayList<DeviceGroup>(list.subList(n4, n5))), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    private static int deviceProblemCount(ProductionMonitorBlockEntity.DeviceGroupSnapshot deviceGroupSnapshot) {
        return deviceGroupSnapshot.missingChannel() + deviceGroupSnapshot.unpowered() + deviceGroupSnapshot.booting();
    }

    private static boolean matchesDeviceFilter(ProductionMonitorBlockEntity.DeviceGroupSnapshot deviceGroupSnapshot, DeviceFilter deviceFilter) {
        return switch (deviceFilter) {
            default -> throw new IncompatibleClassChangeError();
            case DeviceFilter.ALL -> true;
            case DeviceFilter.ACTIVE -> {
                if (deviceGroupSnapshot.active() > 0) {
                    yield true;
                }
                yield false;
            }
            case DeviceFilter.MISSING_CHANNEL -> {
                if (deviceGroupSnapshot.missingChannel() > 0) {
                    yield true;
                }
                yield false;
            }
            case DeviceFilter.UNPOWERED -> deviceGroupSnapshot.unpowered() > 0;
        };
    }

    private static void handleHudRequest(RequestHud requestHud, IPayloadContext iPayloadContext) {
        Object object;
        Object object2 = iPayloadContext.player();
        if (!(object2 instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)object2;
        object2 = TabletCurios.findEquippedLink((LivingEntity)serverPlayer);
        if (((Optional)object2).isEmpty()) {
            PacketDistributor.sendToPlayer((ServerPlayer)serverPlayer, (CustomPacketPayload)new HudSnapshot(Status.INVALID_LINK, requestHud.mode(), List.of()), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        ResourceKey resourceKey = ResourceKey.create((ResourceKey)Registries.DIMENSION, (ResourceLocation)((ProductionTabletItem.MonitorLink)((Optional)object2).get()).dimension());
        ServerLevel serverLevel = serverPlayer.server.getLevel(resourceKey);
        if (serverLevel == null || !serverLevel.hasChunkAt(((ProductionTabletItem.MonitorLink)((Optional)object2).get()).pos())) {
            PacketDistributor.sendToPlayer((ServerPlayer)serverPlayer, (CustomPacketPayload)new HudSnapshot(Status.CHUNK_UNLOADED, requestHud.mode(), List.of()), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        Object object3 = serverLevel.getBlockEntity(((ProductionTabletItem.MonitorLink)((Optional)object2).get()).pos());
        if (!(object3 instanceof ProductionMonitorBlockEntity)) {
            PacketDistributor.sendToPlayer((ServerPlayer)serverPlayer, (CustomPacketPayload)new HudSnapshot(Status.MONITOR_MISSING, requestHud.mode(), List.of()), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        ProductionMonitorBlockEntity productionMonitorBlockEntity = (ProductionMonitorBlockEntity)((Object)object3);
        if (!productionMonitorBlockEntity.isMonitorOnline()) {
            PacketDistributor.sendToPlayer((ServerPlayer)serverPlayer, (CustomPacketPayload)new HudSnapshot(Status.NETWORK_OFFLINE, requestHud.mode(), List.of()), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        productionMonitorBlockEntity.requestSampleInterval(requestHud.sampleIntervalTicks(), serverLevel.getGameTime());
        if (requestHud.mode() == HudMode.PINNED) {
            object3 = productionMonitorBlockEntity.dashboardSnapshot(serverLevel.getGameTime()).stream().limit(requestHud.entryCount()).map(dashboardEntrySnapshot -> new Entry(MonitorNetwork.fromDashboardKind(dashboardEntrySnapshot.kind()), dashboardEntrySnapshot.key(), dashboardEntrySnapshot.amount(), dashboardEntrySnapshot.currentPerMinute(), dashboardEntrySnapshot.averagePerMinute(), dashboardEntrySnapshot.secondsSinceChange(), dashboardEntrySnapshot.infinite(), dashboardEntrySnapshot.state() == ProductionMonitorBlockEntity.AlarmState.ACTIVE)).toList();
        } else {
            object = MonitorNetwork.displaySnapshot(productionMonitorBlockEntity, serverLevel.getGameTime()).stream().filter(displayEntry -> MonitorNetwork.hudFilter(requestHud.mode(), displayEntry)).sorted(MonitorNetwork.hudComparator(requestHud.mode())).limit(requestHud.entryCount()).toList();
            List<DisplayEntry> entries = (List<DisplayEntry>)object;
            List<Entry> hudEntries = new ArrayList<>(entries.size());
            for (DisplayEntry displayEntry2 : entries) {
                hudEntries.add(displayEntry2.toNetworkEntry());
            }
            object3 = hudEntries;
        }
        object = productionMonitorBlockEntity.isWarmingUp() ? Status.WARMING_UP : Status.ONLINE;
        PacketDistributor.sendToPlayer((ServerPlayer)serverPlayer, (CustomPacketPayload)new HudSnapshot((Status)((Object)object), requestHud.mode(), (List<Entry>)object3), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    private static boolean hudFilter(HudMode hudMode, DisplayEntry displayEntry) {
        return switch (hudMode) {
            default -> throw new IncompatibleClassChangeError();
            case HudMode.ACTIVITY -> {
                if (!displayEntry.infinite() && displayEntry.averagePerMinute() != 0L) {
                    yield true;
                }
                yield false;
            }
            case HudMode.STORED -> {
                if (displayEntry.stored() > 0L) {
                    yield true;
                }
                yield false;
            }
            case HudMode.INCOMING -> {
                if (!displayEntry.infinite() && displayEntry.averagePerMinute() > 0L) {
                    yield true;
                }
                yield false;
            }
            case HudMode.OUTGOING -> {
                if (!displayEntry.infinite() && displayEntry.averagePerMinute() < 0L) {
                    yield true;
                }
                yield false;
            }
            case HudMode.FLUIDS -> {
                if (displayEntry.kind() == EntryKind.FLUID) {
                    yield true;
                }
                yield false;
            }
            case HudMode.ENERGY -> {
                if (displayEntry.kind() == EntryKind.ENERGY) {
                    yield true;
                }
                yield false;
            }
            case HudMode.PINNED -> true;
        };
    }

    private static Comparator<DisplayEntry> hudComparator(HudMode hudMode) {
        Comparator<DisplayEntry> comparator = Comparator.comparing(MonitorNetwork::displayName, String.CASE_INSENSITIVE_ORDER);
        return switch (hudMode) {
            default -> throw new IncompatibleClassChangeError();
            case HudMode.STORED -> Comparator.comparingLong(DisplayEntry::stored).reversed().thenComparing(comparator);
            case HudMode.INCOMING -> Comparator.comparingLong(DisplayEntry::averagePerMinute).reversed().thenComparing(comparator);
            case HudMode.OUTGOING -> Comparator.<DisplayEntry>comparingLong(displayEntry -> MonitorNetwork.absSafe(displayEntry.averagePerMinute())).reversed().thenComparing(comparator);
            case HudMode.ACTIVITY, HudMode.FLUIDS, HudMode.ENERGY, HudMode.PINNED -> Comparator.<DisplayEntry>comparingLong(displayEntry -> MonitorNetwork.absSafe(displayEntry.averagePerMinute())).reversed().thenComparing(comparator);
        };
    }

    private static void handleRequest(RequestSnapshot requestSnapshot, IPayloadContext iPayloadContext) {
        Object object;
        Player player = iPayloadContext.player();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)player;
        if (!MonitorNetwork.hasMatchingTablet(serverPlayer, requestSnapshot.dimension(), requestSnapshot.pos())) {
            MonitorNetwork.sendOffline(serverPlayer, requestSnapshot, Status.INVALID_LINK);
            return;
        }
        ResourceKey<net.minecraft.world.level.Level> dimension = ResourceKey.create(Registries.DIMENSION, requestSnapshot.dimension());
        ServerLevel serverLevel = serverPlayer.server.getLevel(dimension);
        if (serverLevel == null || !serverLevel.hasChunkAt(requestSnapshot.pos())) {
            MonitorNetwork.sendOffline(serverPlayer, requestSnapshot, Status.CHUNK_UNLOADED);
            return;
        }
        Object object2 = serverLevel.getBlockEntity(requestSnapshot.pos());
        if (!(object2 instanceof ProductionMonitorBlockEntity)) {
            MonitorNetwork.sendOffline(serverPlayer, requestSnapshot, Status.MONITOR_MISSING);
            return;
        }
        ProductionMonitorBlockEntity productionMonitorBlockEntity = (ProductionMonitorBlockEntity)((Object)object2);
        if (!productionMonitorBlockEntity.isMonitorOnline()) {
            MonitorNetwork.sendOffline(serverPlayer, requestSnapshot, Status.NETWORK_OFFLINE);
            return;
        }
        productionMonitorBlockEntity.requestSampleInterval(requestSnapshot.sampleIntervalTicks(), serverLevel.getGameTime());
        List<DisplayEntry> displayEntries = MonitorNetwork.displaySnapshot(productionMonitorBlockEntity, serverLevel.getGameTime());
        long l = 0L;
        int n = 0;
        long l2 = 0L;
        long l3 = 0L;
        long l4 = 0L;
        Iterator<DisplayEntry> iterator = displayEntries.iterator();
        while (iterator.hasNext()) {
            object = iterator.next();
            if (((DisplayEntry)object).kind() != EntryKind.ITEM || ((DisplayEntry)object).infinite()) continue;
            l = MonitorNetwork.saturatingAdd(l, Math.max(0L, ((DisplayEntry)object).stored()));
            if (((DisplayEntry)object).stored() > 0L) {
                ++n;
            }
            long l5 = ((DisplayEntry)object).currentPerMinute();
            l4 = MonitorNetwork.saturatingAdd(l4, l5);
            if (l5 > 0L) {
                l2 = MonitorNetwork.saturatingAdd(l2, l5);
                continue;
            }
            if (l5 >= 0L) continue;
            l3 = MonitorNetwork.saturatingAdd(l3, l5 == Long.MIN_VALUE ? Long.MAX_VALUE : -l5);
        }
        String search = requestSnapshot.search().strip().toLowerCase(Locale.ROOT);
        List<DisplayEntry> sortedEntries = displayEntries.stream().filter(displayEntry -> requestSnapshot.sort() != SortMode.FLUIDS || displayEntry.kind() == EntryKind.FLUID).filter(entry -> MonitorNetwork.matchesSearch(search, entry)).sorted(MonitorNetwork.comparator(requestSnapshot.sort())).toList();
        int n2 = Math.max(6, Math.min(18, requestSnapshot.pageSize()));
        int n3 = Math.max(1, (sortedEntries.size() + n2 - 1) / n2);
        int n4 = Math.max(0, Math.min(requestSnapshot.page(), n3 - 1));
        int n5 = n4 * n2;
        int n6 = Math.min(sortedEntries.size(), n5 + n2);
        ArrayList<Entry> arrayList = new ArrayList<Entry>(Math.max(0, n6 - n5));
        for (int i = n5; i < n6; ++i) {
            DisplayEntry displayEntry2 = sortedEntries.get(i);
            arrayList.add(displayEntry2.toNetworkEntry());
        }
        Status status = productionMonitorBlockEntity.isWarmingUp() ? Status.WARMING_UP : Status.ONLINE;
        PacketDistributor.sendToPlayer((ServerPlayer)serverPlayer, (CustomPacketPayload)new MonitorSnapshot(requestSnapshot.dimension(), requestSnapshot.pos(), status, n4, n3, sortedEntries.size(), l, n, l2, l3, l4, arrayList), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    private static Comparator<DisplayEntry> comparator(SortMode sortMode) {
        Comparator<DisplayEntry> comparator = Comparator.comparing(MonitorNetwork::displayName, String.CASE_INSENSITIVE_ORDER);
        Comparator<DisplayEntry> comparator2 = Comparator.comparing(displayEntry -> displayEntry.kind() != EntryKind.ENERGY);
        return switch (sortMode) {
            default -> throw new IncompatibleClassChangeError();
            case SortMode.NAME -> comparator2.thenComparing(comparator);
            case SortMode.STORED -> comparator2.thenComparing(Comparator.comparingLong(DisplayEntry::stored).reversed()).thenComparing(comparator);
            case SortMode.ACTIVITY -> comparator2.thenComparing(Comparator.<DisplayEntry>comparingLong(displayEntry -> MonitorNetwork.absSafe(displayEntry.averagePerMinute())).reversed()).thenComparing(comparator);
            case SortMode.FLUIDS -> Comparator.<DisplayEntry>comparingLong(displayEntry -> MonitorNetwork.absSafe(displayEntry.averagePerMinute())).reversed().thenComparing(comparator);
        };
    }

    private static List<DisplayEntry> displaySnapshot(ProductionMonitorBlockEntity productionMonitorBlockEntity, long l) {
        ArrayList<DisplayEntry> arrayList = new ArrayList<DisplayEntry>();
        for (ProductionMonitorBlockEntity.SnapshotEntry snapshotEntry : productionMonitorBlockEntity.snapshot(l)) {
            EntryKind entryKind = snapshotEntry.key() instanceof AEFluidKey ? EntryKind.FLUID : EntryKind.ITEM;
            arrayList.add(new DisplayEntry(entryKind, snapshotEntry.key(), snapshotEntry.amount(), snapshotEntry.currentPerMinute(), snapshotEntry.averagePerMinute(), snapshotEntry.secondsSinceChange(), MonitorNetwork.isInfinite(snapshotEntry.key(), snapshotEntry.amount())));
        }
        ProductionMonitorBlockEntity.EnergySnapshot energySnapshot = productionMonitorBlockEntity.energySnapshot(l);
        arrayList.add(new DisplayEntry(EntryKind.ENERGY, (AEKey)AEItemKey.of((ItemLike)AEBlocks.ENERGY_CELL), energySnapshot.storedFe(), energySnapshot.currentPerMinute(), energySnapshot.averagePerMinute(), energySnapshot.secondsSinceChange(), energySnapshot.infinite()));
        return arrayList;
    }

    private static String displayName(DisplayEntry displayEntry) {
        return displayEntry.kind() == EntryKind.ENERGY ? "FE Energy" : displayEntry.key().getDisplayName().getString();
    }

    private static boolean isInfinite(AEKey aEKey, long l) {
        if (l == Long.MAX_VALUE) {
            return true;
        }
        long l2 = aEKey instanceof AEFluidKey ? 2147483647000L : Integer.MAX_VALUE;
        return l >= l2;
    }

    private static long absSafe(long l) {
        return l == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(l);
    }

    private static long saturatingAdd(long l, long l2) {
        if (l2 > 0L && l > Long.MAX_VALUE - l2) {
            return Long.MAX_VALUE;
        }
        if (l2 < 0L && l < Long.MIN_VALUE - l2) {
            return Long.MIN_VALUE;
        }
        return l + l2;
    }

    private static boolean hasMatchingTablet(ServerPlayer serverPlayer, ResourceLocation resourceLocation, BlockPos blockPos) {
        for (int i = 0; i < serverPlayer.getInventory().getContainerSize(); ++i) {
            ItemStack itemStack = serverPlayer.getInventory().getItem(i);
            if (!ProductionTabletItem.getLink(itemStack).filter(monitorLink -> monitorLink.dimension().equals((Object)resourceLocation) && monitorLink.pos().equals((Object)blockPos)).isPresent()) continue;
            return true;
        }
        return TabletCurios.findEquippedLink((LivingEntity)serverPlayer).filter(monitorLink -> monitorLink.dimension().equals((Object)resourceLocation) && monitorLink.pos().equals((Object)blockPos)).isPresent();
    }

    private static void sendOffline(ServerPlayer serverPlayer, RequestSnapshot requestSnapshot, Status status) {
        PacketDistributor.sendToPlayer((ServerPlayer)serverPlayer, (CustomPacketPayload)new MonitorSnapshot(requestSnapshot.dimension(), requestSnapshot.pos(), status, 0, 1, 0, 0L, 0, 0L, 0L, 0L, List.of()), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    private static void sendStatisticsOffline(ServerPlayer serverPlayer, RequestStatistics requestStatistics, Status status) {
        PacketDistributor.sendToPlayer((ServerPlayer)serverPlayer, (CustomPacketPayload)new StatisticsSnapshot(requestStatistics.dimension(), requestStatistics.pos(), status, requestStatistics.statisticsPage(), 0, 1, 0, StorageStats.EMPTY, List.of(), DeviceTotals.EMPTY, List.of()), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    private static void handleSnapshot(MonitorSnapshot monitorSnapshot, IPayloadContext iPayloadContext) {
        ClientMonitorState.accept(monitorSnapshot);
    }

    private static void handleHudSnapshot(HudSnapshot hudSnapshot, IPayloadContext iPayloadContext) {
        ClientMonitorState.acceptHud(hudSnapshot);
    }

    private static void handleStatisticsSnapshot(StatisticsSnapshot statisticsSnapshot, IPayloadContext iPayloadContext) {
        ClientMonitorState.acceptStatistics(statisticsSnapshot);
    }

    private static void handleLocateResult(LocateDeviceResult locateDeviceResult, IPayloadContext iPayloadContext) {
        DeviceLocator.accept(locateDeviceResult);
    }

    private static void handleDashboardSnapshot(DashboardSnapshot dashboardSnapshot, IPayloadContext iPayloadContext) {
        ClientMonitorState.acceptDashboard(dashboardSnapshot);
    }

    private static void handleNetworkMapSnapshot(NetworkMapPayload networkMapPayload, IPayloadContext iPayloadContext) {
        ClientMonitorState.acceptNetworkMap(networkMapPayload);
    }

    private static int clampSampleInterval(int n) {
        return Math.max(20, Math.min(100, n));
    }

    private static ResourceLocation id(String string) {
        return ResourceLocation.fromNamespaceAndPath((String)"forever_production_monitor", (String)string);
    }

    private static /* synthetic */ boolean matchesSearch(String string, DisplayEntry displayEntry) {
        return string.isEmpty() || MonitorNetwork.displayName(displayEntry).toLowerCase(Locale.ROOT).contains(string) || displayEntry.key().getId().toString().contains(string);
    }

    public record RequestSnapshot(ResourceLocation dimension, BlockPos pos, String search, SortMode sort, int page, int pageSize, int sampleIntervalTicks) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<RequestSnapshot> TYPE = new CustomPacketPayload.Type(MonitorNetwork.id("request_snapshot"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RequestSnapshot> STREAM_CODEC = StreamCodec.of((registryFriendlyByteBuf, requestSnapshot) -> {
            ResourceLocation.STREAM_CODEC.encode(registryFriendlyByteBuf, requestSnapshot.dimension);
            registryFriendlyByteBuf.writeBlockPos(requestSnapshot.pos);
            registryFriendlyByteBuf.writeUtf(requestSnapshot.search, 64);
            registryFriendlyByteBuf.writeEnum((Enum)requestSnapshot.sort);
            registryFriendlyByteBuf.writeVarInt(Math.max(0, requestSnapshot.page));
            registryFriendlyByteBuf.writeVarInt(Math.max(6, Math.min(18, requestSnapshot.pageSize)));
            registryFriendlyByteBuf.writeVarInt(requestSnapshot.sampleIntervalTicks);
        }, registryFriendlyByteBuf -> new RequestSnapshot((ResourceLocation)ResourceLocation.STREAM_CODEC.decode(registryFriendlyByteBuf), registryFriendlyByteBuf.readBlockPos(), registryFriendlyByteBuf.readUtf(64), (SortMode)registryFriendlyByteBuf.readEnum(SortMode.class), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarInt()));

        public RequestSnapshot {
            search = search == null ? "" : search.substring(0, Math.min(64, search.length()));
            page = Math.max(0, page);
            pageSize = Math.max(6, Math.min(18, pageSize));
            sampleIntervalTicks = MonitorNetwork.clampSampleInterval(sampleIntervalTicks);
        }

        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record MonitorSnapshot(ResourceLocation dimension, BlockPos pos, Status status, int page, int pages, int totalEntries, long totalStored, int totalTypes, long totalIncomingPerMinute, long totalOutgoingPerMinute, long totalNetPerMinute, List<Entry> entries) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<MonitorSnapshot> TYPE = new CustomPacketPayload.Type(MonitorNetwork.id("monitor_snapshot"));
        public static final StreamCodec<RegistryFriendlyByteBuf, MonitorSnapshot> STREAM_CODEC = StreamCodec.of((registryFriendlyByteBuf, monitorSnapshot) -> {
            ResourceLocation.STREAM_CODEC.encode(registryFriendlyByteBuf, monitorSnapshot.dimension);
            registryFriendlyByteBuf.writeBlockPos(monitorSnapshot.pos);
            registryFriendlyByteBuf.writeEnum((Enum)monitorSnapshot.status);
            registryFriendlyByteBuf.writeVarInt(monitorSnapshot.page);
            registryFriendlyByteBuf.writeVarInt(monitorSnapshot.pages);
            registryFriendlyByteBuf.writeVarInt(monitorSnapshot.totalEntries);
            registryFriendlyByteBuf.writeVarLong(monitorSnapshot.totalStored);
            registryFriendlyByteBuf.writeVarInt(monitorSnapshot.totalTypes);
            registryFriendlyByteBuf.writeLong(monitorSnapshot.totalIncomingPerMinute);
            registryFriendlyByteBuf.writeLong(monitorSnapshot.totalOutgoingPerMinute);
            registryFriendlyByteBuf.writeLong(monitorSnapshot.totalNetPerMinute);
            registryFriendlyByteBuf.writeVarInt(monitorSnapshot.entries.size());
            monitorSnapshot.entries.forEach(entry -> entry.write((RegistryFriendlyByteBuf)registryFriendlyByteBuf));
        }, registryFriendlyByteBuf -> {
            ResourceLocation resourceLocation = (ResourceLocation)ResourceLocation.STREAM_CODEC.decode(registryFriendlyByteBuf);
            BlockPos blockPos = registryFriendlyByteBuf.readBlockPos();
            Status status = (Status)registryFriendlyByteBuf.readEnum(Status.class);
            int n = registryFriendlyByteBuf.readVarInt();
            int n2 = registryFriendlyByteBuf.readVarInt();
            int n3 = registryFriendlyByteBuf.readVarInt();
            long l = registryFriendlyByteBuf.readVarLong();
            int n4 = registryFriendlyByteBuf.readVarInt();
            long l2 = registryFriendlyByteBuf.readLong();
            long l3 = registryFriendlyByteBuf.readLong();
            long l4 = registryFriendlyByteBuf.readLong();
            int n5 = Math.min(18, registryFriendlyByteBuf.readVarInt());
            ArrayList<Entry> arrayList = new ArrayList<Entry>(n5);
            for (int i = 0; i < n5; ++i) {
                arrayList.add(Entry.read(registryFriendlyByteBuf));
            }
            return new MonitorSnapshot(resourceLocation, blockPos, status, n, n2, n3, l, n4, l2, l3, l4, arrayList);
        });

        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record RequestStatistics(ResourceLocation dimension, BlockPos pos, StatisticsPage statisticsPage, String search, int page, int pageSize, DeviceSort deviceSort, DeviceFilter deviceFilter) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<RequestStatistics> TYPE = new CustomPacketPayload.Type(MonitorNetwork.id("request_statistics"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RequestStatistics> STREAM_CODEC = StreamCodec.of((registryFriendlyByteBuf, requestStatistics) -> {
            ResourceLocation.STREAM_CODEC.encode(registryFriendlyByteBuf, requestStatistics.dimension);
            registryFriendlyByteBuf.writeBlockPos(requestStatistics.pos);
            registryFriendlyByteBuf.writeEnum((Enum)requestStatistics.statisticsPage);
            registryFriendlyByteBuf.writeUtf(requestStatistics.search, 64);
            registryFriendlyByteBuf.writeVarInt(requestStatistics.page);
            registryFriendlyByteBuf.writeVarInt(requestStatistics.pageSize);
            registryFriendlyByteBuf.writeEnum((Enum)requestStatistics.deviceSort);
            registryFriendlyByteBuf.writeEnum((Enum)requestStatistics.deviceFilter);
        }, registryFriendlyByteBuf -> new RequestStatistics((ResourceLocation)ResourceLocation.STREAM_CODEC.decode(registryFriendlyByteBuf), registryFriendlyByteBuf.readBlockPos(), (StatisticsPage)registryFriendlyByteBuf.readEnum(StatisticsPage.class), registryFriendlyByteBuf.readUtf(64), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarInt(), (DeviceSort)registryFriendlyByteBuf.readEnum(DeviceSort.class), (DeviceFilter)registryFriendlyByteBuf.readEnum(DeviceFilter.class)));

        public RequestStatistics {
            search = search == null ? "" : search.substring(0, Math.min(64, search.length()));
            page = Math.max(0, page);
            pageSize = Math.max(6, Math.min(18, pageSize));
        }

        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record StatisticsSnapshot(ResourceLocation dimension, BlockPos pos, Status status, StatisticsPage statisticsPage, int page, int pages, int totalEntries, StorageStats storage, List<ComponentGroup> componentGroups, DeviceTotals deviceTotals, List<DeviceGroup> deviceGroups) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<StatisticsSnapshot> TYPE = new CustomPacketPayload.Type(MonitorNetwork.id("statistics_snapshot"));
        public static final StreamCodec<RegistryFriendlyByteBuf, StatisticsSnapshot> STREAM_CODEC = StreamCodec.of((registryFriendlyByteBuf, statisticsSnapshot) -> {
            ResourceLocation.STREAM_CODEC.encode(registryFriendlyByteBuf, statisticsSnapshot.dimension);
            registryFriendlyByteBuf.writeBlockPos(statisticsSnapshot.pos);
            registryFriendlyByteBuf.writeEnum((Enum)statisticsSnapshot.status);
            registryFriendlyByteBuf.writeEnum((Enum)statisticsSnapshot.statisticsPage);
            registryFriendlyByteBuf.writeVarInt(statisticsSnapshot.page);
            registryFriendlyByteBuf.writeVarInt(statisticsSnapshot.pages);
            registryFriendlyByteBuf.writeVarInt(statisticsSnapshot.totalEntries);
            statisticsSnapshot.storage.write((RegistryFriendlyByteBuf)registryFriendlyByteBuf);
            registryFriendlyByteBuf.writeVarInt(statisticsSnapshot.componentGroups.size());
            statisticsSnapshot.componentGroups.forEach(componentGroup -> componentGroup.write((RegistryFriendlyByteBuf)registryFriendlyByteBuf));
            statisticsSnapshot.deviceTotals.write((RegistryFriendlyByteBuf)registryFriendlyByteBuf);
            registryFriendlyByteBuf.writeVarInt(statisticsSnapshot.deviceGroups.size());
            statisticsSnapshot.deviceGroups.forEach(deviceGroup -> deviceGroup.write((RegistryFriendlyByteBuf)registryFriendlyByteBuf));
        }, registryFriendlyByteBuf -> {
            ResourceLocation resourceLocation = (ResourceLocation)ResourceLocation.STREAM_CODEC.decode(registryFriendlyByteBuf);
            BlockPos blockPos = registryFriendlyByteBuf.readBlockPos();
            Status status = (Status)registryFriendlyByteBuf.readEnum(Status.class);
            StatisticsPage statisticsPage = (StatisticsPage)registryFriendlyByteBuf.readEnum(StatisticsPage.class);
            int n = registryFriendlyByteBuf.readVarInt();
            int n2 = registryFriendlyByteBuf.readVarInt();
            int n3 = registryFriendlyByteBuf.readVarInt();
            StorageStats storageStats = StorageStats.read(registryFriendlyByteBuf);
            int n4 = Math.min(18, registryFriendlyByteBuf.readVarInt());
            ArrayList<ComponentGroup> arrayList = new ArrayList<ComponentGroup>(n4);
            for (int i = 0; i < n4; ++i) {
                arrayList.add(ComponentGroup.read(registryFriendlyByteBuf));
            }
            DeviceTotals deviceTotals = DeviceTotals.read(registryFriendlyByteBuf);
            int n5 = Math.min(18, registryFriendlyByteBuf.readVarInt());
            ArrayList<DeviceGroup> arrayList2 = new ArrayList<DeviceGroup>(n5);
            for (int i = 0; i < n5; ++i) {
                arrayList2.add(DeviceGroup.read(registryFriendlyByteBuf));
            }
            return new StatisticsSnapshot(resourceLocation, blockPos, status, statisticsPage, n, n2, n3, storageStats, arrayList, deviceTotals, arrayList2);
        });

        public StatisticsSnapshot {
            componentGroups = List.copyOf(componentGroups.subList(0, Math.min(18, componentGroups.size())));
            deviceGroups = List.copyOf(deviceGroups.subList(0, Math.min(18, deviceGroups.size())));
        }

        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record RequestHud(HudMode mode, int sampleIntervalTicks, int entryCount) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<RequestHud> TYPE = new CustomPacketPayload.Type(MonitorNetwork.id("request_hud"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RequestHud> STREAM_CODEC = StreamCodec.of((registryFriendlyByteBuf, requestHud) -> {
            registryFriendlyByteBuf.writeEnum((Enum)requestHud.mode);
            registryFriendlyByteBuf.writeVarInt(requestHud.sampleIntervalTicks);
            registryFriendlyByteBuf.writeVarInt(requestHud.entryCount);
        }, registryFriendlyByteBuf -> new RequestHud((HudMode)registryFriendlyByteBuf.readEnum(HudMode.class), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarInt()));

        public RequestHud {
            sampleIntervalTicks = MonitorNetwork.clampSampleInterval(sampleIntervalTicks);
            entryCount = Math.max(1, Math.min(10, entryCount));
        }

        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record HudSnapshot(Status status, HudMode mode, List<Entry> entries) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<HudSnapshot> TYPE = new CustomPacketPayload.Type(MonitorNetwork.id("hud_snapshot"));
        public static final StreamCodec<RegistryFriendlyByteBuf, HudSnapshot> STREAM_CODEC = StreamCodec.of((registryFriendlyByteBuf, hudSnapshot) -> {
            registryFriendlyByteBuf.writeEnum((Enum)hudSnapshot.status);
            registryFriendlyByteBuf.writeEnum((Enum)hudSnapshot.mode);
            registryFriendlyByteBuf.writeVarInt(Math.min(10, hudSnapshot.entries.size()));
            hudSnapshot.entries.stream().limit(10L).forEach(entry -> entry.write((RegistryFriendlyByteBuf)registryFriendlyByteBuf));
        }, registryFriendlyByteBuf -> {
            Status status = (Status)registryFriendlyByteBuf.readEnum(Status.class);
            HudMode hudMode = (HudMode)registryFriendlyByteBuf.readEnum(HudMode.class);
            int n = Math.min(10, registryFriendlyByteBuf.readVarInt());
            ArrayList<Entry> arrayList = new ArrayList<Entry>(n);
            for (int i = 0; i < n; ++i) {
                arrayList.add(Entry.read(registryFriendlyByteBuf));
            }
            return new HudSnapshot(status, hudMode, arrayList);
        });

        public HudSnapshot {
            entries = List.copyOf(entries.subList(0, Math.min(10, entries.size())));
        }

        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record RequestLocateDevice(ResourceLocation dimension, BlockPos monitorPos, ResourceLocation visualId, String name, int index) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<RequestLocateDevice> TYPE = new CustomPacketPayload.Type(MonitorNetwork.id("request_locate_device"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RequestLocateDevice> STREAM_CODEC = StreamCodec.of((registryFriendlyByteBuf, requestLocateDevice) -> {
            ResourceLocation.STREAM_CODEC.encode(registryFriendlyByteBuf, requestLocateDevice.dimension);
            registryFriendlyByteBuf.writeBlockPos(requestLocateDevice.monitorPos);
            ResourceLocation.STREAM_CODEC.encode(registryFriendlyByteBuf, requestLocateDevice.visualId);
            registryFriendlyByteBuf.writeUtf(requestLocateDevice.name, 96);
            registryFriendlyByteBuf.writeVarInt(requestLocateDevice.index);
        }, registryFriendlyByteBuf -> new RequestLocateDevice((ResourceLocation)ResourceLocation.STREAM_CODEC.decode(registryFriendlyByteBuf), registryFriendlyByteBuf.readBlockPos(), (ResourceLocation)ResourceLocation.STREAM_CODEC.decode(registryFriendlyByteBuf), registryFriendlyByteBuf.readUtf(96), registryFriendlyByteBuf.readVarInt()));

        public RequestLocateDevice {
            name = name == null ? "" : name.substring(0, Math.min(96, name.length()));
            index = Math.max(0, index);
        }

        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record LocateDeviceResult(boolean found, ResourceLocation dimension, BlockPos pos, int side, String name, int index, int total) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<LocateDeviceResult> TYPE = new CustomPacketPayload.Type(MonitorNetwork.id("locate_device_result"));
        public static final StreamCodec<RegistryFriendlyByteBuf, LocateDeviceResult> STREAM_CODEC = StreamCodec.of((registryFriendlyByteBuf, locateDeviceResult) -> {
            registryFriendlyByteBuf.writeBoolean(locateDeviceResult.found);
            ResourceLocation.STREAM_CODEC.encode(registryFriendlyByteBuf, locateDeviceResult.dimension);
            registryFriendlyByteBuf.writeBlockPos(locateDeviceResult.pos);
            registryFriendlyByteBuf.writeVarInt(locateDeviceResult.side + 1);
            registryFriendlyByteBuf.writeUtf(locateDeviceResult.name, 96);
            registryFriendlyByteBuf.writeVarInt(locateDeviceResult.index);
            registryFriendlyByteBuf.writeVarInt(locateDeviceResult.total);
        }, registryFriendlyByteBuf -> new LocateDeviceResult(registryFriendlyByteBuf.readBoolean(), (ResourceLocation)ResourceLocation.STREAM_CODEC.decode(registryFriendlyByteBuf), registryFriendlyByteBuf.readBlockPos(), registryFriendlyByteBuf.readVarInt() - 1, registryFriendlyByteBuf.readUtf(96), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarInt()));

        public LocateDeviceResult {
            name = name == null ? "" : name.substring(0, Math.min(96, name.length()));
            index = Math.max(0, index);
            total = Math.max(0, total);
        }

        public static LocateDeviceResult unavailable(ResourceLocation resourceLocation, String string) {
            return new LocateDeviceResult(false, resourceLocation, BlockPos.ZERO, -1, string, 0, 0);
        }

        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record RequestDashboard(ResourceLocation dimension, BlockPos pos) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<RequestDashboard> TYPE = new CustomPacketPayload.Type(MonitorNetwork.id("request_dashboard"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RequestDashboard> STREAM_CODEC = StreamCodec.of((registryFriendlyByteBuf, requestDashboard) -> {
            ResourceLocation.STREAM_CODEC.encode(registryFriendlyByteBuf, requestDashboard.dimension);
            registryFriendlyByteBuf.writeBlockPos(requestDashboard.pos);
        }, registryFriendlyByteBuf -> new RequestDashboard((ResourceLocation)ResourceLocation.STREAM_CODEC.decode(registryFriendlyByteBuf), registryFriendlyByteBuf.readBlockPos()));

        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record DashboardSnapshot(ResourceLocation dimension, BlockPos pos, Status status, List<DashboardEntry> entries) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<DashboardSnapshot> TYPE = new CustomPacketPayload.Type(MonitorNetwork.id("dashboard_snapshot"));
        public static final StreamCodec<RegistryFriendlyByteBuf, DashboardSnapshot> STREAM_CODEC = StreamCodec.of((registryFriendlyByteBuf, dashboardSnapshot) -> {
            ResourceLocation.STREAM_CODEC.encode(registryFriendlyByteBuf, dashboardSnapshot.dimension);
            registryFriendlyByteBuf.writeBlockPos(dashboardSnapshot.pos);
            registryFriendlyByteBuf.writeEnum((Enum)dashboardSnapshot.status);
            registryFriendlyByteBuf.writeVarInt(dashboardSnapshot.entries.size());
            dashboardSnapshot.entries.forEach(dashboardEntry -> dashboardEntry.write((RegistryFriendlyByteBuf)registryFriendlyByteBuf));
        }, registryFriendlyByteBuf -> {
            ResourceLocation resourceLocation = (ResourceLocation)ResourceLocation.STREAM_CODEC.decode(registryFriendlyByteBuf);
            BlockPos blockPos = registryFriendlyByteBuf.readBlockPos();
            Status status = (Status)registryFriendlyByteBuf.readEnum(Status.class);
            int n = Math.min(24, registryFriendlyByteBuf.readVarInt());
            ArrayList<DashboardEntry> arrayList = new ArrayList<DashboardEntry>(n);
            for (int i = 0; i < n; ++i) {
                arrayList.add(DashboardEntry.read(registryFriendlyByteBuf));
            }
            return new DashboardSnapshot(resourceLocation, blockPos, status, arrayList);
        });

        public DashboardSnapshot {
            entries = List.copyOf(entries.subList(0, Math.min(24, entries.size())));
        }

        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record UpdateDashboardPin(ResourceLocation dimension, BlockPos pos, DashboardAction action, EntryKind kind, AEKey key, ProductionMonitorBlockEntity.AlarmMode alarmMode, long threshold, int delaySeconds, long hysteresis) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<UpdateDashboardPin> TYPE = new CustomPacketPayload.Type(MonitorNetwork.id("update_dashboard_pin"));
        public static final StreamCodec<RegistryFriendlyByteBuf, UpdateDashboardPin> STREAM_CODEC = StreamCodec.of((registryFriendlyByteBuf, updateDashboardPin) -> {
            ResourceLocation.STREAM_CODEC.encode(registryFriendlyByteBuf, updateDashboardPin.dimension);
            registryFriendlyByteBuf.writeBlockPos(updateDashboardPin.pos);
            registryFriendlyByteBuf.writeEnum((Enum)updateDashboardPin.action);
            registryFriendlyByteBuf.writeEnum((Enum)updateDashboardPin.kind);
            AEKey.STREAM_CODEC.encode(registryFriendlyByteBuf, updateDashboardPin.key);
            registryFriendlyByteBuf.writeEnum((Enum)updateDashboardPin.alarmMode);
            registryFriendlyByteBuf.writeLong(updateDashboardPin.threshold);
            registryFriendlyByteBuf.writeVarInt(updateDashboardPin.delaySeconds);
            registryFriendlyByteBuf.writeVarLong(updateDashboardPin.hysteresis);
        }, registryFriendlyByteBuf -> new UpdateDashboardPin((ResourceLocation)ResourceLocation.STREAM_CODEC.decode(registryFriendlyByteBuf), registryFriendlyByteBuf.readBlockPos(), (DashboardAction)registryFriendlyByteBuf.readEnum(DashboardAction.class), (EntryKind)registryFriendlyByteBuf.readEnum(EntryKind.class), (AEKey)AEKey.STREAM_CODEC.decode(registryFriendlyByteBuf), (ProductionMonitorBlockEntity.AlarmMode)registryFriendlyByteBuf.readEnum(ProductionMonitorBlockEntity.AlarmMode.class), registryFriendlyByteBuf.readLong(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarLong()));

        public UpdateDashboardPin {
            delaySeconds = Math.max(0, Math.min(3600, delaySeconds));
            hysteresis = Math.max(0L, hysteresis);
        }

        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record RequestNetworkMap(ResourceLocation dimension, BlockPos pos,
                                    ResourceLocation viewDimension) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<RequestNetworkMap> TYPE =
                new CustomPacketPayload.Type(MonitorNetwork.id("request_network_map"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RequestNetworkMap> STREAM_CODEC =
                StreamCodec.of((buf, request) -> {
                    ResourceLocation.STREAM_CODEC.encode(buf, request.dimension);
                    buf.writeBlockPos(request.pos);
                    ResourceLocation.STREAM_CODEC.encode(buf, request.viewDimension);
                }, buf -> new RequestNetworkMap(
                        (ResourceLocation)ResourceLocation.STREAM_CODEC.decode(buf),
                        buf.readBlockPos(),
                        (ResourceLocation)ResourceLocation.STREAM_CODEC.decode(buf)));

        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record NetworkMapPayload(ResourceLocation dimension,
                                    BlockPos pos,
                                    ResourceLocation viewDimension,
                                    Status status,
                                    boolean truncated,
                                    List<ResourceLocation> dimensions,
                                    List<QuantumLink> quantumLinks,
                                    List<WirelessLink> wirelessLinks,
                                    List<MapNode> nodes) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<NetworkMapPayload> TYPE =
                new CustomPacketPayload.Type(MonitorNetwork.id("network_map_snapshot"));
        public static final StreamCodec<RegistryFriendlyByteBuf, NetworkMapPayload> STREAM_CODEC =
                StreamCodec.of((buf, payload) -> {
                    ResourceLocation.STREAM_CODEC.encode(buf, payload.dimension);
                    buf.writeBlockPos(payload.pos);
                    ResourceLocation.STREAM_CODEC.encode(buf, payload.viewDimension);
                    buf.writeEnum((Enum)payload.status);
                    buf.writeBoolean(payload.truncated);

                    buf.writeVarInt(Math.min(32, payload.dimensions.size()));
                    payload.dimensions.stream().limit(32L)
                            .forEach(dimension -> ResourceLocation.STREAM_CODEC.encode(buf, dimension));

                    buf.writeVarInt(Math.min(128, payload.quantumLinks.size()));
                    payload.quantumLinks.stream().limit(128L)
                            .forEach(link -> link.write(buf));

                    buf.writeVarInt(Math.min(256, payload.wirelessLinks.size()));
                    payload.wirelessLinks.stream().limit(256L)
                            .forEach(link -> link.write(buf));

                    buf.writeVarInt(Math.min(4096, payload.nodes.size()));
                    payload.nodes.stream().limit(4096L)
                            .forEach(node -> node.write(buf));
                }, buf -> {
                    ResourceLocation dimension =
                            (ResourceLocation)ResourceLocation.STREAM_CODEC.decode(buf);
                    BlockPos pos = buf.readBlockPos();
                    ResourceLocation viewDimension =
                            (ResourceLocation)ResourceLocation.STREAM_CODEC.decode(buf);
                    Status status = (Status)buf.readEnum(Status.class);
                    boolean truncated = buf.readBoolean();

                    int dimensionCount = Math.min(32, buf.readVarInt());
                    ArrayList<ResourceLocation> dimensions = new ArrayList<>(dimensionCount);
                    for (int i = 0; i < dimensionCount; ++i) {
                        dimensions.add((ResourceLocation)ResourceLocation.STREAM_CODEC.decode(buf));
                    }

                    int linkCount = Math.min(128, buf.readVarInt());
                    ArrayList<QuantumLink> quantumLinks = new ArrayList<>(linkCount);
                    for (int i = 0; i < linkCount; ++i) {
                        quantumLinks.add(QuantumLink.read(buf));
                    }

                    int wirelessLinkCount = Math.min(256, buf.readVarInt());
                    ArrayList<WirelessLink> wirelessLinks = new ArrayList<>(wirelessLinkCount);
                    for (int i = 0; i < wirelessLinkCount; ++i) {
                        wirelessLinks.add(WirelessLink.read(buf));
                    }

                    int nodeCount = Math.min(4096, buf.readVarInt());
                    ArrayList<MapNode> nodes = new ArrayList<>(nodeCount);
                    for (int i = 0; i < nodeCount; ++i) {
                        nodes.add(MapNode.read(buf));
                    }

                    return new NetworkMapPayload(
                            dimension, pos, viewDimension, status, truncated,
                            dimensions, quantumLinks, wirelessLinks, nodes);
                });

        public NetworkMapPayload {
            dimensions = List.copyOf(dimensions.subList(0, Math.min(32, dimensions.size())));
            quantumLinks = List.copyOf(quantumLinks.subList(0, Math.min(128, quantumLinks.size())));
            wirelessLinks = List.copyOf(wirelessLinks.subList(0, Math.min(256, wirelessLinks.size())));
            nodes = List.copyOf(nodes.subList(0, Math.min(4096, nodes.size())));
        }

        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static enum SortMode {
        ACTIVITY,
        STORED,
        NAME,
        FLUIDS;


        public SortMode next() {
            return SortMode.values()[(this.ordinal() + 1) % SortMode.values().length];
        }
    }

    public static enum HudMode {
        ACTIVITY,
        STORED,
        INCOMING,
        OUTGOING,
        FLUIDS,
        ENERGY,
        PINNED;


        public HudMode next() {
            return HudMode.values()[(this.ordinal() + 1) % HudMode.values().length];
        }
    }

    public static enum StatisticsPage {
        STORAGE,
        COMPONENTS,
        DEVICES;

    }

    public static enum DeviceSort {
        POWER,
        COUNT,
        NAME,
        PROBLEMS;


        public DeviceSort next() {
            return DeviceSort.values()[(this.ordinal() + 1) % DeviceSort.values().length];
        }
    }

    public static enum DeviceFilter {
        ALL,
        ACTIVE,
        MISSING_CHANNEL,
        UNPOWERED;


        public DeviceFilter next() {
            return DeviceFilter.values()[(this.ordinal() + 1) % DeviceFilter.values().length];
        }
    }

    public record DeviceGroup(AEItemKey visual, String name, int count, int active, int missingChannel, int unpowered, int booting, int assignedChannels, double idlePower, int locatableDevices) {
        private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            AEKey.STREAM_CODEC.encode(registryFriendlyByteBuf, this.visual);
            registryFriendlyByteBuf.writeUtf(this.name, 96);
            registryFriendlyByteBuf.writeVarInt(this.count);
            registryFriendlyByteBuf.writeVarInt(this.active);
            registryFriendlyByteBuf.writeVarInt(this.missingChannel);
            registryFriendlyByteBuf.writeVarInt(this.unpowered);
            registryFriendlyByteBuf.writeVarInt(this.booting);
            registryFriendlyByteBuf.writeVarInt(this.assignedChannels);
            registryFriendlyByteBuf.writeDouble(this.idlePower);
            registryFriendlyByteBuf.writeVarInt(this.locatableDevices);
        }

        private static DeviceGroup read(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            AEKey aEKey = (AEKey)AEKey.STREAM_CODEC.decode(registryFriendlyByteBuf);
            if (!(aEKey instanceof AEItemKey)) {
                throw new IllegalArgumentException("Channel device visual is not an item key");
            }
            AEItemKey aEItemKey = (AEItemKey)aEKey;
            return new DeviceGroup(aEItemKey, registryFriendlyByteBuf.readUtf(96), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readDouble(), registryFriendlyByteBuf.readVarInt());
        }

        private static DeviceGroup from(ProductionMonitorBlockEntity.DeviceGroupSnapshot deviceGroupSnapshot) {
            return new DeviceGroup(deviceGroupSnapshot.visual(), deviceGroupSnapshot.name(), deviceGroupSnapshot.count(), deviceGroupSnapshot.active(), deviceGroupSnapshot.missingChannel(), deviceGroupSnapshot.unpowered(), deviceGroupSnapshot.booting(), deviceGroupSnapshot.assignedChannels(), deviceGroupSnapshot.idlePower(), deviceGroupSnapshot.missingLocations().size());
        }
    }

    public static enum DashboardAction {
        ADD,
        REMOVE,
        UPDATE,
        MOVE_UP,
        MOVE_DOWN;

    }

    public static enum EntryKind {
        ITEM,
        FLUID,
        ENERGY;

    }

    public static enum Status {
        ONLINE,
        WARMING_UP,
        NETWORK_OFFLINE,
        CHUNK_UNLOADED,
        MONITOR_MISSING,
        INVALID_LINK;

    }

    public record StorageStats(long usedBytes, long totalBytes, long usedTypes, long totalTypes, int finiteCells, int infiniteCells, int otherCells, int externalStorageBuses) {
        public static final StorageStats EMPTY = new StorageStats(0L, 0L, 0L, 0L, 0, 0, 0, 0);

        private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            registryFriendlyByteBuf.writeVarLong(this.usedBytes);
            registryFriendlyByteBuf.writeVarLong(this.totalBytes);
            registryFriendlyByteBuf.writeVarLong(this.usedTypes);
            registryFriendlyByteBuf.writeVarLong(this.totalTypes);
            registryFriendlyByteBuf.writeVarInt(this.finiteCells);
            registryFriendlyByteBuf.writeVarInt(this.infiniteCells);
            registryFriendlyByteBuf.writeVarInt(this.otherCells);
            registryFriendlyByteBuf.writeVarInt(this.externalStorageBuses);
        }

        private static StorageStats read(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            return new StorageStats(registryFriendlyByteBuf.readVarLong(), registryFriendlyByteBuf.readVarLong(), registryFriendlyByteBuf.readVarLong(), registryFriendlyByteBuf.readVarLong(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarInt());
        }

        private static StorageStats from(ProductionMonitorBlockEntity.StorageCapacitySnapshot storageCapacitySnapshot) {
            return new StorageStats(storageCapacitySnapshot.usedBytes(), storageCapacitySnapshot.totalBytes(), storageCapacitySnapshot.usedTypes(), storageCapacitySnapshot.totalTypes(), storageCapacitySnapshot.finiteCells(), storageCapacitySnapshot.infiniteCells(), storageCapacitySnapshot.otherCells(), storageCapacitySnapshot.externalStorageBuses());
        }

        public long freeBytes() {
            return Math.max(0L, this.totalBytes - this.usedBytes);
        }

        public long freeTypes() {
            return Math.max(0L, this.totalTypes - this.usedTypes);
        }
    }

    public record DeviceTotals(int totalDevices, int activeDevices, int missingChannels, int unpoweredDevices, int bootingDevices, int assignedChannels, int networkChannels, int unresolvedChannels, double idlePower) {
        public static final DeviceTotals EMPTY = new DeviceTotals(0, 0, 0, 0, 0, 0, 0, 0, 0.0);

        private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            registryFriendlyByteBuf.writeVarInt(this.totalDevices);
            registryFriendlyByteBuf.writeVarInt(this.activeDevices);
            registryFriendlyByteBuf.writeVarInt(this.missingChannels);
            registryFriendlyByteBuf.writeVarInt(this.unpoweredDevices);
            registryFriendlyByteBuf.writeVarInt(this.bootingDevices);
            registryFriendlyByteBuf.writeVarInt(this.assignedChannels);
            registryFriendlyByteBuf.writeVarInt(this.networkChannels);
            registryFriendlyByteBuf.writeVarInt(this.unresolvedChannels);
            registryFriendlyByteBuf.writeDouble(this.idlePower);
        }

        private static DeviceTotals read(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            return new DeviceTotals(registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readDouble());
        }
    }

    private static final class MutableComponentGroup {
        private final AEItemKey baseKey;
        private AEItemKey representativeKey;
        private long representativeAmount;
        private long totalAmount;
        private int variants;
        private boolean infinite;
        private final StringBuilder searchable = new StringBuilder();

        private MutableComponentGroup(AEItemKey aEItemKey) {
            this.baseKey = aEItemKey;
            this.searchable.append(aEItemKey.getId()).append(' ').append(aEItemKey.getDisplayName().getString().toLowerCase(Locale.ROOT));
        }

        private void add(AEItemKey aEItemKey, long l, boolean bl) {
            ++this.variants;
            this.infinite |= bl;
            long l2 = this.totalAmount = bl ? Long.MAX_VALUE : MonitorNetwork.saturatingAdd(this.totalAmount, l);
            if (this.representativeKey == null || l > this.representativeAmount) {
                this.representativeKey = aEItemKey;
                this.representativeAmount = l;
            }
            this.searchable.append(' ').append(aEItemKey.getDisplayName().getString().toLowerCase(Locale.ROOT));
        }

        private boolean matches(String string) {
            return this.searchable.indexOf(string) >= 0;
        }

        private ComponentGroup finish() {
            return new ComponentGroup(this.baseKey, this.representativeKey, this.totalAmount, this.variants, this.infinite);
        }
    }

    private record DisplayEntry(EntryKind kind, AEKey key, long stored, long currentPerMinute, long averagePerMinute, long secondsSinceChange, boolean infinite) {
        Entry toNetworkEntry() {
            return new Entry(this.kind, this.key, this.stored, this.currentPerMinute, this.averagePerMinute, this.secondsSinceChange, this.infinite, false);
        }
    }

    public record Entry(EntryKind kind, AEKey key, long stored, long currentPerMinute, long averagePerMinute, long secondsSinceChange, boolean infinite, boolean alarmActive) {
        private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            registryFriendlyByteBuf.writeEnum((Enum)this.kind);
            AEKey.STREAM_CODEC.encode(registryFriendlyByteBuf, this.key);
            registryFriendlyByteBuf.writeVarLong(this.stored);
            registryFriendlyByteBuf.writeLong(this.currentPerMinute);
            registryFriendlyByteBuf.writeLong(this.averagePerMinute);
            registryFriendlyByteBuf.writeVarLong(this.secondsSinceChange);
            registryFriendlyByteBuf.writeBoolean(this.infinite);
            registryFriendlyByteBuf.writeBoolean(this.alarmActive);
        }

        private static Entry read(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            return new Entry((EntryKind)registryFriendlyByteBuf.readEnum(EntryKind.class), (AEKey)AEKey.STREAM_CODEC.decode(registryFriendlyByteBuf), registryFriendlyByteBuf.readVarLong(), registryFriendlyByteBuf.readLong(), registryFriendlyByteBuf.readLong(), registryFriendlyByteBuf.readVarLong(), registryFriendlyByteBuf.readBoolean(), registryFriendlyByteBuf.readBoolean());
        }
    }

    public record ComponentGroup(AEItemKey baseKey, AEItemKey representativeKey, long totalAmount, int variants, boolean infinite) {
        private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            AEKey.STREAM_CODEC.encode(registryFriendlyByteBuf, this.baseKey);
            AEKey.STREAM_CODEC.encode(registryFriendlyByteBuf, this.representativeKey);
            registryFriendlyByteBuf.writeVarLong(this.totalAmount);
            registryFriendlyByteBuf.writeVarInt(this.variants);
            registryFriendlyByteBuf.writeBoolean(this.infinite);
        }

        private static ComponentGroup read(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            AEItemKey aEItemKey;
            AEKey aEKey;
            block3: {
                block2: {
                    AEKey aEKey2 = (AEKey)AEKey.STREAM_CODEC.decode(registryFriendlyByteBuf);
                    aEKey = (AEKey)AEKey.STREAM_CODEC.decode(registryFriendlyByteBuf);
                    if (!(aEKey2 instanceof AEItemKey)) break block2;
                    aEItemKey = (AEItemKey)aEKey2;
                    if (aEKey instanceof AEItemKey) break block3;
                }
                throw new IllegalArgumentException("Component group contains a non-item key");
            }
            AEItemKey aEItemKey2 = (AEItemKey)aEKey;
            return new ComponentGroup(aEItemKey, aEItemKey2, registryFriendlyByteBuf.readVarLong(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readBoolean());
        }
    }

    public record DashboardEntry(EntryKind kind, AEKey key, long stored, long currentPerMinute, long averagePerMinute, long secondsSinceChange, boolean infinite, ProductionMonitorBlockEntity.AlarmMode alarmMode, long threshold, int delaySeconds, long hysteresis, ProductionMonitorBlockEntity.AlarmState alarmState) {
        private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            registryFriendlyByteBuf.writeEnum((Enum)this.kind);
            AEKey.STREAM_CODEC.encode(registryFriendlyByteBuf, this.key);
            registryFriendlyByteBuf.writeVarLong(this.stored);
            registryFriendlyByteBuf.writeLong(this.currentPerMinute);
            registryFriendlyByteBuf.writeLong(this.averagePerMinute);
            registryFriendlyByteBuf.writeVarLong(this.secondsSinceChange);
            registryFriendlyByteBuf.writeBoolean(this.infinite);
            registryFriendlyByteBuf.writeEnum((Enum)this.alarmMode);
            registryFriendlyByteBuf.writeLong(this.threshold);
            registryFriendlyByteBuf.writeVarInt(this.delaySeconds);
            registryFriendlyByteBuf.writeVarLong(this.hysteresis);
            registryFriendlyByteBuf.writeEnum((Enum)this.alarmState);
        }

        private static DashboardEntry read(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            return new DashboardEntry((EntryKind)registryFriendlyByteBuf.readEnum(EntryKind.class), (AEKey)AEKey.STREAM_CODEC.decode(registryFriendlyByteBuf), registryFriendlyByteBuf.readVarLong(), registryFriendlyByteBuf.readLong(), registryFriendlyByteBuf.readLong(), registryFriendlyByteBuf.readVarLong(), registryFriendlyByteBuf.readBoolean(), (ProductionMonitorBlockEntity.AlarmMode)registryFriendlyByteBuf.readEnum(ProductionMonitorBlockEntity.AlarmMode.class), registryFriendlyByteBuf.readLong(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarLong(), (ProductionMonitorBlockEntity.AlarmState)registryFriendlyByteBuf.readEnum(ProductionMonitorBlockEntity.AlarmState.class));
        }
    }

    public record QuantumLink(ResourceLocation dimensionA, BlockPos posA,
                              ResourceLocation dimensionB, BlockPos posB) {
        private void write(RegistryFriendlyByteBuf buf) {
            ResourceLocation.STREAM_CODEC.encode(buf, this.dimensionA);
            buf.writeBlockPos(this.posA);
            ResourceLocation.STREAM_CODEC.encode(buf, this.dimensionB);
            buf.writeBlockPos(this.posB);
        }

        private static QuantumLink read(RegistryFriendlyByteBuf buf) {
            return new QuantumLink(
                    (ResourceLocation)ResourceLocation.STREAM_CODEC.decode(buf),
                    buf.readBlockPos(),
                    (ResourceLocation)ResourceLocation.STREAM_CODEC.decode(buf),
                    buf.readBlockPos());
        }

        private static QuantumLink from(ProductionMonitorBlockEntity.NetworkMapLink link) {
            return new QuantumLink(
                    link.dimensionA(), link.posA(),
                    link.dimensionB(), link.posB());
        }
    }

    public record WirelessLink(ResourceLocation dimension, BlockPos posA, BlockPos posB) {
        private void write(RegistryFriendlyByteBuf buf) {
            ResourceLocation.STREAM_CODEC.encode(buf, this.dimension);
            buf.writeBlockPos(this.posA);
            buf.writeBlockPos(this.posB);
        }

        private static WirelessLink read(RegistryFriendlyByteBuf buf) {
            return new WirelessLink(
                    (ResourceLocation)ResourceLocation.STREAM_CODEC.decode(buf),
                    buf.readBlockPos(),
                    buf.readBlockPos());
        }

        private static WirelessLink from(ProductionMonitorBlockEntity.NetworkMapWirelessLink link) {
            return new WirelessLink(link.dimension(), link.posA(), link.posB());
        }
    }

    public record MapNode(BlockPos pos, int blockStateId, ResourceLocation visualId, String name, ProductionMonitorBlockEntity.MapNodeState state, int channels, double idlePower, boolean device, ProductionMonitorBlockEntity.MapRenderKind renderKind, int side) {
        private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            registryFriendlyByteBuf.writeBlockPos(this.pos);
            registryFriendlyByteBuf.writeVarInt(this.blockStateId);
            ResourceLocation.STREAM_CODEC.encode(registryFriendlyByteBuf, this.visualId);
            registryFriendlyByteBuf.writeUtf(this.name, 128);
            registryFriendlyByteBuf.writeEnum((Enum)this.state);
            registryFriendlyByteBuf.writeVarInt(this.channels);
            registryFriendlyByteBuf.writeDouble(this.idlePower);
            registryFriendlyByteBuf.writeBoolean(this.device);
            registryFriendlyByteBuf.writeEnum((Enum)this.renderKind);
            registryFriendlyByteBuf.writeByte(this.side);
        }

        private static MapNode read(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            return new MapNode(registryFriendlyByteBuf.readBlockPos(), registryFriendlyByteBuf.readVarInt(), (ResourceLocation)ResourceLocation.STREAM_CODEC.decode(registryFriendlyByteBuf), registryFriendlyByteBuf.readUtf(128), (ProductionMonitorBlockEntity.MapNodeState)registryFriendlyByteBuf.readEnum(ProductionMonitorBlockEntity.MapNodeState.class), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readDouble(), registryFriendlyByteBuf.readBoolean(), (ProductionMonitorBlockEntity.MapRenderKind)registryFriendlyByteBuf.readEnum(ProductionMonitorBlockEntity.MapRenderKind.class), registryFriendlyByteBuf.readByte());
        }

        private static MapNode from(ProductionMonitorBlockEntity.NetworkMapNode networkMapNode) {
            return new MapNode(networkMapNode.pos(), networkMapNode.blockStateId(), networkMapNode.visualId(), networkMapNode.name(), networkMapNode.state(), networkMapNode.channels(), networkMapNode.idlePower(), networkMapNode.device(), networkMapNode.renderKind(), networkMapNode.side());
        }
    }
}

