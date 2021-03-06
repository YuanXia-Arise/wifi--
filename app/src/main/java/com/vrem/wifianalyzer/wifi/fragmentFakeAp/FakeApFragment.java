package com.vrem.wifianalyzer.wifi.fragmentFakeAp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.vrem.util.WifiStatus;
import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.common.BackgroundTask;
import com.vrem.wifianalyzer.wifi.common.CommonUpdater;
import com.vrem.wifianalyzer.wifi.common.DevStatusDBUtils;
import com.vrem.wifianalyzer.wifi.common.FakeAPUpdater;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ZhenShiJie on 2018/4/24.
 */

public class FakeApFragment extends Fragment {


    private EditText ssidEdit;
    private RelativeLayout ssidLayout;
    private TextView encryEdit;
    private RelativeLayout encryLayout;
    private EditText passEdit;
    private RelativeLayout passLayout;
    private TextView channelEdit;
    private RelativeLayout channelLayout;
    private TextView encryMethodEdit;
    private RelativeLayout encryMethodLayout;
    private Button startButton;
    private Button cancelButton;
    private RelativeLayout openOptionLayout;
    private RelativeLayout encryOptionLayout;
    private RelativeLayout openChoose;
    private RelativeLayout encryChoose;
    private ImageButton openButton;
    private ImageButton encryButton;
    private EditText openSsidEdit;
    private RelativeLayout openChannelLayout;
    private TextView openChannelEdit;
    private int encryId = 1;
    private int channelId = 1;
    private int openChannelId = 3;
    private int encryMethodId = 1;

    private Spinner fakeSpinner;
    private RelativeLayout fakeLayout;
    private RelativeLayout inputLayout;
    private RelativeLayout apchooseLayout;
    private RelativeLayout wifiPassLayout;
    private Button apchooseButton;
    private EditText wifipassedit;

    private String apSsid = "";
    private int apPower = 0;
    private String apPrivacy = "";
    private String apMac = "";
    private String apCipher = "";
    private int  apChannel = 0;
    private String devId = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fakeap, container, false);
        ssidEdit = view.findViewById(R.id.ssidedit);
        ssidLayout = view.findViewById(R.id.ssidlayout);
        encryEdit = view.findViewById(R.id.encryedit);
        encryLayout = view.findViewById(R.id.encrylayout);
        passEdit = view.findViewById(R.id.passedit);
        passLayout = view.findViewById(R.id.passlayout);
        channelEdit = view.findViewById(R.id.channeledit);
        channelLayout = view.findViewById(R.id.channellayout);
        encryMethodEdit = view.findViewById(R.id.encrymethodedit);
        encryMethodLayout = view.findViewById(R.id.encrymethodlayout);
        startButton = view.findViewById(R.id.startButton);
        openOptionLayout = view.findViewById(R.id.openoptionlayout);
        encryOptionLayout = view.findViewById(R.id.encryoptionlayout);
        openChoose = view.findViewById(R.id.openchoose);
        encryChoose = view.findViewById(R.id.encrychoose);
        openButton = view.findViewById(R.id.openbtn);
        encryButton = view.findViewById(R.id.encrybtn);
        openSsidEdit = view.findViewById(R.id.openssidedit);
        fakeSpinner = view.findViewById(R.id.fakespinner);
        SpinnerAdapter adapter1 = ArrayAdapter.createFromResource(view.getContext(), R.array.fake_spinner, R.layout.dropdown_listitem);
        fakeSpinner.setAdapter(adapter1);
        fakeLayout = view.findViewById(R.id.fakelayout);
        inputLayout = view.findViewById(R.id.inputlayout);
        apchooseLayout = view.findViewById(R.id.apchooseLayout);
        wifiPassLayout = view.findViewById(R.id.wifiapsslayout);
        openChannelLayout = view.findViewById(R.id.openchannellayout);
        openChannelEdit = view.findViewById(R.id.openchanneledit);
        apchooseButton =view.findViewById(R.id.apchooseButton);
        wifipassedit = view.findViewById(R.id.wifipassedit);
        devId = PrefSingleton.getInstance().getString("device");//????????????ID
        MainContext.INSTANCE.getScannerService().pause();//?????????????????????????????????
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Context context = getContext();

        apchooseButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                View view = FakeApFragment.this.getLayoutInflater().inflate(R.layout.scan_dialog_list,null);
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(view);
                dialog.setTitle("????????????");
                dialog.show();
                final ListView listview = view.findViewById(R.id.scanlist);
                final ProgressBar progressBar = view.findViewById(R.id.progressbar);
                final TextView refresh = view.findViewById(R.id.clickrefresh);
                final TextView noData = view.findViewById(R.id.nodata);
                refresh.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        refresh.setVisibility(View.GONE);
                        try {
                            DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(context);
                            devStatusDBUtils.open();
                            devStatusDBUtils.preScan(devId);
                            devStatusDBUtils.close();

                            BackgroundTask.clearAll();
                            BackgroundTask.mTimerScan = new Timer();
                            BackgroundTask.mTimerTaskScan = new TimerTask() {
                                public void run() {
                                    if (getActivity() == null){ //????????????????????????activity???????????????,getActivity()????????????????????????????????????
                                        return;
                                    }
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            new CommonUpdater(context, listview, devId, 0, progressBar, 1,
                                                    0, refresh, noData, true).execute();
                                        }
                                    });
                                }
                            };
                            BackgroundTask.mTimerScan.schedule(BackgroundTask.mTimerTaskScan, 0, 3000);
                        } catch (/*JSON*/Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
                try {
                    DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(context);
                    devStatusDBUtils.open();
                    devStatusDBUtils.preScan(devId);
                    devStatusDBUtils.close();

                    BackgroundTask.clearAll();
                    BackgroundTask.mTimerScan = new Timer();
                    BackgroundTask.mTimerTaskScan = new TimerTask() {
                        @Override
                        public void run() {
                            if (getActivity() == null){ //????????????????????????activity???????????????,getActivity()????????????????????????????????????
                                return;
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new CommonUpdater(context, listview, devId, 0, progressBar, 1,
                                            0, refresh, noData, true).execute();
                                }
                            });
                        }
                    };
                    BackgroundTask.mTimerScan.schedule(BackgroundTask.mTimerTaskScan, 0, 3000);
                } catch (/*JSON*/Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1,
                                            int arg2, long arg3) {
                        // TODO Auto-generated method stub
                        TextView ssid = arg1.findViewById(R.id.ssid);
                        WiFiDetail apInfo = (WiFiDetail) ssid.getTag();
                        apchooseButton.setText(apInfo.getSSID());
                        apSsid = apInfo.getSSID();
                        apPower = 0;
                        apPrivacy = apInfo.getCapabilities();
                        apMac = apInfo.getBSSID();
                        apCipher = apInfo.getCipher();
                        apChannel = Integer.parseInt(apInfo.getWiFiSignal().getChannel());

                        if (apPrivacy.equals("OPEN")) {
                            wifiPassLayout.setVisibility(View.GONE);
                        } else {
                            wifiPassLayout.setVisibility(View.VISIBLE);
                        }
                        dialog.dismiss();
                    }
                });
            }
        });

        fakeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    inputLayout.setVisibility(View.VISIBLE);
                    apchooseLayout.setVisibility(View.GONE);
                } else if (i == 1) {
                    inputLayout.setVisibility(View.VISIBLE);
                    apchooseLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        openChoose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (openOptionLayout.getVisibility() != View.VISIBLE) {
                    openOptionLayout.setVisibility(View.VISIBLE);
                    encryOptionLayout.setVisibility(View.GONE);
                    openButton.setBackgroundResource(R.drawable.selected);
                    encryButton.setBackgroundResource(R.drawable.notselect);
                }
            }
        });

        openButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (openOptionLayout.getVisibility() != View.VISIBLE) {
                    openOptionLayout.setVisibility(View.VISIBLE);
                    encryOptionLayout.setVisibility(View.GONE);
                    openButton.setBackgroundResource(R.drawable.selected);
                    encryButton.setBackgroundResource(R.drawable.notselect);
                }
            }
        });

        encryChoose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (encryOptionLayout.getVisibility() != View.VISIBLE) {
                    encryOptionLayout.setVisibility(View.VISIBLE);
                    openOptionLayout.setVisibility(View.GONE);
                    openButton.setBackgroundResource(R.drawable.notselect);
                    encryButton.setBackgroundResource(R.drawable.selected);
                }
            }
        });
        encryButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (encryOptionLayout.getVisibility() != View.VISIBLE) {
                    encryOptionLayout.setVisibility(View.VISIBLE);
                    openOptionLayout.setVisibility(View.GONE);
                    openButton.setBackgroundResource(R.drawable.notselect);
                    encryButton.setBackgroundResource(R.drawable.selected);
                }
            }
        });
        openChannelLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                final String[] channelString = {"??????1", "??????2", "??????3", "??????4", "??????5", "??????6", "??????7", "??????8", "??????9",
                        "??????10", "??????11", "??????12", "??????13", "??????14", "??????36", "??????38", "??????40", "??????42", "??????44", "??????46",
                        "??????48", "??????52", "??????56", "??????60", "??????64", "??????149", "??????153", "??????157", "??????161", "??????165"};
                new AlertDialog.Builder(context)
                        .setTitle("????????????")
                        .setSingleChoiceItems(channelString, openChannelId - 1,
                                new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface arg0,
                                                        int arg1) {
                                        // TODO Auto-generated method stub
                                        if (arg1 >= 0) {
                                            openChannelId = Integer.parseInt(channelString[arg1].replace("??????", ""));//arg1 + 1;
                                        }
                                    }
                                })
                        .setPositiveButton("??????",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        // TODO Auto-generated method stub
                                        dialog.dismiss();
                                        openChannelEdit.setText("??????" + openChannelId);//channelString[openChannelId - 1]);
                                    }

                                })
                        .setNegativeButton("??????",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        // TODO Auto-generated method stub
                                        dialog.dismiss();
                                    }
                                }).show();
            }
        });

        encryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                final String[] encryString = {"WPA", "WPA2", "WPA2 WPA"}; //WPA-PSK
                new AlertDialog.Builder(context)
                        .setTitle("??????????????????")
                        .setSingleChoiceItems(encryString, encryId - 1,
                                new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface arg0,
                                                        int arg1) {
                                        // TODO Auto-generated method stub
                                        if (arg1 >= 0) {
                                            encryId = arg1 + 1;
                                        }
                                    }
                                })
                        .setPositiveButton("??????",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        // TODO Auto-generated method stub
                                        dialog.dismiss();
                                        encryEdit.setText(encryString[encryId - 1]);

                                    }

                                })
                        .setNegativeButton("??????",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        // TODO Auto-generated method stub
                                        dialog.dismiss();
                                    }
                                }).show();
            }
        });

        channelLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                final String[] channelString = {"??????1", "??????2", "??????3", "??????4", "??????5", "??????6", "??????7", "??????8", "??????9",
                        "??????10", "??????11", "??????12", "??????13", "??????14", "??????36", "??????38", "??????40", "??????42", "??????44", "??????46",
                        "??????48", "??????52", "??????56", "??????60", "??????64", "??????149", "??????153", "??????157", "??????161", "??????165"};
                new AlertDialog.Builder(context)
                        .setTitle("????????????")
                        .setSingleChoiceItems(channelString, channelId - 1,
                                new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface arg0,
                                                        int arg1) {
                                        // TODO Auto-generated method stub
                                        if (arg1 >= 0) {
                                            channelId = Integer.parseInt(channelString[arg1].replace("??????", ""));//arg1 + 1;
                                        }
                                    }
                                })
                        .setPositiveButton("??????",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        // TODO Auto-generated method stub
                                        dialog.dismiss();
                                        channelEdit.setText("??????" + channelId); //channelString[channelId - 1]);
                                    }

                                })
                        .setNegativeButton("??????",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        // TODO Auto-generated method stub
                                        dialog.dismiss();
                                    }
                                }).show();
            }
        });

        encryMethodLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                final String[] encryMethodString = {"TKIP", "CCMP", "TKIP CCMP"};

                new AlertDialog.Builder(context)
                        .setTitle("??????????????????")
                        .setSingleChoiceItems(encryMethodString, encryMethodId - 1,
                                new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface arg0,
                                                        int arg1) {
                                        // TODO Auto-generated method stub
                                        if (arg1 >= 0) {
                                            encryMethodId = arg1 + 1;
                                        }
                                    }
                                })
                        .setPositiveButton("??????",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        // TODO Auto-generated method stub
                                        dialog.dismiss();
                                        encryMethodEdit.setText(encryMethodString[encryMethodId - 1]);
                                    }

                                })
                        .setNegativeButton("??????",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        // TODO Auto-generated method stub
                                        dialog.dismiss();
                                    }
                                }).show();
            }
        });

        //??????????????????
        startButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                getFragmentManager().beginTransaction().addToBackStack(null).commit(); //???????????????
                if (fakeSpinner.getSelectedItemId() == 0) {
                    sendCommand("wifi_fake_ap");
                } else if (fakeSpinner.getSelectedItemId() == 1) {
                    if (apchooseButton.getText().toString().equals("??????????????????") == false
                            && ((wifipassedit.getText().toString().equals("") && apPrivacy.equals("OPN"))
                            || (!(wifipassedit.getText().toString().equals("")) && !apPrivacy.equals("OPN")))) {
                        sendCommand("connect_wifi_and_fake");
                    } else {
                        Toast.makeText(context, "???????????????????????????????????????", Toast.LENGTH_SHORT).show();
                        return;
                    }

                }
            }
        });
    }

    @Override
    public void onStart() {
        Log.d("FakeAp status???","Start");
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d("FakeAp status???","Resume");
        super.onResume();
    }

    @Override
    public void onPause() {
        BackgroundTask.clearAll();
        Log.d("FakeAp status???","Pause");
        super.onPause();
    }

    private  void sendCommand(String command){
        PrefSingleton.getInstance().remove_fake();
        final Context context1 = getView().getContext();
        JSONObject jo = new JSONObject();

        if (openOptionLayout.getVisibility() == View.VISIBLE ) { // ??????????????????
            if(!(openSsidEdit.getText().toString().equals(""))){
                // 4G ??? wifi ??????
                try {
                    jo.put("net", "open");
                    jo.put("out", "4g" );
                    jo.put("essid", openSsidEdit.getText().toString());
                    jo.put("channel", openChannelId);
                    PrefSingleton.getInstance().putString("fake_net","open");
                    PrefSingleton.getInstance().putString("fake_out","4g");
                    PrefSingleton.getInstance().putString("fake_essid",openSsidEdit.getText().toString());
                    PrefSingleton.getInstance().putInt("fake_channel",openChannelId);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if(command.equals("connect_wifi_and_fake")){ // wifi ??????
                    try {
                        jo.put("out", "wifi");
                        jo.put("ssid", apSsid); // essid???fakeap??????ssid???out ap???
                        jo.put("ticket", wifipassedit.getText().toString()); // out ap password
                        jo.put("ap_channel", apChannel);
                        PrefSingleton.getInstance().putString("fake_out","wifi");
                        PrefSingleton.getInstance().putString("fake_ssid",apSsid);
                        PrefSingleton.getInstance().putString("fake_ticket",wifipassedit.getText().toString());
                        PrefSingleton.getInstance().putInt("fake_ap_channel",apChannel);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }else{
                Toast.makeText(context1, "???????????????SSID???", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (encryOptionLayout.getVisibility() == View.VISIBLE && !(ssidEdit.getText().toString().equals(""))
                && !(encryEdit.getText().toString().equals(""))
                && !(passEdit.getText().toString().equals(""))
                && !(channelEdit.getText().toString().equals(""))
                && !(encryMethodEdit.getText().toString().equals(""))) { // ????????????

            String ssid = ssidEdit.getText().toString();
            String password = passEdit.getText().toString();
            if (password.length() < 8) {
                passEdit.setText("");
                Toast.makeText(getContext(), "??????????????????8????????????????????????", Toast.LENGTH_SHORT).show();
                return;
            } else if (password.length() > 63) {
                passEdit.setText("");
                Toast.makeText(getContext(), "??????????????????63????????????????????????", Toast.LENGTH_SHORT).show();
                return;
            }
            // 4G ??? wifi ??????
            try {
                jo.put("net", "enc");
                jo.put("out", "4g" );
                jo.put("essid", ssid); //jo.put("essid", ssidEdit.getText().toString());
                jo.put("channel", channelId);
                jo.put("security", encryEdit.getText().toString()); //wap,wap2,wap2-wap
                jo.put("password", password); //jo.put("password", passEdit.getText().toString());
                jo.put("encryption", encryMethodEdit.getText().toString()); // TKIP,CCMP,TCIP-CCMP
                PrefSingleton.getInstance().putString("fake_net","enc");
                PrefSingleton.getInstance().putString("fake_out","4g");
                PrefSingleton.getInstance().putString("fake_essid",ssid);
                PrefSingleton.getInstance().putInt("fake_channel",channelId);
                PrefSingleton.getInstance().putString("fake_security",encryEdit.getText().toString());
                PrefSingleton.getInstance().putString("fake_password",password);
                PrefSingleton.getInstance().putString("fake_encryption",encryMethodEdit.getText().toString());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if(command.equals("connect_wifi_and_fake")){ // wifi??????
                try {
                    jo.put("out", "wifi");
                    jo.put("ssid", apSsid);
                    jo.put("ticket", wifipassedit.getText().toString());
                    jo.put("ap_channel", apChannel);
                    PrefSingleton.getInstance().putString("fake_out","wifi");
                    PrefSingleton.getInstance().putString("fake_ssid",apSsid);
                    PrefSingleton.getInstance().putString("fake_ticket",wifipassedit.getText().toString());
                    PrefSingleton.getInstance().putInt("fake_ap_channel",apChannel);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } else {
            Toast.makeText(context1, "????????????????????????", Toast.LENGTH_SHORT).show();
            return;
        }

        final JSONObject jof = jo;
        System.out.println("20200911????????????<==>" + jo);

        //DeviceInfo.sendCommand(FakeAPActivity.this, deviceInfo, jo, command);
        DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(context1);
        devStatusDBUtils.open();
        devStatusDBUtils.preHandling(devId);
        devStatusDBUtils.close();

        BackgroundTask.clearAll();
        BackgroundTask.mTimerHandling = new Timer();
        BackgroundTask.mTimerTaskHandling = new TimerTask() {
            @Override
            public void run() {
                if (getActivity() == null){ //????????????????????????activity???????????????,getActivity()????????????????????????????????????
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new FakeAPUpdater(context1, devId, jof).execute();
                    }
                });
            }
        };
        BackgroundTask.mTimerHandling.schedule(BackgroundTask.mTimerTaskHandling, 0, 30000);
    }
}
