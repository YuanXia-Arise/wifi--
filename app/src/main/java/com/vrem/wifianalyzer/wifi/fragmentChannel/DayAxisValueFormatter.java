package com.vrem.wifianalyzer.wifi.fragmentChannel;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;


public class DayAxisValueFormatter implements IAxisValueFormatter {


    private final BarLineChartBase<?> chart;

    public DayAxisValueFormatter(BarLineChartBase<?> chart) {
        this.chart = chart;
    }

    private String[] channelString = {"信道1","信道2","信道3","信道4","信道5","信道6","信道7","信道8","信道9","信道10",
            "信道11","信道12","信道13","信道14", "信道36","信道38","信道40","信道42","信道44","信道46","信道48","信道52",
            "信道56","信道60","信道64","信道149","信道153","信道157","信道161","信道165"};
    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        int num = (int) value;
        String x_channel = channelString[num];
        return x_channel;
    }

    private int getDaysForMonth(int month, int year) {
        // month is 0-based
        if (month == 1) {
            boolean is29Feb = false;

            if (year < 1582)
                is29Feb = (year < 1 ? year + 1 : year) % 4 == 0;
            else if (year > 1582)
                is29Feb = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);

            return is29Feb ? 29 : 28;
        }
        if (month == 3 || month == 5 || month == 8 || month == 10)
            return 30;
        else
            return 31;
    }

    private int determineMonth(int dayOfYear) {
        int month = -1;
        int days = 0;
        while (days < dayOfYear) {
            month = month + 1;

            if (month >= 12)
                month = 0;

            int year = determineYear(days);
            days += getDaysForMonth(month, year);
        }
        return Math.max(month, 0);
    }

    private int determineDayOfMonth(int days, int month) {
        int count = 0;
        int daysForMonths = 0;
        while (count < month) {
            int year = determineYear(daysForMonths);
            daysForMonths += getDaysForMonth(count % 12, year);
            count++;
        }
        return days - daysForMonths;
    }

    private int determineYear(int days) {

        if (days <= 366)
            return 2016;
        else if (days <= 730)
            return 2017;
        else if (days <= 1094)
            return 2018;
        else if (days <= 1458)
            return 2019;
        else
            return 2020;
    }
}
