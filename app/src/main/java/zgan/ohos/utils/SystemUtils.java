package zgan.ohos.utils;

import android.content.res.Resources;

public class SystemUtils {
    public static int getScreenOrientation() {
        return Resources.getSystem().getConfiguration().orientation;
    }

    private static boolean isLogin = false;
    private static boolean isCommunityLogin = false;
    public static final String FORRESULT = "forresult";

    private static String address;
    private static String village;
    private static String shop;
    private static String property;
    private static String Fname;

    public static String getShop() {
        return shop;
    }

    public static void setShop(String _shop) {
        SystemUtils.shop = _shop;
    }

    public static String getProperty() {
        return property;
    }

    public static void setProperty(String _property) {
        SystemUtils.property = _property;
    }

    public static String getFname() {
        return Fname;
    }

    public static void setFname(String _fname) {
        Fname = _fname;
    }

    public static String getAddress() {
        return address;
    }

    public static void setAddress(String _address) {
        SystemUtils.address = _address;
    }

    public static String getVillage() {
        return village;
    }

    public static void setVillage(String _village) {
        SystemUtils.village = _village;
    }


    //private static String SID="";

    public static boolean getIsLogin() {
        return isLogin;
    }

    public static void setIsLogin(boolean islogin) {
        isLogin = islogin;
    }

    public static boolean getIsCommunityLogin() {
        return isCommunityLogin;
    }

    public static void setIsCommunityLogin(boolean islogin) {
        isCommunityLogin = islogin;
    }

//    public static String getSID() {
//        return SID;
//    }
//
//    public static void setSID(String _SID) {
//        SID = _SID;
//    }

    public static Integer getIntValue(String strValue) {
        try {
            return Integer.valueOf(strValue);
        } catch (NumberFormatException nfe) {
            int dotIndex = strValue.indexOf(".");
            if (dotIndex > -1)
                return Integer.valueOf(strValue.substring(0, dotIndex));
        } catch (Exception e) {
        }
        return 0;
    }
}
