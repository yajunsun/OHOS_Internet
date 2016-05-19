package zgan.ohos.services.login;

import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import zgan.ohos.MyApplication;
import zgan.ohos.utils.Frame;

import java.util.LinkedList;

public class ZganLoginService_Main implements Runnable {
    private java.util.Queue<byte[]> Queue = new LinkedList<byte[]>();
    private java.util.Queue<Frame> Queue_Function = new LinkedList<Frame>();
    private boolean isGetData = false;
    private static final String  TAG="ZganLoginService_Main";
    private int intSendOutTime = 200; // 20秒
    private boolean isSendOutTime = false;
    private int intTime = 0;
    private Frame getFrame;

    public ZganLoginService_Main(java.util.Queue<byte[]> _queue, java.util.Queue<Frame> _fqueue) {
        Queue = _queue;
        Queue_Function = _fqueue;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        Log.v(TAG, "ZganLoginService_MainMain");
        Thread tt = new Thread(new Thread_SendOutTime());
        tt.start();

        while (true) {
            try {
                Thread.sleep(100);

                if (ZganLoginService_Listen.ServerState == 1
                        && Queue_Function != null && Queue_Function.size() > 0) {
                    getFrame = Queue_Function.poll();

                    isGetData = true;

                    // 发送数据
                    ZganLoginServiceTools.toSendMsg(getFrame);

                    intTime = 0;
                    isSendOutTime = true;

                    // 接收数据
                    while (isGetData) {
                        if (Queue.size() > 0) {
                            byte[] resultByte = null;
                            resultByte = Queue.poll();

                            Frame f = new Frame(resultByte);
                            Log.v(TAG, "ZganLoginService_Main接收到数据"+f.subCmd);
                            //modified by yajunsun 20151218暂时修改
                            intTime = 0;
                            isSendOutTime = false;
                            isGetData = false;

                            if(f.subCmd==1&&f.platform==1&&f.strData.equals("0"))
                            {
                                 //ZganLoginService.toGetServerData();
                            }
                            //回调接口
                            if (getFrame._handler!=null) {
                                Message msg = getFrame._handler
                                        .obtainMessage();
                                msg.obj = f;
                                msg.what = 1;
                                getFrame._handler.sendMessage(msg);
                            }
                            // 用户登录
//							if (ZganLoginService.LoginServerState == 0
//									&& f.mainCmd == 0x01 && f.subCmd == 1
//									&& f.version == 1) {
//
//								if (checkUserLogin(f.strData)) {
//									// 登录成功后获取服务器列表
//
////									ZganLoginService.toGetServerList(
////											ZganLoginService.UserName,
////											getFrame._handler);
//
//									ZganLoginService.LoginServerState = 1;
//									intTime = 0;
//									isSendOutTime = false;
//									isGetData = false;
//
//								} else {
//
//									Message msg = getFrame._handler
//											.obtainMessage();
//									msg.obj = f;
//									msg.what = 0;
//
//									ZganLoginService.toClearZganDB();
//
//									getFrame._handler.sendMessage(msg);
//
//									toStopMainData();
//								}
//							}
//
//							// 解析服务器列表
//							if (ZganLoginService.LoginServerState == 1
//									&& f.mainCmd == 0x01 && f.subCmd == 4) {
//
//								if (toGetServerList(f.strData)) {
//									ZganLoginService.LoginServerState = 2;
//
//									// 保存用户信息
//									ZganLoginService.toSetZganDB(
//											ZganLoginService.ZGAN_USERNAME,
//											ZganLoginService.UserName);
//									ZganLoginService.toSetZganDB(
//											ZganLoginService.ZGAN_USERPWD,
//											ZganLoginService.UPwd);
//									ZganLoginService.toSetZganDB(
//											ZganLoginService.ZGAN_USERIMEI,
//											ZganLoginService.UIMIE);
//
//									MyApplication.phone = ZganLoginService.UserName;
//
//									f.strData = "0";
//
//									Message msg = getFrame._handler
//											.obtainMessage();
//									msg.obj = f;
//									msg.what = 1;
//
//									getFrame._handler.sendMessage(msg);
//
//									toStopMainData();
//								} else {
//
//									Message msg = getFrame._handler
//											.obtainMessage();
//									msg.obj = f;
//									msg.what = 0;
//
//									ZganLoginService.toClearZganDB();
//
//									getFrame._handler.sendMessage(msg);
//
//									toStopMainData();
//								}
//
//							}
//
//							// 登录服务器
//
//							// 处理数据
//							if (ZganLoginService.LoginServerState == 2
//									&& getFrame != null
//									&& getFrame.mainCmd == f.mainCmd
//									&& getFrame.subCmd == f.subCmd
//									&& getFrame.version == f.version
//									&& getFrame._handler != null) {
//
//								Message msg = getFrame._handler.obtainMessage();
//								msg.obj = f;
//								msg.what = 1;
//
//								getFrame._handler.sendMessage(msg);
//								Log.i("ZganLoginService_Main", "处理数据完成");
//								toStopMainData();
//							}
                        }
                    }

                }

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                continue;
            }

        }

    }

    private void toStopMainData() {
        intTime = 0;
        isSendOutTime = false;
        isGetData = false;
        // getFrame=null;

        //ZganLoginService_Listen.ServerState = 2;
    }

    private boolean checkUserLogin(String strData) {
        if (!TextUtils.isEmpty(strData)) {

            String[] aryData = strData.split("\t");

            if (aryData.length == 1 && aryData[0].equals("0")) {
                return true;
            }
        }

        return false;
    }

    private boolean toGetServerList(String strData) {
        if (!TextUtils.isEmpty(strData)) {

            String[] aryData = strData.split("\t");

            if (aryData.length > 1 && !aryData[0].equals("0")) {

                for (int i = 1; i < aryData.length; i++) {
                    String[] aryIPData = aryData[i].split(":");

                    if (aryIPData.length == 3) {
                        String temp_ip = aryIPData[0];
                        String temp_port = aryIPData[1];
                        String temp_PT = aryIPData[2];

                        temp_ip = longToIP(Long.parseLong(temp_ip)) + ":"
                                + temp_port;

//                        // 家庭卫士服务器
//                        if (temp_PT.equals("6")) {
//                            ZganLoginService.toSetZganDB(
//                                    ZganLoginService.ZGAN_JTWSSERVER, temp_ip);
//                        }
//
//                        // 消息服务器
//                        if (temp_PT.equals("7")) {
//                            ZganLoginService.toSetZganDB(
//                                    ZganLoginService.ZGAN_PUSHSERVER, temp_ip);
//                        }
//
//                        // 文件服务器
//                        if (temp_PT.equals("8")) {
//                            ZganLoginService.toSetZganDB(
//                                    ZganLoginService.ZGAN_FILESERVER, temp_ip);
//                        }

                    }

                }

                return true;
            }
        }

        return false;
    }

    // 将十进制整数形式转换成127.0.0.1形式的ip地址
    public static String longToIP(long longIp) {
        int[] buff_ip = new int[4];
        StringBuffer sb = new StringBuffer("");

        buff_ip[0] = (int) (0xFF & (longIp));
        buff_ip[1] = (int) (0xFF & (longIp >>> 8));
        buff_ip[2] = (int) (0xFF & (longIp >>> 16));
        buff_ip[3] = (int) (0xFF & (longIp >>> 24));

        sb.append(Integer.toString(buff_ip[0]));
        sb.append(".");
        sb.append(Integer.toString(buff_ip[1]));
        sb.append(".");
        sb.append(Integer.toString(buff_ip[2]));
        sb.append(".");
        sb.append(Integer.toString(buff_ip[3]));

        return sb.toString();
    }

    // 判断数据发送超时
    private class Thread_SendOutTime implements Runnable {

        @Override
        public void run() {
            // TODO Auto-generated method stub

            while (true) {

                try {
                    Thread.sleep(100);

                    if (isSendOutTime) {

                        if (intSendOutTime == intTime) {
                            if (getFrame._handler != null) {
                                Message msg = getFrame._handler.obtainMessage();
                                msg.what = 0;

                                getFrame._handler.sendMessage(msg);
                            }

                            toStopMainData();

                            Log.i("ZganLoginService_Main", "接收超时");
                        } else {
                            intTime++;
                        }
                    }

                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    ;

}
