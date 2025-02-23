package com.denzo.mypomodoro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PomodoroReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if ("com.denzo.mypomodoro.PLAY".equals(action)) {
            // Handle the play action
            // Start the timer or resume it
            Intent serviceIntent = new Intent(context, PomodoroService.class);
            context.startService(serviceIntent);
        } else if ("com.denzo.mypomodoro.STOP".equals(action)) {
            // Handle the stop action
            // Stop the timer or pause it
            context.stopService(new Intent(context, PomodoroService.class));
        }
    }
}
