package com.vrem.wifianalyzer.wifi.fragmentTargetSearch;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.google.gson.Gson;
import com.vrem.wifianalyzer.MainActivity;
import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.common.BackgroundTask;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;
import com.vrem.wifianalyzer.wifi.common.VolleySingleton;
import com.vrem.wifianalyzer.wifi.fragmentChannel.ChannelListViewAdapter;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static android.content.ContentValues.TAG;

public class SearchChart extends AppCompatActivity implements View.OnClickListener{

    private DynamicLineChartManager dynamicLineChartManager;
    private LineChart mChart2;
    private List<Integer> list = new ArrayList<>(); //????????????
    private List<String> names = new ArrayList<>(); //??????????????????
    private List<Integer> colour = new ArrayList<>();//??????????????????
    private int[] colour0 = new int[]{Color.CYAN,Color.GREEN,Color.BLUE,Color.YELLOW,Color.RED,Color.WHITE,Color.LTGRAY,Color.BLACK,Color.DKGRAY,Color.GRAY,Color.MAGENTA};
    private List<WiFiDetail> wiFiDetailList;//???????????????wifi??????
    private boolean Chart = true;
    private String Target_mac = "";
    private int Mac_size = 0;
    private String[] mac = new String[1024];

    private ArrayList<HashMap<String,String>> List = new ArrayList<HashMap<String,String>>(); // ??????????????????????????????

    private RelativeLayout devicelayout;
    private ImageView deviceicon;
    private TextView commit,status;
    private ImageButton stop;

    private List<Integer> chann = new ArrayList<>(); //????????????

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_chart);
        devicelayout = findViewById(R.id.devicelayout);
        devicelayout.setEnabled(true);
        deviceicon = findViewById(R.id.deviceicon);
        deviceicon.setBackgroundResource(R.drawable.wifiblue);
        commit = findViewById(R.id.commit);
        commit.setText(PrefSingleton.getInstance().getString("device"));
        status = findViewById(R.id.status);
        status.setText("????????????????????????");
        stop = findViewById(R.id.stop);
        stop.setOnClickListener(this);

        mChart2 = (LineChart) findViewById(R.id.dynamic_chart2);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        Target_mac = bundle.getString("Target_mac");
        chann = bundle.getIntegerArrayList("channels");
        System.out.println("20200924==0>" + new Gson().toJson(chann));
        if (Target_mac.contains(",")) {
            mac = Target_mac.split(",");
            Mac_size = mac.length;
            for (int i = 0; i < mac.length; i++){
                names.add(mac[i]); //????????????
                colour.add(colour0[i]); //????????????
            }
        } else {
            Mac_size = 1;
            names.add(Target_mac);
            colour.add(colour0[0]);
            System.out.println("20200922==881>" + Target_mac);
        }

        dynamicLineChartManager = new DynamicLineChartManager(mChart2, names, colour);
        //dynamicLineChartManager.setYAxis(100, 0, 10);
        dynamicLineChartManager.setYAxis(0, -100, 10);

        BackgroundTask.clearAll();
        try{
            scanStep1();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (Chart) {
                    try {
                        //Thread.sleep(500);
                        String url = PrefSingleton.getInstance().getString("url");
                        final JSONObject obj = new JSONObject();
                        final JSONObject param = new JSONObject();
                        param.put("action", "action");
                        obj.put("param", param);
                        RequestFuture < JSONObject > requestFuture = RequestFuture.newFuture(); //?????????????????????????????????
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,  obj, requestFuture, requestFuture); //??????????????????
                        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(500,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        //???????????????????????????,???????????????????????????,???????????????????????????????????????,???????????????1.0f?????????????????????????????????
                        VolleySingleton.getInstance(getApplicationContext()).getRequestQueue().add(jsonObjectRequest); //?????????????????????,?????????????????????
                        //JSONObject response = requestFuture.get(10 - 1, TimeUnit.SECONDS); //??????????????????,???????????????wifi??????
                        JSONObject response = requestFuture.get(1, TimeUnit.SECONDS);
                        if (response != null){
                            wiFiDetailList = WiFiDetail.response2ApData(response, 0, 0, getApplicationContext());
                        } else {
                            wiFiDetailList = MainContext.INSTANCE.getScannerService().getWiFiData().getWiFiDetails();
                        }
                        List<ClientInfo_Search> clientData = new ArrayList<ClientInfo_Search>();
                        // ???????????????
                        try {
                            for (int i = 0; i < wiFiDetailList.size(); i++){ //??????????????????
                                if (!"[]".equals(String.valueOf(wiFiDetailList.get(i).getClient()))){
                                    String SSID = wiFiDetailList.get(i).getSSID();
                                    JSONArray client = new JSONArray(wiFiDetailList.get(i).getClient());
                                    for (int j = 0; j < client.length(); j++) { //???????????????
                                        String a = Target_mac.toLowerCase();
                                        String mac_search = client.getJSONObject(j).getString("mac");
                                        if (a.indexOf(mac_search) != -1) {
                                            ClientInfo_Search clientInfo = new ClientInfo_Search();
                                            clientInfo.setSSID("????????????:" + SSID); // ????????????
                                            clientInfo.setMac(client.getJSONObject(j).getString("mac")); // MAC
                                            clientInfo.setPower(client.getJSONObject(j).getString("power")); // ????????????
                                            clientInfo.setChannel(wiFiDetailList.get(i).getWiFiSignal().getChannel()); // ??????
                                            clientData.add(clientInfo);
                                        }
                                    }
                                }
                            }
                            // ????????????
                            final List<WiFiDetail> clientData0 = new ArrayList<>();
                            for (int i = 0; i<wiFiDetailList.size(); i++){
                                WiFiDetail wiFiDetail_o = wiFiDetailList.get(i);
                                String target_search = Target_mac.toLowerCase();
                                String a = wiFiDetail_o.getBSSID().toLowerCase();
                                if (a.length() > 17){
                                    a = a.substring(0,17);
                                }
                                if (target_search.indexOf(a) != -1) {
                                    clientData0.add(wiFiDetail_o);
                                }
                            }
                            for (int a = 0; a < clientData0.size(); a++) {
                                ClientInfo_Search clientInfo = new ClientInfo_Search();
                                clientInfo.setSSID("??????"); // ????????????
                                if (clientData0.get(a).getBSSID().length() >17){
                                    clientInfo.setMac(clientData0.get(a).getBSSID().substring(0,17)); // MAC
                                } else {
                                    clientInfo.setMac(clientData0.get(a).getBSSID()); // MAC
                                }
                                //clientInfo.setPower(clientData0.get(a).getBeacons()); // ????????????
                                clientInfo.setPower(String.valueOf(clientData0.get(a).getWiFiSignal().getLevel())); // ????????????
                                clientInfo.setChannel(clientData0.get(a).getWiFiSignal().getChannel()); // ??????
                                clientData.add(clientInfo);
                            }
                            System.out.println("20200202==999>" + new Gson().toJson(clientData));
                            //[{"SSID":"????????????:FT21098","channel":"1","mac":"94:65:2d:2b:f5:97","power":"-40"}]
                        } catch (JSONException e){}
                        if (Mac_size == 1 && clientData.size() > 0) {
                            //int power = (clientData.get(0).getPower() != null) ? Math.abs(Integer.valueOf(clientData.get(0).getPower())) : 0;
                            int power = (clientData.get(0).getPower() != null) ? Integer.valueOf(clientData.get(0).getPower()) : 0;
                            dynamicLineChartManager.addEntry(power);
                        } else if (Mac_size > 1 && clientData.size() > 0){
                            if (clientData.size() == 1){
                                for (int m = 0; m < Mac_size; m++){
                                    if (clientData.get(0).getMac().equals(names.get(m))){
                                        int power = 0;
                                        //power = (clientData.get(0).getPower() != null) ? Math.abs(Integer.valueOf(clientData.get(0).getPower())) : power;
                                        power = (clientData.get(0).getPower() != null) ? Integer.valueOf(clientData.get(0).getPower()) : power;
                                        list.add(power);
                                    } else {
                                        int power = 0;
                                        list.add(power);
                                    }
                                }
                            } else {
                                List.clear();
                                String a = null;
                                try {
                                    for (int i = 0; i < names.size(); i++) {
                                        HashMap<String, String> temp = new HashMap<String, String>();
                                        temp.put("text1", names.get(i));
                                        for (int m = 0; m < clientData.size(); m++){
                                            if (names.get(i).equals(clientData.get(m).getMac())){
                                                a = clientData.get(m).getPower();
                                                temp.put("text2", a);
                                            } else {
                                                if (a == null){
                                                    temp.put("text2", "0");
                                                }
                                            }
                                        }
                                        List.add(temp);
                                    }
                                    System.out.println("20200202==003>" + new Gson().toJson(List));

                                } catch (NullPointerException e){}

                                for (int n = 0; n < List.size(); n++) {
                                    String str = List.get(n).get("text2");
                                    if (str == null){
                                        list.add(0);
                                    } else {
                                        //list.add(Math.abs(Integer.valueOf(List.get(n).get("text2"))));
                                        list.add(Integer.valueOf(List.get(n).get("text2")));
                                    }
                                }

                            }
                            dynamicLineChartManager.addEntry(list);
                            list.clear();
                        } else if (Mac_size == 1 && clientData.size() == 0) {
                            dynamicLineChartManager.addEntry(0);
                        } else if (Mac_size > 1 && clientData.size() == 0) {
                            for (int i = 0; i < Mac_size; i++) {
                                list.add(0);
                            }
                            dynamicLineChartManager.addEntry(list);
                            list.clear();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                        Chart = false;
                        MainContext.INSTANCE.getScannerService().pause();
                        finish();
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                        Chart = false;
                        MainContext.INSTANCE.getScannerService().pause();
                        finish();
                    } catch (IndexOutOfBoundsException e){
                        e.printStackTrace();
                    }

                }
            }
        }).start();
    }


    public void scanStep1() throws JSONException {
        String url = PrefSingleton.getInstance().getString("url");
        JSONObject obj = new JSONObject();
        JSONArray channels = new JSONArray();
        for (int i = 0; i < chann.size(); i++){
            channels.put(chann.get(i));
        }
        int gId = PrefSingleton.getInstance().getInt("id");
        PrefSingleton.getInstance().putInt("id", gId + 1);
        obj.put("id", gId);
        JSONObject param = new JSONObject();
        param.put("channels", channels);
        param.put("interval", 1.5);
        param.put("action", "scan");
        obj.put("param", param);
        Log.w("SCAN_STEP_20200202", "REQUEST: " + obj.toString());
        System.out.println("20210305==???????????????" + obj.toString());
        RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,  obj, requestFuture, requestFuture);
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(getApplicationContext()).getRequestQueue().add(jsonObjectRequest);
    }

    @SuppressLint("WrongConstant")
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(this)
                    .setTitle("??????").setMessage("??????????????????").setPositiveButton("???",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            Chart = false;
                            MainContext.INSTANCE.getScannerService().pause();
                            finish();
                        }
                    })
                    .setNegativeButton("???", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            // TODO Auto-generated method stub
                            arg0.dismiss();
                        }
                    }).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.stop:
                new AlertDialog.Builder(this)
                        .setTitle("??????").setMessage("??????????????????").setPositiveButton("???",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                Chart = false;
                                MainContext.INSTANCE.getScannerService().pause();
                                finish();
                            }
                        })
                        .setNegativeButton("???", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                // TODO Auto-generated method stub
                                arg0.dismiss();
                            }

                        }).show();
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Chart = false;
        MainContext.INSTANCE.getScannerService().pause();
        finish();
        super.onDestroy();
    }
}
