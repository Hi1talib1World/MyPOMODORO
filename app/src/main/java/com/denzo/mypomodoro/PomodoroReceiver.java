package com.denzo.mypomodoro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

public class PomodoroReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int activityId = prefs.getInt(Constants.CURRENT_ACTIVITY_ID, 1);

        Intent actionIntent = new Intent(context, TimerActionReceiver.class);
        actionIntent.putExtra(Constants.CURRENT_ACTIVITY_ID_INTENT, activityId);

        if ("com.denzo.mypomodoro.PLAY".equals(action)) {
            boolean isRunning = prefs.getBoolean(Constants.IS_TIMER_RUNNING, false);
            if (isRunning) {
                actionIntent.putExtra(Constants.BUTTON_ACTION, Constants.BUTTON_PAUSE);
            } else {
                actionIntent.putExtra(Constants.BUTTON_ACTION, Constants.BUTTON_START);
            }
            context.sendBroadcast(actionIntent);
        } else if ("com.denzo.mypomodoro.STOP".equals(action)) {
            actionIntent.putExtra(Constants.BUTTON_ACTION, Constants.BUTTON_STOP);
            context.sendBroadcast(actionIntent);
        }
    }
}
