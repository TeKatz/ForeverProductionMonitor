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
                graphics.fill(x + 3, bottom, split, bottom + 1, alpha(a, 90));
                graphics.fill(split, bottom, x + width - 3, bottom + 1, alpha(b, 90));
                if (GuiMotion.ambientMotionEnabled()) {
                    int px = x + 3 + (int)((width - 7) * phase(2300L, 0.5f));
                    graphics.fill(px, bottom - 1, Math.min(x + width - 3, px + 2), bottom + 1, alpha(palette.text(), 125));
                }
            }
            case AE2 -> {
                int cy = y + height / 2;
                int leftNode = x + 7;
                int rightNode = x + width - 8;
                graphics.fill(leftNode + 2, cy, rightNode - 1, cy + 1, alpha(palette.border(), 76));
                diamond(graphics, leftNode, cy, 2, subtle);
                diamond(graphics, rightNode, cy, 2, bright);
                int cellCount = Math.max(2, Math.min(5, (width - 24) / 16));
                for (int i = 0; i < cellCount; ++i) {
                    int cellX = x + 13 + i * Math.max(12, (width - 28) / cellCount);
                    meCell(graphics, cellX, y + 3, 8, height - 6, i % 2 == 0 ? palette.accentA() : palette.accentB(), palette.border(), 54);
                }
                int pulseX = leftNode + 3 + (int)((Math.max(1, rightNode - leftNode - 6)) * phase(1900L, 0.35f));
                diamond(graphics, pulseX, cy, 1, alpha(palette.text(), motionAlpha(58, 128)));
            }
            case ORITECH -> {
                for (int px = x + 3; px < x + width - 13; px += 12) {
                    graphics.fill(px, y + 2, Math.min(px + 8, x + width - 14), y + 3, (px / 12 & 1) == 0 ? subtle : alpha(b, 65));
                    graphics.fill(px + 2, bottom, Math.min(px + 7, x + width - 14), bottom + 1, alpha(palette.border(), 58));
                }
                rotor(graphics, x + width - 8, y + height / 2, 4, subtle, bright, phase(2400L, 0.18f));
                rivet(graphics, x + 4, y + height / 2 - 1, alpha(palette.border(), 82));
            }
            case MEKANISM -> {
                int cy = y + height / 2;
                graphics.fill(x + 3, y + 3, x + 4, y + height - 3, alpha(a, 76));
                graphics.fill(x + width - 4, y + 3, x + width - 3, y + height - 3, alpha(b, 76));
                graphics.fill(x + 4, cy, x + width - 4, cy + 1, alpha(palette.border(), 60));
                energyCore(graphics, x + width / 2, cy, 4, palette, phase(2700L, 0.22f), 76);
                int leftPulseY = y + 3 + (int)((height - 7) * phase(1500L, 0.28f));
                int rightPulseY = y + 3 + (int)((height - 7) * phase(1900L, 0.74f));
                graphics.fill(x + 2, leftPulseY, x + 5, Math.min(y + height - 3, leftPulseY + 2), alpha(palette.accentB(), motionAlpha(46, 118)));
                graphics.fill(x + width - 5, rightPulseY, x + width - 2, Math.min(y + height - 3, rightPulseY + 2), alpha(palette.accentA(), motionAlpha(42, 104)));
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
                graphics.fill(x + 3, cy, x + width - 8, cy + 1, alpha(a, 72));
                graphics.fill(x + width - 9, cy - 2, x + width - 8, cy + 1, alpha(a, 72));
                node(graphics, x + width - 8, cy - 1, bright);
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
                    meCell(graphics, cellX, y + 3, cellW, Math.min(12, height - 6), i % 2 == 0 ? palette.accentA() : palette.accentB(), palette.border(), 50);
                    if (i + 1 < cells) {
                        graphics.fill(cellX + cellW, y + 8, x + 6 + (i + 1) * usable / cells, y + 9, alpha(palette.border(), 34));
                    }
                }
                int pulse = (int)(phase(2500L, 0.32f) * Math.max(1, usable - 10));
                diamond(graphics, x + 9 + pulse, y + 8, 1, alpha(palette.text(), motionAlpha(42, 100)));
            }
            case ORITECH -> {
                rivet(graphics, x + 4, y + 4, a);
                rivet(graphics, right - 6, bottom - 6, b);
                int seam = x + width * 2 / 3;
                graphics.fill(seam, y + 2, seam + 1, bottom - 2, alpha(palette.border(), 38));
                for (int px = x + 12; px < seam - 5; px += 18) {
                    graphics.fill(px, bottom - 4, Math.min(px + 10, seam - 4), bottom - 3, alpha(palette.border(), 42));
                }
                rotor(graphics, right - 12, y + Math.min(10, height / 2), 4, a, b, phase(3300L, 0.21f));
                piston(graphics, seam + 8, bottom - 5, Math.max(8, right - seam - 24), true, a, b, phase(2100L, 0.4f));
            }
            case MEKANISM -> {
                int cy = y + height / 2;
                segmentedRail(graphics, x + 3, y + 3, bottom - 3, palette.accentA(), palette.border());
                segmentedRail(graphics, right - 4, y + 3, bottom - 3, palette.accentB(), palette.border());
                graphics.fill(x + 5, cy, right - 5, cy + 1, alpha(palette.border(), 36));
                energyCore(graphics, x + Math.min(width - 14, Math.max(14, width / 5)), cy, 5, palette, phase(3000L, 0.32f), 68);
                int pulseX = x + 12 + (int)((Math.max(1, width - 26)) * phase(2300L, 0.57f));
                graphics.fill(pulseX, cy - 2, Math.min(right - 5, pulseX + 5), cy + 3, alpha(palette.accentB(), motionAlpha(28, 74)));
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
                graphics.fill(x + 3, cy, right - 10, cy + 1, a);
                graphics.fill(right - 11, cy - 4, right - 10, cy + 1, a);
                node(graphics, right - 10, cy - 4, alpha(palette.accentB(), 70));
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
        int split = l + width * 3 / 5;

        // Signature segmented rails: geometric rather than free-floating particles.
        for (int x = l + 7; x < r - 7; x += 24) {
            int color = x < split ? p.accentA() : p.accentB();
            g.fill(x, t + 7, Math.min(x + 12, r - 6), t + 8, alpha(color, 34));
            g.fill(x + 5, b - 8, Math.min(x + 17, r - 6), b - 7, alpha(color, 30));
        }
        g.fill(l + 6, t + 8, l + 7, b - 8, alpha(p.accentA(), 25));
        g.fill(r - 7, t + 8, r - 6, b - 8, alpha(p.accentB(), 25));

        // Two long, slow data sweeps with opposite directions.
        if (GuiMotion.ambientMotionEnabled()) {
            float a = phase(9800L, 0.22f);
            float c = 1.0f - phase(12700L, 0.64f);
            int x1 = l + 8 + (int)((width - 17) * a);
            int x2 = l + 8 + (int)((width - 17) * c);
            g.fill(x1, t + 5, Math.min(x1 + 18, r - 7), t + 7, alpha(x1 < split ? p.accentA() : p.accentB(), motionAlpha(24, 64)));
            g.fill(x2, b - 7, Math.min(x2 + 13, r - 7), b - 5, alpha(x2 < split ? p.accentA() : p.accentB(), motionAlpha(20, 54)));
        }

        // Fixed geometric anchor marks.
        corner(g, l + 6, t + 6, 8, alpha(p.accentA(), 58), true, true);
        corner(g, r - 7, b - 7, 8, alpha(p.accentB(), 58), false, false);
        diamond(g, split, t + height / 2, 2, alpha(p.accentB(), 62));
    }

    private static void ae2Panel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;
        int networkRight = l + width * 3 / 5;
        int cols = 4;
        int rows = 4;
        int dx = Math.max(22, (networkRight - l - 18) / Math.max(1, cols - 1));
        int dy = Math.max(20, (height - 18) / Math.max(1, rows - 1));

        // Explicit ME-like network topology. Motion is constrained to actual paths.
        for (int row = 0; row < rows; ++row) {
            int y = t + 9 + row * dy;
            for (int col = 0; col < cols; ++col) {
                int x = l + 9 + col * dx;
                if (x >= networkRight - 3 || y >= b - 3) continue;
                if (col + 1 < cols && x + dx < networkRight) {
                    g.fill(x + 3, y, x + dx, y + 1, alpha(p.border(), 26));
                }
                if (row + 1 < rows && y + dy < b) {
                    g.fill(x, y + 3, x + 1, y + dy, alpha(p.border(), 22));
                }
                diamond(g, x, y, 2, alpha((row + col & 1) == 0 ? p.accentA() : p.accentB(), 58));
            }
        }

        // A packet changes node-to-node, rather than drifting freely across the screen.
        int routeLength = cols * rows;
        float routePhase = phase(7200L, 0.28f);
        int pathIndex = Math.min(routeLength - 1, (int)(routePhase * routeLength));
        int pathCol = pathIndex % cols;
        int pathRow = pathIndex / cols;
        int pulseX = l + 9 + pathCol * dx;
        int pulseY = t + 9 + pathRow * dy;
        if (pulseX < networkRight - 2 && pulseY < b - 2) {
            diamond(g, pulseX, pulseY, 3, alpha(p.text(), motionAlpha(52, 126)));
        }

        // Storage-cell bay gives AE2 a recognizable storage-system silhouette.
        int bayLeft = networkRight + 8;
        g.fill(bayLeft - 5, t + 5, bayLeft - 4, b - 5, alpha(p.accentA(), 32));
        int cellW = Math.max(14, (r - bayLeft - 12) / 2);
        int cellH = Math.max(14, Math.min(24, (height - 24) / 3));
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 2; ++col) {
                int cellX = bayLeft + col * (cellW + 4);
                int cellY = t + 7 + row * (cellH + 4);
                if (cellX + cellW >= r - 3 || cellY + cellH >= b - 3) continue;
                int glow = (row + col & 1) == 0 ? p.accentA() : p.accentB();
                meCell(g, cellX, cellY, cellW, cellH, glow, p.border(), 54);
                int meter = 3 + mod(hash(211 + row * 17 + col * 31), Math.max(4, cellH - 7));
                g.fill(cellX + cellW - 3, cellY + cellH - meter - 2, cellX + cellW - 2, cellY + cellH - 2, alpha(glow, 72));
            }
        }
    }

    private static void oritechPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;
        int seamA = l + width / 3;
        int seamB = l + width * 2 / 3;

        // Heavy metal plate construction.
        g.fill(seamA, t + 4, seamA + 1, b - 4, alpha(p.border(), 44));
        g.fill(seamB, t + 4, seamB + 1, b - 4, alpha(p.border(), 44));
        g.fill(l + 4, t + height / 2, r - 4, t + height / 2 + 1, alpha(p.border(), 36));
        rivet(g, l + 6, t + 6, alpha(p.accentB(), 70));
        rivet(g, r - 8, t + 6, alpha(p.accentA(), 64));
        rivet(g, l + 6, b - 8, alpha(p.accentA(), 64));
        rivet(g, r - 8, b - 8, alpha(p.accentB(), 70));

        // Actual mechanical movement only: rotors, shafts and actuator.
        float turnA = phase(8400L, 0.14f);
        float turnB = 1.0f - phase(11200L, 0.67f);
        rotor(g, l + width / 4, t + height / 3, 9, alpha(p.accentA(), 64), alpha(p.accentB(), 76), turnA);
        rotor(g, l + width / 2, t + height * 2 / 3, 7, alpha(p.accentB(), 60), alpha(p.accentA(), 72), turnB);

        int shaftY = t + height / 3;
        g.fill(l + width / 4 + 10, shaftY - 1, seamB - 4, shaftY + 2, alpha(p.border(), 54));
        for (int x = l + width / 4 + 16; x < seamB - 6; x += 13) {
            g.fill(x, shaftY - 2, x + 2, shaftY + 3, alpha(p.accentA(), 34));
        }

        int pistonY = t + height / 2 + 11;
        piston(g, seamB + 7, pistonY, Math.max(12, r - seamB - 18), true,
               alpha(p.border(), 72), alpha(p.accentB(), 90), phase(6200L, 0.42f));

        // Fixed machine rail, no matrix-style particles.
        for (int x = l + 10; x < r - 10; x += 18) {
            g.fill(x, b - 6, Math.min(x + 11, r - 8), b - 4,
                    alpha((x / 18 & 1) == 0 ? p.accentA() : p.border(), 42));
        }
    }

    private static void mekanismPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;
        int leftRail = l + 11;
        int rightRail = r - 12;
        int cx = l + width / 2;
        int cy = t + height / 2;

        // Enclosed energy conduits and machine rails.
        segmentedRail(g, leftRail, t + 6, b - 6, p.accentA(), p.border());
        segmentedRail(g, rightRail, t + 6, b - 6, p.accentB(), p.border());
        g.fill(leftRail + 3, cy, cx - 11, cy + 1, alpha(p.accentA(), 46));
        g.fill(cx + 11, cy, rightRail - 2, cy + 1, alpha(p.accentB(), 46));
        g.fill(cx, t + 8, cx + 1, cy - 10, alpha(p.border(), 32));
        g.fill(cx, cy + 10, cx + 1, b - 8, alpha(p.border(), 32));

        // Central reactor-like core; its animation is a contained power cycle.
        energyCore(g, cx, cy, 10, p, phase(12800L, 0.24f), 94);

        // Power packets remain inside conduits and are deliberately slow.
        int leftPulse = leftRail + 4 + (int)((Math.max(1, cx - leftRail - 17)) * phase(6800L, 0.18f));
        int rightPulse = cx + 11 + (int)((Math.max(1, rightRail - cx - 17)) * phase(8200L, 0.61f));
        g.fill(leftPulse, cy - 2, Math.min(leftPulse + 7, cx - 8), cy + 3, alpha(p.accentB(), motionAlpha(34, 96)));
        g.fill(rightPulse, cy - 2, Math.min(rightPulse + 7, rightRail - 2), cy + 3, alpha(p.accentA(), motionAlpha(32, 88)));

        // Tank / process gauges: fixed machinery with gently changing fill levels.
        int gaugeTop = t + 10;
        int gaugeBottom = Math.min(b - 10, gaugeTop + 30);
        int[] gaugeXs = {l + width / 4, l + width * 3 / 4};
        for (int i = 0; i < gaugeXs.length; ++i) {
            int gx = gaugeXs[i];
            int gc = i == 0 ? p.accentA() : p.accentB();
            g.fill(gx - 5, gaugeTop, gx + 6, gaugeBottom, alpha(p.border(), 54));
            g.fill(gx - 4, gaugeTop + 1, gx + 5, gaugeBottom - 1, alpha(p.tableOuter(), 80));
            float gaugePhase = phase(17000L + i * 4200L, 0.34f + i * 0.18f);
            int fill = 4 + (int)((gaugeBottom - gaugeTop - 7) * (0.35f + 0.45f * (0.5f + 0.5f * (float)Math.sin(gaugePhase * Math.PI * 2.0))));
            g.fill(gx - 3, gaugeBottom - 2 - fill, gx + 4, gaugeBottom - 2, alpha(gc, 58));
            g.fill(gx - 2, gaugeTop + 4, gx + 3, gaugeTop + 5, alpha(p.text(), 24));
        }
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

        // Physical copper plate seams and rivets.
        g.fill(l + width / 3, t + 4, l + width / 3 + 1, b - 4, alpha(p.border(), 38));
        g.fill(l + width * 2 / 3, t + 4, l + width * 2 / 3 + 1, b - 4, alpha(p.border(), 38));
        g.fill(l + 4, t + height / 2, r - 4, t + height / 2 + 1, alpha(p.border(), 30));
        rivet(g, l + 6, t + 6, alpha(p.accentA(), 76));
        rivet(g, r - 8, t + 6, alpha(p.accentA(), 64));
        rivet(g, l + 6, b - 8, alpha(p.accentB(), 60));
        rivet(g, r - 8, b - 8, alpha(p.accentB(), 72));

        // Patina patches are static material detail.
        int patina = alpha(p.accentB(), 26);
        for (int i = 0; i < 7; ++i) {
            int px = l + 12 + mod(hash(4021 + i * 43), Math.max(1, width - 24));
            int py = t + 10 + mod(hash(5099 + i * 37), Math.max(1, height - 20));
            g.fill(px, py, px + 4 + i % 3, py + 2, patina);
            if ((i & 1) == 0) g.fill(px + 2, py - 2, px + 4, py + 4, alpha(p.accentB(), 18));
        }

        // Only a broad, slow material reflection moves.
        int sheen = l - 36 + (int)((width + 72) * phase(24000L, 0.31f));
        diagonal(g, sheen, t + 5, sheen + 36, b - 5, alpha(p.text(), motionAlpha(4, 18)));
        diagonal(g, sheen + 4, t + 5, sheen + 40, b - 5, alpha(p.accentA(), motionAlpha(5, 20)));
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
        int x1 = l + width / 4;
        int x2 = l + width / 2;
        int x3 = l + width * 3 / 4;
        int y1 = t + height / 3;
        int y2 = t + height * 2 / 3;

        // Fixed circuit board routes.
        g.fill(l + 6, y1, x1, y1 + 1, alpha(p.accentA(), 38));
        g.fill(x1, y1, x1 + 1, y2, alpha(p.accentA(), 38));
        g.fill(x1, y2, x3, y2 + 1, alpha(p.accentA(), 38));
        g.fill(x2, t + 7, x2 + 1, y1, alpha(p.accentB(), 32));
        g.fill(x2, y1, r - 7, y1 + 1, alpha(p.accentB(), 32));
        g.fill(x3, y2, x3 + 1, b - 8, alpha(p.accentA(), 30));

        node(g, x1 - 1, y1 - 1, alpha(p.accentB(), 78));
        node(g, x1 - 1, y2 - 1, alpha(p.accentA(), 74));
        node(g, x2 - 1, y1 - 1, alpha(p.accentB(), 68));
        node(g, x3 - 1, y2 - 1, alpha(p.accentA(), 68));

        // Pulses travel only along defined traces.
        float p1 = phase(6400L, 0.18f);
        int route1 = (x1 - (l + 6)) + (y2 - y1) + (x3 - x1);
        int d1 = (int)(p1 * Math.max(1, route1));
        if (d1 < x1 - (l + 6)) {
            int px = l + 6 + d1;
            g.fill(px, y1 - 2, px + 4, y1 + 3, alpha(p.accentB(), motionAlpha(38, 98)));
        } else if (d1 < x1 - (l + 6) + y2 - y1) {
            int py = y1 + d1 - (x1 - (l + 6));
            g.fill(x1 - 2, py, x1 + 3, py + 4, alpha(p.accentB(), motionAlpha(38, 98)));
        } else {
            int px = x1 + d1 - (x1 - (l + 6)) - (y2 - y1);
            g.fill(px, y2 - 2, Math.min(px + 4, x3 + 1), y2 + 3, alpha(p.accentB(), motionAlpha(38, 98)));
        }

        // Lamp-like endpoints blink only when a trace completes.
        int lampAlpha = phase(6400L, 0.18f) > 0.9f ? motionAlpha(52, 122) : 34;
        g.fill(x3 - 3, b - 12, x3 + 4, b - 7, alpha(p.accentB(), lampAlpha));
        g.fill(x3 - 1, b - 11, x3 + 2, b - 8, alpha(p.text(), Math.max(18, lampAlpha - 28)));
    }

    private static void frostPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;
        float grow = growthCycle(phase(34000L, 0.74f));
        int reachX = Math.max(6, (int)(width * 0.42f * grow));
        int reachY = Math.max(6, (int)(height * 0.56f * grow));

        // Ice veins visibly grow from opposite corners.
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

        // Sparse static frost specks, not a continuous falling particle loop.
        for (int i = 0; i < 8; ++i) {
            int px = l + 8 + mod(hash(8011 + i * 73), Math.max(1, width - 16));
            int py = t + 8 + mod(hash(9109 + i * 53), Math.max(1, height - 16));
            star(g, px, py, 1, alpha(i % 3 == 0 ? p.accentB() : p.text(), 22 + (i % 3) * 5));
        }
    }

    private static void naturePanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;
        float cycle = phase(46000L, 0.76f);
        float growth = growthCycle(cycle);
        float swayPhase = phase(17000L, 0.31f);
        int sway = GuiMotion.ambientMotionEnabled() ? (int)Math.round(Math.sin(swayPhase * Math.PI * 2.0) * 2.0) : 0;

        // Main vine grows down the left edge over tens of seconds.
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

        // Hanging vine grows from the opposite top corner on a different cycle.
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

        // Ground-cover spreads horizontally after the main vine is established.
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

        // One larger bloom slowly opens and closes; no free-floating light particles.
        float bloomCycle = phase(28000L, 0.36f);
        float bloomSize = 0.45f + 0.55f * (0.5f + 0.5f * (float)Math.sin(bloomCycle * Math.PI * 2.0));
        int bx = l + width * 2 / 3;
        int by = t + height / 2 + sway;
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
