package zgan.ohos.Activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import zgan.ohos.R;
import zgan.ohos.utils.ImageLoader;

public class bitmaptshow extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bitmaptshow);
        ImageView ivimg = (ImageView) findViewById(R.id.ivimg);
        Bitmap bmp = ImageLoader.loadBitmap("decoded", 200, 200);
        if (bmp != null)
            ivimg.setImageBitmap(bmp);
    }
}
