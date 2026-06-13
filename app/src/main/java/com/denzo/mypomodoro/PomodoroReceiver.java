package com.denzo.mypomodoro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PomodoroReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        Intent timerAction = new Intent(context, TimerActionReceiver.class);
        if ("com.denzo.mypomodoro.PLAY".equals(action)) {
            timerAction.putExtra(Constants.BUTTON_ACTION, Constants.BUTTON_START);
        } else if ("com.denzo.mypomodoro.STOP".equals(action)) {
            timerAction.putExtra(Constants.BUTTON_ACTION, Constants.BUTTON_STOP);
        } else {
            return;
        }
        context.sendBroadcast(timerAction);
    }
}
