package com.vrem.wifianalyzer.wifi.deviceList.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.vrem.wifianalyzer.R;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class DeviceAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private String deviceName[];
//    private int image[];
    private String deviceStatus[];
    private String[] deviceBtty;
    private int workType[];

    public DeviceAdapter(Context context, /*int[] image_Objects, */String[] deviceName, String[] deviceStatus, String[] deviceBtty,int[] workType){
        this.layoutInflater = LayoutInflater.from(context);
        this.deviceName     = deviceName;
        this.deviceStatus   = deviceStatus;
        this.deviceBtty     = deviceBtty;
        this.workType       = workType;
    }

    @Override
    public int getCount() {
        return deviceName.length;
    }

    @Override
    public Object getItem(int position) {
        return deviceName[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.device_list,null);
        MyClass myClass = new MyClass();
        myClass.imageView = convertView.findViewById(R.id.image_id);
        myClass.nameView  = convertView.findViewById(R.id.name);
        myClass.moodView  = convertView.findViewById(R.id.mood);
        myClass.battyView = convertView.findViewById(R.id.batty);

        myClass.nameView.setText(deviceName[position]);
        myClass.moodView.setText(deviceStatus[position]);
        int Battery = Integer.valueOf(deviceBtty[position]);
        if (Battery > 99) {
            Battery = 100;
            myClass.battyView.setText(Integer.toString(Battery) + "%");
        } else if (Battery == 0) {
            myClass.battyView.setText(Integer.toString(Battery)  + "%");
        } else if (Battery == -1) {
            myClass.battyView.setText("未连接电池");
        } else {
            myClass.battyView.setText(Integer.toString(Battery)  + "%");
        }
        myClass.nameView.setTextColor(Color.WHITE);

        if (Battery < 70 && Battery > 40){
            myClass.battyView.setTextColor(Color.WHITE);
        } else if (Battery <= 40 && Battery >= 0){
            myClass.battyView.setTextColor(Color.RED);
        } else if (Battery == -1){
            myClass.battyView.setTextColor(Color.RED);
        } else {
            myClass.battyView.setTextColor(Color.GREEN);
        }
        String deviceStatusFlag = deviceStatus[position];

        if (workType[position] == 100){
            myClass.moodView.setTextColor(Color.GREEN);
            myClass.imageView.setBackgroundResource(R.drawable.wifigreen);
        } else {
            myClass.moodView.setTextColor(Color.RED);
            myClass.imageView.setBackgroundResource(R.drawable.wifiblue);
        }
        return convertView;
    }

    class MyClass{
        ImageView imageView;
        TextView nameView;
        TextView moodView;
        TextView battyView;
    }
}