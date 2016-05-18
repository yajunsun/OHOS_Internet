package com.decoder.util;

public class DecADPCM {

	public static native int ResetDecoder();

	public static native int Decode(byte[] in, int in_size, byte[] out);

	static {

		try {
			System.loadLibrary("ADPCMAndroid");
		} catch (UnsatisfiedLinkError ule) {
			System.out.println("loadLibrary(ADPCMAndroid)," + ule.getMessage());
		}
	}
}
