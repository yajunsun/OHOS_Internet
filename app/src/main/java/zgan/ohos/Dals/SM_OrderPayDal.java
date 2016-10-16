package zgan.ohos.Dals;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import zgan.ohos.Contracts.UpdateCartListner;
import zgan.ohos.Models.SM_GoodsM;
import zgan.ohos.Models.SM_OrderPayInfo;
import zgan.ohos.Models.SM_Payway;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;

/**
 * Created by Administrator on 2016/10/15.
 */
public class SM_OrderPayDal extends ZGbaseDal{
    OkHttpClient mOkHttpClient;
    public void ComfirmOrder(List<SM_GoodsM> goodsMs,final UpdateCartListner listner)
    {
        mOkHttpClient=new OkHttpClient();
        mOkHttpClient = new OkHttpClient();
        FormEncodingBuilder builder = new FormEncodingBuilder();
        StringBuilder sb = new StringBuilder();
        StringBuilder subsb = new StringBuilder();
        sb.append("[");
        for (SM_GoodsM goodsM : goodsMs) {
            subsb.append("{");
            subsb.append("\"product_id\":");
            subsb.append("\"" + goodsM.getproduct_id() + "\",");
            subsb.append("\"price\":");
            subsb.append("\"" + goodsM.getprice() + "\"");
            subsb.append("},");
        }
        String substr = subsb.substring(0, subsb.length() - 1);
        sb.append(substr);
        sb.append("]");
        builder.add("data", sb.toString());
        builder.add("account", PreferenceUtil.getUserName());
        builder.add("token", SystemUtils.getNetToken());
        final Request request = new Request.Builder()
                .url("http://app.yumanc.1home1shop.com/V1_0/goodscartorder.aspx").post(builder.build())
                .build();
        //new call
        Call call = mOkHttpClient.newCall(request);
        //请求加入调度
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                if (listner != null) {
                    listner.onFailure();
                }
                e.printStackTrace();
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                final String htmlStr = response.body().string().replace("\\", "");
                if (listner != null)
                    listner.onResponse(htmlStr);
            }
        });
    }
    public SM_Payway getPayWays(String xmlString)
    {
        SM_Payway payway=new SM_Payway();
        try {
            JSONObject obj = new JSONObject(xmlString).getJSONObject("data");
            String address_name=getNullableString(obj,"address_name","");
            String address_id=getNullableString(obj,"address_id","");
            JSONArray arr=getNullableArr(obj,"pay_ways");
            List<Integer> pay_ways=new ArrayList<>();
            if(arr.length()>0)
            {
                for(int i=0;i<arr.length();i++) {
                    int pay_num = (int) arr.opt(i);
                    pay_ways.add(pay_num);
                }
            }
            payway.setaddress_id(address_id);
            payway.setaddress_name(address_name);
            payway.setpay_ways(pay_ways);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return payway ;
    }
}
