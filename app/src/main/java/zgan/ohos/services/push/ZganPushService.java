package zgan.ohos.services.push;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import zgan.ohos.services.login.ZganLoginService;
import zgan.ohos.R;
import zgan.ohos.utils.PreferenceUtil;


public class ZganPushService extends Service {
	private static final String TAG = "ZganPushService";
    private int MsgTypeID;  
    private Thread _threadListen;
    private Thread _threadMain;
    private Resources res = null;
	
	// 设置window type
	
   
    private ZganPushService_Listen zpl;
    public static boolean ServiceRin=false;
    private Intent intentbb;
    
    @Override
    public void onCreate() {  
        super.onCreate();  

    }  
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		
		return null;
	}
	 
	
	
	
	@Override
    public void onStart(Intent intent, int startId) {
		Log.i(TAG, "消息启动.....");
        super.onStart(intent, startId); 
    }
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		
        //注册广播
        intentbb = new Intent("com.zgan.youbao.broadcast");
		
		res = getApplicationContext().getResources();		
		
	    String PushServerIP="";
	    String strIP="";
	    String strUserName="";
	    int PushServerPort=0;
	    
		
		if(!ServiceRin){
			
			SharedPreferences ZganInfo=getSharedPreferences(ZganLoginService.ZGAN_DBNAME, Context.MODE_PRIVATE);
			
			PushServerIP=ZganInfo.getString(ZganLoginService.ZGAN_PUSHSERVER, null);
			
			strUserName=ZganInfo.getString(ZganLoginService.ZGAN_USERNAME, null);
			
			if(PushServerIP!=null && !PushServerIP.equals("")){
				strIP=PushServerIP.split(":")[0];
				PushServerPort= Integer.parseInt(PushServerIP.split(":")[1]);
			}
			
			//启动监听线程
			zpl=new ZganPushService_Listen(ZganPushService.this,strIP,PushServerPort,strUserName);
			_threadListen=new Thread(zpl);
			_threadListen.start();
			
			//启动主线程
			ZganPushService_Main zm=new ZganPushService_Main(ZganPushServiceTools.PushQueue_Receive,mHandler);
			_threadMain=new Thread(zm);
			_threadMain.start();		
			
			ServiceRin=true;
		}
		
		Log.i(TAG, "消息启动成功.....");
        return START_STICKY;
    }
	
	public int GetMsgTypeID() {  
        return MsgTypeID;  
    }

	
	 @Override
	 public void onDestroy() { 
		 super.onDestroy();
		_threadMain.interrupt();
		_threadListen.interrupt();
		
		 
		ServiceRin=false;
		 
		Log.i("ZganPushService", "消息服务关闭...");
		
		System.exit(0);
			 
	 }

	@SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			int what = msg.what;
			
			intentbb.putExtra("ZganMsgTypeID", what);
			intentbb.putExtra("ZganMsgData", msg.obj.toString());
			sendBroadcast(intentbb);
			
			switch (what) {
				case 1: 
					//布防消息
					toMsgID_1(msg.arg1,msg.obj.toString());
				break;
				case 2: 
					toMsgID_2(msg.arg1,msg.obj.toString());
				break;
				case 3: 
					toMsgID_3(msg.arg1,msg.obj.toString());
				break;
				case 23: 
					//归家\离家
					toMsgID_23(msg.arg1,msg.obj.toString());
				break;
			}
		}
	};

	//布防消息
	private void toMsgID_1(int msgId,String strData){
		boolean openDefence = PreferenceUtil.getInstance(getApplicationContext())
				.getBoolean("defenceMessage", true);
		
		if(openDefence){
			String actor = getDefenceActor(strData);
			String strTime=getTime(strData, 2);
			String strContent="";
			
			strContent=res.getString(R.string.push_txt1);			

			strContent=strContent.replace("{name}", actor);
			strContent=strContent.replace("{time}", strTime);
			
			//createNotification(msgId, strContent, ImprintingActivity.class);
		}
	}
	 
	//撤防消息
	private void toMsgID_2(int msgId,String strData){
		boolean openDefence = PreferenceUtil.getInstance(getApplicationContext())
				.getBoolean("defenceMessage", true);
		//是否提示消息
		
		if (openDefence) {
			String actor = getDefenceActor(strData);
			String strTime=getTime(strData, 2);
			String strContent="";
			
			strContent=res.getString(R.string.push_txt2);			

			strContent=strContent.replace("{name}", actor);
			strContent=strContent.replace("{time}", strTime);
			
			//createNotification(msgId, strContent, ImprintingActivity.class);
		}
	}
	
	//入侵消息
	private void toMsgID_3(int msgId,String strData){
		boolean alarmMessage = PreferenceUtil.getInstance(getApplicationContext())
				.getBoolean("alarmMessage", true);
		
		if(alarmMessage){
			String strTime=getTime(strData, 2);
			String strContent="";
			
			strContent=res.getString(R.string.push_txt5);			

			strContent=strContent.replace("{time}", strTime);
			
			//createNotification(msgId, strContent, ImprintingActivity.class);
			
			
			
		}
	}
	
	private void toMsgID_23(int msgId,String strData){
		boolean goHome = PreferenceUtil.getInstance(
				getApplicationContext()).getBoolean(
				"goHomeMessage", true);
		if (goHome) {
			String actor = getNickname(strData);
			String strTime=getTime(strData, 4);
			String strContent="";
			
			
			if (getHomeType(strData) == 0) { // 离家
				
				strContent=res.getString(R.string.push_txt4);	
				
			} else if (getHomeType(strData) == 1) { // 归家
				strContent=res.getString(R.string.push_txt3);
			}
			
			strContent=strContent.replace("{name}", actor);
			strContent=strContent.replace("{time}", strTime);
			
			//createNotification(msgId, strContent, ImprintingActivity.class);
		}
	}
	
	/**
	 * 创建通知栏
	 */
	private void createNotification(int notificationId,String content, Class<?> cls) {
		Intent intent = new Intent(this, cls);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				intent, 0);

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.mipmap.ic_launcher,
				content, System.currentTimeMillis());
		                                                          
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults = Notification.DEFAULT_SOUND;
//		notification.setLatestEventInfo(getApplicationContext(), getApplicationContext().getResources().getString(R.string.remind),
//				content, pendingIntent);
                                                                    
		notificationManager.notify(notificationId, notification);
	}
	
	/**
	 * 获取布防撤防操作者
	 * 
	 * @param recieveData
	 * @return
	 */
	private String getDefenceActor(String recieveData) {
		if (TextUtils.isEmpty(recieveData)) {
			return "";
		}
		String[] str = recieveData.split("\t");
		if (str.length < 5) {
			return "";
		}
		return str[4].trim();
	}
	
	/**
	 * 获取归家离家人的昵称
	 * 
	 * @param recieveData
	 * @return
	 */
	private String getNickname(String recieveData) {
		if (TextUtils.isEmpty(recieveData)) {
			return "";
		}
		String[] str = recieveData.split("\t");
		if (str.length < 2) {
		    return "";
		}
		return str[2].trim();
	}
	
    
    
	/**
	 * 获取推送时间
	 * 
	 * @param recieveData
	 *            推送信息
	 * @return
	 */
	private String getTime(String recieveData, int index) {
		if (TextUtils.isEmpty(recieveData)) {
			return getApplicationContext().getResources().getString(R.string.just_now);
		}
		String[] str = recieveData.split("\t");
		String[] time = str[index].split(" ");
		return time[1];
		                                                                
	}
	
	/**
	 * 归家离家状态【0表示离家、1表示归家】
	 * 
	 * @param recieveData
	 * @return
	 */
	private int getHomeType(String recieveData) {
		if (TextUtils.isEmpty(recieveData)) {
			return -1;
		}
		String[] str = recieveData.split("\t");
		return Integer.valueOf(str[3]);
	}
}
