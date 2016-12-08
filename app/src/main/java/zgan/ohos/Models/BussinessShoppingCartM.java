package zgan.ohos.Models;

import java.io.Serializable;
import java.util.List;

import zgan.ohos.Activities.ShoppingCart;

/**
 * Created by yajunsun on 2016/12/8.
 */
public class BussinessShoppingCartM extends BaseModel implements Serializable {

    //商家分类id
    private String bussinessCid;
    //商家分类名称
    private String bussinessName;
    //产品数组
    private List<ShoppingCartM> goodsArray;

    public List<ShoppingCartM> getgoodsarray() {
        return goodsArray;
    }

    public void setgoodsarray(List<ShoppingCartM> goodsArray) {
        this.goodsArray = goodsArray;
    }

    public String getbussinesscid() {
        return bussinessCid;
    }

    public void setbussinesscid(String bussinessCid) {
        this.bussinessCid = bussinessCid;
    }

    public String getbussinessname() {
        return bussinessName;
    }

    public void setbussinessname(String bussinessName) {
        this.bussinessName = bussinessName;
    }

    @Override
    public BussinessShoppingCartM getnewinstance() {
        return new BussinessShoppingCartM();
    }
}
