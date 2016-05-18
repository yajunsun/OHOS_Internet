package zgan.ohos.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Blob;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by yajunsun on 2015/11/12.
 */
public final class generalhelper {

    public static boolean hasSDCard() {
        boolean b = false;
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            b = true;
        }
        return b;
    }

    public static String getExtPath() {
        String path = "";
        if (hasSDCard()) {
            path = Environment.getExternalStorageDirectory().getPath();
        }
        return path;
    }

    public static String getPackagePath(Activity mActivity) {
        return mActivity.getFilesDir().toString();
    }


    public static String getImageName(String url) {
        String imageName = "";
        if (url != null) {
            imageName = url.substring(url.lastIndexOf("/") + 1);
        }
        return imageName;
    }

    public static String getmyPhoneNumber(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String NativePhoneNumber
                = telephonyManager.getLine1Number();
        return NativePhoneNumber;
    }

    public static Date getDateFromString(String datestr, Date date) {
        String formartstr = "yyyy-MM-dd HH:mm:ss";
        if (datestr.contains("T"))
            formartstr = "yyyy-MM-dd'T'HH:mm:ss";
        return getDateFromString(datestr, date, formartstr);
    }

    public static Date getDateFromString(String datestr, Date date,
                                         String formartstr) {
        try {

            if (formartstr.equals(""))
                formartstr = "yyyy-MM-dd HH:mm:ss";
            SimpleDateFormat sdf = new SimpleDateFormat(formartstr);
            date = sdf.parse(datestr);
            return date;
            // return true;
        } catch (Exception e) {
            // TODO: handle exception
            // return false;
            return date;
        }
    }

    public static String gettimestamp() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        return format.format(date);
    }

    public static String getStringFromDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String str = format.format(date);
        return str;
    }

    public static String getStringFromDate(Date date, String formart) {
        SimpleDateFormat format = new SimpleDateFormat(formart);
        String str = format.format(date);
        return str;
    }

    public static byte[] blob2ByteArr(Blob blob) throws Exception {
        byte[] b = null;
        try {
            if (blob != null) {
                long in = 0;
                b = blob.getBytes(in, (int) (blob.length()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("fault");
        }
        return b;
    }

    public static void ToastShow(Context context, Object text) {
        if (context == null)
            return;
        if (text == null)
            text = "程序异常";
        Toast.makeText(context, text.toString(), Toast.LENGTH_SHORT).show();
    }

    // public static
    public class returnsta {
        public static final String success = "success", failure = "failure";
    }


    /**
     * @param str frame.strData
     * @return "0\t"取0  用于和服务器通信返回状态码
     */
    public static String getSocketeStringResult(String str) {
        String result = str.replace("\t", ",");
        if (result.startsWith(","))
            result = result.substring(1, result.length());
        if (result.endsWith(","))
            result = result.substring(0, result.length() - 1);
        return result;
    }

    public final static int uploadhispic = 1;
    public final static int uploadheader = 2;

    public static NetworkInfo getNetworkInfo(Context context) {
        return nethelper.networkInfo(context);
    }


    /**
     * 获取年龄
     */
    public static String getAge(Date birthDay) throws Exception {
        Calendar cal = Calendar.getInstance();
        if (cal.before(birthDay)) {
            throw new IllegalArgumentException(
                    "The birthDay is before Now.It's unbelievable!");
        }
        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH) + 1;
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);

        cal.setTime(birthDay);
        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH);
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);

        int age = yearNow - yearBirth;

        if (monthNow <= monthBirth) {
            if (monthNow == monthBirth) {
                // monthNow==monthBirth
                if (dayOfMonthNow < dayOfMonthBirth) {
                    age--;
                }
            } else {
                // monthNow>monthBirth
                age--;
            }
        }

        return age + "";
    }

    /**********
     * 分享内容，比如发朋友圈
     ************/
    public static void shareMsg(Context context, String activityTitle,
                                String msgTitle, String msgText, String[] imgPaths) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        if (imgPaths == null || imgPaths.length == 0) {
            intent.setType("text/plain");
        } else {
            intent.setType("image/*");
            for (String imgPath : imgPaths) {
                File f = new File(imgPath);
                if (f != null && f.exists() && f.isFile()) {
                    Uri u = Uri.fromFile(f);
                    intent.putExtra(Intent.EXTRA_STREAM, u);
                }
            }
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
        intent.putExtra(Intent.EXTRA_TEXT, msgText);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent.createChooser(intent, activityTitle));
    }

    public static String[] netfiles = new String[]{
            "http://img38.ddimg.cn/1/29/1275307408-1_u_2.jpg",
            "http://img38.ddimg.cn/95/16/1290275708-1_u_2.jpg",
            "http://img38.ddimg.cn/18/14/1317235608-1_u_1.jpg",
            "http://image1.suning.cn/b2c/catentries/000000000133893154_1_800x800.jpg",
            "http://image4.suning.cn/b2c/catentries/000000000133892749_1_800x800.jpg",
            "http://image5.suning.cn/b2c/catentries/000000000132406160_1_800x800.jpg",
            "http://image1.suning.cn/b2c/catentries/000000000128313132_1_800x800.jpg",
            "http://image1.suning.cn/b2c/catentries/000000000132798170_1_800x800.jpg",
            "http://image1.suning.cn/b2c/catentries/000000000128267368_1_800x800.jpg",
            "http://image1.suning.cn/b2c/catentries/000000000133538991_1_800x800.jpg",
            "http://image3.suning.cn/b2c/catentries/000000000129818743_1_800x800.jpg",
            "http://image5.suning.cn/b2c/catentries/000000000133336820_1_800x800.jpg",
            "http://image1.suning.cn/b2c/catentries/000000000134415643_1_800x800.jpg",
            "http://image1.suning.cn/b2c/catentries/000000000105271566_1_800x800.jpg",
            "http://image5.suning.cn/b2c/catentries/000000000128989884_1_800x800.jpg",
            "http://image3.suning.cn/b2c/catentries/000000000133893418_1_800x800.jpg",
            "http://image3.suning.cn/b2c/catentries/000000000133961729_1_800x800.jpg",
            "http://image4.suning.cn/b2c/catentries/000000000138188207_1_800x800.jpg",
            "http://image5.suning.cn/b2c/catentries/000000000131043167_1_800x800.jpg",
            "http://image4.suning.cn/b2c/catentries/000000000132405603_1_800x800.jpg",
            "http://img3.wgimg.com/qqbuy/3251788547/item-00000000000000000000006EC1D25B03.0.jpg/600?56178A83",
            "http://img1.wgimg.com/qqbuy/2269255001/item-00000000000000000000007187421559.0.jpg/600?56431167",
            "http://img1.wgimg.com/qqbuy/3248577217/item-00000000000000000000006EC1A15AC1.0.jpg/600?561F1592",
            "http://img1.wgimg.com/qqbuy/3757503597/item-000000000000000000000071DFF6F46D.0.jpg/600?56430B19",
            "http://img0.wgimg.com/qqbuy/980897520/item-0000000000000000000000713A774EF0.0.jpg/600?561F1393",
            "http://img1.wgimg.com/qqbuy/3249429193/item-00000000000000000000006EC1AE5AC9.0.jpg/600?561E3810",
            "http://img2.wgimg.com/qqbuy/3760782254/item-000000000000000000000071E028FBAE.0.jpg/600?56431080",
            "http://img3.wgimg.com/qqbuy/2192688067/item-00000000000000000000007182B1C3C3.0.jpg/600?56431923",
            "http://img1.wgimg.com/qqbuy/3273350337/item-00000000000000000000006EC31B5CC1.4.jpg/600?5640469B",
            "http://img3.wgimg.com/qqbuy/3431054835/item-000000000000000000000070CC81BDF3.0.jpg/600?56493EBC",
            "http://img3.wgimg.com/qqbuy/3262209019/item-00000000000000000000006EC2715BFB.0.jpg/600?5638B9AC",
            "http://img1.wgimg.com/qqbuy/2191375613/item-000000000000000000000071829DBCFD.0.jpg/600?561C9D28",
            "http://img1.wgimg.com/qqbuy/3270007941/item-00000000000000000000006EC2E85C85.0.jpg/600?5630A6BC",
            "http://img2.wgimg.com/qqbuy/3429678562/item-000000000000000000000070CC6CBDE2.0.jpg/600?55C696DE",
            "http://img1.wgimg.com/qqbuy/3271908529/item-00000000000000000000006EC3055CB1.0.jpg/600?5649AAC7",
            "http://img1.wgimg.com/qqbuy/627296205/item-0000000000000000000000722563C7CD.0.jpg/600?562D86AA",
            "http://img1.wgimg.com/qqbuy/3272694969/item-00000000000000000000006EC3115CB9.0.jpg/600?5604B5E4",
            "http://img2.wgimg.com/qqbuy/3998447498/item-000000000000000000000071EE53778A.0.jpg/600?562F4E6C",
            "http://img0.wgimg.com/qqbuy/3429547488/item-000000000000000000000070CC6ABDE0.0.jpg/600?56331752",
            "http://img3.wgimg.com/qqbuy/3274726615/item-00000000000000000000006EC3305CD7.0.jpg/600?5618863C",
            "http://img1.wgimg.com/qqbuy/2794522257/item-000000000000000000000071A6910691.0.jpg/600?55F2877D",
            "http://img0.wgimg.com/qqbuy/358339884/item-000000000000000000000072155BD52C.0.jpg/600?5627618C",
            "http://img1.wgimg.com/qqbuy/3480584225/item-00000000000000000000006ECF758021.0.jpg/600?561F8D11",
    };

}
