package zgan.ohos.services.push;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import zgan.ohos.utils.Frame;
import zgan.ohos.utils.FrameTools;

public class ZganPushService_Listen implements Runnable {
	private String ServerIP="";
	private int ServerPort=0;
	private String UName="";
    private Context _context;
    private int ServerState=0;
    private ZganSocketClient zsc;
	
	public ZganPushService_Listen(Context context,String _ip,int _port,String strUName){
		_context=context;
		ServerIP=_ip;
		ServerPort=_port;
		UName=strUName;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		zsc=new ZganSocketClient(ServerIP,ServerPort,
				ZganPushServiceTools.PushQueue_Send,ZganPushServiceTools.PushQueue_Receive);
		zsc.toStartClient();
		zsc.toStartPing(0xF, FrameTools.Frame_MainCmd_Ping);
		zsc.ThreadName="ZganPushService";
		
		while(true){
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			boolean isNet=isNetworkAvailable(_context);	
			
			if(ServerState==1){
				if(!isNet){					
					ServerState=2;
				}
				
				if(zsc.client.isClosed()){
					ServerState=2;
				}
				
				if(!zsc.isRun){
					ServerState=2;
				}
				
			}else if(ServerState==0){
				
				if(isNet){
					Log.i("ZganPushService_Listen", "重新连接");

					ServerState=3;
					
					if(zsc.toConnectServer()){						
						ServerState=1;
						
						LoginMsgServer(UName);
					}else{
						ServerState=0;
					}

				}
				
			}else if(ServerState==2){
				//网络断开
				zsc.toConnectDisconnect();
				ServerState=0;
			}
		}		

	}
	
	 public boolean isNetworkAvailable(Context context) {
	        ConnectivityManager cm = (ConnectivityManager) context
	                .getSystemService(Context.CONNECTIVITY_SERVICE);
		 if (cm == null)
	        {     
	  
	        }   
	        else   
	        {  
	            //如果仅仅是用来判断网络连接则可以使用 cm.getActiveNetworkInfo().isAvailable();  
	            NetworkInfo[] info = cm.getAllNetworkInfo();
	            if (info != null)  
	            {     
	                for (int i = 0; i < info.length; i++)  
	                {     
	                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
	                    {     
	                        return true;     
	                    }     
	                }     
	            }     
	        }     
	        
	        return false;     
	    }  

	
	//登录消息服务器
    public void LoginMsgServer(String user) {
        Frame f = new Frame();
        f.platform = 4;
        f.mainCmd = 0x0d;
        f.subCmd = 21;
        f.strData = user;
        
        UName=user;
       
        Log.i("ZganPushService_Listen", "登录消息服务器");
        
        ZganPushServiceTools.toSendMsg(f);
    }
}
