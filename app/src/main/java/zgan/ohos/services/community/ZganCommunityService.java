package zgan.ohos.services.community;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.tutk.IOTC.Camera;
import com.tutk.IOTC.RDTCamera;

import zgan.ohos.MyApplication;
import zgan.ohos.utils.DataCacheHelper;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.LocationUtil;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.generalhelper;

public class ZganCommunityService extends Service {

    public static boolean ServiceRin = false;
    private static Thread _threadListen;
    private static Thread _threadMain;
    private static ZganCommunityService_Listen ztl;
    public static int Tag = 0;
    public static final String TAG = "suntestCommunityService";
    //小区云IP
    public static String CommunityIp;
    //小区云port
    public static int CommunityPort;

    //Android平台
    public final static int PLATFORM_APP = 0xF;
    //版本号1
    public final static int VERSION_1 = 0x01;
    //版本号2
    public final static int VERSION_2 = 0x02;
    public final static int MAIN_CMD = 0x01;
    //登录名
    //public final static String ZGAN_USERNAME = PreferenceUtil.getUserName();

    public final static String ZGAN_DBNAME = "ZGANDB";
    public final static String ZGAN_SOCKETE_ERR = "zgan.ohos.Community.ZGAN_SOCKETE_ERR";

    public static Context _zgan_context;
    //private static SharedPreferences ZganInfo;
    public static int ServerState = 0;  //0:未启动,1:正在运行

    @Override
    public void onCreate() {
        super.onCreate();
//        CommunityIp=PreferenceUtil.getCommunityIP();
//        CommunityPort=PreferenceUtil.getCommunityPORT();
        ServerState=1;
        toStartLoginService();
    }

    public static void setContext(Context _context)
    {
        _zgan_context=_context;
    }

    public static Handler myhandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.v("TAG", "ZganCommunityServicehandle:" + msg.what);
            if (msg.what == 0) {
                toRestartLoginSerice();
            } else if (msg.what == 1) {//autologin
                Frame frame = (Frame) msg.obj;
                String result = generalhelper.getSocketeStringResult(frame.strData);
                String[] results = result.split(",");
                if (frame.subCmd == 1 && results[0].equals("0")) {
                    toGetServerData(28, 0, PreferenceUtil.getUserName(), myhandler);
                    Log.v("TAG", "ZganCommunityService自动重新登录成功");
                } else if (frame.subCmd == 28 && results[0].equals("0")) {
                    if (results.length == 2)
                        PreferenceUtil.setSID(results[1]);
                } else {
                    Log.v("TAG", "ZganCommunityService自动重新登录失败");
                }
            }
        }
    };

    private IBinder mIBinder=new communityService();
    @Override
    public IBinder onBind(Intent intent) {
        return mIBinder;
    }

    /***
     * 视频通话中通过服务与服务器通信
     */
    public class communityService extends Binder {
        ICallOutListner callOutListner;
        public void setCallOutListner(ICallOutListner _listner)
        {
            this.callOutListner=_listner;
        }
        public void hangup()
        {
            toGetServerData(37, 0, String.format("%s\t4", PreferenceUtil.getUserName()), null);
            Log.i("suntest","toGetServerData(37, 0, \""+PreferenceUtil.getUserName()+"\\t4\"), null)");
        }
        public void reqquestCall()
        {
            ZganCommunityService.toGetServerData(37, 0, String.format("%s\t3", PreferenceUtil.getUserName()), thishandler);
        }
        public void uninstallCamera()
        {
            thishandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    RDTCamera.uninit();
                }
            },5000);
        }
        private Handler thishandler=new Handler(Looper.getMainLooper())
        {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what)
                {
                    case 1:
                    {
                        Frame frame = (Frame) msg.obj;
                        String ret = generalhelper.getSocketeStringResult(frame.strData);
                        String[] results = ret.split(",");
                        Log.i(TAG, "CallOut:" + frame.subCmd + "  " + ret);
                        if (frame.subCmd == 37) {
                            if (results.length == 3 && results[0].equals("0")) {
                                if (results[2].equals("4")) {
                                    if (callOutListner!=null)
                                    {
                                        callOutListner.CanntConnect();
                                    }
                                } else {
                                    if (callOutListner!=null)
                                    {
                                        callOutListner.Connect(results[1]);
                                    }
                                }
                            } else if (results[0].equals("20")) {
                                //设备不在线
                                if (callOutListner!=null)
                                {
                                    callOutListner.NotOnLine();
                                }
                            }
                            else if (results[0].equals("25"))
                            {
                                //设备忙
                                if (callOutListner!=null)
                                {
                                    callOutListner.InBusy();
                                }
                            }

                        }
                        break;
                    }
                }
            }
        };

    }
    public interface ICallOutListner{
        void Connect(String devUid);
        void CanntConnect();
        void NotOnLine();
        void InBusy();
    }
    /**
     * 用户登录
     */
    public static void toUserLogin(String strUName, String strPwd, Handler _handler) {
        String strImei = LocationUtil.getDrivenToken(MyApplication.context, strUName);
        Log.i(TAG, "ZganCommunityService log in");
        Frame f = createFrame();
        f.subCmd = 1;
        f.strData = strUName + "\t" + strPwd + "\t" + strImei + "\t0";
        f._handler = _handler;
        f.version = VERSION_1;
        ztl.toConnectServer();
        toGetData(f);
    }

    /**
     * 用户自动登录
     */
    public static boolean toAutoUserLogin(Handler _handler) {
        String strUserName = PreferenceUtil.getUserName();
        String strPwd = PreferenceUtil.getPassWord();

        Log.v(TAG, "toAutoUserLogin");
        if (!TextUtils.isEmpty(strUserName) && !TextUtils.isEmpty(strPwd)) {
            try {
                toUserLogin(strUserName, strPwd, _handler);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 获取服务器数据(通用)
     */
    public static void toGetServerData(int subcmd, String strData, Handler _handler) {
        Frame f = createFrame();
        f.subCmd = subcmd;
        f.strData = strData;
        f._handler = _handler;
        Log.i(TAG,String.format("toGetServerData(%s,%s,handler)",subcmd,strData));
        ztl.toConnectServer();

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
            Log.i(TAG,String.format("toGetServerData(%s,%s,%s,%s,handler)",subcmd,zip,ver,strData));
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
        ZganCommunityServiceTools.toGetFunction(f);
    }

    public static void disconnectToserver()
    {
        ztl.toDisConnectServer();
    }
    public static void toRestartLoginSerice() {
        ServiceRin = false;
        ztl.toDisConnectServer();
        _threadListen.interrupt();
        _threadMain.interrupt();
        _threadListen = null;
        _threadMain = null;
        toStartLoginService();
        Log.i("toConnectServer","toRestartLoginSerice 登陆小区云");
        toAutoUserLogin(myhandler);
    }

    //启动登录服务线程
    public static void toStartLoginService() {
        if (!ServiceRin) {
            Log.v(TAG, "ZganCommunityServicestart service");
            Log.v(TAG, "ZganCommunityServiceget host ip");
            //_zgan_context = context;

            //ZganInfo = _zgan_context.getSharedPreferences(ZGAN_DBNAME, Context.MODE_PRIVATE);

            //启动监听线程
            ztl = new ZganCommunityService_Listen(_zgan_context);
            _threadListen = new Thread(ztl);
            _threadListen.start();

            //启动主线程
            ZganCommunityService_Main zm = new ZganCommunityService_Main(ZganCommunityServiceTools.PushQueue_Receive,
                    ZganCommunityServiceTools.PushQueue_Function);
            _threadMain = new Thread(zm);
            _threadMain.start();

            ServiceRin = true;
        }
    }

//    //解析登录服务器IP
//    public static String toGetHostIP() {
//        InetAddress x;
//        String strIP = ZGAN_LOGIN_IP;
//
//        try {
//            x = java.net.InetAddress.getByName(ZGAN_LOGIN_DOMAINNAME);
//            strIP = x.getHostAddress();
//
//        } catch (UnknownHostException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//        return strIP;
//    }

    public static boolean isNetworkAvailable(Context context) {
        try{
        if (context==null)
            context=MyApplication.context;
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
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public static void BroadError(String error) {
        Intent intent = new Intent(ZganCommunityService.ZGAN_SOCKETE_ERR);
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
        Log.i(TAG, "ZganCommunityService stoped");
        return super.stopService(name);
    }
}
