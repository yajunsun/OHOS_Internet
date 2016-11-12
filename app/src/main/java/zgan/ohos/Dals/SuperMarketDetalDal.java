package zgan.ohos.Dals;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import zgan.ohos.Models.GoodsDetailM;
import zgan.ohos.Models.SuperMarketDetailM;
import zgan.ohos.utils.JsonParser;

/**
 * Created by yajunsun on 2016/9/28.
 * 商品详情数据解析
 */
public class SuperMarketDetalDal {
    public SuperMarketDetailM Get(String xmlString) {
        SuperMarketDetailM m = new SuperMarketDetailM();
        try {
            JSONObject obj = new JSONObject(xmlString).getJSONArray("data").getJSONObject(0);
            String name = obj.getString("name");
            String product_id = obj.getString("product_id");
            String oldprice = obj.getString("oldprice");
            String price = obj.getString("price");
            int countdown = obj.getInt("countdown");
            String specification = obj.getString("specification");
            String comment_pic= JsonParser.getNullableString(obj,"comment_pic","");
            String phone=JsonParser.getNullableString(obj,"comment_pic","");
            JSONObject gdobj = obj.getJSONObject("goodsdetail");
            int detailtype = gdobj.getInt("detailtype");
            String detail_url = gdobj.getString("detail_url");
            String detail_pic_url = gdobj.getString("detail_pic_url");

            GoodsDetailM goodsdetail = new GoodsDetailM();
            goodsdetail.setdetailtype(detailtype);
            goodsdetail.setdetail_url(detail_url);
            goodsdetail.setdetail_pic_url(detail_pic_url);
            JSONArray typearry = obj.getJSONArray("type_list");
            List<String> type_list = new ArrayList<>();
            for (int i = 0; i < typearry.length(); i++) {
                JSONObject type = (JSONObject) typearry.opt(i);
                type_list.add(type.getString("type_icon_url"));
            }
            JSONArray picurlarry = obj.getJSONArray("pic_urls_list");
            List<String> pic_urls_list = new ArrayList<>();
            for (int i = 0; i < picurlarry.length(); i++) {
                JSONObject jurl = (JSONObject) picurlarry.opt(i);
                pic_urls_list.add(jurl.getString("pic_url"));
            }
            m.setname(name);
            m.setproduct_id(product_id);
            m.setoldprice(oldprice);
            m.setprice(price);
            m.setcountdown(countdown);
            m.setgoodsdetail(goodsdetail);
            m.setspecification(specification);
            m.settype_list(type_list);
            m.setpic_urls_list(pic_urls_list);
            m.setcomment_pic(comment_pic);
            m.setphone(phone);
        } catch (JSONException jse) {
            jse.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return m;
    }
}
