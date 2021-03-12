package com.vrem.wifianalyzer.wifi.fragmentTargetSearch;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.common.TargetMacDBUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MacListViewAdapter extends BaseAdapter {

    private Context context;
    private List<String> list_data = new ArrayList<>(); // 列表listview本地数据

    private int selectedPosition = -1;
    public void setSelectedPosition(int position) {
        selectedPosition = position;
    }

    public MacListViewAdapter(Context context, List<String> list_data) {
        this.context = context;
        this.list_data = list_data;
    }

    @Override
    public int getCount() {
        return list_data.size();
    }

    @Override
    public Object getItem(int position) {
        return list_data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (view == null){
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.search_mac_list,null);
            viewHolder.textView1 = (TextView) view.findViewById(R.id.text1);
            viewHolder.textView2 = (TextView) view.findViewById(R.id.text2);
            viewHolder.textView3 = (TextView) view.findViewById(R.id.text3);
            viewHolder.remarks = view.findViewById(R.id.remarks);
            viewHolder.textView2.setSelected(true);
            viewHolder.textView3.setSelected(true);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        TargetMacDBUtils targetMacDBUtils = new TargetMacDBUtils(context);
        targetMacDBUtils.open();
        viewHolder.textView1.setText(list_data.get(i));
        viewHolder.textView2.setText(targetMacDBUtils.getCompany(list_data.get(i)));
        viewHolder.textView3.setText(targetMacDBUtils.getRemarks(list_data.get(i)));
        if (targetMacDBUtils.getRemarks(list_data.get(i)).equals("")){
            viewHolder.remarks.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.remarks.setVisibility(View.VISIBLE);
        }
        targetMacDBUtils.close();

        return view;
    }

   private class ViewHolder{
        TextView textView1;
        TextView textView2;
        TextView textView3;
        TextView remarks;
    }

}
