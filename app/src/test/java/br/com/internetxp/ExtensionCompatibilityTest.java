package br.com.internetxp;

import static org.junit.Assert.*;
import org.junit.Test;

public final class ExtensionCompatibilityTest {
    @Test public void recognizesDownloadedPackagesOnlyByFileName() {
        assertTrue(ExtensionCompatibility.isXpiFileName("cookie_editor_plus-2.6.0.xpi"));
        assertTrue(ExtensionCompatibility.isXpiFileName("EXTENSION.XPI"));
        assertFalse(ExtensionCompatibility.isXpiFileName("extension.xpi.zip"));
        assertFalse(ExtensionCompatibility.isXpiFileName(null));
    }

    @Test public void permitsOnlyAbsoluteHttpsWithoutCredentialsOrFragments() {
        assertTrue(ExtensionCompatibility.isSecurePackageUrl("https://example.org/download/addon.xpi?source=amo"));
        assertFalse(ExtensionCompatibility.isSecurePackageUrl("http://example.org/addon.xpi"));
        assertFalse(ExtensionCompatibility.isSecurePackageUrl("https://user:secret@example.org/addon.xpi"));
        assertFalse(ExtensionCompatibility.isSecurePackageUrl("https://example.org/addon.xpi#frag"));
        assertFalse(ExtensionCompatibility.isSecurePackageUrl("file:///sdcard/extension.xpi"));
        assertFalse(ExtensionCompatibility.isSecurePackageUrl("javascript:alert(1)"));
    }

    @Test public void verifiesManifestFirefoxMajorRangeConservatively() {
        assertTrue(ExtensionCompatibility.requiresNewerFirefox("159.0", 158));
        assertFalse(ExtensionCompatibility.requiresNewerFirefox("158.0a1", 158));
        assertFalse(ExtensionCompatibility.requiresNewerFirefox("128.0", 158));
        assertTrue(ExtensionCompatibility.isBelowMinimumFirefox("157.*", 158));
        assertFalse(ExtensionCompatibility.isBelowMinimumFirefox("158.*", 158));
        assertFalse(ExtensionCompatibility.isBelowMinimumFirefox("*", 158));
    }
}
