package br.com.internetxp;

import android.content.Context;
import android.graphics.Bitmap;
import java.io.File;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
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
    private final ExecutorService io = Executors.newFixedThreadPool(2);
    private final File diskCache;
    FaviconRepository(Context context) {
        diskCache = new File(context.getCacheDir(), "xp_favicons_v2");
    }
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
            Bitmap image = fromDisk(key);
            if (image == null) {
                image = request(key);
                if (image != null) toDisk(key, image);
            }
            if (image != null) cache.put(key, image);
            else notFound.add(key);
            final Bitmap readyIcon = image;
            main.post(() -> {
                inFlight.remove(key);
                java.util.ArrayList<ImageView> views;
                synchronized (observers) { views = observers.remove(key); }
                if (views == null || readyIcon == null) return;
                for (ImageView view : views) {
                    if (key.equals(view.getTag())) view.setImageBitmap(readyIcon);
                }
            });
        });
    }

    private Bitmap request(String origin) {
        try {
            String host = new URI(origin).getHost();
            // Não buscar favicons em servidores locais nem serviços externos.
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (address.isAnyLocalAddress() || address.isLoopbackAddress() ||
                        address.isLinkLocalAddress() || address.isSiteLocalAddress() ||
                        address.isMulticastAddress()) return null;
            }
            java.util.ArrayList<String> candidates = new java.util.ArrayList<>();
            candidates.add(origin + "/favicon.ico");
            candidates.add(origin + "/favicon.png");
            candidates.add(origin + "/apple-touch-icon.png");
            candidates.addAll(iconLinks(origin));
            Set<String> tried = new HashSet<>();
            for (String url : candidates) {
                if (!tried.add(url)) continue;
                Bitmap icon = requestIcon(url);
                if (icon != null) return icon;
            }
        } catch (Exception ignored) { /* Falhas de ícones nunca interrompem navegação. */ }
        return null;
    }

    /** Descobre tags link rel=icon da página inicial, sem script e sem redirecionar terceiros. */
    private java.util.List<String> iconLinks(String origin) {
        java.util.ArrayList<String> found = new java.util.ArrayList<>();
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(origin + "/").openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(2300);
            connection.setReadTimeout(2300);
            connection.setRequestProperty("Accept", "text/html");
            if (connection.getResponseCode() != 200 ||
                    !connection.getContentType().toLowerCase(java.util.Locale.ROOT).contains("text/html")) return found;
            byte[] buffer = new byte[4096];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (InputStream stream = connection.getInputStream()) {
                for (int len; (len = stream.read(buffer)) != -1;) {
                    if (out.size() + len > 64 * 1024) break;
                    out.write(buffer, 0, len);
                }
            }
            String html = out.toString("UTF-8");
            java.util.regex.Matcher tags = java.util.regex.Pattern.compile("(?is)<link\\b[^>]{0,1024}>").matcher(html);
            while (tags.find() && found.size() < 5) {
                String tag = tags.group();
                String rel = attribute(tag, "rel");
                String href = attribute(tag, "href");
                if (rel == null || !rel.toLowerCase(java.util.Locale.ROOT).contains("icon") || href == null) continue;
                URI candidate = new URI(origin + "/").resolve(href);
                String candidateOrigin = origin(candidate.toASCIIString());
                if (origin.equals(candidateOrigin) && candidate.getRawUserInfo() == null && candidate.getRawQuery() == null) {
                    String address = candidate.toASCIIString();
                    if (address.length() <= 2048 && !address.toLowerCase(java.util.Locale.ROOT).endsWith(".svg")) found.add(address);
                }
            }
        } catch (Exception ignored) { }
        finally { if (connection != null) connection.disconnect(); }
        return found;
    }

    private String attribute(String tag, String name) {
        java.util.regex.Matcher attr = java.util.regex.Pattern.compile(
                "(?is)\\b" + name + "\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s>]+))").matcher(tag);
        return attr.find() ? (attr.group(1) != null ? attr.group(1) :
                (attr.group(2) != null ? attr.group(2) : attr.group(3))) : null;
    }

    private Bitmap requestIcon(String url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(2400);
            connection.setReadTimeout(2400);
            connection.setRequestProperty("Accept", "image/x-icon,image/png,image/webp,image/*;q=0.7");
            if (connection.getResponseCode() != 200 || connection.getContentLengthLong() > MAX_BYTES) return null;
            try (InputStream input = connection.getInputStream(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] data = new byte[4096];
                int total = 0;
                for (int len; (len = input.read(data)) != -1;) {
                    total += len;
                    if (total > MAX_BYTES) return null;
                    output.write(data, 0, len);
                }
                return decodeIcon(output.toByteArray());
            }
        } catch (Exception ignored) { return null; }
        finally { if (connection != null) connection.disconnect(); }
    }

    private File fileFor(String origin) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(origin.getBytes(StandardCharsets.UTF_8));
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < 16; i++) name.append(String.format(java.util.Locale.ROOT, "%02x", digest[i] & 255));
        return new File(diskCache, name + ".png");
    }

    private Bitmap fromDisk(String origin) {
        try {
            File file = fileFor(origin);
            if (!file.isFile() || file.length() > MAX_BYTES || file.length() == 0 ||
                    System.currentTimeMillis() - file.lastModified() > 30L * 86400000L) return null;
            byte[] data = java.nio.file.Files.readAllBytes(file.toPath());
            return decodeBounded(data, 0, data.length);
        } catch (Exception ignored) { return null; }
    }

    private void toDisk(String origin, Bitmap bitmap) {
        try {
            if (!diskCache.exists() && !diskCache.mkdirs()) return;
            File file = fileFor(origin);
            try (FileOutputStream output = new FileOutputStream(file)) {
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) return;
            }
            if (file.length() > MAX_BYTES) file.delete();
            File[] files = diskCache.listFiles();
            if (files != null && files.length > 64) {
                java.util.Arrays.sort(files, java.util.Comparator.comparingLong(File::lastModified));
                for (int i = 0; i < files.length - 64; i++) files[i].delete();
            }
        } catch (Exception ignored) { /* Cache é opcional. */ }
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
