package zgan.ohos.utils;

import android.content.Context;
import android.util.Log;

import java.lang.Thread.UncaughtExceptionHandler;

public class UncaughtException implements UncaughtExceptionHandler {

	private static UncaughtException instance; // �������ã������������ɵ���ģ���Ϊ����һ��Ӧ�ó�������ֻ��Ҫһ��UncaughtExceptionHandlerʵ��
	private Context context;

	private UncaughtException() {
	}

	public synchronized static UncaughtException getInstance() { // ͬ�����������ⵥ����̻߳����³����쳣
		if (instance == null) {
			instance = new UncaughtException();
		}
		return instance;
	}

	public void init(Context ctx) { // ��ʼ�����ѵ�ǰ�������ó�UncaughtExceptionHandler������
		this.context = ctx;
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) { // ����δ������쳣����ʱ���ͻ����������
		Log.d("suntest", "uncaughtException, thread: " + thread + " name: "
				+ thread.getName() + " id: " + thread.getId() + "exception: "
				+ ex);
		generalhelper.ToastShow(context, "�����쳣");
	}

}
