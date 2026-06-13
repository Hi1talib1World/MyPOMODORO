package com.denzo.mypomodoro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

public final class TimerActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        TimerStateManager stateManager = TimerStateManager.getInstance(context);
        TimerStateManager.TimerState current = stateManager.getCurrentState();

        if (PomodoroWidgetProvider.ACTION_WIDGET_PLAY_PAUSE.equals(action)) {
            handlePlayPause(context, stateManager, current);
        } else if (PomodoroWidgetProvider.ACTION_WIDGET_RESET.equals(action)) {
            handleReset(context, stateManager, current);
        } else {
            // Handle notification or other actions via Constants.BUTTON_ACTION
            String buttonAction = intent.getStringExtra(Constants.BUTTON_ACTION);
            if (Constants.BUTTON_START.equals(buttonAction) || Constants.BUTTON_PAUSE.equals(buttonAction)) {
                handlePlayPause(context, stateManager, current);
            } else if (Constants.BUTTON_STOP.equals(buttonAction)) {
                handleReset(context, stateManager, current);
            }
        }
        
        PomodoroWidgetProvider.updateAllWidgets(context);
    }

    private void handlePlayPause(Context context, TimerStateManager stateManager, TimerStateManager.TimerState current) {
        TimerStateManager.TimerState newState;
        if (current.isRunning) {
            long remaining = current.endTime - System.currentTimeMillis();
            newState = new TimerStateManager.TimerState(false, remaining, current.totalLimitMs, current.isBreak);
            if (stateManager.commitState(newState)) {
                context.stopService(new Intent(context, PomodoroService.class));
            }
        } else {
            long remaining = current.endTime > 0 ? current.endTime : current.totalLimitMs;
            long targetEndTime = System.currentTimeMillis() + remaining;
            newState = new TimerStateManager.TimerState(true, targetEndTime, current.totalLimitMs, current.isBreak);
            if (stateManager.commitState(newState)) {
                Intent serviceIntent = new Intent(context, PomodoroService.class);
                context.startService(serviceIntent);
            }
        }
    }

    private void handleReset(Context context, TimerStateManager stateManager, TimerStateManager.TimerState current) {
        TimerStateManager.TimerState newState = new TimerStateManager.TimerState(false, current.totalLimitMs, current.totalLimitMs, current.isBreak);
        if (stateManager.commitState(newState)) {
            context.stopService(new Intent(context, PomodoroService.class));
        }
    }
}
