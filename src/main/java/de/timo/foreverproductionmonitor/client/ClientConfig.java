/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.neoforge.common.ModConfigSpec
 *  net.neoforged.neoforge.common.ModConfigSpec$BooleanValue
 *  net.neoforged.neoforge.common.ModConfigSpec$Builder
 *  net.neoforged.neoforge.common.ModConfigSpec$DoubleValue
 *  net.neoforged.neoforge.common.ModConfigSpec$EnumValue
 *  net.neoforged.neoforge.common.ModConfigSpec$IntValue
 *  org.apache.commons.lang3.tuple.Pair
 */
package de.timo.foreverproductionmonitor.client;

import de.timo.foreverproductionmonitor.network.MonitorNetwork;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class ClientConfig {
    public static final Values VALUES;
    public static final ModConfigSpec SPEC;

    private ClientConfig() {
    }

    public static HudFrameStyle effectiveHudFrameStyle() {
        if (!((Boolean)ClientConfig.VALUES.matchHudTheme.get()).booleanValue()) {
            return (HudFrameStyle)((Object)ClientConfig.VALUES.hudFrameStyle.get());
        }
        return switch ((InterfaceStyle)((Object)ClientConfig.VALUES.interfaceStyle.get())) {
            default -> throw new IncompatibleClassChangeError();
            case InterfaceStyle.STANDARD -> HudFrameStyle.STANDARD;
            case InterfaceStyle.FOREVER -> HudFrameStyle.FOREVER;
            case InterfaceStyle.AE2 -> HudFrameStyle.AE2;
            case InterfaceStyle.ORITECH -> HudFrameStyle.ORITECH;
            case InterfaceStyle.MEKANISM, InterfaceStyle.HOLOGRAPHIC, InterfaceStyle.TERMINAL, InterfaceStyle.DEEP_SPACE, InterfaceStyle.FROST -> HudFrameStyle.AE2;
            case InterfaceStyle.QUANTUM, InterfaceStyle.AURORA, InterfaceStyle.NATURE -> HudFrameStyle.FOREVER;
            case InterfaceStyle.MONOCHROME, InterfaceStyle.CARBON, InterfaceStyle.COPPER, InterfaceStyle.REDSTONE -> HudFrameStyle.STANDARD;
            case InterfaceStyle.MINIMAL -> HudFrameStyle.NONE;
            case InterfaceStyle.CUSTOM -> HudFrameStyle.CUSTOM;
        };
    }

    public static InterfaceStyle effectiveHudTheme() {
        return (Boolean)ClientConfig.VALUES.matchHudTheme.get()
                ? (InterfaceStyle)((Object)ClientConfig.VALUES.interfaceStyle.get())
                : (InterfaceStyle)((Object)ClientConfig.VALUES.hudThemeStyle.get());
    }

    public static int effectiveHudCustomHue() {
        return (Boolean)ClientConfig.VALUES.matchHudTheme.get() != false ? (Integer)ClientConfig.VALUES.interfaceCustomHue.get() : (Integer)ClientConfig.VALUES.hudCustomHue.get();
    }

    static {
        Pair pair = new ModConfigSpec.Builder().configure(Values::new);
        VALUES = (Values)pair.getLeft();
        SPEC = (ModConfigSpec)pair.getRight();
    }

    public static final class Values {
        public final ModConfigSpec.BooleanValue hudEnabled;
        public final ModConfigSpec.EnumValue<HudAnchor> hudAnchor;
        public final ModConfigSpec.IntValue hudXOffset;
        public final ModConfigSpec.IntValue hudYOffset;
        public final ModConfigSpec.DoubleValue hudScale;
        public final ModConfigSpec.DoubleValue hudOpacity;
        public final ModConfigSpec.EnumValue<MonitorNetwork.HudMode> hudMode;
        public final ModConfigSpec.EnumValue<HudFrameStyle> hudFrameStyle;
        public final ModConfigSpec.EnumValue<InterfaceStyle> hudThemeStyle;
        public final ModConfigSpec.EnumValue<HudLayoutStyle> hudLayoutStyle;
        public final ModConfigSpec.BooleanValue hudAnimations;
        public final ModConfigSpec.DoubleValue hudAnimationIntensity;
        public final ModConfigSpec.IntValue hudCustomHue;
        public final ModConfigSpec.IntValue hudEntryCount;
        public final ModConfigSpec.BooleanValue hudShowIcons;
        public final ModConfigSpec.BooleanValue hudShowNames;
        public final ModConfigSpec.BooleanValue hudShowValues;
        public final ModConfigSpec.EnumValue<RateUnit> rateUnit;
        public final ModConfigSpec.EnumValue<RefreshInterval> refreshInterval;
        public final ModConfigSpec.EnumValue<InterfaceStyle> interfaceStyle;
        public final ModConfigSpec.IntValue interfaceCustomHue;
        public final ModConfigSpec.IntValue interfaceCustomSecondaryHue;
        public final ModConfigSpec.DoubleValue interfaceCustomBrightness;
        public final ModConfigSpec.DoubleValue interfaceOpacity;
        public final ModConfigSpec.BooleanValue guiAnimations;
        public final ModConfigSpec.BooleanValue updatePulseEnabled;
        public final ModConfigSpec.DoubleValue updatePulseIntensity;
        public final ModConfigSpec.DoubleValue updatePulseDuration;
        public final ModConfigSpec.BooleanValue ambientMotionEnabled;
        public final ModConfigSpec.DoubleValue ambientMotionIntensity;
        public final ModConfigSpec.BooleanValue matchHudTheme;
        public final ModConfigSpec.BooleanValue invertMapRotation;
        public final ModConfigSpec.EnumValue<DefaultTab> defaultTab;
        public final ModConfigSpec.BooleanValue rememberLastTab;
        public final ModConfigSpec.EnumValue<MapDefaultView> mapDefaultView;
        public final ModConfigSpec.DoubleValue mapRotationSensitivity;
        public final ModConfigSpec.DoubleValue mapPanSensitivity;
        public final ModConfigSpec.DoubleValue mapZoomSensitivity;
        public final ModConfigSpec.DoubleValue mapFocusZoom;
        public final ModConfigSpec.BooleanValue mapInvertHorizontal;
        public final ModConfigSpec.BooleanValue mapInvertVertical;
        public final ModConfigSpec.BooleanValue mapShowCables;
        public final ModConfigSpec.BooleanValue mapShowParts;
        public final ModConfigSpec.BooleanValue mapShowInactive;
        public final ModConfigSpec.BooleanValue mapShowControls;
        public final ModConfigSpec.BooleanValue mapShowViewButtons;
        public final ModConfigSpec.BooleanValue mapShowDetails;
        public final ModConfigSpec.BooleanValue mapRememberCamera;

        private Values(ModConfigSpec.Builder builder) {
            builder.comment("Production Tablet compact HUD settings").push("hud");
            this.hudEnabled = builder.comment("Show the compact production HUD while a linked tablet is equipped").define("enabled", true);
            this.hudAnchor = builder.comment("Screen corner used to anchor the HUD").defineEnum("anchor", (Enum)HudAnchor.TOP_LEFT);
            this.hudXOffset = builder.comment("Horizontal distance from the selected corner").defineInRange("xOffset", 8, 0, 1000);
            this.hudYOffset = builder.comment("Vertical distance from the selected corner").defineInRange("yOffset", 8, 0, 1000);
            this.hudScale = builder.comment("HUD scale").defineInRange("scale", 1.0, 0.5, 2.0);
            this.hudOpacity = builder.comment("HUD background opacity").defineInRange("opacity", 0.78, 0.0, 1.0);
            this.hudMode = builder.comment("Entries shown in the compact HUD").defineEnum("mode", (Enum)MonitorNetwork.HudMode.ACTIVITY);
            this.hudFrameStyle = builder.comment("Legacy compact HUD frame style kept for backwards compatibility").defineEnum("frameStyle", (Enum)HudFrameStyle.FOREVER);
            this.hudThemeStyle = builder.comment("Independent visual theme used by the compact HUD when theme matching is disabled").defineEnum("theme", (Enum)InterfaceStyle.FOREVER);
            this.hudLayoutStyle = builder.comment("Compact HUD frame geometry").defineEnum("layout", (Enum)HudLayoutStyle.FULL);
            this.hudAnimations = builder.comment("Enable compact HUD theme animations").define("animations", true);
            this.hudAnimationIntensity = builder.comment("Strength of compact HUD theme animations").defineInRange("animationIntensity", 0.35, 0.0, 1.0);
            this.hudCustomHue = builder.comment("Hue used by the custom HUD theme, from 0 to 359 degrees").defineInRange("customHue", 275, 0, 359);
            this.hudEntryCount = builder.comment("Number of rows shown by the compact HUD").defineInRange("entryCount", 5, 1, 10);
            this.hudShowIcons = builder.comment("Show resource icons in compact HUD rows").define("showIcons", true);
            this.hudShowNames = builder.comment("Show resource names in compact HUD rows").define("showNames", true);
            this.hudShowValues = builder.comment("Show amounts or rates in compact HUD rows").define("showValues", true);
            this.rateUnit = builder.comment("Rate display unit used by the dashboard and compact HUD").defineEnum("rateUnit", (Enum)RateUnit.MINUTE);
            this.refreshInterval = builder.comment("How often the monitor samples and refreshes while actively viewed").defineEnum("refreshInterval", (Enum)RefreshInterval.FIVE_SECONDS);
            builder.pop();
            builder.comment("Production Monitor dashboard appearance").push("interface");
            this.interfaceStyle = builder.comment("Visual theme used by the full monitor dashboard").defineEnum("style", (Enum)InterfaceStyle.FOREVER);
            this.interfaceCustomHue = builder.comment("Hue used by the custom dashboard theme, from 0 to 359 degrees").defineInRange("customHue", 275, 0, 359);
            this.interfaceCustomSecondaryHue = builder.comment("Secondary hue used by the custom dashboard theme").defineInRange("customSecondaryHue", 38, 0, 359);
            this.interfaceCustomBrightness = builder.comment("Brightness of custom dashboard surfaces").defineInRange("customBrightness", 0.52, 0.2, 0.9);
            this.interfaceOpacity = builder.comment("Opacity of the full dashboard").defineInRange("opacity", 0.95, 0.55, 1.0);
            this.guiAnimations = builder.comment("Enable subtle client-side GUI animations").define("guiAnimations", true);
            this.updatePulseEnabled = builder.comment("Show a subtle outline pulse when monitor data is refreshed").define("updatePulseEnabled", true);
            this.updatePulseIntensity = builder.comment("Strength of the monitor data refresh pulse").defineInRange("updatePulseIntensity", 0.35, 0.0, 1.0);
            this.updatePulseDuration = builder.comment("Duration of the monitor data refresh pulse in seconds").defineInRange("updatePulseDuration", 1.8, 0.6, 4.0);
            this.ambientMotionEnabled = builder.comment("Show subtle continuous accent motion on the monitor frame").define("ambientMotionEnabled", true);
            this.ambientMotionIntensity = builder.comment("Strength of continuous monitor accent motion").defineInRange("ambientMotionIntensity", 0.3, 0.0, 1.0);
            this.matchHudTheme = builder.comment("Make the compact HUD follow the dashboard theme").define("matchHudTheme", false);
            this.invertMapRotation = builder.comment("Invert horizontal and vertical left-drag rotation in the network map").define("invertMapRotation", false);
            builder.pop();
            builder.comment("General Production Monitor behaviour").push("general");
            this.defaultTab = builder.comment("Tab opened when a monitor is opened").defineEnum("defaultTab", (Enum)DefaultTab.DASHBOARD);
            this.rememberLastTab = builder.comment("Remember the last open tab for this game session").define("rememberLastTab", true);
            builder.pop();
            builder.comment("Interactive network map controls and visibility").push("networkMap");
            this.mapDefaultView = builder.defineEnum("defaultView", (Enum)MapDefaultView.ISO);
            this.mapRotationSensitivity = builder.defineInRange("rotationSensitivity", 0.55, 0.15, 1.5);
            this.mapPanSensitivity = builder.defineInRange("panSensitivity", 1.0, 0.25, 2.0);
            this.mapZoomSensitivity = builder.defineInRange("zoomSensitivity", 1.0, 0.35, 2.0);
            this.mapFocusZoom = builder.defineInRange("focusZoom", 2.5, 1.0, 6.0);
            this.mapInvertHorizontal = builder.define("invertHorizontal", false);
            this.mapInvertVertical = builder.define("invertVertical", false);
            this.mapShowCables = builder.define("showCables", true);
            this.mapShowParts = builder.define("showParts", true);
            this.mapShowInactive = builder.define("showInactive", true);
            this.mapShowControls = builder.define("showControls", true);
            this.mapShowViewButtons = builder.define("showViewButtons", true);
            this.mapShowDetails = builder.define("showDetailsPanel", true);
            this.mapRememberCamera = builder.define("rememberCamera", true);
            builder.pop();
        }
    }

    public static enum HudFrameStyle {
        NONE,
        ORITECH,
        AE2,
        FOREVER,
        STANDARD,
        CUSTOM;


        public HudFrameStyle next() {
            HudFrameStyle[] hudFrameStyleArray = HudFrameStyle.values();
            return hudFrameStyleArray[(this.ordinal() + 1) % hudFrameStyleArray.length];
        }
    }

    public static enum HudLayoutStyle {
        FULL,
        COMPACT,
        MINIMAL,
        NONE;

        public HudLayoutStyle next() {
            HudLayoutStyle[] values = HudLayoutStyle.values();
            return values[(this.ordinal() + 1) % values.length];
        }
    }

    public static enum InterfaceStyle {
        STANDARD,
        FOREVER,
        AE2,
        ORITECH,
        MEKANISM,
        QUANTUM,
        HOLOGRAPHIC,
        MONOCHROME,
        MINIMAL,
        CARBON,
        TERMINAL,
        DEEP_SPACE,
        COPPER,
        AURORA,
        REDSTONE,
        FROST,
        NATURE,
        CUSTOM;


        public InterfaceStyle next() {
            InterfaceStyle[] interfaceStyleArray = InterfaceStyle.values();
            return interfaceStyleArray[(this.ordinal() + 1) % interfaceStyleArray.length];
        }
    }

    public static enum RefreshInterval {
        ONE_SECOND(20),
        TWO_SECONDS(40),
        FIVE_SECONDS(100);

        private final int ticks;

        private RefreshInterval(int n2) {
            this.ticks = n2;
        }

        public int ticks() {
            return this.ticks;
        }

        public RefreshInterval next() {
            RefreshInterval[] refreshIntervalArray = RefreshInterval.values();
            return refreshIntervalArray[(this.ordinal() + 1) % refreshIntervalArray.length];
        }
    }

    public static enum RateUnit {
        SECOND("/s", 0.016666666666666666),
        MINUTE("/m", 1.0),
        HOUR("/h", 60.0);

        private final String suffix;
        private final double fromPerMinute;

        private RateUnit(String string2, double d) {
            this.suffix = string2;
            this.fromPerMinute = d;
        }

        public String suffix() {
            return this.suffix;
        }

        public double scale(long l) {
            return (double)l * this.fromPerMinute;
        }

        public RateUnit next() {
            RateUnit[] rateUnitArray = RateUnit.values();
            return rateUnitArray[(this.ordinal() + 1) % rateUnitArray.length];
        }
    }

    public static enum MapDefaultView {
        ISO,
        TOP,
        FRONT;


        public MapDefaultView next() {
            MapDefaultView[] mapDefaultViewArray = MapDefaultView.values();
            return mapDefaultViewArray[(this.ordinal() + 1) % mapDefaultViewArray.length];
        }
    }

    public static enum DefaultTab {
        DASHBOARD,
        PRODUCTION,
        STORAGE,
        COMPONENTS,
        DEVICES,
        MAP;


        public DefaultTab next() {
            DefaultTab[] defaultTabArray = DefaultTab.values();
            return defaultTabArray[(this.ordinal() + 1) % defaultTabArray.length];
        }
    }

    public static enum HudAnchor {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT;

    }
}
