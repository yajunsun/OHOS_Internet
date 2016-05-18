package com.encoder.util;

public class EncADPCM {

	public static native int ResetEncoder();

	public static native int Encode(byte[] in, int in_size, byte[] out);

	static {

		try {
			System.loadLibrary("ADPCMAndroid");
		} catch (UnsatisfiedLinkError ule) {
			System.out.println("loadLibrary(ADPCMAndroid)," + ule.getMessage());
		}
	}
}
