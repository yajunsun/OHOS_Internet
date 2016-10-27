package zgan.ohos.Dals;

import android.util.Log;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import zgan.ohos.Contracts.UpdateCartListner;
import zgan.ohos.Models.SM_GoodsM;
import zgan.ohos.Models.ShoppingCartM;
import zgan.ohos.Models.ShoppingCartSummary;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;

/**
 * Created by yajunsun on 16/10/3.
 */
public class ShoppingCartDal extends ZGbaseDal {

    public final static String ADDCART = "add";
    public final static String UPDATECART = "upd";
    public final static String DELETECART = "del";
    public final static String SELECTCART = "select";
    OkHttpClient mOkHttpClient;
    public static ArrayList<SM_GoodsM> mOrderIDs;
    public static ArrayList<ShoppingCartM> localCarts;
    DecimalFormat decimalFormat = new DecimalFormat("0.00");

    public List<ShoppingCartM> getList(String xmlString) {
        List<ShoppingCartM> carts = new ArrayList<>();
        try {
            JSONArray cartarr = new JSONObject(xmlString).getJSONArray("data");
            for (int i = 0; i < cartarr.length(); i++) {
                try {
                    JSONObject jcart = (JSONObject) cartarr.opt(i);
                    ShoppingCartM cart = new ShoppingCartM();
                    String distributionType = getNullableString(jcart, "distributionType", "");
                    String distribution_Icon_url = getNullableString(jcart, "distribution_Icon_url", "");
                    JSONArray goods = getNullableArr(jcart, "productArray");
                    List<SM_GoodsM> goodsMs = new ArrayList<>();
                    for (int g = 0; g < goods.length(); g++) {
                        JSONObject go = (JSONObject) goods.opt(g);
                        SM_GoodsM sm_goodsM = new SM_GoodsM();
                        try {
                            String gname = getNullableString(go, "name", "");
                            String product_id = getNullableString(go, "product_id", "");
                            int count = getNullableInt(go, "count", 1);
                            String price = getNullableString(go, "price", "0");
                            String pic_url = getNullableString(go, "pic_url", "");
                            String oldprice = getNullableString(go, "oldprice", "");
                            String specification = getNullableString(go, "specification", "");
                            int can_handsel = getNullableInt(go, "can_handsel", 0);
                            JSONArray typelist = getNullableArr(go, "type_list");
                            sm_goodsM.setname(gname);
                            sm_goodsM.setcount(count);
                            sm_goodsM.setproduct_id(product_id);
                            sm_goodsM.setpic_url(pic_url);
                            sm_goodsM.setprice(price);
                            sm_goodsM.setoldprice(oldprice);
                            sm_goodsM.setspecification(specification);
                            sm_goodsM.setcan_handsel(can_handsel);
                            List<String> type_list = new ArrayList<>();
                            for (int t = 0; t < typelist.length(); t++) {
                                JSONObject type = (JSONObject) typelist.opt(t);
                                type_list.add(getNullableString(type, "type_icon_url", ""));
                            }
                            sm_goodsM.settype_list(type_list);
                            goodsMs.add(sm_goodsM);
                        } catch (Exception e) {
                            e.printStackTrace();
                            continue;
                        }
                    }
                    cart.setdistributionType(distributionType);
                    cart.setdistribution_Icon_url(distribution_Icon_url);
                    cart.setproductArray(goodsMs);
                    carts.add(cart);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        } catch (JSONException jse) {
            return carts;
        } catch (Exception e) {
            return carts;
        }
        return carts;
    }

    public void getCartList(final UpdateCartListner gets_listner) {
        mOkHttpClient = new OkHttpClient();
        //创建一个Request
        FormEncodingBuilder builder = new FormEncodingBuilder();
        builder.add("account", PreferenceUtil.getUserName());
        builder.add("token", SystemUtils.getNetToken());
        final Request request = new Request.Builder()
                .url(String.format("%s/V1_0/shoppingcartlist.aspx", SystemUtils.getAppurl())).post(builder.build())
                .build();
        //new call
        Call call = mOkHttpClient.newCall(request);
        //请求加入调度
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                if (gets_listner != null)
                    gets_listner.onFailure();
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                final String htmlStr = response.body().string().replace("\\", "");
                gets_listner.onResponse(htmlStr);
            }
        });

    }

    //修改购物车（新增、修改、删除、状态改变）
    public void updateCart(String method, final SM_GoodsM goodsM, int count, final UpdateCartListner listner) {
        if (goodsM == null)
            return;
        //当传入的是新增或修改的时候遍历本地购物车
        if (method.equals(ADDCART) || method.equals(UPDATECART))

            for (SM_GoodsM m : mOrderIDs) {
                //本地购物车已有需要操作的商品
                if (m.getproduct_id().equals(goodsM.getproduct_id())) {
                    //操作是新增,则将操作改为修改,增1
                    if (method.equals(ADDCART)) {
                        method = UPDATECART;
                        count = m.getcount() + 1;
                        m.setcan_handsel(1);
                        goodsM.setcan_handsel(1);
                        break;
                    }
//                    else {
//                        //如果是修改并且修改值为0,则将操作改为删除
//                        if (count == 0) {
//                            method = DELETECART;
//                            count = m.getcount();
//                            break;
//                        }
//                    }
                }
            }
        final String finalMethod = method;
        final int finalcount = count;
        //网络请求api
        mOkHttpClient = new OkHttpClient();
        FormEncodingBuilder builder = new FormEncodingBuilder();
        StringBuilder sb = new StringBuilder();
        sb.append("[{");
        sb.append("\"method\":");
        sb.append("\"" + finalMethod + "\",");
        sb.append("\"product_id\":");
        sb.append("\"" + goodsM.getproduct_id() + "\",");
        sb.append("\"count\":");
        sb.append("\"" + finalcount + "\"}]");
        builder.add("data", sb.toString());
        builder.add("account", PreferenceUtil.getUserName());
        builder.add("token", SystemUtils.getNetToken());
        final Request request = new Request.Builder()
                .url(String.format("%s/V1_0/updateshoppingcart.aspx", SystemUtils.getAppurl())).post(builder.build())
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

                if (finalMethod.equals(ADDCART)) {
                    goodsM.setcount(1);
                    goodsM.setcan_handsel(1);
                    mOrderIDs.add(goodsM);
                } else if (finalMethod.equals(DELETECART)) {
                    for (SM_GoodsM m : mOrderIDs) {
                        if (m.getproduct_id().equals(goodsM.getproduct_id())) {
                            mOrderIDs.remove(m);
                            break;
                        }
                    }
                } else if (finalMethod.equals(UPDATECART)) {
                    for (SM_GoodsM m : mOrderIDs) {
                        if (m.getproduct_id().equals(goodsM.getproduct_id())) {
                            m.setcount(finalcount);
                            break;
                        }
                    }
                } else if (finalMethod.equals(SELECTCART)) {
                    for (SM_GoodsM m : mOrderIDs) {
                        if (m.getproduct_id().equals(goodsM.getproduct_id())) {
                            m.setcan_handsel(finalcount);
                            goodsM.setcan_handsel(finalcount);
                            break;
                        }
                    }
                }
                if (listner != null)
                    listner.onResponse(htmlStr);
            }
        });
    }

    //修改购物车（选中状态改变和批量删除）
    public void updateCart(String method, final List<SM_GoodsM> goodsMs, int count, final UpdateCartListner listner) {
        if (goodsMs == null || goodsMs.size() == 0)
            return;
        final String finalMethod = method;
        final int finalcount = count;
        //网络请求api
        mOkHttpClient = new OkHttpClient();
        FormEncodingBuilder builder = new FormEncodingBuilder();
        StringBuilder sb = new StringBuilder();
        StringBuilder subsb = new StringBuilder();
        sb.append("[");
        for (SM_GoodsM goodsM : goodsMs) {
            subsb.append("{");
            subsb.append("\"method\":");
            subsb.append("\"" + finalMethod + "\",");
            subsb.append("\"product_id\":");
            subsb.append("\"" + goodsM.getproduct_id() + "\",");
            subsb.append("\"count\":");
            subsb.append("\"" + finalcount + "\"},");
        }
        sb.append(subsb.substring(0, subsb.length() - 1));
        sb.append("]");
        builder.add("data", sb.toString());
        builder.add("account", PreferenceUtil.getUserName());
        builder.add("token", SystemUtils.getNetToken());
        final Request request = new Request.Builder()
                .url(String.format("%s/V1_0/updateshoppingcart.aspx", SystemUtils.getAppurl())).post(builder.build())
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

                if (finalMethod.equals(DELETECART)) {
                    List<SM_GoodsM> deleteItems = new ArrayList<>();
                    for (SM_GoodsM m : mOrderIDs) {
                        for (SM_GoodsM m1 : goodsMs) {
                            if (m.getproduct_id().equals(m1.getproduct_id())) {
                                deleteItems.add(m);
                                break;
                            }
                        }
                    }
                    mOrderIDs.removeAll(deleteItems);
                } else if (finalMethod.equals(SELECTCART)) {
                    boolean isselected = finalcount == 1;
                    for (SM_GoodsM m : mOrderIDs) {
                        for (SM_GoodsM m1 : goodsMs) {
                            if (m.getproduct_id().equals(m1.getproduct_id())) {
                                m.setSelect(isselected);
                                m.setcan_handsel(finalcount);
                                break;
                            }
                        }
                    }
                }
                if (listner != null)
                    listner.onResponse(htmlStr);
            }
        });
    }

    //同步网络购物车和本地购物车
    public void syncCart(List<ShoppingCartM> carts) {
        localCarts = new ArrayList<>();
        mOrderIDs = new ArrayList<>();
        if (carts != null && carts.size() > 0) {
            localCarts.addAll(carts);
            inimOrderIDs();
        }
    }

    public ShoppingCartSummary getSCSummary() {
        ShoppingCartSummary summary = new ShoppingCartSummary();
        int i = 0;
        int tcount = 0;
        double totalprice = 0.0;
        double oldtotalprice = 0.0;
        for (SM_GoodsM m : mOrderIDs) {
            if (m.getSelect()) {
                i++;
                tcount += m.getcount();
                totalprice += m.getprice() * m.getcount();
                if (!m.getoldprice().equals("") && !m.getoldprice().equals("0"))
                    oldtotalprice += Double.parseDouble(m.getoldprice()) * m.getcount();
            }
        }
        summary.setCount(String.valueOf(i));
        summary.setTotalcount(String.valueOf(tcount));
        summary.setTotalprice(decimalFormat.format(totalprice));
        if (oldtotalprice == 0.0)
            summary.setOldtotalprice("0");
        else
            summary.setOldtotalprice(decimalFormat.format(oldtotalprice));
        return summary;
    }

    private void inimOrderIDs() {

        if (localCarts != null) {
            for (int i = 0; i < localCarts.size(); i++) {
                mOrderIDs.addAll(localCarts.get(i).getproductArray());
            }
        }
    }

    public void verifyGoods(List<SM_GoodsM> goodsMs) {
        //验证商品
        mOkHttpClient = new OkHttpClient();
        //创建一个Request
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
        builder.add("account", PreferenceUtil.getUserName());
        builder.add("token", SystemUtils.getNetToken());
        builder.add("data", sb.toString());
        final Request request = new Request.Builder()
                .url(String.format("%s/V1_0/goodscartorder.aspx", SystemUtils.getAppurl())).post(builder.build())
                .build();
        //new call
        Call call = mOkHttpClient.newCall(request);
        //请求加入调度
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.i("suntest", e.getMessage());
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                final String htmlStr = response.body().string().replace("\\", "");
                Log.i("suntext", htmlStr);
            }
        });
    }

    //提交购物车订单
    public void commitCart(List<SM_GoodsM> goodsMs, ShoppingCartSummary summary, String dilivertime, UpdateCartListner listner) {
        dilivertime = "0";
        //验证商品
        mOkHttpClient = new OkHttpClient();
        //创建一个Request
        FormEncodingBuilder builder = new FormEncodingBuilder();
        StringBuilder sb = new StringBuilder();
        StringBuilder subsb = new StringBuilder();
        sb.append("{");
        sb.append("\"diliver_time\":");
        sb.append("\"" + dilivertime + "\",");
        sb.append("\"goods_list\":");
        sb.append("[");
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
        sb.append("]");
        sb.append("}");
        builder.add("account", PreferenceUtil.getUserName());
        builder.add("token", SystemUtils.getNetToken());
        builder.add("data", sb.toString());
        final Request request = new Request.Builder()
                .url(String.format("%s/V1_0/goodscartorder.aspx", SystemUtils.getAppurl())).post(builder.build())
                .build();
        //new call
        Call call = mOkHttpClient.newCall(request);
        //请求加入调度
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.i("suntest", e.getMessage());
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                final String htmlStr = response.body().string().replace("\\", "");
                Log.i("suntext", htmlStr);
            }
        });
    }

    //获取搜索关键字
    public void getSearchKeys(String key, final UpdateCartListner listner) {
        mOkHttpClient = new OkHttpClient();
        //创建一个Request
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"search_str\":");
        sb.append("\"" + key + "\"");
        sb.append("}");
        FormEncodingBuilder builder = new FormEncodingBuilder();
        builder.add("account", PreferenceUtil.getUserName());
        builder.add("token", SystemUtils.getNetToken());
        builder.add("data", sb.toString());
        final Request request = new Request.Builder()
                .url(String.format("%s/V1_0/searchgoodstitle.aspx", SystemUtils.getAppurl())).post(builder.build())
                .build();
        //new call
        Call call = mOkHttpClient.newCall(request);
        //请求加入调度
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                if (listner != null)
                    listner.onFailure();
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                final String htmlStr = response.body().string().replace("\\", "");
                listner.onResponse(htmlStr);
            }
        });
    }

    public List<String> getStringList(String json) {
        List<String> result = new ArrayList<>();
        if (json.isEmpty())
            return result;
        try {
            JSONArray array = new JSONObject(json).getJSONArray("data");
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = (JSONObject) array.opt(i);
                result.add(getNullableString(obj, "name", ""));
            }
        } catch (JSONException je) {
            je.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
