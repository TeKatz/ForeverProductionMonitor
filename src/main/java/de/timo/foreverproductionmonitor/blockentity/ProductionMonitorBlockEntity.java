/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  appeng.api.config.PowerUnit
 *  appeng.api.implementations.blockentities.IChestOrDrive
 *  appeng.api.networking.GridFlags
 *  appeng.api.networking.IGrid
 *  appeng.api.networking.IGridMultiblock
 *  appeng.api.networking.IGridNode
 *  appeng.api.networking.IManagedGridNode
 *  appeng.api.networking.energy.IEnergyService
 *  appeng.api.stacks.AEFluidKey
 *  appeng.api.stacks.AEItemKey
 *  appeng.api.stacks.AEKey
 *  appeng.api.storage.cells.StorageCell
 *  appeng.blockentity.grid.AENetworkedBlockEntity
 *  appeng.core.definitions.AEBlocks
 *  appeng.parts.storagebus.StorageBusPart
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 */
package de.timo.foreverproductionmonitor.blockentity;

import appeng.api.config.PowerUnit;
import appeng.api.implementations.blockentities.IChestOrDrive;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridMultiblock;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.cells.StorageCell;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.blockentity.qnb.QuantumBridgeBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.parts.storagebus.StorageBusPart;
import de.timo.foreverproductionmonitor.ModContent;
import de.timo.foreverproductionmonitor.block.ProductionMonitorBlock;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class ProductionMonitorBlockEntity
extends AENetworkedBlockEntity {
    public static final int MAX_DASHBOARD_PINS = 24;
    public static final int DEFAULT_SAMPLE_INTERVAL_TICKS = 100;
    public static final int MIN_SAMPLE_INTERVAL_TICKS = 20;
    private static final int HISTORY_TICKS = 1200;
    private static final long STALE_TICKS = 6000L;
    private static final long DEVICE_CACHE_TICKS = 20L;
    private final Map<AEKey, RateRecord> records = new HashMap<AEKey, RateRecord>();
    private IGrid sampledGrid;
    private long lastSampleTick = -1L;
    private int requestedSampleIntervalTicks = 100;
    private long sampleIntervalRequestExpiresAt = -1L;
    private boolean warmingUp = true;
    private long storedEnergyFe;
    private long currentEnergyPerMinute;
    private long energyLastChangedTick;
    private boolean infiniteEnergy;
    private final RateHistory energyHistory = new RateHistory();
    private IGrid deviceSnapshotGrid;
    private long deviceSnapshotTick = -1L;
    private DeviceSnapshot deviceSnapshot = DeviceSnapshot.EMPTY;
    private final List<DashboardPin> dashboardPins = new ArrayList<DashboardPin>();
    private final Set<UUID> linkedTablets = new HashSet<>();

    public ProductionMonitorBlockEntity(BlockPos blockPos, BlockState blockState) {
        super((BlockEntityType)ModContent.PRODUCTION_MONITOR_BLOCK_ENTITY.get(), blockPos, blockState);
        this.getMainNode().setFlags(new GridFlags[]{GridFlags.REQUIRE_CHANNEL}).setIdlePowerUsage(1.0);
        this.setMainNodeVisual();
    }

    private void setMainNodeVisual() {
        IManagedGridNode iManagedGridNode = this.getMainNode();
        for (Method method : iManagedGridNode.getClass().getMethods()) {
            if (!method.getName().equals("setVisualRepresentation") || method.getParameterCount() != 1) continue;
            Class<?> clazz = method.getParameterTypes()[0];
            Object itemStack = clazz.isAssignableFrom(ItemStack.class) ? new ItemStack((ItemLike)ModContent.PRODUCTION_MONITOR.get()) : (clazz.isAssignableFrom(AEItemKey.class) ? AEItemKey.of((ItemLike)ModContent.PRODUCTION_MONITOR.get()) : null);
            if (itemStack == null) continue;
            try {
                method.invoke(iManagedGridNode, itemStack);
                return;
            }
            catch (ReflectiveOperationException | RuntimeException exception) {
                // empty catch block
            }
        }
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, ProductionMonitorBlockEntity productionMonitorBlockEntity) {
        boolean bl;
        productionMonitorBlockEntity.syncVisualState();
        long l = level.getGameTime();
        int n = productionMonitorBlockEntity.effectiveSampleInterval(l);
        boolean bl2 = bl = productionMonitorBlockEntity.lastSampleTick < 0L && l % (long)n == (long)Math.floorMod(blockPos.asLong(), n);
        if (bl || productionMonitorBlockEntity.lastSampleTick >= 0L && l - productionMonitorBlockEntity.lastSampleTick >= (long)n) {
            productionMonitorBlockEntity.sample(l);
        }
    }

    public void requestSampleInterval(int n, long l) {
        this.requestedSampleIntervalTicks = Math.max(20, Math.min(100, n));
        this.sampleIntervalRequestExpiresAt = l + 300L;
    }

    private int effectiveSampleInterval(long l) {
        return l <= this.sampleIntervalRequestExpiresAt ? this.requestedSampleIntervalTicks : 100;
    }

    private void sample(long l) {
        Object object;
        IGrid iGrid;
        IGrid iGrid2 = iGrid = this.getMainNode().isOnline() ? this.getMainNode().getGrid() : null;
        if (iGrid == null) {
            this.sampledGrid = null;
            this.lastSampleTick = -1L;
            this.warmingUp = true;
            this.records.clear();
            this.clearEnergy();
            return;
        }
        HashMap<AEKey, Long> hashMap = new HashMap<AEKey, Long>();
        for (Object2LongMap.Entry entry2 : iGrid.getStorageService().getCachedInventory()) {
            object = (AEKey)entry2.getKey();
            if (!(object instanceof AEItemKey) && !(object instanceof AEFluidKey) || entry2.getLongValue() <= 0L) continue;
            hashMap.put((AEKey)object, entry2.getLongValue());
        }
        if (this.sampledGrid != iGrid || this.lastSampleTick < 0L) {
            this.records.clear();
            hashMap.forEach((aEKey, l2) -> this.records.put((AEKey)aEKey, RateRecord.baseline(l2, l)));
            this.sampleEnergy(iGrid, 0L, l, true);
            this.sampledGrid = iGrid;
            this.lastSampleTick = l;
            this.warmingUp = true;
            return;
        }
        long l3 = Math.max(1L, l - this.lastSampleTick);
        Set<AEKey> keys = new HashSet<AEKey>(this.records.keySet());
        keys.addAll(hashMap.keySet());
        Iterator iterator = keys.iterator();
        while (iterator.hasNext()) {
            AEKey aEKey2 = (AEKey)iterator.next();
            long l4 = hashMap.getOrDefault(aEKey2, 0L);
            RateRecord rateRecord = this.records.computeIfAbsent(aEKey2, aEKey -> RateRecord.baseline(0L, this.lastSampleTick));
            rateRecord.update(l4, l3, l);
        }
        this.sampleEnergy(iGrid, l3, l, false);
        this.records.entrySet().removeIf(entry -> ((RateRecord)entry.getValue()).amount == 0L && ((RateRecord)entry.getValue()).averagePerMinute() == 0L && l - ((RateRecord)entry.getValue()).lastChangedTick > 6000L);
        this.sampledGrid = iGrid;
        this.lastSampleTick = l;
        this.warmingUp = false;
        this.evaluateAlarms(l);
    }

    public List<DashboardEntrySnapshot> dashboardSnapshot(long l) {
        ArrayList<DashboardEntrySnapshot> arrayList = new ArrayList<DashboardEntrySnapshot>(this.dashboardPins.size());
        for (DashboardPin dashboardPin : this.dashboardPins) {
            AEKey aEKey = this.resolveKey(dashboardPin);
            if (aEKey == null) continue;
            DashboardValues dashboardValues = this.valuesFor(dashboardPin.kind, aEKey, l);
            arrayList.add(new DashboardEntrySnapshot(dashboardPin.kind, aEKey, dashboardValues.amount, dashboardValues.currentPerMinute, dashboardValues.averagePerMinute, dashboardValues.secondsSinceChange, dashboardValues.infinite, dashboardPin.rule, this.alarmState(dashboardPin, dashboardValues, l)));
        }
        return arrayList;
    }

    public boolean addDashboardPin(DashboardKind dashboardKind, AEKey aEKey) {
        if (aEKey == null || this.dashboardPins.size() >= 24 || this.findPin(dashboardKind, aEKey) >= 0) {
            return false;
        }
        this.dashboardPins.add(new DashboardPin(dashboardKind, aEKey, aEKey.getId(), AlarmRule.NONE));
        this.setChanged();
        return true;
    }

    public boolean removeDashboardPin(DashboardKind dashboardKind, AEKey aEKey) {
        int n = this.findPin(dashboardKind, aEKey);
        if (n < 0) {
            return false;
        }
        this.dashboardPins.remove(n);
        this.setChanged();
        return true;
    }

    public boolean updateDashboardPin(DashboardKind dashboardKind, AEKey aEKey, AlarmRule alarmRule) {
        int n = this.findPin(dashboardKind, aEKey);
        if (n < 0) {
            return false;
        }
        DashboardPin dashboardPin = this.dashboardPins.get(n);
        dashboardPin.rule = alarmRule == null ? AlarmRule.NONE : alarmRule.sanitized();
        dashboardPin.pendingSince = -1L;
        dashboardPin.active = false;
        this.setChanged();
        return true;
    }

    public boolean moveDashboardPin(DashboardKind dashboardKind, AEKey aEKey, int n) {
        int n2 = this.findPin(dashboardKind, aEKey);
        int n3 = Math.max(0, Math.min(this.dashboardPins.size() - 1, n2 + Integer.signum(n)));
        if (n2 < 0 || n3 == n2) {
            return false;
        }
        DashboardPin dashboardPin = this.dashboardPins.remove(n2);
        this.dashboardPins.add(n3, dashboardPin);
        this.setChanged();
        return true;
    }

    private int findPin(DashboardKind dashboardKind, AEKey aEKey) {
        for (int i = 0; i < this.dashboardPins.size(); ++i) {
            DashboardPin dashboardPin = this.dashboardPins.get(i);
            AEKey aEKey2 = this.resolveKey(dashboardPin);
            if (dashboardPin.kind != dashboardKind || !aEKey.equals(aEKey2) && !aEKey.getId().equals((Object)dashboardPin.resourceId)) continue;
            return i;
        }
        return -1;
    }

    private AEKey resolveKey(DashboardPin dashboardPin) {
        if (dashboardPin.kind == DashboardKind.ENERGY) {
            return AEItemKey.of((ItemLike)AEBlocks.ENERGY_CELL);
        }
        if (dashboardPin.key != null) {
            return dashboardPin.key;
        }
        for (AEKey aEKey : this.records.keySet()) {
            if (!aEKey.getId().equals((Object)dashboardPin.resourceId) || dashboardPin.kind == DashboardKind.FLUID != aEKey instanceof AEFluidKey) continue;
            dashboardPin.key = aEKey;
            return aEKey;
        }
        return null;
    }

    private DashboardValues valuesFor(DashboardKind dashboardKind, AEKey aEKey, long l) {
        if (dashboardKind == DashboardKind.ENERGY) {
            EnergySnapshot energySnapshot = this.energySnapshot(l);
            return new DashboardValues(energySnapshot.storedFe, energySnapshot.currentPerMinute, energySnapshot.averagePerMinute, energySnapshot.secondsSinceChange, energySnapshot.infinite);
        }
        RateRecord rateRecord = this.records.get(aEKey);
        if (rateRecord == null) {
            return new DashboardValues(0L, 0L, 0L, 0L, false);
        }
        long l2 = Math.max(0L, (l - rateRecord.lastChangedTick) / 20L);
        long l3 = aEKey instanceof AEFluidKey ? 2147483647000L : Integer.MAX_VALUE;
        return new DashboardValues(rateRecord.amount, rateRecord.currentPerMinute, rateRecord.averagePerMinute(), l2, rateRecord.amount == Long.MAX_VALUE || rateRecord.amount >= l3);
    }

    private void evaluateAlarms(long l) {
        for (DashboardPin dashboardPin : this.dashboardPins) {
            AEKey aEKey = this.resolveKey(dashboardPin);
            if (aEKey == null) continue;
            this.alarmState(dashboardPin, this.valuesFor(dashboardPin.kind, aEKey, l), l);
        }
    }

    private AlarmState alarmState(DashboardPin dashboardPin, DashboardValues dashboardValues, long l) {
        int n;
        boolean bl;
        AlarmRule alarmRule = dashboardPin.rule;
        if (alarmRule.mode == AlarmMode.NONE || dashboardValues.infinite && alarmRule.isAmountRule()) {
            dashboardPin.pendingSince = -1L;
            dashboardPin.active = false;
            return AlarmState.NORMAL;
        }
        long l2 = switch (alarmRule.mode) {
            default -> throw new IncompatibleClassChangeError();
            case AlarmMode.AMOUNT_BELOW, AlarmMode.AMOUNT_ABOVE -> dashboardValues.amount;
            case AlarmMode.RATE_BELOW, AlarmMode.RATE_ABOVE -> dashboardValues.averagePerMinute;
            case AlarmMode.STALLED -> dashboardValues.secondsSinceChange;
            case AlarmMode.NONE -> 0L;
        };
        switch (alarmRule.mode) {
            default: {
                throw new IncompatibleClassChangeError();
            }
            case AMOUNT_BELOW: {
                if (dashboardPin.active) {
                    if (l2 < ProductionMonitorBlockEntity.saturatingAdd(alarmRule.threshold, alarmRule.hysteresis)) {
                        bl = true;
                        break;
                    }
                    bl = false;
                    break;
                }
                if (l2 < alarmRule.threshold) {
                    bl = true;
                    break;
                }
                bl = false;
                break;
            }
            case AMOUNT_ABOVE: {
                if (dashboardPin.active) {
                    if (l2 > Math.max(0L, alarmRule.threshold - alarmRule.hysteresis)) {
                        bl = true;
                        break;
                    }
                    bl = false;
                    break;
                }
                if (l2 > alarmRule.threshold) {
                    bl = true;
                    break;
                }
                bl = false;
                break;
            }
            case RATE_BELOW: {
                if (dashboardPin.active) {
                    if (l2 < ProductionMonitorBlockEntity.saturatingAdd(alarmRule.threshold, alarmRule.hysteresis)) {
                        bl = true;
                        break;
                    }
                    bl = false;
                    break;
                }
                if (l2 < alarmRule.threshold) {
                    bl = true;
                    break;
                }
                bl = false;
                break;
            }
            case RATE_ABOVE: {
                if (dashboardPin.active) {
                    if (l2 > alarmRule.threshold - alarmRule.hysteresis) {
                        bl = true;
                        break;
                    }
                    bl = false;
                    break;
                }
                if (l2 > alarmRule.threshold) {
                    bl = true;
                    break;
                }
                bl = false;
                break;
            }
            case STALLED: {
                if (dashboardValues.secondsSinceChange >= (long)Math.max(1, alarmRule.delaySeconds)) {
                    bl = true;
                    break;
                }
                bl = false;
                break;
            }
            case NONE: {
                bl = false;
            }
        }
        if (!bl) {
            dashboardPin.pendingSince = -1L;
            dashboardPin.active = false;
            return AlarmState.NORMAL;
        }
        if (dashboardPin.active) {
            return AlarmState.ACTIVE;
        }
        if (dashboardPin.pendingSince < 0L) {
            dashboardPin.pendingSince = l;
        }
        int n2 = n = alarmRule.mode == AlarmMode.STALLED ? 0 : alarmRule.delaySeconds;
        if (l - dashboardPin.pendingSince >= (long)n * 20L) {
            dashboardPin.active = true;
            return AlarmState.ACTIVE;
        }
        return AlarmState.PENDING;
    }

    public void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        ListTag listTag = new ListTag();
        for (DashboardPin dashboardPin : this.dashboardPins) {
            CompoundTag compoundTag2 = new CompoundTag();
            compoundTag2.putString("Kind", dashboardPin.kind.name());
            compoundTag2.putString("Id", dashboardPin.resourceId.toString());
            AEKey aEKey = this.resolveKey(dashboardPin);
            if (aEKey instanceof AEItemKey) {
                AEItemKey aEItemKey = (AEItemKey)aEKey;
                compoundTag2.put("Item", aEItemKey.getReadOnlyStack().copyWithCount(1).save(provider));
            }
            compoundTag2.putString("Alarm", dashboardPin.rule.mode.name());
            compoundTag2.putLong("Threshold", dashboardPin.rule.threshold);
            compoundTag2.putInt("Delay", dashboardPin.rule.delaySeconds);
            compoundTag2.putLong("Hysteresis", dashboardPin.rule.hysteresis);
            listTag.add(compoundTag2);
        }
        compoundTag.put("DashboardPins", (Tag)listTag);

        CompoundTag linkedTabletsTag = new CompoundTag();
        for (UUID tabletId : this.linkedTablets) {
            linkedTabletsTag.putBoolean(tabletId.toString(), true);
        }
        compoundTag.put("LinkedTablets", linkedTabletsTag);
    }

    public void loadTag(CompoundTag compoundTag, HolderLookup.Provider provider) {
        this.dashboardPins.clear();
        this.linkedTablets.clear();
        CompoundTag linkedTabletsTag = compoundTag.getCompound("LinkedTablets");
        for (String key : linkedTabletsTag.getAllKeys()) {
            try {
                this.linkedTablets.add(UUID.fromString(key));
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed legacy/custom data instead of breaking monitor loading.
            }
        }
        ListTag listTag = compoundTag.getList("DashboardPins", 10);
        for (int i = 0; i < listTag.size() && this.dashboardPins.size() < 24; ++i) {
            CompoundTag compoundTag2 = listTag.getCompound(i);
            try {
                AlarmMode alarmMode;
                DashboardKind dashboardKind = DashboardKind.valueOf(compoundTag2.getString("Kind"));
                ResourceLocation resourceLocation = ResourceLocation.tryParse((String)compoundTag2.getString("Id"));
                if (resourceLocation == null) continue;
                AEItemKey aEItemKey = null;
                ItemStack itemStack = ItemStack.parseOptional(provider, compoundTag2.getCompound("Item"));
                if (compoundTag2.contains("Item", 10) && !itemStack.isEmpty()) {
                    aEItemKey = AEItemKey.of(itemStack);
                }
                alarmMode = AlarmMode.valueOf(compoundTag2.getString("Alarm"));
                AlarmRule alarmRule = new AlarmRule(alarmMode, compoundTag2.getLong("Threshold"), compoundTag2.getInt("Delay"), compoundTag2.getLong("Hysteresis")).sanitized();
                this.dashboardPins.add(new DashboardPin(dashboardKind, (AEKey)aEItemKey, resourceLocation, alarmRule));
                continue;
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
        }
    }

    public boolean isMonitorOnline() {
        return this.getMainNode().isOnline();
    }

    public void linkTablet(UUID tabletId) {
        if (tabletId != null && this.linkedTablets.add(tabletId)) {
            this.setChanged();
            this.syncVisualState();
        }
    }

    public void unlinkTablet(UUID tabletId) {
        if (tabletId != null && this.linkedTablets.remove(tabletId)) {
            this.setChanged();
            this.syncVisualState();
        }
    }

    public boolean hasLinkedTablet() {
        return !this.linkedTablets.isEmpty();
    }

    private void syncVisualState() {
        Level level = this.getLevel();
        if (level == null || level.isClientSide) {
            return;
        }

        BlockState state = this.getBlockState();
        if (!state.hasProperty(ProductionMonitorBlock.VISUAL_STATE)) {
            return;
        }

        ProductionMonitorBlock.VisualState visualState;
        if (!this.isMonitorOnline()) {
            visualState = ProductionMonitorBlock.VisualState.OFFLINE;
        } else if (this.hasLinkedTablet()) {
            visualState = ProductionMonitorBlock.VisualState.LINKED;
        } else {
            visualState = ProductionMonitorBlock.VisualState.ONLINE;
        }

        if (state.getValue(ProductionMonitorBlock.VISUAL_STATE) != visualState) {
            level.setBlock(
                    this.getBlockPos(),
                    state.setValue(ProductionMonitorBlock.VISUAL_STATE, visualState),
                    Block.UPDATE_CLIENTS);
        }
    }

    public boolean isWarmingUp() {
        return this.warmingUp;
    }

    public long getLastSampleTick() {
        return this.lastSampleTick;
    }

    public List<SnapshotEntry> snapshot(long l) {
        ArrayList<SnapshotEntry> arrayList = new ArrayList<SnapshotEntry>(this.records.size());
        this.records.forEach((aEKey, rateRecord) -> arrayList.add(new SnapshotEntry((AEKey)aEKey, rateRecord.amount, rateRecord.currentPerMinute, rateRecord.averagePerMinute(), Math.max(0L, (l - rateRecord.lastChangedTick) / 20L))));
        return arrayList;
    }

    public EnergySnapshot energySnapshot(long l) {
        return new EnergySnapshot(this.storedEnergyFe, this.currentEnergyPerMinute, this.energyHistory.average(), Math.max(0L, (l - this.energyLastChangedTick) / 20L), this.infiniteEnergy);
    }

    public StorageCapacitySnapshot storageCapacitySnapshot() {
        IGrid iGrid;
        IGrid iGrid2 = iGrid = this.getMainNode().isOnline() ? this.getMainNode().getGrid() : null;
        if (iGrid == null) {
            return StorageCapacitySnapshot.EMPTY;
        }
        long l = 0L;
        long l2 = 0L;
        long l3 = 0L;
        long l4 = 0L;
        int n = 0;
        int n2 = 0;
        int n3 = 0;
        int n4 = 0;
        Set<Object> set = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());
        for (Class clazz : iGrid.getMachineClasses()) {
            if (StorageBusPart.class.isAssignableFrom(clazz)) {
                n4 += iGrid.getActiveMachines(clazz).size();
            }
            if (!IChestOrDrive.class.isAssignableFrom(clazz)) continue;
            for (Object e : iGrid.getMachines(clazz)) {
                if (!(e instanceof IChestOrDrive)) continue;
                IChestOrDrive iChestOrDrive = (IChestOrDrive)e;
                if (!set.add(e) || !iChestOrDrive.isPowered()) continue;
                for (int i = 0; i < iChestOrDrive.getCellCount(); ++i) {
                    StorageCell storageCell = iChestOrDrive.getOriginalCellInventory(i);
                    if (storageCell == null) continue;
                    Long l5 = ProductionMonitorBlockEntity.metric(storageCell, "getUsedBytes");
                    Long l6 = ProductionMonitorBlockEntity.metric(storageCell, "getTotalBytes");
                    Long l7 = ProductionMonitorBlockEntity.metric(storageCell, "getStoredItemTypes");
                    Long l8 = ProductionMonitorBlockEntity.metric(storageCell, "getTotalItemTypes");
                    if (l5 != null && l6 != null && l7 != null && l8 != null) {
                        l = ProductionMonitorBlockEntity.saturatingAdd(l, Math.max(0L, l5));
                        l2 = ProductionMonitorBlockEntity.saturatingAdd(l2, Math.max(0L, l6));
                        l3 = ProductionMonitorBlockEntity.saturatingAdd(l3, Math.max(0L, l7));
                        l4 = ProductionMonitorBlockEntity.saturatingAdd(l4, Math.max(0L, l8));
                        ++n;
                        continue;
                    }
                    String string = storageCell.getClass().getName().toLowerCase(Locale.ROOT);
                    if (string.contains("creative") || string.contains("infinite")) {
                        ++n2;
                        continue;
                    }
                    ++n3;
                }
            }
        }
        return new StorageCapacitySnapshot(l, l2, l3, l4, n, n2, n3, n4);
    }

    public DeviceSnapshot deviceSnapshot(long l) {
        IGrid iGrid;
        IGrid iGrid2 = iGrid = this.getMainNode().isOnline() ? this.getMainNode().getGrid() : null;
        if (iGrid == null) {
            this.deviceSnapshotGrid = null;
            this.deviceSnapshotTick = -1L;
            this.deviceSnapshot = DeviceSnapshot.EMPTY;
            return this.deviceSnapshot;
        }
        if (this.deviceSnapshotGrid == iGrid && this.deviceSnapshotTick >= 0L && l - this.deviceSnapshotTick < 20L) {
            return this.deviceSnapshot;
        }
        LinkedHashMap<DeviceGroupKey, MutableDeviceGroup> linkedHashMap = new LinkedHashMap<DeviceGroupKey, MutableDeviceGroup>();
        Set set = Collections.newSetFromMap(new IdentityHashMap());
        for (IGridNode iGridNode : iGrid.getNodes()) {
            LogicalDevice logicalDevice;
            Object object;
            IGridMultiblock iGridMultiblock;
            if (set.contains(iGridNode) || !iGridNode.hasFlag(GridFlags.REQUIRE_CHANNEL)) continue;
            IGridMultiblock iGridMultiblock2 = iGridMultiblock = iGridNode.hasFlag(GridFlags.MULTIBLOCK) ? (IGridMultiblock)iGridNode.getService(IGridMultiblock.class) : null;
            if (iGridMultiblock != null) {
                List<IGridNode> nodes = new ArrayList<IGridNode>();
                iGridMultiblock.getMultiblockNodes().forEachRemaining(nodes::add);
                if (nodes.isEmpty()) {
                    set.add(iGridNode);
                    logicalDevice = ProductionMonitorBlockEntity.inspectDevice(List.of(iGridNode));
                } else {
                    nodes.removeIf(Objects::isNull);
                    if (nodes.isEmpty()) {
                        set.add(iGridNode);
                        logicalDevice = ProductionMonitorBlockEntity.inspectDevice(List.of(iGridNode));
                    } else {
                        set.addAll(nodes);
                        logicalDevice = ProductionMonitorBlockEntity.inspectDevice(nodes);
                    }
                }
            } else {
                set.add(iGridNode);
                logicalDevice = ProductionMonitorBlockEntity.inspectDevice(List.of(iGridNode));
            }
            object = new DeviceGroupKey(logicalDevice.visual(), logicalDevice.name());
            linkedHashMap.computeIfAbsent((DeviceGroupKey)object, deviceGroupKey -> new MutableDeviceGroup(logicalDevice.visual(), logicalDevice.name())).add(logicalDevice);
        }
        List<DeviceGroupSnapshot> list = linkedHashMap.values().stream().map(MutableDeviceGroup::finish).toList();
        int n = 0;
        int n2 = 0;
        int n3 = 0;
        int n4 = 0;
        int n5 = 0;
        int n6 = 0;
        double d = 0.0;
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            DeviceGroupSnapshot deviceGroupSnapshot = (DeviceGroupSnapshot)iterator.next();
            n += deviceGroupSnapshot.count();
            n2 += deviceGroupSnapshot.active();
            n3 += deviceGroupSnapshot.missingChannel();
            n4 += deviceGroupSnapshot.unpowered();
            n5 += deviceGroupSnapshot.booting();
            n6 += deviceGroupSnapshot.assignedChannels();
            d += deviceGroupSnapshot.idlePower();
        }
        this.deviceSnapshotGrid = iGrid;
        this.deviceSnapshotTick = l;
        int n7 = ProductionMonitorBlockEntity.networkChannelCount(iGrid, n6);
        int n8 = Math.max(0, n7 - n6);
        this.deviceSnapshot = new DeviceSnapshot(list, n, n2, n3, n4, n5, n6, n7, n8, d);
        return this.deviceSnapshot;
    }

    public NetworkMapSnapshot networkMapSnapshot(ResourceLocation requestedDimension) {
        IGrid grid = this.getMainNode().isOnline() ? this.getMainNode().getGrid() : null;
        Level monitorLevel = this.getLevel();
        ResourceLocation fallbackDimension = monitorLevel == null
                ? requestedDimension
                : monitorLevel.dimension().location();
        ResourceLocation viewDimension = requestedDimension == null ? fallbackDimension : requestedDimension;
        if (grid == null || monitorLevel == null) {
            return NetworkMapSnapshot.empty(viewDimension);
        }

        LinkedHashMap<ResourceLocation, ServerLevel> levels = new LinkedHashMap<>();
        ArrayList<MapNodeSource> viewSources = new ArrayList<>();
        HashMap<Long, List<QuantumBridgeEndpoint>> quantumEndpoints = new HashMap<>();
        LinkedHashMap<WirelessConnectorEndpoint, BlockPos> wirelessEndpoints = new LinkedHashMap<>();

        // IGrid is already the authoritative logical AE network. In particular, an active
        // Quantum Bridge makes both physical sides part of this same grid. Wireless terminals
        // do not create bridge nodes here; they resolve the grid through a physical WAP.
        for (IGridNode node : grid.getNodes()) {
            ServerLevel nodeLevel;
            try {
                nodeLevel = node.getLevel();
            } catch (RuntimeException ignored) {
                continue;
            }

            Object owner = node.getOwner();
            DeviceLocation location = ProductionMonitorBlockEntity.locationOf(node, owner);
            if (location == null) {
                continue;
            }

            ResourceLocation dimension = nodeLevel.dimension().location();
            levels.putIfAbsent(dimension, nodeLevel);
            if (dimension.equals(viewDimension)) {
                // Only the requested dimension is rendered. Keep global dimension/link
                // discovery network-wide, but avoid retaining MapNodeSource objects for
                // dimensions that cannot contribute to this snapshot.
                viewSources.add(new MapNodeSource(node, owner, location));
            }

            // Only a real, formed ME Quantum Link Chamber can become a navigable map link.
            // This intentionally excludes WAPs/wireless terminals and unrelated "quantum"
            // devices such as AdvancedAE's Quantum Computer.
            if (owner instanceof QuantumBridgeBlockEntity bridge
                    && bridge.getBlockState().is(AEBlocks.QUANTUM_LINK.block())
                    && bridge.isFormed()
                    && bridge.isPowered()) {
                // On the dedicated/server side, hasQES() reflects a synchronization flag in
                // the formed-state byte and is not authoritative for the chamber inventory.
                // The real entangled-singularity identity is the non-zero frequency stored
                // on the QES itself, which is also what AE2's QuantumCluster pairs on.
                long frequency = bridge.getQEFrequency();
                if (frequency != 0L) {
                    QuantumBridgeEndpoint endpoint = new QuantumBridgeEndpoint(
                            dimension, bridge.getBlockPos().immutable());
                    List<QuantumBridgeEndpoint> endpoints = quantumEndpoints.computeIfAbsent(
                            frequency, ignored -> new ArrayList<>());
                    if (!endpoints.contains(endpoint)) {
                        endpoints.add(endpoint);
                    }
                }
            }

            // ExtendedAE's ME Wireless Connector exposes its paired block position through
            // getOtherSide(). Keep this integration optional by using the stable block id and
            // reflection instead of introducing a hard ExtendedAE dependency.
            BlockState ownerState = nodeLevel.getBlockState(location.pos());
            ResourceLocation ownerBlockId = BuiltInRegistries.BLOCK.getKey(ownerState.getBlock());
            if ("extendedae".equals(ownerBlockId.getNamespace())
                    && "wireless_connect".equals(ownerBlockId.getPath())
                    && Boolean.TRUE.equals(ProductionMonitorBlockEntity.invokeNoArg(owner, "isConnected"))) {
                Object otherSide = ProductionMonitorBlockEntity.invokeNoArg(owner, "getOtherSide");
                if (otherSide instanceof BlockPos otherPos && !otherPos.equals(location.pos())) {
                    wirelessEndpoints.put(
                            new WirelessConnectorEndpoint(dimension, location.pos().immutable()),
                            otherPos.immutable());
                }
            }
        }

        ArrayList<ResourceLocation> dimensions = new ArrayList<>(levels.keySet());
        dimensions.sort((a, b) -> a.toString().compareToIgnoreCase(b.toString()));

        ArrayList<NetworkMapLink> quantumLinks = new ArrayList<>();
        for (List<QuantumBridgeEndpoint> endpoints : quantumEndpoints.values()) {
            // A valid QES pair has exactly two physical centers. Refuse ambiguous/corrupt
            // frequencies instead of guessing a destination.
            if (endpoints.size() != 2) {
                continue;
            }
            QuantumBridgeEndpoint a = endpoints.get(0);
            QuantumBridgeEndpoint b = endpoints.get(1);
            quantumLinks.add(new NetworkMapLink(a.dimension(), a.pos(), b.dimension(), b.pos()));
        }

        ArrayList<NetworkMapWirelessLink> wirelessLinks = new ArrayList<>();
        for (Map.Entry<WirelessConnectorEndpoint, BlockPos> entry : wirelessEndpoints.entrySet()) {
            WirelessConnectorEndpoint a = entry.getKey();
            BlockPos bPos = entry.getValue();
            WirelessConnectorEndpoint b = new WirelessConnectorEndpoint(a.dimension(), bPos);
            BlockPos reciprocal = wirelessEndpoints.get(b);
            if (reciprocal == null || !reciprocal.equals(a.pos())) {
                continue;
            }

            // Both endpoints report each other. Add the physical pair only once.
            if (Long.compare(a.pos().asLong(), bPos.asLong()) < 0) {
                wirelessLinks.add(new NetworkMapWirelessLink(
                        a.dimension(), a.pos(), bPos));
            }
        }

        ServerLevel level = levels.get(viewDimension);
        if (level == null) {
            return new NetworkMapSnapshot(
                    viewDimension, dimensions, quantumLinks, wirelessLinks, List.of(), false);
        }

        ArrayList<MapNodeSource> mapSources = new ArrayList<>();
        HashSet<BlockPos> networkPositions = new HashSet<>();
        for (MapNodeSource source : viewSources) {
            BlockPos pos = source.location().pos();
            if (!level.hasChunkAt(pos)) {
                continue;
            }
            networkPositions.add(pos);
            mapSources.add(source);
        }

        LinkedHashMap<MapElementKey, MutableMapNode> map = new LinkedHashMap<>();
        Set<BlockPos> knownBlocks = new HashSet<>();

        for (MapNodeSource source : mapSources) {
            IGridNode node = source.node();
            Object owner = source.owner();
            DeviceLocation location = source.location();
            BlockPos blockPos = location.pos();
            BlockState blockState = level.getBlockState(blockPos);
            AEItemKey visual = node.getVisualRepresentation();
            if (owner instanceof ProductionMonitorBlockEntity) {
                visual = AEItemKey.of((ItemLike)ModContent.PRODUCTION_MONITOR.get());
            } else if (visual == null) {
                visual = ProductionMonitorBlockEntity.visualFromOwner(owner);
            }

            ResourceLocation visualId = visual == null
                    ? BuiltInRegistries.ITEM.getKey(blockState.getBlock().asItem())
                    : BuiltInRegistries.ITEM.getKey(visual.getItem());
            String name = visual == null
                    ? blockState.getBlock().getName().getString()
                    : visual.getDisplayName().getString();
            MapNodeState nodeState = ProductionMonitorBlockEntity.mapNodeState(node);
            MapRenderKind renderKind = ProductionMonitorBlockEntity.isMultipartHost(blockState)
                    || location.side() != -1 ? MapRenderKind.PART : MapRenderKind.BLOCK;
            MapElementKey key = new MapElementKey(
                    blockPos.immutable(),
                    renderKind,
                    renderKind == MapRenderKind.PART ? location.side() : -1,
                    renderKind == MapRenderKind.PART ? visualId : null);

            map.computeIfAbsent(key, ignored -> new MutableMapNode(
                    blockPos, Block.getId(blockState), visualId, renderKind, key.side()))
                    .merge(visualId, name, nodeState, node.getUsedChannels(),
                            node.getIdlePowerUsage(), node.hasFlag(GridFlags.REQUIRE_CHANNEL));
            ProductionMonitorBlockEntity.addKnownMultiblock(
                    level, blockPos, visualId, map, knownBlocks);
        }

        for (BlockPos blockPos : networkPositions) {
            for (Direction direction : Direction.values()) {
                BlockPos adjacentPos = blockPos.relative(direction);
                if (networkPositions.contains(adjacentPos) || !level.hasChunkAt(adjacentPos)) {
                    continue;
                }
                ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(
                        level.getBlockState(adjacentPos).getBlock());
                ProductionMonitorBlockEntity.addKnownMultiblock(
                        level, adjacentPos, blockId, map, knownBlocks);
                ProductionMonitorBlockEntity.addExternalMachine(level, adjacentPos, map);
            }
        }

        ProductionMonitorBlockEntity.discoverKnownMultiblocks(
                level, networkPositions, map, knownBlocks);
        List<NetworkMapNode> nodes = map.values().stream()
                .map(MutableMapNode::finish)
                .limit(NetworkMapSnapshot.MAX_NODES)
                .toList();

        return new NetworkMapSnapshot(
                viewDimension,
                dimensions,
                quantumLinks,
                wirelessLinks,
                nodes,
                map.size() > NetworkMapSnapshot.MAX_NODES);
    }

    private static void discoverKnownMultiblocks(Level level, Set<BlockPos> set, Map<MapElementKey, MutableMapNode> map, Set<BlockPos> set2) {
        HashSet<Long> hashSet = new HashSet<Long>();
        for (BlockPos blockPos : set) {
            int n = blockPos.getX() >> 4;
            int n2 = blockPos.getZ() >> 4;
            for (int i = n - 1; i <= n + 1; ++i) {
                for (int j = n2 - 1; j <= n2 + 1; ++j) {
                    long l = (long)i << 32 ^ (long)j & 0xFFFFFFFFL;
                    if (!hashSet.add(l) || !level.hasChunk(i, j)) continue;
                    for (BlockPos blockPos2 : level.getChunk(i, j).getBlockEntities().keySet()) {
                        ResourceLocation resourceLocation = BuiltInRegistries.BLOCK.getKey(level.getBlockState(blockPos2).getBlock());
                        if (!resourceLocation.getNamespace().equals("advanced_ae") || !resourceLocation.getPath().startsWith("quantum_") && !resourceLocation.getPath().equals("data_entangler")) continue;
                        ProductionMonitorBlockEntity.addKnownMultiblock(level, blockPos2, resourceLocation, map, set2);
                    }
                }
            }
        }
    }

    private static boolean isMultipartHost(BlockState blockState) {
        ResourceLocation resourceLocation = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());
        String string = resourceLocation.getPath();
        return string.contains("cable_bus") || string.contains("multipart") || string.contains("part_host");
    }

    private static void addExternalMachine(Level level, BlockPos blockPos, Map<MapElementKey, MutableMapNode> map) {
        BlockState blockState = level.getBlockState(blockPos);
        if (blockState.isAir() || ProductionMonitorBlockEntity.isMultipartHost(blockState) || level.getBlockEntity(blockPos) == null) {
            return;
        }
        ResourceLocation resourceLocation = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());
        if (!ProductionMonitorBlockEntity.isRelevantExternalMachine(resourceLocation)) {
            return;
        }
        ResourceLocation resourceLocation2 = BuiltInRegistries.ITEM.getKey(blockState.getBlock().asItem());
        if (resourceLocation2 == null || resourceLocation2.equals((Object)BuiltInRegistries.ITEM.getKey(Items.AIR))) {
            resourceLocation2 = resourceLocation;
        }
        MapElementKey mapElementKey2 = new MapElementKey(blockPos.immutable(), MapRenderKind.BLOCK, -1, null);
        ResourceLocation resourceLocation3 = resourceLocation2;
        map.computeIfAbsent(mapElementKey2, mapElementKey -> new MutableMapNode(blockPos, Block.getId((BlockState)blockState), resourceLocation3, MapRenderKind.BLOCK, -1)).merge(resourceLocation3, blockState.getBlock().getName().getString(), MapNodeState.CONNECTED, 0, 0.0, false);
    }

    private static boolean isRelevantExternalMachine(ResourceLocation resourceLocation) {
        String string = resourceLocation.getNamespace();
        if (!(string.equals("mekanism") || string.equals("mekanismgenerators") || string.equals("oritech") || string.equals("evolvedmekanism"))) {
            return false;
        }
        String string2 = resourceLocation.getPath();
        return !string2.contains("cable") && !string2.contains("wire") && !string2.contains("pipe") && !string2.contains("tube") && !string2.contains("transporter") && !string2.contains("conductor") && !string2.contains("duct") && !string2.contains("connector") && !string2.contains("structural_glass") && !string2.contains("casing") && !string2.contains("valve");
    }

    private static void addKnownMultiblock(Level level, BlockPos blockPos, ResourceLocation resourceLocation, Map<MapElementKey, MutableMapNode> map, Set<BlockPos> set) {
        if (set.contains(blockPos)) {
            return;
        }
        ResourceLocation resourceLocation2 = BuiltInRegistries.BLOCK.getKey(level.getBlockState(blockPos).getBlock());
        String string2 = (String.valueOf(resourceLocation) + " " + String.valueOf(resourceLocation2)).toLowerCase(Locale.ROOT);
        String string = string2.contains("quantum") || string2.contains("data_entangler") ? "quantum" : (string2.contains("assembler_matrix") ? "assembler_matrix" : null);
        if (string == null) {
            return;
        }
        for (int i = -8; i <= 8; ++i) {
            for (int j = -8; j <= 8; ++j) {
                for (int k = -8; k <= 8; ++k) {
                    BlockPos blockPos2 = blockPos.offset(i, j, k);
                    BlockState blockState = level.getBlockState(blockPos2);
                    ResourceLocation resourceLocation3 = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());
                    if (!ProductionMonitorBlockEntity.isKnownMultiblockMember(resourceLocation3, string)) continue;
                    set.add(blockPos2.immutable());
                    ResourceLocation resourceLocation4 = BuiltInRegistries.ITEM.getKey(blockState.getBlock().asItem());
                    MapElementKey mapElementKey2 = new MapElementKey(blockPos2.immutable(), MapRenderKind.BLOCK, -1, null);
                    map.computeIfAbsent(mapElementKey2, mapElementKey -> new MutableMapNode(blockPos2, Block.getId((BlockState)blockState), resourceLocation4, MapRenderKind.BLOCK, -1)).merge(resourceLocation4, blockState.getBlock().getName().getString(), MapNodeState.CONNECTED, 0, 0.0, false);
                }
            }
        }
    }

    private static boolean isKnownMultiblockMember(ResourceLocation resourceLocation, String string) {
        String string2 = resourceLocation.getPath();
        if (string.equals("quantum")) {
            return resourceLocation.getNamespace().equals("advanced_ae")
                    && (string2.startsWith("quantum_") || string2.equals("data_entangler"))
                    || resourceLocation.getNamespace().equals("ae2")
                    && (string2.equals("quantum_ring") || string2.equals("quantum_link"));
        }
        return string2.contains("assembler_matrix");
    }

    private static MapNodeState mapNodeState(IGridNode iGridNode) {
        if (!iGridNode.isPowered()) {
            return MapNodeState.UNPOWERED;
        }
        if (!iGridNode.hasGridBooted()) {
            return MapNodeState.BOOTING;
        }
        if (iGridNode.hasFlag(GridFlags.REQUIRE_CHANNEL) && (!iGridNode.meetsChannelRequirements() || iGridNode.getUsedChannels() <= 0)) {
            return MapNodeState.MISSING_CHANNEL;
        }
        return iGridNode.hasFlag(GridFlags.REQUIRE_CHANNEL) ? MapNodeState.ACTIVE : MapNodeState.CONNECTED;
    }

    private static LogicalDevice inspectDevice(List<IGridNode> list) {
        boolean bl;
        boolean bl2;
        AEItemKey visual;
        AEItemKey aEItemKey = null;
        int n = Integer.MIN_VALUE;
        String string = "Unknown Channel Device";
        double d = 0.0;
        int n2 = 1;
        boolean bl3 = true;
        boolean bl4 = true;
        boolean bl5 = false;
        boolean bl6 = false;
        boolean bl7 = list.size() > 1;
        ArrayList<DeviceLocation> arrayList = new ArrayList<DeviceLocation>();
        for (IGridNode node : list) {
            int n3;
            Object object3 = node.getOwner();
            visual = node.getVisualRepresentation();
            if (object3 instanceof ProductionMonitorBlockEntity) {
                visual = AEItemKey.of((ItemLike)ModContent.PRODUCTION_MONITOR.get());
            } else if (visual == null) {
                visual = ProductionMonitorBlockEntity.visualFromOwner(object3);
            }
            if (visual != null) {
                n3 = ProductionMonitorBlockEntity.visualScore(visual);
                if (aEItemKey == null || n3 > n) {
                    aEItemKey = visual;
                    n = n3;
                }
            }
            if (object3 != null) {
                string = ProductionMonitorBlockEntity.humanizeClassName(object3.getClass().getSimpleName());
            }
            d += Math.max(0.0, node.getIdlePowerUsage());
            if (node.hasFlag(GridFlags.REQUIRE_CHANNEL)) {
                DeviceLocation deviceLocation;
                bl6 = true;
                n3 = node.isPowered() ? 1 : 0;
                boolean bl8 = node.hasGridBooted();
                boolean bl9 = node.meetsChannelRequirements();
                n2 &= n3;
                bl3 &= bl8;
                bl4 &= bl9;
                bl5 |= node.getUsedChannels() > 0;
                if (!(n3 == 0 || !bl8 || bl9 && node.getUsedChannels() > 0 || (deviceLocation = ProductionMonitorBlockEntity.locationOf(node, object3)) == null || arrayList.contains(deviceLocation))) {
                    arrayList.add(deviceLocation);
                }
            }
            bl7 |= node.hasFlag(GridFlags.MULTIBLOCK);
        }
        boolean bl10 = bl2 = aEItemKey == null;
        if (bl2) {
            aEItemKey = ProductionMonitorBlockEntity.fallbackVisual(string);
        }
        String name = bl2 ? string : aEItemKey.getDisplayName().getString();
        name = ProductionMonitorBlockEntity.friendlyMultiblockName(name, string, bl7);
        boolean bl11 = bl = bl6 && n2 != 0 && bl3 && bl4;
        DeviceState state = bl ? DeviceState.ACTIVE : (n2 == 0 ? DeviceState.UNPOWERED : (!bl4 ? DeviceState.MISSING_CHANNEL : DeviceState.BOOTING));
        return new LogicalDevice(aEItemKey, name, state, bl5 ? 1 : 0, d, arrayList);
    }

    private static int visualScore(AEItemKey aEItemKey) {
        String string = aEItemKey.getDisplayName().getString().toLowerCase(Locale.ROOT);
        if (string.contains("controller") || string.contains("core")) {
            return 4;
        }
        if (string.contains("quantum computer") || string.contains("assembler matrix")) {
            return string.contains("frame") || string.contains("structural") ? 1 : 3;
        }
        return string.contains("frame") || string.contains("structural glass") ? 0 : 2;
    }

    private static String friendlyMultiblockName(String string, String string2, boolean bl) {
        if (!bl) {
            return string;
        }
        String string3 = (string + " " + string2).toLowerCase(Locale.ROOT);
        if (string3.contains("quantum") && string3.contains("computer")) {
            return "Quantum Computer";
        }
        if (string3.contains("assembler") && string3.contains("matrix")) {
            return "Assembler Matrix";
        }
        return string;
    }

    private static AEItemKey fallbackVisual(String string) {
        String string2 = string.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
        for (Item item : BuiltInRegistries.ITEM) {
            String string3 = BuiltInRegistries.ITEM.getKey(item).getPath().replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
            if (string2.isEmpty() || !string3.equals(string2) && !string3.contains(string2) && (string3.length() < 6 || !string2.contains(string3))) continue;
            return AEItemKey.of((ItemLike)item);
        }
        return AEItemKey.of((ItemLike)Items.RECOVERY_COMPASS);
    }

    private static AEItemKey visualFromOwner(Object object) {
        if (object == null) {
            return null;
        }
        for (String string : List.of("getPartItem", "getItemStack", "getItem")) {
            Object object2 = ProductionMonitorBlockEntity.invokeNoArg(object, string);
            AEItemKey aEItemKey = ProductionMonitorBlockEntity.visualFromValue(object2);
            if (aEItemKey == null) continue;
            return aEItemKey;
        }
        return null;
    }

    private static AEItemKey visualFromValue(Object object) {
        if (object instanceof ItemStack itemStack && !itemStack.isEmpty()) {
            return AEItemKey.of(itemStack);
        }
        if (object instanceof Item) {
            return AEItemKey.of((ItemLike)object);
        }
        Object object2 = object == null ? null : ProductionMonitorBlockEntity.invokeNoArg(object, "asItem");
        if (object2 instanceof Item) {
            Item item = (Item)object2;
            return AEItemKey.of((ItemLike)item);
        }
        return null;
    }

    static DeviceLocation locationOf(IGridNode iGridNode, Object object) {
        Object object2 = ProductionMonitorBlockEntity.invokeNoArg(iGridNode, "getInWorldNode");
        BlockPos blockPos = ProductionMonitorBlockEntity.findBlockPos(object2, 0);
        if (blockPos == null) {
            blockPos = ProductionMonitorBlockEntity.findBlockPos(object, 0);
        }
        if (blockPos == null) {
            return null;
        }

        // A logical AE grid can span multiple dimensions through Quantum Bridges. A position
        // alone is therefore not a complete device location: the same coordinates can even
        // exist in several dimensions. Preserve the owning grid node's real dimension so the
        // locator never mistakes the monitor dimension for the device dimension.
        ResourceLocation dimension = null;
        try {
            Level nodeLevel = iGridNode.getLevel();
            if (nodeLevel != null) {
                dimension = nodeLevel.dimension().location();
            }
        } catch (RuntimeException ignored) {
        }
        if (dimension == null) {
            Level foundLevel = ProductionMonitorBlockEntity.findLevel(object2, 0);
            if (foundLevel == null) {
                foundLevel = ProductionMonitorBlockEntity.findLevel(object, 0);
            }
            if (foundLevel != null) {
                dimension = foundLevel.dimension().location();
            }
        }
        if (dimension == null) {
            return null;
        }

        Direction direction = ProductionMonitorBlockEntity.findDirection(object);
        return new DeviceLocation(dimension, blockPos.immutable(),
                direction == null ? -1 : direction.get3DDataValue());
    }

    private static BlockPos findBlockPos(Object object, int n) {
        Object object2;
        if (object == null || n > 3) {
            return null;
        }
        if (object instanceof BlockEntity) {
            BlockEntity blockEntity = (BlockEntity)object;
            return blockEntity.getBlockPos();
        }
        if (object instanceof BlockPos) {
            BlockPos blockPos = (BlockPos)object;
            return blockPos;
        }
        for (String string : List.of("getLocation", "getBlockPos", "getPos", "getPosition")) {
            object2 = ProductionMonitorBlockEntity.invokeNoArg(object, string);
            if (!(object2 instanceof BlockPos)) continue;
            BlockPos blockPos = (BlockPos)object2;
            return blockPos;
        }
        for (String string : List.of("getHost", "getBlockEntity", "getPartHost")) {
            BlockPos found = ProductionMonitorBlockEntity.findBlockPos(ProductionMonitorBlockEntity.invokeNoArg(object, string), n + 1);
            if (found == null) continue;
            return found;
        }
        return null;
    }

    private static Level findLevel(Object object, int n) {
        Object object2;
        if (object == null || n > 3) {
            return null;
        }
        if (object instanceof BlockEntity) {
            BlockEntity blockEntity = (BlockEntity)object;
            return blockEntity.getLevel();
        }
        for (String string : List.of("getLevel", "getWorld")) {
            object2 = ProductionMonitorBlockEntity.invokeNoArg(object, string);
            if (!(object2 instanceof Level)) continue;
            Level level = (Level)object2;
            return level;
        }
        for (String string : List.of("getHost", "getBlockEntity", "getPartHost")) {
            Level found = ProductionMonitorBlockEntity.findLevel(ProductionMonitorBlockEntity.invokeNoArg(object, string), n + 1);
            if (found == null) continue;
            return found;
        }
        return null;
    }

    private static Direction findDirection(Object object) {
        if (object == null) {
            return null;
        }
        for (String string : List.of("getSide", "getPartSide", "getDirection")) {
            Object object2 = ProductionMonitorBlockEntity.invokeNoArg(object, string);
            if (!(object2 instanceof Direction)) continue;
            Direction direction = (Direction)object2;
            return direction;
        }
        return null;
    }

    private static Object invokeNoArg(Object object, String string) {
        if (object == null) {
            return null;
        }
        try {
            Method method = object.getClass().getMethod(string, new Class[0]);
            if (!method.canAccess(object)) {
                method.trySetAccessible();
            }
            return method.invoke(object, new Object[0]);
        }
        catch (ReflectiveOperationException | RuntimeException exception) {
            return null;
        }
    }

    private static int networkChannelCount(IGrid iGrid, int n) {
        Object object = ProductionMonitorBlockEntity.invokeNoArg(iGrid, "getPathingService");
        if (object != null) {
            for (String string : List.of("getUsedChannels", "getChannelsInUse", "getChannelCount")) {
                Object object2 = ProductionMonitorBlockEntity.invokeNoArg(object, string);
                if (!(object2 instanceof Number)) continue;
                Number number = (Number)object2;
                return Math.max(0, number.intValue());
            }
        }
        return Math.max(0, n);
    }

    private static String humanizeClassName(String string) {
        String string2 = string.replace("BlockEntity", "").replace("Part", "").replace("TileEntity", "");
        return string2.replaceAll("(?<=[a-z0-9])(?=[A-Z])", " ").strip();
    }

    private static Long metric(Object object, String string) {
        try {
            Long l;
            Method method = object.getClass().getMethod(string, new Class[0]);
            Object object2 = method.invoke(object, new Object[0]);
            if (object2 instanceof Number) {
                Number number = (Number)object2;
                l = number.longValue();
            } else {
                l = null;
            }
            return l;
        }
        catch (ReflectiveOperationException | SecurityException exception) {
            return null;
        }
    }

    private static long saturatingAdd(long l, long l2) {
        if (l2 > 0L && l > Long.MAX_VALUE - l2) {
            return Long.MAX_VALUE;
        }
        return l + l2;
    }

    private void sampleEnergy(IGrid iGrid, long l, long l2, boolean bl) {
        IEnergyService iEnergyService = iGrid.getEnergyService();
        double d = PowerUnit.AE.convertTo(PowerUnit.FE, iEnergyService.getStoredPower());
        double d2 = iEnergyService.getAvgPowerInjection() - iEnergyService.getAvgPowerUsage();
        double d3 = PowerUnit.AE.convertTo(PowerUnit.FE, d2) * 20.0 * 60.0;
        long l3 = ProductionMonitorBlockEntity.safeRound(d);
        long l4 = ProductionMonitorBlockEntity.safeRound(d3);
        boolean bl2 = this.infiniteEnergy = !Double.isFinite(d) || d >= 9.223372036854776E18;
        if (bl) {
            this.energyHistory.clear();
            this.energyLastChangedTick = l2;
        } else {
            this.energyHistory.add(l4, l);
            if (l3 != this.storedEnergyFe || l4 != 0L) {
                this.energyLastChangedTick = l2;
            }
        }
        this.storedEnergyFe = l3;
        this.currentEnergyPerMinute = l4;
    }

    private void clearEnergy() {
        this.storedEnergyFe = 0L;
        this.currentEnergyPerMinute = 0L;
        this.energyLastChangedTick = 0L;
        this.infiniteEnergy = false;
        this.energyHistory.clear();
    }

    private static long safeRound(double d) {
        if (Double.isNaN(d)) {
            return 0L;
        }
        if (d >= 9.223372036854776E18) {
            return Long.MAX_VALUE;
        }
        if (d <= -9.223372036854776E18) {
            return Long.MIN_VALUE;
        }
        return Math.round(d);
    }

    private static final class RateHistory {
        private final ArrayDeque<RateSample> samples = new ArrayDeque();
        private long totalTicks;

        private RateHistory() {
        }

        void add(long l, long l2) {
            long l3 = Math.max(1L, l2);
            this.samples.addLast(new RateSample(l, l3));
            this.totalTicks += l3;
            while (this.totalTicks > 1200L && !this.samples.isEmpty()) {
                long l4 = this.totalTicks - 1200L;
                RateSample rateSample = this.samples.removeFirst();
                if (rateSample.ticks() <= l4) {
                    this.totalTicks -= rateSample.ticks();
                    continue;
                }
                long l5 = rateSample.ticks() - l4;
                this.samples.addFirst(new RateSample(rateSample.rate(), l5));
                this.totalTicks -= l4;
            }
        }

        long average() {
            if (this.samples.isEmpty() || this.totalTicks <= 0L) {
                return 0L;
            }
            double d = 0.0;
            for (RateSample rateSample : this.samples) {
                d += (double)rateSample.rate() * (double)rateSample.ticks();
            }
            return ProductionMonitorBlockEntity.safeRound(d / (double)this.totalTicks);
        }

        void clear() {
            this.samples.clear();
            this.totalTicks = 0L;
        }
    }

    public record DeviceSnapshot(List<DeviceGroupSnapshot> groups, int totalDevices, int activeDevices, int missingChannels, int unpoweredDevices, int bootingDevices, int assignedChannels, int networkChannels, int unresolvedChannels, double idlePower) {
        public static final DeviceSnapshot EMPTY = new DeviceSnapshot(List.of(), 0, 0, 0, 0, 0, 0, 0, 0, 0.0);

        public DeviceSnapshot {
            groups = List.copyOf(groups);
        }
    }

    private static final class RateRecord {
        private long amount;
        private long currentPerMinute;
        private long lastChangedTick;
        private final RateHistory history = new RateHistory();

        private RateRecord(long l, long l2) {
            this.amount = l;
            this.lastChangedTick = l2;
        }

        static RateRecord baseline(long l, long l2) {
            return new RateRecord(l, l2);
        }

        void update(long l, long l2, long l3) {
            long l4 = l - this.amount;
            this.currentPerMinute = Math.round((double)l4 * 1200.0 / (double)l2);
            if (l4 != 0L) {
                this.lastChangedTick = l3;
            }
            this.amount = l;
            this.history.add(this.currentPerMinute, l2);
        }

        long averagePerMinute() {
            return this.history.average();
        }
    }

    private static final class DashboardPin {
        private final DashboardKind kind;
        private AEKey key;
        private final ResourceLocation resourceId;
        private AlarmRule rule;
        private long pendingSince = -1L;
        private boolean active;

        private DashboardPin(DashboardKind dashboardKind, AEKey aEKey, ResourceLocation resourceLocation, AlarmRule alarmRule) {
            this.kind = dashboardKind;
            this.key = aEKey;
            this.resourceId = resourceLocation;
            this.rule = alarmRule;
        }
    }

    public static enum DashboardKind {
        ITEM,
        FLUID,
        ENERGY;

    }

    private record DashboardValues(long amount, long currentPerMinute, long averagePerMinute, long secondsSinceChange, boolean infinite) {
    }

    public record DashboardEntrySnapshot(DashboardKind kind, AEKey key, long amount, long currentPerMinute, long averagePerMinute, long secondsSinceChange, boolean infinite, AlarmRule rule, AlarmState state) {
    }

    public record AlarmRule(AlarmMode mode, long threshold, int delaySeconds, long hysteresis) {
        public static final AlarmRule NONE = new AlarmRule(AlarmMode.NONE, 0L, 0, 0L);

        public AlarmRule sanitized() {
            return new AlarmRule(this.mode == null ? AlarmMode.NONE : this.mode, this.threshold, Math.max(0, Math.min(3600, this.delaySeconds)), Math.max(0L, this.hysteresis));
        }

        public boolean isAmountRule() {
            return this.mode == AlarmMode.AMOUNT_BELOW || this.mode == AlarmMode.AMOUNT_ABOVE;
        }
    }

    public static enum AlarmState {
        NORMAL,
        PENDING,
        ACTIVE;

    }

    public record EnergySnapshot(long storedFe, long currentPerMinute, long averagePerMinute, long secondsSinceChange, boolean infinite) {
    }

    public static enum AlarmMode {
        NONE,
        AMOUNT_BELOW,
        AMOUNT_ABOVE,
        RATE_BELOW,
        RATE_ABOVE,
        STALLED;

    }

    public record StorageCapacitySnapshot(long usedBytes, long totalBytes, long usedTypes, long totalTypes, int finiteCells, int infiniteCells, int otherCells, int externalStorageBuses) {
        public static final StorageCapacitySnapshot EMPTY = new StorageCapacitySnapshot(0L, 0L, 0L, 0L, 0, 0, 0, 0);

        public long freeBytes() {
            return Math.max(0L, this.totalBytes - this.usedBytes);
        }

        public long freeTypes() {
            return Math.max(0L, this.totalTypes - this.usedTypes);
        }
    }

    private record LogicalDevice(AEItemKey visual, String name, DeviceState state, int assignedChannels, double idlePower, List<DeviceLocation> missingLocations) {
    }

    private record DeviceGroupKey(AEItemKey visual, String name) {
    }

    private static final class MutableDeviceGroup {
        private final AEItemKey visual;
        private final String name;
        private int count;
        private int active;
        private int missingChannel;
        private int unpowered;
        private int booting;
        private int assignedChannels;
        private double idlePower;
        private final List<DeviceLocation> missingLocations = new ArrayList<DeviceLocation>();

        private MutableDeviceGroup(AEItemKey aEItemKey, String string) {
            this.visual = aEItemKey;
            this.name = string;
        }

        private void add(LogicalDevice logicalDevice) {
            ++this.count;
            this.assignedChannels += logicalDevice.assignedChannels();
            this.idlePower += logicalDevice.idlePower();
            for (DeviceLocation deviceLocation : logicalDevice.missingLocations()) {
                if (this.missingLocations.contains(deviceLocation)) continue;
                this.missingLocations.add(deviceLocation);
            }
            switch (logicalDevice.state()) {
                case ACTIVE: {
                    ++this.active;
                    break;
                }
                case MISSING_CHANNEL: {
                    ++this.missingChannel;
                    break;
                }
                case UNPOWERED: {
                    ++this.unpowered;
                    break;
                }
                case BOOTING: {
                    ++this.booting;
                }
            }
        }

        private DeviceGroupSnapshot finish() {
            return new DeviceGroupSnapshot(this.visual, this.name, this.count, this.active, this.missingChannel, this.unpowered, this.booting, this.assignedChannels, this.idlePower, this.missingLocations);
        }
    }

    public record DeviceGroupSnapshot(AEItemKey visual, String name, int count, int active, int missingChannel, int unpowered, int booting, int assignedChannels, double idlePower, List<DeviceLocation> missingLocations) {
        public DeviceGroupSnapshot {
            missingLocations = List.copyOf(missingLocations);
        }
    }

    public record NetworkMapSnapshot(ResourceLocation viewDimension,
                                     List<ResourceLocation> dimensions,
                                     List<NetworkMapLink> quantumLinks,
                                     List<NetworkMapWirelessLink> wirelessLinks,
                                     List<NetworkMapNode> nodes,
                                     boolean truncated) {
        public static final int MAX_NODES = 4096;

        public NetworkMapSnapshot {
            dimensions = List.copyOf(dimensions);
            quantumLinks = List.copyOf(quantumLinks);
            wirelessLinks = List.copyOf(wirelessLinks);
            nodes = List.copyOf(nodes);
        }

        public static NetworkMapSnapshot empty(ResourceLocation viewDimension) {
            return new NetworkMapSnapshot(
                    viewDimension, List.of(), List.of(), List.of(), List.of(), false);
        }
    }

    public record NetworkMapLink(ResourceLocation dimensionA, BlockPos posA,
                                 ResourceLocation dimensionB, BlockPos posB) {
    }

    public record NetworkMapWirelessLink(ResourceLocation dimension,
                                         BlockPos posA, BlockPos posB) {
    }

    private record QuantumBridgeEndpoint(ResourceLocation dimension, BlockPos pos) {
    }

    private record WirelessConnectorEndpoint(ResourceLocation dimension, BlockPos pos) {
    }

    public record DeviceLocation(ResourceLocation dimension, BlockPos pos, int side) {
        public static final int NO_SIDE = -1;
    }

    private record MapNodeSource(IGridNode node, Object owner, DeviceLocation location) {
    }

    public static enum MapNodeState {
        CONNECTED,
        ACTIVE,
        MISSING_CHANNEL,
        UNPOWERED,
        BOOTING;


        private int severity() {
            return switch (this) {
                default -> throw new IncompatibleClassChangeError();
                case CONNECTED -> 0;
                case ACTIVE -> 1;
                case BOOTING -> 2;
                case UNPOWERED -> 3;
                case MISSING_CHANNEL -> 4;
            };
        }
    }

    public static enum MapRenderKind {
        BLOCK,
        PART;

    }

    private record MapElementKey(BlockPos pos, MapRenderKind renderKind, int side, ResourceLocation visualId) {
    }

    private static final class MutableMapNode {
        private final BlockPos pos;
        private final int blockStateId;
        private final MapRenderKind renderKind;
        private final int side;
        private ResourceLocation visualId;
        private String name = "ME Network Block";
        private MapNodeState state = MapNodeState.CONNECTED;
        private int channels;
        private double idlePower;
        private boolean device;

        private MutableMapNode(BlockPos blockPos, int n, ResourceLocation resourceLocation, MapRenderKind mapRenderKind, int n2) {
            this.pos = blockPos;
            this.blockStateId = n;
            this.visualId = resourceLocation;
            this.renderKind = mapRenderKind;
            this.side = n2;
        }

        private void merge(ResourceLocation resourceLocation, String string, MapNodeState mapNodeState, int n, double d, boolean bl) {
            boolean hasName = string != null && !string.isBlank();
            if (bl || this.visualId == null) {
                this.visualId = resourceLocation;
                if (hasName) {
                    this.name = string;
                }
            } else if ("ME Network Block".equals(this.name) && hasName) {
                // The first merge belongs to the visual representation already stored by the
                // constructor. Preserve later device-priority replacement, but never leave
                // ordinary cables/parts stuck with the generic placeholder name.
                this.name = string;
            }
            if (mapNodeState.severity() > this.state.severity()) {
                this.state = mapNodeState;
            }
            this.channels += Math.max(0, n);
            this.idlePower += Math.max(0.0, d);
            this.device |= bl;
        }

        private NetworkMapNode finish() {
            ResourceLocation resourceLocation = this.visualId == null ? ResourceLocation.withDefaultNamespace((String)"barrier") : this.visualId;
            return new NetworkMapNode(this.pos, this.blockStateId, resourceLocation, this.name, this.state, this.channels, this.idlePower, this.device, this.renderKind, this.side);
        }
    }

    private static enum DeviceState {
        ACTIVE,
        MISSING_CHANNEL,
        UNPOWERED,
        BOOTING;

    }

    public record SnapshotEntry(AEKey key, long amount, long currentPerMinute, long averagePerMinute, long secondsSinceChange) {
    }

    private record RateSample(long rate, long ticks) {
    }

    public record NetworkMapNode(BlockPos pos, int blockStateId, ResourceLocation visualId, String name, MapNodeState state, int channels, double idlePower, boolean device, MapRenderKind renderKind, int side) {
    }
}
