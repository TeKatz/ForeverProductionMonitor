/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.component.DataComponentType
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.component.CustomData
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.Level
 */
package de.timo.foreverproductionmonitor.item;

import de.timo.foreverproductionmonitor.blockentity.ProductionMonitorBlockEntity;
import de.timo.foreverproductionmonitor.client.ClientTabletHooks;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class ProductionTabletItem
extends Item {
    private static final String LINK_TAG = "ForeverProductionMonitorLink";
    private static final String TABLET_ID_TAG = "ForeverProductionMonitorTabletId";

    public ProductionTabletItem(Item.Properties properties) {
        super(properties);
    }

    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.getBlockEntity(context.getClickedPos()) instanceof ProductionMonitorBlockEntity productionMonitorBlockEntity) {
            if (!level.isClientSide && level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                ItemStack itemStack = context.getItemInHand();
                MonitorLink monitorLink = new MonitorLink(level.dimension().location(), context.getClickedPos());
                Optional<MonitorLink> optional = ProductionTabletItem.getLink(itemStack);
                UUID uUID = ProductionTabletItem.getOrCreateTabletId(itemStack);
                optional.ifPresent(oldLink -> ProductionTabletItem.unlinkPreviousMonitor(serverLevel, oldLink, monitorLink, uUID));
                ProductionTabletItem.bind(itemStack, monitorLink);
                productionMonitorBlockEntity.linkTablet(uUID);
                context.getPlayer().displayClientMessage((Component)Component.translatable((String)"message.forever_production_monitor.linked", (Object[])new Object[]{context.getClickedPos().toShortString()}), true);
            }
            return InteractionResult.sidedSuccess((boolean)level.isClientSide);
        }
        return super.useOn(context);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Optional<MonitorLink> link = ProductionTabletItem.getLink(stack);
        if (level.isClientSide) {
            if (link.isPresent()) {
                ClientTabletHooks.open(link.get());
            } else {
                player.displayClientMessage((Component)Component.translatable((String)"message.forever_production_monitor.not_linked"), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ProductionTabletItem.getLink(stack).ifPresentOrElse(link -> {
            tooltip.add((Component)Component.translatable((String)"tooltip.forever_production_monitor.linked_dimension", (Object[])new Object[]{link.dimension()}).withColor(11897574));
            tooltip.add((Component)Component.translatable((String)"tooltip.forever_production_monitor.linked_position", (Object[])new Object[]{link.pos().toShortString()}).withColor(15180606));
        }, () -> tooltip.add((Component)Component.translatable((String)"tooltip.forever_production_monitor.unlinked").withColor(0x999999)));
    }

    public static void bind(ItemStack stack, MonitorLink link) {
        CustomData.update((DataComponentType)DataComponents.CUSTOM_DATA, (ItemStack)stack, root -> {
            CompoundTag tag = new CompoundTag();
            tag.putString("dimension", link.dimension().toString());
            tag.putLong("pos", link.pos().asLong());
            root.put(LINK_TAG, (Tag)tag);
        });
    }

    public static Optional<MonitorLink> getLink(ItemStack stack) {
        if (!(stack.getItem() instanceof ProductionTabletItem)) {
            return Optional.empty();
        }
        CompoundTag root = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, (Object)CustomData.EMPTY)).copyTag();
        if (!root.contains(LINK_TAG)) {
            return Optional.empty();
        }
        CompoundTag tag = root.getCompound(LINK_TAG);
        ResourceLocation dimension = ResourceLocation.tryParse((String)tag.getString("dimension"));
        if (dimension == null || !tag.contains("pos")) {
            return Optional.empty();
        }
        return Optional.of(new MonitorLink(dimension, BlockPos.of((long)tag.getLong("pos"))));
    }

    public static UUID getOrCreateTabletId(ItemStack stack) {
        CompoundTag compoundTag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, (Object)CustomData.EMPTY)).copyTag();
        if (compoundTag.contains(TABLET_ID_TAG)) {
            try {
                return UUID.fromString(compoundTag.getString(TABLET_ID_TAG));
            }
            catch (IllegalArgumentException ignored) {
                // Replace malformed custom data with a fresh stable tablet identity.
            }
        }
        UUID uUID = UUID.randomUUID();
        CustomData.update((DataComponentType)DataComponents.CUSTOM_DATA, (ItemStack)stack, root -> root.putString(TABLET_ID_TAG, uUID.toString()));
        return uUID;
    }

    private static void unlinkPreviousMonitor(ServerLevel currentLevel, MonitorLink oldLink, MonitorLink newLink, UUID tabletId) {
        if (oldLink.equals(newLink)) {
            return;
        }
        ResourceKey<Level> resourceKey = ResourceKey.create(Registries.DIMENSION, oldLink.dimension());
        ServerLevel serverLevel = currentLevel.getServer().getLevel(resourceKey);
        if (serverLevel == null) {
            return;
        }
        serverLevel.getChunkAt(oldLink.pos());
        if (serverLevel.getBlockEntity(oldLink.pos()) instanceof ProductionMonitorBlockEntity productionMonitorBlockEntity) {
            productionMonitorBlockEntity.unlinkTablet(tabletId);
        }
    }

    public record MonitorLink(ResourceLocation dimension, BlockPos pos) {
    }
}
