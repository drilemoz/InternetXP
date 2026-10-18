package br.com.internetxp;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Histórico local: títulos, endereços e horários reais, com migração sem datas fictícias. */
final class HistoryRecords {
    static final int LIMIT = 200;

    static final class Entry {
        final String url;
        final String title;
        final long visitedAt;

        Entry(String url, String title, long visitedAt) {
            this.url = url;
            this.title = title == null ? "" : title;
            this.visitedAt = Math.max(0, visitedAt);
        }
    }

    private HistoryRecords() { }

    static void visited(List<Entry> entries, String url, String title, long timeMillis) {
        if (!TabSessionStore.restorable(url) || "about:blank".equals(url)) return;
        String lastTitle = title == null ? "" : title;
        for (int i = 0; i < entries.size(); i++) {
            if (url.equals(entries.get(i).url)) {
                if (lastTitle.isEmpty()) lastTitle = entries.get(i).title;
                entries.remove(i);
                break;
            }
        }
        entries.add(0, new Entry(url, lastTitle, timeMillis));
        while (entries.size() > LIMIT) entries.remove(entries.size() - 1);
    }

    static void updateTitle(List<Entry> entries, String url, String title) {
        if (title == null || title.trim().isEmpty()) return;
        for (int i = 0; i < entries.size(); i++) {
            Entry old = entries.get(i);
            if (old.url.equals(url)) {
                entries.set(i, new Entry(url, title, old.visitedAt));
                return;
            }
        }
    }

    static ArrayList<Entry> filter(List<Entry> entries, String query, int max) {
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        ArrayList<Entry> filtered = new ArrayList<>();
        if (max <= 0) return filtered;
        for (Entry entry : entries) {
            if (normalized.isEmpty() || entry.url.toLowerCase(Locale.ROOT).contains(normalized)
                    || entry.title.toLowerCase(Locale.ROOT).contains(normalized)) {
                filtered.add(entry);
                if (filtered.size() == max) break;
            }
        }
        return filtered;
    }
}
