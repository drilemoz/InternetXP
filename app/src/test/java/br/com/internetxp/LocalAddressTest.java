package br.com.internetxp;
import static org.junit.Assert.*;
import org.junit.Test;

public class LocalAddressTest {
    @Test public void localhostAndIpv4DefaultToHttp() {
        assertEquals("http://127.0.0.1:8000", BrowserAddress.resolve("127.0.0.1:8000"));
        assertEquals("http://localhost:8000", BrowserAddress.resolve("localhost:8000"));
        assertEquals("http://localhost", BrowserAddress.resolve("localhost"));
        assertEquals("http://192.168.1.20:8080/path?q=1", BrowserAddress.resolve("192.168.1.20:8080/path?q=1"));
        assertEquals("http://10.0.2.2:8000", BrowserAddress.resolve("10.0.2.2:8000"));
        assertEquals("http://172.16.2.3", BrowserAddress.resolve("172.16.2.3"));
        assertEquals("http://192.168.0.1", BrowserAddress.resolve("192.168.0.1"));
        assertEquals("http://host.local:8000", BrowserAddress.resolve("host.local:8000"));
        assertEquals("http://server.lan", BrowserAddress.resolve("server.lan"));
        assertEquals("http://raspberrypi:3000", BrowserAddress.resolve("raspberrypi:3000"));
        assertEquals("http://[::1]:3000", BrowserAddress.resolve("[::1]:3000"));
        assertEquals("http://[fd00::123]", BrowserAddress.resolve("[fd00::123]"));
    }
    @Test public void publicSitesRemainHttpsAndExplicitSchemesAreHonored() {
        assertEquals("https://chatgpt.com", BrowserAddress.resolve("chatgpt.com"));
        assertEquals("https://example.com:8000", BrowserAddress.resolve("example.com:8000"));
        assertEquals("https://8.8.8.8:53", BrowserAddress.resolve("8.8.8.8:53"));
        assertEquals("https://localhost:8443/", BrowserAddress.resolve("https://localhost:8443/"));
        assertEquals("http://example.com", BrowserAddress.resolve("http://example.com"));
        assertEquals("http://127.0.0.1:8000/a", BrowserAddress.resolve("http://127.0.0.1:8000/a"));
    }
    @Test public void invalidHostsAreNotTurnedIntoUrls() {
        for (String input : new String[] {"999.10.10.10:3000", "127.0.0.1:99999", "127.0.0.1:0",
                "localhost:xyz", "http://user@localhost:8000", "127.0.0.1:8000\\path", "bad host:8000",
                "javascript:alert(1)", "localhost:8000\nsecret", "http://localhost:65536"}) {
            assertFalse(input, BrowserAddress.isDirectAddress(input));
            assertTrue(input, BrowserAddress.resolve(input).startsWith("https://www.google.com/search?q="));
        }
        assertFalse(BrowserAddress.isDirectAddress("google"));
        assertTrue(BrowserAddress.resolve("google").contains("search?q=google"));
    }
    @Test public void localHomePagesAndAutocompletePrivacy() {
        assertEquals("http://localhost:8000", HomePageSettings.normalize("localhost:8000"));
        assertEquals("http://192.168.1.20:8080", HomePageSettings.normalize("192.168.1.20:8080"));
        assertEquals("http://[::1]:3000", HomePageSettings.normalize("[::1]:3000"));
        assertEquals("about:blank", HomePageSettings.normalize("só uma pesquisa"));
        for (String address : new String[] {"localhost:8000", "127.0.0.1:8000", "192.168.0.1:8080",
                "[::1]:8000", "raspberrypi:3000"}) {
            assertFalse(address, OnlineSuggestions.isEligibleQuery(address));
        }
    }
}
