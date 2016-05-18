package zgan.ohos.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.util.List;

import zgan.ohos.Activities.MainActivity;
import zgan.ohos.Models.BaseModel;

public class AppUtils {
    private AppUtils() {
            /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");

    }

    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.versionName;

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static int getVersionCode(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.versionCode;

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public static Point getWindowSize(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;     // 屏幕宽度（像素）
        int height = metric.heightPixels;   // 屏幕高度（像素）
        Point p = new Point(width, height);
        return p;
    }

    public static float getDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    public static <T extends BaseModel> List<T> parseXMLtoList(List<String> field, String xmlString) {

        return null;
    }

    private static Activity mainActivity;
    private static Activity loginActivity;

    public static void iniLoginActivity(Activity _login) {
        loginActivity = _login;
    }

    public static void iniMainActivity(Activity _main) {
        mainActivity = _main;
        if (loginActivity != null)
            loginActivity.finish();
    }

    public static void exits() {
        if (mainActivity != null)
            mainActivity.finish();
    }
}
