package com.vrem.wifianalyzer.wifi.fragmentTargetSearch;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.vrem.wifianalyzer.GetCompany;
import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;
import com.vrem.wifianalyzer.wifi.model.ClientListAdapter;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by huangche on 2019/12/27.
 */

public class ClientInfo_Search {

    private String mac;
    private String probe;
    private String company;
    private String SSID;
    private String power;
    private String channel;

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getProbe() {
        return probe;
    }

    public void setProbe(String probe) {
        this.probe = probe;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    //搜索热点客户端
    public static void setAllClientInfo(Context context,List<WiFiDetail> wiFiDetails, ListView listView) throws JSONException {
        List<WiFiDetail> wiFiDetailsTmp = wiFiDetails;
        List<ClientInfo_Search> clientData = new ArrayList<ClientInfo_Search>();
        for (int i = 0; i < wiFiDetailsTmp.size(); i++){ //遍历所有热点
            if (!"[]".equals(String.valueOf(wiFiDetailsTmp.get(i).getClient()))){
                String SSID = wiFiDetailsTmp.get(i).getSSID();
                JSONArray client = new JSONArray(wiFiDetailsTmp.get(i).getClient());
                Log.d(TAG, "2020==22>" + client);
                for (int j = 0; j < client.length(); j++) { //遍历客户端
                    String a = PrefSingleton.getInstance().getString("target_search").toLowerCase();
                    String mac_search = client.getJSONObject(j).getString("mac");
                    Log.d(TAG, "2020==23>" + mac_search);
                    Log.d(TAG, "2020==24>" + a);
                    if (a.indexOf(mac_search) != -1) {
                        ClientInfo_Search clientInfo = new ClientInfo_Search();
                        JSONObject client_data = (JSONObject) client.get(j);
                        Log.d(TAG, "2020==25>" + client_data);
                        clientInfo.setSSID(SSID);
                        clientInfo.setMac(client.getJSONObject(j).getString("mac"));
                        clientInfo.setProbe(client.getJSONObject(j).getString("probe"));
                        String company_info = new GetCompany().read_csv(client.getJSONObject(j).getString("mac").
                                substring(0, 8).replace(":", "").toUpperCase());
                        clientInfo.setCompany(company_info);
                        clientInfo.setPower(client.getJSONObject(j).getString("power"));
                        clientData.add(clientInfo);
                        System.out.println("20200921==99>" + wiFiDetailsTmp.get(i).getWiFiSignal().getChannel());
                        Log.d(TAG, "2020==27>" + new Gson().toJson(clientData));
                    }
                }
            }
        }
        Log.d(TAG, "2020==28>" + new Gson().toJson(clientData));
        ClientListSearchAdapter clientListAdapter = new ClientListSearchAdapter(context,clientData, R.layout.client_list_item_search);
        listView.setAdapter(clientListAdapter);
        clientListAdapter.notifyDataSetChanged();
    }
}
