package com.denzo.mypomodoro;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {


    public int counter;
    private TextView countdownText;
    Button button;
    CountDownTimer countDownTimer;
    private long timeLeftMillSeconds = 15000000;
    private boolean timerRunning;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        countdownText = findViewById(R.id.countdown_text);
        button = findViewById(R.id.button);

        updateTimer();
    }
    public void startStop(){
        if (timerRunning){
            stopTimer();
        }else {
            startTimer();
        }
    }
    public void startTimer(){
        countDownTimer = new CountDownTimer(timeLeftMillSeconds, 1000){
            @Override
            public void onTick(long l){
                timeLeftMillSeconds = 1;
                updateTimer();
            }
            @Override
            public void onFinish(){

            }
        }.start();
        button.setText("PAUSE");
        timerRunning = true;
    }

    public void stopTimer(){
        countDownTimer.cancel();
        button.setText("START");
        timerRunning = false;
    }
    public void updateTimer(){
        int minutes = (int) timeLeftMillSeconds / 15000000;
        int seconds = (int) timeLeftMillSeconds % 15000000 / 1000;

        String timeLeftText;

        timeLeftText = "" + minutes;
        timeLeftText += ":";
        if (seconds < 10) timeLeftText += "0";
        timeLeftText += seconds;
        countdownText.setText(timeLeftText);


    }

}
