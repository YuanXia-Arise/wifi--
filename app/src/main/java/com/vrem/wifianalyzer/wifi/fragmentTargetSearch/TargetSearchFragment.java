package com.vrem.wifianalyzer.wifi.fragmentTargetSearch;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.common.BackgroundTask;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.android.volley.VolleyLog.TAG;


/**
 * Created by huangche on 2019/12/25.
 */

public class TargetSearchFragment extends Fragment {

    private Button Search_btn, Start_btn, Client_btn;
    private Context context;

    private String Target_mac = "";
    private TextView textView;

    private ListView listView;
    private ListView listView_ap;
    private List<WiFiDetail> wiFiDetailList;//wifi列表


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_search,container,false);
        context = getContext();
        Search_btn = view.findViewById(R.id.Target);
        Start_btn = view.findViewById(R.id.startButton);

        Client_btn = view.findViewById(R.id.clientButton);
        Client_btn.setText("热点");

        textView = view.findViewById(R.id.line);
        textView.setVisibility(View.INVISIBLE);

        listView = view.findViewById(R.id.client_search);
        listView_ap = view.findViewById(R.id.ap_search);
        Target_mac = "";

        MainContext.INSTANCE.getScannerService().pause();//暂停扫描，防止命令冲突
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

        Client_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Client_btn.getText().equals("热点")) {
                    listView_ap.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                    Client_btn.setText("客户端");
                } else {
                    listView_ap.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                    Client_btn.setText("热点");
                }
            }
        });

        Search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TargetMac();
            }
        });

        Start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrefSingleton.getInstance().putString("target_search", Target_mac);
                if (PrefSingleton.getInstance().getString("target_search").equals("")) {
                    Toast.makeText(getContext(), "请添加目标MAC地址", Toast.LENGTH_SHORT).show();
                    return;
                } else if (PrefSingleton.getInstance().getString("target_search").length() < 17) {
                    Toast.makeText(getContext(), "目标MAC地址格式不正确", Toast.LENGTH_SHORT).show();
                    return;
                }
                Client_btn.setText("热点");
                listView_ap.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
                textView.setVisibility(View.VISIBLE);

                //客户端
                BackgroundTask.clearAll();
                BackgroundTask.mTimerHandling = new Timer();
                BackgroundTask.mTimerTaskHandling = new TimerTask() {
                    @Override
                    public void run() {
                        /*if (getActivity() == null){ //由于当线程结束时activity变得不可见,getActivity()有可能为空，需要提前判断
                            return;
                        }*/
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    wiFiDetailList = MainContext.INSTANCE.getScannerService().getWiFiData().getWiFiDetails();
                                    ApInfo_Search.setAllClientInfo(context, wiFiDetailList, listView_ap); //热点
                                    ClientInfo_Search.setAllClientInfo(context, wiFiDetailList, listView); //客户端
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                };
                BackgroundTask.mTimerHandling.schedule(BackgroundTask.mTimerTaskHandling, 0, 5000);
            }
        });
    }

    //添加MAC地址
    private void TargetMac(){
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.input_add_mac,null);
        final EditText remarksEt  = view.findViewById(R.id.input_mac);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        //remarksEt.setText(PrefSingleton.getInstance().getString("target_search"));
        remarksEt.setText(Target_mac);
        builder.setTitle("添加Mac");
        builder.setView(view);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Target_mac = remarksEt.getText().toString();
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
