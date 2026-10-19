package br.com.internetxp;

import org.junit.Test;
import static org.junit.Assert.*;

public class SitePermissionRulesTest {
    @Test public void normalizesDistinctOriginsAndPorts() {
        assertEquals("https://chatgpt.com:443", SitePermissionRules.origin("https://ChatGPT.com/help"));
        assertEquals("http://localhost:8000", SitePermissionRules.origin("http://localhost:8000/index"));
        assertEquals("http://[::1]:3000", SitePermissionRules.origin("http://[::1]:3000"));
        assertNotEquals(SitePermissionRules.origin("http://localhost:8000"),
                SitePermissionRules.origin("http://localhost:3000"));
    }

    @Test public void refusesInvalidOrUntrustedOrigins() {
        assertNull(SitePermissionRules.origin("javascript:alert(1)"));
        assertNull(SitePermissionRules.origin("https://user:pw@chatgpt.com"));
        assertNull(SitePermissionRules.origin("http://site.local:0"));
        assertFalse(SitePermissionRules.isTrustworthyMediaOrigin("http://192.168.1.2:8000"));
        assertTrue(SitePermissionRules.isTrustworthyMediaOrigin("http://127.0.0.1:8000"));
        assertTrue(SitePermissionRules.isTrustworthyMediaOrigin("http://[::1]:8000"));
        assertTrue(SitePermissionRules.isTrustworthyMediaOrigin("https://facebook.com"));
    }

    @Test public void preferenceKeysAreOpaqueAndOriginScoped() {
        String key = SitePermissionRules.preferenceKey("https://chatgpt.com/path?token=SECRET", "camera");
        assertNotNull(key);
        assertFalse(key.contains("chatgpt"));
        assertFalse(key.contains("SECRET"));
        assertEquals(key, SitePermissionRules.preferenceKey("https://chatgpt.com/other", "camera"));
        assertNotEquals(key, SitePermissionRules.preferenceKey("https://chatgpt.com", "microphone"));
        assertNotEquals(key, SitePermissionRules.preferenceKey("https://chatgpt.com:444", "camera"));
        assertNull(SitePermissionRules.preferenceKey("https://chatgpt.com", "location"));
    }
}
