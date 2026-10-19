package br.com.internetxp;

import java.net.URI;
import java.util.Locale;

/** Exceções explícitas de compatibilidade, nunca automáticas ou para domínios semelhantes. */
final class SitePerformanceRules {
    private SitePerformanceRules() { }

    static boolean isCompatibilityMode(String url, boolean chatgpt, boolean facebook) {
        if (url == null) return false;
        try {
            URI uri = new URI(url);
            if (!"https".equalsIgnoreCase(uri.getScheme())) return false;
            String host = uri.getHost();
            if (host == null) return false;
            host = host.toLowerCase(Locale.ROOT);
            return (chatgpt && matches(host, "chatgpt.com")) ||
                    (facebook && matches(host, "facebook.com"));
        } catch (Exception ignored) { return false; }
    }

    private static boolean matches(String host, String domain) {
        return host.equals(domain) || host.endsWith("." + domain);
    }
}
