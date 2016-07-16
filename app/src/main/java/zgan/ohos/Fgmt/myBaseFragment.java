package zgan.ohos.Fgmt;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;

import zgan.ohos.R;
import zgan.ohos.utils.DataCacheHelper;

/**
 * Created by yajunsun on 2016/1/11.
 *
 * 本系统内定义的fragment的父类
 */
public class myBaseFragment extends Fragment {

    protected final static String TAG = "suntest";

    public void startActivityWithAnim(Activity activity, Intent intent) {
//        if (Build.VERSION.SDK_INT > 20)
//            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity).toBundle());
//        else {
        startActivity(intent);
        activity.overridePendingTransition(R.animator.enter, R.animator.exit);
        // }
    }

    public void startActivityWithAnimForResult(Activity activity, Intent intent, int requestCode) {
        if (Build.VERSION.SDK_INT > 20)
            startActivityForResult(intent, requestCode, ActivityOptions.makeSceneTransitionAnimation(activity).toBundle());
        else {
            startActivityForResult(intent, requestCode);
            activity.overridePendingTransition(R.animator.enter, R.animator.exit);
        }
    }

    protected void addCache(String param, String data) {
        try {
            DataCacheHelper.add2DiskCache(param, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
