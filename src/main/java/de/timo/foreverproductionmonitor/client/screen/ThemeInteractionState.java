package de.timo.foreverproductionmonitor.client.screen;

import de.timo.foreverproductionmonitor.client.ClientConfig;

/**
 * Client-only transient interaction state for decorative interface themes.
 * It intentionally stores no world/config data and is rebuilt every rendered frame.
 */
final class ThemeInteractionState {
    private static final int MAX_HITS = 32;
    private static final ClientConfig.InterfaceStyle[] HIT_STYLE = new ClientConfig.InterfaceStyle[MAX_HITS];
    private static final int[] HIT_ID = new int[MAX_HITS];
    private static final int[] HIT_LEFT = new int[MAX_HITS];
    private static final int[] HIT_TOP = new int[MAX_HITS];
    private static final int[] HIT_RIGHT = new int[MAX_HITS];
    private static final int[] HIT_BOTTOM = new int[MAX_HITS];

    private static int hitCount;
    private static int pointerX = Integer.MIN_VALUE;
    private static int pointerY = Integer.MIN_VALUE;

    private static ClientConfig.InterfaceStyle clickedStyle;
    private static int clickedId = -1;
    private static long clickedNanos;

    private ThemeInteractionState() {
    }

    static void beginFrame(int mouseX, int mouseY) {
        pointerX = mouseX;
        pointerY = mouseY;
        hitCount = 0;
        CatThemeRenderer.beginFrame(mouseX, mouseY);
    }

    static void registerHit(ClientConfig.InterfaceStyle style, int id,
                            int left, int top, int right, int bottom) {
        if (hitCount >= MAX_HITS || right <= left || bottom <= top) {
            return;
        }
        HIT_STYLE[hitCount] = style;
        HIT_ID[hitCount] = id;
        HIT_LEFT[hitCount] = left;
        HIT_TOP[hitCount] = top;
        HIT_RIGHT[hitCount] = right;
        HIT_BOTTOM[hitCount] = bottom;
        ++hitCount;
    }

    static boolean isHovered(int left, int top, int right, int bottom) {
        return pointerX >= left && pointerX < right && pointerY >= top && pointerY < bottom;
    }

    static float hoverFactor(int centerX, int centerY, float radius) {
        if (pointerX == Integer.MIN_VALUE || pointerY == Integer.MIN_VALUE || radius <= 0.0f) {
            return 0.0f;
        }
        double dx = pointerX - centerX;
        double dy = pointerY - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return Math.max(0.0f, Math.min(1.0f, 1.0f - (float)(distance / radius)));
    }

    static int parallaxX(int left, int right, int maxOffset) {
        if (pointerX == Integer.MIN_VALUE || right <= left || maxOffset == 0) {
            return 0;
        }
        float center = (left + right) * 0.5f;
        float half = Math.max(1.0f, (right - left) * 0.5f);
        float normalized = Math.max(-1.0f, Math.min(1.0f, (pointerX - center) / half));
        return Math.round(normalized * maxOffset);
    }

    static int parallaxY(int top, int bottom, int maxOffset) {
        if (pointerY == Integer.MIN_VALUE || bottom <= top || maxOffset == 0) {
            return 0;
        }
        float center = (top + bottom) * 0.5f;
        float half = Math.max(1.0f, (bottom - top) * 0.5f);
        float normalized = Math.max(-1.0f, Math.min(1.0f, (pointerY - center) / half));
        return Math.round(normalized * maxOffset);
    }

    static boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (CatThemeRenderer.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button != 0) {
            return false;
        }

        ClientConfig.InterfaceStyle current =
                (ClientConfig.InterfaceStyle)ClientConfig.VALUES.interfaceStyle.get();
        for (int i = hitCount - 1; i >= 0; --i) {
            if (HIT_STYLE[i] == current
                    && mouseX >= HIT_LEFT[i] && mouseX < HIT_RIGHT[i]
                    && mouseY >= HIT_TOP[i] && mouseY < HIT_BOTTOM[i]) {
                clickedStyle = current;
                clickedId = HIT_ID[i];
                clickedNanos = GuiMotion.now();
                // Decorative interactions must never steal clicks from real controls
                // that happen to overlap the animated background.
                return false;
            }
        }
        return false;
    }

    static float clickProgress(ClientConfig.InterfaceStyle style, int id, long durationMillis) {
        if (clickedStyle != style || clickedId != id || clickedNanos == 0L || durationMillis <= 0L) {
            return 0.0f;
        }
        long elapsed = Math.max(0L, GuiMotion.now() - clickedNanos);
        float progress = Math.min(1.0f, (float)((double)elapsed / (durationMillis * 1_000_000.0)));
        if (progress >= 1.0f) {
            clickedStyle = null;
            clickedId = -1;
            clickedNanos = 0L;
            return 0.0f;
        }
        return progress;
    }
}
