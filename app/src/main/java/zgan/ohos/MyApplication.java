package zgan.ohos;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.facebook.device.yearclass.YearClass;

import zgan.ohos.services.crash.CrashHandler;
import zgan.ohos.utils.ImageLoader;


/**
 * Created by yajunsun on 2015/11/12.
 */
public class MyApplication extends Application {

    public static String phone = "75B31FF74287E76F37E3F7817B123AEC";
    public static Context context;
    public static RequestQueue requestQueue;
    public static final String SIGN_APP = "";
    public static int PhoneYear = 0;

/*    Year	Cores	Clock	RAM
    2008	1	528MHz	192MB
    2009	n/a	600MHz	290MB
    2010	n/a	1.0GHz	512MB
    2011	2	1.2GHz	1GB
    2012	4	1.5GHz	1.5GB
    2013	n/a	2.0GHz	2GB
    2014	n/a	>2GHz	>2GB*/

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        PhoneYear = YearClass.get(context);
//        if (isOwnAPP()) {
        requestQueue = Volley.newRequestQueue(this);
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
//        }
//        else
//        {
//            android.os.Process.killProcess(android.os.Process.myPid());
//            System.exit(1);
//        }
    }

    private String getSignature() {
        StringBuilder thisSign = new StringBuilder();
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            android.content.pm.Signature[] signatures = packageInfo.signatures;
            for (Signature sign : signatures) {
                thisSign.append(sign.toCharsString());
            }
            return thisSign.toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    private boolean isOwnAPP() {
        String signStr = ImageLoader.hashKeyFromUrl(getSignature());
        return SIGN_APP.equals(signStr);
    }
}
