package de.timo.foreverproductionmonitor.client.screen;

import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;

/** Two hand-drawn cats and frame-only motion for the Cats interface theme. */
final class CatThemeRenderer {
    private static final int INK = 0xFF16282C;
    private static final int FUR = 0xFFFFD49F;
    private static final int FUR_SHADE = 0xFFD49D70;
    private static final int CREAM = 0xFFFFF4E5;
    private static final int PINK = 0xFFFFAD96;
    private static final int SLEEP_FUR = 0xFFC7DCD0;
    private static final int SLEEP_SHADE = 0xFF779E9B;

    private CatThemeRenderer() {
    }

    static void drawHeader(GuiGraphics g, int x, int y, int width, InterfaceTheme.Palette p) {
        // Layered piping makes the panel recognizable even when motion is switched off.
        g.fill(x + 1, y + 1, x + width - 1, y + 2, p.accentB());
        g.fill(x + 3, y + 3, x + width - 3, y + 4, GuiMotion.alpha(CREAM, 42));
        g.fill(x + 1, y + 32, x + width - 1, y + 34, p.accentA());
        g.fill(x + 2, y + 35, x + 4, y + 53, p.accentB());
        g.fill(x + width - 4, y + 35, x + width - 2, y + 53, p.accentA());

        if (width >= 380) {
            badge(g, x + 5, y + 5, 53, 25, 0xFF31565A, p.accentB());
            drawAwakeCat(g, x + 30, y + 17);
        }
        // Keep the sleeping cat clear of both the centered title and the settings gear.
        if (width >= 560) {
            badge(g, x + width - 131, y + 5, 86, 25, 0xFF31565A, p.accentA());
            drawSleepingCat(g, x + width - 80, y + 17);
        }
        if (GuiMotion.ambientMotionEnabled() && width >= 300) {
            float travel = smooth((1.0f - (float)Math.cos(GuiMotion.cycle(13000L)
                    * Math.PI * 2.0)) * 0.5f);
            int track = width - (width >= 560 ? 250 : 115);
            int glintX = x + 82 + Math.round(track * travel);
            g.fill(glintX - 9, y + 2, glintX + 9, y + 3,
                    GuiMotion.alpha(CREAM, Math.round(90 * motionStrength())));
        }
    }

    private static void badge(GuiGraphics g, int x, int y, int width, int height,
                              int background, int edge) {
        // Stepped corners echo a soft cushion without introducing textures or blur.
        g.fill(x + 3, y, x + width - 3, y + height, edge);
        g.fill(x, y + 3, x + width, y + height - 3, edge);
        g.fill(x + 4, y + 1, x + width - 4, y + height - 1, background);
        g.fill(x + 1, y + 4, x + width - 1, y + height - 4, background);
        g.fill(x + 5, y + 3, x + width - 5, y + 4,
                GuiMotion.alpha(CREAM, 38));
        g.fill(x + 5, y + height - 4, x + width - 5, y + height - 3,
                GuiMotion.alpha(CREAM, 32));
    }

    private static void drawAwakeCat(GuiGraphics g, int x, int y) {
        boolean animated = GuiMotion.ambientMotionEnabled();
        float intensity = motionStrength();
        float cycle = animated ? GuiMotion.cycle(9600L) : 0.25f;
        float breath = (float)Math.sin(cycle * Math.PI * 2.0);
        float blink = animated ? Math.max(window(cycle, 0.60f, 0.627f, 0.651f),
                window(cycle, 0.693f, 0.708f, 0.725f)) : 0.0f;
        float ear = animated ? window(cycle, 0.30f, 0.333f, 0.37f) * intensity : 0.0f;
        float yawn = animated ? window(GuiMotion.cycle(17300L), 0.54f, 0.60f, 0.68f)
                * intensity : 0.0f;

        g.pose().pushPose();
        g.pose().translate(x, y + breath * 3.0f * intensity, 0.0f);
        g.pose().mulPose(Axis.ZP.rotationDegrees(breath * 3.2f * intensity));
        // Tapered cheeks, layered fur and cream muzzle give the face a clear silhouette.
        g.fill(-15, -7, 16, 9, FUR_SHADE);
        g.fill(-13, -12 - Math.round(ear * 2.0f), -4, -4, FUR);
        g.fill(5, -12, 14, -4, FUR);
        g.fill(-11, -10 - Math.round(ear * 2.0f), -6, -5, PINK);
        g.fill(7, -10, 12, -5, PINK);
        g.fill(-17, -3, 18, 7, FUR);
        g.fill(-12, 7, 13, 11, FUR_SHADE);
        g.fill(-8, -7, -5, -5, FUR_SHADE);
        g.fill(-2, -8, 3, -5, FUR_SHADE);
        g.fill(6, -7, 9, -5, FUR_SHADE);
        g.fill(-9, 4, 10, 9, CREAM);
        g.fill(-5, 8, 6, 10, CREAM);

        int eyeHeight = Math.max(1, Math.round(4.0f * (1.0f - blink)));
        int gaze = Math.round(window(cycle, 0.36f, 0.46f, 0.56f) * intensity);
        g.fill(-9 + gaze, 2 - eyeHeight, -6 + gaze, 2, INK);
        g.fill(7 + gaze, 2 - eyeHeight, 10 + gaze, 2, INK);
        if (eyeHeight >= 3) {
            g.fill(-8 + gaze, -1, -7 + gaze, 0, CREAM);
            g.fill(8 + gaze, -1, 9 + gaze, 0, CREAM);
        }
        int blush = Math.round(50 + 30 * (0.5f + 0.5f * breath));
        g.fill(-13, 3, -10, 6, GuiMotion.alpha(PINK, blush));
        g.fill(11, 3, 14, 6, GuiMotion.alpha(PINK, blush));
        g.fill(-1, 3, 2, 5, PINK);
        if (yawn > 0.05f) {
            g.fill(-2, 6, 3, 7 + Math.round(yawn * 3.0f), INK);
            g.fill(-1, 7, 2, 8 + Math.round(yawn * 2.0f), PINK);
        } else {
            g.fill(0, 5, 1, 7, INK);
            g.fill(-4, 7, -1, 8, INK);
            g.fill(2, 7, 5, 8, INK);
        }
        // Whiskers stay with the head throughout its rotation.
        g.fill(-21, 3, -11, 4, CREAM);
        g.fill(-22, 6, -11, 7, CREAM);
        g.fill(12, 3, 22, 4, CREAM);
        g.fill(12, 6, 23, 7, CREAM);
        g.pose().popPose();
    }

    private static void drawSleepingCat(GuiGraphics g, int x, int y) {
        boolean animated = GuiMotion.ambientMotionEnabled();
        float intensity = motionStrength();
        float breath = animated ? (float)Math.sin(GuiMotion.cycle(4800L) * Math.PI * 2.0) : 0.0f;
        float tail = animated ? GuiMotion.cycle(8100L) : 0.25f;
        float paws = animated ? GuiMotion.cycle(2800L) : 0.25f;

        // The body expands continuously around its center rather than hopping pixels.
        g.pose().pushPose();
        g.pose().translate(x, y + 2.0f, 0.0f);
        g.pose().scale(1.0f + breath * 0.035f * intensity,
                1.0f + breath * 0.13f * intensity, 1.0f);
        g.fill(-27, -7, 12, 10, SLEEP_SHADE);
        g.fill(-22, -10, 7, 8, SLEEP_FUR);
        g.fill(-17, -10, -13, 4, SLEEP_SHADE);
        g.fill(-6, -9, -2, 5, SLEEP_SHADE);
        g.fill(5, -5, 16, 10, SLEEP_FUR);
        g.pose().popPose();

        // A soft curved tail moves independently of the breathing body.
        g.pose().pushPose();
        g.pose().translate(x, y, 0.0f);
        for (int i = 0; i < 12; ++i) {
            float along = i / 11.0f;
            float sweep = (float)Math.sin(tail * Math.PI * 2.0) * 5.0f * along * intensity;
            float px = -22.0f - 23.0f * along;
            float py = 7.0f - 11.0f * (float)Math.sin(along * Math.PI) + sweep;
            g.pose().pushPose();
            g.pose().translate(px, py, 0.0f);
            g.fill(-2, -2, 2, 2, i % 4 == 0 ? SLEEP_SHADE : SLEEP_FUR);
            g.pose().popPose();
        }
        g.pose().popPose();

        // Head rests on the body; two paws knead in alternating, smooth phases.
        g.pose().pushPose();
        g.pose().translate(x + 12, y + 2 + breath * 0.35f * intensity, 0.0f);
        g.fill(-8, -8, 10, 7, SLEEP_SHADE);
        g.fill(-6, -11, -1, -6, SLEEP_FUR);
        g.fill(5, -11, 10, -6, SLEEP_FUR);
        g.fill(-5, -9, -2, -6, PINK);
        g.fill(6, -9, 9, -6, PINK);
        g.fill(-8, -5, 9, 5, SLEEP_FUR);
        g.fill(-5, 0, -1, 1, INK);
        g.fill(4, 0, 8, 1, INK);
        g.fill(1, 2, 3, 4, PINK);
        for (int i = 0; i < 2; ++i) {
            float lift = animated ? (float)Math.sin(paws * Math.PI * 2.0 + i * Math.PI)
                    * 3.0f * intensity : 0.0f;
            g.pose().pushPose();
            g.pose().translate(-4 + i * 7, 6 + lift, 0.0f);
            g.fill(-3, 0, 4, 4, SLEEP_FUR);
            g.fill(-1, 2, 1, 3, PINK);
            g.pose().popPose();
        }
        g.pose().popPose();

        // Little dream signs rise and fade in the otherwise free corner of the header.
        if (animated) {
            for (int i = 0; i < 2; ++i) {
                float progress = (GuiMotion.cycle(5700L) + i * 0.5f) % 1.0f;
                int opacity = Math.round((float)Math.sin(progress * Math.PI) * 125.0f * intensity);
                g.pose().pushPose();
                g.pose().translate(x + 27 + i * 7 + (float)Math.sin(progress * Math.PI * 2.0)
                                * 1.5f * intensity, y - 1 - progress * 9.0f * intensity, 0.0f);
                int color = GuiMotion.alpha(CREAM, opacity);
                g.fill(0, 0, 5, 1, color);
                g.fill(3, 1, 4, 3, color);
                g.fill(0, 3, 5, 4, color);
                g.pose().popPose();
            }
        }
    }

    static void drawBody(GuiGraphics g, int left, int top, int right, int bottom,
                         InterfaceTheme.Palette p) {
        if (right - left < 120 || bottom - top < 64) return;
        int height = bottom - top - 24;
        float walk = GuiMotion.ambientMotionEnabled() ? GuiMotion.cycle(19000L) : 0.15f;
        float intensity = motionStrength();
        for (int i = 0; i < 5; ++i) {
            float progress = (walk + i * 0.2f) % 1.0f;
            int opacity = Math.round((45 + 55 * (float)Math.sin(progress * Math.PI))
                    * (GuiMotion.ambientMotionEnabled() ? intensity : 0.65f));
            int color = GuiMotion.alpha(i % 2 == 0 ? p.accentB() : p.accentA(), opacity);
            g.pose().pushPose();
            g.pose().translate(i % 2 == 0 ? left + 7 : right - 7,
                    top + 10 + height * progress, 0.0f);
            paw(g, color);
            g.pose().popPose();
        }
        g.fill(left + 8, bottom - 4, right - 8, bottom - 3,
                GuiMotion.alpha(p.accentA(), 68));
        if (GuiMotion.ambientMotionEnabled()) {
            int span = right - left - 42;
            int cx = left + 18 + Math.round(span * smooth((1.0f - (float)Math.cos(
                    GuiMotion.cycle(16000L) * Math.PI * 2.0)) * 0.5f));
            g.fill(cx - 8, bottom - 4, cx + 8, bottom - 3,
                    GuiMotion.alpha(CREAM, Math.round(90 * intensity)));
        }
    }

    private static void paw(GuiGraphics g, int color) {
        g.fill(-2, 0, 3, 3, color);
        g.fill(-4, -3, -2, -1, color);
        g.fill(-1, -4, 1, -2, color);
        g.fill(2, -3, 4, -1, color);
    }

    private static float smooth(float value) {
        float v = Math.max(0.0f, Math.min(1.0f, value));
        return v * v * (3.0f - 2.0f * v);
    }

    private static float motionStrength() {
        return (float)Math.sqrt(GuiMotion.ambientMotionIntensity());
    }

    private static float window(float time, float start, float peak, float end) {
        if (time <= start || time >= end) return 0.0f;
        return time < peak ? smooth((time - start) / (peak - start))
                : smooth((end - time) / (end - peak));
    }
}
