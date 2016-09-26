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
                    String name = obj.getString("name");
                    String id=obj.getString("id");
                    String recommend=  obj.getString("recommend");
                    JSONArray category=obj.getJSONArray("category");
                    sm.setname(name);
                    sm.setid(id);
                    sm.setrecommend(recommend);
                    List<SM_SecondaryM> secondaryMs=new ArrayList<>();
                    for (int c=0;c<category.length();c++)
                    {
                        JSONObject cate=(JSONObject)category.opt(c);
                        SM_SecondaryM sm_secondaryM=new SM_SecondaryM();
                        try{
                            String cname=cate.getString("name");
                            String cid=cate.getString("id");
                            JSONArray goods=cate.getJSONArray("list");
                            sm_secondaryM.setname(cname);
                            sm_secondaryM.setid(cid);
                            List<SM_GoodsM>goodsMs=new ArrayList<>();
                            for (int g=0;g<goods.length();g++)
                            {
                                JSONObject go=(JSONObject)goods.opt(g);
                                SM_GoodsM sm_goodsM=new SM_GoodsM();
                                try{
                                    String gname=go.getString("name");
                                    String product_id=go.getString("product_id");
                                    String pic_url=go.getString("pic_url");
                                    String oldprice=go.getString("oldprice");
                                    String specification=go.getString("specification");
                                    JSONArray typelist=go.getJSONArray("type_list");
                                    sm_goodsM.setname(gname);
                                    sm_goodsM.setproduct_id(product_id);
                                    sm_goodsM.setpic_url(pic_url);
                                    sm_goodsM.setoldprice(oldprice);
                                    sm_goodsM.setspecification(specification);
                                    List<String> type_list=new ArrayList<>();
                                    for (int t=0;t<typelist.length();t++)
                                    {
                                        JSONObject type=(JSONObject)typelist.opt(t);
                                        type_list.add(type.getString("type_icon_url"));
                                    }
                                    sm_goodsM.settype_list(type_list);
                                    goodsMs.add(sm_goodsM);
                                }catch (JSONException jse) {
                                    continue;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    continue;
                                }
                            }
                            sm_secondaryM.setlist(goodsMs);
                        }
                        catch (JSONException jse) {
                            continue;
                        } catch (Exception e) {
                            e.printStackTrace();
                            continue;
                        }
                        secondaryMs.add(sm_secondaryM);
                    }
                    sm.setcategory(secondaryMs);
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
                    String gname = go.getString("name");
                    String product_id = go.getString("product_id");
                    String pic_url = go.getString("pic_url");
                    String oldprice = go.getString("oldprice");
                    String specification = go.getString("specification");
                    JSONArray typelist = go.getJSONArray("type_list");
                    sm_goodsM.setname(gname);
                    sm_goodsM.setproduct_id(product_id);
                    sm_goodsM.setpic_url(pic_url);
                    sm_goodsM.setoldprice(oldprice);
                    sm_goodsM.setspecification(specification);
                    List<String> type_list = new ArrayList<>();
                    for (int t = 0; t < typelist.length(); t++) {
                        JSONObject type = (JSONObject) typelist.opt(t);
                        type_list.add(type.getString("type_icon_url"));
                    }
                    sm_goodsM.settype_list(type_list);
                    goodsMs.add(sm_goodsM);
                } catch (JSONException jse) {
                    continue;
                } catch (Exception e) {
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
