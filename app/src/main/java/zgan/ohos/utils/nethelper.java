package zgan.ohos.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class nethelper {

	static ConnectivityManager cnManager;

	public static NetworkInfo networkInfo(Context context) {
		if (cnManager == null)
			cnManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo nInfo = cnManager.getActiveNetworkInfo();
		return nInfo;
	}

	public static boolean isServiceStarted(Context context, String PackageName) {
		boolean isStarted = false;

		try {
			int intGetTastCounter = 1000;

			ActivityManager mActivityManager = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);

			List<ActivityManager.RunningServiceInfo> mRunningService = mActivityManager
					.getRunningServices(intGetTastCounter);

			for (ActivityManager.RunningServiceInfo amService : mRunningService) {
				if (0 == amService.service.getPackageName().compareTo(
						PackageName)) {
					isStarted = true;
					break;
				}
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		}

		return isStarted;
	}

	public static String getDataFromStream(InputStream is)
	{
		String result="";
		try {
			byte[] buffer = new byte[8 * 1024];
			int intLen = is.read(buffer);
			result=new String(buffer);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
}
