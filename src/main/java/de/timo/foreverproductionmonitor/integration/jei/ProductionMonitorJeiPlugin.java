package de.timo.foreverproductionmonitor.integration.jei;

import appeng.core.definitions.AEItems;
import de.timo.foreverproductionmonitor.client.ClientPatternImportQueue;
import de.timo.foreverproductionmonitor.integration.TabletCurios;
import de.timo.foreverproductionmonitor.network.PatternImportNetwork;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.buttons.IButtonState;
import mezz.jei.api.gui.buttons.IIconButtonController;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.recipe.advanced.IRecipeButtonControllerFactory;
import mezz.jei.api.registration.IAdvancedRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Adds Pattern Import controls beside JEI crafting/smithing recipes.
 * No processing recipe category is registered or accepted.
 */
@JeiPlugin
public final class ProductionMonitorJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(
            "forever_production_monitor", "pattern_import");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerAdvanced(IAdvancedRegistration registration) {
        IDrawable rawBlankPattern = registration.getJeiHelpers().getGuiHelper()
                .createDrawableIngredient(VanillaTypes.ITEM_STACK, AEItems.BLANK_PATTERN.stack());
        // JEI's side buttons are 13x13 while a normal item drawable is 16x16.
        // Scale the item down so it stays cleanly inside the button frame.
        IDrawable blankPattern = new ScaledDrawable(rawBlankPattern, 9, 9);

        registration.addRecipeButtonFactory(new IRecipeButtonControllerFactory() {
            @Override
            public <T> IIconButtonController createButtonController(IRecipeLayoutDrawable<T> layout) {
                return createButton(layout, blankPattern);
            }
        });
    }

    private static <T> IIconButtonController createButton(IRecipeLayoutDrawable<T> layout, IDrawable icon) {
        var type = layout.getRecipeCategory().getRecipeType();
        boolean supported = type.equals(RecipeTypes.CRAFTING) || type.equals(RecipeTypes.SMITHING);
        if (!supported || !(layout.getRecipe() instanceof RecipeHolder<?> holder)) return null;
        return new PatternImportButton(holder.id(), icon);
    }

    private record PatternImportButton(ResourceLocation recipeId, IDrawable icon) implements IIconButtonController {
        @Override
        public boolean onPress(IJeiUserInput input) {
            if (input.isSimulate()) return true;

            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) return true;

            if (Screen.hasControlDown()) {
                var link = TabletCurios.findEquippedLink(minecraft.player);
                if (link.isEmpty()) {
                    warnMissingTablet(minecraft);
                    return true;
                }
                ClientPatternImportQueue.uploadQueued(link.get());
                return true;
            }

            if (Screen.hasShiftDown()) {
                ClientPatternImportQueue.toggleAndNotify(recipeId);
                return true;
            }

            var link = TabletCurios.findEquippedLink(minecraft.player);
            if (link.isEmpty()) {
                warnMissingTablet(minecraft);
                return true;
            }

            PatternImportNetwork.request(link.get(), recipeId);
            return true;
        }

        @Override
        public void initState(IButtonState state) {
            state.setIcon(icon);
            state.setActive(true);
            state.setVisible(true);
            state.setForcePressed(ClientPatternImportQueue.contains(recipeId));
        }

        @Override
        public void updateState(IButtonState state) {
            state.setActive(Minecraft.getInstance().player != null
                    && !ClientPatternImportQueue.isBatchActive());
            state.setForcePressed(ClientPatternImportQueue.contains(recipeId));
        }

        @Override
        public void getTooltips(ITooltipBuilder tooltip) {
            tooltip.add(Component.literal("AE2 Pattern → Assembler Matrix")
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.literal("Click: import this recipe now")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Shift + Click: add/remove queue")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Ctrl + Click: upload queue ("
                            + ClientPatternImportQueue.size() + ")")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Crafting & Smithing only · Processing is blocked")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static void warnMissingTablet(Minecraft minecraft) {
        if (minecraft.player == null) return;
        minecraft.player.displayClientMessage(
                Component.literal("Equip a linked Production Tablet to upload patterns")
                        .withStyle(ChatFormatting.RED), true);
    }

    private record ScaledDrawable(IDrawable source, int width, int height) implements IDrawable {
        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
            float xScale = (float) width / source.getWidth();
            float yScale = (float) height / source.getHeight();
            var pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(xOffset, yOffset, 0.0F);
            pose.scale(xScale, yScale, 1.0F);
            source.draw(guiGraphics, 0, 0);
            pose.popPose();
        }
    }
}
