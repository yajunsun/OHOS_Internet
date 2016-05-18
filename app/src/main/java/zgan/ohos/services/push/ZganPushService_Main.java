package zgan.ohos.services.push;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import zgan.ohos.utils.Frame;

import java.util.LinkedList;

public class ZganPushService_Main implements Runnable {
	private java.util.Queue<byte[]> Queue=new LinkedList<byte[]>();
	private Handler _handler;
	public ZganPushService_Main(java.util.Queue _queue,Handler mHandler){
		Queue=_queue;
		_handler=mHandler;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		while(true){
			
			try {
				Thread.sleep(100);
			
				if (Queue!=null && Queue.size() > 0) {
					byte[] resultByte=null;
					resultByte=Queue.poll();
					
					Frame f=new Frame(resultByte);
					
					
					//处理心跳包
					if(f!=null && f.platform==7){
						if(f.mainCmd==13 && f.subCmd==21 && f.strData!=null && f.strData.equals("0")){
							
							ZganPushServiceTools.isLoginOK=true;
							
							Log.i("ZganPushService_Main", "登录消息服务器成功");
						}					
						//布防
						else if(f.version==1 && f.mainCmd==13 && f.subCmd==1){
							Message temp = new Message();
							temp.obj = f.strData;
							temp.what = 1;
							
							
							int msgId = spliteData(f.strData);
							
							replyMsg(f.mainCmd,f.subCmd,msgId);						
							
							temp.arg1=msgId;
							_handler.sendMessage(temp);
						}
						
						//撤防
						else if(f.version==1 && f.mainCmd==13 && f.subCmd==2){
							Message temp = new Message();
							temp.obj = f.strData;
							temp.what = 2;
							
							int msgId = spliteData(f.strData);
							
							replyMsg(f.mainCmd,f.subCmd,msgId);
							temp.arg1=msgId;
							_handler.sendMessage(temp);
						}
						
						//入侵			
						else if(f.version==1 && f.mainCmd==13 && f.subCmd==3){
							Message temp = new Message();
							temp.obj = f.strData;
							temp.what = 3;
							
							int msgId = spliteData(f.strData);
							
							replyMsg(f.mainCmd,f.subCmd,msgId);
							temp.arg1=msgId;
							_handler.sendMessage(temp);
						}
						
						//归家\离家
						else if(f.version==1 && f.mainCmd==13 && f.subCmd==23){
							Message temp = new Message();
							temp.obj = f.strData;
							temp.what = 23;
							
							int msgId = spliteData(f.strData);
							
							replyMsg(f.mainCmd,f.subCmd,msgId);
							temp.arg1=msgId;
							_handler.sendMessage(temp);
						}										
					}
					
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	

	/**
	 * 反馈信息给服务器
	 * 
	 * @param subCmd
	 *            子功能号
	 * @param msgId
	 *            消息ID
	 */
    public void replyMsg(byte mainCmd, int subCmd, int msgId) {
    	Frame f = new Frame();
    	f.mainCmd = mainCmd;
    	f.subCmd = subCmd;
    	f.strData = msgId + "\t" + 0;

    	ZganPushServiceTools.toSendMsg(f);
    }

	private int spliteData(String recieveData) {
		if (TextUtils.isEmpty(recieveData)) {
			return 0;
		}
		String[] str = recieveData.split("\t");

		if (str.length < 2) {
			return 0;
		}
		return Integer.valueOf(str[1]);
	}
}
