package com.vrem.wifianalyzer.wifi.fragmentTargetSearch;

/**
 * Created by huangche on 2019/12/26.
 */

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.model.Security;
import com.vrem.wifianalyzer.wifi.model.Strength;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;
import com.vrem.wifianalyzer.wifi.model.WiFiSignal;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class ApListSearchAdapter extends BaseAdapter{
    private Context context;
    private List<WiFiDetail> listItems;
    private LayoutInflater listContainer;
    private int itemViewResource;

    private static final int VENDOR_SHORT_MAX = 12;

    static class ListItemView{
        public TextView tab;
        public ImageView groupIndicator;
        public TextView ssid;
        public TextView level;
        public ImageView levelImage;
        public TextView channel;
        public TextView primaryFrequency;
        public TextView distance;
        public TextView beacons;
        public TextView channel_frequency_range;
        public TextView width;
        public TextView vendorShort;
        public ImageView securityImage;
        public TextView capabilities;
    }

//    public void UpdateData(List<ClientInfo> data) {
//        this.listItems = data;
//    }

    public ApListSearchAdapter(Context context, List<WiFiDetail> data, int resource){
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
            listItemView.ssid = convertView.findViewById(R.id.ssid);
            listItemView.tab = convertView.findViewById(R.id.tab);
            listItemView.groupIndicator = convertView.findViewById(R.id.groupIndicator);
            listItemView.level = convertView.findViewById(R.id.level);
            listItemView.levelImage = convertView.findViewById(R.id.levelImage); //绑定图片控件
            listItemView.channel = convertView.findViewById(R.id.channel);
            listItemView.primaryFrequency = convertView.findViewById(R.id.primaryFrequency);
            listItemView.distance = convertView.findViewById(R.id.distance);
            listItemView.beacons = convertView.findViewById(R.id.beacons);
            listItemView.channel_frequency_range = convertView.findViewById(R.id.channel_frequency_range);
            listItemView.width = convertView.findViewById(R.id.width);
            listItemView.vendorShort = convertView.findViewById(R.id.vendorShort);
            listItemView.securityImage = convertView.findViewById(R.id.securityImage);
            listItemView.capabilities = convertView.findViewById(R.id.capabilities);

            convertView.setTag(listItemView);
        }else{
            listItemView = (ListItemView) convertView.getTag();
        }
        final WiFiDetail clientInfo = listItems.get(position);
        WiFiSignal wiFiSignal = clientInfo.getWiFiSignal();
        Strength strength = wiFiSignal.getStrength(); //获取这条wifi的信号强度等级 Strength：枚举类，枚举信号强度等级
        Security security = clientInfo.getSecurity(); //获取这条wifi的信号安全等级 Security：枚举类，枚举信号安全等级
        listItemView.ssid.setTag(clientInfo);
        listItemView.ssid.setText(clientInfo.getTitle());
        listItemView.tab.setVisibility(View.VISIBLE);
        listItemView.level.setText(String.format(Locale.ENGLISH, "%ddBm", wiFiSignal.getLevel())); //设置dBm值
        listItemView.level.setTextColor(ContextCompat.getColor(context, strength.colorResource())); //设置dBm值颜色
        listItemView.levelImage.setImageResource(strength.imageResource()); //设置wifi信号强度
        listItemView.levelImage.setColorFilter(ContextCompat.getColor(context, strength.colorResource())); //设置wifi信号强度颜色
        listItemView.channel.setText(wiFiSignal.getChannelDisplay()); //设置信道
        listItemView.primaryFrequency.setText(String.format(Locale.ENGLISH, "%d%s", wiFiSignal.getPrimaryFrequency(),
                WiFiSignal.FREQUENCY_UNITS)); //设置主频率
        listItemView.distance.setText(String.format(Locale.ENGLISH, "%5.1fm", wiFiSignal.getDistance())); //设置距离
        listItemView.beacons.setText("信号强度 " + clientInfo.getBeacons());
        listItemView.channel_frequency_range.setText(Integer.toString(wiFiSignal.getFrequencyStart()) + " - "
                + Integer.toString(wiFiSignal.getFrequencyEnd())); //设置wifi频率:xxxx-xxxx
        listItemView.width.setText("(" + Integer.toString(wiFiSignal.getWiFiWidth().getFrequencyWidth())
                + WiFiSignal.FREQUENCY_UNITS + ")"); //设置wifi宽度
        String vendor = clientInfo.getWiFiAdditional().getVendorName();
        listItemView.vendorShort.setVisibility(View.VISIBLE);
        listItemView.vendorShort.setText(vendor.substring(0, Math.min(VENDOR_SHORT_MAX, vendor.length()))); //供应商
        listItemView.securityImage.setImageResource(security.getImageResource());//设置安全等级图片
        listItemView.securityImage.setColorFilter(ContextCompat.getColor(context, R.color.icons_color)); //设置安全等级图片颜色
        listItemView.capabilities.setText(clientInfo.getCapabilities()); //设置wifi加密方式

        return convertView;
    }

}