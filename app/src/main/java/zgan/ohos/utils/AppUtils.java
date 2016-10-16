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

    public static boolean NEED_REFRESH_ORDER=false;

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
    public final static String P_FRONT = "1001";//专题内容（旧）
    public final static String P_ADVER = "3002";//"1002";//广告
    public final static String P_CAKELIST = "1003";
    public final static String P_CAKEDETAIL = "1004";
    public final static String P_VIGTABLE ="1005";
    public final static String P_HOMESPECAIL ="1006";
    public final static String P_HIGHTSPECAIL ="1007";
    public final static String P_LUANDRY ="1008";
    public final static String P_SUPERMARKAT ="1009";
    public final static String P_HOUSEHOLD ="1010";
    public final static String P_CLEAN ="1011";
    // public final static String       1012";
    public final static String P_CAKEBOOK ="1013";
    public final static String P_COMMITORDER ="1015";
    public final static String P_ORDERLIST ="1016";
    //    public final static String   P_ORDERDETAIL     1017";
//    public final static String        1018";
//    public final static String       1019";
    public final static String P_USERINFO=      "1020";//用户信息（地址、积分等）
    public final static String P_ICECREAM ="1021";
    public final static String P_INTEGERINFO ="1022";
    public final static String P_INTEGERUSEINFO ="1023";
    public final static String P_INTEGER ="1024";
    //功能区
    public final static String P_FUNCPAGE ="300102";//"100102";
    public final static String P_GVNEWS ="2001";
    public final static String P_ZONENEWS ="2002";
    public final static String P_NEWDETAIL ="2003";
    public final static String P_LEAVEMSGLIST ="2004";
    public final static String P_LEAVEMSGDETAIL ="2005";
    public final static String P_REPLYMSG ="2006";
    public final static String P_LEAVEMSG ="2007";
    public final static String P_EXPRESSOUT ="2008";
    public final static String P_EXPRESSIN ="2009";
    public final static String P_CALLOUT ="2010";

    public  final static String P_FRONTITMES1="100103";
    public final static String P_FRONTITMES2="300104";//"100104";
}
