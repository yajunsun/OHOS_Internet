package zgan.ohos.Activities;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import zgan.ohos.R;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.ImageLoader;

/**
 * Created by yajunsun on 16/10/18.
 */
public class SM_GoodsDetail extends myBaseActivity {
    ImageView ivpreview;TextView title;

    int detailtype;
    String detailview,name;
    float density;
    float windowW;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_sm_goods_detail);
        density=AppUtils.getDensity(SM_GoodsDetail.this);
        windowW=AppUtils.getWindowSize(SM_GoodsDetail.this).x;
        ivpreview = (ImageView) findViewById(R.id.iv_preview);
        title=(TextView)findViewById(R.id.txt_title);
        detailtype = getIntent().getIntExtra("detailtype", 2);
        detailview = getIntent().getStringExtra("detailview");
        name=getIntent().getStringExtra("name");
        title.setText(name);
        View back=findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (detailtype == 2) {
            ImageLoader.bindBitmap(detailview, ivpreview);
            ivpreview.setMaxHeight(Math.round(5 * density * windowW));
        }
    }

    @Override
    public void ViewClick(View v) {

    }
}
