package de.timo.foreverproductionmonitor.client.screen;

import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;

/**
 * A little working factory behind Carbon Industrial's content. All coordinates
 * are relative to the panel edge; the panel scissor and opaque content surfaces
 * keep the machinery out of text, tables and the surrounding screen.
 */
final class CarbonIndustrialRenderer {
    private static final int SHADOW = 0xFF101316;
    private static final int IRON = 0xFF343A40;
    private static final int STEEL = 0xFF687681;
    private static final int ORE = 0xFF89939C;
    private static final int AMBER = 0xFFE0A43A;

    private CarbonIndustrialRenderer() {
    }

    static void draw(GuiGraphics g, int l, int t, int r, int b, InterfaceTheme.Palette p) {
        int width = r - l;
        int height = b - t;
        if (width < 170 || height < 56) {
            return;
        }

        int railY = b - 14;
        float train = cycle(17000L, 0.0f);
        float strength = GuiMotion.ambientMotionIntensity();
        boolean moving = GuiMotion.ambientMotionEnabled();

        // Narrow panels get a real, continuous railway, without a cramped skyline.
        if (width >= 340 && height >= 130) {
            factory(g, l, r, railY, train, strength, moving);
        }
        tracks(g, l + 10, r - 10, railY, p);
        if (width >= 340 && height >= 130) {
            int start = l + 147;
            int tunnel = r - 65;
            if (!moving || train < 0.84f) {
                float travel = moving ? smooth(clamp((train - 0.17f) / 0.67f)) : 0.0f;
                int cartX = start + Math.round((tunnel - start) * travel);
                float visible = moving ? 1.0f - smooth(clamp((train - 0.76f) / 0.08f)) : 1.0f;
                float loaded = moving ? clamp(train / 0.17f) : 1.0f;
                cart(g, cartX, railY, travel, strength, visible, loaded);
            }
            tunnelFront(g, r - 89, railY);
        } else if (width >= 230) {
            int start = l + 27;
            int end = r - 27;
            float travel = moving ? smooth(clamp((train - 0.12f) / 0.73f)) : 0.0f;
            if (!moving || train < 0.85f) {
                cart(g, start + Math.round((end - start) * travel), railY, travel,
                        strength, 1.0f, 1.0f);
            }
        }
    }

    private static void factory(GuiGraphics g, int l, int r, int railY,
                                float train, float strength, boolean moving) {
        int mid = (l + r) / 2;

        // Smoke is behind the buildings. Different lifetimes and wind offsets
        // keep the three chimneys from emitting in lockstep.
        smoke(g, l + 47, railY - 83, 5100L, 0.12f, strength);
        smoke(g, l + 81, railY - 68, 6500L, 0.47f, strength);
        smoke(g, r - 52, railY - 72, 5800L, 0.71f, strength);

        // The rear production hall and its pipe network tie the three clusters
        // together, leaving the middle of the view quiet enough for content.
        g.fill(l + 17, railY - 49, l + 121, railY - 6, SHADOW);
        g.fill(l + 20, railY - 46, l + 118, railY - 8, IRON);
        g.fill(l + 20, railY - 47, l + 118, railY - 45, STEEL);
        g.fill(l + 39, railY - 83, l + 54, railY - 44, SHADOW);
        g.fill(l + 42, railY - 80, l + 51, railY - 46, IRON);
        g.fill(l + 44, railY - 82, l + 49, railY - 80, STEEL);
        g.fill(l + 74, railY - 68, l + 89, railY - 45, SHADOW);
        g.fill(l + 77, railY - 66, l + 86, railY - 46, IRON);
        g.fill(l + 79, railY - 68, l + 84, railY - 66, STEEL);

        g.fill(l + 115, railY - 52, mid + 14, railY - 49, shade(STEEL, 74));
        g.fill(mid + 11, railY - 52, mid + 15, railY - 24, shade(STEEL, 74));
        g.fill(mid - 43, railY - 29, mid + 30, railY - 7, SHADOW);
        g.fill(mid - 40, railY - 27, mid + 27, railY - 9, IRON);
        g.fill(mid - 40, railY - 28, mid + 27, railY - 27, STEEL);

        // Loading belt, turning wheels, piston, furnace, gauge, and tiny windows.
        g.fill(l + 94, railY - 32, l + 151, railY - 27, SHADOW);
        g.fill(l + 96, railY - 31, l + 149, railY - 29, STEEL);
        float belt = cycle(3600L, 0.25f);
        for (int i = 0; i < 4; i++) {
            int oreX = l + 97 + Math.round(((belt + i * 0.25f) % 1.0f) * 48);
            g.fill(oreX, railY - 34, oreX + 4, railY - 32, ORE);
        }
        if (moving && train < 0.17f) {
            float falling = (train * 18.0f) % 1.0f;
            int oreY = railY - 31 + Math.round(falling * 10);
            g.fill(l + 143, oreY, l + 147, oreY + 3,
                    shade(ORE, Math.round((1 - falling * 0.6f) * 230)));
        }
        gear(g, l + 107, railY - 27, 7, cycle(6500L, 0.12f), false);
        gear(g, mid - 13, railY - 18, 7, cycle(6500L, 0.12f), true);
        int stroke = moving ? Math.round(4 * (float)Math.sin(cycle(3200L, 0.2f) * Math.PI * 2.0)) : 0;
        g.fill(mid + 1, railY - 19, mid + 27, railY - 13, SHADOW);
        g.fill(mid + 2, railY - 17, mid + 13 + stroke, railY - 15, STEEL);
        g.fill(mid + 11 + stroke, railY - 19, mid + 15 + stroke, railY - 12, IRON);
        g.fill(l + 28, railY - 37, l + 58, railY - 15, SHADOW);
        int glow = Math.round(95 + 58 * (0.5f + 0.5f *
                (float)Math.sin(cycle(4500L, 0.15f) * Math.PI * 2.0)));
        g.fill(l + 34, railY - 32, l + 53, railY - 21, shade(AMBER, glow));
        g.fill(l + 34, railY - 22, l + 53, railY - 20, IRON);
        g.fill(l + 26, railY - 15, l + 57, railY - 13, STEEL);
        for (int i = 0; i < 3; i++) {
            int wx = l + 67 + i * 13;
            g.fill(wx, railY - 39, wx + 7, railY - 31, SHADOW);
            g.fill(wx + 1, railY - 38, wx + 6, railY - 32,
                    shade(AMBER, i == 1 ? glow / 2 : 65));
        }
        gauge(g, mid + 20, railY - 34);

        // Sparks rise only over the furnace; they fade instead of blinking out.
        for (int i = 0; i < 3; i++) {
            float spark = (cycle(2800L + i * 600L, 0.18f) + i * 0.36f) % 1.0f;
            int sx = l + 37 + i * 6 + Math.round(2 * (float)Math.sin(spark * 5.0f));
            int sy = railY - 39 - Math.round(spark * 12);
            g.fill(sx, sy, sx + 2, sy + 2,
                    shade(AMBER, Math.round((1 - spark) * 95 * strength)));
        }

        // The crane and right loading bay are the destination of the rail.
        crane(g, r - 131, railY, moving);
        g.fill(r - 102, railY - 54, r - 18, railY - 6, SHADOW);
        g.fill(r - 99, railY - 51, r - 21, railY - 8, IRON);
        g.fill(r - 101, railY - 55, r - 20, railY - 52, STEEL);
        g.fill(r - 59, railY - 72, r - 45, railY - 53, SHADOW);
        g.fill(r - 56, railY - 70, r - 48, railY - 54, IRON);
        g.fill(r - 55, railY - 72, r - 49, railY - 70, STEEL);
        g.fill(r - 92, railY - 40, r - 41, railY - 7, SHADOW);
        for (int i = 0; i < 3; i++) {
            g.fill(r - 39 + i * 5, railY - 33, r - 37 + i * 5, railY - 18,
                    shade(AMBER, Math.round(31 + 23 * (0.5f + 0.5f *
                            (float)Math.sin(cycle(5400L, i * 0.16f) * Math.PI * 2.0)))));
        }
        int signal = moving && train >= 0.16f && train < 0.84f ? 170 : 58;
        g.fill(l + 165, railY - 36, l + 167, railY - 6, STEEL);
        g.fill(l + 161, railY - 39, l + 171, railY - 31, SHADOW);
        g.fill(l + 164, railY - 37, l + 168, railY - 33, shade(AMBER, signal));
    }

    private static void smoke(GuiGraphics g, int x, int y, long duration,
                              float offset, float strength) {
        boolean moving = GuiMotion.ambientMotionEnabled();
        for (int i = 0; i < 5; i++) {
            float age = moving ? (GuiMotion.cycle(duration) + offset + i * 0.2f) % 1.0f
                    : i * 0.17f;
            int drift = Math.round(13 * age + 3 * (float)Math.sin(age * Math.PI * 3 + offset * 6));
            int rise = Math.round(41 * age);
            int radius = 2 + Math.round(3 * age);
            int opacity = Math.round((1 - age) * (1 - age) * (moving ? 112 * strength + 33 : 38));
            int cx = x + drift;
            int cy = y - rise;
            g.fill(cx - radius, cy - 2, cx + radius, cy + 3, shade(ORE, opacity));
            g.fill(cx - radius + 1, cy - 4, cx + radius - 1, cy + 5,
                    shade(STEEL, opacity / 2));
        }
    }

    private static void tracks(GuiGraphics g, int l, int r, int y,
                               InterfaceTheme.Palette p) {
        g.fill(l - 2, y + 5, r + 2, y + 8, SHADOW);
        for (int x = l + 4; x < r - 3; x += 15) {
            g.fill(x, y - 2, x + 3, y + 8, p.border());
            g.fill(x + 1, y - 1, x + 2, y + 6, STEEL);
        }
        g.fill(l, y - 3, r, y - 1, IRON);
        g.fill(l, y - 3, r, y - 2, STEEL);
        g.fill(l, y + 3, r, y + 5, IRON);
        g.fill(l, y + 3, r, y + 4, STEEL);
    }

    private static void cart(GuiGraphics g, int x, int y, float progress,
                             float strength, float visible, float loaded) {
        int bounce = GuiMotion.ambientMotionEnabled()
                ? Math.round((float)Math.sin(progress * 38 * Math.PI) * strength) : 0;
        g.pose().pushPose();
        g.pose().translate(x, y - bounce, 0.0f);
        int opacity = Math.round(255 * visible);
        g.fill(-14, -17, 14, -15, shade(STEEL, opacity));
        g.fill(-16, -15, 16, -9, shade(IRON, opacity));
        g.fill(-13, -9, 13, -5, shade(SHADOW, opacity));
        g.fill(-11, -14, 11, -9, shade(STEEL, opacity));
        if (loaded > 0.2f) {
            g.fill(-8, -19, -2, -16, shade(ORE, opacity));
        }
        if (loaded > 0.5f) {
            g.fill(-1, -20, 7, -16, shade(ORE, opacity));
        }
        if (loaded > 0.85f) {
            g.fill(6, -18, 12, -16, shade(STEEL, opacity));
        }
        g.fill(-13, -12, -11, -9, shade(AMBER, opacity));
        g.fill(11, -12, 13, -9, shade(AMBER, opacity));
        wheel(g, -9, -3, progress * 1800, opacity);
        wheel(g, 9, -3, progress * 1800, opacity);
        g.pose().popPose();
    }

    private static void wheel(GuiGraphics g, int x, int y, float angle, int opacity) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0.0f);
        g.fill(-3, -3, 4, 4, shade(SHADOW, opacity));
        g.fill(-2, -2, 3, 3, shade(ORE, opacity));
        g.pose().mulPose(Axis.ZP.rotationDegrees(angle));
        g.fill(-1, -2, 1, 3, shade(IRON, opacity));
        g.fill(-2, -1, 3, 1, shade(IRON, opacity));
        g.pose().popPose();
    }

    private static void tunnelFront(GuiGraphics g, int x, int y) {
        g.fill(x - 3, y - 23, x + 37, y - 19, STEEL);
        g.fill(x - 3, y - 22, x + 4, y + 5, IRON);
        g.fill(x + 30, y - 22, x + 37, y + 5, IRON);
        g.fill(x, y - 19, x + 4, y + 4, SHADOW);
        g.fill(x + 30, y - 19, x + 34, y + 4, SHADOW);
        g.fill(x + 5, y - 22, x + 29, y - 21, AMBER);
        g.fill(x - 2, y - 23, x, y - 21, ORE);
        g.fill(x + 33, y - 23, x + 35, y - 21, ORE);
    }

    private static void gear(GuiGraphics g, int x, int y, int radius,
                             float phase, boolean reverse) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().mulPose(Axis.ZP.rotationDegrees((reverse ? -1 : 1) * phase * 360));
        g.fill(-radius, -radius + 2, radius + 1, radius - 1, STEEL);
        g.fill(-radius + 2, -radius, radius - 1, radius + 1, STEEL);
        g.fill(-radius + 2, -radius + 2, radius - 1, radius - 1, SHADOW);
        g.fill(-1, -radius + 1, 2, radius, IRON);
        g.fill(-radius + 1, -1, radius, 2, IRON);
        g.fill(-2, -2, 3, 3, AMBER);
        g.pose().popPose();
    }

    private static void gauge(GuiGraphics g, int x, int y) {
        g.fill(x - 6, y - 6, x + 7, y + 7, STEEL);
        g.fill(x - 4, y - 4, x + 5, y + 5, SHADOW);
        g.pose().pushPose();
        g.pose().translate(x, y, 0.0f);
        float sweep = (float)Math.sin(cycle(7300L, 0.25f) * Math.PI * 2.0);
        g.pose().mulPose(Axis.ZP.rotationDegrees(sweep * 42.0f));
        g.fill(0, -4, 1, 2, AMBER);
        g.pose().popPose();
        g.fill(x - 1, y - 1, x + 2, y + 2, ORE);
    }

    private static void crane(GuiGraphics g, int x, int y, boolean moving) {
        int mastTop = y - 73;
        g.fill(x - 2, mastTop, x + 2, y - 13, STEEL);
        g.fill(x - 5, mastTop, x + 63, mastTop + 3, STEEL);
        g.fill(x + 50, mastTop + 3, x + 52, mastTop + 8, STEEL);
        float sway = moving ? (float)Math.sin(cycle(11700L, 0.14f) * Math.PI * 2.0) : 0.0f;
        int hookX = x + 50 + Math.round(sway * 4);
        int lift = moving ? Math.round((sway + 1) * 5) : 4;
        g.fill(hookX, mastTop + 7, hookX + 1, mastTop + 23 + lift, ORE);
        g.fill(hookX - 2, mastTop + 23 + lift, hookX + 4, mastTop + 25 + lift, AMBER);
        g.fill(hookX - 2, mastTop + 25 + lift, hookX, mastTop + 29 + lift, STEEL);
    }

    private static float cycle(long duration, float still) {
        return GuiMotion.ambientMotionEnabled() ? GuiMotion.cycle(duration) : still;
    }

    private static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static float smooth(float value) {
        return value * value * (3.0f - 2.0f * value);
    }

    private static int shade(int color, int opacity) {
        return GuiMotion.alpha(color, opacity);
    }
}
