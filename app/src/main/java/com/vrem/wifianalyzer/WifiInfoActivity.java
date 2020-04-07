package com.vrem.wifianalyzer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.vrem.wifianalyzer.wifi.common.BackgroundTask;
import com.vrem.wifianalyzer.wifi.common.DevStatusDBUtils;
import com.vrem.wifianalyzer.wifi.common.DosUpdater;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;
import com.vrem.wifianalyzer.wifi.fragmentDos.DosFragment;
import com.vrem.wifianalyzer.wifi.fragmentSniffer.SnifferFragment;
import com.vrem.wifianalyzer.wifi.model.ClientInfo;
import com.vrem.wifianalyzer.wifi.model.ClientListAdapter;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

/**
 * Created by ZhenShiJie on 2018/3/21.
 */

public class WifiInfoActivity extends Activity {

    private WiFiDetail wiFiDetail;

    private TextView channel;
    private TextView client;
    private TextView signal;
    private TextView encry;
    private TextView wps;
    private TextView method;
    private TextView mac;
    private TextView company;
    private ListView listView;

    private Vibrator vibrator;
    private boolean flag = false;
    private String Cus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_info);
        LayoutInflater lif = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View wifiListView = lif.inflate(R.layout.wifi_info,null);
        channel = wifiListView.findViewById(R.id.channel);
        client = wifiListView.findViewById(R.id.client);
        signal = wifiListView.findViewById(R.id.signal);
        encry = wifiListView.findViewById(R.id.encry);
        wps = wifiListView.findViewById(R.id.wps);
        method = wifiListView.findViewById(R.id.method);
        mac = wifiListView.findViewById(R.id.mac);
        company = wifiListView.findViewById(R.id.company);
        listView = findViewById(R.id.clientlist);
        listView.addHeaderView(wifiListView);
        TextView nodata = findViewById(R.id.nodata);
        String wifiDetailJson = getIntent().getStringExtra("wifiDetail");
        wiFiDetail = new Gson().fromJson(wifiDetailJson,WiFiDetail.class);
        //List<WiFiDetail> wifiDetailJson = MainContext.INSTANCE.getScannerService().getWiFiData().getWiFiDetails();
       // wiFiDetail = new Gson().fromJson((JsonElement) wifiDetailJson,WiFiDetail.class);
        //wiFiDetail = new Gson().fromJson(MainContext.INSTANCE.getScannerService().getWiFiData().getWiFiDetails(),WiFiDetail.class);
        try {
            ClientInfo.setClientInfo(WifiInfoActivity.this, wiFiDetail, listView, nodata);
            Log.d(TAG, "2020==>01:" + wiFiDetail);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //客户端长按事件
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, final long id) {
                vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(30);//设置震动
                View view1 = getLayoutInflater().inflate(R.layout.context_menu, null);
                ArrayList<String> list = new ArrayList<String>();
                list.add("客户端定向阻断");
                final Dialog dialog = new Dialog(WifiInfoActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(view1);
                Window dialogWindow = dialog.getWindow();
                WindowManager wm = getWindowManager();
                Display display = wm.getDefaultDisplay();
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.width = (display.getWidth() * 2 / 3);
                dialogWindow.setAttributes(lp);
                dialog.show();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                final ListView listView = view1.findViewById(R.id.listview);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(WifiInfoActivity.this, R.layout.context_menu_listitem, list);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0,View arg1, int arg2, long arg3) {
                        switch (arg2) {
                            case 0:
                                try {
                                    JSONArray client = new JSONArray(wiFiDetail.getClient());
                                    Cus = client.getJSONObject(position - 1).getString("mac");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Intent intent = new Intent(WifiInfoActivity.this, DosActivity.class);
                                intent.putExtra("ssid",wiFiDetail.getSSID());
                                intent.putExtra("bssid",wiFiDetail.getBSSID());
                                intent.putExtra("channel",wiFiDetail.getWiFiSignal().getChannel());
                                intent.putExtra("id","0");
                                intent.putExtra("cus",Cus);
                                startActivity(intent);
                                finish();
                                break;
                            default:
                                break;
                        }
                    }
                });
                return true;
            }
        });

        String strchannel = wiFiDetail.getWiFiSignal().getChannel();
        channel.setText(strchannel);
        JSONArray clientJson = null;
        try {
            if (wiFiDetail.getClient() != null && !"".equals(wiFiDetail.getClient())){
                clientJson = new JSONArray(wiFiDetail.getClient());
                int inClient = clientJson.length();
                client.setText("" + inClient);
            }else{
                client.setText("" + 0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        int j = wiFiDetail.getWiFiSignal().getLevel();
        String strSignal = String.valueOf(j);
        signal.setText(strSignal);
        String strEnc = wiFiDetail.getCapabilities();
        if (strEnc.length() > 4){
            String str1 = strEnc.substring(2,strEnc.length()-2); //截取字符串 ["abs"]
            encry.setText(str1);
        } else {
            encry.setText(strEnc);
        }
        if (wiFiDetail.getWps().equals("true"))
            wps.setText("是");
        else
            wps.setText("否");
        String strMethod = wiFiDetail.getCipher();
        if (strMethod.length() >= 8){
            String tmp = wiFiDetail.getCipher().substring(0,4);
            method.setText(tmp);
        } else {
            method.setText("无");
        }
        String strMac = wiFiDetail.getBSSID();
        mac.setText(strMac);

        company.setSelected(true);
        GetCompany getCompany = new GetCompany();
        company.setText(getCompany.read_csv(strMac.substring(0, 8).replace(":", "").toUpperCase()));
    }

    @Override
    protected void onStart() {
        Log.d("WifiInfo status：","Start");
        super.onStart();
    }

    @Override
    protected void onPause() {
        BackgroundTask.clearAll();
        Log.d("WifiInfo status：","Pause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d("WifiInfo status：","Resume");
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d("WifiInfo status","Stop");
        super.onStop();
    }
}
