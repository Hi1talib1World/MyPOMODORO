package com.denzo.mypomodoro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
        long now = System.currentTimeMillis();
        
        if (current.isRunning) {
            // Save progress
            if (current.lastSaveTime > 0) {
                long delta = now - current.lastSaveTime;
                saveManualProgress(context, delta, current.isBreak);
            }
            
            long remaining = current.endTime - now;
            newState = new TimerStateManager.TimerState(false, remaining, current.totalLimitMs, current.isBreak, 0L);
            if (stateManager.commitState(newState)) {
                context.stopService(new Intent(context, PomodoroService.class));
            }
        } else {
            long remaining = current.endTime > 0 ? current.endTime : current.totalLimitMs;
            long targetEndTime = now + remaining;
            newState = new TimerStateManager.TimerState(true, targetEndTime, current.totalLimitMs, current.isBreak, now);
            if (stateManager.commitState(newState)) {
                Intent serviceIntent = new Intent(context, PomodoroService.class);
                context.startService(serviceIntent);
            }
        }
    }

    private void handleReset(Context context, TimerStateManager stateManager, TimerStateManager.TimerState current) {
        if (current.isRunning && current.lastSaveTime > 0) {
            long delta = System.currentTimeMillis() - current.lastSaveTime;
            saveManualProgress(context, delta, current.isBreak);
        }
        
        TimerStateManager.TimerState newState = new TimerStateManager.TimerState(false, current.totalLimitMs, current.totalLimitMs, current.isBreak, 0L);
        if (stateManager.commitState(newState)) {
            context.stopService(new Intent(context, PomodoroService.class));
        }
    }

    private void saveManualProgress(Context context, long deltaMs, boolean isBreak) {
        if (deltaMs < 1000) return;
        
        int activityId = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(Constants.CURRENT_ACTIVITY_ID, 1);
        
        if (isBreak) {
            Utility.updateDatabaseBreakTimeOnly(context, deltaMs, activityId);
        } else {
            Utility.updateDatabaseWorkTimeOnly(context, deltaMs, activityId);
        }
    }
}
