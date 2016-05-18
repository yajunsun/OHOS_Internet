package zgan.ohos.services.push;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import zgan.ohos.services.login.ZganLoginService;
import zgan.ohos.services.login.ZganLoginService_Listen;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.FrameTools;
import zgan.ohos.utils.SystemUtils;


public class ZganSocketClient {
    public Socket client;
    public String Server_IP = "";
    public int ServerPort = 0;
    private Queue<byte[]> send_Queue = new LinkedList<byte[]>();
    private Queue<byte[]> receive_Queue = new LinkedList<byte[]>();
    private Thread thread_send;
    private Thread thread_receive;
    private Thread thread_ping;
    private Thread thread_pingoutime;
    public boolean isRun = false;
    private boolean isPing = false;
    private OutputStream ops;
    private InputStream sin;
    private int platform;
    private byte mainCmd;
    private int PingTimeCount = 0;
    private int PingOutTime = 300; //30秒
    public String ThreadName = "";
    private Calendar PingTime = null;
    private Calendar PingSendTime = null;
    private int intPOutTime;
    public int ZganReceiveTime = 100;
    public int ZganSendTime = 100;
    private List<byte[]> aryRecData = new ArrayList<byte[]>();
    //socket上次连接是否成功
    private boolean LastSocketStatus = true;

    public ZganSocketClient(String strIP, int intPort, Queue<byte[]> sq, Queue<byte[]> rq) {
        Server_IP = strIP;
        ServerPort = intPort;
        send_Queue = sq;
        receive_Queue = rq;
    }

    /**
     * 启动客户端
     */
    public void toStartClient() {
        Thread_Send ts = new Thread_Send();
        Thread_Receive tr = new Thread_Receive();

        thread_send = new Thread(ts);
        thread_send.start();

        thread_receive = new Thread(tr);
        thread_receive.start();
    }

    public void toStartPing(int _platform, byte _mainCmd) {
        Thread_Ping tp = new Thread_Ping();
        Thread_PingOutTime tpo = new Thread_PingOutTime();

        platform = _platform;
        mainCmd = _mainCmd;

        thread_ping = new Thread(tp);
        thread_ping.start();

        thread_pingoutime = new Thread(tpo);
        thread_pingoutime.start();
    }

    /**
     * 连接服务器
     */
    public boolean toConnectServer() {
        boolean flag = false;
        try {
            client = new Socket();
            client.connect(new InetSocketAddress(Server_IP, ServerPort), 10000);

            isRun = true;

            flag = true;

            ops = client.getOutputStream();
            sin = client.getInputStream();


            Log.i("toConnectServer", "连接服务器[" + Server_IP + ":" + ServerPort + "]");
            Log.v("suntest", "ZganSocketClient连接服务器[" + Server_IP + ":" + ServerPort + "]");
            LastSocketStatus = true;
            ZganLoginService.BroadError("1");
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            Log.v("suntest", "ZganSocketClient连接"+Server_IP+":"+ServerPort+"超时UnknownHostException" + e.getMessage());
            Log.i("suntest", "ZganSocketClient连接"+Server_IP+":"+ServerPort+"超时UnknownHostException" + e.getMessage());
            isRun = false;
            ZganLoginService.BroadError("连接服务器超时");
            LastSocketStatus = false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            Log.v("suntest", "ZganSocketClient连接"+Server_IP+":"+ServerPort+"超时IOException" + e.getMessage());
            Log.i("suntest", "ZganSocketClient连接" + Server_IP + ":" + ServerPort+"超时IOException" + e.getMessage());
            isRun = false;
            ZganLoginService.BroadError("连接服务器超时");
            LastSocketStatus = false;
        }

        return flag;
    }

    /**
     * 断开连接
     */
    public void toConnectDisconnect() {
        isRun = false;

        if (client != null) {
            try {
                Log.v("suntest", "ZganSocketClientsocket close");
                Log.i("suntest", "ZganSocketClientsocket close");
                client.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.v("suntest", "ZganSocketClientsocket close faild " + e.getMessage());
                Log.i("suntest", "ZganSocketClientsocket close faild " + e.getMessage());
                // e.printStackTrace();
            }
        }

        if (ops != null) {
            try {
                Log.v("suntest", "ZganSocketClientops close");
                Log.i("suntest", "ZganSocketClientops close");
                ops.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.v("suntest", "ZganSocketClientops close faild " + e.getMessage());
                Log.i("suntest", "ZganSocketClientops close faild " + e.getMessage());
                //e.printStackTrace();
            }
        }

        if (sin != null) {
            try {
                Log.v("suntest", "ZganSocketClientsin close");
                Log.i("suntest", "ZganSocketClientsin close");
                sin.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.v("suntest", "ZganSocketClientsin close faild" + e.getMessage());
                Log.i("suntest", "ZganSocketClientsin close faild" + e.getMessage());
                //e.printStackTrace();
            }
        }
    }

    /**
     * 关闭客户端
     */
    public void toCloseClient() {
        Log.v("suntest","toCloseClient");
        Log.i("suntest", "toCloseClient");
        toConnectDisconnect();

        thread_send.interrupt();
        thread_receive.interrupt();
        thread_send=null;
        thread_receive=null;
    }

    /**
     * 发送子线程
     */
    private class Thread_Send implements Runnable {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            while (true) {

                try {
                    Thread.sleep(ZganSendTime);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    isRun = false;
                    e1.printStackTrace();
                    Log.v("suntest", e1.getMessage());
                }

                if (isRun && send_Queue.size() > 0) {
                    byte[] sendByte = null;

                    toStopPing();
                    Log.v("suntest", "ZganSocketClient发送数据");
                    Log.i("suntest", "ZganSocketClient发送数据");
                    sendByte = send_Queue.poll();

                    try {

                        ops.write(sendByte);
                        ops.flush();

                        toBeginPing();

                    } catch (IOException e) {
                        ZganLoginService.BroadError("服务器连接错误");
                        LastSocketStatus = false;
                        isRun = false;
                        // TODO Auto-generated catch block
                        //e.printStackTrace();
                        Log.v("suntest", "ZganSocketClientsend error" + e.getMessage());
                        Log.i("suntest", "ZganSocketClientsend error" + e.getMessage());
                        //ZganLoginService_Listen.ServerState=2;
                        continue;
                    }

                }
            }
        }

    }

    /**
     * 接收子线程+
     */
    private class Thread_Receive implements Runnable {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            int intRecLeng = 0;
            int intRecCount = 0;
            boolean isAddData = false;


            while (true) {

                try {
                    Thread.sleep(ZganReceiveTime);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    isRun = false;
                    e1.printStackTrace();
                    Log.v("suntest", "ZganSocketClientreceive error" + e1.getMessage());
                    Log.i("suntest", "ZganSocketClientreceive error" + e1.getMessage());
                    break;
                }

                if (isRun) {
                    byte[] resultByte = new byte[4096];
                    byte[] recByte = null;

                    try {
                        if (sin != null) {
                            int intLen = sin.read(resultByte);

                            if (intLen != -1) {

                                toStopPing();

                                if (resultByte != null && resultByte.length > 0 &&
                                        resultByte[0] == 36 && resultByte[1] == 90
                                        && resultByte[2] == 71 && resultByte[3] == 38) {

                                    //判断是否是心跳包
                                    if (resultByte[7] == 0 && resultByte[8] == 0) {
                                        Log.v("suntest", "ZganSocketClient心跳 1");
                                        Log.i("suntest", "ZganSocketClient心跳 1");
                                        PingTime = Calendar.getInstance();
                                        PingSendTime = null;
                                    } else {
                                        intRecLeng = 0;
                                        intRecCount = 0;
                                        aryRecData.clear();

                                        recByte = toGetReceiveData(intLen, resultByte);

                                        resultByte = null;

                                        intRecLeng = toGetlBuffLen(recByte);
                                        intRecCount = recByte.length;
                                        Log.i("suntest", "ZganSocketClient获得数据1");
                                        aryRecData.add(recByte);

                                    }
                                } else if (isAddData) {
                                    recByte = toGetReceiveData(intLen, resultByte);
                                    resultByte = null;
                                    Log.i("suntest", "ZganSocketClient获得数据2");
                                    aryRecData.add(recByte);
                                    intRecCount += recByte.length;
                                }
                                if (intRecLeng == intRecCount) {
                                    if (aryRecData.size() > 0) {
                                        Log.i("suntest", "ZganSocketClient获得数据3");
                                        receive_Queue.add(toGetData(aryRecData, intRecLeng));
                                        intRecLeng = 0;
                                        intRecCount = 0;
                                        aryRecData.clear();
                                    }

                                    toBeginPing();
                                } else {

                                    isAddData = true;
                                }
                            }
                        }

                    } catch (IOException e) {
                        ZganLoginService.BroadError("服务器错误");
                        LastSocketStatus = false;
                        isRun = false;
                        //ZganLoginService_Listen.ServerState=2;
                        // TODO Auto-generated catch block
                        //e.printStackTrace();
                        Log.i("suntest", "ZganSocketClientreceive error 1" + e.getMessage());
                        continue;
                    }
                }
            }
        }
    }

    //去掉多余数据
    private byte[] toGetReceiveData(int intLen, byte[] resultByte) {
        byte[] Buff = new byte[intLen];

        System.arraycopy(resultByte, 0, Buff, 0, Buff.length);

        return Buff;
    }

    //得到数据长度
    private int toGetlBuffLen(byte[] resultByte) {
        int intLeng = 0;

        intLeng = FrameTools.HighLowToInt(resultByte[9], resultByte[10]);
        intLeng = intLeng + 12;


        return intLeng;
    }

    //组合数据
    private byte[] toGetData(List<byte[]> aryRecData, int intLen) {

        byte[] aryResult = null;

        if (intLen > 0) {
            aryResult = new byte[intLen];
            int i = 0;

            for (byte[] b : aryRecData) {
                System.arraycopy(b, 0, aryResult, i, b.length);
                i = i + b.length;

            }
        }

        return aryResult;
    }

    //发送消息
    public void toSendMsg(Frame f) {
        byte[] Buff = null;
        Buff = FrameTools.getFrameBuffData(f);

        if (Buff != null) {
            send_Queue.offer(Buff);
        }
    }

    private void toStopPing() {
        isPing = false;
        PingTimeCount = 0;
        PingTime = null;
    }

    private void toBeginPing() {
        isPing = true;
        PingTimeCount = 0;
        PingTime = null;
    }

    /**
     * 心跳线程
     */
    private class Thread_Ping implements Runnable {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            while (true) {

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    break;
                }

                if (isRun && isPing&& SystemUtils.getIsCommunityLogin()) {
                    PingTimeCount += 1;

                    if (PingTimeCount == 150) {
                        PingTimeCount = 0;

                        Frame f = new Frame();
                        f.platform = platform;
                        f.mainCmd = mainCmd;

                        PingSendTime = Calendar.getInstance();
                        PingTime = null;
                        toSendMsg(f);
                        Log.v("suntest", "ZganSocketClientping");
                        Log.i("suntest", "ZganSocketClientping");
                    }
                }
            }
        }

    }

    /**
     * 心跳超时线程
     */
    private class Thread_PingOutTime implements Runnable {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            while (true) {

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    Log.v("suntest", e1.getMessage());
                    break;
                }

                if (isRun && PingSendTime != null) {
                    //计时器
                    if (intPOutTime == PingOutTime
                            && PingTime == null) {
                        isRun = false;

                        PingTime = null;
                        PingSendTime = null;

                        intPOutTime = 0;

                    } else {
                        intPOutTime++;
                    }

                }
            }
        }

    }
}
