package com.vrem.wifianalyzer.wifi.fragmentChannelRate;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

public class ValueFormatter implements IValueFormatter {

    private final DecimalFormat mFormat;

    public ValueFormatter() {
        mFormat = new DecimalFormat("###,###,###,##0.000");
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        if (value == 0.0){
            return "";
        } else {
            return mFormat.format(value);
        }
        //return mFormat.format(value);
    }
}
