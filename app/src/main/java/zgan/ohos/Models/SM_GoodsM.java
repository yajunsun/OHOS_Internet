package zgan.ohos.Models;

import java.io.Serializable;
import java.util.List;

/**
 * Created by yajunsun on 2016/9/25.
 * 超市购商品结构
 */
public class SM_GoodsM extends BaseGoods implements Serializable {
    //商品名
    private String name;
    //原价
    private String oldprice;
    //规格
    private String specification;
    //标签
    private List<String> type_list;
    //是否选中
    private boolean isSelect=false;

    public String getname() {
        return name;
    }

    public void setname(String name) {
        this.name = name;
    }

    public String getoldprice() {
        return oldprice;
    }

    public void setoldprice(String oldprice) {
        this.oldprice = oldprice;
    }

    public String getspecification() {
        return specification;
    }

    public void setspecification(String specification) {
        this.specification = specification;
    }

    public List<String> gettype_list() {
        return type_list;
    }

    public void settype_list(List<String> type_list) {
        this.type_list = type_list;
    }

    public boolean getSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }
    @Override
    public SM_GoodsM getnewinstance() {
        return new SM_GoodsM();
    }
}
