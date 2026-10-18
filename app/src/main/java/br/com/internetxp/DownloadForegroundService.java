package br.com.internetxp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;

import androidx.core.content.ContextCompat;

import java.util.LinkedHashMap;
import java.util.Map;

/** Notificação e proteção de processo enquanto o próprio Internet XP transfere arquivos.
 * Não utiliza DownloadManager. O serviço não reinicia automaticamente transferências perdidas.
 */
public final class DownloadForegroundService extends Service {
    private static final String CHANNEL = "internet_xp_downloads";
    private static final int NOTIFICATION_ID = 4521;
    private static final String ACTION_START = "br.com.internetxp.DOWNLOAD_START";
    private static final String ACTION_UPDATE = "br.com.internetxp.DOWNLOAD_UPDATE";
    private static final String ACTION_END = "br.com.internetxp.DOWNLOAD_END";
    private static final String EXTRA_ID = "transfer_id";
    private static final String EXTRA_NAME = "transfer_name";
    private static final String EXTRA_RECEIVED = "received";
    private static final String EXTRA_TOTAL = "total";

    private static final class TransferState {
        final String name;
        long received;
        long total;
        TransferState(String name, long total) {
            this.name = name;
            this.total = total;
        }
    }
    private final Map<Long, TransferState> transfers = new LinkedHashMap<>();
    private long lastNotificationMs;

    static void begin(Context context, long id, String name, long total) {
        Intent intent = new Intent(context, DownloadForegroundService.class);
        intent.setAction(ACTION_START);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_NAME, AppDownloadEngine.sanitize(name));
        intent.putExtra(EXTRA_TOTAL, total);
        ContextCompat.startForegroundService(context, intent);
    }

    static void progress(Context context, long id, long received, long total) {
        Intent intent = new Intent(context, DownloadForegroundService.class);
        intent.setAction(ACTION_UPDATE);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_RECEIVED, received);
        intent.putExtra(EXTRA_TOTAL, total);
        try { context.startService(intent); } catch (RuntimeException ignored) { }
    }

    static void end(Context context, long id) {
        Intent intent = new Intent(context, DownloadForegroundService.class);
        intent.setAction(ACTION_END);
        intent.putExtra(EXTRA_ID, id);
        try { context.startService(intent); } catch (RuntimeException ignored) { }
    }

    @Override public void onCreate() {
        super.onCreate();
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null && manager.getNotificationChannel(CHANNEL) == null) {
            manager.createNotificationChannel(new NotificationChannel(
                    CHANNEL, "Downloads do Internet XP", NotificationManager.IMPORTANCE_LOW));
        }
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            if (transfers.isEmpty()) stopSelfResult(startId);
            return START_NOT_STICKY;
        }
        long id = intent.getLongExtra(EXTRA_ID, -1L);
        if (id < 0) return START_NOT_STICKY;
        switch (intent.getAction()) {
            case ACTION_START:
                if (!transfers.containsKey(id)) {
                    transfers.put(id, new TransferState(intent.getStringExtra(EXTRA_NAME),
                            intent.getLongExtra(EXTRA_TOTAL, -1L)));
                }
                break;
            case ACTION_UPDATE:
                TransferState state = transfers.get(id);
                if (state != null) {
                    state.received = Math.max(0, intent.getLongExtra(EXTRA_RECEIVED, 0));
                    state.total = intent.getLongExtra(EXTRA_TOTAL, -1L);
                }
                break;
            case ACTION_END:
                transfers.remove(id);
                break;
            default: break;
        }
        if (transfers.isEmpty()) {
            stopForeground(STOP_FOREGROUND_REMOVE);
            stopSelfResult(startId);
        } else {
            long now = SystemClock.elapsedRealtime();
            if (!ACTION_UPDATE.equals(intent.getAction()) || now - lastNotificationMs >= 1500) {
                showNotification();
                lastNotificationMs = now;
            }
        }
        return START_NOT_STICKY;
    }

    private void showNotification() {
        TransferState first = transfers.values().iterator().next();
        int pct = first.total > 0 ? (int) DownloadDisplay.percent(first.received, first.total) : -1;
        String name = first.name == null ? "Arquivo" : first.name;
        String text = transfers.size() == 1 ? name : transfers.size() + " downloads ativos";
        Intent open = new Intent(this, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending = PendingIntent.getActivity(this, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new Notification.Builder(this, CHANNEL)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle("Internet XP: baixando")
                .setContentText(text)
                .setProgress(100, Math.max(0, pct), pct < 0)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setContentIntent(pending)
                .build();
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    @Override public void onTimeout(int startId, int fgsType) {
        // Android 15 pode encerrar dataSync após sua cota diária.
        AppDownloadEngine.get(getApplicationContext()).cancelAllForTimeout();
        transfers.clear();
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
