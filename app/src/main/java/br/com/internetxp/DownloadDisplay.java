package br.com.internetxp;

import java.util.Locale;

/** Pure presentation helpers for download status; safe from division by zero. */
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
        return String.format(Locale.US, "%.1f MB", value / 1048576.0);
    }
}
