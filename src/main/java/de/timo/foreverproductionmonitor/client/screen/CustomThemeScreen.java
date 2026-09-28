package de.timo.foreverproductionmonitor.client.screen;

import de.timo.foreverproductionmonitor.client.ClientConfig;
import de.timo.foreverproductionmonitor.client.ForeverProductionMonitorClient;
import java.util.Locale;
import java.util.function.DoubleConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Dedicated editor for the CUSTOM interface theme.
 * All values are client config only and the preview renders directly from the live config.
 */
public final class CustomThemeScreen extends Screen {
    private final Screen parent;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;
    private int controlsLeft;
    private int controlsWidth;
    private int previewLeft;
    private int previewWidth;
    private int contentTop;
    private int contentBottom;
    private int footerY;

    public CustomThemeScreen(Screen parent) {
        super(Component.translatable("screen.forever_production_monitor.custom_theme.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.panelWidth = Math.max(1, Math.min(840, this.width - 12));
        this.panelHeight = Math.max(1, Math.min(540, this.height - 12));
        this.left = (this.width - this.panelWidth) / 2;
        this.top = (this.height - this.panelHeight) / 2;
        this.contentTop = this.top + 42;
        this.footerY = this.top + this.panelHeight - 29;
        this.contentBottom = this.footerY - 12;

        int innerLeft = this.left + 18;
        int innerRight = this.left + this.panelWidth - 18;
        int innerWidth = Math.max(1, innerRight - innerLeft);
        this.controlsWidth = Math.max(180, Math.min(330, innerWidth * 42 / 100));
        this.controlsLeft = innerLeft;
        this.previewLeft = this.controlsLeft + this.controlsWidth + 12;
        this.previewWidth = Math.max(120, innerRight - this.previewLeft);

        int sliderY = this.contentTop + 18;
        int step = 24;
        int sliderWidth = this.controlsWidth;

        addSlider(0, sliderY, sliderWidth, "custom_theme.primary_hue",
                0.0, 359.0, ((Integer)ClientConfig.VALUES.interfaceCustomHue.get()).doubleValue(),
                value -> ClientConfig.VALUES.interfaceCustomHue.set((int)Math.round(value)), false);
        addSlider(1, sliderY + step, sliderWidth, "custom_theme.secondary_hue",
                0.0, 359.0, ((Integer)ClientConfig.VALUES.interfaceCustomSecondaryHue.get()).doubleValue(),
                value -> ClientConfig.VALUES.interfaceCustomSecondaryHue.set((int)Math.round(value)), false);
        addSlider(2, sliderY + step * 2, sliderWidth, "custom_theme.primary_saturation",
                0.0, 1.0, (Double)ClientConfig.VALUES.interfaceCustomPrimarySaturation.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceCustomPrimarySaturation).set(value), true);
        addSlider(3, sliderY + step * 3, sliderWidth, "custom_theme.secondary_saturation",
                0.0, 1.0, (Double)ClientConfig.VALUES.interfaceCustomSecondarySaturation.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceCustomSecondarySaturation).set(value), true);
        addSlider(4, sliderY + step * 4, sliderWidth, "custom_theme.surface_saturation",
                0.0, 0.75, (Double)ClientConfig.VALUES.interfaceCustomSurfaceSaturation.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceCustomSurfaceSaturation).set(value), true);
        addSlider(5, sliderY + step * 5, sliderWidth, "custom_theme.brightness",
                0.2, 0.9, (Double)ClientConfig.VALUES.interfaceCustomBrightness.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceCustomBrightness).set(value), true);
        addSlider(6, sliderY + step * 6, sliderWidth, "custom_theme.contrast",
                0.65, 1.45, (Double)ClientConfig.VALUES.interfaceCustomContrast.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceCustomContrast).set(value), true);
        addSlider(7, sliderY + step * 7, sliderWidth, "custom_theme.header_strength",
                0.65, 1.45, (Double)ClientConfig.VALUES.interfaceCustomHeaderStrength.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceCustomHeaderStrength).set(value), true);
        addSlider(8, sliderY + step * 8, sliderWidth, "custom_theme.border_strength",
                0.5, 1.5, (Double)ClientConfig.VALUES.interfaceCustomBorderStrength.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceCustomBorderStrength).set(value), true);
        addSlider(9, sliderY + step * 9, sliderWidth, "custom_theme.accent_brightness",
                0.65, 1.25, (Double)ClientConfig.VALUES.interfaceCustomAccentBrightness.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceCustomAccentBrightness).set(value), true);
        addSlider(10, sliderY + step * 10, sliderWidth, "custom_theme.opacity",
                0.55, 1.0, (Double)ClientConfig.VALUES.interfaceOpacity.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceOpacity).set(value), true);

        int buttonGap = 8;
        int buttonWidth = Math.max(1, (innerWidth - buttonGap) / 2);
        this.addRenderableWidget(ForeverButton.create(
                Component.translatable("config.forever_production_monitor.reset"),
                button -> {
                    resetCustomValues();
                    this.rebuildWidgets();
                },
                ForeverButton.Style.THEMED,
                innerLeft, this.footerY, buttonWidth, 20));
        this.addRenderableWidget(ForeverButton.create(
                Component.translatable("gui.done"),
                button -> this.onClose(),
                ForeverButton.Style.THEMED_ACTIVE,
                innerLeft + buttonWidth + buttonGap, this.footerY,
                Math.max(1, innerRight - (innerLeft + buttonWidth + buttonGap)), 20));
    }

    private void addSlider(int index, int y, int width, String key,
                           double min, double max, double value,
                           DoubleConsumer setter, boolean decimal) {
        this.addRenderableWidget(new CustomSlider(
                this.controlsLeft, y, width, key, min, max, value, setter, decimal));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ThemeInteractionState.beginFrame(mouseX, mouseY);
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        graphics.fill(0, 0, this.width, this.height, palette.backdrop());
        InterfaceTheme.drawPanel(graphics, this.left, this.top, this.panelWidth, this.panelHeight,
                ClientConfig.InterfaceStyle.CUSTOM, palette);
        graphics.drawCenteredString(this.font, this.title,
                this.left + this.panelWidth / 2, this.top + 13, palette.text());

        graphics.drawString(this.font,
                Component.translatable("screen.forever_production_monitor.custom_theme.controls"),
                this.controlsLeft, this.contentTop, palette.text(), false);
        graphics.drawString(this.font,
                Component.translatable("screen.forever_production_monitor.custom_theme.preview"),
                this.previewLeft, this.contentTop, palette.text(), false);

        for (Renderable renderable : this.renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTick);
        }

        drawPreview(graphics, palette);
    }

    private void drawPreview(GuiGraphics graphics, InterfaceTheme.Palette palette) {
        int previewTop = this.contentTop + 18;
        int previewBottom = this.contentBottom;
        int previewHeight = Math.max(100, previewBottom - previewTop);
        if (this.previewWidth < 120 || previewHeight < 100) {
            return;
        }

        InterfaceTheme.drawPanel(graphics, this.previewLeft, previewTop,
                this.previewWidth, previewHeight, ClientConfig.InterfaceStyle.CUSTOM, palette);

        graphics.drawCenteredString(this.font,
                Component.translatable("screen.forever_production_monitor.custom_theme.preview_title"),
                this.previewLeft + this.previewWidth / 2, previewTop + 11, palette.text());

        int pad = 12;
        int tabY = previewTop + 39;
        int usable = Math.max(1, this.previewWidth - pad * 2);
        int tabGap = 4;
        int tabWidth = Math.max(34, (usable - tabGap * 2) / 3);
        String[] tabs = {"Dashboard", "Production", "Storage"};
        for (int i = 0; i < 3; ++i) {
            int x = this.previewLeft + pad + i * (tabWidth + tabGap);
            int right = i == 2 ? this.previewLeft + this.previewWidth - pad : x + tabWidth;
            graphics.fill(x, tabY, right, tabY + 20, palette.border());
            graphics.fill(x + 1, tabY + 1, right - 1, tabY + 19,
                    i == 0 ? palette.header() : palette.summary());
            int textX = x + Math.max(3, (right - x - this.font.width(tabs[i])) / 2);
            graphics.drawString(this.font, tabs[i], textX, tabY + 6,
                    i == 0 ? palette.accentB() : palette.text(), false);
        }

        int tableTop = tabY + 31;
        int tableBottom = Math.min(previewBottom - 48, tableTop + 112);
        if (tableBottom > tableTop + 54) {
            int tableLeft = this.previewLeft + pad;
            int tableRight = this.previewLeft + this.previewWidth - pad;
            graphics.fill(tableLeft, tableTop, tableRight, tableBottom, palette.tableOuter());
            graphics.fill(tableLeft + 1, tableTop + 1, tableRight - 1, tableTop + 18, palette.tableHeader());
            InterfaceTheme.drawTableDecoration(graphics, tableLeft, tableTop,
                    tableRight - tableLeft, tableBottom - tableTop);
            graphics.drawString(this.font, "Resource", tableLeft + 8, tableTop + 6, palette.text(), false);
            String rateHeader = "Rate";
            graphics.drawString(this.font, rateHeader,
                    tableRight - 8 - this.font.width(rateHeader), tableTop + 6, palette.muted(), false);

            String[] names = {"Iron Ingot", "Certus Quartz", "FE Energy"};
            String[] values = {"+3.84k/m", "+660/m", "+6.25k FE/m"};
            for (int i = 0; i < 3; ++i) {
                int rowY = tableTop + 19 + i * 24;
                if (rowY + 23 >= tableBottom) break;
                int rowColor = i % 2 == 0 ? palette.rowEven() : palette.rowOdd();
                graphics.fill(tableLeft + 1, rowY, tableRight - 1, rowY + 23, rowColor);
                graphics.fill(tableLeft + 7, rowY + 7, tableLeft + 15, rowY + 15,
                        i == 1 ? palette.accentB() : palette.accentA());
                graphics.drawString(this.font, names[i], tableLeft + 21, rowY + 7, palette.text(), false);
                graphics.drawString(this.font, values[i],
                        tableRight - 8 - this.font.width(values[i]), rowY + 7,
                        i == 1 ? palette.accentB() : palette.accentA(), false);
            }
        }

        int swatchY = previewBottom - 34;
        int swatchLeft = this.previewLeft + pad;
        graphics.drawString(this.font,
                Component.translatable("screen.forever_production_monitor.custom_theme.accents"),
                swatchLeft, swatchY - 12, palette.muted(), false);
        graphics.fill(swatchLeft, swatchY, swatchLeft + 42, swatchY + 14, palette.accentA());
        graphics.fill(swatchLeft + 48, swatchY, swatchLeft + 90, swatchY + 14, palette.accentB());
        graphics.fill(swatchLeft + 96, swatchY,
                Math.min(this.previewLeft + this.previewWidth - pad, swatchLeft + 138),
                swatchY + 14, palette.border());
    }

    private static void resetCustomValues() {
        ClientConfig.VALUES.interfaceCustomHue.set((Integer)ClientConfig.VALUES.interfaceCustomHue.getDefault());
        ClientConfig.VALUES.interfaceCustomSecondaryHue.set((Integer)ClientConfig.VALUES.interfaceCustomSecondaryHue.getDefault());
        ClientConfig.VALUES.interfaceCustomBrightness.set((Double)ClientConfig.VALUES.interfaceCustomBrightness.getDefault());
        ClientConfig.VALUES.interfaceCustomPrimarySaturation.set((Double)ClientConfig.VALUES.interfaceCustomPrimarySaturation.getDefault());
        ClientConfig.VALUES.interfaceCustomSecondarySaturation.set((Double)ClientConfig.VALUES.interfaceCustomSecondarySaturation.getDefault());
        ClientConfig.VALUES.interfaceCustomSurfaceSaturation.set((Double)ClientConfig.VALUES.interfaceCustomSurfaceSaturation.getDefault());
        ClientConfig.VALUES.interfaceCustomContrast.set((Double)ClientConfig.VALUES.interfaceCustomContrast.getDefault());
        ClientConfig.VALUES.interfaceCustomHeaderStrength.set((Double)ClientConfig.VALUES.interfaceCustomHeaderStrength.getDefault());
        ClientConfig.VALUES.interfaceCustomBorderStrength.set((Double)ClientConfig.VALUES.interfaceCustomBorderStrength.getDefault());
        ClientConfig.VALUES.interfaceCustomAccentBrightness.set((Double)ClientConfig.VALUES.interfaceCustomAccentBrightness.getDefault());
        ClientConfig.VALUES.interfaceOpacity.set((Double)ClientConfig.VALUES.interfaceOpacity.getDefault());
        ForeverProductionMonitorClient.refreshHudNow();
    }

    private static void saveCustomValues() {
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
        ClientConfig.VALUES.interfaceOpacity.save();
    }

    @Override
    public void onClose() {
        saveCustomValues();
        ForeverProductionMonitorClient.refreshHudNow();
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private final class CustomSlider extends AbstractSliderButton {
        private final String key;
        private final double min;
        private final double max;
        private final DoubleConsumer setter;
        private final boolean decimal;

        CustomSlider(int x, int y, int width, String key,
                     double min, double max, double initial,
                     DoubleConsumer setter, boolean decimal) {
            super(x, y, width, 20, Component.empty(),
                    Math.max(0.0, Math.min(1.0, (initial - min) / (max - min))));
            this.key = key;
            this.min = min;
            this.max = max;
            this.setter = setter;
            this.decimal = decimal;
            this.updateMessage();
        }

        private double actual() {
            return this.min + this.value * (this.max - this.min);
        }

        @Override
        protected void updateMessage() {
            String valueText = this.decimal
                    ? String.format(Locale.ROOT, "%.2f", this.actual())
                    : Integer.toString((int)Math.round(this.actual()));
            this.setMessage(Component.translatable(
                    "screen.forever_production_monitor." + this.key, valueText));
        }

        @Override
        protected void applyValue() {
            this.setter.accept(this.actual());
            ForeverProductionMonitorClient.refreshHudNow();
        }
    }
}
