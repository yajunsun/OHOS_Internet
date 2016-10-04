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
import zgan.ohos.Models.ShoppingCart;
import zgan.ohos.Models.ShoppingCartSummary;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;

/**
 * Created by yajunsun on 16/10/3.
 */
public class ShoppingCartDal extends ZGbaseDal {

    public final static String ADDCART = "add";
    public final static String UPDATECART = "update";
    public final static String DELETECART = "delete";
    OkHttpClient mOkHttpClient;
    public static ArrayList<SM_GoodsM> mOrderIDs;
    public static ArrayList<ShoppingCart> localCarts;
    DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
    public List<ShoppingCart> getList(String xmlString) {
        List<ShoppingCart> carts = new ArrayList<>();
        try {
            JSONArray cartarr = new JSONObject(xmlString).getJSONArray("data");
            for (int i = 0; i < cartarr.length(); i++) {
                try {
                    JSONObject jcart = (JSONObject) cartarr.opt(i);
                    ShoppingCart cart = new ShoppingCart();
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
                            String price=getNullableString(go,"price","0");
                            String pic_url = getNullableString(go, "pic_url", "");
                            String oldprice = getNullableString(go, "oldprice", "");
                            String specification = getNullableString(go, "specification", "");
                            JSONArray typelist = getNullableArr(go, "type_list");
                            sm_goodsM.setname(gname);
                            sm_goodsM.setproduct_id(product_id);
                            sm_goodsM.setpic_url(pic_url);
                            sm_goodsM.setprice(price);
                            sm_goodsM.setoldprice(oldprice);
                            sm_goodsM.setspecification(specification);
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
                .url("http://app.yumanc.1home1shop.com/V1_0/shoppingcartlist.aspx").post(builder.build())
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

    //修改购物车
    public void updateCart(String method, final SM_GoodsM goodsM, int count,final UpdateCartListner listner) {

        //当传入的是新增或修改的时候遍历本地购物车
        if (method.equals(ADDCART) || method.equals(UPDATECART))
            for (SM_GoodsM m : mOrderIDs) {
                //本地购物车已有需要操作的商品
                if (m.getproduct_id().equals(goodsM.getproduct_id())) {
                    //操作是新增,则将操作改为修改,增1
                    if (method.equals(ADDCART)) {
                        method = UPDATECART;
                        count = m.getcount() + 1;
                        break;
                    } else {
                        //如果是修改并且修改值为0,则将操作改为删除
                        if (count == 0) {
                            method = DELETECART;
                            count = m.getcount();
                        }
                    }
                }
            }
        final String finalMethod = method;
        final int finalcount = count;
        //网络请求api
        mOkHttpClient = new OkHttpClient();
        FormEncodingBuilder builder = new FormEncodingBuilder();
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"method\":");
        sb.append("\"" + finalMethod + "\",");
        sb.append("\"product_id\":");
        sb.append("\"" + goodsM.getproduct_id() + "\",");
        sb.append("\"count\":");
        sb.append("\"" + finalcount + "\"}");
        builder.add("data", sb.toString());
        builder.add("account", PreferenceUtil.getUserName());
        builder.add("token", SystemUtils.getNetToken());
        final Request request = new Request.Builder()
                .url("http://app.yumanc.1home1shop.com/V1_0/updateshoppingcart.aspx").post(builder.build())
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
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                if (listner != null) {
                    final String htmlStr = response.body().string().replace("\\", "");
                    if (listner != null)
                        listner.onResponse(htmlStr);
                    if (finalMethod.equals(ADDCART)) {
                        mOrderIDs.add(goodsM);
                    } else if (finalMethod.equals(DELETECART)) {
                        for (SM_GoodsM m : mOrderIDs) {
                            if (m.getproduct_id().equals(goodsM.getproduct_id()))
                                mOrderIDs.remove(m);
                        }
                    } else {
                        for (SM_GoodsM m : mOrderIDs) {
                            if (m.getproduct_id().equals(goodsM.getproduct_id()))
                                m.setcount(finalcount);
                        }
                    }
                }
            }
        });
    }

    //同步网络购物车和本地购物车
    public void syncCart(List<ShoppingCart> carts) {
        localCarts = new ArrayList<>();
        if (carts != null && carts.size() > 0) {
            localCarts.addAll(carts);
            inimOrderIDs();
        }
    }

    public ShoppingCartSummary getSCSummary() {
        ShoppingCartSummary summary = new ShoppingCartSummary();
        int i = 0;
        double totalprice = 0.0;
        double oldtotalprice = 0.0;
        for (SM_GoodsM m : mOrderIDs) {
            i++;
            totalprice += m.getprice();
            if (!m.getoldprice().equals("") && !m.getoldprice().equals("0"))
                oldtotalprice += Double.parseDouble(m.getoldprice());
        }
        if (i > 0)
            summary.setCount(String.valueOf(i));
        summary.setTotalprice(decimalFormat.format(totalprice));
        if (oldtotalprice == 0.0)
            summary.setOldtotalprice("0");
        else
            summary.setOldtotalprice(decimalFormat.format(oldtotalprice));
        return summary;
    }

    private void inimOrderIDs() {

        if (localCarts != null) {
            mOrderIDs = new ArrayList<>();
            for (int i = 0; i < localCarts.size(); i++) {
                mOrderIDs.addAll(localCarts.get(i).getproductArray());
            }
        }
    }
}
