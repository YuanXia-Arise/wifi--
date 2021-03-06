/*
 * WiFiAnalyzer
 * Copyright (C) 2018  VREM Software Development <VREMSoftwareDevelopment@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.vrem.wifianalyzer.wifi.accesspoint;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.vrem.util.WifiStatus;
import com.vrem.wifianalyzer.MainActivity;
import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.WifiInfoActivity;
import com.vrem.wifianalyzer.wifi.common.APInfoUpdater;
import com.vrem.wifianalyzer.wifi.common.InfoUpdater;
import com.vrem.wifianalyzer.wifi.common.MacSsidDBUtils;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;
import com.vrem.wifianalyzer.wifi.deviceList.Deviece;
import com.vrem.wifianalyzer.wifi.fragmentDos.DosFragment;
import com.vrem.wifianalyzer.wifi.fragmentSniffer.SnifferFragment;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;
import static android.content.Context.WIFI_SERVICE;


public class AccessPointsFragment extends Fragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    private AccessPointsAdapter accessPointsAdapter;
    private ExpandableListView expandableListView;

    private Vibrator vibrator;
    private FragmentManager manager; //Fragment?????????

    private List<WiFiDetail> wiFiDetailList;//wifi??????
    private String Channel;
    private String Mac;

        @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        manager = this.getFragmentManager();
        View view = inflater.inflate(R.layout.access_points_content, container, false); // ????????????

        swipeRefreshLayout = view.findViewById(R.id.accessPointsRefresh); // ????????????
        swipeRefreshLayout.setOnRefreshListener(new ListViewOnRefreshListener()); // ??????????????????

        accessPointsAdapter = new AccessPointsAdapter(getActivity());  // ??????AccessPointsAdapter???????????????
        expandableListView = view.findViewById(R.id.accessPointsView); // ??????ExpandableListView??????
        expandableListView.setAdapter(accessPointsAdapter); // ???expandableListView???????????????
        accessPointsAdapter.setExpandableListView(expandableListView); // ??????????????????ExpandableListView
        expandableListView.setVisibility(View.VISIBLE);

        //????????????
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                WiFiDetail detail = (WiFiDetail) accessPointsAdapter.getGroup(groupPosition); // ??????????????????
                Intent intent = new Intent(getActivity(),WifiInfoActivity.class); // ?????????Intent??????????????????????????????????????????
                intent.putExtra("wifiDetail",new Gson().toJson(detail)); // ??????????????????Intent??????????????????????????????????????????Gson??????????????????
                startActivity(intent); // ??????
                return true;
            }
        });

        //????????????
        expandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final WiFiDetail detail = (WiFiDetail) accessPointsAdapter.getGroup(position); //??????????????????

                String Ssid = detail.getSSID();
                if (Ssid.contains("_5G")){
                    Ssid = Ssid.replace("_5G","");
                } else if (Ssid.contains("-5G")){
                    Ssid = Ssid.replace("-5G","");
                } else if (Ssid.contains(" 5G")){
                    Ssid = Ssid.replace(" 5G","");
                }
                String Ssid1 = Ssid + "_5G";
                String Ssid2 = Ssid + "-5G";
                String Ssid3 = Ssid + " 5G";
                Channel = "";
                Mac = "";
                try{
                    wiFiDetailList = MainContext.INSTANCE.getScannerService().getWiFiData().getWiFiDetails();
                    for (int i = 0; i < wiFiDetailList.size(); i++){
                        String ssid = wiFiDetailList.get(i).getSSID();
                        String mac = wiFiDetailList.get(i).getBSSID();
                        String channel = wiFiDetailList.get(i).getWiFiSignal().getChannel();
                        if (ssid.equals(Ssid) || ssid.equals(Ssid1) || ssid.equals(Ssid2) || ssid.equals(Ssid3)){
                            Channel = Channel.equals("") ? channel : Channel + "," + channel;
                            Mac = Mac.equals("") ? mac : Mac + "," + mac;
                        }
                    }
                }catch (Exception e){}

                vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(30); //????????????
                View view1 = getActivity().getLayoutInflater().inflate(R.layout.context_menu, null);
                ArrayList<String> list = new ArrayList<String>();
                list.add("??????????????????");
                list.add("???????????????");
//                list.add("Wps??????");
                final Dialog dialog = new Dialog(getActivity());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(view1);
                Window dialogWindow = dialog.getWindow();
                WindowManager wm = getActivity().getWindowManager();
                Display display = wm.getDefaultDisplay();
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.width = (display.getWidth() * 2 / 3);
                dialogWindow.setAttributes(lp);
                dialog.show();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                RelativeLayout contextLayout = view1.findViewById(R.id.context_menu);
//                contextLayout.setBackgroundResource(R.drawable.contextbg3);
                final ListView listView = view1.findViewById(R.id.listview);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.context_menu_listitem, list);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0,View arg1, int arg2, long arg3) {
                            switch (arg2) {
                                case 0:
                                    DosFragment dosFragment = new DosFragment(); //??????Fragment??????
                                    Bundle dosBundle = new Bundle(); //??????bundle??????????????????
                                    dosBundle.putString("ssid",detail.getSSID());
                                    //dosBundle.putString("bssid",detail.getBSSID());
                                    dosBundle.putString("bssid",Mac);
                                    //dosBundle.putString("channel",detail.getWiFiSignal().getChannel());
                                    dosBundle.putString("channel",Channel);
                                    dosBundle.putString("mac",Mac);
                                    dosBundle.putString("Fragment","Dos");
                                    dosFragment.setArguments(dosBundle); //dosFragment????????????
                                    FragmentTransaction transaction; //Fragment??????
                                    transaction = manager.beginTransaction(); //????????????
                                    transaction.replace(R.id.main_fragment,dosFragment); //?????????????????????????????????????????????????????????????????????????????????replace
                                    transaction.commit(); //????????????
                                    dialog.dismiss();
                                    break;
                                case 1:
                                    SnifferFragment snifferFragment = new SnifferFragment();
                                    Bundle snifBundle = new Bundle();
                                    snifBundle.putString("ssid",detail.getSSID());
                                    snifBundle.putString("bssid",detail.getBSSID());
                                    snifBundle.putInt("channel",Integer.parseInt(detail.getWiFiSignal().getChannel()));
                                    snifBundle.putDouble("rate",detail.getRate());
                                    snifBundle.putString("Fragment","Sniffer");
                                    snifferFragment.setArguments(snifBundle);
                                    FragmentTransaction snifferFT;
                                    snifferFT = manager.beginTransaction();
                                    snifferFT.replace(R.id.main_fragment,snifferFragment);
                                    snifferFT.commit();//????????????
                                    dialog.dismiss();
                                    break;
                                /*case 2:
                                    WpsCrackFragment wpsCrackFragment = new WpsCrackFragment();
                                    FragmentTransaction wpsFT;
                                    Bundle wpsBundle = new Bundle();
                                    wpsBundle.putString("ssid",detail.getSSID());
                                    wpsBundle.putString("bssid",detail.getBSSID());
                                    wpsBundle.putString("channel",detail.getWiFiSignal().getChannel());
                                    wpsCrackFragment.setArguments(wpsBundle);
                                    wpsFT = manager.beginTransaction();
                                    wpsFT.replace(R.id.main_fragment,wpsCrackFragment);
                                    wpsFT.commit();//????????????
                                    dialog.dismiss();
                                    break;*/
                                default:
                                    break;
                            }
                    }
                });
                return true;
            }
        });

        MainContext.INSTANCE.getScannerService().register(accessPointsAdapter);
        if (MainContext.INSTANCE.getScannerService().isRunning() == false){
            MainContext.INSTANCE.getScannerService().resume();
        }

        /*Bundle bundle = getArguments();
        String status = bundle.getString("status");
        if (status != null) {
            if (status.equals("ok")){
                MainContext.INSTANCE.getMainActivity().recreate();
            }
        }*/
        return view;
    }

    //????????????
    private void refresh() {
        swipeRefreshLayout.setRefreshing(true);
        MainContext.INSTANCE.getScannerService().update(); // ??????????????????
        swipeRefreshLayout.setRefreshing(false);
    }


    @SuppressLint("LongLogTag")
    @Override
    public void onResume() {
        Log.w("AccessPointsFragment status","Resume");
        super.onResume();
        refresh();
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onPause() {
        Log.w("AccessPointsFragment status","Pause");
        super.onPause();
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onDestroy() {
        Log.w("AccessPointsFragment status","Destroy");
        MainContext.INSTANCE.getScannerService().unregister(accessPointsAdapter);
        super.onDestroy();
    }

    AccessPointsAdapter getAccessPointsAdapter() {
        return accessPointsAdapter;
    }


    //??????SwipeRefreshLayout???????????????????????? ????????????
    private class ListViewOnRefreshListener implements SwipeRefreshLayout.OnRefreshListener {
        @Override
        public void onRefresh() {
            new InfoUpdater(getActivity(), true).execute(); // ??????????????????
            MenuItem menuItem = ((MainActivity) getActivity()).getOptionMenu().getMenu().findItem(R.id.action_scanner);
            boolean wifi_status = new WifiStatus().Wifi_Status(getContext());
            if (!wifi_status) {
                Toast.makeText(getContext(), "?????????????????????", Toast.LENGTH_SHORT).show();
                PrefSingleton.getInstance().putString("deviceInfo",null); // ?????????????????????????????????
                swipeRefreshLayout.setRefreshing(false);
            } else {
                if (MainContext.INSTANCE.getScannerService().isRunning() == true) {
                    MainContext.INSTANCE.getScannerService().pause();
                    menuItem.setIcon(R.drawable.ic_play_arrow_grey_500_48dp);
                }
                swipeRefreshLayout.setRefreshing(true);
                //accessPointsAdapter.clear();
                //accessPointsAdapter.notifyDataSetChanged();
                if (MainContext.INSTANCE.getScannerService().isRunning() == false) {
                    MainContext.INSTANCE.getScannerService().resume();
                    menuItem.setIcon(R.drawable.ic_pause_grey_500_48dp);
                }
                MainContext.INSTANCE.getScannerService().update();
                accessPointsAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

}
