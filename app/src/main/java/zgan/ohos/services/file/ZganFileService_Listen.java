package zgan.ohos.services.file;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;


import zgan.ohos.services.push.ZganSocketClient;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.FrameTools;

public class ZganFileService_Listen implements Runnable {
	private int ServerPort=0;
	private String ServerIP="";
	private String UName="";
    private Context _context;
    private int ServerState=0;
    private ZganSocketClient zsc;
	
	public ZganFileService_Listen(Context context,String _ip,int _prot,String strUName){
		_context=context;
		ServerPort=_prot;
		ServerIP=_ip;
		UName=strUName;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		zsc=new ZganSocketClient(ServerIP,ServerPort,
				ZganFileServiceTools.PushQueue_Send,ZganFileServiceTools.PushQueue_Receive);
		zsc.toStartClient();
		zsc.toStartPing(0xF, FrameTools.Frame_MainCmd_Ping);
		zsc.ThreadName="ZganFileService";
		
		while(true){
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			boolean isNet=isNetworkAvailable(_context);	
			
			if(ServerState==1){
				
				//				//判断是否登录成功
				//				if(!ZganFileServiceTools.isLoginOK && ZganFileServiceTools.PushQueue_Receive.size()>0){
				//					byte[] resultByte=null;
				//					resultByte=ZganFileServiceTools.PushQueue_Receive.poll();
				//
				//					Frame f=new Frame(resultByte);
				//
				//					if(f.mainCmd==0x0f && f.subCmd==21 && f.strData!=null && f.strData.equals("0")){
				//
				//						ZganFileServiceTools.isLoginOK=true;
				//
				//						Log.i("ZganFileService_Listen", "登录文件服务器成功");
				//					}
				//				}
				
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
					Log.i("ZganFileService_Listen", "重新连接");

					ServerState=3;
					
					if(zsc.toConnectServer()){						
						ServerState=1;
						
						ZganFileServiceTools.isConnect=true;
						
						LoginMsgServer(UName);
					}else{
						ServerState=0;
					}

				}
				
			}else if(ServerState==2){
				//网络断开
				Log.v("suntest","断开连接");
				Log.i("suntest","断开连接");
				ZganFileServiceTools.isConnect=false;
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
        f.mainCmd = 0x0f;
        f.subCmd = 21;
        f.strData = user;
        
        UName=user;

        ZganFileServiceTools.isLoginOK=false;
        
        ZganFileServiceTools.toSendMsg(f);
    }
}
