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

package com.vrem.wifianalyzer.wifi.filter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.vrem.wifianalyzer.MainActivity;
import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.navigation.NavigationMenu;
import com.vrem.wifianalyzer.wifi.accesspoint.AccessPointsFragment;

import static android.content.ContentValues.TAG;


public class Filter {

    private final AlertDialog alertDialog;

    private Filter(@NonNull AlertDialog alertDialog) {
        this.alertDialog = alertDialog;
    }

    public static Filter build() {
        return new Filter(buildAlertDialog());
    }

    private static AlertDialog buildAlertDialog() { //设置过滤器的五个参数
        if (MainContext.INSTANCE.getMainActivity().isFinishing()) {
            return null;
        }
        View view = MainContext.INSTANCE.getLayoutInflater().inflate(R.layout.filter_popup, null); //获取页面对象
        return new AlertDialog
            .Builder(view.getContext())
            .setView(view)
            .setTitle(R.string.filter_title)
            .setIcon(R.drawable.ic_filter_list_grey_500_48dp)
            .setNegativeButton(R.string.filter_reset, new Reset()) //重置
            .setNeutralButton(R.string.filter_close, new Close()) //关闭
            .setPositiveButton(R.string.filter_apply, new Apply()) //应用
            .create();
    }

    public void show() {
        if (alertDialog != null && !alertDialog.isShowing()) {
            alertDialog.show();
            addWiFiBandFilter(alertDialog);
            addSSIDFilter(alertDialog);
            addStrengthFilter(alertDialog);
            addSecurityFilter(alertDialog);
        }
    }

    AlertDialog getAlertDialog() {
        return alertDialog;
    }

    private void addSSIDFilter(@NonNull AlertDialog alertDialog) {
        new SSIDFilter(MainContext.INSTANCE.getFilterAdapter().getSSIDAdapter(), alertDialog);
    }

    private void addWiFiBandFilter(@NonNull AlertDialog alertDialog) {
        if (NavigationMenu.ACCESS_POINTS.equals(MainContext.INSTANCE.getMainActivity().getNavigationMenuView().getCurrentNavigationMenu())) {
            new WiFiBandFilter(MainContext.INSTANCE.getFilterAdapter().getWiFiBandAdapter(), alertDialog);
        }
    }


    private void addStrengthFilter(@NonNull AlertDialog alertDialog) {
        new StrengthFilter(MainContext.INSTANCE.getFilterAdapter().getStrengthAdapter(), alertDialog);
    }

    private void addSecurityFilter(@NonNull AlertDialog alertDialog) {
        new SecurityFilter(MainContext.INSTANCE.getFilterAdapter().getSecurityAdapter(), alertDialog);
    }

    //过滤器关闭事件
    private static class Close implements OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            MainContext.INSTANCE.getFilterAdapter().reload();
        }
    }

    //过滤器应用事件
    private static class Apply implements OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            MainContext mainContext = MainContext.INSTANCE;
            mainContext.getFilterAdapter().save();
            mainContext.getMainActivity().update();
        }
    }

    //过滤器重置事件
    private static class Reset implements OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            MainContext mainContext = MainContext.INSTANCE;
            mainContext.getFilterAdapter().reset();
            mainContext.getMainActivity().update();
        }
    }
}
