package de.timo.foreverproductionmonitor.item;

import de.timo.foreverproductionmonitor.blockentity.ProductionMonitorBlockEntity;
import de.timo.foreverproductionmonitor.network.DeepCoreConstructorNetwork;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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

/** AE-linked Deep Core construction and dismantling tool. */
public final class MultiblockConstructorItem extends Item {
    private static final String ROOT_TAG = "ForeverMultiblockConstructor";
    private static final String LINK_DIM = "link_dimension";
    private static final String LINK_POS = "link_pos";
    private static final String TARGET_DIM = "target_dimension";
    private static final String TARGET_POS = "target_pos";
    private static final String TARGET_FRONT = "target_front";
    private static final String TIER = "tier";
    private static final String MODE = "mode";

    public enum Mode {
        PREVIEW,
        BUILD,
        DISMANTLE;

        public Mode next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public String translationKey() {
            return "mode.forever_production_monitor.constructor."
                    + name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    public MultiblockConstructorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();

        if (level.getBlockEntity(context.getClickedPos()) instanceof ProductionMonitorBlockEntity) {
            if (!level.isClientSide) {
                bind(stack, new ProductionTabletItem.MonitorLink(
                        level.dimension().location(), context.getClickedPos()));
                if (player != null) {
                    player.displayClientMessage(Component.translatable(
                            "message.forever_production_monitor.constructor.linked",
                            context.getClickedPos().toShortString()), true);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (getMode(stack) == Mode.DISMANTLE) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                DeepCoreConstructorNetwork.dismantle(
                        serverPlayer, stack, context.getClickedPos());
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide) {
            if (getLink(stack).isEmpty()) {
                if (player != null) {
                    player.displayClientMessage(Component.translatable(
                            "message.forever_production_monitor.constructor.not_linked"), true);
                }
                return InteractionResult.FAIL;
            }

            Direction front = player == null ? Direction.NORTH : player.getDirection();
            if (front.getAxis().isVertical()) front = Direction.NORTH;
            BlockPos corePos = context.getClickedPos().relative(context.getClickedFace());
            setTarget(stack, new PreviewTarget(level.dimension().location(), corePos, front));
            if (player != null) {
                player.displayClientMessage(Component.translatable(
                        "message.forever_production_monitor.constructor.target",
                        corePos.toShortString(), front.getName(), roman(getTier(stack))), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                Mode mode = getMode(stack).next();
                setMode(stack, mode);
                player.displayClientMessage(Component.translatable(
                        "message.forever_production_monitor.constructor.mode",
                        Component.translatable(mode.translationKey())), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        Mode mode = getMode(stack);
        if (mode == Mode.PREVIEW) {
            if (!level.isClientSide) {
                int tier = getTier(stack) % 3 + 1;
                setTier(stack, tier);
                player.displayClientMessage(Component.translatable(
                        "message.forever_production_monitor.constructor.tier", roman(tier)), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        if (mode == Mode.BUILD) {
            if (level.isClientSide) {
                if (getLink(stack).isEmpty()) {
                    player.displayClientMessage(Component.translatable(
                            "message.forever_production_monitor.constructor.not_linked"), true);
                } else if (getTarget(stack).isEmpty()) {
                    player.displayClientMessage(Component.translatable(
                            "message.forever_production_monitor.constructor.no_target"), true);
                } else {
                    DeepCoreConstructorNetwork.requestBuild(stack);
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable(
                    "message.forever_production_monitor.constructor.dismantle_target"), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(
                "tooltip.forever_production_monitor.constructor.mode",
                Component.translatable(getMode(stack).translationKey())).withColor(0xD9C46A));
        tooltip.add(Component.translatable(
                "tooltip.forever_production_monitor.constructor.tier", roman(getTier(stack)))
                .withColor(0xB58BFF));
        getLink(stack).ifPresentOrElse(
                link -> tooltip.add(Component.translatable(
                        "tooltip.forever_production_monitor.constructor.linked",
                        link.dimension(), link.pos().toShortString()).withColor(0x78D6A8)),
                () -> tooltip.add(Component.translatable(
                        "tooltip.forever_production_monitor.constructor.unlinked").withColor(0x999999)));
        getTarget(stack).ifPresent(target -> tooltip.add(Component.translatable(
                "tooltip.forever_production_monitor.constructor.target",
                target.dimension(), target.corePos().toShortString(), target.front().getName())
                .withColor(0xD9C46A)));
        tooltip.add(Component.translatable(
                "tooltip.forever_production_monitor.constructor.controls").withColor(0xAAAAAA));
    }

    public static void bind(ItemStack stack, ProductionTabletItem.MonitorLink link) {
        CustomData.update((DataComponentType) DataComponents.CUSTOM_DATA, stack, root -> {
            CompoundTag tag = getOrCreate(root);
            tag.putString(LINK_DIM, link.dimension().toString());
            tag.putLong(LINK_POS, link.pos().asLong());
            if (!tag.contains(TIER)) tag.putInt(TIER, 1);
            if (!tag.contains(MODE)) tag.putString(MODE, Mode.PREVIEW.name());
            root.put(ROOT_TAG, tag);
        });
    }

    public static Optional<ProductionTabletItem.MonitorLink> getLink(ItemStack stack) {
        if (!(stack.getItem() instanceof MultiblockConstructorItem)) return Optional.empty();
        CompoundTag tag = data(stack);
        if (tag == null || !tag.contains(LINK_DIM) || !tag.contains(LINK_POS)) return Optional.empty();
        ResourceLocation dim = ResourceLocation.tryParse(tag.getString(LINK_DIM));
        if (dim == null) return Optional.empty();
        return Optional.of(new ProductionTabletItem.MonitorLink(dim, BlockPos.of(tag.getLong(LINK_POS))));
    }

    public static int getTier(ItemStack stack) {
        CompoundTag tag = data(stack);
        if (tag == null || !tag.contains(TIER)) return 1;
        return Math.max(1, Math.min(3, tag.getInt(TIER)));
    }

    public static void setTier(ItemStack stack, int tier) {
        int safe = Math.max(1, Math.min(3, tier));
        CustomData.update((DataComponentType) DataComponents.CUSTOM_DATA, stack, root -> {
            CompoundTag tag = getOrCreate(root);
            tag.putInt(TIER, safe);
            root.put(ROOT_TAG, tag);
        });
    }

    public static Mode getMode(ItemStack stack) {
        CompoundTag tag = data(stack);
        if (tag == null || !tag.contains(MODE)) return Mode.PREVIEW;
        try {
            return Mode.valueOf(tag.getString(MODE));
        } catch (IllegalArgumentException ignored) {
            return Mode.PREVIEW;
        }
    }

    public static void setMode(ItemStack stack, Mode mode) {
        CustomData.update((DataComponentType) DataComponents.CUSTOM_DATA, stack, root -> {
            CompoundTag tag = getOrCreate(root);
            tag.putString(MODE, mode.name());
            root.put(ROOT_TAG, tag);
        });
    }

    public static Optional<PreviewTarget> getTarget(ItemStack stack) {
        if (!(stack.getItem() instanceof MultiblockConstructorItem)) return Optional.empty();
        CompoundTag tag = data(stack);
        if (tag == null || !tag.contains(TARGET_DIM)
                || !tag.contains(TARGET_POS) || !tag.contains(TARGET_FRONT)) {
            return Optional.empty();
        }
        ResourceLocation dim = ResourceLocation.tryParse(tag.getString(TARGET_DIM));
        Direction front = Direction.byName(tag.getString(TARGET_FRONT));
        if (dim == null || front == null || front.getAxis().isVertical()) return Optional.empty();
        return Optional.of(new PreviewTarget(dim, BlockPos.of(tag.getLong(TARGET_POS)), front));
    }

    public static void setTarget(ItemStack stack, PreviewTarget target) {
        CustomData.update((DataComponentType) DataComponents.CUSTOM_DATA, stack, root -> {
            CompoundTag tag = getOrCreate(root);
            tag.putString(TARGET_DIM, target.dimension().toString());
            tag.putLong(TARGET_POS, target.corePos().asLong());
            tag.putString(TARGET_FRONT, target.front().getName());
            if (!tag.contains(TIER)) tag.putInt(TIER, 1);
            if (!tag.contains(MODE)) tag.putString(MODE, Mode.PREVIEW.name());
            root.put(ROOT_TAG, tag);
        });
    }

    private static CompoundTag data(ItemStack stack) {
        CompoundTag root = ((CustomData) stack.getOrDefault(
                DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        return root.contains(ROOT_TAG) ? root.getCompound(ROOT_TAG) : null;
    }

    private static CompoundTag getOrCreate(CompoundTag root) {
        return root.contains(ROOT_TAG) ? root.getCompound(ROOT_TAG) : new CompoundTag();
    }

    private static String roman(int tier) {
        return switch (tier) {
            case 2 -> "II";
            case 3 -> "III";
            default -> "I";
        };
    }

    public record PreviewTarget(ResourceLocation dimension, BlockPos corePos, Direction front) {}
}
