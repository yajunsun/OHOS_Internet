package com.tutk.IOTC;

import android.graphics.Bitmap;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import com.decoder.util.DecADPCM;
import com.decoder.util.DecG726;
import com.decoder.util.DecH264;
import com.decoder.util.DecMp3;
import com.decoder.util.DecMpeg4;
import com.decoder.util.DecSpeex;

import net.iwebrtc.audioprocess.sdk.AudioProcess;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import zgan.ohos.utils.RDTFrame;

public class Camera {

    private static volatile int mCameraCount = 0;
    private static int mDefaultMaxCameraLimit = 4;

    public static final int DEFAULT_AV_CHANNEL = 0;

    public static final int CONNECTION_STATE_NONE = 0;
    public static final int CONNECTION_STATE_CONNECTING = 1;
    public static final int CONNECTION_STATE_CONNECTED = 2;
    public static final int CONNECTION_STATE_DISCONNECTED = 3;
    public static final int CONNECTION_STATE_UNKNOWN_DEVICE = 4;
    public static final int CONNECTION_STATE_WRONG_PASSWORD = 5;
    public static final int CONNECTION_STATE_TIMEOUT = 6;
    public static final int CONNECTION_STATE_UNSUPPORTED = 7;
    public static final int CONNECTION_STATE_CONNECT_FAILED = 8;

    // public static final int CHANGE_SESSION_MODE = 1006;
    // public static final int CHANGE_CHANNEL_STREAMINFO = 1104;

    private final Object mWaitObjectForConnected = new Object();

    private ThreadConnectDev mThreadConnectDev = null;

    //private ThreadSendAudio mThreadSendAudio = null;

    private volatile int mSID = -1;
    private volatile int mSessionMode = -1;
    private volatile int nRDT_ID = -1;

    private boolean mInitAudio = false;
    private AudioTrack mAudioTrack = null;
    private int mCamIndex = 0;

    /* camera info */
    private String mDevUID;
    private String mDevPwd;

    private List<IRegisterIOTCListener> mIOTCListeners = Collections.synchronizedList(new Vector<IRegisterIOTCListener>());
    private List<AVChannel> mAVChannels = Collections.synchronizedList(new Vector<AVChannel>());


    public Camera() {
        mDevUID = "";
        mDevPwd = "";
    }

    public long getChannelServiceType(int avChannel) {
        long ret = 0;
        synchronized (mAVChannels) {
            for (AVChannel ch : mAVChannels) {
                if (ch.getChannel() == avChannel) {
                    ret = ch.getServiceType();
                    break;
                }
            }
        }
        return ret;
    }

    public boolean registerIOTCListener(IRegisterIOTCListener listener) {
        boolean result = false;

        // synchronized (mIOTCListeners) {
        if (!mIOTCListeners.contains(listener)) {
            Log.i("IOTCamera", "register IOTC listener");
            mIOTCListeners.add(listener);
            result = true;
        }
        // }

        return result;
    }

    public boolean unregisterIOTCListener(IRegisterIOTCListener listener) {
        boolean result = false;

        // synchronized (mIOTCListeners) {
        if (mIOTCListeners.contains(listener)) {
            Log.i("IOTCamera", "unregister IOTC listener");
            mIOTCListeners.remove(listener);
            result = true;
        }
        // }

        return result;
    }

    public synchronized static int init() {
        int nRet = 0;

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
            RDTAPIs.RDT_Initialize();
            Log.i("suntest", "RDT_Initialize() = " + nRet);

            if (nRet < 0) {
                return nRet;
            }
        }

        mCameraCount++;
        return nRet;
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

    public boolean isSessionConnected() {
        return mSID >= 0;
    }

    public boolean isChannelConnected(int avChannel) {

        boolean result = false;

        synchronized (mAVChannels) {
            for (AVChannel ch : mAVChannels) {
                if (avChannel == ch.getChannel()) {
                    result = mSID >= 0 && ch.getAVIndex() >= 0;
                    break;
                }
            }
        }

        return result;
    }

    public void sendIOCtrl(int avChannel, int type, byte[] data) {

        synchronized (mAVChannels) {
            for (AVChannel ch : mAVChannels) {
                if (avChannel == ch.getChannel()) {
                    ch.IOCtrlQueue.Enqueue(type, data);
                }
            }
        }
    }

    public void connect(String uid, int channel) {

        mDevUID = uid;

        if (mThreadConnectDev == null) {
            mThreadConnectDev = new ThreadConnectDev(0, channel);
            mThreadConnectDev.start();
        }
    }

    public void connect(String uid, String pwd) {

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
    }

    public void start(int avChannel, String viewAccount, String viewPasswd) {

        AVChannel session = null;
        AVChannel ch = new AVChannel(avChannel, viewAccount, viewPasswd);
        mAVChannels.add(ch);
    }

    public void startShow(int avChannel) {

        synchronized (mAVChannels) {

            for (int i = 0; i < mAVChannels.size(); i++) {

                AVChannel ch = mAVChannels.get(i);

                if (ch.getChannel() == avChannel) {

                    ch.VideoFrameQueue.removeAll();

                    if (ch.threadRecvVideo == null) {
                        ch.threadRecvVideo = new ThreadRecvVideo2(ch);
                        ch.threadRecvVideo.start();
                    }
                    if (ch.threadSendAudio == null) {
                        ch.threadSendAudio = new ThreadSendAudio(ch);
                        ch.threadSendAudio.start();
                    }
                    if (ch.pollData == null) {
                        ch.pollData = new PollData(ch);
                        ch.pollData.start();
                    }
                    if (ch.threadDecVideo == null) {
                        ch.threadDecVideo = new ThreadDecodeVideo2(ch);
                        ch.threadDecVideo.start();
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

                    if (ch.threadSendAudio != null) {
                        ch.threadSendAudio.stopThread();
                        try {
                            ch.threadSendAudio.interrupt();
                            ch.threadSendAudio.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }

                    if (ch.threadDecVideo != null) {
                        ch.threadDecVideo.stopThread();
                        try {
                            ch.threadDecVideo.interrupt();
                            ch.threadDecVideo.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ch.threadDecVideo = null;
                    }


                    if (ch.pollData != null) {
                        ch.pollData.stopThread();
                        try {
                            ch.pollData.interrupt();
                            ch.pollData.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (ch.threadRecvVideo != null) {
                        ch.threadRecvVideo.stopThread();
                        try {
                            ch.threadRecvVideo.interrupt();
                            ch.threadRecvVideo.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ch.threadRecvVideo = null;
                    }

                    ch.VideoFrameQueue.removeAll();
                    Log.i("suntest", "stop show");
                    break;
                }
            }
        }
    }

    private synchronized boolean audioDev_init(int sampleRateInHz, int channel, int dataBit, int codec_id) {

        if (!mInitAudio) {

            int channelConfig = 2;
            int audioFormat = 2;
            int mMinBufSize = 0;

            channelConfig = (channel == AVFrame.AUDIO_CHANNEL_STERO) ? AudioFormat.CHANNEL_CONFIGURATION_STEREO : AudioFormat.CHANNEL_CONFIGURATION_MONO;
            audioFormat = (dataBit == AVFrame.AUDIO_DATABITS_16) ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT;
            Log.i("suntest", String.format("getMinBufferSize(%s,%s,%s)", sampleRateInHz, channelConfig, audioFormat));
            mMinBufSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);

            Log.i("suntest", "mMinBufSize:" + mMinBufSize);
            if (mMinBufSize == AudioTrack.ERROR_BAD_VALUE || mMinBufSize == AudioTrack.ERROR)
                return false;

            try {
                Log.i("suntest", "begin initialize AudioTrack");
                mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz, channelConfig, audioFormat, mMinBufSize, AudioTrack.MODE_STREAM);
                Log.i("IOTCamera", "init AudioTrack with SampleRate:" + sampleRateInHz + " " + ((dataBit == AVFrame.AUDIO_DATABITS_16) ? String.valueOf(16) : String.valueOf(8)) + "bit " + (channel == AVFrame.AUDIO_CHANNEL_STERO ? "Stereo" : "Mono"));
                ;

            } catch (IllegalArgumentException iae) {

                iae.printStackTrace();
                return false; // return----------------------------------------
            }

            if (codec_id == AVFrame.MEDIA_CODEC_AUDIO_SPEEX) {
                DecSpeex.InitDecoder(sampleRateInHz);
            } else if (codec_id == AVFrame.MEDIA_CODEC_AUDIO_MP3) {
                int bit = (dataBit == AVFrame.AUDIO_DATABITS_16) ? 16 : 8;
                DecMp3.InitDecoder(sampleRateInHz, bit);
            } else if (codec_id == AVFrame.MEDIA_CODEC_AUDIO_ADPCM || codec_id == AVFrame.MEDIA_CODEC_AUDIO_PCM) {
                DecADPCM.ResetDecoder();
            } else if (codec_id == AVFrame.MEDIA_CODEC_AUDIO_G726) {
                DecG726.g726_dec_state_create((byte) DecG726.G726_16, (byte) DecG726.FORMAT_LINEAR);
            }
            Log.i("suntest", "play voice");
            //mAudioTrack.setStereoVolume(1.0f, 1.0f);
            mAudioTrack.play();
            mInitAudio = true;

            return true;
        } else
            return false;
    }

    private synchronized void audioDev_stop(int codec_id) {

        if (mInitAudio) {

            if (mAudioTrack != null) {
                mAudioTrack.stop();
                mAudioTrack.release();
                mAudioTrack = null;
            }

            if (codec_id == AVFrame.MEDIA_CODEC_AUDIO_SPEEX) {
                DecSpeex.UninitDecoder();
            } else if (codec_id == AVFrame.MEDIA_CODEC_AUDIO_MP3) {
                DecMp3.UninitDecoder();
            } else if (codec_id == AVFrame.MEDIA_CODEC_AUDIO_G726) {
                DecG726.g726_dec_state_destroy();
            }

            mInitAudio = false;

        }
    }

    private class ThreadConnectDev extends Thread {

        private int mConnType = -1;
        private boolean mIsRunning = false;
        private Object m_waitForStopConnectThread = new Object();
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
                    listener.receiveSessionInfo(Camera.this, CONNECTION_STATE_CONNECTING);
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

                        for (int i = 0; i < mIOTCListeners.size(); i++) {
                            IRegisterIOTCListener listener = mIOTCListeners.get(i);
                            listener.receiveSessionInfo(Camera.this, CONNECTION_STATE_CONNECTED);
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
                        listener.receiveSessionInfo(Camera.this, CONNECTION_STATE_DISCONNECTED);
                        //setLog(Sample_RDTAPIs.MainHandler.MSGTYPE_LOG, str);
                    }
                    break;
                }

                Log.i("IOTCamera", "===ThreadConnectDev exit===");
            }
        }
    }


    private class ThreadRecvVideo2 extends Thread {
        private static final int MAX_BUF_SIZE = 1024 * 20;
        private boolean bIsRunning = false;

        private AVChannel mAVChannel;

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

                    if (System.currentTimeMillis() - lastTimeStamp > 1000) {

                        lastTimeStamp = System.currentTimeMillis();

                        for (int i = 0; i < mIOTCListeners.size(); i++) {

                            IRegisterIOTCListener listener = mIOTCListeners.get(i);
                            listener.receiveFrameInfo(Camera.this, mAVChannel.getChannel(), (mAVChannel.AudioBPS + mAVChannel.VideoBPS) * 8 / 1024, mAVChannel.VideoFPS, nOnlineNumber, nFrmCount, nIncompleteFrmCount);
                        }

                        mAVChannel.VideoFPS = mAVChannel.VideoBPS = mAVChannel.AudioBPS = 0;
                    }
                    //读取RDT
                    //short[] buf1 = new short[]{};
                    int len = RDTAPIs.RDT_Read(nRDT_ID, buf, MAX_BUF_SIZE, 30000);
                    if (len < 0) {
                        for (int i = 0; i < mIOTCListeners.size(); i++) {
                            IRegisterIOTCListener listener = mIOTCListeners.get(i);
                            listener.receiveChannelInfo(Camera.this, mAVChannel.getChannel(), CONNECTION_STATE_DISCONNECTED);
                        }
                        break;
                    }
                    byte[] data = new byte[len];
                    System.arraycopy(buf, 0, data, 0, len);
                    mAVChannel.rdtQueue.Enqueue(len, data);
                    Log.i("IOTCamera", "Enqueue rdtQueue left " + mAVChannel.rdtQueue.getCount());
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

    private class PollData extends Thread {

        private Object m_waitObjForCheckDevStatus1 = new Object();
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

                while (mAVChannel.rdtQueue == null || mAVChannel.rdtQueue.isEmpty()) {
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
                    RDTQueue.RDTData data = mAVChannel.rdtQueue.Dequeue();
                    Log.i("IOTCamera", "Dequeue rdtQueue left " + mAVChannel.rdtQueue.getCount());
                    //将获取的数据放入写入缓存readPool中
                    System.arraycopy(data.getData(), 0, readPool, nReadSize, data.getLength());
                    //缓存中数据的长度
                    nReadSize += data.getLength();//此处size有问题
                    //将数据frame化
                    rdtFrame = new RDTFrame(readPool);
                } catch (Exception e) {
                    //Log.i("suntest", e.getMessage());
                    e.printStackTrace();
                    nReadSize = 0;
                    readPool = new byte[1024 * 30];
                    continue;
                }
                //此帧数据长度如果大于缓存的长度则继续往缓存中写入数据
                if (rdtFrame.mLen > nReadSize) {
                    continue;
                }
                //如果此帧数据长度小于缓存的长度则从缓存中读取数据
                else if (rdtFrame.mLen < nReadSize) {
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
                } else {
                    pFrmNo++;
                    playRDT(mAVChannel, pFrmNo, rdtFrame);
                    nReadSize = 0;
                    readPool = new byte[1024 * 30];
                }
            }
        }
    }

    private void playRDT(AVChannel mAVChannel, int pFrmNo, RDTFrame rdtFrame) {
        byte[] adpcmOutBuf = new byte[640];
        int nCodecId = 0;


        byte[] frameData = new byte[rdtFrame.mLen - 12];
        System.arraycopy(rdtFrame.mContent, 0, frameData, 0, frameData.length);
        if (rdtFrame.mType == 1) {
            //mAVChannel.VideoBPS += rdtFrame.mLen; //outBufSize[0];
            // nFrmCount++;
            AVFrame frame = new AVFrame(pFrmNo, AVFrame.FRM_STATE_COMPLETE, frameData, frameData.length);
            nCodecId = (int) frame.getCodecId();
            if (nCodecId == AVFrame.MEDIA_CODEC_VIDEO_H264) {
                mAVChannel.VideoFrameQueue.addLast(frame);
                Log.i("IOTCamera", "Enqueue AVFrameQueue left " + mAVChannel.VideoFrameQueue.getCount());
            }
        }
        if (rdtFrame.mType == 2) {
            try {
                mAVChannel.AudioBPS += rdtFrame.mLen;
                //AVFrame frame = new AVFrame(pFrmNo, AVFrame.FRM_STATE_COMPLETE, frameData, frameData.length);

//                nCodecId = AVFrame.MEDIA_CODEC_AUDIO_PCM;//AVFrame.MEDIA_CODEC_AUDIO_PCM;
//
//                if (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_ADPCM) {
//                    DecADPCM.Decode(frameData, frameData.length, adpcmOutBuf);
//                    mAudioTrack.write(adpcmOutBuf, 0, 640);
//                } else if (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_PCM) {
                mAudioTrack.write(frameData, 0, frameData.length);
                // }
            } catch (Exception e) {
                e.printStackTrace();
                //Log.i("suntest", "voice error:" + e.getMessage());
            }
        }
    }

    private class ThreadDecodeVideo2 extends Thread {

        static final int MAX_FRAMEBUF = 1280 * 720 * 3;

        private boolean m_bIsRunning = false;

        private AVChannel mAVChannel;

        public ThreadDecodeVideo2(AVChannel channel) {
            mAVChannel = channel;
        }

        public synchronized void stopThread() {
            m_bIsRunning = false;
        }

        @Override
        public void run() {

            //System.gc();

            // int consumed_bytes = 0;
            int avFrameSize = 0;

            AVFrame avFrame = null;

            int videoWidth = 0;
            int videoHeight = 0;

            long firstTimeStampFromDevice = 0;
            long firstTimeStampFromLocal = 0;
            long sleepTime = 0;
            long t1 = 0, t2 = 0;

            long lastFrameTimeStamp = 0;
            long delayTime = 0;

            int[] framePara = new int[4];
            byte[] bufOut = new byte[MAX_FRAMEBUF];
            byte[] bmpBuff = null;
            ByteBuffer bytBuffer = null;
            Bitmap bmp = null;

            int[] out_width = new int[1];
            int[] out_height = new int[1];
            int[] out_size = new int[1];
            boolean bInitH264 = false;
            boolean bInitMpeg4 = false;

            mAVChannel.VideoFPS = 0;
            m_bIsRunning = true;

            //System.gc();

            while (m_bIsRunning) {

                try {
                    if (mAVChannel.VideoFrameQueue.getCount() > 0) {
                        avFrame = mAVChannel.VideoFrameQueue.removeHead();
                        if (avFrame == null)
                            continue;
                        Log.i("IOTCamera", "Dequeue AVFrameQueue left " + mAVChannel.VideoFrameQueue.getCount());
                        avFrameSize = avFrame.getFrmSize();
                    } else {
//                    try {
//                        Thread.sleep(32);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }

//                    continue;
//                        try {
//                            synchronized (mWaitObjectForConnected) {
//                                mWaitObjectForConnected.wait(100);
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                    }

                    while (mAVChannel.VideoFrameQueue.getCount() > 0 && delayTime > 1000) {

                        int skipTime = 0;

                        // drop the first frame, whether it's I or P frame
                        AVFrame tmp = mAVChannel.VideoFrameQueue.removeHead();
                        if (tmp == null) continue;
                        skipTime += (tmp.getTimeStamp() - lastFrameTimeStamp);
                        Log.i("IOTCamera", "low decode performance, drop " + (tmp.isIFrame() ? "I" : "P") + " frame, skip time: " + (tmp.getTimeStamp() - lastFrameTimeStamp) + ", total skip: " + skipTime);
                        lastFrameTimeStamp = tmp.getTimeStamp();

                        // check and drop all the following P frame
                        while (true) {

                            if (!mAVChannel.VideoFrameQueue.isFirstIFrame()) {
                                tmp = mAVChannel.VideoFrameQueue.removeHead();
                                if (tmp == null) break;
                                skipTime += (tmp.getTimeStamp() - lastFrameTimeStamp);
                                Log.i("IOTCamera", "low decode performance, drop " + (tmp.isIFrame() ? "I" : "P") + " frame, skip time: " + (tmp.getTimeStamp() - lastFrameTimeStamp) + ", total skip: " + skipTime);
                                lastFrameTimeStamp = tmp.getTimeStamp();
                            } else break;
                        }

                        delayTime -= skipTime;
                        Log.i("IOTCamera", "delayTime: " + delayTime);
                    }

                    if (avFrameSize > 0 && avFrame != null) {
                        out_size[0] = 0;
                        out_width[0] = 0;
                        out_height[0] = 0;
                        Log.i("IOTCamera", "avFrame.getCodecId()=" + avFrame.getCodecId());
                        if (avFrame.getCodecId() == AVFrame.MEDIA_CODEC_VIDEO_H264) {

                            t1 = System.currentTimeMillis();

                            if (!bInitH264) {
                                DecH264.InitDecoder();
                                bInitH264 = true;
                            } // else {
                            Log.i("IOTCamera", "before decode: " + (avFrame.isIFrame() ? "i" : "p"));
                            // FFMPEG - DecH264.Decode(avFrame.frmData, avFrameSize, bufOut, out_size, out_width, out_height);
                            DecH264.DecoderNal(avFrame.frmData, avFrameSize, framePara, bufOut);
                            Log.i("IOTCamera", "after decode");
                            // }

                        } else if (avFrame.getCodecId() == AVFrame.MEDIA_CODEC_VIDEO_MPEG4) {
                            if (!bInitMpeg4) {

                                int w = ((avFrame.frmData[0x17] & 0x0F) << 9) | ((avFrame.frmData[0x18] & 0xFF) << 1) | ((avFrame.frmData[0x19] & 0x80) >> 7);
                                int h = ((avFrame.frmData[0x19] & 0x3F) << 7) | ((avFrame.frmData[0x1A] & 0xFE) >> 1);

                                DecMpeg4.InitDecoder(w, h);
                                bInitMpeg4 = true;

                            } // else {
                            // Log.i("IOTCamera", "before decode: " + (avFrame.isIFrame() ? "i" : "p"));
                            DecMpeg4.Decode(avFrame.frmData, avFrameSize, bufOut, out_size, out_width, out_height);
                            // Log.i("IOTCamera", "after decode");
                            // }
                        }

                        if (avFrame.getCodecId() == AVFrame.MEDIA_CODEC_VIDEO_H264) {
                            out_width[0] = framePara[2];
                            out_height[0] = framePara[3];
                            out_size[0] = out_width[0] * out_height[0] * 2;
                        }

                        if (out_size[0] > 0 && out_width[0] > 0 && out_height[0] > 0) {

                            if (videoWidth != out_width[0] || videoHeight != out_height[0]) {
                                videoWidth = out_width[0];
                                videoHeight = out_height[0];
                                bmpBuff = new byte[out_size[0]];
                                // bytBuffer = ByteBuffer.wrap(bufOut); // for Android 4.2
                                bmp = Bitmap.createBitmap(videoWidth, videoHeight, android.graphics.Bitmap.Config.RGB_565);
                            }

                            // generate frame
                            if (bmpBuff != null) {
                                System.arraycopy(bufOut, 0, bmpBuff, 0, videoWidth * videoHeight * 2);
                                bytBuffer = ByteBuffer.wrap(bufOut); // for Android 4.2
                                bmp.copyPixelsFromBuffer(bytBuffer);
                                // Log.i("IOTCamera", "generate bitmap");
                            }

                            // ------ calculate sleep time ------
                            if (avFrame != null && firstTimeStampFromDevice != 0 && firstTimeStampFromLocal != 0) {

                                long t = System.currentTimeMillis();
                                t2 = t - t1;

//                                sleepTime = (firstTimeStampFromLocal + (avFrame.getTimeStamp() - firstTimeStampFromDevice)) - t;
//                                delayTime = sleepTime * -1;
                                // Log.i("IOTCamera", "decode time(" + t2 + "); sleep time (" + sleepTime + ") = t0 (" + firstTimeStampFromLocal + ") + (Tn (" + avFrame.getTimeStamp() + ") - T0 (" + firstTimeStampFromDevice + ") " + (avFrame.getTimeStamp() - firstTimeStampFromDevice) + ") - tn' (" + t + ")" );

                                if (sleepTime >= 0) {

                                    // sometimes, the time interval from device will large than 1 second, must reset the base timestamp
                                    if ((avFrame.getTimeStamp() - lastFrameTimeStamp) > 1000) {
                                        firstTimeStampFromDevice = avFrame.getTimeStamp();
                                        firstTimeStampFromLocal = t;
                                        Log.i("IOTCamera", "RESET base timestamp");

                                        if (sleepTime > 1000) sleepTime = 33;
                                    }

                                    if (sleepTime > 1000) sleepTime = 1000;
                                    try {
                                        Thread.sleep(sleepTime);
                                    } catch (Exception e) {

                                    }
                                }

                                lastFrameTimeStamp = avFrame.getTimeStamp();
                            }

                            if (firstTimeStampFromDevice == 0 || firstTimeStampFromLocal == 0) {
                                firstTimeStampFromDevice = lastFrameTimeStamp = avFrame.getTimeStamp();
                                firstTimeStampFromLocal = System.currentTimeMillis();
                            }

                            // -- end calculate sleep time --

                            mAVChannel.VideoFPS++;

                            synchronized (mIOTCListeners) {
                                for (int i = 0; i < mIOTCListeners.size(); i++) {

                                    IRegisterIOTCListener listener = mIOTCListeners.get(i);
                                    listener.receiveFrameData(Camera.this, mAVChannel.getChannel(), bmp);
                                }
                            }
                            mAVChannel.LastFrame = bmp;
                            Log.i("IOTCamera", String.format("decoded width=%s and height=%s", bmp.getWidth(), bmp.getHeight()));

                        } else {
                            // Log.i("IOTCamera", "decoded size, width and height = 0");
                        }
                    }

                    if (avFrame != null) {
                        avFrame.frmData = null;
                        avFrame = null;
                        System.gc();
                    }
                } catch (Exception e) {
                    Log.i("IOTCamera", e.getMessage());
                    e.printStackTrace();
                }
            }
            if (bInitH264)
                DecH264.UninitDecoder();

            if (bInitMpeg4)
                DecMpeg4.UninitDecoder();

            bufOut = null;
            bmpBuff = null;

//            if (bmp != null) {
//                bmp.recycle();
//                bmp = null;
//            }

            //System.gc();

            Log.i("IOTCamera", "===ThreadDecodeVideo exit===");
        }
    }


    private class ThreadSendAudio extends Thread {

        private boolean m_bIsRunning = false;
        private static final int SAMPLE_RATE_IN_HZ = 16000;
        private int avIndexForSendAudio = -1;
        private int chIndexForSendAudio = -1;
        private AVChannel mAVChannel = null;
        private Object m_waitObjForCheckDevStatus = new Object();


        public ThreadSendAudio(AVChannel ch) {
            mAVChannel = ch;
        }

        public synchronized void stopThread() {
//            synchronized (m_waitObjForCheckDevStatus) {
//                m_waitObjForCheckDevStatus.notify();
//            }
            Log.i("suntest", "停止音频发送线程");
            m_bIsRunning = false;
        }

        @Override
        public void run() {
            super.run();
            m_bIsRunning = true;
            int nMinBufSize = 0, playBufSize = 0;
            int nReadBytes = 0;

            while (mSID < 0 || nRDT_ID < 0) {
                try {

                    synchronized (m_waitObjForCheckDevStatus) {
                        m_waitObjForCheckDevStatus.wait(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            nMinBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            //}

			/* init mic of phone */
            AudioRecord recorder = null;
            AudioProcess mAudioProcess = null;
            //AudioTrack audioTrack = null;

            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, nMinBufSize);
            mAudioProcess = new AudioProcess();
            //playBufSize = AudioTrack.getMinBufferSize(SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
            //audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE_IN_HZ, AudioFormat.ENCODING_PCM_16BIT, AudioFormat.CHANNEL_CONFIGURATION_DEFAULT, playBufSize, AudioTrack.MODE_STREAM);
            mAudioProcess.init(SAMPLE_RATE_IN_HZ, AudioFormat.ENCODING_PCM_8BIT, AudioFormat.CHANNEL_CONFIGURATION_MONO);
            recorder.startRecording();
            //audioTrack.play();

			/* send audio data continuously */
            while (m_bIsRunning) {
                int bufferSize = mAudioProcess.calculateBufferSize(SAMPLE_RATE_IN_HZ, AudioFormat.ENCODING_PCM_8BIT, AudioFormat.CHANNEL_CONFIGURATION_MONO);
                byte[] inPCMBuf = new byte[bufferSize];
                nReadBytes = recorder.read(inPCMBuf, 0, inPCMBuf.length);
                byte d = (byte) (nReadBytes + 12 & 0x000000ff);
                byte c = (byte) ((nReadBytes + 12 & 0x0000ff00) >> 8);
                byte b = (byte) ((nReadBytes + 12 & 0x00ff0000) >> 16);
                byte a = (byte) ((nReadBytes + 12 & 0xff000000) >> 24);
                if (nReadBytes > 0) {
                    byte[] head = new byte[]{36, 83, 88, 38, a, b, c, d, 2, 0, 0, 0};
                    byte[] tmpBuf_src = new byte[nReadBytes + 12];
                    System.arraycopy(inPCMBuf, 0, tmpBuf_src, 12, nReadBytes); // setp
                    System.arraycopy(head, 0, tmpBuf_src, 0, 12);
                    //byte[] tmpBuf_processed =  mAudioProcess.processCircle(tmpBuf_src,nReadBytes + 12,3);
                    RDTAPIs.RDT_Write(nRDT_ID, tmpBuf_src, nReadBytes + 12);
                }
            }
            //audioTrack.stop();
            recorder.stop();
            /* uninit speaker of phone */
//            if (recorder != null) {
//                recorder.stop();
//                recorder.release();
            //mAudioProcess.destroy();
            //recorder = null;
            //           }

			/* close connection */
//            if (avIndexForSendAudio >= 0) {
//                AVAPIs.avServStop(avIndexForSendAudio);
//            }
//
//            if (chIndexForSendAudio >= 0) {
//                IOTCAPIs.IOTC_Session_Channel_OFF(mSID, chIndexForSendAudio);
//            }

            avIndexForSendAudio = -1;
            chIndexForSendAudio = -1;

            Log.i("IOTCamera", "===ThreadSendAudio exit===");
        }
    }


    private class AVChannel {

        private volatile int mChannel = -1;
        private volatile int mAVIndex = -1;
        private long mServiceType = 0xFFFFFFFF;
        private String mViewAcc;
        private String mViewPwd;
        private int mAudioCodec;

        public IOCtrlQueue IOCtrlQueue;
        public RDTQueue rdtQueue;
        public AVFrameQueue VideoFrameQueue;
        public AVFrameQueue AudioFrameQueue;

        public Bitmap LastFrame;

        public int VideoFPS;
        public int VideoBPS;
        public int AudioBPS;

        public AVChannel(int channel, String view_acc, String view_pwd) {
            mChannel = channel;
            mViewAcc = view_acc;
            mViewPwd = view_pwd;
            mServiceType = 0xFFFFFFFF;

            VideoFPS = VideoBPS = AudioBPS = 0;

            LastFrame = null;

            IOCtrlQueue = new IOCtrlQueue();
            rdtQueue = new RDTQueue();
            VideoFrameQueue = new AVFrameQueue();
            AudioFrameQueue = new AVFrameQueue();
        }

        public int getChannel() {
            return mChannel;
        }

        public synchronized int getAVIndex() {
            return mAVIndex;
        }

        public synchronized void setAVIndex(int idx) {
            mAVIndex = idx;
        }

        public synchronized long getServiceType() {
            return mServiceType;
        }

        public synchronized int getAudioCodec() {
            return mAudioCodec;
        }

        public synchronized void setAudioCodec(int codec) {
            mAudioCodec = codec;
        }

        public synchronized void setServiceType(long serviceType) {
            mServiceType = serviceType;
            //mAudioCodec = (serviceType & 4096) == 0 ? AVFrame.MEDIA_CODEC_AUDIO_SPEEX : AVFrame.MEDIA_CODEC_AUDIO_ADPCM;
        }

        public String getViewAcc() {
            return mViewAcc;
        }

        public String getViewPwd() {
            return mViewPwd;
        }

        public ThreadRecvVideo2 threadRecvVideo = null;
        public PollData pollData = null;
        public ThreadSendAudio threadSendAudio = null;
        public ThreadDecodeVideo2 threadDecVideo = null;
    }

    private class IOCtrlQueue {

        public class IOCtrlSet {

            public int IOCtrlType;
            public byte[] IOCtrlBuf;

            public IOCtrlSet(int avIndex, int type, byte[] buf) {
                IOCtrlType = type;
                IOCtrlBuf = buf;
            }

            public IOCtrlSet(int type, byte[] buf) {
                IOCtrlType = type;
                IOCtrlBuf = buf;
            }
        }

        LinkedList<IOCtrlSet> listData = new LinkedList<IOCtrlSet>();

        public synchronized boolean isEmpty() {
            return listData.isEmpty();
        }

        public synchronized void Enqueue(int type, byte[] data) {
            listData.addLast(new IOCtrlSet(type, data));
        }

        public synchronized void Enqueue(int avIndex, int type, byte[] data) {
            listData.addLast(new IOCtrlSet(avIndex, type, data));
        }

        public synchronized IOCtrlSet Dequeue() {

            return listData.isEmpty() ? null : listData.removeFirst();
        }

        public synchronized void removeAll() {
            if (!listData.isEmpty())
                listData.clear();
        }

    }

    private class RDTQueue {

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

            private int length = 0;
            private byte[] data = new byte[1024 * 20];

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

}