/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 */
package de.timo.foreverproductionmonitor.client;

import de.timo.foreverproductionmonitor.blockentity.ProductionMonitorBlockEntity;
import de.timo.foreverproductionmonitor.client.screen.ProductionMonitorScreen;
import de.timo.foreverproductionmonitor.network.MonitorNetwork;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ClientMonitorState {
    private static MonitorNetwork.HudSnapshot hudSnapshot;
    private static long hudReceivedAt;
    private static MonitorNetwork.DashboardSnapshot dashboardSnapshot;
    private static final Set<String> activeAlarms;
    private static boolean alarmBaselineReady;

    private ClientMonitorState() {
    }

    public static void accept(MonitorNetwork.MonitorSnapshot monitorSnapshot) {
        Minecraft.getInstance().execute(() -> {
            Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof ProductionMonitorScreen) {
                ProductionMonitorScreen productionMonitorScreen = (ProductionMonitorScreen)screen;
                productionMonitorScreen.accept(monitorSnapshot);
            }
        });
    }

    public static void acceptHud(MonitorNetwork.HudSnapshot hudSnapshot) {
        Minecraft.getInstance().execute(() -> {
            ClientMonitorState.hudSnapshot = hudSnapshot;
            hudReceivedAt = System.currentTimeMillis();
        });
    }

    public static void acceptStatistics(MonitorNetwork.StatisticsSnapshot statisticsSnapshot) {
        Minecraft.getInstance().execute(() -> {
            Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof ProductionMonitorScreen) {
                ProductionMonitorScreen productionMonitorScreen = (ProductionMonitorScreen)screen;
                productionMonitorScreen.acceptStatistics(statisticsSnapshot);
            }
        });
    }

    public static void acceptDashboard(MonitorNetwork.DashboardSnapshot dashboardSnapshot) {
        Minecraft.getInstance().execute(() -> {
            HashSet hashSet = new HashSet();
            dashboardSnapshot.entries().stream().filter(dashboardEntry -> dashboardEntry.alarmState() == ProductionMonitorBlockEntity.AlarmState.ACTIVE).forEach(dashboardEntry -> {
                String string = String.valueOf((Object)dashboardEntry.kind()) + ":" + String.valueOf(dashboardEntry.key().getId()) + ":" + dashboardEntry.key().hashCode();
                hashSet.add(string);
                if (alarmBaselineReady && !activeAlarms.contains(string) && Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.displayClientMessage((Component)Component.translatable((String)"message.forever_production_monitor.alarm", (Object[])new Object[]{dashboardEntry.key().getDisplayName()}), false);
                }
            });
            activeAlarms.clear();
            activeAlarms.addAll(hashSet);
            alarmBaselineReady = true;
            ClientMonitorState.dashboardSnapshot = dashboardSnapshot;
            Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof ProductionMonitorScreen) {
                ProductionMonitorScreen productionMonitorScreen = (ProductionMonitorScreen)screen;
                productionMonitorScreen.acceptDashboard(dashboardSnapshot);
            }
        });
    }

    public static void acceptNetworkMap(MonitorNetwork.NetworkMapPayload networkMapPayload) {
        Minecraft.getInstance().execute(() -> {
            Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof ProductionMonitorScreen) {
                ProductionMonitorScreen productionMonitorScreen = (ProductionMonitorScreen)screen;
                productionMonitorScreen.acceptNetworkMap(networkMapPayload);
            }
        });
    }

    public static void acceptDeepCoreFacilities(MonitorNetwork.DeepCoreFacilitiesPayload payload) {
        Minecraft.getInstance().execute(() -> {
            Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof ProductionMonitorScreen productionMonitorScreen) {
                productionMonitorScreen.acceptDeepCoreFacilities(payload);
            }
        });
    }

    public static void acceptDeepCoreDiagnostics(MonitorNetwork.DeepCoreDiagnosticsPayload payload) {
        Minecraft.getInstance().execute(() -> {
            Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof ProductionMonitorScreen productionMonitorScreen) {
                productionMonitorScreen.acceptDeepCoreDiagnostics(payload);
            }
        });
    }

    public static MonitorNetwork.DashboardSnapshot dashboardSnapshot() {
        return dashboardSnapshot;
    }

    public static MonitorNetwork.HudSnapshot hudSnapshot() {
        return hudSnapshot;
    }

    public static boolean hudFresh() {
        return hudSnapshot != null && System.currentTimeMillis() - hudReceivedAt < 15000L;
    }

    public static void clearHud() {
        hudSnapshot = null;
        hudReceivedAt = 0L;
    }

    static {
        activeAlarms = new HashSet<String>();
    }
}

