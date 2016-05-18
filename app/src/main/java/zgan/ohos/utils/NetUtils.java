package zgan.ohos.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetUtils {
    private NetUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * �ж������Ƿ�����
     *
     * @param context
     * @return
     */
    public static boolean isConnected(Context context) {

        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (null != connectivity) {

            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (null != info && info.isConnected()) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * �ж��Ƿ���wifi����
     */
    public static boolean isWifi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null)
            return false;
        return cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;

    }

    /**
     * ���������ý���
     */
    public static void openSetting(Activity activity) {
        Intent intent = new Intent("/");
        ComponentName cm = new ComponentName("com.android.settings",
                "com.android.settings.WirelessSettings");
        intent.setComponent(cm);
        intent.setAction("android.intent.action.VIEW");
        activity.startActivityForResult(intent, 0);
    }

    //解析登录服务器IP
    public static String toGetHostIP(String domin) {
        InetAddress x;
        String strIP = "0,0,0,0";

        try {
            x = java.net.InetAddress.getByName(domin);
            strIP = x.getHostAddress();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block

        }
        return strIP;
    }

    public static String getIp(String sip) {
        BigInteger intIp=new BigInteger(sip,10);
        String dip =intIp.toString(16);
        String[] items = new String[4];
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 4; i ++) {
            int index=i*2;
            items[i] = dip.substring(index, index + 2);
        }
        for (int i = 3; i > -1; i--) {
            builder.append(Integer.parseInt(items[i],16) + ".");
        }
        String IP = builder.toString();
        return IP.substring(0, IP.length() - 1);
    }

    public static String buildXMLfromNetData(String netString) {
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
        builder.append("<root>");
        builder.append(netString);
        builder.append("</root>");
        return builder.toString();
    }

}
