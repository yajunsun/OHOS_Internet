package zgan.ohos.services.community;

import android.util.Log;

import zgan.ohos.utils.Frame;
import zgan.ohos.utils.FrameTools;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

public class ZganCommunityServiceTools {
    //发送消息队列
    public static Queue<byte[]> PushQueue_Send = new LinkedList<byte[]>();

    //接收消息队列
    public static Queue<byte[]> PushQueue_Receive = new LinkedList<byte[]>();

    //任务队列
    public static Queue<Frame> PushQueue_Function = new LinkedList<Frame>();

    public static boolean Thread_Ping = false;

    public static int Thread_PingTime = 0;
    public static int Thread_PingOutTime = 20000;//20秒

    public static boolean ISmsgThread = false;

    public static Calendar PingTime = null;
    public static Calendar PingSendTime = null;

    public static boolean isWifiOK = false;
    public static boolean isConnect = false;

    //发送消息
    public static void toSendMsg(Frame f) {
        byte[] Buff = null;
        Buff = FrameTools.getFrameBuffData(f);

        if (Buff != null) {
            PushQueue_Send.offer(Buff);
        }
    }

    public static void toGetFunction(Frame f) {
        PushQueue_Function.offer(f);
    }

    public static void toLog(String strTAG, String strMsg, Frame f) {
        //发送数据
        Log.i(strTAG, strMsg);
        Log.i(strTAG, strMsg + "...平台代码" + Integer.toString(f.platform));
        Log.i(strTAG, strMsg + "...版本号" + Integer.toString(f.version));
        Log.i(strTAG, strMsg + "...主功能命令字" + Byte.toString(f.mainCmd));
        Log.i(strTAG, strMsg + "...子功能命令字" + Integer.toString(f.subCmd));
        Log.i(strTAG, strMsg + "...数据" + f.strData);
    }
}
