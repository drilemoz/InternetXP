package br.com.internetxp;

import org.junit.Test;
import static org.junit.Assert.*;

public class SitePerformanceRulesTest {
    @Test public void modeIsOptInForExactDomainsAndSubdomains() {
        assertFalse(SitePerformanceRules.isCompatibilityMode("https://chatgpt.com/", false, false));
        assertTrue(SitePerformanceRules.isCompatibilityMode("https://chatgpt.com/", true, false));
        assertTrue(SitePerformanceRules.isCompatibilityMode("https://www.facebook.com/", false, true));
        assertFalse(SitePerformanceRules.isCompatibilityMode("https://fakefacebook.com", false, true));
        assertFalse(SitePerformanceRules.isCompatibilityMode("https://chatgpt.com.evil.tld", true, false));
        assertFalse(SitePerformanceRules.isCompatibilityMode("http://chatgpt.com", true, false));
        assertFalse(SitePerformanceRules.isCompatibilityMode("https://example.com", true, true));
        assertFalse(SitePerformanceRules.isCompatibilityMode("", true, true));
    }
}
