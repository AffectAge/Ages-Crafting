package com.agescrafting.agescrafting.compat.jade;

import java.util.Locale;

public final class JadeTimeFormat {
    private JadeTimeFormat() {
    }

    public static String formatRemainingTicks(int progressTicks, int totalTicks) {
        int clampedTotal = Math.max(1, totalTicks);
        int clampedProgress = Math.max(0, Math.min(progressTicks, clampedTotal));
        int remainingTicks = Math.max(0, clampedTotal - clampedProgress);
        int remainingSeconds = (remainingTicks + 19) / 20;
        return formatClock(remainingSeconds);
    }

    private static String formatClock(int totalSeconds) {
        int seconds = Math.max(0, totalSeconds);
        int minutes = seconds / 60;
        int remainder = seconds % 60;
        return String.format(Locale.ROOT, "%02d:%02d", minutes, remainder);
    }
}
