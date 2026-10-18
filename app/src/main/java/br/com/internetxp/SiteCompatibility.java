package br.com.internetxp;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Locale;

/** Validações conservadoras para acesso a dispositivos e navegação fora do navegador. */
public final class SiteCompatibility {
    private SiteCompatibility() { }

    public static boolean isSecureOrigin(String value) {
        if (value == null) return false;
        try {
            URI uri = new URI(value);
            return "https".equalsIgnoreCase(uri.getScheme())
                    && uri.getHost() != null && !uri.getHost().isEmpty()
                    && uri.getUserInfo() == null;
        } catch (Exception ignored) { return false; }
    }

    public static boolean isExternalLink(String value) {
        if (value == null || value.length() > 4096 || value.indexOf('\n') >= 0
                || value.indexOf('\r') >= 0) return false;
        try {
            String scheme = new URI(value).getScheme();
            if (scheme == null) return false;
            switch (scheme.toLowerCase(Locale.ROOT)) {
                case "tel": case "mailto": case "sms": case "geo": case "market": return true;
                default: return false;
            }
        } catch (Exception ignored) { return false; }
    }

    public static String originLabel(String value) {
        if (value == null) return "site desconhecido";
        try {
            URI uri = new URI(value);
            if (uri.getHost() != null) return uri.getHost();
            if (isExternalLink(value)) return value.length() > 140 ? value.substring(0, 140) + "…" : value;
        } catch (Exception ignored) { }
        return "site desconhecido";
    }

    public static boolean isKnownAndroidPermission(String permission) {
        return "android.permission.CAMERA".equals(permission)
                || "android.permission.RECORD_AUDIO".equals(permission)
                || "android.permission.ACCESS_FINE_LOCATION".equals(permission)
                || "android.permission.ACCESS_COARSE_LOCATION".equals(permission);
    }

    public static String[] mimeTypes(String[] provided) {
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        if (provided != null) for (String item : provided) {
            if (item != null && item.matches("[A-Za-z0-9.+-]+/[A-Za-z0-9.+*-]+")) unique.add(item);
        }
        return unique.toArray(new String[0]);
    }

    public static String primaryMimeType(String[] provided) {
        String[] filtered = mimeTypes(provided);
        return filtered.length == 1 ? filtered[0] : "*/*";
    }
}
