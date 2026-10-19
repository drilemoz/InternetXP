package br.com.internetxp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Reordenação e busca de favoritos, sem acesso a Android ou a dados privados. */
final class BookmarkOrganizer {
    private BookmarkOrganizer() { }

    static List<String> search(List<String> items, String query) {
        ArrayList<String> result = new ArrayList<>();
        if (items == null) return result;
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        for (String value : items) {
            if (value != null && value.toLowerCase(Locale.ROOT).contains(needle)) result.add(value);
        }
        return result;
    }

    static boolean move(List<String> items, String value, int direction) {
        if (items == null || value == null || (direction != -1 && direction != 1)) return false;
        int index = items.indexOf(value);
        int target = index + direction;
        if (index < 0 || target < 0 || target >= items.size()) return false;
        Collections.swap(items, index, target);
        return true;
    }
}
