package com.denzo.mypomodoro.statistics.historychart;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.time.DayOfWeek.MONDAY;

public final class WeekData extends ChartData {

    private List<HistoryChartItem> weeks;

    public WeekData(List<HistoryChartItem> data) {
        super(data);
    }

    public void generate() {
        prepareWeeks(LocalDate.now());
        createEntries(weeks);
    }

    @Override
    public List<HistoryChartItem> getGeneratedData() {
        return new ArrayList<>(weeks);
    }

    /**
     * Prepares week data by making sure that there is always at least 12 entries.
     * Also, fills any gaps between weeks.
     *
     * @param currentDate current date
     */
    public void prepareWeeks(LocalDate currentDate) {
        weeks = getData();

        LocalDate firstDayOfCurrentWeek = currentDate;
        DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

        if (dayOfWeek != MONDAY) {
            firstDayOfCurrentWeek = currentDate.minusDays(dayOfWeek.getValue() - 1);
        }

        // Use Locale.GERMAN so that the first day of week is Monday. This makes it consistent with the SQLite query.
        TemporalField weekOfYear = WeekFields.of(Locale.GERMAN).weekOfWeekBasedYear();

        if (weeks.size() == 1) {
            if (weeks.get(0).getDate().get(weekOfYear) != firstDayOfCurrentWeek.get(weekOfYear)) {
                weeks.add(HistoryChartItem.of(firstDayOfCurrentWeek));
            }
        }

        for (int i = 0; i < weeks.size() - 1; i++) {
            LocalDate thisWeek = weeks.get(i).getDate().plusWeeks(1);
            LocalDate nextWeek = weeks.get(i + 1).getDate();

            if (thisWeek.get(weekOfYear) != nextWeek.get(weekOfYear) || thisWeek.getYear() != nextWeek.getYear()) {
                weeks.add(i + 1, HistoryChartItem.of(thisWeek));
                continue;
            }

            if (i + 1 == weeks.size() - 1 &&
                    weeks.get(i + 1).getDate().get(weekOfYear) != firstDayOfCurrentWeek.get(weekOfYear)) {
                weeks.add(HistoryChartItem.of(thisWeek.plusWeeks(1)));
            }
        }

        if (weeks.size() == 0) {
            weeks.add(HistoryChartItem.of(firstDayOfCurrentWeek));
        }

        if (weeks.size() < 12) {
            LocalDate firstWeek = weeks.get(0).getDate();

            for (int i = 12 - weeks.size(); i > 0; i--) {
                firstWeek = firstWeek.minusWeeks(1);
                weeks.add(0, HistoryChartItem.of(firstWeek));
            }
        }
    }
}