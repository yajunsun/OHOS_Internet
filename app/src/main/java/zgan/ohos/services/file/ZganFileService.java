package zgan.ohos.services.file;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import zgan.ohos.services.login.ZganLoginService;
import zgan.ohos.utils.Frame;

public class ZganFileService {

	private static boolean ServiceRin=false;
    private static Thread _threadListen;
    private static Thread _threadMain;
	private static ZganFileService_Listen ztl;
	public static String UserName="";
    
    public final static int PLATFORM_APP = 0xF;
    public final static int PLATFORM_MSG = 0x07;
    public final static int VERSION_1 = 0x01;
    public final static int VERSION_2 = 0x02;
    public final static int MAIN_CMD=0x0F;

    
	public ZganFileService(){
		
	}
	
	/**
	 * 得到当前用户绑定设备号
	 * */
	public static void getDeviceCode(Handler _handler) {
		Frame f = createFrame();
		f.subCmd = 80;
		f.strData = UserName;
		f.version=2;
		f._handler=_handler;

		toGetData(f);
	}
	
	/**
	 * 获取服务器数据(通用)
	 * */
	public static void toGetServerData(int subcmd,String strData,Handler _handler) {
		Frame f = createFrame();
		f.subCmd = subcmd;
		f.strData = strData;
		f._handler=_handler;
		
		toGetData(f);
	}
	
	/**
	 * 获取服务器数据(通用)
	 * */
	public static void toGetServerData(int subcmd,String[] aryParam,Handler _handler) {
		Frame f = createFrame();
		f.subCmd = subcmd;
		f.strData = getParam(aryParam);
		f._handler=_handler;

		toGetData(f);
	}
	
	/**
	 * 获取服务器数据(通用)
	 * */
	public static void toGetServerData(int subcmd,String[] aryParam,Handler _handler,int intVar) {
		Frame f = createFrame();
		f.subCmd = subcmd;
		f.strData = getParam(aryParam);
		f._handler=_handler;
		f.version=intVar;
		
		toGetData(f);
	}
	
	/**
	 * 获取服务器数据(通用)
	 * */
	public static void toGetServerData(int subcmd,String[] aryParam,Handler _handler,int intVar,int mainCmd) {
		Frame f = createFrame();
		f.mainCmd= (byte)mainCmd;
		f.subCmd = subcmd;
		f.strData = getParam(aryParam);
		f._handler=_handler;
		f.version=intVar;
		
		toGetData(f);
	}
	
	private static String getParam(String[] aryParam){
		String strParam="";
		
		if(aryParam!=null){
			for (String oneRow : aryParam) {
				strParam+="\t"+oneRow;
			}
			
			if(strParam!=null && !strParam.equals("")){
				strParam=strParam.substring(1);
			}
		}
		
		return strParam;
	}
	
	/**
	 * 创建数据包
	 * */
	public static Frame createFrame() {
		Frame f = new Frame();
		f.platform = PLATFORM_APP;
		f.mainCmd = MAIN_CMD;
		f.version = VERSION_1;
		return f;
	}
	
	public static void toGetData(Frame f){
		ZganFileServiceTools.toGetFunction(f);
	}
	
	//启动文件服务线程
	public static void toStartFileService(Context context){
	    String PushServerIP="";
	    String strIP="";
	    String strUserName="";
	    int PushServerPort=0;
		
		if(!ServiceRin){			
			
			SharedPreferences ZganInfo=context.getSharedPreferences(ZganLoginService.ZGAN_DBNAME, Context.MODE_PRIVATE);
			PushServerIP=ZganInfo.getString(ZganLoginService.ZGAN_FILESERVER, null);			
			strUserName=ZganInfo.getString(ZganLoginService.ZGAN_USERNAME, null);			
			
			if(PushServerIP!=null && !PushServerIP.equals("")){
				strIP=PushServerIP.split(":")[0];
				PushServerPort= Integer.parseInt(PushServerIP.split(":")[1]);
			}
			
			//启动监听线程
			ztl=new ZganFileService_Listen(context,strIP,PushServerPort,strUserName);
			_threadListen=new Thread(ztl);
			_threadListen.start();
			
			//启动主线程
			ZganFileService_Main zm=new ZganFileService_Main(ZganFileServiceTools.PushQueue_Receive,
					ZganFileServiceTools.PushQueue_Function);
			_threadMain=new Thread(zm);
			_threadMain.start();		

			UserName=strUserName;		
			
			ServiceRin=true;
		}
	}
	
	//关闭文件服务线程
	public static void toCloseJTWSService(){
		if(ServiceRin){
			ServiceRin=false;
			
			_threadListen.interrupt();
			_threadMain.interrupt();			

		}
	}
}
