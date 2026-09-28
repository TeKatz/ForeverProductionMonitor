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
            case InterfaceStyle.QUANTUM, InterfaceStyle.AURORA, InterfaceStyle.NATURE, InterfaceStyle.CAT -> HudFrameStyle.FOREVER;
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
        // Matching determines which theme family the HUD uses. CUSTOM deliberately
        // keeps its own palette so the compact HUD can be tuned independently.
        return (Integer)ClientConfig.VALUES.hudCustomHue.get();
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
        public final ModConfigSpec.IntValue hudCustomSecondaryHue;
        public final ModConfigSpec.DoubleValue hudCustomPrimarySaturation;
        public final ModConfigSpec.DoubleValue hudCustomSecondarySaturation;
        public final ModConfigSpec.DoubleValue hudCustomSurfaceSaturation;
        public final ModConfigSpec.DoubleValue hudCustomBrightness;
        public final ModConfigSpec.DoubleValue hudCustomContrast;
        public final ModConfigSpec.DoubleValue hudCustomHeaderStrength;
        public final ModConfigSpec.DoubleValue hudCustomRowContrast;
        public final ModConfigSpec.DoubleValue hudCustomBorderStrength;
        public final ModConfigSpec.DoubleValue hudCustomAccentBrightness;
        public final ModConfigSpec.EnumValue<CustomHudDecorationStyle> hudCustomDecorationStyle;
        public final ModConfigSpec.IntValue hudEntryCount;
        public final ModConfigSpec.BooleanValue hudShowIcons;
        public final ModConfigSpec.BooleanValue hudShowNames;
        public final ModConfigSpec.BooleanValue hudShowValues;
        public final ModConfigSpec.BooleanValue hudIncludeItems;
        public final ModConfigSpec.BooleanValue hudIncludeFluids;
        public final ModConfigSpec.BooleanValue hudIncludeEnergy;
        public final ModConfigSpec.BooleanValue hudIncludeInfinite;
        public final ModConfigSpec.EnumValue<HudStoredSort> hudStoredSort;
        public final ModConfigSpec.EnumValue<RateUnit> rateUnit;
        public final ModConfigSpec.EnumValue<RefreshInterval> refreshInterval;
        public final ModConfigSpec.EnumValue<InterfaceStyle> interfaceStyle;
        public final ModConfigSpec.IntValue interfaceCustomHue;
        public final ModConfigSpec.IntValue interfaceCustomSecondaryHue;
        public final ModConfigSpec.DoubleValue interfaceCustomBrightness;
        public final ModConfigSpec.DoubleValue interfaceCustomPrimarySaturation;
        public final ModConfigSpec.DoubleValue interfaceCustomSecondarySaturation;
        public final ModConfigSpec.DoubleValue interfaceCustomSurfaceSaturation;
        public final ModConfigSpec.DoubleValue interfaceCustomContrast;
        public final ModConfigSpec.DoubleValue interfaceCustomHeaderStrength;
        public final ModConfigSpec.DoubleValue interfaceCustomBorderStrength;
        public final ModConfigSpec.DoubleValue interfaceCustomAccentBrightness;
        public final ModConfigSpec.EnumValue<CustomBackgroundStyle> interfaceCustomBackgroundStyle;
        public final ModConfigSpec.DoubleValue interfaceCustomBackgroundDensity;
        public final ModConfigSpec.DoubleValue interfaceCustomBackgroundSpeed;
        public final ModConfigSpec.DoubleValue interfaceCustomBackgroundOpacity;
        public final ModConfigSpec.DoubleValue interfaceCustomBackgroundScale;
        public final ModConfigSpec.EnumValue<CustomParticleStyle> interfaceCustomParticleStyle;
        public final ModConfigSpec.EnumValue<CustomParticleColorMode> interfaceCustomParticleColorMode;
        public final ModConfigSpec.DoubleValue interfaceCustomParticleAmount;
        public final ModConfigSpec.DoubleValue interfaceCustomParticleSpeed;
        public final ModConfigSpec.DoubleValue interfaceCustomParticleSize;
        public final ModConfigSpec.DoubleValue interfaceCustomParticleOpacity;
        public final ModConfigSpec.EnumValue<CustomTabStyle> interfaceCustomTabStyle;
        public final ModConfigSpec.EnumValue<CustomButtonStyle> interfaceCustomButtonStyle;
        public final ModConfigSpec.DoubleValue interfaceCustomTabAccentStrength;
        public final ModConfigSpec.DoubleValue interfaceCustomButtonHoverStrength;
        public final ModConfigSpec.DoubleValue interfaceOpacity;
        public final ModConfigSpec.BooleanValue guiAnimations;
        public final ModConfigSpec.BooleanValue updatePulseEnabled;
        public final ModConfigSpec.DoubleValue updatePulseIntensity;
        public final ModConfigSpec.DoubleValue updatePulseDuration;
        public final ModConfigSpec.BooleanValue ambientMotionEnabled;
        public final ModConfigSpec.DoubleValue ambientMotionIntensity;
        public final ModConfigSpec.BooleanValue themeInteractionsEnabled;
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
        public final ModConfigSpec.ConfigValue<String> mapCameraBookmarks;

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
            this.hudCustomHue = builder.comment("Primary hue used by the custom HUD theme, from 0 to 359 degrees").defineInRange("customHue", 275, 0, 359);
            this.hudCustomSecondaryHue = builder.comment("Secondary hue used by the custom HUD theme").defineInRange("customSecondaryHue", 315, 0, 359);
            this.hudCustomPrimarySaturation = builder.comment("Saturation of the primary custom HUD accent").defineInRange("customPrimarySaturation", 0.62, 0.0, 1.0);
            this.hudCustomSecondarySaturation = builder.comment("Saturation of the secondary custom HUD accent").defineInRange("customSecondarySaturation", 0.48, 0.0, 1.0);
            this.hudCustomSurfaceSaturation = builder.comment("Amount of primary hue mixed into custom HUD surfaces").defineInRange("customSurfaceSaturation", 0.16, 0.0, 0.75);
            this.hudCustomBrightness = builder.comment("Brightness of custom HUD surfaces").defineInRange("customBrightness", 0.36, 0.15, 0.85);
            this.hudCustomContrast = builder.comment("Contrast between custom HUD surface layers").defineInRange("customContrast", 1.0, 0.65, 1.45);
            this.hudCustomHeaderStrength = builder.comment("Brightness strength of the custom HUD header").defineInRange("customHeaderStrength", 1.0, 0.65, 1.45);
            this.hudCustomRowContrast = builder.comment("Contrast between alternating custom HUD rows").defineInRange("customRowContrast", 1.0, 0.65, 1.45);
            this.hudCustomBorderStrength = builder.comment("Brightness strength of custom HUD borders").defineInRange("customBorderStrength", 1.0, 0.5, 1.5);
            this.hudCustomAccentBrightness = builder.comment("Brightness multiplier for custom HUD accents").defineInRange("customAccentBrightness", 1.0, 0.65, 1.25);
            this.hudCustomDecorationStyle = builder.comment("Decorative language used by the custom HUD").defineEnum("customDecorationStyle", (Enum)CustomHudDecorationStyle.CLEAN);
            this.hudEntryCount = builder.comment("Number of rows shown by the compact HUD").defineInRange("entryCount", 5, 1, 10);
            this.hudShowIcons = builder.comment("Show resource icons in compact HUD rows").define("showIcons", true);
            this.hudShowNames = builder.comment("Show resource names in compact HUD rows").define("showNames", true);
            this.hudShowValues = builder.comment("Show amounts or rates in compact HUD rows").define("showValues", true);
            this.hudIncludeItems = builder.comment("Allow item entries in the compact HUD").define("includeItems", true);
            this.hudIncludeFluids = builder.comment("Allow fluid entries in the compact HUD").define("includeFluids", true);
            this.hudIncludeEnergy = builder.comment("Allow FE energy entries in the compact HUD").define("includeEnergy", true);
            this.hudIncludeInfinite = builder.comment("Allow infinite/creative storage entries in the compact HUD").define("includeInfinite", true);
            this.hudStoredSort = builder.comment("Sort direction used by the compact HUD Stored mode").defineEnum("storedSort", (Enum)HudStoredSort.HIGHEST);
            this.rateUnit = builder.comment("Rate display unit used by the dashboard and compact HUD").defineEnum("rateUnit", (Enum)RateUnit.MINUTE);
            this.refreshInterval = builder.comment("How often the monitor samples and refreshes while actively viewed").defineEnum("refreshInterval", (Enum)RefreshInterval.FIVE_SECONDS);
            builder.pop();
            builder.comment("Production Monitor dashboard appearance").push("interface");
            this.interfaceStyle = builder.comment("Visual theme used by the full monitor dashboard").defineEnum("style", (Enum)InterfaceStyle.FOREVER);
            this.interfaceCustomHue = builder.comment("Hue used by the custom dashboard theme, from 0 to 359 degrees").defineInRange("customHue", 275, 0, 359);
            this.interfaceCustomSecondaryHue = builder.comment("Secondary hue used by the custom dashboard theme").defineInRange("customSecondaryHue", 38, 0, 359);
            this.interfaceCustomBrightness = builder.comment("Brightness of custom dashboard surfaces").defineInRange("customBrightness", 0.52, 0.2, 0.9);
            this.interfaceCustomPrimarySaturation = builder.comment("Saturation of the primary custom accent").defineInRange("customPrimarySaturation", 0.62, 0.0, 1.0);
            this.interfaceCustomSecondarySaturation = builder.comment("Saturation of the secondary custom accent").defineInRange("customSecondarySaturation", 0.52, 0.0, 1.0);
            this.interfaceCustomSurfaceSaturation = builder.comment("Amount of primary hue mixed into custom theme surfaces").defineInRange("customSurfaceSaturation", 0.25, 0.0, 0.75);
            this.interfaceCustomContrast = builder.comment("Contrast between custom theme surfaces").defineInRange("customContrast", 1.0, 0.65, 1.45);
            this.interfaceCustomHeaderStrength = builder.comment("Brightness strength of custom headers").defineInRange("customHeaderStrength", 1.0, 0.65, 1.45);
            this.interfaceCustomBorderStrength = builder.comment("Brightness strength of custom borders").defineInRange("customBorderStrength", 1.0, 0.5, 1.5);
            this.interfaceCustomAccentBrightness = builder.comment("Brightness multiplier for custom accent colours").defineInRange("customAccentBrightness", 1.0, 0.65, 1.25);
            this.interfaceCustomBackgroundStyle = builder.comment("Background decoration used by the custom theme").defineEnum("customBackgroundStyle", (Enum)CustomBackgroundStyle.GRID);
            this.interfaceCustomBackgroundDensity = builder.comment("Density of the selected custom background").defineInRange("customBackgroundDensity", 0.45, 0.1, 1.0);
            this.interfaceCustomBackgroundSpeed = builder.comment("Motion speed of the selected custom background").defineInRange("customBackgroundSpeed", 0.35, 0.0, 1.0);
            this.interfaceCustomBackgroundOpacity = builder.comment("Opacity of custom background decoration").defineInRange("customBackgroundOpacity", 0.28, 0.0, 1.0);
            this.interfaceCustomBackgroundScale = builder.comment("Scale/spacing of the custom background pattern").defineInRange("customBackgroundScale", 1.0, 0.5, 2.0);
            this.interfaceCustomParticleStyle = builder.comment("Particle layer used by the custom theme").defineEnum("customParticleStyle", (Enum)CustomParticleStyle.NONE);
            this.interfaceCustomParticleColorMode = builder.comment("Colour source for custom theme particles").defineEnum("customParticleColorMode", (Enum)CustomParticleColorMode.MIXED);
            this.interfaceCustomParticleAmount = builder.comment("Amount of custom theme particles").defineInRange("customParticleAmount", 0.35, 0.0, 1.0);
            this.interfaceCustomParticleSpeed = builder.comment("Speed of custom theme particles").defineInRange("customParticleSpeed", 0.35, 0.0, 1.0);
            this.interfaceCustomParticleSize = builder.comment("Size of custom theme particles").defineInRange("customParticleSize", 1.0, 0.5, 2.0);
            this.interfaceCustomParticleOpacity = builder.comment("Opacity of custom theme particles").defineInRange("customParticleOpacity", 0.45, 0.0, 1.0);
            this.interfaceCustomTabStyle = builder.comment("Visual shape used by custom-theme navigation tabs").defineEnum("customTabStyle", (Enum)CustomTabStyle.FRAMED);
            this.interfaceCustomButtonStyle = builder.comment("Visual shape used by custom-theme action buttons").defineEnum("customButtonStyle", (Enum)CustomButtonStyle.FILLED);
            this.interfaceCustomTabAccentStrength = builder.comment("Strength of the active-tab accent").defineInRange("customTabAccentStrength", 0.85, 0.0, 1.0);
            this.interfaceCustomButtonHoverStrength = builder.comment("Strength of custom button hover effects").defineInRange("customButtonHoverStrength", 0.65, 0.0, 1.0);
            this.interfaceOpacity = builder.comment("Opacity of the full dashboard").defineInRange("opacity", 0.95, 0.55, 1.0);
            this.guiAnimations = builder.comment("Enable subtle client-side GUI animations").define("guiAnimations", true);
            this.updatePulseEnabled = builder.comment("Show a subtle outline pulse when monitor data is refreshed").define("updatePulseEnabled", true);
            this.updatePulseIntensity = builder.comment("Strength of the monitor data refresh pulse").defineInRange("updatePulseIntensity", 0.35, 0.0, 1.0);
            this.updatePulseDuration = builder.comment("Duration of the monitor data refresh pulse in seconds").defineInRange("updatePulseDuration", 1.8, 0.6, 4.0);
            this.ambientMotionEnabled = builder.comment("Show subtle continuous accent motion on the monitor frame").define("ambientMotionEnabled", true);
            this.ambientMotionIntensity = builder.comment("Strength of continuous monitor accent motion").defineInRange("ambientMotionIntensity", 0.3, 0.0, 1.0);
            this.themeInteractionsEnabled = builder.comment("Allow decorative themes to react to mouse movement and clicks").define("themeInteractionsEnabled", true);
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
            this.mapCameraBookmarks = builder.comment("Persistent camera bookmark data for network map slots 1-3")
                    .define("cameraBookmarks", "");
            builder.pop();
        }
    }

    public static enum HudStoredSort {
        HIGHEST,
        LOWEST;

        public HudStoredSort next() {
            return this == HIGHEST ? LOWEST : HIGHEST;
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

    public static enum CustomHudDecorationStyle {
        CLEAN,
        SPLIT,
        CORNERS,
        CIRCUIT;
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
        CAT,
        CUSTOM;


        public InterfaceStyle next() {
            InterfaceStyle[] styles = selectableValues();
            for (int i = 0; i < styles.length; ++i) {
                if (styles[i] == this) {
                    return styles[(i + 1) % styles.length];
                }
            }
            return STANDARD;
        }

        public static InterfaceStyle[] selectableValues() {
            // AURORA remains serialized for backwards-compatible configs, but is
            // intentionally retired from all user-facing theme selection.
            return new InterfaceStyle[]{
                    STANDARD, FOREVER, AE2, ORITECH, MEKANISM, QUANTUM,
                    HOLOGRAPHIC, MONOCHROME, MINIMAL, CARBON, TERMINAL,
                    DEEP_SPACE, COPPER, REDSTONE, FROST, NATURE, CAT, CUSTOM
            };
        }
    }

    public static enum CustomBackgroundStyle {
        NONE,
        GRID,
        CIRCUIT,
        STARFIELD,
        SCAN_LINES,
        ENERGY_WAVES;
    }

    public static enum CustomParticleStyle {
        NONE,
        PIXELS,
        SPARKS,
        DATA_PACKETS,
        STARS;
    }

    public static enum CustomParticleColorMode {
        PRIMARY,
        SECONDARY,
        MIXED,
        WHITE;
    }

    public static enum CustomTabStyle {
        FLAT,
        FILLED,
        UNDERLINE,
        FRAMED,
        SEGMENTED;
    }

    public static enum CustomButtonStyle {
        FLAT,
        FILLED,
        OUTLINE,
        UNDERLINE,
        SEGMENTED;
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
