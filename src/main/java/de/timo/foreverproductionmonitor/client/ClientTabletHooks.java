/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 */
package de.timo.foreverproductionmonitor.client;

import de.timo.foreverproductionmonitor.client.screen.ProductionMonitorScreen;
import de.timo.foreverproductionmonitor.item.ProductionTabletItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class ClientTabletHooks {
    private ClientTabletHooks() {
    }

    public static void open(ProductionTabletItem.MonitorLink link) {
        Minecraft.getInstance().setScreen((Screen)new ProductionMonitorScreen(link));
    }
}

