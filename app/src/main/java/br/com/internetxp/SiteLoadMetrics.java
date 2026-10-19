package br.com.internetxp;

/** Medição em memória baseada no monotônico da JVM, sem URL e sem telemetria. */
final class SiteLoadMetrics {
    private long startedAt = -1;
    private long lastDurationMs = -1;
    private boolean lastSuccess;

    void started(long nowMs) {
        startedAt = nowMs;
        lastDurationMs = -1;
    }

    void finished(long nowMs, boolean success) {
        if (startedAt < 0) return;
        lastDurationMs = Math.max(0, nowMs - startedAt);
        lastSuccess = success;
        startedAt = -1;
    }

    String summary() {
        if (startedAt >= 0) return "Carregando…";
        if (lastDurationMs < 0) return "Ainda não medido";
        return (lastSuccess ? "Concluído" : "Falhou") + " em "
                + String.format(java.util.Locale.ROOT, "%.2f", lastDurationMs / 1000.0) + " s";
    }
}
