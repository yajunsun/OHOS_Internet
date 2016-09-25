package zgan.ohos.Activities;

import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;

import okio.BufferedSink;
import zgan.ohos.Dals.SuperMarketDal;
import zgan.ohos.Models.SuperMarketM;
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
    SuperMarketDal dal;
    List<SuperMarketM> list;
    ListView lstclass;
    RecyclerView rvproducts;
    LinearLayout llcategray1;
    LinearLayout llcategray2;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_super_market);
        dal = new SuperMarketDal();
        lstclass=(ListView)findViewById(R.id.lst_class);
        rvproducts=(RecyclerView)findViewById(R.id.rv_products);
        llcategray1=(LinearLayout)findViewById(R.id.ll_categray1);
        llcategray2=(LinearLayout)findViewById(R.id.ll_categray2);
        loadData();
    }

    protected void loadData() {
        toSetProgressText("正在加载...");
        toShowProgress();

        OkHttpClient mOkHttpClient = new OkHttpClient();
//创建一个Request
        FormEncodingBuilder builder = new FormEncodingBuilder();
        builder.add("ID", "2016");
        builder.add("account", PreferenceUtil.getUserName());
        builder.add("token", SystemUtils.getNetToken());
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
                final String htmlStr = response.body().string().replace("\\", "");
                Message msg = handler.obtainMessage();
                msg.what = 1;
                msg.obj = htmlStr;
                msg.sendToTarget();
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
                String data = msg.obj.toString();
                if (!data.isEmpty()) {
                    try {
                        String result = new JSONObject(data).get("result").toString();
                        String errmsg = new JSONObject(data).get("msg").toString();
                        if (result.equals("0")) {
                            list = dal.getList(data);
                            bindData();
                        }
                    } catch (JSONException jse) {

                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                }
                toCloseProgress();
            }
        }
    };

    @Override
    public void ViewClick(View v) {

    }
    //商品列表适配器
    class sm_product_Adapter {
        class ViewHoler extends RecyclerView.ViewHolder
        {
            ImageView img_product,btn_add;
            TextView txt_name,txt_price,txt_oldprice1,txt_oldprice2;
            LinearLayout ll_types;
            public ViewHoler(View itemView) {
                super(itemView);
            }
        }
    }
}