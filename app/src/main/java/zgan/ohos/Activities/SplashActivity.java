package zgan.ohos.Activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.Calendar;

import zgan.ohos.Contracts.IImageloader;
import zgan.ohos.GuideIndexActivity;
import zgan.ohos.MyApplication;
import zgan.ohos.R;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.services.login.ZganLoginService;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.NetUtils;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;
import zgan.ohos.utils.generalhelper;

public class SplashActivity extends myBaseActivity {


    Calendar etime;
    Calendar btime;
    private Thread t1;
    ImageView iv_logo;
    ImageLoader imageLoader;

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_splash2);
        Log.i(TAG, "start services");
        ZganLoginService.setContext(SplashActivity.this);
        ZganCommunityService.setContext(MyApplication.context);

        Intent intent = new Intent(SplashActivity.this, ZganCommunityService.class);
        startService(intent);
        intent = new Intent(SplashActivity.this, ZganLoginService.class);
        startService(intent);
        iv_logo=(ImageView)findViewById(R.id.iv_logo);
        int maxwidth = AppUtils.getWindowSize(this).x;
        int maxheight = 5 * maxwidth;
        iv_logo.setMaxWidth(maxwidth);
        iv_logo.setMaxHeight(maxheight);
        imageLoader=new ImageLoader();
        imageLoader.loadDrawableRS(this, R.drawable.splashlauncher, iv_logo, new IImageloader() {
            @Override
            public void onDownloadSucc(Bitmap bitmap, String c_url, View imageView, int w, int h) {
                ((ImageView)imageView).setImageBitmap(bitmap);
            }
        },750,1334);
        Log.i(TAG,"services is running");
        new Thread(new Runnable() {
            @Override
            public void run() {
                btime = Calendar.getInstance();
                Looper.prepare();
                try {
                    // 检查网络是否正常
                    if (!ZganLoginService.isNetworkAvailable(MyApplication.context)) {
                        handler.sendEmptyMessage(0);
                    } else {
//                        // 判断自动登录
//                        try {
//                            while (!ZganLoginService.ServiceRin) {
//                                Thread.currentThread().sleep(100);
//                            }
//                            if (!ZganLoginService.toAutoUserLogin(handler)) {
//                                Thread_TimerToActivity tt = new Thread_TimerToActivity();
//                                t1 = new Thread(tt);
//                                t1.start();
//                            }
//                        } catch (Exception ex) {
//                            generalhelper.ToastShow(SplashActivity.this, ex.getMessage());
//                        }
//                    }
                        etime = Calendar.getInstance();
                        long costtime = etime.getTimeInMillis() - btime.getTimeInMillis();
                        long time = 2000;
                        if (costtime < time) {
                            try {
                                Thread.sleep(time - costtime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        handler.sendEmptyMessage(2);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void ViewClick(View v) {

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {//error
                Intent intent = new Intent(SplashActivity.this, Login.class);
                startActivityWithAnim(intent);
                generalhelper.ToastShow(SplashActivity.this, "网络未连接");
                finish();
            } else if (msg.what == 1) {//autologin
                Frame frame = (Frame) msg.obj;
                String result = generalhelper.getSocketeStringResult(frame.strData);
                String[] results = result.split(",");
                if (frame.subCmd == 1 && results[0].equals("0")) {
                    SystemUtils.setIsLogin(true);
                    ZganLoginService.toGetServerData(3, 0, PreferenceUtil.getUserName(), handler);
                    Log.i("suntest", "自动登录成功");
                } else if (frame.subCmd == 3) {
                    String communityIP = PreferenceUtil.getCommunityIP();
                    int communityPort = PreferenceUtil.getCommunityPORT();
                    if (results.length == 3 && results[0].equals("0")) {
                        Log.i("TAG", "ZganLoginService小区ID：" + results[1]);
                        //String[] ipport = results[1].split(":");
                        if (!communityIP.equals(NetUtils.getIp(results[1])) || communityPort != Integer.parseInt(results[2])) {
                            PreferenceUtil.setCommunityIP(NetUtils.getIp(results[1]));
                            PreferenceUtil.setCommunityPORT(Integer.parseInt(results[2]));
                            communityIP = NetUtils.getIp(results[1]);
                            communityPort = Integer.parseInt(results[2]);
                        }
                        ZganCommunityService.CommunityIp = communityIP;
                        ZganCommunityService.CommunityPort = communityPort;
                        ZganCommunityService.toAutoUserLogin(communityHandler);
                    }
                }
            } else if (msg.what == 2)//main
            {
                int usedTimes = PreferenceUtil.getUsedTimes();
                int lastVersion = PreferenceUtil.getLastVersion();
                String userName = PreferenceUtil.getUserName();
                int thisVersion = 1;
                try {
                    thisVersion = getPackageManager().getPackageInfo("zgan.ohos", PackageManager.GET_CONFIGURATIONS).versionCode;
                } catch (Exception e) {
                }
                //首次安装或升级安装会先出现引导页
                if (usedTimes == 0 || (lastVersion != thisVersion)) {
                    if (lastVersion != thisVersion) {
                        PreferenceUtil.setLastVersion(thisVersion);
                        PreferenceUtil.setUsedTimes(0);
                    }
                    handler.sendEmptyMessageDelayed(3, 500);
                }//存储的用户名为空则跳转到登录页面
                else if (userName.equals("")) {
                    PreferenceUtil.setUsedTimes(usedTimes + 1);
                    Intent intent = new Intent(SplashActivity.this, Login.class);
                    startActivityWithAnim(intent);
                    finish();
                }//直接跳转到首页
                else {
                    PreferenceUtil.setUsedTimes(usedTimes + 1);
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivityWithAnim(intent);
                    finish();
                }
            } else if (msg.what == 3) {//first
                Intent intent = new Intent(SplashActivity.this, GuideIndexActivity.class);
                startActivity(intent);
                finish();
            }
        }
    };
    private Handler communityHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {//autologin
                Frame frame = (Frame) msg.obj;
                String result = generalhelper.getSocketeStringResult(frame.strData);
                String[] results = result.split(",");
                if (frame.subCmd == 1 && results[0].equals("0")) {
                    SystemUtils.setIsCommunityLogin(true);
                }
            }
        }
    };

    // 判断数据发送超时
    private class Thread_TimerToActivity implements Runnable {

        private boolean isRun = true;

        @Override
        public void run() {
            // TODO Auto-generated method stub

            while (isRun) {

                try {
                    Thread.sleep(500);

                    isRun = false;
                    //handler.sendEmptyMessage(2);

                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    ;

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(null);
    }
}
