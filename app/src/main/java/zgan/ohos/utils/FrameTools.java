package zgan.ohos.utils;

import java.io.UnsupportedEncodingException;
import java.nio.channels.Selector;
import java.util.ArrayList;

public class FrameTools {

	public static final int Frame_Max = 512;
	public static final int Frame_Len = 12;
	public static final byte Frame_MainCmd_Login = 0x01;
	public static final byte Frame_MainCmd_Centr = 0x0C;
	public static final byte Frame_MainCmd_Client = 0x0E;
	public static final byte Frame_MainCmd_Ping = 0x00;
	public static Selector selector;

	public boolean Thread_Ping = true;
	public int Thread_PingTime = 0;

	public static byte[] getFrameBuffData(Frame f) {
		byte[] Buff = null;
		byte[] dataBuff = null;
		int intDataLen = 0;

		if (f.strData != null && !f.strData.equals("")) {
			try {
				dataBuff = f.strData.getBytes("GBK");

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			intDataLen = dataBuff.length;
		} else {
			dataBuff = f.aryData;
			if (null == dataBuff) {
				intDataLen = 0;
			} else {
				intDataLen = dataBuff.length;
			}
		}

		Buff = new byte[Frame_Len + intDataLen];

		Buff[0] = 36;
		Buff[1] = 90;
		Buff[2] = 71;
		Buff[3] = 38;

		/*
		 * Buff[4] = (byte)(f.platform>>8); Buff[5] = (byte)f.platform;
		 */
		IntToHighLowByte(Buff, 4, f.platform);

		Buff[6] = (byte) f.version;

		Buff[7] =(byte)f.zip; //(byte) f.mainCmd;

		Buff[8] = (byte) f.subCmd;

		IntToHighLowByte(Buff, 9, intDataLen);

		if (intDataLen > 0) {
			/*
			 * Buff[9] = (byte)(intDataLen>>8); Buff[10] = (byte)(intDataLen);
			 */
			System.arraycopy(dataBuff, 0, Buff, 11, intDataLen);
		}/*
		 * else { Buff[9] = 0; Buff[10] = 0; }
		 */
		// 数据校验
		/*
		 * byte cbXOR = 0; for(int i = 4;i< intDataLen+11 ;i++) { cbXOR ^=
		 * Buff[i]; } Buff[11+intDataLen] = cbXOR;
		 */
		Buff[Buff.length - 1] = getCheckSum(Buff);

		return Buff;
	}

	public static void getByteToFrame(byte[] Buff, Frame f) {
		getByteToFrame_Version_1(Buff, f);
	}

	private static void getByteToFrame_Version_1(byte[] Buff, Frame f) {
		byte CheckSum = 0;
		int intDataLen = 0;
		byte[] aryData = null;

		intDataLen = HighLowToInt(Buff[9], Buff[10]);

		if (Buff.length > intDataLen + 11) {

			CheckSum = Buff[intDataLen + 11];

			if (CheckSum == getCheckSum(Buff, intDataLen + 11)) {

				f.platform = HighLowToInt(Buff[4], Buff[5]);

				f.version = Buff[6] & 0xFF;

				f.mainCmd = Buff[7];

				f.subCmd = Buff[8] & 0xFF;

				intDataLen = HighLowToInt(Buff[9], Buff[10]);

				if (intDataLen > 0) {
					aryData = new byte[intDataLen];

					System.arraycopy(Buff, 11, aryData, 0, intDataLen);

					f.aryData = aryData;

					f.strData = getFrameData(f.aryData);

				}
			}
		} else {
			f = null;
		}
	}

	public static String decodeFrameData(byte[] buff) {
		String strData = "";

		if (buff != null) {
			try {
				strData = new String(buff, "GBK");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		return strData;
	}

	public static String getFrameData(byte[] buff) {
		return decodeFrameData(buff);
	}

	private static byte getCheckSum(byte[] Buff) {

		return getCheckSum(Buff, Buff.length - 1);
	}

	private static byte getCheckSum(byte[] Buff, int len) {
		byte b = 0;

		for (int i = 4; i < len; i++) {
			b ^= (0xff & Buff[i]);
		}

		return b;
	}

	private static void IntToHighLowByte(byte[] aryData, int intS, int intData) {
		int hValue = (intData & 0xFF00) >> 8;
		int lValue = intData & 0xFF;

		aryData[intS] = (byte) hValue;
		aryData[intS + 1] = (byte) lValue;
	}

	public static int HighLowToInt(byte hb, byte lb) {
		int intH = hb & 0xFF;
		int intL = lb & 0xFF;

		String strBinary = DecToBinary(intH, 8) + DecToBinary(intL, 8);

		return Integer.valueOf(strBinary, 2);
	}

	public static String DecToBinary(int intDec) {
		return Integer.toBinaryString(intDec);
	}

	public static String DecToBinary(int intDec, int intLen) {
		String strBinary = "";
		String strZ = "";
		int intStrLen = 0;

		strBinary = Integer.toBinaryString(intDec);

		intStrLen = intLen - strBinary.length();

		if (intStrLen > 0) {
			for (int i = 0; i < intStrLen; i++) {
				strZ += "0";
			}

			strBinary = strZ + strBinary;
		}

		return strBinary;
	}

	public static int parseInt(byte[] data) {
		return Integer.parseInt(getFrameData(data));
	}

	public static ArrayList<byte[]> split(byte[] src, char sp) {
		return split(src, (byte) sp);
	}

	public static ArrayList<byte[]> split(byte[] src, byte sp) {
		ArrayList<byte[]> result = new ArrayList<byte[]>();
		int p = 0;
		for (int i = 0; i < src.length; i++) {
			if (src[i] == sp) {
				if (p < i) {
					byte[] temp = new byte[i - p];
					System.arraycopy(src, p, temp, 0, i - p);
					result.add(temp);
				} else {

					result.add(new byte[0]);
				}
				p = i + 1;
			}
		}
		if (p < src.length) {
			byte[] temp = new byte[src.length - p];
			System.arraycopy(src, p, temp, 0, src.length - p);
			result.add(temp);
		}
		return result;
	}

	public static void logln(String str) {
		System.out.println(str);
	}

	public static void logln() {
		System.out.println();
	}

	public static void log(String str) {
		System.out.print(str);
	}

	public static void output(ArrayList<byte[]> datas) {
		System.out.println(datas.size());
		for (byte[] b : datas) {
			logln(new String(b));
		}
		System.out.println("-----");
	}

	public static void hexoutput(byte[] datas) {

		for (byte b : datas) {
			log(String.format("%02x ", b & 0xff));
		}

	}
}
