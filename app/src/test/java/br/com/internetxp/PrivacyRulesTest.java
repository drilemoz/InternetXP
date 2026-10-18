package br.com.internetxp;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PrivacyRulesTest {
    @Test public void privatePagesNeverRecorded() {
        assertFalse(PrivacyRules.shouldRecordHistory(true, "https://example.com/segredo"));
        assertFalse(PrivacyRules.shouldRecordHistory(true, "http://example.org"));
    }
    @Test public void normalHistoryRequiresWebUrl() {
        assertTrue(PrivacyRules.shouldRecordHistory(false, "https://example.com"));
        assertTrue(PrivacyRules.shouldRecordHistory(false, "http://example.com"));
        assertFalse(PrivacyRules.shouldRecordHistory(false, "about:blank"));
        assertFalse(PrivacyRules.shouldRecordHistory(false, null));
    }
    @Test public void httpsAloneDoesNotVerifyConnection() {
        assertFalse(PrivacyRules.isVerifiedSecure("https://example.com", false, false, false));
        assertFalse(PrivacyRules.isVerifiedSecure("http://example.com", true, false, false));
        assertFalse(PrivacyRules.isVerifiedSecure("https://example.com", true, true, false));
        assertFalse(PrivacyRules.isVerifiedSecure("https://example.com", true, false, true));
        assertTrue(PrivacyRules.isVerifiedSecure("https://example.com", true, false, false));
    }
    @Test public void hostSafelyDisplayed() {
        assertEquals("example.com", PrivacyRules.displayHost("https://example.com/test"));
        assertEquals("Página interna", PrivacyRules.displayHost("about:blank"));
        assertEquals("Sem endereço", PrivacyRules.displayHost(null));
    }
}
