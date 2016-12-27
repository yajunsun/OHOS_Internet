package zgan.ohos.Dals;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import zgan.ohos.Contracts.UpdateCartListner;
import zgan.ohos.Models.ProvinceModel;
import zgan.ohos.Models.ShippingAddressModel;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;

/**
 * Created by yajunsun on 2016/12/27.
 */
public class ShippingAddressDal extends ZGbaseDal{

    public final static String ADD="add";//-ÐÂÔö
    public final static String UPDATE="upd";//-ÐÞ¸Ä
    public final static String DELETE="del";//-É¾³ý
    public final static String  GET="get";//-Ê¹ÓÃ
    OkHttpClient mOkHttpClient;
    //ÐÂÔöµØÖ·
    public void addAddress(ShippingAddressModel addr,final UpdateCartListner listner)
    {
        //ÍøÂçÇëÇóapi
        mOkHttpClient = new OkHttpClient();
        FormEncodingBuilder builder = new FormEncodingBuilder();
        StringBuilder sb = new StringBuilder();
        sb.append("[{");
        sb.append("\"method\":");
        sb.append("\"" + ADD + "\",");
        sb.append("\"UserName\":");
        sb.append("\"" + addr.getUserName() + "\",");
        sb.append("\"UserPhone\":");
        sb.append("\"" + addr.getUserPhone() + "\",");
        sb.append("\"UserAdress\":");
        sb.append("\"" + addr.getUserAdress() + "\",");
        sb.append("\"IsUse\":");
        sb.append("\"" + addr.getIsUse() + "\"}]");
        builder.add("data", sb.toString());
        builder.add("account", PreferenceUtil.getUserName());
        builder.add("token", SystemUtils.getNetToken());
        final Request request = new Request.Builder()
                .url(String.format("%s/V1_0/goodscartAddressManage.aspx", SystemUtils.getAppurl())).post(builder.build())
                .build();
        //new call
        Call call = mOkHttpClient.newCall(request);
        //ÇëÇó¼ÓÈëµ÷¶È
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
    //ÐÞ¸ÄµØÖ·
    public void updateAddress(ShippingAddressModel addr,final UpdateCartListner listner)
    {
        //ÍøÂçÇëÇóapi
        mOkHttpClient = new OkHttpClient();
        FormEncodingBuilder builder = new FormEncodingBuilder();
        StringBuilder sb = new StringBuilder();
        sb.append("[{");
        sb.append("\"method\":");
        sb.append("\"" + UPDATE + "\",");
        sb.append("\"address_id\":");
        sb.append("\"" + addr.getaddress_id() + "\",");
        sb.append("\"UserName\":");
        sb.append("\"" + addr.getUserName() + "\",");
        sb.append("\"UserPhone\":");
        sb.append("\"" + addr.getUserPhone() + "\",");
        sb.append("\"UserAdress\":");
        sb.append("\"" + addr.getUserAdress() + "\",");
        sb.append("\"IsUse\":");
        sb.append("\"" + addr.getIsUse() + "\"}]");
        builder.add("data", sb.toString());
        builder.add("account", PreferenceUtil.getUserName());
        builder.add("token", SystemUtils.getNetToken());
        final Request request = new Request.Builder()
                .url(String.format("%s/V1_0/goodscartAddress.aspx", SystemUtils.getAppurl())).post(builder.build())
                .build();
        //new call
        Call call = mOkHttpClient.newCall(request);
        //ÇëÇó¼ÓÈëµ÷¶È
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
    //É¾³ýµØÖ·
    public void deleteAddress(ShippingAddressModel addr,final UpdateCartListner listner)
    {
        //ÍøÂçÇëÇóapi
        mOkHttpClient = new OkHttpClient();
        FormEncodingBuilder builder = new FormEncodingBuilder();
        StringBuilder sb = new StringBuilder();
        sb.append("[{");
        sb.append("\"method\":");
        sb.append("\"" + DELETE + "\",");
        sb.append("\"address_id\":");
        sb.append("\"" + addr.getaddress_id() + "\",");
        sb.append("\"IsUse\":");
        sb.append("\"" + addr.getIsUse() + "\"}]");
        builder.add("data", sb.toString());
        builder.add("account", PreferenceUtil.getUserName());
        builder.add("token", SystemUtils.getNetToken());
        final Request request = new Request.Builder()
                .url(String.format("%s/V1_0/goodscartAddress.aspx", SystemUtils.getAppurl())).post(builder.build())
                .build();
        //new call
        Call call = mOkHttpClient.newCall(request);
        //ÇëÇó¼ÓÈëµ÷¶È
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
    public void getAddress(final UpdateCartListner listner){
        //ÍøÂçÇëÇóapi
        mOkHttpClient = new OkHttpClient();
        FormEncodingBuilder builder = new FormEncodingBuilder();
        StringBuilder sb = new StringBuilder();
        sb.append("[{");
        sb.append("\"method\":");
        sb.append("\"" + GET + "\"}]");
        builder.add("data", sb.toString());
        builder.add("account", PreferenceUtil.getUserName());
        builder.add("token", SystemUtils.getNetToken());
        final Request request = new Request.Builder()
                .url(String.format("%s/V1_0/goodscartAddress.aspx", SystemUtils.getAppurl())).post(builder.build())
                .build();
        //new call
        Call call = mOkHttpClient.newCall(request);
        //ÇëÇó¼ÓÈëµ÷¶È
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
    public void getProvince(final UpdateCartListner listner)
    {
        //ÍøÂçÇëÇóapi
        mOkHttpClient = new OkHttpClient();
        FormEncodingBuilder builder = new FormEncodingBuilder();
        /*StringBuilder sb = new StringBuilder();
        sb.append("[{");
        sb.append("\"method\":");
        sb.append("\"" + GET + "\"}]");
        builder.add("data", sb.toString());
        */
        builder.add("account", PreferenceUtil.getUserName());
        builder.add("token", SystemUtils.getNetToken());
        final Request request = new Request.Builder()
                .url(String.format("%s/V1_0/Province.aspx", SystemUtils.getAppurl())).post(builder.build())
                .build();
        //new call
        Call call = mOkHttpClient.newCall(request);
        //ÇëÇó¼ÓÈëµ÷¶È
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
    public List<ShippingAddressModel> getList(String xmlString)
    {
        return getModelList(xmlString,new ShippingAddressModel());
    }
    //¸ù¾Ýjson×Ö·û´®»ñÈ¡½âÎö³ÉProvinceModel¶ÔÏó
    public ProvinceModel getProviceModel(String xmlString)
    {
        try {
            JSONObject obj = new JSONObject(xmlString);
            ProvinceModel model = new ProvinceModel();
            model.setnum(getNullableInt(obj, "num", 0));
            model.setname(getNullableString(obj, "name", ""));
            model.setdata(getNullableString(obj, "data", ""));
            return model;
        }
        catch (JSONException jse)
        {
            jse.printStackTrace();
        }
        return null;
    }
}
