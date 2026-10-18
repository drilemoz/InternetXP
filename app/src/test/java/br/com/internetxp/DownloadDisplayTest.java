package br.com.internetxp;
import static org.junit.Assert.*;
import org.junit.Test;
public class DownloadDisplayTest {
    @Test public void unknownSize() { assertEquals(-1, DownloadDisplay.percent(123, -1)); }
    @Test public void normalPercent() { assertEquals(25, DownloadDisplay.percent(250, 1000)); }
    @Test public void clamped() { assertEquals(0, DownloadDisplay.percent(-50, 1000)); assertEquals(100, DownloadDisplay.percent(2000, 1000)); }
    @Test public void overflowSafe() { assertEquals(50, DownloadDisplay.percent(Long.MAX_VALUE / 2, Long.MAX_VALUE)); }
    @Test public void bytesFormat() { assertEquals("1.0 MB", DownloadDisplay.bytes(1048576)); }
}
