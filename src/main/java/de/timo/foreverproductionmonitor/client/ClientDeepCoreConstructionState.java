package de.timo.foreverproductionmonitor.client;

import de.timo.foreverproductionmonitor.client.screen.DeepCoreConstructionScreen;
import de.timo.foreverproductionmonitor.network.DeepCoreConstructionNetwork;
import net.minecraft.client.Minecraft;

public final class ClientDeepCoreConstructionState {
    private ClientDeepCoreConstructionState() {}

    public static void acceptPlan(DeepCoreConstructionNetwork.PlanResponse response) {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().screen instanceof DeepCoreConstructionScreen screen) {
                screen.acceptPlan(response);
            }
        });
    }

    public static void acceptBatch(DeepCoreConstructionNetwork.BatchResult response) {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().screen instanceof DeepCoreConstructionScreen screen) {
                screen.acceptBatch(response);
            }
        });
    }
}
