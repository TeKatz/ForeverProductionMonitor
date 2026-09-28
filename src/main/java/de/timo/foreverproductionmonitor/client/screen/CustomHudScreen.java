package de.timo.foreverproductionmonitor.client.screen;

import de.timo.foreverproductionmonitor.client.ClientConfig;
import de.timo.foreverproductionmonitor.client.ForeverProductionMonitorClient;
import de.timo.foreverproductionmonitor.client.ProductionHud;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Dedicated appearance editor for the CUSTOM Mini HUD.
 * Theme matching still selects the CUSTOM family, while these settings keep the compact HUD
 * independently readable instead of mirroring full-screen surface colours blindly.
 */
public final class CustomHudScreen extends Screen {
    private final Screen parent;
    private final List<AbstractWidget> controlWidgets = new ArrayList<>();
    private Category category = Category.COLORS;
    private EnumDropdown<ClientConfig.CustomHudDecorationStyle> decorationDropdown;
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

    public CustomHudScreen(Screen parent) {
        super(Component.translatable("screen.forever_production_monitor.custom_hud.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.decorationDropdown = null;
        this.controlWidgets.clear();
        this.panelWidth = Math.max(1, Math.min(900, this.width - 12));
        this.panelHeight = Math.max(1, Math.min(520, this.height - 12));
        this.left = (this.width - this.panelWidth) / 2;
        this.top = (this.height - this.panelHeight) / 2;
        this.contentTop = this.top + 42;
        this.footerY = this.top + this.panelHeight - 29;
        this.contentBottom = this.footerY - 12;

        int innerLeft = this.left + 18;
        int innerRight = this.left + this.panelWidth - 18;
        int innerWidth = Math.max(1, innerRight - innerLeft);
        this.controlsWidth = Math.max(230, Math.min(350, innerWidth * 42 / 100));
        this.controlsLeft = innerLeft;
        this.previewLeft = this.controlsLeft + this.controlsWidth + 12;
        this.previewWidth = Math.max(120, innerRight - this.previewLeft);

        buildCategoryTabs();
        if (this.category == Category.COLORS) {
            buildColors();
        } else {
            buildStructure();
        }

        int gap = 8;
        int buttonWidth = Math.max(1, (innerWidth - gap) / 2);
        this.addRenderableWidget(ForeverButton.create(
                Component.translatable("config.forever_production_monitor.reset"),
                button -> {
                    resetCustomHudValues();
                    this.rebuildWidgets();
                },
                ForeverButton.Style.THEMED,
                innerLeft, this.footerY, buttonWidth, 20));
        this.addRenderableWidget(ForeverButton.create(
                Component.translatable("gui.done"),
                button -> this.onClose(),
                ForeverButton.Style.THEMED_ACTIVE,
                innerLeft + buttonWidth + gap, this.footerY,
                Math.max(1, innerRight - innerLeft - buttonWidth - gap), 20));
    }

    private void buildCategoryTabs() {
        int gap = 4;
        int width = Math.max(1, (this.controlsWidth - gap) / 2);
        Category[] values = Category.values();
        for (int i = 0; i < values.length; ++i) {
            Category value = values[i];
            int x = this.controlsLeft + i * (width + gap);
            int w = i == values.length - 1
                    ? this.controlsLeft + this.controlsWidth - x : width;
            this.addRenderableWidget(ForeverButton.create(
                    categoryName(value),
                    button -> {
                        this.category = value;
                        this.rebuildWidgets();
                    },
                    value == this.category ? ForeverButton.Style.THEMED_ACTIVE : ForeverButton.Style.THEMED,
                    x, this.contentTop, w, 20).setRole(ForeverButton.Role.TAB));
        }
    }

    private int controlsY() {
        return this.contentTop + 40;
    }

    private void buildColors() {
        int y = controlsY();
        int step = 24;
        addSlider(y, "custom_hud.primary_hue", 0.0, 359.0,
                ((Integer)ClientConfig.VALUES.hudCustomHue.get()).doubleValue(),
                value -> ClientConfig.VALUES.hudCustomHue.set((int)Math.round(value)), false);
        addSlider(y + step, "custom_hud.secondary_hue", 0.0, 359.0,
                ((Integer)ClientConfig.VALUES.hudCustomSecondaryHue.get()).doubleValue(),
                value -> ClientConfig.VALUES.hudCustomSecondaryHue.set((int)Math.round(value)), false);
        addSlider(y + step * 2, "custom_hud.primary_saturation", 0.0, 1.0,
                (Double)ClientConfig.VALUES.hudCustomPrimarySaturation.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.hudCustomPrimarySaturation).set(value), true);
        addSlider(y + step * 3, "custom_hud.secondary_saturation", 0.0, 1.0,
                (Double)ClientConfig.VALUES.hudCustomSecondarySaturation.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.hudCustomSecondarySaturation).set(value), true);
        addSlider(y + step * 4, "custom_hud.surface_saturation", 0.0, 0.75,
                (Double)ClientConfig.VALUES.hudCustomSurfaceSaturation.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.hudCustomSurfaceSaturation).set(value), true);
        addSlider(y + step * 5, "custom_hud.brightness", 0.15, 0.85,
                (Double)ClientConfig.VALUES.hudCustomBrightness.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.hudCustomBrightness).set(value), true);
        addSlider(y + step * 6, "custom_hud.contrast", 0.65, 1.45,
                (Double)ClientConfig.VALUES.hudCustomContrast.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.hudCustomContrast).set(value), true);
        addSlider(y + step * 7, "custom_hud.accent_brightness", 0.65, 1.25,
                (Double)ClientConfig.VALUES.hudCustomAccentBrightness.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.hudCustomAccentBrightness).set(value), true);
    }

    private void buildStructure() {
        int y = controlsY();
        int step = 24;
        addSlider(y, "custom_hud.header_strength", 0.65, 1.45,
                (Double)ClientConfig.VALUES.hudCustomHeaderStrength.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.hudCustomHeaderStrength).set(value), true);
        addSlider(y + step, "custom_hud.row_contrast", 0.65, 1.45,
                (Double)ClientConfig.VALUES.hudCustomRowContrast.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.hudCustomRowContrast).set(value), true);
        addSlider(y + step * 2, "custom_hud.border_strength", 0.5, 1.5,
                (Double)ClientConfig.VALUES.hudCustomBorderStrength.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.hudCustomBorderStrength).set(value), true);

        this.decorationDropdown = new EnumDropdown<>(
                this.controlsLeft, y + step * 3, this.controlsWidth, 20,
                "custom_hud.decoration",
                ClientConfig.CustomHudDecorationStyle.values(),
                () -> (ClientConfig.CustomHudDecorationStyle)ClientConfig.VALUES.hudCustomDecorationStyle.get(),
                value -> ClientConfig.VALUES.hudCustomDecorationStyle.set(value),
                value -> Component.translatable(
                        "screen.forever_production_monitor.custom_hud.decoration."
                                + value.name().toLowerCase(Locale.ROOT)));
        this.addWidget(this.decorationDropdown);

        addSlider(y + step * 4 + 4, "custom_hud.opacity", 0.0, 1.0,
                (Double)ClientConfig.VALUES.hudOpacity.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.hudOpacity).set(value), true);
        addSlider(y + step * 5 + 4, "custom_hud.animation_intensity", 0.0, 1.0,
                (Double)ClientConfig.VALUES.hudAnimationIntensity.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.hudAnimationIntensity).set(value), true);
    }

    private void addSlider(int y, String key, double min, double max, double value,
                           DoubleConsumer setter, boolean decimal) {
        HudSlider slider = new HudSlider(
                this.controlsLeft, y, this.controlsWidth, key, min, max, value, setter, decimal);
        this.addWidget(slider);
        this.controlWidgets.add(slider);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ThemeInteractionState.beginFrame(mouseX, mouseY);
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        graphics.fill(0, 0, this.width, this.height, palette.backdrop());
        InterfaceTheme.drawPanel(graphics, this.left, this.top, this.panelWidth, this.panelHeight,
                (ClientConfig.InterfaceStyle)ClientConfig.VALUES.interfaceStyle.get(), palette);
        graphics.drawCenteredString(this.font, this.title,
                this.left + this.panelWidth / 2, this.top + 13, palette.text());

        graphics.drawString(this.font, categoryName(this.category),
                this.controlsLeft, this.contentTop + 24, palette.text(), false);
        graphics.drawString(this.font,
                Component.translatable("screen.forever_production_monitor.custom_hud.preview"),
                this.previewLeft, this.contentTop, palette.text(), false);

        for (Renderable renderable : this.renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTick);
        }

        updateControlVisibility();
        for (AbstractWidget widget : this.controlWidgets) {
            if (widget.visible) {
                widget.render(graphics, mouseX, mouseY, partialTick);
            }
        }

        int previewTop = this.contentTop + 18;
        int previewHeight = Math.max(100, this.contentBottom - previewTop);
        graphics.fill(this.previewLeft, previewTop,
                this.previewLeft + this.previewWidth, previewTop + previewHeight, opaque(palette.tableOuter()));
        graphics.renderOutline(this.previewLeft, previewTop, this.previewWidth, previewHeight,
                opaque(palette.border()));
        graphics.drawCenteredString(this.font,
                Component.translatable("screen.forever_production_monitor.custom_hud.preview_title"),
                this.previewLeft + this.previewWidth / 2, previewTop + 10, palette.text());

        ProductionHud.renderPreview(graphics,
                this.previewLeft + 10, previewTop + 28,
                this.previewLeft + this.previewWidth - 10, previewTop + previewHeight - 10);

        if (this.decorationDropdown != null) {
            graphics.pose().pushPose();
            graphics.pose().translate(0.0f, 0.0f, 400.0f);
            this.decorationDropdown.renderOverlay(graphics, mouseX, mouseY);
            graphics.pose().popPose();
        }
    }

    private void updateControlVisibility() {
        if (this.decorationDropdown == null || !this.decorationDropdown.isOpen()) {
            for (AbstractWidget widget : this.controlWidgets) {
                widget.visible = true;
            }
            return;
        }

        int overlayTop = this.decorationDropdown.getY() + this.decorationDropdown.getHeight() + 1;
        int overlayBottom = this.decorationDropdown.overlayBottom();
        for (AbstractWidget widget : this.controlWidgets) {
            boolean covered = widget != this.decorationDropdown
                    && widget.getX() < this.decorationDropdown.getX() + this.decorationDropdown.getWidth()
                    && widget.getX() + widget.getWidth() > this.decorationDropdown.getX()
                    && widget.getY() < overlayBottom
                    && widget.getY() + widget.getHeight() > overlayTop;
            widget.visible = !covered;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.decorationDropdown != null
                && this.decorationDropdown.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static void resetCustomHudValues() {
        ClientConfig.VALUES.hudCustomHue.set((Integer)ClientConfig.VALUES.hudCustomHue.getDefault());
        ClientConfig.VALUES.hudCustomSecondaryHue.set((Integer)ClientConfig.VALUES.hudCustomSecondaryHue.getDefault());
        ClientConfig.VALUES.hudCustomPrimarySaturation.set((Double)ClientConfig.VALUES.hudCustomPrimarySaturation.getDefault());
        ClientConfig.VALUES.hudCustomSecondarySaturation.set((Double)ClientConfig.VALUES.hudCustomSecondarySaturation.getDefault());
        ClientConfig.VALUES.hudCustomSurfaceSaturation.set((Double)ClientConfig.VALUES.hudCustomSurfaceSaturation.getDefault());
        ClientConfig.VALUES.hudCustomBrightness.set((Double)ClientConfig.VALUES.hudCustomBrightness.getDefault());
        ClientConfig.VALUES.hudCustomContrast.set((Double)ClientConfig.VALUES.hudCustomContrast.getDefault());
        ClientConfig.VALUES.hudCustomHeaderStrength.set((Double)ClientConfig.VALUES.hudCustomHeaderStrength.getDefault());
        ClientConfig.VALUES.hudCustomRowContrast.set((Double)ClientConfig.VALUES.hudCustomRowContrast.getDefault());
        ClientConfig.VALUES.hudCustomBorderStrength.set((Double)ClientConfig.VALUES.hudCustomBorderStrength.getDefault());
        ClientConfig.VALUES.hudCustomAccentBrightness.set((Double)ClientConfig.VALUES.hudCustomAccentBrightness.getDefault());
        ClientConfig.VALUES.hudCustomDecorationStyle.set(
                (ClientConfig.CustomHudDecorationStyle)ClientConfig.VALUES.hudCustomDecorationStyle.getDefault());
        ForeverProductionMonitorClient.refreshHudNow();
    }

    private static void saveCustomHudValues() {
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
        ClientConfig.VALUES.hudOpacity.save();
        ClientConfig.VALUES.hudAnimationIntensity.save();
    }

    @Override
    public void onClose() {
        saveCustomHudValues();
        ForeverProductionMonitorClient.refreshHudNow();
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static Component categoryName(Category category) {
        return Component.translatable("screen.forever_production_monitor.custom_hud.category."
                + category.name().toLowerCase(Locale.ROOT));
    }

    private static int opaque(int color) {
        return 0xFF000000 | color & 0xFFFFFF;
    }

    private enum Category {
        COLORS,
        STRUCTURE
    }

    private final class HudSlider extends AbstractSliderButton {
        private final String key;
        private final double min;
        private final double max;
        private final DoubleConsumer setter;
        private final boolean decimal;

        HudSlider(int x, int y, int width, String key, double min, double max, double initial,
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
            String value = this.decimal
                    ? String.format(Locale.ROOT, "%.2f", this.actual())
                    : Integer.toString((int)Math.round(this.actual()));
            this.setMessage(Component.translatable(
                    "screen.forever_production_monitor." + this.key, value));
        }

        @Override
        protected void applyValue() {
            this.setter.accept(this.actual());
            ForeverProductionMonitorClient.refreshHudNow();
        }
    }

    private final class EnumDropdown<T extends Enum<T>> extends AbstractWidget {
        private static final int ROW_HEIGHT = 18;
        private final String key;
        private final T[] values;
        private final Supplier<T> current;
        private final Consumer<T> selection;
        private final Function<T, Component> label;
        private boolean open;

        EnumDropdown(int x, int y, int width, int height, String key, T[] values,
                     Supplier<T> current, Consumer<T> selection, Function<T, Component> label) {
            super(x, y, width, height, Component.empty());
            this.key = key;
            this.values = values;
            this.current = current;
            this.selection = selection;
            this.label = label;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            InterfaceTheme.Palette palette = InterfaceTheme.current();
            graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(),
                    this.getY() + this.getHeight(), opaque(palette.border()));
            graphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.getWidth() - 1,
                    this.getY() + this.getHeight() - 1, opaque(palette.summary()));
            Component title = Component.translatable(
                    "screen.forever_production_monitor." + this.key,
                    this.label.apply(this.current.get()).getString());
            graphics.drawString(CustomHudScreen.this.font,
                    CustomHudScreen.this.font.plainSubstrByWidth(title.getString(), this.getWidth() - 20),
                    this.getX() + 6, this.getY() + 6, palette.text(), false);
            graphics.drawString(CustomHudScreen.this.font, this.open ? "▲" : "▼",
                    this.getX() + this.getWidth() - 12, this.getY() + 6, palette.accentB(), false);
        }

        void renderOverlay(GuiGraphics graphics, int mouseX, int mouseY) {
            if (!this.open) return;
            InterfaceTheme.Palette palette = InterfaceTheme.current();
            int listY = this.getY() + this.getHeight() + 1;
            int height = this.values.length * ROW_HEIGHT + 2;
            graphics.fill(this.getX(), listY, this.getX() + this.getWidth(), listY + height,
                    opaque(palette.border()));
            for (int i = 0; i < this.values.length; ++i) {
                int rowY = listY + 1 + i * ROW_HEIGHT;
                boolean hover = mouseX >= this.getX() && mouseX < this.getX() + this.getWidth()
                        && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;
                boolean selected = this.values[i] == this.current.get();
                int fill = opaque(selected ? palette.header()
                        : (hover ? palette.tableHeader() : palette.summary()));
                graphics.fill(this.getX() + 1, rowY, this.getX() + this.getWidth() - 1,
                        rowY + ROW_HEIGHT, fill);
                graphics.drawString(CustomHudScreen.this.font,
                        this.label.apply(this.values[i]).getString(),
                        this.getX() + 6, rowY + 5,
                        selected ? palette.accentB() : palette.text(), false);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0) return false;
            if (mouseX >= this.getX() && mouseX < this.getX() + this.getWidth()
                    && mouseY >= this.getY() && mouseY < this.getY() + this.getHeight()) {
                this.open = !this.open;
                return true;
            }
            if (this.open) {
                int listY = this.getY() + this.getHeight() + 1;
                if (mouseX >= this.getX() && mouseX < this.getX() + this.getWidth()
                        && mouseY >= listY && mouseY < listY + this.values.length * ROW_HEIGHT + 2) {
                    int row = (int)((mouseY - listY - 1) / ROW_HEIGHT);
                    if (row >= 0 && row < this.values.length) {
                        this.selection.accept(this.values[row]);
                        this.open = false;
                        ForeverProductionMonitorClient.refreshHudNow();
                        return true;
                    }
                }
                this.open = false;
            }
            return false;
        }

        boolean isOpen() {
            return this.open;
        }

        int overlayBottom() {
            return this.getY() + this.getHeight() + 1 + this.values.length * ROW_HEIGHT + 2;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }
}
