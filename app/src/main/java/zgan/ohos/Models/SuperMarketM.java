package zgan.ohos.Models;

import android.view.View;

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
    //是否选中
    private  int isSelected= 0;
    //联系电话
    private String phone;

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

    public int getIsSelected(){return isSelected;}
    public void setIsSelected(int status)
    {
        isSelected=status;
    }

    public String getphone()
    {return this.phone;}
    public void setphone(String p)
    {
        if(!p.isEmpty())
            this.phone=p;
    }

    @Override
    public SuperMarketM getnewinstance() {
        return new SuperMarketM();
    }
}
