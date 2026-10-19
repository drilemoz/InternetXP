package br.com.internetxp;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Registro local mínimo. Sem URLs, cookies, mensagens de exceção ou dados da página. */
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
                // Um nome de classe de falha é útil e não expõe o conteúdo da exceção.
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
        String line = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(new Date()) + "  " + event;
        String[] lines = current.split("\\n");
        StringBuilder result = new StringBuilder(line);
        for (String existing : lines) {
            if (existing.isEmpty()) continue;
            if (result.toString().split("\\n").length >= MAX_EVENTS) break;
            result.append('\n').append(existing);
        }
        prefs.edit().putString(EVENTS, result.toString()).commit(); // persistir mesmo antes de crash do processo
    }

    static String read(Context context) {
        String events = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(EVENTS, "");
        return events.isEmpty() ? "Nenhuma falha registrada" : events;
    }

    static String report(Context context, String appVersion) {
        return "Internet XP " + appVersion + "\nAndroid API " + android.os.Build.VERSION.SDK_INT +
                "\nEventos locais, sem URLs ou dados de navegação:\n" + read(context);
    }

    static void clear(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(EVENTS).apply();
    }
}
