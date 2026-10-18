package br.com.internetxp;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;

/** Autocomplete remoto opcional: sem envio em modo privado, HTTPS, consultas curtas e sem redirecionamento. */
final class OnlineSuggestions {
    private OnlineSuggestions() { }

    static boolean isEligibleQuery(String value) {
        if (value == null) return false;
        String query = value.trim();
        if (query.length() < 2 || query.length() > 100) return false;
        if (query.contains("://") || query.contains("/") || query.contains("\\") || query.contains("@")) return false;
        if (query.equalsIgnoreCase("localhost") || query.matches("[0-9.]+") || query.endsWith(".local")) return false;
        if (query.matches("(?i)^[a-z0-9-]+(\\.[a-z0-9.-]+)(:[0-9]+)?$")) return false;
        for (int i = 0; i < query.length(); i++) {
            if (Character.isISOControl(query.charAt(i))) return false;
        }
        return true;
    }

    static ArrayList<String> parse(String body) {
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        try {
            JSONArray array = new JSONArray(body);
            for (int i = 0; i < array.length() && unique.size() < 8; i++) {
                JSONObject object = array.optJSONObject(i);
                if (object == null) continue;
                String phrase = object.optString("phrase", "").trim();
                if (!phrase.isEmpty() && phrase.length() <= 150) unique.add(phrase);
            }
        } catch (Exception ignored) { }
        return new ArrayList<>(unique);
    }

    static ArrayList<String> fetch(String query) {
        if (!isEligibleQuery(query)) return new ArrayList<>();
        HttpURLConnection connection = null;
        try {
            String parameter = URLEncoder.encode(query, "UTF-8");
            connection = (HttpURLConnection) new URL("https://duckduckgo.com/ac/?q=" + parameter).openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Accept", "application/json");
            if (connection.getResponseCode() != 200 || connection.getContentLengthLong() > 48 * 1024) return new ArrayList<>();
            try (InputStream stream = connection.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[2048];
                int len;
                while ((len = stream.read(buffer)) >= 0) {
                    if (out.size() + len > 48 * 1024) return new ArrayList<>();
                    out.write(buffer, 0, len);
                }
                return parse(out.toString(StandardCharsets.UTF_8.name()));
            }
        } catch (Exception ignored) {
            return new ArrayList<>(); // Offline: histórico e favoritos continuam funcionando.
        } finally {
            if (connection != null) connection.disconnect();
        }
    }
}
