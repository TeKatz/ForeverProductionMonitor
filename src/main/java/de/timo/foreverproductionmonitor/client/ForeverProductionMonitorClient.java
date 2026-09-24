/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.LivingEntity
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.fml.IExtensionPoint
 *  net.neoforged.fml.ModContainer
 *  net.neoforged.fml.common.Mod
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Post
 *  net.neoforged.neoforge.client.event.RegisterGuiLayersEvent
 *  net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
 *  net.neoforged.neoforge.client.gui.IConfigScreenFactory
 *  net.neoforged.neoforge.client.gui.VanillaGuiLayers
 *  net.neoforged.neoforge.common.NeoForge
 */
package de.timo.foreverproductionmonitor.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.timo.foreverproductionmonitor.client.ClientConfig;
import de.timo.foreverproductionmonitor.client.ClientMonitorState;
import de.timo.foreverproductionmonitor.client.ClientTabletHooks;
import de.timo.foreverproductionmonitor.client.DeviceLocator;
import de.timo.foreverproductionmonitor.client.ProductionHud;
import de.timo.foreverproductionmonitor.client.screen.ProductionMonitorThemeScreen;
import de.timo.foreverproductionmonitor.integration.TabletCurios;
import de.timo.foreverproductionmonitor.item.ProductionTabletItem;
import de.timo.foreverproductionmonitor.network.MonitorNetwork;
import java.util.Optional;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value="forever_production_monitor", dist={Dist.CLIENT})
public final class ForeverProductionMonitorClient {
    public static final KeyMapping OPEN_TABLET = new KeyMapping("key.forever_production_monitor.open_tablet", InputConstants.Type.KEYSYM, 80, "key.categories.forever_production_monitor");
    private static int hudRequestTicks;
    private static int dashboardRequestTicks;

    public ForeverProductionMonitorClient(IEventBus iEventBus, ModContainer modContainer2) {
        iEventBus.addListener(ForeverProductionMonitorClient::registerKeys);
        iEventBus.addListener(ForeverProductionMonitorClient::registerGuiLayers);
        NeoForge.EVENT_BUS.addListener(ForeverProductionMonitorClient::clientTick);
        NeoForge.EVENT_BUS.addListener(DeviceLocator::render);
        modContainer2.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, screen) -> new ProductionMonitorThemeScreen(screen));
    }

    private static void registerKeys(RegisterKeyMappingsEvent registerKeyMappingsEvent) {
        registerKeyMappingsEvent.register(OPEN_TABLET);
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent registerGuiLayersEvent) {
        registerGuiLayersEvent.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath((String)"forever_production_monitor", (String)"production_hud"), ProductionHud::render);
    }

    private static void clientTick(ClientTickEvent.Post post) {
        int n;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            ClientMonitorState.clearHud();
            hudRequestTicks = 0;
            dashboardRequestTicks = 0;
            return;
        }
        Optional<ProductionTabletItem.MonitorLink> optional = TabletCurios.findEquippedLink((LivingEntity)minecraft.player);
        while (OPEN_TABLET.consumeClick()) {
            if (minecraft.screen != null) continue;
            optional.ifPresentOrElse(ClientTabletHooks::open, () -> minecraft.player.displayClientMessage((Component)Component.translatable((String)"message.forever_production_monitor.no_equipped_tablet"), true));
        }
        if (optional.isEmpty()) {
            ClientMonitorState.clearHud();
            hudRequestTicks = 0;
            dashboardRequestTicks = 0;
            return;
        }
        if (dashboardRequestTicks-- <= 0) {
            n = ((ClientConfig.RefreshInterval)((Object)ClientConfig.VALUES.refreshInterval.get())).ticks();
            MonitorNetwork.requestDashboard(optional.get());
            dashboardRequestTicks = n;
        }
        if (!((Boolean)ClientConfig.VALUES.hudEnabled.get()).booleanValue()) {
            ClientMonitorState.clearHud();
            hudRequestTicks = 0;
            return;
        }
        if (hudRequestTicks-- <= 0) {
            n = ((ClientConfig.RefreshInterval)((Object)ClientConfig.VALUES.refreshInterval.get())).ticks();
            MonitorNetwork.requestHud((MonitorNetwork.HudMode)((Object)ClientConfig.VALUES.hudMode.get()), n, (Integer)ClientConfig.VALUES.hudEntryCount.get());
            hudRequestTicks = n;
        }
    }

    public static void refreshHudNow() {
        hudRequestTicks = 0;
        ClientMonitorState.clearHud();
    }
}
