package com.vrem.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.vrem.wifianalyzer.wifi.common.PrefSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.WIFI_SERVICE;

public class WifiStatus {


    public String getWifi_SSID(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
        if (wm != null) {
            WifiInfo winfo = wm.getConnectionInfo();
            if (winfo != null) {
                String s = winfo.getSSID();
                if (s.length() > 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
                    return s.substring(1, s.length() - 1);
                }
            }
        }
        return "";
    }

    public boolean Wifi_Status(Context context) {
        String device = null;
        try {
            JSONObject jsonObject = new JSONObject(PrefSingleton.getInstance().getString("deviceInfo"));
            JSONObject dataJson = new JSONObject(jsonObject.getString("data"));
            device = dataJson.getString("device");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println("20210305==98>" + getWifi_SSID(context));
        System.out.println("20210305==99>" + device);
        if (device != null && getWifi_SSID(context).contains(device)) {
            return true;
        } else {
            return false;
        }
    }
}
