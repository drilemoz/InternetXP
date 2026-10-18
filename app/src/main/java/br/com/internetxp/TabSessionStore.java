package br.com.internetxp;

import java.util.Locale;

/** Regras de restauração de URLs sem executar esquemas internos ou maliciosos. */
final class TabSessionStore {
    static final int MAX_TABS = 8;
    private TabSessionStore() { }

    static boolean restorable(String value) {
        if (value == null || value.length() > 4096 || value.indexOf('\\') >= 0) return false;
        if (value.equals("about:blank")) return true;
        if (!value.equals(value.trim())) return false;
        for (int i = 0; i < value.length(); i++) {
            if (Character.isWhitespace(value.charAt(i)) || Character.isISOControl(value.charAt(i))) return false;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        if (!lower.startsWith("https://") && !lower.startsWith("http://")) return false;
        int start = lower.startsWith("https://") ? 8 : 7;
        return value.length() > start && value.charAt(start) != '/';
    }

    static int activeIndex(int requested, int size) {
        return size < 1 ? -1 : Math.max(0, Math.min(requested, size - 1));
    }

    static String safeTitle(String title) {
        if (title == null || title.trim().isEmpty()) return "Nova aba";
        String trimmed = title.trim();
        return trimmed.length() <= 120 ? trimmed : trimmed.substring(0, 120);
    }
}
