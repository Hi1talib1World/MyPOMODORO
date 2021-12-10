package com.denzo.mypomodoro.statistics.historychart;

import androidx.annotation.NonNull;
import androidx.room.Ignore;


import java.time.LocalDate;

public final class HistoryChartItem implements Comparable<HistoryChartItem> {

    private final String date;

    /** Sum of CompletedWorkTime and IncompleteWorkTime from {@link Pomodoro}. */
    private final long time;

    private final int activityId;

    public HistoryChartItem(String date, long time, int activityId) {
        this.date = date;
        this.time = time;
        this.activityId = activityId;
    }

    @Ignore
    public HistoryChartItem(String Date) {
        this(Date, 0, 0);
    }

    public static HistoryChartItem of(LocalDate LocalDate, long totalTime, int activityId) {
        return new HistoryChartItem(LocalDate.toString(), totalTime, activityId);
    }

    public static HistoryChartItem of(LocalDate LocalDate) {
        return new HistoryChartItem(LocalDate.toString(), 0, 0);
    }

    public LocalDate getDate() {
        return LocalDate.parse(date);
    }

    public long getTime() {
        return time;
    }

    public int getActivityId() {
        return activityId;
    }

    @Override
    public int compareTo(@NonNull HistoryChartItem historyChartItem) {
        return getDate().compareTo(historyChartItem.getDate());
    }

    @Override
    public String toString() {
        return "HistoryChartItem{" +
                "date='" + date + '\'' +
                ", time=" + time +
                ", activityId=" + activityId +
                '}';
    }
}