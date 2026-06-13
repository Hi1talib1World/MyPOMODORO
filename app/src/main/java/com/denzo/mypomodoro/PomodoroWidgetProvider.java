package com.denzo.mypomodoro;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

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
        TimerStateManager stateManager = TimerStateManager.getInstance(context);
        TimerStateManager.TimerState state = stateManager.getCurrentState();
        
        long remaining = state.endTime - System.currentTimeMillis();
        if (!state.isRunning) remaining = state.endTime; // Paused state stores remaining

        String timeStr = Utility.formatTime(Math.max(0, remaining));

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setTextViewText(R.id.widget_timer, timeStr);
        views.setImageViewResource(R.id.widget_play_pause, 
            state.isRunning ? R.drawable.pause : R.drawable.ic_play_button);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_timer, pendingIntent);

        Intent playIntent = new Intent(context, TimerActionReceiver.class);
        playIntent.setAction(ACTION_WIDGET_PLAY_PAUSE);
        PendingIntent playPending = PendingIntent.getBroadcast(context, 1, playIntent, 
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_play_pause, playPending);

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
}
