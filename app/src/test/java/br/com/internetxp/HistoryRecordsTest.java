package br.com.internetxp;

import org.junit.Test;
import java.util.ArrayList;
import static org.junit.Assert.*;

public final class HistoryRecordsTest {
    @Test public void deduplicatesVisitsKeepingMostRecentFirst() {
        ArrayList<HistoryRecords.Entry> list = new ArrayList<>();
        HistoryRecords.visited(list, "https://a.org", "Primeiro", 100);
        HistoryRecords.visited(list, "https://b.org", "Outro", 200);
        HistoryRecords.visited(list, "https://a.org", "Atualizado", 300);
        assertEquals(2, list.size());
        assertEquals("https://a.org", list.get(0).url);
        assertEquals(300, list.get(0).visitedAt);
        assertEquals("Atualizado", list.get(0).title);
    }
    @Test public void titleUpdateDoesNotInventVisitTime() {
        ArrayList<HistoryRecords.Entry> list = new ArrayList<>();
        HistoryRecords.visited(list, "https://a.org", "", 0);
        HistoryRecords.updateTitle(list, "https://a.org", "Página antiga");
        assertEquals(0, list.get(0).visitedAt);
        assertEquals("Página antiga", list.get(0).title);
    }
    @Test public void searchTitleAndUrlIgnoringCase() {
        ArrayList<HistoryRecords.Entry> list = new ArrayList<>();
        HistoryRecords.visited(list, "https://mozilla.org", "Firefox", 100);
        HistoryRecords.visited(list, "https://example.com", "Site XP", 200);
        assertEquals("https://mozilla.org", HistoryRecords.filter(list, "firefox", 8).get(0).url);
        assertEquals("https://example.com", HistoryRecords.filter(list, "EXAMPLE", 8).get(0).url);
    }
    @Test public void rejectsInternalUrlsAndRespectsLimit() {
        ArrayList<HistoryRecords.Entry> list = new ArrayList<>();
        HistoryRecords.visited(list, "javascript:alert(1)", "x", 10);
        assertTrue(list.isEmpty());
        for (int i = 0; i < 230; i++) HistoryRecords.visited(list, "https://site" + i + ".org", "", i);
        assertEquals(HistoryRecords.LIMIT, list.size());
        assertEquals("https://site229.org", list.get(0).url);
    }
}
