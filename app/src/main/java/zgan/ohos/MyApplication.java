package zgan.ohos;

import android.app.Application;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import zgan.ohos.services.crash.CrashHandler;


/**
 * Created by yajunsun on 2015/11/12.
 */
public class MyApplication extends Application {

    public static String phone = "";
    public static Context context;
    public static RequestQueue requestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        requestQueue = Volley.newRequestQueue(this);
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
    }
}
