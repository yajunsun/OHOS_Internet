package zgan.ohos.utils;

import android.content.res.Resources;
import android.os.Handler;

import zgan.ohos.MyApplication;
import zgan.ohos.services.login.ZganLoginService;

public class SystemUtils {
    public static int getScreenOrientation() {
        return Resources.getSystem().getConfiguration().orientation;
    }

    private static boolean isLogin = false;
    private static boolean isCommunityLogin=false;
    public static final String FORRESULT = "forresult";

    private static String address;
    private static String village;
    private static String housenum;

    public static String getAddress() {
        return address;
    }

    public static void setAddress(String address) {
        SystemUtils.address = address;
    }

    public static String getVillage() {
        return village;
    }

    public static void setVillage(String village) {
        SystemUtils.village = village;
    }

    public static String getHousenum(){return housenum;}

    public static void setHousenum(String housenum){
        SystemUtils.housenum=housenum;
    }

    //private static String SID="";

    public static boolean getIsLogin() {
        return isLogin;
    }

    public static void setIsLogin(boolean islogin) {
        isLogin = islogin;
    }

    public static boolean getIsCommunityLogin(){return isCommunityLogin;}

    public static void setIsCommunityLogin(boolean islogin)
    {
        isCommunityLogin=islogin;
    }

//    public static String getSID() {
//        return SID;
//    }
//
//    public static void setSID(String _SID) {
//        SID = _SID;
//    }

}
