package com.speex;

/**
 * Created by Administrator on 16-6-2.
 */
public class speexprocess {
    static {
        System.loadLibrary("speexprocess");
    }

    public native int Speex_init(int frame_size, int filter_length, int sample_rate);

    public native int Speex_exit();

    public native int Speex_process(byte[] net_buf, byte[] mic_buf, byte[] out_buf, byte[] noise);
}
