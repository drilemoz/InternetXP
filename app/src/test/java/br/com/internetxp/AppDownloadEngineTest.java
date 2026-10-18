package br.com.internetxp;

import org.junit.Test;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

public class AppDownloadEngineTest {
    @Test public void onlyWebLinksAreDownloadable() {
        assertTrue(AppDownloadEngine.isWebUrl("https://example.org/file.zip"));
        assertTrue(AppDownloadEngine.isWebUrl("http://example.org/file"));
        assertFalse(AppDownloadEngine.isWebUrl("javascript:alert(1)"));
        assertFalse(AppDownloadEngine.isWebUrl("content://app/test"));
        assertFalse(AppDownloadEngine.isWebUrl("https://user:pass@example.org/x"));
    }
    @Test public void filenameUsesSafeName() {
        assertEquals("example.xpi", AppDownloadEngine.filename("https://site.org/assets/example.xpi", null, null));
        assertEquals("arquivo.pdf", AppDownloadEngine.filename("https://site.org/", "attachment; filename=arquivo.pdf", "application/pdf"));
        assertFalse(AppDownloadEngine.sanitize("../root.apk").contains("/"));
    }
    @Test public void contentLengthIsConservative() {
        Map<String,String> headers = new HashMap<>();
        headers.put("content-length", "5000");
        assertEquals(5000, AppDownloadEngine.contentLength(headers));
        assertEquals(-1, AppDownloadEngine.contentLength(Collections.emptyMap()));
    }
}
