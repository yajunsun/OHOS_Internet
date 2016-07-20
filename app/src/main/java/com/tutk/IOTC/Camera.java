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
import com.speex.speexprocess;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;

import zgan.ohos.utils.RDTFrame;

public abstract class Camera {


    protected static int mDefaultMaxCameraLimit = 4;

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

    protected final Object mWaitObjectForConnected = new Object();


    //protected ThreadSendAudio mThreadSendAudio = null;

    protected volatile int mSID = -1;
    protected volatile int mSessionMode = -1;
    protected volatile int nRDT_ID = -1;

    protected boolean mInitAudio = false;
    protected AudioTrack mAudioTrack = null;
    protected LinkedList<byte[]> latestNetVoice;
    protected LinkedList<byte[]> micvoice;
    //protected speexprocess mSpeex = null;
    ExecutorService executorService = null;
    //AudioProcess mAudioProcess = null;
    protected int mCamIndex = 0;

    /* camera info */
    protected String mDevUID="";
    protected String mDevPwd="";

    protected List<IRegisterIOTCListener> mIOTCListeners = Collections.synchronizedList(new Vector<IRegisterIOTCListener>());
    protected List<AVChannel> mAVChannels = Collections.synchronizedList(new Vector<AVChannel>());


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

    public abstract void connect(String uid, int channel);

    public abstract void connect(String uid, String pwd);

    public abstract void disconnect();

    public void start(int avChannel, String viewAccount, String viewPasswd) {

        AVChannel session = null;
        AVChannel ch = new AVChannel(avChannel, viewAccount, viewPasswd);
        mAVChannels.add(ch);
    }

    public abstract void startShow(int avChannel);

    public abstract void stopShow(int avChannel);

    protected synchronized boolean audioDev_init(int sampleRateInHz, int channel, int dataBit, int codec_id) {

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

//            if (codec_id == AVFrame.MEDIA_CODEC_AUDIO_SPEEX) {
//                DecSpeex.InitDecoder(sampleRateInHz);
//            } else if (codec_id == AVFrame.MEDIA_CODEC_AUDIO_MP3) {
//                int bit = (dataBit == AVFrame.AUDIO_DATABITS_16) ? 16 : 8;
//                DecMp3.InitDecoder(sampleRateInHz, bit);
//            } else if (codec_id == AVFrame.MEDIA_CODEC_AUDIO_ADPCM || codec_id == AVFrame.MEDIA_CODEC_AUDIO_PCM) {
//                DecADPCM.ResetDecoder();
//            } else if (codec_id == AVFrame.MEDIA_CODEC_AUDIO_G726) {
//                DecG726.g726_dec_state_create((byte) DecG726.G726_16, (byte) DecG726.FORMAT_LINEAR);
//            }
            Log.i("suntest", "play voice");
            //mAudioTrack.setStereoVolume(1.0f, 1.0f);
            mAudioTrack.play();
            mInitAudio = true;

            return true;
        } else
            return false;
    }

    protected synchronized void audioDev_stop(int codec_id) {

        if (mInitAudio) {

            if (mAudioTrack != null) {
                mAudioTrack.stop();
                mAudioTrack.release();
                mAudioTrack = null;
            }

//            if (codec_id == AVFrame.MEDIA_CODEC_AUDIO_SPEEX) {
//                DecSpeex.UninitDecoder();
//            } else if (codec_id == AVFrame.MEDIA_CODEC_AUDIO_MP3) {
//                DecMp3.UninitDecoder();
//            } else if (codec_id == AVFrame.MEDIA_CODEC_AUDIO_G726) {
//                DecG726.g726_dec_state_destroy();
//            }

            mInitAudio = false;

        }
    }


    public synchronized void audioTraceWrite(byte[] source, int offset, int length) {
        if (mAudioTrack != null)
            mAudioTrack.write(source, offset, length);
    }

    protected class ThreadDecodeVideo2 extends Thread {

        static final int MAX_FRAMEBUF = 1280 * 720 * 3;

        protected boolean m_bIsRunning = false;

        protected AVChannel mAVChannel;

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
                        //Log.i("IOTCamera", "Dequeue AVFrameQueue left " + mAVChannel.VideoFrameQueue.getCount());
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
                        if (avFrame.getCodecId() == AVFrame.MEDIA_CODEC_VIDEO_H264) {

                            t1 = System.currentTimeMillis();

                            if (!bInitH264) {
                                DecH264.InitDecoder();
                                bInitH264 = true;
                            } // else {
                            // FFMPEG - DecH264.Decode(avFrame.frmData, avFrameSize, bufOut, out_size, out_width, out_height);
                            DecH264.DecoderNal(avFrame.frmData, avFrameSize, framePara, bufOut);
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

    protected class AVChannel {

        protected volatile int mChannel = -1;
        protected volatile int mAVIndex = -1;
        protected long mServiceType = 0xFFFFFFFF;
        protected String mViewAcc;
        protected String mViewPwd;
        protected int mAudioCodec;

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
//            rdtQueue = new RDTQueue();
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
    }

    protected class IOCtrlQueue {

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

    protected class TimBytes {
        public TimBytes(long t, byte[] d) {
            setTimstamp(t);
            setData(d);
        }

        public Long getTimstamp() {
            return timstamp;
        }

        public void setTimstamp(Long timstamp) {
            this.timstamp = timstamp;
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        protected Long timstamp;
        protected byte[] data;
    }

}