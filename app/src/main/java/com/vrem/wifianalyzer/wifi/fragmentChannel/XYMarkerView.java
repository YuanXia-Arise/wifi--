
package com.vrem.wifianalyzer.wifi.fragmentChannel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.gson.Gson;
import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.dosClientModel.DosChildClientModel;
import com.vrem.wifianalyzer.wifi.dosClientModel.DosGroupClientModel;
import com.vrem.wifianalyzer.wifi.fragmentTargetSearch.MacListViewAdapter;
import com.vrem.wifianalyzer.wifi.model.ClientInfo;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;


import org.json.JSONArray;
import org.json.JSONException;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Custom implementation of the MarkerView.
 *
 * @author Philipp Jahoda
 */
@SuppressLint("ViewConstructor")
public class XYMarkerView extends MarkerView {

    private ListView listView;
    private TextView textView;
    private final IAxisValueFormatter xAxisValueFormatter;

    private List<WiFiDetail> wiFiDetailList;//wifi列表
    private Activity activity;
    private Context context;

    public XYMarkerView(Context context, IAxisValueFormatter xAxisValueFormatter, Activity activity) {
        super(context, R.layout.custom_marker_view);
        this.activity = activity;
        this.context = context;

        this.xAxisValueFormatter = xAxisValueFormatter;
        //listView = findViewById(R.id.channel_mar);
        //listView.setAdapter(new ChannelListViewAdapter(context,getData(0, "1")));
        //textView = findViewById(R.id.tvContent);
    }

    // runs every time the MarkerView is redrawn, can be used to update the content (user-interface)

    // highlight.getStackIndex() 获取分段（0，1）
    // xAxisValueFormatter.getFormattedValue(e.getX()，null) x轴
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        super.refreshContent(e, highlight);

        int position = highlight.getStackIndex();
        String channel = xAxisValueFormatter.getFormattedValue(e.getX(), null).replace("信道", "");
        //textView.setText(channel);
        //listView.setAdapter(new ChannelListViewAdapter(context,getData(position, channel)));
        //allEnumButtonHandle(getData(position, channel));
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }

    public List<List<String>> getData(int position, String channel){
        wiFiDetailList = MainContext.INSTANCE.getScannerService().getWiFiData().getWiFiDetails();
        List<List<String>> list = new ArrayList<>();
        try{
            for(int i = 0; i < wiFiDetailList.size(); i++){
                if (channel.equals(wiFiDetailList.get(i).getWiFiSignal().getChannel()) && position == 0){
                    String Ssid = wiFiDetailList.get(i).getSSID();
                    String Mac = wiFiDetailList.get(i).getBSSID();
                    String Signal = String.valueOf(wiFiDetailList.get(i).getWiFiSignal().getLevel());
                    List<String> namesList = Arrays.asList( Ssid, Mac, Signal);
                    list.add(namesList);
                } else if (channel.equals(wiFiDetailList.get(i).getWiFiSignal().getChannel()) && position == 1){
                    if (!"[]".equals(String.valueOf(wiFiDetailList.get(i).getClient()))) {
                        JSONArray client = new JSONArray(wiFiDetailList.get(i).getClient());
                        for (int j = 0; j < client.length(); j++) { //遍历客户端
                            String Mac = client.getJSONObject(j).getString("mac");
                            String Signal = client.getJSONObject(j).getString("power");
                            List<String> namesList = Arrays.asList( "", Mac, Signal);
                            list.add(namesList);
                        }
                    }
                }
            }
        } catch (NullPointerException e){

        } catch (Exception e){

        }
        return list;
    }

    //枚举所有热点客户端
    private void allEnumButtonHandle(final List<List<String>> wiFiDetailList) {
        View view       = activity.getLayoutInflater().inflate(R.layout.client_list_dialog, null);
        Dialog dialog   = new Dialog(getContext());
        dialog.setContentView(view);
        dialog.setTitle("客户端列表");
        dialog.show();
        ListView listview   = view.findViewById(R.id.client_list);
        TextView noData     = view.findViewById(R.id.nodata);
        listview.setAdapter(new ChannelListViewAdapter(context,wiFiDetailList));
        WindowManager manager = activity.getWindowManager();
        Display display = manager.getDefaultDisplay();
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.height = (int) (display.getHeight() * 0.85);
        lp.width = (int) (display.getWidth() * 0.85);
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(lp);
    }

}
