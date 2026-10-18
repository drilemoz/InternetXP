package br.com.internetxp;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Gerenciador nativo do Internet XP. Não utiliza DownloadManager nem navegador externo. */
final class AppDownloadEngine {
    static final int STATUS_PENDING = 1;
    static final int STATUS_RUNNING = 2;
    static final int STATUS_SUCCESSFUL = 8;
    static final int STATUS_FAILED = 16;
    static final int STATUS_CANCELLED = -2;
    static final int REASON_NETWORK = -10;
    static final int REASON_STORAGE = -11;
    static final int REASON_MISSING_BODY = -12;
    static final int REASON_INTERRUPTED = -13;

    interface Listener {
        void onProgress(long received, long total, long bytesPerSecond);
        void onDone(String contentUri, long received);
        void onError(int reason);
    }

    interface ErrorCallback { void onError(Exception error); }

    private static volatile AppDownloadEngine INSTANCE;
    private final Context context;
    private final ExecutorService workers = Executors.newFixedThreadPool(2);
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ConcurrentHashMap<Long, Transfer> active = new ConcurrentHashMap<>();

    private static final class Transfer {
        volatile boolean cancelled;
        volatile InputStream input;
        volatile Future<?> future;
    }

    static AppDownloadEngine get(Context context) {
        if (INSTANCE == null) synchronized (AppDownloadEngine.class) {
            if (INSTANCE == null) INSTANCE = new AppDownloadEngine(context.getApplicationContext());
        }
        return INSTANCE;
    }

    private AppDownloadEngine(Context context) { this.context = context; }

    static boolean isWebUrl(String raw) {
        if (raw == null || raw.length() > 8192) return false;
        try {
            URI parsed = new URI(raw);
            String scheme = parsed.getScheme();
            return ("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme))
                    && parsed.getHost() != null && parsed.getRawUserInfo() == null;
        } catch (Exception ignored) { return false; }
    }

    static String header(Map<String, String> headers, String name) {
        if (headers == null) return null;
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (name.equalsIgnoreCase(entry.getKey())) return entry.getValue();
        }
        return null;
    }

    static long contentLength(Map<String, String> headers) {
        try {
            long n = Long.parseLong(header(headers, "Content-Length"));
            return n >= 0 ? n : -1;
        } catch (Exception ignored) { return -1; }
    }

    static String sanitize(String name) {
        if (name == null) return "arquivo";
        String cleaned = name.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").trim();
        while (cleaned.startsWith(".")) cleaned = cleaned.substring(1);
        if (cleaned.isEmpty()) return "arquivo";
        return cleaned.length() <= 110 ? cleaned : cleaned.substring(0, 110);
    }

    static String filename(String url, String disposition, String mime) {
        String candidate = null;
        if (disposition != null) {
            // Nomes simples do Content-Disposition. Nunca usar caminhos informados pelo servidor.
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?i)filename\\s*=\\s*(?:\"([^\"]+)\"|([^;]+))").matcher(disposition);
            if (m.find()) candidate = m.group(1) != null ? m.group(1) : m.group(2).trim();
        }
        if (candidate == null || candidate.trim().isEmpty()) {
            try {
                String path = new URI(url).getRawPath();
                if (path != null && path.lastIndexOf('/') + 1 < path.length()) {
                    candidate = URLDecoder.decode(path.substring(path.lastIndexOf('/') + 1), "UTF-8");
                }
            } catch (Exception ignored) { }
        }
        if (candidate == null || candidate.trim().isEmpty()) candidate = "download";
        candidate = sanitize(candidate);
        if (!candidate.contains(".")) {
            if (mime != null && mime.toLowerCase(Locale.ROOT).contains("pdf")) candidate += ".pdf";
            else if (mime != null && mime.toLowerCase(Locale.ROOT).contains("zip")) candidate += ".zip";
        }
        return candidate;
    }

    void start(long id, String filename, InputStream input, long expected, Listener listener) {
        Transfer transfer = new Transfer();
        transfer.input = input;
        Transfer previous = active.putIfAbsent(id, transfer);
        if (previous != null) {
            closeQuietly(input);
            main.post(() -> listener.onError(REASON_INTERRUPTED));
            return;
        }
        transfer.future = workers.submit(() -> {
            File file = null;
            long received = 0;
            long lastBytes = 0;
            long lastTime = android.os.SystemClock.elapsedRealtime();
            int errorCode = REASON_NETWORK;
            try (InputStream source = input) {
                File root = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (root == null) root = new File(context.getFilesDir(), "Downloads");
                if (!root.exists() && !root.mkdirs()) throw new IOException("Pasta de downloads indisponível");
                file = new File(root, id + "-" + sanitize(filename));
                if (file.exists() && !file.delete()) throw new IOException("Nome ocupado");
                errorCode = REASON_STORAGE;
                try (OutputStream target = new FileOutputStream(file)) {
                    byte[] buffer = new byte[32768];
                    int size;
                    for (;;) {
                        if (transfer.cancelled || Thread.currentThread().isInterrupted()) throw new IOException("Cancelado");
                        errorCode = REASON_NETWORK;
                        size = source.read(buffer);
                        if (size < 0) break;
                        if (size == 0) continue;
                        errorCode = REASON_STORAGE;
                        target.write(buffer, 0, size);
                        received += size;
                        long now = android.os.SystemClock.elapsedRealtime();
                        if (now - lastTime >= 650) {
                            long currentBytes = received;
                            long speed = DownloadDisplay.speed(lastBytes, currentBytes, now - lastTime);
                            lastTime = now;
                            lastBytes = received;
                            main.post(() -> { if (!transfer.cancelled) listener.onProgress(currentBytes, expected, speed); });
                        }
                    }
                    target.flush();
                }
                if (transfer.cancelled) throw new IOException("Cancelado");
                if (expected > 0 && received != expected) throw new IOException("Conteúdo incompleto");
                Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".files", file);
                long done = received;
                main.post(() -> { if (!transfer.cancelled) listener.onDone(fileUri.toString(), done); });
            } catch (Exception failure) {
                if (file != null) file.delete();
                int reason = errorCode;
                main.post(() -> { if (!transfer.cancelled) listener.onError(reason); });
            } finally {
                active.remove(id, transfer);
                transfer.input = null;
            }
        });
    }

    void cancel(long id) {
        Transfer transfer = active.get(id);
        if (transfer == null) return;
        transfer.cancelled = true;
        closeQuietly(transfer.input);
        if (transfer.future != null) transfer.future.cancel(true);
        // O worker remove o arquivo parcial ao observar o cancelamento.
    }

    void exportCopy(Uri source, Uri target, Runnable finished, ErrorCallback error) {
        workers.execute(() -> {
            try (InputStream in = context.getContentResolver().openInputStream(source);
                 OutputStream out = context.getContentResolver().openOutputStream(target, "w")) {
                if (in == null || out == null) throw new IOException("Não foi possível abrir arquivo de destino");
                byte[] bytes = new byte[32768];
                int read;
                while ((read = in.read(bytes)) != -1) out.write(bytes, 0, read);
                out.flush();
                main.post(finished);
            } catch (Exception ex) { main.post(() -> error.onError(ex)); }
        });
    }

    private static void closeQuietly(InputStream stream) {
        if (stream != null) try { stream.close(); } catch (IOException ignored) { }
    }
}
