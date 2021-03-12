package com.vrem.wifianalyzer.wifi.common;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.vrem.wifianalyzer.GetCompany;
import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.dosClientModel.DosChildClientModel;
import com.vrem.wifianalyzer.wifi.dosClientModel.DosGroupClientModel;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by ZhenShiJie on 2018/4/25.
 */

public class DosClientExpandableAdapter extends BaseExpandableListAdapter {


    private List<DosGroupClientModel> dosGroupClientModels;
    private String str;

    public DosClientExpandableAdapter(List<DosGroupClientModel> dosGroupClientModels,String str){
        this.dosGroupClientModels = dosGroupClientModels;
        this.str = str;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return dosGroupClientModels.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (!(dosGroupClientModels.get(groupPosition).getDosChildClientModelList() == null)){ //避免子项为空时报NULL异常
            return dosGroupClientModels.get(groupPosition).getDosChildClientModelList().size();
        }else{
            return 0;
        }
    }

    @Override
    public Object getGroup(int groupPosition) {
        return dosGroupClientModels.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return dosGroupClientModels.get(groupPosition).getDosChildClientModelList().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
//        return true;
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolderGroup group;
        if (convertView == null) {
            convertView = View.inflate(MainContext.INSTANCE.getContext(), R.layout.dos_client_group, null);
            //convertView = LayoutInflater.from(MainContext.INSTANCE.getContext()).inflate(R.layout.dos_client_group, parent,false);
            //convertView = LayoutInflater.from(MainContext.INSTANCE.getContext()).inflate(R.layout.dos_client_group, null);
            group = new ViewHolderGroup();
            group.tv_group = convertView.findViewById(R.id.tv_group);
            group.tv_count = convertView.findViewById(R.id.count_data);
            group.tv_rx = convertView.findViewById(R.id.rx_data);
            group.tv_tx = convertView.findViewById(R.id.tx_data);
            convertView.setTag(group);
        } else {
            group = (ViewHolderGroup) convertView.getTag();
        }

        group.tv_group.setText(dosGroupClientModels.get(groupPosition).getGroup_bssid());
        group.tv_count.setText(String.valueOf(dosGroupClientModels.get(groupPosition).getGroup_count()));
        group.tv_rx.setText(String.valueOf(dosGroupClientModels.get(groupPosition).getGroup_tx_datas()));
        group.tv_tx.setText(String.valueOf(dosGroupClientModels.get(groupPosition).getGroup_rx_datas()));
        //notifyDataSetChanged();
        return convertView;
    }
    private static class ViewHolderGroup{
        private TextView tv_group;
        private TextView tv_count;
        private TextView tv_rx;
        private TextView tv_tx;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolderItem item;
        if (convertView == null) {
            convertView = View.inflate(MainContext.INSTANCE.getContext(), R.layout.dos_client_child, null);
            //convertView = LayoutInflater.from(MainContext.INSTANCE.getContext()).inflate(R.layout.dos_client_child, parent,false);
            //convertView = LayoutInflater.from(MainContext.INSTANCE.getContext()).inflate(R.layout.dos_client_child, null);
            item = new ViewHolderItem();
            item.tv_group = convertView.findViewById(R.id.tv_child_group);
            item.tv_count = convertView.findViewById(R.id.child_count_data);
            item.tv_rx = convertView.findViewById(R.id.rx_child_data);
            item.tv_tx = convertView.findViewById(R.id.tx_child_data);
            item.tv_company = convertView.findViewById(R.id.child_company);
            item.tv_company.setSelected(true);
            convertView.setTag(item);
        } else {
            item = (ViewHolderItem) convertView.getTag();
        }

        item.tv_group.setText(dosGroupClientModels.get(groupPosition).getDosChildClientModelList().get(childPosition).getChild_bssid());
        item.tv_count.setText(String.valueOf(dosGroupClientModels.get(groupPosition).getDosChildClientModelList().get(childPosition).getChild_count()));
        item.tv_rx.setText(String.valueOf(dosGroupClientModels.get(groupPosition).getDosChildClientModelList().get(childPosition).getChild_rx_datas()));//没毛病
        item.tv_tx.setText(String.valueOf(dosGroupClientModels.get(groupPosition).getDosChildClientModelList().get(childPosition).getChild_tx_datas()));
        String mac = dosGroupClientModels.get(groupPosition).getDosChildClientModelList().get(childPosition).getChild_bssid();
        String company = new GetCompany().read_csv(mac.substring(0,8).replace(":","").toUpperCase());
        item.tv_company.setText(company);
        //notifyDataSetChanged();
        return convertView;
    }

    private class ViewHolderItem {
        TextView tv_group;
        TextView tv_count;
        TextView tv_rx;
        TextView tv_tx;
        TextView tv_company;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
