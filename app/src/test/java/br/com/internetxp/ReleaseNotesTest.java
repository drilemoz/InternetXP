package br.com.internetxp;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.HashSet;

/** Regras independentes do numero fixo de versao (sincronizado com Gradle no validador). */
public final class ReleaseNotesTest {
    @Test public void displaysAfterFreshInstallAndUpdate() {
        int current = ReleaseNotes.VERSION_CODE;
        assertTrue("codigo de versao deve ser positivo", current > 0);
        assertTrue("primeira instalacao", ReleaseNotes.shouldShow(0));
        assertTrue("atualizacao de codigo anterior", ReleaseNotes.shouldShow(current - 1));
        assertFalse("nao repetir dentro da versao atual", ReleaseNotes.shouldShow(current));
        assertFalse("nao exibir apos codigo futuro", ReleaseNotes.shouldShow(current + 1));
    }

    @Test public void releaseEntriesAreUsefulAndUnique() {
        assertTrue("nome semantico", ReleaseNotes.VERSION_NAME.matches("\\d+\\.\\d+\\.\\d+"));
        assertTrue("codigo positivo", ReleaseNotes.VERSION_CODE > 0);
        assertNotNull(ReleaseNotes.HEADLINE);
        assertTrue(ReleaseNotes.HEADLINE.trim().length() > 10);
        assertNotNull(ReleaseNotes.SUBTITLE);
        assertTrue(ReleaseNotes.SUBTITLE.trim().length() > 25);
        assertNotNull(ReleaseNotes.FEATURES);
        assertTrue("pelo menos tres novidades", ReleaseNotes.FEATURES.length >= 3);
        HashSet<String> names = new HashSet<>();
        HashSet<String> numbers = new HashSet<>();
        for (ReleaseNotes.Feature feature : ReleaseNotes.FEATURES) {
            assertNotNull(feature);
            assertNotNull(feature.number);
            assertNotNull(feature.title);
            assertNotNull(feature.description);
            assertFalse("identificador vazio", feature.number.trim().isEmpty());
            assertFalse("titulo vazio", feature.title.trim().isEmpty());
            assertTrue("descricao deve ser util", feature.description.trim().length() > 25);
            assertTrue("identificador repetido", numbers.add(feature.number));
            assertTrue("titulo repetido", names.add(feature.title));
        }
    }
}
