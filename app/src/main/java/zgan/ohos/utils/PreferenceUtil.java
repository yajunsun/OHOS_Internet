package zgan.ohos.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import zgan.ohos.MyApplication;


public class PreferenceUtil {

    private static final String PREFERENCE_NAME = "USER_INFO";

    private static final String Field_UserNmae = "zgan.ohos.userid";
    private static final String Field_PassWord = "zgan.ohos.password";
    private static final String Field_Residential = "zgan.ohos.Residential";
    private static final String Field_UsedTimes = "zgan.ohos.UsedTimes";
    private static final String Field_LastVersion = "zgan.ohos.LastVersion";
    private static String Field_SID;
    //小区云登录IP
    private static String Field_CommunityIP;
    //小区云登录端口
    private static String Field_CommunityPORT;

    private static PreferenceUtil preferenceUtil;

    private SharedPreferences sp;

    private Editor ed;

    private PreferenceUtil(Context context) {
        init(context);
    }

    public void init(Context context) {
        if (sp == null || ed == null) {
            try {
                sp = context.getSharedPreferences(PREFERENCE_NAME, 0);
                ed = sp.edit();
            } catch (Exception e) {
            }
        }
    }

    public static PreferenceUtil getInstance(Context context) {
        if (preferenceUtil == null) {
            preferenceUtil = new PreferenceUtil(context);
        }
        return preferenceUtil;
    }

    /***
     * 获取登陆用户名
     */
    public static String getUserName() {
        try {
            String username = getInstance(MyApplication.context).getString(Field_UserNmae, "");
            //Field_CommunityId="zgan.ohos."+username;
            Field_CommunityIP = "zgan.ohos.communityIp." + username;
            Field_CommunityPORT = "zgan.ohos.communityPORT." + username;
            Field_SID="zgan.ohos.SID." + username;
            return username;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /***
     * 保存登陆用户名
     */
    public static void setUserName(String username) {
        try {
            getInstance(MyApplication.context).saveString(Field_UserNmae, username);
            //Field_CommunityId="zgan.ohos."+username;
            Field_CommunityIP = "zgan.ohos.communityIp." + username;
            Field_CommunityPORT = "zgan.ohos.communityPORT." + username;
            Field_SID="zgan.ohos.SID." + username;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 获取登陆密码
     *
     * @return
     */
    public static String getPassWord() {
        try {
            return getInstance(MyApplication.context).getString(Field_PassWord, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /***
     * 保存登陆密码
     *
     * @param password
     */
    public static void setPassWord(String password) {
        try {
            getInstance(MyApplication.context).saveString(Field_PassWord, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 获取小区号
     *
     * @return
     */
    public static String getResidential() {
        try {
            return getInstance(MyApplication.context).getString(Field_Residential, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /***
     * 保存小区号
     *
     * @param residential
     */
    public static void setResidential(String residential) {
        try {
            getInstance(MyApplication.context).saveString(Field_Residential, residential);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getUsedTimes() {
        try {
            return getInstance(MyApplication.context).getInt(Field_UsedTimes, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void setUsedTimes(int times) {
        try {
            getInstance(MyApplication.context).saveInt(Field_UsedTimes, times);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getLastVersion() {
        try {
            return getInstance(MyApplication.context).getInt(Field_LastVersion, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    public static void setLastVersion(int versionCode) {
        try {
            getInstance(MyApplication.context).saveInt(Field_LastVersion, versionCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //    public static String getCommunityId()
//    {
//        try {
//            return getInstance(MyApplication.context).getString(Field_CommunityId, "0");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "0";
//    }
//    public static void setCommunityId(String communityId)
//    {
//        try {
//            getInstance(MyApplication.context).saveString(Field_CommunityId, communityId);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /***
     * 获取小区服务器IP
     *
     * @return
     */
    public static String getCommunityIP() {
        try {
            return getInstance(MyApplication.context).getString(Field_CommunityIP, "0");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0";
    }
    /***
     * 设置小区服务器IP
     *
     * @param communityId
     */
    public static void setCommunityIP(String communityId) {
        try {
            getInstance(MyApplication.context).saveString(Field_CommunityIP, communityId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 获取小区服务器端口
     *
     * @return
     */
    public static int getCommunityPORT() {
        try {
            return getInstance(MyApplication.context).getInt(Field_CommunityPORT, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /***
     * 设置小区服务器端口
     *
     * @param port
     */
    public static void setCommunityPORT(int port) {
        try {
            getInstance(MyApplication.context).saveInt(Field_CommunityPORT, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 获取室内机SID
     * @return
     */
    public static String getSID()
    {
        try {
            return getInstance(MyApplication.context).getString(Field_SID, "0");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "0";
    }

    /***
     * 设置室内机SID
     * @param sID
     */
    public static void setSID(String sID)
    {
        try {
            getInstance(MyApplication.context).saveString(Field_SID, sID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void saveLong(String key, long l) {
        ed.putLong(key, l);
        ed.commit();
    }

    public long getLong(String key, long defaultlong) {
        return sp.getLong(key, defaultlong);
    }

    public void saveBoolean(String key, boolean value) {
        ed.putBoolean(key, value);
        ed.commit();
    }

    public boolean getBoolean(String key, boolean defaultboolean) {
        return sp.getBoolean(key, defaultboolean);
    }

    public void saveInt(String key, int value) {
        if (ed != null) {
            ed.putInt(key, value);
            ed.commit();
        }
    }

    public int getInt(String key, int defaultInt) {
        return sp.getInt(key, defaultInt);
    }

    public String getString(String key, String defaultValue) {
        return sp.getString(key, defaultValue);
    }

    public String getString(Context context, String key, String defaultValue) {
        if (sp == null || ed == null) {
            sp = context.getSharedPreferences(PREFERENCE_NAME, 0);
            ed = sp.edit();
        }
        if (sp != null) {
            return sp.getString(key, defaultValue);
        }
        return defaultValue;
    }

    public void saveString(String key, String value) {
        ed.putString(key, value);
        ed.commit();
    }

    public void remove(String key) {

        ed.remove(key);
        ed.commit();
    }

    public void clear() {

        ed.clear();
        ed.commit();
    }

    public void destroy() {
        sp = null;
        ed = null;
        preferenceUtil = null;
    }
}
