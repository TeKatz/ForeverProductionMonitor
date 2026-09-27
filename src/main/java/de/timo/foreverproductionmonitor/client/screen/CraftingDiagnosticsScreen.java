package de.timo.foreverproductionmonitor.client.screen;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEKey;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import de.timo.foreverproductionmonitor.blockentity.CraftingDiagnostics;
import de.timo.foreverproductionmonitor.client.ClientConfig;
import de.timo.foreverproductionmonitor.item.ProductionTabletItem;
import de.timo.foreverproductionmonitor.network.CraftingDiagnosticsNetwork;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Read-only craft diagnostics. AE2's own crafting screen remains responsible for
 * progress, job planning and cancellation.
 */
public final class CraftingDiagnosticsScreen extends Screen {
    private final ProductionMonitorScreen parent;
    private final ProductionTabletItem.MonitorLink link;
    private CraftingDiagnostics.Snapshot snapshot;
    private EditBox search;
    private Section section = Section.JOBS;
    private int selectedPattern = -1;
    private int selectedProvider = -1;
    private int selectedJob = -1;
    private int listScroll;
    private int detailScroll;
    private int left, top, widthPanel, heightPanel, listLeft, listRight, detailsLeft, contentTop, contentBottom;
    private List<Integer> visibleIndexes = List.of();
    private final List<ProviderHit> providerHits = new ArrayList<>();
    private CraftingDiagnostics.Snapshot graphSnapshot;
    private int graphSource = -1;
    private List<Integer> cachedDependents = List.of();
    private boolean cachedCycle;
    private AEKey hoveredIcon;
    private Component hoveredTab;

    private enum Section { JOBS, PATTERNS, PROVIDERS }
    private record ProviderHit(int y, int providerIndex) {}

    public CraftingDiagnosticsScreen(ProductionMonitorScreen parent, ProductionTabletItem.MonitorLink link) {
        super(Component.translatable("screen.forever_production_monitor.crafting.title"));
        this.parent = parent;
        this.link = link;
    }

    @Override
    protected void init() {
        String previousSearch = search == null ? "" : search.getValue();
        widthPanel = Math.max(1, Math.min(960, width - 12));
        heightPanel = Math.max(1, Math.min(560, height - 12));
        left = (width - widthPanel) / 2;
        top = (height - heightPanel) / 2;
        listLeft = left + 18;
        listRight = left + Math.max(120, widthPanel * 43 / 100);
        detailsLeft = listRight + 12;
        contentTop = top + 110;
        contentBottom = top + heightPanel - 38;
        int inner = Math.max(1, widthPanel - 36);
        int gap = 4;
        Section[] sections = { Section.JOBS, Section.PATTERNS, Section.PROVIDERS };
        int tabWidth = Math.max(1, (inner - (sections.length - 1) * gap) / sections.length);
        for (int i = 0; i < sections.length; i++) {
            Section target = sections[i];
            int x = listLeft + i * (tabWidth + gap);
            addRenderableWidget(new IconTabButton(Component.translatable(
                    "screen.forever_production_monitor.crafting." + target.name().toLowerCase(Locale.ROOT)),
                    target, x, top + 38, tabWidth));
        }
        int searchWidth = Math.max(1, inner - 160);
        search = new EditBox(font, listLeft, top + 66, searchWidth, 20,
                Component.translatable("screen.forever_production_monitor.search"));
        search.setHint(Component.translatable("screen.forever_production_monitor.crafting.search"));
        search.setMaxLength(64);
        search.setResponder(value -> listScroll = 0);
        search.setValue(previousSearch);
        addRenderableWidget(search);
        addRenderableWidget(ForeverButton.create(Component.translatable(
                "screen.forever_production_monitor.crafting.refresh"),
                button -> { snapshot = null; CraftingDiagnosticsNetwork.request(link); },
                ForeverButton.Style.THEMED, listLeft + searchWidth + 4, top + 66, 76, 20));
        addRenderableWidget(ForeverButton.create(Component.translatable(
                "screen.forever_production_monitor.crafting.back"),
                button -> minecraft.setScreen(parent), ForeverButton.Style.THEMED,
                listLeft + searchWidth + 84, top + 66, 72, 20));
        if (snapshot == null) CraftingDiagnosticsNetwork.request(link);
    }

    public void accept(CraftingDiagnosticsNetwork.Response response) {
        if (!response.dimension().equals(link.dimension()) || !response.pos().equals(link.pos())) return;
        snapshot = response.snapshot();
        graphSnapshot = null;
        if (selectedPattern >= snapshot.patterns().size()) selectedPattern = -1;
        if (selectedProvider >= snapshot.providers().size()) selectedProvider = -1;
        if (selectedJob >= snapshot.jobs().size()) selectedJob = -1;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        hoveredIcon = null;
        hoveredTab = null;
        var palette = InterfaceTheme.current();
        g.fill(0, 0, width, height, palette.backdrop());
        InterfaceTheme.drawPanel(g, left, top, widthPanel, heightPanel,
                (ClientConfig.InterfaceStyle) ClientConfig.VALUES.interfaceStyle.get(), palette);
        centered(g, title.getString(), left + widthPanel / 2, top + 13, palette.text());
        for (Renderable widget : renderables) widget.render(g, mouseX, mouseY, partialTick);
        providerHits.clear();
        if (snapshot == null) {
            line(g, tr("loading"), listLeft, contentTop, palette.text(), listRight - listLeft);
            renderTabTooltip(g, mouseX, mouseY);
            return;
        }
        if (!snapshot.online()) {
            line(g, tr("offline"), listLeft, contentTop, palette.text(), widthPanel - 36);
            renderTabTooltip(g, mouseX, mouseY);
            return;
        }
        String summary = snapshot.jobs().size() + " jobs  ·  " + snapshot.patterns().size()
                + " patterns  ·  " + snapshot.providers().size() + " providers";
        if (snapshot.limited()) summary += "  ·  " + tr("limited");
        line(g, summary, listLeft, top + 94, snapshot.limited() ? palette.accentB() : palette.text(), widthPanel - 36);
        g.fill(listRight + 3, contentTop - 2, listRight + 4, contentBottom, palette.border());
        drawList(g, mouseX, mouseY);
        drawDetails(g, mouseX, mouseY);
        line(g, tr("hint_" + section.name().toLowerCase(Locale.ROOT)),
                listLeft, contentBottom + 10, palette.text(), widthPanel - 36);
        if (hoveredIcon != null) {
            g.renderComponentTooltip(font, AEKeyRendering.getTooltip(hoveredIcon), mouseX, mouseY);
        }
        renderTabTooltip(g, mouseX, mouseY);
    }

    private void drawList(GuiGraphics g, int mouseX, int mouseY) {
        var palette = InterfaceTheme.current();
        visibleIndexes = matchingIndexes();
        int slots = rows();
        listScroll = Math.max(0, Math.min(listScroll, Math.max(0, visibleIndexes.size() - slots)));
        g.enableScissor(listLeft, contentTop, listRight, contentBottom);
        for (int row = 0; row < slots && row + listScroll < visibleIndexes.size(); row++) {
            int index = visibleIndexes.get(row + listScroll);
            int y = contentTop + row * 18;
            boolean active = section == Section.PROVIDERS ? index == selectedProvider
                    : section == Section.JOBS ? index == selectedJob : index == selectedPattern;
            if (active) g.fill(listLeft, y, listRight - 3, y + 17, palette.border());
            AEKey icon = switch (section) {
                case JOBS -> snapshot.jobs().get(index).targetIcon();
                case PROVIDERS -> snapshot.providers().get(index).visualIcon();
                case PATTERNS -> snapshot.patterns().get(index).outputIcon();
            };
            drawIcon(g, icon, listLeft + 3, y, mouseX, mouseY);
            String label = switch (section) {
                case JOBS -> snapshot.jobs().get(index).cpuName() + " · " + snapshot.jobs().get(index).target();
                case PROVIDERS -> snapshot.providers().get(index).name()
                        + (snapshot.providers().get(index).active() ? "" : " · " + tr("inactive"));
                case PATTERNS -> {
                    var pattern = snapshot.patterns().get(index);
                    yield "[" + pattern.type() + "] " + pattern.output()
                            + (pattern.multipleProviders() ? " · ×" + pattern.providers().size() : "");
                }
            };
            line(g, label, listLeft + 23, y + 4, active ? palette.accentA() : palette.text(),
                    listRight - listLeft - 31);
        }
        g.disableScissor();
        if (visibleIndexes.isEmpty()) {
            line(g, tr("empty"), listLeft + 4, contentTop + 6, palette.text(), listRight - listLeft - 12);
        }
    }

    private void drawDetails(GuiGraphics g, int mouseX, int mouseY) {
        int x = detailsLeft;
        int available = left + widthPanel - 18 - x;
        if (available < 50) return;
        var palette = InterfaceTheme.current();
        List<String> lines = new ArrayList<>();
        Map<Integer, AEKey> icons = new HashMap<>();
        if (section == Section.JOBS) {
            if (selectedJob >= 0 && selectedJob < snapshot.jobs().size()) {
                var job = snapshot.jobs().get(selectedJob);
                lines.add(job.cpuName());
                lines.add(tr("target") + ": " + job.target());
                if (job.targetIcon() != null) icons.put(lines.size() - 1, job.targetIcon());
                lines.add(tr("storage") + ": " + job.storage() + " bytes");
                lines.add(tr("coprocessors") + ": " + job.coProcessors());
                lines.add("");
                lines.addAll(wrap(tr("job_limit"), available));
            } else lines.add(tr("select_job"));
        } else if (section == Section.PROVIDERS) {
            if (selectedProvider >= 0 && selectedProvider < snapshot.providers().size()) {
                var provider = snapshot.providers().get(selectedProvider);
                lines.add(provider.name());
                if (provider.visualIcon() != null) icons.put(0, provider.visualIcon());
                lines.add(tr("power") + ": " + yes(provider.powered()));
                lines.add(tr("channel") + ": " + yes(provider.channel()));
                lines.add(tr("booted") + ": " + yes(provider.booted()));
                lines.add(tr("active") + ": " + yes(provider.active()));
                lines.add(tr("priority") + ": " + provider.priority());
                lines.add(provider.dimension() == null ? tr("no_location")
                        : provider.dimension() + " · " + provider.pos().toShortString());
                if (provider.dimension() != null) {
                    int count = 0;
                    for (var pattern : snapshot.patterns()) {
                        if (pattern.providers().contains(selectedProvider)) count++;
                    }
                    lines.add(tr("patterns") + ": " + count);
                    lines.add(tr("locate"));
                    providerHits.add(new ProviderHit(8, selectedProvider));
                }
            } else lines.add(tr("select_provider"));
        } else if (selectedPattern >= 0 && selectedPattern < snapshot.patterns().size()) {
            var pattern = snapshot.patterns().get(selectedPattern);
            lines.add("[" + pattern.type() + "] " + pattern.output() + " × " + pattern.amount());
            icons.put(0, pattern.outputIcon());
            lines.add(tr("inputs") + ":");
            for (int i = 0; i < pattern.inputs().size(); i++) {
                if (i < pattern.inputIcons().size()) icons.put(lines.size(), pattern.inputIcons().get(i));
                lines.add(pattern.inputs().get(i));
            }
            lines.add(tr("outputs") + ":");
            for (int i = 0; i < pattern.outputs().size(); i++) {
                if (i < pattern.outputIcons().size()) icons.put(lines.size(), pattern.outputIcons().get(i));
                lines.add(pattern.outputs().get(i));
            }
            lines.add(tr("provider_trace") + " (" + pattern.providers().size() + "):");
            for (int index : pattern.providers()) {
                if (index < 0 || index >= snapshot.providers().size()) continue;
                var provider = snapshot.providers().get(index);
                providerHits.add(new ProviderHit(lines.size(), index));
                if (provider.visualIcon() != null) icons.put(lines.size(), provider.visualIcon());
                lines.add("↗ " + provider.name() + " · " + (provider.active() ? tr("active") : tr("inactive")));
            }
            if (pattern.multipleProviders()) lines.add(tr("multiple_providers"));
            if (pattern.copies() > 1) lines.add(tr("identical_copies") + ": " + pattern.copies());
            if (pattern.outputVariants()) lines.add(tr("output_variants"));
            updateGraph(selectedPattern);
            lines.add("");
            lines.add(tr("dependencies") + ":");
            lines.add(tr("requires") + ":");
            for (int dep : pattern.dependencies()) {
                if (dep >= 0 && dep < snapshot.patterns().size()) {
                    icons.put(lines.size(), snapshot.patterns().get(dep).outputIcon());
                    lines.add("→ " + snapshot.patterns().get(dep).output());
                }
            }
            lines.add(tr("reverse_dependencies") + ":");
            for (int dependent : cachedDependents) {
                icons.put(lines.size(), snapshot.patterns().get(dependent).outputIcon());
                lines.add("← " + snapshot.patterns().get(dependent).output());
            }
            if (cachedCycle) lines.add(tr("cycle"));
            if (snapshot.limited()) lines.add(tr("partial"));
        } else lines.add(tr("select_pattern"));

        int maxLines = Math.max(1, (contentBottom - contentTop) / 18);
        detailScroll = Math.max(0, Math.min(detailScroll, Math.max(0, lines.size() - maxLines)));
        g.enableScissor(x, contentTop, left + widthPanel - 16, contentBottom);
        for (int i = detailScroll; i < lines.size() && i < detailScroll + maxLines; i++) {
            boolean actionable = false;
            for (ProviderHit hit : providerHits) if (hit.y() == i) actionable = true;
            int rowY = contentTop + (i - detailScroll) * 18;
            if (actionable) {
                g.fill(x, rowY, x + available, rowY + 17,
                        mouseX >= x && mouseX < x + available
                                && mouseY >= rowY && mouseY < rowY + 17
                                ? palette.tableHeader() : palette.summary());
                g.renderOutline(x, rowY, available, 17, palette.border());
            }
            AEKey icon = icons.get(i);
            if (icon != null) drawIcon(g, icon, x, rowY, mouseX, mouseY);
            int inset = icon == null ? 0 : 20;
            line(g, lines.get(i), x + inset, rowY + 4,
                    actionable ? palette.accentA() : palette.text(), available - inset);
        }
        g.disableScissor();
    }

    private List<Integer> matchingIndexes() {
        if (snapshot == null) return List.of();
        String query = search.getValue().toLowerCase(Locale.ROOT).trim();
        List<Integer> indexes = new ArrayList<>();
        int size = section == Section.JOBS ? snapshot.jobs().size()
                : section == Section.PROVIDERS ? snapshot.providers().size() : snapshot.patterns().size();
        for (int i = 0; i < size; i++) {
            String text = section == Section.JOBS ? snapshot.jobs().get(i).target() + snapshot.jobs().get(i).cpuName()
                    : section == Section.PROVIDERS ? snapshot.providers().get(i).name()
                    : snapshot.patterns().get(i).output() + snapshot.patterns().get(i).type();
            if (text.toLowerCase(Locale.ROOT).contains(query)) indexes.add(i);
        }
        return indexes;
    }

    private void updateGraph(int source) {
        if (graphSnapshot == snapshot && graphSource == source) return;
        graphSnapshot = snapshot;
        graphSource = source;
        cachedDependents = dependents(source);
        cachedCycle = reachesItself(source);
    }

    private List<Integer> dependents(int source) {
        List<Integer> found = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        visited.add(source);
        queue.add(source);
        while (!queue.isEmpty()) {
            int current = queue.removeFirst();
            for (int i = 0; i < snapshot.patterns().size(); i++) {
                if (snapshot.patterns().get(i).dependencies().contains(current) && visited.add(i)) {
                    found.add(i);
                    queue.add(i);
                }
            }
        }
        return found;
    }

    private boolean reachesItself(int source) {
        Set<Integer> visited = new HashSet<>();
        ArrayDeque<Integer> queue = new ArrayDeque<>(snapshot.patterns().get(source).dependencies());
        while (!queue.isEmpty()) {
            int current = queue.removeFirst();
            if (current == source) return true;
            if (current < 0 || current >= snapshot.patterns().size() || !visited.add(current)) continue;
            queue.addAll(snapshot.patterns().get(current).dependencies());
        }
        return false;
    }

    private int rows() { return Math.max(1, (contentBottom - contentTop) / 18); }
    private String yes(boolean value) { return tr(value ? "yes" : "no"); }
    private String tr(String name) {
        return Component.translatable("screen.forever_production_monitor.crafting." + name).getString();
    }
    private void line(GuiGraphics g, String value, int x, int y, int color, int maxWidth) {
        g.drawString(font, font.plainSubstrByWidth(value, Math.max(1, maxWidth)), x, y, color, false);
    }
    private void drawIcon(GuiGraphics g, AEKey icon, int x, int y, int mouseX, int mouseY) {
        if (icon == null) return;
        AEKeyRendering.drawInGui(minecraft, g, x, y, icon);
        if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) hoveredIcon = icon;
    }
    private List<String> wrap(String value, int maxWidth) {
        List<String> result = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : value.split(" ")) {
            String next = line.isEmpty() ? word : line + " " + word;
            if (!line.isEmpty() && font.width(next) > maxWidth) {
                result.add(line.toString());
                line.setLength(0);
            }
            if (!line.isEmpty()) line.append(' ');
            line.append(word);
        }
        if (!line.isEmpty()) result.add(line.toString());
        return result;
    }
    private void centered(GuiGraphics g, String value, int x, int y, int color) {
        g.drawCenteredString(font, value, x, y, color);
    }
    private void renderTabTooltip(GuiGraphics g, int mouseX, int mouseY) {
        if (hoveredTab != null) g.renderTooltip(font, hoveredTab, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (super.mouseClicked(x, y, button)) return true;
        if (button != 0 || snapshot == null || y < contentTop || y >= contentBottom) return false;
        if (x >= listLeft && x < listRight) {
            int row = (int) (y - contentTop) / 18 + listScroll;
            if (row >= 0 && row < visibleIndexes.size()) {
                int index = visibleIndexes.get(row);
                if (section == Section.JOBS) selectedJob = index;
                else if (section == Section.PROVIDERS) selectedProvider = index;
                else selectedPattern = index;
                detailScroll = 0;
                return true;
            }
        } else if (x >= detailsLeft) {
            int line = (int) (y - contentTop) / 18 + detailScroll;
            for (ProviderHit hit : providerHits) {
                if (hit.y() != line || hit.providerIndex() < 0
                        || hit.providerIndex() >= snapshot.providers().size()) continue;
                var provider = snapshot.providers().get(hit.providerIndex());
                if (provider.dimension() != null && provider.pos() != null) {
                    parent.locateCraftingProvider(provider.dimension(), provider.pos());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (snapshot != null && y >= contentTop && y < contentBottom) {
            if (x < listRight) listScroll = Math.max(0, listScroll - (int) Math.signum(scrollY) * 3);
            else detailScroll = Math.max(0, detailScroll - (int) Math.signum(scrollY) * 3);
            return true;
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    @Override
    public void onClose() { minecraft.setScreen(parent); }
    @Override
    public boolean isPauseScreen() { return false; }

    private final class IconTabButton extends Button {
        private final Section target;
        private final ItemStack icon;

        private IconTabButton(Component label, Section target, int x, int y, int width) {
            super(x, y, width, 20, label, button -> {
                section = target;
                listScroll = 0;
                detailScroll = 0;
                if (search != null) search.setValue("");
            }, DEFAULT_NARRATION);
            this.target = target;
            this.icon = switch (target) {
                case JOBS -> AEBlocks.CRAFTING_UNIT.stack();
                case PATTERNS -> AEItems.BLANK_PATTERN.stack();
                case PROVIDERS -> AEBlocks.PATTERN_PROVIDER.stack();
            };
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            var palette = InterfaceTheme.current();
            boolean selected = section == target;
            int x = getX();
            int y = getY();
            g.fill(x, y, x + getWidth(), y + getHeight(), selected ? palette.accentA() : palette.border());
            g.fill(x + 1, y + 1, x + getWidth() - 1, y + getHeight() - 1,
                    selected ? palette.tableHeader() : palette.summary());
            if (selected) g.fill(x + 2, y + getHeight() - 3, x + getWidth() - 2,
                    y + getHeight() - 1, palette.accentB());
            g.renderItem(icon, x + 4, y + 2);
            String label = getMessage().getString();
            int textWidth = Math.max(1, getWidth() - 28);
            g.drawString(font, font.plainSubstrByWidth(label, textWidth), x + 24, y + 6,
                    palette.text(), false);
            if (isHoveredOrFocused() && font.width(label) > textWidth) hoveredTab = getMessage();
        }
    }
}
