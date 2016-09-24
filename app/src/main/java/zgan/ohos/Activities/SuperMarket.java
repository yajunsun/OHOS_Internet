package zgan.ohos.Activities;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.TextView;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import okio.BufferedSink;
import zgan.ohos.R;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;
import zgan.ohos.utils.generalhelper;

/**
 * create by yajunsun
 * <p/>
 * 超市购界面
 */
public class SuperMarket extends myBaseActivity {

    TextView txtdata;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_super_market);
        txtdata = (TextView) findViewById(R.id.txt_data);
        loadData();
    }

    protected void loadData() {
        OkHttpClient mOkHttpClient = new OkHttpClient();
//创建一个Request
        FormEncodingBuilder builder = new FormEncodingBuilder();
        builder.add("ID","2016");
        builder.add("account",PreferenceUtil.getUserName());
        builder.add("token",SystemUtils.getNetToken());
        final Request request = new Request.Builder()
                .url("http://app.yumanc.1home1shop.com/V1_0/marketlist.aspx").post(builder.build())
                .build();
//new call
        Call call = mOkHttpClient.newCall(request);
//请求加入调度
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                final String htmlStr =  response.body().string().replace("\\","");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        txtdata.setText(htmlStr);
                    }
                });
            }
        });
    }

    void bindData() {

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Frame frame = (Frame) msg.obj;
                String[] results = frame.strData.split("\t");
                String ret = generalhelper.getSocketeStringResult(frame.strData);
                Log.i(TAG, frame.subCmd + "  " + ret);

                if (frame.subCmd == 40) {
                    if (results[0].equals("0") && results[1].equals("2016")) {
                        try {

                        } catch (Exception ex) {
                            android.os.Message msg1 = new android.os.Message();
                            msg1.what = 0;
                            msg1.obj = ex.getMessage();
                            handler.sendMessage(msg1);
                        }
                    }
                }
                toCloseProgress();
            }
        }
    };

    @Override
    public void ViewClick(View v) {

    }
}