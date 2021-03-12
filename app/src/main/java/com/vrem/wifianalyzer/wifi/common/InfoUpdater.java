package com.vrem.wifianalyzer.wifi.common;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.wifi.fragmentWiFiHotspot.WIFIHotspotFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static android.content.ContentValues.TAG;

/**
 * 获取前置信息：电量%、4G信号强度%
 * */
public class InfoUpdater extends AsyncTask<Object, Object, JSONObject> {
    private Context mContext;
    private boolean mIsFirst;
    private boolean isAp = false;
    private String hotspotName;
    private String hotspotPsw;
    private Handler handler;


    public InfoUpdater(Context context, boolean isFirst) {
        mContext = context;
        mIsFirst = isFirst;
    }
    public InfoUpdater(Context context, boolean isFirst,boolean isAp, Handler handler, String hotspotName, String hotspotPsw) {
        mContext            = context;
        mIsFirst            = isFirst;
        this.isAp           = isAp;
        this.handler        = handler;
        this.hotspotName    = hotspotName;
        this.hotspotPsw     = hotspotPsw;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected JSONObject doInBackground  (Object... params) {
        try {
            JSONObject response = info(mContext);
            return response;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject param) {
        if (mIsFirst == false) {
            return;
        }
//        mProgressBar.setVisibility(View.GONE);
        if (param == null) {
            Toast.makeText(mContext, "出错啦", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int status = param.getInt("status");
            if (status == 0) {
                JSONObject data = param.getJSONObject("data");
                String devID;
                if (data.has("device")) {
                    devID = data.getString("device");
                } else {
                    devID = "HEHE2017";
                }
                PrefSingleton.getInstance().putString("device", devID);
            } else if (status == 1) {
                String devID = "HEHE2017";
//                Intent it = new Intent();
//                it.setClass(mContext, DeviceListActivity.class);
                PrefSingleton.getInstance().putString("device", devID);
//                mContext.startActivity(it);
//                ((Activity)mContext).overridePendingTransition(R.anim.slide_right_in,
//                        R.anim.slide_left_out);
            } else {
                Toast.makeText(mContext, "出错啦", Toast.LENGTH_SHORT).show();
                return;
            }
            //status = param.getInt("status");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject info(final Context context) throws JSONException {
        String url = PrefSingleton.getInstance().getString("url");

        JSONObject obj = new JSONObject();
        JSONObject param = new JSONObject();
        param.put("action", "info");
        if (isAp){ //是否打开热点
            JSONObject ap = new JSONObject();
            ap.put("essid",hotspotName);
            ap.put("pass",hotspotPsw);
            param.put("ap",ap);
        }
        //param.put("timestamp", System.currentTimeMillis() / 1000.0);
        obj.put("param", param);

        Log.w("INFO", "REQUEST: " + obj.toString());
        System.out.println("20210305==发送指令：" + obj.toString());

        RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,  obj, requestFuture, requestFuture);
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(context).getRequestQueue().add(jsonObjectRequest);

        try { // {"action":"info","data":{"battery":-1,"signal":"","version":"0.0.0","device":"FT21098"},"status":0}
            JSONObject response = requestFuture.get(10 - 1, TimeUnit.SECONDS);
            System.out.println("20210305==返回结果：" + response.toString());
            if (null != response ){
                PrefSingleton.getInstance().putString("deviceInfo",response.toString()); // 将数据存入数据存储类中
            }

            new InteractRecordDBUtils(mContext).easy_insert(obj.toString(), response.toString()); // 将请求命令、返回结果存入数据库

            Log.w("INFO", "RESPONSE: " + response.toString());
            if (isAp){ // 将结果到接口当中，供WIFIHotspotFragment类使用
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Message message = new Message();
                        message.what = 101;
                        handler.sendMessage(message);
                        WIFIHotspotFragment wifiHotspotFragment = new WIFIHotspotFragment();
                        if (Build.VERSION.SDK_INT >= 26){
                            wifiHotspotFragment.openHotspot8();
                        } else {
                            wifiHotspotFragment.createWifiHotspot(hotspotName,hotspotPsw);
                        }
                    }
                });
//                wifiHotspotFragment.createWifiHotspot8(MainContext.INSTANCE.getContext(),true);
            }
            return response;
        } catch (TimeoutException e) {
            Log.w("INFO_STEP_2", "TIMEOUT");
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
