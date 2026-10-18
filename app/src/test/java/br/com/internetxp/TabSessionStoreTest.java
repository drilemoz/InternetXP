package br.com.internetxp;

import org.junit.Test;
import static org.junit.Assert.*;

public final class TabSessionStoreTest {
    @Test public void permitsWebPagesAndBlank() {
        assertTrue(TabSessionStore.restorable("https://mozilla.org"));
        assertTrue(TabSessionStore.restorable("http://example.com/p?q=ok"));
        assertTrue(TabSessionStore.restorable("about:blank"));
    }
    @Test public void rejectsUnsafeOrCorruptedUrls() {
        assertFalse(TabSessionStore.restorable("javascript:alert(1)"));
        assertFalse(TabSessionStore.restorable("file:///sdcard/test"));
        assertFalse(TabSessionStore.restorable("https://foo.com\\evil"));
        assertFalse(TabSessionStore.restorable("https://foo.com\n"));
        assertFalse(TabSessionStore.restorable("https:///"));
    }
    @Test public void clampsSelectedIndex() {
        assertEquals(-1, TabSessionStore.activeIndex(9, 0));
        assertEquals(0, TabSessionStore.activeIndex(-3, 4));
        assertEquals(3, TabSessionStore.activeIndex(10, 4));
        assertEquals(2, TabSessionStore.activeIndex(2, 4));
    }
    @Test public void boundsTitleLength() {
        assertEquals("Nova aba", TabSessionStore.safeTitle("  "));
        assertEquals(120, TabSessionStore.safeTitle("x".repeat(200)).length());
    }
}
