package com.tutk.RDT;

import android.graphics.Bitmap;

public interface IRegisterIOTCListener {

	public void receiveFrameData(final Camera camera, final int avChannel, final Bitmap bmp);

	public void receiveFrameInfo(final Camera camera, final int avChannel, final long bitRate, final int frameRate, final int onlineNm, final int frameCount,
			final int incompleteFrameCount);

	public void receiveSessionInfo(final Camera camera, final int resultCode);

	public void receiveChannelInfo(final Camera camera, final int avChannel, final int resultCode);

	public void receiveIOCtrlData(final Camera camera, final int avChannel, final int avIOCtrlMsgType, final byte[] data);
}