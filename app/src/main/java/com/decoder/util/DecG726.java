package com.decoder.util;

public class DecG726 {
	public static final int G726_16 = 0;
	public static final int G726_24 = 1;
	public static final int G726_32 = 2;
	public static final int G726_40 = 3;

	public static final byte FORMAT_ULAW = 0x00; // ISDN u-law
	public static final byte FORMAT_ALAW = 0x01; // ISDN A-law
	public static final byte FORMAT_LINEAR = 0x02; // PCM 2's-complement (0-center)

	public static final int API_ER_ANDROID_NULL = -10000;

	public static native int g726_dec_state_create(byte bitrule, byte format);

	public static native void g726_dec_state_destroy();

	public static native int g726_decode(byte[] inBuf, long inLen, byte[] outBuf, long[] outLen);

	static {
		try {
			System.loadLibrary("G726Android");
		} catch (UnsatisfiedLinkError ule) {
			System.out.println("loadLibrary(G726Android)," + ule.getMessage());
		}
	}
}
