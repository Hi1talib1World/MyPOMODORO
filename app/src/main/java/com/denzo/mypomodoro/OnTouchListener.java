package com.denzo.mypomodoro;


import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public abstract class OnTouchListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;

    public OnTouchListener(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public abstract void onSwipeLeft();

    public abstract void onUp();

    public abstract void onDown();

    public abstract void onMyLongPress();

    public abstract void onTap();

    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            onDown();
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            onUp();
        }

        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_DISTANCE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public void onLongPress(MotionEvent e) {
            onMyLongPress();
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            onTap();
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float distanceX = e2.getX() - e1.getX();
            float distanceY = e2.getY() - e1.getY();

            if (Math.abs(distanceX) > Math.abs(distanceY) &&
                    Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD &&
                    Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {

                if (distanceX < 0) {
                    onSwipeLeft();
                }
                return true;
            }
            return false;
        }
    }
}
