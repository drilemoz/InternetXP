package br.com.internetxp;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

/** Suggestion ranking independent from Android UI and GeckoView. */
final class HistorySuggestions {
    private HistorySuggestions() { }

    static ArrayList<String> filter(List<String> history, List<String> bookmarks, String query, int maxItems) {
        if (maxItems < 1) throw new IllegalArgumentException("maxItems");
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        ArrayList<String> prefixes = new ArrayList<>();
        ArrayList<String> partials = new ArrayList<>();
        append(history, needle, seen, prefixes, partials);
        append(bookmarks, needle, seen, prefixes, partials);
        ArrayList<String> result = new ArrayList<>(Math.min(maxItems, prefixes.size() + partials.size()));
        for (String item : prefixes) if (result.size() < maxItems) result.add(item);
        for (String item : partials) if (result.size() < maxItems) result.add(item);
        return result;
    }

    private static void append(List<String> source, String needle, LinkedHashSet<String> seen,
                               ArrayList<String> prefixes, ArrayList<String> partials) {
        if (source == null) return;
        for (String url : source) {
            if (url == null || url.trim().isEmpty() || !seen.add(url)) continue;
            String lower = url.toLowerCase(Locale.ROOT);
            String withoutScheme = lower.replaceFirst("^https?://", "");
            if (needle.isEmpty() || lower.startsWith(needle) || withoutScheme.startsWith(needle)) {
                prefixes.add(url);
            } else if (lower.contains(needle)) {
                partials.add(url);
            }
        }
    }
}
