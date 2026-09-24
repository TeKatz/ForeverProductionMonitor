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
 */
package de.timo.foreverproductionmonitor.client.screen;

import de.timo.foreverproductionmonitor.client.ClientConfig;
import de.timo.foreverproductionmonitor.client.ForeverProductionMonitorClient;
import de.timo.foreverproductionmonitor.client.ProductionHud;
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

public final class ProductionMonitorConfigScreen
extends Screen {
    private final Screen parent;

    public ProductionMonitorConfigScreen(Screen screen) {
        super((Component)Component.translatable((String)"config.forever_production_monitor.title"));
        this.parent = screen;
    }

    protected void init() {
        int n = Math.min(430, this.width - 24);
        int n2 = (this.width - n) / 2;
        int n3 = this.isCustomFrame() ? 30 : 0;
        int n4 = this.panelTop(n3);
        int n5 = (n - 46) / 2;
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)this.enabledLabel(), button -> {
            ClientConfig.VALUES.hudEnabled.set((Object)((Boolean)ClientConfig.VALUES.hudEnabled.get() == false ? 1 : 0));
            button.setMessage(this.enabledLabel());
            ForeverProductionMonitorClient.refreshHudNow();
        }).bounds(n2 + 18, n4 + 16, n5, 20).build());
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)this.anchorLabel(), button -> {
            ClientConfig.HudAnchor[] hudAnchorArray = ClientConfig.HudAnchor.values();
            ClientConfig.HudAnchor hudAnchor = hudAnchorArray[(((ClientConfig.HudAnchor)((Object)((Object)ClientConfig.VALUES.hudAnchor.get()))).ordinal() + 1) % hudAnchorArray.length];
            ClientConfig.VALUES.hudAnchor.set((Object)hudAnchor);
            button.setMessage(this.anchorLabel());
        }).bounds(n2 + 28 + n5, n4 + 16, n5, 20).build());
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)this.modeLabel(), button -> {
            MonitorNetwork.HudMode hudMode = ((MonitorNetwork.HudMode)((Object)((Object)ClientConfig.VALUES.hudMode.get()))).next();
            ClientConfig.VALUES.hudMode.set((Object)hudMode);
            button.setMessage(this.modeLabel());
            ForeverProductionMonitorClient.refreshHudNow();
        }).bounds(n2 + 18, n4 + 46, n - 36, 20).build());
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)this.frameLabel(), button -> {
            ClientConfig.VALUES.hudFrameStyle.set((Object)((ClientConfig.HudFrameStyle)((Object)((Object)ClientConfig.VALUES.hudFrameStyle.get()))).next());
            this.rebuildWidgets();
        }).bounds(n2 + 18, n4 + 76, n5, 20).build());
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)this.rateUnitLabel(), button -> {
            ClientConfig.VALUES.rateUnit.set((Object)((ClientConfig.RateUnit)((Object)((Object)ClientConfig.VALUES.rateUnit.get()))).next());
            button.setMessage(this.rateUnitLabel());
        }).bounds(n2 + 28 + n5, n4 + 76, n5, 20).build());
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)this.refreshLabel(), button -> {
            ClientConfig.VALUES.refreshInterval.set((Object)((ClientConfig.RefreshInterval)((Object)((Object)ClientConfig.VALUES.refreshInterval.get()))).next());
            button.setMessage(this.refreshLabel());
            ForeverProductionMonitorClient.refreshHudNow();
        }).bounds(n2 + 18, n4 + 106, n - 36, 20).build());
        if (this.isCustomFrame()) {
            this.addRenderableWidget((GuiEventListener)new ConfigSlider(n2 + 18, n4 + 136, n - 36, "config.forever_production_monitor.custom_hue", 0.0, 359.0, ((Integer)ClientConfig.VALUES.hudCustomHue.get()).intValue(), d -> ClientConfig.VALUES.hudCustomHue.set((Object)((int)Math.round(d))), false));
        }
        this.addRenderableWidget((GuiEventListener)new ConfigSlider(n2 + 18, n4 + 141 + n3, n - 36, "config.forever_production_monitor.scale", 0.5, 2.0, (Double)ClientConfig.VALUES.hudScale.get(), d -> ClientConfig.VALUES.hudScale.set((Object)d), true));
        this.addRenderableWidget((GuiEventListener)new ConfigSlider(n2 + 18, n4 + 171 + n3, n - 36, "config.forever_production_monitor.opacity", 0.0, 1.0, (Double)ClientConfig.VALUES.hudOpacity.get(), d -> ClientConfig.VALUES.hudOpacity.set((Object)d), true));
        this.addRenderableWidget((GuiEventListener)new ConfigSlider(n2 + 18, n4 + 201 + n3, n5, "config.forever_production_monitor.x_offset", 0.0, 200.0, ((Integer)ClientConfig.VALUES.hudXOffset.get()).intValue(), d -> ClientConfig.VALUES.hudXOffset.set((Object)((int)Math.round(d))), false));
        this.addRenderableWidget((GuiEventListener)new ConfigSlider(n2 + 28 + n5, n4 + 201 + n3, n5, "config.forever_production_monitor.y_offset", 0.0, 200.0, ((Integer)ClientConfig.VALUES.hudYOffset.get()).intValue(), d -> ClientConfig.VALUES.hudYOffset.set((Object)((int)Math.round(d))), false));
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)Component.translatable((String)"config.forever_production_monitor.reset"), button -> {
            this.resetDefaults();
            this.rebuildWidgets();
        }).bounds(n2 + 18, n4 + 242 + n3, n5, 20).build());
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)Component.translatable((String)"gui.done"), button -> this.onClose()).bounds(n2 + 28 + n5, n4 + 242 + n3, n5, 20).build());
    }

    private Component enabledLabel() {
        return Component.translatable((String)"config.forever_production_monitor.enabled", (Object[])new Object[]{Component.translatable((String)((Boolean)ClientConfig.VALUES.hudEnabled.get() != false ? "options.on" : "options.off"))});
    }

    private Component anchorLabel() {
        return Component.translatable((String)"config.forever_production_monitor.anchor", (Object[])new Object[]{Component.translatable((String)("config.forever_production_monitor.anchor." + ((ClientConfig.HudAnchor)((Object)ClientConfig.VALUES.hudAnchor.get())).name().toLowerCase(Locale.ROOT)))});
    }

    private Component modeLabel() {
        return Component.translatable((String)"config.forever_production_monitor.mode", (Object[])new Object[]{Component.translatable((String)("hud.forever_production_monitor.mode." + ((MonitorNetwork.HudMode)((Object)ClientConfig.VALUES.hudMode.get())).name().toLowerCase(Locale.ROOT)))});
    }

    private Component frameLabel() {
        ClientConfig.HudFrameStyle hudFrameStyle = (ClientConfig.HudFrameStyle)((Object)ClientConfig.VALUES.hudFrameStyle.get());
        return Component.translatable((String)"config.forever_production_monitor.frame", (Object[])new Object[]{Component.translatable((String)("config.forever_production_monitor.frame." + hudFrameStyle.name().toLowerCase(Locale.ROOT)))});
    }

    private Component rateUnitLabel() {
        ClientConfig.RateUnit rateUnit = (ClientConfig.RateUnit)((Object)ClientConfig.VALUES.rateUnit.get());
        return Component.translatable((String)"config.forever_production_monitor.rate_unit", (Object[])new Object[]{rateUnit.suffix()});
    }

    private Component refreshLabel() {
        ClientConfig.RefreshInterval refreshInterval = (ClientConfig.RefreshInterval)((Object)ClientConfig.VALUES.refreshInterval.get());
        return Component.translatable((String)"config.forever_production_monitor.refresh", (Object[])new Object[]{Component.translatable((String)("config.forever_production_monitor.refresh." + refreshInterval.name().toLowerCase(Locale.ROOT)))});
    }

    private boolean isCustomFrame() {
        return ClientConfig.VALUES.hudFrameStyle.get() == ClientConfig.HudFrameStyle.CUSTOM;
    }

    private int panelTop(int n) {
        return Math.max(8, (this.height - 275 - n) / 2);
    }

    private void resetDefaults() {
        ClientConfig.VALUES.hudEnabled.set((Object)((Boolean)ClientConfig.VALUES.hudEnabled.getDefault()));
        ClientConfig.VALUES.hudAnchor.set((Object)((ClientConfig.HudAnchor)((Object)ClientConfig.VALUES.hudAnchor.getDefault())));
        ClientConfig.VALUES.hudXOffset.set((Object)((Integer)ClientConfig.VALUES.hudXOffset.getDefault()));
        ClientConfig.VALUES.hudYOffset.set((Object)((Integer)ClientConfig.VALUES.hudYOffset.getDefault()));
        ClientConfig.VALUES.hudScale.set((Object)((Double)ClientConfig.VALUES.hudScale.getDefault()));
        ClientConfig.VALUES.hudOpacity.set((Object)((Double)ClientConfig.VALUES.hudOpacity.getDefault()));
        ClientConfig.VALUES.hudMode.set((Object)((MonitorNetwork.HudMode)((Object)ClientConfig.VALUES.hudMode.getDefault())));
        ClientConfig.VALUES.hudFrameStyle.set((Object)((ClientConfig.HudFrameStyle)((Object)ClientConfig.VALUES.hudFrameStyle.getDefault())));
        ClientConfig.VALUES.hudCustomHue.set((Object)((Integer)ClientConfig.VALUES.hudCustomHue.getDefault()));
        ClientConfig.VALUES.rateUnit.set((Object)((ClientConfig.RateUnit)((Object)ClientConfig.VALUES.rateUnit.getDefault())));
        ClientConfig.VALUES.refreshInterval.set((Object)((ClientConfig.RefreshInterval)((Object)ClientConfig.VALUES.refreshInterval.getDefault())));
        ForeverProductionMonitorClient.refreshHudNow();
    }

    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        int n3 = Math.min(430, this.width - 24);
        int n4 = (this.width - n3) / 2;
        int n5 = this.isCustomFrame() ? 30 : 0;
        int n6 = this.panelTop(n5);
        int n7 = 275 + n5;
        guiGraphics.fill(n4 - 1, n6 - 1, n4 + n3 + 1, n6 + n7 + 1, -16184560);
        guiGraphics.fill(n4, n6, n4 + n3, n6 + n7, -233103576);
        guiGraphics.fill(n4, n6, n4 + n3 * 3 / 5, n6 + 2, -2582978);
        guiGraphics.fill(n4 + n3 * 3 / 5, n6, n4 + n3, n6 + 2, -8627288);
        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, n, n2, f);
        }
        ProductionHud.renderPreview(guiGraphics);
    }

    public void onClose() {
        ClientConfig.VALUES.hudEnabled.save();
        ForeverProductionMonitorClient.refreshHudNow();
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    public boolean isPauseScreen() {
        return false;
    }

    private static final class ConfigSlider
    extends AbstractSliderButton {
        private final String translationKey;
        private final double min;
        private final double max;
        private final DoubleConsumer setter;
        private final boolean decimal;

        private ConfigSlider(int n, int n2, int n3, String string, double d, double d2, double d3, DoubleConsumer doubleConsumer, boolean bl) {
            super(n, n2, n3, 20, (Component)Component.empty(), (d3 - d) / (d2 - d));
            this.translationKey = string;
            this.min = d;
            this.max = d2;
            this.setter = doubleConsumer;
            this.decimal = bl;
            this.updateMessage();
        }

        private double actualValue() {
            return this.min + this.value * (this.max - this.min);
        }

        protected void updateMessage() {
            double d = this.actualValue();
            String string = this.decimal ? String.format(Locale.ROOT, "%.2f", d) : Integer.toString((int)Math.round(d));
            this.setMessage((Component)Component.translatable((String)this.translationKey, (Object[])new Object[]{string}));
        }

        protected void applyValue() {
            this.setter.accept(this.actualValue());
        }
    }
}

