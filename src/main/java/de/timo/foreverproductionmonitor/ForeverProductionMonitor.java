/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.fml.ModContainer
 *  net.neoforged.fml.common.Mod
 *  net.neoforged.fml.config.IConfigSpec
 *  net.neoforged.fml.config.ModConfig$Type
 *  org.slf4j.Logger
 */
package de.timo.foreverproductionmonitor;

import com.mojang.logging.LogUtils;
import de.timo.foreverproductionmonitor.ModContent;
import de.timo.foreverproductionmonitor.client.ClientConfig;
import de.timo.foreverproductionmonitor.network.MonitorNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(value="forever_production_monitor")
public final class ForeverProductionMonitor {
    public static final String MOD_ID = "forever_production_monitor";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ForeverProductionMonitor(IEventBus modBus, ModContainer container) {
        ModContent.register(modBus);
        modBus.addListener(ModContent::registerCapabilities);
        modBus.addListener(ModContent::addCreativeTabContents);
        modBus.addListener(MonitorNetwork::register);
        container.registerConfig(ModConfig.Type.CLIENT, (IConfigSpec)ClientConfig.SPEC);
    }
}

