package com.vrem.wifianalyzer.wifi.fragmentChannelRate;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DecimalFormat;

public class AxisValueFormatter implements IAxisValueFormatter
{

    private final DecimalFormat mFormat;

    public AxisValueFormatter() {
        mFormat = new DecimalFormat("###,###,###,##0.000");
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        //return mFormat.format(value) + " Mb";
        return mFormat.format(value);
    }
}
