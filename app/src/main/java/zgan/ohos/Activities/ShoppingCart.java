package zgan.ohos.Activities;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import zgan.ohos.Contracts.UpdateCartListner;
import zgan.ohos.Dals.ShoppingCartDal;
import zgan.ohos.Models.ShoppingCartSummary;
import zgan.ohos.R;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;
import zgan.ohos.utils.generalhelper;

/**
 * Created by yajunsun on 16/10/3.
 */
public class ShoppingCart extends myBaseActivity {
    OkHttpClient mOkHttpClient;
    TextView  txtcontent;
    ShoppingCartDal cartDal;
    @Override
    protected void initView() {
        setContentView(R.layout.fragment_fg_shopping_cart);
        txtcontent=(TextView)findViewById(R.id.txt_content) ;
        cartDal=new ShoppingCartDal();
        loadData();
    }

    void loadData(){

        mOkHttpClient = new OkHttpClient();
        //创建一个Request
        FormEncodingBuilder builder = new FormEncodingBuilder();
        builder.add("account", PreferenceUtil.getUserName());
        builder.add("token", SystemUtils.getNetToken());
        final Request request = new Request.Builder()
                .url("http://app.yumanc.1home1shop.com/V1_0/shoppingcartlist.aspx").post(builder.build())
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
    void bindData(){}
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //全部数据 包括一级二级分类和第一页商品数据
            if (msg.what == 1) {
                String data = msg.obj.toString();
                if (!data.isEmpty()) {
                    try {
                        String result = new JSONObject(data).get("result").toString();
                        String errmsg = new JSONObject(data).get("msg").toString();
                        //获取数据并绑定数据
                        if (result.equals("0")) {
                            //list = dal.getGoodsList(data);
                            txtcontent.setText(data);
                            bindData();
                        } else if (!errmsg.isEmpty()) {
                            generalhelper.ToastShow(ShoppingCart.this, "服务器错误:" + errmsg);
                        }
                    } catch (JSONException jse) {

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };
//    UpdateCartListner cartChanged =new UpdateCartListner() {
//        @Override
//        public void onFailure() {
//            generalhelper.ToastShow(SuperMarket.this, "加入购物车失败!");
//        }
//
//        @Override
//        public void onResponse(String response) {
//            ShoppingCartSummary summary=cartDal.getSCSummary();
//            bindShoppingCard(summary);
//        }
//    };
    @Override
    public void ViewClick(View v) {

    }
}
