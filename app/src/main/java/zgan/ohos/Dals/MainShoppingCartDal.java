package zgan.ohos.Dals;

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
import java.util.ArrayList;
import java.util.List;

import zgan.ohos.Contracts.UpdateCartListner;
import zgan.ohos.Models.BussinessShoppingCartM;
import zgan.ohos.Models.MainShoppingCartM;
import zgan.ohos.Models.SM_GoodsM;
import zgan.ohos.Models.ShoppingCartM;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;

/**
 * Created by yajunsun on 2016/12/8.
 */
public class MainShoppingCartDal extends ZGbaseDal {
    OkHttpClient mOkHttpClient;

    public void getCartList(final UpdateCartListner gets_listner) {
        mOkHttpClient = new OkHttpClient();
        //创建一个Request
        FormEncodingBuilder builder = new FormEncodingBuilder();
        builder.add("account", PreferenceUtil.getUserName());
        builder.add("token", SystemUtils.getNetToken());
        final Request request = new Request.Builder()
                .url(String.format("%s/V1_0/shoppingcartlist1.aspx", SystemUtils.getAppurl())).post(builder.build())
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

    public MainShoppingCartM getList(String xmlString) {
        MainShoppingCartM mcart = new MainShoppingCartM();
        try {
            JSONObject mcartobj = new JSONObject(xmlString).getJSONArray("data").getJSONObject(0);
            int business_flag = getNullableInt(mcartobj, "business_flag", 0);
            List<BussinessShoppingCartM> bussiness_GoodsArray = new ArrayList<>();
            JSONArray bussinessarray = getNullableArr(mcartobj, "bussiness_GoodsArray");
            for (int b = 0; b < bussinessarray.length(); b++) {
                try {
                    BussinessShoppingCartM bussinessShoppingCartM = new BussinessShoppingCartM();
                    JSONObject bsobj = (JSONObject) bussinessarray.opt(b);

                    String bussinessCid = getNullableString(bsobj, "bussinessCid", "");
                    //商家分类名称
                    String bussinessName = getNullableString(bsobj, "bussinessName", "");
                    //产品数组
                    List<ShoppingCartM> goodsArray = new ArrayList<>();
                    JSONArray cartarr = getNullableArr(bsobj, "goodsArray");

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
                            goodsArray.add(cart);

                        } catch (Exception e) {
                            e.printStackTrace();
                            continue;
                        }
                    }
                    bussinessShoppingCartM.setgoodsarray(goodsArray);
                    bussinessShoppingCartM.setbussinesscid(bussinessCid);
                    bussinessShoppingCartM.setbussinessname(bussinessName);
                    bussiness_GoodsArray.add(bussinessShoppingCartM);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }

            mcart.setbusiness_flag(business_flag);
            mcart.setbussiness_goodsArray(bussiness_GoodsArray);
        } catch (Exception e) {
            e.printStackTrace();

        }

        return mcart;
    }
}

