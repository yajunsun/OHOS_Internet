package com.tutk.IOTC;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import zgan.ohos.utils.RDTFrame;

/**
 * Created by yajunsun on 2016/7/20 0020.
 * 通过RDT方式
 */
public abstract class RDTCamera extends Camera implements com.tutk.IOTC.IRegisterIOTCListener {
    private String mName;
    private String mUID;
    private String mAcc;
    private String mPwd;

    private int mEventCount = 0;

    private boolean bIsMotionDetected;
    private boolean bIsIOAlarm;

    private UUID mUUID = UUID.randomUUID();

    private List<AVIOCTRLDEFs.SStreamDef> mStreamDefs = Collections.synchronizedList(new ArrayList<AVIOCTRLDEFs.SStreamDef>());


    private static volatile int mCameraCount = 0;
    protected ThreadConnectDev mThreadConnectDev = null;
    public ThreadRecvVideo2 threadRecvVideo = null;
    public PollData pollData = null;
    public RDTQueue rdtQueue;

    public ThreadDecodeVideo2 threadDecVideo = null;

    public synchronized static int init() {
        int nRet = -1;
        int rRet = -1;

        if (mCameraCount == 0) {
            int port = (int) (10000 + (System.currentTimeMillis() % 10000));

            // nRet = IOTCAPIs.ialize(port, "50.19.254.134", "122.248.234.207", "m4.iotcplatform.com", "m5.iotcplatform.com");
            nRet = IOTCAPIs.IOTC_Initialize(port, "50.19.254.134", "122.248.234.207", null, null);
            //nRet = IOTCAPIs.IOTC_Initialize2(port);

            Log.i("IOTCamera", "IOTC_Initialize2() returns " + nRet);

            if (nRet < 0) {
                return nRet;
            }

            //nRet = AVAPIs.avInitialize(mDefaultMaxCameraLimit * 16);
            rRet = RDTAPIs.RDT_Initialize();
            Log.i("suntest", "RDT_Initialize() = " + rRet);

            if (rRet < 0) {
                return rRet;
            }
        }

        mCameraCount++;
        return rRet;
    }

    public synchronized static int uninit() {

        int nRet = 0;

        if (mCameraCount > 0) {
            mCameraCount--;

            if (mCameraCount == 0) {
                RDTAPIs.RDT_DeInitialize();
                //nRet = AVAPIs.avDeInitialize();
                Log.i("IOTCamera", "RDT_DeInitialize() returns " + nRet);
                nRet = IOTCAPIs.IOTC_DeInitialize();
                Log.i("IOTCamera", "IOTC_DeInitialize() returns " + nRet);
            }
        }

        return nRet;
    }

    public RDTCamera(String name, String uid, String acc, String pwd) {
        mName = name;
        mUID = uid;
        mAcc = acc;
        mPwd = pwd;

        this.registerIOTCListener(this);
    }

    public void connect(String uid, int channel) {
        mUID = uid;
        mDevUID = uid;

        if (mThreadConnectDev == null) {
            mThreadConnectDev = new ThreadConnectDev(0, channel);
            mThreadConnectDev.start();
        }
    }

    public void connect(String uid, String pwd) {
        mUID = uid;
        mDevUID = uid;
        mDevPwd = pwd;

        if (mThreadConnectDev == null) {
            mThreadConnectDev = new ThreadConnectDev(1, 0);
            mThreadConnectDev.start();
        }
    }

    public void disconnect() {

        Log.i("suntest", "disconnect");
        synchronized (mAVChannels) {

            for (AVChannel ch : mAVChannels) {

                ch.AudioFrameQueue.removeAll();
                ch.AudioFrameQueue = null;

                ch.VideoFrameQueue.removeAll();
                ch.VideoFrameQueue = null;

                if (micvoice != null) {
                    micvoice.clear();
                    micvoice = null;
                }
                if (latestNetVoice != null) {
                    latestNetVoice.clear();
                    latestNetVoice = null;
                }

                ch.IOCtrlQueue.removeAll();
                ch.IOCtrlQueue = null;
            }
        }

        mAVChannels.clear();

        try {
            synchronized (mWaitObjectForConnected) {
                mWaitObjectForConnected.wait(1000);
            }
        } catch (InterruptedException e) {
        }
        if (mThreadConnectDev != null)
            mThreadConnectDev.stopThread();
        if (mThreadConnectDev != null && mThreadConnectDev.isAlive()) {
            try {
                mThreadConnectDev.interrupt();
                mThreadConnectDev.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mThreadConnectDev = null;

        if (mSID >= 0) {

            IOTCAPIs.IOTC_Session_Close(mSID);
            Log.i("IOTCamera", "IOTC_Session_Close(nSID = " + mSID + ")");
            mSID = -1;
        }
        Log.i("suntest", "disconnected");
        mSessionMode = -1;

        mStreamDefs.clear();
    }

    public void startShow(int avChannel) {

        synchronized (mAVChannels) {

            for (int i = 0; i < mAVChannels.size(); i++) {

                AVChannel ch = mAVChannels.get(i);

                if (ch.getChannel() == avChannel) {
                    rdtQueue = new RDTQueue();
                    ch.VideoFrameQueue.removeAll();

                    if (threadRecvVideo == null) {
                        threadRecvVideo = new ThreadRecvVideo2(ch);
                        threadRecvVideo.start();
                    }
                    if (pollData == null) {
                        pollData = new PollData(ch);
                        pollData.start();
                    }
                    if (threadDecVideo == null) {
                        threadDecVideo = new ThreadDecodeVideo2(ch);
                        threadDecVideo.start();
                    }
                    break;
                }
            }
        }
    }

    public void stopShow(int avChannel) {

        Log.i("suntest", "stopShow");
        synchronized (mAVChannels) {

            for (int i = 0; i < mAVChannels.size(); i++) {

                AVChannel ch = mAVChannels.get(i);

                if (ch.getChannel() == avChannel) {

                    if (threadDecVideo != null) {
                        threadDecVideo.stopThread();
                        try {
                            threadDecVideo.interrupt();
                            threadDecVideo.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        threadDecVideo = null;
                    }


                    if (pollData != null) {
                        pollData.stopThread();
                        try {
                            pollData.interrupt();
                            pollData.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (threadRecvVideo != null) {
                        threadRecvVideo.stopThread();
                        try {
                            threadRecvVideo.interrupt();
                            threadRecvVideo.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        threadRecvVideo = null;
                    }

                    ch.VideoFrameQueue.removeAll();
                    if(rdtQueue!=null) {
                        rdtQueue.removeAll();
                        rdtQueue = null;
                    }
                    Log.i("suntest", "stop show");
                    break;
                }
            }
        }
    }

    protected class ThreadConnectDev extends Thread {

        protected int mConnType = -1;
        protected boolean mIsRunning = false;
        protected Object m_waitForStopConnectThread = new Object();
        int nRet = -1;
        int Channel = 0;

        public ThreadConnectDev(int connType, int channel) {
            mConnType = connType;
            Channel = channel;
        }

        public synchronized void stopThread() {

            mIsRunning = false;

            if (mSID < 0)
                IOTCAPIs.IOTC_Connect_Stop();

            synchronized (m_waitForStopConnectThread) {
                m_waitForStopConnectThread.notify();
            }
        }

        public void run() {

            int nRetryForIOTC_Conn = 0;

            mIsRunning = true;

            while (mIsRunning && mSID < 0) {

                // synchronized (mIOTCListeners) {
                for (int i = 0; i < mIOTCListeners.size(); i++) {
                    IRegisterIOTCListener listener = mIOTCListeners.get(i);
                    listener.receiveSessionInfo(RDTCamera.this, CONNECTION_STATE_CONNECTING);
                }
                // }


                if (mConnType == 0) {
                    mSID = IOTCAPIs.IOTC_Connect_ByUID(mDevUID);
                    Log.i("IOTCamera", "IOTC_Connect_ByUID(" + mDevUID + ") returns " + mSID);
                    Log.i("suntest", "IOTC_Connect_ByUID(" + mDevUID + ") returns " + mSID);
                } else if (mConnType == 1) {
                    mSID = IOTCAPIs.IOTC_Connect_ByUID2(mDevUID, mDevPwd, 2);
                    Log.i("IOTCamera", "IOTC_Connect_ByUID2(" + mDevUID + ", " + mDevPwd + ") returns " + mSID);
                } else {
                    return;
                }

                if (mSID >= 0) {

                    nRDT_ID = RDTAPIs.RDT_Create(mSID, 3000, 0);
                    Log.i("suntest", "RDTAPIs.RDT_Create(" + mSID + ", 3000, 0) returns " + nRDT_ID);
                    if (nRDT_ID < 0) {
                        IOTCAPIs.IOTC_Session_Close(mSID);
                        return;
                    } else {
                        St_RDT_Status stSInfo = new St_RDT_Status();
                        RDTAPIs.RDT_Status_Check(nRDT_ID, stSInfo);
                        Log.i("suntest", "rdt status check");
                        for (int i = 0; i < mIOTCListeners.size(); i++) {
                            IRegisterIOTCListener listener = mIOTCListeners.get(i);
                            listener.receiveSessionInfo(RDTCamera.this, CONNECTION_STATE_CONNECTED);
                        }
                        // }

                        synchronized (mWaitObjectForConnected) {
                            mWaitObjectForConnected.notify();
                        }
                        startShow(Channel);
                    }
                } else if (mSID < 0) {
                    Log.i("suntest", "IOTC_Connect_ByUID(" + mDevUID + ") returns " + mSID);
                    mIsRunning = false;
                    for (int i = 0; i < mIOTCListeners.size(); i++) {
                        IRegisterIOTCListener listener = mIOTCListeners.get(i);
                        listener.receiveSessionInfo(RDTCamera.this, CONNECTION_STATE_DISCONNECTED);
                        //setLog(Sample_RDTAPIs.MainHandler.MSGTYPE_LOG, str);
                    }
                    break;
                }

                Log.i("IOTCamera", "===ThreadConnectDev exit===");
            }
        }
    }


    protected class ThreadRecvVideo2 extends Thread {
        protected static final int MAX_BUF_SIZE = 1024 * 20;
        protected boolean bIsRunning = false;

        protected AVChannel mAVChannel;

        public ThreadRecvVideo2(AVChannel channel) {
            mAVChannel = channel;
        }

        public synchronized void stopThread() {
            bIsRunning = false;
            Log.i("suntest", "停止视频接收线程");
        }

        @Override
        public void run() {

            //System.gc();

            bIsRunning = true;

            //while (bIsRunning && (mSID < 0 || mAVChannel.getAVIndex() < 0)) {
            while (bIsRunning && (mSID < 0)) {
                try {
                    synchronized (mWaitObjectForConnected) {
                        mWaitObjectForConnected.wait(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            mAVChannel.VideoBPS = 0;
            mAVChannel.AudioBPS = 0;
            byte[] buf = new byte[MAX_BUF_SIZE];
            boolean bInitAudio = false;

            int nSamplerate = 44100;
            int nDatabits = 1;
            int nChannel = 1;
            int nVoiceCodecId = AVFrame.MEDIA_CODEC_AUDIO_PCM;
            int nFrmCount = 0;
            int nIncompleteFrmCount = 0;
            int nOnlineNumber = 0;
            long lastTimeStamp = System.currentTimeMillis();

            int ret = -1;
            //if (bIsRunning && mSID >= 0 && mAVChannel.getAVIndex() >= 0) {
            if (bIsRunning && mSID >= 0) {
                mAVChannel.IOCtrlQueue.Enqueue(mAVChannel.getAVIndex(), AVIOCTRLDEFs.IOTYPE_USER_IPCAM_START, Packet.intToByteArray_Little(mCamIndex));
                mAVChannel.IOCtrlQueue.Enqueue(mAVChannel.getAVIndex(), AVIOCTRLDEFs.IOTYPE_USER_IPCAM_AUDIOSTART, Packet.intToByteArray_Little(mCamIndex));
            }

            mAVChannel.AudioFrameQueue.removeAll();

            St_RDT_Status stSInfo = new St_RDT_Status();
            if (RDTAPIs.RDT_Status_Check(nRDT_ID, stSInfo) < 0) {
                Log.v("suntest", String.format("RDT send start failed[%d]!!\\n", ret));
                //IOTCAPIs.IOTC_Session_Close(mSID);
                //stopShow(mAVChannel.getChannel());
                return;
            }


            /*******初始化音频*********/
            nSamplerate = 16000;//AVFrame.getSamplerate(frame.getFlags());
            nDatabits = 0x02; //(int) (frame.getFlags() & 0x02);
            nDatabits = (nDatabits == 0x02) ? 1 : 0;
            nChannel = AVFrame.AUDIO_CHANNEL_MONO;//(int) (frame.getFlags() & 0x01);

            Log.i("suntest", "begin initialize audio");
            Log.i("suntest", String.format("nSamplerate:%d,nChannel:%d,nDatabits:%d,nCodecId:%d", nSamplerate, nChannel, nDatabits, nVoiceCodecId));
            bInitAudio = audioDev_init(nSamplerate, nChannel, nDatabits, nVoiceCodecId);
            Log.i("suntest", "audio initialized:");

            while (bIsRunning) {
                if (mSID >= 0 && nRDT_ID >= 0) {//&& mAVChannel.getAVIndex() >= 0) {

                    //modified by yajunsun 20160719
//                    if (System.currentTimeMillis() - lastTimeStamp > 1000) {
//
//                        lastTimeStamp = System.currentTimeMillis();
//
//                        for (int i = 0; i < mIOTCListeners.size(); i++) {
//
//                            IRegisterIOTCListener listener = mIOTCListeners.get(i);
//                            listener.receiveFrameInfo(Camera.this, mAVChannel.getChannel(), (mAVChannel.AudioBPS + mAVChannel.VideoBPS) * 8 / 1024, mAVChannel.VideoFPS, nOnlineNumber, nFrmCount, nIncompleteFrmCount);
//                        }
//
//                        mAVChannel.VideoFPS = mAVChannel.VideoBPS = mAVChannel.AudioBPS = 0;
//                    }
                    //读取RDT
                    //short[] buf1 = new short[]{};
                    int len = RDTAPIs.RDT_Read(nRDT_ID, buf, MAX_BUF_SIZE, 30000);
                    if (len < 0) {
                        for (int i = 0; i < mIOTCListeners.size(); i++) {
                            IRegisterIOTCListener listener = mIOTCListeners.get(i);
                            listener.receiveChannelInfo(RDTCamera.this, mAVChannel.getChannel(), CONNECTION_STATE_DISCONNECTED);
                        }
                        break;
                    }
//                    Log.i("suntest","receive data from rdt");
                    byte[] data = new byte[len];
                    System.arraycopy(buf, 0, data, 0, len);
                    rdtQueue.Enqueue(len, data);
                    Log.i("IOTCamera", "Enqueue rdtQueue left " + rdtQueue.getCount());
                }
            }// while--end
            if (bInitAudio) {
                Log.i("suntest", "voice stopping...");
                audioDev_stop(nVoiceCodecId);
                Log.i("suntest", "voice stopped");
            }
            mAVChannel.VideoFrameQueue.removeAll();
            if (mSID >= 0) {
                mAVChannel.IOCtrlQueue.Enqueue(mAVChannel.getAVIndex(), AVIOCTRLDEFs.IOTYPE_USER_IPCAM_STOP, Packet.intToByteArray_Little(mCamIndex));
                //AVAPIs.avClientCleanBuf(mAVChannel.getAVIndex());
                mAVChannel.IOCtrlQueue.Enqueue(mAVChannel.getAVIndex(), AVIOCTRLDEFs.IOTYPE_USER_IPCAM_AUDIOSTOP, Packet.intToByteArray_Little(mCamIndex));
            }
            Log.i("IOTCamera", "===ThreadRecvAudio exit===");
            Log.i("IOTCamera", "===ThreadRecvVideo exit===");
        }
    }

    protected class PollData extends Thread {

        protected Object m_waitObjForCheckDevStatus1 = new Object();
        AVChannel mAVChannel;

        public PollData(AVChannel _mAVChannel) {
            mAVChannel = _mAVChannel;
        }

        boolean bIsRunning = false;

        public synchronized void stopThread() {
            bIsRunning = false;
            Log.i("suntest", "停止音频接受线程");
//            synchronized (m_waitObjForCheckDevStatus) {
//                m_waitObjForCheckDevStatus.notify();
//            }
        }

        @Override
        public void run() {
            bIsRunning = true;
            int pFrmNo = 0;
            RDTFrame rdtFrame = null;
            byte[] readPool = new byte[1024 * 30];
            int nReadSize = 0;
            while (bIsRunning) {

                while (rdtQueue == null || rdtQueue.isEmpty()) {
                    try {
                        if (!bIsRunning)
                            break;
//                        synchronized (m_waitObjForCheckDevStatus1) {
//                            m_waitObjForCheckDevStatus1.wait(1000);
//                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                try {
                    //从队列中获取数据
                    RDTQueue.RDTData data = rdtQueue.Dequeue();
                    //Log.i("IOTCamera", "Dequeue rdtQueue left " + mAVChannel.rdtQueue.getCount());
                    //将获取的数据放入写入缓存readPool中
                    if (nReadSize + data.getLength() > readPool.length) {
                        nReadSize = 0;
                        readPool = new byte[1024 * 30];
                        continue;
                    }
                    System.arraycopy(data.getData(), 0, readPool, nReadSize, data.getLength());
                    //缓存中数据的长度
                    nReadSize += data.getLength();//此处size有问题
                    //将数据frame化
                    rdtFrame = new RDTFrame(readPool);
                } catch (ArrayIndexOutOfBoundsException e) {
                    //Log.i("suntest", e.getMessage());
                    e.printStackTrace();
                    nReadSize = 0;
                    readPool = new byte[1024 * 30];
                    continue;
                } catch (Exception ex) {
                    Log.i("suntest", ex.getMessage());
                    ex.printStackTrace();
                    nReadSize = 0;
                    readPool = new byte[1024 * 30];
                    continue;
                }
                //此帧数据长度如果大于缓存的长度则继续往缓存中写入数据
                if (rdtFrame.mLen > nReadSize) {
                    continue;
                }
                //如果此帧数据长度小于缓存的长度则从缓存中读取数据
                else if (rdtFrame.mLen < nReadSize && rdtFrame.mLen > 0) {
                    int dataL = rdtFrame.mLen;//此帧数据长度
                    int writeSize = 0;//缓存中已经被读取的长度
                    RDTFrame f1 = null;
                    //循环读取缓存数据
                    while (bIsRunning && dataL < nReadSize && mAudioTrack != null) {
                        try {
                            //每一帧的数据
                            byte[] b1 = new byte[dataL];
                            System.arraycopy(readPool, 0, b1, 0, dataL);
                            RDTFrame f = new RDTFrame(b1);
                            pFrmNo++;
                            //播放此帧
                            playRDT(mAVChannel, pFrmNo, f);
                            //播放后缓存中的数据减少，长度减少一帧的长度
                            nReadSize = nReadSize - dataL;
                            //播放后已经读取的长度增加一帧的长度
                            writeSize = writeSize + dataL;
                            //此处将缓存里面还没播放的数据提到位置0处
                            byte[] unread = new byte[nReadSize];
                            System.arraycopy(readPool, dataL, unread, 0, nReadSize);
                            readPool = new byte[1024 * 30];
                            System.arraycopy(unread, 0, readPool, 0, nReadSize);
                            //当缓存池中的数据长度小于12时就继续从队列中取出数据
                            if (nReadSize < 12)
                                break;
                            //继续计算下一帧数据的长度
                            f1 = new RDTFrame(readPool);
                            dataL = f1.mLen;
                            //如果下一帧播放的长度=缓存池数据的长度则直接播放并初始化缓存池
                            if (dataL == nReadSize) {
                                pFrmNo++;
                                playRDT(mAVChannel, pFrmNo, f1);
                                nReadSize = 0;
                                readPool = new byte[1024 * 30];
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            //Log.i("suntest", "read error:" + e.getMessage());
                            nReadSize = 0;
                            break;
                        }
                    }
                } else if (rdtFrame.mLen == nReadSize && rdtFrame.mLen > 0) {
                    pFrmNo++;
                    playRDT(mAVChannel, pFrmNo, rdtFrame);
                    nReadSize = 0;
                    readPool = new byte[1024 * 30];
                } else {
                    nReadSize = 0;
                    readPool = new byte[1024 * 30];
                    continue;
                }
            }
        }
    }

    protected abstract void playRDT(AVChannel mAVChannel, int pFrmNo, RDTFrame rdtFrame);


    public synchronized void rdtWrite(int rdtID, byte[] source, int length) {
        RDTAPIs.RDT_Write(rdtID, source, length);
    }

    protected class RDTQueue {

        public class RDTData {
            public int getLength() {
                return length;
            }

            public void setLength(int length) {
                this.length = length;
            }

            public byte[] getData() {
                return data;
            }

            public void setData(byte[] data) {
                this.data = data;
            }

            protected int length = 0;
            protected byte[] data = new byte[1024 * 20];

            public RDTData(int len, byte[] buf) {
                length = len;
                data = buf;
            }

            public RDTData() {
            }
        }

        LinkedList<RDTData> listData = new LinkedList<RDTData>();

        public synchronized boolean isEmpty() {
            return listData.isEmpty();
        }

        public synchronized void Enqueue(int len, byte[] data) {
            listData.addLast(new RDTData(len, data));
        }

        public int getCount() {
            return this.listData.size();
        }

        public synchronized RDTData Dequeue() {

            return listData.isEmpty() ? null : listData.removeFirst();
        }

        public synchronized void removeAll() {
            if (!listData.isEmpty())
                listData.clear();
        }

    }


    public String getUUID() {
        return mUUID.toString();
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getUID() {
        return mUID;
    }

    public String getPassword() {
        return mPwd;
    }

    public void setPassword(String pwd) {
        mPwd = pwd;
    }

    public void resetEventCount() {
        mEventCount = 0;
    }

    public int getEventCount() {
        return mEventCount;
    }

    public com.tutk.IOTC.AVIOCTRLDEFs.SStreamDef[] getSupportedStream() {
        AVIOCTRLDEFs.SStreamDef[] result = new AVIOCTRLDEFs.SStreamDef[mStreamDefs.size()];

        for (int i = 0; i < result.length; i++)
            result[i] = mStreamDefs.get(i);

        return result;
    }

    public boolean getAudioInSupported(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 1) == 0;
    }

    public boolean getAudioOutSupported(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 2) == 0;
    }

    public boolean getPanTiltSupported(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 4) == 0;
    }

    public boolean getEventListSupported(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 8) == 0;
    }

    public boolean getPlaybackSupported(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 16) == 0;
    }

    public boolean getWiFiSettingSupported(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 32) == 0;
    }

    public boolean getEventSettingSupported(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 64) == 0;
    }

    public boolean getRecordSettingSupported(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 128) == 0;
    }

    public boolean getSDCardFormatSupported(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 256) == 0;
    }

    public boolean getVideoFlipSupported(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 512) == 0;
    }

    public boolean getEnvironmentModeSupported(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 1024) == 0;
    }

    public boolean getMultiStreamSupported(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 2048) == 0;
    }

    public int getAudioOutEncodingFormat(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 4096) == 0 ? AVFrame.MEDIA_CODEC_AUDIO_SPEEX : AVFrame.MEDIA_CODEC_AUDIO_ADPCM;
    }

    public boolean getVideoQualitySettingSupport(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 8192) == 0;
    }

    public boolean getDeviceInfoSupport(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 16384) == 0;
    }

    @Override
    public void receiveChannelInfo(Camera arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void receiveFrameData(Camera arg0, int arg1, Bitmap arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void receiveFrameInfo(Camera arg0, int arg1, long arg2, int arg3, int arg4, int arg5, int arg6) {
        // TODO Auto-generated method stub

    }

    @Override
    public void receiveIOCtrlData(final Camera camera, final int avChannel, int avIOCtrlMsgType, final byte[] data) {

        if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETSUPPORTSTREAM_RESP) {

            mStreamDefs.clear();

            int num = Packet.byteArrayToInt_Little(data, 0);

            if (avChannel == 0 && this.getMultiStreamSupported(0)) {

                for (int i = 0; i < num; i++) {

                    byte[] buf = new byte[8];
                    System.arraycopy(data, i * 8 + 4, buf, 0, 8);
                    AVIOCTRLDEFs.SStreamDef streamDef = new AVIOCTRLDEFs.SStreamDef(buf);
                    mStreamDefs.add(streamDef);

                    //camera.start(streamDef.channel, mAcc, mPwd);
                }
            }

        } else if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_EVENT_REPORT) {

            int evtType = Packet.byteArrayToInt_Little(data, 12);

            if (evtType == AVIOCTRLDEFs.AVIOCTRL_EVENT_MOTIONDECT) {
                if (!bIsMotionDetected)
                    mEventCount++;
                bIsMotionDetected = true;
            } else if (evtType == AVIOCTRLDEFs.AVIOCTRL_EVENT_MOTIONPASS) {
                bIsMotionDetected = false;
            } else if (evtType == AVIOCTRLDEFs.AVIOCTRL_EVENT_IOALARM) {
                if (!bIsIOAlarm)
                    mEventCount++;
                bIsIOAlarm = true;
            } else if (evtType == AVIOCTRLDEFs.AVIOCTRL_EVENT_IOALARMPASS) {
                bIsIOAlarm = false;
            }
        }
    }

    @Override
    public void receiveSessionInfo(Camera arg0, int arg1) {

    }

}
