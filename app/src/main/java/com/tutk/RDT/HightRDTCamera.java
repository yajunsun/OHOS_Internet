package com.tutk.RDT;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.util.LinkedList;

import zgan.ohos.utils.RDTFrame;

/**
 * Created by yajunsun on 2016/7/20 0020.
 * 手机高配置使用
 */
public class HightRDTCamera extends RDTCamera {

    public ThreadSendAudio threadSendAudio = null;
    public  VoiceIndust voiceIndust=null;

    public HightRDTCamera(String name, String uid, String acc, String pwd) {
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

                    if (voiceIndust != null) {
                        voiceIndust.StopThread();

                        try {
                            voiceIndust.interrupt();
                            voiceIndust.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        voiceIndust = null;
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
        if (rdtFrame.mType == 1) {
            //mAVChannel.VideoBPS += rdtFrame.mLen; //outBufSize[0];
            // nFrmCount++;
            AVFrame frame = new AVFrame(pFrmNo, AVFrame.FRM_STATE_COMPLETE, frameData, frameData.length);
            nCodecId = (int) frame.getCodecId();
            //Log.i("suntest", "recieved vedio");
            if (nCodecId == AVFrame.MEDIA_CODEC_VIDEO_H264) {
                //保持图像队列的容量为10
                if (mAVChannel.VideoFrameQueue.getCount() < 11)
                    mAVChannel.VideoFrameQueue.addLast(frame);
                Log.i("IOTCamera", "Enqueue AVFrameQueue left " + mAVChannel.VideoFrameQueue.getCount());
            }
        }
        if (rdtFrame.mType == 2) {
            try {
                mAVChannel.AudioBPS += rdtFrame.mLen;
                //960 4*960,16000
                if (latestNetVoice == null)
                    latestNetVoice = new LinkedList<>();
                else {
                    if (micvoice == null) {
                        //MIC未启动时直接播放
                        audioTraceWrite(frameData, 0, frameData.length);
                        Log.i("IOTCamera", "voice play0");
                    } else {
                        //保持音频队列的容量为10
                        if (latestNetVoice.size() < 11)
                            latestNetVoice.addLast(frameData);
                        Log.i("IOTCamera", "voice in, latestNetVoice.size()=" + latestNetVoice.size());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                //Log.i("suntest", "voice error:" + e.getMessage());
            }
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
            Log.i("suntest", "ready to send voice");
            nMinBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            //}

			/* init mic of phone */
            AudioRecord recorder = null;
            //AudioTrack audioTrack = null;
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, nMinBufSize);
            recorder.startRecording();
            //mSpeex = new speexprocess();
            //int speexinit = mSpeex.Speex_init(960, 960 * 4, 16000);
            //int speexinit = mSpeex.Speex_init(960, 960 * 4, 8000);

            Log.i("IOTCamera", "recorder begin work");
            while (m_bIsRunning) {
                byte[] inPCMBuf = new byte[960];
                recorder.read(inPCMBuf, 0, inPCMBuf.length);
                if (micvoice == null)
                    micvoice = new LinkedList<>();
                if (micvoice.size() < 11)
                    micvoice.addLast(inPCMBuf);
                if (voiceIndust == null) {
                    voiceIndust = new VoiceIndust();
                    voiceIndust.start();
                }
            }
            recorder.stop();
            //mSpeex.Speex_exit();
            //mSpeex = null;
            avIndexForSendAudio = -1;
            chIndexForSendAudio = -1;

            Log.i("IOTCamera", "===ThreadSendAudio exit===");
        }
    }

    private class VoiceIndust extends Thread {
        private boolean isRunning = false;

        public VoiceIndust() {
            isRunning = true;
            //executorService = Executors.newFixedThreadPool(5);
        }

        public void StopThread() {
            isRunning = false;
            if (executorService != null)
                executorService.shutdown();
            Log.i("IOTCamera", "VoiceIndust stoped 1");
        }

        @Override
        public void run() {
            super.run();
            while (isRunning) {
//                try {
//                    Thread.sleep(10);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                if (latestNetVoice != null && micvoice != null) {
                    if (latestNetVoice.size() > 0 && micvoice.size() > 0) {
                        byte[] voice_in;
                        byte[] voice_out;
                        synchronized (latestNetVoice) {
                            voice_in = latestNetVoice.removeFirst();
                        }
                        synchronized (micvoice) {
                            voice_out = micvoice.removeFirst();
                        }
                        if (voice_in != null && voice_out != null) {
                            byte[] srcProcess = new byte[960];
                            Log.i("IOTCamera", "voice play1, latestNetVoice.size()=" + latestNetVoice.size());
                            audioTraceWrite(voice_in, 0, voice_in.length);
                            Log.i("IOTCamera", "voice has played1");
                            //int re = mSpeex.Speex_process(voice_in, voice_out, srcProcess);
                            //Log.i("suntest","result of speex:"+re);
                            int nReadBytes = 960;
                            byte d = (byte) (nReadBytes + 12 & 0x000000ff);
                            byte c = (byte) ((nReadBytes + 12 & 0x0000ff00) >> 8);
                            byte b = (byte) ((nReadBytes + 12 & 0x00ff0000) >> 16);
                            byte a = (byte) ((nReadBytes + 12 & 0xff000000) >> 24);
                            byte[] head = new byte[]{36, 83, 88, 38, a, b, c, d, 2, 0, 0, 0};
                            byte[] Buf_processed = new byte[nReadBytes + 12];
                            System.arraycopy(voice_out, 0, Buf_processed, 12, nReadBytes); // setp
                            System.arraycopy(head, 0, Buf_processed, 0, 12);
                            Log.i("IOTCamera", "voice send1, micvoice.size()=" + micvoice.size());
                            rdtWrite(nRDT_ID, Buf_processed, nReadBytes + 12);
                            Log.i("IOTCamera", "voice has sent1");
//                        latestNetVoice.clear();
//                        micvoice.clear();
                            System.gc();
                        }
                    } else if (latestNetVoice.size() > 0) {
                        final byte[] voice_in;
                        synchronized (latestNetVoice) {
                            voice_in = latestNetVoice.removeFirst();
                        }
                        if (voice_in != null) {
                            Log.i("IOTCamera", "voice play2, latestNetVoice.size()=" + latestNetVoice.size());
                            audioTraceWrite(voice_in, 0, voice_in.length);
                            Log.i("IOTCamera", "voice has played2");
                            System.gc();
                        }
                    } else if (micvoice.size() > 0 && !micvoice.isEmpty()) {
                        byte[] voice_out;
                        synchronized (micvoice) {
                            voice_out = micvoice.removeFirst();
                        }
                        if (voice_out != null) {
                            int nReadBytes = 960;
                            byte d = (byte) (nReadBytes + 12 & 0x000000ff);
                            byte c = (byte) ((nReadBytes + 12 & 0x0000ff00) >> 8);
                            byte b = (byte) ((nReadBytes + 12 & 0x00ff0000) >> 16);
                            byte a = (byte) ((nReadBytes + 12 & 0xff000000) >> 24);
                            byte[] head = new byte[]{36, 83, 88, 38, a, b, c, d, 2, 0, 0, 0};
                            byte[] Buf_processed = new byte[nReadBytes + 12];
                            System.arraycopy(voice_out, 0, Buf_processed, 12, nReadBytes); // setp
                            System.arraycopy(head, 0, Buf_processed, 0, 12);
                            Log.i("IOTCamera", "voice send2, micvoice.size()=" + micvoice.size());
                            rdtWrite(nRDT_ID, Buf_processed, nReadBytes + 12);
                            Log.i("IOTCamera", "voice has sent2");
                            System.gc();
                        }

                    }
                }
            }
            Log.i("IOTCamera", "VoiceIndust stoped 0");
        }
    }

    public synchronized void audioTraceWrite(byte[] source, int offset, int length) {
        if (mAudioTrack != null)
            mAudioTrack.write(source, offset, length);
    }

    public synchronized void rdtWrite(int rdtID, byte[] source, int length) {
        RDTAPIs.RDT_Write(rdtID, source, length);
    }

}
