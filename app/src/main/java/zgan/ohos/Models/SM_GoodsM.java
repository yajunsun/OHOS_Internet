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
    //是否选中 和can_handsel有关联
    private boolean isSelect=false;
    //选中为1 未选中为0  默认为选中
    private int can_handsel=1;

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

    public int getcan_handsel() {
        return can_handsel;
    }

    public void setcan_handsel(int can_handsel) {
        this.can_handsel = can_handsel;
        setSelect(can_handsel==1);
    }

    @Override
    public SM_GoodsM getnewinstance() {
        return new SM_GoodsM();
    }
}
