package de.timo.foreverproductionmonitor.integration.jei;

import appeng.core.definitions.AEItems;
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
import mezz.jei.api.registration.IAdvancedRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Adds a one-click Pattern Import button beside JEI crafting/smithing recipes.
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
        IDrawable blankPattern = registration.getJeiHelpers().getGuiHelper()
                .createDrawableIngredient(VanillaTypes.ITEM_STACK, AEItems.BLANK_PATTERN.stack());

        registration.addRecipeButtonFactory(layout -> createButton(layout, blankPattern));
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
            var link = TabletCurios.findEquippedLink(minecraft.player);
            if (link.isEmpty()) {
                minecraft.player.displayClientMessage(
                        Component.literal("Equip a linked Production Tablet to upload patterns")
                                .withStyle(ChatFormatting.RED), true);
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
        }

        @Override
        public void updateState(IButtonState state) {
            state.setActive(Minecraft.getInstance().player != null);
        }

        @Override
        public void getTooltips(ITooltipBuilder tooltip) {
            tooltip.add(Component.literal("Upload AE2 pattern to Assembler Matrix")
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.literal("Crafting & Smithing only · Processing is blocked")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Requires a linked Production Tablet and 1 Blank Pattern in ME storage")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
