package zgan.ohos.Dals;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import zgan.ohos.Models.SM_GoodsM;
import zgan.ohos.Models.SM_SecondaryM;
import zgan.ohos.Models.SuperMarketM;
import zgan.ohos.Models.Vegetable;
import zgan.ohos.utils.JsonParser;
import zgan.ohos.utils.SystemUtils;

/**
 * Created by yajunsun on 16-5-6.
 */
public class SuperMarketDal extends ZGbaseDal<SuperMarketM> {
    public List<SuperMarketM> getList(String xmlString) {
        //return getModelList(xmlString, new SuperMarketM());
        List<SuperMarketM> list = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONObject(xmlString)
                    .getJSONArray("data");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = (JSONObject) jsonArray.opt(i);
                SuperMarketM sm=new SuperMarketM();
                try {
                    String name =getNullableString(obj,"name",""); obj.getString("name");
                    String id=getNullableString(obj,"id","");//obj.getString("id");
                    String recommend= getNullableString(obj,"recommend",""); //obj.getString("recommend");
                    String phone=getNullableString(obj,"phone","");// JsonParser.getNullableString(obj,"phone","");
                    JSONArray category=getNullableArr(obj,"category"); //obj.getJSONArray("category");
                    sm.setname(name);
                    sm.setid(id);
                    sm.setrecommend(recommend);
                    List<SM_SecondaryM> secondaryMs=new ArrayList<>();
                    for (int c=0;c<category.length();c++)
                    {
                        JSONObject cate=(JSONObject)category.opt(c);
                        SM_SecondaryM sm_secondaryM=new SM_SecondaryM();
                        try{
                            String cname=getNullableString(cate,"name","");// cate.getString("name");
                            String cid=getNullableString(cate,"id","");//cate.getString("id");
                            JSONArray goods=getNullableArr(cate,"list");//cate.getJSONArray("list");
                            sm_secondaryM.setname(cname);
                            sm_secondaryM.setid(cid);
                            List<SM_GoodsM>goodsMs=new ArrayList<>();
                            for (int g=0;g<goods.length();g++)
                            {
                                JSONObject go=(JSONObject)goods.opt(g);
                                SM_GoodsM sm_goodsM=new SM_GoodsM();
                                try{
                                    String gname=getNullableString(go,"name","");// go.getString("name");
                                    String product_id=getNullableString(go,"product_id","");// go.getString("product_id");
                                    String pic_url=getNullableString(go,"pic_url","");//go.getString("pic_url");
                                    String oldprice=getNullableString(go,"oldprice","");//go.getString("oldprice");
                                    String price=getNullableString(go,"price","0");
                                    String specification=getNullableString(go,"specification","");//go.getString("specification");
                                    JSONArray typelist= getNullableArr(go,"type_list");//go.getJSONArray("type_list");
                                    sm_goodsM.setname(gname);
                                    sm_goodsM.setproduct_id(product_id);
                                    sm_goodsM.setpic_url(pic_url);
                                    sm_goodsM.setoldprice(oldprice);
                                    sm_goodsM.setprice(price);
                                    sm_goodsM.setspecification(specification);
                                    List<String> type_list=new ArrayList<>();
                                    for (int t=0;t<typelist.length();t++)
                                    {
                                        JSONObject type=(JSONObject)typelist.opt(t);
                                        type_list.add(getNullableString(type,"type_icon_url",""));//type.getString("type_icon_url"));
                                    }
                                    sm_goodsM.settype_list(type_list);
                                    goodsMs.add(sm_goodsM);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    continue;
                                }
                            }
                            sm_secondaryM.setlist(goodsMs);
                        }
                         catch (Exception e) {
                            e.printStackTrace();
                            continue;
                        }
                        secondaryMs.add(sm_secondaryM);
                    }
                    sm.setcategory(secondaryMs);
                    sm.setphone(phone);
                } catch (JSONException jse) {
                    continue;
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                list.add(sm);
            }
        } catch (Exception e) {
        }
        return list;
    }

    public List<SM_GoodsM>getGoodsList(String xmlString)
    {
        List<SM_GoodsM> goodsMs = new ArrayList<>();
        try {
            JSONArray goods = new JSONObject(xmlString).getJSONArray("data");

            for (int g = 0; g < goods.length(); g++) {
                JSONObject go = (JSONObject) goods.opt(g);
                SM_GoodsM sm_goodsM = new SM_GoodsM();
                try {
                    String gname =getNullableString(go,"name","");// go.getString("name");
                    String product_id =getNullableString(go,"product_id",""); //go.getString("product_id");
                    String pic_url =getNullableString(go,"pic_url","");// go.getString("pic_url");
                    String oldprice =getNullableString(go,"oldprice","");// go.getString("oldprice");
                    String price =getNullableString(go,"price","0");
                    String specification =getNullableString(go,"specification","");// go.getString("specification");
                    JSONArray typelist =getNullableArr(go,"type_list");// go.getJSONArray("type_list");
                    sm_goodsM.setname(gname);
                    sm_goodsM.setproduct_id(product_id);
                    sm_goodsM.setpic_url(pic_url);
                    sm_goodsM.setoldprice(oldprice);
                    sm_goodsM.setprice(price);
                    sm_goodsM.setspecification(specification);
                    List<String> type_list = new ArrayList<>();
                    for (int t = 0; t < typelist.length(); t++) {
                        JSONObject type = (JSONObject) typelist.opt(t);
                        type_list.add(getNullableString(type,"type_icon_url","")); //type.getString("type_icon_url"));
                    }
                    sm_goodsM.settype_list(type_list);
                    goodsMs.add(sm_goodsM);
                }  catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
        catch (JSONException jse) {
            Log.e("suntest",jse.getMessage());
            return goodsMs;
        } catch (Exception e) {
            e.printStackTrace();
            return goodsMs;
        }
        return goodsMs;
    }
    public SuperMarketM getItem(String xmlString) {
        return null; //GetSingleModel(xmlString, new SuperMarketM());
    }
}
