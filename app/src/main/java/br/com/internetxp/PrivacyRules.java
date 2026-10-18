package br.com.internetxp;

import java.net.URI;
import java.net.URISyntaxException;

/** Pure rules for privacy-related UI decisions; no Android dependencies. */
public final class PrivacyRules {
    private PrivacyRules() { }

    public static boolean shouldRecordHistory(boolean privateMode, String url) {
        return !privateMode && url != null &&
                (url.startsWith("http://") || url.startsWith("https://"));
    }

    public static boolean isVerifiedSecure(String url, boolean engineSecure,
            boolean certificateException, boolean activeMixedContent) {
        return url != null && url.startsWith("https://") && engineSecure &&
                !certificateException && !activeMixedContent;
    }

    public static String displayHost(String url) {
        if (url == null) return "Sem endereço";
        try {
            String host = new URI(url).getHost();
            return host == null || host.isEmpty() ? "Página interna" : host;
        } catch (URISyntaxException ignored) {
            return "Endereço não identificado";
        }
    }
}
