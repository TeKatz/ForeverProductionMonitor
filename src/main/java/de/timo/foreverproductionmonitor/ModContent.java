/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  appeng.api.AECapabilities
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.CreativeModeTabs
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.entity.BlockEntityType$Builder
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent
 *  net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
 *  net.neoforged.neoforge.registries.DeferredBlock
 *  net.neoforged.neoforge.registries.DeferredHolder
 *  net.neoforged.neoforge.registries.DeferredItem
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  net.neoforged.neoforge.registries.DeferredRegister$Blocks
 *  net.neoforged.neoforge.registries.DeferredRegister$Items
 */
package de.timo.foreverproductionmonitor;

import appeng.api.AECapabilities;
import de.timo.foreverproductionmonitor.block.ProductionMonitorBlock;
import de.timo.foreverproductionmonitor.blockentity.ProductionMonitorBlockEntity;
import de.timo.foreverproductionmonitor.item.MultiblockConstructorItem;\nimport de.timo.foreverproductionmonitor.item.ProductionTabletItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModContent {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks((String)"forever_production_monitor");
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems((String)"forever_production_monitor");
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create((ResourceKey)Registries.BLOCK_ENTITY_TYPE, (String)"forever_production_monitor");
    public static final DeferredBlock<ProductionMonitorBlock> PRODUCTION_MONITOR = BLOCKS.register("production_monitor", ProductionMonitorBlock::new);
    public static final DeferredItem<BlockItem> PRODUCTION_MONITOR_ITEM = ITEMS.register("production_monitor", () -> new BlockItem((Block)PRODUCTION_MONITOR.get(), new Item.Properties()));
    public static final DeferredItem<ProductionTabletItem> PRODUCTION_TABLET = ITEMS.register("production_tablet", () -> new ProductionTabletItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<MultiblockConstructorItem> MULTIBLOCK_CONSTRUCTOR = ITEMS.register(
            "multiblock_constructor", () -> new MultiblockConstructorItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProductionMonitorBlockEntity>> PRODUCTION_MONITOR_BLOCK_ENTITY = BLOCK_ENTITIES.register("production_monitor", () -> BlockEntityType.Builder.of(ProductionMonitorBlockEntity::new, (Block[])new Block[]{(Block)PRODUCTION_MONITOR.get()}).build(null));

    private ModContent() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, PRODUCTION_MONITOR_BLOCK_ENTITY.get(), (blockEntity, side) -> blockEntity);
    }

    public static void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept((ItemLike)PRODUCTION_MONITOR_ITEM.get());
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept((ItemLike)PRODUCTION_TABLET.get());\n            event.accept((ItemLike)MULTIBLOCK_CONSTRUCTOR.get());
        }
    }
}
