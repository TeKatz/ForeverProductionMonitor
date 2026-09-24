/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  appeng.api.client.AEKeyRendering
 *  appeng.api.stacks.AEKey
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.Button$OnPress
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.Renderable
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 */
package de.timo.foreverproductionmonitor.client.screen;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEKey;
import de.timo.foreverproductionmonitor.blockentity.ProductionMonitorBlockEntity;
import de.timo.foreverproductionmonitor.client.ClientConfig;
import de.timo.foreverproductionmonitor.client.screen.AlarmRuleScreen;
import de.timo.foreverproductionmonitor.client.screen.ForeverButton;
import de.timo.foreverproductionmonitor.client.screen.InterfaceTheme;
import de.timo.foreverproductionmonitor.client.screen.NetworkMapView;
import de.timo.foreverproductionmonitor.client.screen.ProductionMonitorThemeScreen;
import de.timo.foreverproductionmonitor.item.ProductionTabletItem;
import de.timo.foreverproductionmonitor.network.MonitorNetwork;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;

public final class ProductionMonitorScreen
extends Screen {
    private static final int MAX_PANEL_WIDTH = 670;
    private static final int MAX_PANEL_HEIGHT = 454;
    private static final int HEADER_HEIGHT = 32;
    private static final int ROW_HEIGHT = 17;
    private static ViewMode lastViewMode;
    private final ProductionTabletItem.MonitorLink link;
    private EditBox search;
    private ForeverButton sortButton;
    private ForeverButton filterButton;
    private ForeverButton previousButton;
    private ForeverButton nextButton;
    private ForeverButton productionTab;
    private ForeverButton dashboardTab;
    private ForeverButton storageTab;
    private ForeverButton storageCapacityButton;
    private ForeverButton nbtItemsButton;
    private ForeverButton devicesTab;
    private ForeverButton mapTab;
    private GearButton settingsButton;
    private MonitorNetwork.MonitorSnapshot snapshot;
    private MonitorNetwork.StatisticsSnapshot statisticsSnapshot;
    private MonitorNetwork.DashboardSnapshot dashboardSnapshot;
    private MonitorNetwork.NetworkMapPayload networkMapSnapshot;
    private NetworkMapView networkMapView;
    private MonitorNetwork.SortMode sort = MonitorNetwork.SortMode.ACTIVITY;
    private MonitorNetwork.DeviceSort deviceSort = MonitorNetwork.DeviceSort.POWER;
    private MonitorNetwork.DeviceFilter deviceFilter = MonitorNetwork.DeviceFilter.ALL;
    private ViewMode viewMode = ViewMode.DASHBOARD;
    private int requestedPage;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;
    private int visibleRows;
    private int refreshTicks;
    private int searchDelay;
    private int sortX;
    private int sortWidth;
    private final Map<String, Integer> locatorIndexes = new HashMap<String, Integer>();

    public ProductionMonitorScreen(ProductionTabletItem.MonitorLink monitorLink) {
        super((Component)Component.translatable((String)"screen.forever_production_monitor.title"));
        this.link = monitorLink;
        this.viewMode = (Boolean)ClientConfig.VALUES.rememberLastTab.get() != false && lastViewMode != null ? lastViewMode : ViewMode.valueOf(((ClientConfig.DefaultTab)((Object)ClientConfig.VALUES.defaultTab.get())).name());
    }

    protected void init() {
        String string2 = this.search == null ? "" : this.search.getValue();
        this.panelWidth = Math.min(670, this.width - 16);
        this.panelHeight = Math.min(454, this.height - 16);
        this.left = (this.width - this.panelWidth) / 2;
        this.top = (this.height - this.panelHeight) / 2;
        this.visibleRows = Math.max(6, Math.min(18, (this.panelHeight - 180) / 17));
        int n = this.left + 18;
        int n2 = this.panelWidth - 36;
        int n3 = 4;
        int n4 = (n2 - n3 * 4) / 5;
        this.dashboardTab = (ForeverButton)this.addRenderableWidget((GuiEventListener)ForeverButton.create((Component)Component.translatable((String)"screen.forever_production_monitor.tab.dashboard"), button -> this.switchView(ViewMode.DASHBOARD), ForeverButton.Style.SECONDARY, n, this.top + 37, n4, 18));
        this.productionTab = (ForeverButton)this.addRenderableWidget((GuiEventListener)ForeverButton.create((Component)Component.translatable((String)"screen.forever_production_monitor.tab.production"), button -> this.switchView(ViewMode.PRODUCTION), ForeverButton.Style.SECONDARY, n + n4 + n3, this.top + 37, n4, 18));
        this.storageTab = (ForeverButton)this.addRenderableWidget((GuiEventListener)ForeverButton.create((Component)Component.translatable((String)"screen.forever_production_monitor.tab.storage"), button -> this.switchView(ViewMode.STORAGE), ForeverButton.Style.SECONDARY, n + (n4 + n3) * 2, this.top + 37, n4, 18));
        this.devicesTab = (ForeverButton)this.addRenderableWidget((GuiEventListener)ForeverButton.create((Component)Component.translatable((String)"screen.forever_production_monitor.tab.devices"), button -> this.switchView(ViewMode.DEVICES), ForeverButton.Style.SECONDARY, n + (n4 + n3) * 3, this.top + 37, n4, 18));
        this.mapTab = (ForeverButton)this.addRenderableWidget((GuiEventListener)ForeverButton.create((Component)Component.translatable((String)"screen.forever_production_monitor.tab.map"), button -> this.switchView(ViewMode.MAP), ForeverButton.Style.SECONDARY, n + (n4 + n3) * 4, this.top + 37, n4, 18));
        this.settingsButton = (GearButton)this.addRenderableWidget((GuiEventListener)new GearButton(this.left + this.panelWidth - 25, this.top + 7, button -> this.minecraft.setScreen((Screen)new ProductionMonitorThemeScreen(this))));
        int n5 = Math.max(150, Math.min(260, this.panelWidth * 2 / 5));
        this.search = new EditBox(this.font, this.left + 18, this.top + 62, n5, 20, (Component)Component.translatable((String)"screen.forever_production_monitor.search"));
        this.search.setHint((Component)Component.translatable((String)"screen.forever_production_monitor.search"));
        this.search.setMaxLength(64);
        this.search.setResponder(string -> {
            this.requestedPage = 0;
            if (this.viewMode == ViewMode.MAP && this.networkMapView != null) {
                this.networkMapView.setSearch(this.search.getValue());
            } else {
                this.searchDelay = 8;
            }
        });
        this.search.setValue(string2);
        this.addRenderableWidget((GuiEventListener)this.search);
        if (this.networkMapView == null) {
            this.networkMapView = new NetworkMapView(this.minecraft, this.font);
        }
        this.networkMapView.setBounds(this.left + 18, this.top + 88, this.panelWidth - 36, this.panelHeight - 130);
        if (this.networkMapSnapshot != null) {
            this.networkMapView.accept(this.networkMapSnapshot);
        }
        this.storageCapacityButton = (ForeverButton)this.addRenderableWidget((GuiEventListener)ForeverButton.create((Component)Component.translatable((String)"screen.forever_production_monitor.storage.section.capacity"), button -> this.switchView(ViewMode.STORAGE), ForeverButton.Style.SECONDARY, n, this.top + 62, n4, 20));
        this.nbtItemsButton = (ForeverButton)this.addRenderableWidget((GuiEventListener)ForeverButton.create((Component)Component.translatable((String)"screen.forever_production_monitor.storage.section.nbt_items"), button -> this.switchView(ViewMode.COMPONENTS), ForeverButton.Style.SECONDARY, n + n4 + n3, this.top + 62, n4, 20));
        int n6 = this.left + this.panelWidth - 132;
        int n7 = this.left + this.panelWidth - 52;
        this.sortX = this.left + 18 + n5 + 12;
        this.sortWidth = Math.max(90, n6 - 12 - this.sortX);
        this.sortButton = (ForeverButton)this.addRenderableWidget((GuiEventListener)ForeverButton.create(this.sortLabel(), button -> {
            if (this.viewMode == ViewMode.DEVICES) {
                this.deviceSort = this.deviceSort.next();
            } else {
                this.sort = this.sort.next();
            }
            this.requestedPage = 0;
            this.sortButton.setMessage(this.sortLabel());
            this.requestNow();
        }, ForeverButton.Style.VIOLET, this.sortX, this.top + 62, this.sortWidth, 20));
        this.filterButton = (ForeverButton)this.addRenderableWidget((GuiEventListener)ForeverButton.create(this.filterLabel(), button -> {
            this.deviceFilter = this.deviceFilter.next();
            this.requestedPage = 0;
            this.filterButton.setMessage(this.filterLabel());
            this.requestNow();
        }, ForeverButton.Style.SECONDARY, this.sortX, this.top + 62, this.sortWidth, 20));
        this.previousButton = (ForeverButton)this.addRenderableWidget((GuiEventListener)ForeverButton.create((Component)Component.literal((String)"\u2039"), button -> {
            if (this.requestedPage > 0) {
                --this.requestedPage;
                this.requestNow();
            }
        }, ForeverButton.Style.SECONDARY, n6, this.top + 62, 34, 20));
        this.nextButton = (ForeverButton)this.addRenderableWidget((GuiEventListener)ForeverButton.create((Component)Component.literal((String)"\u203a"), button -> {
            if (this.requestedPage + 1 < this.currentPages()) {
                ++this.requestedPage;
                this.requestNow();
            }
        }, ForeverButton.Style.SECONDARY, n7, this.top + 62, 34, 20));
        this.updateViewWidgets();
        this.requestNow();
    }

    private void switchView(ViewMode viewMode) {
        if (this.viewMode == viewMode) {
            return;
        }
        this.viewMode = viewMode;
        lastViewMode = viewMode;
        this.requestedPage = 0;
        this.statisticsSnapshot = null;
        if (viewMode == ViewMode.MAP && this.networkMapView != null && this.search != null) {
            this.networkMapView.setSearch(this.search.getValue());
        }
        this.updateViewWidgets();
        this.requestNow();
    }

    private void updateViewWidgets() {
        boolean bl;
        boolean bl2;
        if (this.search == null) {
            return;
        }
        this.search.active = this.search.visible = this.viewMode != ViewMode.STORAGE && this.viewMode != ViewMode.DASHBOARD;
        this.search.setHint((Component)Component.translatable((String)(this.viewMode == ViewMode.DEVICES ? "screen.forever_production_monitor.search.devices" : (this.viewMode == ViewMode.MAP ? "screen.forever_production_monitor.search.map" : (this.viewMode == ViewMode.COMPONENTS ? "screen.forever_production_monitor.search.components" : "screen.forever_production_monitor.search")))));
        int n = Math.max(150, Math.min(260, this.panelWidth * 2 / 5));
        if (this.viewMode == ViewMode.COMPONENTS) {
            this.search.setX(this.left + 242);
            this.search.setWidth(Math.max(120, this.left + this.panelWidth - 144 - this.search.getX()));
        } else {
            this.search.setX(this.left + 18);
            this.search.setWidth(this.viewMode == ViewMode.MAP ? Math.min(330, this.panelWidth - 36) : n);
        }
        this.sortButton.visible = bl2 = this.viewMode == ViewMode.PRODUCTION || this.viewMode == ViewMode.DEVICES;
        this.sortButton.active = bl2;
        int n2 = 4;
        int n3 = Math.max(44, (this.sortWidth - n2) / 2);
        this.sortButton.setX(this.sortX);
        this.sortButton.setWidth(this.viewMode == ViewMode.DEVICES ? n3 : this.sortWidth);
        this.sortButton.setMessage(this.sortLabel());
        this.filterButton.visible = this.viewMode == ViewMode.DEVICES;
        this.filterButton.active = this.viewMode == ViewMode.DEVICES;
        this.filterButton.setX(this.sortX + n3 + n2);
        this.filterButton.setWidth(this.sortWidth - n3 - n2);
        this.filterButton.setMessage(this.filterLabel());
        this.previousButton.visible = this.viewMode != ViewMode.STORAGE && this.viewMode != ViewMode.MAP;
        this.nextButton.visible = this.viewMode != ViewMode.STORAGE && this.viewMode != ViewMode.MAP;
        this.storageCapacityButton.visible = bl = this.viewMode == ViewMode.STORAGE || this.viewMode == ViewMode.COMPONENTS;
        this.storageCapacityButton.active = bl;
        this.nbtItemsButton.visible = bl;
        this.nbtItemsButton.active = bl;
        this.storageCapacityButton.setStyle(this.viewMode == ViewMode.STORAGE ? ForeverButton.Style.THEMED_ACTIVE : ForeverButton.Style.THEMED);
        this.nbtItemsButton.setStyle(this.viewMode == ViewMode.COMPONENTS ? ForeverButton.Style.THEMED_ACTIVE : ForeverButton.Style.THEMED);
        this.dashboardTab.setMessage(this.tabLabel(ViewMode.DASHBOARD, "screen.forever_production_monitor.tab.dashboard"));
        this.productionTab.setMessage(this.tabLabel(ViewMode.PRODUCTION, "screen.forever_production_monitor.tab.production"));
        this.storageTab.setMessage(this.tabLabel(ViewMode.STORAGE, "screen.forever_production_monitor.tab.storage"));
        this.devicesTab.setMessage(this.tabLabel(ViewMode.DEVICES, "screen.forever_production_monitor.tab.devices"));
        this.mapTab.setMessage(this.tabLabel(ViewMode.MAP, "screen.forever_production_monitor.tab.map"));
        this.dashboardTab.setStyle(this.viewMode == ViewMode.DASHBOARD ? ForeverButton.Style.THEMED_ACTIVE : ForeverButton.Style.THEMED);
        this.productionTab.setStyle(this.viewMode == ViewMode.PRODUCTION ? ForeverButton.Style.THEMED_ACTIVE : ForeverButton.Style.THEMED);
        this.storageTab.setStyle(this.viewMode == ViewMode.STORAGE || this.viewMode == ViewMode.COMPONENTS ? ForeverButton.Style.THEMED_ACTIVE : ForeverButton.Style.THEMED);
        this.devicesTab.setStyle(this.viewMode == ViewMode.DEVICES ? ForeverButton.Style.THEMED_ACTIVE : ForeverButton.Style.THEMED);
        this.mapTab.setStyle(this.viewMode == ViewMode.MAP ? ForeverButton.Style.THEMED_ACTIVE : ForeverButton.Style.THEMED);
        this.sortButton.setStyle(ForeverButton.Style.THEMED_ACTIVE);
        this.filterButton.setStyle(ForeverButton.Style.THEMED);
        this.previousButton.setStyle(ForeverButton.Style.THEMED);
        this.nextButton.setStyle(ForeverButton.Style.THEMED);
        this.updateButtons();
    }

    private Component tabLabel(ViewMode viewMode, String string) {
        boolean bl = this.viewMode == viewMode || viewMode == ViewMode.STORAGE && this.viewMode == ViewMode.COMPONENTS;
        return Component.literal((String)(bl ? "\u25c6 " : "")).append((Component)Component.translatable((String)string));
    }

    private Component sortLabel() {
        if (this.viewMode == ViewMode.DEVICES) {
            return Component.translatable((String)("screen.forever_production_monitor.device_sort." + this.deviceSort.name().toLowerCase()));
        }
        return Component.translatable((String)("screen.forever_production_monitor.sort." + this.sort.name().toLowerCase()));
    }

    private Component filterLabel() {
        return Component.translatable((String)("screen.forever_production_monitor.device_filter." + this.deviceFilter.name().toLowerCase()));
    }

    private void requestNow() {
        int n = ((ClientConfig.RefreshInterval)((Object)ClientConfig.VALUES.refreshInterval.get())).ticks();
        int n2 = this.refreshTicks = this.viewMode == ViewMode.MAP ? Math.max(100, n) : n;
        if (this.viewMode == ViewMode.DASHBOARD) {
            MonitorNetwork.requestDashboard(this.link);
        } else if (this.viewMode == ViewMode.PRODUCTION) {
            MonitorNetwork.request(this.link, this.search == null ? "" : this.search.getValue(), this.sort, this.requestedPage, this.visibleRows, n);
        } else if (this.viewMode == ViewMode.MAP) {
            MonitorNetwork.requestNetworkMap(this.link);
        } else {
            MonitorNetwork.StatisticsPage statisticsPage = switch (this.viewMode) {
                default -> throw new IncompatibleClassChangeError();
                case ViewMode.STORAGE -> MonitorNetwork.StatisticsPage.STORAGE;
                case ViewMode.COMPONENTS -> MonitorNetwork.StatisticsPage.COMPONENTS;
                case ViewMode.DEVICES -> MonitorNetwork.StatisticsPage.DEVICES;
                case ViewMode.PRODUCTION, ViewMode.DASHBOARD, ViewMode.MAP -> throw new IllegalStateException("This view uses its own packet");
            };
            MonitorNetwork.requestStatistics(this.link, statisticsPage, this.search == null ? "" : this.search.getValue(), this.requestedPage, this.visibleRows, this.deviceSort, this.deviceFilter);
        }
    }

    public void tick() {
        super.tick();
        if (this.searchDelay > 0 && --this.searchDelay == 0) {
            this.requestNow();
        }
        if (--this.refreshTicks <= 0) {
            this.requestNow();
        }
    }

    public void accept(MonitorNetwork.MonitorSnapshot monitorSnapshot) {
        if (this.viewMode == ViewMode.PRODUCTION && monitorSnapshot.dimension().equals((Object)this.link.dimension()) && monitorSnapshot.pos().equals((Object)this.link.pos())) {
            this.snapshot = monitorSnapshot;
            this.requestedPage = monitorSnapshot.page();
            this.updateButtons();
        }
    }

    public void acceptStatistics(MonitorNetwork.StatisticsSnapshot statisticsSnapshot) {
        MonitorNetwork.StatisticsPage statisticsPage;
        switch (this.viewMode) {
            default: {
                throw new IncompatibleClassChangeError();
            }
            case STORAGE: {
                MonitorNetwork.StatisticsPage statisticsPage2 = MonitorNetwork.StatisticsPage.STORAGE;
                break;
            }
            case COMPONENTS: {
                MonitorNetwork.StatisticsPage statisticsPage2 = MonitorNetwork.StatisticsPage.COMPONENTS;
                break;
            }
            case DEVICES: {
                MonitorNetwork.StatisticsPage statisticsPage2 = MonitorNetwork.StatisticsPage.DEVICES;
                break;
            }
            case PRODUCTION: 
            case DASHBOARD: 
            case MAP: {
                MonitorNetwork.StatisticsPage statisticsPage2 = statisticsPage = null;
            }
        }
        if (this.viewMode != ViewMode.PRODUCTION && statisticsSnapshot.statisticsPage() == statisticsPage && statisticsSnapshot.dimension().equals((Object)this.link.dimension()) && statisticsSnapshot.pos().equals((Object)this.link.pos())) {
            this.statisticsSnapshot = statisticsSnapshot;
            this.requestedPage = statisticsSnapshot.page();
            this.updateButtons();
        }
    }

    public void acceptDashboard(MonitorNetwork.DashboardSnapshot dashboardSnapshot) {
        if (dashboardSnapshot.dimension().equals((Object)this.link.dimension()) && dashboardSnapshot.pos().equals((Object)this.link.pos())) {
            this.dashboardSnapshot = dashboardSnapshot;
            this.updateButtons();
        }
    }

    public void acceptNetworkMap(MonitorNetwork.NetworkMapPayload networkMapPayload) {
        if (networkMapPayload.dimension().equals((Object)this.link.dimension()) && networkMapPayload.pos().equals((Object)this.link.pos())) {
            this.networkMapSnapshot = networkMapPayload;
            if (this.networkMapView != null) {
                this.networkMapView.accept(networkMapPayload);
            }
        }
    }

    private void updateButtons() {
        if (this.previousButton != null) {
            int n;
            int n2 = this.viewMode == ViewMode.DASHBOARD ? this.requestedPage : (this.viewMode == ViewMode.PRODUCTION && this.snapshot != null ? this.snapshot.page() : (n = this.statisticsSnapshot == null ? 0 : this.statisticsSnapshot.page()));
            int n3 = this.viewMode == ViewMode.DASHBOARD ? this.dashboardPages() : (this.viewMode == ViewMode.PRODUCTION && this.snapshot != null ? this.snapshot.pages() : (this.statisticsSnapshot == null ? 1 : this.statisticsSnapshot.pages()));
            this.previousButton.active = this.viewMode != ViewMode.STORAGE && this.viewMode != ViewMode.MAP && n > 0;
            this.nextButton.active = this.viewMode != ViewMode.STORAGE && this.viewMode != ViewMode.MAP && n + 1 < n3;
        }
    }

    private int currentPages() {
        return this.viewMode == ViewMode.MAP ? 1 : (this.viewMode == ViewMode.DASHBOARD ? this.dashboardPages() : (this.viewMode == ViewMode.PRODUCTION && this.snapshot != null ? this.snapshot.pages() : (this.statisticsSnapshot == null ? 1 : this.statisticsSnapshot.pages())));
    }

    private int dashboardPages() {
        int n = this.dashboardSnapshot == null ? 0 : this.dashboardSnapshot.entries().size();
        int n2 = this.dashboardCapacity();
        return Math.max(1, (n + n2 - 1) / n2);
    }

    private int dashboardCapacity() {
        return Math.max(1, (this.panelHeight - 108) / 28);
    }

    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        guiGraphics.fill(0, 0, this.width, this.height, palette.backdrop());
        this.drawPanel(guiGraphics);
        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, n, n2, f);
        }
        this.drawContent(guiGraphics, n, n2, f);
        this.drawTitle(guiGraphics);
        if (this.settingsButton != null && this.settingsButton.isHoveredOrFocused()) {
            guiGraphics.renderTooltip(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.theme.open"), n, n2);
        }
    }

    private void drawPanel(GuiGraphics guiGraphics) {
        InterfaceTheme.drawPanel(guiGraphics, this.left, this.top, this.panelWidth, this.panelHeight, (ClientConfig.InterfaceStyle)((Object)ClientConfig.VALUES.interfaceStyle.get()), InterfaceTheme.current());
    }

    private void drawTitle(GuiGraphics guiGraphics) {
        guiGraphics.drawString(this.font, this.title, this.left + (this.panelWidth - this.font.width((FormattedText)this.title)) / 2, this.top + 12, InterfaceTheme.current().text(), false);
    }

    private void drawContent(GuiGraphics guiGraphics, int n, int n2, float f) {
        switch (this.viewMode) {
            case DASHBOARD: {
                this.drawDashboardContent(guiGraphics, n, n2);
                break;
            }
            case PRODUCTION: {
                this.drawProductionContent(guiGraphics, n, n2);
                break;
            }
            case STORAGE: {
                this.drawStorageContent(guiGraphics);
                break;
            }
            case COMPONENTS: {
                this.drawComponentsContent(guiGraphics, n, n2);
                break;
            }
            case DEVICES: {
                this.drawDevicesContent(guiGraphics, n, n2);
                break;
            }
            case MAP: {
                this.drawNetworkMapContent(guiGraphics, n, n2, f);
            }
        }
    }

    private void drawNetworkMapContent(GuiGraphics guiGraphics, int n, int n2, float f) {
        if (this.networkMapView != null) {
            this.networkMapView.render(guiGraphics, n, n2, f);
        }
        int n3 = this.top + this.panelHeight - 29;
        String string = String.valueOf(this.link.dimension()) + "  " + this.link.pos().toShortString();
        guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(string, this.panelWidth / 2), this.left + 18, n3, InterfaceTheme.current().muted(), false);
        if (this.networkMapSnapshot != null) {
            this.drawStatus(guiGraphics, this.networkMapSnapshot.status(), this.left + this.panelWidth - 18, n3);
        }
    }

    private void drawDashboardContent(GuiGraphics guiGraphics, int n, int n2) {
        int n3;
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        int n4 = this.top + 66;
        int n5 = this.left + 18;
        int n6 = this.left + this.panelWidth - 18;
        int n7 = this.top + this.panelHeight - 42;
        int n8 = n6 - 176;
        int n9 = n6 - 300;
        guiGraphics.fill(n5, n4, n6, n7, palette.tableOuter());
        guiGraphics.fill(n5 + 1, n4 + 1, n6 - 1, n4 + 20, palette.tableHeader());
        guiGraphics.drawString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.dashboard.pinned"), n5 + 24, n4 + 6, palette.text(), false);
        this.drawRight(guiGraphics, (Component)Component.translatable((String)"screen.forever_production_monitor.stored"), n9, n4 + 6, palette.text());
        this.drawRight(guiGraphics, (Component)Component.translatable((String)"screen.forever_production_monitor.average"), n8, n4 + 6, palette.accentA());
        guiGraphics.drawString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.dashboard.alarm"), n8 + 18, n4 + 6, palette.accentB(), false);
        List list = this.dashboardSnapshot == null ? List.of() : this.dashboardSnapshot.entries();
        int n10 = this.dashboardCapacity();
        int n11 = Math.min(list.size(), this.requestedPage * n10);
        int n12 = Math.min(list.size(), n11 + n10);
        for (n3 = n11; n3 < n12; ++n3) {
            boolean bl;
            MonitorNetwork.DashboardEntry dashboardEntry2 = (MonitorNetwork.DashboardEntry)list.get(n3);
            int n13 = n3 - n11;
            int n14 = n4 + 21 + n13 * 28;
            int n15 = n13 % 2 == 0 ? palette.rowEven() : palette.rowOdd();
            guiGraphics.fill(n5 + 1, n14, n6 - 1, n14 + 27, n15);
            boolean bl2 = bl = n >= n5 + 1 && n < n6 - 1 && n2 >= n14 && n2 < n14 + 27;
            if (bl) {
                guiGraphics.fill(n5 + 1, n14, n6 - 1, n14 + 27, palette.hover());
            }
            int n16 = dashboardEntry2.alarmState() == ProductionMonitorBlockEntity.AlarmState.ACTIVE ? -39826 : (dashboardEntry2.alarmState() == ProductionMonitorBlockEntity.AlarmState.PENDING ? -14740 : -9972847);
            guiGraphics.fill(n5 + 1, n14, n5 + 4, n14 + 27, n16);
            AEKeyRendering.drawInGui((Minecraft)this.minecraft, (GuiGraphics)guiGraphics, (int)(n5 + 7), (int)(n14 + 5), (AEKey)dashboardEntry2.key());
            MutableComponent mutableComponent = dashboardEntry2.kind() == MonitorNetwork.EntryKind.ENERGY ? Component.translatable((String)"screen.forever_production_monitor.energy") : AEKeyRendering.getDisplayName((AEKey)dashboardEntry2.key());
            guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(mutableComponent.getString(), n9 - n5 - 80), n5 + 29, n14 + 5, palette.text(), false);
            guiGraphics.drawString(this.font, dashboardEntry2.key().getId().toString(), n5 + 29, n14 + 16, palette.muted(), false);
            this.drawRight(guiGraphics, (Component)Component.literal((String)(dashboardEntry2.infinite() ? "Infinite" : ProductionMonitorScreen.formatStored(dashboardEntry2.stored(), dashboardEntry2.kind()))), n9, n14 + 9, dashboardEntry2.infinite() ? palette.accentB() : palette.text());
            this.drawRight(guiGraphics, (Component)Component.literal((String)(dashboardEntry2.infinite() ? "\u2014" : ProductionMonitorScreen.formatRate(dashboardEntry2.averagePerMinute(), dashboardEntry2.kind()))), n8, n14 + 9, ProductionMonitorScreen.rateColor(dashboardEntry2.averagePerMinute()));
            MutableComponent mutableComponent2 = Component.translatable((String)("screen.forever_production_monitor.alarm." + dashboardEntry2.alarmMode().name().toLowerCase()));
            guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(mutableComponent2.getString(), 92), n8 + 18, n14 + 9, n16, false);
            this.drawDashboardAction(guiGraphics, n6 - 42, n14 + 6, "\u2699", bl, palette);
            this.drawDashboardAction(guiGraphics, n6 - 20, n14 + 6, "\u00d7", bl, palette);
        }
        if (list.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.dashboard.empty"), this.left + this.panelWidth / 2, n4 + 80, palette.muted());
            guiGraphics.drawCenteredString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.dashboard.empty_hint"), this.left + this.panelWidth / 2, n4 + 96, palette.accentA());
        }
        n3 = (int)list.stream().filter(dashboardEntry -> dashboardEntry.alarmState() == ProductionMonitorBlockEntity.AlarmState.ACTIVE).count();
        guiGraphics.drawString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.dashboard.summary", (Object[])new Object[]{list.size(), n3}), n5, this.top + this.panelHeight - 27, n3 > 0 ? -39826 : palette.muted(), false);
        if (this.dashboardSnapshot != null) {
            this.drawStatus(guiGraphics, this.dashboardSnapshot.status(), n6, this.top + this.panelHeight - 27);
        }
    }

    private void drawDashboardAction(GuiGraphics guiGraphics, int n, int n2, String string, boolean bl, InterfaceTheme.Palette palette) {
        guiGraphics.fill(n, n2, n + 16, n2 + 16, bl ? palette.border() : palette.tableOuter());
        guiGraphics.drawCenteredString(this.font, string, n + 8, n2 + 4, bl ? palette.accentA() : palette.muted());
    }

    private void drawProductionContent(GuiGraphics guiGraphics, int n, int n2) {
        int n3;
        MonitorNetwork.Entry entry;
        int n4;
        int n5;
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        int n6 = this.top + 95;
        int n7 = this.left + 18;
        int n8 = this.left + this.panelWidth - 18;
        int n9 = n8 - n7;
        int n10 = n7 + n9 * 66 / 100;
        int n11 = n7 + n9 * 82 / 100;
        int n12 = n6 + 18 + this.visibleRows * 17;
        guiGraphics.fill(n7, n6, n8, n12, palette.tableOuter());
        guiGraphics.fill(n7 + 1, n6 + 1, n8 - 1, n6 + 18, palette.tableHeader());
        guiGraphics.drawString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.item"), n7 + 24, n6 + 5, palette.text(), false);
        this.drawRight(guiGraphics, (Component)Component.translatable((String)"screen.forever_production_monitor.stored"), n10, n6 + 5, palette.text());
        this.drawRight(guiGraphics, (Component)Component.translatable((String)"screen.forever_production_monitor.rate"), n11, n6 + 5, palette.accentA());
        this.drawRight(guiGraphics, (Component)Component.translatable((String)"screen.forever_production_monitor.average"), n8 - 25, n6 + 5, palette.accentB());
        List list = this.snapshot == null ? List.of() : this.snapshot.entries();
        for (n5 = 0; n5 < this.visibleRows; ++n5) {
            MutableComponent mutableComponent;
            n4 = n6 + 18 + n5 * 17;
            int n13 = n5 % 2 == 0 ? palette.rowEven() : palette.rowOdd();
            guiGraphics.fill(n7 + 1, n4, n8 - 1, n4 + 17, n13);
            if (n5 >= list.size()) continue;
            entry = (MonitorNetwork.Entry)list.get(n5);
            int n14 = n3 = n >= n7 + 1 && n < n8 - 1 && n2 >= n4 && n2 < n4 + 17 ? 1 : 0;
            if (n3 != 0) {
                guiGraphics.fill(n7 + 1, n4, n8 - 1, n4 + 17, palette.hover());
                guiGraphics.fill(n7 + 1, n4, n7 + 3, n4 + 17, palette.accentB());
            }
            AEKeyRendering.drawInGui((Minecraft)this.minecraft, (GuiGraphics)guiGraphics, (int)(n7 + 4), (int)n4, (AEKey)entry.key());
            int n15 = Math.max(80, n10 - n7 - 120);
            String string = this.font.plainSubstrByWidth(ProductionMonitorScreen.displayName(entry).getString(), n15);
            guiGraphics.drawString(this.font, string, n7 + 25, n4 + 4, palette.text(), false);
            MutableComponent mutableComponent2 = mutableComponent = entry.infinite() ? Component.translatable((String)"screen.forever_production_monitor.infinite") : Component.literal((String)ProductionMonitorScreen.formatStored(entry.stored(), entry.kind()));
            this.drawRight(guiGraphics, (Component)mutableComponent, n10, n4 + 4, entry.infinite() ? palette.accentB() : (entry.kind() == MonitorNetwork.EntryKind.FLUID ? -8861464 : (entry.kind() == MonitorNetwork.EntryKind.ENERGY ? palette.accentA() : -3485478)));
            MutableComponent mutableComponent3 = Component.literal((String)(entry.infinite() ? "\u2014" : ProductionMonitorScreen.formatRate(entry.currentPerMinute(), entry.kind())));
            MutableComponent mutableComponent4 = Component.literal((String)(entry.infinite() ? "\u2014" : ProductionMonitorScreen.formatRate(entry.averagePerMinute(), entry.kind())));
            this.drawRight(guiGraphics, (Component)mutableComponent3, n11, n4 + 4, entry.infinite() ? -7366746 : ProductionMonitorScreen.rateColor(entry.currentPerMinute()));
            this.drawRight(guiGraphics, (Component)mutableComponent4, n8 - 25, n4 + 4, entry.infinite() ? -7366746 : ProductionMonitorScreen.rateColor(entry.averagePerMinute()));
            boolean bl = this.isPinned(entry.kind(), entry.key());
            guiGraphics.drawString(this.font, bl ? "\u25c6" : "\u25c7", n8 - 16, n4 + 4, bl ? palette.accentB() : (n3 != 0 ? palette.accentA() : palette.muted()), false);
            if (n3 == 0) continue;
            guiGraphics.renderComponentTooltip(this.font, entry.kind() == MonitorNetwork.EntryKind.ENERGY ? List.of(Component.translatable((String)"screen.forever_production_monitor.energy_tooltip")) : AEKeyRendering.getTooltip((AEKey)entry.key()), n, n2);
        }
        n5 = n12 + 6;
        this.drawSummary(guiGraphics, n7, n8, n5);
        n4 = n5 + 24;
        String string = String.valueOf(this.link.dimension()) + "  " + this.link.pos().toShortString();
        guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(string, n9 / 2), n7, n4 + 7, palette.muted(), false);
        if (this.snapshot == null) {
            guiGraphics.drawCenteredString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.loading"), this.left + this.panelWidth / 2, n6 + (n12 - n6) / 2, -14740);
        } else {
            entry = Component.translatable((String)("screen.forever_production_monitor.status." + this.snapshot.status().name().toLowerCase()));
            n3 = this.snapshot.status() == MonitorNetwork.Status.ONLINE ? -9972847 : (this.snapshot.status() == MonitorNetwork.Status.WARMING_UP ? -14740 : -35716);
            this.drawRight(guiGraphics, (Component)entry, n8, n4 + 7, n3);
            guiGraphics.drawCenteredString(this.font, (Component)Component.literal((String)(this.snapshot.page() + 1 + " / " + this.snapshot.pages())), this.left + this.panelWidth - 75, this.top + 68, palette.text());
            guiGraphics.drawString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.entries", (Object[])new Object[]{this.snapshot.totalEntries()}), this.left + 18, this.top + 87, palette.muted(), false);
        }
    }

    private void drawStorageContent(GuiGraphics guiGraphics) {
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        int n = this.left + 18;
        int n2 = this.left + this.panelWidth - 18;
        int n3 = n2 - n;
        int n4 = this.top + 91;
        MonitorNetwork.StatisticsSnapshot statisticsSnapshot = this.statisticsSnapshot != null && this.statisticsSnapshot.statisticsPage() == MonitorNetwork.StatisticsPage.STORAGE ? this.statisticsSnapshot : null;
        guiGraphics.drawString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.storage.subtitle"), n, n4, palette.muted(), false);
        MonitorNetwork.StorageStats storageStats = statisticsSnapshot == null ? MonitorNetwork.StorageStats.EMPTY : statisticsSnapshot.storage();
        double d = storageStats.totalBytes() <= 0L ? 0.0 : Math.min(1.0, (double)storageStats.usedBytes() / (double)storageStats.totalBytes());
        this.drawCapacityBar(guiGraphics, n, n2, n4 += 16, (Component)Component.translatable((String)"screen.forever_production_monitor.storage.bytes"), d, ProductionMonitorScreen.formatBytes(storageStats.usedBytes()) + " / " + ProductionMonitorScreen.formatBytes(storageStats.totalBytes()));
        int n5 = 5;
        int n6 = (n3 - n5 * 3) / 4;
        this.drawStatCard(guiGraphics, n, n4 += 34, n6, (Component)Component.translatable((String)"screen.forever_production_monitor.storage.used"), ProductionMonitorScreen.formatBytes(storageStats.usedBytes()), palette.accentA());
        this.drawStatCard(guiGraphics, n + n6 + n5, n4, n6, (Component)Component.translatable((String)"screen.forever_production_monitor.storage.free"), ProductionMonitorScreen.formatBytes(storageStats.freeBytes()), -9972847);
        this.drawStatCard(guiGraphics, n + (n6 + n5) * 2, n4, n6, (Component)Component.translatable((String)"screen.forever_production_monitor.storage.capacity"), ProductionMonitorScreen.formatBytes(storageStats.totalBytes()), -1185038);
        this.drawStatCard(guiGraphics, n + (n6 + n5) * 3, n4, n3 - (n6 + n5) * 3, (Component)Component.translatable((String)"screen.forever_production_monitor.storage.utilization"), String.format(Locale.ROOT, "%.1f%%", d * 100.0), palette.accentB());
        double d2 = storageStats.totalTypes() <= 0L ? 0.0 : Math.min(1.0, (double)storageStats.usedTypes() / (double)storageStats.totalTypes());
        this.drawCapacityBar(guiGraphics, n, n2, n4 += 50, (Component)Component.translatable((String)"screen.forever_production_monitor.storage.types"), d2, ProductionMonitorScreen.formatAmount(storageStats.usedTypes()) + " / " + ProductionMonitorScreen.formatAmount(storageStats.totalTypes()));
        guiGraphics.fill(n, n4 += 40, n2, n4 + 79, palette.tableOuter());
        guiGraphics.fill(n + 1, n4 + 1, n2 - 1, n4 + 19, palette.tableHeader());
        guiGraphics.drawString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.storage.sources"), n + 8, n4 + 6, palette.text(), false);
        int n7 = n3 / 2;
        this.drawSourceLine(guiGraphics, n + 9, n4 + 28, (Component)Component.translatable((String)"screen.forever_production_monitor.storage.finite_cells"), storageStats.finiteCells(), -9972847);
        this.drawSourceLine(guiGraphics, n + n7 + 5, n4 + 28, (Component)Component.translatable((String)"screen.forever_production_monitor.storage.infinite_cells"), storageStats.infiniteCells(), palette.accentB());
        this.drawSourceLine(guiGraphics, n + 9, n4 + 48, (Component)Component.translatable((String)"screen.forever_production_monitor.storage.other_cells"), storageStats.otherCells(), palette.accentA());
        this.drawSourceLine(guiGraphics, n + n7 + 5, n4 + 48, (Component)Component.translatable((String)"screen.forever_production_monitor.storage.external"), storageStats.externalStorageBuses(), -8861464);
        guiGraphics.drawString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.storage.note"), n + 9, n4 + 66, palette.muted(), false);
        this.drawStatisticsFooter(guiGraphics, statisticsSnapshot, n, n2);
        if (statisticsSnapshot == null) {
            guiGraphics.drawCenteredString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.loading"), this.left + this.panelWidth / 2, this.top + this.panelHeight / 2, -14740);
        }
    }

    private void drawComponentsContent(GuiGraphics guiGraphics, int n, int n2) {
        int n3;
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        int n4 = this.top + 95;
        int n5 = this.left + 18;
        int n6 = this.left + this.panelWidth - 18;
        int n7 = n6 - n5;
        int n8 = n5 + n7 * 78 / 100;
        int n9 = n4 + 18 + this.visibleRows * 17;
        MonitorNetwork.StatisticsSnapshot statisticsSnapshot = this.statisticsSnapshot != null && this.statisticsSnapshot.statisticsPage() == MonitorNetwork.StatisticsPage.COMPONENTS ? this.statisticsSnapshot : null;
        List<Object> list = statisticsSnapshot == null ? List.of() : statisticsSnapshot.componentGroups();
        guiGraphics.fill(n5, n4, n6, n9, palette.tableOuter());
        guiGraphics.fill(n5 + 1, n4 + 1, n6 - 1, n4 + 18, palette.tableHeader());
        guiGraphics.drawString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.components.item"), n5 + 24, n4 + 5, palette.text(), false);
        this.drawRight(guiGraphics, (Component)Component.translatable((String)"screen.forever_production_monitor.components.variants"), n8, n4 + 5, palette.accentB());
        this.drawRight(guiGraphics, (Component)Component.translatable((String)"screen.forever_production_monitor.stored"), n6 - 8, n4 + 5, palette.accentA());
        for (n3 = 0; n3 < this.visibleRows; ++n3) {
            boolean bl;
            int n10 = n4 + 18 + n3 * 17;
            guiGraphics.fill(n5 + 1, n10, n6 - 1, n10 + 17, n3 % 2 == 0 ? palette.rowEven() : palette.rowOdd());
            if (n3 >= list.size()) continue;
            MonitorNetwork.ComponentGroup componentGroup = (MonitorNetwork.ComponentGroup)list.get(n3);
            boolean bl2 = bl = n >= n5 + 1 && n < n6 - 1 && n2 >= n10 && n2 < n10 + 17;
            if (bl) {
                guiGraphics.fill(n5 + 1, n10, n6 - 1, n10 + 17, palette.hover());
                guiGraphics.fill(n5 + 1, n10, n5 + 3, n10 + 17, palette.accentB());
            }
            AEKeyRendering.drawInGui((Minecraft)this.minecraft, (GuiGraphics)guiGraphics, (int)(n5 + 4), (int)n10, (AEKey)componentGroup.baseKey());
            int n11 = Math.max(80, n8 - n5 - 115);
            String string = this.font.plainSubstrByWidth(AEKeyRendering.getDisplayName((AEKey)componentGroup.baseKey()).getString(), n11);
            guiGraphics.drawString(this.font, string, n5 + 25, n10 + 4, palette.text(), false);
            this.drawRight(guiGraphics, (Component)Component.literal((String)Integer.toString(componentGroup.variants())), n8, n10 + 4, palette.accentB());
            MutableComponent mutableComponent = componentGroup.infinite() ? Component.translatable((String)"screen.forever_production_monitor.infinite") : Component.literal((String)ProductionMonitorScreen.formatAmount(componentGroup.totalAmount()));
            this.drawRight(guiGraphics, (Component)mutableComponent, n6 - 8, n10 + 4, componentGroup.infinite() ? palette.accentB() : -3485478);
            if (!bl) continue;
            ArrayList<MutableComponent> arrayList = new ArrayList<MutableComponent>(AEKeyRendering.getTooltip((AEKey)componentGroup.representativeKey()));
            arrayList.add(Component.empty());
            arrayList.add(Component.translatable((String)"screen.forever_production_monitor.components.tooltip.variants", (Object[])new Object[]{componentGroup.variants()}).withColor(12490482));
            arrayList.add(Component.translatable((String)"screen.forever_production_monitor.components.tooltip.grouped").withColor(9410470));
            guiGraphics.renderComponentTooltip(this.font, arrayList, n, n2);
        }
        n3 = n9 + 6;
        guiGraphics.fill(n5, n3, n6, n3 + 19, palette.summary());
        guiGraphics.fill(n5, n3, n5 + 2, n3 + 19, palette.accentA());
        guiGraphics.fill(n6 - 2, n3, n6, n3 + 19, palette.accentB());
        MutableComponent mutableComponent = Component.translatable((String)"screen.forever_production_monitor.components.grouped_hint", (Object[])new Object[]{statisticsSnapshot == null ? 0 : statisticsSnapshot.totalEntries()});
        guiGraphics.drawCenteredString(this.font, (Component)mutableComponent, this.left + this.panelWidth / 2, n3 + 6, palette.text());
        this.drawStatisticsFooter(guiGraphics, statisticsSnapshot, n5, n6);
        if (statisticsSnapshot == null) {
            guiGraphics.drawCenteredString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.loading"), this.left + this.panelWidth / 2, n4 + (n9 - n4) / 2, -14740);
        } else {
            guiGraphics.drawCenteredString(this.font, (Component)Component.literal((String)(statisticsSnapshot.page() + 1 + " / " + statisticsSnapshot.pages())), this.left + this.panelWidth - 75, this.top + 68, palette.text());
            guiGraphics.drawString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.components.entries", (Object[])new Object[]{statisticsSnapshot.totalEntries()}), this.left + 18, this.top + 87, palette.muted(), false);
        }
    }

    private void drawDevicesContent(GuiGraphics guiGraphics, int n, int n2) {
        int n3;
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        int n4 = this.top + 95;
        int n5 = this.left + 18;
        int n6 = this.left + this.panelWidth - 18;
        int n7 = n6 - n5;
        int n8 = n5 + n7 * 60 / 100;
        int n9 = n5 + n7 * 72 / 100;
        int n10 = n5 + n7 * 84 / 100;
        int n11 = n10 - 8;
        int n12 = n6 - 18;
        int n13 = n12 - 6;
        int n14 = n4 + 18 + this.visibleRows * 17;
        MonitorNetwork.StatisticsSnapshot statisticsSnapshot = this.statisticsSnapshot != null && this.statisticsSnapshot.statisticsPage() == MonitorNetwork.StatisticsPage.DEVICES ? this.statisticsSnapshot : null;
        List<Object> list = statisticsSnapshot == null ? List.of() : statisticsSnapshot.deviceGroups();
        guiGraphics.fill(n5, n4, n6, n14, palette.tableOuter());
        guiGraphics.fill(n5 + 1, n4 + 1, n6 - 1, n4 + 18, palette.tableHeader());
        guiGraphics.drawString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.devices.device"), n5 + 24, n4 + 5, palette.text(), false);
        this.drawRight(guiGraphics, (Component)Component.translatable((String)"screen.forever_production_monitor.devices.count"), n8, n4 + 5, palette.text());
        this.drawRight(guiGraphics, (Component)Component.translatable((String)"screen.forever_production_monitor.devices.channels"), n9, n4 + 5, -8861464);
        this.drawRight(guiGraphics, (Component)Component.translatable((String)"screen.forever_production_monitor.devices.idle_power"), n11, n4 + 5, palette.accentA());
        this.drawRight(guiGraphics, (Component)Component.translatable((String)"screen.forever_production_monitor.devices.status"), n13, n4 + 5, palette.accentB());
        for (n3 = 0; n3 < this.visibleRows; ++n3) {
            boolean bl;
            boolean bl2;
            int n15 = n4 + 18 + n3 * 17;
            guiGraphics.fill(n5 + 1, n15, n6 - 1, n15 + 17, n3 % 2 == 0 ? palette.rowEven() : palette.rowOdd());
            if (n3 >= list.size()) continue;
            MonitorNetwork.DeviceGroup deviceGroup = (MonitorNetwork.DeviceGroup)list.get(n3);
            boolean bl3 = bl2 = n >= n5 + 1 && n < n6 - 1 && n2 >= n15 && n2 < n15 + 17;
            if (bl2) {
                guiGraphics.fill(n5 + 1, n15, n6 - 1, n15 + 17, palette.hover());
                guiGraphics.fill(n5 + 1, n15, n5 + 3, n15 + 17, palette.accentB());
            }
            AEKeyRendering.drawInGui((Minecraft)this.minecraft, (GuiGraphics)guiGraphics, (int)(n5 + 4), (int)n15, (AEKey)deviceGroup.visual());
            int n16 = Math.max(55, n8 - n5 - 82);
            guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(deviceGroup.name(), n16), n5 + 25, n15 + 4, palette.text(), false);
            this.drawRight(guiGraphics, (Component)Component.literal((String)Integer.toString(deviceGroup.count())), n8, n15 + 4, -3485478);
            this.drawRight(guiGraphics, (Component)Component.literal((String)Integer.toString(deviceGroup.assignedChannels())), n9, n15 + 4, deviceGroup.assignedChannels() == deviceGroup.count() ? -8861464 : -35716);
            this.drawRight(guiGraphics, (Component)Component.literal((String)ProductionMonitorScreen.formatAe(deviceGroup.idlePower())), n11, n15 + 4, palette.accentA());
            Component component = ProductionMonitorScreen.deviceStatus(deviceGroup);
            int n17 = Math.max(1, n13 - n10);
            MutableComponent mutableComponent = Component.literal((String)this.font.plainSubstrByWidth(component.getString(), n17));
            this.drawRight(guiGraphics, (Component)mutableComponent, n13, n15 + 4, ProductionMonitorScreen.deviceStatusColor(deviceGroup));
            boolean bl4 = bl = deviceGroup.locatableDevices() > 0 && n >= n12 && n < n12 + 13 && n2 >= n15 + 2 && n2 < n15 + 15;
            if (deviceGroup.locatableDevices() > 0) {
                this.drawLocatorIcon(guiGraphics, n12, n15 + 2, bl);
            }
            if (bl) {
                guiGraphics.renderTooltip(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.devices.locate", (Object[])new Object[]{deviceGroup.locatableDevices()}), n, n2);
                continue;
            }
            if (!bl2) continue;
            ArrayList<MutableComponent> arrayList = new ArrayList<MutableComponent>(AEKeyRendering.getTooltip((AEKey)deviceGroup.visual()));
            arrayList.add(Component.empty());
            arrayList.add(Component.translatable((String)"screen.forever_production_monitor.devices.tooltip.count", (Object[])new Object[]{deviceGroup.count()}).withColor(13291738));
            arrayList.add(Component.translatable((String)"screen.forever_production_monitor.devices.tooltip.channels", (Object[])new Object[]{deviceGroup.assignedChannels()}).withColor(7915752));
            arrayList.add(Component.translatable((String)"screen.forever_production_monitor.devices.tooltip.power", (Object[])new Object[]{ProductionMonitorScreen.formatAe(deviceGroup.idlePower())}).withColor(0xFFC66C));
            arrayList.add(Component.translatable((String)"screen.forever_production_monitor.devices.tooltip.per_device", (Object[])new Object[]{ProductionMonitorScreen.formatAe(deviceGroup.idlePower() / (double)Math.max(1, deviceGroup.count()))}).withColor(9410470));
            arrayList.add(Component.translatable((String)"screen.forever_production_monitor.devices.tooltip.states", (Object[])new Object[]{deviceGroup.active(), deviceGroup.missingChannel(), deviceGroup.unpowered(), deviceGroup.booting()}).withColor(12490482));
            guiGraphics.renderComponentTooltip(this.font, arrayList, n, n2);
        }
        n3 = n14 + 6;
        this.drawDeviceSummary(guiGraphics, statisticsSnapshot, n5, n6, n3);
        this.drawStatisticsFooter(guiGraphics, statisticsSnapshot, n5, n6);
        if (statisticsSnapshot == null) {
            guiGraphics.drawCenteredString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.loading"), this.left + this.panelWidth / 2, n4 + (n14 - n4) / 2, -14740);
        } else {
            guiGraphics.drawCenteredString(this.font, (Component)Component.literal((String)(statisticsSnapshot.page() + 1 + " / " + statisticsSnapshot.pages())), this.left + this.panelWidth - 75, this.top + 68, palette.text());
            MutableComponent mutableComponent = Component.translatable((String)"screen.forever_production_monitor.devices.entries", (Object[])new Object[]{statisticsSnapshot.totalEntries()});
            guiGraphics.drawString(this.font, (Component)mutableComponent, this.left + 18, this.top + 87, palette.muted(), false);
            if (statisticsSnapshot.deviceTotals().unresolvedChannels() > 0) {
                guiGraphics.drawString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.devices.unresolved", (Object[])new Object[]{statisticsSnapshot.deviceTotals().unresolvedChannels()}), this.left + 23 + this.font.width((FormattedText)mutableComponent), this.top + 87, -14740, false);
            }
        }
    }

    private void drawLocatorIcon(GuiGraphics guiGraphics, int n, int n2, boolean bl) {
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        guiGraphics.fill(n, n2, n + 13, n2 + 13, bl ? palette.accentA() : palette.border());
        guiGraphics.fill(n + 1, n2 + 1, n + 12, n2 + 12, bl ? palette.header() : palette.tableOuter());
        int n3 = bl ? palette.accentB() : palette.accentA();
        guiGraphics.fill(n + 3, n2 + 6, n + 10, n2 + 7, n3);
        guiGraphics.fill(n + 6, n2 + 3, n + 7, n2 + 10, n3);
        guiGraphics.fill(n + 5, n2 + 5, n + 8, n2 + 8, -856074);
    }

    private void drawDeviceSummary(GuiGraphics guiGraphics, MonitorNetwork.StatisticsSnapshot statisticsSnapshot, int n, int n2, int n3) {
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        guiGraphics.fill(n, n3, n2, n3 + 19, palette.summary());
        guiGraphics.fill(n, n3, n + 2, n3 + 19, palette.accentA());
        guiGraphics.fill(n2 - 2, n3, n2, n3 + 19, palette.accentB());
        if (statisticsSnapshot == null) {
            return;
        }
        MonitorNetwork.DeviceTotals deviceTotals = statisticsSnapshot.deviceTotals();
        int n4 = n2 - n;
        int n5 = n4 / 5;
        this.drawSummaryValue(guiGraphics, (Component)Component.translatable((String)"screen.forever_production_monitor.devices.summary.devices"), Integer.toString(deviceTotals.totalDevices()), n, n5, n3, palette.text());
        this.drawSummaryValue(guiGraphics, (Component)Component.translatable((String)"screen.forever_production_monitor.devices.summary.channels"), Integer.toString(deviceTotals.networkChannels()), n + n5, n5, n3, deviceTotals.missingChannels() > 0 ? -35716 : (deviceTotals.unresolvedChannels() > 0 ? -14740 : -8861464));
        this.drawSummaryValue(guiGraphics, (Component)Component.translatable((String)"screen.forever_production_monitor.devices.summary.idle"), ProductionMonitorScreen.formatAe(deviceTotals.idlePower()), n + n5 * 2, n5, n3, -14740);
        this.drawSummaryValue(guiGraphics, (Component)Component.translatable((String)"screen.forever_production_monitor.devices.summary.missing"), Integer.toString(deviceTotals.missingChannels()), n + n5 * 3, n5, n3, deviceTotals.missingChannels() > 0 ? -35716 : -9972847);
        this.drawSummaryValue(guiGraphics, (Component)Component.translatable((String)"screen.forever_production_monitor.devices.summary.unpowered"), Integer.toString(deviceTotals.unpoweredDevices()), n + n5 * 4, n4 - n5 * 4, n3, deviceTotals.unpoweredDevices() > 0 ? -35716 : -9972847);
    }

    private static Component deviceStatus(MonitorNetwork.DeviceGroup deviceGroup) {
        if (deviceGroup.missingChannel() > 0) {
            return Component.translatable((String)"screen.forever_production_monitor.devices.status.missing", (Object[])new Object[]{deviceGroup.missingChannel()});
        }
        if (deviceGroup.unpowered() > 0) {
            return Component.translatable((String)"screen.forever_production_monitor.devices.status.unpowered", (Object[])new Object[]{deviceGroup.unpowered()});
        }
        if (deviceGroup.booting() > 0) {
            return Component.translatable((String)"screen.forever_production_monitor.devices.status.booting", (Object[])new Object[]{deviceGroup.booting()});
        }
        return Component.translatable((String)"screen.forever_production_monitor.devices.status.active", (Object[])new Object[]{deviceGroup.active()});
    }

    private static int deviceStatusColor(MonitorNetwork.DeviceGroup deviceGroup) {
        if (deviceGroup.missingChannel() > 0 || deviceGroup.unpowered() > 0) {
            return -35716;
        }
        if (deviceGroup.booting() > 0) {
            return -14740;
        }
        return -9972847;
    }

    private void drawCapacityBar(GuiGraphics guiGraphics, int n, int n2, int n3, Component component, double d, String string) {
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        guiGraphics.fill(n, n3, n2, n3 + 27, palette.tableOuter());
        guiGraphics.fill(n + 1, n3 + 1, n2 - 1, n3 + 26, palette.rowEven());
        int n4 = n + 8;
        int n5 = n2 - 8;
        int n6 = n3 + 16;
        guiGraphics.fill(n4, n6, n5, n6 + 5, palette.outer());
        int n7 = n4 + (int)Math.round((double)(n5 - n4) * Math.max(0.0, Math.min(1.0, d)));
        if (n7 > n4) {
            guiGraphics.fill(n4, n6, n7, n6 + 5, palette.accentA());
            guiGraphics.fill(n4 + (n7 - n4) * 3 / 5, n6, n7, n6 + 5, palette.accentB());
        }
        guiGraphics.drawString(this.font, component, n + 8, n3 + 5, palette.muted(), false);
        this.drawRight(guiGraphics, (Component)Component.literal((String)string), n2 - 8, n3 + 5, palette.text());
    }

    private void drawStatCard(GuiGraphics guiGraphics, int n, int n2, int n3, Component component, String string, int n4) {
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        guiGraphics.fill(n, n2, n + n3, n2 + 43, palette.tableOuter());
        guiGraphics.fill(n + 1, n2 + 1, n + n3 - 1, n2 + 42, palette.rowEven());
        guiGraphics.drawCenteredString(this.font, component, n + n3 / 2, n2 + 8, palette.muted());
        guiGraphics.drawCenteredString(this.font, (Component)Component.literal((String)string), n + n3 / 2, n2 + 25, n4);
    }

    private void drawSourceLine(GuiGraphics guiGraphics, int n, int n2, Component component, int n3, int n4) {
        guiGraphics.fill(n, n2 + 2, n + 4, n2 + 10, n4);
        guiGraphics.drawString(this.font, component, n + 9, n2 + 2, InterfaceTheme.current().text(), false);
        guiGraphics.drawString(this.font, Integer.toString(n3), n + 9 + this.font.width((FormattedText)component) + 5, n2 + 2, n4, false);
    }

    private void drawStatisticsFooter(GuiGraphics guiGraphics, MonitorNetwork.StatisticsSnapshot statisticsSnapshot, int n, int n2) {
        int n3 = this.top + this.panelHeight - 29;
        String string = String.valueOf(this.link.dimension()) + "  " + this.link.pos().toShortString();
        guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(string, (n2 - n) / 2), n, n3, InterfaceTheme.current().muted(), false);
        if (statisticsSnapshot != null) {
            this.drawStatus(guiGraphics, statisticsSnapshot.status(), n2, n3);
        }
    }

    private void drawStatus(GuiGraphics guiGraphics, MonitorNetwork.Status status, int n, int n2) {
        MutableComponent mutableComponent = Component.translatable((String)("screen.forever_production_monitor.status." + status.name().toLowerCase()));
        int n3 = status == MonitorNetwork.Status.ONLINE ? -9972847 : (status == MonitorNetwork.Status.WARMING_UP ? -14740 : -35716);
        this.drawRight(guiGraphics, (Component)mutableComponent, n, n2, n3);
    }

    private void drawSummary(GuiGraphics guiGraphics, int n, int n2, int n3) {
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        guiGraphics.fill(n, n3, n2, n3 + 19, palette.summary());
        guiGraphics.fill(n, n3, n + 2, n3 + 19, palette.accentA());
        guiGraphics.fill(n2 - 2, n3, n2, n3 + 19, palette.accentB());
        if (this.snapshot == null) {
            return;
        }
        int n4 = n2 - n;
        int n5 = n4 / 5;
        this.drawSummaryValue(guiGraphics, (Component)Component.translatable((String)"screen.forever_production_monitor.summary.items"), ProductionMonitorScreen.formatAmount(this.snapshot.totalStored()), n, n5, n3, palette.text());
        this.drawSummaryValue(guiGraphics, (Component)Component.translatable((String)"screen.forever_production_monitor.summary.types"), Integer.toString(this.snapshot.totalTypes()), n + n5, n5, n3, -3485478);
        this.drawSummaryValue(guiGraphics, (Component)Component.translatable((String)"screen.forever_production_monitor.summary.incoming"), "+" + ProductionMonitorScreen.formatAmount(ProductionMonitorScreen.rateUnit().scale(this.snapshot.totalIncomingPerMinute())) + ProductionMonitorScreen.rateUnit().suffix(), n + n5 * 2, n5, n3, -9972847);
        this.drawSummaryValue(guiGraphics, (Component)Component.translatable((String)"screen.forever_production_monitor.summary.outgoing"), "\u2212" + ProductionMonitorScreen.formatAmount(ProductionMonitorScreen.rateUnit().scale(this.snapshot.totalOutgoingPerMinute())) + ProductionMonitorScreen.rateUnit().suffix(), n + n5 * 3, n5, n3, -35716);
        this.drawSummaryValue(guiGraphics, (Component)Component.translatable((String)"screen.forever_production_monitor.summary.net"), ProductionMonitorScreen.formatRate(this.snapshot.totalNetPerMinute()), n + n5 * 4, n4 - n5 * 4, n3, ProductionMonitorScreen.rateColor(this.snapshot.totalNetPerMinute()));
    }

    private void drawSummaryValue(GuiGraphics guiGraphics, Component component, String string, int n, int n2, int n3, int n4) {
        MutableComponent mutableComponent = Component.literal((String)(component.getString() + " ")).append((Component)Component.literal((String)string));
        int n5 = this.font.width((FormattedText)mutableComponent);
        int n6 = n + Math.max(3, (n2 - n5) / 2);
        guiGraphics.drawString(this.font, component, n6, n3 + 6, InterfaceTheme.current().muted(), false);
        guiGraphics.drawString(this.font, string, n6 + this.font.width((FormattedText)component) + 3, n3 + 6, n4, false);
    }

    public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        if (this.viewMode == ViewMode.MAP && this.networkMapView != null && this.networkMapView.mouseScrolled(d, d2, d4)) {
            return true;
        }
        if (d4 > 0.0 && this.requestedPage > 0) {
            --this.requestedPage;
            this.requestNow();
            return true;
        }
        int n = this.currentPages();
        if (d4 < 0.0 && this.viewMode != ViewMode.STORAGE && this.viewMode != ViewMode.MAP && this.requestedPage + 1 < n) {
            ++this.requestedPage;
            this.requestNow();
            return true;
        }
        return super.mouseScrolled(d, d2, d3, d4);
    }

    public boolean mouseClicked(double d, double d2, int n) {
        Object object;
        int n2;
        int n3;
        int n4;
        int n5;
        int n6;
        if (this.viewMode == ViewMode.MAP && this.networkMapView != null && this.networkMapView.mouseClicked(d, d2, n)) {
            return true;
        }
        if (n == 0 && this.viewMode == ViewMode.DASHBOARD && this.dashboardSnapshot != null) {
            n6 = this.top + 66;
            n5 = this.left + this.panelWidth - 18;
            n4 = (int)((d2 - (double)n6 - 21.0) / 28.0);
            n3 = this.requestedPage * this.dashboardCapacity() + n4;
            if (n4 >= 0 && n3 >= 0 && n3 < this.dashboardSnapshot.entries().size()) {
                n2 = n6 + 21 + n4 * 28;
                object = this.dashboardSnapshot.entries().get(n3);
                if (d2 >= (double)n2 && d2 < (double)(n2 + 27) && d >= (double)(n5 - 22) && d < (double)(n5 - 3)) {
                    MonitorNetwork.updateDashboardPin(this.link, MonitorNetwork.DashboardAction.REMOVE, ((MonitorNetwork.DashboardEntry)object).kind(), ((MonitorNetwork.DashboardEntry)object).key(), ProductionMonitorBlockEntity.AlarmMode.NONE, 0L, 0, 0L);
                    return true;
                }
                if (d2 >= (double)n2 && d2 < (double)(n2 + 27) && d >= (double)(n5 - 44) && d < (double)(n5 - 24)) {
                    this.minecraft.setScreen((Screen)new AlarmRuleScreen(this, this.link, (MonitorNetwork.DashboardEntry)object));
                    return true;
                }
            }
        }
        if (n == 0 && this.viewMode == ViewMode.PRODUCTION && this.snapshot != null) {
            n6 = this.top + 95;
            n5 = this.left + this.panelWidth - 18;
            n4 = (int)((d2 - (double)n6 - 18.0) / 17.0);
            if (d >= (double)(n5 - 22) && d < (double)n5 && n4 >= 0 && n4 < this.snapshot.entries().size() && d2 >= (double)(n3 = n6 + 18 + n4 * 17) && d2 < (double)(n3 + 17)) {
                MonitorNetwork.Entry entry = this.snapshot.entries().get(n4);
                object = this.isPinned(entry.kind(), entry.key()) ? MonitorNetwork.DashboardAction.REMOVE : MonitorNetwork.DashboardAction.ADD;
                MonitorNetwork.updateDashboardPin(this.link, (MonitorNetwork.DashboardAction)((Object)object), entry.kind(), entry.key(), ProductionMonitorBlockEntity.AlarmMode.NONE, 0L, 0, 0L);
                MonitorNetwork.requestDashboard(this.link);
                return true;
            }
        }
        if (n == 0 && this.viewMode == ViewMode.DEVICES && this.statisticsSnapshot != null && this.statisticsSnapshot.statisticsPage() == MonitorNetwork.StatisticsPage.DEVICES) {
            n6 = this.top + 95;
            n5 = this.left + this.panelWidth - 18;
            n4 = n5 - 18;
            n3 = n6 + 18;
            n2 = (int)((d2 - (double)n3) / 17.0);
            object = this.statisticsSnapshot.deviceGroups();
            if (d >= (double)n4 && d < (double)(n4 + 13) && n2 >= 0 && n2 < object.size()) {
                int n7 = n3 + n2 * 17;
                MonitorNetwork.DeviceGroup deviceGroup = (MonitorNetwork.DeviceGroup)object.get(n2);
                if (d2 >= (double)(n7 + 2) && d2 < (double)(n7 + 15) && deviceGroup.locatableDevices() > 0) {
                    String string = String.valueOf(deviceGroup.visual().getId()) + "\u0000" + deviceGroup.name();
                    int n8 = this.locatorIndexes.getOrDefault(string, 0) % deviceGroup.locatableDevices();
                    this.locatorIndexes.put(string, (n8 + 1) % deviceGroup.locatableDevices());
                    MonitorNetwork.requestLocate(this.link, deviceGroup, n8);
                    return true;
                }
            }
        }
        if (n == 1 && this.search != null && this.search.isMouseOver(d, d2)) {
            this.search.setValue("");
            this.searchDelay = 0;
            this.search.setFocused(true);
            this.requestedPage = 0;
            this.requestNow();
            return true;
        }
        return super.mouseClicked(d, d2, n);
    }

    public boolean mouseDragged(double d, double d2, int n, double d3, double d4) {
        if (this.viewMode == ViewMode.MAP && this.networkMapView != null && this.networkMapView.mouseDragged(d, d2, n, d3, d4)) {
            return true;
        }
        return super.mouseDragged(d, d2, n, d3, d4);
    }

    public boolean mouseReleased(double d, double d2, int n) {
        if (this.viewMode == ViewMode.MAP && this.networkMapView != null && this.networkMapView.mouseReleased(d, d2, n)) {
            return true;
        }
        return super.mouseReleased(d, d2, n);
    }

    private boolean isPinned(MonitorNetwork.EntryKind entryKind, AEKey aEKey) {
        return this.dashboardSnapshot != null && this.dashboardSnapshot.entries().stream().anyMatch(dashboardEntry -> dashboardEntry.kind() == entryKind && dashboardEntry.key().equals(aEKey));
    }

    public boolean keyPressed(int n, int n2, int n3) {
        if (this.viewMode == ViewMode.MAP && this.search != null && this.search.isFocused() && (n == 257 || n == 335) && this.networkMapView != null) {
            this.networkMapView.focusNextSearchMatch();
            return true;
        }
        if (this.viewMode == ViewMode.MAP && this.networkMapView != null && this.networkMapView.keyPressed(n)) {
            return true;
        }
        if (this.minecraft != null && this.minecraft.options.keyInventory.matches(n, n2) && (this.search == null || !this.search.isFocused())) {
            this.onClose();
            return true;
        }
        return super.keyPressed(n, n2, n3);
    }

    private void drawRight(GuiGraphics guiGraphics, Component component, int n, int n2, int n3) {
        guiGraphics.drawString(this.font, component, n - this.font.width((FormattedText)component), n2, n3, false);
    }

    private static int rateColor(long l) {
        return l > 0L ? -9972847 : (l < 0L ? -35716 : -7366746);
    }

    private static Component displayName(MonitorNetwork.Entry entry) {
        return entry.kind() == MonitorNetwork.EntryKind.ENERGY ? Component.translatable((String)"screen.forever_production_monitor.energy") : AEKeyRendering.getDisplayName((AEKey)entry.key());
    }

    private static ClientConfig.RateUnit rateUnit() {
        return (ClientConfig.RateUnit)((Object)ClientConfig.VALUES.rateUnit.get());
    }

    private static String formatRate(long l) {
        return ProductionMonitorScreen.formatRate(l, MonitorNetwork.EntryKind.ITEM);
    }

    private static String formatRate(long l, MonitorNetwork.EntryKind entryKind) {
        if (l == 0L) {
            return "\u2014";
        }
        return (l > 0L ? "+" : "") + ProductionMonitorScreen.formatAmount(ProductionMonitorScreen.rateUnit().scale(l)) + ProductionMonitorScreen.quantitySuffix(entryKind) + ProductionMonitorScreen.rateUnit().suffix();
    }

    private static String formatStored(long l, MonitorNetwork.EntryKind entryKind) {
        return ProductionMonitorScreen.formatAmount(l) + ProductionMonitorScreen.quantitySuffix(entryKind);
    }

    private static String formatBytes(long l) {
        return ProductionMonitorScreen.formatAmount(l) + " B";
    }

    private static String formatAe(double d) {
        return ProductionMonitorScreen.formatAmount(d) + " AE/t";
    }

    private static String formatAmount(long l) {
        return ProductionMonitorScreen.formatAmount((double)l);
    }

    private static String formatAmount(double d) {
        double d2 = Math.abs(d);
        if (d2 < 1000.0) {
            if (Math.abs(d - Math.rint(d)) < 1.0E-6) {
                return String.format(Locale.ROOT, "%.0f", d);
            }
            return String.format(Locale.ROOT, d2 >= 10.0 ? "%.1f" : "%.2f", d);
        }
        if (d2 < 1000000.0) {
            return String.format(Locale.ROOT, "%.2fk", d / 1000.0);
        }
        if (d2 < 1.0E9) {
            return String.format(Locale.ROOT, "%.2fM", d / 1000000.0);
        }
        if (d2 < 1.0E12) {
            return String.format(Locale.ROOT, "%.2fG", d / 1.0E9);
        }
        return String.format(Locale.ROOT, "%.2fT", d / 1.0E12);
    }

    private static String quantitySuffix(MonitorNetwork.EntryKind entryKind) {
        return switch (entryKind) {
            default -> throw new IncompatibleClassChangeError();
            case MonitorNetwork.EntryKind.ITEM -> "";
            case MonitorNetwork.EntryKind.FLUID -> " mB";
            case MonitorNetwork.EntryKind.ENERGY -> " FE";
        };
    }

    public boolean isPauseScreen() {
        return false;
    }

    private static enum ViewMode {
        DASHBOARD,
        PRODUCTION,
        STORAGE,
        COMPONENTS,
        DEVICES,
        MAP;

    }

    private static final class GearButton
    extends Button {
        private GearButton(int n, int n2, Button.OnPress onPress) {
            super(n, n2, 18, 18, (Component)Component.translatable((String)"screen.forever_production_monitor.theme.open"), onPress, DEFAULT_NARRATION);
        }

        protected void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
            InterfaceTheme.Palette palette = InterfaceTheme.current();
            boolean bl = this.isHoveredOrFocused();
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), bl ? palette.accentA() : palette.border());
            guiGraphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.getWidth() - 1, this.getY() + this.getHeight() - 1, bl ? palette.tableHeader() : palette.summary());
            int n3 = bl ? palette.accentB() : palette.text();
            int n4 = this.getX() + 9;
            int n5 = this.getY() + 9;
            guiGraphics.fill(n4 - 1, n5 - 6, n4 + 2, n5 + 7, n3);
            guiGraphics.fill(n4 - 6, n5 - 1, n4 + 7, n5 + 2, n3);
            guiGraphics.fill(n4 - 4, n5 - 4, n4 + 5, n5 + 5, n3);
            guiGraphics.fill(n4 - 2, n5 - 2, n4 + 3, n5 + 3, palette.tableOuter());
            guiGraphics.fill(n4 - 1, n5 - 1, n4 + 2, n5 + 2, n3);
        }
    }
}

