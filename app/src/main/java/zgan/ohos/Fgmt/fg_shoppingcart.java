package zgan.ohos.Fgmt;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import zgan.ohos.Activities.SMSearchResult;
import zgan.ohos.R;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;
import zgan.ohos.utils.generalhelper;

/**
 * Created by yajunsun on 16/10/3.
 */
public class fg_shoppingcart extends myBaseFragment {
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadData();
    }

    OkHttpClient mOkHttpClient;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_fg_shopping_cart,null);
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
                            bindData();
                        } else if (!errmsg.isEmpty()) {
                            generalhelper.ToastShow(getActivity(), "服务器错误:" + errmsg);
                        }
                    } catch (JSONException jse) {

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };
}
