package br.com.internetxp;
import org.junit.Test;
import static org.junit.Assert.*;
public class BrowserAddressTest {
 @Test public void blank() { assertEquals("about:blank", BrowserAddress.resolve(" ")); }
 @Test public void host() { assertEquals("https://mozilla.org", BrowserAddress.resolve("mozilla.org")); }
 @Test public void url() { assertEquals("https://example.com/x", BrowserAddress.resolve("https://example.com/x")); }
 @Test public void search() { assertTrue(BrowserAddress.resolve("internet xp").endsWith("internet+xp")); }
 @Test public void js() { assertTrue(BrowserAddress.resolve("javascript:alert(1)").startsWith("https://www.google.com/search?q=")); }
}
