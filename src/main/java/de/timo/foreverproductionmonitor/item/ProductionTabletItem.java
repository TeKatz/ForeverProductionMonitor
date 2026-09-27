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

public final class ProductionTabletItem extends Item {
    private static final String LINK_TAG = "ForeverProductionMonitorLink";
    private static final String TABLET_ID_TAG = "ForeverProductionMonitorTabletId";

    public ProductionTabletItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.getBlockEntity(context.getClickedPos()) instanceof ProductionMonitorBlockEntity monitor) {
            if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                ItemStack stack = context.getItemInHand();
                MonitorLink newLink =
                        new MonitorLink(level.dimension().location(), context.getClickedPos());
                Optional<MonitorLink> oldLink = ProductionTabletItem.getLink(stack);
                UUID tabletId = ProductionTabletItem.getOrCreateTabletId(stack);

                oldLink.ifPresent(link ->
                        ProductionTabletItem.unlinkPreviousMonitor(
                                serverLevel, link, newLink, tabletId));

                ProductionTabletItem.bind(stack, newLink);
                monitor.linkTablet(tabletId);

                context.getPlayer().displayClientMessage(
                        Component.translatable(
                                "message.forever_production_monitor.linked",
                                context.getClickedPos().toShortString()),
                        true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Optional<MonitorLink> link = ProductionTabletItem.getLink(stack);
        if (level.isClientSide) {
            if (link.isPresent()) {
                ClientTabletHooks.open(link.get());
            } else {
                player.displayClientMessage(
                        Component.translatable(
                                "message.forever_production_monitor.not_linked"),
                        true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag) {
        ProductionTabletItem.getLink(stack).ifPresentOrElse(
                link -> {
                    tooltip.add(Component.translatable(
                                    "tooltip.forever_production_monitor.linked_dimension",
                                    link.dimension())
                            .withColor(11897574));
                    tooltip.add(Component.translatable(
                                    "tooltip.forever_production_monitor.linked_position",
                                    link.pos().toShortString())
                            .withColor(15180606));
                },
                () -> tooltip.add(Component.translatable(
                                "tooltip.forever_production_monitor.unlinked")
                        .withColor(0x999999)));
    }

    public static void bind(ItemStack stack, MonitorLink link) {
        CustomData.update(
                (DataComponentType) DataComponents.CUSTOM_DATA,
                stack,
                root -> {
                    CompoundTag tag = new CompoundTag();
                    tag.putString("dimension", link.dimension().toString());
                    tag.putLong("pos", link.pos().asLong());
                    root.put(LINK_TAG, (Tag) tag);
                });
    }

    public static Optional<MonitorLink> getLink(ItemStack stack) {
        if (!(stack.getItem() instanceof ProductionTabletItem)) {
            return Optional.empty();
        }

        CompoundTag root =
                ((CustomData) stack.getOrDefault(
                                DataComponents.CUSTOM_DATA,
                                CustomData.EMPTY))
                        .copyTag();
        if (!root.contains(LINK_TAG)) {
            return Optional.empty();
        }

        CompoundTag tag = root.getCompound(LINK_TAG);
        ResourceLocation dimension =
                ResourceLocation.tryParse(tag.getString("dimension"));
        if (dimension == null || !tag.contains("pos")) {
            return Optional.empty();
        }

        return Optional.of(
                new MonitorLink(
                        dimension,
                        BlockPos.of(tag.getLong("pos"))));
    }

    private static UUID getOrCreateTabletId(ItemStack stack) {
        CompoundTag root =
                ((CustomData) stack.getOrDefault(
                                DataComponents.CUSTOM_DATA,
                                CustomData.EMPTY))
                        .copyTag();

        if (root.contains(TABLET_ID_TAG)) {
            try {
                return UUID.fromString(root.getString(TABLET_ID_TAG));
            } catch (IllegalArgumentException ignored) {
                // Replace malformed custom data with a fresh stable tablet identity.
            }
        }

        UUID tabletId = UUID.randomUUID();
        CustomData.update(
                (DataComponentType) DataComponents.CUSTOM_DATA,
                stack,
                tag -> tag.putString(TABLET_ID_TAG, tabletId.toString()));
        return tabletId;
    }

    private static void unlinkPreviousMonitor(
            ServerLevel currentLevel,
            MonitorLink oldLink,
            MonitorLink newLink,
            UUID tabletId) {
        if (oldLink.equals(newLink)) {
            return;
        }

        ResourceKey<Level> oldDimension =
                ResourceKey.create(Registries.DIMENSION, oldLink.dimension());
        ServerLevel oldLevel =
                currentLevel.getServer().getLevel(oldDimension);
        if (oldLevel == null) {
            return;
        }

        // Relinking is a deliberate one-off action, so loading the old monitor's chunk
        // here is preferable to leaving a stale LINKED visual state behind.
        oldLevel.getChunkAt(oldLink.pos());
        if (oldLevel.getBlockEntity(oldLink.pos())
                instanceof ProductionMonitorBlockEntity oldMonitor) {
            oldMonitor.unlinkTablet(tabletId);
        }
    }

    public record MonitorLink(ResourceLocation dimension, BlockPos pos) {
    }
}
