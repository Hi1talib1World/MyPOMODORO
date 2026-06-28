package com.denzo.mypomodoro;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.app.Service;
import android.widget.RemoteViews;
import java.util.concurrent.TimeUnit;

public class PomodoroService extends Service {

    private static final int NOTIFICATION_ID = 1;
    private CountDownTimer countDownTimer;
    private NotificationManagerCompat notificationManager;
    private RemoteViews remoteViews;
    private TimerStateManager stateManager;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = NotificationManagerCompat.from(this);
        stateManager = TimerStateManager.getInstance(this);
        remoteViews = new RemoteViews(getPackageName(), R.layout.notification_layout);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        syncAndStart();
        return START_STICKY;
    }

    private void syncAndStart() {
        TimerStateManager.TimerState state = stateManager.getCurrentState();
        if (!state.isRunning) {
            stopSelf();
            return;
        }

        long now = System.currentTimeMillis();
        long remaining = state.endTime - now;

        if (remaining <= 0) {
            handleFinish();
            return;
        }

        if (countDownTimer != null) countDownTimer.cancel();
        
        countDownTimer = new CountDownTimer(remaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateNotification(millisUntilFinished);
                PomodoroWidgetProvider.updateAllWidgets(PomodoroService.this);
                
                // Atomic incremental save every minute
                long now = System.currentTimeMillis();
                TimerStateManager.TimerState current = stateManager.getCurrentState();
                if (current.isRunning && current.lastSaveTime > 0 && (now - current.lastSaveTime >= 60000)) {
                    long delta = now - current.lastSaveTime;
                    saveProgress(delta);
                    // Update lastSaveTime in SSOT
                    stateManager.commitState(new TimerStateManager.TimerState(true, current.endTime, current.totalLimitMs, current.isBreak, now));
                }
            }

            @Override
            public void onFinish() {
                TimerStateManager.TimerState current = stateManager.getCurrentState();
                if (current.isRunning && current.lastSaveTime > 0) {
                    saveProgress(System.currentTimeMillis() - current.lastSaveTime);
                }
                handleFinish();
            }
        }.start();

        startForeground(NOTIFICATION_ID, buildNotification(formatTime(remaining)));
    }

    private void handleFinish() {
        TimerStateManager.TimerState current = stateManager.getCurrentState();
        int activityId = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(Constants.CURRENT_ACTIVITY_ID, 1);
        
        if (current.isBreak) {
            Utility.updateDatabaseBreaksCount(this, activityId);
        } else {
            Utility.updateDatabaseCompletedWorksCount(this, activityId);
        }

        // Commit finished state to SSOT
        stateManager.commitState(new TimerStateManager.TimerState(false, 0, current.totalLimitMs, current.isBreak, 0L));
        
        // Trigger completion alert
        Intent endIntent = new Intent(this, EndNotificationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(endIntent);
        } else {
            startService(endIntent);
        }
        
        stopSelf();
    }

    private void saveProgress(long deltaMs) {
        TimerStateManager.TimerState current = stateManager.getCurrentState();
        int activityId = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(Constants.CURRENT_ACTIVITY_ID, 1);
        
        if (current.isBreak) {
            Utility.updateDatabaseBreakTimeOnly(this, deltaMs, activityId);
        } else {
            Utility.updateDatabaseWorkTimeOnly(this, deltaMs, activityId);
        }
    }

    private Notification buildNotification(String text) {
        TimerStateManager.TimerState state = stateManager.getCurrentState();
        
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_IMMUTABLE;

        Intent playIntent = new Intent(this, PomodoroReceiver.class);
        playIntent.setAction("com.denzo.mypomodoro.PLAY");
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, 0, playIntent, flags);

        Intent stopIntent = new Intent(this, PomodoroReceiver.class);
        stopIntent.setAction("com.denzo.mypomodoro.STOP");
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, flags);

        remoteViews.setTextViewText(R.id.countdownText, text);
        remoteViews.setImageViewResource(R.id.playButton, state.isRunning ? R.drawable.pause : R.drawable.ic_play_button);
        remoteViews.setOnClickPendingIntent(R.id.playButton, playPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.stopButton, stopPendingIntent);

        return new NotificationCompat.Builder(this, Constants.CHANNEL_TIMER)
                .setSmallIcon(R.drawable.notification_icon)
                .setCustomContentView(remoteViews)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setColor(getResources().getColor(R.color.brand_blue))
                .build();
    }

    private void updateNotification(long ms) {
        startForeground(NOTIFICATION_ID, buildNotification(formatTime(ms)));
    }

    private String formatTime(long ms) {
        return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(ms), 
                TimeUnit.MILLISECONDS.toSeconds(ms % 60000));
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        if (countDownTimer != null) countDownTimer.cancel();
        
        TimerStateManager.TimerState current = stateManager.getCurrentState();
        if (current.isRunning && current.lastSaveTime > 0) {
            saveProgress(System.currentTimeMillis() - current.lastSaveTime);
            // Update state so it doesn't double count if restarted
            stateManager.commitState(new TimerStateManager.TimerState(true, current.endTime, current.totalLimitMs, current.isBreak, System.currentTimeMillis()));
        }

        super.onDestroy();
    }
}
