package zgan.ohos.utils;

import android.graphics.Bitmap;

import com.tutk.IOTC.AVFrame;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.AVIOCTRLDEFs.SStreamDef;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.IOTCAPIs;
import com.tutk.IOTC.Packet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MyCamera extends Camera implements com.tutk.IOTC.IRegisterIOTCListener {

	public int LastAudioMode;

	private String mName;
	private String mUID;
	private String mAcc;
	private String mPwd;

	private int mEventCount = 0;

	private boolean bIsMotionDetected;
	private boolean bIsIOAlarm;

	private UUID mUUID = UUID.randomUUID();
	private List<SStreamDef> mStreamDefs = Collections.synchronizedList(new ArrayList<SStreamDef>());

	public MyCamera(String name, String uid, String acc, String pwd) {
		mName = name;
		mUID = uid;
		mAcc = acc;
		mPwd = pwd;

		this.registerIOTCListener(this);
	}

	@Override
	public void connect(String uid,int channel) {
		// TODO Auto-generated method stub
		super.connect(uid,channel);
		mUID = uid;

	}

	@Override
	public void connect(String uid, String pwd) {
		super.connect(uid, pwd);
		mUID = uid;
	}

	@Override
	public void disconnect() {
		super.disconnect();
		mStreamDefs.clear();
	}

	public String getUUID() {
		return mUUID.toString();
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getUID() {
		return mUID;
	}

	public String getPassword() {
		return mPwd;
	}
	
	public void setPassword(String pwd) {
		mPwd = pwd;
	}

	public void resetEventCount() {
		mEventCount = 0;
	}

	public int getEventCount() {
		return mEventCount;
	}

	public com.tutk.IOTC.AVIOCTRLDEFs.SStreamDef[] getSupportedStream() {
		SStreamDef[] result = new SStreamDef[mStreamDefs.size()];

		for (int i = 0; i < result.length; i++)
			result[i] = mStreamDefs.get(i);

		return result;
	}

	public boolean getAudioInSupported(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 1) == 0;
	}

	public boolean getAudioOutSupported(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 2) == 0;
	}

	public boolean getPanTiltSupported(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 4) == 0;
	}

	public boolean getEventListSupported(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 8) == 0;
	}

	public boolean getPlaybackSupported(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 16) == 0;
	}

	public boolean getWiFiSettingSupported(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 32) == 0;
	}

	public boolean getEventSettingSupported(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 64) == 0;
	}

	public boolean getRecordSettingSupported(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 128) == 0;
	}

	public boolean getSDCardFormatSupported(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 256) == 0;
	}

	public boolean getVideoFlipSupported(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 512) == 0;
	}

	public boolean getEnvironmentModeSupported(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 1024) == 0;
	}

	public boolean getMultiStreamSupported(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 2048) == 0;
	}

	public int getAudioOutEncodingFormat(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 4096) == 0 ? AVFrame.MEDIA_CODEC_AUDIO_SPEEX : AVFrame.MEDIA_CODEC_AUDIO_ADPCM;
	}

	public boolean getVideoQualitySettingSupport(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 8192) == 0;
	}

	public boolean getDeviceInfoSupport(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 16384) == 0;
	}

	@Override
	public void receiveChannelInfo(Camera arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void receiveFrameData(Camera arg0, int arg1, Bitmap arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void receiveFrameInfo(Camera arg0, int arg1, long arg2, int arg3, int arg4, int arg5, int arg6) {
		// TODO Auto-generated method stub

	}

	@Override
	public void receiveIOCtrlData(final Camera camera, final int avChannel, int avIOCtrlMsgType, final byte[] data) {

		if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETSUPPORTSTREAM_RESP) {

			mStreamDefs.clear();

			int num = Packet.byteArrayToInt_Little(data, 0);

			if (avChannel == 0 && this.getMultiStreamSupported(0)) {

				for (int i = 0; i < num; i++) {

					byte[] buf = new byte[8];
					System.arraycopy(data, i * 8 + 4, buf, 0, 8);
					SStreamDef streamDef = new SStreamDef(buf);
					mStreamDefs.add(streamDef);

					//camera.start(streamDef.channel, mAcc, mPwd);
				}
			}

		} else if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_EVENT_REPORT) {

			int evtType = Packet.byteArrayToInt_Little(data, 12);

			if (evtType == AVIOCTRLDEFs.AVIOCTRL_EVENT_MOTIONDECT) {
				if (!bIsMotionDetected)
					mEventCount++;
				bIsMotionDetected = true;
			} else if (evtType == AVIOCTRLDEFs.AVIOCTRL_EVENT_MOTIONPASS) {
				bIsMotionDetected = false;
			} else if (evtType == AVIOCTRLDEFs.AVIOCTRL_EVENT_IOALARM) {
				if (!bIsIOAlarm)
					mEventCount++;
				bIsIOAlarm = true;
			} else if (evtType == AVIOCTRLDEFs.AVIOCTRL_EVENT_IOALARMPASS) {
				bIsIOAlarm = false;
			}
		}
	}

	@Override
	public void receiveSessionInfo(Camera arg0, int arg1) {

	}
}
