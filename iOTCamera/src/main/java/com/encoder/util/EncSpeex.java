package com.encoder.util;

public class EncSpeex {
	
	public static native int InitEncoder(int quality);
    public static native int UninitEncoder();
	public static native int Encode(short[] in, int in_size, byte[] out);
	
	static {
		
		try {
			System.loadLibrary("SpeexAndroid");
		}
		catch (UnsatisfiedLinkError ule){
			System.out.println("loadLibrary(SpeexAndroid),"+ule.getMessage());
		}
	}
}
