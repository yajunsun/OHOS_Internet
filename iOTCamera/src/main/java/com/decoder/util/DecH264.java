package com.decoder.util;

public class DecH264 {

	public native static int InitDecoder();

	public native static int UninitDecoder();

	public native static int DecoderNal(byte[] in, int insize, int[] gotPicture, byte[] out);

	// public native static int Decode(byte[] in, int in_size, byte[] out, int[] out_size, int[] out_width, int[] out_height);

	static {
		try {
			System.loadLibrary("H264Android");
		} catch (UnsatisfiedLinkError ule) {
			System.out.println("loadLibrary(H264Android)," + ule.getMessage());
		}
	}
}
