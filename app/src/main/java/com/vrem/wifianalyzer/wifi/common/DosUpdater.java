package com.vrem.wifianalyzer.wifi.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.vrem.wifianalyzer.DeviceListActivity;
import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.dosClientModel.DosChildClientModel;
import com.vrem.wifianalyzer.wifi.dosClientModel.DosGroupClientModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static android.content.ContentValues.TAG;

public class DosUpdater extends AsyncTask<Object, Object, Void> {
    Context mContext;
    String mDevId;
    JSONObject mJo;

    boolean mStep1Needed;
    boolean mError;
    boolean mExit;
    boolean mStep1Run;

    JSONObject jExpandableLVS;
    boolean flag;
    String bssid;
    String param;
    String Param = null;
    String Run;

    private ExpandableListView expandableListView;

    public DosUpdater(Context context, String devID, JSONObject jo, ExpandableListView expandableListView,boolean flag,String bssid,String run) {
        mContext = context;
        mDevId = devID;
        mJo = jo;

        mError = false;
        mExit = false;
        mStep1Run = false;

        this.flag = flag;
        this.bssid = bssid;
        this.expandableListView = expandableListView;
        this.Run = run;
    }

    @Override  //1
    protected void onPreExecute() {
        DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(mContext);
        devStatusDBUtils.open();
        int dosStep1Done = devStatusDBUtils.getDosstep1done(mDevId); //第一次进来就等于1了，所以无法执行true操作
        devStatusDBUtils.close();

        if (dosStep1Done == 0) {
            mStep1Needed = true;
        } else {
            mStep1Needed = false;
        }
    }

    @Override  //2
    protected Void doInBackground  (Object... params) {
        try {
            Log.d(TAG, "555555==1>" + mStep1Needed);
            if (mStep1Needed) {
                try {
                    if(mJo.getJSONObject("data") == null){
                        return null;
                    }
                } catch (JSONException e) {
                    mExit = true;
                    e.printStackTrace();
                    return null;
                }  catch (NullPointerException e) {
                    mExit = true;
                    e.printStackTrace();
                    return null;
                }
                int r1 = dosStep1(mContext, mJo.getJSONObject("data"));
                Log.d(TAG, "555555==2>" + r1 + mJo.getJSONObject("data"));
                if (r1 < 0) {
                    mError = true;
                    return null;
                }
                DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(mContext);
                devStatusDBUtils.open();
                devStatusDBUtils.dosStep1Done(mDevId, mJo.getString("type"), mJo.getString("detail"));
                devStatusDBUtils.close();
                mStep1Run = true;
                return null;
            }

            param = PrefSingleton.getInstance().getString("param");
            if (param.length() > 8){
                Param = param.substring(2,param.length()-2);
                Log.d(TAG, "777777==1>" + Param);
            }

            JSONObject response = dosStep2(mContext);
            if (response == null) {
                return null;
            }
            jExpandableLVS = response;
            Log.w("DOS_STEP_2", response.toString());
        } catch (JSONException e) {
            mExit = true;
            e.printStackTrace();
            Log.w("DOS_ERROR", "STEP2 INVALID RESPONS");
            DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(mContext);
            devStatusDBUtils.open();
            devStatusDBUtils.dosCancel(mDevId);
            devStatusDBUtils.close();
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void param) {
        Log.d(TAG, "onPostExecute: 888888-1:" + mError + mStep1Run + mExit);
        if (jExpandableLVS != null){
            try {
                expandableListView.setAdapter(new DosClientExpandableAdapter(getDosGroupList(jExpandableLVS),Run));
                int groupCount = expandableListView.getExpandableListAdapter().getGroupCount();
                for (int i = 0; i < groupCount; i++) {
                    expandableListView.expandGroup(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (mError == true) {
            Toast.makeText(mContext, "出错啦",Toast.LENGTH_SHORT).show();
            return;
        }
        if (mStep1Run == true) {
            Intent intent = new Intent();
            intent.setClass(mContext,DeviceListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);
            ((Activity) mContext).overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out);
            ((Activity) mContext).finish();
            return;
        }
        if (mExit == true) {
            DevStatusDBUtils devStatusDBUtils1 = new DevStatusDBUtils(mContext);
            devStatusDBUtils1.open();
            devStatusDBUtils1.scanStep1Done(mDevId);//扫描结束时的sql语句
            devStatusDBUtils1.close();

            BackgroundTask.clearAll();

            /*Intent intent = new Intent();
            intent.setClass(mContext,DeviceListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);*/

            ((Activity) mContext).overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out);
            ((Activity) mContext).finish();
        }
    }

    //客户端拼接
    private List<DosGroupClientModel> getDosGroupList(JSONObject jsonObject) throws JSONException {
//        DosActivity dosActivity = new DosActivity();
        String strData = jsonObject.getString("data");
        JSONObject jsonObjectData = new JSONObject(strData);
        JSONObject jsonObjectAps = (JSONObject) jsonObjectData.get("aps");
        JSONObject jsonObjectStas = (JSONObject) jsonObjectData.get("stas");

        Iterator iteratorAps = jsonObjectAps.keys();
        List<DosGroupClientModel> dList = new ArrayList<>();
        if (flag){
            while(iteratorAps.hasNext()){
                String apsKey = (String) iteratorAps.next(); //宿主bssid
                if(apsKey.equals(bssid)){
                    String apsValues = jsonObjectAps.getString(apsKey);
                    JSONObject apsDosInfo = new JSONObject(apsValues); //宿主bssid参数
                    int cout = apsDosInfo.getInt("count"); //攻击次数
                    int rx_datas = apsDosInfo.getInt("rx_datas"); //接收数据
                    int tx_datas = apsDosInfo.getInt("tx_datas"); //发送数据
                    DosGroupClientModel dosGroupClientModel = new DosGroupClientModel(null,
                            0, 0, 0, null);
                    dosGroupClientModel.setGroup_bssid(apsKey);
                    dosGroupClientModel.setGroup_count(cout);
                    dosGroupClientModel.setGroup_rx_datas(rx_datas);
                    dosGroupClientModel.setGroup_tx_datas(tx_datas);

                    ArrayList<DosChildClientModel> alDosChildClientModels = new ArrayList<>();
                    Iterator iteratorStas = jsonObjectStas.keys();
                    while(iteratorStas.hasNext()){

                        Log.d(TAG, "444==00>" + Run);
                        if (Run != null) {
                            String cus_bssid = PrefSingleton.getInstance().getString("param").
                                    substring(2,PrefSingleton.getInstance().getString("param").length()-2);
                            String stasKey = (String) iteratorStas.next();
                            if (cus_bssid.equals(stasKey)) {
                                String stasValeus = jsonObjectStas.getString(stasKey);
                                JSONObject stasDosInfo = new JSONObject(stasValeus);
                                String ap_bssid = stasDosInfo.getString("ap_bssid");
                                if(apsKey.equals(ap_bssid)){
                                    DosChildClientModel dosChildClientModel = new DosChildClientModel(null,
                                            0,0, 0);
                                    int stas_count = stasDosInfo.getInt("count");
                                    int stas_rx_datas = stasDosInfo.getInt("rx_datas");
                                    int stas_tx_datas = stasDosInfo.getInt("tx_datas");

                                    dosChildClientModel.setChild_bssid(stasKey);
                                    dosChildClientModel.setChild_count(stas_count);
                                    dosChildClientModel.setChild_rx_datas(stas_rx_datas);
                                    dosChildClientModel.setChild_tx_datas(stas_tx_datas);
                                    Log.d(TAG, "444==01>" + stasKey + " " + stas_count + " " + stas_rx_datas + " " + stas_tx_datas);
                                    alDosChildClientModels.add(dosChildClientModel);
                                    dosGroupClientModel.setDosChildClientModelList(alDosChildClientModels);
                                }
                            }
                        } else {
                            String stasKey = (String) iteratorStas.next(); //客户端bssid
                            String stasValeus = jsonObjectStas.getString(stasKey);
                            JSONObject stasDosInfo = new JSONObject(stasValeus); //客户端bssid参数
                            String ap_bssid = stasDosInfo.getString("ap_bssid"); //宿主MAC地址
                            if(apsKey.equals(ap_bssid)){
                                DosChildClientModel dosChildClientModel = new DosChildClientModel(null,
                                        0,0, 0);
                                int stas_count = stasDosInfo.getInt("count"); //攻击次数
                                int stas_rx_datas = stasDosInfo.getInt("rx_datas"); //接收数据
                                int stas_tx_datas = stasDosInfo.getInt("tx_datas"); //发送数据

                                dosChildClientModel.setChild_bssid(stasKey);
                                dosChildClientModel.setChild_count(stas_count);
                                dosChildClientModel.setChild_rx_datas(stas_rx_datas);
                                dosChildClientModel.setChild_tx_datas(stas_tx_datas);
                                Log.d(TAG, "444==02>" + stasKey + " " + stas_count + " " + stas_rx_datas + " " + stas_tx_datas);
                                alDosChildClientModels.add(dosChildClientModel);
                                dosGroupClientModel.setDosChildClientModelList(alDosChildClientModels);
                            }
                        }
                    }
                    dList.add(dosGroupClientModel);
                }
            }
        }else {
            while(iteratorAps.hasNext()){
                DosGroupClientModel dosGroupClientModel = new DosGroupClientModel(null, 0,
                        0, 0, null);
                String apsKey =(String) iteratorAps.next();
                String apsValues = jsonObjectAps.getString(apsKey);
                JSONObject apsDosInfo = new JSONObject(apsValues);
                int cout = apsDosInfo.getInt("count");
                int rx_datas = apsDosInfo.getInt("rx_datas");
                int tx_datas = apsDosInfo.getInt("tx_datas");

                dosGroupClientModel.setGroup_bssid(apsKey);
                dosGroupClientModel.setGroup_count(cout);
                dosGroupClientModel.setGroup_rx_datas(rx_datas);
                dosGroupClientModel.setGroup_tx_datas(tx_datas);

                ArrayList<DosChildClientModel> alDosChildClientModels = new ArrayList<>();
                Iterator iteratorStas = jsonObjectStas.keys();
                while(iteratorStas.hasNext()){
                    Log.d(TAG, "666==00>" + Run);
                    if (Run != null) {
                        String cus_bssid = PrefSingleton.getInstance().getString("param").
                                substring(2,PrefSingleton.getInstance().getString("param").length()-2);
                        String stasKey = (String) iteratorStas.next();
                        if (cus_bssid.equals(stasKey)) {
                            String stasValeus = jsonObjectStas.getString(stasKey);
                            JSONObject stasDosInfo = new JSONObject(stasValeus);
                            String ap_bssid = stasDosInfo.getString("ap_bssid");
                            if(apsKey.equals(ap_bssid)){
                                DosChildClientModel dosChildClientModel = new DosChildClientModel(null,
                                        0,0, 0);
                                int stas_count = stasDosInfo.getInt("count");
                                int stas_rx_datas = stasDosInfo.getInt("rx_datas");
                                int stas_tx_datas = stasDosInfo.getInt("tx_datas");

                                dosChildClientModel.setChild_bssid(stasKey);
                                dosChildClientModel.setChild_count(stas_count);
                                dosChildClientModel.setChild_rx_datas(stas_rx_datas);
                                dosChildClientModel.setChild_tx_datas(stas_tx_datas);
                                Log.d(TAG, "666==01>" + stasKey + " " + stas_count + " " + stas_rx_datas + " " + stas_tx_datas);
                                alDosChildClientModels.add(dosChildClientModel);
                                dosGroupClientModel.setDosChildClientModelList(alDosChildClientModels);
                            }
                        }
                    } else {
                        String stasKey = (String) iteratorStas.next();
                        String stasValeus = jsonObjectStas.getString(stasKey);
                        JSONObject stasDosInfo = new JSONObject(stasValeus);
                        String ap_bssid = stasDosInfo.getString("ap_bssid");
                        if(apsKey.equals(ap_bssid)) {
                            DosChildClientModel dosChildClientModel = new DosChildClientModel(null,
                                    0, 0, 0);
                            int stas_count = stasDosInfo.getInt("count");
                            int stas_rx_datas = stasDosInfo.getInt("rx_datas");
                            int stas_tx_datas = stasDosInfo.getInt("tx_datas");

                            dosChildClientModel.setChild_bssid(stasKey);
                            dosChildClientModel.setChild_count(stas_count);
                            dosChildClientModel.setChild_rx_datas(stas_rx_datas);
                            dosChildClientModel.setChild_tx_datas(stas_tx_datas);
                            Log.d(TAG, "666==02>" + stasKey + " " + stas_count + " " + stas_rx_datas + " " + stas_tx_datas);
                            alDosChildClientModels.add(dosChildClientModel);
                            dosGroupClientModel.setDosChildClientModelList(alDosChildClientModels);
                        }
                    }
                }
                dList.add(dosGroupClientModel);
            }
        }
        return dList;
    }


    public int dosStep1(final Context context, JSONObject jo) throws JSONException {
//        String url ="http://192.168.100.1:9494";
        String url = PrefSingleton.getInstance().getString("url");

        JSONObject obj = jo; //{"id":466,"param":{"action":"mdk","channels":[13],"wlist":[],"blist":["2e:b2:1a:27:ac:6b"],"interval":1.5}}
        Log.w("DOS_STEP_1_REQUEST", obj.toString());

        if (Run != null) {
            param = mJo.getJSONObject("data").getJSONObject("param").getJSONArray("blist").toString();
            Log.d(TAG, "777777==0 param:>" + param);
            PrefSingleton.getInstance().putString("param", param);
            Log.d(TAG, "111000==>" + mJo.getJSONObject("data"));
        }

        RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, obj, requestFuture, requestFuture);
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(context).getRequestQueue().add(jsonObjectRequest);
        try {
            JSONObject response = requestFuture.get(10 - 1, TimeUnit.SECONDS);

            new InteractRecordDBUtils(mContext).easy_insert(obj.toString(), response.toString()); //将请求命令、返回结果存入数据库

            int status = response.getInt("status");
            if (status == 0) {
                Log.w("DOS_STEP_1", "RESPONSE:" + response.toString());
                return 0;
            } else {
                Log.w("DOS_STEP_1", "UNEXPECTED RESPONSE: " + response.toString());
                return -2;
            }
        } catch (TimeoutException e) {
            Log.w("DOS_STEP_1", "TIMEOUT");
            return -1;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return -3;
    }

    public JSONObject dosStep2(final Context context) throws JSONException {
//        String url ="http://192.168.100.1:9494";
        String url = PrefSingleton.getInstance().getString("url");

        JSONObject obj = new JSONObject();
        JSONObject param = new JSONObject();
        param.put("action", "action");
        obj.put("param", param);

        Log.w("DOS_STEP_2", "REQUEST: " + obj.toString());

        RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, obj, requestFuture, requestFuture);
        VolleySingleton.getInstance(context).getRequestQueue().add(jsonObjectRequest);

        try {
            JSONObject response = requestFuture.get(5 - 1, TimeUnit.SECONDS);
            int status = response.getInt("status");
            if (status == 0) {
                Log.w("DOS_STEP_2", "RESPONSE:" + response.toString());
                return response;
            } else {
                Log.w("DOS_STEP_2", "UNEXPECTED RESPONSE: " + response.toString());
                return null;
            }
        } catch (TimeoutException e) {
            Log.w("DOS_STEP_2", "TIMEOUT");
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}