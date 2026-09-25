package de.timo.foreverproductionmonitor.client.screen;

import de.timo.foreverproductionmonitor.client.ClientConfig;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Lightweight, client-only ambient visuals that give every interface theme a
 * distinct identity without textures, particles, allocations, or server work.
 *
 * Static motifs remain visible when animations are disabled. Continuous motion
 * is controlled by the existing ambient-motion settings.
 */
final class ThemeAmbientRenderer {
    private static final int BODY_INSET = 3;
    private static final int AE2_FLUIX_DARK = 0xFF5A479E;
    private static final int AE2_FLUIX_MEDIUM = 0xFF915DCD;
    private static final int AE2_FLUIX_BRIGHT = 0xFFE2A3E3;
    private static final int AE2_CELL_EMPTY = 0xFF00FF00;
    private static final int AE2_CELL_USED = 0xFF00AAFF;
    private static final int AE2_CELL_TYPES_FULL = 0xFFFFAA00;
    private static final int AE2_CELL_FULL = 0xFFFF0000;

    private ThemeAmbientRenderer() {
    }

    static void drawPanel(GuiGraphics graphics, int x, int y, int width, int height,
                          ClientConfig.InterfaceStyle style, InterfaceTheme.Palette palette) {
        if (width < 48 || height < 58) {
            return;
        }

        int left = x + BODY_INSET;
        int top = y + 35;
        int right = x + width - BODY_INSET;
        int bottom = y + height - BODY_INSET;
        if (right <= left || bottom <= top) {
            return;
        }

        graphics.enableScissor(left, top, right, bottom);
        switch (style) {
            case STANDARD -> standardPanel(graphics, left, top, right, bottom, palette);
            case FOREVER -> foreverPanel(graphics, left, top, right, bottom, palette);
            case AE2 -> ae2Panel(graphics, left, top, right, bottom, palette);
            case ORITECH -> oritechPanel(graphics, left, top, right, bottom, palette);
            case MEKANISM -> mekanismPanel(graphics, left, top, right, bottom, palette);
            case QUANTUM -> quantumPanel(graphics, left, top, right, bottom, palette);
            case HOLOGRAPHIC -> holographicPanel(graphics, left, top, right, bottom, palette);
            case MONOCHROME -> monochromePanel(graphics, left, top, right, bottom, palette);
            case MINIMAL -> minimalPanel(graphics, left, top, right, bottom, palette);
            case CARBON -> carbonPanel(graphics, left, top, right, bottom, palette);
            case TERMINAL -> terminalPanel(graphics, left, top, right, bottom, palette);
            case DEEP_SPACE -> deepSpacePanel(graphics, left, top, right, bottom, palette);
            case COPPER -> copperPanel(graphics, left, top, right, bottom, palette);
            case AURORA -> auroraPanel(graphics, left, top, right, bottom, palette);
            case REDSTONE -> redstonePanel(graphics, left, top, right, bottom, palette);
            case FROST -> frostPanel(graphics, left, top, right, bottom, palette);
            case NATURE -> naturePanel(graphics, left, top, right, bottom, palette);
            case CUSTOM -> customPanel(graphics, left, top, right, bottom, palette);
        }
        graphics.disableScissor();
    }

    static void drawButton(GuiGraphics graphics, int x, int y, int width, int height,
                           ClientConfig.InterfaceStyle style, InterfaceTheme.Palette palette,
                           boolean active, float hoverProgress) {
        if (width < 18 || height < 8) {
            return;
        }

        int a = active ? palette.accentB() : palette.accentA();
        int b = active ? palette.accentA() : palette.accentB();
        int subtle = alpha(a, 72);
        int bright = alpha(b, 115);
        int bottom = y + height - 2;

        switch (style) {
            case STANDARD -> {
                int span = Math.max(6, width / 4);
                graphics.fill(x + 3, bottom, x + 3 + span, bottom + 1, subtle);
                graphics.fill(x + width - 3 - span, bottom, x + width - 3, bottom + 1, alpha(b, 52));
            }
            case FOREVER -> {
                int split = x + width * 3 / 5;
                graphics.fill(x + 3, bottom, split, bottom + 1, alpha(a, 84));
                graphics.fill(split, bottom, x + width - 3, bottom + 1, alpha(b, 84));
                int breathe = GuiMotion.ambientMotionEnabled()
                        ? 1 + (int)Math.round((0.5 + 0.5 * Math.sin(phase(9000L, 0.25f) * Math.PI * 2.0)) * 2.0)
                        : 2;
                corner(graphics, x + 4, y + 3, 4 + breathe, alpha(a, 60), true, true);
                corner(graphics, x + width - 5, y + height - 4, 4 + breathe, alpha(b, 60), false, false);
                diamond(graphics, split, y + height / 2, 1, alpha(palette.text(), 74));
            }
            case AE2 -> {
                int cy = y + height / 2;
                graphics.fill(x + 4, cy, x + width - 5, cy + 1, alpha(AE2_FLUIX_DARK, 96));
                int cellCount = Math.max(2, Math.min(5, (width - 24) / 15));
                for (int i = 0; i < cellCount; ++i) {
                    int cellX = x + 8 + i * Math.max(12, (width - 18) / cellCount);
                    int led = ae2CellColor(i);
                    graphics.fill(cellX, y + 4, Math.min(cellX + 8, x + width - 5), y + height - 4, alpha(0xFF22252B, 180));
                    graphics.fill(cellX + 2, y + height - 6, Math.min(cellX + 6, x + width - 6), y + height - 4, alpha(led, 122));
                }
                int channels = Math.max(2, Math.min(4, width / 34));
                for (int i = 0; i < channels; ++i) {
                    int px = x + 6 + i * Math.max(15, (width - 14) / channels);
                    graphics.fill(px, cy - 2, px + 4, cy - 1, alpha(i % 2 == 0 ? AE2_FLUIX_MEDIUM : AE2_FLUIX_BRIGHT, 74));
                }
            }
            case ORITECH -> {
                graphics.fill(x + 3, y + 3, x + width - 13, y + 4, alpha(palette.border(), 64));
                graphics.fill(x + 3, bottom, x + width - 13, bottom + 1, alpha(palette.border(), 58));
                for (int px = x + 8; px < x + width - 18; px += 16) {
                    rivet(graphics, px, y + height / 2 - 1, alpha(palette.accentA(), 62));
                }
                rotor(graphics, x + width - 8, y + height / 2, 4, alpha(palette.border(), 84), bright, phase(7600L, 0.18f));
            }
            case MEKANISM -> {
                int cy = y + height / 2;
                graphics.fill(x + 3, y + 3, x + 4, y + height - 3, alpha(a, 70));
                graphics.fill(x + width - 4, y + 3, x + width - 3, y + height - 3, alpha(b, 70));
                graphics.fill(x + 4, cy, x + width - 4, cy + 1, alpha(palette.border(), 54));
                energyCore(graphics, x + width / 2, cy, 4, palette, phase(9200L, 0.22f), 68);
                int tankTop = y + 4;
                int tankBottom = y + height - 4;
                graphics.fill(x + 7, tankTop, x + 11, tankBottom, alpha(palette.border(), 66));
                graphics.fill(x + width - 11, tankTop, x + width - 7, tankBottom, alpha(palette.border(), 66));
                int fill = Math.max(2, (tankBottom - tankTop - 2) * 2 / 3);
                graphics.fill(x + 8, tankBottom - fill, x + 10, tankBottom - 1, alpha(palette.accentA(), 60));
                graphics.fill(x + width - 10, tankBottom - fill / 2, x + width - 8, tankBottom - 1, alpha(palette.accentB(), 56));
            }
            case QUANTUM -> {
                int cx = x + width / 2;
                int cy = y + height / 2;
                rectFrame(graphics, cx - 8, cy - 5, cx + 8, cy + 5, alpha(palette.border(), 58));
                rectFrame(graphics, cx - 5, cy - 3, cx + 5, cy + 3, subtle);
                int phaseStep = Math.floorMod((int)(phase(7000L, 0.24f) * 4.0f), 4);
                if ((phaseStep & 1) == 0) {
                    graphics.fill(cx - 8, cy - 5, cx - 1, cy - 4, bright);
                    graphics.fill(cx + 1, cy + 5, cx + 9, cy + 6, bright);
                } else {
                    graphics.fill(cx - 8, cy - 5, cx - 7, cy + 1, bright);
                    graphics.fill(cx + 8, cy - 1, cx + 9, cy + 6, bright);
                }
                diamond(graphics, cx, cy, 1, alpha(palette.text(), 118));
            }
            case HOLOGRAPHIC -> {
                corner(graphics, x + 2, y + 2, 5, subtle, true, true);
                corner(graphics, x + width - 3, y + height - 3, 5, bright, false, false);
                int py = y + 2 + (int)((height - 5) * phase(1800L, 0.5f));
                graphics.fill(x + 7, py, x + width - 7, py + 1, alpha(a, 46));
            }
            case MONOCHROME -> {
                for (int px = x + 3; px < x + width - 3; px += 7) {
                    graphics.fill(px, bottom, Math.min(px + 3, x + width - 3), bottom + 1, alpha(palette.text(), 55));
                }
            }
            case MINIMAL -> {
                int span = Math.max(8, (int)((width - 8) * Math.max(0.28f, hoverProgress)));
                int start = x + (width - span) / 2;
                graphics.fill(start, bottom, start + span, bottom + 1, alpha(a, 82));
            }
            case CARBON -> {
                graphics.fill(x + 3, y + 2, x + width - 3, y + 3, alpha(palette.border(), 70));
                for (int px = x + 5; px < x + width - 4; px += 12) {
                    graphics.fill(px, bottom - 1, Math.min(px + 5, x + width - 4), bottom, subtle);
                }
            }
            case TERMINAL -> {
                graphics.fill(x + 3, y + 3, x + 4, y + height - 3, subtle);
                if (!GuiMotion.ambientMotionEnabled() || phase(900L, 0.2f) < 0.62f) {
                    graphics.fill(x + width - 8, bottom - 2, x + width - 4, bottom, bright);
                }
            }
            case DEEP_SPACE -> {
                star(graphics, x + 5, y + 4, 1, alpha(palette.text(), 95));
                star(graphics, x + width - 7, y + height - 5, 1, bright);
                int sx = x + width / 2 + (int)(Math.sin(phase(3200L, 0.3f) * Math.PI * 2.0) * 3.0);
                star(graphics, sx, y + 3, 2, alpha(palette.accentB(), 105));
            }
            case COPPER -> {
                rivet(graphics, x + 4, y + 4, subtle);
                rivet(graphics, x + width - 6, y + height - 6, bright);
                int sheen = x - 8 + (int)((width + 16) * phase(12000L, 0.3f));
                graphics.fill(sheen, y + 2, Math.min(sheen + 12, x + width - 2), y + 3, alpha(palette.text(), motionAlpha(3, 16)));
                graphics.fill(x + 6, bottom, x + width - 6, bottom + 1, alpha(palette.border(), 46));
            }
            case AURORA -> {
                int split = x + width / 2;
                int wave = GuiMotion.ambientMotionEnabled() ? (int)Math.round(Math.sin(phase(7600L, 0.35f) * Math.PI * 2.0)) : 0;
                graphics.fill(x + 3, bottom - 1 + wave, split, bottom + wave, alpha(a, 72));
                graphics.fill(split, bottom - wave, x + width - 3, bottom + 1 - wave, alpha(b, 72));
                graphics.fill(x + width / 4, y + 3, x + width * 3 / 4, y + 4, alpha(palette.text(), 18));
            }
            case REDSTONE -> {
                int cy = y + height / 2;
                boolean powered = phase(6800L, 0.2f) < 0.56f;
                int dust = alpha(powered ? palette.accentA() : palette.border(), powered ? 108 : 52);
                graphics.fill(x + 3, cy, x + width - 12, cy + 1, dust);
                redstoneRepeater(graphics, x + width / 2 - 4, cy - 4, powered, palette);
                redstoneLamp(graphics, x + width - 9, cy - 3, powered, palette);
            }
            case FROST -> {
                crystal(graphics, x + 4, y + height / 2, 4, subtle);
                crystal(graphics, x + width - 5, y + height / 2, 3, bright);
            }
            case NATURE -> {
                int stemY = bottom - 1;
                graphics.fill(x + 3, stemY, x + width - 3, stemY + 1, alpha(a, 74));
                int sway = GuiMotion.ambientMotionEnabled() ? (int)Math.round(Math.sin(phase(5200L, 0.3f) * Math.PI * 2.0)) : 0;
                leaf(graphics, x + width / 3 + sway, stemY - 2, subtle, true);
                leaf(graphics, x + width * 2 / 3 - sway, stemY - 3, alpha(a, 86), false);
                if (active || hoverProgress > 0.35f) {
                    flower(graphics, x + width - 9, stemY - 2, alpha(palette.accentB(), 118), alpha(palette.text(), 105));
                } else {
                    bud(graphics, x + width - 8, stemY - 2, alpha(palette.accentB(), 82));
                }
            }
            case CUSTOM -> {
                int split = x + width / 2;
                graphics.fill(x + 3, bottom, split, bottom + 1, alpha(a, 75));
                graphics.fill(split, bottom, x + width - 3, bottom + 1, alpha(b, 75));
                node(graphics, split - 1, bottom - 1, alpha(palette.text(), 80));
            }
        }
    }

    static void drawTable(GuiGraphics graphics, int x, int y, int width, int height,
                          ClientConfig.InterfaceStyle style, InterfaceTheme.Palette palette) {
        if (width < 24 || height < 12) {
            return;
        }

        int right = x + width;
        int bottom = y + height;
        int a = alpha(palette.accentA(), 42);
        int b = alpha(palette.accentB(), 38);

        switch (style) {
            case STANDARD -> {
                for (int px = x + 12; px < right - 6; px += 48) {
                    graphics.fill(px, y + 2, Math.min(px + 18, right - 4), y + 3, a);
                }
            }
            case FOREVER -> {
                int split = x + width * 3 / 5;
                graphics.fill(x + 2, y + 2, split, y + 3, a);
                graphics.fill(split, y + 2, right - 2, y + 3, b);
            }
            case AE2 -> {
                int cells = Math.max(3, Math.min(7, width / 44));
                int usable = Math.max(1, width - 16);
                for (int i = 0; i < cells; ++i) {
                    int cellX = x + 6 + i * usable / cells;
                    int cellW = Math.max(12, usable / cells - 4);
                    int led = ae2CellColor(i);
                    graphics.fill(cellX, y + 3, cellX + cellW, Math.min(bottom - 3, y + 14), alpha(0xFF24272D, 176));
                    graphics.fill(cellX + 2, y + 5, cellX + cellW - 2, y + 6, alpha(AE2_FLUIX_DARK, 70));
                    graphics.fill(cellX + 2, Math.min(bottom - 6, y + 10), cellX + cellW - 2, Math.min(bottom - 4, y + 12), alpha(led, 108));
                }
                graphics.fill(x + 5, bottom - 3, right - 5, bottom - 2, alpha(AE2_FLUIX_MEDIUM, 42));
            }
            case ORITECH -> {
                rivet(graphics, x + 4, y + 4, a);
                rivet(graphics, right - 6, bottom - 6, b);
                int seam = x + width * 2 / 3;
                graphics.fill(seam, y + 2, seam + 1, bottom - 2, alpha(palette.border(), 42));
                for (int px = x + 12; px < seam - 5; px += 18) {
                    graphics.fill(px, bottom - 4, Math.min(px + 10, seam - 4), bottom - 3, alpha(palette.border(), 44));
                }
                rotor(graphics, right - 12, y + Math.min(10, height / 2), 4, alpha(palette.border(), 76), b, phase(8800L, 0.21f));
                piston(graphics, seam + 8, bottom - 5, Math.max(8, right - seam - 24), true,
                        alpha(palette.border(), 68), alpha(palette.accentA(), 72), phase(7200L, 0.4f));
            }
            case MEKANISM -> {
                int cy = y + height / 2;
                segmentedRail(graphics, x + 3, y + 3, bottom - 3, palette.accentA(), palette.border());
                segmentedRail(graphics, right - 4, y + 3, bottom - 3, palette.accentB(), palette.border());
                graphics.fill(x + 5, cy, right - 5, cy + 1, alpha(palette.border(), 34));
                energyCore(graphics, x + Math.min(width - 14, Math.max(14, width / 5)), cy, 5, palette, phase(10500L, 0.32f), 62);
                int gaugeX = right - Math.min(18, Math.max(10, width / 8));
                graphics.fill(gaugeX, y + 4, gaugeX + 6, bottom - 4, alpha(palette.border(), 58));
                int gaugeH = Math.max(2, (height - 10) * 3 / 5);
                graphics.fill(gaugeX + 1, bottom - 5 - gaugeH, gaugeX + 5, bottom - 5, alpha(palette.accentB(), 54));
            }
            case QUANTUM -> {
                int cx = x + width / 2;
                int cy = y + height / 2;
                int hw = Math.min(24, Math.max(10, width / 8));
                int hh = Math.min(8, Math.max(4, height / 4));
                rectFrame(graphics, cx - hw, cy - hh, cx + hw, cy + hh, alpha(palette.border(), 38));
                rectFrame(graphics, cx - hw / 2, cy - Math.max(2, hh / 2), cx + hw / 2, cy + Math.max(2, hh / 2), a);
                diamond(graphics, cx, cy, 1, alpha(palette.accentB(), 84));
            }
            case HOLOGRAPHIC -> {
                for (int py = y + 4; py < bottom - 3; py += 9) {
                    graphics.fill(x + 2, py, right - 2, py + 1, alpha(palette.accentA(), 18));
                }
            }
            case MONOCHROME -> {
                for (int px = x + 5; px < right - 3; px += 11) {
                    graphics.fill(px, bottom - 3, Math.min(px + 5, right - 3), bottom - 2, alpha(palette.text(), 30));
                }
            }
            case MINIMAL -> graphics.fill(x + width / 4, y + 2, x + width * 3 / 4, y + 3, a);
            case CARBON -> {
                for (int px = x + 4; px < right - 4; px += 14) {
                    graphics.fill(px, y + 2, Math.min(px + 6, right - 4), y + 3, alpha(palette.border(), 42));
                }
            }
            case TERMINAL -> {
                for (int py = y + 4; py < bottom - 3; py += 8) {
                    graphics.fill(x + 2, py, right - 2, py + 1, alpha(palette.accentA(), 22));
                }
                graphics.fill(x + 4, bottom - 4, x + 11, bottom - 2, alpha(palette.accentB(), 55));
            }
            case DEEP_SPACE -> {
                star(graphics, x + width / 4, y + 6, 1, alpha(palette.text(), 66));
                star(graphics, x + width * 3 / 4, bottom - 7, 1, alpha(palette.accentB(), 72));
                star(graphics, right - 11, y + height / 2, 1, alpha(palette.accentA(), 52));
            }
            case COPPER -> {
                rivet(graphics, x + 4, y + 4, a);
                rivet(graphics, right - 6, y + 4, b);
                graphics.fill(x + 3, bottom - 4, right - 3, bottom - 3, alpha(palette.border(), 50));
            }
            case AURORA -> {
                for (int px = x + 3; px < right - 3; px += 6) {
                    float t = (float)(px - x) / Math.max(1, width);
                    int py = y + 3 + (int)((Math.sin(t * Math.PI * 2.0 + phase(4800L, 0.15f) * Math.PI * 2.0) + 1.0) * 1.5);
                    graphics.fill(px, py, Math.min(px + 5, right - 3), py + 1, t < 0.5f ? a : b);
                }
            }
            case REDSTONE -> {
                int cy = y + height / 2;
                boolean powered = phase(7600L, 0.2f) < 0.52f;
                int dust = alpha(powered ? palette.accentA() : palette.border(), powered ? 88 : 40);
                graphics.fill(x + 3, cy, right - 12, cy + 1, dust);
                redstoneRepeater(graphics, x + width / 2 - 4, cy - 4, powered, palette);
                redstoneLamp(graphics, right - 9, cy - 3, powered, palette);
            }
            case FROST -> {
                crystal(graphics, x + 5, y + 7, 4, a);
                crystal(graphics, right - 6, bottom - 7, 4, b);
            }
            case NATURE -> {
                int vineX = x + 4;
                graphics.fill(vineX, y + 3, vineX + 1, bottom - 3, alpha(palette.accentA(), 46));
                leaf(graphics, vineX + 3, y + height / 3, alpha(palette.accentA(), 62), true);
                leaf(graphics, vineX + 2, y + height * 2 / 3, alpha(palette.accentA(), 54), false);
                flower(graphics, right - 9, y + 7, alpha(palette.accentB(), 78), alpha(palette.text(), 64));
                graphics.fill(x + 7, bottom - 4, right - 14, bottom - 3, alpha(palette.accentA(), 30));
                leaf(graphics, x + width / 2, bottom - 4, alpha(palette.accentA(), 45), true);
            }
            case CUSTOM -> {
                graphics.fill(x + 2, y + 2, x + width / 2, y + 3, a);
                graphics.fill(x + width / 2, y + 2, right - 2, y + 3, b);
                node(graphics, x + width / 2 - 1, y + 2, alpha(palette.text(), 52));
            }
        }
    }

    private static void standardPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;
        for (int x = l + 24; x < r; x += 48) {
            g.fill(x, t + 4, x + 1, b - 4, alpha(p.border(), 12));
        }
        for (int y = t + 20; y < b; y += 32) {
            g.fill(l + 4, y, r - 4, y + 1, alpha(p.border(), 10));
        }
        for (int i = 0; i < 5; ++i) {
            int tickX = l + 8 + i * Math.max(18, (width - 16) / 5);
            g.fill(tickX, t + 5, tickX + 8, t + 6, alpha(p.accentA(), 34));
        }
        if (GuiMotion.ambientMotionEnabled()) {
            int sweep = l + 5 + (int)((width - 11) * phase(14000L, 0.42f));
            g.fill(sweep, t + 3, sweep + 1, b - 3, alpha(p.accentA(), motionAlpha(8, 22)));
        }
    }

    private static void foreverPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;
        int cx = l + width * 3 / 5;
        int cy = t + height / 2;

        // Forever's own visual language: paired geometric frames that slowly breathe,
        // with no data packets or matrix-like sweeps.
        int maxInset = Math.max(8, Math.min(28, Math.min(width, height) / 7));
        float breathe = 0.5f + 0.5f * (float)Math.sin(phase(18000L, 0.28f) * Math.PI * 2.0);
        int inset = 6 + (int)(maxInset * breathe * 0.45f);
        corner(g, l + inset, t + inset, 13, alpha(p.accentA(), 50), true, true);
        corner(g, r - inset - 1, b - inset - 1, 13, alpha(p.accentB(), 50), false, false);
        corner(g, r - inset - 1, t + inset, 8, alpha(p.accentB(), 30), false, true);
        corner(g, l + inset, b - inset - 1, 8, alpha(p.accentA(), 30), true, false);

        int ringW = Math.max(18, Math.min(width / 5, 42));
        int ringH = Math.max(12, Math.min(height / 5, 28));
        rectFrame(g, cx - ringW, cy - ringH, cx + ringW, cy + ringH, alpha(p.border(), 28));
        rectFrame(g, cx - ringW / 2, cy - ringH / 2, cx + ringW / 2, cy + ringH / 2, alpha(p.accentA(), 34));
        diamond(g, cx, cy, 3, alpha(p.accentB(), 68));

        // Slow symmetric shutters open and close around the central mark.
        int travel = Math.max(5, ringW / 2);
        int shutter = (int)(travel * (0.25f + 0.75f * breathe));
        g.fill(cx - ringW - 7, cy - 1, cx - ringW - 7 + shutter, cy + 2, alpha(p.accentA(), 42));
        g.fill(cx + ringW + 7 - shutter, cy - 1, cx + ringW + 7, cy + 2, alpha(p.accentB(), 42));

        for (int x = l + 9; x < r - 9; x += 26) {
            g.fill(x, b - 6, Math.min(x + 12, r - 7), b - 5, alpha(x < cx ? p.accentA() : p.accentB(), 24));
        }
    }

    private static void ae2Panel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;

        // Closer to AE2: neutral machine surfaces, Fluix-colored smart-cable backbone,
        // channel indicators and a recognizable 10-cell ME Drive bank.
        int cableY = t + Math.max(18, height / 3);
        int cableLeft = l + 9;
        int driveLeft = l + width * 3 / 5;
        g.fill(cableLeft, cableY - 2, driveLeft - 8, cableY + 3, alpha(0xFF24272D, 170));
        g.fill(cableLeft, cableY - 1, driveLeft - 8, cableY + 2, alpha(AE2_FLUIX_DARK, 105));

        // Smart-cable channel bars, based on Fluix bright/medium variants.
        int channelCount = Math.max(4, Math.min(8, (driveLeft - cableLeft - 10) / 18));
        for (int i = 0; i < channelCount; ++i) {
            int px = cableLeft + 7 + i * Math.max(12, (driveLeft - cableLeft - 16) / channelCount);
            int channelColor = (i & 1) == 0 ? AE2_FLUIX_MEDIUM : AE2_FLUIX_BRIGHT;
            int h = 3 + (i % 3);
            g.fill(px, cableY - 5 - h, px + 4, cableY - 5, alpha(channelColor, 72));
        }

        // Terminal-like attached panels.
        int termY = cableY + 11;
        for (int i = 0; i < 3; ++i) {
            int tx = cableLeft + i * 31;
            if (tx + 24 >= driveLeft - 7) break;
            g.fill(tx, termY, tx + 24, termY + 15, alpha(0xFF30333A, 180));
            g.fill(tx + 1, termY + 1, tx + 23, termY + 14, alpha(0xFF17191E, 188));
            g.fill(tx + 4, termY + 4, tx + 19, termY + 6, alpha(AE2_FLUIX_MEDIUM, 54));
            g.fill(tx + 4, termY + 9, tx + 14, termY + 10, alpha(AE2_FLUIX_BRIGHT, 40));
        }

        // ME Drive silhouette: 10 slots, with official LED state colors:
        // green empty, blue used, orange types full, red bytes full.
        int driveTop = t + 7;
        int driveRight = r - 8;
        int driveWidth = Math.max(34, driveRight - driveLeft);
        g.fill(driveLeft, driveTop, driveRight, b - 8, alpha(0xFF3A3D44, 182));
        g.fill(driveLeft + 2, driveTop + 2, driveRight - 2, b - 10, alpha(0xFF17191E, 190));

        int slotW = Math.max(11, (driveWidth - 10) / 2);
        int slotH = Math.max(9, Math.min(18, (height - 30) / 5));
        for (int i = 0; i < 10; ++i) {
            int col = i % 2;
            int row = i / 2;
            int sx = driveLeft + 4 + col * (slotW + 2);
            int sy = driveTop + 4 + row * (slotH + 2);
            if (sx + slotW >= driveRight - 2 || sy + slotH >= b - 10) continue;
            int led = ae2CellColor(i);
            g.fill(sx, sy, sx + slotW, sy + slotH, alpha(0xFF292C32, 188));
            g.fill(sx + 2, sy + 2, sx + slotW - 2, sy + 3, alpha(AE2_FLUIX_DARK, 52));
            g.fill(sx + 2, sy + slotH - 4, sx + slotW - 2, sy + slotH - 2, alpha(led, 116));
        }

        // Very subtle activity: one drive LED briefly brightens; no moving blob.
        if (GuiMotion.ambientMotionEnabled()) {
            int active = Math.floorMod((int)(phase(12000L, 0.3f) * 10.0f), 10);
            int col = active % 2;
            int row = active / 2;
            int sx = driveLeft + 4 + col * (slotW + 2);
            int sy = driveTop + 4 + row * (slotH + 2);
            if (sx + slotW < driveRight - 2 && sy + slotH < b - 10) {
                int led = ae2CellColor(active);
                g.fill(sx + 2, sy + slotH - 5, sx + slotW - 2, sy + slotH - 1, alpha(led, motionAlpha(72, 145)));
            }
        }
    }

    private static void oritechPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;
        int seamA = l + width / 3;
        int seamB = l + width * 2 / 3;

        // Heavy plate construction: seams, bolts and machine housings.
        g.fill(seamA, t + 4, seamA + 1, b - 4, alpha(p.border(), 48));
        g.fill(seamB, t + 4, seamB + 1, b - 4, alpha(p.border(), 48));
        g.fill(l + 4, t + height / 2, r - 4, t + height / 2 + 1, alpha(p.border(), 38));
        rivet(g, l + 6, t + 6, alpha(p.accentB(), 68));
        rivet(g, r - 8, t + 6, alpha(p.accentA(), 64));
        rivet(g, l + 6, b - 8, alpha(p.accentA(), 64));
        rivet(g, r - 8, b - 8, alpha(p.accentB(), 68));

        // Slow mechanical drive: gears connected by a physical belt and shaft.
        float turnA = phase(15000L, 0.14f);
        float turnB = 1.0f - phase(19000L, 0.67f);
        int gearAX = l + width / 4;
        int gearAY = t + height / 3;
        int gearBX = l + width / 2;
        int gearBY = t + height * 2 / 3;
        rotor(g, gearAX, gearAY, 9, alpha(p.border(), 92), alpha(p.accentB(), 74), turnA);
        rotor(g, gearBX, gearBY, 7, alpha(p.border(), 88), alpha(p.accentA(), 70), turnB);
        belt(g, gearAX + 9, gearAY, gearBX - 7, gearBY, alpha(p.border(), 62));

        int shaftY = t + height / 3;
        g.fill(gearAX + 10, shaftY - 1, seamB - 4, shaftY + 2, alpha(p.border(), 58));
        for (int x = gearAX + 16; x < seamB - 6; x += 13) {
            g.fill(x, shaftY - 2, x + 2, shaftY + 3, alpha(p.accentA(), 32));
        }

        // Hydraulic-looking actuator. Motion is deliberately slow and physical.
        int pistonY = t + height / 2 + 11;
        piston(g, seamB + 7, pistonY, Math.max(12, r - seamB - 18), true,
               alpha(p.border(), 74), alpha(p.accentB(), 82), phase(12000L, 0.42f));

        // Machine feet / lower rail.
        for (int x = l + 10; x < r - 10; x += 18) {
            g.fill(x, b - 6, Math.min(x + 11, r - 8), b - 4,
                    alpha((x / 18 & 1) == 0 ? p.accentA() : p.border(), 40));
        }
    }

    private static void mekanismPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;
        int leftRail = l + 12;
        int rightRail = r - 13;
        int cx = l + width / 2;
        int cy = t + height / 2;

        // Mekanism as an industrial process machine, not a digital network.
        segmentedRail(g, leftRail, t + 7, b - 7, p.accentA(), p.border());
        segmentedRail(g, rightRail, t + 7, b - 7, p.accentB(), p.border());

        // Central contained power core with slow reactor-like breathing.
        g.fill(leftRail + 3, cy, cx - 12, cy + 1, alpha(p.border(), 38));
        g.fill(cx + 12, cy, rightRail - 2, cy + 1, alpha(p.border(), 38));
        energyCore(g, cx, cy, 10, p, phase(18000L, 0.24f), 86);

        // Process tanks with slowly changing levels.
        int tankTop = t + 10;
        int tankBottom = Math.min(b - 11, tankTop + Math.max(30, height / 2));
        int[] tankXs = {l + width / 4, l + width * 3 / 4};
        for (int i = 0; i < tankXs.length; ++i) {
            int tx = tankXs[i];
            int fluid = i == 0 ? p.accentA() : p.accentB();
            g.fill(tx - 7, tankTop, tx + 8, tankBottom, alpha(p.border(), 64));
            g.fill(tx - 6, tankTop + 1, tx + 7, tankBottom - 1, alpha(p.tableOuter(), 100));
            float tankPhase = phase(26000L + i * 7000L, 0.21f + i * 0.28f);
            int innerH = Math.max(4, tankBottom - tankTop - 5);
            int fill = 4 + (int)(innerH * (0.28f + 0.46f * (0.5f + 0.5f * (float)Math.sin(tankPhase * Math.PI * 2.0))));
            g.fill(tx - 5, tankBottom - 2 - fill, tx + 6, tankBottom - 2, alpha(fluid, 56));
            g.fill(tx - 4, tankTop + 5, tx + 5, tankTop + 6, alpha(p.text(), 22));
        }

        // Pressure / process gauge with a rotating needle, not a moving packet.
        int gaugeX = cx;
        int gaugeY = Math.min(b - 18, t + 18);
        gauge(g, gaugeX, gaugeY, 7, p, phase(21000L, 0.33f));

        // Fixed pipe joints.
        pipeJoint(g, leftRail, cy, alpha(p.accentA(), 70), alpha(p.border(), 76));
        pipeJoint(g, rightRail - 1, cy, alpha(p.accentB(), 70), alpha(p.border(), 76));
    }

    private static void quantumPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;
        int cx = l + width / 2;
        int cy = t + height / 2;

        // Quantum Computer inspired: large symmetric multiblock frame, nested cells,
        // central core and phase-shifting modules instead of generic orbiting dots.
        int outerHalfW = Math.min(70, Math.max(28, width / 4));
        int outerHalfH = Math.min(46, Math.max(22, height / 3));
        rectFrame(g, cx - outerHalfW, cy - outerHalfH, cx + outerHalfW, cy + outerHalfH, alpha(p.border(), 44));

        int midHalfW = Math.max(18, outerHalfW - 15);
        int midHalfH = Math.max(14, outerHalfH - 11);
        rectFrame(g, cx - midHalfW, cy - midHalfH, cx + midHalfW, cy + midHalfH, alpha(p.accentA(), 36));

        int innerHalfW = Math.max(10, midHalfW - 14);
        int innerHalfH = Math.max(8, midHalfH - 10);
        rectFrame(g, cx - innerHalfW, cy - innerHalfH, cx + innerHalfW, cy + innerHalfH, alpha(p.accentB(), 48));

        // Corner compute modules.
        int moduleW = 12;
        int moduleH = 9;
        quantumModule(g, cx - outerHalfW - 2, cy - outerHalfH - 2, moduleW, moduleH, p.accentA(), p.border(), 0);
        quantumModule(g, cx + outerHalfW - moduleW + 2, cy - outerHalfH - 2, moduleW, moduleH, p.accentB(), p.border(), 1);
        quantumModule(g, cx - outerHalfW - 2, cy + outerHalfH - moduleH + 2, moduleW, moduleH, p.accentB(), p.border(), 2);
        quantumModule(g, cx + outerHalfW - moduleW + 2, cy + outerHalfH - moduleH + 2, moduleW, moduleH, p.accentA(), p.border(), 3);

        // Phase cycle changes whole frame segments, not particle positions.
        float phaseA = phase(18000L, 0.18f);
        int phaseStep = Math.floorMod((int)(phaseA * 8.0f), 8);
        int phaseColor = alpha(phaseStep < 4 ? p.accentA() : p.accentB(), motionAlpha(42, 92));
        if ((phaseStep & 1) == 0) {
            g.fill(cx - outerHalfW + 8, cy - outerHalfH - 1, cx - 5, cy - outerHalfH + 2, phaseColor);
            g.fill(cx + 5, cy + outerHalfH - 1, cx + outerHalfW - 8, cy + outerHalfH + 2, phaseColor);
        } else {
            g.fill(cx - outerHalfW - 1, cy - outerHalfH + 8, cx - outerHalfW + 2, cy - 5, phaseColor);
            g.fill(cx + outerHalfW - 1, cy + 5, cx + outerHalfW + 2, cy + outerHalfH - 8, phaseColor);
        }

        // Central computation core slowly breathes between two states.
        float breathe = 0.5f + 0.5f * (float)Math.sin(phase(9600L, 0.27f) * Math.PI * 2.0);
        int coreRadius = 4 + (int)(breathe * 3.0f);
        diamond(g, cx, cy, coreRadius + 3, alpha(p.border(), 34));
        diamond(g, cx, cy, coreRadius, alpha(p.accentA(), 48 + (int)(breathe * 30.0f)));
        diamond(g, cx, cy, Math.max(2, coreRadius - 3), alpha(p.accentB(), 72 + (int)(breathe * 36.0f)));
        g.fill(cx, cy, cx + 1, cy + 1, alpha(p.text(), 150));
    }

    private static void holographicPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        // Hologram = scan + projection instability, not travelling particles.
        for (int y = t + 3; y < b - 2; y += 7) {
            g.fill(l + 2, y, r - 2, y + 1, alpha(p.accentA(), 15));
        }
        int scanY = t + (int)((b - t - 2) * phase(7200L, 0.42f));
        g.fill(l + 3, scanY, r - 3, Math.min(scanY + 2, b - 2), alpha(p.accentB(), motionAlpha(22, 58)));
        corner(g, l + 5, t + 5, 9, alpha(p.accentA(), 68), true, true);
        corner(g, r - 6, b - 6, 9, alpha(p.accentB(), 62), false, false);

        if (GuiMotion.ambientMotionEnabled()) {
            int glitchStep = Math.floorMod((int)(phase(5300L, 0.3f) * 12.0f), 12);
            if (glitchStep == 2 || glitchStep == 7) {
                int gy = t + 14 + glitchStep * Math.max(3, (b - t - 28) / 12);
                int gx = l + 10 + mod(hash(glitchStep * 73 + 19), Math.max(1, r - l - 48));
                g.fill(gx, gy, Math.min(gx + 28, r - 8), gy + 1, alpha(p.text(), 34));
                g.fill(Math.max(l + 6, gx - 8), gy + 3, Math.min(gx + 14, r - 8), gy + 4, alpha(p.accentB(), 28));
            }
        }
    }

    private static void monochromePanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        // Old diagnostic display: fixed phosphor grid, waveform and slow refresh bar.
        for (int y = t + 5; y < b - 3; y += 5) {
            g.fill(l + 3, y, r - 3, y + 1, alpha(p.text(), 7));
        }
        int mid = t + (b - t) / 2;
        int prevX = l + 7;
        int prevY = mid;
        for (int x = l + 8; x < r - 8; x += 5) {
            double wave = Math.sin((x - l) * 0.065) * 4.0 + Math.sin((x - l) * 0.021) * 3.0;
            int y = mid + (int)Math.round(wave);
            diagonal(g, prevX, prevY, x, y, alpha(p.text(), 34));
            prevX = x;
            prevY = y;
        }
        if (GuiMotion.ambientMotionEnabled()) {
            int scanY = t + 3 + (int)((b - t - 7) * phase(15000L, 0.46f));
            g.fill(l + 4, scanY, r - 4, scanY + 1, alpha(p.text(), motionAlpha(8, 26)));
        }
        for (int i = 0; i < 8; ++i) {
            int h = 3 + mod(hash(120 + i * 13), 12);
            g.fill(r - 8 - i * 3, b - 5 - h, r - 7 - i * 3, b - 5, alpha(p.muted(), 25 + i * 2));
        }
    }

    private static void minimalPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        // Deliberately almost motionless. Its identity is the absence of ambient clutter.
        int cy = t + (b - t) / 2;
        int width = r - l;
        g.fill(l + width / 4, cy, r - width / 4, cy + 1, alpha(p.accentA(), 16));
        g.fill(l + 8, t + 8, l + 20, t + 9, alpha(p.accentA(), 34));
        g.fill(r - 20, b - 9, r - 8, b - 8, alpha(p.accentB(), 34));
        g.fill(l + width / 2, cy - 2, l + width / 2 + 1, cy + 3, alpha(p.text(), 26));
    }

    private static void carbonPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;
        int rowStep = Math.max(18, height / 8);
        int columnStep = Math.max(28, width / 9);

        // Material weave only.
        for (int row = 0; row < 8; ++row) {
            int y = t + 7 + row * rowStep;
            if (y >= b - 5) break;
            int offset = (row & 1) == 0 ? 0 : columnStep / 2;
            for (int x = l + 5 + offset; x < r - 8; x += columnStep) {
                g.fill(x, y, Math.min(x + 11, r - 5), y + 1, alpha(p.border(), 24));
                if (y + 5 < b - 3) {
                    g.fill(x + 5, y + 5, Math.min(x + 16, r - 4), y + 6, alpha(p.outer(), 18));
                }
            }
        }

        // Slow reflective sheen behaves like light on a material surface.
        int sheen = l - 36 + (int)((width + 72) * phase(22000L, 0.28f));
        diagonal(g, sheen, t + 5, sheen + Math.min(62, height), Math.min(b - 5, t + 67), alpha(p.accentA(), motionAlpha(5, 20)));
        diagonal(g, sheen + 5, t + 5, sheen + 5 + Math.min(62, height), Math.min(b - 5, t + 67), alpha(p.text(), motionAlpha(3, 10)));
        rivet(g, l + 6, t + 6, alpha(p.accentA(), 50));
        rivet(g, r - 8, b - 8, alpha(p.accentB(), 42));
    }

    private static void terminalPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        // This is intentionally the one strongly "matrix-like" theme.
        for (int y = t + 4; y < b - 3; y += 8) {
            g.fill(l + 2, y, r - 2, y + 1, alpha(p.accentA(), 12));
        }
        int width = r - l;
        int height = b - t;
        for (int i = 0; i < 16; ++i) {
            int x = l + 6 + mod(hash(301 + i * 47), Math.max(1, width - 12));
            int base = mod(hash(707 + i * 31), Math.max(1, height - 14));
            int drift = GuiMotion.ambientMotionEnabled() ? (int)(phase(8200L + i * 173L, i / 16.0f) * (height + 18)) : 0;
            int y = t + mod(base + drift, Math.max(1, height - 8));
            int len = 2 + mod(hash(900 + i * 17), 8);
            g.fill(x, y, x + 1, Math.min(b - 3, y + len), alpha(i % 4 == 0 ? p.accentB() : p.accentA(), 24 + (i % 4) * 7));
        }
        if (!GuiMotion.ambientMotionEnabled() || phase(1100L, 0.2f) < 0.58f) {
            g.fill(l + 8, b - 9, l + 15, b - 7, alpha(p.accentB(), 82));
        }
    }

    private static void deepSpacePanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;

        // Three star layers with very slow parallax drift.
        float twinkle = phase(13000L, 0.23f);
        int[] counts = {18, 13, 9};
        long[] speeds = {90000L, 65000L, 43000L};
        int[] alphas = {34, 48, 68};
        for (int layer = 0; layer < 3; ++layer) {
            int drift = GuiMotion.ambientMotionEnabled() ? (int)(phase(speeds[layer], 0.17f * (layer + 1)) * width) : 0;
            for (int i = 0; i < counts[layer]; ++i) {
                int seed = 1103 + layer * 1009 + i * 97;
                int sx = l + mod(hash(seed) + drift, Math.max(1, width));
                int sy = t + 3 + mod(hash(seed + 811), Math.max(1, height - 6));
                double shimmer = 0.5 + 0.5 * Math.sin(twinkle * Math.PI * 2.0 + i * 1.37 + layer);
                int starAlpha = alphas[layer] + (int)(shimmer * (GuiMotion.ambientMotionEnabled() ? 42 : 18));
                int color = i % 5 == 0 ? p.accentB() : (i % 3 == 0 ? p.accentA() : p.text());
                star(g, sx, sy, layer == 2 && i % 4 == 0 ? 2 : 1, alpha(color, starAlpha));
            }
        }

        // A recognizable constellation drifts as one object instead of independent blobs.
        int constellationX = l + mod((int)(phase(76000L, 0.36f) * (width + 70)) - 35, Math.max(1, width));
        int constellationY = t + Math.max(16, height / 4);
        int[][] pts = {{0, 4}, {13, 0}, {25, 8}, {39, 3}, {52, 12}};
        for (int i = 0; i < pts.length - 1; ++i) {
            int x1 = constellationX + pts[i][0];
            int y1 = constellationY + pts[i][1];
            int x2 = constellationX + pts[i + 1][0];
            int y2 = constellationY + pts[i + 1][1];
            diagonal(g, x1, y1, x2, y2, alpha(p.accentA(), 22));
            star(g, x1, y1, 1, alpha(p.text(), 86));
        }
        star(g, constellationX + pts[pts.length - 1][0], constellationY + pts[pts.length - 1][1], 1, alpha(p.text(), 86));

        // Very slow galaxy.
        int gx = l + width * 3 / 4;
        int gy = t + height / 3;
        float galaxyTurn = phase(52000L, 0.11f) * 0.45f;
        for (int arm = 0; arm < 2; ++arm) {
            for (int i = 0; i < 18; ++i) {
                double angle = i * 0.47 + arm * Math.PI + galaxyTurn * Math.PI * 2.0;
                double radius = 2.0 + i * 0.72;
                int px = gx + (int)Math.round(Math.cos(angle) * radius);
                int py = gy + (int)Math.round(Math.sin(angle) * radius * 0.48);
                int color = arm == 0 ? p.accentA() : p.accentB();
                star(g, px, py, i < 4 ? 2 : 1, alpha(color, Math.max(18, 72 - i * 2)));
            }
        }
        star(g, gx, gy, 2, alpha(p.text(), 90));

        // Black hole with slow accretion rotation.
        int bx = l + Math.max(20, width / 5);
        int by = t + height * 2 / 3;
        float blackHoleTurn = phase(36000L, 0.36f);
        for (int i = 0; i < 18; ++i) {
            double angle = i * Math.PI * 2.0 / 18.0 + blackHoleTurn * Math.PI * 2.0;
            int px = bx + (int)Math.round(Math.cos(angle) * 12.0);
            int py = by + (int)Math.round(Math.sin(angle) * 4.5);
            int color = i < 9 ? p.accentB() : p.accentA();
            g.fill(px, py, px + 2, py + 1, alpha(color, 56 + (i % 3) * 9));
        }
        g.fill(bx - 4, by - 4, bx + 5, by + 5, 0xD9000000);
    }

    private static void copperPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;

        // Copper is material-first: plate seams, rivets and static patina.
        g.fill(l + width / 3, t + 4, l + width / 3 + 1, b - 4, alpha(p.border(), 40));
        g.fill(l + width * 2 / 3, t + 4, l + width * 2 / 3 + 1, b - 4, alpha(p.border(), 40));
        g.fill(l + 4, t + height / 2, r - 4, t + height / 2 + 1, alpha(p.border(), 32));
        rivet(g, l + 6, t + 6, alpha(p.accentA(), 76));
        rivet(g, r - 8, t + 6, alpha(p.accentA(), 64));
        rivet(g, l + 6, b - 8, alpha(p.accentB(), 60));
        rivet(g, r - 8, b - 8, alpha(p.accentB(), 72));

        int patina = alpha(p.accentB(), 24);
        for (int i = 0; i < 7; ++i) {
            int px = l + 12 + mod(hash(4021 + i * 43), Math.max(1, width - 24));
            int py = t + 10 + mod(hash(5099 + i * 37), Math.max(1, height - 20));
            g.fill(px, py, px + 5 + i % 3, py + 2, patina);
            if ((i & 1) == 0) {
                g.fill(px + 2, py - 2, px + 4, py + 4, alpha(p.accentB(), 16));
            }
        }

        // Replaces the previous particle-like diagonal sheen with a broad heat/reflection band.
        float warm = 0.5f + 0.5f * (float)Math.sin(phase(26000L, 0.31f) * Math.PI * 2.0);
        int bandAlpha = motionAlpha(5, 18 + (int)(warm * 12));
        int bandWidth = Math.max(20, width / 5);
        int bandX = l + (width - bandWidth) / 2;
        g.fill(bandX, t + 5, bandX + bandWidth, b - 5, alpha(p.accentA(), bandAlpha));
        g.fill(bandX + 3, t + 7, bandX + bandWidth - 3, t + 8, alpha(p.text(), Math.max(3, bandAlpha / 2)));
    }

    private static void auroraPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;
        float flow = phase(26000L, 0.16f);

        // Large continuous curtains only: no particle field.
        for (int ribbon = 0; ribbon < 4; ++ribbon) {
            int baseY = t + height * (ribbon + 1) / 5;
            int color = ribbon % 2 == 0 ? p.accentA() : p.accentB();
            int opacity = 16 + ribbon * 5;
            for (int px = l + 2; px < r - 3; px += 4) {
                float u = (float)(px - l) / Math.max(1, width);
                double wave = Math.sin(u * Math.PI * (1.8 + ribbon * 0.33) + flow * Math.PI * 2.0 + ribbon * 1.55);
                double wave2 = Math.sin(u * Math.PI * 0.72 - flow * Math.PI * 1.4 + ribbon) * 0.45;
                int py = baseY + (int)Math.round((wave + wave2) * (5 + ribbon));
                g.fill(px, py, Math.min(px + 5, r - 2), py + 2, alpha(color, opacity));
                if (ribbon == 1 || ribbon == 2) {
                    g.fill(px, py + 2, Math.min(px + 5, r - 2), py + 3, alpha(color, Math.max(5, opacity - 8)));
                }
            }
        }
        g.fill(l + 3, b - 4, l + width / 2, b - 3, alpha(p.accentA(), 34));
        g.fill(l + width / 2, b - 4, r - 3, b - 3, alpha(p.accentB(), 34));
    }

    private static void redstonePanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;
        int midY = t + height / 2;
        int upperY = t + height / 3;
        int lowerY = t + height * 2 / 3;

        // Discrete Minecraft-like redstone circuit states. Nothing travels like a data packet.
        float cycle = phase(12000L, 0.18f);
        boolean inputOn = cycle < 0.62f;
        boolean branchOn = cycle > 0.18f && cycle < 0.76f;
        boolean outputOn = cycle > 0.34f && cycle < 0.90f;

        int dustOff = alpha(p.border(), 52);
        int dustOn = alpha(p.accentA(), 112);

        // Main dust line.
        g.fill(l + 7, midY, r - 14, midY + 1, inputOn ? dustOn : dustOff);
        redstoneRepeater(g, l + width / 3 - 4, midY - 4, inputOn, p);
        redstoneRepeater(g, l + width * 2 / 3 - 4, midY - 4, branchOn, p);

        // Two branches with torch/repeater style logic.
        int branchX = l + width / 2;
        g.fill(branchX, upperY, branchX + 1, midY, branchOn ? dustOn : dustOff);
        g.fill(branchX, midY, branchX + 1, lowerY, outputOn ? dustOn : dustOff);
        g.fill(branchX, upperY, r - 15, upperY + 1, branchOn ? dustOn : dustOff);
        g.fill(branchX, lowerY, r - 15, lowerY + 1, outputOn ? dustOn : dustOff);
        redstoneTorch(g, branchX - 1, upperY - 4, branchOn, p);
        redstoneTorch(g, branchX - 1, lowerY + 2, outputOn, p);

        // Lamps turn on/off as circuit states change.
        redstoneLamp(g, r - 11, midY - 4, outputOn, p);
        redstoneLamp(g, r - 11, upperY - 4, branchOn, p);
        redstoneLamp(g, r - 11, lowerY - 4, outputOn && inputOn, p);

        // Lever-like input switch at the left.
        redstoneLever(g, l + 9, midY - 5, inputOn, p);
    }

    private static void frostPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;

        // Growing ice veins remain the main animation.
        float grow = growthCycle(phase(34000L, 0.74f));
        int reachX = Math.max(6, (int)(width * 0.42f * grow));
        int reachY = Math.max(6, (int)(height * 0.56f * grow));
        int leftBaseX = l + 5;
        int leftBaseY = t + 7;
        diagonal(g, leftBaseX, leftBaseY, leftBaseX + reachX, leftBaseY + Math.min(reachY, height / 2), alpha(p.accentB(), 54));
        if (grow > 0.28f) {
            int bx = leftBaseX + reachX / 2;
            int by = leftBaseY + Math.min(reachY, height / 2) / 2;
            diagonal(g, bx, by, bx + Math.max(4, reachX / 4), by - Math.max(3, reachY / 5), alpha(p.text(), 34));
            crystal(g, bx, by, Math.max(3, Math.min(6, 2 + (int)(grow * 5))), alpha(p.accentA(), 46));
        }

        float grow2 = growthCycle(phase(41000L, 0.37f));
        int reachX2 = Math.max(6, (int)(width * 0.36f * grow2));
        int reachY2 = Math.max(6, (int)(height * 0.48f * grow2));
        int rightBaseX = r - 6;
        int rightBaseY = b - 8;
        diagonal(g, rightBaseX, rightBaseY, rightBaseX - reachX2, rightBaseY - Math.min(reachY2, height / 2), alpha(p.accentA(), 52));
        if (grow2 > 0.35f) {
            int bx = rightBaseX - reachX2 / 2;
            int by = rightBaseY - Math.min(reachY2, height / 2) / 2;
            diagonal(g, bx, by, bx - Math.max(4, reachX2 / 4), by + Math.max(3, reachY2 / 5), alpha(p.accentB(), 34));
            crystal(g, bx, by, Math.max(3, Math.min(6, 2 + (int)(grow2 * 5))), alpha(p.text(), 38));
        }

        // Proper lightweight snowfall with deterministic wrap inside the clipped panel.
        if (GuiMotion.ambientMotionEnabled()) {
            for (int i = 0; i < 10; ++i) {
                int px = l + 7 + mod(hash(13007 + i * 83), Math.max(1, width - 14));
                long duration = 18000L + i * 1700L;
                float fall = phase(duration, i / 10.0f);
                int py = t - 5 + (int)(fall * (height + 12));
                int drift = (int)Math.round(Math.sin((fall + i * 0.17f) * Math.PI * 2.0) * (1 + i % 3));
                snowflake(g, px + drift, py, i % 4 == 0 ? 2 : 1, alpha(i % 3 == 0 ? p.accentB() : p.text(), 38 + (i % 4) * 8));
            }
        } else {
            for (int i = 0; i < 6; ++i) {
                int px = l + 9 + mod(hash(14009 + i * 71), Math.max(1, width - 18));
                int py = t + 8 + mod(hash(15013 + i * 59), Math.max(1, height - 16));
                snowflake(g, px, py, 1, alpha(p.text(), 34));
            }
        }

        // Small scenic details, intentionally tucked into the lower edge.
        int snowY = b - 8;
        g.fill(l + 5, snowY, r - 5, b - 5, alpha(p.text(), 18));
        if (width > 150 && height > 90) {
            snowman(g, l + 26, b - 13, p);
            igloo(g, r - 35, b - 7, p);
        }
    }

    private static void naturePanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;
        float cycle = phase(46000L, 0.76f);
        float growth = growthCycle(cycle);
        float swayPhase = phase(17000L, 0.31f);
        int sway = GuiMotion.ambientMotionEnabled() ? (int)Math.round(Math.sin(swayPhase * Math.PI * 2.0) * 2.0) : 0;

        // Main vine grows down the left edge.
        int vineX = l + 10;
        int maxVine = Math.max(10, height - 12);
        int grown = Math.max(3, (int)(maxVine * growth));
        g.fill(vineX, t + 5, vineX + 1, Math.min(b - 6, t + 5 + grown), alpha(p.accentA(), 56));

        int leafStep = Math.max(14, maxVine / 7);
        for (int i = 0; i < 7; ++i) {
            int localY = 10 + i * leafStep;
            if (localY > grown) break;
            int py = t + 5 + localY;
            boolean right = (i & 1) == 0;
            int branch = right ? 11 : -7;
            int ex = vineX + branch + (right ? sway : -sway);
            g.fill(Math.min(vineX, ex), py, Math.max(vineX, ex) + 1, py + 1, alpha(p.accentA(), 48));
            leaf(g, ex, py - 1, alpha(i % 3 == 0 ? p.accentB() : p.accentA(), 72), right);
            if ((i == 2 || i == 5) && growth > 0.58f) {
                float bloom = Math.min(1.0f, (growth - 0.58f) / 0.22f);
                flower(g, ex + (right ? 3 : -2), py - 4,
                        alpha(p.accentB(), 36 + (int)(bloom * 68)),
                        alpha(p.text(), 28 + (int)(bloom * 58)));
            }
        }

        // Independent hanging vine on the opposite side.
        float hangingGrowth = growthCycle(phase(53000L, 0.41f));
        int rightVine = r - 13;
        int hangingLength = Math.max(4, (int)(Math.min(height * 2 / 3, 96) * hangingGrowth));
        g.fill(rightVine, t + 4, rightVine + 1, Math.min(b - 8, t + 4 + hangingLength), alpha(p.accentA(), 44));
        for (int i = 0; i < 5; ++i) {
            int localY = 9 + i * 17;
            if (localY > hangingLength) break;
            int py = t + 4 + localY;
            boolean right = (i & 1) != 0;
            leaf(g, rightVine + (right ? 4 + sway : -3 - sway), py, alpha(p.accentA(), 58), right);
        }
        if (hangingGrowth > 0.72f) {
            float bloom = Math.min(1.0f, (hangingGrowth - 0.72f) / 0.18f);
            flower(g, rightVine, Math.min(t + 7 + hangingLength, b - 8),
                    alpha(p.accentB(), 42 + (int)(bloom * 70)),
                    alpha(p.text(), 34 + (int)(bloom * 54)));
        }

        // Ground cover spreads only after the main vine has established itself.
        float groundGrowth = Math.max(0.0f, Math.min(1.0f, (growth - 0.35f) / 0.55f));
        int groundY = b - 7;
        int groundEnd = l + 8 + (int)((width - 16) * groundGrowth);
        g.fill(l + 8, groundY, groundEnd, groundY + 1, alpha(p.accentA(), 44));
        for (int i = 0; i < 7; ++i) {
            int px = l + 18 + i * Math.max(18, (width - 36) / 7);
            if (px >= groundEnd || px >= r - 12) break;
            boolean right = (i & 1) == 0;
            leaf(g, px + (right ? sway : -sway), groundY - 1 - (i % 2), alpha(p.accentA(), 56), right);
            if ((i == 2 || i == 5) && growth > 0.72f) {
                flower(g, px, groundY - 5, alpha(p.accentB(), 78), alpha(p.text(), 62));
            }
        }

        // A small tree now grows through a full trunk -> branches -> crown cycle.
        if (width > 150 && height > 100) {
            float treeGrowth = growthCycle(phase(72000L, 0.18f));
            int treeX = l + width * 2 / 3;
            int treeBase = b - 9;
            growingTree(g, treeX, treeBase, Math.min(48, Math.max(28, height / 3)), treeGrowth, sway, p);
        }

        // One larger flower opens/closes independently.
        float bloomCycle = phase(28000L, 0.36f);
        float bloomSize = 0.45f + 0.55f * (0.5f + 0.5f * (float)Math.sin(bloomCycle * Math.PI * 2.0));
        int bx = l + width / 2;
        int by = t + height * 2 / 3 + sway;
        if (growth > 0.64f) {
            if (bloomSize > 0.62f) {
                flower(g, bx, by, alpha(p.accentB(), 78 + (int)(bloomSize * 28)), alpha(p.text(), 66));
            } else {
                bud(g, bx, by, alpha(p.accentB(), 72));
            }
        }
    }

    private static void customPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;

        // Neutral geometry so custom colors remain the visual focus.
        for (int i = 1; i < 5; ++i) {
            int x = l + width * i / 5;
            g.fill(x, t + 5, x + 1, b - 5, alpha(i % 2 == 0 ? p.accentA() : p.accentB(), 12));
        }
        for (int i = 1; i < 4; ++i) {
            int y = t + height * i / 4;
            g.fill(l + 5, y, r - 5, y + 1, alpha(i % 2 == 0 ? p.accentB() : p.accentA(), 10));
        }
        corner(g, l + 7, t + 7, 9, alpha(p.accentA(), 42), true, true);
        corner(g, r - 8, b - 8, 9, alpha(p.accentB(), 42), false, false);
        g.fill(l + width / 2, t + height / 2 - 3, l + width / 2 + 1, t + height / 2 + 4, alpha(p.text(), 22));
        g.fill(l + width / 2 - 3, t + height / 2, l + width / 2 + 4, t + height / 2 + 1, alpha(p.text(), 22));
    }

    private static int ae2CellColor(int index) {
        return switch (Math.floorMod(index, 5)) {
            case 0 -> AE2_CELL_EMPTY;
            case 1, 2 -> AE2_CELL_USED;
            case 3 -> AE2_CELL_TYPES_FULL;
            default -> AE2_CELL_FULL;
        };
    }

    private static void belt(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        diagonal(g, x1, y1 - 3, x2, y2 - 3, color);
        diagonal(g, x1, y1 + 3, x2, y2 + 3, alpha(color, Math.max(18, (color >>> 24) - 12)));
        for (int i = 1; i < 5; ++i) {
            int x = x1 + (x2 - x1) * i / 5;
            int y = y1 + (y2 - y1) * i / 5;
            g.fill(x - 1, y - 4, x + 1, y + 5, alpha(color, 34));
        }
    }

    private static void gauge(GuiGraphics g, int cx, int cy, int radius, InterfaceTheme.Palette p, float turn) {
        int r = Math.max(4, radius);
        g.fill(cx - r, cy, cx + r + 1, cy + 1, alpha(p.border(), 48));
        g.fill(cx, cy - r, cx + 1, cy + 2, alpha(p.border(), 42));
        diagonal(g, cx - r + 2, cy - 2, cx, cy - r + 2, alpha(p.accentA(), 40));
        diagonal(g, cx, cy - r + 2, cx + r - 2, cy - 2, alpha(p.accentB(), 40));
        double angle = Math.PI * (1.1 + 0.8 * (0.5 + 0.5 * Math.sin(turn * Math.PI * 2.0)));
        int nx = cx + (int)Math.round(Math.cos(angle) * (r - 2));
        int ny = cy + (int)Math.round(Math.sin(angle) * (r - 2));
        diagonal(g, cx, cy, nx, ny, alpha(p.text(), 72));
        g.fill(cx - 1, cy - 1, cx + 2, cy + 2, alpha(p.accentB(), 70));
    }

    private static void pipeJoint(GuiGraphics g, int cx, int cy, int accent, int border) {
        g.fill(cx - 3, cy - 3, cx + 4, cy + 4, border);
        g.fill(cx - 1, cy - 1, cx + 2, cy + 2, accent);
    }

    private static void redstoneRepeater(GuiGraphics g, int x, int y, boolean powered, InterfaceTheme.Palette p) {
        g.fill(x, y, x + 9, y + 7, alpha(0xFF6E6E6E, 110));
        g.fill(x + 1, y + 1, x + 8, y + 6, alpha(0xFFB9B9B9, 92));
        int torch = alpha(powered ? p.accentA() : p.border(), powered ? 124 : 58);
        g.fill(x + 2, y + 2, x + 4, y + 4, torch);
        g.fill(x + 6, y + 3, x + 8, y + 5, torch);
    }

    private static void redstoneTorch(GuiGraphics g, int x, int y, boolean powered, InterfaceTheme.Palette p) {
        g.fill(x, y + 2, x + 2, y + 7, alpha(0xFF6B3B22, 130));
        g.fill(x - 1, y, x + 3, y + 3, alpha(powered ? p.accentA() : p.border(), powered ? 128 : 54));
    }

    private static void redstoneLamp(GuiGraphics g, int x, int y, boolean powered, InterfaceTheme.Palette p) {
        g.fill(x, y, x + 8, y + 8, alpha(0xFF3A1B14, 140));
        int lamp = powered ? 0xFFFF7A24 : 0xFF5B241C;
        g.fill(x + 2, y + 2, x + 6, y + 6, alpha(lamp, powered ? 132 : 76));
        if (powered) {
            g.fill(x + 3, y + 1, x + 5, y + 7, alpha(p.accentB(), 36));
            g.fill(x + 1, y + 3, x + 7, y + 5, alpha(p.accentB(), 30));
        }
    }

    private static void redstoneLever(GuiGraphics g, int x, int y, boolean powered, InterfaceTheme.Palette p) {
        g.fill(x - 2, y + 5, x + 5, y + 8, alpha(0xFF6E6E6E, 110));
        if (powered) {
            diagonal(g, x, y + 5, x + 4, y, alpha(0xFF7A5135, 150));
            g.fill(x + 3, y - 1, x + 5, y + 1, alpha(p.accentA(), 94));
        } else {
            diagonal(g, x + 2, y + 5, x - 1, y, alpha(0xFF7A5135, 150));
            g.fill(x - 2, y - 1, x, y + 1, alpha(p.border(), 68));
        }
    }

    private static void snowflake(GuiGraphics g, int cx, int cy, int size, int color) {
        int r = Math.max(1, size);
        g.fill(cx - r, cy, cx + r + 1, cy + 1, color);
        g.fill(cx, cy - r, cx + 1, cy + r + 1, color);
        if (r > 1) {
            g.fill(cx - 1, cy - 1, cx, cy, alpha(color, Math.max(20, (color >>> 24) - 12)));
            g.fill(cx + 1, cy + 1, cx + 2, cy + 2, alpha(color, Math.max(20, (color >>> 24) - 12)));
        }
    }

    private static void snowman(GuiGraphics g, int cx, int groundY, InterfaceTheme.Palette p) {
        int snow = alpha(p.text(), 88);
        g.fill(cx - 5, groundY - 9, cx + 6, groundY + 1, snow);
        g.fill(cx - 3, groundY - 16, cx + 4, groundY - 8, snow);
        g.fill(cx - 2, groundY - 21, cx + 3, groundY - 15, snow);
        g.fill(cx - 1, groundY - 19, cx, groundY - 18, alpha(0xFF20242A, 140));
        g.fill(cx + 1, groundY - 19, cx + 2, groundY - 18, alpha(0xFF20242A, 140));
        g.fill(cx + 3, groundY - 17, cx + 6, groundY - 16, alpha(0xFFE88947, 120));
        g.fill(cx - 4, groundY - 23, cx + 5, groundY - 21, alpha(0xFF20242A, 120));
        g.fill(cx - 2, groundY - 27, cx + 3, groundY - 23, alpha(0xFF20242A, 120));
    }

    private static void igloo(GuiGraphics g, int cx, int groundY, InterfaceTheme.Palette p) {
        int ice = alpha(p.text(), 52);
        int shade = alpha(p.accentA(), 42);
        for (int row = 0; row < 5; ++row) {
            int half = 12 - row * 2;
            g.fill(cx - half, groundY - 3 - row * 4, cx + half + 1, groundY - row * 4, row % 2 == 0 ? ice : shade);
        }
        g.fill(cx - 4, groundY - 10, cx + 5, groundY + 1, alpha(0xFF203344, 100));
        g.fill(cx - 2, groundY - 8, cx + 3, groundY + 1, alpha(0xFF101A22, 130));
    }

    private static void growingTree(GuiGraphics g, int x, int baseY, int maxHeight, float growth, int sway,
                                    InterfaceTheme.Palette p) {
        float clamped = Math.max(0.0f, Math.min(1.0f, growth));
        int trunkHeight = Math.max(2, (int)(maxHeight * Math.min(1.0f, clamped / 0.58f)));
        int trunkColor = alpha(0xFF6A4728, 92);
        g.fill(x - 1, baseY - trunkHeight, x + 2, baseY, trunkColor);

        if (clamped > 0.28f) {
            int branchY = baseY - trunkHeight * 2 / 3;
            int branch = Math.max(4, trunkHeight / 4);
            diagonal(g, x, branchY, x - branch, branchY - branch / 2, trunkColor);
            diagonal(g, x, branchY - 3, x + branch + sway, branchY - branch / 2 - 3, trunkColor);
        }
        if (clamped > 0.48f) {
            int topY = baseY - trunkHeight;
            int crown = Math.max(4, (int)(10 * Math.min(1.0f, (clamped - 0.48f) / 0.30f)));
            leafCluster(g, x + sway, topY, crown, alpha(p.accentA(), 74));
            leafCluster(g, x - crown + sway / 2, topY + crown / 2, Math.max(3, crown - 2), alpha(p.accentA(), 62));
            leafCluster(g, x + crown - sway / 2, topY + crown / 2, Math.max(3, crown - 2), alpha(p.accentA(), 66));
            if (clamped > 0.78f) {
                flower(g, x + crown - 2, topY + 1, alpha(p.accentB(), 78), alpha(p.text(), 58));
                flower(g, x - crown + 3, topY + crown / 2, alpha(p.accentB(), 68), alpha(p.text(), 52));
            }
        }
    }

    private static void leafCluster(GuiGraphics g, int cx, int cy, int radius, int color) {
        int r = Math.max(2, radius);
        g.fill(cx - r, cy - r / 2, cx + r + 1, cy + r / 2 + 1, color);
        g.fill(cx - r / 2, cy - r, cx + r / 2 + 1, cy + r + 1, alpha(color, Math.max(24, (color >>> 24) - 8)));
    }

    private static float growthCycle(float phase) {
        float p = Math.max(0.0f, Math.min(1.0f, phase));
        if (p < 0.58f) {
            return p / 0.58f;
        }
        if (p < 0.86f) {
            return 1.0f;
        }
        return Math.max(0.0f, 1.0f - (p - 0.86f) / 0.14f);
    }

    private static void rectFrame(GuiGraphics g, int left, int top, int right, int bottom, int color) {
        if (right <= left || bottom <= top) return;
        g.fill(left, top, right + 1, top + 1, color);
        g.fill(left, bottom, right + 1, bottom + 1, color);
        g.fill(left, top, left + 1, bottom + 1, color);
        g.fill(right, top, right + 1, bottom + 1, color);
    }

    private static void quantumModule(GuiGraphics g, int x, int y, int width, int height,
                                      int accent, int border, int phaseOffset) {
        int w = Math.max(8, width);
        int h = Math.max(7, height);
        g.fill(x, y, x + w, y + h, alpha(border, 54));
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, alpha(0xFF050714, 86));
        float p = phase(15000L + phaseOffset * 1300L, 0.2f + phaseOffset * 0.13f);
        int step = Math.floorMod((int)(p * 6.0f), 6);
        if (step < 4) {
            g.fill(x + 2, y + 2, x + w - 2, y + 3, alpha(accent, 72));
            g.fill(x + 2, y + h - 3, x + w - 2, y + h - 2, alpha(accent, 42));
        } else {
            g.fill(x + 2, y + 2, x + 3, y + h - 2, alpha(accent, 64));
            g.fill(x + w - 3, y + 2, x + w - 2, y + h - 2, alpha(accent, 48));
        }
        diamond(g, x + w / 2, y + h / 2, 1, alpha(accent, 92));
    }

    private static void diamond(GuiGraphics g, int cx, int cy, int radius, int color) {
        int r = Math.max(1, radius);
        for (int dy = -r; dy <= r; ++dy) {
            int half = r - Math.abs(dy);
            g.fill(cx - half, cy + dy, cx + half + 1, cy + dy + 1, color);
        }
        g.fill(cx, cy, cx + 1, cy + 1, alpha(0xFFFFFFFF, Math.min(170, (color >>> 24) + 34)));
    }

    private static void meCell(GuiGraphics g, int x, int y, int width, int height, int glow, int border, int strength) {
        int w = Math.max(6, width);
        int h = Math.max(6, height);
        g.fill(x, y, x + w, y + h, alpha(border, 54));
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, alpha(0xFF050814, 76));
        int cx = x + w / 2;
        int cy = y + h / 2;
        diamond(g, cx, cy, Math.max(1, Math.min(3, Math.min(w, h) / 4)), alpha(glow, strength));
        g.fill(x + 2, y + h - 3, x + w - 2, y + h - 2, alpha(glow, Math.max(22, strength - 18)));
    }

    private static void rotor(GuiGraphics g, int cx, int cy, int radius, int body, int accent, float turn) {
        int r = Math.max(3, radius);
        int phase = Math.floorMod((int)(turn * 8.0f), 8);
        g.fill(cx - r + 1, cy - r + 1, cx + r, cy + r, alpha(body, Math.max(18, (body >>> 24) / 2)));
        g.fill(cx - 2, cy - 2, cx + 3, cy + 3, alpha(0xFF000000, 138));
        if ((phase & 1) == 0) {
            g.fill(cx - r, cy - 1, cx + r + 1, cy + 2, accent);
            g.fill(cx - 1, cy - r, cx + 2, cy + r + 1, accent);
        } else {
            diagonal(g, cx - r, cy - r, cx + r, cy + r, accent);
            diagonal(g, cx - r, cy + r, cx + r, cy - r, accent);
        }
        g.fill(cx - 1, cy - 1, cx + 2, cy + 2, alpha(body, Math.min(180, (body >>> 24) + 45)));
    }

    private static void piston(GuiGraphics g, int x, int y, int travel, boolean horizontal,
                               int body, int head, float position) {
        int range = Math.max(4, travel);
        int extension = Math.max(2, (int)(range * (0.25f + 0.65f * (0.5f + 0.5f * (float)Math.sin(position * Math.PI * 2.0)))));
        if (horizontal) {
            g.fill(x, y, x + range + 4, y + 3, alpha(body, 46));
            g.fill(x + 2, y + 1, x + extension + 2, y + 2, head);
            g.fill(x + extension, y - 2, x + extension + 4, y + 5, alpha(head, Math.min(150, (head >>> 24) + 28)));
        } else {
            g.fill(x, y, x + 3, y + range + 4, alpha(body, 46));
            g.fill(x + 1, y + 2, x + 2, y + extension + 2, head);
            g.fill(x - 2, y + extension, x + 5, y + extension + 4, alpha(head, Math.min(150, (head >>> 24) + 28)));
        }
    }

    private static void segmentedRail(GuiGraphics g, int x, int top, int bottom, int accent, int border) {
        g.fill(x, top, x + 2, bottom, alpha(border, 44));
        for (int y = top + 2; y < bottom - 2; y += 9) {
            g.fill(x - 1, y, x + 3, Math.min(y + 5, bottom - 1), alpha(accent, 55));
        }
    }

    private static void energyCore(GuiGraphics g, int cx, int cy, int radius,
                                   InterfaceTheme.Palette p, float turn, int strength) {
        int r = Math.max(4, radius);
        int outer = alpha(p.accentA(), Math.max(34, strength - 20));
        int inner = alpha(p.accentB(), strength);
        diamond(g, cx, cy, r, alpha(p.border(), 36));
        diamond(g, cx, cy, Math.max(2, r - 3), outer);
        diamond(g, cx, cy, Math.max(1, r - 5), inner);
        int phase = Math.floorMod((int)(turn * 8.0f), 8);
        int arm = r + 3;
        if ((phase & 1) == 0) {
            g.fill(cx - arm, cy - 1, cx - r, cy + 1, alpha(p.accentB(), 54));
            g.fill(cx + r + 1, cy - 1, cx + arm + 1, cy + 1, alpha(p.accentA(), 54));
        } else {
            g.fill(cx - 1, cy - arm, cx + 1, cy - r, alpha(p.accentA(), 54));
            g.fill(cx - 1, cy + r + 1, cx + 1, cy + arm + 1, alpha(p.accentB(), 54));
        }
        g.fill(cx, cy, cx + 1, cy + 1, alpha(p.text(), Math.min(190, strength + 42)));
    }

    private static void flower(GuiGraphics g, int cx, int cy, int petal, int center) {
        g.fill(cx - 1, cy - 3, cx + 2, cy, petal);
        g.fill(cx - 1, cy + 1, cx + 2, cy + 4, petal);
        g.fill(cx - 3, cy - 1, cx, cy + 2, petal);
        g.fill(cx + 1, cy - 1, cx + 4, cy + 2, petal);
        g.fill(cx, cy, cx + 1, cy + 1, center);
    }

    private static void bud(GuiGraphics g, int cx, int cy, int color) {
        g.fill(cx - 1, cy - 1, cx + 2, cy + 2, color);
        g.fill(cx, cy - 2, cx + 1, cy + 3, alpha(color, Math.max(30, (color >>> 24) - 12)));
    }

    private static float phase(long durationMillis, float fallback) {
        return GuiMotion.ambientMotionEnabled() ? GuiMotion.cycle(durationMillis) : fallback;
    }

    private static int motionAlpha(int base, int animated) {
        if (!GuiMotion.ambientMotionEnabled()) {
            return base;
        }
        float intensity = GuiMotion.ambientMotionIntensity();
        return Math.max(0, Math.min(255, Math.round(base + (animated - base) * intensity)));
    }

    private static int alpha(int color, int alpha) {
        return GuiMotion.alpha(color, alpha);
    }

    private static int hash(int value) {
        int x = value;
        x ^= x >>> 16;
        x *= 0x7FEB352D;
        x ^= x >>> 15;
        x *= 0x846CA68B;
        x ^= x >>> 16;
        return x;
    }

    private static int mod(int value, int modulo) {
        return Math.floorMod(value, Math.max(1, modulo));
    }

    private static void node(GuiGraphics g, int x, int y, int color) {
        g.fill(x, y, x + 3, y + 3, color);
        g.fill(x + 1, y + 1, x + 2, y + 2, alpha(0xFFFFFFFF, 90));
    }

    private static void star(GuiGraphics g, int x, int y, int size, int color) {
        if (size <= 1) {
            g.fill(x, y, x + 1, y + 1, color);
            return;
        }
        g.fill(x - size, y, x + size + 1, y + 1, color);
        g.fill(x, y - size, x + 1, y + size + 1, color);
        g.fill(x, y, x + 1, y + 1, alpha(0xFFFFFFFF, Math.min(180, (color >>> 24) + 40)));
    }

    private static void rivet(GuiGraphics g, int x, int y, int color) {
        g.fill(x, y, x + 3, y + 3, alpha(0xFF000000, 80));
        g.fill(x + 1, y + 1, x + 2, y + 2, color);
    }

    private static void leaf(GuiGraphics g, int x, int y, int color, boolean right) {
        if (right) {
            g.fill(x, y, x + 4, y + 2, color);
            g.fill(x + 1, y - 1, x + 3, y + 3, color);
        } else {
            g.fill(x - 3, y, x + 1, y + 2, color);
            g.fill(x - 2, y - 1, x, y + 3, color);
        }
    }

    private static void gear(GuiGraphics g, int cx, int cy, int radius, int color) {
        g.fill(cx - radius, cy - 1, cx + radius + 1, cy + 2, color);
        g.fill(cx - 1, cy - radius, cx + 2, cy + radius + 1, color);
        g.fill(cx - radius + 2, cy - radius + 2, cx + radius - 1, cy + radius - 1, alpha(color, Math.max(18, (color >>> 24) / 2)));
        g.fill(cx - 2, cy - 2, cx + 3, cy + 3, alpha(0xFF000000, 130));
        g.fill(cx - 1, cy - 1, cx + 2, cy + 2, alpha(color, Math.min(170, (color >>> 24) + 35)));
    }

    private static void orbit(GuiGraphics g, int cx, int cy, int rx, int ry, int colorA, int colorB, float turn) {
        if (rx <= 1 || ry <= 1) {
            return;
        }
        for (int i = 0; i < 20; ++i) {
            double angle = i * Math.PI * 2.0 / 20.0;
            int x = cx + (int)Math.round(Math.cos(angle) * rx);
            int y = cy + (int)Math.round(Math.sin(angle) * ry);
            g.fill(x, y, x + 1, y + 1, i < 10 ? colorA : colorB);
        }
        double moving = turn * Math.PI * 2.0;
        int px = cx + (int)Math.round(Math.cos(moving) * rx);
        int py = cy + (int)Math.round(Math.sin(moving) * ry);
        star(g, px, py, 1, alpha(0xFFFFFFFF, motionAlpha(50, 130)));
    }

    private static void crystal(GuiGraphics g, int cx, int cy, int radius, int color) {
        g.fill(cx, cy - radius, cx + 1, cy + radius + 1, color);
        g.fill(cx - radius, cy, cx + radius + 1, cy + 1, color);
        diagonal(g, cx - radius + 2, cy - radius + 2, cx + radius - 1, cy + radius - 1, alpha(color, Math.max(18, (color >>> 24) * 3 / 4)));
        diagonal(g, cx - radius + 2, cy + radius - 2, cx + radius - 1, cy - radius + 1, alpha(color, Math.max(18, (color >>> 24) * 3 / 4)));
    }

    private static void corner(GuiGraphics g, int x, int y, int size, int color, boolean right, boolean down) {
        int sx = right ? 1 : -1;
        int sy = down ? 1 : -1;
        int x2 = x + sx * size;
        int y2 = y + sy * size;
        g.fill(Math.min(x, x2), y, Math.max(x, x2) + 1, y + 1, color);
        g.fill(x, Math.min(y, y2), x + 1, Math.max(y, y2) + 1, color);
    }

    private static void diagonal(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int steps = Math.max(dx, dy);
        if (steps <= 0) {
            g.fill(x1, y1, x1 + 1, y1 + 1, color);
            return;
        }
        int stride = steps > 80 ? 3 : (steps > 40 ? 2 : 1);
        for (int i = 0; i <= steps; i += stride) {
            int x = x1 + (x2 - x1) * i / steps;
            int y = y1 + (y2 - y1) * i / steps;
            g.fill(x, y, x + 1, y + 1, color);
        }
    }
}
