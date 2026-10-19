package br.com.internetxp;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BookmarkOrganizerTest {
    @Test public void keepsOrderWhenFiltering() {
        List<String> items = Arrays.asList("https://example.com", "https://globo.com", "https://m.globo.com");
        assertEquals(Arrays.asList("https://globo.com", "https://m.globo.com"), BookmarkOrganizer.search(items, "GLOBO"));
        assertEquals(items, BookmarkOrganizer.search(items, ""));
        assertTrue(BookmarkOrganizer.search(items, "not-here").isEmpty());
    }

    @Test public void reordersWithoutDroppingFavorites() {
        List<String> items = new ArrayList<>(Arrays.asList("A", "B", "C"));
        assertTrue(BookmarkOrganizer.move(items, "C", -1));
        assertEquals(Arrays.asList("A", "C", "B"), items);
        assertTrue(BookmarkOrganizer.move(items, "C", 1));
        assertEquals(Arrays.asList("A", "B", "C"), items);
        assertFalse(BookmarkOrganizer.move(items, "A", -1));
        assertFalse(BookmarkOrganizer.move(items, "missing", 1));
        assertFalse(BookmarkOrganizer.move(items, "A", 0));
    }
    @Test public void handlesNullInputs() {
        assertTrue(BookmarkOrganizer.search(null, "x").isEmpty());
        assertFalse(BookmarkOrganizer.move(null, "a", 1));
    }
}
