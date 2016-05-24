package zgan.ohos.services.community;

import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import zgan.ohos.MyApplication;
import zgan.ohos.utils.DataCacheHelper;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.FrameTools;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.RDTFrame;

import java.util.LinkedList;

public class ZganCommunityService_Main implements Runnable {
    private java.util.Queue<byte[]> Queue = new LinkedList<byte[]>();
    private java.util.Queue<Frame> Queue_Function = new LinkedList<Frame>();
    private boolean isGetData = false;
    private static final String  TAG="ZganCommunity_Main";
    private int intSendOutTime = 200; // 20秒
    private boolean isSendOutTime = false;
    private int intTime = 0;
    private Frame getFrame;

    public ZganCommunityService_Main(java.util.Queue<byte[]> _queue, java.util.Queue<Frame> _fqueue) {
        Queue = _queue;
        Queue_Function = _fqueue;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        Log.i(TAG, "ZganCommunityService_MainMain");
        Thread tt = new Thread(new Thread_SendOutTime());
        tt.start();

        while (true) {
            try {
                Thread.sleep(100);

                if (ZganCommunityService_Listen.ServerState == 1
                        && Queue_Function != null && Queue_Function.size() > 0) {
                   Log.v(TAG, "Queue_Function:" + Queue_Function.size()) ;
                    getFrame = Queue_Function.poll();

                    isGetData = true;

                    // 发送数据
                    ZganCommunityServiceTools.toSendMsg(getFrame);

                    intTime = 0;
                    isSendOutTime = true;

                    byte[] readPool = new byte[1024 * 30];
                    int nReadSize = 0;
                    // 接收数据
                    while (isGetData) {
                        if (Queue.size() > 0) {
                            Log.v(TAG, "Queue:" + Queue.size()) ;
                            byte[] resultByte = null;
                            resultByte = Queue.poll();
                            /****************沾包分包********************/
                            //将获取的数据放入写入缓存readPool中
                            System.arraycopy(resultByte, 0, readPool, nReadSize, resultByte.length);
                            //缓存中数据的长度
                            nReadSize += resultByte.length;//此处size有问题
                            if (nReadSize<11)
                                continue;
                            //读取数据的长度
                            int intDataLen = FrameTools.HighLowToInt(readPool[9], readPool[10])+12;
                            if (intDataLen>nReadSize)
                            {
                                continue;
                            }
                            else if (intDataLen<nReadSize)
                            {
                                int dataL = intDataLen;//此帧数据长度
                                int writeSize = 0;//缓存中已经被读取的长度
                                Frame f1;
                                //循环读取缓存数据
                                while (dataL < nReadSize) {
                                    try {
                                        //每一帧的数据
                                        byte[] b1 = new byte[dataL];
                                        System.arraycopy(readPool, 0, b1, 0, dataL);
                                        Frame f = new Frame(b1);
                                        intTime = 0;
                                        isSendOutTime = false;
                                        isGetData = false;
                                        //回调接口
                                        if (getFrame._handler!=null) {
                                            Message msg = getFrame._handler
                                                    .obtainMessage();
                                            msg.obj = f;
                                            msg.what = 1;
                                            getFrame._handler.sendMessage(msg);
                                        }
                                        nReadSize = nReadSize - dataL;
                                        //播放后已经读取的长度增加一帧的长度
                                        writeSize = writeSize + dataL;
                                        //此处将缓存里面还没播放的数据提到位置0处
                                        byte[] unread = new byte[nReadSize];
                                        System.arraycopy(readPool, dataL, unread, 0, nReadSize);
                                        readPool = new byte[1024 * 30];
                                        System.arraycopy(unread, 0, readPool, 0, nReadSize);
                                        //当缓存池中的数据长度小于11时就继续从队列中取出数据
                                        if (nReadSize < 11)
                                            break;
                                        //继续计算下一帧数据的长度
                                        //f1 = new RDTFrame(readPool);
                                        dataL =FrameTools.HighLowToInt(readPool[9], readPool[10])+12; //f1.mLen;
                                        //如果下一帧播放的长度=缓存池数据的长度则直接播放并初始化缓存池
                                        if (dataL == nReadSize) {
                                            //playRDT(mAVChannel, pFrmNo, f1);
                                            f1 = new Frame(readPool);
                                            intTime = 0;
                                            isSendOutTime = false;
                                            isGetData = false;
                                            //回调接口
                                            if (getFrame._handler!=null) {
                                                Message msg = getFrame._handler
                                                        .obtainMessage();
                                                msg.obj = f1;
                                                msg.what = 1;
                                                getFrame._handler.sendMessage(msg);
                                            }
                                            nReadSize = 0;
                                            readPool = new byte[1024 * 30];
                                            break;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Log.i("suntest", "read error:" + e.getMessage());
                                        nReadSize = 0;
                                        break;
                                    }
                                }
                            }
                            else
                            {
                                byte[] data=new byte[nReadSize];
                                System.arraycopy(readPool,0,data,0,nReadSize);
                                Frame f = new Frame(data);
                                readPool=new byte[1024*30];
                                nReadSize=0;
                                Log.v(TAG, "ZganCommunityService_Main接收到数据"+f.subCmd);
                                //modified by yajunsun 20151218暂时修改
                                intTime = 0;
                                isSendOutTime = false;
                                isGetData = false;
                                //回调接口
                                if (getFrame._handler!=null) {
                                    Message msg = getFrame._handler
                                            .obtainMessage();
                                    msg.obj = f;
                                    msg.what = 1;
                                    getFrame._handler.sendMessage(msg);
                                }
                            }
                            /***************************************/
//                            Frame f = new Frame(resultByte);
//                            Log.v(TAG, "ZganCommunityService_Main接收到数据"+f.subCmd);
//                            //modified by yajunsun 20151218暂时修改
//                            intTime = 0;
//                            isSendOutTime = false;
//                            isGetData = false;
//
//                            if(f.subCmd==1&&f.platform==1&&f.strData.equals("0"))
//                            {
//                                 //ZganCommunityService.toGetServerData();
//                            }
//                            //回调接口
//                            if (getFrame._handler!=null) {
//                                Message msg = getFrame._handler
//                                        .obtainMessage();
//                                msg.obj = f;
//                                msg.what = 1;
//                                getFrame._handler.sendMessage(msg);
//                            }

                            /*******************************/
                            // 用户登录
//							if (ZganCommunityService.LoginServerState == 0
//									&& f.mainCmd == 0x01 && f.subCmd == 1
//									&& f.version == 1) {
//
//								if (checkUserLogin(f.strData)) {
//									// 登录成功后获取服务器列表
//
////									ZganCommunityService.toGetServerList(
////											ZganCommunityService.UserName,
////											getFrame._handler);
//
//									ZganCommunityService.LoginServerState = 1;
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
//									ZganCommunityService.toClearZganDB();
//
//									getFrame._handler.sendMessage(msg);
//
//									toStopMainData();
//								}
//							}
//
//							// 解析服务器列表
//							if (ZganCommunityService.LoginServerState == 1
//									&& f.mainCmd == 0x01 && f.subCmd == 4) {
//
//								if (toGetServerList(f.strData)) {
//									ZganCommunityService.LoginServerState = 2;
//
//									// 保存用户信息
//									ZganCommunityService.toSetZganDB(
//											ZganCommunityService.ZGAN_USERNAME,
//											ZganCommunityService.UserName);
//									ZganCommunityService.toSetZganDB(
//											ZganCommunityService.ZGAN_USERPWD,
//											ZganCommunityService.UPwd);
//									ZganCommunityService.toSetZganDB(
//											ZganCommunityService.ZGAN_USERIMEI,
//											ZganCommunityService.UIMIE);
//
//									MyApplication.phone = ZganCommunityService.UserName;
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
//									ZganCommunityService.toClearZganDB();
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
//							if (ZganCommunityService.LoginServerState == 2
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
//								Log.i("ZganCommunityService_Main", "处理数据完成");
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

        //ZganCommunityService_Listen.ServerState = 2;
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
//                            ZganCommunityService.toSetZganDB(
//                                    ZganCommunityService.ZGAN_JTWSSERVER, temp_ip);
//                        }
//
//                        // 消息服务器
//                        if (temp_PT.equals("7")) {
//                            ZganCommunityService.toSetZganDB(
//                                    ZganCommunityService.ZGAN_PUSHSERVER, temp_ip);
//                        }
//
//                        // 文件服务器
//                        if (temp_PT.equals("8")) {
//                            ZganCommunityService.toSetZganDB(
//                                    ZganCommunityService.ZGAN_FILESERVER, temp_ip);
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
                                //getFrame.strData
                                msg.what = 0;
                                Frame frame=loadData(getFrame.subCmd,getFrame.strData);
                                if (frame.strData!=null&&frame.strData.length()>0)
                                {
                                    msg.what=1;
                                }
                                getFrame._handler.sendMessage(msg);
                            }

                            toStopMainData();

                            Log.i("ZganJTWSService_Main", "接收超时");
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
    private Frame loadData(int subCmd, String strData) {
        String param = String.format("s%s%", subCmd, strData);
        Log.i("suntest",param);
        String key = ImageLoader.hashKeyFromUrl(param);
        Frame f = new Frame();
        f.platform = 0;
        f.subCmd = subCmd;
        f.strData = DataCacheHelper.loadData(key);
        Log.i("suntest","load from cache");
        return f;
    }


}
