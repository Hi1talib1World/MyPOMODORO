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

    private static final String CHANNEL_ID = "pomodoro_channel";
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
                stopSelf(); // Stop service when the countdown finishes
            }
        }.start();

        // Start the foreground service with a notification
        startForeground(NOTIFICATION_ID, buildNotification("Pomodoro Timer", formatTime(timeRemaining)));
    }

    private Notification buildNotification(String title, String text) {
        // Create the play and stop button intents
        Intent playIntent = new Intent(this, PomodoroReceiver.class); // Custom BroadcastReceiver to handle play
        playIntent.setAction("com.denzo.mypomodoro.PLAY");
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, 0, playIntent, 0);

        Intent stopIntent = new Intent(this, PomodoroReceiver.class); // Custom BroadcastReceiver to handle stop
        stopIntent.setAction("com.denzo.mypomodoro.STOP");
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, 0);

        // Set the custom view
        remoteViews.setTextViewText(R.id.countdownText, text);
        remoteViews.setOnClickPendingIntent(R.id.playButton, playPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.stopButton, stopPendingIntent);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo) // Use your icon here
                .setContentTitle(title)
                .setContent(remoteViews)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setAutoCancel(false);

        return builder.build();
    }

    private void updateNotification() {
        String timeFormatted = formatTime(timeRemaining);
        Notification notification = buildNotification("Pomodoro Timer", timeFormatted);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private String formatTime(long milliSeconds) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Pomodoro Channel";
            String description = "Channel for Pomodoro timer notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
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
