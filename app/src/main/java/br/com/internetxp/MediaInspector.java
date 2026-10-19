package br.com.internetxp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Inspeção limitada de mídia HTTP(S) sob gesto explícito do usuário. Sem cookies nem redirects. */
final class MediaInspector {
    static final class Result {
        final String mime;
        final long bytes;
        final int width, height;
        final Bitmap preview;
        Result(String mime, long bytes, int width, int height, Bitmap preview) {
            this.mime = mime; this.bytes = bytes; this.width = width; this.height = height; this.preview = preview;
        }
    }
    private static final int MAX_PREVIEW_BYTES = 512 * 1024;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();

    void inspect(MediaResource media, android.os.Handler handler, java.util.function.Consumer<Result> callback) {
        worker.execute(() -> {
            Result result = inspect(media);
            handler.post(() -> callback.accept(result));
        });
    }

    private Result inspect(MediaResource media) {
        String mime = MediaResource.guessMime(media.url, media.kind);
        long bytes = -1;
        int width = 0, height = 0;
        Bitmap preview = null;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(media.url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(3500);
            connection.setReadTimeout(3500);
            int status = connection.getResponseCode();
            if (status >= 200 && status < 300) {
                bytes = connection.getContentLengthLong();
                String contentType = connection.getContentType();
                if (contentType != null && !contentType.trim().isEmpty()) {
                    mime = contentType.split(";", 2)[0].trim().toLowerCase(java.util.Locale.ROOT);
                }
            }
        } catch (Exception ignored) { /* Sites podem recusar HEAD; manter dados desconhecidos. */ }
        finally { if (connection != null) connection.disconnect(); }

        // Prévia apenas de imagens leves, sem scripts, cookies, redirects ou leitura ilimitada.
        if ("Imagem".equals(media.kind) && !"image/svg+xml".equals(mime)
                && (bytes < 0 || bytes <= MAX_PREVIEW_BYTES)) {
            HttpURLConnection imageConnection = null;
            try {
                imageConnection = (HttpURLConnection) new URL(media.url).openConnection();
                imageConnection.setInstanceFollowRedirects(false);
                imageConnection.setConnectTimeout(3500);
                imageConnection.setReadTimeout(3500);
                imageConnection.setRequestProperty("Accept", "image/png,image/jpeg,image/webp,image/gif");
                if (imageConnection.getResponseCode() == 200
                        && imageConnection.getContentLengthLong() <= MAX_PREVIEW_BYTES) {
                    try (InputStream stream = imageConnection.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        byte[] buffer = new byte[4096];
                        for (int len; (len = stream.read(buffer)) != -1;) {
                            if (out.size() + len > MAX_PREVIEW_BYTES) break;
                            out.write(buffer, 0, len);
                        }
                        byte[] data = out.toByteArray();
                        BitmapFactory.Options probe = new BitmapFactory.Options();
                        probe.inJustDecodeBounds = true;
                        BitmapFactory.decodeByteArray(data, 0, data.length, probe);
                        if (probe.outWidth > 0 && probe.outHeight > 0 && probe.outWidth <= 4096 && probe.outHeight <= 4096) {
                            width = probe.outWidth; height = probe.outHeight;
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = Math.max(1, (int) Math.ceil(Math.max(width, height) / 360.0));
                            preview = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                        }
                    }
                }
            } catch (Exception ignored) { /* Não prometer prévia para recursos autenticados. */ }
            finally { if (imageConnection != null) imageConnection.disconnect(); }
        }
        return new Result(mime, bytes, width, height, preview);
    }

    void close() { worker.shutdownNow(); }
}
