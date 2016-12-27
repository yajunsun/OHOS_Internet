package com.tutk.RDT;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.encoder.util.EncADPCM;
import com.encoder.util.EncG726;
import com.encoder.util.EncSpeex;
import com.tutk.RDT.AVIOCTRLDEFs.SMsgAVIoctrlAVStream;

import net.iwebrtc.audioprocess.sdk.AudioProcess;

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
    private ThreadCheckDevStatus mThreadChkDevStatus = null;
    private ThreadSendAudio mThreadSendAudio = null;

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

    /****
     * RDT相关
     ***/
    boolean m_bStoped = true;

    /*******/

    public Camera() {
        mDevUID = "";
        mDevPwd = "";
    }

    public int getSessionMode() {
        return mSessionMode;
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

//    public synchronized static St_LanSearchResp[] SearchLAN() {
//
//        int num[] = new int[1];
//        St_LanSearchResp[] result = null;
//
//        result = IOTCAPIs.SA(num, 0xFD86AA1C);
//
//        return result;
//    }

    public static void setMaxCameraLimit(int limit) {
        mDefaultMaxCameraLimit = limit;
    }

    public synchronized static int init() {
        int nRet = 0;

        if (mCameraCount == 0) {
            int port = (int) (10000 + (System.currentTimeMillis() % 10000));

            // nRet = IOTCAPIs.ialize(port, "50.19.254.134", "122.248.234.207", "m4.iotcplatform.com", "m5.iotcplatform.com");
            nRet = IOTCAPIs.IOTC_Initialize2(port);

            Log.i("IOTCamera", "IOTC_Initialize2() returns " + nRet);

            if (nRet < 0) {
                return nRet;
            }

            nRet = AVAPIs.avInitialize(mDefaultMaxCameraLimit * 16);
            Log.i("IOTCamera", "avInitialize() = " + nRet);

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
                nRet = AVAPIs.avDeInitialize();
                Log.i("IOTCamera", "avDeInitialize() returns " + nRet);
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

    public void connect(String uid) {

        mDevUID = uid;

        if (mThreadConnectDev == null) {
            mThreadConnectDev = new ThreadConnectDev(0);
            mThreadConnectDev.start();
        }

        if (mThreadChkDevStatus == null) {
            mThreadChkDevStatus = new ThreadCheckDevStatus();
            mThreadChkDevStatus.start();
        }
    }

    public void connect(String uid, String pwd) {

        mDevUID = uid;
        mDevPwd = pwd;

        if (mThreadConnectDev == null) {
            mThreadConnectDev = new ThreadConnectDev(1);
            mThreadConnectDev.start();
        }

        if (mThreadChkDevStatus == null) {
            mThreadChkDevStatus = new ThreadCheckDevStatus();
            mThreadChkDevStatus.start();
        }
    }

    public void disconnect() {

        synchronized (mAVChannels) {

            for (AVChannel ch : mAVChannels) {

                stopSpeaking(ch.getChannel());

                if (ch.threadStartDev != null)
                    ch.threadStartDev.stopThread();

                if (ch.threadDecAudio != null)
                    ch.threadDecAudio.stopThread();

                if (ch.threadDecVideo != null)
                    ch.threadDecVideo.stopThread();

                if (ch.threadRecvAudio != null)
                    ch.threadRecvAudio.stopThread();

                if (ch.threadRecvVideo != null)
                    ch.threadRecvVideo.stopThread();

                if (ch.threadRecvIOCtrl != null)
                    ch.threadRecvIOCtrl.stopThread();

                if (ch.threadSendIOCtrl != null)
                    ch.threadSendIOCtrl.stopThread();

                if (ch.threadRecvVideo != null) {
                    try {
                        ch.threadRecvVideo.interrupt();
                        ch.threadRecvVideo.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ch.threadRecvVideo = null;
                }

                if (ch.threadRecvAudio != null) {
                    try {
                        ch.threadRecvAudio.interrupt();
                        ch.threadRecvAudio.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ch.threadRecvAudio = null;
                }

                if (ch.threadDecAudio != null) {
                    try {
                        ch.threadDecAudio.interrupt();
                        ch.threadDecAudio.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ch.threadDecAudio = null;
                }

                if (ch.threadDecVideo != null) {
                    try {
                        ch.threadDecVideo.interrupt();
                        ch.threadDecVideo.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ch.threadDecVideo = null;
                }

                if (ch.threadRecvIOCtrl != null) {
                    try {
                        ch.threadRecvIOCtrl.interrupt();
                        ch.threadRecvIOCtrl.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ch.threadRecvIOCtrl = null;
                }

                if (ch.threadSendIOCtrl != null) {
                    try {
                        ch.threadSendIOCtrl.interrupt();
                        ch.threadSendIOCtrl.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ch.threadSendIOCtrl = null;
                }

                if (ch.threadStartDev != null && ch.threadStartDev.isAlive()) {
                    try {
                        ch.threadStartDev.interrupt();
                        ch.threadStartDev.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                ch.threadStartDev = null;


                ch.AudioFrameQueue.removeAll();
                ch.AudioFrameQueue = null;

                ch.VideoFrameQueue.removeAll();
                ch.VideoFrameQueue = null;

                ch.IOCtrlQueue.removeAll();
                ch.IOCtrlQueue = null;

                if (ch.getAVIndex() >= 0) {

                    AVAPIs.avClientStop(ch.getAVIndex());
                    Log.i("IOTCamera", "avClientStop(avIndex = " + ch.getAVIndex() + ")");
                }
            }
        }

        mAVChannels.clear();

        synchronized (mWaitObjectForConnected) {
            mWaitObjectForConnected.notify();
        }

        if (mThreadChkDevStatus != null)
            mThreadChkDevStatus.stopThread();

        if (mThreadConnectDev != null)
            mThreadConnectDev.stopThread();

        if (mThreadChkDevStatus != null) {
            try {
                mThreadChkDevStatus.interrupt();
                mThreadChkDevStatus.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mThreadChkDevStatus = null;
        }

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

        mSessionMode = -1;
    }

//    public void start(int avChannel, String viewAccount, String viewPasswd) {
//
//        AVChannel session = null;
//
//        synchronized (mAVChannels) {
//            for (AVChannel ch : mAVChannels) {
//                if (ch.getChannel() == avChannel) {
//                    session = ch;
//                    break;
//                }
//            }
//        }
//
//        if (session == null) {
//
//            AVChannel ch = new AVChannel(avChannel, viewAccount, viewPasswd);
//            mAVChannels.add(ch);
//
//            ch.threadStartDev = new ThreadStartDev(ch);
//            ch.threadStartDev.start();
//
//            ch.threadRecvIOCtrl = new ThreadRecvIOCtrl(ch);
//            ch.threadRecvIOCtrl.start();
//
//            ch.threadSendIOCtrl = new ThreadSendIOCtrl(ch);
//            ch.threadSendIOCtrl.start();
//
//        } else {
//
//            if (session.threadStartDev == null) {
//                session.threadStartDev = new ThreadStartDev(session);
//                session.threadStartDev.start();
//            }
//
//            if (session.threadRecvIOCtrl == null) {
//                session.threadRecvIOCtrl = new ThreadRecvIOCtrl(session);
//                session.threadRecvIOCtrl.start();
//            }
//
//            if (session.threadSendIOCtrl == null) {
//                session.threadSendIOCtrl = new ThreadSendIOCtrl(session);
//                session.threadSendIOCtrl.start();
//            }
//        }
//    }

    public void stop(int avChannel) {

        synchronized (mAVChannels) {

            int idx = -1;

            for (int i = 0; i < mAVChannels.size(); i++) {

                AVChannel ch = mAVChannels.get(i);

                if (ch.getChannel() == avChannel) {

                    idx = i;

                    stopSpeaking(ch.getChannel());

                    if (ch.threadStartDev != null)
                        ch.threadStartDev.stopThread();

                    if (ch.threadDecAudio != null)
                        ch.threadDecAudio.stopThread();

                    if (ch.threadDecVideo != null)
                        ch.threadDecVideo.stopThread();

                    if (ch.threadRecvAudio != null)
                        ch.threadRecvAudio.stopThread();

                    if (ch.threadRecvVideo != null)
                        ch.threadRecvVideo.stopThread();

                    if (ch.threadRecvIOCtrl != null)
                        ch.threadRecvIOCtrl.stopThread();

                    if (ch.threadSendIOCtrl != null)
                        ch.threadSendIOCtrl.stopThread();

                    if (ch.threadRecvVideo != null) {
                        try {
                            ch.threadRecvVideo.interrupt();
                            ch.threadRecvVideo.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ch.threadRecvVideo = null;
                    }

                    if (ch.threadRecvAudio != null) {
                        try {
                            ch.threadRecvAudio.interrupt();
                            ch.threadRecvAudio.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ch.threadRecvAudio = null;
                    }

                    if (ch.threadDecAudio != null) {
                        try {
                            ch.threadDecAudio.interrupt();
                            ch.threadDecAudio.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ch.threadDecAudio = null;
                    }

                    if (ch.threadDecVideo != null) {
                        try {
                            ch.threadDecVideo.interrupt();
                            ch.threadDecVideo.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ch.threadDecVideo = null;
                    }

                    if (ch.threadRecvIOCtrl != null) {
                        try {
                            ch.threadRecvIOCtrl.interrupt();
                            ch.threadRecvIOCtrl.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ch.threadRecvIOCtrl = null;
                    }

                    if (ch.threadSendIOCtrl != null) {
                        try {
                            ch.threadSendIOCtrl.interrupt();
                            ch.threadSendIOCtrl.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ch.threadSendIOCtrl = null;
                    }

                    if (ch.threadStartDev != null && ch.threadStartDev.isAlive()) {
                        try {
                            ch.threadStartDev.interrupt();
                            ch.threadStartDev.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    ch.threadStartDev = null;


                    ch.AudioFrameQueue.removeAll();
                    ch.AudioFrameQueue = null;

                    ch.VideoFrameQueue.removeAll();
                    ch.VideoFrameQueue = null;

                    ch.IOCtrlQueue.removeAll();
                    ch.IOCtrlQueue = null;

                    if (ch.getAVIndex() >= 0) {

                        AVAPIs.avClientStop(ch.getAVIndex());
                        Log.i("IOTCamera", "avClientStop(avIndex = " + ch.getAVIndex() + ")");
                    }

                    break;
                }
            }

            if (idx >= 0) {
                mAVChannels.remove(idx);
            }
        }
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

        synchronized (mAVChannels) {

            for (int i = 0; i < mAVChannels.size(); i++) {

                AVChannel ch = mAVChannels.get(i);

                if (ch.getChannel() == avChannel) {

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

                    ch.VideoFrameQueue.removeAll();

                    break;
                }
            }
        }
    }

    public void startSpeaking(int avChannel) {

        synchronized (mAVChannels) {

            for (int i = 0; i < mAVChannels.size(); i++) {

                AVChannel ch = mAVChannels.get(i);

                if (ch.getChannel() == avChannel) {

                    ch.AudioFrameQueue.removeAll();

                    if (mThreadSendAudio == null) {
                        mThreadSendAudio = new ThreadSendAudio(ch);
                        mThreadSendAudio.start();
                    }

                    break;
                }
            }
        }
    }

    public void stopSpeaking(int avChannel) {

        if (mThreadSendAudio != null) {
            mThreadSendAudio.stopThread();

            try {
                mThreadSendAudio.interrupt();
                mThreadSendAudio.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mThreadSendAudio = null;
        }
    }

    public void startListening(int avChannel) {

        synchronized (mAVChannels) {

            for (int i = 0; i < mAVChannels.size(); i++) {

                AVChannel ch = mAVChannels.get(i);

                if (avChannel == ch.getChannel()) {

                    ch.AudioFrameQueue.removeAll();

                    if (ch.threadRecvAudio == null) {
                        ch.threadRecvAudio = new ThreadRecvAudio(ch);
                        ch.threadRecvAudio.start();
                    }

					/*
                    if (ch.threadDecAudio == null) {
						ch.threadDecAudio = new ThreadDecodeAudio(ch);
						ch.threadDecAudio.start();
					}
					*/

                    break;
                }
            }
        }
    }

    public void stopListening(int avChannel) {

        synchronized (mAVChannels) {

            for (int i = 0; i < mAVChannels.size(); i++) {

                AVChannel ch = mAVChannels.get(i);

                if (avChannel == ch.getChannel()) {

                    if (ch.threadRecvAudio != null) {
                        ch.threadRecvAudio.stopThread();
                        try {
                            ch.threadRecvAudio.interrupt();
                            ch.threadRecvAudio.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ch.threadRecvAudio = null;
                    }

                    if (ch.threadDecAudio != null) {
                        ch.threadDecAudio.stopThread();
                        try {
                            ch.threadDecAudio.interrupt();
                            ch.threadDecAudio.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ch.threadDecAudio = null;
                    }

                    ch.AudioFrameQueue.removeAll();

                    break;
                }
            }
        }
    }

    public Bitmap Snapshot(int avChannel) {

        Bitmap result = null;

        synchronized (mAVChannels) {

            for (int i = 0; i < mAVChannels.size(); i++) {

                AVChannel ch = mAVChannels.get(i);

                if (avChannel == ch.getChannel()) {
                    result = ch.LastFrame;
                    break;
                }
            }
        }

        return result;
    }

    private synchronized boolean audioDev_init(int sampleRateInHz, int channel, int dataBit, int codec_id) {

        if (!mInitAudio) {

            int channelConfig = 2;
            int audioFormat = 2;
            int mMinBufSize = 0;

            channelConfig = (channel == AVFrame.AUDIO_CHANNEL_STERO) ? AudioFormat.CHANNEL_CONFIGURATION_STEREO : AudioFormat.CHANNEL_CONFIGURATION_MONO;
            audioFormat = (dataBit == AVFrame.AUDIO_DATABITS_16) ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT;
            mMinBufSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);

            if (mMinBufSize == AudioTrack.ERROR_BAD_VALUE || mMinBufSize == AudioTrack.ERROR)
                return false;

            try {

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

            mAudioTrack.setStereoVolume(1.0f, 1.0f);
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

        public ThreadConnectDev(int connType) {
            mConnType = connType;
        }

        public void stopThread() {

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
                } else if (mConnType == 1) {
                    mSID = IOTCAPIs.IOTC_Connect_ByUID2(mDevUID, mDevPwd, 2);
                    Log.i("IOTCamera", "IOTC_Connect_ByUID2(" + mDevUID + ", " + mDevPwd + ") returns " + mSID);
                } else {
                    return;
                }

                if (mSID >= 0) {

                    int nRDT_ID = RDTAPIs.RDT_Create(mSID, 3000, 0);
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
                } else if (mSID == IOTCAPIs.IOTC_ER_CONNECT_IS_CALLING) {

                    try {
                        synchronized (m_waitForStopConnectThread) {
                            m_waitForStopConnectThread.wait(1000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else if (mSID == IOTCAPIs.IOTC_ER_UNKNOWN_DEVICE || mSID == IOTCAPIs.IOTC_ER_UNLICENSE || mSID == IOTCAPIs.IOTC_ER_CAN_NOT_FIND_DEVICE || mSID == IOTCAPIs.IOTC_ER_TIMEOUT) {

                    if (mSID != IOTCAPIs.IOTC_ER_TIMEOUT) {
                        // synchronized (mIOTCListeners) {
                        for (int i = 0; i < mIOTCListeners.size(); i++) {

                            IRegisterIOTCListener listener = mIOTCListeners.get(i);
                            listener.receiveSessionInfo(Camera.this, CONNECTION_STATE_UNKNOWN_DEVICE);
                        }
                        // }
                    }

                    nRetryForIOTC_Conn++;

                    try {
                        long sleepTime = nRetryForIOTC_Conn > 60 ? 60000 : nRetryForIOTC_Conn * 1000;
                        synchronized (m_waitForStopConnectThread) {
                            m_waitForStopConnectThread.wait(sleepTime);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else if (mSID == IOTCAPIs.IOTC_ER_DEVICE_NOT_SECURE_MODE ||
                        mSID == IOTCAPIs.IOTC_ER_DEVICE_SECURE_MODE) {

                    // synchronized (mIOTCListeners) {
                    for (int i = 0; i < mIOTCListeners.size(); i++) {

                        IRegisterIOTCListener listener = mIOTCListeners.get(i);
                        listener.receiveSessionInfo(Camera.this, CONNECTION_STATE_UNSUPPORTED);
                    }
                    // }

                    break;
                } else {

                    // synchronized (mIOTCListeners) {
                    for (int i = 0; i < mIOTCListeners.size(); i++) {

                        IRegisterIOTCListener listener = mIOTCListeners.get(i);
                        listener.receiveSessionInfo(Camera.this, CONNECTION_STATE_CONNECT_FAILED);
                    }
                    // }

                    break;
                }
            }

            Log.i("IOTCamera", "===ThreadConnectDev exit===");
        }
    }

    private class ThreadStartDev extends Thread {

        private boolean mIsRunning = false;
        private AVChannel mAVChannel;
        private Object mWaitObject = new Object();

        public ThreadStartDev(AVChannel channel) {
            mAVChannel = channel;
        }

        public void stopThread() {

            mIsRunning = false;

            if (mSID >= 0) {
                Log.i("IOTCamera", "avClientExit(" + mSID + ", " + mAVChannel.getChannel() + ")");
                AVAPIs.avClientExit(mSID, mAVChannel.getChannel());
            }

            synchronized (mWaitObject) {
                mWaitObject.notify();
            }
        }

        @Override
        public void run() {

            mIsRunning = true;
            // int nRetryForAVClientStart = 0;
            int avIndex = -1;

            while (mIsRunning) {

                if (mSID < 0) {

                    try {
                        synchronized (mWaitObjectForConnected) {
                            mWaitObjectForConnected.wait(100);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    continue;
                }

                // synchronized (mIOTCListeners) {
                for (int i = 0; i < mIOTCListeners.size(); i++) {

                    IRegisterIOTCListener listener = mIOTCListeners.get(i);
                    listener.receiveChannelInfo(Camera.this, mAVChannel.getChannel(), CONNECTION_STATE_CONNECTING);
                }
                // }

                long[] nServType = new long[1];
                nServType[0] = -1;
                // Log.i("IOTCamera", "avClientStart(" + mSID + ")");
                avIndex = AVAPIs.avClientStart(mSID, mAVChannel.getViewAcc(), mAVChannel.getViewPwd(), 30, nServType, mAVChannel.getChannel());
                Log.i("IOTCamera", "avClientStart(" + mAVChannel.getChannel() + ", " + mAVChannel.getViewAcc() + ", " + mAVChannel.getViewPwd() + ") in Session(" + mSID + ") returns " + avIndex);
                long servType = nServType[0];

                if (avIndex >= 0) {

                    mAVChannel.setAVIndex(avIndex);
                    mAVChannel.setServiceType(servType);

                    for (int i = 0; i < mIOTCListeners.size(); i++) {

                        IRegisterIOTCListener listener = mIOTCListeners.get(i);
                        listener.receiveChannelInfo(Camera.this, mAVChannel.getChannel(), CONNECTION_STATE_CONNECTED);
                    }

                    break;

                } else if (avIndex == AVAPIs.AV_ER_REMOTE_TIMEOUT_DISCONNECT || avIndex == AVAPIs.AV_ER_TIMEOUT) {

                    for (int i = 0; i < mIOTCListeners.size(); i++) {

                        IRegisterIOTCListener listener = mIOTCListeners.get(i);
                        listener.receiveChannelInfo(Camera.this, mAVChannel.getChannel(), CONNECTION_STATE_TIMEOUT);
                    }

                } /*else if (avIndex == AVAPIs.AV_ER_SESSION_CLOSE_BY_REMOTE || avIndex == AVAPIs.AV_ER_INVALID_SID) {

					synchronized (mIOTCListeners) {
						for (int i = 0; i < mIOTCListeners.size(); i++) {

							IRegisterIOTCListener listener = mIOTCListeners.get(i);
							listener.receiveChannelInfo(Camera.this, mAVChannel.getChannel(), STS_CHANGE_CHANNEL_STATUS_CONNECT_FAILED);
						}
					}

					break;
					
				}*/ else if (avIndex == AVAPIs.AV_ER_WRONG_VIEWACCorPWD) {

                    for (int i = 0; i < mIOTCListeners.size(); i++) {

                        IRegisterIOTCListener listener = mIOTCListeners.get(i);
                        listener.receiveChannelInfo(Camera.this, mAVChannel.getChannel(), CONNECTION_STATE_WRONG_PASSWORD);
                    }

                    break;

                } else {

                    try {
                        synchronized (mWaitObject) {
                            mWaitObject.wait(1000);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // mAVChannel.threadStartDev = null;

            Log.i("IOTCamera", "===ThreadStartDev exit===");
        }
    }

    private class ThreadCheckDevStatus extends Thread {

        private boolean m_bIsRunning = false;
        private Object m_waitObjForCheckDevStatus = new Object();

        public void stopThread() {

            m_bIsRunning = false;

            synchronized (m_waitObjForCheckDevStatus) {
                m_waitObjForCheckDevStatus.notify();
            }
        }

        @Override
        public void run() {
            super.run();

            m_bIsRunning = true;
            St_SInfo stSInfo = new St_SInfo();
            int ret = 0;

            while (m_bIsRunning && mSID < 0) {

                try {

                    synchronized (mWaitObjectForConnected) {
                        mWaitObjectForConnected.wait(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            while (m_bIsRunning) {

                if (mSID >= 0) {

                    ret = IOTCAPIs.IOTC_Session_Check(mSID, stSInfo);

                    if (ret >= 0) {

                        if (mSessionMode != stSInfo.Mode) {

                            mSessionMode = stSInfo.Mode;

							/*
							synchronized (mIOTCListeners) {
								for (int i = 0; i < mIOTCListeners.size(); i++) {

									IRegisterIOTCListener listener = mIOTCListeners.get(i);
									listener.receiveSessionInfo(Camera.this, STS_CHANGE_SESSION_MODE);
								}
							}
							*/
                        }
                    } else if (ret == IOTCAPIs.IOTC_ER_REMOTE_TIMEOUT_DISCONNECT || ret == IOTCAPIs.IOTC_ER_TIMEOUT) {

                        Log.i("IOTCamera", "IOTC_Session_Check(" + mSID + ") timeout");

                        // synchronized (mIOTCListeners) {
                        for (int i = 0; i < mIOTCListeners.size(); i++) {

                            IRegisterIOTCListener listener = mIOTCListeners.get(i);
                            listener.receiveSessionInfo(Camera.this, CONNECTION_STATE_TIMEOUT);
                        }
                        // }

                    } else {

                        Log.i("IOTCamera", "IOTC_Session_Check(" + mSID + ") Failed return " + ret);

                        // synchronized (mIOTCListeners) {
                        for (int i = 0; i < mIOTCListeners.size(); i++) {

                            IRegisterIOTCListener listener = mIOTCListeners.get(i);
                            listener.receiveSessionInfo(Camera.this, CONNECTION_STATE_CONNECT_FAILED);
                        }
                        // }
                    }
                }

                synchronized (m_waitObjForCheckDevStatus) {
                    try {
                        m_waitObjForCheckDevStatus.wait(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            Log.i("IOTCamera", "===ThreadCheckDevStatus exit===");
        }
    }

    private class ThreadRecvVideo extends Thread {
        private static final int MAX_BUF_SIZE = 1280 * 720 * 3;
        private boolean bIsRunning = false;
        private AVChannel mAVChannel;

        public ThreadRecvVideo(AVChannel channel) {
            mAVChannel = channel;
        }

        public void stopThread() {
            bIsRunning = false;
        }

        @Override
        public void run() {

            System.gc();

            bIsRunning = true;

            while (bIsRunning && (mSID < 0 || mAVChannel.getAVIndex() < 0)) {
                try {
                    synchronized (mWaitObjectForConnected) {
                        mWaitObjectForConnected.wait(100);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            mAVChannel.VideoBPS = 0;

            byte[] recvBuf = new byte[MAX_BUF_SIZE];
            byte[] bytAVFrame = new byte[AVFrame.FRAMEINFO_SIZE];

            int[] pFrmNo = new int[1];
            int nCodecId = 0;
            int nReadSize = 0;
            int nFrmCount = 0;
            int nIncompleteFrmCount = 0;
            int nOnlineNumber = 0;
            long nPrevFrmNo = 0x0FFFFFFF;
            long lastTimeStamp = System.currentTimeMillis();

            if (bIsRunning && mSID >= 0 && mAVChannel.getAVIndex() >= 0) {
                mAVChannel.IOCtrlQueue.Enqueue(mAVChannel.getAVIndex(), AVIOCTRLDEFs.IOTYPE_USER_IPCAM_START, Packet.intToByteArray_Little(mCamIndex));
            }
			
			/* write frame data */
			/*
			File rootFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Output/");
			
			if (!rootFolder.exists()) {
				try {
					rootFolder.mkdir();
				} catch (Exception e) {
					
				}
			} else {
				File[] files = rootFolder.listFiles();
				for (File file : files) {
					file.delete();
				}
			}
			*/
			/* -- */

            while (bIsRunning) {

                if (mSID >= 0 && mAVChannel.getAVIndex() >= 0) {

                    if (System.currentTimeMillis() - lastTimeStamp > 1000) {

                        lastTimeStamp = System.currentTimeMillis();

                        for (int i = 0; i < mIOTCListeners.size(); i++) {

                            IRegisterIOTCListener listener = mIOTCListeners.get(i);
                            listener.receiveFrameInfo(Camera.this, mAVChannel.getChannel(), (mAVChannel.AudioBPS + mAVChannel.VideoBPS) * 8 / 1024, mAVChannel.VideoFPS, nOnlineNumber, nFrmCount, nIncompleteFrmCount);
                        }

                        mAVChannel.VideoFPS = mAVChannel.VideoBPS = mAVChannel.AudioBPS = 0;
                    }

                    nReadSize = AVAPIs.avRecvFrameData(mAVChannel.getAVIndex(), recvBuf, recvBuf.length, bytAVFrame, AVFrame.FRAMEINFO_SIZE, pFrmNo);

                    if (nReadSize >= 0) {

                        // Log.i("IOTCamera", "avRecvFrameData(" + mSID + ") = " + nReadSize + ", " + Camera.getHex(recvBuf, nReadSize));

						/* write frame data to file */
						/*
						try {
							
							String fileName = rootFolder.getAbsolutePath() + File.separator + String.format("%05d", nFrmCount) + ".bin"; 
							
							java.io.BufferedOutputStream bos = new java.io.BufferedOutputStream(new FileOutputStream(fileName));
							bos.write(recvBuf, 0, nReadSize);
							bos.flush();
							bos.close();
						} catch (Exception e) {
							
						}
						*/
						/* - end - */

                        mAVChannel.VideoBPS += nReadSize;
                        nFrmCount++;

                        byte[] frameData = new byte[nReadSize];
                        System.arraycopy(recvBuf, 0, frameData, 0, nReadSize);

                        AVFrame frame = new AVFrame(pFrmNo[0], AVFrame.FRM_STATE_COMPLETE, bytAVFrame, frameData, nReadSize);

                        nCodecId = (int) frame.getCodecId();
                        nOnlineNumber = (int) frame.getOnlineNum();

                        if (frame.isIFrame() || pFrmNo[0] == (nPrevFrmNo + 1)) {

                            nPrevFrmNo = pFrmNo[0];
                            // Log.i("IOTCamera", nFrmNo + " - avRecvFrameData(" + mSID + ") = " + nReadSize);

                            if (nCodecId == AVFrame.MEDIA_CODEC_VIDEO_H264) {

                                mAVChannel.VideoFrameQueue.addLast(frame);

                            } else if (nCodecId == AVFrame.MEDIA_CODEC_VIDEO_MPEG4) {

                                mAVChannel.VideoFrameQueue.addLast(frame);

                            } else if (nCodecId == AVFrame.MEDIA_CODEC_VIDEO_MJPEG) {

                                Bitmap bmp = BitmapFactory.decodeByteArray(frameData, 0, nReadSize);

                                if (bmp != null) {

                                    mAVChannel.VideoFPS++;

                                    // synchronized (mIOTCListeners) {
                                    for (int i = 0; i < mIOTCListeners.size(); i++) {

                                        IRegisterIOTCListener listener = mIOTCListeners.get(i);
                                        listener.receiveFrameData(Camera.this, mAVChannel.getChannel(), bmp);
                                    }
                                    // }

                                    mAVChannel.LastFrame = bmp;
                                }

                                try {
                                    Thread.sleep(33);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    } else if (nReadSize == AVAPIs.AV_ER_SESSION_CLOSE_BY_REMOTE) {

                        Log.i("IOTCamera", "AV_ER_SESSION_CLOSE_BY_REMOTE");
                        continue;

                    } else if (nReadSize == AVAPIs.AV_ER_REMOTE_TIMEOUT_DISCONNECT) {

                        Log.i("IOTCamera", "AV_ER_REMOTE_TIMEOUT_DISCONNECT");
                        continue;

                    } else if (nReadSize == AVAPIs.AV_ER_DATA_NOREADY) {

						/*
						try {
							Thread.sleep(30);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						*/
                        //Log.i("IOTCamera", "AV_ER_DATA_NOREADY");
                        continue;

                    } else if (nReadSize == AVAPIs.AV_ER_BUFPARA_MAXSIZE_INSUFF) {

                        continue;

                    } else if (nReadSize == AVAPIs.AV_ER_MEM_INSUFF) {

                        nFrmCount++;
                        nIncompleteFrmCount++;
                        Log.i("IOTCamera", "AV_ER_MEM_INSUFF");

                    } else if (nReadSize == AVAPIs.AV_ER_LOSED_THIS_FRAME) {

                        Log.i("IOTCamera", "AV_ER_LOSED_THIS_FRAME");
                        //mAVChannel.VideoFrameQueue.addLast(new AVFrame(pFrmNo[0], AVFrame.FRM_STATE_LOSED, bytAVFrame, null, 0));

                        nFrmCount++;
                        nIncompleteFrmCount++;

                    } else if (nReadSize == AVAPIs.AV_ER_INCOMPLETE_FRAME) {

                        Log.i("IOTCamera", "AV_ER_INCOMPLETE_FRAME");
                        //mAVChannel.VideoFrameQueue.addLast(new AVFrame(pFrmNo[0], AVFrame.FRM_STATE_INCOMPLETE, bytAVFrame, null, 0));

                        nFrmCount++;
                        nIncompleteFrmCount++;

                    }
					/*
					else {

						try {
							Thread.sleep(33);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					*/
                }

            }// while--end


            mAVChannel.IOCtrlQueue.Enqueue(mAVChannel.getAVIndex(), AVIOCTRLDEFs.IOTYPE_USER_IPCAM_STOP, Packet.intToByteArray_Little(mCamIndex));
            mAVChannel.VideoFrameQueue.removeAll();

            recvBuf = null;

            Log.i("IOTCamera", "===ThreadRecvVideo exit===");
        }
    }

    private class ThreadRecvVideo2 extends Thread {
        private static final int MAX_BUF_SIZE = 1280 * 720 * 3;
        private boolean bIsRunning = false;
        private AVChannel mAVChannel;

        public ThreadRecvVideo2(AVChannel channel) {
            mAVChannel = channel;
        }

        public void stopThread() {
            bIsRunning = false;
        }

        @Override
        public void run() {

            System.gc();

            bIsRunning = true;

            while (bIsRunning && (mSID < 0 || mAVChannel.getAVIndex() < 0)) {
                try {
                    synchronized (mWaitObjectForConnected) {
                        mWaitObjectForConnected.wait(100);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            mAVChannel.VideoBPS = 0;

            byte[] buf = new byte[MAX_BUF_SIZE];
            byte[] pFrmInfoBuf = new byte[AVFrame.FRAMEINFO_SIZE];

            int[] pFrmNo = new int[1];
            int nCodecId = 0;
            int nReadSize = 0;
            int nFrmCount = 0;
            int nIncompleteFrmCount = 0;
            int nOnlineNumber = 0;
            long nPrevFrmNo = 0x0FFFFFFF;
            long lastTimeStamp = System.currentTimeMillis();

            int[] outBufSize = new int[1];
            int[] outFrmSize = new int[1];
            int[] outFrmInfoBufSize = new int[1];

            if (bIsRunning && mSID >= 0 && mAVChannel.getAVIndex() >= 0) {
                mAVChannel.IOCtrlQueue.Enqueue(mAVChannel.getAVIndex(), AVIOCTRLDEFs.IOTYPE_USER_IPCAM_START, Packet.intToByteArray_Little(mCamIndex));
            }

            mAVChannel.AudioFrameQueue.removeAll();
            if (mSID >= 0 && mAVChannel.getAVIndex() >= 0)
                AVAPIs.avClientCleanBuf(mAVChannel.getAVIndex());


            while (bIsRunning) {

                if (mSID >= 0 && mAVChannel.getAVIndex() >= 0) {

                    if (System.currentTimeMillis() - lastTimeStamp > 1000) {

                        lastTimeStamp = System.currentTimeMillis();

                        for (int i = 0; i < mIOTCListeners.size(); i++) {

                            IRegisterIOTCListener listener = mIOTCListeners.get(i);
                            listener.receiveFrameInfo(Camera.this, mAVChannel.getChannel(), (mAVChannel.AudioBPS + mAVChannel.VideoBPS) * 8 / 1024, mAVChannel.VideoFPS, nOnlineNumber, nFrmCount, nIncompleteFrmCount);
                        }

                        mAVChannel.VideoFPS = mAVChannel.VideoBPS = mAVChannel.AudioBPS = 0;
                    }

                    //nReadSize = AVAPIs.avRecvFrameData2(mAVChannel.getAVIndex(), buf, buf.length, outBufSize, outFrmSize, pFrmInfoBuf, pFrmInfoBuf.length, outFrmInfoBufSize, pFrmNo);
                    // nReadSize = AVAPIs.AV_ER_DATA_NOREADY;
                    int ret = RDTAPIs.RDT_Write(nRDT_ID, buf, buf.length);
                    nReadSize = RDTAPIs.RDT_Read(nRDT_ID, buf, buf.length, 3000000);
                    outFrmSize[0]=nReadSize;
                    outFrmInfoBufSize[0]=nReadSize;
                    if (nReadSize >= 0) {

                        mAVChannel.VideoBPS += outBufSize[0];
                        nFrmCount++;

                        byte[] frameData = new byte[nReadSize];
                        System.arraycopy(buf, 0, frameData, 0, nReadSize);

                        AVFrame frame = new AVFrame(pFrmNo[0], AVFrame.FRM_STATE_COMPLETE, frameData, nReadSize);

                        nCodecId = (int) frame.getCodecId();
                        nOnlineNumber = (int) frame.getOnlineNum();

                        if (nCodecId == AVFrame.MEDIA_CODEC_VIDEO_H264) {

                            pFrmNo[0]=(int)(nPrevFrmNo+1);
                            if (frame.isIFrame() || pFrmNo[0] == (nPrevFrmNo + 1)) {
                                nPrevFrmNo = pFrmNo[0];
                                mAVChannel.VideoFrameQueue.addLast(frame);
                            } else {
                                Log.i("IOTCamera", "Incorrect frame no(" + pFrmNo[0] + "), prev:" + nPrevFrmNo + " -> drop frame");
                            }

                        } else if (nCodecId == AVFrame.MEDIA_CODEC_VIDEO_MPEG4) {

                            if (frame.isIFrame() || pFrmNo[0] == (nPrevFrmNo + 1)) {
                                nPrevFrmNo = pFrmNo[0];
                                mAVChannel.VideoFrameQueue.addLast(frame);
                            }

                        } else if (nCodecId == AVFrame.MEDIA_CODEC_VIDEO_MJPEG) {

                            Bitmap bmp = BitmapFactory.decodeByteArray(frameData, 0, nReadSize);

                            if (bmp != null) {

                                mAVChannel.VideoFPS++;

                                for (int i = 0; i < mIOTCListeners.size(); i++) {

                                    IRegisterIOTCListener listener = mIOTCListeners.get(i);
                                    listener.receiveFrameData(Camera.this, mAVChannel.getChannel(), bmp);
                                }

                                mAVChannel.LastFrame = bmp;
                            }

                            try {
                                Thread.sleep(32);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }


                    } else if (nReadSize == AVAPIs.AV_ER_SESSION_CLOSE_BY_REMOTE) {

                        Log.i("IOTCamera", "AV_ER_SESSION_CLOSE_BY_REMOTE");
                        continue;

                    } else if (nReadSize == AVAPIs.AV_ER_REMOTE_TIMEOUT_DISCONNECT) {

                        Log.i("IOTCamera", "AV_ER_REMOTE_TIMEOUT_DISCONNECT");
                        continue;

                    } else if (nReadSize == AVAPIs.AV_ER_DATA_NOREADY) {

                        try {
                            Thread.sleep(32);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        //Log.i("IOTCamera", "AV_ER_DATA_NOREADY");
                        continue;

                    } else if (nReadSize == AVAPIs.AV_ER_BUFPARA_MAXSIZE_INSUFF) {

                        continue;

                    } else if (nReadSize == AVAPIs.AV_ER_MEM_INSUFF) {

                        nFrmCount++;
                        nIncompleteFrmCount++;
                        Log.i("IOTCamera", "AV_ER_MEM_INSUFF");

                    } else if (nReadSize == AVAPIs.AV_ER_LOSED_THIS_FRAME) {

                        Log.i("IOTCamera", "AV_ER_LOSED_THIS_FRAME");

                        nFrmCount++;
                        nIncompleteFrmCount++;

                    } else if (nReadSize == AVAPIs.AV_ER_INCOMPLETE_FRAME) {

                        nFrmCount++;

                        mAVChannel.VideoBPS += outBufSize[0];

                        if (outFrmInfoBufSize[0] == 0 || (outFrmSize[0] * 0.9) > outBufSize[0] || (int) pFrmInfoBuf[2] == AVFrame.IPC_FRAME_FLAG_PBFRAME) {
                            nIncompleteFrmCount++;
                            Log.i("IOTCamera", ((int) pFrmInfoBuf[2] == AVFrame.IPC_FRAME_FLAG_PBFRAME ? "P" : "I") + " frame, outFrmSize(" + outFrmSize[0] + ") * 0.9 = " + ((outFrmSize[0] * 0.9)) + " > outBufSize(" + outBufSize[0] + ")");
                            continue;
                        }

                        byte[] frameData = new byte[outFrmSize[0]];
                        System.arraycopy(buf, 0, frameData, 0, outFrmSize[0]);
                        nCodecId = Packet.byteArrayToShort_Little(pFrmInfoBuf, 0);

                        if (nCodecId == AVFrame.MEDIA_CODEC_VIDEO_MJPEG) {

                            nIncompleteFrmCount++;
                            continue;
							
							/*
							Bitmap bmp = BitmapFactory.decodeByteArray(frameData, 0, outFrmSize[0]);

							if (bmp != null) {

								mAVChannel.VideoFPS++;

								for (int i = 0; i < mIOTCListeners.size(); i++) {

									IRegisterIOTCListener listener = mIOTCListeners.get(i);
									listener.receiveFrameData(Camera.this, mAVChannel.getChannel(), bmp);
								}
									
								mAVChannel.LastFrame = bmp;
							}
							
							Log.i("IOTCamera", "AV_ER_INCOMPLETE_FRAME - MJPEG");
							*/

                        } else if (nCodecId == AVFrame.MEDIA_CODEC_VIDEO_MPEG4 || nCodecId == AVFrame.MEDIA_CODEC_VIDEO_H264) {

                            AVFrame frame = new AVFrame(pFrmNo[0], AVFrame.FRM_STATE_COMPLETE, pFrmInfoBuf, frameData, outFrmSize[0]);

                            if (frame.isIFrame() || pFrmNo[0] == (nPrevFrmNo + 1)) {
                                nPrevFrmNo = pFrmNo[0];
                                mAVChannel.VideoFrameQueue.addLast(frame);
                                Log.i("IOTCamera", "AV_ER_INCOMPLETE_FRAME - H264 or MPEG4");
                            } else {
                                nIncompleteFrmCount++;
                                Log.i("IOTCamera", "AV_ER_INCOMPLETE_FRAME - H264 or MPEG4 - LOST");
                            }

                        } else {
                            nIncompleteFrmCount++;
                        }
                    }
                }

            }// while--end


            mAVChannel.VideoFrameQueue.removeAll();
            if (mSID >= 0 && mAVChannel.getAVIndex() >= 0) {
                mAVChannel.IOCtrlQueue.Enqueue(mAVChannel.getAVIndex(), AVIOCTRLDEFs.IOTYPE_USER_IPCAM_STOP, Packet.intToByteArray_Little(mCamIndex));
                AVAPIs.avClientCleanBuf(mAVChannel.getAVIndex());
            }

            buf = null;

            Log.i("IOTCamera", "===ThreadRecvVideo exit===");
        }
    }

    private class ThreadDecodeVideo extends Thread {

        static final int MAX_FRAMEBUF = 1280 * 720 * 3;

        private boolean m_bIsRunning = false;

        private AVChannel mAVChannel;

        public ThreadDecodeVideo(AVChannel channel) {
            mAVChannel = channel;
        }

        public void stopThread() {
            m_bIsRunning = false;
        }

        @Override
        public void run() {

            System.gc();

            // int consumed_bytes = 0;
            int avFrameSize = 0;

            AVFrame avFrame = null;

            int videoWidth = 0;
            int videoHeight = 0;

            int delayFrameCount = 0;

            boolean bWaitI = false;

            long firstTimeStampFromDevice = 0;
            long firstTimeStampFromLocal = 0;
            long t = 0, Rt = 0;
            long sleepTime = 0;
            long t1 = 0, t2 = 0;

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

            System.gc();

            // DecH264 decH264 = new DecH264();
            // decH264.InitDecoder();

            while (m_bIsRunning) {

                if (mAVChannel.VideoFrameQueue.getCount() > 0) {

                    avFrame = mAVChannel.VideoFrameQueue.removeHead();

                    if (avFrame == null)
                        continue;

                    avFrameSize = avFrame.getFrmSize();

					/*
					if (avFrame.getFrmState() != AVFrame.FRM_STATE_COMPLETE)
						bWaitI = true;

					if (bWaitI) {

						if (avFrame.isIFrame() && avFrame.getFrmState() == AVFrame.FRM_STATE_COMPLETE) {
							bWaitI = false;
						} else {
							avFrame = null;
							avFrameSize = 0;
							Log.i("IOTCamera", "frame is not complete and frame is p frame");
						}
					}
					*/

                } else {

                    // Log.i("IOTCamera", "there is no data in video queue");

                    try {
                        Thread.sleep(4);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    continue;
                }

                if ((avFrame != null && !avFrame.isIFrame()) && delayFrameCount >= 30) {
                    avFrame.frmData = null;
                    avFrameSize = 0;
                    avFrame = null;
                    Log.i("IOTCamera", "(avFrame != null && avFrame == pFrame && delayFrameCount = " + delayFrameCount);
                    continue;
                }

                if (avFrameSize > 0) {

                    out_size[0] = 0;
                    out_width[0] = 0;
                    out_height[0] = 0;

                    if (avFrame.getCodecId() == AVFrame.MEDIA_CODEC_VIDEO_H264) {

                        t1 = System.currentTimeMillis();

                        if (!bInitH264) {
                            DecH264.InitDecoder();
                            bInitH264 = true;
                        } else {
                            // Log.i("IOTCamera", "before decode: " + (avFrame.isIFrame() ? "i" : "p"));
                            // FFMPEG - DecH264.Decode(avFrame.frmData, avFrameSize, bufOut, out_size, out_width, out_height);
                            DecH264.DecoderNal(avFrame.frmData, avFrameSize, framePara, bufOut);
                            // Log.i("IOTCamera", "after decode");
                        }

                    } else if (avFrame.getCodecId() == AVFrame.MEDIA_CODEC_VIDEO_MPEG4) {

                        if (!bInitMpeg4) {

                            int w = ((avFrame.frmData[0x17] & 0x0F) << 9) | ((avFrame.frmData[0x18] & 0xFF) << 1) | ((avFrame.frmData[0x19] & 0x80) >> 7);
                            int h = ((avFrame.frmData[0x19] & 0x3F) << 7) | ((avFrame.frmData[0x1A] & 0xFE) >> 1);

                            DecMpeg4.InitDecoder(w, h);
                            bInitMpeg4 = true;

                        } else {
                            // Log.i("IOTCamera", "before decode: " + (avFrame.isIFrame() ? "i" : "p"));
                            DecMpeg4.Decode(avFrame.frmData, avFrameSize, bufOut, out_size, out_width, out_height);
                            // Log.i("IOTCamera", "after decode");
                        }
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

                        // calc fps
                        mAVChannel.VideoFPS++;

                        // ------ calculate sleep time ------
                        if (avFrame != null && firstTimeStampFromDevice != 0 && firstTimeStampFromLocal != 0) {

                            t = System.currentTimeMillis();
                            t2 = t - t1;

                            sleepTime = (firstTimeStampFromLocal + (avFrame.getTimeStamp() - firstTimeStampFromDevice)) - t - Rt;
                            Log.i("IOTCamera", "decode time(" + t2 + "); sleep time (" + sleepTime + ") = t0 (" + firstTimeStampFromLocal + ") + (Tn (" + avFrame.getTimeStamp() + ") - T0 (" + firstTimeStampFromDevice + ") " + (avFrame.getTimeStamp() - firstTimeStampFromDevice) + ") - tn' (" + t + ") - Rt (" + Rt + ")");

                            if (sleepTime >= 0) {

                                try {
                                    Thread.sleep(sleepTime);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                firstTimeStampFromLocal -= mAVChannel.VideoFrameQueue.getCount();

                                delayFrameCount = 0;
                                Rt = 0;

                            } else if (sleepTime < 0 && sleepTime > -33) {

                                firstTimeStampFromDevice = avFrame.getTimeStamp();
                                firstTimeStampFromLocal = t;

                                //Log.i("IOTCamera", "sleep time (" + sleepTime + ") = t0 (" + firstTimeStampFromLocal + ") + (Tn (" + avFrame.getTimeStamp() + ") - T0 (" + firstTimeStampFromDevice + ") " + (avFrame.getTimeStamp() - firstTimeStampFromDevice) + ") - tn' (" + t + ") - Rt (" + Rt + ")" );

                            } else {

                                firstTimeStampFromDevice = avFrame.getTimeStamp();
                                firstTimeStampFromLocal = t;
                                delayFrameCount++;

                                //Log.i("IOTCamera", "sleep time (" + sleepTime + ") = t0 (" + firstTimeStampFromLocal + ") + (Tn (" + avFrame.getTimeStamp() + ") - T0 (" + firstTimeStampFromDevice + ") " + (avFrame.getTimeStamp() - firstTimeStampFromDevice) + ") - tn' (" + t + ") - Rt (" + Rt + ")" );
                            }
                        }

                        if (firstTimeStampFromDevice == 0 || firstTimeStampFromLocal == 0) {
                            firstTimeStampFromDevice = avFrame.getTimeStamp();
                            firstTimeStampFromLocal = System.currentTimeMillis();
                        }

                        // -- end calculate sleep time --

                        t = System.currentTimeMillis();


                        synchronized (mIOTCListeners) {
                            for (int i = 0; i < mIOTCListeners.size(); i++) {

                                IRegisterIOTCListener listener = mIOTCListeners.get(i);
                                listener.receiveFrameData(Camera.this, mAVChannel.getChannel(), bmp);
                            }
                        }


                        mAVChannel.LastFrame = bmp;
                        Rt += System.currentTimeMillis() - t;

                    } else {
                        // Log.i("IOTCamera", "decoded size, width and height = 0");
                    }
                }

                if (avFrame != null) {
                    avFrame.frmData = null;
                    avFrame = null;
                    //System.gc();
                }
            }

            if (bInitH264)
                DecH264.UninitDecoder();

            if (bInitMpeg4)
                DecMpeg4.UninitDecoder();

            bufOut = null;
            bmpBuff = null;

            if (bmp != null) {
                bmp.recycle();
                bmp = null;
            }

            System.gc();

            Log.i("IOTCamera", "===ThreadDecodeVideo exit===");
        }
    }

    private class ThreadDecodeVideo2 extends Thread {

        static final int MAX_FRAMEBUF = 1280 * 720 * 3;

        private boolean m_bIsRunning = false;

        private AVChannel mAVChannel;

        public ThreadDecodeVideo2(AVChannel channel) {
            mAVChannel = channel;
        }

        public void stopThread() {
            m_bIsRunning = false;
        }

        @Override
        public void run() {

            System.gc();

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

            System.gc();

            while (m_bIsRunning) {

                if (mAVChannel.VideoFrameQueue.getCount() > 0) {

                    avFrame = mAVChannel.VideoFrameQueue.removeHead();

                    if (avFrame == null)
                        continue;

                    avFrameSize = avFrame.getFrmSize();

                } else {

                    try {
                        Thread.sleep(32);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    continue;
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

                if (avFrameSize > 0) {

                    out_size[0] = 0;
                    out_width[0] = 0;
                    out_height[0] = 0;

                    if (avFrame.getCodecId() == AVFrame.MEDIA_CODEC_VIDEO_H264) {

                        t1 = System.currentTimeMillis();

                        if (!bInitH264) {
                            DecH264.InitDecoder();
                            bInitH264 = true;
                        } // else {
                        // Log.i("IOTCamera", "before decode: " + (avFrame.isIFrame() ? "i" : "p"));
                        // FFMPEG - DecH264.Decode(avFrame.frmData, avFrameSize, bufOut, out_size, out_width, out_height);
                        DecH264.DecoderNal(avFrame.frmData, avFrameSize, framePara, bufOut);
                        // Log.i("IOTCamera", "after decode");
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

                            sleepTime = (firstTimeStampFromLocal + (avFrame.getTimeStamp() - firstTimeStampFromDevice)) - t;
                            delayTime = sleepTime * -1;
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

                    } else {
                        // Log.i("IOTCamera", "decoded size, width and height = 0");
                    }
                }

                if (avFrame != null) {
                    avFrame.frmData = null;
                    avFrame = null;
                    // System.gc();
                }
            }

            if (bInitH264)
                DecH264.UninitDecoder();

            if (bInitMpeg4)
                DecMpeg4.UninitDecoder();

            bufOut = null;
            bmpBuff = null;

            if (bmp != null) {
                bmp.recycle();
                bmp = null;
            }

            System.gc();

            Log.i("IOTCamera", "===ThreadDecodeVideo exit===");
        }
    }

    private class ThreadRecvAudio extends Thread {

        private final int MAX_BUF_SIZE = 1280;
        private int nReadSize = 0;
        private boolean bIsRunning = false;

        private AVChannel mAVChannel;

        public ThreadRecvAudio(AVChannel channel) {
            mAVChannel = channel;
        }

        public void stopThread() {
            bIsRunning = false;
        }

        @Override
        public void run() {

            bIsRunning = true;

            while (bIsRunning && (mSID < 0 || mAVChannel.getAVIndex() < 0)) {

                try {
                    synchronized (mWaitObjectForConnected) {
                        mWaitObjectForConnected.wait(100);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            mAVChannel.AudioBPS = 0;
            byte[] recvBuf = new byte[MAX_BUF_SIZE];
            byte[] bytAVFrame = new byte[AVFrame.FRAMEINFO_SIZE];
            int[] pFrmNo = new int[1];


            byte[] mp3OutBuf = new byte[65535];
            short[] speexOutBuf = new short[160];
            byte[] adpcmOutBuf = new byte[640];
            byte[] G726OutBuf = new byte[2048];
            long[] G726OutBufLen = new long[1];


            boolean bFirst = true;
            boolean bInitAudio = false;

            int nSamplerate = 44100;
            int nDatabits = 1;
            int nChannel = 1;
            int nCodecId = 0;
            int nFPS = 0;

            if (bIsRunning && mSID >= 0 && mAVChannel.getAVIndex() >= 0)
                mAVChannel.IOCtrlQueue.Enqueue(mAVChannel.getAVIndex(), AVIOCTRLDEFs.IOTYPE_USER_IPCAM_AUDIOSTART, Packet.intToByteArray_Little(mCamIndex));

            while (bIsRunning) {

                if (mSID >= 0 && mAVChannel.getAVIndex() >= 0) {

					/*
					int nBufCnt = AVAPIs.avCheckAudioBuf(mAVChannel.getAVIndex());

					if (nBufCnt < nFPS) {

						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						continue;
					}
					*/

                    nReadSize = AVAPIs.avRecvAudioData(mAVChannel.getAVIndex(), recvBuf, recvBuf.length, bytAVFrame, AVFrame.FRAMEINFO_SIZE, pFrmNo);

                    if (nReadSize < 0 && nReadSize != AVAPIs.AV_ER_DATA_NOREADY)
                        Log.i("IOTCamera", "avRecvAudioData < 0");

                    if (nReadSize > 0) {

                        // Log.i("IOTCamera", "avRecvAudioData(" + mSID + ") = " + nReadSize);

                        mAVChannel.AudioBPS += nReadSize;

                        byte[] frameData = new byte[nReadSize];
                        System.arraycopy(recvBuf, 0, frameData, 0, nReadSize);

                        AVFrame frame = new AVFrame(pFrmNo[0], AVFrame.FRM_STATE_COMPLETE, bytAVFrame, frameData, nReadSize);

                        nCodecId = (int) frame.getCodecId();

                        // mAVChannel.AudioFrameQueue.addLast(frame);

                        if (bFirst) {

                            if (!mInitAudio && (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_MP3 || nCodecId == AVFrame.MEDIA_CODEC_AUDIO_SPEEX || nCodecId == AVFrame.MEDIA_CODEC_AUDIO_ADPCM || nCodecId == AVFrame.MEDIA_CODEC_AUDIO_PCM || nCodecId == AVFrame.MEDIA_CODEC_AUDIO_G726)) {

                                bFirst = false;

                                nSamplerate = AVFrame.getSamplerate(frame.getFlags());
                                nDatabits = (int) (frame.getFlags() & 0x02);
                                nDatabits = (nDatabits == 0x02) ? 1 : 0;
                                nChannel = (int) (frame.getFlags() & 0x01);

                                if (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_SPEEX)
                                    nFPS = ((nSamplerate * (nChannel == AVFrame.AUDIO_CHANNEL_MONO ? 1 : 2) * (nDatabits == AVFrame.AUDIO_DATABITS_8 ? 8 : 16)) / 8) / 160;
                                else if (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_ADPCM)
                                    nFPS = ((nSamplerate * (nChannel == AVFrame.AUDIO_CHANNEL_MONO ? 1 : 2) * (nDatabits == AVFrame.AUDIO_DATABITS_8 ? 8 : 16)) / 8) / 640;
                                else if (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_PCM)
                                    nFPS = ((nSamplerate * (nChannel == AVFrame.AUDIO_CHANNEL_MONO ? 1 : 2) * (nDatabits == AVFrame.AUDIO_DATABITS_8 ? 8 : 16)) / 8) / frame.getFrmSize();

                                bInitAudio = audioDev_init(nSamplerate, nChannel, nDatabits, nCodecId);

                                if (!bInitAudio)
                                    break;

                            }
                        }


                        if (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_SPEEX) {
                            DecSpeex.Decode(recvBuf, nReadSize, speexOutBuf);
                            mAudioTrack.write(speexOutBuf, 0, 160);
                        } else if (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_MP3) {
                            int len = DecMp3.Decode(recvBuf, nReadSize, mp3OutBuf);
                            mAudioTrack.write(mp3OutBuf, 0, len);
                            nFPS = ((nSamplerate * (nChannel == AVFrame.AUDIO_CHANNEL_MONO ? 1 : 2) * (nDatabits == AVFrame.AUDIO_DATABITS_8 ? 8 : 16)) / 8) / len;
                        } else if (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_ADPCM) {
                            DecADPCM.Decode(recvBuf, nReadSize, adpcmOutBuf);
                            mAudioTrack.write(adpcmOutBuf, 0, 640);
                        } else if (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_PCM) {
                            mAudioTrack.write(recvBuf, 0, nReadSize);
                        } else if (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_G726) {
                            DecG726.g726_decode(recvBuf, nReadSize, G726OutBuf, G726OutBufLen);
                            Log.i("IOTCamera", "G726 decode size:" + G726OutBufLen[0]);
                            mAudioTrack.write(G726OutBuf, 0, (int) G726OutBufLen[0]);
                            nFPS = ((nSamplerate * (nChannel == AVFrame.AUDIO_CHANNEL_MONO ? 1 : 2) * (nDatabits == AVFrame.AUDIO_DATABITS_8 ? 8 : 16)) / 8) / (int) G726OutBufLen[0];
                        }
						
						/*
						try {
							Thread.sleep(1000 / nFPS);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						*/

                    } else if (nReadSize == AVAPIs.AV_ER_DATA_NOREADY) {
                        // Log.i("IOTCamera", "avRecvAudioData returns AV_ER_DATA_NOREADY");
                        try {
                            Thread.sleep(nFPS == 0 ? 33 : (1000 / nFPS));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    } else if (nReadSize == AVAPIs.AV_ER_LOSED_THIS_FRAME) {
                        Log.i("IOTCamera", "avRecvAudioData returns AV_ER_LOSED_THIS_FRAME");
                    } else {
                        try {
                            Thread.sleep(nFPS == 0 ? 33 : (1000 / nFPS));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.i("IOTCamera", "avRecvAudioData returns " + nReadSize);
                    }
                }
            } // while(true);


            if (bInitAudio)
                audioDev_stop(nCodecId);


            mAVChannel.IOCtrlQueue.Enqueue(mAVChannel.getAVIndex(), AVIOCTRLDEFs.IOTYPE_USER_IPCAM_AUDIOSTOP, Packet.intToByteArray_Little(mCamIndex));

            Log.i("IOTCamera", "===ThreadRecvAudio exit===");
        }
    }

    private class ThreadDecodeAudio extends Thread {

        private boolean mStopedDecodeAudio = false;

        private AVChannel mAVChannel;

        public ThreadDecodeAudio(AVChannel channel) {
            mAVChannel = channel;
        }

        public void stopThread() {
            mStopedDecodeAudio = false;
        }

        @Override
        public void run() {

            byte[] mp3OutBuf = new byte[65535];
            short[] speexOutBuf = new short[160];
            byte[] adpcmOutBuf = new byte[640];
            byte[] G726OutBuf = new byte[2048];
            long[] G726OutBufLen = new long[1];

            boolean bFirst = true;
            boolean bInitAudio = false;

            int nCodecId = -1;
            int nSamplerate = -1;
            int nDatabits = -1;
            int nChannel = -1;

            int nFPS = 0;

            long firstTimeStampFromDevice = 0;
            long firstTimeStampFromLocal = 0;
            long sleepTime = 0;

            mStopedDecodeAudio = true;

            while (mStopedDecodeAudio) {

                if (mAVChannel.AudioFrameQueue.getCount() > 0) {

                    AVFrame frame = mAVChannel.AudioFrameQueue.removeHead();
                    nCodecId = frame.getCodecId();

                    if (bFirst) {

                        if (!mInitAudio && (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_MP3 || nCodecId == AVFrame.MEDIA_CODEC_AUDIO_SPEEX || nCodecId == AVFrame.MEDIA_CODEC_AUDIO_ADPCM || nCodecId == AVFrame.MEDIA_CODEC_AUDIO_PCM || nCodecId == AVFrame.MEDIA_CODEC_AUDIO_G726)) {

                            bFirst = false;

                            nSamplerate = AVFrame.getSamplerate(frame.getFlags());
                            nDatabits = (int) (frame.getFlags() & 0x02);
                            nDatabits = (nDatabits == 0x02) ? 1 : 0;
                            nChannel = (int) (frame.getFlags() & 0x01);

                            bInitAudio = audioDev_init(nSamplerate, nChannel, nDatabits, nCodecId);

                            if (!bInitAudio)
                                break;
                        }
                    }

                    if (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_SPEEX) {
                        DecSpeex.Decode(frame.frmData, frame.getFrmSize(), speexOutBuf);
                        mAudioTrack.write(speexOutBuf, 0, 160);
                        nFPS = ((nSamplerate * (nChannel == AVFrame.AUDIO_CHANNEL_MONO ? 1 : 2) * (nDatabits == AVFrame.AUDIO_DATABITS_8 ? 8 : 16)) / 8) / 160;
                    } else if (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_MP3) {
                        int len = DecMp3.Decode(frame.frmData, frame.getFrmSize(), mp3OutBuf);
                        mAudioTrack.write(mp3OutBuf, 0, len);
                        nFPS = ((nSamplerate * (nChannel == AVFrame.AUDIO_CHANNEL_MONO ? 1 : 2) * (nDatabits == AVFrame.AUDIO_DATABITS_8 ? 8 : 16)) / 8) / len;
                    } else if (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_ADPCM) {
                        DecADPCM.Decode(frame.frmData, frame.getFrmSize(), adpcmOutBuf);
                        mAudioTrack.write(adpcmOutBuf, 0, 640);
                        nFPS = ((nSamplerate * (nChannel == AVFrame.AUDIO_CHANNEL_MONO ? 1 : 2) * (nDatabits == AVFrame.AUDIO_DATABITS_8 ? 8 : 16)) / 8) / 640;
                    } else if (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_PCM) {
                        mAudioTrack.write(frame.frmData, 0, frame.getFrmSize());
                        nFPS = ((nSamplerate * (nChannel == AVFrame.AUDIO_CHANNEL_MONO ? 1 : 2) * (nDatabits == AVFrame.AUDIO_DATABITS_8 ? 8 : 16)) / 8) / frame.getFrmSize();
                    } else if (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_G726) {
                        DecG726.g726_decode(frame.frmData, frame.getFrmSize(), G726OutBuf, G726OutBufLen);
                        Log.i("IOTCamera", "G726 decode size:" + G726OutBufLen[0]);
                        mAudioTrack.write(G726OutBuf, 0, (int) G726OutBufLen[0]);
                        nFPS = ((nSamplerate * (nChannel == AVFrame.AUDIO_CHANNEL_MONO ? 1 : 2) * (nDatabits == AVFrame.AUDIO_DATABITS_8 ? 8 : 16)) / 8) / (int) G726OutBufLen[0];
                    }

                    try {
                        Thread.sleep(1000 / nFPS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else {
                    try {
                        Thread.sleep(4);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (bInitAudio) {
                audioDev_stop(nCodecId);
            }

            Log.i("IOTCamera", "===ThreadDecodeAudio exit===");
        }
    }

    private class ThreadSendAudio extends Thread {

        private boolean m_bIsRunning = false;
        private static final int SAMPLE_RATE_IN_HZ = 16000;
        private int avIndexForSendAudio = -1;
        private int chIndexForSendAudio = -1;
        private AVChannel mAVChannel = null;

        public ThreadSendAudio(AVChannel ch) {
            mAVChannel = ch;
        }

        public void stopThread() {

            if (mSID >= 0 && chIndexForSendAudio >= 0) {
                AVAPIs.avServExit(mSID, chIndexForSendAudio);
                sendIOCtrl(mAVChannel.mChannel, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SPEAKERSTOP, SMsgAVIoctrlAVStream.parseContent(chIndexForSendAudio));
            }

            m_bIsRunning = false;
        }

        @Override
        public void run() {
            super.run();

            if (mSID < 0) {
                Log.i("IOTCamera", "=== ThreadSendAudio exit because SID < 0 ===");
                return;
            }

            m_bIsRunning = true;

            boolean bInitSpeexEnc = false;
            boolean bInitG726Enc = false;
            boolean bInitADPCM = false;
            boolean bInitPCM = false;

            int nMinBufSize = 0, playBufSize = 0;
            int nReadBytes = 0;

			/* wait for connection */
            chIndexForSendAudio = IOTCAPIs.IOTC_Session_Get_Free_Channel(mSID);

            if (chIndexForSendAudio < 0) {
                Log.i("IOTCamera", "=== ThreadSendAudio exit becuase no more channel for connection ===");
                return;
            }

            sendIOCtrl(mAVChannel.mChannel, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SPEAKERSTART, SMsgAVIoctrlAVStream.parseContent(chIndexForSendAudio));

            Log.i("IOTCamera", "start avServerStart(" + mSID + ", " + chIndexForSendAudio + ")");

            while (m_bIsRunning && (avIndexForSendAudio = AVAPIs.avServStart(mSID, null, null, 60, 0, chIndexForSendAudio)) < 0) {
                Log.i("IOTCamera", "avServerStart(" + mSID + ", " + chIndexForSendAudio + ") : " + avIndexForSendAudio);
            }

            Log.i("IOTCamera", "avServerStart(" + mSID + ", " + chIndexForSendAudio + ") : " + avIndexForSendAudio);

			/* init speex encoder */
            if (m_bIsRunning && mAVChannel.getAudioCodec() == AVFrame.MEDIA_CODEC_AUDIO_SPEEX) {
                EncSpeex.InitEncoder(8);
                bInitSpeexEnc = true;
                //AudioFormat.CHANNEL_IN_MONO
                nMinBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                // nMinBufSize = 640;

                Log.i("IOTCamera", "Speex encoder init");
            }

			/* init ADPCM encoder */
            if (m_bIsRunning && mAVChannel.getAudioCodec() == AVFrame.MEDIA_CODEC_AUDIO_ADPCM) {
                EncADPCM.ResetEncoder();
                bInitADPCM = true;

                nMinBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                // nMinBufSize = 640;

                Log.i("IOTCamera", "ADPCM encoder init");
            }

			/* init G726 encoder */
            if (m_bIsRunning && mAVChannel.getAudioCodec() == AVFrame.MEDIA_CODEC_AUDIO_G726) {
                EncG726.g726_enc_state_create((byte) EncG726.G726_16, EncG726.FORMAT_LINEAR);
                bInitG726Enc = true;

                nMinBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                // nMinBufSize = 320;

                Log.i("IOTCamera", "G726 encoder init");
            }

            if (m_bIsRunning && mAVChannel.getAudioCodec() == AVFrame.MEDIA_CODEC_AUDIO_PCM) {
                bInitPCM = true;
                nMinBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            }
			
			/* init mic of phone */
            AudioRecord recorder = null;
            AudioProcess mAudioProcess = null;
            AudioTrack audioTrack = null;
            if (m_bIsRunning && (bInitADPCM || bInitG726Enc || bInitSpeexEnc || bInitPCM)) {
                recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, nMinBufSize);
                mAudioProcess = new AudioProcess();
                playBufSize = AudioTrack.getMinBufferSize(SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE_IN_HZ, AudioFormat.ENCODING_PCM_16BIT, AudioFormat.CHANNEL_CONFIGURATION_DEFAULT, playBufSize, AudioTrack.MODE_STREAM);
                mAudioProcess.init(SAMPLE_RATE_IN_HZ, AudioFormat.ENCODING_PCM_8BIT, AudioFormat.CHANNEL_CONFIGURATION_MONO);
                recorder.startRecording();
                audioTrack.play();
            }

            short[] inSpeexBuf = new short[160];
            byte[] inADPCMBuf = new byte[640];
            byte[] inG726Buf = new byte[320];


            byte[] outSpeexBuf = new byte[38];
            byte[] outADPCMBuf = new byte[160];
            byte[] outG726Buf = new byte[2048];
            long[] outG726BufLen = new long[1];

			/* send audio data continuously */
            while (m_bIsRunning) {
                // read speaker data

                if (mAVChannel.getAudioCodec() == AVFrame.MEDIA_CODEC_AUDIO_SPEEX) {

                    nReadBytes = recorder.read(inSpeexBuf, 0, inSpeexBuf.length);

                    if (nReadBytes > 0) {
                        int len = EncSpeex.Encode(inSpeexBuf, nReadBytes, outSpeexBuf);
                        byte flag = (AVFrame.AUDIO_SAMPLE_8K << 2) | (AVFrame.AUDIO_DATABITS_16 << 1) | AVFrame.AUDIO_CHANNEL_MONO;
                        byte[] frameInfo = AVIOCTRLDEFs.SFrameInfo.parseContent((short) AVFrame.MEDIA_CODEC_AUDIO_SPEEX, flag, (byte) 0, (byte) 0, (int) System.currentTimeMillis());

                        AVAPIs.avSendAudioData(avIndexForSendAudio, outSpeexBuf, len, frameInfo, 16);
                    }

                } else if (mAVChannel.getAudioCodec() == AVFrame.MEDIA_CODEC_AUDIO_ADPCM) {

                    nReadBytes = recorder.read(inADPCMBuf, 0, inADPCMBuf.length);

                    if (nReadBytes > 0) {
                        EncADPCM.Encode(inADPCMBuf, nReadBytes, outADPCMBuf);
                        byte flag = (AVFrame.AUDIO_SAMPLE_8K << 2) | (AVFrame.AUDIO_DATABITS_16 << 1) | AVFrame.AUDIO_CHANNEL_MONO;
                        byte[] frameInfo = AVIOCTRLDEFs.SFrameInfo.parseContent((short) AVFrame.MEDIA_CODEC_AUDIO_ADPCM, flag, (byte) 0, (byte) 0, (int) System.currentTimeMillis());

                        AVAPIs.avSendAudioData(avIndexForSendAudio, outADPCMBuf, nReadBytes / 4, frameInfo, 16);
                    }

                } else if (mAVChannel.getAudioCodec() == AVFrame.MEDIA_CODEC_AUDIO_G726) {

                    nReadBytes = recorder.read(inG726Buf, 0, inG726Buf.length);

                    if (nReadBytes > 0) {

                        EncG726.g726_encode(inG726Buf, nReadBytes, outG726Buf, outG726BufLen);
                        byte flag = (AVFrame.AUDIO_SAMPLE_8K << 2) | (AVFrame.AUDIO_DATABITS_16 << 1) | AVFrame.AUDIO_CHANNEL_MONO;
                        byte[] frameInfo = AVIOCTRLDEFs.SFrameInfo.parseContent((short) AVFrame.MEDIA_CODEC_AUDIO_G726, flag, (byte) 0, (byte) 0, (int) System.currentTimeMillis());

                        AVAPIs.avSendAudioData(avIndexForSendAudio, outG726Buf, (int) outG726BufLen[0], frameInfo, 16);
                    }

                } else if (mAVChannel.getAudioCodec() == AVFrame.MEDIA_CODEC_AUDIO_PCM) {

//					nReadBytes = recorder.read(inPCMBuf, 0, inPCMBuf.length);
//
//					if (nReadBytes > 0) {
//						byte flag = (AVFrame.AUDIO_SAMPLE_8K << 2) | (AVFrame.AUDIO_DATABITS_16 << 1) | AVFrame.AUDIO_CHANNEL_MONO;
//						byte[] frameInfo = AVIOCTRLDEFs.SFrameInfo.parseContent((short) AVFrame.MEDIA_CODEC_AUDIO_PCM, flag, (byte) 0, (byte) 0, (int) System.currentTimeMillis());
//
//					  int result=AVAPIs.avSendAudioData(avIndexForSendAudio, inPCMBuf, nReadBytes, frameInfo, 16);
//						Log.v("suntest",String.valueOf(result));
//					}
                    //int bufferSize = mAudioProcess.calculateBufferSize(SAMPLE_RATE_IN_HZ,  AudioFormat.ENCODING_PCM_16BIT, AudioFormat.CHANNEL_CONFIGURATION_DEFAULT);
                    int bufferSize = mAudioProcess.calculateBufferSize(SAMPLE_RATE_IN_HZ, AudioFormat.ENCODING_PCM_8BIT, AudioFormat.CHANNEL_CONFIGURATION_MONO);
                    byte[] inPCMBuf = new byte[960];
                    nReadBytes = recorder.read(inPCMBuf, 0, inPCMBuf.length);
                    if (nReadBytes > 0) {
                        byte[] tmpBuf_src = new byte[nReadBytes];
                        System.arraycopy(inPCMBuf, 0, tmpBuf_src, 0, nReadBytes); // setp
                        // 2
                        // 进行处理
                        byte[] tmpBuf_processed = //new byte[bufferSize];
                                mAudioProcess.processCircle(tmpBuf_src, bufferSize, 3);

//						mAudioProcess.processStream10msData(tmpBuf_src,
//								bufferSize, tmpBuf_processed);
//						//audioTrack.write(tmpBuf_processed, 0, tmpBuf_processed.length);
//						mAudioProcess.AnalyzeReverseStream10msData(
//								tmpBuf_processed, bufferSize);


                        byte flag = (AVFrame.AUDIO_SAMPLE_8K << 2)
                                | (AVFrame.AUDIO_DATABITS_16 << 1)
                                | AVFrame.AUDIO_CHANNEL_MONO;
                        byte[] frameInfo = AVIOCTRLDEFs.SFrameInfo
                                .parseContent(
                                        (short) AVFrame.MEDIA_CODEC_AUDIO_PCM,
                                        flag, (byte) 0, (byte) 0,
                                        (int) System.currentTimeMillis());
                        int result = AVAPIs.avSendAudioData(
                                avIndexForSendAudio, tmpBuf_processed,
                                nReadBytes, frameInfo, 16);

                        Log.v("suntest", String.valueOf(result));
                    }
                }
            }
            audioTrack.stop();
            recorder.stop();
			/* uninit speex encoder */
            if (bInitSpeexEnc) {
                EncSpeex.UninitEncoder();
            }

			/* uninit g726 encoder */
            if (bInitG726Enc) {
                EncG726.g726_enc_state_destroy();
            }

			/* uninit speaker of phone */
            if (recorder != null) {
                recorder.stop();
                recorder.release();
                mAudioProcess.destroy();
                recorder = null;
            }

			/* close connection */
            if (avIndexForSendAudio >= 0) {
                AVAPIs.avServStop(avIndexForSendAudio);
            }

            if (chIndexForSendAudio >= 0) {
                IOTCAPIs.IOTC_Session_Channel_OFF(mSID, chIndexForSendAudio);
            }

            avIndexForSendAudio = -1;
            chIndexForSendAudio = -1;

            Log.i("IOTCamera", "===ThreadSendAudio exit===");
        }
    }

    private class ThreadSendIOCtrl extends Thread {

        private boolean bIsRunning = false;
        private AVChannel mAVChannel;

        public ThreadSendIOCtrl(AVChannel channel) {
            mAVChannel = channel;
        }

        public void stopThread() {

            bIsRunning = false;

            if (mAVChannel.getAVIndex() >= 0) {
                Log.i("IOTCamera", "avSendIOCtrlExit(" + mAVChannel.getAVIndex() + ")");
                AVAPIs.avSendIOCtrlExit(mAVChannel.getAVIndex());
            }
        }

        @Override
        public void run() {

            bIsRunning = true;

            while (bIsRunning && (mSID < 0 || mAVChannel.getAVIndex() < 0)) {
                try {
                    synchronized (mWaitObjectForConnected) {
                        mWaitObjectForConnected.wait(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (bIsRunning && mSID >= 0 && mAVChannel.getAVIndex() >= 0) {
                int nDelayTime_ms = 0;
                AVAPIs.avSendIOCtrl(mAVChannel.getAVIndex(), AVAPIs.IOTYPE_INNER_SND_DATA_DELAY, Packet.intToByteArray_Little(nDelayTime_ms), 4);
                Log.i("IOTCamera", "avSendIOCtrl(" + mAVChannel.getAVIndex() + ", 0x" + Integer.toHexString(AVAPIs.IOTYPE_INNER_SND_DATA_DELAY) + ", " + getHex(Packet.intToByteArray_Little(nDelayTime_ms), 4) + ")");
            }

            while (bIsRunning) {

                if (mSID >= 0 && mAVChannel.getAVIndex() >= 0 && !mAVChannel.IOCtrlQueue.isEmpty()) {

                    IOCtrlQueue.IOCtrlSet data = mAVChannel.IOCtrlQueue.Dequeue();

                    if (bIsRunning && data != null) {

                        int ret = AVAPIs.avSendIOCtrl(mAVChannel.getAVIndex(), data.IOCtrlType, data.IOCtrlBuf, data.IOCtrlBuf.length);

						/*
						if (data.IOCtrlType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_AUDIOSTOP) {
							byte[] recvBuf = new byte[1280];
							byte[] bytAVFrame = new byte[AVFrame.FRAMEINFO_SIZE];
							int[] pFrmNo = new int[1];
							
							while(AVAPIs.avRecvAudioData(mAVChannel.getAVIndex(), recvBuf, recvBuf.length, bytAVFrame, bytAVFrame.length, pFrmNo) >= 0);
							recvBuf = null;
							bytAVFrame = null;
							pFrmNo = null;
						} else if (data.IOCtrlType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_STOP) {
							byte[] recvBuf = new byte[1280 * 720 * 3];
							byte[] bytAVFrame = new byte[AVFrame.FRAMEINFO_SIZE];
							int[] pFrmNo = new int[1];
							
							while(AVAPIs.avRecvFrameData(mAVChannel.getAVIndex(), recvBuf, recvBuf.length, bytAVFrame, bytAVFrame.length, pFrmNo) >= 0);
							recvBuf = null;
							bytAVFrame = null;
							pFrmNo = null;
						}
						*/
                        if (ret >= 0) {
                            Log.i("IOTCamera", "avSendIOCtrl(" + mAVChannel.getAVIndex() + ", 0x" + Integer.toHexString(data.IOCtrlType) + ", " + getHex(data.IOCtrlBuf, data.IOCtrlBuf.length) + ")");
                        } else {
                            Log.i("IOTCamera", "avSendIOCtrl failed : " + ret);
                        }
                    }
                } else {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            Log.i("IOTCamera", "===ThreadSendIOCtrl exit===");
        }
    }

    private class ThreadRecvIOCtrl extends Thread {

        private final int TIME_OUT = 0;
        private boolean bIsRunning = false;

        private AVChannel mAVChannel;

        public ThreadRecvIOCtrl(AVChannel channel) {
            mAVChannel = channel;
        }

        public void stopThread() {
            bIsRunning = false;
        }

        @Override
        public void run() {

            bIsRunning = true;

            while (bIsRunning && (mSID < 0 || mAVChannel.getAVIndex() < 0)) {
                try {
                    synchronized (mWaitObjectForConnected) {
                        mWaitObjectForConnected.wait(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            int idx = 0;

            while (bIsRunning) {

                if (mSID >= 0 && mAVChannel.getAVIndex() >= 0) {

                    int[] ioCtrlType = new int[1];
                    byte[] ioCtrlBuf = new byte[1024];

                    int nRet = AVAPIs.avRecvIOCtrl(mAVChannel.getAVIndex(), ioCtrlType, ioCtrlBuf, ioCtrlBuf.length, TIME_OUT);

                    if (nRet >= 0) {

                        Log.i("IOTCamera", "avRecvIOCtrl(" + mAVChannel.getAVIndex() + ", 0x" + Integer.toHexString(ioCtrlType[0]) + ", " + getHex(ioCtrlBuf, nRet) + ")");

                        byte[] data = new byte[nRet];
                        System.arraycopy(ioCtrlBuf, 0, data, 0, nRet);
						
						/* write frame data to file */
						/*
						try {
							
							File rootFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Output/");
							if (!rootFolder.exists()) {
								try {
									rootFolder.mkdir();
								} catch (Exception e) {
									
								}
							}
							
							String fileName = rootFolder.getAbsolutePath() + File.separator + String.format("IOCtrl_%05d", idx++) + ".bin"; 
							
							java.io.BufferedOutputStream bos = new java.io.BufferedOutputStream(new FileOutputStream(fileName));
							bos.write(data, 0, nRet);
							bos.flush();
							bos.close();
						} catch (Exception e) {
							
						}
						*/
						/* - end - */

                        if (ioCtrlType[0] == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETAUDIOOUTFORMAT_RESP) {

                            int channel = Packet.byteArrayToInt_Little(data, 0);
                            int format = Packet.byteArrayToInt_Little(data, 4);

                            for (AVChannel ch : mAVChannels) {
                                if (ch.getChannel() == channel) {
                                    ch.setAudioCodec(format);
                                    break;
                                }
                            }
                        }

                        // synchronized (mIOTCListeners) {
                        for (int i = 0; i < mIOTCListeners.size(); i++) {
                            IRegisterIOTCListener listener = mIOTCListeners.get(i);
                            listener.receiveIOCtrlData(Camera.this, mAVChannel.getChannel(), ioCtrlType[0], data);
                        }
                        // }

                    } else {

                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }

            Log.i("IOTCamera", "===ThreadRecvIOCtrl exit===");
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
            mAudioCodec = (serviceType & 4096) == 0 ? AVFrame.MEDIA_CODEC_AUDIO_SPEEX : AVFrame.MEDIA_CODEC_AUDIO_ADPCM;
        }

        public String getViewAcc() {
            return mViewAcc;
        }

        public String getViewPwd() {
            return mViewPwd;
        }

        public ThreadStartDev threadStartDev = null;
        public ThreadRecvIOCtrl threadRecvIOCtrl = null;
        public ThreadSendIOCtrl threadSendIOCtrl = null;
        public ThreadRecvVideo2 threadRecvVideo = null;
        public ThreadRecvAudio threadRecvAudio = null;
        public ThreadDecodeVideo2 threadDecVideo = null;
        public ThreadDecodeAudio threadDecAudio = null;
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

    private static final String HEXES = "0123456789ABCDEF";

    static String getHex(byte[] raw, int size) {

        if (raw == null) {
            return null;
        }

        final StringBuilder hex = new StringBuilder(2 * raw.length);

        int len = 0;

        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F))).append(" ");

            if (++len >= size)
                break;
        }

        return hex.toString();
    }
}
