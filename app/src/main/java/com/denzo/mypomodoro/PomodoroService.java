package com.denzo.mypomodoro;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;
import android.app.Service;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import java.util.concurrent.TimeUnit;

public class PomodoroService extends Service {

    private static final int NOTIFICATION_ID = 1;
    private CountDownTimer countDownTimer;
    private long timeRemaining;
    private NotificationManagerCompat notificationManager;
    private RemoteViews remoteViews;
    private SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = NotificationManagerCompat.from(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        createNotificationChannel();
        remoteViews = new RemoteViews(getPackageName(), R.layout.notification_layout);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long endTime = prefs.getLong(Constants.TIMER_END_TIME, 0);
        long now = System.currentTimeMillis();
        timeRemaining = endTime - now;

        if (timeRemaining <= 0) {
            stopSelf();
            return START_NOT_STICKY;
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        startCountDown();
        return START_STICKY;
    }

    private void startCountDown() {
        countDownTimer = new CountDownTimer(timeRemaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                updateNotification();
                PomodoroWidgetProvider.updateAllWidgets(PomodoroService.this);
            }

            @Override
            public void onFinish() {
                timeRemaining = 0;
                prefs.edit().putBoolean(Constants.IS_TIMER_RUNNING, false).apply();
                updateNotification();
                countDownTimer = null;
                stopSelf();
            }
        }.start();

        // Start the foreground service with a notification
        startForeground(NOTIFICATION_ID, buildNotification(formatTime(timeRemaining)));
    }

    private Notification buildNotification(String text) {
        boolean isRunning = prefs.getBoolean(Constants.IS_TIMER_RUNNING, false);

        // Create the play and stop button intents
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        Intent playIntent = new Intent(this, PomodoroReceiver.class);
        playIntent.setAction("com.denzo.mypomodoro.PLAY");
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, 0, playIntent, flags);

        Intent stopIntent = new Intent(this, PomodoroReceiver.class);
        stopIntent.setAction("com.denzo.mypomodoro.STOP");
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, flags);

        // Set the custom view
        remoteViews.setTextViewText(R.id.countdownText, text);
        remoteViews.setImageViewResource(R.id.playButton, isRunning ? R.drawable.pause : R.drawable.ic_play_button);
        remoteViews.setOnClickPendingIntent(R.id.playButton, playPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.stopButton, stopPendingIntent);

        // Build the notification
        return new NotificationCompat.Builder(this, Constants.CHANNEL_TIMER)
                .setSmallIcon(R.drawable.notification_icon)
                .setCustomContentView(remoteViews)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setColor(0xFF24395B)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
    }

    private void updateNotification() {
        String timeFormatted = formatTime(timeRemaining);
        Notification notification = buildNotification(timeFormatted);
        
        startForeground(NOTIFICATION_ID, notification);
    }

    private String formatTime(long milliSeconds) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));
    }

    private void createNotificationChannel() {
        // Channels are now created in SplashActivity
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
