package com.vrem.wifianalyzer.wifi.fragmentTargetSearch;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.google.gson.Gson;
import com.vrem.util.WifiStatus;
import com.vrem.wifianalyzer.DeviceListActivity;
import com.vrem.wifianalyzer.GetCompany;
import com.vrem.wifianalyzer.MainActivity;
import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.common.BackgroundTask;
import com.vrem.wifianalyzer.wifi.common.InfoUpdater;
import com.vrem.wifianalyzer.wifi.common.MacSsidDBUtils;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;
import com.vrem.wifianalyzer.wifi.common.TargetMacDBUtils;
import com.vrem.wifianalyzer.wifi.common.VolleySingleton;
import com.vrem.wifianalyzer.wifi.fragmentDos.DosFragment;
import com.vrem.wifianalyzer.wifi.fragmentSniffer.SnifferFragment;
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

import static com.android.volley.VolleyLog.TAG;
import static com.android.volley.VolleyLog.e;
import static com.android.volley.VolleyLog.v;


/**
 * Created by huangche on 2019/12/25.
 */

public class TargetSearchFragment extends Fragment implements AdapterView.OnItemLongClickListener{

    private Button Search_btn, Start_btn;
    private Context context;
    private ListView listView;
    private String Target_mac = "";
    private List<WiFiDetail> wiFiDetailList;//wifi列表
    private List<String> MacData = new ArrayList<>(); // 列表listview本地数据
    private List<String> Mac_list = new ArrayList<>();



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_search,container,false);
        context = getContext();
        Search_btn = view.findViewById(R.id.Target);
        Start_btn = view.findViewById(R.id.startButton);
        Target_mac = "";
        listView = view.findViewById(R.id.search_mac);

        TargetMacDBUtils targetMacDBUtils = new TargetMacDBUtils(getContext());
        targetMacDBUtils.open();
        Mac_list = targetMacDBUtils.selectMac();
        targetMacDBUtils.close();
        System.out.println("20202020==0>" + new Gson().toJson(Mac_list));
        listView.setAdapter(new MacListViewAdapter(getActivity().getApplicationContext(),Mac_list));
        listView.setOnItemLongClickListener(this);

        //MainContext.INSTANCE.getScannerService().pause();//暂停扫描，防止命令冲突
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            //FragmentActivity的onSaveInstanceState方法可以看到，fragment保存时的标签是android:support:fragments
            String FRAGMENTS_TAG = "android:support:fragments";
            savedInstanceState.remove(FRAGMENTS_TAG);
        }


        Search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TargetMac();
            }
        });

        Start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackgroundTask.clearAll();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                PrefSingleton.getInstance().putString("target_search", Target_mac);
                if (PrefSingleton.getInstance().getString("target_search").equals("")) {
                    Toast.makeText(getContext(), "请添加目标MAC地址", Toast.LENGTH_SHORT).show();
                    return;
                } else if (PrefSingleton.getInstance().getString("target_search").length() < 17) {
                    Toast.makeText(getContext(), "目标MAC地址格式不正确", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<Integer> channels = new ArrayList<>(); //数据集合
                wiFiDetailList = MainContext.INSTANCE.getScannerService().getWiFiData().getWiFiDetails();
                List<ClientInfo_Search> clientData = new ArrayList<ClientInfo_Search>();
                // 客户端信息
                try {
                    for (int i = 0; i < wiFiDetailList.size(); i++){ //遍历所有热点
                        if (!"[]".equals(String.valueOf(wiFiDetailList.get(i).getClient()))){
                            String SSID = wiFiDetailList.get(i).getSSID();
                            JSONArray client = new JSONArray(wiFiDetailList.get(i).getClient());
                            for (int j = 0; j < client.length(); j++) { //遍历客户端
                                String a = Target_mac.toLowerCase();
                                String mac_search = client.getJSONObject(j).getString("mac");
                                if (a.indexOf(mac_search) != -1) {
                                    ClientInfo_Search clientInfo = new ClientInfo_Search();
                                    clientInfo.setSSID("连接热点:" + SSID); // 连接热点
                                    clientInfo.setMac(client.getJSONObject(j).getString("mac")); // MAC
                                    clientInfo.setPower(client.getJSONObject(j).getString("power")); // 信号强度
                                    clientInfo.setChannel(wiFiDetailList.get(i).getWiFiSignal().getChannel()); // 信道
                                    clientData.add(clientInfo);
                                }
                            }
                        }
                    }
                    // 热点信息
                    final List<WiFiDetail> clientData0 = new ArrayList<>();
                    for (int i = 0; i<wiFiDetailList.size(); i++){
                        WiFiDetail wiFiDetail_o = wiFiDetailList.get(i);
                        String target_search = Target_mac.toLowerCase();
                        String a = wiFiDetail_o.getBSSID().toLowerCase();
                        if (target_search.indexOf(a) != -1) {
                            clientData0.add(wiFiDetail_o);
                        }
                    }
                    for (int a = 0; a < clientData0.size(); a++) {
                        ClientInfo_Search clientInfo = new ClientInfo_Search();
                        clientInfo.setSSID("热点"); // 连接热点
                        clientInfo.setMac(clientData0.get(a).getBSSID()); // MAC
                        clientInfo.setPower(String.valueOf(clientData0.get(a).getWiFiSignal().getLevel())); // 信号强度
                        clientInfo.setChannel(clientData0.get(a).getWiFiSignal().getChannel()); // 信道
                        clientData.add(clientInfo);
                    }
                    //[{"SSID":"连接热点:FT21098","channel":"1","mac":"94:65:2d:2b:f5:97","power":"-40"}]
                    if (clientData.size() != 0){
                        for (int o = 0; o < clientData.size(); o++){
                            if (channels.size() == 0){
                                channels.add(Integer.valueOf(clientData.get(o).getChannel()));
                            } else {
                                for (int p = 0; p < channels.size(); p++) {
                                    if (channels.get(p) != Integer.valueOf(clientData.get(o).getChannel())) {
                                        channels.add(Integer.valueOf(clientData.get(o).getChannel()));
                                    }
                                }
                            }
                        }
                    }
                } catch (JSONException e){

                } catch (OutOfMemoryError e){

                }

                new InfoUpdater(getActivity(), true).execute(); // 获取前置信息
                boolean wifi_status = new WifiStatus().Wifi_Status(getContext());
                try {
                    if (!wifi_status) {
                        Toast.makeText(getContext(), "设备已断开连接", Toast.LENGTH_SHORT).show();
                        PrefSingleton.getInstance().putString("deviceInfo",null); // 将数据存入数据存储类中
                    } else {
                        Intent intent = new Intent(getActivity(), SearchChart.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("Target_mac", Target_mac);
                        bundle.putIntegerArrayList("channels", (ArrayList<Integer>) channels);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                } catch (OutOfMemoryError e){
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), MainActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                } catch (RuntimeException e){
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), MainActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }

            }
        });
    }

    //添加MAC地址
    public void TargetMac(){
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.input_add_mac,null);
        final EditText remarksEt = view.findViewById(R.id.input_mac);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        remarksEt.setText(Target_mac);
        builder.setTitle("添加Mac");
        builder.setView(view);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TargetMacDBUtils targetMacDBUtils = new TargetMacDBUtils(getContext());
                targetMacDBUtils.open();
                Target_mac = remarksEt.getText().toString();
                if (Target_mac.contains(",")) {
                    MacData.clear();
                    String mac[] = Target_mac.split(",");
                    for (int i = 0; i < mac.length; i++){
                        if (mac[i].length() != 17) {
                            Toast.makeText(context, "请输入正确的MAC", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String company = new GetCompany().read_csv(mac[i].substring(0, 8).replace(":", "").toUpperCase());
                        if (!targetMacDBUtils.isMac(mac[i])) {
                            targetMacDBUtils.insertOrUpdate(mac[i], company, "");
                        }
                    }
                } else {
                    if (Target_mac.length() != 17) {
                        Toast.makeText(context, "请输入正确的MAC", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String company = new GetCompany().read_csv(Target_mac.substring(0, 8).replace(":", "").toUpperCase());
                    if (!targetMacDBUtils.isMac(Target_mac)) {
                        targetMacDBUtils.insertOrUpdate(Target_mac, company, "");
                    }
                }

                Mac_list = targetMacDBUtils.selectMac();
                targetMacDBUtils.close();
                listView.setAdapter(new MacListViewAdapter(getActivity().getApplicationContext(),Mac_list));
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!TextUtils.isEmpty(remarksEt.getText().toString())){
                    remarksEt.setText("");
                }
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create(); //获取dialog
        dialog.show(); //显示对话框
    }


    /*长按事件*/
    private Button remarksBtn,delBtn;
    private AlertDialog dialog;
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, final long id) {
        view = getActivity().getLayoutInflater().inflate(R.layout.targetsearch_long_click,null);
        remarksBtn = view.findViewById(R.id.remarks_btn);
        delBtn = view.findViewById(R.id.del_btn);
        remarksBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remarksClick(v,Mac_list.get(position)); //备注
                dialog.dismiss();
            }
        });
        delBtn.setOnClickListener(new View.OnClickListener() { // 删除
            @Override
            public void onClick(View v) {
                TargetMacDBUtils targetMacDBUtils = new TargetMacDBUtils(getContext());
                targetMacDBUtils.open();
                targetMacDBUtils.delData(Mac_list.get(position));
                Mac_list = targetMacDBUtils.selectMac();
                targetMacDBUtils.close();
                listView.setAdapter(new MacListViewAdapter(getActivity().getApplicationContext(),Mac_list));
                dialog.dismiss();
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("选择操作");
        builder.setView(view);
        dialog = builder.create();
        dialog.show();

        WindowManager manager = getActivity().getWindowManager();
        Display display = manager.getDefaultDisplay();
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(lp);
        return true;
    }

    //备注添加
    public void remarksClick(View view, final String mac){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        view = inflater.inflate(R.layout.input_remarks,null);
        final EditText remarksEt  = view.findViewById(R.id.input_et);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("备注");
        builder.setView(view);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!TextUtils.isEmpty(remarksEt.getText().toString())){
                    String remarksStr = remarksEt.getText().toString();
                    TargetMacDBUtils targetMacDBUtils = new TargetMacDBUtils(getContext());
                    targetMacDBUtils.open();
                    targetMacDBUtils.updataRemarks(mac,remarksStr);
                    Mac_list = targetMacDBUtils.selectMac();
                    targetMacDBUtils.close();
                    listView.setAdapter(new MacListViewAdapter(getActivity().getApplicationContext(),Mac_list));
                }else {
                    Toast.makeText(getContext(),"备注不能为空。",Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!TextUtils.isEmpty(remarksEt.getText().toString())){
                    remarksEt.setText("");
                }
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();//获取dialog
        dialog.show();//显示对话框
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onStart() {
        Log.d("TargetSearchFragment status：","Start");
        super.onStart();
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onResume() {
        Log.d("TargetSearchFragment status：","Resume");
        try{
            String url = PrefSingleton.getInstance().getString("url");
            JSONObject obj = new JSONObject();
            JSONArray channels = new JSONArray();
            int gId = PrefSingleton.getInstance().getInt("id");
            PrefSingleton.getInstance().putInt("id", gId + 1);
            obj.put("id", gId); // 1
            JSONObject param = new JSONObject();
            param.put("channels", channels); // 2-1
            param.put("interval", 1.5); // 2-2
            param.put("action", "scan"); // 2-3
            obj.put("param", param); // 3
            Log.w("SCAN_STEP_20200202", "REQUEST: " + obj.toString());
            System.out.println("20210305==发送指令：" + obj.toString());
            RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,  obj, requestFuture, requestFuture);
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(context).getRequestQueue().add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e){}

        MainContext.INSTANCE.getScannerService().update();

        super.onResume();
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onPause() {
        //Target_mac = "";
        PrefSingleton.getInstance().putString("target_search", "");
        BackgroundTask.clearAll();
        Log.d("TargetSearchFragment status：","Pause");
        super.onPause();
    }

}
