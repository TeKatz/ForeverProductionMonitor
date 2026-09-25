package de.timo.foreverproductionmonitor.client.screen;

import de.timo.foreverproductionmonitor.client.ClientConfig;

final class GuiMotion {
    private GuiMotion() {
    }

    static boolean enabled() {
        return (Boolean)ClientConfig.VALUES.guiAnimations.get();
    }

    static long now() {
        return System.nanoTime();
    }

    static float progress(long startedNanos, long durationMillis) {
        if (!enabled() || startedNanos == 0L) {
            return 1.0f;
        }
        return Math.min(1.0f, Math.max(0.0f, (float)((double)(now() - startedNanos) / (double)(durationMillis * 1000000L))));
    }

    static float easeOut(float progress) {
        float remaining = 1.0f - progress;
        return 1.0f - remaining * remaining * remaining;
    }

    static int alpha(int color, int alpha) {
        return color & 0xFFFFFF | Math.max(0, Math.min(255, alpha)) << 24;
    }
}
