package de.timo.foreverproductionmonitor.blockentity;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEItemKey;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.AECraftingPattern;
import appeng.crafting.pattern.AESmithingTablePattern;
import appeng.helpers.patternprovider.PatternContainer;
import appeng.me.helpers.PlayerSource;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;

/**
 * Server-side one-click importer for Assembler Matrix patterns.
 *
 * <p>This class deliberately has a very narrow contract: only vanilla crafting-recipe and
 * smithing-recipe pattern encodings are accepted. Processing, stonecutting and third-party
 * pattern kinds are rejected both before and after encoding.</p>
 */
public final class AssemblerMatrixPatternImporter {
    private AssemblerMatrixPatternImporter() {}

    public static Result upload(ServerPlayer player, ProductionMonitorBlockEntity monitor, ResourceLocation recipeId) {
        if (player == null || monitor == null || recipeId == null) return Result.INVALID_LINK;
        if (!monitor.isMonitorOnline() || monitor.getMainNode().getGrid() == null) return Result.NETWORK_OFFLINE;

        var holder = player.serverLevel().getRecipeManager().byKey(recipeId).orElse(null);
        if (holder == null) return Result.RECIPE_NOT_FOUND;

        ItemStack encoded;
        try {
            if (holder.value() instanceof CraftingRecipe crafting) {
                encoded = encodeCrafting(player, holder.id(), crafting);
            } else if (holder.value() instanceof SmithingRecipe smithing) {
                encoded = encodeSmithing(player, holder.id(), smithing);
            } else {
                return Result.UNSUPPORTED_RECIPE;
            }
        } catch (UnsupportedRecipeException ignored) {
            return Result.UNSUPPORTED_RECIPE;
        } catch (Throwable ignored) {
            return Result.ENCODE_FAILED;
        }

        if (encoded.isEmpty()) return Result.ENCODE_FAILED;
        IPatternDetails details = PatternDetailsHelper.decodePattern(encoded, player.serverLevel());
        // Second hard safety gate. Even a future recipe/decoder change cannot smuggle a processing pattern in.
        if (!(details instanceof AECraftingPattern) && !(details instanceof AESmithingTablePattern)) {
            return Result.UNSUPPORTED_RECIPE;
        }

        IGrid grid = monitor.getMainNode().getGrid();
        if (grid == null) return Result.NETWORK_OFFLINE;
        if (hasDuplicate(grid, details)) return Result.ALREADY_EXISTS;

        MatrixSlot destination = findMatrixSlot(grid, encoded);
        if (destination == null) return hasAssemblerMatrix(grid) ? Result.MATRIX_FULL : Result.NO_ASSEMBLER_MATRIX;

        var source = new PlayerSource(player, monitor);
        var storage = grid.getStorageService().getInventory();
        AEItemKey blankPattern = AEItemKey.of(AEItems.BLANK_PATTERN.stack());
        if (blankPattern == null) return Result.ENCODE_FAILED;

        long available = storage.extract(blankPattern, 1, Actionable.SIMULATE, source);
        if (available < 1) return Result.NO_BLANK_PATTERN;
        if (!destination.inventory().simulateAdd(encoded).isEmpty()) return Result.MATRIX_FULL;

        long extracted = storage.extract(blankPattern, 1, Actionable.MODULATE, source);
        if (extracted < 1) return Result.NO_BLANK_PATTERN;

        ItemStack remainder = destination.inventory().insertItem(destination.slot(), encoded, false);
        if (!remainder.isEmpty()) {
            storage.insert(blankPattern, 1, Actionable.MODULATE, source);
            return Result.MATRIX_FULL;
        }

        return Result.UPLOADED;
    }

    @SuppressWarnings("unchecked")
    private static ItemStack encodeCrafting(ServerPlayer player, ResourceLocation id, CraftingRecipe recipe) {
        if (!(recipe instanceof ShapedRecipe) && !(recipe instanceof ShapelessRecipe)) {
            // Special/dynamic recipes need recipe-specific input construction. Keep v1 deterministic and safe.
            throw new UnsupportedRecipeException();
        }

        ItemStack[] sparse = new ItemStack[9];
        for (int i = 0; i < sparse.length; i++) sparse[i] = ItemStack.EMPTY;
        List<Ingredient> ingredients = recipe.getIngredients();

        if (recipe instanceof ShapedRecipe shaped) {
            int width = shaped.getWidth();
            int height = shaped.getHeight();
            if (width <= 0 || height <= 0 || width > 3 || height > 3 || ingredients.size() != width * height) {
                throw new UnsupportedRecipeException();
            }
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    sparse[y * 3 + x] = representative(ingredients.get(y * width + x));
                }
            }
        } else {
            if (ingredients.isEmpty() || ingredients.size() > 9) throw new UnsupportedRecipeException();
            for (int i = 0; i < ingredients.size(); i++) sparse[i] = representative(ingredients.get(i));
        }

        NonNullList<ItemStack> inputItems = NonNullList.withSize(9, ItemStack.EMPTY);
        for (int i = 0; i < sparse.length; i++) inputItems.set(i, sparse[i]);
        CraftingInput input = CraftingInput.of(3, 3, inputItems);
        if (!recipe.matches(input, player.serverLevel())) throw new UnsupportedRecipeException();
        ItemStack output = recipe.assemble(input, player.serverLevel().registryAccess());
        if (output.isEmpty()) throw new UnsupportedRecipeException();

        RecipeHolder<CraftingRecipe> typed = new RecipeHolder<>(id, recipe);
        return PatternDetailsHelper.encodeCraftingPattern(typed, sparse, output, true, false);
    }

    private static ItemStack encodeSmithing(ServerPlayer player, ResourceLocation id, SmithingRecipe recipe) {
        List<Ingredient> ingredients = recipe.getIngredients();
        if (ingredients.size() != 3) throw new UnsupportedRecipeException();

        ItemStack template = representative(ingredients.get(0));
        ItemStack base = representative(ingredients.get(1));
        ItemStack addition = representative(ingredients.get(2));
        if (template.isEmpty() || base.isEmpty() || addition.isEmpty()) throw new UnsupportedRecipeException();

        SmithingRecipeInput input = new SmithingRecipeInput(template, base, addition);
        if (!recipe.matches(input, player.serverLevel())) throw new UnsupportedRecipeException();
        ItemStack outputStack = recipe.assemble(input, player.serverLevel().registryAccess());
        AEItemKey templateKey = AEItemKey.of(template);
        AEItemKey baseKey = AEItemKey.of(base);
        AEItemKey additionKey = AEItemKey.of(addition);
        AEItemKey outputKey = AEItemKey.of(outputStack);
        if (templateKey == null || baseKey == null || additionKey == null || outputKey == null) {
            throw new UnsupportedRecipeException();
        }

        RecipeHolder<SmithingRecipe> typed = new RecipeHolder<>(id, recipe);
        return PatternDetailsHelper.encodeSmithingTablePattern(
                typed, templateKey, baseKey, additionKey, outputKey, true);
    }

    private static ItemStack representative(Ingredient ingredient) {
        if (ingredient == null || ingredient.isEmpty()) return ItemStack.EMPTY;
        ItemStack[] candidates = ingredient.getItems();
        if (candidates.length == 0) throw new UnsupportedRecipeException();
        ItemStack chosen = candidates[0].copy();
        chosen.setCount(1);
        return chosen;
    }

    private static boolean hasDuplicate(IGrid grid, IPatternDetails candidate) {
        var definition = candidate.getDefinition();
        if (definition == null) return false;
        for (var node : grid.getNodes()) {
            ICraftingProvider provider = node.getService(ICraftingProvider.class);
            if (provider == null) continue;
            for (IPatternDetails existing : provider.getAvailablePatterns()) {
                if (existing != null && definition.equals(existing.getDefinition())) return true;
            }
        }
        return false;
    }

    private static boolean hasAssemblerMatrix(IGrid grid) {
        for (var node : grid.getNodes()) {
            if (isAssemblerMatrixPatternOwner(node.getOwner())) return true;
        }
        return false;
    }

    private static MatrixSlot findMatrixSlot(IGrid grid, ItemStack encoded) {
        for (var node : grid.getNodes()) {
            if (!node.isActive()) continue;
            Object owner = node.getOwner();
            if (!isAssemblerMatrixPatternOwner(owner) || !(owner instanceof PatternContainer container)) continue;
            InternalInventory inventory = container.getTerminalPatternInventory();
            for (int slot = 0; slot < inventory.size(); slot++) {
                if (!inventory.getStackInSlot(slot).isEmpty()) continue;
                if (!inventory.isItemValid(slot, encoded)) continue;
                if (inventory.insertItem(slot, encoded, true).isEmpty()) {
                    return new MatrixSlot(inventory, slot);
                }
            }
        }
        return null;
    }

    private static boolean isAssemblerMatrixPatternOwner(Object owner) {
        if (owner == null) return false;
        String name = owner.getClass().getName().toLowerCase(Locale.ROOT);
        return name.contains("extendedae") && name.contains("assemblermatrixpattern");
    }

    private record MatrixSlot(InternalInventory inventory, int slot) {}

    private static final class UnsupportedRecipeException extends RuntimeException {}

    public enum Result {
        UPLOADED,
        ALREADY_EXISTS,
        NO_BLANK_PATTERN,
        NO_ASSEMBLER_MATRIX,
        MATRIX_FULL,
        UNSUPPORTED_RECIPE,
        RECIPE_NOT_FOUND,
        INVALID_LINK,
        NETWORK_OFFLINE,
        ENCODE_FAILED
    }
}
