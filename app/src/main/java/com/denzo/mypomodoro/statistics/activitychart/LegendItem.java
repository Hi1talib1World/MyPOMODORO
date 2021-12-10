package com.denzo.mypomodoro.statistics.activitychart;

public final class LegendItem {

    private final String name;
    private final String time;
    private final double percent;

    public LegendItem(String name, double percent, String time) {
        this.name = name;
        this.time = time;
        this.percent = percent;
    }

    public double getPercent() {
        return percent;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public int getTimeLength() {
        return time.length();
    }
}