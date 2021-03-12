package com.vrem.wifianalyzer.wifi.fragmentChannel;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.gson.Gson;
import com.vrem.wifianalyzer.GetCompany;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.common.MacSsidDBUtils;
import com.vrem.wifianalyzer.wifi.common.TargetMacDBUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChannelListViewAdapter extends BaseAdapter {

    private Context context;
    private List<List<String>> list_data = new ArrayList<>();

    public ChannelListViewAdapter(Context context, List<List<String>> list_data) {
        this.context = context;
        this.list_data = list_data;
    }

    @Override
    public int getCount() {
        return list_data.size();
    }

    /*@Override
    public Object getItem(int position) {
        return list_data.get(position);
    }*/
    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    /*@Override
    public long getItemId(int position) {
        return position;
    }*/
    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (view == null){
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.channel_list,null);
            viewHolder.ssid = (TextView) view.findViewById(R.id.ssid);
            viewHolder.mac = (TextView) view.findViewById(R.id.mac);
            viewHolder.signal = (TextView) view.findViewById(R.id.signal);
            viewHolder.factory = (TextView) view.findViewById(R.id.factory);
            viewHolder.time = (TextView) view.findViewById(R.id.time);
            viewHolder.factory.setSelected(true);
            viewHolder.time.setSelected(true);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        if (list_data.get(position).get(0).equals("")){
            viewHolder.ssid.setText("SSID:" + list_data.get(position).get(0));
            viewHolder.ssid.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.ssid.setText("SSID:" + list_data.get(position).get(0));
            viewHolder.ssid.setVisibility(View.VISIBLE);
        }
        //viewHolder.ssid.setText("SSID:" + list_data.get(position).get(0));

        if (list_data.get(position).get(0).length() > 17){
            viewHolder.mac.setText("MAC:" + list_data.get(position).get(1).substring(0,17));
            viewHolder.mac.setTextColor(Color.RED);

            MacSsidDBUtils macSsidDBUtils = new MacSsidDBUtils(context);
            macSsidDBUtils.open();
            String bssid = list_data.get(position).get(1).substring(0, 17);
            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String sd = sdf.format(new Date(Long.parseLong(macSsidDBUtils.getTime(bssid))));
            macSsidDBUtils.close();
            viewHolder.time.setText("最后更新于:" + sd);
            viewHolder.time.setTextColor(Color.RED);
        } else {
            viewHolder.mac.setText("MAC:" + list_data.get(position).get(1));
            viewHolder.mac.setTextColor(Color.WHITE);
            viewHolder.time.setText("");
            viewHolder.time.setTextColor(Color.WHITE);
        }
        //viewHolder.mac.setText("MAC:" + list_data.get(position).get(1).substring(0,17));
        viewHolder.signal.setText("信号强度:" + list_data.get(position).get(2));
        GetCompany getCompany = new GetCompany();
        viewHolder.factory.setText("厂商:" + getCompany.read_csv(list_data.get(position).get(1).substring(0, 8).replace(":", "").toUpperCase()));
        //notifyDataSetChanged();
        return view;
    }

   private class ViewHolder{
        TextView ssid;
        TextView mac;
        TextView signal;
        TextView factory;
        TextView time;
    }

}
