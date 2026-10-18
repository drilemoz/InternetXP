package br.com.internetxp;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.regex.Pattern;

public final class BrowserAddress {
 private static final Pattern HOST = Pattern.compile("(?i)^((localhost)|(\\[[0-9a-f:]+\\])|([a-z0-9_-]+(\\.[a-z0-9_-]+)+))(:[0-9]{1,5})?([/?#].*)?$");
 private BrowserAddress() {}
 public static String resolve(String input) {
  String s = input == null ? "" : input.trim();
  if (s.isEmpty() || s.equalsIgnoreCase("about:blank")) return "about:blank";
  String lower = s.toLowerCase(Locale.ROOT);
  if ((lower.startsWith("https://") || lower.startsWith("http://")) && !s.matches("(?s).*\\s+.*") && !s.contains("\\")) return s;
  if (!s.contains("://") && HOST.matcher(s).matches()) return "https://" + s;
  // The Charset overload is available only from Android API 33.
  // The String charset-name overload works from Android API 26 (our minSdk).
  try {
   return "https://www.google.com/search?q=" + URLEncoder.encode(s, "UTF-8");
  } catch (UnsupportedEncodingException impossible) {
   throw new IllegalStateException("UTF-8 ausente na JVM", impossible);
  }
 }
}
