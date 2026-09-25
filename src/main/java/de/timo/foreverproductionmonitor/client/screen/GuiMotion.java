package de.timo.foreverproductionmonitor.client.screen;

import de.timo.foreverproductionmonitor.client.ClientConfig;

final class GuiMotion {
    private GuiMotion() {
    }

    static boolean enabled() {
        return (Boolean)ClientConfig.VALUES.guiAnimations.get();
    }

    static boolean updatePulseEnabled() {
        return enabled() && (Boolean)ClientConfig.VALUES.updatePulseEnabled.get();
    }

    static float updatePulseIntensity() {
        return updatePulseEnabled() ? ((Double)ClientConfig.VALUES.updatePulseIntensity.get()).floatValue() : 0.0f;
    }

    static long updatePulseDurationMillis() {
        return Math.round((Double)ClientConfig.VALUES.updatePulseDuration.get() * 1000.0);
    }

    static boolean ambientMotionEnabled() {
        return enabled() && (Boolean)ClientConfig.VALUES.ambientMotionEnabled.get() && (Double)ClientConfig.VALUES.ambientMotionIntensity.get() > 0.0;
    }

    static float ambientMotionIntensity() {
        return ambientMotionEnabled() ? ((Double)ClientConfig.VALUES.ambientMotionIntensity.get()).floatValue() : 0.0f;
    }

    static float cycle(long durationMillis) {
        if (!enabled() || durationMillis <= 0L) {
            return 0.0f;
        }
        long durationNanos = durationMillis * 1000000L;
        return (float)((double)Math.floorMod(now(), durationNanos) / (double)durationNanos);
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
