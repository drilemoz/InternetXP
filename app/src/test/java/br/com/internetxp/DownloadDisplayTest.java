package br.com.internetxp;
import static org.junit.Assert.*;
import org.junit.Test;
public class DownloadDisplayTest {
    @Test public void unknownTotalNeverShowsPercent() {
        assertEquals(-1, DownloadDisplay.percent(1024, -1));
        assertEquals(-1, DownloadDisplay.remainingSeconds(2048, -1, 1024));
    }

    @Test public void unknownSize() { assertEquals(-1, DownloadDisplay.percent(123, -1)); }
    @Test public void normalPercent() { assertEquals(25, DownloadDisplay.percent(250, 1000)); }
    @Test public void clamped() { assertEquals(0, DownloadDisplay.percent(-50, 1000)); assertEquals(100, DownloadDisplay.percent(2000, 1000)); }
    @Test public void overflowSafe() { assertEquals(50, DownloadDisplay.percent(Long.MAX_VALUE / 2, Long.MAX_VALUE)); }
    @Test public void bytesFormat() { assertEquals("1.0 MB", DownloadDisplay.bytes(1048576)); assertEquals("1.00 GB",DownloadDisplay.bytes(1073741824)); }
    @Test public void realSpeed() { assertEquals(1024, DownloadDisplay.speed(1000, 3048, 2000)); }
    @Test public void unavailableSpeed() { assertEquals(-1,DownloadDisplay.speed(1000,1000,2000)); assertEquals(-1,DownloadDisplay.speed(1000,2000,100)); assertEquals(-1,DownloadDisplay.speed(-1,2000,2000)); }
    @Test public void eta() { assertEquals(4, DownloadDisplay.remainingSeconds(2000, 6000, 1000)); assertEquals(-1, DownloadDisplay.remainingSeconds(1, -1, 100)); assertEquals(0,DownloadDisplay.remainingSeconds(5,5,12)); }
    @Test public void duration() { assertEquals("5s",DownloadDisplay.duration(5)); assertEquals("2min",DownloadDisplay.duration(121)); assertEquals("1h 1min",DownloadDisplay.duration(3661)); assertEquals("indisponível",DownloadDisplay.duration(-1)); }
}
