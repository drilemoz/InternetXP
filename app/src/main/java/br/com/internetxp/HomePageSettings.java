package br.com.internetxp;

import java.net.URI;

/** Normaliza endereços de página inicial; aceita host local sem permitir pesquisa como URL. */
final class HomePageSettings {
    static final String DEFAULT = "about:blank";
    private HomePageSettings() { }

    static String normalize(String input) {
        if (input == null || input.trim().isEmpty() || DEFAULT.equalsIgnoreCase(input.trim())) return DEFAULT;
        String entered = input.trim();
        if (!BrowserAddress.isDirectAddress(entered)) return DEFAULT;
        String resolved = BrowserAddress.resolve(entered);
        if (!TabSessionStore.restorable(resolved)) return DEFAULT;
        try {
            URI uri = new URI(resolved);
            return uri.getHost() != null && uri.getRawUserInfo() == null ? resolved : DEFAULT;
        } catch (Exception ignored) { return DEFAULT; }
    }
}
