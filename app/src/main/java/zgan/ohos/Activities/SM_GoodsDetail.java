package zgan.ohos.Activities;

import android.view.View;
import android.widget.ImageView;

import zgan.ohos.R;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.ImageLoader;

/**
 * Created by yajunsun on 16/10/18.
 */
public class SM_GoodsDetail extends myBaseActivity {
    ImageView ivpreview;
    int detailtype;
    String detailview;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_sm_goods_detail);
        ivpreview = (ImageView) findViewById(R.id.iv_preview);
        detailtype = getIntent().getIntExtra("detailtype", 2);
        detailview = getIntent().getStringExtra("detailview");
        View back=findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (detailtype == 2) {
            ImageLoader.bindBitmap(detailview, ivpreview);
            ivpreview.setMaxHeight(Math.round(5 * AppUtils.getDensity(SM_GoodsDetail.this) * AppUtils.getWindowSize(SM_GoodsDetail.this).x));
        }
    }

    @Override
    public void ViewClick(View v) {

    }
}
