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
import appeng.core.definitions.AEItems;
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
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public final class ProductionMonitorScreen
extends Screen {
    private static final int MAX_PANEL_WIDTH = 960;
    private static final int MAX_PANEL_HEIGHT = 560;
    private static final int HEADER_HEIGHT = 32;
    private static final int ROW_HEIGHT = 17;
    private static final int DIMENSION_DROPDOWN_ROW_HEIGHT = 16;
    private static final int DIMENSION_DROPDOWN_MAX_ROWS = 8;
    private static final int MAP_VIEW_TOOLBAR_HEIGHT = 24;
    private static final int MAP_CONTROL_BAR_HEIGHT = 28;
    private static final int DEEP_CORE_HEADER_BUTTON_HEIGHT = 18;
    private static final int DEEP_CORE_MENU_ROW_HEIGHT = 24;
    private static final int DEEP_CORE_MENU_MAX_ROWS = 6;
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
    private ForeverButton craftingTab;
    private GearButton settingsButton;
    private MonitorNetwork.MonitorSnapshot snapshot;
    private MonitorNetwork.StatisticsSnapshot statisticsSnapshot;
    private MonitorNetwork.DashboardSnapshot dashboardSnapshot;
    private MonitorNetwork.NetworkMapPayload networkMapSnapshot;
    private NetworkMapView networkMapView;
    private ResourceLocation mapViewDimension;
    private BlockPos pendingMapFocus;
    private boolean mapAutoInitialized;
    private boolean mapAutoDimensionPending;
    private boolean dimensionDropdownOpen;
    private int dimensionDropdownScroll;
    private int dimensionSelectorX;
    private int dimensionSelectorY;
    private int dimensionSelectorWidth;
    private int dimensionSelectorHeight;
    private int dimensionMenuX;
    private int dimensionMenuY;
    private int dimensionMenuWidth;
    private int dimensionMenuRows;
    private List<MonitorNetwork.DeepCoreFacility> deepCoreFacilities = List.of();
    private boolean deepCoreDropdownOpen;
    private int deepCoreDropdownScroll;
    private int deepCoreRefreshTicks;
    private int deepCoreButtonX;
    private int deepCoreButtonY;
    private int deepCoreButtonWidth;
    private int deepCoreMenuX;
    private int deepCoreMenuY;
    private int deepCoreMenuWidth;
    private int deepCoreMenuRows;
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
    private int contentLeft;
    private int contentRight;
    private int secondaryY;
    private int controlsY;
    private int auxiliaryControlsY;
    private int contentY;
    private int contentBottom;
    private int footerY;
    private boolean compactControls;
    private long contentTransitionStartedNanos;
    private int contentTransitionDirection = 1;
    private long dataPulseStartedNanos;
    private final Map<String, Integer> locatorIndexes = new HashMap<String, Integer>();

    public ProductionMonitorScreen(ProductionTabletItem.MonitorLink monitorLink) {
        super((Component)Component.translatable((String)"screen.forever_production_monitor.title"));
        this.link = monitorLink;
        this.mapViewDimension = monitorLink.dimension();
        this.viewMode = (Boolean)ClientConfig.VALUES.rememberLastTab.get() != false && lastViewMode != null ? lastViewMode : ViewMode.valueOf(((ClientConfig.DefaultTab)((Object)ClientConfig.VALUES.defaultTab.get())).name());
    }

    protected void init() {
        String string2 = this.search == null ? "" : this.search.getValue();
        this.panelWidth = Math.max(1, Math.min(MAX_PANEL_WIDTH, this.width - 12));
        this.panelHeight = Math.max(1, Math.min(MAX_PANEL_HEIGHT, this.height - 12));
        this.left = (this.width - this.panelWidth) / 2;
        this.top = (this.height - this.panelHeight) / 2;
        this.contentLeft = this.left + 18;
        this.contentRight = this.left + this.panelWidth - 18;
        this.footerY = this.top + this.panelHeight - 29;
        this.contentBottom = this.footerY - 8;
        int n = this.contentLeft;
        int n2 = Math.max(5, this.contentRight - this.contentLeft);
        int n3 = 4;
        int n4 = Math.max(1, (n2 - n3 * 5) / 6);
        this.dashboardTab = (ForeverButton)this.addRenderableWidget(ForeverButton.create((Component)Component.translatable((String)"screen.forever_production_monitor.tab.dashboard"), button -> this.switchView(ViewMode.DASHBOARD), ForeverButton.Style.SECONDARY, n, this.top + 37, n4, 18).setRole(ForeverButton.Role.TAB));
        this.productionTab = (ForeverButton)this.addRenderableWidget(ForeverButton.create((Component)Component.translatable((String)"screen.forever_production_monitor.tab.production"), button -> this.switchView(ViewMode.PRODUCTION), ForeverButton.Style.SECONDARY, n + n4 + n3, this.top + 37, n4, 18).setRole(ForeverButton.Role.TAB));
        this.storageTab = (ForeverButton)this.addRenderableWidget(ForeverButton.create((Component)Component.translatable((String)"screen.forever_production_monitor.tab.storage"), button -> this.switchView(ViewMode.STORAGE), ForeverButton.Style.SECONDARY, n + (n4 + n3) * 2, this.top + 37, n4, 18).setRole(ForeverButton.Role.TAB));
        this.devicesTab = (ForeverButton)this.addRenderableWidget(ForeverButton.create((Component)Component.translatable((String)"screen.forever_production_monitor.tab.devices"), button -> this.switchView(ViewMode.DEVICES), ForeverButton.Style.SECONDARY, n + (n4 + n3) * 3, this.top + 37, n4, 18).setRole(ForeverButton.Role.TAB));
        this.mapTab = (ForeverButton)this.addRenderableWidget(ForeverButton.create((Component)Component.translatable((String)"screen.forever_production_monitor.tab.map"), button -> this.switchView(ViewMode.MAP), ForeverButton.Style.SECONDARY, n + (n4 + n3) * 4, this.top + 37, n4, 18).setRole(ForeverButton.Role.TAB));
        this.craftingTab = (ForeverButton)this.addRenderableWidget(ForeverButton.create(Component.translatable("screen.forever_production_monitor.tab.crafting"), button -> this.minecraft.setScreen(new CraftingDiagnosticsScreen(this, this.link)), ForeverButton.Style.SECONDARY, n + (n4 + n3) * 5, this.top + 37, n4, 18).setRole(ForeverButton.Role.TAB));
        this.settingsButton = (GearButton)this.addRenderableWidget(new GearButton(this.left + this.panelWidth - 25, this.top + 7, button -> this.minecraft.setScreen((Screen)new ProductionMonitorThemeScreen(this))));
        int n5 = Math.max(60, Math.min(260, n2));
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
        this.addRenderableWidget(this.search);
        if (this.networkMapView == null) {
            this.networkMapView = new NetworkMapView(this.minecraft, this.font, this::followNetworkPath);
        }
        if (this.viewMode == ViewMode.MAP && !this.mapAutoInitialized) {
            this.prepareMapForCurrentDimension();
        }
        this.networkMapView.setBounds(this.contentLeft, this.top + 62, n2, Math.max(24, this.contentBottom - this.top - 62));
        if (this.networkMapSnapshot != null
                && this.networkMapSnapshot.viewDimension().equals((Object)this.mapViewDimension)) {
            this.networkMapView.accept(this.networkMapSnapshot);
        }
        this.storageCapacityButton = (ForeverButton)this.addRenderableWidget(ForeverButton.create((Component)Component.translatable((String)"screen.forever_production_monitor.storage.section.capacity"), button -> this.switchView(ViewMode.STORAGE), ForeverButton.Style.SECONDARY, n, this.top + 62, n4, 20));
        this.nbtItemsButton = (ForeverButton)this.addRenderableWidget(ForeverButton.create((Component)Component.translatable((String)"screen.forever_production_monitor.storage.section.nbt_items"), button -> this.switchView(ViewMode.COMPONENTS), ForeverButton.Style.SECONDARY, n + n4 + n3, this.top + 62, n4, 20));
        int n6 = this.contentRight - 74;
        int n7 = this.contentRight - 34;
        this.sortX = this.contentLeft;
        this.sortWidth = Math.max(44, n2 - 82);
        this.sortButton = (ForeverButton)this.addRenderableWidget(ForeverButton.create(this.sortLabel(), button -> {
            if (this.viewMode == ViewMode.DEVICES) {
                this.deviceSort = this.deviceSort.next();
            } else {
                this.sort = this.sort.next();
            }
            this.requestedPage = 0;
            this.sortButton.setMessage(this.sortLabel());
            this.requestNow();
        }, ForeverButton.Style.VIOLET, this.sortX, this.top + 62, this.sortWidth, 20));
        this.filterButton = (ForeverButton)this.addRenderableWidget(ForeverButton.create(this.filterLabel(), button -> {
            this.deviceFilter = this.deviceFilter.next();
            this.requestedPage = 0;
            this.filterButton.setMessage(this.filterLabel());
            this.requestNow();
        }, ForeverButton.Style.SECONDARY, this.sortX, this.top + 62, this.sortWidth, 20));
        this.previousButton = (ForeverButton)this.addRenderableWidget(ForeverButton.create((Component)Component.literal((String)"\u2039"), button -> {
            if (this.requestedPage > 0) {
                --this.requestedPage;
                this.requestNow();
            }
        }, ForeverButton.Style.SECONDARY, n6, this.top + 62, 34, 20));
        this.nextButton = (ForeverButton)this.addRenderableWidget(ForeverButton.create((Component)Component.literal((String)"\u203a"), button -> {
            if (this.requestedPage + 1 < this.currentPages()) {
                ++this.requestedPage;
                this.requestNow();
            }
        }, ForeverButton.Style.SECONDARY, n7, this.top + 62, 34, 20));
        this.updateViewWidgets();
        this.requestNow();
        this.deepCoreDropdownOpen = false;
        this.deepCoreDropdownScroll = 0;
        this.deepCoreRefreshTicks = 100;
        MonitorNetwork.requestDeepCoreFacilities(this.link);
    }

    private void switchView(ViewMode viewMode) {
        if (this.viewMode == viewMode) {
            return;
        }
        this.contentTransitionDirection = viewMode.ordinal() >= this.viewMode.ordinal() ? 1 : -1;
        this.viewMode = viewMode;
        this.contentTransitionStartedNanos = GuiMotion.enabled() ? GuiMotion.now() : 0L;
        lastViewMode = viewMode;
        this.requestedPage = 0;
        this.statisticsSnapshot = null;
        if (viewMode == ViewMode.MAP) {
            this.prepareMapForCurrentDimension();
            if (this.networkMapView != null && this.search != null) {
                this.networkMapView.setSearch(this.search.getValue());
            }
        } else {
            this.dimensionDropdownOpen = false;
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
        this.layoutCurrentView();
        this.search.active = this.search.visible = this.viewMode != ViewMode.STORAGE && this.viewMode != ViewMode.DASHBOARD;
        this.search.setHint((Component)Component.translatable((String)(this.viewMode == ViewMode.DEVICES ? "screen.forever_production_monitor.search.devices" : (this.viewMode == ViewMode.MAP ? "screen.forever_production_monitor.search.map" : (this.viewMode == ViewMode.COMPONENTS ? "screen.forever_production_monitor.search.components" : "screen.forever_production_monitor.search")))));
        int innerWidth = Math.max(1, this.contentRight - this.contentLeft);
        int pagingWidth = 114;
        int searchWidth = this.viewMode == ViewMode.MAP ? Math.min(200, Math.max(150, innerWidth / 4)) : (this.compactControls ? innerWidth : Math.max(100, Math.min(260, innerWidth * 2 / 5)));
        this.search.setX(this.viewMode == ViewMode.MAP ? this.contentLeft + 8 : this.contentLeft);
        this.search.setY(this.viewMode == ViewMode.MAP ? this.auxiliaryControlsY : this.controlsY);
        this.search.setWidth(Math.max(1, searchWidth));
        if (this.viewMode == ViewMode.COMPONENTS) {
            this.search.setWidth(Math.max(1, innerWidth - pagingWidth - 8));
        }
        int pagingX = this.contentRight - pagingWidth;
        int pagingY = this.viewMode == ViewMode.COMPONENTS || this.compactControls ? this.auxiliaryControlsY : this.controlsY;
        this.sortX = this.compactControls ? this.contentLeft : this.search.getX() + this.search.getWidth() + 8;
        this.sortWidth = Math.max(44, pagingX - 8 - this.sortX);
        this.sortButton.visible = bl2 = this.viewMode == ViewMode.PRODUCTION || this.viewMode == ViewMode.DEVICES;
        this.sortButton.active = bl2;
        int n2 = 4;
        int n3 = Math.max(20, (this.sortWidth - n2) / 2);
        this.sortButton.setX(this.sortX);
        this.sortButton.setY(this.compactControls ? this.auxiliaryControlsY : this.controlsY);
        this.sortButton.setWidth(this.viewMode == ViewMode.DEVICES ? n3 : this.sortWidth);
        this.sortButton.setMessage(this.sortLabel());
        this.filterButton.visible = this.viewMode == ViewMode.DEVICES;
        this.filterButton.active = this.viewMode == ViewMode.DEVICES;
        this.filterButton.setX(this.sortX + n3 + n2);
        this.filterButton.setY(this.compactControls ? this.auxiliaryControlsY : this.controlsY);
        this.filterButton.setWidth(Math.max(1, this.sortWidth - n3 - n2));
        this.filterButton.setMessage(this.filterLabel());
        this.previousButton.visible = this.viewMode != ViewMode.STORAGE && this.viewMode != ViewMode.MAP;
        this.nextButton.visible = this.viewMode != ViewMode.STORAGE && this.viewMode != ViewMode.MAP;
        this.previousButton.setX(pagingX);
        this.previousButton.setY(pagingY);
        this.nextButton.setX(pagingX + 80);
        this.nextButton.setY(pagingY);
        this.storageCapacityButton.visible = bl = this.viewMode == ViewMode.STORAGE || this.viewMode == ViewMode.COMPONENTS;
        this.storageCapacityButton.active = bl;
        this.nbtItemsButton.visible = bl;
        this.nbtItemsButton.active = bl;
        int secondaryWidth = Math.max(1, (innerWidth - 4) / 2);
        this.storageCapacityButton.setX(this.contentLeft);
        this.storageCapacityButton.setY(this.secondaryY);
        this.storageCapacityButton.setWidth(secondaryWidth);
        this.nbtItemsButton.setX(this.contentLeft + secondaryWidth + 4);
        this.nbtItemsButton.setY(this.secondaryY);
        this.nbtItemsButton.setWidth(Math.max(1, innerWidth - secondaryWidth - 4));
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
        this.craftingTab.setStyle(ForeverButton.Style.THEMED);
        this.sortButton.setStyle(ForeverButton.Style.THEMED_ACTIVE);
        this.filterButton.setStyle(ForeverButton.Style.THEMED);
        this.previousButton.setStyle(ForeverButton.Style.THEMED);
        this.nextButton.setStyle(ForeverButton.Style.THEMED);
        this.updateButtons();
    }

    private void layoutCurrentView() {
        int innerWidth = Math.max(1, this.contentRight - this.contentLeft);
        this.secondaryY = this.top + 62;
        this.controlsY = this.top + 62;
        this.auxiliaryControlsY = this.top + 86;
        if (this.viewMode == ViewMode.COMPONENTS) {
            this.controlsY = this.auxiliaryControlsY;
        }
        this.compactControls = innerWidth < 520 && (this.viewMode == ViewMode.PRODUCTION || this.viewMode == ViewMode.DEVICES);
        this.contentY = switch (this.viewMode) {
            case DASHBOARD -> this.top + 91;
            case STORAGE -> this.top + 91;
            case COMPONENTS -> this.top + 119;
            case PRODUCTION, DEVICES -> this.top + (this.compactControls ? 119 : 95);
            case MAP -> this.top + 62;
        };
        if (this.viewMode == ViewMode.MAP) {
            // The map now uses the entire content area. Search/help are overlays instead of
            // reserving permanent rows that shrink the 3D viewport.
            this.auxiliaryControlsY = this.contentBottom - 24;
            this.networkMapView.setBounds(this.contentLeft, this.contentY, innerWidth, Math.max(24, this.contentBottom - this.contentY));
        }
        int rowsHeight = this.contentBottom - this.contentY - 18 - 31;
        this.visibleRows = Math.max(1, Math.min(18, rowsHeight / ROW_HEIGHT));
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
            MonitorNetwork.requestNetworkMap(this.link, this.mapViewDimension);
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
        if (--this.deepCoreRefreshTicks <= 0) {
            this.deepCoreRefreshTicks = 100;
            MonitorNetwork.requestDeepCoreFacilities(this.link);
        }
    }

    public void acceptDeepCoreFacilities(MonitorNetwork.DeepCoreFacilitiesPayload payload) {
        if (!payload.monitorDimension().equals(this.link.dimension())
                || !payload.monitorPos().equals(this.link.pos())) return;
        this.deepCoreFacilities = payload.facilities();
        this.clampDeepCoreDropdownScroll();
        if (this.deepCoreFacilities.isEmpty()) this.deepCoreDropdownOpen = false;
    }

    public void accept(MonitorNetwork.MonitorSnapshot monitorSnapshot) {
        if (this.viewMode == ViewMode.PRODUCTION && monitorSnapshot.dimension().equals((Object)this.link.dimension()) && monitorSnapshot.pos().equals((Object)this.link.pos())) {
            this.snapshot = monitorSnapshot;
            this.requestedPage = monitorSnapshot.page();
            this.dataPulseStartedNanos = GuiMotion.enabled() ? GuiMotion.now() : 0L;
            this.updateButtons();
        }
    }

    public void acceptStatistics(MonitorNetwork.StatisticsSnapshot statisticsSnapshot) {
        MonitorNetwork.StatisticsPage statisticsPage = switch (this.viewMode) {
            case STORAGE -> MonitorNetwork.StatisticsPage.STORAGE;
            case COMPONENTS -> MonitorNetwork.StatisticsPage.COMPONENTS;
            case DEVICES -> MonitorNetwork.StatisticsPage.DEVICES;
            case PRODUCTION, DASHBOARD, MAP -> null;
        };
        if (this.viewMode != ViewMode.PRODUCTION && statisticsSnapshot.statisticsPage() == statisticsPage && statisticsSnapshot.dimension().equals((Object)this.link.dimension()) && statisticsSnapshot.pos().equals((Object)this.link.pos())) {
            this.statisticsSnapshot = statisticsSnapshot;
            this.requestedPage = statisticsSnapshot.page();
            this.dataPulseStartedNanos = GuiMotion.enabled() ? GuiMotion.now() : 0L;
            this.updateButtons();
        }
    }

    public void acceptDashboard(MonitorNetwork.DashboardSnapshot dashboardSnapshot) {
        if (dashboardSnapshot.dimension().equals((Object)this.link.dimension()) && dashboardSnapshot.pos().equals((Object)this.link.pos())) {
            this.dashboardSnapshot = dashboardSnapshot;
            this.dataPulseStartedNanos = GuiMotion.enabled() ? GuiMotion.now() : 0L;
            this.updateButtons();
        }
    }

    public void acceptNetworkMap(MonitorNetwork.NetworkMapPayload networkMapPayload) {
        if (networkMapPayload.dimension().equals((Object)this.link.dimension())
                && networkMapPayload.pos().equals((Object)this.link.pos())
                && networkMapPayload.viewDimension().equals((Object)this.mapViewDimension)) {
            if (this.mapAutoDimensionPending) {
                this.mapAutoDimensionPending = false;
                if (!networkMapPayload.dimensions().isEmpty()
                        && !networkMapPayload.dimensions().contains(this.mapViewDimension)) {
                    ResourceLocation fallback = networkMapPayload.dimensions().contains(this.link.dimension())
                            ? this.link.dimension()
                            : networkMapPayload.dimensions().get(0);
                    if (!fallback.equals(this.mapViewDimension)) {
                        this.switchMapDimension(fallback, null, false);
                        return;
                    }
                }
            }
            this.networkMapSnapshot = networkMapPayload;
            this.clampDimensionDropdownScroll();
            this.dataPulseStartedNanos = GuiMotion.enabled() ? GuiMotion.now() : 0L;
            if (this.networkMapView != null) {
                this.networkMapView.accept(networkMapPayload);
                if (this.pendingMapFocus != null) {
                    this.networkMapView.focusPosition(this.pendingMapFocus);
                    this.pendingMapFocus = null;
                }
            }
        }
    }

    private void followNetworkPath(ResourceLocation dimension, BlockPos pos) {
        this.switchMapDimension(dimension, pos == null ? null : pos.immutable(), true);
    }

    public void locateCraftingProvider(ResourceLocation dimension, BlockPos pos) {
        this.minecraft.setScreen(this);
        this.switchView(ViewMode.MAP);
        this.switchMapDimension(dimension, pos.immutable(), true);
    }

    private void prepareMapForCurrentDimension() {
        this.mapAutoInitialized = true;
        this.mapAutoDimensionPending = true;
        ResourceLocation currentDimension = this.currentPlayerDimension();
        this.mapViewDimension = currentDimension == null ? this.link.dimension() : currentDimension;
        this.pendingMapFocus = null;
        this.dimensionDropdownOpen = false;
        this.dimensionDropdownScroll = 0;
        this.networkMapSnapshot = null;
        if (this.networkMapView != null) {
            this.networkMapView.clearSnapshot();
        }
    }

    private ResourceLocation currentPlayerDimension() {
        return this.minecraft != null && this.minecraft.level != null
                ? this.minecraft.level.dimension().location()
                : null;
    }

    private void switchMapDimension(ResourceLocation dimension, BlockPos focus, boolean clearSearch) {
        if (dimension == null) {
            return;
        }
        if (dimension.equals(this.mapViewDimension) && focus == null && this.networkMapSnapshot != null) {
            this.dimensionDropdownOpen = false;
            return;
        }
        this.mapAutoDimensionPending = false;
        this.mapViewDimension = dimension;
        this.pendingMapFocus = focus;
        this.dimensionDropdownOpen = false;
        this.networkMapSnapshot = null;
        if (this.networkMapView != null) {
            this.networkMapView.clearSnapshot();
        }
        if (clearSearch && this.search != null && !this.search.getValue().isEmpty()) {
            this.search.setValue("");
        }
        this.refreshTicks = Math.max(100,
                ((ClientConfig.RefreshInterval)((Object)ClientConfig.VALUES.refreshInterval.get())).ticks());
        MonitorNetwork.requestNetworkMap(this.link, dimension);
    }

    private int dimensionDropdownVisibleRows() {
        int size = this.networkMapSnapshot == null ? 0 : this.networkMapSnapshot.dimensions().size();
        if (size <= 0) {
            return 0;
        }

        // Keep the popup completely inside the 3D scene. The NetworkMapView reserves
        // a toolbar at the top and the search/control bar at the bottom, so the dimension
        // list must never cross either of those GUI regions.
        int mapTop = this.top + 62;
        int sceneTop = mapTop + MAP_VIEW_TOOLBAR_HEIGHT;
        int controlBarTop = this.contentBottom - MAP_CONTROL_BAR_HEIGHT;
        int availableHeight = Math.max(DIMENSION_DROPDOWN_ROW_HEIGHT,
                controlBarTop - sceneTop - 4);
        int rowsByHeight = Math.max(1, availableHeight / DIMENSION_DROPDOWN_ROW_HEIGHT);
        return Math.min(size, Math.min(DIMENSION_DROPDOWN_MAX_ROWS, rowsByHeight));
    }

    private void clampDimensionDropdownScroll() {
        int size = this.networkMapSnapshot == null ? 0 : this.networkMapSnapshot.dimensions().size();
        int visible = this.dimensionDropdownVisibleRows();
        this.dimensionDropdownScroll = Math.max(0, Math.min(this.dimensionDropdownScroll, Math.max(0, size - visible)));
    }

    private boolean handleDimensionDropdownClick(double mouseX, double mouseY, int button) {
        if (button != 0 || this.viewMode != ViewMode.MAP || this.networkMapSnapshot == null) {
            return false;
        }
        List<ResourceLocation> dimensions = this.networkMapSnapshot.dimensions();
        if (this.dimensionDropdownOpen
                && mouseX >= this.dimensionMenuX && mouseX < this.dimensionMenuX + this.dimensionMenuWidth
                && mouseY >= this.dimensionMenuY
                && mouseY < this.dimensionMenuY + this.dimensionMenuRows * DIMENSION_DROPDOWN_ROW_HEIGHT) {
            int row = (int)((mouseY - this.dimensionMenuY) / DIMENSION_DROPDOWN_ROW_HEIGHT);
            int index = this.dimensionDropdownScroll + row;
            if (index >= 0 && index < dimensions.size()) {
                this.switchMapDimension(dimensions.get(index), null, false);
            }
            return true;
        }
        if (mouseX >= this.dimensionSelectorX && mouseX < this.dimensionSelectorX + this.dimensionSelectorWidth
                && mouseY >= this.dimensionSelectorY
                && mouseY < this.dimensionSelectorY + this.dimensionSelectorHeight) {
            if (dimensions.size() > 1) {
                this.dimensionDropdownOpen = !this.dimensionDropdownOpen;
                this.clampDimensionDropdownScroll();
            }
            return true;
        }
        if (this.dimensionDropdownOpen) {
            this.dimensionDropdownOpen = false;
        }
        return false;
    }

    private boolean handleDimensionDropdownScroll(double mouseX, double mouseY, double delta) {
        if (!this.dimensionDropdownOpen || this.networkMapSnapshot == null || delta == 0.0) {
            return false;
        }
        if (mouseX < this.dimensionMenuX || mouseX >= this.dimensionMenuX + this.dimensionMenuWidth
                || mouseY < this.dimensionMenuY
                || mouseY >= this.dimensionMenuY + this.dimensionMenuRows * DIMENSION_DROPDOWN_ROW_HEIGHT) {
            return false;
        }
        int size = this.networkMapSnapshot.dimensions().size();
        int visible = this.dimensionDropdownVisibleRows();
        int maxScroll = Math.max(0, size - visible);
        if (delta > 0.0) {
            this.dimensionDropdownScroll = Math.max(0, this.dimensionDropdownScroll - 1);
        } else {
            this.dimensionDropdownScroll = Math.min(maxScroll, this.dimensionDropdownScroll + 1);
        }
        return true;
    }

    private void drawDimensionSelector(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        ResourceLocation displayedDimension = this.networkMapSnapshot == null
                ? this.mapViewDimension : this.networkMapSnapshot.viewDimension();
        String dimensionName = NetworkMapView.dimensionName(displayedDimension);
        int dimensionCount = this.networkMapSnapshot == null ? 0 : this.networkMapSnapshot.dimensions().size();
        String buttonText = dimensionName + (dimensionCount > 1 ? "  ▾" : "");
        this.dimensionSelectorX = this.left + 18;
        this.dimensionSelectorY = this.footerY - 4;
        this.dimensionSelectorHeight = 17;
        this.dimensionSelectorWidth = Math.min(190, Math.max(72, this.font.width(buttonText) + 14));
        boolean hovered = mouseX >= this.dimensionSelectorX
                && mouseX < this.dimensionSelectorX + this.dimensionSelectorWidth
                && mouseY >= this.dimensionSelectorY
                && mouseY < this.dimensionSelectorY + this.dimensionSelectorHeight;
        int selectorFill = hovered && dimensionCount > 1 ? palette.hover() : palette.rowEven();
        guiGraphics.fill(this.dimensionSelectorX, this.dimensionSelectorY,
                this.dimensionSelectorX + this.dimensionSelectorWidth,
                this.dimensionSelectorY + this.dimensionSelectorHeight, selectorFill);
        guiGraphics.fill(this.dimensionSelectorX, this.dimensionSelectorY,
                this.dimensionSelectorX + 2,
                this.dimensionSelectorY + this.dimensionSelectorHeight, palette.accentA());
        guiGraphics.drawString(this.font,
                this.font.plainSubstrByWidth(buttonText, Math.max(20, this.dimensionSelectorWidth - 10)),
                this.dimensionSelectorX + 6, this.dimensionSelectorY + 4, palette.text(), false);

        if (this.networkMapSnapshot != null) {
            Component count = Component.translatable(
                    "screen.forever_production_monitor.map.dimension_count", dimensionCount);
            int countX = this.dimensionSelectorX + this.dimensionSelectorWidth + 7;
            int maxWidth = Math.max(10, this.left + this.panelWidth / 2 - countX);
            guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(count.getString(), maxWidth),
                    countX, this.footerY, palette.muted(), false);
        }
    }

    private void drawDimensionDropdownOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!this.dimensionDropdownOpen || this.networkMapSnapshot == null
                || this.networkMapSnapshot.dimensions().size() <= 1) {
            this.dimensionMenuRows = 0;
            return;
        }
        this.clampDimensionDropdownScroll();
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        List<ResourceLocation> dimensions = this.networkMapSnapshot.dimensions();
        this.dimensionMenuRows = this.dimensionDropdownVisibleRows();
        if (this.dimensionMenuRows <= 0) {
            return;
        }

        int widest = this.dimensionSelectorWidth;
        for (ResourceLocation dimension : dimensions) {
            widest = Math.max(widest, this.font.width(NetworkMapView.dimensionName(dimension)) + 34);
        }
        this.dimensionMenuWidth = Math.min(220, Math.max(this.dimensionSelectorWidth, widest));
        this.dimensionMenuX = this.dimensionSelectorX;
        int menuHeight = this.dimensionMenuRows * DIMENSION_DROPDOWN_ROW_HEIGHT;

        // The selector itself lives in the footer, but its popup is intentionally detached
        // by the height of the map's search/control bar. This prevents the menu from ever
        // covering "Search devices and blocks..." or the navigation help text.
        int controlBarTop = this.contentBottom - MAP_CONTROL_BAR_HEIGHT;
        this.dimensionMenuY = controlBarTop - 2 - menuHeight;

        guiGraphics.fill(this.dimensionMenuX - 1, this.dimensionMenuY - 1,
                this.dimensionMenuX + this.dimensionMenuWidth + 1,
                this.dimensionMenuY + menuHeight + 1, opaque(palette.border()));
        guiGraphics.fill(this.dimensionMenuX, this.dimensionMenuY,
                this.dimensionMenuX + this.dimensionMenuWidth,
                this.dimensionMenuY + menuHeight, opaque(palette.tableOuter()));

        ResourceLocation playerDimension = this.currentPlayerDimension();
        for (int row = 0; row < this.dimensionMenuRows; ++row) {
            int index = this.dimensionDropdownScroll + row;
            if (index >= dimensions.size()) {
                break;
            }
            ResourceLocation dimension = dimensions.get(index);
            int rowY = this.dimensionMenuY + row * DIMENSION_DROPDOWN_ROW_HEIGHT;
            boolean selected = dimension.equals(this.mapViewDimension);
            boolean hovered = mouseX >= this.dimensionMenuX
                    && mouseX < this.dimensionMenuX + this.dimensionMenuWidth
                    && mouseY >= rowY && mouseY < rowY + DIMENSION_DROPDOWN_ROW_HEIGHT;
            int rowFill = selected ? palette.summary()
                    : (hovered ? palette.hover() : (row % 2 == 0 ? palette.rowEven() : palette.rowOdd()));
            guiGraphics.fill(this.dimensionMenuX + 1, rowY,
                    this.dimensionMenuX + this.dimensionMenuWidth - 1,
                    rowY + DIMENSION_DROPDOWN_ROW_HEIGHT, opaque(rowFill));
            if (selected) {
                guiGraphics.fill(this.dimensionMenuX + 1, rowY,
                        this.dimensionMenuX + 3, rowY + DIMENSION_DROPDOWN_ROW_HEIGHT,
                        opaque(palette.accentA()));
            }
            String name = NetworkMapView.dimensionName(dimension);
            guiGraphics.drawString(this.font,
                    this.font.plainSubstrByWidth(name, Math.max(20, this.dimensionMenuWidth - 30)),
                    this.dimensionMenuX + 7, rowY + 4,
                    selected ? palette.accentA() : palette.text(), false);
            if (dimension.equals(playerDimension)) {
                guiGraphics.drawString(this.font, "◆",
                        this.dimensionMenuX + this.dimensionMenuWidth - 13, rowY + 4,
                        palette.accentB(), false);
            }
        }
    }

    private static int opaque(int color) {
        return 0xFF000000 | color & 0x00FFFFFF;
    }

    private void updateButtons() {
        if (this.previousButton != null) {
            int n = this.viewMode == ViewMode.DASHBOARD ? this.requestedPage : (this.viewMode == ViewMode.PRODUCTION && this.snapshot != null ? this.snapshot.page() : (this.statisticsSnapshot == null ? 0 : this.statisticsSnapshot.page()));
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
        return Math.max(1, (this.contentBottom - this.contentY - 21) / 28);
    }

    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        ThemeInteractionState.beginFrame(n, n2);
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        guiGraphics.fill(0, 0, this.width, this.height, palette.backdrop());
        this.drawPanel(guiGraphics);
        this.drawAmbientFrameAccent(guiGraphics, palette);
        float transition = GuiMotion.progress(this.contentTransitionStartedNanos, 160L);
        float easedTransition = GuiMotion.easeOut(transition);
        float transitionOffset = transition < 1.0f ? (1.0f - easedTransition) * 8.0f * (float)this.contentTransitionDirection : 0.0f;
        guiGraphics.pose().pushPose();
        if (transition < 1.0f) {
            guiGraphics.pose().translate(transitionOffset, 0.0f, 0.0f);
        }
        this.drawContent(guiGraphics, Math.round((float)n - transitionOffset), n2, f);
        guiGraphics.pose().popPose();
        if (transition < 1.0f) {
            int alpha = (int)((1.0f - easedTransition) * 72.0f);
            guiGraphics.fill(this.contentLeft, this.contentY, this.contentRight, this.contentBottom, GuiMotion.alpha(InterfaceTheme.current().panel(), alpha));
        }
        float dataPulse = GuiMotion.progress(this.dataPulseStartedNanos, GuiMotion.updatePulseDurationMillis());
        float pulseIntensity = GuiMotion.updatePulseIntensity();
        if (dataPulse < 1.0f && pulseIntensity > 0.0f) {
            int pulseAlpha = Math.round((1.0f - GuiMotion.easeOut(dataPulse)) * 72.0f * pulseIntensity);
            guiGraphics.renderOutline(this.contentLeft, this.contentY, Math.max(1, this.contentRight - this.contentLeft), Math.max(1, this.contentBottom - this.contentY), GuiMotion.alpha(palette.accentB(), pulseAlpha));
        }
        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, n, n2, f);
        }
        if (this.craftingTab != null && this.craftingTab.getWidth() >= 110) {
            guiGraphics.renderItem(AEItems.BLANK_PATTERN.stack(),
                    this.craftingTab.getX() + 5, this.craftingTab.getY() + 1);
        }
        if (this.viewMode == ViewMode.MAP) {
            this.drawDimensionDropdownOverlay(guiGraphics, n, n2);
        }
        this.drawTitle(guiGraphics);
        this.drawDeepCoreAccess(guiGraphics, n, n2);
        if (this.settingsButton != null && this.settingsButton.isHoveredOrFocused()) {
            guiGraphics.renderTooltip(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.theme.open"), n, n2);
        }
    }

    private void drawPanel(GuiGraphics guiGraphics) {
        InterfaceTheme.drawPanel(guiGraphics, this.left, this.top, this.panelWidth, this.panelHeight, (ClientConfig.InterfaceStyle)((Object)ClientConfig.VALUES.interfaceStyle.get()), InterfaceTheme.current());
    }

    private void drawAmbientFrameAccent(GuiGraphics guiGraphics, InterfaceTheme.Palette palette) {
        if (!GuiMotion.ambientMotionEnabled()) {
            return;
        }
        float intensity = GuiMotion.ambientMotionIntensity();
        int inset = 20;
        int available = this.panelWidth - inset * 2;
        int length = Math.min(36, Math.max(12, 16 + Math.round(intensity * 20.0f)));
        if (available <= length) {
            return;
        }
        int accentX = this.left + inset + Math.round(GuiMotion.cycle(7200L) * (float)(available - length));
        int alpha = Math.round(46.0f * intensity);
        guiGraphics.fill(accentX, this.top + 2, accentX + length, this.top + 3, GuiMotion.alpha(palette.accentB(), alpha));
    }

    private void drawTitle(GuiGraphics guiGraphics) {
        guiGraphics.drawString(this.font, this.title, this.left + (this.panelWidth - this.font.width((FormattedText)this.title)) / 2, this.top + 12, InterfaceTheme.current().text(), false);
    }

    private void drawDeepCoreAccess(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.deepCoreFacilities.isEmpty()) return;

        InterfaceTheme.Palette palette = InterfaceTheme.current();
        String label = this.deepCoreFacilities.size() == 1 ? "Deep Core" : "Deep Core ▾";
        this.deepCoreButtonWidth = Math.min(132, Math.max(84, this.font.width(label) + 20));
        this.deepCoreButtonX = this.left + this.panelWidth - 31 - this.deepCoreButtonWidth;
        this.deepCoreButtonY = this.top + 7;

        boolean hover = mouseX >= this.deepCoreButtonX
                && mouseX < this.deepCoreButtonX + this.deepCoreButtonWidth
                && mouseY >= this.deepCoreButtonY
                && mouseY < this.deepCoreButtonY + DEEP_CORE_HEADER_BUTTON_HEIGHT;
        MonitorNetwork.DeepCoreFacility only = this.deepCoreFacilities.size() == 1
                ? this.deepCoreFacilities.get(0) : null;
        boolean enabled = only == null || only.status() == MonitorNetwork.DeepCoreFacilityStatus.ONLINE;
        int border = hover && enabled ? palette.accentB() : palette.border();
        int inner = hover && enabled ? palette.hover() : palette.summary();
        guiGraphics.fill(this.deepCoreButtonX, this.deepCoreButtonY,
                this.deepCoreButtonX + this.deepCoreButtonWidth,
                this.deepCoreButtonY + DEEP_CORE_HEADER_BUTTON_HEIGHT, border);
        guiGraphics.fill(this.deepCoreButtonX + 1, this.deepCoreButtonY + 1,
                this.deepCoreButtonX + this.deepCoreButtonWidth - 1,
                this.deepCoreButtonY + DEEP_CORE_HEADER_BUTTON_HEIGHT - 1, inner);
        int textColor = enabled ? palette.text() : palette.muted();
        guiGraphics.drawCenteredString(this.font, Component.literal(label),
                this.deepCoreButtonX + this.deepCoreButtonWidth / 2,
                this.deepCoreButtonY + 5, textColor);

        int statusColor = only == null ? palette.accentA() : this.deepCoreStatusColor(only.status(), palette);
        guiGraphics.fill(this.deepCoreButtonX + 5, this.deepCoreButtonY + 7,
                this.deepCoreButtonX + 8, this.deepCoreButtonY + 10, statusColor);

        if (!this.deepCoreDropdownOpen || this.deepCoreFacilities.size() <= 1) return;

        this.deepCoreMenuWidth = Math.min(250, Math.max(190, this.deepCoreButtonWidth + 70));
        this.deepCoreMenuRows = Math.min(DEEP_CORE_MENU_MAX_ROWS, this.deepCoreFacilities.size());
        this.deepCoreMenuX = Math.max(this.left + 6,
                this.deepCoreButtonX + this.deepCoreButtonWidth - this.deepCoreMenuWidth);
        this.deepCoreMenuY = this.deepCoreButtonY + DEEP_CORE_HEADER_BUTTON_HEIGHT + 3;
        int menuHeight = this.deepCoreMenuRows * DEEP_CORE_MENU_ROW_HEIGHT + 2;

        guiGraphics.fill(this.deepCoreMenuX - 1, this.deepCoreMenuY - 1,
                this.deepCoreMenuX + this.deepCoreMenuWidth + 1,
                this.deepCoreMenuY + menuHeight + 1, palette.border());
        guiGraphics.fill(this.deepCoreMenuX, this.deepCoreMenuY,
                this.deepCoreMenuX + this.deepCoreMenuWidth,
                this.deepCoreMenuY + menuHeight, palette.tableOuter());

        this.clampDeepCoreDropdownScroll();
        for (int row = 0; row < this.deepCoreMenuRows; row++) {
            int index = this.deepCoreDropdownScroll + row;
            if (index >= this.deepCoreFacilities.size()) break;
            MonitorNetwork.DeepCoreFacility facility = this.deepCoreFacilities.get(index);
            int y = this.deepCoreMenuY + 1 + row * DEEP_CORE_MENU_ROW_HEIGHT;
            boolean rowHover = mouseX >= this.deepCoreMenuX
                    && mouseX < this.deepCoreMenuX + this.deepCoreMenuWidth
                    && mouseY >= y && mouseY < y + DEEP_CORE_MENU_ROW_HEIGHT;
            guiGraphics.fill(this.deepCoreMenuX + 1, y,
                    this.deepCoreMenuX + this.deepCoreMenuWidth - 1,
                    y + DEEP_CORE_MENU_ROW_HEIGHT - 1,
                    rowHover ? palette.hover() : (row % 2 == 0 ? palette.rowEven() : palette.rowOdd()));

            String facilityName = facility.name().isBlank()
                    ? Component.translatable("screen.forever_production_monitor.deep_core.unnamed").getString()
                    : facility.name();
            String status = Component.translatable(
                    "screen.forever_production_monitor.deep_core.status."
                            + facility.status().name().toLowerCase(Locale.ROOT)).getString();
            String detail = NetworkMapView.dimensionName(facility.dimension())
                    + " · " + facility.pos().toShortString() + " · " + status;
            guiGraphics.fill(this.deepCoreMenuX + 5, y + 6,
                    this.deepCoreMenuX + 8, y + 9,
                    this.deepCoreStatusColor(facility.status(), palette));
            guiGraphics.drawString(this.font,
                    this.font.plainSubstrByWidth(facilityName, this.deepCoreMenuWidth - 18),
                    this.deepCoreMenuX + 12, y + 3,
                    facility.status() == MonitorNetwork.DeepCoreFacilityStatus.ONLINE
                            ? palette.text() : palette.muted(), false);
            guiGraphics.drawString(this.font,
                    this.font.plainSubstrByWidth(detail, this.deepCoreMenuWidth - 14),
                    this.deepCoreMenuX + 6, y + 13, palette.muted(), false);
        }
    }

    private int deepCoreStatusColor(MonitorNetwork.DeepCoreFacilityStatus status,
                                    InterfaceTheme.Palette palette) {
        return switch (status) {
            case ONLINE -> 0xFF62CF99;
            case UNLOADED -> 0xFFD5A45B;
            case INVALID -> 0xFFE06B6B;
        };
    }

    private void clampDeepCoreDropdownScroll() {
        int visible = Math.min(DEEP_CORE_MENU_MAX_ROWS, this.deepCoreFacilities.size());
        this.deepCoreDropdownScroll = Math.max(0, Math.min(this.deepCoreDropdownScroll,
                Math.max(0, this.deepCoreFacilities.size() - visible)));
    }

    private void openDeepCore(MonitorNetwork.DeepCoreFacility facility) {
        if (facility.status() != MonitorNetwork.DeepCoreFacilityStatus.ONLINE) return;
        this.deepCoreDropdownOpen = false;
        MonitorNetwork.requestOpenDeepCore(this.link.dimension(), this.link.pos(),
                facility.dimension(), facility.pos());
    }

    private boolean handleDeepCoreAccessClick(double mouseX, double mouseY, int button) {
        if (button != 0 || this.deepCoreFacilities.isEmpty()) return false;

        if (this.deepCoreDropdownOpen && this.deepCoreFacilities.size() > 1
                && mouseX >= this.deepCoreMenuX
                && mouseX < this.deepCoreMenuX + this.deepCoreMenuWidth
                && mouseY >= this.deepCoreMenuY
                && mouseY < this.deepCoreMenuY + this.deepCoreMenuRows * DEEP_CORE_MENU_ROW_HEIGHT + 2) {
            int row = (int)((mouseY - this.deepCoreMenuY - 1) / DEEP_CORE_MENU_ROW_HEIGHT);
            int index = this.deepCoreDropdownScroll + Math.max(0, row);
            if (index >= 0 && index < this.deepCoreFacilities.size()) {
                this.openDeepCore(this.deepCoreFacilities.get(index));
            }
            return true;
        }

        if (mouseX >= this.deepCoreButtonX
                && mouseX < this.deepCoreButtonX + this.deepCoreButtonWidth
                && mouseY >= this.deepCoreButtonY
                && mouseY < this.deepCoreButtonY + DEEP_CORE_HEADER_BUTTON_HEIGHT) {
            if (this.deepCoreFacilities.size() == 1) {
                this.openDeepCore(this.deepCoreFacilities.get(0));
            } else {
                this.deepCoreDropdownOpen = !this.deepCoreDropdownOpen;
                this.clampDeepCoreDropdownScroll();
            }
            return true;
        }

        if (this.deepCoreDropdownOpen) this.deepCoreDropdownOpen = false;
        return false;
    }

    private boolean handleDeepCoreDropdownScroll(double mouseX, double mouseY, double scrollY) {
        if (!this.deepCoreDropdownOpen || this.deepCoreFacilities.size() <= DEEP_CORE_MENU_MAX_ROWS
                || scrollY == 0.0
                || mouseX < this.deepCoreMenuX
                || mouseX >= this.deepCoreMenuX + this.deepCoreMenuWidth
                || mouseY < this.deepCoreMenuY
                || mouseY >= this.deepCoreMenuY + this.deepCoreMenuRows * DEEP_CORE_MENU_ROW_HEIGHT + 2) {
            return false;
        }
        this.deepCoreDropdownScroll += scrollY < 0.0 ? 1 : -1;
        this.clampDeepCoreDropdownScroll();
        return true;
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
        this.drawDimensionSelector(guiGraphics, n, n2);
        if (this.networkMapSnapshot != null) {
            this.drawStatus(guiGraphics, this.networkMapSnapshot.status(),
                    this.left + this.panelWidth - 18, this.footerY);
        }
    }

    private void drawDashboardContent(GuiGraphics guiGraphics, int n, int n2) {
        int n3;
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        int n4 = this.contentY;
        int n5 = this.contentLeft;
        int n6 = this.contentRight;
        int n7 = this.contentBottom;
        boolean compact = this.panelWidth < 520;
        int n8 = n6 - (compact ? 92 : 176);
        int n9 = n6 - (compact ? 170 : 300);
        guiGraphics.fill(n5, n4, n6, n7, palette.tableOuter());
        guiGraphics.fill(n5 + 1, n4 + 1, n6 - 1, n4 + 20, palette.tableHeader());
        InterfaceTheme.drawTableDecoration(guiGraphics, n5, n4, n6 - n5, n7 - n4);
        guiGraphics.drawString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.dashboard.pinned"), n5 + 24, n4 + 6, palette.text(), false);
        this.drawRight(guiGraphics, (Component)Component.translatable((String)"screen.forever_production_monitor.stored"), n9, n4 + 6, palette.text());
        this.drawRight(guiGraphics, (Component)Component.translatable((String)"screen.forever_production_monitor.average"), n8, n4 + 6, palette.accentA());
        guiGraphics.drawString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.dashboard.alarm"), n8 + 18, n4 + 6, palette.accentB(), false);
        List<MonitorNetwork.DashboardEntry> list = this.dashboardSnapshot == null ? List.of() : this.dashboardSnapshot.entries();
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
            if (GuiMotion.enabled() && dashboardEntry2.alarmState() != ProductionMonitorBlockEntity.AlarmState.NORMAL) {
                int pulseAlpha = 18 + (int)((Math.sin((double)GuiMotion.now() / 2.4E8) + 1.0) * 13.0);
                guiGraphics.fill(n5 + 4, n14, n6 - 1, n14 + 27, GuiMotion.alpha(n16, pulseAlpha));
            }
            AEKeyRendering.drawInGui((Minecraft)this.minecraft, (GuiGraphics)guiGraphics, (int)(n5 + 7), (int)(n14 + 5), (AEKey)dashboardEntry2.key());
            Component mutableComponent = dashboardEntry2.kind() == MonitorNetwork.EntryKind.ENERGY ? Component.translatable((String)"screen.forever_production_monitor.energy") : AEKeyRendering.getDisplayName((AEKey)dashboardEntry2.key());
            guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(mutableComponent.getString(), Math.max(32, n9 - n5 - 80)), n5 + 29, n14 + 5, palette.text(), false);
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
        guiGraphics.drawString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.dashboard.summary", (Object[])new Object[]{list.size(), n3}), n5, this.footerY, n3 > 0 ? -39826 : palette.muted(), false);
        if (this.dashboardSnapshot != null) {
            guiGraphics.drawCenteredString(this.font, (Component)Component.literal((String)(this.requestedPage + 1 + " / " + this.dashboardPages())), this.contentRight - 57, this.controlsY + 6, palette.text());
            this.drawStatus(guiGraphics, this.dashboardSnapshot.status(), n6, this.footerY);
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
        int n6 = this.contentY;
        int n7 = this.contentLeft;
        int n8 = this.contentRight;
        int n9 = n8 - n7;
        int n10 = n7 + n9 * 66 / 100;
        int n11 = n7 + n9 * 82 / 100;
        int n12 = n6 + 18 + this.visibleRows * 17;
        guiGraphics.fill(n7, n6, n8, n12, palette.tableOuter());
        guiGraphics.fill(n7 + 1, n6 + 1, n8 - 1, n6 + 18, palette.tableHeader());
        InterfaceTheme.drawTableDecoration(guiGraphics, n7, n6, n8 - n7, n12 - n6);
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
        n4 = this.footerY - 7;
        String string = String.valueOf(this.link.dimension()) + "  " + this.link.pos().toShortString();
        guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(string, n9 / 2), n7, n4 + 7, palette.muted(), false);
        if (this.snapshot == null) {
            guiGraphics.drawCenteredString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.loading"), this.left + this.panelWidth / 2, n6 + (n12 - n6) / 2, -14740);
        } else {
            Component statusText = Component.translatable((String)("screen.forever_production_monitor.status." + this.snapshot.status().name().toLowerCase()));
            n3 = this.snapshot.status() == MonitorNetwork.Status.ONLINE ? -9972847 : (this.snapshot.status() == MonitorNetwork.Status.WARMING_UP ? -14740 : -35716);
            this.drawRight(guiGraphics, statusText, n8, n4 + 7, n3);
            guiGraphics.drawCenteredString(this.font, (Component)Component.literal((String)(this.snapshot.page() + 1 + " / " + this.snapshot.pages())), this.contentRight - 57, (this.compactControls ? this.auxiliaryControlsY : this.controlsY) + 6, palette.text());
            guiGraphics.drawString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.entries", (Object[])new Object[]{this.snapshot.totalEntries()}), this.contentLeft, this.contentY - 8, palette.muted(), false);
        }
    }

    private void drawStorageContent(GuiGraphics guiGraphics) {
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        int n = this.contentLeft;
        int n2 = this.contentRight;
        int n3 = n2 - n;
        int n4 = this.contentY;
        MonitorNetwork.StatisticsSnapshot statisticsSnapshot = this.statisticsSnapshot != null && this.statisticsSnapshot.statisticsPage() == MonitorNetwork.StatisticsPage.STORAGE ? this.statisticsSnapshot : null;
        guiGraphics.drawString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.storage.subtitle"), n, n4, palette.muted(), false);
        MonitorNetwork.StorageStats storageStats = statisticsSnapshot == null ? MonitorNetwork.StorageStats.EMPTY : statisticsSnapshot.storage();
        double d = storageStats.totalBytes() <= 0L ? 0.0 : Math.min(1.0, (double)storageStats.usedBytes() / (double)storageStats.totalBytes());
        this.drawCapacityBar(guiGraphics, n, n2, n4 += 16, (Component)Component.translatable((String)"screen.forever_production_monitor.storage.bytes"), d, ProductionMonitorScreen.formatBytes(storageStats.usedBytes()) + " / " + ProductionMonitorScreen.formatBytes(storageStats.totalBytes()));
        int n5 = 5;
        int n6 = (n3 - n5 * 3) / 4;
        if (n4 + 77 <= this.contentBottom) {
            this.drawStatCard(guiGraphics, n, n4 += 34, n6, (Component)Component.translatable((String)"screen.forever_production_monitor.storage.used"), ProductionMonitorScreen.formatBytes(storageStats.usedBytes()), palette.accentA());
            this.drawStatCard(guiGraphics, n + n6 + n5, n4, n6, (Component)Component.translatable((String)"screen.forever_production_monitor.storage.free"), ProductionMonitorScreen.formatBytes(storageStats.freeBytes()), -9972847);
            this.drawStatCard(guiGraphics, n + (n6 + n5) * 2, n4, n6, (Component)Component.translatable((String)"screen.forever_production_monitor.storage.capacity"), ProductionMonitorScreen.formatBytes(storageStats.totalBytes()), -1185038);
            this.drawStatCard(guiGraphics, n + (n6 + n5) * 3, n4, n3 - (n6 + n5) * 3, (Component)Component.translatable((String)"screen.forever_production_monitor.storage.utilization"), String.format(Locale.ROOT, "%.1f%%", d * 100.0), palette.accentB());
        }
        double d2 = storageStats.totalTypes() <= 0L ? 0.0 : Math.min(1.0, (double)storageStats.usedTypes() / (double)storageStats.totalTypes());
        if (n4 + 84 <= this.contentBottom) {
            this.drawCapacityBar(guiGraphics, n, n2, n4 += 50, (Component)Component.translatable((String)"screen.forever_production_monitor.storage.types"), d2, ProductionMonitorScreen.formatAmount(storageStats.usedTypes()) + " / " + ProductionMonitorScreen.formatAmount(storageStats.totalTypes()));
        }
        if (n4 + 119 <= this.contentBottom) {
            guiGraphics.fill(n, n4 += 40, n2, n4 + 79, palette.tableOuter());
        guiGraphics.fill(n + 1, n4 + 1, n2 - 1, n4 + 19, palette.tableHeader());
        guiGraphics.drawString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.storage.sources"), n + 8, n4 + 6, palette.text(), false);
        int n7 = n3 / 2;
        this.drawSourceLine(guiGraphics, n + 9, n4 + 28, (Component)Component.translatable((String)"screen.forever_production_monitor.storage.finite_cells"), storageStats.finiteCells(), -9972847);
        this.drawSourceLine(guiGraphics, n + n7 + 5, n4 + 28, (Component)Component.translatable((String)"screen.forever_production_monitor.storage.infinite_cells"), storageStats.infiniteCells(), palette.accentB());
        this.drawSourceLine(guiGraphics, n + 9, n4 + 48, (Component)Component.translatable((String)"screen.forever_production_monitor.storage.other_cells"), storageStats.otherCells(), palette.accentA());
        this.drawSourceLine(guiGraphics, n + n7 + 5, n4 + 48, (Component)Component.translatable((String)"screen.forever_production_monitor.storage.external"), storageStats.externalStorageBuses(), -8861464);
        guiGraphics.drawString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.storage.note"), n + 9, n4 + 66, palette.muted(), false);
        }
        this.drawStatisticsFooter(guiGraphics, statisticsSnapshot, n, n2);
        if (statisticsSnapshot == null) {
            guiGraphics.drawCenteredString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.loading"), this.left + this.panelWidth / 2, this.top + this.panelHeight / 2, -14740);
        }
    }

    private void drawComponentsContent(GuiGraphics guiGraphics, int n, int n2) {
        int n3;
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        int n4 = this.contentY;
        int n5 = this.contentLeft;
        int n6 = this.contentRight;
        int n7 = n6 - n5;
        int n8 = n5 + n7 * 78 / 100;
        int n9 = n4 + 18 + this.visibleRows * 17;
        MonitorNetwork.StatisticsSnapshot statisticsSnapshot = this.statisticsSnapshot != null && this.statisticsSnapshot.statisticsPage() == MonitorNetwork.StatisticsPage.COMPONENTS ? this.statisticsSnapshot : null;
        List<MonitorNetwork.ComponentGroup> list = statisticsSnapshot == null ? List.of() : statisticsSnapshot.componentGroups();
        guiGraphics.fill(n5, n4, n6, n9, palette.tableOuter());
        guiGraphics.fill(n5 + 1, n4 + 1, n6 - 1, n4 + 18, palette.tableHeader());
        InterfaceTheme.drawTableDecoration(guiGraphics, n5, n4, n6 - n5, n9 - n4);
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
            ArrayList<Component> arrayList = new ArrayList<Component>(AEKeyRendering.getTooltip((AEKey)componentGroup.representativeKey()));
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
            guiGraphics.drawCenteredString(this.font, (Component)Component.literal((String)(statisticsSnapshot.page() + 1 + " / " + statisticsSnapshot.pages())), this.contentRight - 57, this.auxiliaryControlsY + 6, palette.text());
            guiGraphics.drawString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.components.entries", (Object[])new Object[]{statisticsSnapshot.totalEntries()}), this.contentLeft, this.contentY - 8, palette.muted(), false);
        }
    }

    private void drawDevicesContent(GuiGraphics guiGraphics, int n, int n2) {
        int n3;
        InterfaceTheme.Palette palette = InterfaceTheme.current();
        int n4 = this.contentY;
        int n5 = this.contentLeft;
        int n6 = this.contentRight;
        int n7 = n6 - n5;
        int n8 = n5 + n7 * 60 / 100;
        int n9 = n5 + n7 * 72 / 100;
        int n10 = n5 + n7 * 84 / 100;
        int n11 = n10 - 8;
        int n12 = n6 - 18;
        int n13 = n12 - 6;
        int n14 = n4 + 18 + this.visibleRows * 17;
        MonitorNetwork.StatisticsSnapshot statisticsSnapshot = this.statisticsSnapshot != null && this.statisticsSnapshot.statisticsPage() == MonitorNetwork.StatisticsPage.DEVICES ? this.statisticsSnapshot : null;
        List<MonitorNetwork.DeviceGroup> list = statisticsSnapshot == null ? List.of() : statisticsSnapshot.deviceGroups();
        guiGraphics.fill(n5, n4, n6, n14, palette.tableOuter());
        guiGraphics.fill(n5 + 1, n4 + 1, n6 - 1, n4 + 18, palette.tableHeader());
        InterfaceTheme.drawTableDecoration(guiGraphics, n5, n4, n6 - n5, n14 - n4);
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
            ArrayList<Component> arrayList = new ArrayList<Component>(AEKeyRendering.getTooltip((AEKey)deviceGroup.visual()));
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
            guiGraphics.drawCenteredString(this.font, (Component)Component.literal((String)(statisticsSnapshot.page() + 1 + " / " + statisticsSnapshot.pages())), this.contentRight - 57, (this.compactControls ? this.auxiliaryControlsY : this.controlsY) + 6, palette.text());
            MutableComponent mutableComponent = Component.translatable((String)"screen.forever_production_monitor.devices.entries", (Object[])new Object[]{statisticsSnapshot.totalEntries()});
            guiGraphics.drawString(this.font, (Component)mutableComponent, this.contentLeft, this.contentY - 8, palette.muted(), false);
            if (statisticsSnapshot.deviceTotals().unresolvedChannels() > 0) {
                guiGraphics.drawString(this.font, (Component)Component.translatable((String)"screen.forever_production_monitor.devices.unresolved", (Object[])new Object[]{statisticsSnapshot.deviceTotals().unresolvedChannels()}), this.contentLeft + 5 + this.font.width((FormattedText)mutableComponent), this.contentY - 8, -14740, false);
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
        guiGraphics.drawCenteredString(this.font, this.font.plainSubstrByWidth(component.getString(), Math.max(12, n3 - 6)), n + n3 / 2, n2 + 8, palette.muted());
        guiGraphics.drawCenteredString(this.font, (Component)Component.literal((String)string), n + n3 / 2, n2 + 25, n4);
    }

    private void drawSourceLine(GuiGraphics guiGraphics, int n, int n2, Component component, int n3, int n4) {
        guiGraphics.fill(n, n2 + 2, n + 4, n2 + 10, n4);
        guiGraphics.drawString(this.font, component, n + 9, n2 + 2, InterfaceTheme.current().text(), false);
        guiGraphics.drawString(this.font, Integer.toString(n3), n + 9 + this.font.width((FormattedText)component) + 5, n2 + 2, n4, false);
    }

    private void drawStatisticsFooter(GuiGraphics guiGraphics, MonitorNetwork.StatisticsSnapshot statisticsSnapshot, int n, int n2) {
        int n3 = this.footerY;
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
        int available = Math.max(1, n2 - 6);
        int labelWidth = Math.min(this.font.width((FormattedText)component), available * 3 / 5);
        String label = this.font.plainSubstrByWidth(component.getString(), labelWidth);
        int valueWidth = Math.max(1, available - this.font.width(label) - 3);
        String value = this.font.plainSubstrByWidth(string, valueWidth);
        int combinedWidth = this.font.width(label) + 3 + this.font.width(value);
        int n6 = n + Math.max(3, (n2 - combinedWidth) / 2);
        guiGraphics.drawString(this.font, label, n6, n3 + 6, InterfaceTheme.current().muted(), false);
        guiGraphics.drawString(this.font, value, n6 + this.font.width(label) + 3, n3 + 6, n4, false);
    }

    private boolean contentTransitionRunning() {
        return GuiMotion.enabled() && GuiMotion.progress(this.contentTransitionStartedNanos, 160L) < 1.0f;
    }

    public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        if (this.contentTransitionRunning()) {
            return false;
        }
        if (this.handleDeepCoreDropdownScroll(d, d2, d4)) {
            return true;
        }
        if (this.viewMode == ViewMode.MAP && this.handleDimensionDropdownScroll(d, d2, d4)) {
            return true;
        }
        if (this.viewMode == ViewMode.MAP && this.search != null && this.search.isMouseOver(d, d2)) {
            return super.mouseScrolled(d, d2, d3, d4);
        }
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
        if (ThemeInteractionState.mouseClicked(d, d2, n)) {
            return true;
        }
        if (this.handleDeepCoreAccessClick(d, d2, n)) {
            return true;
        }
        Object object;
        int n2;
        int n3;
        int n4;
        int n5;
        int n6;
        if (this.contentTransitionRunning()) {
            return super.mouseClicked(d, d2, n);
        }
        if (this.search != null && this.search.visible && this.search.active) {
            boolean overSearch = this.search.isMouseOver(d, d2);
            if (overSearch && n == 1) {
                this.search.setValue("");
                this.searchDelay = 0;
                this.search.setFocused(true);
                this.requestedPage = 0;
                if (this.viewMode == ViewMode.MAP && this.networkMapView != null) {
                    this.networkMapView.setSearch("");
                } else {
                    this.requestNow();
                }
                return true;
            }
            if (overSearch && n == 0) {
                this.search.setFocused(true);
                return super.mouseClicked(d, d2, n);
            }
            if (!overSearch && (n == 0 || n == 1)) {
                this.search.setFocused(false);
            }
        }
        if (this.viewMode == ViewMode.MAP && this.handleDimensionDropdownClick(d, d2, n)) {
            return true;
        }
        if (this.viewMode == ViewMode.MAP && this.networkMapView != null && this.networkMapView.mouseClicked(d, d2, n)) {
            return true;
        }
        if (n == 0 && this.viewMode == ViewMode.DASHBOARD && this.dashboardSnapshot != null) {
            n6 = this.contentY;
            n5 = this.contentRight;
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
            n6 = this.contentY;
            n5 = this.contentRight;
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
            n6 = this.contentY;
            n5 = this.contentRight;
            n4 = n5 - 18;
            n3 = n6 + 18;
            n2 = (int)((d2 - (double)n3) / 17.0);
            List<MonitorNetwork.DeviceGroup> deviceGroups = this.statisticsSnapshot.deviceGroups();
            if (d >= (double)n4 && d < (double)(n4 + 13) && n2 >= 0 && n2 < deviceGroups.size()) {
                int n7 = n3 + n2 * 17;
                MonitorNetwork.DeviceGroup deviceGroup = deviceGroups.get(n2);
                if (d2 >= (double)(n7 + 2) && d2 < (double)(n7 + 15) && deviceGroup.locatableDevices() > 0) {
                    String string = String.valueOf(deviceGroup.visual().getId()) + "\u0000" + deviceGroup.name();
                    int n8 = this.locatorIndexes.getOrDefault(string, 0) % deviceGroup.locatableDevices();
                    this.locatorIndexes.put(string, (n8 + 1) % deviceGroup.locatableDevices());
                    MonitorNetwork.requestLocate(this.link, deviceGroup, n8);
                    return true;
                }
            }
        }
        return super.mouseClicked(d, d2, n);
    }

    public boolean mouseDragged(double d, double d2, int n, double d3, double d4) {
        if (this.contentTransitionRunning()) {
            return false;
        }
        if (this.viewMode == ViewMode.MAP && this.networkMapView != null && this.networkMapView.mouseDragged(d, d2, n, d3, d4)) {
            return true;
        }
        return super.mouseDragged(d, d2, n, d3, d4);
    }

    public boolean mouseReleased(double d, double d2, int n) {
        if (this.contentTransitionRunning()) {
            return false;
        }
        if (this.viewMode == ViewMode.MAP && this.networkMapView != null && this.networkMapView.mouseReleased(d, d2, n)) {
            return true;
        }
        return super.mouseReleased(d, d2, n);
    }

    private boolean isPinned(MonitorNetwork.EntryKind entryKind, AEKey aEKey) {
        return this.dashboardSnapshot != null && this.dashboardSnapshot.entries().stream().anyMatch(dashboardEntry -> dashboardEntry.kind() == entryKind && dashboardEntry.key().equals(aEKey));
    }

    public boolean keyPressed(int n, int n2, int n3) {
        if (this.contentTransitionRunning()) {
            return super.keyPressed(n, n2, n3);
        }
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
