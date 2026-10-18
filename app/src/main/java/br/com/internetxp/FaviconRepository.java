package br.com.internetxp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Consulta conservadora a /favicon.ico usando HTTPS do próprio site, sem serviços de terceiros. */
final class FaviconRepository {
    private static final int MAX_BYTES = 96 * 1024;
    private static final int MAX_CACHE = 48;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final Map<String, Bitmap> cache = Collections.synchronizedMap(
            new LinkedHashMap<String, Bitmap>(64, .75f, true) {
                @Override protected boolean removeEldestEntry(Map.Entry<String, Bitmap> eldest) {
                    return size() > MAX_CACHE;
                }
            });
    private final Set<String> notFound = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> inFlight = Collections.synchronizedSet(new HashSet<>());
    private final Map<String, java.util.ArrayList<ImageView>> observers = new HashMap<>();

    static String origin(String address) {
        if (address == null) return null;
        try {
            URI uri = new URI(address);
            String hostname = uri.getHost();
            if (!"https".equalsIgnoreCase(uri.getScheme()) || hostname == null) return null;
            if (uri.getRawUserInfo() != null || hostname.length() > 253) return null;
            if (hostname.indexOf('.') < 1 || hostname.endsWith(".local") || hostname.endsWith(".internal")) return null;
            if (!hostname.matches("(?i)[a-z0-9.-]+") || hostname.matches("[0-9.]+")) return null;
            int port = uri.getPort();
            if (port != -1 && port != 443) return null;
            return "https://" + hostname.toLowerCase(java.util.Locale.ROOT);
        } catch (Exception e) { return null; }
    }

    void load(String site, ImageView target) {
        String key = origin(site);
        target.setTag(key);
        if (key == null) return;
        Bitmap bitmap = cache.get(key);
        if (bitmap != null) { target.setImageBitmap(bitmap); return; }
        if (notFound.contains(key)) return;
        synchronized (observers) {
            observers.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(target);
        }
        if (!inFlight.add(key)) return;
        io.execute(() -> {
            Bitmap image = request(key);
            if (image != null) cache.put(key, image);
            else notFound.add(key);
            main.post(() -> {
                inFlight.remove(key);
                java.util.ArrayList<ImageView> views;
                synchronized (observers) { views = observers.remove(key); }
                if (views == null || image == null) return;
                for (ImageView view : views) {
                    if (key.equals(view.getTag())) view.setImageBitmap(image);
                }
            });
        });
    }

    private Bitmap request(String origin) {
        HttpURLConnection connection = null;
        try {
            String host = new URI(origin).getHost();
            // Bloqueio conservador para favoritos em endereços de rede local.
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (address.isAnyLocalAddress() || address.isLoopbackAddress() ||
                        address.isLinkLocalAddress() || address.isSiteLocalAddress() ||
                        address.isMulticastAddress()) return null;
            }
            connection = (HttpURLConnection) new URL(origin + "/favicon.ico").openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(2400);
            connection.setReadTimeout(2400);
            connection.setRequestProperty("Accept", "image/x-icon,image/png,image/*;q=0.7");
            if (connection.getResponseCode() != 200 || connection.getContentLengthLong() > MAX_BYTES) return null;
            try (InputStream input = connection.getInputStream(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] data = new byte[4096];
                int total = 0;
                for (int len; (len = input.read(data)) != -1;) {
                    total += len;
                    if (total > MAX_BYTES) return null;
                    output.write(data, 0, len);
                }
                byte[] bytes = output.toByteArray();
                Bitmap bitmap = decodeIcon(bytes);
                if (bitmap != null) return bitmap;
            }
        } catch (Exception ignored) {
            // Falhas de favicon nunca interrompem navegação.
        } finally {
            if (connection != null) connection.disconnect();
        }
        return null;
    }

    private Bitmap decodeIcon(byte[] bytes) {
        Bitmap decoded = decodeBounded(bytes, 0, bytes.length);
        if (decoded != null) return decoded;
        // ICO contém às vezes PNGs internos. O Android decodifica o PNG diretamente.
        if (bytes.length < 22 || bytes[0] != 0 || bytes[1] != 0 || bytes[2] != 1 || bytes[3] != 0) return null;
        int count = (bytes[4] & 255) | ((bytes[5] & 255) << 8);
        for (int index = 0; index < Math.min(count, 16); index++) {
            int entry = 6 + index * 16;
            if (entry + 16 > bytes.length) break;
            int length = le32(bytes, entry + 8);
            int offset = le32(bytes, entry + 12);
            if (offset < 0 || length <= 8 || offset > bytes.length - length) continue;
            if ((bytes[offset] & 255) == 137 && bytes[offset + 1] == 80 && bytes[offset + 2] == 78 && bytes[offset + 3] == 71) {
                decoded = decodeBounded(bytes, offset, length);
                if (decoded != null) return decoded;
            }
        }
        return null;
    }

    private Bitmap decodeBounded(byte[] bytes, int offset, int length) {
        BitmapFactory.Options probe = new BitmapFactory.Options();
        probe.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, offset, length, probe);
        if (probe.outWidth < 1 || probe.outHeight < 1 || probe.outWidth > 2048 || probe.outHeight > 2048) return null;
        int scale = 1;
        while (Math.max(probe.outWidth, probe.outHeight) / scale > 96) scale *= 2;
        BitmapFactory.Options bounded = new BitmapFactory.Options();
        bounded.inSampleSize = scale;
        return BitmapFactory.decodeByteArray(bytes, offset, length, bounded);
    }

    private int le32(byte[] bytes, int at) {
        return (bytes[at] & 255) | ((bytes[at + 1] & 255) << 8)
                | ((bytes[at + 2] & 255) << 16) | ((bytes[at + 3] & 255) << 24);
    }

    void shutdown() {
        io.shutdownNow();
    }
}
