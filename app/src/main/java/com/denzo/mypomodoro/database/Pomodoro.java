package com.denzo.mypomodoro.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public final class Pomodoro {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "Date")
    private final String Date;

    @ColumnInfo(name = "CompletedWorks")
    private final int CompletedWorks;

    @ColumnInfo(name = "CompletedWorkTime")
    private final long CompletedWorkTime;

    @ColumnInfo(name = "IncompleteWorks")
    private final int IncompleteWorks;

    @ColumnInfo(name = "IncompleteWorkTime")
    private final long IncompleteWorkTime;

    @ColumnInfo(name = "Breaks")
    private final int Breaks;

    @ColumnInfo(name = "BreakTime")
    private final long BreakTime;

    @ColumnInfo(name = "ActivityId")
    private final int activityId;

    public Pomodoro(@NonNull String Date, int CompletedWorks, int CompletedWorkTime,
                    int IncompleteWorks, int IncompleteWorkTime, int Breaks, int BreakTime, int activityId) {
        this.Date = Date;
        this.CompletedWorks = CompletedWorks;
        this.CompletedWorkTime = CompletedWorkTime;
        this.IncompleteWorks = IncompleteWorks;
        this.IncompleteWorkTime = IncompleteWorkTime;
        this.Breaks = Breaks;
        this.BreakTime = BreakTime;
        this.activityId = activityId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getActivityId() {
        return activityId;
    }

    @NonNull
    String getDate() {
        return Date;
    }

    int getCompletedWorks() {
        return CompletedWorks;
    }

    long getCompletedWorkTime() {
        return CompletedWorkTime;
    }

    public int getIncompleteWorks() {
        return IncompleteWorks;
    }

    public long getIncompleteWorkTime() {
        return IncompleteWorkTime;
    }

    int getBreaks() {
        return Breaks;
    }

    long getBreakTime() {
        return BreakTime;
    }
}