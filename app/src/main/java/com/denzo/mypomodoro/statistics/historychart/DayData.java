package com.denzo.mypomodoro.statistics.historychart;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class DayData extends ChartData {

    private List<HistoryChartItem> days;

    public DayData(List<HistoryChartItem> data) {
        super(data);
    }

    public void generate() {
        prepareDays(LocalDate.now());
        createEntries(days);
    }

    @Override
    public List<HistoryChartItem> getGeneratedData() {
        return new ArrayList<>(days);
    }

    /**
     * Prepares day data by making sure that there is always at least 12 entries.
     * Also, fills any gaps between dates.
     *
     * @param currentDate current date
     */
    public void prepareDays(LocalDate currentDate) {
        days = getData();

        if (days.size() == 1) {
            if (!days.get(0).getDate().equals(currentDate)) {
                days.add(HistoryChartItem.of(currentDate));
            }
        }

        for (int i = 0; i < days.size() - 1; i++) {
            LocalDate todayDate = days.get(i).getDate().plusDays(1);
            LocalDate nextDate = days.get(i + 1).getDate();

            if (!todayDate.equals(nextDate)) {
                days.add(i + 1, HistoryChartItem.of(todayDate));
                continue;
            }

            if (i + 1 == days.size() - 1 && !days.get(i + 1).getDate().equals(currentDate)) {
                days.add(HistoryChartItem.of(todayDate.plusDays(1)));
            }
        }

        if (days.size() == 0) {
            days.add(HistoryChartItem.of(currentDate));
        }

        if (days.size() < 12) {
            LocalDate firstDay = days.get(0).getDate();

            for (int i = 12 - days.size(); i > 0; i--) {
                firstDay = firstDay.minusDays(1);
                days.add(0, HistoryChartItem.of(firstDay));
            }
        }
    }
}