package zgan.ohos.Contracts;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by yajunsun on 2015/11/12.
 */
public interface IImageloader {
    void onDownloadSucc(Bitmap bitmap,String c_url,View imageView,int w,int h);
}
