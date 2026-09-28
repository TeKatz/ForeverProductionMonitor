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

import de.timo.foreverproductionmonitor.ForeverProductionMonitor;
import de.timo.foreverproductionmonitor.client.ClientConfig;
import de.timo.foreverproductionmonitor.client.ForeverProductionMonitorClient;
import de.timo.foreverproductionmonitor.client.ProductionHud;
import de.timo.foreverproductionmonitor.client.screen.ForeverButton;
import de.timo.foreverproductionmonitor.client.screen.InterfaceTheme;
import de.timo.foreverproductionmonitor.network.MonitorNetwork;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.DoubleConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.narration.NarrationElementOutput;
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
    private int contentLeft;
    private int contentWidth;
    private int contentBottom;
    private int footerY;
    private int previewTop;
    private int themeDropdownScroll;
    private ThemeDropdown themeDropdown;
    private final List<AbstractWidget> hudControlWidgets = new ArrayList<>();
    private HudSettingsSection hudSettingsSection = HudSettingsSection.GENERAL;
    private HudPreviewZoom hudPreviewZoom = HudPreviewZoom.FIT;
    private int hudSettingsLeft;
    private int hudSettingsTop;
    private int hudSettingsWidth;
    private int hudSettingsUsableWidth;
    private int hudSettingsBottom;
    private int hudSettingsColumns;
    private int hudSettingsScroll;
    private int hudSettingsMaxScroll;
    private int hudPreviewLeft;
    private int hudPreviewTop;
    private int hudPreviewRight;
    private int hudPreviewBottom;
    private boolean draggingHudScrollbar;

    public ProductionMonitorThemeScreen(Screen screen) {
        super((Component)Component.translatable((String)"screen.forever_production_monitor.settings.title"));
        this.parent = screen;
    }

    protected void init() {
        int n;
        this.themeDropdown = null;
        this.hudControlWidgets.clear();
        this.draggingHudScrollbar = false;
        this.panelWidth = Math.max(1, Math.min(760, this.width - 12));
        this.panelHeight = Math.max(1, Math.min(520, this.height - 12));
        this.left = (this.width - this.panelWidth) / 2;
        this.top = (this.height - this.panelHeight) / 2;
        this.contentTop = this.top + 72;
        this.contentLeft = this.left + 18;
        this.contentWidth = Math.max(1, this.panelWidth - 36);
        this.footerY = this.top + this.panelHeight - 29;
        this.contentBottom = this.footerY - 18;
        this.previewTop = this.contentTop;
        int n2 = this.contentLeft;
        int n3 = this.contentWidth;
        int n4 = 5;
        int categoryCount = Category.values().length;
        int n5 = Math.max(1, (n3 - n4 * (categoryCount - 1)) / categoryCount);
        for (n = 0; n < Category.values().length; ++n) {
            Category category = Category.values()[n];
            int tabX = n2 + n * (n5 + n4);
            int tabWidth = n == categoryCount - 1 ? Math.max(1, n2 + n3 - tabX) : n5;
            this.addRenderableWidget(ForeverButton.create(ProductionMonitorThemeScreen.categoryName(category), button -> {
                this.category = category;
                this.rebuildWidgets();
            }, category == this.category ? ForeverButton.Style.THEMED_ACTIVE : ForeverButton.Style.THEMED, tabX, this.top + 40, tabWidth, 22).setRole(ForeverButton.Role.TAB));
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
                break;
            }
            case ANIMATIONS: {
                this.buildAnimations(n2, n3);
            }
        }
        n = Math.max(1, (n3 - 8) / 2);
        this.addRenderableWidget(ForeverButton.create((Component)Component.translatable((String)"config.forever_production_monitor.reset"), button -> {
            this.resetCategory();
            this.rebuildWidgets();
        }, ForeverButton.Style.THEMED, n2, this.footerY, n, 20));
        this.addRenderableWidget(ForeverButton.create((Component)Component.translatable((String)"gui.done"), button -> this.onClose(), ForeverButton.Style.THEMED_ACTIVE, n2 + n + 8, this.footerY, Math.max(1, n3 - n - 8), 20));
    }

    private void buildGeneral(int n, int n2) {
        int columns = this.gridColumns(2, 4, this.contentTop);
        this.addToggle(this.gridX(0, columns), this.gridY(0, columns), this.gridWidth(0, columns), ProductionMonitorThemeScreen.defaultTabLabel(), () -> ClientConfig.VALUES.defaultTab.set(((ClientConfig.DefaultTab)((Object)((Object)ClientConfig.VALUES.defaultTab.get()))).next()));
        this.addToggle(this.gridX(1, columns), this.gridY(1, columns), this.gridWidth(1, columns), ProductionMonitorThemeScreen.booleanLabel("settings.remember_tab", (Boolean)ClientConfig.VALUES.rememberLastTab.get()), () -> ClientConfig.VALUES.rememberLastTab.set(!ClientConfig.VALUES.rememberLastTab.get()));
        this.addToggle(this.gridX(2, columns), this.gridY(2, columns), this.gridWidth(2, columns), ProductionMonitorThemeScreen.rateLabel(), () -> ClientConfig.VALUES.rateUnit.set(((ClientConfig.RateUnit)((Object)((Object)ClientConfig.VALUES.rateUnit.get()))).next()));
        this.addToggle(this.gridX(3, columns), this.gridY(3, columns), this.gridWidth(3, columns), ProductionMonitorThemeScreen.refreshLabel(), () -> ClientConfig.VALUES.refreshInterval.set(((ClientConfig.RefreshInterval)((Object)((Object)ClientConfig.VALUES.refreshInterval.get()))).next()));
    }

    private void buildInterface(int n, int n2) {
        int dropdownWidth = Math.min(n2, Math.max(Math.min(220, n2), Math.min(300, n2 / 2)));
        this.themeDropdown = new ThemeDropdown(n, this.contentTop, dropdownWidth, 20,
                style -> this.selectInterfaceStyle(style),
                () -> (ClientConfig.InterfaceStyle)ClientConfig.VALUES.interfaceStyle.get(),
                ProductionMonitorThemeScreen::themeName);
        this.addWidget(this.themeDropdown);

        boolean customActive = ClientConfig.VALUES.interfaceStyle.get() == ClientConfig.InterfaceStyle.CUSTOM;
        int itemCount = customActive ? 2 : 1;
        int startY = this.contentTop + 28;
        int columns = this.gridColumns(2, itemCount, startY);
        this.addRenderableWidget(new SettingSlider(
                this.gridX(0, columns), this.gridY(0, columns, startY), this.gridWidth(0, columns),
                "settings.interface.opacity", 0.55, 1.0,
                (Double)ClientConfig.VALUES.interfaceOpacity.get(),
                arg_0 -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceOpacity).set(arg_0), true));

        if (customActive) {
            this.addRenderableWidget(ForeverButton.create(
                    ProductionMonitorThemeScreen.translated("settings.custom.open_designer"),
                    button -> {
                        if (this.minecraft != null) {
                            this.minecraft.setScreen(new CustomThemeScreen(this));
                        }
                    },
                    ForeverButton.Style.THEMED_ACTIVE,
                    this.gridX(1, columns), this.gridY(1, columns, startY), this.gridWidth(1, columns), 20));
        }
        this.previewTop = this.gridY(itemCount - 1, columns, startY) + 28;
    }

    private void buildMap(int n, int n2) {
        int columns = this.gridColumns(4, 10, this.contentTop);
        this.addToggle(this.gridX(0, columns), this.gridY(0, columns), this.gridWidth(0, columns), ProductionMonitorThemeScreen.translated("settings.map.default_view", ProductionMonitorThemeScreen.translated("settings.map.view." + ProductionMonitorThemeScreen.lower((Enum)ClientConfig.VALUES.mapDefaultView.get()), new Object[0])), () -> ClientConfig.VALUES.mapDefaultView.set(((ClientConfig.MapDefaultView)((Object)((Object)ClientConfig.VALUES.mapDefaultView.get()))).next()));
        this.addToggle(this.gridX(1, columns), this.gridY(1, columns), this.gridWidth(1, columns), ProductionMonitorThemeScreen.booleanLabel("settings.map.remember", (Boolean)ClientConfig.VALUES.mapRememberCamera.get()), () -> ClientConfig.VALUES.mapRememberCamera.set(!ClientConfig.VALUES.mapRememberCamera.get()));
        this.addRenderableWidget(new SettingSlider(this.gridX(2, columns), this.gridY(2, columns), this.gridWidth(2, columns), "settings.map.rotation", 0.15, 1.5, (Double)ClientConfig.VALUES.mapRotationSensitivity.get(), arg_0 -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.mapRotationSensitivity).set(arg_0), true));
        this.addRenderableWidget(new SettingSlider(this.gridX(3, columns), this.gridY(3, columns), this.gridWidth(3, columns), "settings.map.pan", 0.25, 2.0, (Double)ClientConfig.VALUES.mapPanSensitivity.get(), arg_0 -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.mapPanSensitivity).set(arg_0), true));
        this.addRenderableWidget(new SettingSlider(this.gridX(4, columns), this.gridY(4, columns), this.gridWidth(4, columns), "settings.map.zoom", 0.35, 2.0, (Double)ClientConfig.VALUES.mapZoomSensitivity.get(), arg_0 -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.mapZoomSensitivity).set(arg_0), true));
        this.addRenderableWidget(new SettingSlider(this.gridX(5, columns), this.gridY(5, columns), this.gridWidth(5, columns), "settings.map.focus_zoom", 1.0, 6.0, (Double)ClientConfig.VALUES.mapFocusZoom.get(), arg_0 -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.mapFocusZoom).set(arg_0), true));
        this.addToggle(this.gridX(6, columns), this.gridY(6, columns), this.gridWidth(6, columns), ProductionMonitorThemeScreen.booleanLabel("settings.map.invert_horizontal", (Boolean)ClientConfig.VALUES.mapInvertHorizontal.get()), () -> ClientConfig.VALUES.mapInvertHorizontal.set(!ClientConfig.VALUES.mapInvertHorizontal.get()));
        this.addToggle(this.gridX(7, columns), this.gridY(7, columns), this.gridWidth(7, columns), ProductionMonitorThemeScreen.booleanLabel("settings.map.invert_vertical", (Boolean)ClientConfig.VALUES.mapInvertVertical.get()), () -> ClientConfig.VALUES.mapInvertVertical.set(!ClientConfig.VALUES.mapInvertVertical.get()));
        this.addToggle(this.gridX(8, columns), this.gridY(8, columns), this.gridWidth(8, columns), ProductionMonitorThemeScreen.booleanLabel("settings.map.controls", (Boolean)ClientConfig.VALUES.mapShowControls.get()), () -> ClientConfig.VALUES.mapShowControls.set(!ClientConfig.VALUES.mapShowControls.get()));
        this.addToggle(this.gridX(9, columns), this.gridY(9, columns), this.gridWidth(9, columns), ProductionMonitorThemeScreen.booleanLabel("settings.map.details", (Boolean)ClientConfig.VALUES.mapShowDetails.get()), () -> ClientConfig.VALUES.mapShowDetails.set(!ClientConfig.VALUES.mapShowDetails.get()));
    }

    private void buildHud(int n, int n2) {
        boolean matched = (Boolean)ClientConfig.VALUES.matchHudTheme.get();
        boolean customActive = ClientConfig.effectiveHudTheme() == ClientConfig.InterfaceStyle.CUSTOM;
        boolean storedMode = ClientConfig.VALUES.hudMode.get() == MonitorNetwork.HudMode.STORED;

        int splitGap = 10;
        int minimumPreview = Math.min(150, Math.max(110, n2 / 3));
        this.hudSettingsWidth = Math.max(180,
                Math.min(n2 - splitGap - minimumPreview, Math.round(n2 * 0.57f)));
        this.hudSettingsLeft = n;
        this.hudPreviewLeft = n + this.hudSettingsWidth + splitGap;
        this.hudPreviewRight = n + n2;
        this.hudPreviewTop = this.contentTop;
        this.hudPreviewBottom = this.contentBottom;

        int tabGap = 3;
        HudSettingsSection[] sections = HudSettingsSection.values();
        int tabWidth = Math.max(1, (this.hudSettingsWidth - tabGap * (sections.length - 1)) / sections.length);
        for (int i = 0; i < sections.length; ++i) {
            HudSettingsSection section = sections[i];
            int x = this.hudSettingsLeft + i * (tabWidth + tabGap);
            int width = i == sections.length - 1
                    ? Math.max(1, this.hudSettingsLeft + this.hudSettingsWidth - x)
                    : tabWidth;
            this.addRenderableWidget(ForeverButton.create(
                    ProductionMonitorThemeScreen.hudSectionLabel(section),
                    button -> {
                        this.hudSettingsSection = section;
                        this.hudSettingsScroll = 0;
                        this.rebuildWidgets();
                    },
                    section == this.hudSettingsSection
                            ? ForeverButton.Style.THEMED_ACTIVE
                            : ForeverButton.Style.THEMED,
                    x, this.contentTop, width, 20).setRole(ForeverButton.Role.TAB));
        }

        this.hudSettingsTop = this.contentTop + 28;
        this.hudSettingsBottom = this.contentBottom;
        int itemCount = switch (this.hudSettingsSection) {
            case GENERAL -> 4;
            case CONTENT -> 7 + (storedMode ? 1 : 0);
            case LAYOUT -> 5;
            case APPEARANCE -> 4 + (customActive ? 1 : 0);
        };
        this.hudSettingsColumns = this.hudSettingsWidth < 330 ? 1 : 2;
        int rows = Math.max(1, (itemCount + this.hudSettingsColumns - 1) / this.hudSettingsColumns);
        int contentHeight = 20 + (rows - 1) * this.gridStep();
        int viewportHeight = Math.max(1, this.hudSettingsBottom - this.hudSettingsTop);
        this.hudSettingsMaxScroll = Math.max(0, contentHeight - viewportHeight);
        this.hudSettingsScroll = Math.max(0, Math.min(this.hudSettingsScroll, this.hudSettingsMaxScroll));
        this.hudSettingsUsableWidth = Math.max(1,
                this.hudSettingsWidth - (this.hudSettingsMaxScroll > 0 ? 8 : 0));

        switch (this.hudSettingsSection) {
            case GENERAL -> this.buildHudGeneral(matched);
            case CONTENT -> this.buildHudContent(storedMode);
            case LAYOUT -> this.buildHudLayout();
            case APPEARANCE -> this.buildHudAppearance(matched, customActive);
        }

        int previewWidth = Math.max(1, this.hudPreviewRight - this.hudPreviewLeft);
        int zoomWidth = Math.max(72, Math.min(150, previewWidth - 12));
        int zoomX = this.hudPreviewLeft + Math.max(6, (previewWidth - zoomWidth) / 2);
        this.addRenderableWidget(ForeverButton.create(
                ProductionMonitorThemeScreen.hudPreviewZoomLabel(this.hudPreviewZoom),
                button -> {
                    this.hudPreviewZoom = this.hudPreviewZoom.next();
                    this.rebuildWidgets();
                },
                ForeverButton.Style.THEMED,
                zoomX, this.hudPreviewBottom - 22, Math.min(zoomWidth, previewWidth - 12), 20));
    }

    private void buildHudGeneral(boolean matched) {
        this.addHudToggle(0,
                ProductionMonitorThemeScreen.booleanLabel(
                        "settings.hud.enabled", (Boolean)ClientConfig.VALUES.hudEnabled.get()),
                () -> ClientConfig.VALUES.hudEnabled.set(!ClientConfig.VALUES.hudEnabled.get()));
        this.addHudToggle(1,
                ProductionMonitorThemeScreen.booleanLabel("settings.hud.match", matched),
                () -> ClientConfig.VALUES.matchHudTheme.set(!ClientConfig.VALUES.matchHudTheme.get()));
        this.addHudToggle(2,
                ProductionMonitorThemeScreen.modeLabel(),
                () -> ClientConfig.VALUES.hudMode.set(
                        ((MonitorNetwork.HudMode)((Object)ClientConfig.VALUES.hudMode.get())).next()));
        this.addHudSlider(3, "settings.hud.entries", 1.0, 10.0,
                ((Integer)ClientConfig.VALUES.hudEntryCount.get()).intValue(),
                d -> ClientConfig.VALUES.hudEntryCount.set((int)Math.round(d)), false);
    }

    private void buildHudContent(boolean storedMode) {
        this.addHudToggle(0,
                ProductionMonitorThemeScreen.booleanLabel(
                        "settings.hud.icons", (Boolean)ClientConfig.VALUES.hudShowIcons.get()),
                () -> ClientConfig.VALUES.hudShowIcons.set(!ClientConfig.VALUES.hudShowIcons.get()));
        this.addHudToggle(1,
                ProductionMonitorThemeScreen.booleanLabel(
                        "settings.hud.names", (Boolean)ClientConfig.VALUES.hudShowNames.get()),
                () -> ClientConfig.VALUES.hudShowNames.set(!ClientConfig.VALUES.hudShowNames.get()));
        this.addHudToggle(2,
                ProductionMonitorThemeScreen.booleanLabel(
                        "settings.hud.values", (Boolean)ClientConfig.VALUES.hudShowValues.get()),
                () -> ClientConfig.VALUES.hudShowValues.set(!ClientConfig.VALUES.hudShowValues.get()));
        this.addHudToggle(3,
                ProductionMonitorThemeScreen.booleanLabel(
                        "settings.hud.include_items", (Boolean)ClientConfig.VALUES.hudIncludeItems.get()),
                () -> ClientConfig.VALUES.hudIncludeItems.set(!ClientConfig.VALUES.hudIncludeItems.get()));
        this.addHudToggle(4,
                ProductionMonitorThemeScreen.booleanLabel(
                        "settings.hud.include_fluids", (Boolean)ClientConfig.VALUES.hudIncludeFluids.get()),
                () -> ClientConfig.VALUES.hudIncludeFluids.set(!ClientConfig.VALUES.hudIncludeFluids.get()));
        this.addHudToggle(5,
                ProductionMonitorThemeScreen.booleanLabel(
                        "settings.hud.include_energy", (Boolean)ClientConfig.VALUES.hudIncludeEnergy.get()),
                () -> ClientConfig.VALUES.hudIncludeEnergy.set(!ClientConfig.VALUES.hudIncludeEnergy.get()));
        this.addHudToggle(6,
                ProductionMonitorThemeScreen.booleanLabel(
                        "settings.hud.include_infinite", (Boolean)ClientConfig.VALUES.hudIncludeInfinite.get()),
                () -> ClientConfig.VALUES.hudIncludeInfinite.set(!ClientConfig.VALUES.hudIncludeInfinite.get()));
        if (storedMode) {
            this.addHudToggle(7,
                    ProductionMonitorThemeScreen.storedSortLabel(),
                    () -> ClientConfig.VALUES.hudStoredSort.set(
                            ((ClientConfig.HudStoredSort)ClientConfig.VALUES.hudStoredSort.get()).next()));
        }
    }

    private void buildHudLayout() {
        this.addHudToggle(0,
                ProductionMonitorThemeScreen.hudLayoutLabel(),
                () -> ClientConfig.VALUES.hudLayoutStyle.set(
                        ((ClientConfig.HudLayoutStyle)((Object)ClientConfig.VALUES.hudLayoutStyle.get())).next()));
        this.addHudToggle(1, ProductionMonitorThemeScreen.anchorLabel(), () -> {
            ClientConfig.HudAnchor[] values = ClientConfig.HudAnchor.values();
            ClientConfig.VALUES.hudAnchor.set(values[
                    (((ClientConfig.HudAnchor)((Object)ClientConfig.VALUES.hudAnchor.get())).ordinal() + 1)
                            % values.length]);
        });
        this.addHudSlider(2, "settings.hud.scale", 0.5, 2.0,
                (Double)ClientConfig.VALUES.hudScale.get(),
                d -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.hudScale).set(d), true);
        this.addHudSlider(3, "settings.hud.x", 0.0, 200.0,
                ((Integer)ClientConfig.VALUES.hudXOffset.get()).intValue(),
                d -> ClientConfig.VALUES.hudXOffset.set((int)Math.round(d)), false);
        this.addHudSlider(4, "settings.hud.y", 0.0, 200.0,
                ((Integer)ClientConfig.VALUES.hudYOffset.get()).intValue(),
                d -> ClientConfig.VALUES.hudYOffset.set((int)Math.round(d)), false);
    }

    private void buildHudAppearance(boolean matched, boolean customActive) {
        if (matched) {
            ForeverButton matchedTheme = ForeverButton.create(
                    ProductionMonitorThemeScreen.hudThemeMatchedLabel(ClientConfig.effectiveHudTheme()),
                    button -> {},
                    ForeverButton.Style.THEMED,
                    this.hudControlX(0), this.hudControlY(0), this.hudControlWidth(0), 20);
            matchedTheme.active = false;
            this.addHudControl(matchedTheme);
        } else {
            this.themeDropdown = new ThemeDropdown(
                    this.hudControlX(0), this.hudControlY(0), this.hudControlWidth(0), 20,
                    this::selectHudStyle,
                    () -> (ClientConfig.InterfaceStyle)ClientConfig.VALUES.hudThemeStyle.get(),
                    ProductionMonitorThemeScreen::hudThemeLabel);
            this.addHudControl(this.themeDropdown);
        }

        this.addHudSlider(1, "settings.hud.opacity", 0.0, 1.0,
                (Double)ClientConfig.VALUES.hudOpacity.get(),
                d -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.hudOpacity).set(d), true);
        this.addHudToggle(2,
                ProductionMonitorThemeScreen.booleanLabel(
                        "settings.hud.animations", (Boolean)ClientConfig.VALUES.hudAnimations.get()),
                () -> ClientConfig.VALUES.hudAnimations.set(!ClientConfig.VALUES.hudAnimations.get()));
        this.addHudSlider(3, "settings.hud.animation_intensity", 0.0, 1.0,
                (Double)ClientConfig.VALUES.hudAnimationIntensity.get(),
                d -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.hudAnimationIntensity).set(d), true);

        if (customActive) {
            ForeverButton designer = ForeverButton.create(
                    ProductionMonitorThemeScreen.translated("settings.hud.open_custom_designer"),
                    button -> {
                        if (this.minecraft != null) {
                            this.minecraft.setScreen(new CustomHudScreen(this));
                        }
                    },
                    ForeverButton.Style.THEMED_ACTIVE,
                    this.hudControlX(4), this.hudControlY(4), this.hudControlWidth(4), 20);
            this.addHudControl(designer);
        }
    }

    private void addHudToggle(int index, Component component, Runnable runnable) {
        ForeverButton button = ForeverButton.create(component, pressed -> {
            runnable.run();
            this.rebuildWidgets();
            ForeverProductionMonitorClient.refreshHudNow();
        }, ForeverButton.Style.THEMED,
                this.hudControlX(index), this.hudControlY(index), this.hudControlWidth(index), 20);
        this.addHudControl(button);
    }

    private void addHudSlider(int index, String key, double min, double max, double value,
                              DoubleConsumer setter, boolean decimal) {
        this.addHudControl(new SettingSlider(
                this.hudControlX(index), this.hudControlY(index), this.hudControlWidth(index),
                key, min, max, value, setter, decimal));
    }

    private void addHudControl(AbstractWidget widget) {
        widget.visible = widget.getY() + widget.getHeight() > this.hudSettingsTop
                && widget.getY() < this.hudSettingsBottom;
        this.addWidget(widget);
        this.hudControlWidgets.add(widget);
    }

    private int hudControlX(int index) {
        int gap = 4;
        int columnWidth = Math.max(1,
                (this.hudSettingsUsableWidth - gap * (this.hudSettingsColumns - 1)) / this.hudSettingsColumns);
        return this.hudSettingsLeft + index % this.hudSettingsColumns * (columnWidth + gap);
    }

    private int hudControlY(int index) {
        return this.hudSettingsTop - this.hudSettingsScroll
                + index / this.hudSettingsColumns * this.gridStep();
    }

    private int hudControlWidth(int index) {
        int gap = 4;
        int columnWidth = Math.max(1,
                (this.hudSettingsUsableWidth - gap * (this.hudSettingsColumns - 1)) / this.hudSettingsColumns);
        return index % this.hudSettingsColumns == this.hudSettingsColumns - 1
                ? Math.max(1, this.hudSettingsLeft + this.hudSettingsUsableWidth - this.hudControlX(index))
                : columnWidth;
    }

    private void buildAnimations(int n, int n2) {
        int columns = this.gridColumns(3, 7, this.contentTop);
        this.addToggle(this.gridX(0, columns), this.gridY(0, columns), this.gridWidth(0, columns),
                ProductionMonitorThemeScreen.booleanLabel("settings.gui_animations", (Boolean)ClientConfig.VALUES.guiAnimations.get()),
                () -> ClientConfig.VALUES.guiAnimations.set(!ClientConfig.VALUES.guiAnimations.get()));
        this.addToggle(this.gridX(1, columns), this.gridY(1, columns), this.gridWidth(1, columns),
                ProductionMonitorThemeScreen.booleanLabel("settings.animations.update_pulse", (Boolean)ClientConfig.VALUES.updatePulseEnabled.get()),
                () -> ClientConfig.VALUES.updatePulseEnabled.set(!ClientConfig.VALUES.updatePulseEnabled.get()));
        this.addRenderableWidget(new SettingSlider(this.gridX(2, columns), this.gridY(2, columns), this.gridWidth(2, columns),
                "settings.animations.pulse_intensity", 0.0, 1.0, (Double)ClientConfig.VALUES.updatePulseIntensity.get(),
                arg_0 -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.updatePulseIntensity).set(arg_0), true));
        this.addRenderableWidget(new SettingSlider(this.gridX(3, columns), this.gridY(3, columns), this.gridWidth(3, columns),
                "settings.animations.pulse_duration", 0.6, 4.0, (Double)ClientConfig.VALUES.updatePulseDuration.get(),
                arg_0 -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.updatePulseDuration).set(arg_0), true));
        this.addToggle(this.gridX(4, columns), this.gridY(4, columns), this.gridWidth(4, columns),
                ProductionMonitorThemeScreen.booleanLabel("settings.animations.ambient_motion", (Boolean)ClientConfig.VALUES.ambientMotionEnabled.get()),
                () -> ClientConfig.VALUES.ambientMotionEnabled.set(!ClientConfig.VALUES.ambientMotionEnabled.get()));
        this.addRenderableWidget(new SettingSlider(this.gridX(5, columns), this.gridY(5, columns), this.gridWidth(5, columns),
                "settings.animations.ambient_intensity", 0.0, 1.0, (Double)ClientConfig.VALUES.ambientMotionIntensity.get(),
                arg_0 -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.ambientMotionIntensity).set(arg_0), true));
        this.addToggle(this.gridX(6, columns), this.gridY(6, columns), this.gridWidth(6, columns),
                ProductionMonitorThemeScreen.booleanLabel("settings.animations.interactions", (Boolean)ClientConfig.VALUES.themeInteractionsEnabled.get()),
                () -> ClientConfig.VALUES.themeInteractionsEnabled.set(!ClientConfig.VALUES.themeInteractionsEnabled.get()));
    }

    private void addToggle(int n, int n2, int n3, Component component, Runnable runnable) {
        this.addRenderableWidget(ForeverButton.create(component, button -> {
            runnable.run();
            this.rebuildWidgets();
            ForeverProductionMonitorClient.refreshHudNow();
        }, ForeverButton.Style.THEMED, n, n2, n3, 20));
    }

    private int gridColumns(int preferred, int itemCount, int startY) {
        int base = this.contentWidth < 360 ? Math.min(preferred, 2) : (this.contentWidth < 560 ? Math.min(preferred, 3) : preferred);
        int rowsAvailable = Math.max(1, (this.contentBottom - startY - 20) / this.gridStep() + 1);
        int required = Math.max(1, (itemCount + rowsAvailable - 1) / rowsAvailable);
        return Math.max(1, Math.min(itemCount, Math.min(preferred, Math.max(base, required))));
    }

    private int gridStep() {
        return this.panelHeight < 340 ? 22 : 28;
    }

    private int gridX(int index, int columns) {
        int gap = 4;
        int column = Math.max(1, (this.contentWidth - gap * (columns - 1)) / columns);
        return this.contentLeft + index % columns * (column + gap);
    }

    private int gridY(int index, int columns) {
        return this.gridY(index, columns, this.contentTop);
    }

    private int gridY(int index, int columns, int startY) {
        return startY + index / columns * this.gridStep();
    }

    private int gridWidth(int index, int columns) {
        int gap = 4;
        int column = Math.max(1, (this.contentWidth - gap * (columns - 1)) / columns);
        return index % columns == columns - 1 ? Math.max(1, this.contentLeft + this.contentWidth - this.gridX(index, columns)) : column;
    }

    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        ThemeInteractionState.beginFrame(n, n2);
        ClientConfig.InterfaceStyle interfaceStyle = (ClientConfig.InterfaceStyle)((Object)ClientConfig.VALUES.interfaceStyle.get());
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        guiGraphics.fill(0, 0, this.width, this.height, palette.backdrop());
        InterfaceTheme.drawPanel(guiGraphics, this.left, this.top, this.panelWidth, this.panelHeight, interfaceStyle, palette);
        guiGraphics.drawCenteredString(this.font, this.title, this.left + this.panelWidth / 2, this.top + 13, palette.text());
        String version = "Forever Production Monitor " + ForeverProductionMonitor.VERSION;
        guiGraphics.drawString(this.font, version, this.left + this.panelWidth - 18 - this.font.width(version), this.footerY - 14, palette.muted(), false);
        if (this.category == Category.HUD) {
            this.drawHudPanels(guiGraphics, palette);
        }
        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, n, n2, f);
        }
        if (this.category == Category.INTERFACE) {
            this.drawInterfacePreview(guiGraphics, palette, interfaceStyle);
        }
        if (this.category == Category.HUD) {
            this.drawHudPreview(guiGraphics, palette);
            this.renderHudControls(guiGraphics, n, n2, f, palette);
        }
        if (this.themeDropdown != null && this.category != Category.HUD) {
            guiGraphics.flush();
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0f, 0.0f, 300.0f);
            this.themeDropdown.render(guiGraphics, n, n2, f);
            guiGraphics.flush();
            guiGraphics.pose().popPose();
        }
    }

    private void drawHudPanels(GuiGraphics guiGraphics, InterfaceTheme.Palette palette) {
        guiGraphics.fill(this.hudSettingsLeft, this.contentTop,
                this.hudSettingsLeft + this.hudSettingsWidth, this.hudSettingsBottom,
                palette.tableOuter());
        InterfaceTheme.drawTableDecoration(guiGraphics, this.hudSettingsLeft, this.contentTop,
                this.hudSettingsWidth, Math.max(1, this.hudSettingsBottom - this.contentTop));

        int previewWidth = Math.max(1, this.hudPreviewRight - this.hudPreviewLeft);
        int previewHeight = Math.max(1, this.hudPreviewBottom - this.hudPreviewTop);
        guiGraphics.fill(this.hudPreviewLeft, this.hudPreviewTop,
                this.hudPreviewRight, this.hudPreviewBottom, palette.tableOuter());
        guiGraphics.fill(this.hudPreviewLeft + 1, this.hudPreviewTop + 1,
                this.hudPreviewRight - 1, this.hudPreviewTop + 20, palette.tableHeader());
        InterfaceTheme.drawTableDecoration(guiGraphics, this.hudPreviewLeft, this.hudPreviewTop,
                previewWidth, previewHeight);
        guiGraphics.drawCenteredString(this.font,
                ProductionMonitorThemeScreen.translated("settings.hud.live_preview"),
                this.hudPreviewLeft + previewWidth / 2, this.hudPreviewTop + 7, palette.text());
    }

    private void drawHudPreview(GuiGraphics guiGraphics, InterfaceTheme.Palette palette) {
        int previewContentTop = this.hudPreviewTop + 22;
        int previewContentBottom = this.hudPreviewBottom - 28;
        int previewWidth = Math.max(1, this.hudPreviewRight - this.hudPreviewLeft);
        int previewHeight = Math.max(1, previewContentBottom - previewContentTop);
        if (previewWidth < 48 || previewHeight < 48) {
            return;
        }

        double multiplier = this.hudPreviewScaleMultiplier(previewWidth - 12, previewHeight - 8);
        guiGraphics.flush();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0f, 0.0f, 200.0f);
        guiGraphics.enableScissor(this.hudPreviewLeft + 2, previewContentTop,
                this.hudPreviewRight - 2, previewContentBottom);
        ProductionHud.renderPreviewCentered(guiGraphics,
                this.hudPreviewLeft + 6, previewContentTop + 4,
                this.hudPreviewRight - 6, previewContentBottom - 4, multiplier);
        guiGraphics.flush();
        guiGraphics.disableScissor();
        guiGraphics.pose().popPose();
    }

    private double hudPreviewScaleMultiplier(int availableWidth, int availableHeight) {
        if (this.hudPreviewZoom != HudPreviewZoom.FIT) {
            return this.hudPreviewZoom.multiplier;
        }
        double configuredScale = Math.max(0.05, (Double)ClientConfig.VALUES.hudScale.get());
        int rows = Math.max(1, Math.min(10, (Integer)ClientConfig.VALUES.hudEntryCount.get()));
        int naturalHeight = 20 + rows * 17;
        double fitWidth = Math.max(0.25, availableWidth / (188.0 * configuredScale));
        double fitHeight = Math.max(0.25, availableHeight / (naturalHeight * configuredScale));
        return Math.max(0.25, Math.min(3.0, Math.min(fitWidth, fitHeight)));
    }

    private void renderHudControls(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick,
                                   InterfaceTheme.Palette palette) {
        guiGraphics.enableScissor(this.hudSettingsLeft, this.hudSettingsTop,
                this.hudSettingsLeft + this.hudSettingsWidth, this.hudSettingsBottom);
        for (AbstractWidget widget : this.hudControlWidgets) {
            widget.visible = widget.getY() + widget.getHeight() > this.hudSettingsTop
                    && widget.getY() < this.hudSettingsBottom;
            if (widget.visible && widget != this.themeDropdown) {
                widget.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }

        if (this.themeDropdown != null && this.themeDropdown.visible) {
            guiGraphics.flush();
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0f, 0.0f, 300.0f);
            this.themeDropdown.render(guiGraphics, mouseX, mouseY, partialTick);
            guiGraphics.flush();
            guiGraphics.pose().popPose();
        }
        guiGraphics.disableScissor();

        if (this.hudSettingsMaxScroll > 0) {
            int trackX = this.hudSettingsLeft + this.hudSettingsWidth - 4;
            int trackTop = this.hudSettingsTop + 1;
            int trackHeight = Math.max(12, this.hudSettingsBottom - this.hudSettingsTop - 2);
            int viewportHeight = Math.max(1, this.hudSettingsBottom - this.hudSettingsTop);
            int contentHeight = viewportHeight + this.hudSettingsMaxScroll;
            int thumbHeight = Math.max(18, trackHeight * viewportHeight / Math.max(viewportHeight, contentHeight));
            int travel = Math.max(1, trackHeight - thumbHeight);
            int thumbY = trackTop + travel * this.hudSettingsScroll / Math.max(1, this.hudSettingsMaxScroll);
            guiGraphics.fill(trackX, trackTop, trackX + 3, trackTop + trackHeight,
                    ProductionMonitorThemeScreen.opaque(palette.outer()));
            guiGraphics.fill(trackX, thumbY, trackX + 3, thumbY + thumbHeight,
                    ProductionMonitorThemeScreen.opaque(palette.accentB()));
        }
    }

    private void drawInterfacePreview(GuiGraphics guiGraphics, InterfaceTheme.Palette palette, ClientConfig.InterfaceStyle interfaceStyle) {
        int n = this.contentLeft;
        int n2 = this.previewTop;
        int n3 = this.contentWidth;
        int n4 = Math.max(0, this.contentBottom - n2);
        if (n4 < 104) {
            return;
        }
        InterfaceTheme.drawPanel(guiGraphics, n, n2, n3, n4, interfaceStyle, palette);
        guiGraphics.drawCenteredString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.theme.preview"), n + n3 / 2, n2 + 11, palette.text());
        int n5 = n + 12;
        int n6 = n + n3 - 12;
        int n7 = n2 + 38;
        guiGraphics.fill(n5, n7, n6, n7 + 59, palette.tableOuter());
        guiGraphics.fill(n5 + 1, n7 + 1, n6 - 1, n7 + 15, palette.tableHeader());
        InterfaceTheme.drawTableDecoration(guiGraphics, n5, n7, n6 - n5, 59);
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
                ClientConfig.VALUES.interfaceCustomPrimarySaturation.set(((Double)ClientConfig.VALUES.interfaceCustomPrimarySaturation.getDefault()));
                ClientConfig.VALUES.interfaceCustomSecondarySaturation.set(((Double)ClientConfig.VALUES.interfaceCustomSecondarySaturation.getDefault()));
                ClientConfig.VALUES.interfaceCustomSurfaceSaturation.set(((Double)ClientConfig.VALUES.interfaceCustomSurfaceSaturation.getDefault()));
                ClientConfig.VALUES.interfaceCustomContrast.set(((Double)ClientConfig.VALUES.interfaceCustomContrast.getDefault()));
                ClientConfig.VALUES.interfaceCustomHeaderStrength.set(((Double)ClientConfig.VALUES.interfaceCustomHeaderStrength.getDefault()));
                ClientConfig.VALUES.interfaceCustomBorderStrength.set(((Double)ClientConfig.VALUES.interfaceCustomBorderStrength.getDefault()));
                ClientConfig.VALUES.interfaceCustomAccentBrightness.set(((Double)ClientConfig.VALUES.interfaceCustomAccentBrightness.getDefault()));
                ClientConfig.VALUES.interfaceCustomBackgroundStyle.set((ClientConfig.CustomBackgroundStyle)ClientConfig.VALUES.interfaceCustomBackgroundStyle.getDefault());
                ClientConfig.VALUES.interfaceCustomBackgroundDensity.set((Double)ClientConfig.VALUES.interfaceCustomBackgroundDensity.getDefault());
                ClientConfig.VALUES.interfaceCustomBackgroundSpeed.set((Double)ClientConfig.VALUES.interfaceCustomBackgroundSpeed.getDefault());
                ClientConfig.VALUES.interfaceCustomBackgroundOpacity.set((Double)ClientConfig.VALUES.interfaceCustomBackgroundOpacity.getDefault());
                ClientConfig.VALUES.interfaceCustomBackgroundScale.set((Double)ClientConfig.VALUES.interfaceCustomBackgroundScale.getDefault());
                ClientConfig.VALUES.interfaceCustomParticleStyle.set((ClientConfig.CustomParticleStyle)ClientConfig.VALUES.interfaceCustomParticleStyle.getDefault());
                ClientConfig.VALUES.interfaceCustomParticleColorMode.set((ClientConfig.CustomParticleColorMode)ClientConfig.VALUES.interfaceCustomParticleColorMode.getDefault());
                ClientConfig.VALUES.interfaceCustomParticleAmount.set((Double)ClientConfig.VALUES.interfaceCustomParticleAmount.getDefault());
                ClientConfig.VALUES.interfaceCustomParticleSpeed.set((Double)ClientConfig.VALUES.interfaceCustomParticleSpeed.getDefault());
                ClientConfig.VALUES.interfaceCustomParticleSize.set((Double)ClientConfig.VALUES.interfaceCustomParticleSize.getDefault());
                ClientConfig.VALUES.interfaceCustomParticleOpacity.set((Double)ClientConfig.VALUES.interfaceCustomParticleOpacity.getDefault());
                ClientConfig.VALUES.interfaceCustomTabStyle.set((ClientConfig.CustomTabStyle)ClientConfig.VALUES.interfaceCustomTabStyle.getDefault());
                ClientConfig.VALUES.interfaceCustomButtonStyle.set((ClientConfig.CustomButtonStyle)ClientConfig.VALUES.interfaceCustomButtonStyle.getDefault());
                ClientConfig.VALUES.interfaceCustomTabAccentStrength.set((Double)ClientConfig.VALUES.interfaceCustomTabAccentStrength.getDefault());
                ClientConfig.VALUES.interfaceCustomButtonHoverStrength.set((Double)ClientConfig.VALUES.interfaceCustomButtonHoverStrength.getDefault());
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
                ClientConfig.VALUES.hudThemeStyle.set(((ClientConfig.InterfaceStyle)((Object)ClientConfig.VALUES.hudThemeStyle.getDefault())));
                ClientConfig.VALUES.hudLayoutStyle.set(((ClientConfig.HudLayoutStyle)((Object)ClientConfig.VALUES.hudLayoutStyle.getDefault())));
                ClientConfig.VALUES.hudAnimations.set(((Boolean)ClientConfig.VALUES.hudAnimations.getDefault()));
                ClientConfig.VALUES.hudAnimationIntensity.set(((Double)ClientConfig.VALUES.hudAnimationIntensity.getDefault()));
                ClientConfig.VALUES.hudCustomHue.set(((Integer)ClientConfig.VALUES.hudCustomHue.getDefault()));
                ClientConfig.VALUES.hudCustomSecondaryHue.set(((Integer)ClientConfig.VALUES.hudCustomSecondaryHue.getDefault()));
                ClientConfig.VALUES.hudCustomPrimarySaturation.set(((Double)ClientConfig.VALUES.hudCustomPrimarySaturation.getDefault()));
                ClientConfig.VALUES.hudCustomSecondarySaturation.set(((Double)ClientConfig.VALUES.hudCustomSecondarySaturation.getDefault()));
                ClientConfig.VALUES.hudCustomSurfaceSaturation.set(((Double)ClientConfig.VALUES.hudCustomSurfaceSaturation.getDefault()));
                ClientConfig.VALUES.hudCustomBrightness.set(((Double)ClientConfig.VALUES.hudCustomBrightness.getDefault()));
                ClientConfig.VALUES.hudCustomContrast.set(((Double)ClientConfig.VALUES.hudCustomContrast.getDefault()));
                ClientConfig.VALUES.hudCustomHeaderStrength.set(((Double)ClientConfig.VALUES.hudCustomHeaderStrength.getDefault()));
                ClientConfig.VALUES.hudCustomRowContrast.set(((Double)ClientConfig.VALUES.hudCustomRowContrast.getDefault()));
                ClientConfig.VALUES.hudCustomBorderStrength.set(((Double)ClientConfig.VALUES.hudCustomBorderStrength.getDefault()));
                ClientConfig.VALUES.hudCustomAccentBrightness.set(((Double)ClientConfig.VALUES.hudCustomAccentBrightness.getDefault()));
                ClientConfig.VALUES.hudCustomDecorationStyle.set((ClientConfig.CustomHudDecorationStyle)ClientConfig.VALUES.hudCustomDecorationStyle.getDefault());
                ClientConfig.VALUES.hudEntryCount.set(((Integer)ClientConfig.VALUES.hudEntryCount.getDefault()));
                ClientConfig.VALUES.hudShowIcons.set(((Boolean)ClientConfig.VALUES.hudShowIcons.getDefault()));
                ClientConfig.VALUES.hudShowNames.set(((Boolean)ClientConfig.VALUES.hudShowNames.getDefault()));
                ClientConfig.VALUES.hudShowValues.set(((Boolean)ClientConfig.VALUES.hudShowValues.getDefault()));
                ClientConfig.VALUES.hudIncludeItems.set(((Boolean)ClientConfig.VALUES.hudIncludeItems.getDefault()));
                ClientConfig.VALUES.hudIncludeFluids.set(((Boolean)ClientConfig.VALUES.hudIncludeFluids.getDefault()));
                ClientConfig.VALUES.hudIncludeEnergy.set(((Boolean)ClientConfig.VALUES.hudIncludeEnergy.getDefault()));
                ClientConfig.VALUES.hudIncludeInfinite.set(((Boolean)ClientConfig.VALUES.hudIncludeInfinite.getDefault()));
                ClientConfig.VALUES.hudStoredSort.set((ClientConfig.HudStoredSort)ClientConfig.VALUES.hudStoredSort.getDefault());
                ClientConfig.VALUES.hudScale.set(((Double)ClientConfig.VALUES.hudScale.getDefault()));
                ClientConfig.VALUES.hudOpacity.set(((Double)ClientConfig.VALUES.hudOpacity.getDefault()));
                ClientConfig.VALUES.hudXOffset.set(((Integer)ClientConfig.VALUES.hudXOffset.getDefault()));
                ClientConfig.VALUES.hudYOffset.set(((Integer)ClientConfig.VALUES.hudYOffset.getDefault()));
                ClientConfig.VALUES.matchHudTheme.set(((Boolean)ClientConfig.VALUES.matchHudTheme.getDefault()));
                break;
            }
            case ANIMATIONS: {
                ClientConfig.VALUES.guiAnimations.set(((Boolean)ClientConfig.VALUES.guiAnimations.getDefault()));
                ClientConfig.VALUES.updatePulseEnabled.set(((Boolean)ClientConfig.VALUES.updatePulseEnabled.getDefault()));
                ClientConfig.VALUES.updatePulseIntensity.set(((Double)ClientConfig.VALUES.updatePulseIntensity.getDefault()));
                ClientConfig.VALUES.updatePulseDuration.set(((Double)ClientConfig.VALUES.updatePulseDuration.getDefault()));
                ClientConfig.VALUES.ambientMotionEnabled.set(((Boolean)ClientConfig.VALUES.ambientMotionEnabled.getDefault()));
                ClientConfig.VALUES.ambientMotionIntensity.set(((Double)ClientConfig.VALUES.ambientMotionIntensity.getDefault()));
                ClientConfig.VALUES.themeInteractionsEnabled.set(((Boolean)ClientConfig.VALUES.themeInteractionsEnabled.getDefault()));
            }
        }
        ForeverProductionMonitorClient.refreshHudNow();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (ThemeInteractionState.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 0 && this.category == Category.HUD && this.hudSettingsMaxScroll > 0) {
            int trackX = this.hudSettingsLeft + this.hudSettingsWidth - 4;
            if (mouseX >= trackX - 2 && mouseX < trackX + 5
                    && mouseY >= this.hudSettingsTop && mouseY < this.hudSettingsBottom) {
                this.draggingHudScrollbar = true;
                this.setHudSettingsScrollFromMouse(mouseY);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.draggingHudScrollbar && button == 0) {
            this.setHudSettingsScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.draggingHudScrollbar && button == 0) {
            this.draggingHudScrollbar = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void setHudSettingsScrollFromMouse(double mouseY) {
        int trackTop = this.hudSettingsTop + 1;
        int trackHeight = Math.max(12, this.hudSettingsBottom - this.hudSettingsTop - 2);
        int viewportHeight = Math.max(1, this.hudSettingsBottom - this.hudSettingsTop);
        int contentHeight = viewportHeight + this.hudSettingsMaxScroll;
        int thumbHeight = Math.max(18, trackHeight * viewportHeight / Math.max(viewportHeight, contentHeight));
        int travel = Math.max(1, trackHeight - thumbHeight);
        double normalized = (mouseY - trackTop - thumbHeight / 2.0) / travel;
        int next = (int)Math.round(Math.max(0.0, Math.min(1.0, normalized)) * this.hudSettingsMaxScroll);
        if (next != this.hudSettingsScroll) {
            this.hudSettingsScroll = next;
            this.rebuildWidgets();
        }
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
        ClientConfig.VALUES.hudThemeStyle.save();
        ClientConfig.VALUES.hudLayoutStyle.save();
        ClientConfig.VALUES.hudAnimations.save();
        ClientConfig.VALUES.hudAnimationIntensity.save();
        ClientConfig.VALUES.hudCustomHue.save();
        ClientConfig.VALUES.hudCustomSecondaryHue.save();
        ClientConfig.VALUES.hudCustomPrimarySaturation.save();
        ClientConfig.VALUES.hudCustomSecondarySaturation.save();
        ClientConfig.VALUES.hudCustomSurfaceSaturation.save();
        ClientConfig.VALUES.hudCustomBrightness.save();
        ClientConfig.VALUES.hudCustomContrast.save();
        ClientConfig.VALUES.hudCustomHeaderStrength.save();
        ClientConfig.VALUES.hudCustomRowContrast.save();
        ClientConfig.VALUES.hudCustomBorderStrength.save();
        ClientConfig.VALUES.hudCustomAccentBrightness.save();
        ClientConfig.VALUES.hudCustomDecorationStyle.save();
        ClientConfig.VALUES.hudEntryCount.save();
        ClientConfig.VALUES.hudShowIcons.save();
        ClientConfig.VALUES.hudShowNames.save();
        ClientConfig.VALUES.hudShowValues.save();
        ClientConfig.VALUES.hudIncludeItems.save();
        ClientConfig.VALUES.hudIncludeFluids.save();
        ClientConfig.VALUES.hudIncludeEnergy.save();
        ClientConfig.VALUES.hudIncludeInfinite.save();
        ClientConfig.VALUES.hudStoredSort.save();
        ClientConfig.VALUES.rateUnit.save();
        ClientConfig.VALUES.refreshInterval.save();
        ClientConfig.VALUES.interfaceStyle.save();
        ClientConfig.VALUES.interfaceCustomHue.save();
        ClientConfig.VALUES.interfaceCustomSecondaryHue.save();
        ClientConfig.VALUES.interfaceCustomBrightness.save();
        ClientConfig.VALUES.interfaceCustomPrimarySaturation.save();
        ClientConfig.VALUES.interfaceCustomSecondarySaturation.save();
        ClientConfig.VALUES.interfaceCustomSurfaceSaturation.save();
        ClientConfig.VALUES.interfaceCustomContrast.save();
        ClientConfig.VALUES.interfaceCustomHeaderStrength.save();
        ClientConfig.VALUES.interfaceCustomBorderStrength.save();
        ClientConfig.VALUES.interfaceCustomAccentBrightness.save();
        ClientConfig.VALUES.interfaceCustomBackgroundStyle.save();
        ClientConfig.VALUES.interfaceCustomBackgroundDensity.save();
        ClientConfig.VALUES.interfaceCustomBackgroundSpeed.save();
        ClientConfig.VALUES.interfaceCustomBackgroundOpacity.save();
        ClientConfig.VALUES.interfaceCustomBackgroundScale.save();
        ClientConfig.VALUES.interfaceCustomParticleStyle.save();
        ClientConfig.VALUES.interfaceCustomParticleColorMode.save();
        ClientConfig.VALUES.interfaceCustomParticleAmount.save();
        ClientConfig.VALUES.interfaceCustomParticleSpeed.save();
        ClientConfig.VALUES.interfaceCustomParticleSize.save();
        ClientConfig.VALUES.interfaceCustomParticleOpacity.save();
        ClientConfig.VALUES.interfaceCustomTabStyle.save();
        ClientConfig.VALUES.interfaceCustomButtonStyle.save();
        ClientConfig.VALUES.interfaceCustomTabAccentStrength.save();
        ClientConfig.VALUES.interfaceCustomButtonHoverStrength.save();
        ClientConfig.VALUES.interfaceOpacity.save();
        ClientConfig.VALUES.guiAnimations.save();
        ClientConfig.VALUES.updatePulseEnabled.save();
        ClientConfig.VALUES.updatePulseIntensity.save();
        ClientConfig.VALUES.updatePulseDuration.save();
        ClientConfig.VALUES.ambientMotionEnabled.save();
        ClientConfig.VALUES.ambientMotionIntensity.save();
        ClientConfig.VALUES.themeInteractionsEnabled.save();
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

    private static int opaque(int color) {
        return color & 0xFFFFFF | 0xFF000000;
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

    private static Component storedSortLabel() {
        return ProductionMonitorThemeScreen.translated(
                "settings.hud.stored_sort",
                Component.translatable("settings.forever_production_monitor.hud.stored_sort."
                        + ProductionMonitorThemeScreen.lower((Enum)ClientConfig.VALUES.hudStoredSort.get())));
    }

    private static Component frameLabel() {
        return ProductionMonitorThemeScreen.translated("settings.hud.frame", Component.translatable((String)("config.forever_production_monitor.frame." + ProductionMonitorThemeScreen.lower((Enum)ClientConfig.VALUES.hudFrameStyle.get()))));
    }

    private static Component hudThemeLabel(ClientConfig.InterfaceStyle style) {
        return ProductionMonitorThemeScreen.translated("settings.hud.theme", ProductionMonitorThemeScreen.themeName(style));
    }

    private static Component hudThemeMatchedLabel(ClientConfig.InterfaceStyle style) {
        return ProductionMonitorThemeScreen.translated("settings.hud.theme_matched", ProductionMonitorThemeScreen.themeName(style));
    }

    private static Component hudLayoutLabel() {
        ClientConfig.HudLayoutStyle layout = (ClientConfig.HudLayoutStyle)((Object)ClientConfig.VALUES.hudLayoutStyle.get());
        return ProductionMonitorThemeScreen.translated("settings.hud.layout",
                ProductionMonitorThemeScreen.translated("settings.hud.layout." + ProductionMonitorThemeScreen.lower(layout), new Object[0]));
    }

    private static Component hudSectionLabel(HudSettingsSection section) {
        return ProductionMonitorThemeScreen.translated(
                "settings.hud.section." + ProductionMonitorThemeScreen.lower(section));
    }

    private static Component hudPreviewZoomLabel(HudPreviewZoom zoom) {
        return ProductionMonitorThemeScreen.translated(
                "settings.hud.preview_zoom",
                ProductionMonitorThemeScreen.translated(
                        "settings.hud.preview_zoom." + zoom.translationKey));
    }

    private void selectInterfaceStyle(ClientConfig.InterfaceStyle interfaceStyle) {
        ClientConfig.VALUES.interfaceStyle.set(interfaceStyle);
        if (interfaceStyle == ClientConfig.InterfaceStyle.CUSTOM && this.minecraft != null) {
            this.minecraft.setScreen(new CustomThemeScreen(this));
            return;
        }
        this.rebuildWidgets();
    }

    private void selectHudStyle(ClientConfig.InterfaceStyle interfaceStyle) {
        ClientConfig.VALUES.hudThemeStyle.set(interfaceStyle);
        this.rebuildWidgets();
        ForeverProductionMonitorClient.refreshHudNow();
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.themeDropdown != null && this.themeDropdown.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        if (this.category == Category.HUD && this.hudSettingsMaxScroll > 0
                && mouseX >= this.hudSettingsLeft
                && mouseX < this.hudSettingsLeft + this.hudSettingsWidth
                && mouseY >= this.hudSettingsTop && mouseY < this.hudSettingsBottom
                && scrollY != 0.0) {
            int next = Math.max(0, Math.min(this.hudSettingsMaxScroll,
                    this.hudSettingsScroll + (scrollY < 0.0 ? this.gridStep() : -this.gridStep())));
            if (next != this.hudSettingsScroll) {
                this.hudSettingsScroll = next;
                this.rebuildWidgets();
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private final class ThemeDropdown
    extends AbstractWidget {
        private static final int MAX_VISIBLE = 7;
        private boolean open;
        private int scroll;
        private long openedNanos;
        private final Consumer<ClientConfig.InterfaceStyle> selection;
        private final Supplier<ClientConfig.InterfaceStyle> current;
        private final Function<ClientConfig.InterfaceStyle, Component> selectedLabel;

        ThemeDropdown(int x, int y, int width, int height,
                      Consumer<ClientConfig.InterfaceStyle> selection,
                      Supplier<ClientConfig.InterfaceStyle> current,
                      Function<ClientConfig.InterfaceStyle, Component> selectedLabel) {
            super(x, y, width, height, selectedLabel.apply(current.get()));
            this.selection = selection;
            this.current = current;
            this.selectedLabel = selectedLabel;
            this.scroll = ProductionMonitorThemeScreen.this.themeDropdownScroll;
            this.ensureSelectedVisible();
        }

        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            InterfaceTheme.Palette palette = InterfaceTheme.current();
            this.setMessage(this.selectedLabel.apply(this.current.get()));
            boolean hovered = this.isHoveredOrFocused();
            graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), ProductionMonitorThemeScreen.opaque(hovered || this.open ? palette.accentA() : palette.border()));
            graphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.getWidth() - 1, this.getY() + this.getHeight() - 1, ProductionMonitorThemeScreen.opaque(palette.summary()));
            graphics.drawString(ProductionMonitorThemeScreen.this.font, ProductionMonitorThemeScreen.this.font.plainSubstrByWidth(this.getMessage().getString(), Math.max(1, this.getWidth() - 28)), this.getX() + 7, this.getY() + 6, palette.text(), false);
            graphics.drawString(ProductionMonitorThemeScreen.this.font, this.open ? "▲" : "▼", this.getX() + this.getWidth() - 15, this.getY() + 6, palette.accentB(), false);
            if (!this.open) {
                return;
            }
            ClientConfig.InterfaceStyle[] styles = ClientConfig.InterfaceStyle.selectableValues();
            int rows = this.visibleRows();
            int listY = this.getY() + this.getHeight();
            float openProgress = GuiMotion.easeOut(GuiMotion.progress(this.openedNanos, 140L));
            int revealedHeight = Math.max(1, (int)((rows * 18 + 2) * openProgress));
            graphics.enableScissor(this.getX(), listY, this.getX() + this.getWidth(), listY + revealedHeight);
            graphics.fill(this.getX(), listY, this.getX() + this.getWidth(), listY + rows * 18 + 2, ProductionMonitorThemeScreen.opaque(palette.border()));
            for (int row = 0; row < rows; ++row) {
                int index = this.scroll + row;
                int y = listY + 1 + row * 18;
                ClientConfig.InterfaceStyle style = styles[index];
                boolean over = mouseX >= this.getX() + 1 && mouseX < this.getX() + this.getWidth() - 1 && mouseY >= y && mouseY < y + 18;
                graphics.fill(this.getX() + 1, y, this.getX() + this.getWidth() - 1, y + 18, ProductionMonitorThemeScreen.opaque(style == this.current.get() ? palette.tableHeader() : (over ? palette.hover() : palette.rowEven())));
                graphics.drawString(ProductionMonitorThemeScreen.this.font, ProductionMonitorThemeScreen.this.font.plainSubstrByWidth(ProductionMonitorThemeScreen.themeName(style).getString(), Math.max(1, this.getWidth() - 18)), this.getX() + 7, y + 5, style == this.current.get() ? palette.accentB() : palette.text(), false);
            }
            if (styles.length > rows) {
                int trackX = this.getX() + this.getWidth() - 6;
                int trackHeight = rows * 18 - 4;
                int thumbHeight = Math.max(12, trackHeight * rows / styles.length);
                int maxScroll = styles.length - rows;
                int thumbY = listY + 3 + (trackHeight - thumbHeight) * this.scroll / Math.max(1, maxScroll);
                graphics.fill(trackX, listY + 3, trackX + 3, listY + 3 + trackHeight, ProductionMonitorThemeScreen.opaque(palette.outer()));
                graphics.fill(trackX, thumbY, trackX + 3, thumbY + thumbHeight, ProductionMonitorThemeScreen.opaque(palette.accentB()));
            }
            graphics.disableScissor();
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0) {
                return false;
            }
            if (mouseX >= this.getX() && mouseX < this.getX() + this.getWidth() && mouseY >= this.getY() && mouseY < this.getY() + this.getHeight()) {
                this.open = !this.open;
                if (this.open) {
                    this.ensureSelectedVisible();
                    this.openedNanos = GuiMotion.enabled() ? GuiMotion.now() : 0L;
                }
                return true;
            }
            if (this.open) {
                int row = (int)((mouseY - this.getY() - this.getHeight() - 1) / 18.0);
                ClientConfig.InterfaceStyle[] styles = ClientConfig.InterfaceStyle.selectableValues();
                int revealedHeight = (int)((this.visibleRows() * 18 + 2) * GuiMotion.easeOut(GuiMotion.progress(this.openedNanos, 140L)));
                if (mouseX >= this.getX() && mouseX < this.getX() + this.getWidth() && mouseY >= this.getY() + this.getHeight() && mouseY < this.getY() + this.getHeight() + revealedHeight && row >= 0 && row < this.visibleRows()) {
                    ProductionMonitorThemeScreen.this.themeDropdownScroll = this.scroll;
                    this.selection.accept(styles[this.scroll + row]);
                    this.open = false;
                    return true;
                }
                this.open = false;
            }
            return false;
        }

        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            int listTop = this.getY() + this.getHeight();
            int listBottom = listTop + this.visibleRows() * 18 + 2;
            boolean inside = mouseX >= this.getX() && mouseX < this.getX() + this.getWidth() && mouseY >= this.getY() && mouseY < listBottom;
            if (!this.open || !inside || scrollY == 0.0) {
                return false;
            }
            int max = Math.max(0, ClientConfig.InterfaceStyle.selectableValues().length - this.visibleRows());
            this.scroll = Math.max(0, Math.min(max, this.scroll + (scrollY < 0.0 ? 1 : -1)));
            ProductionMonitorThemeScreen.this.themeDropdownScroll = this.scroll;
            return true;
        }

        private void ensureSelectedVisible() {
            ClientConfig.InterfaceStyle[] styles = ClientConfig.InterfaceStyle.selectableValues();
            int selected = 0;
            for (int i = 0; i < styles.length; ++i) {
                if (styles[i] == this.current.get()) {
                    selected = i;
                    break;
                }
            }
            int rows = this.visibleRows();
            if (selected < this.scroll) {
                this.scroll = selected;
            } else if (selected >= this.scroll + rows) {
                this.scroll = selected - rows + 1;
            }
            this.scroll = Math.max(0, Math.min(styles.length - rows, this.scroll));
            ProductionMonitorThemeScreen.this.themeDropdownScroll = this.scroll;
        }

        private int visibleRows() {
            int available = ProductionMonitorThemeScreen.this.contentBottom - this.getY() - this.getHeight();
            return Math.min(MAX_VISIBLE, Math.max(1, Math.min(ClientConfig.InterfaceStyle.selectableValues().length, available / 18)));
        }

        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }

    private static enum HudSettingsSection {
        GENERAL,
        CONTENT,
        LAYOUT,
        APPEARANCE;
    }

    private static enum HudPreviewZoom {
        FIT("fit", 1.0),
        ONE("one", 1.0),
        ONE_HALF("one_half", 1.5),
        TWO("two", 2.0);

        private final String translationKey;
        private final double multiplier;

        HudPreviewZoom(String translationKey, double multiplier) {
            this.translationKey = translationKey;
            this.multiplier = multiplier;
        }

        HudPreviewZoom next() {
            HudPreviewZoom[] values = HudPreviewZoom.values();
            return values[(this.ordinal() + 1) % values.length];
        }
    }

    private static enum Category {
        GENERAL,
        INTERFACE,
        MAP,
        HUD,
        ANIMATIONS;

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
