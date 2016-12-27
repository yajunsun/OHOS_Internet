package com.tutk.RDT;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import zgan.ohos.utils.RDTFrame;

/**
 * Created by yajunsun on 2016/7/20 0020.
 * 一般手机配置时使用
 */
public class NormalRDTCamera extends RDTCamera {

    public ThreadSendAudio threadSendAudio = null;

    public NormalRDTCamera(String name, String uid, String acc, String pwd) {
        super(name, uid, acc, pwd);
    }

    @Override
    public void startShow(int avChannel) {
        super.startShow(avChannel);
        for (int i = 0; i < mAVChannels.size(); i++) {
            AVChannel ch = mAVChannels.get(i);
            if (ch.getChannel() == avChannel) {
                if (threadSendAudio == null) {
                    threadSendAudio = new ThreadSendAudio(ch);
                    threadSendAudio.start();
                }
            }
        }
    }

    @Override
    public void stopShow(int avChannel) {

        super.stopShow(avChannel);
        Log.i("suntest", "stopShow");
        synchronized (mAVChannels) {

            for (int i = 0; i < mAVChannels.size(); i++) {

                AVChannel ch = mAVChannels.get(i);

                if (ch.getChannel() == avChannel) {

                    if (threadSendAudio != null) {
                        threadSendAudio.stopThread();
                        try {
                            threadSendAudio.interrupt();
                            threadSendAudio.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }
    }
    protected void playRDT(AVChannel mAVChannel, int pFrmNo, RDTFrame rdtFrame) {
        byte[] adpcmOutBuf = new byte[640];
        int nCodecId = 0;


        byte[] frameData = new byte[rdtFrame.mLen - 12];
        System.arraycopy(rdtFrame.mContent, 0, frameData, 0, frameData.length);
//        if (rdtFrame.mType == 1) {
//            //mAVChannel.VideoBPS += rdtFrame.mLen; //outBufSize[0];
//            // nFrmCount++;
//            AVFrame frame = new AVFrame(pFrmNo, AVFrame.FRM_STATE_COMPLETE, frameData, frameData.length);
//            nCodecId = (int) frame.getCodecId();
//            //Log.i("suntest", "recieved vedio");
//            if (nCodecId == AVFrame.MEDIA_CODEC_VIDEO_H264) {
//                //保持图像队列的容量为10
//                if (mAVChannel.VideoFrameQueue.getCount() < 11)
//                    mAVChannel.VideoFrameQueue.addLast(frame);
//                Log.i("IOTCamera", "Enqueue AVFrameQueue left " + mAVChannel.VideoFrameQueue.getCount());
//            }
//        }
        if (rdtFrame.mType == 2) {
            try {
                mAVChannel.AudioBPS += rdtFrame.mLen;
                //960 4*960,16000
//                if (latestNetVoice == null)
//                    latestNetVoice = new LinkedList<>();
//                else {
//                    if (micvoice == null) {
                //MIC未启动时直接播放
                audioTraceWrite(frameData, 0, frameData.length);
                Log.i("IOTCamera", "voice play0");
//                    } else {
//                        //保持音频队列的容量为10
//                        if (latestNetVoice.size() < 11)
//                            latestNetVoice.addLast(frameData);
//                        Log.i("IOTCamera", "voice in, latestNetVoice.size()=" + latestNetVoice.size());
//                    }
//                }
            } catch (Exception e) {
                e.printStackTrace();
                //Log.i("suntest", "voice error:" + e.getMessage());
            }
        }
    }
    protected class ThreadSendAudio extends Thread {

        protected boolean m_bIsRunning = false;
        protected static final int SAMPLE_RATE_IN_HZ = 16000;
        protected AVChannel mAVChannel = null;
        protected Object m_waitObjForCheckDevStatus = new Object();


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
            int nReadBytes = 960;

            while (mSID < 0 || nRDT_ID < 0) {
                try {

                    synchronized (m_waitObjForCheckDevStatus) {
                        m_waitObjForCheckDevStatus.wait(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Log.i("suntest", "ready to send voice");
            nMinBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            //}

			/* init mic of phone */
            AudioRecord recorder = null;
            //AudioTrack audioTrack = null;
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, nMinBufSize);
            recorder.startRecording();

            Log.i("IOTCamera", "recorder begin work");
            while (m_bIsRunning) {
                byte[] inPCMBuf = new byte[960];
                recorder.read(inPCMBuf, 0, inPCMBuf.length);
                byte d = (byte) (nReadBytes + 12 & 0x000000ff);
                byte c = (byte) ((nReadBytes + 12 & 0x0000ff00) >> 8);
                byte b = (byte) ((nReadBytes + 12 & 0x00ff0000) >> 16);
                byte a = (byte) ((nReadBytes + 12 & 0xff000000) >> 24);
                byte[] head = new byte[]{36, 83, 88, 38, a, b, c, d, 2, 0, 0, 0};
                byte[] Buf_processed = new byte[nReadBytes + 12];
                System.arraycopy(inPCMBuf, 0, Buf_processed, 12, nReadBytes); // setp
                System.arraycopy(head, 0, Buf_processed, 0, 12);
                rdtWrite(nRDT_ID, Buf_processed, nReadBytes + 12);
                Log.i("IOTCamera", "voice has sent2");
                System.gc();
            }
            recorder.stop();

            Log.i("IOTCamera", "===ThreadSendAudio exit===");
        }
    }
}
