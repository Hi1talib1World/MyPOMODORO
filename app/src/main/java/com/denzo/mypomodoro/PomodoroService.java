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
import android.app.Service;
import android.widget.RemoteViews;
import java.util.concurrent.TimeUnit;

public class PomodoroService extends Service {

    private static final int NOTIFICATION_ID = 1;
    private CountDownTimer countDownTimer;
    private long timeRemaining = 25 * 60 * 1000; // 25 minutes in milliseconds
    private NotificationManagerCompat notificationManager;
    private RemoteViews remoteViews;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = NotificationManagerCompat.from(this);
        createNotificationChannel();
        remoteViews = new RemoteViews(getPackageName(), R.layout.notification_layout);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("TIME_REMAINING")) {
            timeRemaining = intent.getLongExtra("TIME_REMAINING", 25 * 60 * 1000);
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        startCountDown();
        return START_STICKY; // The service will restart if it's killed
    }

    private void startCountDown() {
        countDownTimer = new CountDownTimer(timeRemaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                updateNotification();
            }

            @Override
            public void onFinish() {
                timeRemaining = 0;
                updateNotification();
                countDownTimer = null;
                stopSelf(); // Stop service when the countdown finishes
            }
        }.start();

        // Start the foreground service with a notification
        startForeground(NOTIFICATION_ID, buildNotification(formatTime(timeRemaining)));
    }

    private Notification buildNotification(String text) {
        // Create the play and stop button intents
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        Intent playIntent = new Intent(this, PomodoroReceiver.class); // Custom BroadcastReceiver to handle play
        playIntent.setAction("com.denzo.mypomodoro.PLAY");
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, 0, playIntent, flags);

        Intent stopIntent = new Intent(this, PomodoroReceiver.class); // Custom BroadcastReceiver to handle stop
        stopIntent.setAction("com.denzo.mypomodoro.STOP");
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, flags);

        // Set the custom view
        remoteViews.setTextViewText(R.id.countdownText, text);
        remoteViews.setOnClickPendingIntent(R.id.playButton, playPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.stopButton, stopPendingIntent);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constants.CHANNEL_TIMER)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(getString(R.string.app_name))
                .setCustomContentView(remoteViews)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setAutoCancel(false);

        return builder.build();
    }

    private void updateNotification() {
        String timeFormatted = formatTime(timeRemaining);
        Notification notification = buildNotification(timeFormatted);
        
        notificationManager.notify(NOTIFICATION_ID, notification);
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
