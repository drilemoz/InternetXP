package br.com.internetxp;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.HashSet;
public final class ReleaseNotesTest {
    @Test public void displaysAfterFreshInstallAndUpdate() {
        assertTrue(ReleaseNotes.shouldShow(0));
        assertTrue(ReleaseNotes.shouldShow(21));
        assertFalse(ReleaseNotes.shouldShow(22));
        assertFalse(ReleaseNotes.shouldShow(23));
    }
    @Test public void releaseEntriesAreUsefulAndUnique() {
        assertEquals("1.0.21", ReleaseNotes.VERSION_NAME);
        assertEquals(22, ReleaseNotes.VERSION_CODE);
        assertTrue(ReleaseNotes.FEATURES.length >= 3);
        HashSet<String> names = new HashSet<>();
        for (ReleaseNotes.Feature feature : ReleaseNotes.FEATURES) {
            assertFalse(feature.title.trim().isEmpty());
            assertTrue(feature.description.trim().length() > 25);
            assertTrue(names.add(feature.title));
        }
    }
}
