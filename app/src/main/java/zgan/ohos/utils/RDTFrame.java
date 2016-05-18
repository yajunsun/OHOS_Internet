package zgan.ohos.utils;

import android.util.Log;

/**
 * Created by Administrator on 16-4-19.
 */
public class RDTFrame {

    private final static int mLenHead = 4;
    private final static int mLenPackage = 4;
    private final static int mLenType = 1;
    private final static int mLenCommand = 1;
    private final static int mLenReserv = 2;
    private final static int mBeginData = 12;

    public RDTFrame(byte[] buf) throws Exception {
        if (buf.length<12)
            throw new Exception("数据长度错误");
        byte[] head = new byte[mLenHead];
        byte[] length = new byte[mLenPackage];
        //头
        System.arraycopy(buf, 0, head, 0, mLenHead);
        mHead = FrameTools.decodeFrameData(head);
        //包长
        System.arraycopy(buf, mLenHead, length, 0, mLenPackage);
        mLen =bytesToInt2(length,0);
        //类型
        mType = buf[mLenHead + mLenPackage];
        //数据
        mContent = new byte[buf.length - mBeginData];
        System.arraycopy(buf, mBeginData, mContent, 0, mContent.length);
    }

    public String mHead;
    public int mType;
    public byte[] mContent;
    public int mLen;

    /**
     * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序，和和intToBytes（）配套使用
     *
     * @param src
     *            byte数组
     * @param offset
     *            从数组的第offset位开始
     * @return int数值
     */
    public static int bytesToInt(byte[] src, int offset) {
        int value;
        value = (int) ((src[offset] & 0xFF)
                | ((src[offset+1] & 0xFF)<<8)
                | ((src[offset+2] & 0xFF)<<16)
                | ((src[offset+3] & 0xFF)<<24));
        return value;
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在后，高位在前)的顺序。和intToBytes2（）配套使用
     */
    public static int bytesToInt2(byte[] src, int offset) {
        int value;
        value = (int) ( ((src[offset] & 0xFF)<<24)
                |((src[offset+1] & 0xFF)<<16)
                |((src[offset+2] & 0xFF)<<8)
                |(src[offset+3] & 0xFF));
        return value;
    }
}
