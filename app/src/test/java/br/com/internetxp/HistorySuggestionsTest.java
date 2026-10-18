package br.com.internetxp;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;

public class HistorySuggestionsTest {
    @Test public void hostPrefixWithoutScheme() {
        assertEquals("https://globo.com/a", HistorySuggestions.filter(
            Arrays.asList("https://example.org", "https://globo.com/a"), null, "glo", 8).get(0));
    }
    @Test public void historyTakesPriorityAndNoDuplicates() {
        assertEquals(Arrays.asList("https://a.com", "https://b.com"), HistorySuggestions.filter(
            Arrays.asList("https://a.com"), Arrays.asList("https://a.com", "https://b.com"), "", 10));
    }
    @Test public void maxResults() {
        assertEquals(1, HistorySuggestions.filter(Arrays.asList("a", "b", "c"), null, "", 1).size());
    }
    @Test public void matchesMiddleCaseInsensitive() {
        assertEquals(Collections.singletonList("https://site.com/Pesquisa"), HistorySuggestions.filter(
            Arrays.asList("https://site.com/Pesquisa"), null, "PESQ", 3));
    }
    @Test public void handlesEmpty() {
        assertTrue(HistorySuggestions.filter(null, null, "", 10).isEmpty());
    }
    @Test(expected=IllegalArgumentException.class) public void invalidLimit() {
        HistorySuggestions.filter(null, null, "", 0);
    }
}
