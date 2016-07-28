package zgan.ohos.services.login;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.UnknownHostException;

import zgan.ohos.MyApplication;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.LocationUtil;
import zgan.ohos.utils.NetUtils;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;
import zgan.ohos.utils.generalhelper;

public class ZganLoginService extends Service {

    public static boolean ServiceRin = false;
    private static Thread _threadListen;
    private static Thread _threadMain;
    private static ZganLoginService_Listen ztl;
    public static String UserName = "";
    public static String UPwd = "";
    public static String UIMIE = "";
    public static int Tag = 0;
    public static final String TAG = "ZganLoginService";

    //正式115.29.147.12  测试60.172.246.193
    //神行云域名
    private final static String ZGAN_LOGIN_DOMAINNAME ="login.1home1shop.com";//"test.1home1shop.com";//
    //神行云默认IP
    private final static String ZGAN_LOGIN_IP = "115.28.202.130";
    //神行云默认port
    public final static int ZGAN_LOGIN_PORT = 31001;//21000;
    public static String LoginService_IP = "115.28.202.130";
    //Android平台
    public final static int PLATFORM_APP = 0xF;
    //版本号1
    public final static int VERSION_1 = 0x01;
    //版本号2
    public final static int VERSION_2 = 0x02;
    public final static int MAIN_CMD = 0x01;
    //登录名
    public final static String ZGAN_USERNAME = ""; //PreferenceUtil.getUserName();

    public final static String ZGAN_DBNAME = "ZGANDB";
    public final static String ZGAN_JTWSSERVER = "ZGAN_JTWSSERVER";
    public final static String ZGAN_FILESERVER = "ZGAN_FILESERVER";
    public final static String ZGAN_PUSHSERVER = "ZGAN_PUSHSERVER";
    public final static String ZGAN_SOCKETE_ERR = "zgan.ohos.Community.ZGAN_SOCKETE_ERR";

    public static Context _zgan_context;
    //private static SharedPreferences ZganInfo;
    public static int LoginServerState = 0;  //0:登录用户,1:获取IP列表,2:其它方法

    @Override
    public void onCreate() {
        super.onCreate();
        //_zgan_context = MyApplication.context;
        toStartLoginService();
    }

    public static void setContext(Context _context) {
        _zgan_context = _context;
    }

    public static Handler myhandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.v("TAG", "ZganLoginServicehandle:" + msg.what);
            if (msg.what == 0) {
                toRestartLoginSerice();
            } else if (msg.what == 1) {//autologin
                Frame frame = (Frame) msg.obj;
                String result = generalhelper.getSocketeStringResult(frame.strData);
                String[] results = result.split(",");
                if (frame.subCmd == 1 && results[0].equals("0")) {
                    SystemUtils.setIsLogin(true);
                    toGetServerData(3, 0, PreferenceUtil.getUserName(), myhandler);
                    Log.v("TAG", "ZganLoginService自动重新登录成功");
                } else if (frame.subCmd == 3) {
                    String communityIP = PreferenceUtil.getCommunityIP();
                    int communityPort = PreferenceUtil.getCommunityPORT();
                    if (results.length == 3 && results[0].equals("0")) {
                        Log.v("TAG", "ZganLoginService小区ID：" + results[1]);
                        //String[]ipport=results[1].split(":");
                        if (!communityIP.equals(NetUtils.getIp(results[1])) || communityPort != Integer.parseInt(results[2])) {
                            PreferenceUtil.setCommunityIP(NetUtils.getIp(results[1]));
                            PreferenceUtil.setCommunityPORT(Integer.parseInt(results[2]));
                            communityIP = NetUtils.getIp(results[1]);
                            communityPort = Integer.parseInt(results[2]);
                        }
                        ZganCommunityService.CommunityIp = communityIP;
                        ZganCommunityService.CommunityPort = communityPort;
                        Log.i("toConnectServer","ZganLoginService 登陆小区云");
                        ZganCommunityService.toAutoUserLogin(mycommunityHandler);
                    }
                } else {
                    Log.v("TAG", "ZganLoginService自动重新登录失败");
                }
            }
        }
    };

    public static Handler mycommunityHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {//autologin
                Frame frame = (Frame) msg.obj;
                String result = generalhelper.getSocketeStringResult(frame.strData);
                String[] results = result.split(",");
                if (frame.subCmd == 1 && results[0].equals("0")) {
                    ZganCommunityService.toGetServerData(28, 0, PreferenceUtil.getUserName(), mycommunityHandler);
                    SystemUtils.setIsCommunityLogin(true);
                }
                else if (frame.subCmd == 28 && results[0].equals("0")) {
                    if (results.length == 2)
                        PreferenceUtil.setSID(results[1]);
                }
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 用户登录
     */
    public static void toUserLogin(String strUName, String strPwd, String strImei, Handler _handler) {
        if (strImei.equals(""))
            strImei=LocationUtil.getDrivenToken(MyApplication.context, strUName);
        Log.v(TAG, "ZganLoginService log in");
        Frame f = createFrame();
        f.subCmd = 1;
        f.strData = strUName + "\t" + strPwd + "\t" + strImei + "\t0";
        f._handler = _handler;
        //f.version = VERSION_1;
        Log.i(TAG, "toUserLogin");
        ztl.toConnectServer();
        toGetData(f);
        UserName = strUName;
        UPwd = strPwd;
        UIMIE = strImei;
    }

    /**
     * 用户自动登录
     */
    public static boolean toAutoUserLogin(Handler _handler) {
        //PreferenceUtil sp= PreferenceUtil.getInstance(MyApplication.context);
        String strUserName = PreferenceUtil.getUserName(); //"15223796495";
        String strPwd = PreferenceUtil.getPassWord(); //"123456";//toGetDB(ZGAN_USERPWD);
        //"8886c1f212ae6576";//toGetDB(ZGAN_USERIMEI);

        Log.i(TAG, "ZganLoginService auto login");
        if (!TextUtils.isEmpty(strUserName) && !TextUtils.isEmpty(strPwd)) {
            try {
                String strImei = LocationUtil.getDrivenToken(MyApplication.context, strUserName);
                toUserLogin(strUserName, strPwd, strImei, _handler);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 用户注销登录
     */
    public static void toUserQuit(Handler _handler) {
        String strUserName = PreferenceUtil.getUserName(); //getUserName();

        Frame f = createFrame();
        f.subCmd = 5;
        f.strData = strUserName;
        f._handler = _handler;

        ZganLoginService.LoginServerState = 2;

        ztl.toConnectServer();

        toGetData(f);
    }

    /**
     * 获取服务器数据(通用)
     */
    public static void toGetServerData(int subcmd, String strData, Handler _handler) {
        Frame f = createFrame();
        f.subCmd = subcmd;
        f.strData = strData;
        f._handler = _handler;
        Log.i(TAG, String.format("toGetServerData(%s,%s,handler)", subcmd, strData));
        ztl.toConnectServer();

        ZganLoginService.LoginServerState = 2;

        toGetData(f);
    }

    public static void toGetServerData(int subcmd, int zip, String strData, Handler _handler) {
        Frame f = createFrame();
        f.subCmd = subcmd;
        f.strData = strData;
        f.zip = zip;
        f._handler = _handler;

        toGetData(f);
    }

    public static void toGetServerData(int subcmd, int zip, int ver, String strData, Handler _handler) {
        Frame f = createFrame();
        f.subCmd = subcmd;
        f.strData = strData;
        f.zip = zip;
        f._handler = _handler;
        f.version = ver;

        toGetData(f);
    }

    /**
     * 获取服务器数据(通用)
     */
    public static void toGetServerData(int subcmd, String[] aryParam, Handler _handler) {
        Frame f = createFrame();
        f.subCmd = subcmd;
        f.strData = getParam(aryParam);
        f._handler = _handler;

        ZganLoginService.LoginServerState = 2;

        ztl.toConnectServer();

        toGetData(f);
    }

    /**
     * 获取服务器数据(通用)
     */
    public static void toGetServerData(int subcmd, String[] aryParam, Handler _handler, int intVar) {
        Frame f = createFrame();
        f.subCmd = subcmd;
        f.strData = getParam(aryParam);
        f._handler = _handler;
        f.version = intVar;

        ZganLoginService.LoginServerState = 2;

        ztl.toConnectServer();

        toGetData(f);
    }

    /**
     * 获取服务器数据(通用)
     */
    public static void toGetServerData(int subcmd, String[] aryParam, Handler _handler, int intVar, int mainCmd) {
        Frame f = createFrame();
        f.mainCmd = (byte) mainCmd;
        f.subCmd = subcmd;
        f.strData = getParam(aryParam);
        f._handler = _handler;
        f.version = intVar;

        ZganLoginService.LoginServerState = 2;

        ztl.toConnectServer();

        toGetData(f);
    }

    private static String getParam(String[] aryParam) {
        String strParam = "";

        if (aryParam != null) {
            for (String oneRow : aryParam) {
                strParam += "\t" + oneRow;
            }

            if (strParam != null && !strParam.equals("")) {
                strParam = strParam.substring(1);
            }
        }

        return strParam;
    }

    /**
     * 创建小区云数据包
     */
    public static Frame createFrame() {
        Frame f = new Frame();
        f.platform = PLATFORM_APP;
        f.mainCmd = MAIN_CMD;
        f.version = VERSION_1;
        return f;
    }

    public static void toGetData(Frame f) {
        ZganLoginServiceTools.toGetFunction(f);
    }

    public static void toRestartLoginSerice() {
        ServiceRin = false;
        ztl.toDisConnectServer();
        _threadListen.interrupt();
        _threadMain.interrupt();
        _threadListen = null;
        _threadMain = null;
        toStartLoginService();
        Log.v(TAG, "toRestartLoginSerice");
        toAutoUserLogin(myhandler);
    }

    //启动登录服务线程
    public static void toStartLoginService() {
        if (!ServiceRin) {
            Log.v(TAG, "ZganLoginServicestart service");
            LoginService_IP = toGetHostIP();
            Log.v(TAG, "ZganLoginServiceget host ip");
            //_zgan_context = context;

            //ZganInfo = _zgan_context.getSharedPreferences(ZGAN_DBNAME, Context.MODE_PRIVATE);

            //启动监听线程
            ztl = new ZganLoginService_Listen(_zgan_context);
            _threadListen = new Thread(ztl);
            _threadListen.start();

            //启动主线程
            ZganLoginService_Main zm = new ZganLoginService_Main(ZganLoginServiceTools.PushQueue_Receive,
                    ZganLoginServiceTools.PushQueue_Function);
            _threadMain = new Thread(zm);
            _threadMain.start();

            ServiceRin = true;
        }
    }

//    private static boolean toSaveDB(String strKey, String strValue) {
//        Editor editor = ZganInfo.edit();
//
//        editor.putString(strKey, strValue);
//        return editor.commit();
//    }

//    public static String toGetDB(String strKey) {
//        if (ZganInfo.getString(strKey, null) != null) {
//            return ZganInfo.getString(strKey, null);
//        } else {
//            return null;
//        }
//    }

    //解析登录服务器IP
    public static  String toGetHostIP() {
        String strIP = ZGAN_LOGIN_IP;
        try {
            strIP=new getHostIpTask().execute().get();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
      return strIP;
    }

    public static class getHostIpTask extends  AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... params) {
            InetAddress x;
            String strIP = ZGAN_LOGIN_IP;
            try {
                x = java.net.InetAddress.getByName(ZGAN_LOGIN_DOMAINNAME);
                strIP = x.getHostAddress();

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return strIP;
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        if (context == null)
            context = MyApplication.context;
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {

        } else {
            //如果仅仅是用来判断网络连接则可以使用 cm.getActiveNetworkInfo().isAvailable();  
            NetworkInfo[] info = cm.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static void BroadError(String error) {
        Intent intent = new Intent(ZganLoginService.ZGAN_SOCKETE_ERR);
        Bundle bundle = new Bundle();
        bundle.putString("msg", error);
        intent.putExtras(bundle);
        MyApplication.context.sendBroadcast(intent);
    }

    @Override
    public boolean stopService(Intent name) {
        ztl.toDisConnectServer();
        _threadListen.interrupt();
        _threadMain.interrupt();
        Log.v(TAG, "ZganLoginService stoped");
        Log.i(TAG, "ZganLoginService stoped");
        return super.stopService(name);
    }
}
