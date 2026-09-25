/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractSliderButton
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.Renderable
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.neoforged.neoforge.common.ModConfigSpec$DoubleValue
 */
package de.timo.foreverproductionmonitor.client.screen;

import de.timo.foreverproductionmonitor.client.ClientConfig;
import de.timo.foreverproductionmonitor.client.ForeverProductionMonitorClient;
import de.timo.foreverproductionmonitor.client.ProductionHud;
import de.timo.foreverproductionmonitor.client.screen.ForeverButton;
import de.timo.foreverproductionmonitor.client.screen.InterfaceTheme;
import de.timo.foreverproductionmonitor.network.MonitorNetwork;
import java.util.Locale;
import java.util.function.DoubleConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ProductionMonitorThemeScreen
extends Screen {
    private final Screen parent;
    private Category category = Category.INTERFACE;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;
    private int contentTop;

    public ProductionMonitorThemeScreen(Screen screen) {
        super((Component)Component.translatable((String)"screen.forever_production_monitor.settings.title"));
        this.parent = screen;
    }

    protected void init() {
        int n;
        this.panelWidth = Math.min(700, this.width - 20);
        this.panelHeight = Math.min(430, this.height - 16);
        this.left = (this.width - this.panelWidth) / 2;
        this.top = (this.height - this.panelHeight) / 2;
        this.contentTop = this.top + 72;
        int n2 = this.left + 18;
        int n3 = this.panelWidth - 36;
        int n4 = 5;
        int n5 = (n3 - n4 * 3) / 4;
        for (n = 0; n < Category.values().length; ++n) {
            Category category = Category.values()[n];
            this.addRenderableWidget(ForeverButton.create(ProductionMonitorThemeScreen.categoryName(category), button -> {
                this.category = category;
                this.rebuildWidgets();
            }, category == this.category ? ForeverButton.Style.THEMED_ACTIVE : ForeverButton.Style.THEMED, n2 + n * (n5 + n4), this.top + 40, n5, 22));
        }
        switch (this.category) {
            case GENERAL: {
                this.buildGeneral(n2, n3);
                break;
            }
            case INTERFACE: {
                this.buildInterface(n2, n3);
                break;
            }
            case MAP: {
                this.buildMap(n2, n3);
                break;
            }
            case HUD: {
                this.buildHud(n2, n3);
            }
        }
        n = (n3 - 8) / 2;
        this.addRenderableWidget(ForeverButton.create((Component)Component.translatable((String)"config.forever_production_monitor.reset"), button -> {
            this.resetCategory();
            this.rebuildWidgets();
        }, ForeverButton.Style.THEMED, n2, this.top + this.panelHeight - 29, n, 20));
        this.addRenderableWidget(ForeverButton.create((Component)Component.translatable((String)"gui.done"), button -> this.onClose(), ForeverButton.Style.THEMED_ACTIVE, n2 + n + 8, this.top + this.panelHeight - 29, n, 20));
    }

    private void buildGeneral(int n, int n2) {
        int n3 = (n2 - 8) / 2;
        this.addToggle(n, this.contentTop, n3, ProductionMonitorThemeScreen.defaultTabLabel(), () -> ClientConfig.VALUES.defaultTab.set(((ClientConfig.DefaultTab)((Object)((Object)ClientConfig.VALUES.defaultTab.get()))).next()));
        this.addToggle(n + n3 + 8, this.contentTop, n3, ProductionMonitorThemeScreen.booleanLabel("settings.remember_tab", (Boolean)ClientConfig.VALUES.rememberLastTab.get()), () -> ClientConfig.VALUES.rememberLastTab.set(!ClientConfig.VALUES.rememberLastTab.get()));
        this.addToggle(n, this.contentTop + 30, n3, ProductionMonitorThemeScreen.rateLabel(), () -> ClientConfig.VALUES.rateUnit.set(((ClientConfig.RateUnit)((Object)((Object)ClientConfig.VALUES.rateUnit.get()))).next()));
        this.addToggle(n + n3 + 8, this.contentTop + 30, n3, ProductionMonitorThemeScreen.refreshLabel(), () -> ClientConfig.VALUES.refreshInterval.set(((ClientConfig.RefreshInterval)((Object)((Object)ClientConfig.VALUES.refreshInterval.get()))).next()));
    }

    private void buildInterface(int n, int n2) {
        int n3;
        int n4 = 4;
        int n5 = (n2 - n4 * 4) / 5;
        ClientConfig.InterfaceStyle[] interfaceStyleArray = ClientConfig.InterfaceStyle.values();
        for (n3 = 0; n3 < interfaceStyleArray.length; ++n3) {
            ClientConfig.InterfaceStyle style = interfaceStyleArray[n3];
            this.addRenderableWidget(ForeverButton.create(ProductionMonitorThemeScreen.themeName(style), button -> this.selectInterfaceStyle(style, button), style == ClientConfig.VALUES.interfaceStyle.get() ? ForeverButton.Style.THEMED_ACTIVE : ForeverButton.Style.THEMED, n + n3 % 5 * (n5 + n4), this.contentTop + n3 / 5 * 24, n5, 20));
        }
        boolean customActive = ClientConfig.VALUES.interfaceStyle.get() == ClientConfig.InterfaceStyle.CUSTOM;
        SettingSlider primary = new SettingSlider(n, this.contentTop + 52, n2, "settings.custom.primary", 0.0, 359.0, ((Integer)ClientConfig.VALUES.interfaceCustomHue.get()).intValue(), d -> ClientConfig.VALUES.interfaceCustomHue.set(((int)Math.round(d))), false);
        primary.active = customActive;
        this.addRenderableWidget(primary);
        SettingSlider settingSlider = new SettingSlider(n, this.contentTop + 76, n2, "settings.custom.secondary", 0.0, 359.0, ((Integer)ClientConfig.VALUES.interfaceCustomSecondaryHue.get()).intValue(), d -> ClientConfig.VALUES.interfaceCustomSecondaryHue.set(((int)Math.round(d))), false);
        settingSlider.active = customActive;
        this.addRenderableWidget(settingSlider);
        SettingSlider settingSlider2 = new SettingSlider(n, this.contentTop + 100, (n2 - 8) / 2, "settings.custom.brightness", 0.2, 0.9, (Double)ClientConfig.VALUES.interfaceCustomBrightness.get(), arg_0 -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceCustomBrightness).set(arg_0), true);
        settingSlider2.active = customActive;
        this.addRenderableWidget(settingSlider2);
        this.addRenderableWidget(new SettingSlider(n + (n2 - 8) / 2 + 8, this.contentTop + 100, (n2 - 8) / 2, "settings.interface.opacity", 0.55, 1.0, (Double)ClientConfig.VALUES.interfaceOpacity.get(), arg_0 -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceOpacity).set(arg_0), true));
    }

    private void buildMap(int n, int n2) {
        int n3 = (n2 - 8) / 2;
        this.addToggle(n, this.contentTop, n3, ProductionMonitorThemeScreen.translated("settings.map.default_view", ProductionMonitorThemeScreen.translated("settings.map.view." + ProductionMonitorThemeScreen.lower((Enum)ClientConfig.VALUES.mapDefaultView.get()), new Object[0])), () -> ClientConfig.VALUES.mapDefaultView.set(((ClientConfig.MapDefaultView)((Object)((Object)ClientConfig.VALUES.mapDefaultView.get()))).next()));
        this.addToggle(n + n3 + 8, this.contentTop, n3, ProductionMonitorThemeScreen.booleanLabel("settings.map.remember", (Boolean)ClientConfig.VALUES.mapRememberCamera.get()), () -> ClientConfig.VALUES.mapRememberCamera.set(!ClientConfig.VALUES.mapRememberCamera.get()));
        this.addRenderableWidget(new SettingSlider(n, this.contentTop + 28, n3, "settings.map.rotation", 0.15, 1.5, (Double)ClientConfig.VALUES.mapRotationSensitivity.get(), arg_0 -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.mapRotationSensitivity).set(arg_0), true));
        this.addRenderableWidget(new SettingSlider(n + n3 + 8, this.contentTop + 28, n3, "settings.map.pan", 0.25, 2.0, (Double)ClientConfig.VALUES.mapPanSensitivity.get(), arg_0 -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.mapPanSensitivity).set(arg_0), true));
        this.addRenderableWidget(new SettingSlider(n, this.contentTop + 52, n3, "settings.map.zoom", 0.35, 2.0, (Double)ClientConfig.VALUES.mapZoomSensitivity.get(), arg_0 -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.mapZoomSensitivity).set(arg_0), true));
        this.addRenderableWidget(new SettingSlider(n + n3 + 8, this.contentTop + 52, n3, "settings.map.focus_zoom", 1.0, 6.0, (Double)ClientConfig.VALUES.mapFocusZoom.get(), arg_0 -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.mapFocusZoom).set(arg_0), true));
        this.addToggle(n, this.contentTop + 80, n3, ProductionMonitorThemeScreen.booleanLabel("settings.map.invert_horizontal", (Boolean)ClientConfig.VALUES.mapInvertHorizontal.get()), () -> ClientConfig.VALUES.mapInvertHorizontal.set(!ClientConfig.VALUES.mapInvertHorizontal.get()));
        this.addToggle(n + n3 + 8, this.contentTop + 80, n3, ProductionMonitorThemeScreen.booleanLabel("settings.map.invert_vertical", (Boolean)ClientConfig.VALUES.mapInvertVertical.get()), () -> ClientConfig.VALUES.mapInvertVertical.set(!ClientConfig.VALUES.mapInvertVertical.get()));
        this.addToggle(n, this.contentTop + 108, n3, ProductionMonitorThemeScreen.booleanLabel("settings.map.controls", (Boolean)ClientConfig.VALUES.mapShowControls.get()), () -> ClientConfig.VALUES.mapShowControls.set(!ClientConfig.VALUES.mapShowControls.get()));
        this.addToggle(n + n3 + 8, this.contentTop + 108, n3, ProductionMonitorThemeScreen.booleanLabel("settings.map.details", (Boolean)ClientConfig.VALUES.mapShowDetails.get()), () -> ClientConfig.VALUES.mapShowDetails.set(!ClientConfig.VALUES.mapShowDetails.get()));
    }

    private void buildHud(int n, int n2) {
        int n3 = (n2 - 8) / 2;
        this.addToggle(n, this.contentTop, n3, ProductionMonitorThemeScreen.booleanLabel("settings.hud.enabled", (Boolean)ClientConfig.VALUES.hudEnabled.get()), () -> ClientConfig.VALUES.hudEnabled.set(!ClientConfig.VALUES.hudEnabled.get()));
        this.addToggle(n + n3 + 8, this.contentTop, n3, ProductionMonitorThemeScreen.anchorLabel(), () -> {
            ClientConfig.HudAnchor[] hudAnchorArray = ClientConfig.HudAnchor.values();
            ClientConfig.VALUES.hudAnchor.set(hudAnchorArray[(((ClientConfig.HudAnchor)((Object)((Object)ClientConfig.VALUES.hudAnchor.get()))).ordinal() + 1) % hudAnchorArray.length]);
        });
        this.addToggle(n, this.contentTop + 28, n3, ProductionMonitorThemeScreen.modeLabel(), () -> ClientConfig.VALUES.hudMode.set(((MonitorNetwork.HudMode)((Object)((Object)ClientConfig.VALUES.hudMode.get()))).next()));
        this.addToggle(n + n3 + 8, this.contentTop + 28, n3, ProductionMonitorThemeScreen.frameLabel(), () -> ClientConfig.VALUES.hudFrameStyle.set(((ClientConfig.HudFrameStyle)((Object)((Object)ClientConfig.VALUES.hudFrameStyle.get()))).next()));
        this.addToggle(n, this.contentTop + 56, n2, ProductionMonitorThemeScreen.booleanLabel("settings.hud.match", (Boolean)ClientConfig.VALUES.matchHudTheme.get()), () -> ClientConfig.VALUES.matchHudTheme.set(!ClientConfig.VALUES.matchHudTheme.get()));
        int n4 = (n2 - 10) / 3;
        this.addToggle(n, this.contentTop + 84, n4, ProductionMonitorThemeScreen.booleanLabel("settings.hud.icons", (Boolean)ClientConfig.VALUES.hudShowIcons.get()), () -> ClientConfig.VALUES.hudShowIcons.set(!ClientConfig.VALUES.hudShowIcons.get()));
        this.addToggle(n + n4 + 5, this.contentTop + 84, n4, ProductionMonitorThemeScreen.booleanLabel("settings.hud.names", (Boolean)ClientConfig.VALUES.hudShowNames.get()), () -> ClientConfig.VALUES.hudShowNames.set(!ClientConfig.VALUES.hudShowNames.get()));
        this.addToggle(n + (n4 + 5) * 2, this.contentTop + 84, n2 - n4 * 2 - 10, ProductionMonitorThemeScreen.booleanLabel("settings.hud.values", (Boolean)ClientConfig.VALUES.hudShowValues.get()), () -> ClientConfig.VALUES.hudShowValues.set(!ClientConfig.VALUES.hudShowValues.get()));
        this.addRenderableWidget(new SettingSlider(n, this.contentTop + 112, n3, "settings.hud.entries", 1.0, 10.0, ((Integer)ClientConfig.VALUES.hudEntryCount.get()).intValue(), d -> ClientConfig.VALUES.hudEntryCount.set(((int)Math.round(d))), false));
        this.addRenderableWidget(new SettingSlider(n + n3 + 8, this.contentTop + 112, n3, "settings.hud.scale", 0.5, 2.0, (Double)ClientConfig.VALUES.hudScale.get(), arg_0 -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.hudScale).set(arg_0), true));
        this.addRenderableWidget(new SettingSlider(n, this.contentTop + 136, n3, "settings.hud.opacity", 0.0, 1.0, (Double)ClientConfig.VALUES.hudOpacity.get(), arg_0 -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.hudOpacity).set(arg_0), true));
        this.addRenderableWidget(new SettingSlider(n + n3 + 8, this.contentTop + 136, n3, "settings.hud.hue", 0.0, 359.0, ((Integer)ClientConfig.VALUES.hudCustomHue.get()).intValue(), d -> ClientConfig.VALUES.hudCustomHue.set(((int)Math.round(d))), false));
        this.addRenderableWidget(new SettingSlider(n, this.contentTop + 160, n3, "settings.hud.x", 0.0, 200.0, ((Integer)ClientConfig.VALUES.hudXOffset.get()).intValue(), d -> ClientConfig.VALUES.hudXOffset.set(((int)Math.round(d))), false));
        this.addRenderableWidget(new SettingSlider(n + n3 + 8, this.contentTop + 160, n3, "settings.hud.y", 0.0, 200.0, ((Integer)ClientConfig.VALUES.hudYOffset.get()).intValue(), d -> ClientConfig.VALUES.hudYOffset.set(((int)Math.round(d))), false));
    }

    private void addToggle(int n, int n2, int n3, Component component, Runnable runnable) {
        this.addRenderableWidget(ForeverButton.create(component, button -> {
            runnable.run();
            this.rebuildWidgets();
            ForeverProductionMonitorClient.refreshHudNow();
        }, ForeverButton.Style.THEMED, n, n2, n3, 20));
    }

    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        ClientConfig.InterfaceStyle interfaceStyle = (ClientConfig.InterfaceStyle)((Object)ClientConfig.VALUES.interfaceStyle.get());
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        guiGraphics.fill(0, 0, this.width, this.height, palette.backdrop());
        InterfaceTheme.drawPanel(guiGraphics, this.left, this.top, this.panelWidth, this.panelHeight, interfaceStyle, palette);
        guiGraphics.drawCenteredString(this.font, this.title, this.left + this.panelWidth / 2, this.top + 13, palette.text());
        guiGraphics.drawCenteredString(this.font, (Component)Component.literal((String)"Forever Production Monitor 2.2.2"), this.left + this.panelWidth / 2, this.top + 25, palette.muted());
        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, n, n2, f);
        }
        if (this.category == Category.INTERFACE) {
            this.drawInterfacePreview(guiGraphics, palette, interfaceStyle);
        }
        if (this.category == Category.HUD) {
            ProductionHud.renderPreview(guiGraphics);
        }
    }

    private void drawInterfacePreview(GuiGraphics guiGraphics, InterfaceTheme.Palette palette, ClientConfig.InterfaceStyle interfaceStyle) {
        int n = this.left + 18;
        int n2 = this.contentTop + 132;
        int n3 = this.panelWidth - 36;
        int n4 = this.panelHeight - 240;
        InterfaceTheme.drawPanel(guiGraphics, n, n2, n3, n4, interfaceStyle, palette);
        guiGraphics.drawCenteredString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.theme.preview"), n + n3 / 2, n2 + 11, palette.text());
        int n5 = n + 12;
        int n6 = n + n3 - 12;
        int n7 = n2 + 38;
        guiGraphics.fill(n5, n7, n6, n7 + 59, palette.tableOuter());
        guiGraphics.fill(n5 + 1, n7 + 1, n6 - 1, n7 + 15, palette.tableHeader());
        guiGraphics.drawString(this.font, "Item / Fluid / Energy", n5 + 8, n7 + 4, palette.text(), false);
        for (int i = 0; i < 2; ++i) {
            int n8 = n7 + 16 + i * 20;
            guiGraphics.fill(n5 + 1, n8, n6 - 1, n8 + 20, i == 0 ? palette.rowEven() : palette.rowOdd());
            guiGraphics.fill(n5 + 7, n8 + 6, n5 + 15, n8 + 14, i == 0 ? palette.accentA() : palette.accentB());
            guiGraphics.drawString(this.font, i == 0 ? "Iron Ingot" : "Certus Quartz", n5 + 21, n8 + 6, palette.text(), false);
            String string = i == 0 ? "+3.84k/m" : "+660/m";
            guiGraphics.drawString(this.font, string, n6 - 8 - this.font.width(string), n8 + 6, i == 0 ? -9972847 : palette.accentB(), false);
        }
    }

    private void resetCategory() {
        switch (this.category) {
            case GENERAL: {
                ClientConfig.VALUES.defaultTab.set(((ClientConfig.DefaultTab)((Object)ClientConfig.VALUES.defaultTab.getDefault())));
                ClientConfig.VALUES.rememberLastTab.set(((Boolean)ClientConfig.VALUES.rememberLastTab.getDefault()));
                ClientConfig.VALUES.rateUnit.set(((ClientConfig.RateUnit)((Object)ClientConfig.VALUES.rateUnit.getDefault())));
                ClientConfig.VALUES.refreshInterval.set(((ClientConfig.RefreshInterval)((Object)ClientConfig.VALUES.refreshInterval.getDefault())));
                break;
            }
            case INTERFACE: {
                ClientConfig.VALUES.interfaceStyle.set(((ClientConfig.InterfaceStyle)((Object)ClientConfig.VALUES.interfaceStyle.getDefault())));
                ClientConfig.VALUES.interfaceCustomHue.set(((Integer)ClientConfig.VALUES.interfaceCustomHue.getDefault()));
                ClientConfig.VALUES.interfaceCustomSecondaryHue.set(((Integer)ClientConfig.VALUES.interfaceCustomSecondaryHue.getDefault()));
                ClientConfig.VALUES.interfaceCustomBrightness.set(((Double)ClientConfig.VALUES.interfaceCustomBrightness.getDefault()));
                ClientConfig.VALUES.interfaceOpacity.set(((Double)ClientConfig.VALUES.interfaceOpacity.getDefault()));
                break;
            }
            case MAP: {
                ClientConfig.VALUES.mapDefaultView.set(((ClientConfig.MapDefaultView)((Object)ClientConfig.VALUES.mapDefaultView.getDefault())));
                ClientConfig.VALUES.mapRotationSensitivity.set(((Double)ClientConfig.VALUES.mapRotationSensitivity.getDefault()));
                ClientConfig.VALUES.mapPanSensitivity.set(((Double)ClientConfig.VALUES.mapPanSensitivity.getDefault()));
                ClientConfig.VALUES.mapZoomSensitivity.set(((Double)ClientConfig.VALUES.mapZoomSensitivity.getDefault()));
                ClientConfig.VALUES.mapFocusZoom.set(((Double)ClientConfig.VALUES.mapFocusZoom.getDefault()));
                ClientConfig.VALUES.mapInvertHorizontal.set(false);
                ClientConfig.VALUES.mapInvertVertical.set(false);
                ClientConfig.VALUES.mapShowCables.set(true);
                ClientConfig.VALUES.mapShowParts.set(true);
                ClientConfig.VALUES.mapShowInactive.set(true);
                ClientConfig.VALUES.mapShowControls.set(true);
                ClientConfig.VALUES.mapShowViewButtons.set(true);
                ClientConfig.VALUES.mapShowDetails.set(true);
                ClientConfig.VALUES.mapRememberCamera.set(true);
                break;
            }
            case HUD: {
                ClientConfig.VALUES.hudEnabled.set(((Boolean)ClientConfig.VALUES.hudEnabled.getDefault()));
                ClientConfig.VALUES.hudAnchor.set(((ClientConfig.HudAnchor)((Object)ClientConfig.VALUES.hudAnchor.getDefault())));
                ClientConfig.VALUES.hudMode.set(((MonitorNetwork.HudMode)((Object)ClientConfig.VALUES.hudMode.getDefault())));
                ClientConfig.VALUES.hudFrameStyle.set(((ClientConfig.HudFrameStyle)((Object)ClientConfig.VALUES.hudFrameStyle.getDefault())));
                ClientConfig.VALUES.hudEntryCount.set(((Integer)ClientConfig.VALUES.hudEntryCount.getDefault()));
                ClientConfig.VALUES.hudShowIcons.set(((Boolean)ClientConfig.VALUES.hudShowIcons.getDefault()));
                ClientConfig.VALUES.hudShowNames.set(((Boolean)ClientConfig.VALUES.hudShowNames.getDefault()));
                ClientConfig.VALUES.hudShowValues.set(((Boolean)ClientConfig.VALUES.hudShowValues.getDefault()));
                ClientConfig.VALUES.hudScale.set(((Double)ClientConfig.VALUES.hudScale.getDefault()));
                ClientConfig.VALUES.hudOpacity.set(((Double)ClientConfig.VALUES.hudOpacity.getDefault()));
                ClientConfig.VALUES.hudXOffset.set(((Integer)ClientConfig.VALUES.hudXOffset.getDefault()));
                ClientConfig.VALUES.hudYOffset.set(((Integer)ClientConfig.VALUES.hudYOffset.getDefault()));
                ClientConfig.VALUES.matchHudTheme.set(((Boolean)ClientConfig.VALUES.matchHudTheme.getDefault()));
            }
        }
        ForeverProductionMonitorClient.refreshHudNow();
    }

    public void onClose() {
        ProductionMonitorThemeScreen.saveAll();
        ForeverProductionMonitorClient.refreshHudNow();
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private static void saveAll() {
        ClientConfig.VALUES.hudEnabled.save();
        ClientConfig.VALUES.hudAnchor.save();
        ClientConfig.VALUES.hudXOffset.save();
        ClientConfig.VALUES.hudYOffset.save();
        ClientConfig.VALUES.hudScale.save();
        ClientConfig.VALUES.hudOpacity.save();
        ClientConfig.VALUES.hudMode.save();
        ClientConfig.VALUES.hudFrameStyle.save();
        ClientConfig.VALUES.hudCustomHue.save();
        ClientConfig.VALUES.hudEntryCount.save();
        ClientConfig.VALUES.hudShowIcons.save();
        ClientConfig.VALUES.hudShowNames.save();
        ClientConfig.VALUES.hudShowValues.save();
        ClientConfig.VALUES.rateUnit.save();
        ClientConfig.VALUES.refreshInterval.save();
        ClientConfig.VALUES.interfaceStyle.save();
        ClientConfig.VALUES.interfaceCustomHue.save();
        ClientConfig.VALUES.interfaceCustomSecondaryHue.save();
        ClientConfig.VALUES.interfaceCustomBrightness.save();
        ClientConfig.VALUES.interfaceOpacity.save();
        ClientConfig.VALUES.matchHudTheme.save();
        ClientConfig.VALUES.defaultTab.save();
        ClientConfig.VALUES.rememberLastTab.save();
        ClientConfig.VALUES.mapDefaultView.save();
        ClientConfig.VALUES.mapRotationSensitivity.save();
        ClientConfig.VALUES.mapPanSensitivity.save();
        ClientConfig.VALUES.mapZoomSensitivity.save();
        ClientConfig.VALUES.mapFocusZoom.save();
        ClientConfig.VALUES.mapInvertHorizontal.save();
        ClientConfig.VALUES.mapInvertVertical.save();
        ClientConfig.VALUES.mapShowCables.save();
        ClientConfig.VALUES.mapShowParts.save();
        ClientConfig.VALUES.mapShowInactive.save();
        ClientConfig.VALUES.mapShowControls.save();
        ClientConfig.VALUES.mapShowViewButtons.save();
        ClientConfig.VALUES.mapShowDetails.save();
        ClientConfig.VALUES.mapRememberCamera.save();
    }

    public boolean isPauseScreen() {
        return false;
    }

    private static Component categoryName(Category category) {
        return ProductionMonitorThemeScreen.translated("settings.category." + ProductionMonitorThemeScreen.lower(category), new Object[0]);
    }

    private static Component themeName(ClientConfig.InterfaceStyle interfaceStyle) {
        return Component.translatable((String)("screen.forever_production_monitor.theme." + ProductionMonitorThemeScreen.lower(interfaceStyle)));
    }

    private static String lower(Enum<?> enum_) {
        return enum_.name().toLowerCase(Locale.ROOT);
    }

    private static Component translated(String string, Object ... objectArray) {
        return Component.translatable((String)("screen.forever_production_monitor." + string), (Object[])objectArray);
    }

    private static Component booleanLabel(String string, boolean bl) {
        return ProductionMonitorThemeScreen.translated(string, Component.translatable((String)(bl ? "options.on" : "options.off")));
    }

    private static Component defaultTabLabel() {
        return ProductionMonitorThemeScreen.translated("settings.default_tab", Component.translatable((String)("screen.forever_production_monitor.tab." + ProductionMonitorThemeScreen.lower((Enum)ClientConfig.VALUES.defaultTab.get()))));
    }

    private static Component rateLabel() {
        return ProductionMonitorThemeScreen.translated("settings.rate", ((ClientConfig.RateUnit)((Object)ClientConfig.VALUES.rateUnit.get())).suffix());
    }

    private static Component refreshLabel() {
        return ProductionMonitorThemeScreen.translated("settings.refresh", Component.translatable((String)("config.forever_production_monitor.refresh." + ProductionMonitorThemeScreen.lower((Enum)ClientConfig.VALUES.refreshInterval.get()))));
    }

    private static Component anchorLabel() {
        return ProductionMonitorThemeScreen.translated("settings.hud.anchor", Component.translatable((String)("config.forever_production_monitor.anchor." + ProductionMonitorThemeScreen.lower((Enum)ClientConfig.VALUES.hudAnchor.get()))));
    }

    private static Component modeLabel() {
        return ProductionMonitorThemeScreen.translated("settings.hud.mode", Component.translatable((String)("hud.forever_production_monitor.mode." + ProductionMonitorThemeScreen.lower((Enum)ClientConfig.VALUES.hudMode.get()))));
    }

    private static Component frameLabel() {
        return ProductionMonitorThemeScreen.translated("settings.hud.frame", Component.translatable((String)("config.forever_production_monitor.frame." + ProductionMonitorThemeScreen.lower((Enum)ClientConfig.VALUES.hudFrameStyle.get()))));
    }

    private /* synthetic */ void selectInterfaceStyle(ClientConfig.InterfaceStyle interfaceStyle, Button button) {
        ClientConfig.VALUES.interfaceStyle.set(interfaceStyle);
        this.rebuildWidgets();
    }

    private static enum Category {
        GENERAL,
        INTERFACE,
        MAP,
        HUD;

    }

    private static final class SettingSlider
    extends AbstractSliderButton {
        private final String key;
        private final double min;
        private final double max;
        private final DoubleConsumer setter;
        private final boolean decimal;

        SettingSlider(int n, int n2, int n3, String string, double d, double d2, double d3, DoubleConsumer doubleConsumer, boolean bl) {
            super(n, n2, n3, 20, (Component)Component.empty(), (d3 - d) / (d2 - d));
            this.key = string;
            this.min = d;
            this.max = d2;
            this.setter = doubleConsumer;
            this.decimal = bl;
            this.updateMessage();
        }

        private double actual() {
            return this.min + this.value * (this.max - this.min);
        }

        protected void updateMessage() {
            String string = this.decimal ? String.format(Locale.ROOT, "%.2f", this.actual()) : Integer.toString((int)Math.round(this.actual()));
            this.setMessage(ProductionMonitorThemeScreen.translated(this.key, string));
        }

        protected void applyValue() {
            this.setter.accept(this.actual());
            ForeverProductionMonitorClient.refreshHudNow();
        }
    }
}
