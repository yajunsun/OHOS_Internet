package zgan.ohos.Models;

import java.io.Serializable;
import java.util.List;

/**
 * Created by yajunsun on 2016/9/25.
 * 超市购二级分类
 */
public class SM_SecondaryM extends BaseModel implements Serializable{
    //分类名
    private String name;
    //分类id
    private String id;
    //商品集合
    private List<SM_GoodsM>list;
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

    public List<SM_GoodsM> getlist() {
        return list;
    }

    public void setlist(List<SM_GoodsM> list) {
        this.list = list;
    }
    @Override
    public SM_SecondaryM getnewinstance() {
        return new SM_SecondaryM();
    }
}
