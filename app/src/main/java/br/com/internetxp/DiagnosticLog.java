package br.com.internetxp;

import android.app.ActivityManager;
import android.app.ApplicationExitInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Registro local mínimo. Não coleta URLs, cookies, mensagens ou stacks. */
final class DiagnosticLog {
    private static final String PREFS = "diagnostics_local";
    private static final String EVENTS = "events";
    private static final int MAX_EVENTS = 35;
    private static volatile boolean installed;
    private DiagnosticLog() { }

    static synchronized void install(Context context) {
        if (installed) return;
        final Context app = context.getApplicationContext();
        final Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                record(app, "APP_" + throwable.getClass().getSimpleName());
            } catch (Exception ignored) { }
            if (previous != null) previous.uncaughtException(thread, throwable);
        });
        installed = true;
    }

    static synchronized void record(Context context, String event) {
        if (event == null || !event.matches("[A-Za-z0-9_]{3,64}")) return;
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String current = prefs.getString(EVENTS, "");
        String line = time(System.currentTimeMillis()) + "  " + event;
        String[] lines = current.split("\n");
        StringBuilder result = new StringBuilder(line);
        int count = 1;
        for (String existing : lines) {
            if (existing.isEmpty()) continue;
            if (count >= MAX_EVENTS) break;
            result.append('\n').append(existing);
            count++;
        }
        prefs.edit().putString(EVENTS, result.toString()).commit();
    }

    private static String time(long millis) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(new Date(millis));
    }

    static String read(Context context) {
        String events = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(EVENTS, "");
        return events.isEmpty() ? "Nenhuma falha registrada" : events;
    }

    /** Motivos fornecidos pelo Android 11+, sem extrair stacktrace ou descrição (podem conter URLs). */
    static String recentExits(Context context) {
        if (Build.VERSION.SDK_INT < 30) return "Disponível a partir do Android 11";
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am == null) return "Serviço indisponível";
            List<ApplicationExitInfo> exits = am.getHistoricalProcessExitReasons(null, 0, 8);
            if (exits == null || exits.isEmpty()) return "Sem registros fornecidos pelo Android";
            StringBuilder lines = new StringBuilder();
            int count = 0;
            for (ApplicationExitInfo info : exits) {
                // Ignora encerramentos de serviços ou outros processos sem diagnóstico do navegador.
                if (!context.getPackageName().equals(info.getProcessName())) continue;
                if (info.getTimestamp() <= 0) continue;
                if (count++ >= 6) break;
                if (lines.length() > 0) lines.append('\n');
                lines.append(time(info.getTimestamp())).append("  ").append(ExitReasonLabels.label(info.getReason()));
            }
            return lines.length() == 0 ? "Sem encerramentos do processo principal" : lines.toString();
        } catch (RuntimeException error) {
            return "Histórico do Android indisponível";
        }
    }

    static String memorySummary(Context context) {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am == null) return "Indisponível";
            ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(info);
            long availableMb = info.availMem / 1048576L;
            long javaMb = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576L;
            return "Heap Java em uso: " + javaMb + " MiB • RAM livre do sistema: " + availableMb + " MiB";
        } catch (RuntimeException ignored) { return "Indisponível"; }
    }

    static String report(Context context, String appVersion) {
        return "Internet XP " + appVersion + "\nAndroid API " + Build.VERSION.SDK_INT +
                "\n" + memorySummary(context) +
                "\n\nEventos locais (sem URLs ou dados de navegação):\n" + read(context) +
                "\n\nEncerramentos anteriores registrados pelo Android:\n" + recentExits(context) +
                "\n\nO relatório contém apenas categorias de falhas, horários e memória aproximada.\n";
    }

    /** Relatório exportado sob ação explícita via SAF; nada é enviado a um servidor. */
    static void writeZip(Context context, String appVersion, OutputStream destination) throws IOException {
        ZipOutputStream zip = new ZipOutputStream(destination, StandardCharsets.UTF_8);
        zip.putNextEntry(new ZipEntry("diagnostico.txt"));
        zip.write(report(context, appVersion).getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
        zip.putNextEntry(new ZipEntry("LEIA-ME.txt"));
        zip.write(("Relatório local do Internet XP. Revise antes de compartilhar.\n" +
                "Não inclui URLs, senhas, cookies, conteúdo de páginas nem stacktraces.\n" +
                "Horários e informações de memória podem constar do arquivo.\n")
                .getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
        zip.finish();
    }

    static void clear(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(EVENTS).apply();
    }
}
