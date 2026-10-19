package br.com.internetxp;
import static org.junit.Assert.*;
import org.junit.Test;
public class SiteLoadMetricsTest {
    @Test public void durationsAreMonotonicAndInMemory() {
        SiteLoadMetrics m = new SiteLoadMetrics();
        assertEquals("Ainda não medido", m.summary());
        m.started(1000);
        assertEquals("Carregando…", m.summary());
        m.finished(2234, true);
        assertEquals("Concluído em 1.23 s", m.summary());
        m.started(2500);
        m.finished(2000, false);
        assertEquals("Falhou em 0.00 s", m.summary());
        m.started(6000);
        assertEquals("Carregando…", m.summary());
    }
}
