package zgan.ohos.Activities;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import zgan.ohos.Models.Advertise;
import zgan.ohos.R;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.ImageLoader;

public class AdvertiseDetail extends myBaseActivity {

    Advertise advertise;
    TextView txt_title;
    ScrollView ll_content;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_advertise_detail);
        advertise = (Advertise) getIntent().getSerializableExtra("advertise");
        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ll_content = (ScrollView) findViewById(R.id.ll_content);
        txt_title = (TextView) findViewById(R.id.txt_title);
        txt_title.setText(advertise.getview_title());

        if (advertise.getad_type() == 0) {
            View view = getLayoutInflater().inflate(R.layout.lo_yangguangyubei_item, null, false);
            TextView txt_title, txt_content;
            View txt_msg_type, txt_pub_time;
            txt_msg_type = view.findViewById(R.id.txt_msg_type);
            txt_pub_time = view.findViewById(R.id.txt_pub_time);
            txt_msg_type.setVisibility(View.GONE);
            txt_pub_time.setVisibility(View.GONE);
            txt_title = (TextView) view.findViewById(R.id.txt_title);
            txt_content = (TextView) view.findViewById(R.id.txt_content);
            txt_title.setText(advertise.gettitle());
            txt_content.setText(advertise.getad_content());
            ll_content.addView(view);
        } else if (advertise.getad_type() == 1) {
            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            imageView.setLayoutParams(params);
            imageView.setAdjustViewBounds(true);
            int maxwidth = AppUtils.getWindowSize(this).x;
            int maxheight = 5 * maxwidth;
            imageView.setMaxWidth(maxwidth);
            imageView.setMaxHeight(maxheight);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            ImageLoader.bindBitmap(advertise.getpic_url(), imageView, 1000, 1800);
            ll_content.addView(imageView);
        }
    }

    @Override
    public void ViewClick(View v) {

    }
}
