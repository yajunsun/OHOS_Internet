package zgan.ohos.Activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.transition.AutoTransition;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import zgan.ohos.MyApplication;
import zgan.ohos.R;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.services.login.ZganLoginService;
import zgan.ohos.utils.DataCacheHelper;

/**
 * Created by yajunsun on 2016/1/11.
 */
public abstract class myBaseActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;
    protected final static String TAG = "suntest";
    protected TextView txt_net_error;
    protected View ll_net_error;
    public BroadcastReceiver serviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//          if (intent.getAction().equals(ZganLoginService.ZGAN_SOCKETE_ERR)) {
            Bundle bundle = intent.getExtras();
            String msg = bundle.getString("msg");
            Log.v(TAG, "网络状态：" + msg);
            if (ll_net_error != null) {
                if (msg.equals("1")) {
                    ll_net_error.setVisibility(View.GONE);
                } else {
                    ll_net_error.setVisibility(View.VISIBLE);
                    txt_net_error.setText(msg);
                }
            }
            toCloseProgress();
        }
        // }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT > 20) {
            getWindow().setEnterTransition(new AutoTransition());
            getWindow().setExitTransition(new AutoTransition());
        }
        if (Integer.parseInt(Build.VERSION.SDK) > 14
                || Integer.parseInt(Build.VERSION.SDK) == 14) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads().detectDiskWrites().detectNetwork() // or
                    // .detectAll()
                    // for
                    // all
                    // detectable
                    // problems
                    .penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                    .penaltyLog().penaltyDeath().build());
        }
        txt_net_error = (TextView) findViewById(R.id.txt_net_error);
        ll_net_error = findViewById(R.id.lo_net_error);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);
        initView();

    }


    public void startActivityWithAnim(Intent intent) {
//        if (Build.VERSION.SDK_INT > 20)
//            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
//        else {
        startActivity(intent);
        overridePendingTransition(R.animator.enter, R.animator.exit);
        // }
    }

    public void startActivityWithAnimForResult(Intent intent, int requestCode) {
//        if (Build.VERSION.SDK_INT > 20)
//            startActivityForResult(intent, requestCode, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
//        else {
        startActivityForResult(intent, requestCode);
        overridePendingTransition(R.animator.enter, R.animator.exit);
        //}
    }

    protected abstract void initView();

    protected void addCache(String param, String data) {
        try {
            DataCacheHelper.add2DiskCache(param, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract void ViewClick(View v);

    protected void toShowProgress() {
        progressDialog.show();

    }

    protected void toSetProgressText(String strMsg) {

        progressDialog.setMessage(strMsg);
    }

    protected void toCloseProgress() {
        progressDialog.dismiss();

    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ZganLoginService.ZGAN_SOCKETE_ERR);
        intentFilter.addAction(ZganCommunityService.ZGAN_SOCKETE_ERR);
        registerReceiver(serviceReceiver, intentFilter);
        Log.v(TAG, "服务错误监听启动");
        if (!ZganLoginService.isNetworkAvailable(MyApplication.context)) {
            ZganLoginService.BroadError("网络连接错误");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(serviceReceiver);
        Log.v(TAG, "服务错误监听注销");
    }

    protected final static String P_FRONT = "1001";
    protected final static String P_ADVER = "1002";
    protected final static String P_CAKELIST = "1003";
    protected final static String P_CAKEDETAIL = "1004";
    protected final static String P_VIGTABLE ="1005";
    protected final static String P_HOMESPECAIL ="1006";
    protected final static String P_HIGHTSPECAIL ="1007";
    protected final static String P_LUANDRY ="1008";
    protected final static String P_SUPERMARKAT ="1009";
    protected final static String P_HOUSEHOLD ="1010";
    protected final static String P_CLEAN ="1011";
    // protected final static String       1012";
    protected final static String P_CAKEBOOK ="1013";
    protected final static String P_COMMITORDER ="1015";
    protected final static String P_ORDERLIST ="1016";
    //    protected final static String   P_ORDERDETAIL     1017";
//    protected final static String        1018";
//    protected final static String       1019";
//    protected final static String       1020";
    protected final static String P_ICECREAM ="1021";
    protected final static String P_INTEGERINFO ="1022";
    protected final static String P_INTEGERUSEINFO ="1023";
    protected final static String P_INTEGER ="1024";

    protected final static String P_FUNCPAGE ="100102";
    protected final static String P_GVNEWS ="2001";
    protected final static String P_ZONENEWS ="2002";
    protected final static String P_NEWDETAIL ="2003";
    protected final static String P_LEAVEMSGLIST ="2004";
    protected final static String P_LEAVEMSGDETAIL ="2005";
    protected final static String P_REPLYMSG ="2006";
    protected final static String P_LEAVEMSG ="2007";
    protected final static String P_EXPRESSOUT ="2008";
    protected final static String P_EXPRESSIN ="2009";
    protected final static String P_CALLOUT ="2010";

}
