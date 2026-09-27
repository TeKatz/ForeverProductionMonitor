package de.timo.foreverproductionmonitor.client.screen;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEKey;
import de.timo.foreverproductionmonitor.client.ClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class MonitorCraftAmountScreen extends Screen {
    private final CraftingDiagnosticsScreen parent;
    private final AEKey target;
    private EditBox amount;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;

    public MonitorCraftAmountScreen(CraftingDiagnosticsScreen parent, AEKey target) {
        super(Component.translatable("screen.forever_production_monitor.crafting.amount_title"));
        this.parent = parent;
        this.target = target;
    }

    @Override
    protected void init() {
        panelWidth = Math.min(360, width - 24);
        panelHeight = 176;
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;

        int clearWidth = 58;
        amount = new EditBox(font, left + 24, top + 72, panelWidth - 52 - clearWidth, 22,
                Component.translatable("screen.forever_production_monitor.crafting.amount"));
        amount.setMaxLength(10);
        amount.setFilter(value -> value.isEmpty() || value.chars().allMatch(Character::isDigit));
        amount.setValue("1");
        amount.setFocused(true);
        addRenderableWidget(amount);
        addRenderableWidget(ForeverButton.create(Component.literal("Clear"),
                button -> clearAmount(), ForeverButton.Style.SECONDARY,
                left + panelWidth - 24 - clearWidth, top + 72, clearWidth, 22));

        int presetY = top + 101;
        long[] presets = {1L, 10L, 32L, 64L, 128L, 1_000L, 10_000L};
        int presetGap = 3;
        int presetWidth = Math.max(1, (panelWidth - 48 - presetGap * (presets.length - 1)) / presets.length);
        for (int i = 0; i < presets.length; i++) {
            long delta = presets[i];
            addRenderableWidget(ForeverButton.create(Component.literal("+" + delta),
                    button -> addAmount(delta), ForeverButton.Style.SECONDARY,
                    left + 24 + i * (presetWidth + presetGap), presetY, presetWidth, 20));
        }

        int buttonY = top + 132;
        int buttonWidth = (panelWidth - 52) / 2;
        addRenderableWidget(ForeverButton.create(
                Component.translatable("screen.forever_production_monitor.crafting.start"),
                button -> startCraft(), ForeverButton.Style.THEMED,
                left + 24, buttonY, buttonWidth, 22));
        addRenderableWidget(ForeverButton.create(
                Component.translatable("gui.cancel"),
                button -> minecraft.setScreen(parent), ForeverButton.Style.SECONDARY,
                left + 28 + buttonWidth, buttonY, buttonWidth, 22));
    }

    private void clearAmount() {
        amount.setValue("0");
        amount.setFocused(true);
    }

    private void addAmount(long delta) {
        long current;
        try {
            current = amount.getValue().isEmpty() ? 0L : Long.parseLong(amount.getValue());
        } catch (NumberFormatException ignored) {
            current = 0L;
        }
        amount.setValue(Long.toString(Math.min(1_000_000_000L, Math.max(0L, current) + delta)));
        amount.setFocused(true);
    }

    private void startCraft() {
        long value;
        try {
            value = Long.parseLong(amount.getValue());
        } catch (NumberFormatException ignored) {
            return;
        }
        if (value <= 0 || value > 1_000_000_000L) return;
        parent.startCraft(target, value);
        minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        parent.render(g, -1, -1, partialTick);
        var palette = InterfaceTheme.current();
        int opaquePanel = 0xFF000000 | (palette.panel() & 0x00FFFFFF);
        g.fill(left - 2, top - 2, left + panelWidth + 2, top + panelHeight + 2, opaquePanel);
        InterfaceTheme.drawPanel(g, left, top, panelWidth, panelHeight,
                (ClientConfig.InterfaceStyle) ClientConfig.VALUES.interfaceStyle.get(), palette);
        g.drawCenteredString(font, title, left + panelWidth / 2, top + 16, palette.text());
        if (target != null) {
            AEKeyRendering.drawInGui(minecraft, g, left + 24, top + 42, target);
            g.drawString(font, AEKeyRendering.getDisplayName(target), left + 48, top + 46, palette.text(), false);
        }
        g.drawString(font, Component.translatable("screen.forever_production_monitor.crafting.amount"),
                left + 24, top + 61, palette.muted(), false);
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER
                || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER) && amount != null) {
            startCraft();
            return true;
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
                || (minecraft != null && minecraft.options.keyInventory.matches(keyCode, scanCode))) {
            minecraft.setScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
