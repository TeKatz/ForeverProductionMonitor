package de.timo.foreverproductionmonitor.client.screen;

import appeng.api.client.AEKeyRendering;
import de.timo.foreverproductionmonitor.client.ClientConfig;
import de.timo.foreverproductionmonitor.item.ProductionTabletItem;
import de.timo.foreverproductionmonitor.network.DeepCoreConstructionNetwork;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Compact construction planner for ForeverDeepCore Tier I-III.
 */
public final class DeepCoreConstructionScreen extends Screen {
    private final CraftingDiagnosticsScreen parent;
    private final ProductionTabletItem.MonitorLink link;
    private final List<ForeverButton> tierButtons = new ArrayList<>();

    private DeepCoreConstructionNetwork.PlanSnapshot snapshot;
    private ForeverButton craftButton;
    private Component status;
    private int selectedTier = 1;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;
    private int contentTop;
    private int contentBottom;
    private int rowScroll;
    private boolean batchPending;
    private boolean batchLocked;

    public DeepCoreConstructionScreen(CraftingDiagnosticsScreen parent,
                                      ProductionTabletItem.MonitorLink link) {
        super(Component.translatable("screen.forever_production_monitor.deepcore_build.title"));
        this.parent = parent;
        this.link = link;
    }

    @Override
    protected void init() {
        panelWidth = Math.max(360, Math.min(650, width - 20));
        panelHeight = Math.max(300, Math.min(460, height - 20));
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        contentTop = top + 112;
        contentBottom = top + panelHeight - 62;

        tierButtons.clear();
        int gap = 6;
        int tierWidth = Math.max(90, Math.min(150, (panelWidth - 48 - gap * 2) / 3));
        int totalTierWidth = tierWidth * 3 + gap * 2;
        int tierStart = left + (panelWidth - totalTierWidth) / 2;
        for (int tier = 1; tier <= 3; tier++) {
            final int target = tier;
            ForeverButton button = ForeverButton.create(
                    Component.translatable("screen.forever_production_monitor.deepcore_build.tier", roman(tier)),
                    ignored -> selectTier(target),
                    ForeverButton.Style.THEMED,
                    tierStart + (tier - 1) * (tierWidth + gap), top + 40, tierWidth, 22)
                    .setRole(ForeverButton.Role.TAB);
            tierButtons.add(button);
            addRenderableWidget(button);
        }

        addRenderableWidget(ForeverButton.create(
                Component.translatable("screen.forever_production_monitor.deepcore_build.refresh"),
                ignored -> refresh(),
                ForeverButton.Style.THEMED,
                left + 18, top + panelHeight - 42, 82, 22));

        addRenderableWidget(ForeverButton.create(
                Component.translatable("screen.forever_production_monitor.deepcore_build.back"),
                ignored -> minecraft.setScreen(parent),
                ForeverButton.Style.THEMED,
                left + panelWidth - 100, top + panelHeight - 42, 82, 22));

        craftButton = ForeverButton.create(
                Component.translatable("screen.forever_production_monitor.deepcore_build.craft_missing"),
                ignored -> startBatch(),
                ForeverButton.Style.THEMED_ACTIVE,
                left + panelWidth / 2 - 92, top + panelHeight - 42, 184, 22);
        addRenderableWidget(craftButton);

        updateButtons();
        if (snapshot == null) requestPlan();
    }

    private void selectTier(int tier) {
        if (selectedTier == tier && snapshot != null) return;
        selectedTier = tier;
        snapshot = null;
        rowScroll = 0;
        batchLocked = false;
        batchPending = false;
        status = Component.translatable("screen.forever_production_monitor.deepcore_build.loading");
        updateButtons();
        requestPlan();
    }

    private void refresh() {
        snapshot = null;
        rowScroll = 0;
        batchLocked = false;
        batchPending = false;
        status = Component.translatable("screen.forever_production_monitor.deepcore_build.loading");
        updateButtons();
        requestPlan();
    }

    private void requestPlan() {
        DeepCoreConstructionNetwork.requestPlan(link, selectedTier);
    }

    private void startBatch() {
        if (snapshot == null || !snapshot.canStart() || batchPending || batchLocked) return;
        batchPending = true;
        status = Component.translatable("screen.forever_production_monitor.deepcore_build.preflight");
        updateButtons();
        DeepCoreConstructionNetwork.startBatch(link, selectedTier);
    }

    private void updateButtons() {
        for (int i = 0; i < tierButtons.size(); i++) {
            tierButtons.get(i).setStyle(i + 1 == selectedTier
                    ? ForeverButton.Style.THEMED_ACTIVE
                    : ForeverButton.Style.THEMED);
        }
        if (craftButton != null) {
            craftButton.active = snapshot != null && snapshot.canStart() && !batchPending && !batchLocked;
        }
    }

    public void acceptPlan(DeepCoreConstructionNetwork.PlanResponse response) {
        if (!response.dimension().equals(link.dimension()) || !response.pos().equals(link.pos())
                || response.tier() != selectedTier) return;

        snapshot = response.snapshot();
        status = switch (snapshot.status()) {
            case READY -> snapshot.canStart()
                    ? Component.translatable("screen.forever_production_monitor.deepcore_build.ready")
                    : allStored(snapshot)
                        ? Component.translatable("screen.forever_production_monitor.deepcore_build.complete")
                        : Component.translatable("screen.forever_production_monitor.deepcore_build.pattern_missing");
            case MOD_MISSING -> Component.translatable(
                    "screen.forever_production_monitor.deepcore_build.mod_missing");
            case INVALID_TIER -> Component.translatable(
                    "screen.forever_production_monitor.deepcore_build.invalid_tier");
            case INVALID_LINK -> Component.translatable(
                    "screen.forever_production_monitor.deepcore_build.invalid_link");
            case NETWORK_OFFLINE -> Component.translatable(
                    "screen.forever_production_monitor.deepcore_build.offline");
            case PLAN_ERROR -> Component.translatable(
                    "screen.forever_production_monitor.deepcore_build.plan_error");
        };
        updateButtons();
    }

    public void acceptBatch(DeepCoreConstructionNetwork.BatchResult result) {
        if (!result.dimension().equals(link.dimension()) || !result.pos().equals(link.pos())
                || result.tier() != selectedTier) return;

        batchPending = false;
        String detail = result.detail();
        status = switch (result.status()) {
            case STARTED -> {
                batchLocked = true;
                yield Component.translatable(
                        "screen.forever_production_monitor.deepcore_build.batch_started",
                        result.startedJobs());
            }
            case NOTHING_TO_CRAFT -> {
                batchLocked = true;
                yield Component.translatable(
                        "screen.forever_production_monitor.deepcore_build.batch_nothing");
            }
            case MOD_MISSING -> Component.translatable(
                    "screen.forever_production_monitor.deepcore_build.mod_missing");
            case INVALID_TIER -> Component.translatable(
                    "screen.forever_production_monitor.deepcore_build.invalid_tier");
            case INVALID_LINK -> Component.translatable(
                    "screen.forever_production_monitor.deepcore_build.invalid_link");
            case NETWORK_OFFLINE -> Component.translatable(
                    "screen.forever_production_monitor.deepcore_build.offline");
            case NOT_CRAFTABLE -> Component.translatable(
                    "screen.forever_production_monitor.deepcore_build.batch_not_craftable", detail);
            case MISSING_INGREDIENTS -> Component.translatable(
                    "screen.forever_production_monitor.deepcore_build.batch_missing", detail);
            case SUBMIT_FAILED -> Component.translatable(
                    "screen.forever_production_monitor.deepcore_build.batch_submit_failed", detail);
            case PARTIAL -> {
                batchLocked = true;
                yield Component.translatable(
                        "screen.forever_production_monitor.deepcore_build.batch_partial",
                        result.startedJobs(), result.totalJobs(), detail);
            }
            case ERROR -> Component.translatable(
                    "screen.forever_production_monitor.deepcore_build.batch_error", detail);
        };
        updateButtons();
    }

    private static boolean allStored(DeepCoreConstructionNetwork.PlanSnapshot snapshot) {
        if (snapshot.status() != DeepCoreConstructionNetwork.PlanStatus.READY) return false;
        for (var entry : snapshot.entries()) {
            if (entry.missing() > 0L) return false;
        }
        return true;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        ThemeInteractionState.beginFrame(mouseX, mouseY);
        var palette = InterfaceTheme.current();

        g.fill(0, 0, width, height, palette.backdrop());
        InterfaceTheme.drawPanel(g, left, top, panelWidth, panelHeight,
                (ClientConfig.InterfaceStyle) ClientConfig.VALUES.interfaceStyle.get(), palette);

        g.drawCenteredString(font, title, left + panelWidth / 2, top + 12, palette.text());
        for (Renderable widget : renderables) widget.render(g, mouseX, mouseY, partialTick);

        g.drawString(font,
                Component.translatable("screen.forever_production_monitor.deepcore_build.subtitle",
                        roman(selectedTier)),
                left + 18, top + 78, palette.accentA(), false);

        if (status != null) {
            String text = font.plainSubstrByWidth(status.getString(), Math.max(1, panelWidth - 36));
            g.drawString(font, text, left + 18, top + 94, palette.muted(), false);
        }

        if (snapshot == null) {
            g.drawString(font,
                    Component.translatable("screen.forever_production_monitor.deepcore_build.loading"),
                    left + 18, contentTop + 8, palette.text(), false);
            return;
        }
        if (snapshot.status() != DeepCoreConstructionNetwork.PlanStatus.READY) return;

        int tableLeft = left + 18;
        int tableRight = left + panelWidth - 18;
        int nameWidth = Math.max(120, panelWidth - 300);
        int requiredX = tableLeft + nameWidth;
        int storedX = requiredX + 64;
        int missingX = storedX + 64;
        int stateX = missingX + 64;

        g.fill(tableLeft, contentTop, tableRight, contentTop + 18, palette.tableHeader());
        g.drawString(font,
                Component.translatable("screen.forever_production_monitor.deepcore_build.component"),
                tableLeft + 4, contentTop + 5, palette.text(), false);
        g.drawString(font,
                Component.translatable("screen.forever_production_monitor.deepcore_build.required"),
                requiredX, contentTop + 5, palette.text(), false);
        g.drawString(font,
                Component.translatable("screen.forever_production_monitor.deepcore_build.stored"),
                storedX, contentTop + 5, palette.text(), false);
        g.drawString(font,
                Component.translatable("screen.forever_production_monitor.deepcore_build.missing"),
                missingX, contentTop + 5, palette.text(), false);
        g.drawString(font,
                Component.translatable("screen.forever_production_monitor.deepcore_build.state"),
                stateX, contentTop + 5, palette.text(), false);

        int rowHeight = 20;
        int visibleRows = Math.max(1, (contentBottom - (contentTop + 20)) / rowHeight);
        int maxScroll = Math.max(0, snapshot.entries().size() - visibleRows);
        rowScroll = Math.max(0, Math.min(rowScroll, maxScroll));

        g.enableScissor(tableLeft, contentTop + 20, tableRight, contentBottom);
        for (int row = 0; row < visibleRows && row + rowScroll < snapshot.entries().size(); row++) {
            var entry = snapshot.entries().get(row + rowScroll);
            int y = contentTop + 20 + row * rowHeight;
            g.fill(tableLeft, y, tableRight, y + rowHeight - 1,
                    ((row + rowScroll) & 1) == 0 ? palette.rowEven() : palette.rowOdd());

            AEKeyRendering.drawInGui(minecraft, g, tableLeft + 3, y + 2, entry.key());
            String name = font.plainSubstrByWidth(entry.key().getDisplayName().getString(),
                    Math.max(1, nameWidth - 26));
            g.drawString(font, name, tableLeft + 23, y + 6, palette.text(), false);
            g.drawString(font, Long.toString(entry.required()), requiredX, y + 6, palette.text(), false);
            g.drawString(font, Long.toString(entry.stored()), storedX, y + 6, palette.text(), false);
            g.drawString(font, Long.toString(entry.missing()), missingX, y + 6,
                    entry.missing() == 0L ? palette.muted() : palette.accentA(), false);

            Component state = entry.missing() == 0L
                    ? Component.translatable("screen.forever_production_monitor.deepcore_build.state_stored")
                    : entry.craftable()
                        ? Component.translatable("screen.forever_production_monitor.deepcore_build.state_craftable")
                        : Component.translatable("screen.forever_production_monitor.deepcore_build.state_no_pattern");
            g.drawString(font, font.plainSubstrByWidth(state.getString(),
                    Math.max(1, tableRight - stateX - 4)), stateX, y + 6,
                    entry.missing() == 0L ? palette.muted()
                            : entry.craftable() ? palette.accentB() : 0xFFFF6B6B,
                    false);

            if (mouseX >= tableLeft + 3 && mouseX < tableLeft + 19
                    && mouseY >= y + 2 && mouseY < y + 18) {
                g.renderComponentTooltip(font, AEKeyRendering.getTooltip(entry.key()), mouseX, mouseY);
            }
        }
        g.disableScissor();

        if (snapshot.entries().size() > visibleRows) {
            int trackX = tableRight - 5;
            int trackY = contentTop + 20;
            int trackHeight = contentBottom - trackY;
            int thumbHeight = Math.max(18, trackHeight * visibleRows / snapshot.entries().size());
            int thumbY = trackY + (trackHeight - thumbHeight) * rowScroll / Math.max(1, maxScroll);
            g.fill(trackX, trackY, trackX + 3, trackY + trackHeight, palette.summary());
            g.fill(trackX, thumbY, trackX + 3, thumbY + thumbHeight, palette.accentA());
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (snapshot != null && snapshot.status() == DeepCoreConstructionNetwork.PlanStatus.READY
                && mouseY >= contentTop && mouseY < contentBottom) {
            rowScroll = Math.max(0, rowScroll - (int) Math.signum(scrollY));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_E) {
            minecraft.setScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static String roman(int tier) {
        return switch (tier) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            default -> Integer.toString(tier);
        };
    }
}
