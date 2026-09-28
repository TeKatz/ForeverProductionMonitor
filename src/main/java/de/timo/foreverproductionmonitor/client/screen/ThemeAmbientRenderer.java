package de.timo.foreverproductionmonitor.client.screen;

import com.mojang.math.Axis;
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
    private static final int AE2_CONTROLLER_FRAME_LIGHT = 0xFFC9CCD3;
    private static final int AE2_CONTROLLER_FRAME_MID = 0xFF7E838E;
    private static final int AE2_CONTROLLER_FRAME_DARK = 0xFF454A55;
    private static final int AE2_CONTROLLER_INSET = 0xFF17191E;

    // AdvancedAE Quantum Computer visual language: dark casing, metallic frame,
    // emissive purple internals and sparse yellow hazard markings.
    private static final int QUANTUM_CASING = 0xFF17151C;
    private static final int QUANTUM_CASING_LIGHT = 0xFF302D36;
    private static final int QUANTUM_FRAME = 0xFF9B9DA4;
    private static final int QUANTUM_PURPLE_DARK = 0xFF3A1B58;
    private static final int QUANTUM_PURPLE = 0xFF8C43D6;
    private static final int QUANTUM_PURPLE_BRIGHT = 0xFFD39DFF;
    private static final int QUANTUM_HAZARD = 0xFFD2A62B;

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
            case CAT -> catPanel(graphics, left, top, right, bottom, palette);
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
                int cy = y + height / 2;
                graphics.fill(x + 5, bottom, x + width - 5, bottom + 1, alpha(palette.border(), 48));
                int[] colors = {0xFFC9D3DD, 0xFF55D6C7, 0xFFFF8A3D, 0xFFB66BFF};
                for (int i = 0; i < 4; ++i) {
                    int nx = x + 9 + i * Math.max(8, (width - 24) / 4);
                    graphics.fill(nx, cy - 2, nx + 4, cy + 2, alpha(colors[i], 56));
                }
                int px = x + 6 + Math.round(phase(7600L, 0.28f) * Math.max(1, width - 15));
                graphics.fill(px, bottom - 1, Math.min(x + width - 5, px + 4), bottom + 2,
                        alpha(palette.accentA(), motionAlpha(28, 66)));
            }
            case AE2 -> {
                int cy = y + height / 2;
                // One compact ME Controller cell is enough for tabs/buttons.
                graphics.fill(x + 3, y + 4, x + 4, y + height - 4, alpha(AE2_CONTROLLER_FRAME_LIGHT, 28));
                int cellX = x + width - 15;
                graphics.fill(cellX, cy - 5, cellX + 10, cy + 5, alpha(AE2_CONTROLLER_FRAME_MID, 78));
                graphics.fill(cellX + 2, cy - 3, cellX + 8, cy + 3, alpha(AE2_CONTROLLER_INSET, 104));
                graphics.fill(cellX + 3, cy - 2, cellX + 7, cy + 2,
                        ae2ControllerColor(0.12f, active ? 82 : 50));
            }
            case ORITECH -> {
                // Compact machine module: solid orange rail + cyan status glass.
                graphics.fill(x + 3, y + 3, x + 5, y + height - 3, alpha(palette.accentA(), 82));
                graphics.fill(x + 5, y + 3, x + width - 15, y + 4, alpha(palette.border(), 58));
                int statusAlpha = active ? 128 : Math.round(54 + hoverProgress * 62.0f);
                graphics.fill(x + width - 13, y + 4, x + width - 5, y + height - 4, alpha(0xFF111A1D, 120));
                graphics.fill(x + width - 11, y + 6, x + width - 7, y + height - 6,
                        alpha(palette.accentB(), statusAlpha));
                if (hoverProgress > 0.08f) {
                    int travel = Math.max(4, width / 5);
                    piston(graphics, x + 9, y + height - 5, travel, true,
                            alpha(palette.border(), 54), alpha(palette.accentA(), 76),
                            0.14f + hoverProgress * 0.22f);
                }
            }
            case MEKANISM -> {
                int cy = y + height / 2;
                graphics.fill(x + 4, y + 4, x + 8, y + height - 4, alpha(palette.border(), 60));
                graphics.fill(x + 5, cy, x + 7, y + height - 5, alpha(palette.accentA(), 64));
                graphics.fill(x + width - 10, y + 4, x + width - 5, y + height - 4, alpha(palette.border(), 60));
                float energy = 0.48f + 0.34f * (0.5f + 0.5f * (float)Math.sin(phase(8400L, 0.25f) * Math.PI * 2.0));
                int gaugeH = Math.max(2, Math.round((height - 10) * energy));
                graphics.fill(x + width - 8, y + height - 5 - gaugeH, x + width - 7, y + height - 5,
                        alpha(palette.accentB(), 72));
                graphics.fill(x + 13, cy, x + width - 15, cy + 1, alpha(palette.border(), 34));
                int prog = x + 13 + Math.round(phase(7000L, 0.3f) * Math.max(1, width - 31));
                graphics.fill(prog, cy - 1, Math.min(x + width - 15, prog + 5), cy + 2,
                        alpha(palette.accentA(), motionAlpha(28, 62)));
            }
            case QUANTUM -> {
                int cy = y + height / 2;
                // Single compute cell instead of a row of repeated modules.
                int moduleX = x + width - 16;
                float pulse = 0.5f + 0.5f * (float)Math.sin(phase(8200L, 0.28f) * Math.PI * 2.0);
                graphics.fill(moduleX, cy - 5, moduleX + 11, cy + 5, alpha(QUANTUM_CASING, 150));
                graphics.fill(moduleX + 2, cy - 3, moduleX + 9, cy + 3, alpha(QUANTUM_PURPLE, 70));
                graphics.fill(moduleX + 4, cy - 1, moduleX + 7, cy + 2,
                        alpha(QUANTUM_PURPLE_BRIGHT, Math.round(46 + pulse * 48.0f)));
                graphics.fill(moduleX + 2, cy + 5, moduleX + 9, cy + 6, alpha(0xFFD2A62B, 34));
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
                rivet(graphics, x + 5, bottom - 1, subtle);
                rivet(graphics, x + width - 7, bottom - 1, subtle);
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
                float power = signalWindow(phase(6800L, 0.2f), 0.08f, 0.18f, 0.58f, 0.72f);
                int dust = alpha(lerpColor(palette.border(), palette.accentA(), power), Math.round(52 + power * 56));
                graphics.fill(x + 3, cy, x + width - 12, cy + 1, dust);
                redstoneRepeater(graphics, x + width / 2 - 4, cy - 4, power, palette);
                redstoneLamp(graphics, x + width - 9, cy - 3, power, palette);
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
            case CAT -> {
                // Motion stays on the button edge, where it cannot cover its label.
                if (hoverProgress > 0.01f && width > 22) {
                    int travel = Math.round((width - 12) * hoverProgress);
                    graphics.fill(x + 4, bottom, x + 4 + travel, bottom + 1,
                            alpha(palette.accentB(), 165));
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
                graphics.fill(x + 4, bottom - 4, right - 4, bottom - 3, alpha(palette.border(), 34));
                int[] colors = {0xFFC9D3DD, 0xFF55D6C7, 0xFFFF8A3D, 0xFFB66BFF};
                for (int i = 0; i < 4; ++i) {
                    int nx = x + 10 + i * Math.max(10, (width - 28) / 4);
                    graphics.fill(nx, bottom - 6, nx + 6, bottom - 2, alpha(colors[i], 42));
                }
            }
            case AE2 -> {
                graphics.fill(x + 4, y + 3, right - 4, y + 5, alpha(AE2_CONTROLLER_FRAME_MID, 42));
                int cells = Math.min(4, Math.max(2, width / 55));
                for (int i = 0; i < cells; ++i) {
                    int sx = x + 8 + i * 11;
                    graphics.fill(sx, y + 7, sx + 9, y + 15, alpha(AE2_CONTROLLER_INSET, 96));
                    graphics.fill(sx + 2, y + 9, sx + 7, y + 13, ae2ControllerColor(i * 0.13f, 46));
                }
                graphics.fill(x + 6, bottom - 3, right - 6, bottom - 2, alpha(AE2_CONTROLLER_FRAME_DARK, 44));
            }
            case ORITECH -> {
                // Tables stay readable: one structural rail and one powered machine module.
                graphics.fill(x + 2, y + 2, x + 4, bottom - 2, alpha(palette.accentA(), 58));
                graphics.fill(x + 4, y + 2, right - 16, y + 3, alpha(palette.border(), 38));
                rivet(graphics, x + 6, y + 5, alpha(palette.accentA(), 64));
                rivet(graphics, right - 7, bottom - 7, alpha(palette.border(), 62));
                int moduleX = right - 15;
                int moduleY = y + Math.max(4, height / 2 - 5);
                graphics.fill(moduleX, moduleY, right - 4, Math.min(bottom - 3, moduleY + 10),
                        alpha(0xFF111A1D, 110));
                graphics.fill(moduleX + 3, moduleY + 2, right - 7, Math.min(bottom - 5, moduleY + 8),
                        alpha(palette.accentB(), motionAlpha(42, 78)));
            }
            case MEKANISM -> {
                int cy = y + height / 2;
                graphics.fill(x + 3, y + 4, x + 7, bottom - 4, alpha(palette.border(), 52));
                graphics.fill(x + 4, cy, x + 6, bottom - 5, alpha(palette.accentA(), 48));
                graphics.fill(right - 9, y + 4, right - 4, bottom - 4, alpha(palette.border(), 52));
                int gaugeH = Math.max(2, (height - 10) * 3 / 5);
                graphics.fill(right - 7, bottom - 5 - gaugeH, right - 6, bottom - 5, alpha(palette.accentB(), 58));
                graphics.fill(x + 13, cy, right - 14, cy + 1, alpha(palette.border(), 28));
            }
            case QUANTUM -> {
                int moduleW = Math.max(13, Math.min(21, width / 8));
                int start = right - moduleW * 3 - 9;
                for (int i = 0; i < 3; ++i) {
                    int mx = start + i * (moduleW + 2);
                    if (mx < x + 4) continue;
                    quantumCasingCell(graphics, mx, y + 4, moduleW, Math.max(8, height - 8),
                            i == 1 ? 74 : 40, i == 1);
                }
                graphics.fill(start + 2, bottom - 3, right - 5, bottom - 2, alpha(0xFFD2A62B, 38));
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
                rivet(graphics, x + 5, y + 4, a);
                rivet(graphics, right - 7, y + 4, b);
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
                float power = signalWindow(phase(7600L, 0.2f), 0.08f, 0.18f, 0.54f, 0.70f);
                int dust = alpha(lerpColor(palette.border(), palette.accentA(), power), Math.round(40 + power * 48));
                graphics.fill(x + 3, cy, right - 12, cy + 1, dust);
                redstoneRepeater(graphics, x + width / 2 - 4, cy - 4, power, palette);
                redstoneLamp(graphics, right - 9, cy - 3, power, palette);
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
            case CAT -> {
                graphics.fill(x + 2, y + 2, x + 3, bottom - 2, alpha(palette.accentA(), 64));
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

        // Forever is the whole modpack rather than one machine: a shared production
        // backbone links AE-like storage, Mekanism chemistry, Oritech processing and
        // Quantum transport into one coherent megabase network.
        int busY = b - Math.max(24, height / 7);
        int busLeft = l + Math.max(18, width / 12);
        int busRight = r - Math.max(18, width / 12);
        g.fill(busLeft, busY, busRight, busY + 2, alpha(p.border(), 34));

        int[] systemColors = {0xFFC9D3DD, 0xFF55D6C7, 0xFFFF8A3D, 0xFFB66BFF};
        int[] moduleX = new int[4];
        for (int i = 0; i < 4; ++i) {
            moduleX[i] = busLeft + (i + 1) * (busRight - busLeft) / 5;
            int color = systemColors[i];
            g.fill(moduleX[i] - 8, busY - 7, moduleX[i] + 9, busY + 8, alpha(0xFF0B141D, 82));
            g.fill(moduleX[i] - 6, busY - 5, moduleX[i] + 7, busY + 6, alpha(p.border(), 36));
            g.fill(moduleX[i] - 3, busY - 2, moduleX[i] + 4, busY + 3, alpha(color, 54));
            g.fill(moduleX[i] - 1, busY - 10, moduleX[i] + 2, busY - 6, alpha(color, 42));
        }

        // Central crafting/network core.
        int coreX = l + width / 2;
        int coreY = busY - Math.max(24, Math.min(44, height / 6));
        g.fill(coreX - 16, coreY - 9, coreX + 17, coreY + 10, alpha(0xFF0A131C, 92));
        g.fill(coreX - 13, coreY - 6, coreX + 14, coreY + 7, alpha(p.border(), 42));
        g.fill(coreX - 8, coreY - 3, coreX + 9, coreY + 4, alpha(p.accentA(), 48));
        g.fill(coreX - 3, coreY - 5, coreX + 4, coreY + 6, alpha(p.accentB(), 58));
        g.fill(coreX, coreY + 9, coreX + 1, busY, alpha(p.border(), 34));

        float corePulse = 0.5f + 0.5f * (float)Math.sin(phase(9200L, 0.21f) * Math.PI * 2.0);
        g.fill(coreX - 1, coreY - 1, coreX + 2, coreY + 2,
                alpha(p.text(), Math.round(30 + corePulse * 54.0f)));

        // Pack-wide data packets travel along the same backbone instead of random
        // decorative ribbons.
        for (int i = 0; i < 4; ++i) {
            float packet = (phase(11000L + i * 1400L, 0.1f + i * 0.17f) + i * 0.19f) % 1.0f;
            int px = busLeft + Math.round(packet * Math.max(1, busRight - busLeft - 2));
            g.fill(px, busY - 1, px + 3, busY + 3,
                    alpha(systemColors[i], motionAlpha(22, 54)));
        }

        // A paired quantum link across the upper edge references the pack's many
        // cross-network bridges while remaining quiet behind actual UI content.
        if (width > 220 && height > 100) {
            int qy = t + Math.max(20, height / 6);
            int q1 = l + width / 4;
            int q2 = l + width * 3 / 4;
            g.fill(q1 - 6, qy - 6, q1 + 7, qy + 7, alpha(0xFF1B1722, 52));
            g.fill(q2 - 6, qy - 6, q2 + 7, qy + 7, alpha(0xFF1B1722, 52));
            diamond(g, q1, qy, 3, alpha(0xFFB66BFF, 48));
            diamond(g, q2, qy, 3, alpha(0xFF55D6FF, 48));
            g.fill(q1 + 7, qy, q2 - 6, qy + 1, alpha(p.border(), 18));
            float bridge = phase(7600L, 0.37f);
            int bx = q1 + 8 + Math.round(bridge * Math.max(1, q2 - q1 - 16));
            g.fill(bx, qy - 1, bx + 4, qy + 2, alpha(0xFFB66BFF, motionAlpha(28, 64)));
        }
    }

    private static void ae2Panel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;

        // Controller casing first: a dark terminal inset inside cool metal rails.
        g.fill(l + 7, t + 8, r - 7, t + 10, alpha(AE2_CONTROLLER_FRAME_LIGHT, 34));
        g.fill(l + 7, b - 10, r - 7, b - 8, alpha(AE2_CONTROLLER_FRAME_MID, 30));
        g.fill(l + 7, t + 10, l + 9, b - 10, alpha(AE2_CONTROLLER_FRAME_LIGHT, 30));
        g.fill(r - 9, t + 10, r - 7, b - 10, alpha(AE2_CONTROLLER_FRAME_DARK, 30));

        // A real Controller-like cell bank is the visual anchor. Colour changes live
        // inside the cells, surrounded by neutral graphite just like the original block.
        int cellSize = Math.max(13, Math.min(22, Math.min(width / 16, height / 9)));
        int bankX = l + 18;
        int bankY = b - (cellSize * 2 + 18);
        float sweep = phase(19000L, 0.12f) * 6.0f;
        int active = Math.floorMod((int)Math.floor(sweep), 6);
        float blend = smoothstep(sweep - (float)Math.floor(sweep));
        for (int row = 0; row < 2; ++row) {
            for (int col = 0; col < 3; ++col) {
                int idx = row * 3 + col;
                int cx = bankX + col * (cellSize + 3);
                int cy = bankY + row * (cellSize + 3);
                g.fill(cx, cy, cx + cellSize, cy + cellSize, alpha(AE2_CONTROLLER_FRAME_MID, 68));
                g.fill(cx + 2, cy + 2, cx + cellSize - 2, cy + cellSize - 2, alpha(AE2_CONTROLLER_INSET, 98));
                float glow = idx == active ? 1.0f - blend : (idx == (active + 1) % 6 ? blend : 0.0f);
                int color = ae2ControllerColor(idx * 0.11f, 34 + Math.round(glow * 70.0f));
                g.fill(cx + 4, cy + 4, cx + cellSize - 4, cy + cellSize - 4, color);
            }
        }

        // ME Drive / terminal module at the upper-right: fixed slots, tiny status LEDs.
        if (width > 220 && height > 115) {
            int driveW = Math.max(56, Math.min(78, width / 5));
            int driveH = 42;
            int driveRight = r - 18;
            int driveLeft = driveRight - driveW;
            int driveTop = t + 18;
            g.fill(driveLeft, driveTop, driveRight, driveTop + driveH, alpha(AE2_CONTROLLER_FRAME_MID, 62));
            g.fill(driveLeft + 2, driveTop + 2, driveRight - 2, driveTop + driveH - 2, alpha(AE2_CONTROLLER_INSET, 104));
            for (int row = 0; row < 4; ++row) {
                int sy = driveTop + 5 + row * 8;
                g.fill(driveLeft + 6, sy, driveRight - 8, sy + 5, alpha(0xFF2B2E34, 94));
                int led = ae2CellColor(row + 2);
                g.fill(driveRight - 12, sy + 1, driveRight - 9, sy + 4,
                        alpha(led, 42 + row * 6));
            }
        }

        // One restrained orthogonal network trace links the controller bank to the
        // terminal module. No full-screen maze.
        int traceY = bankY - 8;
        int traceStart = bankX + cellSize;
        int traceEnd = r - Math.max(82, width / 5);
        if (traceEnd > traceStart + 18) {
            controllerTrace(g, traceStart, traceY, traceEnd - traceStart, -Math.max(8, height / 8),
                    ae2ControllerColor(0.08f, 34), false);
            int packetX = traceStart + Math.round(phase(10500L, 0.33f) * (traceEnd - traceStart));
            g.fill(packetX, traceY - 1, packetX + 3, traceY + 2, ae2ControllerColor(0.22f, motionAlpha(32, 66)));
        }
    }

    private static void oritechPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;
        int railTop = t + Math.max(18, height / 5);
        int railLeft = l + Math.max(18, width / 10);
        int railRight = r - Math.max(24, width / 7);

        // One coherent machine frame. Every animated module is physically attached
        // to this structure so moving parts never appear to float independently.
        g.fill(railLeft, railTop, railRight, railTop + 3, alpha(p.border(), 78));
        g.fill(railLeft, railTop - 3, railRight, railTop - 1, alpha(p.accentA(), 66));
        g.fill(railLeft, railTop - 2, railLeft + 3, b - 14, alpha(p.accentA(), 58));
        g.fill(railRight - 3, railTop - 2, railRight, b - 14, alpha(p.border(), 58));
        g.fill(railLeft - 5, b - 15, railRight + 5, b - 11, alpha(0xFF11171A, 94));
        g.fill(railLeft, b - 15, railRight, b - 13, alpha(p.accentA(), 40));

        rivet(g, railLeft + 4, railTop + 5, alpha(p.accentA(), 72));
        rivet(g, railRight - 7, railTop + 5, alpha(p.accentB(), 72));
        rivet(g, railLeft + 4, b - 20, alpha(p.border(), 66));
        rivet(g, railRight - 7, b - 20, alpha(p.border(), 66));

        // The entire machine runs from one master cycle:
        // move to station -> lower head -> process -> raise -> return.
        // This replaces the previous independent timers that could contradict each other.
        float cycle = phase(16000L, 0.58f);

        float carriageProgress;
        if (cycle < 0.22f) {
            carriageProgress = smoothstep(cycle / 0.22f);
        } else if (cycle < 0.78f) {
            carriageProgress = 1.0f;
        } else {
            carriageProgress = 1.0f - smoothstep((cycle - 0.78f) / 0.22f);
        }

        float pressProgress;
        if (cycle < 0.30f || cycle >= 0.78f) {
            pressProgress = 0.0f;
        } else if (cycle < 0.42f) {
            pressProgress = smoothstep((cycle - 0.30f) / 0.12f);
        } else if (cycle < 0.68f) {
            pressProgress = 1.0f;
        } else {
            pressProgress = 1.0f - smoothstep((cycle - 0.68f) / 0.10f);
        }

        int homeX = railLeft + 28;
        int workX = railRight - 42;
        int carriageX = homeX + Math.round((workX - homeX) * carriageProgress);
        int carriageY = railTop + 7;

        // A fixed work bed makes the processing station part of the machine instead
        // of a platform that floats around with the moving carriage.
        int workBedY = Math.min(b - 42, railTop + Math.min(88, Math.max(58, height / 6)));
        g.fill(workX - 20, workBedY, workX + 21, workBedY + 4, alpha(p.border(), 76));
        g.fill(workX - 15, workBedY - 3, workX + 16, workBedY, alpha(p.accentA(), 48));
        g.fill(workX + 20, workBedY + 1, railRight, workBedY + 3, alpha(p.border(), 58));
        g.fill(workX + 16, workBedY + 4, workX + 19, b - 14, alpha(p.border(), 38));

        // Gantry carriage stays seated on the upper rail.
        g.fill(carriageX - 9, carriageY - 5, carriageX + 10, carriageY + 8, alpha(0xFF151C20, 118));
        g.fill(carriageX - 7, carriageY - 3, carriageX + 8, carriageY + 5, alpha(p.border(), 78));
        g.fill(carriageX - 4, carriageY - 1, carriageX + 5, carriageY + 3,
                alpha(p.accentB(), motionAlpha(62, 105)));

        // The telescoping shaft now extends all the way to the moving head.
        int headRetractedY = carriageY + 18;
        int headExtendedY = Math.max(headRetractedY, workBedY - 10);
        int headY = headRetractedY + Math.round((headExtendedY - headRetractedY) * pressProgress);
        g.fill(carriageX - 2, carriageY + 7, carriageX + 3, headY + 1, alpha(p.border(), 70));
        g.fill(carriageX - 7, headY, carriageX + 8, headY + 7, alpha(0xFF10171A, 132));
        g.fill(carriageX - 4, headY + 2, carriageX + 5, headY + 5, alpha(p.accentA(), 82));
        g.fill(carriageX - 2, headY + 6, carriageX + 3, headY + 10,
                alpha(p.accentB(), motionAlpha(52, 94)));

        // Right-side emitter is mounted to the main frame rather than floating.
        if (width > 190 && height > 92) {
            int emitterX = r - Math.max(34, width / 9);
            int emitterY = workBedY - 9;

            g.fill(railRight, railTop, emitterX + 2, railTop + 2, alpha(p.border(), 46));
            g.fill(emitterX - 2, railTop + 1, emitterX + 2, emitterY - 10, alpha(p.border(), 52));
            g.fill(emitterX - 9, emitterY - 10, emitterX + 10, emitterY + 11, alpha(0xFF121A1E, 126));
            g.fill(emitterX - 6, emitterY - 7, emitterX + 7, emitterY + 8, alpha(p.border(), 82));
            g.fill(emitterX - 3, emitterY - 4, emitterX + 4, emitterY + 5,
                    alpha(p.accentB(), motionAlpha(58, 112)));

            // A permanent dark conduit makes the connection readable even when idle.
            int receiverX = workX + 20;
            int beamEndX = emitterX - 8;
            int conduitY = emitterY;
            if (beamEndX > receiverX + 2) {
                g.fill(receiverX, conduitY - 1, beamEndX, conduitY + 3, alpha(p.border(), 30));
                g.fill(receiverX, conduitY, beamEndX, conduitY + 2, alpha(p.accentB(), 18));

                // Beam timing is locked to the processing step. It grows from the
                // emitter to the receiver, holds while the head is down, then retracts.
                // No independent fade timer remains.
                float beamProgress = signalWindow(cycle, 0.36f, 0.44f, 0.64f, 0.72f);
                if (beamProgress > 0.01f) {
                    int fullLength = beamEndX - receiverX;
                    int activeLength = Math.max(1, Math.round(fullLength * beamProgress));
                    int beamStartX = beamEndX - activeLength;
                    g.fill(beamStartX, conduitY, beamEndX, conduitY + 2,
                            alpha(p.accentB(), motionAlpha(72, 118)));
                    if (activeLength > 8) {
                        g.fill(beamStartX + 3, conduitY - 1, beamEndX, conduitY,
                                alpha(p.text(), motionAlpha(16, 34)));
                    }
                }
            }

            // Receiver block sits directly on the fixed processing bed.
            g.fill(workX + 16, emitterY - 5, workX + 23, emitterY + 6, alpha(0xFF111A1D, 116));
            g.fill(workX + 18, emitterY - 2, workX + 21, emitterY + 3,
                    alpha(p.accentB(), motionAlpha(48, 86)));
        }

        // Power column is attached directly to the left frame instead of living
        // separately at the edge of the panel.
        int powerX = railLeft - 9;
        int powerTop = t + height / 2;
        int powerBottom = b - 20;
        if (powerBottom - powerTop > 18) {
            g.fill(powerX - 4, powerTop, powerX + 5, powerBottom, alpha(0xFF10171A, 100));
            g.fill(powerX - 2, powerTop + 2, powerX + 3, powerBottom - 2, alpha(p.border(), 62));
            g.fill(powerX + 5, powerTop + 3, railLeft + 1, powerTop + 5, alpha(p.border(), 48));
            float charge = 0.28f + 0.58f * (0.5f + 0.5f * (float)Math.sin(cycle * Math.PI * 2.0));
            tankFill(g, powerX - 1, powerTop + 3, powerX + 2, powerBottom - 3,
                    charge, alpha(p.accentB(), motionAlpha(40, 78)));
        }
    }

    private static void mekanismPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;

        // Mekanism machine GUI: one chemical tank, one energy gauge, clear I/O ports
        // and a processing path. Repetition and decorative rails are intentionally gone.
        int tankX = l + Math.max(18, width / 12);
        int tankTop = t + Math.max(18, height / 7);
        int tankBottom = b - Math.max(24, height / 8);
        int tankW = Math.max(10, Math.min(16, width / 24));
        g.fill(tankX, tankTop, tankX + tankW, tankBottom, alpha(p.border(), 68));
        g.fill(tankX + 2, tankTop + 2, tankX + tankW - 2, tankBottom - 2, alpha(0xFF0B1519, 106));
        float chem = 0.40f + 0.26f * (0.5f + 0.5f * (float)Math.sin(phase(22000L, 0.31f) * Math.PI * 2.0));
        tankFill(g, tankX + 3, tankTop + 3, tankX + tankW - 3, tankBottom - 3, chem, alpha(p.accentA(), 64));

        // Vertical power bar, visually separated from the chemical tank.
        int powerX = r - Math.max(24, width / 13);
        int powerW = 8;
        g.fill(powerX, tankTop, powerX + powerW, tankBottom, alpha(p.border(), 62));
        g.fill(powerX + 2, tankTop + 2, powerX + powerW - 2, tankBottom - 2, alpha(0xFF0A1417, 104));
        float power = 0.56f + 0.30f * (0.5f + 0.5f * (float)Math.sin(phase(17000L, 0.22f) * Math.PI * 2.0));
        tankFill(g, powerX + 3, tankTop + 3, powerX + powerW - 3, tankBottom - 3, power, alpha(p.accentB(), 70));

        // Input -> process -> output layout.
        int cy = t + height / 2;
        int inX = l + width / 3;
        int outX = l + width * 2 / 3;
        int slotSize = Math.max(15, Math.min(23, height / 8));
        g.fill(inX - slotSize / 2, cy - slotSize / 2, inX + slotSize / 2, cy + slotSize / 2, alpha(p.border(), 50));
        g.fill(outX - slotSize / 2, cy - slotSize / 2, outX + slotSize / 2, cy + slotSize / 2, alpha(p.border(), 50));
        g.fill(inX - 2, cy - 2, inX + 3, cy + 3, alpha(0xFF4E8BD8, 54));
        g.fill(outX - 2, cy - 2, outX + 3, cy + 3, alpha(0xFFD85858, 54));

        int arrowLeft = inX + slotSize / 2 + 6;
        int arrowRight = outX - slotSize / 2 - 6;
        if (arrowRight > arrowLeft + 8) {
            g.fill(arrowLeft, cy - 1, arrowRight - 4, cy + 2, alpha(p.border(), 38));
            g.fill(arrowRight - 6, cy - 4, arrowRight, cy + 5, alpha(p.border(), 38));
            float progress = phase(9800L, 0.27f);
            int progressX = arrowLeft + Math.round(progress * Math.max(1, arrowRight - arrowLeft - 5));
            g.fill(progressX, cy - 2, Math.min(arrowRight, progressX + 6), cy + 3,
                    alpha(p.accentA(), motionAlpha(32, 68)));
        }

        // Three tiny side-configuration tabs are enough to evoke Mekanism's GUI.
        int tabY = t + 16;
        g.fill(l + 5, tabY, l + 9, tabY + 6, alpha(0xFF4E8BD8, 76));
        g.fill(l + 5, tabY + 8, l + 9, tabY + 14, alpha(0xFFD85858, 70));
        g.fill(l + 5, tabY + 16, l + 9, tabY + 22, alpha(p.accentB(), 68));
    }

    private static void quantumPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;

        // Keep the whole Quantum identity inside one compact computer multiblock.
        // Nothing is allowed to float elsewhere in the panel.
        int assemblyW = Math.max(78, Math.min(132, width / 3));
        int assemblyH = Math.max(72, Math.min(116, height / 2));
        int left = r - assemblyW - 18;
        int top = b - assemblyH - 18;
        if (left < l + 12) left = l + Math.max(12, width - assemblyW - 12);
        if (top < t + 12) top = t + Math.max(12, height - assemblyH - 12);

        g.fill(left - 4, top - 4, left + assemblyW + 4, top - 2, alpha(QUANTUM_FRAME, 44));
        g.fill(left - 4, top + assemblyH + 2, left + assemblyW + 4, top + assemblyH + 4, alpha(QUANTUM_FRAME, 38));
        g.fill(left - 4, top - 2, left - 2, top + assemblyH + 2, alpha(QUANTUM_FRAME, 40));
        g.fill(left + assemblyW + 2, top - 2, left + assemblyW + 4, top + assemblyH + 2, alpha(QUANTUM_FRAME, 40));

        int gap = 3;
        int cols = 3;
        int rows = 3;
        int cellW = Math.max(18, (assemblyW - gap * 2) / 3);
        int cellH = Math.max(18, (assemblyH - gap * 2) / 3);
        float pulse = phase(12600L, 0.23f);

        int coreX = left + cellW + gap;
        int coreY = top + cellH + gap;
        for (int row = 0; row < rows; ++row) {
            for (int col = 0; col < cols; ++col) {
                int x = left + col * (cellW + gap);
                int y = top + row * (cellH + gap);
                boolean core = row == 1 && col == 1;
                float distance = Math.abs(row - 1) + Math.abs(col - 1);
                float wave = 0.5f + 0.5f * (float)Math.sin((pulse - distance * 0.10f) * Math.PI * 2.0);
                int glow = core ? Math.round(70 + wave * 60.0f) : Math.round(24 + wave * 30.0f);
                quantumCasingCell(g, x, y, cellW, cellH, glow, core);
            }
        }

        // Energy lanes stay entirely inside the casing and converge on the centre.
        int cx = coreX + cellW / 2;
        int cy = coreY + cellH / 2;
        int lane = alpha(QUANTUM_PURPLE, motionAlpha(20, 46));
        g.fill(left + cellW / 2, cy, cx, cy + 1, lane);
        g.fill(cx + 1, cy, left + assemblyW - cellW / 2, cy + 1, lane);
        g.fill(cx, top + cellH / 2, cx + 1, cy, lane);
        g.fill(cx, cy + 1, cx + 1, top + assemblyH - cellH / 2, lane);

        // Gold service contacts are part of the bottom casing rather than free hazard strips.
        int contactY = top + assemblyH - 2;
        for (int i = 0; i < 4; ++i) {
            int x = left + 8 + i * Math.max(10, (assemblyW - 16) / 4);
            g.fill(x, contactY, Math.min(x + 6, left + assemblyW - 4), contactY + 2, alpha(0xFFD2A62B, 46));
        }
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
        CarbonIndustrialRenderer.draw(g, l, t, r, b, p);
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
            if ((i & 1) == 0) g.fill(px + 2, py - 2, px + 4, py + 4, alpha(p.accentB(), 16));
        }

        float travel = 0.5f + 0.5f * (float)Math.sin(phase(28000L, 0.31f) * Math.PI * 2.0);
        float bandX = l + width * 0.18f + travel * width * 0.44f;
        int bandWidth = Math.max(20, width / 5);
        g.pose().pushPose();
        g.pose().translate(bandX, 0.0f, 0.0f);
        g.fill(-bandWidth / 2, t + 5, bandWidth / 2, b - 5, alpha(p.accentA(), motionAlpha(5, 22)));
        g.fill(-bandWidth / 2 + 3, t + 7, bandWidth / 2 - 3, t + 8, alpha(p.text(), motionAlpha(3, 11)));
        g.pose().popPose();
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

        float cycle = phase(12000L, 0.18f);
        float input = signalWindow(cycle, 0.04f, 0.14f, 0.56f, 0.69f);
        float branch = signalWindow(cycle, 0.18f, 0.29f, 0.70f, 0.82f);
        float output = signalWindow(cycle, 0.33f, 0.43f, 0.83f, 0.94f);

        int dustOff = alpha(p.border(), 48);
        int inputDust = alpha(lerpColor(p.border(), p.accentA(), input), Math.round(48 + input * 66));
        int branchDust = alpha(lerpColor(p.border(), p.accentA(), branch), Math.round(48 + branch * 66));
        int outputDust = alpha(lerpColor(p.border(), p.accentA(), output), Math.round(48 + output * 66));

        g.fill(l + 7, midY, r - 14, midY + 1, input > 0.01f ? inputDust : dustOff);
        redstoneRepeater(g, l + width / 3 - 4, midY - 4, input, p);
        redstoneRepeater(g, l + width * 2 / 3 - 4, midY - 4, branch, p);

        int branchX = l + width / 2;
        g.fill(branchX, upperY, branchX + 1, midY, branch > 0.01f ? branchDust : dustOff);
        g.fill(branchX, midY, branchX + 1, lowerY, output > 0.01f ? outputDust : dustOff);
        g.fill(branchX, upperY, r - 15, upperY + 1, branch > 0.01f ? branchDust : dustOff);
        g.fill(branchX, lowerY, r - 15, lowerY + 1, output > 0.01f ? outputDust : dustOff);

        redstoneTorch(g, branchX - 1, upperY - 4, branch, p);
        redstoneTorch(g, branchX - 1, lowerY + 2, output, p);
        redstoneLamp(g, r - 11, midY - 4, output, p);
        redstoneLamp(g, r - 11, upperY - 4, branch, p);
        redstoneLamp(g, r - 11, lowerY - 4, Math.min(output, input), p);
        redstoneLever(g, l + 9, midY - 5, input, p);
    }

    private static void frostPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;

        float grow = growthCycle(phase(34000L, 0.74f));
        int fullX = Math.max(10, (int)(width * 0.42f));
        int fullY = Math.max(10, Math.min((int)(height * 0.56f), height / 2));
        int leftBaseX = l + 5;
        int leftBaseY = t + 7;
        growingDiagonal(g, leftBaseX, leftBaseY, fullX, fullY, grow, alpha(p.accentB(), 54));
        if (grow > 0.28f) {
            float branchProgress = smoothstep(Math.min(1.0f, (grow - 0.28f) / 0.28f));
            int bx = leftBaseX + fullX / 2;
            int by = leftBaseY + fullY / 2;
            growingDiagonal(g, bx, by, Math.max(4, fullX / 4), -Math.max(3, fullY / 5), branchProgress, alpha(p.text(), 34));
            drawScaledCrystal(g, bx, by, 6, branchProgress, alpha(p.accentA(), 46));
        }

        float grow2 = growthCycle(phase(41000L, 0.37f));
        int fullX2 = -Math.max(10, (int)(width * 0.36f));
        int fullY2 = -Math.max(10, Math.min((int)(height * 0.48f), height / 2));
        int rightBaseX = r - 6;
        int rightBaseY = b - 8;
        growingDiagonal(g, rightBaseX, rightBaseY, fullX2, fullY2, grow2, alpha(p.accentA(), 52));
        if (grow2 > 0.35f) {
            float branchProgress = smoothstep(Math.min(1.0f, (grow2 - 0.35f) / 0.30f));
            int bx = rightBaseX + fullX2 / 2;
            int by = rightBaseY + fullY2 / 2;
            growingDiagonal(g, bx, by, fullX2 / 4, -fullY2 / 5, branchProgress, alpha(p.accentB(), 34));
            drawScaledCrystal(g, bx, by, 6, branchProgress, alpha(p.text(), 38));
        }

        if (GuiMotion.ambientMotionEnabled()) {
            for (int i = 0; i < 10; ++i) {
                float fall = phase(18000L + i * 1700L, i / 10.0f);
                float px = l + 7 + mod(hash(13007 + i * 83), Math.max(1, width - 14));
                float py = t - 5.0f + fall * (height + 12.0f);
                float drift = (float)Math.sin((fall + i * 0.17f) * Math.PI * 2.0) * (1 + i % 3);
                g.pose().pushPose();
                g.pose().translate(px + drift, py, 0.0f);
                snowflake(g, 0, 0, i % 4 == 0 ? 2 : 1, alpha(i % 3 == 0 ? p.accentB() : p.text(), 38 + (i % 4) * 8));
                g.pose().popPose();
            }
        } else {
            for (int i = 0; i < 6; ++i) {
                int px = l + 9 + mod(hash(14009 + i * 71), Math.max(1, width - 18));
                int py = t + 8 + mod(hash(15013 + i * 59), Math.max(1, height - 16));
                snowflake(g, px, py, 1, alpha(p.text(), 34));
            }
        }

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
        float growth = growthCycle(phase(46000L, 0.76f));
        float swayPhase = phase(17000L, 0.31f);
        float sway = GuiMotion.ambientMotionEnabled() ? (float)Math.sin(swayPhase * Math.PI * 2.0) * 2.0f : 0.0f;

        int vineX = l + 10;
        int maxVine = Math.max(10, height - 12);
        growingStem(g, vineX, t + 5, maxVine, growth, alpha(p.accentA(), 56));

        int leafStep = Math.max(14, maxVine / 7);
        for (int i = 0; i < 7; ++i) {
            int localY = 10 + i * leafStep;
            float threshold = localY / (float)maxVine;
            float leafProgress = smoothstep(clamp01((growth - threshold) / 0.11f));
            if (leafProgress <= 0.001f) continue;
            int py = t + 5 + localY;
            boolean right = (i & 1) == 0;
            int branch = right ? 11 : -7;
            float ex = vineX + branch + (right ? sway : -sway);
            growingHorizontal(g, vineX, py, (int)Math.round(ex - vineX), leafProgress, alpha(p.accentA(), 48));
            animatedLeaf(g, ex, py - 1, leafProgress,
                    alpha(i % 3 == 0 ? p.accentB() : p.accentA(), 72), right);
            if (i == 2 || i == 5) {
                float bloom = smoothstep(clamp01((leafProgress - 0.55f) / 0.45f));
                if (bloom > 0.01f) {
                    animatedFlower(g, ex + (right ? 3 : -2), py - 4, bloom,
                            alpha(p.accentB(), 104), alpha(p.text(), 86));
                }
            }
        }

        float hangingGrowth = growthCycle(phase(53000L, 0.41f));
        int rightVine = r - 13;
        int hangingMax = Math.min(height * 2 / 3, 96);
        growingStem(g, rightVine, t + 4, hangingMax, hangingGrowth, alpha(p.accentA(), 44));
        for (int i = 0; i < 5; ++i) {
            int localY = 9 + i * 17;
            float threshold = localY / (float)Math.max(1, hangingMax);
            float leafProgress = smoothstep(clamp01((hangingGrowth - threshold) / 0.12f));
            if (leafProgress <= 0.001f) continue;
            int py = t + 4 + localY;
            boolean right = (i & 1) != 0;
            float lx = rightVine + (right ? 4 + sway : -3 - sway);
            animatedLeaf(g, lx, py, leafProgress, alpha(p.accentA(), 58), right);
        }
        float hangingBloom = smoothstep(clamp01((hangingGrowth - 0.72f) / 0.18f));
        if (hangingBloom > 0.01f) {
            animatedFlower(g, rightVine, Math.min(t + 7 + hangingMax, b - 8), hangingBloom,
                    alpha(p.accentB(), 112), alpha(p.text(), 88));
        }

        float groundGrowth = smoothstep(clamp01((growth - 0.35f) / 0.55f));
        int groundY = b - 7;
        growingHorizontal(g, l + 8, groundY, width - 16, groundGrowth, alpha(p.accentA(), 44));
        for (int i = 0; i < 7; ++i) {
            int px = l + 18 + i * Math.max(18, (width - 36) / 7);
            float threshold = (px - (l + 8)) / (float)Math.max(1, width - 16);
            float leafProgress = smoothstep(clamp01((groundGrowth - threshold) / 0.12f));
            if (leafProgress <= 0.001f || px >= r - 12) continue;
            boolean right = (i & 1) == 0;
            animatedLeaf(g, px + (right ? sway : -sway), groundY - 1 - (i % 2), leafProgress,
                    alpha(p.accentA(), 56), right);
            if (i == 2 || i == 5) {
                float bloom = smoothstep(clamp01((leafProgress - 0.58f) / 0.42f));
                if (bloom > 0.01f) animatedFlower(g, px, groundY - 5, bloom, alpha(p.accentB(), 78), alpha(p.text(), 62));
            }
        }

        if (width > 150 && height > 100) {
            float treeGrowth = growthCycle(phase(72000L, 0.18f));
            growingTree(g, l + width * 2 / 3, b - 9, Math.min(48, Math.max(28, height / 3)), treeGrowth, sway, p);
        }

        float bloomCycle = phase(28000L, 0.36f);
        float bloomSize = 0.45f + 0.55f * (0.5f + 0.5f * (float)Math.sin(bloomCycle * Math.PI * 2.0));
        int bx = l + width / 2;
        int by = t + height * 2 / 3 + Math.round(sway);
        if (growth > 0.64f) {
            if (bloomSize > 0.62f) {
                animatedFlower(g, bx, by, smoothstep((bloomSize - 0.62f) / 0.38f),
                        alpha(p.accentB(), 106), alpha(p.text(), 66));
            } else {
                bud(g, bx, by, alpha(p.accentB(), 72));
            }
        }
    }

    private static void catPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        CatThemeRenderer.drawBody(g, l, t, r, b, p);
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

    private static int ae2ControllerColor(float offset, int requestedAlpha) {
        float hue = phase(18000L, 0.075f) + offset;
        hue = hue - (float)Math.floor(hue);

        // Keep the cycle vivid but slightly warmer than a generic RGB rainbow,
        // which better matches the classic ME Controller impression.
        float saturation = 0.86f;
        float value = 0.96f;
        int rgb = hsvColor(hue, saturation, value);
        return alpha(rgb, requestedAlpha);
    }

    private static int hsvColor(float hue, float saturation, float value) {
        float h = hue - (float)Math.floor(hue);
        float s = clamp01(saturation);
        float v = clamp01(value);
        float scaled = h * 6.0f;
        int sector = (int)Math.floor(scaled);
        float f = scaled - sector;
        float p = v * (1.0f - s);
        float q = v * (1.0f - s * f);
        float t = v * (1.0f - s * (1.0f - f));
        float rr, gg, bb;
        switch (Math.floorMod(sector, 6)) {
            case 0 -> { rr = v; gg = t; bb = p; }
            case 1 -> { rr = q; gg = v; bb = p; }
            case 2 -> { rr = p; gg = v; bb = t; }
            case 3 -> { rr = p; gg = q; bb = v; }
            case 4 -> { rr = t; gg = p; bb = v; }
            default -> { rr = v; gg = p; bb = q; }
        }
        int r = Math.max(0, Math.min(255, Math.round(rr * 255.0f)));
        int g = Math.max(0, Math.min(255, Math.round(gg * 255.0f)));
        int b = Math.max(0, Math.min(255, Math.round(bb * 255.0f)));
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    private static void controllerTrace(GuiGraphics g, int startX, int startY, int dx, int dy,
                                        int color, boolean mirror) {
        int endX = startX + dx;
        int endY = startY + dy;
        int stepX = dx >= 0 ? 1 : -1;
        int stepY = dy >= 0 ? 1 : -1;
        int absX = Math.abs(dx);
        int absY = Math.abs(dy);

        int x1 = startX;
        int x2 = startX + stepX * Math.max(5, absX / 4);
        int x3 = startX + stepX * Math.max(10, absX * 3 / 5);
        int y1 = startY + stepY * Math.max(4, absY / 3);
        int y2 = startY + stepY * Math.max(8, absY * 2 / 3);

        if (!mirror) {
            horizontal(g, x1, x2, startY, color);
            vertical(g, x2, startY, y1, color);
            horizontal(g, x2, x3, y1, color);
            vertical(g, x3, y1, y2, color);
            horizontal(g, x3, endX, y2, color);
            vertical(g, endX, y2, endY, color);
        } else {
            vertical(g, startX, startY, y1, color);
            horizontal(g, startX, x2, y1, color);
            vertical(g, x2, y1, y2, color);
            horizontal(g, x2, x3, y2, color);
            vertical(g, x3, y2, endY, color);
            horizontal(g, x3, endX, endY, color);
        }
    }

    private static void horizontal(GuiGraphics g, int x1, int x2, int y, int color) {
        int left = Math.min(x1, x2);
        int right = Math.max(x1, x2);
        if (right > left) g.fill(left, y, right + 1, y + 2, color);
    }

    private static void vertical(GuiGraphics g, int x, int y1, int y2, int color) {
        int top = Math.min(y1, y2);
        int bottom = Math.max(y1, y2);
        if (bottom > top) g.fill(x, top, x + 2, bottom + 1, color);
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
        float needle = 198.0f + 144.0f * (0.5f + 0.5f * (float)Math.sin(turn * Math.PI * 2.0));
        g.pose().pushPose();
        g.pose().translate(cx, cy, 0.0f);
        g.pose().mulPose(Axis.ZP.rotationDegrees(needle));
        g.fill(0, -1, r - 1, 1, alpha(p.text(), 72));
        g.pose().popPose();
        g.fill(cx - 1, cy - 1, cx + 2, cy + 2, alpha(p.accentB(), 70));
    }

    private static void pipeJoint(GuiGraphics g, int cx, int cy, int accent, int border) {
        g.fill(cx - 3, cy - 3, cx + 4, cy + 4, border);
        g.fill(cx - 1, cy - 1, cx + 2, cy + 2, accent);
    }

    private static void redstoneRepeater(GuiGraphics g, int x, int y, float power, InterfaceTheme.Palette p) {
        g.fill(x, y, x + 9, y + 7, alpha(0xFF6E6E6E, 110));
        g.fill(x + 1, y + 1, x + 8, y + 6, alpha(0xFFB9B9B9, 92));
        int torch = alpha(lerpColor(p.border(), p.accentA(), power), Math.round(58 + power * 66.0f));
        g.fill(x + 2, y + 2, x + 4, y + 4, torch);
        g.fill(x + 6, y + 3, x + 8, y + 5, torch);
    }

    private static void redstoneTorch(GuiGraphics g, int x, int y, float power, InterfaceTheme.Palette p) {
        g.fill(x, y + 2, x + 2, y + 7, alpha(0xFF6B3B22, 130));
        g.fill(x - 1, y, x + 3, y + 3, alpha(lerpColor(p.border(), p.accentA(), power), Math.round(54 + power * 74.0f)));
    }

    private static void redstoneLamp(GuiGraphics g, int x, int y, float power, InterfaceTheme.Palette p) {
        g.fill(x, y, x + 8, y + 8, alpha(0xFF3A1B14, 140));
        int lamp = lerpColor(0xFF5B241C, 0xFFFF7A24, power);
        g.fill(x + 2, y + 2, x + 6, y + 6, alpha(lamp, Math.round(76 + power * 56.0f)));
        if (power > 0.01f) {
            g.fill(x + 3, y + 1, x + 5, y + 7, alpha(p.accentB(), Math.round(power * 36.0f)));
            g.fill(x + 1, y + 3, x + 7, y + 5, alpha(p.accentB(), Math.round(power * 30.0f)));
        }
    }

    private static void redstoneLever(GuiGraphics g, int x, int y, float power, InterfaceTheme.Palette p) {
        g.fill(x - 2, y + 5, x + 5, y + 8, alpha(0xFF6E6E6E, 110));
        if (power >= 0.5f) {
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

    private static void growingTree(GuiGraphics g, int x, int baseY, int maxHeight, float growth, float sway,
                                    InterfaceTheme.Palette p) {
        float clamped = clamp01(growth);
        float trunkProgress = smoothstep(clamp01(clamped / 0.46f));
        int trunkColor = alpha(0xFF6A4728, 92);

        g.pose().pushPose();
        g.pose().translate(x, baseY, 0.0f);
        g.pose().scale(1.0f, Math.max(0.001f, trunkProgress), 1.0f);
        g.fill(-1, -maxHeight, 2, 0, trunkColor);
        g.pose().popPose();

        float branchProgress = smoothstep(clamp01((clamped - 0.28f) / 0.28f));
        if (branchProgress > 0.001f) {
            int branchY = baseY - maxHeight * 2 / 3;
            int branch = Math.max(4, maxHeight / 4);
            g.pose().pushPose();
            g.pose().translate(x, branchY, 0.0f);
            g.pose().scale(branchProgress, branchProgress, 1.0f);
            diagonal(g, 0, 0, -branch, -branch / 2, trunkColor);
            diagonal(g, 0, -3, branch + Math.round(sway), -branch / 2 - 3, trunkColor);
            g.pose().popPose();
        }

        float crownProgress = smoothstep(clamp01((clamped - 0.48f) / 0.30f));
        if (crownProgress > 0.001f) {
            int topY = baseY - maxHeight;
            int crown = 10;
            g.pose().pushPose();
            g.pose().translate(x + sway, topY, 0.0f);
            g.pose().scale(crownProgress, crownProgress, 1.0f);
            leafCluster(g, 0, 0, crown, alpha(p.accentA(), 74));
            leafCluster(g, -crown, crown / 2, crown - 2, alpha(p.accentA(), 62));
            leafCluster(g, crown, crown / 2, crown - 2, alpha(p.accentA(), 66));
            float bloom = smoothstep(clamp01((clamped - 0.78f) / 0.16f));
            if (bloom > 0.001f) {
                animatedFlower(g, crown - 2, 1, bloom, alpha(p.accentB(), 78), alpha(p.text(), 58));
                animatedFlower(g, -crown + 3, crown / 2, bloom, alpha(p.accentB(), 68), alpha(p.text(), 52));
            }
            g.pose().popPose();
        }
    }

    private static void leafCluster(GuiGraphics g, int cx, int cy, int radius, int color) {
        int r = Math.max(2, radius);
        g.fill(cx - r, cy - r / 2, cx + r + 1, cy + r / 2 + 1, color);
        g.fill(cx - r / 2, cy - r, cx + r / 2 + 1, cy + r + 1, alpha(color, Math.max(24, (color >>> 24) - 8)));
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static float smoothstep(float value) {
        float x = clamp01(value);
        return x * x * (3.0f - 2.0f * x);
    }

    private static float signalWindow(float phase, float onStart, float fullStart, float fullEnd, float offEnd) {
        float p = clamp01(phase);
        if (p <= onStart || p >= offEnd) return 0.0f;
        if (p < fullStart) return smoothstep((p - onStart) / Math.max(0.0001f, fullStart - onStart));
        if (p <= fullEnd) return 1.0f;
        return 1.0f - smoothstep((p - fullEnd) / Math.max(0.0001f, offEnd - fullEnd));
    }

    private static int lerpColor(int from, int to, float amount) {
        float a = clamp01(amount);
        int r = Math.round(((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * a);
        int g = Math.round(((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * a);
        int b = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * a);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    private static void tankFill(GuiGraphics g, int left, int top, int right, int bottom, float level, int color) {
        float amount = clamp01(level);
        int height = Math.max(1, bottom - top);
        g.pose().pushPose();
        g.pose().translate(0.0f, bottom, 0.0f);
        g.pose().scale(1.0f, Math.max(0.001f, amount), 1.0f);
        g.fill(left, -height, right, 0, color);
        g.pose().popPose();
    }

    private static void growingDiagonal(GuiGraphics g, int x, int y, int dx, int dy, float progress, int color) {
        float amount = smoothstep(progress);
        if (amount <= 0.001f) return;
        g.pose().pushPose();
        g.pose().translate(x, y, 0.0f);
        g.pose().scale(amount, amount, 1.0f);
        diagonal(g, 0, 0, dx, dy, color);
        g.pose().popPose();
    }

    private static void drawScaledCrystal(GuiGraphics g, float x, float y, int radius, float progress, int color) {
        float amount = smoothstep(progress);
        if (amount <= 0.001f) return;
        g.pose().pushPose();
        g.pose().translate(x, y, 0.0f);
        g.pose().scale(amount, amount, 1.0f);
        crystal(g, 0, 0, radius, color);
        g.pose().popPose();
    }

    private static void growingStem(GuiGraphics g, int x, int y, int length, float progress, int color) {
        float amount = smoothstep(progress);
        if (amount <= 0.001f) return;
        g.pose().pushPose();
        g.pose().translate(x, y, 0.0f);
        g.pose().scale(1.0f, amount, 1.0f);
        g.fill(0, 0, 1, Math.max(1, length), color);
        g.pose().popPose();
    }

    private static void growingHorizontal(GuiGraphics g, int x, int y, int length, float progress, int color) {
        float amount = smoothstep(progress);
        if (amount <= 0.001f) return;
        int direction = length < 0 ? -1 : 1;
        int abs = Math.max(1, Math.abs(length));
        g.pose().pushPose();
        g.pose().translate(x, y, 0.0f);
        g.pose().scale(amount * direction, 1.0f, 1.0f);
        g.fill(0, 0, abs, 1, color);
        g.pose().popPose();
    }

    private static void animatedLeaf(GuiGraphics g, float x, float y, float progress, int color, boolean right) {
        float amount = smoothstep(progress);
        if (amount <= 0.001f) return;
        g.pose().pushPose();
        g.pose().translate(x, y, 0.0f);
        g.pose().scale(amount, amount, 1.0f);
        leaf(g, 0, 0, alpha(color, Math.round((color >>> 24) * amount)), right);
        g.pose().popPose();
    }

    private static void animatedFlower(GuiGraphics g, float x, float y, float progress, int petal, int center) {
        float amount = smoothstep(progress);
        if (amount <= 0.001f) return;
        g.pose().pushPose();
        g.pose().translate(x, y, 0.0f);
        g.pose().scale(amount, amount, 1.0f);
        flower(g, 0, 0,
                alpha(petal, Math.round((petal >>> 24) * amount)),
                alpha(center, Math.round((center >>> 24) * amount)));
        g.pose().popPose();
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

    private static void quantumCasingCell(GuiGraphics g, int x, int y, int width, int height,
                                          int glowAlpha, boolean special) {
        int w = Math.max(10, width);
        int h = Math.max(10, height);
        g.fill(x, y, x + w, y + h, alpha(QUANTUM_CASING, 112));
        g.fill(x, y, x + 2, y + h, alpha(QUANTUM_FRAME, 54));
        g.fill(x + w - 2, y, x + w, y + h, alpha(QUANTUM_FRAME, 54));
        g.fill(x + 2, y + 2, x + w - 2, y + h - 2, alpha(QUANTUM_CASING_LIGHT, 58));

        int inset = Math.max(3, Math.min(7, Math.min(w, h) / 4));
        int purple = special ? QUANTUM_PURPLE_BRIGHT : QUANTUM_PURPLE;
        rectFrame(g, x + inset, y + inset, x + w - inset, y + h - inset,
                alpha(purple, Math.max(18, glowAlpha)));
        if (special && w > 18 && h > 18) {
            int cx = x + w / 2;
            int cy = y + h / 2;
            diamond(g, cx, cy, Math.max(2, Math.min(4, Math.min(w, h) / 6)),
                    alpha(QUANTUM_PURPLE_BRIGHT, Math.min(132, glowAlpha + 26)));
        }
    }

    private static void quantumInset(GuiGraphics g, int x, int y, int width, int height, int glowAlpha) {
        int w = Math.max(8, width);
        int h = Math.max(5, height);
        g.fill(x, y, x + w, y + h, alpha(QUANTUM_PURPLE_DARK, 88));
        rectFrame(g, x + 1, y + 1, x + w - 1, y + h - 1,
                alpha(QUANTUM_PURPLE, Math.max(28, glowAlpha)));
        if (w > 10 && h > 4) {
            g.fill(x + 3, y + h / 2, x + w - 3, y + h / 2 + 1,
                    alpha(QUANTUM_PURPLE_BRIGHT, Math.max(18, glowAlpha / 2)));
        }
    }

    private static void hazardStrip(GuiGraphics g, int x, int y, int width, int height, int opacity) {
        int w = Math.max(4, width);
        int h = Math.max(1, height);
        g.fill(x, y, x + w, y + h, alpha(0xFF181818, Math.max(24, opacity)));
        for (int px = x; px < x + w; px += 8) {
            g.fill(px, y, Math.min(px + 4, x + w), y + h, alpha(QUANTUM_HAZARD, opacity));
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
        g.fill(cx - r + 1, cy - r + 1, cx + r, cy + r, alpha(body, Math.max(18, (body >>> 24) / 2)));
        g.fill(cx - 2, cy - 2, cx + 3, cy + 3, alpha(0xFF000000, 138));

        g.pose().pushPose();
        g.pose().translate(cx, cy, 0.0f);
        g.pose().mulPose(Axis.ZP.rotationDegrees(turn * 360.0f));
        g.fill(-r, -1, r + 1, 2, accent);
        g.fill(-1, -r, 2, r + 1, accent);
        g.fill(-r + 2, -r + 2, r - 1, r - 1, alpha(body, Math.max(18, (body >>> 24) / 3)));
        g.pose().popPose();

        g.fill(cx - 1, cy - 1, cx + 2, cy + 2, alpha(body, Math.min(180, (body >>> 24) + 45)));
    }

    private static void piston(GuiGraphics g, int x, int y, int travel, boolean horizontal,
                               int body, int head, float position) {
        int range = Math.max(4, travel);
        float extension = range * (0.25f + 0.65f * (0.5f + 0.5f * (float)Math.sin(position * Math.PI * 2.0)));

        if (horizontal) {
            g.fill(x, y, x + range + 4, y + 3, alpha(body, 46));
            g.pose().pushPose();
            g.pose().translate(x + 2.0f, y + 1.0f, 0.0f);
            g.pose().scale(Math.max(0.02f, extension / range), 1.0f, 1.0f);
            g.fill(0, 0, range, 1, head);
            g.pose().popPose();

            g.pose().pushPose();
            g.pose().translate(x + 2.0f + extension, y, 0.0f);
            g.fill(-2, -2, 2, 5, alpha(head, Math.min(150, (head >>> 24) + 28)));
            g.pose().popPose();
        } else {
            g.fill(x, y, x + 3, y + range + 4, alpha(body, 46));
            g.pose().pushPose();
            g.pose().translate(x + 1.0f, y + 2.0f, 0.0f);
            g.pose().scale(1.0f, Math.max(0.02f, extension / range), 1.0f);
            g.fill(0, 0, 1, range, head);
            g.pose().popPose();

            g.pose().pushPose();
            g.pose().translate(x, y + 2.0f + extension, 0.0f);
            g.fill(-2, -2, 5, 2, alpha(head, Math.min(150, (head >>> 24) + 28)));
            g.pose().popPose();
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

        int arm = r + 3;
        g.pose().pushPose();
        g.pose().translate(cx, cy, 0.0f);
        g.pose().mulPose(Axis.ZP.rotationDegrees(turn * 360.0f));
        g.fill(-arm, -1, -r, 1, alpha(p.accentB(), 54));
        g.fill(r + 1, -1, arm + 1, 1, alpha(p.accentA(), 54));
        g.fill(-1, -arm, 1, -r, alpha(p.accentA(), 46));
        g.fill(-1, r + 1, 1, arm + 1, alpha(p.accentB(), 46));
        g.pose().popPose();

        g.fill(cx, cy, cx + 1, cy + 1, alpha(p.text(), Math.min(190, strength + 42)));
    }


    private static void catPaw(GuiGraphics g, int cx, int cy, int color, int scale) {
        int s = Math.max(1, scale);
        g.fill(cx - 2 * s, cy + s, cx + 3 * s, cy + 4 * s, color);
        g.fill(cx - 4 * s, cy - 2 * s, cx - 2 * s, cy, color);
        g.fill(cx - s, cy - 4 * s, cx + s, cy - 2 * s, color);
        g.fill(cx + 2 * s, cy - 2 * s, cx + 4 * s, cy, color);
    }

    private static void catHeart(GuiGraphics g, int cx, int cy, int color) {
        g.fill(cx - 3, cy - 2, cx, cy + 1, color);
        g.fill(cx + 1, cy - 2, cx + 4, cy + 1, color);
        g.fill(cx - 2, cy, cx + 3, cy + 3, color);
        g.fill(cx - 1, cy + 3, cx + 2, cy + 5, color);
        g.fill(cx, cy + 5, cx + 1, cy + 6, color);
    }

    private static void catFace(GuiGraphics g, int cx, int baseY, InterfaceTheme.Palette p,
                                boolean blink, int strength) {
        int fur = alpha(p.accentB(), strength);
        int inner = alpha(p.accentA(), Math.min(110, strength + 24));
        int detail = alpha(p.text(), Math.min(120, strength + 34));

        // Head and ears.
        g.fill(cx - 11, baseY - 18, cx + 12, baseY + 2, fur);
        g.fill(cx - 10, baseY - 24, cx - 4, baseY - 17, fur);
        g.fill(cx + 5, baseY - 24, cx + 11, baseY - 17, fur);
        g.fill(cx - 8, baseY - 22, cx - 5, baseY - 18, inner);
        g.fill(cx + 6, baseY - 22, cx + 9, baseY - 18, inner);

        // Eyes blink independently from the general ambient pulse.
        if (blink) {
            g.fill(cx - 6, baseY - 9, cx - 2, baseY - 8, detail);
            g.fill(cx + 3, baseY - 9, cx + 7, baseY - 8, detail);
        } else {
            g.fill(cx - 5, baseY - 11, cx - 3, baseY - 7, detail);
            g.fill(cx + 4, baseY - 11, cx + 6, baseY - 7, detail);
            g.fill(cx - 5, baseY - 9, cx - 4, baseY - 8, alpha(p.accentA(), 130));
            g.fill(cx + 4, baseY - 9, cx + 5, baseY - 8, alpha(p.accentA(), 130));
        }

        g.fill(cx, baseY - 6, cx + 2, baseY - 4, inner);
        diagonal(g, cx + 1, baseY - 4, cx - 2, baseY - 2, detail);
        diagonal(g, cx + 1, baseY - 4, cx + 4, baseY - 2, detail);

        // Whiskers.
        diagonal(g, cx - 3, baseY - 5, cx - 15, baseY - 8, alpha(p.text(), 48));
        diagonal(g, cx - 3, baseY - 3, cx - 16, baseY - 2, alpha(p.text(), 42));
        diagonal(g, cx + 4, baseY - 5, cx + 16, baseY - 8, alpha(p.text(), 48));
        diagonal(g, cx + 4, baseY - 3, cx + 17, baseY - 2, alpha(p.text(), 42));
    }

    private static void yarnBall(GuiGraphics g, int cx, int cy, int radius,
                                 InterfaceTheme.Palette p, float turn) {
        int r = Math.max(4, radius);
        int body = alpha(p.accentA(), 74);
        int light = alpha(p.accentB(), 88);
        diamond(g, cx, cy, r, body);
        int offset = Math.round((float)Math.sin(turn * Math.PI * 2.0) * 2.0f);
        diagonal(g, cx - r + 2, cy - 2 + offset, cx + r - 2, cy + 2 + offset, light);
        diagonal(g, cx - r + 2, cy + 3 - offset, cx + r - 2, cy - 3 - offset, alpha(p.text(), 42));
        g.fill(cx - 1, cy - r + 2, cx + 1, cy + r - 1, alpha(p.accentB(), 36));
    }

    private static void catTail(GuiGraphics g, int startX, int startY, int length, int amplitude,
                                float turn, int color) {
        int len = Math.max(6, length);
        int prevX = startX;
        int prevY = startY;
        for (int i = 1; i <= len; i += 2) {
            int x = startX + i;
            double wave = Math.sin((i / (double)len) * Math.PI * 1.35 + turn * Math.PI * 2.0);
            int y = startY - (int)Math.round(wave * amplitude * (i / (double)len));
            diagonal(g, prevX, prevY, x, y, color);
            prevX = x;
            prevY = y;
        }
    }

    private static void sleepingCat(GuiGraphics g, int cx, int cy, InterfaceTheme.Palette p, float breath) {
        int fur = alpha(p.accentB(), 54);
        int warm = alpha(p.accentA(), 62);
        int detail = alpha(p.text(), 48);
        int lift = GuiMotion.ambientMotionEnabled() ? Math.round(breath * 2.0f) : 1;

        // Curled body.
        g.fill(cx - 18, cy - 6 - lift, cx + 11, cy + 7, fur);
        g.fill(cx - 13, cy - 10 - lift, cx + 7, cy + 5, alpha(p.accentB(), 42));
        // Head resting on the body.
        g.fill(cx + 3, cy - 9 - lift, cx + 16, cy + 3, warm);
        g.fill(cx + 4, cy - 13 - lift, cx + 8, cy - 9 - lift, warm);
        g.fill(cx + 11, cy - 13 - lift, cx + 15, cy - 9 - lift, warm);
        // Closed eye and tiny nose.
        g.fill(cx + 7, cy - 4 - lift, cx + 11, cy - 3 - lift, detail);
        g.fill(cx + 13, cy - 1 - lift, cx + 15, cy + 1 - lift, alpha(p.accentA(), 76));
        // Front paw.
        g.fill(cx + 7, cy + 2, cx + 14, cy + 5, alpha(p.accentB(), 46));
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
