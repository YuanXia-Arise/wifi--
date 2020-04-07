package com.vrem.wifianalyzer.wifi.fragmentTargetSearch;

/**
 * Created by huangche on 2019/12/26.
 */

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.gson.Gson;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;
import com.vrem.wifianalyzer.wifi.model.ClientInfo;

import java.util.List;

import static android.content.ContentValues.TAG;

public class ClientListSearchAdapter extends BaseAdapter{
    private Context context;
    private List<ClientInfo_Search> listItems;
    private LayoutInflater listContainer;
    private int itemViewResource;
    static class ListItemView{
        public TextView mac;
        public TextView probe;
        public TextView company;
        public TextView hotspot;
        public TextView power;
    }

    public ClientListSearchAdapter(Context context, List<ClientInfo_Search> data, int resource){
        this.context = context;
        this.listContainer = LayoutInflater.from(context);
        this.itemViewResource = resource;
        this.listItems = data;
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return listItems.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ListItemView listItemView = null;
        if(convertView == null){
            convertView = listContainer.inflate(this.itemViewResource, null);
            listItemView = new ListItemView();
            listItemView.mac = convertView.findViewById(R.id.mac);
            listItemView.probe = convertView.findViewById(R.id.probe);
            listItemView.company = convertView.findViewById(R.id.company);
            listItemView.hotspot = convertView.findViewById(R.id.client_hotspot);
            listItemView.power = convertView.findViewById(R.id.client_power);
            convertView.setTag(listItemView);
        }else{
            listItemView = (ListItemView) convertView.getTag();
        }
        final ClientInfo_Search clientInfo = listItems.get(position);

        listItemView.mac.setTag(clientInfo);
        listItemView.mac.setText(clientInfo.getMac());
        if(clientInfo.getProbe().equals(""))
            listItemView.probe.setText("无");
        else{
            listItemView.probe.setText(clientInfo.getProbe());
        }
        listItemView.company.setText(clientInfo.getCompany());
        listItemView.company.setSelected(true);
        listItemView.hotspot.setText(clientInfo.getSSID());

        listItemView.power.setText(clientInfo.getPower()); //客户端信号

        return convertView;
    }

}