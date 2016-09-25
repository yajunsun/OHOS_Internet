package zgan.ohos.Models;

import java.io.Serializable;
import java.util.List;

import zgan.ohos.utils.SystemUtils;

/**
 * Created by yajunsun on 16-5-6.
 * 超市购数据结构
 */
public class SuperMarketM extends BaseModel  implements Serializable {

    //分类名
    private String name;
    //分类id
    private String id;
    //是否店主推荐 0推荐 1不推荐
    private String recommend;
    //二级分类集合
    private List<SM_SecondaryM>category;

    public String getname() {
        return name;
    }

    public void setname(String name) {
        this.name = name;
    }

    public String getid() {
        return id;
    }

    public void setid(String id) {
        this.id = id;
    }

    public String getrecommend() {
        return recommend;
    }

    public void setrecommend(String recommend) {
        this.recommend = recommend;
    }

    public List<SM_SecondaryM> getcategory() {
        return category;
    }

    public void setcategory(List<SM_SecondaryM> category) {
        this.category = category;
    }

    @Override
    public SuperMarketM getnewinstance() {
        return new SuperMarketM();
    }
}
