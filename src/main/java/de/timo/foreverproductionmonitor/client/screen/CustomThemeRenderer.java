package de.timo.foreverproductionmonitor.client.screen;

import de.timo.foreverproductionmonitor.client.ClientConfig;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Renderer for configurable CUSTOM-theme decoration.
 * It is intentionally client-only and derives all state from ClientConfig.
 */
final class CustomThemeRenderer {
    private CustomThemeRenderer() {
    }

    static void drawAmbient(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        drawBackground(g, l, t, r, b, p);
        drawParticles(g, l, t, r, b, p);
    }

    static void drawControl(GuiGraphics g, boolean tab, int x, int y, int width, int height,
                            boolean enabled, boolean selected, float hover,
                            InterfaceTheme.Palette p) {
        if (width <= 2 || height <= 2) {
            return;
        }
        if (tab) {
            drawTab(g, x, y, width, height, enabled, selected, hover, p);
        } else {
            drawButton(g, x, y, width, height, enabled, selected, hover, p);
        }
    }

    static void drawTab(GuiGraphics g, int x, int y, int width, int height,
                        boolean enabled, boolean selected, float hover,
                        InterfaceTheme.Palette p) {
        ClientConfig.CustomTabStyle style =
                (ClientConfig.CustomTabStyle)ClientConfig.VALUES.interfaceCustomTabStyle.get();
        float accentStrength = ((Double)ClientConfig.VALUES.interfaceCustomTabAccentStrength.get()).floatValue();
        int activeAccent = GuiMotion.alpha(p.accentB(), Math.round(90 + accentStrength * 150.0f));
        int hoverAccent = GuiMotion.alpha(p.accentA(), Math.round(hover * accentStrength * 90.0f));
        int outer = enabled ? p.border() : GuiMotion.alpha(p.border(), 95);
        int base = enabled ? p.summary() : GuiMotion.alpha(p.summary(), 130);
        int selectedBase = enabled ? p.header() : GuiMotion.alpha(p.header(), 130);

        switch (style) {
            case FLAT -> {
                g.fill(x, y, x + width, y + height, selected ? selectedBase : base);
                if (hover > 0.01f) {
                    g.fill(x, y, x + width, y + height, hoverAccent);
                }
            }
            case FILLED -> {
                g.fill(x, y, x + width, y + height, outer);
                g.fill(x + 1, y + 1, x + width - 1, y + height - 1, selected ? selectedBase : base);
                if (selected) {
                    g.fill(x + 2, y + 2, x + width - 2, y + height - 2,
                            GuiMotion.alpha(p.accentB(), Math.round(26 + accentStrength * 54.0f)));
                } else if (hover > 0.01f) {
                    g.fill(x + 1, y + 1, x + width - 1, y + height - 1, hoverAccent);
                }
            }
            case UNDERLINE -> {
                g.fill(x, y, x + width, y + height, base);
                int lineAlpha = selected ? Math.round(100 + accentStrength * 155.0f)
                        : Math.round(hover * accentStrength * 120.0f);
                if (lineAlpha > 0) {
                    g.fill(x + 3, y + height - 2, x + width - 3, y + height,
                            GuiMotion.alpha(selected ? p.accentB() : p.accentA(), lineAlpha));
                }
            }
            case FRAMED -> {
                g.fill(x, y, x + width, y + height, selected ? activeAccent : outer);
                g.fill(x + 1, y + 1, x + width - 1, y + height - 1, selected ? selectedBase : base);
                if (!selected && hover > 0.01f) {
                    g.fill(x + 2, y + 2, x + width - 2, y + height - 2, hoverAccent);
                }
                if (selected) {
                    g.fill(x + 4, y + height - 3, x + width - 4, y + height - 1, activeAccent);
                }
            }
            case SEGMENTED -> {
                g.fill(x, y, x + width, y + height, base);
                int segment = Math.max(6, width / 5);
                int accent = selected ? activeAccent
                        : GuiMotion.alpha(p.accentA(), Math.round(34 + hover * 70.0f));
                g.fill(x, y, x + segment, y + 2, accent);
                g.fill(x + width - segment, y + height - 2, x + width, y + height, accent);
                g.fill(x, y, x + 2, y + Math.min(height, 7), accent);
                g.fill(x + width - 2, y + Math.max(0, height - 7), x + width, y + height, accent);
            }
        }
    }

    static void drawButton(GuiGraphics g, int x, int y, int width, int height,
                           boolean enabled, boolean selected, float hover,
                           InterfaceTheme.Palette p) {
        ClientConfig.CustomButtonStyle style =
                (ClientConfig.CustomButtonStyle)ClientConfig.VALUES.interfaceCustomButtonStyle.get();
        float hoverStrength = ((Double)ClientConfig.VALUES.interfaceCustomButtonHoverStrength.get()).floatValue();
        int outer = enabled ? p.border() : GuiMotion.alpha(p.border(), 95);
        int base = enabled ? (selected ? p.tableHeader() : p.summary())
                : GuiMotion.alpha(p.tableOuter(), 150);
        int accent = selected ? p.accentB() : p.accentA();
        int hoverOverlay = GuiMotion.alpha(accent, Math.round(hover * hoverStrength * 110.0f));

        switch (style) {
            case FLAT -> {
                g.fill(x, y, x + width, y + height, base);
                if (hover > 0.01f) {
                    g.fill(x, y, x + width, y + height, hoverOverlay);
                }
            }
            case FILLED -> {
                g.fill(x, y, x + width, y + height, outer);
                g.fill(x + 1, y + 1, x + width - 1, y + height - 1, base);
                if (selected) {
                    g.fill(x + 2, y + 2, x + width - 2, y + height - 2,
                            GuiMotion.alpha(accent, 34));
                }
                if (hover > 0.01f) {
                    g.fill(x + 1, y + 1, x + width - 1, y + height - 1, hoverOverlay);
                }
            }
            case OUTLINE -> {
                g.fill(x, y, x + width, y + height, base);
                g.renderOutline(x, y, width, height,
                        selected ? p.accentB() : (hover > 0.12f ? p.accentA() : outer));
            }
            case UNDERLINE -> {
                g.fill(x, y, x + width, y + height, base);
                int lineAlpha = selected ? 220 : Math.round(hover * hoverStrength * 190.0f);
                if (lineAlpha > 0) {
                    g.fill(x + 4, y + height - 2, x + width - 4, y + height,
                            GuiMotion.alpha(accent, lineAlpha));
                }
            }
            case SEGMENTED -> {
                g.fill(x, y, x + width, y + height, outer);
                g.fill(x + 1, y + 1, x + width - 1, y + height - 1, base);
                int seg = Math.max(5, width / 8);
                int deco = GuiMotion.alpha(accent,
                        selected ? 180 : Math.round(50 + hover * hoverStrength * 100.0f));
                g.fill(x + 2, y + 2, x + 2 + seg, y + 4, deco);
                g.fill(x + width - 2 - seg, y + height - 4, x + width - 2, y + height - 2, deco);
            }
        }
    }

    private static void drawBackground(GuiGraphics g, int l, int t, int r, int b,
                                       InterfaceTheme.Palette p) {
        ClientConfig.CustomBackgroundStyle style =
                (ClientConfig.CustomBackgroundStyle)ClientConfig.VALUES.interfaceCustomBackgroundStyle.get();
        if (style == ClientConfig.CustomBackgroundStyle.NONE) {
            return;
        }

        float density = ((Double)ClientConfig.VALUES.interfaceCustomBackgroundDensity.get()).floatValue();
        float speed = ((Double)ClientConfig.VALUES.interfaceCustomBackgroundSpeed.get()).floatValue();
        float opacity = ((Double)ClientConfig.VALUES.interfaceCustomBackgroundOpacity.get()).floatValue();
        float scale = ((Double)ClientConfig.VALUES.interfaceCustomBackgroundScale.get()).floatValue();
        int width = Math.max(1, r - l);
        int height = Math.max(1, b - t);
        int alpha = Math.max(0, Math.min(180, Math.round(opacity * 150.0f)));

        switch (style) {
            case GRID -> {
                int spacing = Math.max(8, Math.round((31.0f - density * 17.0f) * scale));
                int shift = motionShift(spacing, speed, 18000L);
                for (int x = l - spacing + shift; x < r + spacing; x += spacing) {
                    g.fill(x, t, x + 1, b, GuiMotion.alpha(p.accentA(), alpha / 2));
                }
                for (int y = t - spacing + shift / 2; y < b + spacing; y += spacing) {
                    g.fill(l, y, r, y + 1, GuiMotion.alpha(p.accentB(), alpha / 3));
                }
            }
            case CIRCUIT -> {
                int tracks = Math.max(4, Math.round(5 + density * 10.0f));
                for (int i = 0; i < tracks; ++i) {
                    int seed = 911 + i * 131;
                    int y = t + 6 + mod(hash(seed), Math.max(1, height - 12));
                    int x1 = l + 5 + mod(hash(seed + 7), Math.max(1, width / 3));
                    int x2 = l + width / 2 + mod(hash(seed + 13), Math.max(1, width / 2 - 8));
                    int color = i % 2 == 0 ? p.accentA() : p.accentB();
                    int line = GuiMotion.alpha(color, alpha / 2);
                    g.fill(x1, y, x2, y + 1, line);
                    int branch = Math.max(t + 3, Math.min(b - 4,
                            y + (mod(hash(seed + 19), 21) - 10)));
                    g.fill(x2 - 1, Math.min(y, branch), x2, Math.max(y, branch) + 1, line);
                    g.fill(x2 - 2, branch - 1, x2 + 2, branch + 2,
                            GuiMotion.alpha(color, Math.min(190, alpha)));
                }
                if (GuiMotion.ambientMotionEnabled() && speed > 0.0f) {
                    float progress = phase(9000L / Math.max(0.2f, speed), 0.14f);
                    int px = l + Math.round(progress * (width - 8));
                    int py = t + height / 2;
                    g.fill(px, py - 1, px + 5, py + 2,
                            GuiMotion.alpha(p.accentB(), Math.min(210, alpha + 50)));
                }
            }
            case STARFIELD -> {
                int stars = Math.max(8, Math.round(12 + density * 46.0f));
                int drift = motionShift(width, speed, 36000L);
                for (int i = 0; i < stars; ++i) {
                    int seed = 1907 + i * 83;
                    int sx = l + mod(hash(seed) + drift, width);
                    int sy = t + mod(hash(seed + 29), height);
                    float twinkle = 0.5f + 0.5f * (float)Math.sin(
                            phase(6000L + (i % 5) * 900L, i * 0.07f) * Math.PI * 2.0);
                    int starAlpha = Math.round(alpha * (0.45f + twinkle * 0.55f));
                    int color = i % 4 == 0 ? p.accentB() : (i % 3 == 0 ? p.accentA() : p.text());
                    int size = i % 11 == 0 ? 2 : 1;
                    g.fill(sx, sy, sx + size, sy + size, GuiMotion.alpha(color, starAlpha));
                }
            }
            case SCAN_LINES -> {
                int spacing = Math.max(4, Math.round((13.0f - density * 7.0f) * scale));
                int shift = motionShift(spacing, speed, 6500L);
                for (int y = t - spacing + shift; y < b + spacing; y += spacing) {
                    g.fill(l, y, r, y + 1, GuiMotion.alpha(p.accentA(), alpha / 2));
                }
                if (GuiMotion.ambientMotionEnabled() && speed > 0.0f) {
                    int scanY = t + Math.round(phase(7200L / Math.max(0.2f, speed), 0.21f) * height);
                    g.fill(l, scanY, r, scanY + 2, GuiMotion.alpha(p.accentB(), Math.min(190, alpha + 40)));
                }
            }
            case ENERGY_WAVES -> {
                int waves = Math.max(2, Math.round(2 + density * 4.0f));
                float phase = phase(11000L / Math.max(0.2f, speed), 0.18f) * (float)Math.PI * 2.0f;
                for (int wave = 0; wave < waves; ++wave) {
                    int baseY = t + (wave + 1) * height / (waves + 1);
                    int color = wave % 2 == 0 ? p.accentA() : p.accentB();
                    int prevX = l;
                    int prevY = baseY;
                    for (int x = l + 4; x < r; x += 4) {
                        double local = (x - l) / Math.max(1.0, width) * Math.PI * 3.0
                                + phase + wave * 0.9;
                        int y = baseY + (int)Math.round(Math.sin(local) * (3.0 + 4.0 * scale));
                        diagonal(g, prevX, prevY, x, y, GuiMotion.alpha(color, alpha / 2));
                        prevX = x;
                        prevY = y;
                    }
                }
            }
            case NONE -> {
            }
        }
    }

    private static void drawParticles(GuiGraphics g, int l, int t, int r, int b,
                                      InterfaceTheme.Palette p) {
        ClientConfig.CustomParticleStyle style =
                (ClientConfig.CustomParticleStyle)ClientConfig.VALUES.interfaceCustomParticleStyle.get();
        if (style == ClientConfig.CustomParticleStyle.NONE) {
            return;
        }

        float amount = ((Double)ClientConfig.VALUES.interfaceCustomParticleAmount.get()).floatValue();
        float speed = ((Double)ClientConfig.VALUES.interfaceCustomParticleSpeed.get()).floatValue();
        float sizeSetting = ((Double)ClientConfig.VALUES.interfaceCustomParticleSize.get()).floatValue();
        float opacity = ((Double)ClientConfig.VALUES.interfaceCustomParticleOpacity.get()).floatValue();
        int width = Math.max(1, r - l);
        int height = Math.max(1, b - t);
        int count = Math.max(1, Math.round(3 + amount * 34.0f));
        int size = Math.max(1, Math.min(3, Math.round(sizeSetting)));
        int baseAlpha = Math.max(0, Math.min(220, Math.round(opacity * 190.0f)));
        float motion = GuiMotion.ambientMotionEnabled() && speed > 0.0f
                ? phase(15000L / Math.max(0.18f, speed), 0.12f) : 0.0f;

        for (int i = 0; i < count; ++i) {
            int seed = 3203 + i * 107;
            int color = particleColor(i, p);
            int x;
            int y;
            switch (style) {
                case PIXELS -> {
                    x = l + mod(hash(seed) + Math.round(motion * width * (1 + i % 3)), width);
                    y = t + mod(hash(seed + 31) - Math.round(motion * height * (1 + i % 2)), height);
                    g.fill(x, y, x + size, y + size, GuiMotion.alpha(color, baseAlpha));
                }
                case SPARKS -> {
                    x = l + mod(hash(seed) + Math.round(motion * width), width);
                    y = t + mod(hash(seed + 41) - Math.round(motion * height), height);
                    diagonal(g, x, y, x + 2 + size * 2, y - 1 - size,
                            GuiMotion.alpha(color, baseAlpha));
                }
                case DATA_PACKETS -> {
                    int lane = t + (i + 1) * height / (count + 1);
                    int direction = (i & 1) == 0 ? 1 : -1;
                    int offset = Math.round(motion * width * direction);
                    x = l + mod(hash(seed) + offset, width);
                    y = lane;
                    g.fill(x, y, x + 3 + size * 2, y + Math.max(1, size),
                            GuiMotion.alpha(color, baseAlpha));
                    g.fill(Math.max(l, x - 7), y, x, y + 1,
                            GuiMotion.alpha(color, baseAlpha / 3));
                }
                case STARS -> {
                    x = l + mod(hash(seed), width);
                    y = t + mod(hash(seed + 59), height);
                    float twinkle = 0.5f + 0.5f * (float)Math.sin(
                            phase(5000L + i % 7 * 600L, i * 0.09f) * Math.PI * 2.0);
                    int a = Math.round(baseAlpha * (0.45f + 0.55f * twinkle));
                    g.fill(x - size, y, x + size + 1, y + 1, GuiMotion.alpha(color, a));
                    g.fill(x, y - size, x + 1, y + size + 1, GuiMotion.alpha(color, a));
                }
                case NONE -> {
                }
            }
        }
    }

    private static int particleColor(int index, InterfaceTheme.Palette p) {
        ClientConfig.CustomParticleColorMode mode =
                (ClientConfig.CustomParticleColorMode)ClientConfig.VALUES.interfaceCustomParticleColorMode.get();
        return switch (mode) {
            case PRIMARY -> p.accentA();
            case SECONDARY -> p.accentB();
            case MIXED -> (index & 1) == 0 ? p.accentA() : p.accentB();
            case WHITE -> p.text();
        };
    }

    private static int motionShift(int span, float speed, long baseDuration) {
        if (!GuiMotion.ambientMotionEnabled() || speed <= 0.0f || span <= 0) {
            return 0;
        }
        long duration = Math.max(800L, Math.round(baseDuration / Math.max(0.18f, speed)));
        return Math.round(phase(duration, 0.0f) * span);
    }

    private static float phase(double durationMillis, float offset) {
        if (!GuiMotion.ambientMotionEnabled() || durationMillis <= 0.0) {
            return offset - (float)Math.floor(offset);
        }
        long durationNanos = Math.max(1L, Math.round(durationMillis * 1_000_000.0));
        float value = (float)((double)Math.floorMod(GuiMotion.now(), durationNanos) / (double)durationNanos) + offset;
        return value - (float)Math.floor(value);
    }

    private static int hash(int x) {
        x ^= x >>> 16;
        x *= 0x7feb352d;
        x ^= x >>> 15;
        x *= 0x846ca68b;
        x ^= x >>> 16;
        return x;
    }

    private static int mod(int value, int mod) {
        if (mod <= 0) return 0;
        int result = value % mod;
        return result < 0 ? result + mod : result;
    }

    private static void diagonal(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int steps = Math.max(dx, dy);
        if (steps == 0) {
            g.fill(x1, y1, x1 + 1, y1 + 1, color);
            return;
        }
        for (int i = 0; i <= steps; ++i) {
            int x = x1 + Math.round((x2 - x1) * (i / (float)steps));
            int y = y1 + Math.round((y2 - y1) * (i / (float)steps));
            g.fill(x, y, x + 1, y + 1, color);
        }
    }
}
