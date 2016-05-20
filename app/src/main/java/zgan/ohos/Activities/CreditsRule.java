package zgan.ohos.Activities;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import zgan.ohos.R;

public class CreditsRule extends myBaseActivity {

    WebView wvcreditsrule;
    String mRuleUrl="http://www.cnblogs.com/zgz345/p/3768174.html";

    @Override
    protected void initView() {
        setContentView(R.layout.activity_credits_rule);
        mRuleUrl = getIntent().getStringExtra("creditsrule");
        wvcreditsrule = (WebView) findViewById(R.id.wvcreditsrule);
        wvcreditsrule.loadUrl(mRuleUrl);
        wvcreditsrule.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(mRuleUrl);
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
        View back=findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    @Override
    public void ViewClick(View v) {

    }
    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
}
