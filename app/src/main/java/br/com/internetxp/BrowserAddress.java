package br.com.internetxp;

import java.net.URI;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.regex.Pattern;

/** Interpreta endereços web e hosts locais sem transformar servidor HTTP em HTTPS. */
public final class BrowserAddress {
    private static final Pattern HOST_LABELS = Pattern.compile(
            "(?i)^[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?(?:\\.[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?)*$");
    private BrowserAddress() { }

    public static String resolve(String input) {
        String s = input == null ? "" : input.trim();
        if (s.isEmpty() || "about:blank".equalsIgnoreCase(s)) return "about:blank";
        if (isDirectAddress(s)) {
            String lower = s.toLowerCase(Locale.ROOT);
            if (lower.startsWith("http://") || lower.startsWith("https://")) return s;
            URI local = URI.create("http://" + s); // Validada anteriormente por isDirectAddress.
            return (isLocalHost(local.getHost(), local.getPort()) ? "http://" : "https://") + s;
        }
        try {
            return "https://www.google.com/search?q=" + URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException impossible) {
            throw new IllegalStateException("UTF-8 ausente na JVM", impossible);
        }
    }

    /** Também usado para não enviar endereços digitados ao serviço remoto de sugestões. */
    static boolean isDirectAddress(String input) {
        if (input == null || input.isEmpty() || input.length() > 4096 || !input.equals(input.trim())
                || input.indexOf('\\') >= 0) return false;
        for (int i = 0; i < input.length(); i++) {
            if (Character.isWhitespace(input.charAt(i)) || Character.isISOControl(input.charAt(i))) return false;
        }
        try {
            boolean explicit = input.regionMatches(true, 0, "http://", 0, 7)
                    || input.regionMatches(true, 0, "https://", 0, 8);
            if (!explicit && input.contains("://")) return false;
            URI uri = explicit ? new URI(input) : candidate(input);
            String scheme = uri.getScheme();
            if (scheme == null || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) return false;
            String host = uri.getHost();
            if (host == null || uri.getRawUserInfo() != null) return false;
            int port = uri.getPort();
            if (port == 0 || port > 65535) return false;
            if (uri.getRawAuthority() == null || uri.getRawAuthority().endsWith(":")) return false;
            if (host.startsWith("[") && host.endsWith("]")) return true; // URI validou IPv6.
            if (!HOST_LABELS.matcher(host).matches()) return false;
            if (isNumericDots(host)) return validIpv4(host);
            if (explicit) return true;
            // Uma palavra sem porta é uma pesquisa, exceto localhost.
            return host.contains(".") || port > 0 || "localhost".equalsIgnoreCase(host);
        } catch (Exception ignored) { return false; }
    }

    private static URI candidate(String input) throws java.net.URISyntaxException {
        return new URI("http://" + input);
    }

    private static boolean isNumericDots(String host) {
        return host.matches("[0-9.]+");
    }

    private static boolean validIpv4(String host) {
        String[] parts = host.split("\\.", -1);
        if (parts.length != 4) return false;
        for (String part : parts) {
            if (part.isEmpty() || part.length() > 3 || (part.length() > 1 && part.startsWith("0"))) return false;
            try { if (Integer.parseInt(part) > 255) return false; }
            catch (NumberFormatException invalid) { return false; }
        }
        return true;
    }

    private static boolean isLocalHost(String host, int port) {
        if (host == null) return false;
        String lower = host.toLowerCase(Locale.ROOT);
        if (lower.equals("localhost") || lower.endsWith(".localhost") || lower.endsWith(".local")
                || lower.endsWith(".lan") || lower.endsWith(".home.arpa")) return true;
        if (lower.startsWith("[") && lower.endsWith("]")) {
            String address = lower.substring(1, lower.length() - 1);
            return address.equals("::1") || address.startsWith("fe8") || address.startsWith("fe9")
                    || address.startsWith("fea") || address.startsWith("feb")
                    || address.startsWith("fc") || address.startsWith("fd");
        }
        if (isNumericDots(lower) && validIpv4(lower)) {
            String[] bytes = lower.split("\\.");
            int a = Integer.parseInt(bytes[0]), b = Integer.parseInt(bytes[1]);
            return a == 10 || a == 127 || (a == 172 && b >= 16 && b <= 31)
                    || (a == 192 && b == 168) || (a == 169 && b == 254)
                    || (a == 100 && b >= 64 && b <= 127);
        }
        // Um host simples acompanhado de porta normalmente é um servidor na rede local.
        return port > 0 && !lower.contains(".");
    }
}
