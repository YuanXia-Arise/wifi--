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

import android.app.Fragment;
import android.net.wifi.WifiInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.vrem.wifianalyzer.MainActivity;
import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.model.WiFiConnection;
import com.vrem.wifianalyzer.wifi.model.WiFiData;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;
import com.vrem.wifianalyzer.wifi.scanner.UpdateNotifier;

import java.util.Locale;

//当前连接wifi的基本信息类
public class ConnectionView implements UpdateNotifier {
    private final MainActivity mainActivity;
    private AccessPointDetail accessPointDetail;
    private AccessPointPopup accessPointPopup;

    public ConnectionView(@NonNull MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        setAccessPointDetail(new AccessPointDetail());
        setAccessPointPopup(new AccessPointPopup());
    }

    @Override
    public void update(@NonNull WiFiData wiFiData) {
        ConnectionViewType connectionViewType = MainContext.INSTANCE.getSettings().getConnectionViewType();
        setConnectionVisibility(wiFiData, connectionViewType);
        setNoDataVisibility(wiFiData);
    }

    void setAccessPointDetail(@NonNull AccessPointDetail accessPointDetail) {
        this.accessPointDetail = accessPointDetail;
    }

    void setAccessPointPopup(@NonNull AccessPointPopup accessPointPopup) {
        this.accessPointPopup = accessPointPopup;
    }

    //设置无数据可见性  wiFiData获取手机上保存的WiFi信息
    private void setNoDataVisibility(@NonNull WiFiData wiFiData) {
        mainActivity.findViewById(R.id.nodata).setVisibility(isDataAvailable(wiFiData) ? View.GONE : View.VISIBLE);
    }

    //可用数据
    private boolean isDataAvailable(@NonNull WiFiData wiFiData) {
        return !mainActivity.getNavigationMenuView().getCurrentNavigationMenu().isRegistered() || !wiFiData.getWiFiDetails().isEmpty();
    }

    private void setConnectionVisibility(@NonNull WiFiData wiFiData, @NonNull ConnectionViewType connectionViewType) {
        WiFiDetail connection = wiFiData.getConnection();
        View connectionView = mainActivity.findViewById(R.id.connection);

        WiFiConnection wiFiConnection = connection.getWiFiAdditional().getWiFiConnection();

        //String str = mainActivity.getNavigationMenuView().getCurrentNavigationMenu().name(); // 当前Fragment
        if (connectionViewType.isHide() || !wiFiConnection.isConnected()) {
            connectionView.setVisibility(View.GONE);
        } else {
            connectionView.setVisibility(View.VISIBLE);
            ViewGroup parent = connectionView.findViewById(R.id.connectionDetail);
            View view = accessPointDetail.makeView(parent.getChildAt(0), parent, connection, false, connectionViewType.getAccessPointViewType());
            if (parent.getChildCount() == 0) {
                parent.addView(view);
            }
            setViewConnection(connectionView, wiFiConnection); //65Mbps 192.168.100.98
            attachPopup(view, connection);
        }
    }

    private void setViewConnection(View connectionView, WiFiConnection wiFiConnection) {
        String ipAddress = wiFiConnection.getIpAddress();
        ((TextView) connectionView.findViewById(R.id.ipAddress)).setText(ipAddress);

        TextView textLinkSpeed = connectionView.findViewById(R.id.linkSpeed);
        int linkSpeed = wiFiConnection.getLinkSpeed();
        if (linkSpeed == WiFiConnection.LINK_SPEED_INVALID) {
            textLinkSpeed.setVisibility(View.GONE);
        } else {
            textLinkSpeed.setVisibility(View.VISIBLE);
            textLinkSpeed.setText(String.format(Locale.ENGLISH, "%d%s", linkSpeed, WifiInfo.LINK_SPEED_UNITS));
        }
        connectionView.findViewById(R.id.ipAddress).setVisibility(View.GONE);
        connectionView.findViewById(R.id.linkSpeed).setVisibility(View.GONE);
    }

    private void attachPopup(@NonNull View view, @NonNull WiFiDetail wiFiDetail) {
        View popupView = view.findViewById(R.id.attachPopup);
        if (popupView != null) {
            accessPointPopup.attach(popupView, wiFiDetail);
            accessPointPopup.attach(view.findViewById(R.id.ssid), wiFiDetail);
        }
    }

}
