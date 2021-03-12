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

package com.vrem.wifianalyzer;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.leakcanary.LeakCanary;
import com.vrem.util.ConfigurationUtils;
import com.vrem.util.EnumUtils;
import com.vrem.wifianalyzer.menu.OptionMenu;
import com.vrem.wifianalyzer.navigation.NavigationMenu;
import com.vrem.wifianalyzer.navigation.NavigationMenuView;
import com.vrem.wifianalyzer.settings.Repository;
import com.vrem.wifianalyzer.settings.SettingActivity;
import com.vrem.wifianalyzer.settings.Settings;
import com.vrem.wifianalyzer.wifi.accesspoint.AccessPointsFragment;
import com.vrem.wifianalyzer.wifi.accesspoint.ConnectionView;
import com.vrem.wifianalyzer.wifi.band.WiFiBand;
import com.vrem.wifianalyzer.wifi.band.WiFiChannel;
import com.vrem.wifianalyzer.wifi.common.FrequencyTransformTools;
import com.vrem.wifianalyzer.wifi.common.InfoUpdater;
import com.vrem.wifianalyzer.wifi.common.MacSsidDBUtils;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;
import com.vrem.wifianalyzer.wifi.deviceList.Deviece;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;

public class MainActivity extends AppCompatActivity implements OnSharedPreferenceChangeListener, OnNavigationItemSelectedListener {
    private MainReload mainReload;
    private NavigationMenuView navigationMenuView;
    private NavigationMenu startNavigationMenu;
    private OptionMenu optionMenu;
    private String currentCountryCode;

    public static final int REQUEST_CAMERA_PERMISSION = 1003;

    private static String TAG = "MainActivity";

//    private Timer mTimer = new Timer();//定时任务

    //附加基础上下文背景
    @Override
    protected void attachBaseContext(Context newBase) {
        Locale newLocale = new Settings(new Repository(newBase)).getLanguageLocale();//获取语言
        Context context = ConfigurationUtils.createContext(newBase, newLocale);//获取系统配置信息
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity status", "Create");

        MainContext mainContext = MainContext.INSTANCE;
        mainContext.initialize(this, isLargeScreen());//调用mainContext 初始化数据

        Settings settings = mainContext.getSettings();//获取设置信息
        settings.initializeDefaultValues();//获取编好设置

        setTheme(settings.getThemeStyle().themeAppCompatStyle());//设置主题风格
        setWiFiChannelPairs(mainContext);//设置WiFi信道组

        mainReload = new MainReload(settings);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        //20210309
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(getApplication()); // 内存泄漏检测

        PrefSingleton.getInstance().Initialize(getApplicationContext());//初始化参数
        if (!PrefSingleton.getInstance().getString("target_search").equals("")) {
            PrefSingleton.getInstance().putString("target_search", "");
        }
        boolean isWIFI = MainContext.INSTANCE.getScannerService().isWifiStatus();
        if (isWIFI) {
            PrefSingleton.getInstance().putString("url", "http://192.168.100.1:9494");
//            PrefSingleton.getInstance().putString("url", "http://39.100.2.75:9494");
            if (PrefSingleton.getInstance().getInt("id") < 0) {
                PrefSingleton.getInstance().putInt("id", 0);
            }
            new InfoUpdater(this, true).execute(); //获取前置信息
        }
        FrequencyTransformTools.getInstance().Initialize(); //初始化对应信道的频率，用于测算wifi距离
//        new WIFIHotspotFragment(mainHandler);//传递mainHandler 给WIFIHotspotFragment，用于更新UI

        settings.registerOnSharedPreferenceChangeListener(this);

        setOptionMenu(new OptionMenu()); //设置菜单 cc 81 da e4 c1 38

        Toolbar toolbar = findViewById(R.id.toolbar); //获取工具栏控件
        toolbar.setOnClickListener(new WiFiBandToggle()); //设置点击事件：切换WiFi频道
        setSupportActionBar(toolbar); //设置操作栏

        DrawerLayout drawer = findViewById(R.id.drawer_layout); //抽屉式布局
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(  //操作栏抽屉式切换
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        startNavigationMenu = settings.getStartMenu();
        navigationMenuView = new NavigationMenuView(this, startNavigationMenu);
        onNavigationItemSelected(navigationMenuView.getCurrentMenuItem());

        ConnectionView connectionView = new ConnectionView(this); // 获取连接视图对象
        mainContext.getScannerService().register(connectionView);

        copyassets();
        if (Build.VERSION.SDK_INT >= 26) { // Android 8.0
            Permission();
        }
        initWithGetPermission(this);
        Permission_write();

    }

    public void Permission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CAMERA_PERMISSION);
            return;
        } else {
            // resume
        }
    }

    //设置WiFi信道组
    private void setWiFiChannelPairs(MainContext mainContext) {
        Settings settings = mainContext.getSettings();//获取设置信息
        String countryCode = settings.getCountryCode();//获取国家代码
        if (!countryCode.equals(currentCountryCode)) {//当前国家代码不为空
            Pair<WiFiChannel, WiFiChannel> pair = WiFiBand.GHZ5.getWiFiChannels().getWiFiChannelPairFirst(countryCode);//设置第一个WiFi信道组
            mainContext.getConfiguration().setWiFiChannelPair(pair);//将WiFi信道组传给mainContext
            currentCountryCode = countryCode;//将国家编码传递给当前国家编码currentCountryCode
        }
    }

    private boolean isLargeScreen() {
        Configuration configuration = getResources().getConfiguration();//声明Configuration对象，用于获取设备信息
        int screenLayoutSize = configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        return screenLayoutSize == Configuration.SCREENLAYOUT_SIZE_LARGE ||
            screenLayoutSize == Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    //共享偏好改变
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        MainContext mainContext = MainContext.INSTANCE;
        if (mainReload.shouldReload(mainContext.getSettings())) {
            reloadActivity();
        } else {
            setWiFiChannelPairs(mainContext);
            update();
        }
    }

    public void update() {
        ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> info = activityManager.getRunningTasks(1);
        if (info != null && info.size()>0){
            ComponentName componentName = info.get(0).topActivity;
            String className = componentName.getClassName();
            Log.d("className:" , className);
            if ("com.vrem.wifianalyzer.SnifferActivity".equals(className)){ // 判断SnifferActivity是否处于打开状态
                Log.w("SnifferActivity:","处于打开状态，不执行更新");
            } else if ("com.vrem.wifianalyzer.FakeAPActivity".equals(className)){
                Log.w("FakeAPActivity:","处于打开状态，不执行更新");
            } else if ("com.vrem.wifianalyzer.MainActivity".equals(className)){
                MainContext.INSTANCE.getScannerService().update();
                updateActionBar();
            }
        } else {
            MainContext.INSTANCE.getScannerService().update();
            updateActionBar();
            Log.w("系统:","执行更新");
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    //重新加载页面
    public void reloadActivity() {
        finish();
//        Intent intent = new Intent(this, MainActivity.class);
        Intent intent = new Intent(this, SettingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP |
            Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        Intent intent0 = new Intent(this, MainActivity.class);
        startActivity(intent0);
    }

    @Override
    public void onBackPressed() {
        if (!closeDrawer()) {
            if (startNavigationMenu.equals(navigationMenuView.getCurrentNavigationMenu())) {
                super.onBackPressed();
            } else {
                navigationMenuView.setCurrentNavigationMenu(startNavigationMenu);
                onNavigationItemSelected(navigationMenuView.getCurrentMenuItem());
            }
        }
    }

    //导航条动作选择
    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        try {
            closeDrawer();//关闭正在打开的页面，为新动作做准备
            NavigationMenu navigationMenu = EnumUtils.find(NavigationMenu.class, menuItem.getItemId(), NavigationMenu.ACCESS_POINTS);
            navigationMenu.activateNavigationMenu(this, menuItem);
        } catch (Exception e) {
            reloadActivity();
        }
        return true;
    }

    //关闭当前页面
    private boolean closeDrawer() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        return false;
    }

    @Override
    public void recreate() {
        MainContext mainContext = MainContext.INSTANCE;
        mainContext.initialize(this, isLargeScreen());//调用mainContext 初始化数据
        ConnectionView connectionView = new ConnectionView(this);//获取连接视图对象
        mainContext.getScannerService().register(connectionView);
        Log.d("MainActivity status","recreate");
    }


    @Override
    protected void onPause() {
        optionMenu.pause();
        MainContext.INSTANCE.getScannerService().pause(); //暂停扫描
        updateActionBar();
        Log.d("MainActivity status","Pause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d("MainActivity status","Resume");
        super.onResume();
        MainContext.INSTANCE.getScannerService().update();
        optionMenu.resume();
        updateActionBar();
    }

    @Override
    protected void onStop() {
        Log.d("MainActivity status","Stop");
        //MainContext.INSTANCE.getScannerService().setWiFiOnExit();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        optionMenu.create(this, menu);
        updateActionBar();
        return true;
    }

    //获取操作栏上的动作选项
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        optionMenu.select(item);
        updateActionBar();//更新操作栏
        return true;
    }

    //更新操作栏
    public void updateActionBar() {
        navigationMenuView.getCurrentNavigationMenu().activateOptions(this);
    }

    //导航菜单视图 每5秒刷新
    public NavigationMenuView getNavigationMenuView() {
        return navigationMenuView;
    }

    public OptionMenu getOptionMenu() {
        return optionMenu;
    }

    void setOptionMenu(OptionMenu optionMenu) {
        this.optionMenu = optionMenu;
    }

    //WIFI频道切换
    private class WiFiBandToggle implements OnClickListener {
        @Override
        public void onClick(View view) {
            if (navigationMenuView.getCurrentNavigationMenu().isWiFiBandSwitchable()) {
                MainContext.INSTANCE.getSettings().toggleWiFiBand();
            }
        }
    }

    public void copyassets(){
        copyFile(this,"oui.csv");
    }
    public static void copyFile(Activity activity, String filePath){
        try {
            String[] fileList = activity.getAssets().list(filePath);
            if(fileList.length>0) {//如果是目录
                File file = new File(activity.getFilesDir().getAbsolutePath()+ File.separator + filePath);
                if (!file.exists()){
                    file.mkdirs();//如果文件夹不存在，则递归
                    for (String fileName:fileList){
                        filePath = filePath + File.separator + fileName;
                        copyFile2(activity,filePath);
                        filePath = filePath.substring(0,filePath.lastIndexOf(File.separator));
                    }
                } else {
                    return;
                }
            } else {//如果是文件
                InputStream inputStream=activity.getAssets().open(filePath);
                File file = new File(activity.getFilesDir().getAbsolutePath()+ File.separator+filePath);
                if(!file.exists() || file.length() == 0) {
                    FileOutputStream fos = new FileOutputStream(file);
                    int len = -1;
                    byte[] buffer = new byte[1024];
                    while ((len = inputStream.read(buffer)) != -1){
                        fos.write(buffer,0,len);
                    }
                    fos.flush();
                    inputStream.close();
                    fos.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void copyFile2(Activity activity, String fileName){
        try {
            InputStream inputStream = activity.getAssets().open(fileName);
            File file = new File(activity.getFilesDir().getAbsolutePath() + File.separator + fileName);
            if(!file.exists() || file.length() == 0) {
                FileOutputStream fos = new FileOutputStream(file); // 如果文件不存在，FileOutputStream会自动创建文件
                int len = -1;
                byte[] buffer = new byte[1024];
                while ((len = inputStream.read(buffer)) != -1){
                    fos.write(buffer,0,len);
                }                fos.flush();//刷新缓存区
                inputStream.close();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
    * 修改系统设置权限,开启热点需要"WRITE_SETTINGS"权限
    */
    private static final int REQUEST_CODE_WRITE_SETTINGS = 2;
    public void initWithGetPermission(Activity context) {
        boolean permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission = android.provider.Settings.System.canWrite(context);
        } else {
            permission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
        }
        if (permission) {
            return;
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS);
            } else {
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_SETTINGS}, REQUEST_CODE_WRITE_SETTINGS);
            }
        }
    }

    // 动态权限申请
    public void Permission_write() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CAMERA_PERMISSION);
            return;
        } else {
            // resume
        }
    }


}
