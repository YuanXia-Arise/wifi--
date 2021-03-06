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

package com.vrem.wifianalyzer.menu;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.common.InfoUpdater;
import com.vrem.wifianalyzer.wifi.common.MacSsidDBUtils;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;
import com.vrem.wifianalyzer.wifi.deviceList.Deviece;
import com.vrem.wifianalyzer.wifi.filter.Filter;

import org.json.JSONException;


public class OptionMenu {
    private Menu menu;
    private Activity activity;

    public void create(@NonNull Activity activity, Menu menu) {
        activity.getMenuInflater().inflate(R.menu.optionmenu, menu);
        this.menu = menu;
        this.activity = activity;
    }

    //菜单动作
    public void select(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_DbToCsv:
                MacSsidDBUtils macSsidDBUtils = new MacSsidDBUtils(activity);
                macSsidDBUtils.open();
                macSsidDBUtils.ExportToCSV("Wifi_Analyzer.csv");
                macSsidDBUtils.close();
                Toast.makeText(activity, "已导出数据库至本地", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_scanner:
                MenuItem menuItem = menu.findItem(R.id.action_scanner);
                if (MainContext.INSTANCE.getScannerService().isRunning()) {
                    pause();
                } else {
                    resume();
                }
                if (MainContext.INSTANCE.getScannerService().isRunning()){
                    menuItem.setIcon(R.drawable.ic_pause_grey_500_48dp);
                } else {
                    menuItem.setIcon(R.drawable.ic_play_arrow_grey_500_48dp);
                }
                break;
            case R.id.action_filter:
                Filter.build().show();//打开过滤器
                break;
            case R.id.action_device:
                new InfoUpdater(MainContext.INSTANCE.getContext(),true).execute(); // 获取前置信息
                String deviceInfo = PrefSingleton.getInstance().getString("deviceInfo");
                if (deviceInfo.equals(null)){
                    MenuInflater inflater = MainContext.INSTANCE.getOptionMenu();
                    inflater.inflate(R.menu.optionmenu,menu);
                    menu.findItem(R.id.action_device).setVisible(false);
                } else {
                    menu.findItem(R.id.action_device).setVisible(true);
                    Deviece.build().show(); //显示设备列表
                }
            default:
                // do nothing
                break;
        }
    }

    public void pause() {
        MainContext.INSTANCE.getScannerService().pause();
    }

    public void resume() {
        MainContext.INSTANCE.getScannerService().resume();
    }

    public Menu getMenu() {
        return menu;
    }

}
