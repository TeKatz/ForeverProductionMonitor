package de.timo.foreverproductionmonitor.client;

import de.timo.foreverproductionmonitor.blockentity.AssemblerMatrixPatternImporter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Client-only feedback for one-click JEI pattern uploads. */
public final class ClientPatternImportFeedback {
    private ClientPatternImportFeedback() {}

    public static void accept(ResourceLocation recipeId, AssemblerMatrixPatternImporter.Result result) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        Component message = switch (result) {
            case UPLOADED -> Component.literal("Pattern uploaded to Assembler Matrix").withStyle(ChatFormatting.GREEN);
            case ALREADY_EXISTS -> Component.literal("Pattern already exists in the AE2 network").withStyle(ChatFormatting.YELLOW);
            case NO_BLANK_PATTERN -> Component.literal("No Blank Pattern available in ME storage").withStyle(ChatFormatting.RED);
            case NO_ASSEMBLER_MATRIX -> Component.literal("No active Assembler Matrix Pattern Core found").withStyle(ChatFormatting.RED);
            case MATRIX_FULL -> Component.literal("Assembler Matrix has no free pattern slot").withStyle(ChatFormatting.RED);
            case UNSUPPORTED_RECIPE -> Component.literal("Only crafting and smithing recipes are supported").withStyle(ChatFormatting.RED);
            case RECIPE_NOT_FOUND -> Component.literal("Recipe is no longer available: " + recipeId).withStyle(ChatFormatting.RED);
            case INVALID_LINK -> Component.literal("Linked Production Monitor is unavailable").withStyle(ChatFormatting.RED);
            case NETWORK_OFFLINE -> Component.literal("Production Monitor / AE2 network is offline").withStyle(ChatFormatting.RED);
            case ENCODE_FAILED -> Component.literal("Pattern could not be encoded safely").withStyle(ChatFormatting.RED);
        };
        minecraft.player.displayClientMessage(message, true);
    }
}
