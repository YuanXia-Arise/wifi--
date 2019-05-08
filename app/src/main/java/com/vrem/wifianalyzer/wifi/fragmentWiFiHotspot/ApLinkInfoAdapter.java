package com.vrem.wifianalyzer.wifi.fragmentWiFiHotspot;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.vrem.wifianalyzer.R;

import java.util.List;

public class ApLinkInfoAdapter extends BaseAdapter implements View.OnClickListener{

//    private String[] str;
    private LayoutInflater layoutInflater;
    private List<String> stringList;
    private Callback callback;

    public interface Callback {
        public void click(View view);
    }

    public ApLinkInfoAdapter(@NonNull Context context, List<String> stringList,Callback callback) {
        this.layoutInflater = LayoutInflater.from(context);
        this.stringList     = stringList;
        this.callback       = callback;
    }

    @Override
    public int getCount() {
        return stringList.size();
    }

    @Override
    public Object getItem(int position) {
        return stringList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.ap_link_list_item,null);
        ViewHolder holder = new ViewHolder();
        holder.textViewVH = convertView.findViewById(R.id.tv);
        holder.buttonVH = convertView.findViewById(R.id.btn);
        holder.textViewVH.setText(stringList.get(position));
        if (stringList.get(position).contains("ip")){
            holder.buttonVH.setVisibility(View.VISIBLE);
        }
        holder.buttonVH.setOnClickListener(this);
        holder.buttonVH.setTag(position);
        return convertView;
    }

    @Override
    public void onClick(View v) {
        callback.click(v);
    }

    private class ViewHolder{
        public TextView textViewVH;
        public Button buttonVH;
    }

}
