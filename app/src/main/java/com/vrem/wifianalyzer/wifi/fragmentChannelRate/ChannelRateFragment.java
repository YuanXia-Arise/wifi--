package com.vrem.wifianalyzer.wifi.fragmentChannelRate;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.Gson;
import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.common.BackgroundTask;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;
import com.vrem.wifianalyzer.wifi.common.VolleySingleton;
import com.vrem.wifianalyzer.wifi.fragmentChannel.ChannelListViewAdapter;
import com.vrem.wifianalyzer.wifi.fragmentChannel.DayAxisValueFormatter;
import com.vrem.wifianalyzer.wifi.fragmentChannel.MyAxisValueFormatter;
import com.vrem.wifianalyzer.wifi.fragmentChannel.MyValueFormatter;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Huangche on 2020/9/27.
 */

public class ChannelRateFragment extends Fragment implements OnChartValueSelectedListener, View.OnClickListener {

    private View view;
    private BarChart chart;

    private List<WiFiDetail> wiFiDetailList;//wifi列表
    private IAxisValueFormatter xAxisFormatter;
    private Button Rx, Tx;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view  = inflater.inflate(R.layout.fragment_channelrate, container, false);

        Rx = view.findViewById(R.id.rx);
        Rx.setOnClickListener(this);
        Tx = view.findViewById(R.id.tx);
        Tx.setOnClickListener(this);

        chart = view.findViewById(R.id.chart_channel);
        chart.setOnChartValueSelectedListener(this);
        chart.getDescription().setEnabled(false);
        //chart.setMaxVisibleValueCount(10);
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(false);
        chart.setHighlightFullBarEnabled(false);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setValueFormatter(new AxisValueFormatter());
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        leftAxis.setAxisMaximum(5f);
        leftAxis.setLabelCount(20, false);
        leftAxis.setTextColor(Color.WHITE);
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setValueFormatter(new AxisValueFormatter());
        rightAxis.setAxisMinimum(0f);
        rightAxis.setAxisMaximum(5f);
        rightAxis.setLabelCount(20, false);
        rightAxis.setTextColor(Color.WHITE);

        xAxisFormatter = new DayAxisValueFormatter(chart);
        XAxis xLabels = chart.getXAxis();
        xLabels.setPosition(XAxis.XAxisPosition.BOTTOM);
        xLabels.setDrawGridLines(false);
        xLabels.setGranularity(1f);
        xLabels.setLabelCount(30);
        xLabels.setLabelRotationAngle(90f); // 字体显示角度
        xLabels.setTextColor(Color.WHITE);
        xLabels.setValueFormatter(xAxisFormatter);

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(10f);

        Rx.setEnabled(false);
        Tx.setEnabled(true);
        Channel_data(0);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rx:
                Rx.setEnabled(false);
                Tx.setEnabled(true);
                Channel_data(0);
                break;
            case R.id.tx:
                Rx.setEnabled(true);
                Tx.setEnabled(false);
                Channel_data(1);
                break;
                default:
                    break;
        }
    }

    public void Channel_data(final int type){
        BackgroundTask.clearAll();
        BackgroundTask.mTimerScan       = new Timer();
        BackgroundTask.mTimerTaskScan   = new TimerTask() {
            @Override
            public void run() {
                if (getActivity() == null){
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (type == 0){
                            list_rx.clear();
                            for (int m = 0; m < 30; m++){
                                list_rx.add(0);
                            }
                        } else {
                            list_tx.clear();
                            for (int m = 0; m < 30; m++){
                                list_tx.add(0);
                            }
                        }

                        MainContext.INSTANCE.getScannerService().update();
                        wiFiDetailList = MainContext.INSTANCE.getScannerService().getWiFiData().getWiFiDetails();
                        try{
                            for (int i = 0; i < wiFiDetailList.size(); i++){
                                String channel = wiFiDetailList.get(i).getWiFiSignal().getChannel(); // 信道获取
                                JSONArray client = new JSONArray(wiFiDetailList.get(i).getClient()); // 信道下客户端
                                for (int m = 0; m < client.length(); m++){
                                    JSONObject client_data = (JSONObject) client.get(m);
                                    int Rx_datas = Integer.valueOf(client_data.getString("rx_datas"));
                                    int Tx_datas = Integer.valueOf(client_data.getString("tx_datas"));
                                    Channel_Rx(channel,Rx_datas,type);
                                    if (type == 0) {
                                        Channel_Rx(channel,Rx_datas,type);
                                    } else {
                                        Channel_Rx(channel,Tx_datas,type);
                                    }
                                }
                            }
                        } catch (NullPointerException e){
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        ArrayList<BarEntry> values = new ArrayList<>();
                        for (int i = 0; i < 30; i++) {
                            if (type == 0){
                                float val1 = Float.valueOf(list_rx.get(i))/(1024*1024);
                                values.add(new BarEntry(i, new float[]{val1}));
                            } else {
                                float val1 = Float.valueOf(list_tx.get(i))/(1024*1024);
                                values.add(new BarEntry(i, new float[]{val1}));
                            }
                        }

                        chart.setVisibleYRangeMaximum(1f,YAxis.AxisDependency.LEFT);
                        chart.setVisibleYRangeMaximum(1f,YAxis.AxisDependency.RIGHT);
                        BarDataSet set1;
                        /*if (chart.getData() != null && chart.getData().getDataSetCount() > 0) {
                            set1 = (BarDataSet) chart.getData().getDataSetByIndex(0);
                            set1.setValues(values);
                            chart.getData().notifyDataChanged();
                            chart.notifyDataSetChanged();
                        } else {*/
                            if (type == 0){
                                set1 = new BarDataSet(values, "信道接收速率(Mb)");
                            } else {
                                set1 = new BarDataSet(values, "信道发送速率(Mb)");
                            }
                            set1.setDrawIcons(false);
                            set1.setColors(getColors()[type]);
                            set1.setStackLabels(new String[]{"发送","接收"});
                            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
                            dataSets.add(set1);
                            BarData data = new BarData(dataSets);
                            data.setValueFormatter(new ValueFormatter());
                            data.setValueTextColor(Color.WHITE);
                            chart.setData(data);
                        //}
                        chart.setFitBars(true);
                        chart.invalidate();
                    }
                });
            }
        };
        BackgroundTask.mTimerScan.schedule(BackgroundTask.mTimerTaskScan, 0, 2000);
    }

    @Override
    public void onStart() {
        Log.d("ChannelFragment status：","Start");
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d("ChannelFragment status：","Resume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d("ChannelFragment status：","Pause");
        super.onPause();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    // 信道处理
    public void Channel_Rx(String channel, int datas, int type){
        int num = Integer.valueOf(channel);
        if (num < 15){
            SetData(num,datas,type);
        } else {
            switch (num){
                case 36:
                    SetData(15,datas,type);
                    break;
                case 38:
                    SetData(16,datas,type);
                    break;
                case 40:
                    SetData(17,datas,type);
                    break;
                case 42:
                    SetData(18,datas,type);
                    break;
                case 44:
                    SetData(19,datas,type);
                    break;
                case 46:
                    SetData(20,datas,type);
                    break;
                case 48:
                    SetData(21,datas,type);
                    break;
                case 52:
                    SetData(22,datas,type);
                    break;
                case 56:
                    SetData(23,datas,type);
                    break;
                case 60:
                    SetData(24,datas,type);
                    break;
                case 64:
                    SetData(25,datas,type);
                    break;
                case 149:
                    SetData(26,datas,type);
                    break;
                case 153:
                    SetData(27,datas,type);
                    break;
                case 157:
                    SetData(28,datas,type);
                    break;
                case 161:
                    SetData(29,datas,type);
                    break;
                case 165:
                    SetData(30,datas,type);
                    break;
            }
        }
    }

    private List<Integer> list_rx = new ArrayList<>(); //接收数据集合
    private List<Integer> list_tx = new ArrayList<>(); //发送数据集合
    // 信道数据处理
    public void SetData(int channel, int datas, int type){
        if (type == 0){
            int rx = list_rx.get(channel - 1);
            list_rx.remove(channel - 1);
            list_rx.add(channel - 1, rx + datas);
        } else {
            int tx = list_tx.get(channel - 1);
            list_tx.remove(channel - 1);
            list_tx.add(channel - 1, tx + datas);
        }
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    private int[] getColors() {
        int[] colors = new int[2];
        System.arraycopy(ColorTemplate.MATERIAL_COLORS, 0, colors, 0, 2);
        return colors;
    }
}
