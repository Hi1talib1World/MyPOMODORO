package com.denzo.mypomodoro.statistics.activitychart;

final class PieChartHelperElement {

    private final int id;
    private float y;

    PieChartHelperElement(int id) {
        this.id = id;
        this.y = 0;
    }

    void setY(float y) {
        this.y = y;
    }

    int getId() {
        return id;
    }

    float getY() {
        return y;
    }

    @Override
    public String toString() {
        return "PieChartHelperElement{" +
                "id=" + id +
                ", y=" + y +
                '}';
    }
}
