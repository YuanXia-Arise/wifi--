package com.vrem.wifianalyzer.wifi.fragmentTargetSearch;

import android.content.Context;
import android.util.Log;
import android.widget.ListView;

import com.google.gson.Gson;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by huangche on 2019/12/27.
 */

public class ApInfo_Search {

    //设置所有热点客户端
    public static void setAllClientInfo(Context context,List<WiFiDetail> wiFiDetails,ListView listView) {
        List<WiFiDetail> wiFiDetailsTmp = wiFiDetails;
        final List<WiFiDetail> clientData = new ArrayList<>();
        Log.d(TAG, "9999==0>" + new Gson().toJson(wiFiDetailsTmp));
        for (int i = 0; i<wiFiDetailsTmp.size(); i++){
            WiFiDetail wiFiDetail_o = wiFiDetailsTmp.get(i);
            String target_search = PrefSingleton.getInstance().getString("target_search").toLowerCase();
            String a = wiFiDetail_o.getBSSID().toLowerCase();
            if (target_search.indexOf(a) != -1) {
                clientData.add(wiFiDetail_o);
            }
        }
        Log.d(TAG, "9999==1>" + new Gson().toJson(clientData));

        ApListSearchAdapter apListAdapter = new ApListSearchAdapter(context,clientData, R.layout.target_point_view_complete);
        listView.setAdapter(apListAdapter);
        apListAdapter.notifyDataSetChanged();
    }
}
