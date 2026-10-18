package br.com.internetxp;
import static org.junit.Assert.*;
import org.junit.Test;

public class HomePageSettingsTest {
    @Test public void newInstallDefaultsToNativeHome() {
        assertEquals("about:blank", HomePageSettings.normalize(null));
        assertEquals("about:blank", HomePageSettings.normalize(""));
        assertEquals("about:blank", HomePageSettings.normalize("about:blank"));
    }
    @Test public void acceptsExplicitSitesAndPlainHosts() {
        assertEquals("https://www.example.com/", HomePageSettings.normalize("https://www.example.com/"));
        assertEquals("https://mozilla.org", HomePageSettings.normalize("mozilla.org"));
    }
    @Test public void rejectsSearchTermsAndNonWebSchemes() {
        assertEquals("about:blank", HomePageSettings.normalize("pesquisar minha bicicleta"));
        assertEquals("about:blank", HomePageSettings.normalize("javascript:alert(1)"));
        assertEquals("about:blank", HomePageSettings.normalize("file:///sdcard/x"));
    }
}
