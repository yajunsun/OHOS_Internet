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
import zgan.ohos.Models.SM_OrderPayDetail;
import zgan.ohos.Models.SM_OrderPayInfo;
import zgan.ohos.Models.SM_Payway;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;

/**
 * Created by Administrator on 2016/10/15.
 */
public class SM_OrderPayDal extends ZGbaseDal {
    OkHttpClient mOkHttpClient;

    //验证
    public void ComfirmOrder(List<SM_GoodsM> goodsMs, final UpdateCartListner listner) {
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

    //提交
    public void CommitOrder(String address_id, int pay_way, String total_price, List<SM_GoodsM> goodsMs, final UpdateCartListner listner) {
        mOkHttpClient = new OkHttpClient();
        FormEncodingBuilder builder = new FormEncodingBuilder();
        StringBuilder sb = new StringBuilder();
        StringBuilder subsb = new StringBuilder();
        sb.append("{\"address_id\":\"" + address_id + "\",");
        sb.append("\"pay_way\":\"" + String.valueOf(pay_way) + "\",");
        sb.append("\"total_price\":\"" + total_price + "\",");
        sb.append("\"goods_list\":[");
        for (SM_GoodsM goodsM : goodsMs) {
            subsb.append("{");
            subsb.append("\"product_id\":");
            subsb.append("\"" + goodsM.getproduct_id() + "\",");
            subsb.append("\"price\":");
            subsb.append("\"" + goodsM.getprice() + "\",");
            subsb.append("\"count\":");
            subsb.append("\"" + goodsM.getcount() + "\"");
            subsb.append("},");
        }
        String substr = subsb.substring(0, subsb.length() - 1);
        sb.append(substr);
        sb.append("]}");
        builder.add("data", sb.toString());
        builder.add("account", PreferenceUtil.getUserName());
        builder.add("token", SystemUtils.getNetToken());

        final Request request = new Request.Builder()
                .url("http://app.yumanc.1home1shop.com/V1_0/goodscartsubmitorder.aspx").post(builder.build())
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

    public void SecondCommit(String order_sn, int pay_way, String total_price, final UpdateCartListner listner) {
        mOkHttpClient = new OkHttpClient();
        FormEncodingBuilder builder = new FormEncodingBuilder();
        StringBuilder sb = new StringBuilder();
        sb.append("{\"order_sn\":\"" + order_sn + "\",");
        sb.append("\"pay_way\":\"" + String.valueOf(pay_way) + "\",");
        sb.append("\"total_price\":\"" + total_price + "\"");
        sb.append("}");
        builder.add("data", sb.toString());
        builder.add("account", PreferenceUtil.getUserName());
        builder.add("token", SystemUtils.getNetToken());

        final Request request = new Request.Builder()
                .url("http://app.yumanc.1home1shop.com/V1_0/goodssecondsubmitorder.aspx").post(builder.build())
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

    //获取支持的支付方式集合
    public SM_Payway getPayWays(String xmlString) {
        SM_Payway payway = new SM_Payway();
        try {
            JSONObject obj = new JSONObject(xmlString).getJSONObject("data");
            String address_name = getNullableString(obj, "address_name", "");
            String address_id = getNullableString(obj, "address_id", "");
            JSONArray arr = getNullableArr(obj, "pay_ways");
            List<Integer> pay_ways = new ArrayList<>();
            if (arr.length() > 0) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject numobj = (JSONObject) arr.opt(i);
                    int pay_num = getNullableInt(numobj, "pay_num", -1);
                    pay_ways.add(pay_num);
                }
            }
            payway.setaddress_id(address_id);
            payway.setaddress_name(address_name);
            payway.setpay_ways(pay_ways);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return payway;
    }

    //获取订单支付接口信息
    public SM_OrderPayInfo getPayInfo(String xmlString) {
        SM_OrderPayInfo info = new SM_OrderPayInfo();
        try {
            JSONObject obj = new JSONObject(xmlString).getJSONObject("data");
            String order_sn = getNullableString(obj, "order_sn", "");
            int order_type = getNullableInt(obj, "order_type", 0);
            String pay_order_sn = getNullableString(obj, "pay_order_sn", "");
            int pay_way = getNullableInt(obj, "pay_way", -1);
            double total_price = Double.parseDouble(getNullableString(obj, "total_price", "0.0"));
            SM_OrderPayDetail pay_ways = new SM_OrderPayDetail();
            JSONObject payways =getNullableObj(obj,"pay_ways") ;
            String pay_account = getNullableString(payways, "pay_account", "");
            String pay_key = getNullableString(payways, "pay_key", "");
            String pay_user = getNullableString(payways, "pay_user", "");
            String business_notice_url = getNullableString(payways, "business_notice_url", "");

            pay_ways.setpay_account(pay_account);
            pay_ways.setpay_key(pay_key);
            pay_ways.setPay_user(pay_user);
            pay_ways.setbusiness_notice_url(business_notice_url);
            info.setorder_sn(order_sn);
            info.setorder_type(order_type);
            info.setpay_order_sn(pay_order_sn);
            info.setpay_way(pay_way);
            info.settotal_price(String.valueOf(total_price));
            info.setpay_ways(pay_ways);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }
}
