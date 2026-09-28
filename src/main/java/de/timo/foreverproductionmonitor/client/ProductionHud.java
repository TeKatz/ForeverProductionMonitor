/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  appeng.api.client.AEKeyRendering
 *  appeng.api.stacks.AEItemKey
 *  appeng.api.stacks.AEKey
 *  appeng.core.definitions.AEBlocks
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package de.timo.foreverproductionmonitor.client;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.core.definitions.AEBlocks;
import de.timo.foreverproductionmonitor.client.ClientConfig;
import de.timo.foreverproductionmonitor.client.ClientMonitorState;
import de.timo.foreverproductionmonitor.network.MonitorNetwork;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public final class ProductionHud {
    private static final int WIDTH = 188;
    private static final int HEADER_HEIGHT = 18;
    private static final int ROW_HEIGHT = 17;

    private ProductionHud() {
    }

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!((Boolean)ClientConfig.VALUES.hudEnabled.get()).booleanValue() || minecraft.options.hideGui || minecraft.screen != null || minecraft.player == null || !ClientMonitorState.hudFresh()) {
            return;
        }
        MonitorNetwork.HudSnapshot hudSnapshot = ClientMonitorState.hudSnapshot();
        if (hudSnapshot == null) {
            return;
        }
        ProductionHud.renderConfigured(guiGraphics, hudSnapshot.mode(), hudSnapshot.status(),
                ProductionHud.prepareEntries(hudSnapshot.mode(), hudSnapshot.entries()));
    }

    public static void renderPreview(GuiGraphics guiGraphics) {
        if (!((Boolean)ClientConfig.VALUES.hudEnabled.get()).booleanValue()) {
            return;
        }
        MonitorNetwork.HudMode hudMode = (MonitorNetwork.HudMode)((Object)ClientConfig.VALUES.hudMode.get());
        MonitorNetwork.HudSnapshot hudSnapshot = ClientMonitorState.hudSnapshot();
        List<MonitorNetwork.Entry> list = hudSnapshot != null && !hudSnapshot.entries().isEmpty()
                ? hudSnapshot.entries() : ProductionHud.previewEntries(hudMode);
        ProductionHud.renderConfigured(guiGraphics, hudMode, MonitorNetwork.Status.ONLINE,
                ProductionHud.prepareEntries(hudMode, list));
    }

    public static void renderPreview(GuiGraphics guiGraphics, int left, int top, int right, int bottom) {
        if (!((Boolean)ClientConfig.VALUES.hudEnabled.get()).booleanValue() || right <= left || bottom <= top) {
            return;
        }
        MonitorNetwork.HudMode hudMode = (MonitorNetwork.HudMode)((Object)ClientConfig.VALUES.hudMode.get());
        MonitorNetwork.HudSnapshot hudSnapshot = ClientMonitorState.hudSnapshot();
        List<MonitorNetwork.Entry> list = hudSnapshot != null && !hudSnapshot.entries().isEmpty()
                ? hudSnapshot.entries() : ProductionHud.previewEntries(hudMode);
        ProductionHud.renderConfigured(guiGraphics, hudMode, MonitorNetwork.Status.ONLINE,
                ProductionHud.prepareEntries(hudMode, list), left, top, right, bottom, true);
    }

    public static void renderPreviewCentered(GuiGraphics guiGraphics, int left, int top, int right, int bottom,
                                             double previewScaleMultiplier) {
        if (!((Boolean)ClientConfig.VALUES.hudEnabled.get()).booleanValue() || right <= left || bottom <= top) {
            return;
        }
        MonitorNetwork.HudMode hudMode = (MonitorNetwork.HudMode)((Object)ClientConfig.VALUES.hudMode.get());
        MonitorNetwork.HudSnapshot hudSnapshot = ClientMonitorState.hudSnapshot();
        List<MonitorNetwork.Entry> list = hudSnapshot != null && !hudSnapshot.entries().isEmpty()
                ? hudSnapshot.entries() : ProductionHud.previewEntries(hudMode);
        ProductionHud.renderConfigured(guiGraphics, hudMode, MonitorNetwork.Status.ONLINE,
                ProductionHud.prepareEntries(hudMode, list), left, top, right, bottom,
                true, true, Math.max(0.25, Math.min(4.0, previewScaleMultiplier)));
    }

    private static void renderConfigured(GuiGraphics guiGraphics, MonitorNetwork.HudMode hudMode, MonitorNetwork.Status status, List<MonitorNetwork.Entry> list) {
        ProductionHud.renderConfigured(guiGraphics, hudMode, status, list, 0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), false);
    }

    private static void renderConfigured(GuiGraphics guiGraphics, MonitorNetwork.HudMode hudMode, MonitorNetwork.Status status,
                                         List<MonitorNetwork.Entry> list, int left, int top, int right, int bottom,
                                         boolean clampToViewport) {
        ProductionHud.renderConfigured(guiGraphics, hudMode, status, list, left, top, right, bottom,
                clampToViewport, false, 1.0);
    }

    private static void renderConfigured(GuiGraphics guiGraphics, MonitorNetwork.HudMode hudMode, MonitorNetwork.Status status,
                                         List<MonitorNetwork.Entry> list, int left, int top, int right, int bottom,
                                         boolean clampToViewport, boolean centerInViewport, double previewScaleMultiplier) {
        Minecraft minecraft = Minecraft.getInstance();
        int n = Math.min(list.size(), (Integer)ClientConfig.VALUES.hudEntryCount.get());
        if (n < list.size()) {
            list = list.subList(0, n);
        }
        int n2 = Math.max(1, list.size());
        int n3 = 18 + n2 * 17 + 2;
        double d = (Double)ClientConfig.VALUES.hudScale.get() * previewScaleMultiplier;
        int viewportLeft = clampToViewport ? (int)Math.ceil((double)left / d) : 0;
        int viewportTop = clampToViewport ? (int)Math.ceil((double)top / d) : 0;
        int viewportRight = clampToViewport ? (int)Math.floor((double)right / d) : (int)Math.round((double)guiGraphics.guiWidth() / d);
        int viewportBottom = clampToViewport ? (int)Math.floor((double)bottom / d) : (int)Math.round((double)guiGraphics.guiHeight() / d);
        int n6 = (Integer)ClientConfig.VALUES.hudXOffset.get();
        int n7 = (Integer)ClientConfig.VALUES.hudYOffset.get();
        ClientConfig.HudAnchor hudAnchor = (ClientConfig.HudAnchor)((Object)ClientConfig.VALUES.hudAnchor.get());
        int n8;
        int n9;
        if (centerInViewport) {
            n8 = viewportLeft + Math.max(0, (viewportRight - viewportLeft - 188) / 2);
            n9 = viewportTop + Math.max(0, (viewportBottom - viewportTop - n3) / 2);
        } else {
            n8 = switch (hudAnchor) {
                default -> throw new IncompatibleClassChangeError();
                case ClientConfig.HudAnchor.TOP_LEFT, ClientConfig.HudAnchor.BOTTOM_LEFT -> viewportLeft + n6;
                case ClientConfig.HudAnchor.TOP_RIGHT, ClientConfig.HudAnchor.BOTTOM_RIGHT -> viewportRight - 188 - n6;
            };
            n9 = switch (hudAnchor) {
                default -> throw new IncompatibleClassChangeError();
                case ClientConfig.HudAnchor.TOP_LEFT, ClientConfig.HudAnchor.TOP_RIGHT -> viewportTop + n7;
                case ClientConfig.HudAnchor.BOTTOM_LEFT, ClientConfig.HudAnchor.BOTTOM_RIGHT -> viewportBottom - n3 - n7;
            };
        }
        if (clampToViewport) {
            n8 = Math.max(viewportLeft, Math.min(Math.max(viewportLeft, viewportRight - 188), n8));
            n9 = Math.max(viewportTop, Math.min(Math.max(viewportTop, viewportBottom - n3), n9));
        }
        int n10 = (int)Math.round((Double)ClientConfig.VALUES.hudOpacity.get() * 255.0);
        ClientConfig.InterfaceStyle hudTheme = ClientConfig.effectiveHudTheme();
        ClientConfig.HudLayoutStyle hudLayout = (ClientConfig.HudLayoutStyle)((Object)ClientConfig.VALUES.hudLayoutStyle.get());
        HudThemeRenderer.Colors frameColors = HudThemeRenderer.colors(hudTheme);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale((float)d, (float)d, 1.0f);
        HudThemeRenderer.renderFrame(guiGraphics, n8, n9, n3, n10, hudTheme, hudLayout);
        HudThemeRenderer.renderMark(guiGraphics, n8, n9, n10, hudTheme, hudLayout);
        MutableComponent mutableComponent = Component.translatable((String)("hud.forever_production_monitor.mode." + hudMode.name().toLowerCase(Locale.ROOT)));
        int n11 = n8 + ((hudLayout == ClientConfig.HudLayoutStyle.NONE || hudLayout == ClientConfig.HudLayoutStyle.MINIMAL) ? 6 : 17);
        guiGraphics.drawString(minecraft.font, (Component)mutableComponent, n11, n9 + 6, frameColors.titleText() | 0xFF000000, false);
        if (list.isEmpty()) {
            MutableComponent mutableComponent2 = Component.translatable((String)("screen.forever_production_monitor.status." + status.name().toLowerCase(Locale.ROOT)));
            guiGraphics.drawString(minecraft.font, minecraft.font.plainSubstrByWidth(mutableComponent2.getString(), 176), n8 + 6, n9 + 18 + 5, -14740, false);
        } else {
            for (int i = 0; i < list.size(); ++i) {
                int n12;
                MonitorNetwork.Entry entry = list.get(i);
                int n13 = n9 + 18 + i * 17;
                HudThemeRenderer.renderRow(guiGraphics, n8, n13, i, n10, hudTheme, hudLayout);
                if (entry.alarmActive()) {
                    guiGraphics.fill(n8 + 1, n13, n8 + 188 - 1, n13 + 17, 1430196248);
                    guiGraphics.fill(n8 + 1, n13 + 2, n8 + 4, n13 + 17 - 2, -41624);
                }
                boolean bl = (Boolean)ClientConfig.VALUES.hudShowIcons.get();
                boolean bl2 = (Boolean)ClientConfig.VALUES.hudShowNames.get();
                boolean bl3 = (Boolean)ClientConfig.VALUES.hudShowValues.get();
                if (bl) {
                    AEKeyRendering.drawInGui((Minecraft)minecraft, (GuiGraphics)guiGraphics, (int)(n8 + 3), (int)n13, (AEKey)entry.key());
                }
                String string = ProductionHud.displayValue(hudMode, entry);
                n12 = entry.alarmActive() ? -38028 : (entry.infinite() ? -4286734 : ProductionHud.rateColor(entry.averagePerMinute()));
                if (hudMode == MonitorNetwork.HudMode.STORED || hudMode == MonitorNetwork.HudMode.ENERGY) {
                    n12 = entry.kind() == MonitorNetwork.EntryKind.FLUID ? -8861464 : (entry.kind() == MonitorNetwork.EntryKind.ENERGY ? -14740 : -3485478);
                }
                int n15 = bl3 ? minecraft.font.width(string) : 0;
                int n16 = n8 + (bl ? 23 : 6);
                if (bl2) {
                    int n17 = bl3 ? Math.max(20, n8 + 188 - 12 - n15 - n16) : Math.max(20, n8 + 188 - 7 - n16);
                    String string2 = minecraft.font.plainSubstrByWidth(ProductionHud.displayName(entry).getString(), n17);
                    guiGraphics.drawString(minecraft.font, string2, n16, n13 + 4, -856074, false);
                }
                if (!bl3) continue;
                guiGraphics.drawString(minecraft.font, string, n8 + 188 - 6 - n15, n13 + 4, n12, false);
            }
        }
        guiGraphics.pose().popPose();
    }

    private static void renderFrame(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, ClientConfig.HudFrameStyle hudFrameStyle, FrameColors frameColors) {
        int n5 = Math.min(255, n4 + 45);
        int n6 = Math.min(255, n4 + 20);
        if (hudFrameStyle == ClientConfig.HudFrameStyle.NONE) {
            guiGraphics.fill(n, n2, n + 188, n2 + n3, ProductionHud.withAlpha(frameColors.panel(), n4));
            guiGraphics.fill(n, n2, n + 188, n2 + 18, ProductionHud.withAlpha(frameColors.header(), n6));
            guiGraphics.fill(n + 5, n2 + 18 - 1, n + 188 - 5, n2 + 18, ProductionHud.withAlpha(6383733, Math.min(150, n5)));
            return;
        }
        guiGraphics.fill(n - 3, n2 - 3, n + 188 + 3, n2 + n3 + 3, ProductionHud.withAlpha(frameColors.outer(), n5));
        guiGraphics.fill(n - 2, n2 - 2, n + 188 + 2, n2 + n3 + 2, ProductionHud.withAlpha(frameColors.border(), n5));
        guiGraphics.fill(n - 1, n2 - 1, n + 188 + 1, n2 + n3 + 1, ProductionHud.withAlpha(frameColors.outer(), n5));
        guiGraphics.fill(n, n2, n + 188, n2 + n3, ProductionHud.withAlpha(frameColors.panel(), n4));
        guiGraphics.fill(n, n2, n + 188, n2 + 18, ProductionHud.withAlpha(frameColors.header(), n6));
        switch (hudFrameStyle) {
            case NONE: {
                break;
            }
            case STANDARD: {
                guiGraphics.fill(n - 2, n2 - 2, n + 188 + 1, n2 - 1, ProductionHud.withAlpha(frameColors.accentA(), n5));
                guiGraphics.fill(n - 2, n2 - 2, n - 1, n2 + n3 + 1, ProductionHud.withAlpha(frameColors.accentA(), n5));
                guiGraphics.fill(n - 1, n2 + n3 + 1, n + 188 + 2, n2 + n3 + 2, ProductionHud.withAlpha(frameColors.accentB(), n5));
                guiGraphics.fill(n + 188 + 1, n2 - 1, n + 188 + 2, n2 + n3 + 2, ProductionHud.withAlpha(frameColors.accentB(), n5));
                guiGraphics.fill(n, n2 + 18 - 1, n + 188, n2 + 18, ProductionHud.withAlpha(8226194, Math.min(190, n5)));
                break;
            }
            case ORITECH: {
                guiGraphics.fill(n, n2, n + 188, n2 + 3, ProductionHud.withAlpha(2168077, 255));
                for (int i = 0; i < 188; i += 14) {
                    int n7 = Math.min(188, i + 9);
                    int n8 = i / 14 % 2 == 0 ? frameColors.accentA() : frameColors.accentB();
                    guiGraphics.fill(n + i, n2, n + n7, n2 + 2, ProductionHud.withAlpha(n8, 255));
                }
                guiGraphics.fill(n, n2 + 3, n + 2, n2 + n3 - 3, ProductionHud.withAlpha(frameColors.accentA(), n5));
                guiGraphics.fill(n + 188 - 2, n2 + 3, n + 188, n2 + n3 - 3, ProductionHud.withAlpha(frameColors.accentB(), n5));
                ProductionHud.cornerBrackets(guiGraphics, n, n2, n3, frameColors.accentA(), frameColors.accentB(), n5);
                ProductionHud.rivet(guiGraphics, n + 4, n2 + 4, frameColors.accentB());
                ProductionHud.rivet(guiGraphics, n + 188 - 6, n2 + 4, frameColors.accentB());
                ProductionHud.rivet(guiGraphics, n + 4, n2 + n3 - 6, frameColors.accentA());
                ProductionHud.rivet(guiGraphics, n + 188 - 6, n2 + n3 - 6, frameColors.accentA());
                guiGraphics.fill(n + 2, n2 + 18 - 1, n + 188 - 2, n2 + 18, ProductionHud.withAlpha(8013092, n5));
                break;
            }
            case AE2: {
                guiGraphics.fill(n, n2, n + 94, n2 + 2, ProductionHud.withAlpha(frameColors.accentA(), 255));
                guiGraphics.fill(n + 94, n2, n + 188, n2 + 2, ProductionHud.withAlpha(frameColors.accentB(), 255));
                guiGraphics.fill(n, n2 + 2, n + 1, n2 + n3, ProductionHud.withAlpha(frameColors.accentA(), n5));
                guiGraphics.fill(n + 188 - 1, n2 + 2, n + 188, n2 + n3, ProductionHud.withAlpha(frameColors.accentB(), n5));
                guiGraphics.fill(n + 1, n2 + 18 - 1, n + 188 - 1, n2 + 18, ProductionHud.withAlpha(4896720, Math.min(210, n5)));
                ProductionHud.circuitTrace(guiGraphics, n + 5, n2 + n3 - 3, frameColors.accentA(), true);
                ProductionHud.circuitTrace(guiGraphics, n + 188 - 5, n2 + n3 - 3, frameColors.accentB(), false);
                guiGraphics.fill(n + 94 - 2, n2 - 2, n + 94 + 2, n2 - 1, ProductionHud.withAlpha(14216435, 255));
                break;
            }
            case FOREVER: {
                int n9 = 112;
                guiGraphics.fill(n, n2, n + n9, n2 + 2, ProductionHud.withAlpha(frameColors.accentA(), 255));
                guiGraphics.fill(n + n9, n2, n + 188, n2 + 2, ProductionHud.withAlpha(frameColors.accentB(), 255));
                guiGraphics.fill(n - 1, n2 + 5, n, n2 + n3 - 5, ProductionHud.withAlpha(frameColors.accentA(), n5));
                guiGraphics.fill(n + 188, n2 + 5, n + 188 + 1, n2 + n3 - 5, ProductionHud.withAlpha(frameColors.accentB(), n5));
                guiGraphics.fill(n + 2, n2 + 18 - 1, n + n9, n2 + 18, ProductionHud.withAlpha(frameColors.accentA(), Math.min(190, n5)));
                guiGraphics.fill(n + n9, n2 + 18 - 1, n + 188 - 2, n2 + 18, ProductionHud.withAlpha(frameColors.accentB(), Math.min(190, n5)));
                ProductionHud.cornerBrackets(guiGraphics, n, n2, n3, frameColors.accentA(), frameColors.accentB(), n5);
                guiGraphics.fill(n + 94 - 5, n2 + n3 + 1, n + 94, n2 + n3 + 2, ProductionHud.withAlpha(frameColors.accentA(), 255));
                guiGraphics.fill(n + 94, n2 + n3 + 1, n + 94 + 5, n2 + n3 + 2, ProductionHud.withAlpha(frameColors.accentB(), 255));
                break;
            }
            case CUSTOM: {
                guiGraphics.fill(n, n2, n + 125, n2 + 2, ProductionHud.withAlpha(frameColors.accentA(), 255));
                guiGraphics.fill(n + 125, n2, n + 188, n2 + 2, ProductionHud.withAlpha(frameColors.accentB(), 255));
                guiGraphics.fill(n, n2 + 2, n + 1, n2 + n3, ProductionHud.withAlpha(frameColors.accentA(), n5));
                guiGraphics.fill(n + 188 - 1, n2 + 2, n + 188, n2 + n3, ProductionHud.withAlpha(frameColors.accentB(), n5));
                ProductionHud.cornerBrackets(guiGraphics, n, n2, n3, frameColors.accentA(), frameColors.accentB(), n5);
                guiGraphics.fill(n + 2, n2 + 18 - 1, n + 188 - 2, n2 + 18, ProductionHud.withAlpha(frameColors.accentA(), Math.min(170, n5)));
            }
        }
    }

    private static void renderRowBackground(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, ClientConfig.HudFrameStyle hudFrameStyle, FrameColors frameColors) {
        int n5 = Math.min(255, n4 + 5);
        int n6 = n3 % 2 == 0 ? frameColors.rowEven() : frameColors.rowOdd();
        int n7 = hudFrameStyle == ClientConfig.HudFrameStyle.NONE ? n : n + 1;
        int n8 = hudFrameStyle == ClientConfig.HudFrameStyle.NONE ? n + 188 : n + 188 - 1;
        guiGraphics.fill(n7, n2, n8, n2 + 17, ProductionHud.withAlpha(n6, n5));
        switch (hudFrameStyle) {
            case NONE: {
                break;
            }
            case STANDARD: {
                guiGraphics.fill(n + 2, n2 + 17 - 1, n + 188 - 2, n2 + 17, ProductionHud.withAlpha(0x333943, Math.min(150, n5)));
                break;
            }
            case ORITECH: {
                int n9 = n3 % 2 == 0 ? frameColors.accentA() : frameColors.accentB();
                guiGraphics.fill(n + 1, n2 + 2, n + 3, n2 + 17 - 2, ProductionHud.withAlpha(n9, Math.min(220, n5 + 30)));
                guiGraphics.fill(n + 188 - 4, n2 + 7, n + 188 - 2, n2 + 9, ProductionHud.withAlpha(frameColors.accentB(), Math.min(210, n5 + 20)));
                break;
            }
            case AE2: {
                int n10 = n3 % 2 == 0 ? frameColors.accentA() : frameColors.accentB();
                guiGraphics.fill(n + 1, n2 + 2, n + 2, n2 + 17 - 2, ProductionHud.withAlpha(n10, Math.min(220, n5 + 30)));
                guiGraphics.fill(n + 188 - 5, n2 + 17 - 2, n + 188 - 2, n2 + 17 - 1, ProductionHud.withAlpha(n10, Math.min(190, n5 + 10)));
                break;
            }
            case FOREVER: {
                int n11 = n3 % 2 == 0 ? frameColors.accentA() : frameColors.accentB();
                guiGraphics.fill(n + 1, n2 + 2, n + 3, n2 + 17 - 2, ProductionHud.withAlpha(n11, Math.min(220, n5 + 30)));
                guiGraphics.fill(n + 188 - 3, n2 + 4, n + 188 - 1, n2 + 17 - 4, ProductionHud.withAlpha(n3 % 2 == 0 ? frameColors.accentB() : frameColors.accentA(), Math.min(170, n5)));
                break;
            }
            case CUSTOM: {
                int n12 = n3 % 2 == 0 ? frameColors.accentA() : frameColors.accentB();
                guiGraphics.fill(n + 1, n2 + 2, n + 2, n2 + 17 - 2, ProductionHud.withAlpha(n12, Math.min(220, n5 + 30)));
            }
        }
    }

    private static void renderThemeMark(GuiGraphics guiGraphics, int n, int n2, int n3, ClientConfig.HudFrameStyle hudFrameStyle, FrameColors frameColors) {
        if (hudFrameStyle == ClientConfig.HudFrameStyle.NONE) {
            return;
        }
        int n4 = Math.min(255, n3 + 65);
        int n5 = n + 5;
        int n6 = n2 + 5;
        switch (hudFrameStyle) {
            case STANDARD: {
                guiGraphics.fill(n5, n6, n5 + 8, n6 + 8, ProductionHud.withAlpha(frameColors.accentB(), n4));
                guiGraphics.fill(n5 + 1, n6 + 1, n5 + 7, n6 + 7, ProductionHud.withAlpha(frameColors.accentA(), n4));
                guiGraphics.fill(n5 + 3, n6 + 3, n5 + 5, n6 + 5, ProductionHud.withAlpha(frameColors.header(), 255));
                break;
            }
            case ORITECH: {
                guiGraphics.fill(n5 + 2, n6, n5 + 6, n6 + 8, ProductionHud.withAlpha(frameColors.accentA(), n4));
                guiGraphics.fill(n5, n6 + 2, n5 + 8, n6 + 6, ProductionHud.withAlpha(frameColors.accentA(), n4));
                guiGraphics.fill(n5 + 2, n6 + 2, n5 + 6, n6 + 6, ProductionHud.withAlpha(frameColors.accentB(), 255));
                guiGraphics.fill(n5 + 3, n6 + 3, n5 + 5, n6 + 5, ProductionHud.withAlpha(2758926, 255));
                break;
            }
            case AE2: {
                guiGraphics.fill(n5 + 3, n6, n5 + 5, n6 + 1, ProductionHud.withAlpha(frameColors.accentA(), 255));
                guiGraphics.fill(n5 + 1, n6 + 1, n5 + 7, n6 + 3, ProductionHud.withAlpha(frameColors.accentA(), n4));
                guiGraphics.fill(n5, n6 + 3, n5 + 8, n6 + 5, ProductionHud.withAlpha(frameColors.accentA(), n4));
                guiGraphics.fill(n5 + 1, n6 + 5, n5 + 7, n6 + 7, ProductionHud.withAlpha(frameColors.accentB(), n4));
                guiGraphics.fill(n5 + 3, n6 + 7, n5 + 5, n6 + 8, ProductionHud.withAlpha(frameColors.accentB(), 255));
                guiGraphics.fill(n5 + 3, n6 + 3, n5 + 5, n6 + 5, ProductionHud.withAlpha(0xF5FCFF, 255));
                break;
            }
            case FOREVER: {
                guiGraphics.fill(n5, n6 + 2, n5 + 3, n6 + 6, ProductionHud.withAlpha(frameColors.accentA(), n4));
                guiGraphics.fill(n5 + 1, n6 + 1, n5 + 4, n6 + 3, ProductionHud.withAlpha(frameColors.accentA(), 255));
                guiGraphics.fill(n5 + 5, n6 + 2, n5 + 8, n6 + 6, ProductionHud.withAlpha(frameColors.accentB(), n4));
                guiGraphics.fill(n5 + 4, n6 + 5, n5 + 7, n6 + 7, ProductionHud.withAlpha(frameColors.accentB(), 255));
                guiGraphics.fill(n5 + 3, n6 + 3, n5 + 5, n6 + 5, ProductionHud.withAlpha(15979007, 255));
                break;
            }
            case CUSTOM: {
                guiGraphics.fill(n5, n6, n5 + 8, n6 + 8, ProductionHud.withAlpha(frameColors.accentA(), n4));
                guiGraphics.fill(n5 + 2, n6 + 2, n5 + 8, n6 + 8, ProductionHud.withAlpha(frameColors.accentB(), n4));
                guiGraphics.fill(n5 + 3, n6 + 3, n5 + 6, n6 + 6, ProductionHud.withAlpha(frameColors.panel(), 255));
                break;
            }
        }
    }

    private static void cornerBrackets(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6) {
        guiGraphics.fill(n - 2, n2 - 2, n + 7, n2 - 1, ProductionHud.withAlpha(n4, n6));
        guiGraphics.fill(n - 2, n2 - 2, n - 1, n2 + 7, ProductionHud.withAlpha(n4, n6));
        guiGraphics.fill(n + 188 - 7, n2 + n3 + 1, n + 188 + 2, n2 + n3 + 2, ProductionHud.withAlpha(n5, n6));
        guiGraphics.fill(n + 188 + 1, n2 + n3 - 7, n + 188 + 2, n2 + n3 + 2, ProductionHud.withAlpha(n5, n6));
    }

    private static void rivet(GuiGraphics guiGraphics, int n, int n2, int n3) {
        guiGraphics.fill(n, n2, n + 2, n2 + 2, ProductionHud.withAlpha(2758926, 255));
        guiGraphics.fill(n, n2, n + 1, n2 + 1, ProductionHud.withAlpha(n3, 255));
    }

    private static void circuitTrace(GuiGraphics guiGraphics, int n, int n2, int n3, boolean bl) {
        int n4 = bl ? 1 : -1;
        int n5 = Math.min(n, n + n4);
        int n6 = Math.max(n, n + n4);
        guiGraphics.fill(n5, n2 - 5, n6, n2 + 1, ProductionHud.withAlpha(n3, 230));
        int n7 = Math.min(n, n + n4 * 5);
        int n8 = Math.max(n, n + n4 * 5);
        guiGraphics.fill(n7, n2 - 5, n8, n2 - 4, ProductionHud.withAlpha(n3, 230));
        guiGraphics.fill(n + n4 * 5 - 1, n2 - 6, n + n4 * 5 + 1, n2 - 4, ProductionHud.withAlpha(15268863, 255));
    }

    private static FrameColors frameColors(ClientConfig.HudFrameStyle hudFrameStyle) {
        return switch (hudFrameStyle) {
            default -> throw new IncompatibleClassChangeError();
            case ClientConfig.HudFrameStyle.NONE -> new FrameColors(526863, 526863, 0x11151B, 2106413, 1580068, 1185309, 0, 0, 15527924);
            case ClientConfig.HudFrameStyle.STANDARD -> new FrameColors(329224, 5199458, 1316637, 2764600, 1843240, 1514274, 12107464, 6646904, 15987958);
            case ClientConfig.HudFrameStyle.ORITECH -> new FrameColors(460292, 9261092, 1315087, 3416855, 2169364, 1643794, 15760168, 15187029, 16769185);
            case ClientConfig.HudFrameStyle.AE2 -> new FrameColors(397337, 6128790, 988959, 1519677, 1319981, 1055780, 5296872, 10580456, 14023423);
            case ClientConfig.HudFrameStyle.FOREVER -> new FrameColors(460043, 6834806, 1183513, 2825268, 2103077, 1577503, 14852936, 9200334, 16769445);
            case ClientConfig.HudFrameStyle.CUSTOM -> {
                float var1_1 = (float)ClientConfig.effectiveHudCustomHue() / 359.0f;
                int var2_2 = ProductionHud.hsvToRgb(var1_1, 0.72f, 0.95f);
                int var3_3 = ProductionHud.hsvToRgb((var1_1 + 0.09f) % 1.0f, 0.48f, 1.0f);
                int var4_4 = ProductionHud.blend(var2_2, 329224, 0.8f);
                int var5_5 = ProductionHud.blend(var2_2, 2435636, 0.5f);
                int var6_6 = ProductionHud.blend(var2_2, 1053466, 0.9f);
                int var7_7 = ProductionHud.blend(var2_2, 2106414, 0.7f);
                int var8_8 = ProductionHud.blend(var2_2, 1580325, 0.87f);
                int var9_9 = ProductionHud.blend(var2_2, 1185566, 0.9f);
                yield new FrameColors(var4_4, var5_5, var6_6, var7_7, var8_8, var9_9, var2_2, var3_3, 16118266);
            }
        };
    }

    private static int withAlpha(int n, int n2) {
        return Math.max(0, Math.min(255, n2)) << 24 | n & 0xFFFFFF;
    }

    private static int blend(int n, int n2, float f) {
        float f2 = 1.0f - f;
        int n3 = Math.round((float)(n >> 16 & 0xFF) * f2 + (float)(n2 >> 16 & 0xFF) * f);
        int n4 = Math.round((float)(n >> 8 & 0xFF) * f2 + (float)(n2 >> 8 & 0xFF) * f);
        int n5 = Math.round((float)(n & 0xFF) * f2 + (float)(n2 & 0xFF) * f);
        return n3 << 16 | n4 << 8 | n5;
    }

    private static int hsvToRgb(float f, float f2, float f3) {
        float f4;
        float f5;
        float f6 = f - (float)Math.floor(f);
        float f7 = f6 * 6.0f;
        int n = (int)f7;
        float f8 = f7 - (float)n;
        float f9 = f3 * (1.0f - f2);
        float f10 = f3 * (1.0f - f8 * f2);
        float f11 = f3 * (1.0f - (1.0f - f8) * f2);
        float blue = switch (n % 6) {
            case 0 -> {
                f5 = f3;
                f4 = f11;
                yield f9;
            }
            case 1 -> {
                f5 = f10;
                f4 = f3;
                yield f9;
            }
            case 2 -> {
                f5 = f9;
                f4 = f3;
                yield f11;
            }
            case 3 -> {
                f5 = f9;
                f4 = f10;
                yield f3;
            }
            case 4 -> {
                f5 = f11;
                f4 = f9;
                yield f3;
            }
            default -> {
                f5 = f3;
                f4 = f9;
                yield f10;
            }
        };
        return Math.round(f5 * 255.0f) << 16 | Math.round(f4 * 255.0f) << 8 | Math.round(blue * 255.0f);
    }

    private static List<MonitorNetwork.Entry> prepareEntries(
            MonitorNetwork.HudMode hudMode, List<MonitorNetwork.Entry> entries) {
        boolean includeItems = (Boolean)ClientConfig.VALUES.hudIncludeItems.get();
        boolean includeFluids = (Boolean)ClientConfig.VALUES.hudIncludeFluids.get();
        boolean includeEnergy = (Boolean)ClientConfig.VALUES.hudIncludeEnergy.get();
        boolean includeInfinite = (Boolean)ClientConfig.VALUES.hudIncludeInfinite.get();

        java.util.stream.Stream<MonitorNetwork.Entry> stream = entries.stream().filter(entry -> {
            if (entry.infinite() && !includeInfinite) {
                return false;
            }
            return switch (entry.kind()) {
                case ITEM -> includeItems;
                case FLUID -> includeFluids;
                case ENERGY -> includeEnergy;
            };
        });

        if (hudMode == MonitorNetwork.HudMode.STORED) {
            Comparator<MonitorNetwork.Entry> comparator = Comparator.comparingLong(MonitorNetwork.Entry::stored);
            if (ClientConfig.VALUES.hudStoredSort.get() == ClientConfig.HudStoredSort.HIGHEST) {
                comparator = comparator.reversed();
            }
            stream = stream.sorted(comparator);
        }
        return stream.toList();
    }

    private static List<MonitorNetwork.Entry> previewEntries(MonitorNetwork.HudMode hudMode) {
        if (hudMode == MonitorNetwork.HudMode.ENERGY) {
            return List.of(new MonitorNetwork.Entry(MonitorNetwork.EntryKind.ENERGY, (AEKey)AEItemKey.of((ItemLike)AEBlocks.ENERGY_CELL), 48250000L, 128400L, 121600L, 0L, false, false));
        }
        long l = hudMode == MonitorNetwork.HudMode.OUTGOING ? -1L : 1L;
        return List.of(ProductionHud.previewEntry((ItemLike)Items.IRON_INGOT, 248640L, l * 3840L), ProductionHud.previewEntry((ItemLike)Items.REDSTONE, 186420L, l * 1920L), ProductionHud.previewEntry((ItemLike)Items.QUARTZ, 94800L, l * 660L), ProductionHud.previewEntry((ItemLike)Items.DIAMOND, 12460L, l * 180L), ProductionHud.previewEntry((ItemLike)Items.COBBLESTONE, 1200000L, l * 120L), ProductionHud.previewEntry((ItemLike)Items.GOLD_INGOT, 86240L, l * 96L), ProductionHud.previewEntry((ItemLike)Items.COPPER_INGOT, 72640L, l * 72L), ProductionHud.previewEntry((ItemLike)Items.COAL, 54720L, l * 48L), ProductionHud.previewEntry((ItemLike)Items.LAPIS_LAZULI, 31360L, l * 24L), ProductionHud.previewEntry((ItemLike)Items.EMERALD, 8120L, l * 12L));
    }

    private static MonitorNetwork.Entry previewEntry(ItemLike itemLike, long l, long l2) {
        return new MonitorNetwork.Entry(MonitorNetwork.EntryKind.ITEM, (AEKey)AEItemKey.of((ItemLike)itemLike), l, l2, l2, 0L, false, false);
    }

    private static String displayValue(MonitorNetwork.HudMode hudMode, MonitorNetwork.Entry entry) {
        if (entry.infinite()) {
            return Component.translatable((String)"hud.forever_production_monitor.infinite").getString();
        }
        if (hudMode == MonitorNetwork.HudMode.STORED || hudMode == MonitorNetwork.HudMode.ENERGY) {
            return ProductionHud.format(entry.stored()) + ProductionHud.quantitySuffix(entry.kind());
        }
        long l = entry.averagePerMinute();
        ClientConfig.RateUnit rateUnit = (ClientConfig.RateUnit)((Object)ClientConfig.VALUES.rateUnit.get());
        return (l > 0L ? "+" : "") + ProductionHud.format(rateUnit.scale(l)) + ProductionHud.quantitySuffix(entry.kind()) + rateUnit.suffix();
    }

    private static Component displayName(MonitorNetwork.Entry entry) {
        return entry.kind() == MonitorNetwork.EntryKind.ENERGY ? Component.translatable((String)"screen.forever_production_monitor.energy") : AEKeyRendering.getDisplayName((AEKey)entry.key());
    }

    private static String quantitySuffix(MonitorNetwork.EntryKind entryKind) {
        return switch (entryKind) {
            default -> throw new IncompatibleClassChangeError();
            case MonitorNetwork.EntryKind.ITEM -> "";
            case MonitorNetwork.EntryKind.FLUID -> " mB";
            case MonitorNetwork.EntryKind.ENERGY -> " FE";
        };
    }

    private static int rateColor(long l) {
        return l > 0L ? -9972847 : (l < 0L ? -35716 : -7366746);
    }

    private static String format(long l) {
        return ProductionHud.format((double)l);
    }

    private static String format(double d) {
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

    private record FrameColors(int outer, int border, int panel, int header, int rowEven, int rowOdd, int accentA, int accentB, int titleText) {
    }
}
