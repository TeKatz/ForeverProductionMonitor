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
import de.timo.foreverproductionmonitor.network.PatternImportNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(value="forever_production_monitor")
public final class ForeverProductionMonitor {
    public static final String MOD_ID = "forever_production_monitor";
    public static String VERSION = "unknown";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ForeverProductionMonitor(IEventBus modBus, ModContainer container) {
        VERSION = container.getModInfo().getVersion().toString();
        ModContent.register(modBus);
        modBus.addListener(ModContent::registerCapabilities);
        modBus.addListener(ModContent::addCreativeTabContents);
        modBus.addListener(MonitorNetwork::register);
        modBus.addListener(PatternImportNetwork::register);
        container.registerConfig(ModConfig.Type.CLIENT, (IConfigSpec)ClientConfig.SPEC);
    }
}
