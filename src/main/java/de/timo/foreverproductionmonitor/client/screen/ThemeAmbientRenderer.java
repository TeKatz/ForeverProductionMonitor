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
                float p = phase(2600L, 0.2f);
                int cx = x + width / 2;
                int cy = y + height / 2;
                int dx = (int)Math.round(Math.cos(p * Math.PI * 2.0) * Math.min(8, width / 6));
                graphics.fill(cx + dx, cy - 1, cx + dx + 2, cy + 1, subtle);
                graphics.fill(cx - dx - 1, cy, cx - dx + 1, cy + 2, bright);
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
                int px = x + 7 + (int)((width - 15) * phase(3100L, 0.3f));
                graphics.fill(px, bottom, Math.min(px + 8, x + width - 4), bottom + 1, alpha(palette.text(), 45));
            }
            case AURORA -> {
                int split = x + width / 2;
                graphics.fill(x + 3, bottom, split, bottom + 1, alpha(a, 88));
                graphics.fill(split, bottom, x + width - 3, bottom + 1, alpha(b, 88));
                int px = x + 4 + (int)((width - 9) * phase(2700L, 0.55f));
                graphics.fill(px, bottom - 1, Math.min(px + 3, x + width - 3), bottom + 1, alpha(palette.text(), 65));
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
                orbit(graphics, cx, cy, Math.min(18, width / 7), Math.min(7, height / 4), a, b, phase(4200L, 0.18f));
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
        for (int x = l + 28; x < r; x += 52) {
            g.fill(x, t, x + 1, b, alpha(p.border(), 13));
        }
        for (int y = t + 24; y < b; y += 34) {
            g.fill(l, y, r, y + 1, alpha(p.border(), 11));
        }
        int sweep = l + (int)((r - l - 1) * phase(5200L, 0.32f));
        g.fill(sweep, t + 3, sweep + 1, b - 3, alpha(p.accentA(), motionAlpha(12, 26)));
        for (int i = 0; i < 4; ++i) {
            node(g, l + 5, t + 10 + i * 24, alpha(i % 2 == 0 ? p.accentA() : p.accentB(), 34));
        }
    }

    private static void foreverPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int mid = l + (r - l) * 3 / 5;
        for (int x = l + 6; x < r - 6; x += 22) {
            int color = x < mid ? p.accentA() : p.accentB();
            g.fill(x, b - 4, Math.min(x + 10, r - 4), b - 3, alpha(color, 40));
        }
        g.fill(l + 5, t + 5, l + 6, b - 7, alpha(p.accentA(), 24));
        g.fill(r - 6, t + 5, r - 5, b - 7, alpha(p.accentB(), 24));
        int px = l + 8 + (int)((r - l - 17) * phase(3600L, 0.44f));
        g.fill(px, b - 6, Math.min(px + 8, r - 6), b - 4, alpha(px < mid ? p.accentA() : p.accentB(), motionAlpha(32, 80)));
        node(g, mid - 1, t + 9, alpha(p.accentB(), 55));
    }

    private static void ae2Panel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;
        int networkRight = l + width * 3 / 5;
        int cols = 4;
        int rows = 4;
        int dx = Math.max(22, (networkRight - l - 18) / Math.max(1, cols - 1));
        int dy = Math.max(20, (height - 18) / Math.max(1, rows - 1));

        for (int row = 0; row < rows; ++row) {
            int y = t + 9 + row * dy;
            for (int col = 0; col < cols; ++col) {
                int x = l + 9 + col * dx;
                if (x >= networkRight - 3 || y >= b - 3) continue;
                if (col + 1 < cols && x + dx < networkRight) {
                    g.fill(x + 3, y, x + dx, y + 1, alpha(p.border(), 24));
                }
                if (row + 1 < rows && y + dy < b) {
                    g.fill(x, y + 3, x + 1, y + dy, alpha(p.border(), 21));
                }
                diamond(g, x, y, 2, alpha((row + col & 1) == 0 ? p.accentA() : p.accentB(), 56));
            }
        }

        int pathIndex = (int)(phase(3200L, 0.28f) * (cols * rows - 1));
        int pathCol = pathIndex % cols;
        int pathRow = pathIndex / cols;
        int pulseX = l + 9 + pathCol * dx;
        int pulseY = t + 9 + pathRow * dy;
        if (pulseX < networkRight - 2 && pulseY < b - 2) {
            diamond(g, pulseX, pulseY, 3, alpha(p.text(), motionAlpha(52, 128)));
        }

        int bayLeft = networkRight + 8;
        g.fill(bayLeft - 5, t + 5, bayLeft - 4, b - 5, alpha(p.accentA(), 30));
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

        // Heavy plate seams and bolt pattern.
        int seamA = l + width / 3;
        int seamB = l + width * 2 / 3;
        g.fill(seamA, t + 4, seamA + 1, b - 4, alpha(p.border(), 42));
        g.fill(seamB, t + 4, seamB + 1, b - 4, alpha(p.border(), 42));
        g.fill(l + 4, t + height / 2, r - 4, t + height / 2 + 1, alpha(p.border(), 34));
        rivet(g, l + 6, t + 6, alpha(p.accentB(), 68));
        rivet(g, r - 8, t + 6, alpha(p.accentA(), 62));
        rivet(g, l + 6, b - 8, alpha(p.accentA(), 62));
        rivet(g, r - 8, b - 8, alpha(p.accentB(), 68));

        // Two counter-rotating machine rotors make the theme read as machinery,
        // not generic moving lights.
        float turnA = phase(4200L, 0.14f);
        float turnB = 1.0f - phase(5600L, 0.67f);
        rotor(g, l + width / 4, t + height / 3, 8, alpha(p.accentA(), 62), alpha(p.accentB(), 74), turnA);
        rotor(g, l + width / 2, t + height * 2 / 3, 6, alpha(p.accentB(), 58), alpha(p.accentA(), 70), turnB);

        // Linear actuator / piston assembly.
        int pistonY = t + height / 2 + 10;
        piston(g, seamB + 7, pistonY, Math.max(12, r - seamB - 18), true,
               alpha(p.border(), 70), alpha(p.accentB(), 88), phase(2700L, 0.42f));

        // Segmented lower machine rail.
        for (int x = l + 10; x < r - 10; x += 18) {
            g.fill(x, b - 6, Math.min(x + 11, r - 8), b - 4, alpha((x / 18 & 1) == 0 ? p.accentA() : p.border(), 40));
        }
    }

    private static void mekanismPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;
        int leftRail = l + 10;
        int rightRail = r - 11;
        int cy = t + height / 2;

        segmentedRail(g, leftRail, t + 5, b - 5, p.accentA(), p.border());
        segmentedRail(g, rightRail, t + 5, b - 5, p.accentB(), p.border());

        // Symmetric machine conduits leading into a central energy core.
        int coreX = l + width / 2;
        g.fill(leftRail + 3, cy, coreX - 10, cy + 1, alpha(p.accentA(), 42));
        g.fill(coreX + 10, cy, rightRail - 2, cy + 1, alpha(p.accentB(), 42));
        g.fill(coreX, t + 7, coreX + 1, cy - 9, alpha(p.border(), 30));
        g.fill(coreX, cy + 9, coreX + 1, b - 7, alpha(p.border(), 30));
        energyCore(g, coreX, cy, 9, p, phase(6400L, 0.24f), 92);

        // Energy packets stay constrained to conduits rather than floating freely.
        int leftPulse = leftRail + 4 + (int)((Math.max(1, coreX - leftRail - 16)) * phase(2200L, 0.18f));
        int rightPulse = coreX + 10 + (int)((Math.max(1, rightRail - coreX - 16)) * phase(2800L, 0.61f));
        g.fill(leftPulse, cy - 2, Math.min(leftPulse + 6, coreX - 7), cy + 3, alpha(p.accentB(), motionAlpha(36, 102)));
        g.fill(rightPulse, cy - 2, Math.min(rightPulse + 6, rightRail - 2), cy + 3, alpha(p.accentA(), motionAlpha(34, 92)));

        // Reactor / chemical-machine style status cells.
        int cellY = t + 9;
        for (int i = -2; i <= 2; ++i) {
            if (i == 0) continue;
            int cellX = coreX + i * 24 - 6;
            if (cellX < l + 16 || cellX + 12 > r - 16) continue;
            g.fill(cellX, cellY, cellX + 12, cellY + 7, alpha(p.tableHeader(), 100));
            g.fill(cellX + 1, cellY + 1, cellX + 11, cellY + 2, alpha(i < 0 ? p.accentA() : p.accentB(), 68));
            g.fill(cellX + 3, cellY + 4, cellX + 9, cellY + 5, alpha(p.text(), 26));
        }
    }

    private static void quantumPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int cx = l + (r - l) / 2;
        int cy = t + (b - t) / 2;
        int rx = Math.min(38, Math.max(15, (r - l) / 8));
        int ry = Math.min(16, Math.max(7, (b - t) / 9));
        orbit(g, cx, cy, rx, ry, alpha(p.accentA(), 56), alpha(p.accentB(), 62), phase(5400L, 0.12f));
        orbit(g, cx, cy, Math.max(8, rx * 2 / 3), Math.max(5, ry * 2), alpha(p.accentB(), 34), alpha(p.accentA(), 42), phase(4100L, 0.58f));
        node(g, cx - 1, cy - 1, alpha(p.text(), 78));
        for (int i = 0; i < 7; ++i) {
            int px = l + 6 + mod(hash(37 + i * 61), Math.max(1, r - l - 12));
            int baseY = t + 5 + mod(hash(91 + i * 79), Math.max(1, b - t - 10));
            int py = baseY + (GuiMotion.ambientMotionEnabled() ? (int)(Math.sin(phase(3700L + i * 113L, i / 7.0f) * Math.PI * 2.0) * 4.0) : 0);
            star(g, px, py, 1, alpha(i % 2 == 0 ? p.accentA() : p.accentB(), 38 + i * 3));
        }
    }

    private static void holographicPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        for (int y = t + 3; y < b - 2; y += 7) {
            g.fill(l + 2, y, r - 2, y + 1, alpha(p.accentA(), 15));
        }
        int scanY = t + (int)((b - t - 2) * phase(3000L, 0.42f));
        g.fill(l + 3, scanY, r - 3, Math.min(scanY + 2, b - 2), alpha(p.accentB(), motionAlpha(24, 62)));
        corner(g, l + 5, t + 5, 9, alpha(p.accentA(), 68), true, true);
        corner(g, r - 6, b - 6, 9, alpha(p.accentB(), 62), false, false);
        int glitchX = l + 10 + mod((int)(phase(1300L, 0.3f) * 997), Math.max(1, r - l - 30));
        g.fill(glitchX, t + 14, Math.min(glitchX + 18, r - 6), t + 15, alpha(p.text(), motionAlpha(12, 32)));
    }

    private static void monochromePanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        for (int x = l + 20; x < r; x += 40) g.fill(x, t, x + 1, b, alpha(p.text(), 10));
        for (int y = t + 18; y < b; y += 28) g.fill(l, y, r, y + 1, alpha(p.text(), 9));
        int scanY = t + (int)((b - t - 1) * phase(6200L, 0.46f));
        g.fill(l + 3, scanY, r - 3, scanY + 1, alpha(p.text(), motionAlpha(12, 35)));
        for (int i = 0; i < 8; ++i) {
            int h = 3 + mod(hash(120 + i * 13), 12);
            g.fill(r - 8 - i * 3, b - 5 - h, r - 7 - i * 3, b - 5, alpha(p.muted(), 25 + i * 2));
        }
    }

    private static void minimalPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int cy = t + (b - t) / 2;
        g.fill(l + (r - l) / 4, cy, r - (r - l) / 4, cy + 1, alpha(p.accentA(), 16));
        g.fill(l + 8, t + 8, l + 18, t + 9, alpha(p.accentA(), 34));
        g.fill(r - 18, b - 9, r - 8, b - 8, alpha(p.accentB(), 34));
        int px = l + (r - l) / 4 + (int)((r - l) / 2 * phase(4400L, 0.4f));
        g.fill(px, cy - 1, Math.min(px + 4, r - 8), cy + 2, alpha(p.text(), motionAlpha(18, 48)));
    }

    private static void carbonPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;
        int rowStep = Math.max(18, height / 8);
        int columnStep = Math.max(28, width / 9);
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
        int sheen = l - 24 + (int)((width + 48) * phase(6800L, 0.28f));
        diagonal(g, sheen, t + 5, sheen + Math.min(54, height), Math.min(b - 5, t + 59), alpha(p.accentA(), motionAlpha(8, 25)));
        rivet(g, l + 6, t + 6, alpha(p.accentA(), 50));
        rivet(g, r - 8, b - 8, alpha(p.accentB(), 42));
    }

    private static void terminalPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        for (int y = t + 4; y < b - 3; y += 8) {
            g.fill(l + 2, y, r - 2, y + 1, alpha(p.accentA(), 13));
        }
        int width = r - l;
        int height = b - t;
        for (int i = 0; i < 14; ++i) {
            int x = l + 6 + mod(hash(301 + i * 47), Math.max(1, width - 12));
            int base = t + 5 + mod(hash(707 + i * 31), Math.max(1, height - 16));
            int drift = GuiMotion.ambientMotionEnabled() ? (int)(phase(4300L + i * 89L, i / 14.0f) * 20.0f) : 0;
            int y = t + mod(base - t + drift, Math.max(1, height - 8));
            int len = 2 + mod(hash(900 + i * 17), 7);
            g.fill(x, y, x + 1, Math.min(b - 3, y + len), alpha(i % 3 == 0 ? p.accentB() : p.accentA(), 24 + (i % 4) * 7));
        }
        if (!GuiMotion.ambientMotionEnabled() || phase(850L, 0.2f) < 0.58f) {
            g.fill(l + 8, b - 9, l + 15, b - 7, alpha(p.accentB(), 82));
        }
    }

    private static void deepSpacePanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;
        float twinklePhase = phase(5100L, 0.23f);
        for (int i = 0; i < 28; ++i) {
            int sx = l + 3 + mod(hash(1103 + i * 97), Math.max(1, width - 6));
            int sy = t + 3 + mod(hash(2039 + i * 131), Math.max(1, height - 6));
            double twinkle = 0.5 + 0.5 * Math.sin((twinklePhase * Math.PI * 2.0) + i * 1.73);
            int starAlpha = 28 + (int)(twinkle * (GuiMotion.ambientMotionEnabled() ? 76 : 28));
            int color = i % 5 == 0 ? p.accentB() : (i % 3 == 0 ? p.accentA() : p.text());
            star(g, sx, sy, i % 9 == 0 ? 2 : 1, alpha(color, starAlpha));
        }

        int gx = l + width * 3 / 4;
        int gy = t + height / 3;
        float galaxyTurn = phase(16000L, 0.11f) * 0.45f;
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

        int bx = l + Math.max(20, width / 5);
        int by = t + height * 2 / 3;
        float blackHoleTurn = phase(12000L, 0.36f);
        for (int i = 0; i < 18; ++i) {
            double angle = i * Math.PI * 2.0 / 18.0 + blackHoleTurn * Math.PI * 2.0;
            int px = bx + (int)Math.round(Math.cos(angle) * 12.0);
            int py = by + (int)Math.round(Math.sin(angle) * 4.5);
            int color = i < 9 ? p.accentB() : p.accentA();
            g.fill(px, py, px + 2, py + 1, alpha(color, 56 + (i % 3) * 9));
        }
        g.fill(bx - 4, by - 4, bx + 5, by + 5, 0xD9000000);
        g.fill(bx - 6, by - 1, bx - 3, by + 1, alpha(p.accentB(), 42));
        g.fill(bx + 4, by - 1, bx + 7, by + 1, alpha(p.accentA(), 42));
    }

    private static void copperPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int w = r - l;
        int h = b - t;
        g.fill(l + w / 3, t + 4, l + w / 3 + 1, b - 4, alpha(p.border(), 35));
        g.fill(l + w * 2 / 3, t + 4, l + w * 2 / 3 + 1, b - 4, alpha(p.border(), 35));
        g.fill(l + 4, t + h / 2, r - 4, t + h / 2 + 1, alpha(p.border(), 28));
        rivet(g, l + 6, t + 6, alpha(p.accentA(), 75));
        rivet(g, r - 8, t + 6, alpha(p.accentA(), 62));
        rivet(g, l + 6, b - 8, alpha(p.accentB(), 58));
        rivet(g, r - 8, b - 8, alpha(p.accentB(), 70));
        for (int i = 0; i < 9; ++i) {
            int px = l + 8 + mod(hash(4021 + i * 43), Math.max(1, w - 16));
            int py = t + 8 + mod(hash(5099 + i * 37), Math.max(1, h - 16));
            star(g, px, py, 1, alpha(p.accentB(), 22 + (i % 3) * 7));
        }
        int sheen = l - 18 + (int)((w + 36) * phase(7200L, 0.31f));
        diagonal(g, sheen, t + 5, sheen + 30, b - 5, alpha(p.text(), motionAlpha(6, 24)));
    }

    private static void auroraPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;
        float phase = phase(11000L, 0.16f);
        for (int ribbon = 0; ribbon < 3; ++ribbon) {
            int baseY = t + height * (ribbon + 1) / 4;
            int color = ribbon == 1 ? p.accentB() : p.accentA();
            int alpha = 22 + ribbon * 7;
            for (int px = l + 3; px < r - 4; px += 5) {
                float u = (float)(px - l) / Math.max(1, width);
                double wave = Math.sin(u * Math.PI * (2.0 + ribbon * 0.45) + phase * Math.PI * 2.0 + ribbon * 1.7);
                int py = baseY + (int)Math.round(wave * (5 + ribbon * 2));
                g.fill(px, py, Math.min(px + 5, r - 3), py + 2, alpha(color, alpha));
            }
        }
        for (int i = 0; i < 8; ++i) {
            int px = l + 5 + mod(hash(6101 + i * 59), Math.max(1, width - 10));
            int pyBase = t + 5 + mod(hash(7103 + i * 41), Math.max(1, height - 10));
            int drift = GuiMotion.ambientMotionEnabled() ? (int)(Math.sin((phase + i * 0.13f) * Math.PI * 2.0) * 5.0) : 0;
            star(g, px, pyBase + drift, 1, alpha(i % 2 == 0 ? p.accentA() : p.accentB(), 38));
        }
        g.fill(l + 3, b - 4, l + width / 2, b - 3, alpha(p.accentA(), 38));
        g.fill(l + width / 2, b - 4, r - 3, b - 3, alpha(p.accentB(), 38));
    }

    private static void redstonePanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;
        int y1 = t + height / 3;
        int y2 = t + height * 2 / 3;
        int x1 = l + width / 3;
        int x2 = l + width * 2 / 3;
        g.fill(l + 5, y1, x1, y1 + 1, alpha(p.accentA(), 42));
        g.fill(x1, y1, x1 + 1, y2, alpha(p.accentA(), 42));
        g.fill(x1, y2, r - 6, y2 + 1, alpha(p.accentA(), 42));
        g.fill(x2, t + 6, x2 + 1, y1 + 1, alpha(p.accentB(), 32));
        g.fill(x2, y1, r - 7, y1 + 1, alpha(p.accentB(), 32));
        node(g, x1 - 1, y1 - 1, alpha(p.accentB(), 84));
        node(g, x1 - 1, y2 - 1, alpha(p.accentA(), 80));
        node(g, x2 - 1, y1 - 1, alpha(p.accentB(), 72));
        float p1 = phase(2600L, 0.2f);
        int px = l + 5 + (int)((x1 - l - 5) * p1);
        g.fill(px, y1 - 2, Math.min(px + 4, x1 + 1), y1 + 3, alpha(p.accentB(), motionAlpha(45, 110)));
        int px2 = x1 + (int)((r - 6 - x1) * phase(3400L, 0.65f));
        g.fill(px2, y2 - 2, Math.min(px2 + 4, r - 5), y2 + 3, alpha(p.accentA(), motionAlpha(38, 96)));
    }

    private static void frostPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        crystal(g, l + 9, t + 12, 8, alpha(p.accentB(), 62));
        crystal(g, r - 10, b - 13, 9, alpha(p.accentA(), 58));
        int width = r - l;
        int height = b - t;
        float drift = phase(7000L, 0.24f);
        for (int i = 0; i < 15; ++i) {
            int px = l + 4 + mod(hash(8011 + i * 73), Math.max(1, width - 8));
            int base = mod(hash(9109 + i * 53), Math.max(1, height - 6));
            int py = t + 3 + mod(base + (GuiMotion.ambientMotionEnabled() ? (int)(drift * height * (0.55f + (i % 4) * 0.12f)) : 0), Math.max(1, height - 6));
            star(g, px, py, i % 7 == 0 ? 2 : 1, alpha(i % 3 == 0 ? p.accentB() : p.text(), 28 + (i % 4) * 8));
        }
        int shimmer = l + 6 + (int)((width - 13) * phase(5900L, 0.41f));
        g.fill(shimmer, t + 4, Math.min(shimmer + 10, r - 4), t + 5, alpha(p.text(), motionAlpha(10, 34)));
    }

    private static void naturePanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;
        float slow = phase(7600L, 0.31f);
        int sway = GuiMotion.ambientMotionEnabled() ? (int)Math.round(Math.sin(slow * Math.PI * 2.0) * 2.0) : 0;

        // Main climbing vine on the left.
        int vineX = l + 10;
        g.fill(vineX, t + 5, vineX + 1, b - 5, alpha(p.accentA(), 54));
        int step = Math.max(15, (height - 24) / 6);
        for (int i = 0; i < 6; ++i) {
            int py = t + 12 + i * step;
            if (py >= b - 7) break;
            boolean right = (i & 1) == 0;
            int branch = right ? 11 : -7;
            int ex = vineX + branch + ((i & 1) == 0 ? sway : -sway);
            g.fill(Math.min(vineX, ex), py, Math.max(vineX, ex) + 1, py + 1, alpha(p.accentA(), 48));
            leaf(g, ex, py - 1, alpha(i % 3 == 0 ? p.accentB() : p.accentA(), 72), right);
            if (i == 1 || i == 4) {
                flower(g, ex + (right ? 3 : -2), py - 4, alpha(p.accentB(), 92), alpha(p.text(), 74));
            }
        }

        // A second hanging vine from the upper-right corner.
        int rightVine = r - 13;
        int hangingEnd = t + Math.min(height * 2 / 3, 90);
        g.fill(rightVine, t + 4, rightVine + 1, hangingEnd, alpha(p.accentA(), 42));
        for (int i = 0; i < 4; ++i) {
            int py = t + 11 + i * 17;
            if (py >= hangingEnd) break;
            boolean right = (i & 1) != 0;
            leaf(g, rightVine + (right ? 4 + sway : -3 - sway), py, alpha(p.accentA(), 58), right);
        }
        flower(g, rightVine, Math.min(hangingEnd + 2, b - 8), alpha(p.accentB(), 102), alpha(p.text(), 82));

        // Ground-cover vine along the lower frame with leaves and small flowers.
        int groundY = b - 7;
        g.fill(l + 8, groundY, r - 8, groundY + 1, alpha(p.accentA(), 44));
        for (int i = 0; i < 7; ++i) {
            int px = l + 18 + i * Math.max(18, (width - 36) / 7);
            if (px >= r - 12) break;
            boolean right = (i & 1) == 0;
            leaf(g, px + (right ? sway : -sway), groundY - 1 - (i % 2), alpha(p.accentA(), 54 + (i % 3) * 6), right);
            if (i == 2 || i == 5) {
                flower(g, px, groundY - 5, alpha(p.accentB(), 84), alpha(p.text(), 68));
            }
        }

        // Flowers breathe gently instead of generic particles moving around.
        int bloomAlpha = motionAlpha(54, 106);
        int bloomX = l + width * 2 / 3;
        int bloomY = t + height / 2 + sway;
        flower(g, bloomX, bloomY, alpha(p.accentB(), bloomAlpha), alpha(p.text(), Math.max(42, bloomAlpha - 18)));
        bud(g, bloomX + 16, bloomY + 7, alpha(p.accentB(), 66));
    }

    private static void customPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;
        for (int i = 1; i < 5; ++i) {
            int x = l + width * i / 5;
            g.fill(x, t + 5, x + 1, b - 5, alpha(i % 2 == 0 ? p.accentA() : p.accentB(), 14));
        }
        for (int i = 1; i < 4; ++i) {
            int y = t + height * i / 4;
            g.fill(l + 5, y, r - 5, y + 1, alpha(i % 2 == 0 ? p.accentB() : p.accentA(), 12));
        }
        int edge = l + 5 + (int)((width - 11) * phase(4600L, 0.48f));
        g.fill(edge, b - 5, Math.min(edge + 10, r - 5), b - 3, alpha(p.accentB(), motionAlpha(26, 64)));
        for (int i = 0; i < 6; ++i) {
            int px = l + 8 + mod(hash(12101 + i * 59), Math.max(1, width - 16));
            int py = t + 8 + mod(hash(13109 + i * 71), Math.max(1, height - 16));
            node(g, px, py, alpha(i % 2 == 0 ? p.accentA() : p.accentB(), 34));
        }
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
