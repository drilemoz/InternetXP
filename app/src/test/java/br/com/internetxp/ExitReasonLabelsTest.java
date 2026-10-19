package br.com.internetxp;

import org.junit.Test;
import static org.junit.Assert.*;

public class ExitReasonLabelsTest {
    @Test public void identifiesCrashesAndMemoryWithoutRawDescriptions() {
        assertEquals("Falha Java", ExitReasonLabels.label(4));
        assertEquals("Falha nativa", ExitReasonLabels.label(5));
        assertEquals("Aplicativo sem resposta (ANR)", ExitReasonLabels.label(6));
        assertEquals("Pouca memória", ExitReasonLabels.label(3));
        assertEquals("Motivo não reconhecido", ExitReasonLabels.label(999));
    }
}
