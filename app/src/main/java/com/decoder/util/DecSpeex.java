package com.decoder.util;

public class DecSpeex {
	
	public static native int InitDecoder(int sampling_rate);
    public static native int UninitDecoder();
	public static native int Decode(byte[] in, int in_size, short[] out);
	
	static {
		
		try {
			System.loadLibrary("SpeexAndroid");
		}
		catch (UnsatisfiedLinkError ule){
			System.out.println("loadLibrary(SpeexAndroid),"+ule.getMessage());
		}
	}
}
