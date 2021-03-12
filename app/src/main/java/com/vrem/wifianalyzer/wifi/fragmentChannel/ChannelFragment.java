package com.vrem.wifianalyzer.wifi.fragmentChannel;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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
import com.vrem.wifianalyzer.wifi.common.GetWpsUpdater;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;
import com.vrem.wifianalyzer.wifi.common.VolleySingleton;
import com.vrem.wifianalyzer.wifi.fragmentTargetSearch.ClientInfo_Search;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Huangche on 2020/9/27.
 */

public class ChannelFragment extends Fragment implements OnChartValueSelectedListener{

    private View view;
    private BarChart chart;

    private List<WiFiDetail> wiFiDetailList;//wifi列表
    private IAxisValueFormatter xAxisFormatter;
    private ListView listView;
    private TextView channel_type;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view  = inflater.inflate(R.layout.fragment_channel, container, false);
        listView = view.findViewById(R.id.channel_mar);
        channel_type = view.findViewById(R.id.channel_type);

        chart = view.findViewById(R.id.chart_channel);
        chart.setOnChartValueSelectedListener(this);
        chart.getDescription().setEnabled(false);
        //chart.setMaxVisibleValueCount(10);
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(false);
        chart.setHighlightFullBarEnabled(false);

        YAxis leftAxis = chart.getAxisLeft();//08
        leftAxis.setValueFormatter(new MyAxisValueFormatter());
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        leftAxis.setAxisMaximum(50f);
        leftAxis.setLabelCount(50, false);
        leftAxis.setTextColor(Color.WHITE);
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setValueFormatter(new MyAxisValueFormatter());
        rightAxis.setAxisMinimum(0f);
        rightAxis.setAxisMaximum(50f);
        rightAxis.setLabelCount(50, false);
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

        /*XYMarkerView mv = new XYMarkerView(getContext(), xAxisFormatter,getActivity());
        mv.setChartView(chart); // For bounds control
        chart.setMarker(mv); // Set the marker to the chart*/


        BackgroundTask.clearAll();
        BackgroundTask.mTimerScan       = new Timer();
        BackgroundTask.mTimerTaskScan   = new TimerTask() {
            @Override
            public void run() {
                if (getActivity() == null){ //由于当线程结束时activity变得不可见,getActivity()有可能为空，需要提前判断
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        list_ap.clear();
                        list_client.clear();
                        for (int m = 0; m < 30; m++){
                            list_ap.add(0);
                            list_client.add(0);
                        }
                        MainContext.INSTANCE.getScannerService().update();
                        wiFiDetailList = MainContext.INSTANCE.getScannerService().getWiFiData().getWiFiDetails();
                        try{
                            for (int i = 0; i < wiFiDetailList.size(); i++){
                                String channel = wiFiDetailList.get(i).getWiFiSignal().getChannel(); // 信道获取
                                JSONArray client = new JSONArray(wiFiDetailList.get(i).getClient());
                                Channel_num(channel,client.length()); // 信道下客户端
                            }
                            System.out.println("20200928==1>" + list_client.toString());
                        } catch (NullPointerException e){
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        ArrayList<BarEntry> values = new ArrayList<>();
                        for (int i = 0; i < 30; i++) {
                            float val1 = list_ap.get(i);
                            float val2 = list_client.get(i);
                            values.add(new BarEntry(i, new float[]{val1, val2}));
                        }

                        //chart.setVisibleXRangeMaximum(14.5f); // 横坐标数量
                        chart.setVisibleYRangeMaximum(50f,YAxis.AxisDependency.LEFT);
                        chart.setVisibleYRangeMaximum(50f,YAxis.AxisDependency.RIGHT);
                        BarDataSet set1;
                        if (chart.getData() != null && chart.getData().getDataSetCount() > 0) {
                            set1 = (BarDataSet) chart.getData().getDataSetByIndex(0);
                            set1.setValues(values);
                            chart.getData().notifyDataChanged();
                            chart.notifyDataSetChanged();
                        } else {
                            set1 = new BarDataSet(values, "   信道信息图");
                            set1.setDrawIcons(false);
                            set1.setColors(getColors());
                            set1.setStackLabels(new String[]{"热点", "客户端"});
                            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
                            dataSets.add(set1);
                            BarData data = new BarData(dataSets);
                            data.setValueFormatter(new MyValueFormatter());
                            data.setValueTextColor(Color.WHITE);
                            chart.setData(data);
                        }
                        chart.setFitBars(true);
                        chart.invalidate();
                    }
                });
            }
        };
        BackgroundTask.mTimerScan.schedule(BackgroundTask.mTimerTaskScan, 0, 2000);

        /*list_ap.clear();
        list_client.clear();
        for (int m = 0; m < 30; m++){
            list_ap.add(0);
            list_client.add(0);
        }

        wiFiDetailList = MainContext.INSTANCE.getScannerService().getWiFiData().getWiFiDetails();
        try{
            for (int i = 0; i < wiFiDetailList.size(); i++){
                String channel = wiFiDetailList.get(i).getWiFiSignal().getChannel(); //信道获取
                JSONArray client = new JSONArray(wiFiDetailList.get(i).getClient());
                Channel_num(channel,client.length());
            }
        } catch (NullPointerException e){
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayList<BarEntry> values = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            float val1 = list_ap.get(i);
            float val2 = list_client.get(i);
            values.add(new BarEntry(i, new float[]{val1, val2}));
        }
        BarDataSet set1;
        if (chart.getData() != null && chart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) chart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(values, "信道信息图");
            set1.setDrawIcons(false);
            set1.setColors(getColors());
            set1.setStackLabels(new String[]{"热点", "客户端"});
            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            BarData data = new BarData(dataSets);
            data.setValueFormatter(new MyValueFormatter());
            data.setValueTextColor(Color.WHITE);
            chart.setData(data);
        }
        chart.setFitBars(true);
        chart.invalidate();*/

        return view;
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
        BackgroundTask.clearAll();
        Log.d("ChannelFragment status：","Pause");
        super.onPause();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private List<Integer> list_ap = new ArrayList<>(); //热点数据集合
    private List<Integer> list_client = new ArrayList<>(); //客户端数据集合
    public void Channel_num(String channel, int client_num){
        int num = Integer.valueOf(channel);
        if (num < 15){
            SetData(num,client_num);
        } else {
            switch (num){
                case 36:
                    SetData(15,client_num);
                    break;
                case 38:
                    SetData(16,client_num);
                    break;
                case 40:
                    SetData(17,client_num);
                    break;
                case 42:
                    SetData(18,client_num);
                    break;
                case 44:
                    SetData(19,client_num);
                    break;
                case 46:
                    SetData(20,client_num);
                    break;
                case 48:
                    SetData(21,client_num);
                    break;
                case 52:
                    SetData(22,client_num);
                    break;
                case 56:
                    SetData(23,client_num);
                    break;
                case 60:
                    SetData(24,client_num);
                    break;
                case 64:
                    SetData(25,client_num);
                    break;
                case 149:
                    SetData(26,client_num);
                    break;
                case 153:
                    SetData(27,client_num);
                    break;
                case 157:
                    SetData(28,client_num);
                    break;
                case 161:
                    SetData(29,client_num);
                    break;
                case 165:
                    SetData(30,client_num);
                    break;
            }
        }
    }
    public void SetData(int num, int client_num){
        int num_ap = 0;
        int num_client = 0;
        num_ap = list_ap.get(num - 1) + 1;
        list_ap.remove(num - 1);
        list_ap.add(num - 1,num_ap);
        num_client = list_client.get(num - 1) + client_num;
        list_client.remove(num - 1);
        list_client.add(num - 1,num_client);
    }

    private int position;
    private String channel;
    @Override
    public void onValueSelected(Entry e, Highlight h) {
        int pos = h.getStackIndex();
        String cha = xAxisFormatter.getFormattedValue(e.getX(), null).replace("信道", "");
        position = pos;
        channel = cha;
        if (position == 0){
            channel_type.setText("信道" + cha + " " + "热点信息");
        } else {
            channel_type.setText("信道" + cha + " " + "客户端信息");
        }

        System.out.println("20201016==00>" + new Gson().toJson(getData(position,channel)));
        listView.setAdapter(new ChannelListViewAdapter(getContext(),getData(position, channel)));

        //BackgroundTask.clearAll();
        /*BackgroundTask.mTimerScan       = new Timer();
        BackgroundTask.mTimerTaskScan   = new TimerTask() {
            @Override
            public void run() {
                if (getActivity() == null){ //由于当线程结束时activity变得不可见,getActivity()有可能为空，需要提前判断
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (position == 0){
                            channel_type.setText("信道" + channel + " " + "热点信息");
                        } else {
                            channel_type.setText("信道" + channel + " " + "客户端信息");
                        }

                        System.out.println("20201016==10>" + position + " " + channel);
                        System.out.println("20201016==11>" + new Gson().toJson(getData(position,channel)));
                        listView.setAdapter(new ChannelListViewAdapter(getContext(),getData(position, channel)));
                    }
                });
            }
        };
        BackgroundTask.mTimerScan.schedule(BackgroundTask.mTimerTaskScan, 0, 4000);*/
    }

    @Override
    public void onNothingSelected() {

    }

    private int[] getColors() {
        int[] colors = new int[2];
        System.arraycopy(ColorTemplate.MATERIAL_COLORS, 0, colors, 0, 2);
        return colors;
    }


    // 处理点击事件数据
    public List<List<String>> getData(int position, String channel){
        wiFiDetailList = MainContext.INSTANCE.getScannerService().getWiFiData().getWiFiDetails();
        List<List<String>> list = new ArrayList<>();
        try{
            for(int i = 0; i < wiFiDetailList.size(); i++){
                if (channel.equals(wiFiDetailList.get(i).getWiFiSignal().getChannel()) && position == 0){
                    String Ssid = wiFiDetailList.get(i).getSSID();
                    String Mac = wiFiDetailList.get(i).getBSSID();
                    String Signal = String.valueOf(wiFiDetailList.get(i).getWiFiSignal().getLevel());
                    List<String> namesList = Arrays.asList(Ssid, Mac, Signal);
                    list.add(namesList);
                } else if (channel.equals(wiFiDetailList.get(i).getWiFiSignal().getChannel()) && position == 1){
                    if (!"[]".equals(String.valueOf(wiFiDetailList.get(i).getClient()))) {
                        JSONArray client = new JSONArray(wiFiDetailList.get(i).getClient());
                        for (int j = 0; j < client.length(); j++) { //遍历客户端
                            String Mac = client.getJSONObject(j).getString("mac");
                            String Signal = client.getJSONObject(j).getString("power");
                            List<String> namesList = Arrays.asList("", Mac, Signal);
                            list.add(namesList);
                        }
                    }//e0 b9 4d db 50 6a
                }
            }
        } catch (NullPointerException e){

        } catch (Exception e){

        }
        return list;
    }
}
