/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  appeng.api.client.AEKeyRendering
 *  appeng.api.stacks.AEKey
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.Renderable
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 */
package de.timo.foreverproductionmonitor.client.screen;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEKey;
import de.timo.foreverproductionmonitor.blockentity.ProductionMonitorBlockEntity;
import de.timo.foreverproductionmonitor.client.ClientConfig;
import de.timo.foreverproductionmonitor.client.screen.ForeverButton;
import de.timo.foreverproductionmonitor.client.screen.InterfaceTheme;
import de.timo.foreverproductionmonitor.item.ProductionTabletItem;
import de.timo.foreverproductionmonitor.network.MonitorNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class AlarmRuleScreen
extends Screen {
    private static final int[] DELAYS = new int[]{0, 10, 30, 60, 300, 900};
    private final Screen parent;
    private final ProductionTabletItem.MonitorLink link;
    private final MonitorNetwork.DashboardEntry entry;
    private ProductionMonitorBlockEntity.AlarmMode mode;
    private int delaySeconds;
    private EditBox threshold;
    private EditBox hysteresis;
    private ForeverButton modeButton;
    private ForeverButton delayButton;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;

    public AlarmRuleScreen(Screen screen, ProductionTabletItem.MonitorLink monitorLink, MonitorNetwork.DashboardEntry dashboardEntry) {
        super((Component)Component.translatable((String)"screen.forever_production_monitor.alarm_editor.title"));
        this.parent = screen;
        this.link = monitorLink;
        this.entry = dashboardEntry;
        this.mode = dashboardEntry.alarmMode();
        this.delaySeconds = dashboardEntry.delaySeconds();
    }

    protected void init() {
        this.panelWidth = Math.min(480, this.width - 24);
        this.panelHeight = Math.min(342, this.height - 24);
        this.left = (this.width - this.panelWidth) / 2;
        this.top = (this.height - this.panelHeight) / 2;
        int n = this.left + 30;
        int n2 = this.panelWidth - 60;
        this.modeButton = (ForeverButton)this.addRenderableWidget(ForeverButton.create(this.modeLabel(), button -> {
            this.mode = ProductionMonitorBlockEntity.AlarmMode.values()[(this.mode.ordinal() + 1) % ProductionMonitorBlockEntity.AlarmMode.values().length];
            this.modeButton.setMessage(this.modeLabel());
            this.updateFields();
        }, ForeverButton.Style.VIOLET, n, this.top + 91, n2, 22));
        this.threshold = new EditBox(this.font, n, this.top + 137, n2, 20, (Component)Component.translatable((String)"screen.forever_production_monitor.alarm_editor.threshold"));
        this.threshold.setMaxLength(20);
        this.threshold.setValue(Long.toString(this.entry.threshold()));
        this.addRenderableWidget(this.threshold);
        this.delayButton = (ForeverButton)this.addRenderableWidget(ForeverButton.create(this.delayLabel(), button -> {
            int delayIndex;
            for (delayIndex = 0; delayIndex < DELAYS.length && DELAYS[delayIndex] != this.delaySeconds; ++delayIndex) {
            }
            this.delaySeconds = DELAYS[(delayIndex + 1) % DELAYS.length];
            this.delayButton.setMessage(this.delayLabel());
        }, ForeverButton.Style.SECONDARY, n, this.top + 183, n2, 22));
        this.hysteresis = new EditBox(this.font, n, this.top + 229, n2, 20, (Component)Component.translatable((String)"screen.forever_production_monitor.alarm_editor.hysteresis"));
        this.hysteresis.setMaxLength(20);
        this.hysteresis.setValue(Long.toString(this.entry.hysteresis()));
        this.addRenderableWidget(this.hysteresis);
        int n3 = 5;
        int n4 = (n2 - n3 * 3) / 4;
        this.addRenderableWidget(ForeverButton.create((Component)Component.literal((String)"\u2191"), button -> this.move(MonitorNetwork.DashboardAction.MOVE_UP), ForeverButton.Style.SECONDARY, n, this.top + this.panelHeight - 48, n4, 22));
        this.addRenderableWidget(ForeverButton.create((Component)Component.literal((String)"\u2193"), button -> this.move(MonitorNetwork.DashboardAction.MOVE_DOWN), ForeverButton.Style.SECONDARY, n + n4 + n3, this.top + this.panelHeight - 48, n4, 22));
        this.addRenderableWidget(ForeverButton.create((Component)Component.translatable((String)"gui.cancel"), button -> this.onClose(), ForeverButton.Style.SECONDARY, n + (n4 + n3) * 2, this.top + this.panelHeight - 48, n4, 22));
        this.addRenderableWidget(ForeverButton.create((Component)Component.translatable((String)"screen.forever_production_monitor.alarm_editor.save"), button -> this.save(), ForeverButton.Style.GOLD, n + (n4 + n3) * 3, this.top + this.panelHeight - 48, n2 - (n4 + n3) * 3, 22));
        this.updateFields();
    }

    private void updateFields() {
        boolean bl = this.mode != ProductionMonitorBlockEntity.AlarmMode.NONE;
        this.threshold.active = this.threshold.visible = bl && this.mode != ProductionMonitorBlockEntity.AlarmMode.STALLED;
        this.hysteresis.active = this.hysteresis.visible = bl && this.mode != ProductionMonitorBlockEntity.AlarmMode.STALLED;
        this.delayButton.visible = bl;
        this.delayButton.active = bl;
    }

    private Component modeLabel() {
        return Component.translatable((String)"screen.forever_production_monitor.alarm_editor.mode", (Object[])new Object[]{Component.translatable((String)("screen.forever_production_monitor.alarm." + this.mode.name().toLowerCase()))});
    }

    private Component delayLabel() {
        return Component.translatable((String)"screen.forever_production_monitor.alarm_editor.delay", (Object[])new Object[]{this.delaySeconds});
    }

    private void save() {
        MonitorNetwork.updateDashboardPin(this.link, MonitorNetwork.DashboardAction.UPDATE, this.entry.kind(), this.entry.key(), this.mode, AlarmRuleScreen.parseLong(this.threshold.getValue()), this.delaySeconds, Math.max(0L, AlarmRuleScreen.parseLong(this.hysteresis.getValue())));
        this.minecraft.setScreen(this.parent);
    }

    private void move(MonitorNetwork.DashboardAction dashboardAction) {
        MonitorNetwork.updateDashboardPin(this.link, dashboardAction, this.entry.kind(), this.entry.key(), this.entry.alarmMode(), this.entry.threshold(), this.entry.delaySeconds(), this.entry.hysteresis());
        this.minecraft.setScreen(this.parent);
    }

    private static long parseLong(String string) {
        try {
            return Long.parseLong(string.strip());
        }
        catch (NumberFormatException numberFormatException) {
            return 0L;
        }
    }

    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        guiGraphics.fill(0, 0, this.width, this.height, palette.backdrop());
        InterfaceTheme.drawPanel(guiGraphics, this.left, this.top, this.panelWidth, this.panelHeight, (ClientConfig.InterfaceStyle)((Object)ClientConfig.VALUES.interfaceStyle.get()), palette);
        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, n, n2, f);
        }
        guiGraphics.drawCenteredString(this.font, this.title, this.left + this.panelWidth / 2, this.top + 14, palette.text());
        int n3 = this.left + 30;
        int n4 = this.top + 40;
        guiGraphics.fill(n3, n4, this.left + this.panelWidth - 30, n4 + 38, palette.tableOuter());
        AEKeyRendering.drawInGui((Minecraft)this.minecraft, (GuiGraphics)guiGraphics, (int)(n3 + 8), (int)(n4 + 10), (AEKey)this.entry.key());
        Component component = AEKeyRendering.getDisplayName((AEKey)this.entry.key());
        guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(component.getString(), this.panelWidth - 118), n3 + 31, n4 + 7, palette.text(), false);
        guiGraphics.drawString(this.font, this.entry.key().getId().toString(), n3 + 31, n4 + 21, palette.muted(), false);
        if (this.threshold.visible) {
            guiGraphics.drawString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.alarm_editor.threshold"), n3, this.top + 124, palette.muted(), false);
        }
        if (this.delayButton.visible) {
            guiGraphics.drawString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.alarm_editor.delay_label"), n3, this.top + 170, palette.muted(), false);
        }
        if (this.hysteresis.visible) {
            guiGraphics.drawString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.alarm_editor.hysteresis"), n3, this.top + 216, palette.muted(), false);
        }
        if (this.mode == ProductionMonitorBlockEntity.AlarmMode.NONE) {
            guiGraphics.drawCenteredString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.alarm_editor.disabled"), this.left + this.panelWidth / 2, this.top + 158, palette.muted());
        }
        if (this.mode == ProductionMonitorBlockEntity.AlarmMode.STALLED) {
            guiGraphics.drawCenteredString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.alarm_editor.stalled_hint"), this.left + this.panelWidth / 2, this.top + 158, palette.accentA());
        }
    }

    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    public boolean isPauseScreen() {
        return false;
    }
}

