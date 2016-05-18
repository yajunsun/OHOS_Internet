package com.decoder.util;

public class DecMpeg4 {

	public native static int InitDecoder(int width, int height);

	public native static int UninitDecoder();

	public native static int Decode(byte[] in, int in_size, byte[] out, int[] out_size, int[] out_width, int[] out_height);

	static {
		try {
			System.loadLibrary("FFmpeg");
		} catch (UnsatisfiedLinkError ule) {
			System.out.println("loadLibrary(FFmpeg)," + ule.getMessage());
		}
	}
}
