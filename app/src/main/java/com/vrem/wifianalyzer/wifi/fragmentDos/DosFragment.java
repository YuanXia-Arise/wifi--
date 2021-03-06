package com.vrem.wifianalyzer.wifi.fragmentDos;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vrem.wifianalyzer.MainActivity;
import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.accesspoint.AccessPointsFragment;
import com.vrem.wifianalyzer.wifi.common.BackgroundTask;
import com.vrem.wifianalyzer.wifi.common.DevStatusDBUtils;
import com.vrem.wifianalyzer.wifi.common.DosUpdater;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;
import com.vrem.wifianalyzer.wifi.model.DeviceInfo;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by ZhenShiJie on 2018/5/7.
 */

public class DosFragment extends Fragment {

    //立flag来判断显示的数据 未完成
    private boolean flag = false;

    private String ssid;
    private String bssid;
    private String channel;
    private Button apDosButton;
    private Button startButton;
    private  Button channelDosButton;
    private Button mChannelDosButton;
    private String channelSelected = ""; //多信道
    private int channelId = 1;//单信道

//    private String intentId="";
    private List<WiFiDetail> wiFiDetails;
    private String dosSsid;

    private View view;
    private Bundle bundle;
    private String Name = "";
    //private int device_frequency = 1;
    private String Mac = "";

    //onCreateView为fragment的初始化页面方法
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view  = inflater.inflate(R.layout.fragment_dos, container, false);
        startButton = view.findViewById(R.id.startButton);//开始dos攻击按钮
        //cancelButton = view.findViewById(R.id.cancelButton);//取消攻击按钮
        channelDosButton = view.findViewById(R.id.channeldos);//单信道按钮
        mChannelDosButton = view.findViewById(R.id.mchanneldos);//多信道按钮
        apDosButton = view.findViewById(R.id.apdos);//选择热点按钮

        //device_frequency = Integer.parseInt(PrefSingleton.getInstance().getString("device_frequency"));
        bundle = getArguments();
        if (bundle.getString("ssid") != null){
            bssid = bundle.getString("bssid");
            ssid = bundle.getString("ssid");
            //channel = Integer.parseInt(bundle.getString("channel"));
            channel = bundle.getString("channel");
            Mac = bundle.getString("mac");
            apDosButton.setText(ssid);
        }
        String name = bundle.getString("Fragment");
        if (name == null){
            Name = "dos";
        } else {
            Name = name;
        }
        MainContext.INSTANCE.getScannerService().pause(); //暂停扫描，防止命令冲突
        return view;
    }


    @Override
    public void onStart() {
        Log.d("DosFragment status：","Start");
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d("DosFragment status：","Resume");
        super.onResume();
        if (Name.equals("Dos")){
            getView().setFocusableInTouchMode(true);
            getView().requestFocus();
            getView().setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if(i == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_UP){
                        Intent intent = new Intent();
                        intent.setClass(getActivity(), MainActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void onPause() {
        BackgroundTask.clearAll();
        Log.d("DosFragment status：","Pause");
        super.onPause();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Context context = getContext();

        String wifiDetailJson = String.valueOf(new Gson().toJson(MainContext.INSTANCE.getScannerService().getWiFiData().getWiFiDetails()));
        Type type = new TypeToken<List<WiFiDetail>>(){}.getType();
        Gson gson = new Gson();
        wiFiDetails = gson.fromJson(wifiDetailJson,type);//将JSON数组转为对象
        apDosButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    apDosButtonHandle(apDosButton,wiFiDetails);
                }
        });

        //单信道按钮事件
        channelDosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                channelButtonHandle(channelDosButton);
            }
        });

        //多信道按钮事件
        mChannelDosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                mChannelButtonHandle(mChannelDosButton);
            }
        });

        //开始按钮事件
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    JSONObject jo = new JSONObject();
                    JSONObject obj = new JSONObject();
                    PrefSingleton.getInstance().Initialize(getContext());
                    int gId = PrefSingleton.getInstance().getInt("id");
                    PrefSingleton.getInstance().putInt("id", gId + 1);
                    obj.put("id", gId); // 1-1
                    JSONObject param = new JSONObject(); // 2
                    JSONArray channels = new JSONArray();
                    JSONArray wlist = new JSONArray();
                    JSONArray blist = new JSONArray();
                    param.put("action", "mdk"); // 2-1

                    if (channelDosButton.getText().toString().equals("选择目标信道") == false) {
                        jo.put("type", "single");
                        jo.put("detail", new Integer(channelId).toString());
                        channels.put(channelId);
                    } else if (mChannelDosButton.getText().toString().equals("选择多目标信道") == false) {
                        jo.put("type", "multi");
                        jo.put("detail", channelSelected);
                        String[] channelIDArr = channelSelected.split(",");
                        for (int i = 0; i < channelIDArr.length; i++) {
                            channels.put(Integer.parseInt(channelIDArr[i]));
                        }
                    } else if (apDosButton.getText().toString().equals("选择目标SSID") == false) {
                        try {
                            String Bssid = "";
                            if (bssid.contains(",")) {
                                String[] tokens = Mac.split(",");
                                for (String token : tokens) {
                                    token = token.length() > 17 ? token.substring(0, 17) : token;
                                    Bssid = Bssid.equals("") ? token : Bssid + "," + token;
                                }
                                bssid = Bssid;
                            }
                            //bssid = bssid.length()>17 ? bssid.substring(0,17) : bssid;
                            jo.put("type", "ap");
                            jo.put("detail", bssid);

                            //blist.put(bssid);
                            if (Mac.contains(",")) {
                                String[] tokens = Mac.split(",");
                                for (String token : tokens) {
                                    token = token.length() > 17 ? token.substring(0, 17) : token;
                                    blist.put(token);
                                }
                            } else {
                                Mac = Mac.length() > 17 ? Mac.substring(0, 17) : Mac;
                                blist.put(Mac);
                            }

                            //channels.put(channel);
                            if (channel.contains(",")) {
                                String[] tokens = channel.split(",");
                                for (String token : tokens) {
                                    channels.put(Integer.valueOf(token));
                                }
                            } else {
                                channels.put(Integer.valueOf(channel));
                            }

                            flag = true;
                            writeFile("DosFlag.txt", String.valueOf(flag), bssid);
                        } catch (NullPointerException e){}
                    } else {
                        Toast.makeText(getContext(), "请选择目标热点或信道！", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    wlist.put(PrefSingleton.getInstance().getString("device_mac"));
                    param.put("channels", channels);
                    param.put("wlist", wlist);
                    param.put("blist", blist);
                    param.put("interval", 1.5);
                    obj.put("param", param);
                    jo.put("data", obj);

                    final JSONObject jof = jo;

                    DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(context);
                    devStatusDBUtils.open();
                    final String devId = PrefSingleton.getInstance().getString("device"); //获取设备ID
                    devStatusDBUtils.preHandling(devId);
                    devStatusDBUtils.close();
                    BackgroundTask.clearAll();
                    BackgroundTask.mTimerHandling = new Timer();
                    BackgroundTask.mTimerTaskHandling = new TimerTask() {
                        @Override
                        public void run() {
                            if (getActivity() == null){ //由于当线程结束时activity变得不可见,getActivity()有可能为空，需要提前判断
                                return;
                            }
                            try {
                                Log.d(TAG, "run: 12345==01>" + jof.toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new DosUpdater(getContext(), devId, jof,null,false,null,null).execute(); //开始阻断
                                }
                            });
                        }
                    };
                    BackgroundTask.mTimerHandling.schedule(BackgroundTask.mTimerTaskHandling, 0, 30000);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return;
                }
            }
        });
    }

    //写文件 用于区分显示攻击的客户端
    private void writeFile(String fileName, String flag, String bssid){
        try {
            FileOutputStream fileOutputStream = getActivity().openFileOutput(fileName,MODE_PRIVATE);/*在fragment中没有openFileOutput，因为它是activity中的方法*/
            fileOutputStream.write(flag.getBytes());
            fileOutputStream.write("\n".getBytes()); //写入换行
            fileOutputStream.write(bssid.getBytes());
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //选择热点事件
    private void apDosButtonHandle(final Button apDosButton, final List<WiFiDetail> wiFiDetails) {
        final String[] strings = new String[wiFiDetails.size()];
        for (int i = 0;i<wiFiDetails.size();i++){
            strings[i] = wiFiDetails.get(i).getSSID() + " 信道："+ wiFiDetails.get(i).getWiFiSignal().getChannel();
        }
        new AlertDialog.Builder(getContext())
                .setTitle("选择热点")
                .setSingleChoiceItems(strings, -1,new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO Auto-generated method stub
                        dosSsid = wiFiDetails.get(arg1).getSSID();
                        bssid = wiFiDetails.get(arg1).getBSSID();
                        //channel = Integer.parseInt(wiFiDetails.get(arg1).getWiFiSignal().getChannel());
                        channel = wiFiDetails.get(arg1).getWiFiSignal().getChannel();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    private int index; // 表示选项的索引
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        index = which;
                        dialog.dismiss();
                        DeviceInfo deviceInfo = new DeviceInfo();
                        deviceInfo.setWorkType(101);
                        apDosButton.setText(dosSsid);
                        channelDosButton.setText("选择目标信道");
                        mChannelDosButton.setText("选择多目标信道");
                        if (dosSsid == null) {
                            apDosButton.setText("选择目标SSID");
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                    }
                }).show();
    }

    //单信道事件
    private void channelButtonHandle(final Button tmpButton) {
        final String[] channelString = { "信道1","信道2","信道3","信道4","信道5","信道6","信道7","信道8","信道9","信道10",
                "信道11","信道12","信道13","信道14", "信道36","信道38","信道40","信道42","信道44","信道46","信道48","信道52",
                "信道56","信道60","信道64","信道149","信道153","信道157","信道161","信道165"};
        new AlertDialog.Builder(getContext())
                .setTitle("选择信道")
                .setSingleChoiceItems(channelString, 0,
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface arg0, int arg1) {
                                // TODO Auto-generated method stub
                                if (arg1 >= 0) {
                                    channelId = Integer.parseInt(channelString[arg1].replace("信道", ""));//arg1 + 1;
                                }
                            }
                        })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                        DeviceInfo deviceInfo = new DeviceInfo();
                        deviceInfo.setWorkType(102);
                        tmpButton.setText("所选信道为：" + channelId);

                        /*if (channelId == device_frequency) {
                            new AlertDialog.Builder(getContext()).setTitle("提示").setMessage("你选择的信道为设备信道")
                                    .setPositiveButton("确定",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                        }*/

                        mChannelDosButton.setText("选择多目标信道");
                        apDosButton.setText("选择目标SSID");
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                    }
                }).show();
    }
    //多信道事件
    private void mChannelButtonHandle(final Button button) {
        final String[] channelString = { "信道1","信道2","信道3","信道4","信道5","信道6","信道7","信道8","信道9","信道10",
                "信道11","信道12","信道13","信道14", "信道36","信道38","信道40","信道42","信道44","信道46","信道48","信道52",
                "信道56","信道60","信道64","信道149","信道153","信道157","信道161","信道165"};
        final List<Integer> listInteger = new ArrayList();
        new AlertDialog.Builder(getContext())
                .setTitle("选择多信道")
                .setMultiChoiceItems(channelString, null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1, boolean arg2) {
                                // TODO Auto-generated method stub
                                if (arg2) {
                                    //listInteger.add(Integer.parseInt(channelString[arg1].replace("信道","")));
                                    int counts = listInteger.size();
                                    if (counts < 4) {
                                        listInteger.add(Integer.parseInt(channelString[arg1].replace("信道","")));
                                    } else {
                                        Toast.makeText(getContext(), "请选择少于等于四个信道！", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                } else {
                                    Iterator<Integer> ii = listInteger.iterator();
                                    while(ii.hasNext()){
                                        Integer e = ii.next();
                                        if(e.equals(Integer.parseInt(channelString[arg1].replace("信道","")))){
                                            ii.remove();
                                        }
                                    }
                                }
                            }
                        })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        channelSelected = "";
                        Collections.sort(listInteger);
                        int count = listInteger.size();
                        for (int i = 0; i < count; i++) {
                            if (channelSelected.equals("")) {
                                channelSelected = channelSelected + listInteger.get(i).toString();
                            } else {
                                channelSelected = channelSelected + "," + listInteger.get(i).toString();
                            }
                        }
                        if (count == 0) {
                            dialog.dismiss();
                            button.setText("选择多目标信道");
                            apDosButton.setEnabled(true);
                            channelDosButton.setEnabled(true);
                            Toast.makeText(getContext(), "未选择信道！", Toast.LENGTH_SHORT).show();
                        } else if(count <= 4){
                            dialog.dismiss();
                            DeviceInfo deviceInfo = new DeviceInfo();
                            deviceInfo.setWorkType(110);
                            button.setText("所选信道为：" + channelSelected);

                            /*if (channelSelected.indexOf(String.valueOf(device_frequency)) != -1) {
                                new AlertDialog.Builder(getContext()).setTitle("提示").setMessage("你选择的信道包含设备信道"
                                        + "\n" + "设备信道为:" + device_frequency)
                                        .setPositiveButton("确定",
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                }).show();
                            }*/

                            channelDosButton.setText("选择目标信道");
                            apDosButton.setText("选择目标SSID");
                        } else {
                            Toast.makeText(getContext(), "请选择少于等于四个信道！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                    }
                }).show();
    }

}
