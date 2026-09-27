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
                int split = x + width * 3 / 5;
                graphics.fill(x + 3, bottom, split, bottom + 1, alpha(a, 78));
                graphics.fill(split, bottom, x + width - 3, bottom + 1, alpha(b, 78));
                int cy = y + height / 2;
                int drift = GuiMotion.ambientMotionEnabled()
                        ? Math.round((float)Math.sin(phase(11000L, 0.24f) * Math.PI * 2.0) * 1.5f)
                        : 0;
                diagonal(graphics, x + 7, cy + 3 + drift, x + width / 3, cy - 3 + drift, alpha(a, 48));
                diagonal(graphics, x + width * 2 / 3, cy - 3 - drift, x + width - 7, cy + 3 - drift, alpha(b, 48));
                graphics.fill(split - 1, cy - 2, split + 1, cy + 3, alpha(palette.text(), 42));
            }
            case AE2 -> {
                int cy = y + height / 2;
                int trace = ae2ControllerColor(active ? 0.03f : 0.0f, active ? 96 : 58);
                graphics.fill(x + 5, cy - 1, x + width / 3, cy, trace);
                graphics.fill(x + width / 3, cy - 4, x + width / 3 + 1, cy, trace);
                graphics.fill(x + width / 3, cy - 4, x + width * 2 / 3, cy - 3, trace);
                graphics.fill(x + width * 2 / 3, cy - 3, x + width * 2 / 3 + 1, cy + 3, trace);
                graphics.fill(x + width * 2 / 3, cy + 2, x + width - 10, cy + 3, trace);

                graphics.fill(x + 3, y + 3, x + 4, y + height - 3, alpha(AE2_CONTROLLER_FRAME_LIGHT, 30));
                graphics.fill(x + width - 4, y + 3, x + width - 3, y + height - 3, alpha(AE2_CONTROLLER_FRAME_MID, 28));

                if (active || hoverProgress > 0.25f) {
                    int glowAlpha = active ? 88 : Math.round(24 + hoverProgress * 52.0f);
                    graphics.fill(x + width - 9, cy - 1, x + width - 6, cy + 2,
                            ae2ControllerColor(0.18f, glowAlpha));
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
                float pulse = 0.5f + 0.5f * (float)Math.sin(phase(8200L, 0.28f) * Math.PI * 2.0);
                graphics.fill(cx - 10, cy - 6, cx + 11, cy + 7, alpha(QUANTUM_CASING, 180));
                graphics.fill(cx - 10, cy - 6, cx - 8, cy + 7, alpha(QUANTUM_FRAME, 102));
                graphics.fill(cx + 9, cy - 6, cx + 11, cy + 7, alpha(QUANTUM_FRAME, 102));
                quantumInset(graphics, cx - 6, cy - 4, 13, 8, Math.round(52 + pulse * 48.0f));
                hazardStrip(graphics, cx - 9, cy + 4, 18, 2, 64);
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
                int split = x + width * 3 / 5;
                graphics.fill(x + 2, y + 2, split, y + 3, a);
                graphics.fill(split, y + 2, right - 2, y + 3, b);
            }
            case AE2 -> {
                int traceA = ae2ControllerColor(0.00f, 42);
                int traceB = ae2ControllerColor(0.22f, 34);
                graphics.fill(x + 5, bottom - 3, right - 5, bottom - 2, alpha(AE2_CONTROLLER_FRAME_DARK, 48));
                controllerTrace(graphics, x + 7, y + 5, Math.max(18, width / 3), Math.max(6, height / 3), traceA, false);
                controllerTrace(graphics, right - 8, bottom - 6, -Math.max(16, width / 4), -Math.max(5, height / 4), traceB, true);
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
                int moduleW = Math.max(14, Math.min(24, width / 7));
                int start = right - moduleW * 3 - 10;
                for (int i = 0; i < 3; ++i) {
                    int mx = start + i * (moduleW + 2);
                    if (mx < x + 4) continue;
                    quantumCasingCell(graphics, mx, y + 3, moduleW, Math.max(8, height - 6),
                            i == 1 ? 72 : 48, i == 1);
                }
                hazardStrip(graphics, x + 5, bottom - 3, Math.max(12, width / 3), 1, 52);
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
        int split = l + width * 3 / 5;
        int cy = t + height / 2;
        float wave = (float)Math.sin(phase(22000L, 0.31f) * Math.PI * 2.0);
        float lift = GuiMotion.ambientMotionEnabled() ? wave * 3.0f : 0.0f;

        // Signature is deliberately asymmetrical and open: two long ribbons crossing
        // the 3/5 split, not a machine frame or a nested computer core.
        g.pose().pushPose();
        g.pose().translate(0.0f, lift, 0.0f);
        diagonal(g, l + 18, cy + 18, split - 8, cy - 12, alpha(p.accentA(), 31));
        diagonal(g, l + 34, cy + 22, split + 18, cy - 8, alpha(p.accentA(), 18));
        diagonal(g, split - 2, cy - 12, r - 22, cy + 10, alpha(p.accentB(), 30));
        diagonal(g, split + 18, cy - 8, r - 38, cy + 15, alpha(p.accentB(), 18));
        g.pose().popPose();

        // A restrained split spine anchors the brand language.
        g.fill(split, t + 13, split + 1, b - 13, alpha(p.border(), 22));
        int markerY = cy + Math.round(wave * Math.min(12, height / 8.0f));
        g.fill(split - 2, markerY - 1, split + 3, markerY + 2, alpha(p.text(), 38));

        for (int x = l + 10; x < split - 9; x += 29) {
            g.fill(x, b - 7, Math.min(x + 13, split - 8), b - 6, alpha(p.accentA(), 21));
        }
        for (int x = split + 8; x < r - 10; x += 33) {
            g.fill(x, t + 8, Math.min(x + 12, r - 8), t + 9, alpha(p.accentB(), 19));
        }
    }

    private static void ae2Panel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;

        // ME Controller-inspired body: pale structural bands framing a dark inset.
        int frameAlpha = 28;
        g.fill(l + 6, t + 7, r - 6, t + 9, alpha(AE2_CONTROLLER_FRAME_LIGHT, frameAlpha));
        g.fill(l + 6, b - 9, r - 6, b - 7, alpha(AE2_CONTROLLER_FRAME_MID, frameAlpha));
        g.fill(l + 6, t + 9, l + 8, b - 9, alpha(AE2_CONTROLLER_FRAME_LIGHT, frameAlpha));
        g.fill(r - 8, t + 9, r - 6, b - 9, alpha(AE2_CONTROLLER_FRAME_DARK, frameAlpha));

        // A large orthogonal maze fills the otherwise-empty panel, but stays behind content.
        int mazeLeft = l + Math.max(16, width / 10);
        int mazeTop = t + Math.max(20, height / 7);
        int mazeRight = r - Math.max(18, width / 9);
        int mazeBottom = b - Math.max(20, height / 8);
        int mazeW = Math.max(50, mazeRight - mazeLeft);
        int mazeH = Math.max(40, mazeBottom - mazeTop);

        // Slowly color-cycling controller traces. Each route gets a phase offset,
        // so the panel feels alive without becoming a rainbow strobe.
        controllerTrace(g, mazeLeft, mazeTop + mazeH / 5, mazeW * 3 / 5, mazeH / 3,
                ae2ControllerColor(0.00f, 34), false);
        controllerTrace(g, mazeRight, mazeTop + mazeH / 3, -mazeW / 2, mazeH / 4,
                ae2ControllerColor(0.14f, 31), true);
        controllerTrace(g, mazeLeft + mazeW / 5, mazeBottom, mazeW / 2, -mazeH * 2 / 5,
                ae2ControllerColor(0.28f, 28), false);
        controllerTrace(g, mazeRight - mazeW / 6, mazeBottom - mazeH / 7, -mazeW * 2 / 5, -mazeH / 3,
                ae2ControllerColor(0.42f, 26), true);

        // Secondary dark traces create the Controller's layered circuit-labyrinth look.
        controllerTrace(g, mazeLeft + 9, mazeTop + 6, mazeW / 3, mazeH / 5,
                alpha(AE2_CONTROLLER_FRAME_DARK, 32), true);
        controllerTrace(g, mazeRight - 12, mazeBottom - 8, -mazeW / 3, -mazeH / 5,
                alpha(AE2_CONTROLLER_FRAME_MID, 24), false);

        // One small ME Drive cluster only; status colours stay contextual instead of covering every widget.
        if (width > 240 && height > 130) {
            int driveW = Math.max(46, Math.min(66, width / 6));
            int driveH = Math.max(34, Math.min(48, height / 5));
            int driveRight = r - 18;
            int driveLeft = driveRight - driveW;
            int driveTop = t + 16;
            int driveBottom = driveTop + driveH;
            g.fill(driveLeft, driveTop, driveRight, driveBottom, alpha(AE2_CONTROLLER_FRAME_MID, 54));
            g.fill(driveLeft + 2, driveTop + 2, driveRight - 2, driveBottom - 2, alpha(AE2_CONTROLLER_INSET, 94));

            int slotW = Math.max(8, (driveW - 11) / 2);
            int slotH = Math.max(5, (driveH - 11) / 5);
            float activity = phase(22000L, 0.31f) * 10.0f;
            int active = Math.floorMod((int)Math.floor(activity), 10);
            int next = (active + 1) % 10;
            float blend = smoothstep(activity - (float)Math.floor(activity));
            for (int i = 0; i < 10; ++i) {
                int col = i % 2;
                int row = i / 2;
                int sx = driveLeft + 4 + col * (slotW + 3);
                int sy = driveTop + 4 + row * (slotH + 1);
                if (sx + slotW > driveRight - 3 || sy + slotH > driveBottom - 3) continue;
                int led = ae2CellColor(i);
                float glow = i == active ? 1.0f - blend : (i == next ? blend : 0.0f);
                g.fill(sx, sy, sx + slotW, sy + slotH, alpha(0xFF292C32, 92));
                g.fill(sx + 2, sy + slotH - 2, sx + slotW - 2, sy + slotH - 1,
                        alpha(led, 44 + Math.round(glow * 42.0f * GuiMotion.ambientMotionIntensity())));
            }
        }

        // One tiny controller core tile anchors the palette without dominating the screen.
        int coreX = l + 24;
        int coreY = b - 28;
        g.fill(coreX, coreY, coreX + 18, coreY + 18, alpha(AE2_CONTROLLER_FRAME_MID, 42));
        g.fill(coreX + 3, coreY + 3, coreX + 15, coreY + 15, alpha(AE2_CONTROLLER_INSET, 72));
        int coreColor = ae2ControllerColor(0.08f, 62);
        controllerTrace(g, coreX + 4, coreY + 5, 10, 7, coreColor, false);
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

        segmentedRail(g, leftRail, t + 7, b - 7, p.accentA(), p.border());
        segmentedRail(g, rightRail, t + 7, b - 7, p.accentB(), p.border());

        g.fill(leftRail + 3, cy, cx - 12, cy + 1, alpha(p.border(), 38));
        g.fill(cx + 12, cy, rightRail - 2, cy + 1, alpha(p.border(), 38));
        energyCore(g, cx, cy, 10, p, phase(18000L, 0.24f), 86);

        int tankTop = t + 10;
        int tankBottom = Math.min(b - 11, tankTop + Math.max(30, height / 2));
        int[] tankXs = {l + width / 4, l + width * 3 / 4};
        for (int i = 0; i < tankXs.length; ++i) {
            int tx = tankXs[i];
            int fluid = i == 0 ? p.accentA() : p.accentB();
            g.fill(tx - 7, tankTop, tx + 8, tankBottom, alpha(p.border(), 64));
            g.fill(tx - 6, tankTop + 1, tx + 7, tankBottom - 1, alpha(p.tableOuter(), 100));
            float tankPhase = phase(26000L + i * 7000L, 0.21f + i * 0.28f);
            float level = 0.28f + 0.46f * (0.5f + 0.5f * (float)Math.sin(tankPhase * Math.PI * 2.0));
            tankFill(g, tx - 5, tankTop + 2, tx + 6, tankBottom - 2, level, alpha(fluid, 56));
            g.fill(tx - 4, tankTop + 5, tx + 5, tankTop + 6, alpha(p.text(), 22));
        }

        int gaugeX = cx;
        int gaugeY = Math.min(b - 18, t + 18);
        gauge(g, gaugeX, gaugeY, 7, p, phase(21000L, 0.33f));
        pipeJoint(g, leftRail, cy, alpha(p.accentA(), 70), alpha(p.border(), 76));
        pipeJoint(g, rightRail - 1, cy, alpha(p.accentB(), 70), alpha(p.border(), 76));
    }

    private static void quantumPanel(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;

        // Build a recognisable AdvancedAE Quantum Computer silhouette rather than
        // another abstract nested-frame UI. The tower stays low-contrast because it
        // is ambient decoration behind the actual monitor controls.
        int towerW = Math.max(86, Math.min(176, width * 2 / 5));
        int towerH = Math.max(92, Math.min(218, height * 3 / 5));
        int towerLeft = r - towerW - 18;
        int towerTop = b - towerH - 16;
        if (towerLeft < l + 18) {
            towerLeft = l + Math.max(18, (width - towerW) / 2);
        }
        towerTop = Math.max(t + 12, towerTop);

        int cols = width > 520 ? 4 : 3;
        int rows = height > 260 ? 5 : 4;
        int gap = 3;
        int cellW = Math.max(18, (towerW - gap * (cols - 1)) / cols);
        int cellH = Math.max(18, (towerH - gap * (rows - 1)) / rows);

        float cycle = phase(26000L, 0.27f);

        // Faint structural-glass silhouette behind the dark internal blocks.
        g.fill(towerLeft - 5, towerTop - 5, towerLeft - 3, towerTop + towerH + 5, alpha(QUANTUM_FRAME, 24));
        g.fill(towerLeft + towerW + 3, towerTop - 5, towerLeft + towerW + 5, towerTop + towerH + 5, alpha(QUANTUM_FRAME, 24));
        g.fill(towerLeft - 5, towerTop - 5, towerLeft + towerW + 5, towerTop - 3, alpha(QUANTUM_FRAME, 20));

        for (int row = 0; row < rows; ++row) {
            float rowWave = 0.5f + 0.5f * (float)Math.sin((cycle + row * 0.12f) * Math.PI * 2.0);
            for (int col = 0; col < cols; ++col) {
                int mx = towerLeft + col * (cellW + gap);
                int my = towerTop + row * (cellH + gap);
                boolean special = (row == 0 && col == cols / 2)
                        || (row == rows / 2 && col == Math.max(0, cols / 2 - 1));
                int glow = special
                        ? Math.round(62 + rowWave * 58.0f)
                        : Math.round(22 + rowWave * 22.0f);
                quantumCasingCell(g, mx, my, cellW, cellH, glow, special);
            }

            // Hazard seams between stacked tiers echo the real machine's industrial bands.
            if (row < rows - 1) {
                int seamY = towerTop + (row + 1) * cellH + row * gap + 1;
                hazardStrip(g, towerLeft + 2, seamY, towerW - 4, 2, 48);
            }
        }

        // Bright top core plate, inspired by the exposed glowing quantum core.
        int coreX = towerLeft + towerW / 2;
        int coreY = towerTop - 1;
        float corePulse = 0.5f + 0.5f * (float)Math.sin(phase(9800L, 0.4f) * Math.PI * 2.0);
        int coreW = Math.max(22, towerW / 4);
        g.fill(coreX - coreW / 2 - 3, coreY - 5, coreX + coreW / 2 + 3, coreY + 4, alpha(QUANTUM_CASING_LIGHT, 72));
        quantumInset(g, coreX - coreW / 2, coreY - 3, coreW, 6, Math.round(64 + corePulse * 64.0f));

        // Central vertical energy shaft is contained inside the multiblock.
        int shaftTop = towerTop + cellH;
        int shaftBottom = towerTop + towerH - cellH / 2;
        g.fill(coreX - 2, shaftTop, coreX + 3, shaftBottom, alpha(QUANTUM_PURPLE_DARK, 28));
        g.fill(coreX, shaftTop, coreX + 1, shaftBottom, alpha(QUANTUM_PURPLE_BRIGHT, Math.round(20 + corePulse * 24.0f)));

        // A small entangler-like cross inside the tower adds another AdvancedAE-specific cue.
        if (towerW > 110 && towerH > 130) {
            int ex = towerLeft + cellW / 2;
            int ey = towerTop + towerH - cellH / 2;
            int entangleAlpha = Math.round(28 + (1.0f - corePulse) * 34.0f);
            diagonal(g, ex - 7, ey - 7, ex + 7, ey + 7, alpha(QUANTUM_PURPLE, entangleAlpha));
            diagonal(g, ex - 7, ey + 7, ex + 7, ey - 7, alpha(QUANTUM_PURPLE_BRIGHT, entangleAlpha));
            diamond(g, ex, ey, 2, alpha(QUANTUM_PURPLE_BRIGHT, 74));
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
