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

package com.vrem.wifianalyzer.wifi.scanner;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.vrem.wifianalyzer.settings.Settings;
import com.vrem.wifianalyzer.wifi.common.APInfoUpdater;
import com.vrem.wifianalyzer.wifi.common.AsyncResponse;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;
import com.vrem.wifianalyzer.wifi.model.WiFiData;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;

import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.IterableUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Scanner implements ScannerService {
    private final List<UpdateNotifier> updateNotifiers;
    private final WifiManager wifiManager;
    private final Settings settings;
    private Transformer transformer;
    private WiFiData wiFiData; //存放wifi数据
    private Cache cache;//缓存
    private PeriodicScan periodicScan; //定期扫描

    private Context context;

    private int count = 0;

    private List<WiFiDetail> wiFiDetails = new ArrayList<>();

    //获取wifi服务 3
    Scanner(@NonNull WifiManager wifiManager, @NonNull Handler handler, @NonNull Settings settings, @NonNull Context context) {
        this.updateNotifiers = new ArrayList<>();
        this.wifiManager = wifiManager;
        this.settings = settings;
        this.wiFiData = WiFiData.EMPTY;
        this.setTransformer(new Transformer());
        this.setCache(new Cache());
        this.periodicScan = new PeriodicScan(this, handler, settings);

        this.context = context;
    }

    //更新，重新扫描 7
    @Override
    public void update() {
        performWiFiScan();
        IterableUtils.forEach(updateNotifiers, new UpdateClosure());
    }

    @Override
    public WiFiData getWiFiData() {
        return wiFiData;
    }

/*    @Override
    public WiFiData setWiFiData(WiFiData wiFiData1) {
        wiFiData = wiFiData1;
        IterableUtils.forEach(updateNotifiers, new UpdateClosure());
        return wiFiData;
    }*/

    //4
    @Override
    public void register(@NonNull UpdateNotifier updateNotifier) {
        updateNotifiers.add(updateNotifier);
    }

    @Override
    public void unregister(@NonNull UpdateNotifier updateNotifier) {
        updateNotifiers.remove(updateNotifier);
    }

    @Override
    public void pause() {
        periodicScan.stop();
    }

    @Override
    public boolean isRunning() {
        return periodicScan.isRunning();
    }

    //4
    @Override
    public void resume() {
        periodicScan.start();
    }

    /*
     * 获取热点状态
     * */
    @Override
    public boolean isWifiApStatus() {
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("getWifiApState"); //通过反射获取 getWifiApState()方法
            int status = (int) method.invoke(wifiManager); //调用getWifiApState() ，获取返回值
            Field field = wifiManager.getClass().getDeclaredField("WIFI_AP_STATE_ENABLED"); //通过反射获取 WIFI_AP的开启状态属性
            int value = (int) field.get(wifiManager); //获取属性值
            if (status == value){ //判断是否开启
                return true;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return false;
    }

    /*获取wifi状态*/
    @Override
    public boolean isWifiStatus() {
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("getWifiState"); //通过反射获取 getWifiApState()方法
            int status = (int) method.invoke(wifiManager); //调用getWifiApState() ，获取返回值
            Field field = wifiManager.getClass().getDeclaredField("WIFI_STATE_ENABLED"); //通过反射获取 WIFI_AP的开启状态属性
            int value = (int) field.get(wifiManager); //获取属性值
            if (status == value){ //判断是否开启
                return true;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void setWiFiOnExit() {
        if (settings.isWiFiOffOnExit()) {
            try {
                wifiManager.setWifiEnabled(false);
            } catch (Exception e) { // critical error: do not die
                e.printStackTrace();
            }
        }
    }

    PeriodicScan getPeriodicScan() {
        return periodicScan;
    }

    void setPeriodicScan(@NonNull PeriodicScan periodicScan) {
        this.periodicScan = periodicScan;
    }

    //2
    void setCache(@NonNull Cache cache) {
        this.cache = cache;
    }

    //1
    void setTransformer(@NonNull Transformer transformer) {
        this.transformer = transformer;
    }

    List<UpdateNotifier> getUpdateNotifiers() {
        return updateNotifiers;
    }

    //执行扫描wifi 5
    private void performWiFiScan() {
        count = count +1;
        if (count == 3){ //控制获取数据的速度
            count = 0;
        }
        List<ScanResult> scanResults = Collections.emptyList(); //用于存放所有的扫描结果
        WifiInfo wifiInfo = null; //用于存放当前连接的wifi动态信息
        List<WifiConfiguration> configuredNetworks = null; //用于存放手机中曾经连接过得wifi列表
        try {
            if (!wifiManager.isWifiEnabled()) { //判断wifi是否开启
                wifiManager.setWifiEnabled(true); //打开wifi
            }
            if (wifiManager.startScan()) { //是否已扫描 扫描周围无线网络
                scanResults = wifiManager.getScanResults(); //获取所有扫描结果，存入List<ScanResult>
            }
            wifiInfo = wifiManager.getConnectionInfo(); //获取当前连接wifi的动态信息
            configuredNetworks = wifiManager.getConfiguredNetworks(); //返回手机列表中曾经连接成功的wifi
        } catch (Exception e) { // critical error: set to no results and do not die
            e.printStackTrace();
        }
        final String devId = PrefSingleton.getInstance().getString("device"); //获取设备ID

        final WifiDetailImpl wifiDetaiOne = new WifiDetailImpl(); //一层缓存
        final WifiDetailImpl wifiDetaiTwo = new WifiDetailImpl(); //二层缓存
        List<WiFiDetail> cacheWifiDetailOne = wifiDetaiOne.getCache("one");
        if (count == 2){
            if (cacheWifiDetailOne != null){
                wifiDetaiOne.clearCache("one");
                Log.d("1层缓存：","已释放");
            }
            APInfoUpdater apInfoUpdater = (APInfoUpdater) new APInfoUpdater(context, devId, 0, 1, 0).execute();
            apInfoUpdater.setOnAsyncResponse(new AsyncResponse() {
                @Override
                public void onDataReceivedSuccess(List<WiFiDetail> listData) {
                    wiFiDetails = listData;
                    wifiDetaiOne.putCache("one",wiFiDetails);
                    Log.d("1层缓存：","释放后已加入");
                    if (wifiDetaiTwo.isContains("two")){
                        wifiDetaiTwo.clearCache("two");
                        Log.d("2层缓存：","已释放");
                        wifiDetaiTwo.putCache("two",wifiDetaiOne.getCache("one"));
                        Log.d("2层缓存：","已加入");
                    }
                }
            });
        }else if (cacheWifiDetailOne != null){
            if (wifiDetaiTwo.isContains("two")){
                wifiDetaiTwo.clearCache("two");
                Log.d("2层缓存：","已释放");
                wifiDetaiTwo.putCache("two",wifiDetaiOne.getCache("one"));
                Log.d("2层缓存：","已加入");
            }
            wiFiData = transformer.transformToWiFiData(cacheWifiDetailOne, wifiInfo, configuredNetworks);
            Log.d("使用：","1层缓存数据");
        } else if (cacheWifiDetailOne == null && wifiDetaiTwo.getCache("two") != null){
            wiFiData = transformer.transformToWiFiData(wifiDetaiTwo.getCache("two"), wifiInfo, configuredNetworks);
            Log.d("使用：","2层缓存数据");
        } else {
            APInfoUpdater apInfoUpdater = (APInfoUpdater) new APInfoUpdater(context, devId, 0, 1, 0).execute();
            apInfoUpdater.setOnAsyncResponse(new AsyncResponse() {
                @Override
                public void onDataReceivedSuccess(List<WiFiDetail> listData) {
                    wiFiDetails = listData;
                    wifiDetaiOne.putCache("one",wiFiDetails);
                    Log.d("1层缓存：","已加入");
                    wifiDetaiTwo.putCache("two",wifiDetaiOne.getCache("one"));
                    Log.d("2层缓存：","已加入");
                }
            });
        }

//        cache.add(scanResults);//将扫描的所有结果加入缓存中，用于转换类型
//        //将扫描周围无线网络的结果转换成wifiData类型、当前连接信息、以前连接成功的wifi传给wiFiData
//        if (wiFiDetails != null){
//            wiFiData = transformer.transformToWiFiData(wiFiDetails, wifiInfo, configuredNetworks);
//        }else {
//            wiFiData = transformer.transformToWiFiData(cache.getScanResults(), wifiInfo, configuredNetworks,getwifidata);
//        }
    }

    private class UpdateClosure implements Closure<UpdateNotifier> {
        @Override
        public void execute(UpdateNotifier updateNotifier) {
            updateNotifier.update(wiFiData);
        }
    }
}
