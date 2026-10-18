package br.com.internetxp;

import java.net.URI;

/** Normaliza endereços de página inicial, sem aceitar termos de busca como URLs. */
final class HomePageSettings {
    static final String DEFAULT = "about:blank";
    private HomePageSettings() { }

    static String normalize(String input) {
        if (input == null || input.trim().isEmpty() || DEFAULT.equalsIgnoreCase(input.trim())) return DEFAULT;
        String entered = input.trim();
        if (entered.matches("(?s).*\\s+.*")) return DEFAULT;
        if (!entered.regionMatches(true, 0, "https://", 0, 8)
                && !entered.regionMatches(true, 0, "http://", 0, 7)
                && !entered.matches("(?i)^[a-z0-9][a-z0-9.-]*\\.[a-z]{2,63}(?::[0-9]{1,5})?([/?#].*)?$")) return DEFAULT;
        String resolved = BrowserAddress.resolve(entered);
        if (!TabSessionStore.restorable(resolved) || DEFAULT.equals(resolved)) return DEFAULT;
        try {
            URI uri = new URI(resolved);
            if (uri.getHost() == null || !uri.getHost().contains(".") || uri.getUserInfo() != null) return DEFAULT;
            return resolved;
        } catch (Exception ignored) { return DEFAULT; }
    }
}
