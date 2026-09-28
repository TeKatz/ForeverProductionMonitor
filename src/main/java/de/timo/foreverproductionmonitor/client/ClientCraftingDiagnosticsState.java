package de.timo.foreverproductionmonitor.client;

import de.timo.foreverproductionmonitor.client.screen.CraftingDiagnosticsScreen;
import de.timo.foreverproductionmonitor.network.CraftingDiagnosticsNetwork;
import net.minecraft.client.Minecraft;

public final class ClientCraftingDiagnosticsState {
    private ClientCraftingDiagnosticsState() {}

    public static void accept(CraftingDiagnosticsNetwork.Response response) {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().screen instanceof CraftingDiagnosticsScreen screen) {
                screen.accept(response);
            }
        });
    }

    public static void acceptCraftResult(CraftingDiagnosticsNetwork.CraftResult response) {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().screen instanceof CraftingDiagnosticsScreen screen) {
                screen.acceptCraftResult(response);
            }
        });
    }
}
