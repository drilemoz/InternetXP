package br.com.internetxp;

import java.net.URI;
import java.util.Locale;

/** Link explícito de mídia obtido do menu contextual GeckoView, sem varrer páginas ou DRM. */
final class MediaResource {
    final String url;
    final String kind;
    final String label;
    final boolean privateMode;

    private MediaResource(String url, String kind, String label, boolean privateMode) {
        this.url = url;
        this.kind = kind;
        this.label = label;
        this.privateMode = privateMode;
    }

    static MediaResource from(String url, String kind, String label, boolean privateMode) {
        if (!isDownloadableUrl(url) || !isMediaKind(kind)) return null;
        String description = label == null ? "" : label.trim().replaceAll("[\\r\\n\\t]", " ");
        if (description.length() > 90) description = description.substring(0, 90);
        return new MediaResource(url, kind, description, privateMode);
    }

    static boolean isMediaKind(String kind) {
        return "Imagem".equals(kind) || "Vídeo".equals(kind) || "Áudio".equals(kind);
    }

    static boolean isDownloadableUrl(String url) {
        if (url == null || url.length() > 4096 || url.length() < 8) return false;
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme(), host = uri.getHost();
            if (scheme == null || !(scheme.equalsIgnoreCase("https") || scheme.equalsIgnoreCase("http"))) return false;
            return host != null && !host.isEmpty() && uri.getRawUserInfo() == null && uri.getPort() != 0
                    && uri.getPort() <= 65535 && uri.getRawFragment() == null;
        } catch (Exception invalid) { return false; }
    }

    static String guessMime(String url, String kind) {
        String path;
        try { path = new URI(url).getPath().toLowerCase(Locale.ROOT); }
        catch (Exception ignored) { path = ""; }
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".webp")) return "image/webp";
        if (path.endsWith(".gif")) return "image/gif";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".mp4")) return "video/mp4";
        if (path.endsWith(".webm")) return "video/webm";
        if (path.endsWith(".mp3")) return "audio/mpeg";
        if (path.endsWith(".ogg")) return "audio/ogg";
        if (path.endsWith(".wav")) return "audio/wav";
        if (path.endsWith(".m3u8")) return "application/vnd.apple.mpegurl";
        if (path.endsWith(".mpd")) return "application/dash+xml";
        return "Imagem".equals(kind) ? "image/*" : "Vídeo".equals(kind) ? "video/*" : "audio/*";
    }

    boolean isStreamingManifest() {
        String mime = guessMime(url, kind);
        return "application/dash+xml".equals(mime) || "application/vnd.apple.mpegurl".equals(mime);
    }
}
