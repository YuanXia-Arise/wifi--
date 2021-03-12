package com.vrem.wifianalyzer;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.res.Configuration;
import android.util.DisplayMetrics;

public class DisplayUtil {

    /**
     * 屏幕适配 修改设备密度
     */

    private static float appDensity; // 屏幕密度
    private static float appScaledDensity; // 表示字体缩放比例,默认与appDensity相等

    public static void setDensity(Activity activity, final Application application) {
        // 获取当前屏幕的显示信息
        DisplayMetrics displayMetrics = application.getResources().getDisplayMetrics();
        if (appDensity == 0) {
            // 初始化赋值
            appDensity = displayMetrics.density;
            appScaledDensity = displayMetrics.scaledDensity;

            // 字体变化监听
            application.registerComponentCallbacks(new ComponentCallbacks() {
                @Override
                public void onConfigurationChanged(Configuration newConfig) {
                    if (newConfig != null && newConfig.fontScale > 0) { // 字体大小改变
                        appScaledDensity = application.getResources().getDisplayMetrics().scaledDensity;  // 赋值appScaledDensity
                    }
                }

                @Override
                public void onLowMemory() { // 监听系统整体内存 系统内存不足时调用

                }
            });
        }

        // 计算目标值density、scaledDensity、densityDpi
        float targetDensity = displayMetrics.widthPixels / (float) 420;
        float targetScaledDensity = targetDensity * (appDensity / appScaledDensity);
        int targetDensityDpi = (int) (targetDensity * 160);

        // 修改activity的density、scaledDensity、densityDpi
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        dm.density = targetDensity;
        dm.scaledDensity = targetScaledDensity;
        dm.densityDpi = targetDensityDpi;
    }

}
