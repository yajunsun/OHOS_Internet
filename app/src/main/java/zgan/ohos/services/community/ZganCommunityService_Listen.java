package zgan.ohos.services.community;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import zgan.ohos.services.push.ZganSocketClient;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.FrameTools;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;
import zgan.ohos.utils.generalhelper;

/***
 * create by yajunsun
 * 小区云长链接数据监听线程
 */
public class ZganCommunityService_Listen implements Runnable {

    private ZganSocketClient zsc;
    private static final String TAG = "suntest_Community_L";
    public static int ServerState = 0;
    private Context _context;
    private boolean iniNetState = false;
    //子线程中的handler，用于APP网络连接后的自动登录操作
    Handler myhandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {//autologin
                Frame frame = (Frame) msg.obj;
                String result = generalhelper.getSocketeStringResult(frame.strData);
                String[] results = result.split(",");
                if (frame.subCmd == 1 && results[0].equals("0")) {
                    SystemUtils.setIsCommunityLogin(true);
                    //获取室内机SID
                    ZganCommunityService.toGetServerData(28, 0, PreferenceUtil.getUserName(), myhandler);
                    //获取联网令牌
                    ZganCommunityService.toGetServerData(43,PreferenceUtil.getUserName(),myhandler);
                    ZganCommunityServiceTools.isConnect = true;
                    Log.i(TAG, "自动重新登录小区云成功");
                }
                else if (frame.subCmd==43&&results[0].equals("0"))
                {
                    SystemUtils.setNetToken(results[1]);
                }else if (frame.subCmd == 28 && results[0].equals("0")) {
                    if (results.length == 2)
                        PreferenceUtil.setSID(results[1]);
                } else {
                    zsc.toConnectDisconnect();
                    ServerState = 0;
                    ZganCommunityServiceTools.isConnect = false;
                    Log.i(TAG, "自动重新登录小区云失败");
                }
            }
        }
    };

    public ZganCommunityService_Listen(Context context) {
        _context = context;
    }

    public void newSocketClient() {

        if (zsc != null) {
            zsc.toCloseClient();
        }
        zsc = new ZganSocketClient(ZganCommunityService.CommunityIp, ZganCommunityService.CommunityPort,
                ZganCommunityServiceTools.PushQueue_Send, ZganCommunityServiceTools.PushQueue_Receive);
        //zsc.ZganReceiveTime = 500;
        zsc.toStartClient();
        zsc.toStartPing(ZganCommunityService.PLATFORM_APP, FrameTools.Frame_MainCmd_Ping);
        zsc.ThreadName = "CommunityClient";
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        newSocketClient();
        boolean isNet = ZganCommunityService.isNetworkAvailable(_context);
        iniNetState = isNet;
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                break;
            }
            try {
                if (SystemUtils.getIsLogin()) {
                    isNet = ZganCommunityService.isNetworkAvailable(_context);

                    if (ServerState == 1) {
                        //用户打开网络后自动登录操作
                        if (!iniNetState) {
                            iniNetState = true;
                            Log.i("toConnectServer", "ZganCommunityService_Lister Line 86 登陆小区云");
                            ZganCommunityService.toAutoUserLogin(myhandler);
                        }
                        if (!isNet) {
                            ServerState = 2;
                            ZganCommunityService.BroadError("网络连接错误");
                            Log.i(TAG, "ZganCommunityService_Listen1ServerState=" + ServerState);
                        }

                        if (zsc.client.isClosed()) {
                            ServerState = 2;
                            Log.i(TAG, "ZganCommunityService_Listen2ServerState=" + ServerState);
                        }

                        if (!zsc.isRun) {
                            ServerState = 2;
                            Log.i(TAG, "ZganCommunityService_Listen3ServerState=" + ServerState);
                        }

                    } else if (ServerState == 0) {

                        if (isNet) {
                            //ServerState = 3;
                            //zsc.Server_IP = ZganCommunityService.toGetHostIP();
                            zsc.Server_IP = ZganCommunityService.CommunityIp;
                            zsc.ServerPort = ZganCommunityService.CommunityPort;
                            if (zsc.Server_IP != null && !zsc.Server_IP.equals("0")) {
                                Log.i(TAG, "ZganCommunityService_Listen client 重新连接");
                                Log.i(TAG, "ZganCommunityService_Listenconnect to=" + zsc.Server_IP);
//                                if (toConnectServer()) {
//                                    ServerState = 1;
//                                    Log.i(TAG, "ZganCommunityService_Listen5ServerState=" + ServerState);
//                                    ZganCommunityServiceTools.isConnect = true;
//                                    //LoginMsgServer(UName);
//                                    Log.i("toConnectServer","ZganCommunityService_listen Line 127 登陆小区云");
//                                    ZganCommunityService.toAutoUserLogin(myhandler);
//                                } else {
//                                    Log.i(TAG, zsc.client == null ? "空 socket" : "非空socket");
//                                    Log.i(TAG, zsc.client.isClosed() ? "socket关闭状态" : "socket打开状态");
//                                    if (zsc.client != null && !zsc.client.isClosed())
//                                        zsc.toConnectDisconnect();
//                                    //newSocketClient();
//                                    ServerState = 0;
//                                    Log.i(TAG, "ZganCommunityService_Listen6ServerState=" + ServerState);
//                                }
                                //LoginMsgServer(UName);
                                ServerState = 1;
                                ZganCommunityServiceTools.isConnect = true;
                                ZganCommunityService.toAutoUserLogin(myhandler);
                            }

                        }

                    } else if (ServerState == 2) {
                        //网络断开
                        ZganCommunityServiceTools.isConnect = false;
                        Log.i(TAG, "ZganCommunityService_Listenclient 断开连接");
                        zsc.toConnectDisconnect();
                        ServerState = 0;
                        Log.v(TAG, "7ServerState=" + ServerState);
                        // Log.v(TAG, "client 重新连接");
                        //toConnectServer();
                    }
                }
            } catch (Exception e) {
                Log.i(TAG, "ZganCommunityService_Listen" + e.getMessage());
                continue;
            }
        }
        //ZganCommunityService.myhandler.sendEmptyMessageDelayed(0,500);
    }

    public boolean toConnectServer() {
        Log.v(TAG, zsc.isRun ? "繁忙" : "空闲");
        if (ZganCommunityService.CommunityIp != null && !ZganCommunityService.CommunityIp.equals("") && !ZganCommunityService.CommunityIp.equals("0")) {
            if (!zsc.isRun) {
                synchronized (ZganCommunityService_Listen.class) {
                    zsc.Server_IP = ZganCommunityService.CommunityIp;
                    zsc.ServerPort = ZganCommunityService.CommunityPort;
                    if (zsc.toConnectServer()) {
                        ServerState = 1;
                        return true;
                    } else
                        return false;
                }
            } else if (ZganCommunityService.CommunityIp != zsc.Server_IP || ZganCommunityService.CommunityPort != zsc.ServerPort) {
                synchronized (ZganCommunityService_Listen.class) {
                    zsc.Server_IP = ZganCommunityService.CommunityIp;
                    zsc.ServerPort = ZganCommunityService.CommunityPort;
                    zsc.toConnectDisconnect();
                    if (zsc.toConnectServer()) {
                        ServerState = 1;
                        return true;
                    } else
                        return false;
                }
            }
        }
        return true;
    }

    public void toDisConnectServer() {
        Log.v(TAG, "toDisConnectServer");
        ServerState = 0;
        zsc.toCloseClient();
    }

}
