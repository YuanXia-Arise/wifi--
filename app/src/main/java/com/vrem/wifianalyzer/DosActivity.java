package com.vrem.wifianalyzer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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


public class DosActivity extends Activity {

	//立flag来判断显示的数据 未完成
	private boolean flag = false;

	private String ssid;
	private String bssid;
	private int channel;
	private Button apDosButton;
	private Button startButton;
	private Button cancelButton;
	private  Button channelDosButton;
	private Button mChannelDosButton;
	private String channelSelected = ""; //多频段
	private int channelId = 1;//单频段

	private String intentId="";

	private List<WiFiDetail> wiFiDetails;

	private String dosSsid;
	private String Cus;
	private Button cusBotton;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dos);

		startButton = findViewById(R.id.startButton);//开始dos攻击按钮
		cancelButton = findViewById(R.id.cancelButton);//取消攻击按钮
		channelDosButton = findViewById(R.id.channeldos);//单频段按钮
		mChannelDosButton = findViewById(R.id.mchanneldos);//多频段按钮

		final Context context = this;

		Intent intent = getIntent();
		intentId = intent.getStringExtra("id");
		Cus = intent.getStringExtra("cus");

		if (intentId.equals("1")){ //需要选择热点
			String wifiDetailJson = String.valueOf(new Gson().toJson(MainContext.INSTANCE.getScannerService().getWiFiData().getWiFiDetails()));
			Type type = new TypeToken<List<WiFiDetail>>(){}.getType();
			Gson gson = new Gson();
			wiFiDetails = gson.fromJson(wifiDetailJson,type);//将JSON数组转为对象

			apDosButton = findViewById(R.id.apdos);
			apDosButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					apDosButtonHandle(apDosButton,wiFiDetails);
				}
			});
		}else { //不需要选择热点
			ssid = intent.getStringExtra("ssid");
			bssid = intent.getStringExtra("bssid");
			channel	= Integer.parseInt(intent.getStringExtra("channel"));
			apDosButton = findViewById(R.id.apdos);
			apDosButton.setText(ssid);

			cusBotton = findViewById(R.id.custom);
			cusBotton.setText(Cus);
		}

		MainContext.INSTANCE.getScannerService().pause();//暂停扫描，防止命令冲突

		//开始按钮事件
		startButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				try {
					JSONObject jo = new JSONObject();
					JSONObject obj = new JSONObject();
					PrefSingleton.getInstance().Initialize(getApplicationContext());
					int gId = PrefSingleton.getInstance().getInt("id");
					PrefSingleton.getInstance().putInt("id", gId + 1);
					obj.put("id", gId); // 1-1
					JSONObject param = new JSONObject(); // 2
					JSONArray channels = new JSONArray();
					JSONArray wlist	= new JSONArray();
					JSONArray blist = new JSONArray();
					param.put("action", "mdk"); // 2-1

					jo.put("type", "cus");
					jo.put("detail", bssid);
					channels.put(channel);

					flag = true;
					writeFile("DosFlag.txt",String.valueOf(flag),bssid);

					blist.put(Cus);

					param.put("channels", channels); // 2-3
					param.put("wlist", wlist); // 2-4
					param.put("blist", blist); // 2-5
					param.put("interval", 1.5);
					obj.put("param", param);
					jo.put("data", obj);

					final JSONObject jof = jo;

					DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(context);
					devStatusDBUtils.open();
					final String devId = PrefSingleton.getInstance().getString("device");//获取设备ID
					devStatusDBUtils.preHandling(devId);
					devStatusDBUtils.close();
					BackgroundTask.clearAll();
					BackgroundTask.mTimerHandling = new Timer();
					BackgroundTask.mTimerTaskHandling = new TimerTask() {
						@Override
						public void run() {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									new DosUpdater(DosActivity.this, devId, jof,null,false,
											null,"106").execute(); //开始阻断
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

		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition(R.anim.slide_left_in,
						R.anim.slide_right_out);
			}
		});
	}

	//写文件 用于区分显示攻击的客户端
	private void writeFile(String fileName, String flag, String bssid){
		try {
			FileOutputStream fileOutputStream = openFileOutput(fileName,MODE_PRIVATE);
			fileOutputStream.write(flag.getBytes());
			fileOutputStream.write("\n".getBytes());//写入换行
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
		for (int i = 0; i < wiFiDetails.size(); i++){
			strings[i] = wiFiDetails.get(i).getSSID() + " 信道："+ wiFiDetails.get(i).getWiFiSignal().getChannel();
		}
		new AlertDialog.Builder(DosActivity.this)
				.setTitle("选择热点")
				.setSingleChoiceItems(strings, -1,new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
									dosSsid = wiFiDetails.get(arg1).getSSID();
							}
						})
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					private int index; // 表示选项的索引
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						index = which;
						dialog.dismiss();
						apDosButton.setText(dosSsid);
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
	protected void onStart() {
		Log.d("DosActivity status：","Start");
		super.onStart();
	}

	@Override
	protected void onPause() {
		BackgroundTask.clearAll();
		Log.d("DosActivity status：","Pause");
		super.onPause();
	}

	@Override
	protected void onResume() {
		Log.d("DosActivity status：","Resume");
		super.onResume();
	}
}
