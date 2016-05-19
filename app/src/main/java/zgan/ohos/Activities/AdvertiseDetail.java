package zgan.ohos.Activities;

import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
    WebView wvcreditsrule;

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
            wvcreditsrule.setVisibility(View.GONE);
            ll_content.setVisibility(View.VISIBLE);
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
            wvcreditsrule.setVisibility(View.GONE);
            ll_content.setVisibility(View.VISIBLE);
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
        else if (advertise.getad_type()==6)
        {
            wvcreditsrule.setVisibility(View.VISIBLE);
            ll_content.setVisibility(View.GONE);
            wvcreditsrule = (WebView) findViewById(R.id.wvcreditsrule);
            wvcreditsrule.loadUrl(advertise.getweb_url());
            wvcreditsrule.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(advertise.getweb_url());
                    return true;
                }
            });
            WebSettings webSettings =   wvcreditsrule .getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setUseWideViewPort(true);//设置此属性，可任意比例缩放
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setJavaScriptEnabled(true);
            webSettings.setBuiltInZoomControls(true);
            webSettings.setSupportZoom(true);
            wvcreditsrule.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    // TODO Auto-generated method stub
                    if (newProgress == 100) {
                        // 网页加载完成
                        toCloseProgress();

                    } else {
                        // 加载中
                        toShowProgress();
                        toSetProgressText("正在加载："+newProgress+"%");
                    }

                }
            });
        }
    }

    @Override
    public void ViewClick(View v) {

    }
}
