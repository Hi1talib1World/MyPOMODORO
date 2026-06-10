package com.denzo.mypomodoro;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import androidx.preference.PreferenceManager;

import java.util.concurrent.TimeUnit;

public class PomodoroWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_WIDGET_PLAY_PAUSE = "com.denzo.mypomodoro.ACTION_WIDGET_PLAY_PAUSE";
    public static final String ACTION_WIDGET_RESET = "com.denzo.mypomodoro.ACTION_WIDGET_RESET";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isRunning = prefs.getBoolean(Constants.IS_TIMER_RUNNING, false);
        long endTime = prefs.getLong(Constants.TIMER_END_TIME, 0);
        long now = System.currentTimeMillis();
        long remaining = endTime - now;

        String timeStr = "25:00";
        if (isRunning && remaining > 0) {
            timeStr = formatTime(remaining);
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setTextViewText(R.id.widget_timer, timeStr);
        views.setImageViewResource(R.id.widget_play_pause, 
            isRunning ? R.drawable.pause : R.drawable.ic_play_button);

        // Intent to open app
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_timer, pendingIntent);

        // Intent for Play/Pause
        Intent playIntent = new Intent(context, TimerActionReceiver.class);
        playIntent.setAction(ACTION_WIDGET_PLAY_PAUSE);
        PendingIntent playPending = PendingIntent.getBroadcast(context, 1, playIntent, 
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_play_pause, playPending);

        // Intent for Reset
        Intent resetIntent = new Intent(context, TimerActionReceiver.class);
        resetIntent.setAction(ACTION_WIDGET_RESET);
        PendingIntent resetPending = PendingIntent.getBroadcast(context, 2, resetIntent, 
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_reset, resetPending);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static void updateAllWidgets(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, PomodoroWidgetProvider.class));
        for (int id : ids) {
            updateAppWidget(context, appWidgetManager, id);
        }
    }

    private static String formatTime(long ms) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(ms),
                TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms)));
    }
}
