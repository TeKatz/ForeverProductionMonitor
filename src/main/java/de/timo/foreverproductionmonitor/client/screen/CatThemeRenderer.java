package de.timo.foreverproductionmonitor.client.screen;

import net.minecraft.client.gui.GuiGraphics;

/** Cat theme artwork. All character animation stays in the title bar, away from content. */
final class CatThemeRenderer {
    private static final int INK = 0xFF16282C;
    private static final int FUR = 0xFFFFD49F;
    private static final int SHADOW = 0xFFD49D70;
    private static final int CREAM = 0xFFFFF4E5;
    private static final int PINK = 0xFFFFAD96;

    private CatThemeRenderer() {
    }

    static void drawHeader(GuiGraphics g, int x, int y, int width, InterfaceTheme.Palette p) {
        // The inset and piping give this theme its own silhouette at every GUI scale.
        g.fill(x + 1, y + 1, x + width - 1, y + 3, p.accentB());
        g.fill(x + 1, y + 32, x + width - 1, y + 34, p.accentA());
        g.fill(x + 2, y + 35, x + 4, y + 53, p.accentB());
        g.fill(x + width - 4, y + 35, x + width - 2, y + 53, p.accentA());

        // Below this width the centered title needs the entire header.
        if (width < 380) return;

        float time = GuiMotion.cycle(9600L);
        int bob = GuiMotion.ambientMotionEnabled()
                ? Math.round((float)Math.sin(time * Math.PI * 2.0) * GuiMotion.ambientMotionIntensity()) : 0;
        boolean blink = GuiMotion.ambientMotionEnabled() &&
                ((time > 0.720f && time < 0.739f) || (time > 0.762f && time < 0.778f));
        int cx = x + 30;
        int cy = y + 17 + bob;

        // Soft shadow, tabby ears, cheek silhouette and a lighter muzzle.
        g.fill(cx - 18, cy - 7, cx + 19, cy + 13, 0x70302C2B);
        g.fill(cx - 13, cy - 13, cx - 4, cy - 6, FUR);
        g.fill(cx + 5, cy - 13, cx + 14, cy - 6, FUR);
        int earFlick = GuiMotion.ambientMotionEnabled() && time > 0.43f && time < 0.47f ? 1 : 0;
        g.fill(cx - 11, cy - 10 - earFlick, cx - 6, cy - 5, PINK);
        g.fill(cx + 7, cy - 10, cx + 12, cy - 5, PINK);
        g.fill(cx - 16, cy - 5, cx + 17, cy + 10, FUR);
        g.fill(cx - 13, cy + 9, cx + 14, cy + 12, SHADOW);
        g.fill(cx - 9, cy - 5, cx - 5, cy - 3, SHADOW);
        g.fill(cx - 2, cy - 6, cx + 3, cy - 4, SHADOW);
        g.fill(cx + 6, cy - 5, cx + 10, cy - 3, SHADOW);
        g.fill(cx - 8, cy + 5, cx + 9, cy + 10, CREAM);
        int blush = GuiMotion.ambientMotionEnabled()
                ? 56 + Math.round((1.0f + (float)Math.sin(GuiMotion.cycle(4400L) * Math.PI * 2.0)) * 14.0f)
                : 65;
        g.fill(cx - 13, cy + 4, cx - 10, cy + 7, GuiMotion.alpha(PINK, blush));
        g.fill(cx + 11, cy + 4, cx + 14, cy + 7, GuiMotion.alpha(PINK, blush));

        if (blink) {
            g.fill(cx - 10, cy + 1, cx - 5, cy + 2, INK);
            g.fill(cx + 6, cy + 1, cx + 11, cy + 2, INK);
        } else {
            g.fill(cx - 9, cy - 1, cx - 6, cy + 4, INK);
            g.fill(cx + 7, cy - 1, cx + 10, cy + 4, INK);
            g.fill(cx - 8, cy, cx - 7, cy + 1, CREAM);
            g.fill(cx + 8, cy, cx + 9, cy + 1, CREAM);
        }
        g.fill(cx - 1, cy + 4, cx + 2, cy + 6, PINK);
        g.fill(cx, cy + 6, cx + 1, cy + 8, INK);
        g.fill(cx - 4, cy + 7, cx - 1, cy + 8, INK);
        g.fill(cx + 2, cy + 7, cx + 5, cy + 8, INK);
        g.fill(cx - 20, cy + 4, cx - 11, cy + 5, CREAM);
        g.fill(cx - 21, cy + 7, cx - 11, cy + 8, CREAM);
        g.fill(cx + 12, cy + 4, cx + 21, cy + 5, CREAM);
        g.fill(cx + 12, cy + 7, cx + 22, cy + 8, CREAM);

        // The yarn follows its own slow, eased orbit. A short thread ties it to the cat.
        int yarnX = x + 67;
        int yarnY = y + 18;
        if (GuiMotion.ambientMotionEnabled()) {
            float orbit = GuiMotion.cycle(7200L);
            yarnX += Math.round((float)Math.sin(orbit * Math.PI * 2.0) * 3.0f);
            yarnY += Math.round((float)Math.cos(orbit * Math.PI * 2.0) * 2.0f);
        }
        g.fill(cx + 17, cy + 9, yarnX - 3, cy + 10, SHADOW);
        g.fill(yarnX - 4, yarnY - 4, yarnX + 5, yarnY + 5, PINK);
        g.fill(yarnX - 2, yarnY - 5, yarnX + 3, yarnY + 6, PINK);
        g.fill(yarnX - 3, yarnY, yarnX + 3, yarnY + 1, CREAM);
        g.fill(yarnX - 1, yarnY - 3, yarnX + 1, yarnY + 4, CREAM);
        g.fill(yarnX + 2, yarnY + 3, yarnX + 5, yarnY + 4, SHADOW);

        // A restrained glint travels only along the header piping.
        if (GuiMotion.ambientMotionEnabled() && width > 240) {
            int span = width - 190;
            float glide = GuiMotion.cycle(12000L);
            int glintX = x + 95 + Math.round((0.5f - 0.5f * (float)Math.cos(glide * Math.PI * 2.0)) * span);
            g.fill(glintX, y + 2, glintX + 18, y + 3, GuiMotion.alpha(CREAM, 120));
        }
    }

    static void drawBody(GuiGraphics g, int left, int top, int right, int bottom,
                         InterfaceTheme.Palette p) {
        // Stitching is confined to the narrow frame gutters; no marks enter data rows.
        if (right - left < 120 || bottom - top < 64) return;
        int stitch = GuiMotion.alpha(p.accentB(), 72);
        for (int y = top + 8; y < bottom - 18; y += 18) {
            g.fill(left + 3, y, left + 5, y + 2, stitch);
            g.fill(right - 5, y + 8, right - 3, y + 10, stitch);
        }
        g.fill(left + 8, bottom - 4, right - 8, bottom - 3, GuiMotion.alpha(p.accentA(), 55));
    }
}
