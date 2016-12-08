package zgan.ohos.Models;

import java.io.Serializable;
import java.util.List;

import zgan.ohos.Activities.MainShoppingCart;

/**
 * Created by yajunsun on 2016/12/8.
 */
public class MainShoppingCartM extends BaseModel implements Serializable {

    //是否有三方商家产品
    private int business_flag;

    public int getbusiness_flag() {
        return business_flag;
    }

    public void setbusiness_flag(int business_flag) {
        this.business_flag = business_flag;
    }

    public List<BussinessShoppingCartM> getbussiness_goodsArray() {
        return bussiness_GoodsArray;
    }

    public void setbussiness_goodsArray(List<BussinessShoppingCartM> bussiness_GoodsArray) {
        this.bussiness_GoodsArray = bussiness_GoodsArray;
    }

    //商家数组
    private List<BussinessShoppingCartM> bussiness_GoodsArray;


    @Override
    public MainShoppingCartM getnewinstance() {
        return new MainShoppingCartM();
    }
}
