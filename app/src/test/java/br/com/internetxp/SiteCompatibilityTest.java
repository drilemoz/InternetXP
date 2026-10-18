package br.com.internetxp;

import org.junit.Test;
import static org.junit.Assert.*;

public class SiteCompatibilityTest {
    @Test public void acceptsHttpsOrigin() {
        assertTrue(SiteCompatibility.isSecureOrigin("https://exemplo.com/login"));
        assertTrue(SiteCompatibility.isSecureOrigin("HTTPS://example.org:443/test"));
    }

    @Test public void deniesInsecureOrInvalidOrigin() {
        assertFalse(SiteCompatibility.isSecureOrigin("http://exemplo.com"));
        assertFalse(SiteCompatibility.isSecureOrigin("javascript:alert(1)"));
        assertFalse(SiteCompatibility.isSecureOrigin("https://user@example.com"));
        assertFalse(SiteCompatibility.isSecureOrigin(null));
    }

    @Test public void supportsOnlySafeExternalSchemes() {
        for (String uri : new String[] {"tel:123456", "mailto:contato@example.com",
                "sms:123", "geo:0,0?q=teste", "market://details?id=example"}) {
            assertTrue(uri, SiteCompatibility.isExternalLink(uri));
        }
        for (String uri : new String[] {"https://example.com", "intent://example.com",
                "javascript:alert(1)", "file:///sdcard/test.txt", "content://test"}) {
            assertFalse(uri, SiteCompatibility.isExternalLink(uri));
        }
        assertFalse(SiteCompatibility.isExternalLink("tel:123\nmalicious"));
    }

    @Test public void keepsKnownPermissionsOnly() {
        assertTrue(SiteCompatibility.isKnownAndroidPermission("android.permission.CAMERA"));
        assertTrue(SiteCompatibility.isKnownAndroidPermission("android.permission.RECORD_AUDIO"));
        assertTrue(SiteCompatibility.isKnownAndroidPermission("android.permission.ACCESS_FINE_LOCATION"));
        assertFalse(SiteCompatibility.isKnownAndroidPermission("android.permission.READ_CONTACTS"));
    }

    @Test public void filtersInvalidMimeAndDeduplicates() {
        assertArrayEquals(new String[]{"image/png", "image/jpeg"},
                SiteCompatibility.mimeTypes(new String[]{"image/png", "image/png", "image/jpeg", ".png", ""}));
        assertEquals("*/*", SiteCompatibility.primaryMimeType(new String[]{"image/png", "image/jpeg"}));
        assertEquals("application/pdf", SiteCompatibility.primaryMimeType(new String[]{"application/pdf"}));
    }
}
