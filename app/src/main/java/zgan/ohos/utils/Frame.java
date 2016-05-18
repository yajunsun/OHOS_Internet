package zgan.ohos.utils;

import android.os.Handler;

public class Frame {


    public String Head = "$ZG&";

    //public int platform = 0xF;
    public int platform = 0xF;


    public int version = 1;


    public byte mainCmd = 0;


    public int subCmd = 0;


    public String strData = "";

    public int zip = 0;


    public byte[] aryData = null;

    public Frame() {
    }

    public Frame(byte[] Buff) {

        FrameTools.getByteToFrame(Buff, this);
    }

    public Handler _handler;
}
