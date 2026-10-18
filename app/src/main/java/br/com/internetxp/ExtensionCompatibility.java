package br.com.internetxp;

import java.net.URI;
import java.util.Locale;

/** Verificações locais conservadoras; não substituem a validação real do GeckoView. */
final class ExtensionCompatibility {
    private ExtensionCompatibility() { }

    static boolean isXpiFileName(String fileName) {
        return fileName != null && fileName.trim().toLowerCase(Locale.ROOT).endsWith(".xpi");
    }

    static boolean isSecurePackageUrl(String candidate) {
        if (candidate == null || candidate.length() > 8192) return false;
        try {
            URI uri = new URI(candidate);
            return "https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null
                    && uri.getRawUserInfo() == null && uri.getFragment() == null;
        } catch (Exception ignored) { return false; }
    }

    static boolean requiresNewerFirefox(String version, int currentMajor) {
        Integer major = major(version);
        return major != null && major > currentMajor;
    }

    static boolean isBelowMinimumFirefox(String maximum, int currentMajor) {
        Integer major = major(maximum);
        return major != null && major < currentMajor;
    }

    private static Integer major(String version) {
        if (version == null) return null;
        String s = version.trim();
        int count = 0;
        while (count < s.length() && s.charAt(count) >= '0' && s.charAt(count) <= '9') count++;
        if (count == 0) return null;
        try { return Integer.parseInt(s.substring(0, count)); }
        catch (NumberFormatException ignored) { return null; }
    }
}
