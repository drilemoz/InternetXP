package br.com.internetxp;

import java.util.Locale;

/** Pure presentation calculations. A download is never assumed to have a known size or speed. */
final class DownloadDisplay {
    private DownloadDisplay() { }

    static long percent(long downloaded, long total) {
        if (total <= 0) return -1;
        return Math.max(0, Math.min(100, (long) ((Math.min(downloaded, total) / (double) total) * 100)));
    }

    static String bytes(long value) {
        if (value < 0) return "desconhecido";
        if (value < 1024) return value + " B";
        if (value < 1048576) return (value / 1024) + " KB";
        if (value < 1073741824) return String.format(Locale.US, "%.1f MB", value / 1048576.0);
        return String.format(Locale.US, "%.2f GB", value / 1073741824.0);
    }

    /** Based only on actual new bytes between two monotonic clock samples. */
    static long speed(long previousBytes, long currentBytes, long elapsedMs) {
        if (previousBytes < 0 || currentBytes <= previousBytes || elapsedMs < 500) return -1;
        return (long) ((currentBytes - previousBytes) * 1000.0 / elapsedMs);
    }

    static long remainingSeconds(long currentBytes, long totalBytes, long bytesPerSecond) {
        if (totalBytes <= 0 || currentBytes < 0 || bytesPerSecond <= 0) return -1;
        if (currentBytes >= totalBytes) return 0;
        return (long) Math.ceil((totalBytes - currentBytes) / (double) bytesPerSecond);
    }

    static String duration(long seconds) {
        if (seconds < 0) return "indisponível";
        if (seconds < 60) return seconds + "s";
        if (seconds < 3600) return (seconds / 60) + "min";
        if (seconds < 86400) return (seconds / 3600) + "h " + ((seconds % 3600) / 60) + "min";
        return (seconds / 86400) + "d";
    }
}
