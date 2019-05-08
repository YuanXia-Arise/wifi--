package com.vrem.wifianalyzer.wifi.fragmentWiFiHotspot;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.navigation.items.FragmentItem;
import com.vrem.wifianalyzer.wifi.accesspoint.AccessPointsFragment;
import com.vrem.wifianalyzer.wifi.common.ApLinkInfoUpdater;
import com.vrem.wifianalyzer.wifi.common.InfoUpdater;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;
import com.vrem.wifianalyzer.wifi.fragmentWiFiHotspot.httpServer.HTTPServer;
import com.vrem.wifianalyzer.wifi.model.WiFiData;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class WIFIHotspotFragment extends Fragment implements ApLinkInfoAdapter.Callback{

    private Button openHotspotBtn;
    private Button closeHotspotBtn;
    private EditText hotspotNname;
    private EditText password;
    private WifiManager wifiManager;
    private String strHotspotName;
    private String strPassword;
    private ListView linkInfoLv;//ap连接信息列表
    private static List<String> promptList;//提示信息
    private static ApLinkInfoAdapter listAdapter; //ap连接适配器
    private View view;//用于判断当前的view是否为空 #解决再次进入的时候回界面数据为空

    private HTTPServer server;//web服务
    private Handler mainHandler;

    private ProgressDialog progressDialog;//连接进度条

    public WIFIHotspotFragment(){

    }

    //负责与子线程通信，原因是子线不能更新UI
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final android.os.Message msg) {
            if (msg.what==100) {
//                Bundle bundle = msg.getData();
//                mTextView.append("client"+bundle.getString("msg")+"\n");
//                Toast.makeText(MainContext.INSTANCE.getContext(),"client"+bundle.getString("msg"),Toast.LENGTH_LONG).show();
                if (MainContext.INSTANCE.getScannerService().isWifiApStatus()) {
                    Toast.makeText(MainContext.INSTANCE.getContext(),"连接成功",Toast.LENGTH_SHORT).show();
                    MainContext.INSTANCE.getMainActivity().recreate();
                    /*AccessPointsFragment accessPointsFragment = new AccessPointsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("status","ok");
                    accessPointsFragment.setArguments(bundle);
                    getFragmentManager().beginTransaction().replace(R.id.main_fragment,accessPointsFragment).commit();*/
                    /*new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Message message = new Message();
                                    message.what = 200;
                                    mainHandler.sendMessage(message); //通知主线程更新页面
                                }
                            });
                        }
                    }).start();*/
                } else {
                    Toast.makeText(MainContext.INSTANCE.getContext(),"热点未启用，请重新启用热点",Toast.LENGTH_SHORT).show();
                }
            }else if (msg.what == 101){
                promptList.add("热点已开启，等待客户端连接");
                listAdapter.notifyDataSetChanged();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view        = inflater.inflate(R.layout.fragment_wifi_hotspot,container,false);
            openHotspotBtn   = view.findViewById(R.id.open_hotspot_btn);
            closeHotspotBtn  = view.findViewById(R.id.close_hotspot_btn);
            hotspotNname     = view.findViewById(R.id.hotspot_name);
            password         = view.findViewById(R.id.hotspot_password);
            linkInfoLv       = view.findViewById(R.id.link_info_list_view);
            promptList       = new ArrayList<>();
            listAdapter      = new ApLinkInfoAdapter(getContext(),promptList,this);
            wifiManager      = (WifiManager) MainContext.INSTANCE.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            linkInfoLv.setAdapter(listAdapter);
        } else {
            return view;
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //打开热点事件
        openHotspotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isAP = MainContext.INSTANCE.getScannerService().isWifiApStatus();
                if (!isAP) {
                    strHotspotName = hotspotNname.getText().toString();
                    strPassword     = password.getText().toString();
                    if (strHotspotName.equals("")){
                        Toast.makeText(getContext(),"请输入名称",Toast.LENGTH_SHORT).show();
                    }else if (strPassword.equals("")){
                        Toast.makeText(getContext(),"请输入密码",Toast.LENGTH_SHORT).show();
                    }else if(strPassword.length()<8){
                        Toast.makeText(getContext(),"密码长度不能少于8位",Toast.LENGTH_SHORT).show();
                    }else {
                        try {
                            server           = new HTTPServer(mHandler,listAdapter,promptList);
                            server.asset_mgr = MainContext.INSTANCE.getContext().getAssets();
                            server.start();//启动web服务
                            Log.d("httpServer","start");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        WiFiData data = MainContext.INSTANCE.getScannerService().getWiFiData();
                        if (data.getWiFiDetails().size() == 0){ //如果用户第一次启动app,直接启动ap模式的话
                            createWifiHotspot(strHotspotName,strPassword);
                            //                    createWifiHotspot8(getContext(),true);
                        }else {
                            new InfoUpdater(getContext(),true,true,mHandler,strHotspotName,strPassword).execute();//直连模式下的热点启动方式
                        }
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Toast.makeText(getContext(),"热点已启用，请打开设备ap模式",Toast.LENGTH_SHORT).show();
                }

            }
        });

        //关闭热点事件
        closeHotspotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isAp = MainContext.INSTANCE.getScannerService().isWifiApStatus();
                if (isAp) {
                    closeWifiHotspot();
//                createWifiHotspot8(getContext(),false);
                    MainContext.INSTANCE.getScannerService().resume();
                    Log.d("wifiScanStatus","resume");
                    server.stop();
                    Log.d("hotspotStatus","Close");
                    promptList.add("热点已关闭");
                    listAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(),"热点没有启用，如有需求请先启用热点",Toast.LENGTH_SHORT).show();
                }

                if (server != null){
                    server.stop();
                }
            }
        });

    }


/*    private boolean isWifiApStatus(){
        try {
            //通过反射获取 getWifiApState()方法
            Method method = wifiManager.getClass().getDeclaredMethod("getWifiApState");
            //调用getWifiApState() ，获取返回值
            int status = (int) method.invoke(wifiManager);
            //通过反射获取 WIFI_AP的开启状态属性
            Field field = wifiManager.getClass().getDeclaredField("WIFI_AP_STATE_ENABLED");
            //获取属性值
            int value = (int) field.get(wifiManager);
            //判断是否开启
            if (status == value){
                return true;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return false;
    }*/

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /**
     * android 6.0 ap create
     */
    public void createWifiHotspot(String name,String psw) {
        MainContext.INSTANCE.getScannerService().pause();//打开热点时暂停wifi扫描
        WifiManager wifiManager = (WifiManager) MainContext.INSTANCE.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            //如果wifi处于打开状态，则关闭wifi,
            wifiManager.setWifiEnabled(false);
        }
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = name;
        config.preSharedKey = psw;
        config.hiddenSSID = false;
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);//公认的IEEE 802.11认证算法 用来判断加密方法。OPEN 开发系统认证
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);//公认的组密码 获取使用GroupCipher 的方法来进行加密。
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);//公认的密钥管理方案
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);//公认的WPA配对密码 获取使用WPA 方式的加密。
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        config.status = WifiConfiguration.Status.ENABLED;// 网络配置的可能状态  获取当前网络的状态。
        //通过反射调用设置热点
        try {
            Method method = wifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            boolean enable = (Boolean) method.invoke(wifiManager, config, true);
            if (enable) {
//                Toast.makeText(getContext(),"热点已开启 SSID:" +name+ " password:"+psw+"",Toast.LENGTH_LONG).show();
                Log.d("","热点已开启 SSID:" +name+ " password:"+psw+"");
            } else {
//                Toast.makeText(getContext(),"创建热点失败",Toast.LENGTH_LONG).show();
                Log.d("","创建热点失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
//            Toast.makeText(getContext(),"创建热点失败",Toast.LENGTH_LONG).show();
            Log.d("","创建热点失败");
        }
    }

    /**
     * 关闭WiFi热点 android 6.0
     */
    public void closeWifiHotspot() {
        try {
            Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");
            method.setAccessible(true);
            WifiConfiguration config = (WifiConfiguration) method.invoke(wifiManager);
            Method method2 = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method2.invoke(wifiManager, config, false);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
//        Toast.makeText(getContext(),"热点已关闭",Toast.LENGTH_LONG).show();
        if (server != null){
            // 在程序退出时关闭web服务器
            server.stop();
            Log.w("Httpd", "The server stopped.");
        }
        if(PrefSingleton.getInstance().getString("url") != null){
            PrefSingleton.getInstance().remove("url");//移除ap模式下的url

            PrefSingleton.getInstance().putString("url", "http://192.168.100.1:9494");//还原直连模式下的url
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /*暂用*/
    @TargetApi(Build.VERSION_CODES.O)
    public void createWifiHotspot8(Context context,boolean isEnable){
        MainContext.INSTANCE.getScannerService().pause();//暂停wifi扫描
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        Field iConnmgrField = null;
        try {
            iConnmgrField = connectivityManager.getClass().getDeclaredField("mService");
            iConnmgrField.setAccessible(true);
            Object iConMgr = iConnmgrField.get(connectivityManager);
            Class<?> iConnMgrClass = Class.forName(iConMgr.getClass().getName());
            if (isEnable){
                Method startTethering = iConnMgrClass.getMethod("startTethering",int.class, ResultReceiver.class,boolean.class);
                startTethering.invoke(iConMgr,0,null,true);
            }else {
                Method stop = iConnMgrClass.getMethod("stopTethering",int.class);
                stop.invoke(iConMgr,0);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /*
    * 实现AplinkInfoAdapter.Callback接口
    * */
    @Override
    public void click(final View view) {
//        Toast.makeText(this,"listview的内部的按钮被点击了！，位置是-->" + v.getTag() + ",内容是-->" + promptList.get((Integer) v.getTag()),
//                                 Toast.LENGTH_SHORT).show();

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("提示");
        progressDialog.setMessage("设备连接中，请稍等...");
        progressDialog.setIcon(R.drawable.ic_location_on_forgery_500_48dp);
        progressDialog.setProgress(100);
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);

        // 设置ProgressDialog 的一个Button
        progressDialog.setButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                // 点击“取消”按钮取消对话框
                dialog.cancel();
            }
        });
        // 让ProgressDialog显示
        progressDialog.show();
        // 创建线程实例
        new Thread() {
            public void run() {
                try {
                    String str = promptList.get((Integer) view.getTag());
                    String ip = splitData(str,":",":");
                    String tmp = str.substring(9);
                    String port = tmp.substring(tmp.indexOf(":")+1);
                    PrefSingleton.getInstance().putString("url", "http://"+ip+":"+port+"");//将ip port存入存储类中，方便ApLinkInfoUpdater调用
                    new ApLinkInfoUpdater(MainContext.INSTANCE.getContext(),true).execute();//执行异步任务，发送数据给前置
                    if (PrefSingleton.getInstance().getString("deviceInfo") !=null){
                        Message message = new Message();
                        message.what =100;
                        mHandler.sendMessage(message);
                        progressDialog.cancel();
                    }
                } catch (Exception e) {
                    progressDialog.cancel();
                }
            }
        }.start();
    }

    /*
    * 截取ip字符串
    * */
    public static String splitData(String str, String strStart, String strEnd) {
        String tempStr;
        tempStr = str.substring(str.indexOf(strStart)+1, str.lastIndexOf(strEnd));
        return tempStr;
    }
}
