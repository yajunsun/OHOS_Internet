package zgan.ohos.services.push;


import zgan.ohos.utils.Frame;
import zgan.ohos.utils.FrameTools;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

public class ZganPushServiceTools {
	//发送消息队列
	public static Queue<byte[]> PushQueue_Send = new LinkedList<byte[]>();
	
	//接收消息队列
	public static Queue<byte[]> PushQueue_Receive = new LinkedList<byte[]>();
	
	
	public static boolean Thread_Ping=false;
	
	public static int Thread_PingTime=0;
	public static int Thread_PingOutTime=200;//20秒
	
	public static boolean isConnect=false;
	
	public static boolean ISmsgThread=false;
	
	public static Calendar PingTime=null;
	public static Calendar PingSendTime=null;
	
	public static boolean isLoginOK=false;
	
	//发送消息
	public static void toSendMsg(Frame f){
		byte[] Buff=null;
		Buff= FrameTools.getFrameBuffData(f);
		
		if(Buff!=null){
			
			PushQueue_Send.offer(Buff);
		}
	}
}
