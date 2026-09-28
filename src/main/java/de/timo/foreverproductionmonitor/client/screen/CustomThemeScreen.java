package de.timo.foreverproductionmonitor.client.screen;

import de.timo.foreverproductionmonitor.client.ClientConfig;
import de.timo.foreverproductionmonitor.client.ForeverProductionMonitorClient;
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
 * Dedicated editor for the CUSTOM interface theme.
 * All values are client config only and the preview renders directly from the live config.
 */
public final class CustomThemeScreen extends Screen {
    private final Screen parent;
    private final List<EnumDropdown<?>> dropdowns = new ArrayList<>();
    private final List<Renderable> controlWidgets = new ArrayList<>();
    private Category category = Category.COLORS;
    private int controlScroll;
    private int maxControlScroll;
    private int controlsViewportTop;
    private int controlsViewportBottom;
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
        this.dropdowns.clear();
        this.controlWidgets.clear();
        this.panelWidth = Math.max(1, Math.min(940, this.width - 12));
        this.panelHeight = Math.max(1, Math.min(560, this.height - 12));
        this.left = (this.width - this.panelWidth) / 2;
        this.top = (this.height - this.panelHeight) / 2;
        this.contentTop = this.top + 42;
        this.footerY = this.top + this.panelHeight - 29;
        this.contentBottom = this.footerY - 12;

        int innerLeft = this.left + 18;
        int innerRight = this.left + this.panelWidth - 18;
        int innerWidth = Math.max(1, innerRight - innerLeft);
        this.controlsWidth = Math.max(230, Math.min(360, innerWidth * 42 / 100));
        this.controlsLeft = innerLeft;
        this.previewLeft = this.controlsLeft + this.controlsWidth + 12;
        this.previewWidth = Math.max(120, innerRight - this.previewLeft);
        this.controlsViewportTop = this.contentTop + 30;
        this.controlsViewportBottom = this.contentBottom;
        this.maxControlScroll = Math.max(0, controlContentHeight() - Math.max(1, this.controlsViewportBottom - this.controlsViewportTop));
        this.controlScroll = Math.max(0, Math.min(this.controlScroll, this.maxControlScroll));

        buildCategoryTabs();
        switch (this.category) {
            case COLORS -> buildColors();
            case BACKGROUND -> buildBackground();
            case PARTICLES -> buildParticles();
            case COMPONENTS -> buildComponents();
        }

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

    private void buildCategoryTabs() {
        int gap = 4;
        int y = this.contentTop;
        int count = Category.values().length;
        int width = Math.max(1, (this.controlsWidth - gap * (count - 1)) / count);
        for (int i = 0; i < count; ++i) {
            Category value = Category.values()[i];
            int x = this.controlsLeft + i * (width + gap);
            int w = i == count - 1
                    ? Math.max(1, this.controlsLeft + this.controlsWidth - x)
                    : width;
            this.addRenderableWidget(ForeverButton.create(
                    categoryName(value),
                    button -> {
                        this.category = value;
                        this.controlScroll = 0;
                        this.rebuildWidgets();
                    },
                    value == this.category ? ForeverButton.Style.THEMED_ACTIVE : ForeverButton.Style.THEMED,
                    x, y, w, 20).setRole(ForeverButton.Role.TAB));
        }
    }

    private int controlsY() {
        return this.controlsViewportTop - this.controlScroll;
    }

    private int controlContentHeight() {
        return switch (this.category) {
            case COLORS -> 260;
            case BACKGROUND -> {
                ClientConfig.CustomBackgroundStyle style =
                        (ClientConfig.CustomBackgroundStyle)ClientConfig.VALUES.interfaceCustomBackgroundStyle.get();
                yield style == ClientConfig.CustomBackgroundStyle.NONE ? 20
                        : (style == ClientConfig.CustomBackgroundStyle.GRID
                        || style == ClientConfig.CustomBackgroundStyle.SCAN_LINES
                        || style == ClientConfig.CustomBackgroundStyle.ENERGY_WAVES ? 120 : 96);
            }
            case PARTICLES -> (ClientConfig.CustomParticleStyle)ClientConfig.VALUES.interfaceCustomParticleStyle.get()
                    == ClientConfig.CustomParticleStyle.NONE ? 20 : 148;
            case COMPONENTS -> 108;
        };
    }

    private void addControlWidget(AbstractWidget widget) {
        widget.visible = widget.getY() + widget.getHeight() > this.controlsViewportTop
                && widget.getY() < this.controlsViewportBottom;
        this.addWidget(widget);
        this.controlWidgets.add(widget);
    }

    private void buildColors() {
        int y = controlsY();
        int step = 24;
        int width = this.controlsWidth;
        addSlider(y, width, "custom_theme.primary_hue",
                0.0, 359.0, ((Integer)ClientConfig.VALUES.interfaceCustomHue.get()).doubleValue(),
                value -> ClientConfig.VALUES.interfaceCustomHue.set((int)Math.round(value)), false);
        addSlider(y + step, width, "custom_theme.secondary_hue",
                0.0, 359.0, ((Integer)ClientConfig.VALUES.interfaceCustomSecondaryHue.get()).doubleValue(),
                value -> ClientConfig.VALUES.interfaceCustomSecondaryHue.set((int)Math.round(value)), false);
        addSlider(y + step * 2, width, "custom_theme.primary_saturation",
                0.0, 1.0, (Double)ClientConfig.VALUES.interfaceCustomPrimarySaturation.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceCustomPrimarySaturation).set(value), true);
        addSlider(y + step * 3, width, "custom_theme.secondary_saturation",
                0.0, 1.0, (Double)ClientConfig.VALUES.interfaceCustomSecondarySaturation.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceCustomSecondarySaturation).set(value), true);
        addSlider(y + step * 4, width, "custom_theme.surface_saturation",
                0.0, 0.75, (Double)ClientConfig.VALUES.interfaceCustomSurfaceSaturation.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceCustomSurfaceSaturation).set(value), true);
        addSlider(y + step * 5, width, "custom_theme.brightness",
                0.2, 0.9, (Double)ClientConfig.VALUES.interfaceCustomBrightness.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceCustomBrightness).set(value), true);
        addSlider(y + step * 6, width, "custom_theme.contrast",
                0.65, 1.45, (Double)ClientConfig.VALUES.interfaceCustomContrast.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceCustomContrast).set(value), true);
        addSlider(y + step * 7, width, "custom_theme.header_strength",
                0.65, 1.45, (Double)ClientConfig.VALUES.interfaceCustomHeaderStrength.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceCustomHeaderStrength).set(value), true);
        addSlider(y + step * 8, width, "custom_theme.border_strength",
                0.5, 1.5, (Double)ClientConfig.VALUES.interfaceCustomBorderStrength.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceCustomBorderStrength).set(value), true);
        addSlider(y + step * 9, width, "custom_theme.accent_brightness",
                0.65, 1.25, (Double)ClientConfig.VALUES.interfaceCustomAccentBrightness.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceCustomAccentBrightness).set(value), true);
        addSlider(y + step * 10, width, "custom_theme.opacity",
                0.55, 1.0, (Double)ClientConfig.VALUES.interfaceOpacity.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceOpacity).set(value), true);
    }

    private void buildBackground() {
        int y = controlsY();
        addEnumDropdown(y, "custom_theme.background_style",
                ClientConfig.CustomBackgroundStyle.values(),
                () -> (ClientConfig.CustomBackgroundStyle)ClientConfig.VALUES.interfaceCustomBackgroundStyle.get(),
                value -> {
                    ClientConfig.VALUES.interfaceCustomBackgroundStyle.set(value);
                    this.rebuildWidgets();
                },
                value -> enumName("custom_theme.background", value));

        ClientConfig.CustomBackgroundStyle style =
                (ClientConfig.CustomBackgroundStyle)ClientConfig.VALUES.interfaceCustomBackgroundStyle.get();
        if (style == ClientConfig.CustomBackgroundStyle.NONE) {
            return;
        }

        int step = 24;
        y += 28;
        addSlider(y, this.controlsWidth, "custom_theme.background_density",
                0.1, 1.0, (Double)ClientConfig.VALUES.interfaceCustomBackgroundDensity.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceCustomBackgroundDensity).set(value), true);
        addSlider(y + step, this.controlsWidth, "custom_theme.background_speed",
                0.0, 1.0, (Double)ClientConfig.VALUES.interfaceCustomBackgroundSpeed.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceCustomBackgroundSpeed).set(value), true);
        addSlider(y + step * 2, this.controlsWidth, "custom_theme.background_opacity",
                0.0, 1.0, (Double)ClientConfig.VALUES.interfaceCustomBackgroundOpacity.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceCustomBackgroundOpacity).set(value), true);
        if (style == ClientConfig.CustomBackgroundStyle.GRID
                || style == ClientConfig.CustomBackgroundStyle.SCAN_LINES
                || style == ClientConfig.CustomBackgroundStyle.ENERGY_WAVES) {
            addSlider(y + step * 3, this.controlsWidth, "custom_theme.background_scale",
                    0.5, 2.0, (Double)ClientConfig.VALUES.interfaceCustomBackgroundScale.get(),
                    value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceCustomBackgroundScale).set(value), true);
        }
    }

    private void buildParticles() {
        int y = controlsY();
        addEnumDropdown(y, "custom_theme.particle_style",
                ClientConfig.CustomParticleStyle.values(),
                () -> (ClientConfig.CustomParticleStyle)ClientConfig.VALUES.interfaceCustomParticleStyle.get(),
                value -> {
                    ClientConfig.VALUES.interfaceCustomParticleStyle.set(value);
                    this.rebuildWidgets();
                },
                value -> enumName("custom_theme.particle", value));

        ClientConfig.CustomParticleStyle style =
                (ClientConfig.CustomParticleStyle)ClientConfig.VALUES.interfaceCustomParticleStyle.get();
        if (style == ClientConfig.CustomParticleStyle.NONE) {
            return;
        }

        y += 28;
        addEnumDropdown(y, "custom_theme.particle_color",
                ClientConfig.CustomParticleColorMode.values(),
                () -> (ClientConfig.CustomParticleColorMode)ClientConfig.VALUES.interfaceCustomParticleColorMode.get(),
                value -> ClientConfig.VALUES.interfaceCustomParticleColorMode.set(value),
                value -> enumName("custom_theme.particle_color_mode", value));

        int step = 24;
        y += 28;
        addSlider(y, this.controlsWidth, "custom_theme.particle_amount",
                0.0, 1.0, (Double)ClientConfig.VALUES.interfaceCustomParticleAmount.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceCustomParticleAmount).set(value), true);
        addSlider(y + step, this.controlsWidth, "custom_theme.particle_speed",
                0.0, 1.0, (Double)ClientConfig.VALUES.interfaceCustomParticleSpeed.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceCustomParticleSpeed).set(value), true);
        addSlider(y + step * 2, this.controlsWidth, "custom_theme.particle_size",
                0.5, 2.0, (Double)ClientConfig.VALUES.interfaceCustomParticleSize.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceCustomParticleSize).set(value), true);
        addSlider(y + step * 3, this.controlsWidth, "custom_theme.particle_opacity",
                0.0, 1.0, (Double)ClientConfig.VALUES.interfaceCustomParticleOpacity.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceCustomParticleOpacity).set(value), true);
    }

    private void buildComponents() {
        int y = controlsY();
        addEnumDropdown(y, "custom_theme.tab_style",
                ClientConfig.CustomTabStyle.values(),
                () -> (ClientConfig.CustomTabStyle)ClientConfig.VALUES.interfaceCustomTabStyle.get(),
                value -> ClientConfig.VALUES.interfaceCustomTabStyle.set(value),
                value -> enumName("custom_theme.tab", value));
        y += 28;
        addSlider(y, this.controlsWidth, "custom_theme.tab_accent_strength",
                0.0, 1.0, (Double)ClientConfig.VALUES.interfaceCustomTabAccentStrength.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceCustomTabAccentStrength).set(value), true);

        y += 32;
        addEnumDropdown(y, "custom_theme.button_style",
                ClientConfig.CustomButtonStyle.values(),
                () -> (ClientConfig.CustomButtonStyle)ClientConfig.VALUES.interfaceCustomButtonStyle.get(),
                value -> ClientConfig.VALUES.interfaceCustomButtonStyle.set(value),
                value -> enumName("custom_theme.button", value));
        y += 28;
        addSlider(y, this.controlsWidth, "custom_theme.button_hover_strength",
                0.0, 1.0, (Double)ClientConfig.VALUES.interfaceCustomButtonHoverStrength.get(),
                value -> ((ModConfigSpec.DoubleValue)ClientConfig.VALUES.interfaceCustomButtonHoverStrength).set(value), true);
    }

    private void addSlider(int y, int width, String key,
                           double min, double max, double value,
                           DoubleConsumer setter, boolean decimal) {
        this.addControlWidget(new CustomSlider(
                this.controlsLeft, y, width, key, min, max, value, setter, decimal));
    }

    private <T extends Enum<T>> void addEnumDropdown(int y, String key, T[] values,
                                                     Supplier<T> current, Consumer<T> selection,
                                                     Function<T, Component> label) {
        EnumDropdown<T> dropdown = new EnumDropdown<>(
                this.controlsLeft, y, this.controlsWidth, 20,
                key, values, current, selection, label);
        this.dropdowns.add(dropdown);
        this.addControlWidget(dropdown);
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

        graphics.drawString(this.font, categoryName(this.category),
                this.controlsLeft, this.contentTop + 23, palette.text(), false);
        graphics.drawString(this.font,
                Component.translatable("screen.forever_production_monitor.custom_theme.preview"),
                this.previewLeft, this.contentTop, palette.text(), false);

        for (Renderable renderable : this.renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTick);
        }

        drawPreview(graphics, palette);

        graphics.enableScissor(this.controlsLeft, this.controlsViewportTop,
                this.controlsLeft + this.controlsWidth, this.controlsViewportBottom);
        for (Renderable renderable : this.controlWidgets) {
            renderable.render(graphics, mouseX, mouseY, partialTick);
        }
        graphics.disableScissor();

        if (this.maxControlScroll > 0) {
            int trackX = this.controlsLeft + this.controlsWidth + 3;
            int trackTop = this.controlsViewportTop;
            int trackHeight = Math.max(12, this.controlsViewportBottom - this.controlsViewportTop);
            int viewport = Math.max(1, this.controlsViewportBottom - this.controlsViewportTop);
            int content = viewport + this.maxControlScroll;
            int thumbHeight = Math.max(18, trackHeight * viewport / Math.max(viewport, content));
            int thumbTravel = Math.max(1, trackHeight - thumbHeight);
            int thumbY = trackTop + thumbTravel * this.controlScroll / Math.max(1, this.maxControlScroll);
            graphics.fill(trackX, trackTop, trackX + 3, trackTop + trackHeight, opaque(palette.outer()));
            graphics.fill(trackX, thumbY, trackX + 3, thumbY + thumbHeight, opaque(palette.accentB()));
        }

        // Dropdown lists are rendered last and fully opaque so controls underneath never bleed through.
        graphics.enableScissor(this.controlsLeft, this.controlsViewportTop,
                this.controlsLeft + this.controlsWidth, this.controlsViewportBottom);
        for (EnumDropdown<?> dropdown : this.dropdowns) {
            dropdown.renderOverlay(graphics, mouseX, mouseY);
        }
        graphics.disableScissor();
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
            CustomThemeRenderer.drawTab(graphics, x, tabY, right - x, 20,
                    true, i == 0, i == 1 ? 0.35f : 0.0f, palette);
            int textX = x + Math.max(3, (right - x - this.font.width(tabs[i])) / 2);
            graphics.drawString(this.font, tabs[i], textX, tabY + 6,
                    i == 0 ? palette.accentB() : palette.text(), false);
        }

        int tableTop = tabY + 31;
        int tableBottom = Math.min(previewBottom - 82, tableTop + 112);
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

        int buttonY = previewBottom - 62;
        int buttonGap = 6;
        int buttonWidth = Math.max(30, (usable - buttonGap) / 2);
        int buttonLeft = this.previewLeft + pad;
        CustomThemeRenderer.drawButton(graphics, buttonLeft, buttonY,
                buttonWidth, 20, true, false, 0.25f, palette);
        CustomThemeRenderer.drawButton(graphics, buttonLeft + buttonWidth + buttonGap, buttonY,
                usable - buttonWidth - buttonGap, 20, true, true, 0.0f, palette);
        String action = "Action";
        String active = "Active";
        graphics.drawString(this.font, action,
                buttonLeft + Math.max(3, (buttonWidth - this.font.width(action)) / 2),
                buttonY + 6, palette.text(), false);
        int activeX = buttonLeft + buttonWidth + buttonGap;
        int activeW = usable - buttonWidth - buttonGap;
        graphics.drawString(this.font, active,
                activeX + Math.max(3, (activeW - this.font.width(active)) / 2),
                buttonY + 6, palette.text(), false);

        int swatchY = previewBottom - 31;
        int swatchLeft = this.previewLeft + pad;
        graphics.drawString(this.font,
                Component.translatable("screen.forever_production_monitor.custom_theme.accents"),
                swatchLeft, swatchY - 11, palette.muted(), false);
        graphics.fill(swatchLeft, swatchY, swatchLeft + 38, swatchY + 12, palette.accentA());
        graphics.fill(swatchLeft + 44, swatchY, swatchLeft + 82, swatchY + 12, palette.accentB());
        graphics.fill(swatchLeft + 88, swatchY,
                Math.min(this.previewLeft + this.previewWidth - pad, swatchLeft + 126),
                swatchY + 12, palette.border());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (EnumDropdown<?> dropdown : this.dropdowns) {
            if (dropdown.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (EnumDropdown<?> dropdown : this.dropdowns) {
            if (dropdown.isOpen()) {
                return true;
            }
        }
        boolean overControls = mouseX >= this.controlsLeft
                && mouseX < this.controlsLeft + this.controlsWidth + 6
                && mouseY >= this.controlsViewportTop && mouseY < this.controlsViewportBottom;
        if (overControls && this.maxControlScroll > 0 && scrollY != 0.0) {
            int next = Math.max(0, Math.min(this.maxControlScroll,
                    this.controlScroll + (scrollY < 0.0 ? 24 : -24)));
            if (next != this.controlScroll) {
                this.controlScroll = next;
                this.rebuildWidgets();
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private static int opaque(int color) {
        return 0xFF000000 | color & 0xFFFFFF;
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

    private static Component categoryName(Category category) {
        return Component.translatable("screen.forever_production_monitor.custom_theme.category."
                + category.name().toLowerCase(Locale.ROOT));
    }

    private static <T extends Enum<T>> Component enumName(String prefix, T value) {
        return Component.translatable("screen.forever_production_monitor." + prefix + "."
                + value.name().toLowerCase(Locale.ROOT));
    }

    private enum Category {
        COLORS,
        BACKGROUND,
        PARTICLES,
        COMPONENTS
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
            graphics.fill(this.getX(), this.getY(),
                    this.getX() + this.getWidth(), this.getY() + this.getHeight(), palette.border());
            graphics.fill(this.getX() + 1, this.getY() + 1,
                    this.getX() + this.getWidth() - 1, this.getY() + this.getHeight() - 1,
                    palette.summary());

            Component title = Component.translatable(
                    "screen.forever_production_monitor." + this.key,
                    this.label.apply(this.current.get()).getString());
            String text = CustomThemeScreen.this.font.plainSubstrByWidth(
                    title.getString(), Math.max(1, this.getWidth() - 20));
            graphics.drawString(CustomThemeScreen.this.font, text,
                    this.getX() + 6, this.getY() + 6, palette.text(), false);
            String arrow = this.open ? "▲" : "▼";
            graphics.drawString(CustomThemeScreen.this.font, arrow,
                    this.getX() + this.getWidth() - 12, this.getY() + 6, palette.accentB(), false);
        }

        void renderOverlay(GuiGraphics graphics, int mouseX, int mouseY) {
            if (!this.open) {
                return;
            }
            InterfaceTheme.Palette palette = InterfaceTheme.current();
            int listY = this.getY() + this.getHeight() + 1;
            int totalHeight = this.values.length * ROW_HEIGHT + 2;
            graphics.fill(this.getX(), listY,
                    this.getX() + this.getWidth(), listY + totalHeight, opaque(palette.border()));
            for (int i = 0; i < this.values.length; ++i) {
                int rowY = listY + 1 + i * ROW_HEIGHT;
                boolean hover = mouseX >= this.getX() && mouseX < this.getX() + this.getWidth()
                        && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;
                boolean selected = this.values[i] == this.current.get();
                int fill = opaque(selected ? palette.header() : (hover ? palette.tableHeader() : palette.summary()));
                graphics.fill(this.getX() + 1, rowY,
                        this.getX() + this.getWidth() - 1, rowY + ROW_HEIGHT, fill);
                String text = CustomThemeScreen.this.font.plainSubstrByWidth(
                        this.label.apply(this.values[i]).getString(), Math.max(1, this.getWidth() - 14));
                graphics.drawString(CustomThemeScreen.this.font, text,
                        this.getX() + 6, rowY + 5,
                        selected ? palette.accentB() : palette.text(), false);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0) {
                return false;
            }
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

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }
}
