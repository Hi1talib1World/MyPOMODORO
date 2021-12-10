package com.denzo.mypomodoro.statistics.historychart;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

public abstract class ChartData {

    private final List<Entry> entries;
    private final List<HistoryChartItem> data;
    private long maxValue = 0;

    ChartData(List<HistoryChartItem> data) {
        this.data = data;
        entries = new ArrayList<>();
    }

    public abstract void generate();

    abstract List<HistoryChartItem> getGeneratedData();

    void createEntries(List<HistoryChartItem> historyChartItems) {
        entries.clear();

        maxValue = 0;

        for (int i = 0; i < historyChartItems.size(); i++) {
            long totalTime = historyChartItems.get(i).getTime();

            entries.add(new Entry(i, totalTime));

            if (totalTime > maxValue) {
                maxValue = totalTime;
            }
        }
    }

    List<Entry> getEntries() {
        return new ArrayList<>(entries);
    }

    long getSize() {
        return entries.size();
    }

    long getMaxValue() {
        return maxValue;
    }

    List<HistoryChartItem> getData() {
        return new ArrayList<>(data);
    }
}
