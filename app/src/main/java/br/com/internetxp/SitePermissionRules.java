package br.com.internetxp;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

/** Política local para permissões de mídia: nunca autoriza câmera ou microfone automaticamente. */
public final class SitePermissionRules {
    private SitePermissionRules() { }

    /** Identifica exatamente a origem que solicitou a permissão (não mistura portas). */
    public static String origin(String value) {
        if (value == null || value.length() > 4096) return null;
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme == null || host == null || host.isEmpty() || uri.getUserInfo() != null) return null;
            scheme = scheme.toLowerCase(Locale.ROOT);
            host = host.toLowerCase(Locale.ROOT);
            if (!"http".equals(scheme) && !"https".equals(scheme)) return null;
            int port = uri.getPort();
            if (port > 65535 || port == 0 || port < -1) return null;
            if (port == -1) port = "https".equals(scheme) ? 443 : 80;
            if (host.indexOf(':') >= 0 && !host.startsWith("[")) host = "[" + host + "]";
            return scheme + "://" + host + ":" + port;
        } catch (Exception ignored) { return null; }
    }

    /** Chave opaca para não salvar URLs ou domínios em SharedPreferences. */
    public static String preferenceKey(String origin, String device) {
        if (!"camera".equals(device) && !"microphone".equals(device)) return null;
        String normalized = origin(origin);
        if (normalized == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalized.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder("media_site_v1_").append(device).append('_');
            for (byte b : hash) result.append(String.format(Locale.ROOT, "%02x", b & 0xff));
            return result.toString();
        } catch (Exception ignored) { return null; }
    }

    /** HTTP é aceito para loopback do próprio aparelho; não para IPs LAN remotos. */
    public static boolean isTrustworthyMediaOrigin(String value) {
        String normalized = origin(value);
        if (normalized == null) return false;
        if (normalized.startsWith("https://")) return true;
        return normalized.startsWith("http://localhost:")
                || normalized.startsWith("http://127.0.0.1:")
                || normalized.startsWith("http://[::1]:");
    }
}
