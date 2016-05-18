package com.decoder.util;

public class DecMp3 {
	
	public static native int InitDecoder(int nSamplerate, int nDatabit);
    public static native int UninitDecoder();
	public static native int Decode(byte[] buf, int len, byte[] outbuf);
	
	static {
		try {
			System.loadLibrary("Mp3Android");
		}
		catch (UnsatisfiedLinkError ule){
			System.out.println("loadLibrary(Mp3Android),"+ule.getMessage());
		}
	}
}
