package com.denzo.mypomodoro.statistics.activitychart;

import androidx.annotation.NonNull;



public final class PieChartItem {

    /** Sum of CompletedWorkTime and IncompleteWorkTime from {@link Pomodoro}. */
    private final long TotalTime;
    private double Percent;
    private final String ActivityName;

    public PieChartItem(long TotalTime, double Percent, String ActivityName) {
        this.TotalTime = TotalTime;
        this.Percent = Percent;
        this.ActivityName = ActivityName;
    }

    public long getTotalTime() {
        return TotalTime;
    }

    public String getActivityName() {
        return ActivityName;
    }

    public void setPercent(double percent) {
        this.Percent = percent;
    }

    public double getPercent() {
        return Percent;
    }

    @NonNull
    @Override
    public String toString() {
        return "PieChartItem{" +
                "TotalTime=" + TotalTime +
                ", Percent=" + Percent +
                ", ActivityName='" + ActivityName + '\'' +
                '}';
    }
}