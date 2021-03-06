package com.vrem.wifianalyzer.wifi.fragmentClientEnum;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.common.BackgroundTask;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;
import com.vrem.wifianalyzer.wifi.model.ClientInfo;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;
import org.json.JSONException;
import java.util.List;

import static android.net.wifi.WifiConfiguration.Status.strings;
import static com.android.volley.VolleyLog.TAG;

/**
 * Created by ZhenShiJie on 2018/5/7.
 */

public class ClientEnumFragment extends Fragment {

    private Button apEnumButton;//枚举单个ap客户端列表按钮
    private Button allEnumButton;//枚举所有ap客户端列表按钮
    private List<WiFiDetail> wiFiDetailList;//wifi列表
    private String devId;//设备id

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view       = inflater.inflate(R.layout.fragment_clientenum,container,false);
        apEnumButton    = view.findViewById(R.id.apenum);//指定枚举热点按钮
        allEnumButton   = view.findViewById(R.id.allenum);//枚举所有热点按钮
        devId           = PrefSingleton.getInstance().getString("device");//获取设备ID
        wiFiDetailList  = MainContext.INSTANCE.getScannerService().getWiFiData().getWiFiDetails();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        apEnumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apenumButtonHandle(wiFiDetailList);//热点列表
            }
        });
        allEnumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allEnumButtonHandle(wiFiDetailList);
            }
        });
    }

    //枚举所有热点客户端
    private void allEnumButtonHandle(final List<WiFiDetail> wiFiDetailList) {
        View view       = getActivity().getLayoutInflater().inflate(R.layout.client_list_dialog, null);
        Dialog dialog   = new Dialog(getContext());
        dialog.setContentView(view);
        dialog.setTitle("客户端列表");
        dialog.show();
        ListView listview   = view.findViewById(R.id.client_list);
        TextView noData     = view.findViewById(R.id.nodata);
        try {
            ClientInfo.setAllClientInfo(getContext(), wiFiDetailList, listview, noData);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        WindowManager manager = getActivity().getWindowManager();
        Display display = manager.getDefaultDisplay();
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.height = (int) (display.getHeight() * 0.85);
        lp.width = (int) (display.getWidth() * 0.85);
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(lp);
    }

    //枚举单条热点客户端
    private void apenumButtonHandle(final List<WiFiDetail> wiFiDetails) {
        final String[] strings = new String[wiFiDetails.size()];
        for (int i = 0;i<wiFiDetails.size();i++){
            strings[i] = wiFiDetails.get(i).getSSID();
        }
        new AlertDialog.Builder(getContext())
                .setTitle("选择热点")
                .setSingleChoiceItems(strings, -1,new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        WiFiDetail wiFiDetail   = wiFiDetails.get(arg1); //获取某条wifi
                        View view               = getActivity().getLayoutInflater().inflate(R.layout.client_list_dialog, null);
                        Dialog dialog           = new Dialog(getContext());
                        dialog.setContentView(view);
                        dialog.setTitle("客户端列表");
                        dialog.show();
                        ListView listview   = view.findViewById(R.id.client_list);
                        TextView noData     = view.findViewById(R.id.nodata);
                        try {
                            System.out.println("20200925==>" + new Gson().toJson(wiFiDetail));
                            ClientInfo.setClientInfo(getContext(), wiFiDetail, listview, noData);
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        WindowManager manager = getActivity().getWindowManager();
                        Display display = manager.getDefaultDisplay();
                        Window dialogWindow = dialog.getWindow();
                        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                        lp.height = (int) (display.getHeight() * 0.85);
                        lp.width = (int) (display.getWidth() * 0.85);
                        dialogWindow.setGravity(Gravity.CENTER);
                        dialogWindow.setAttributes(lp);
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

    @Override
    public void onStart() {
        Log.d("ClientFragment status：","Start");
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d("ClientFragment status：", "Resume");
        super.onResume();
    }

    @Override
    public void onPause() {
        BackgroundTask.clearAll();
        Log.d("ClientFragment status：","Pause");
        super.onPause();
    }
}
