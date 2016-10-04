package zgan.ohos.Models;

import java.io.Serializable;
import java.util.List;

/**
 * Created by yajunsun on 16/10/4.
 */
public class ShoppingCart extends BaseModel implements Serializable {

    private String distributionType;
    private String distribution_Icon_url;
    private List<SM_GoodsM> productArray;

    public String getdistributionType() {
        return distributionType;
    }

    public void setdistributionType(String distributionType) {
        this.distributionType = distributionType;
    }

    public String getdistribution_Icon_url() {
        return distribution_Icon_url;
    }

    public void setdistribution_Icon_url(String distribution_Icon_url) {
        this.distribution_Icon_url = distribution_Icon_url;
    }

    public List<SM_GoodsM> getproductArray() {
        return productArray;
    }

    public void setproductArray(List<SM_GoodsM> productArray) {
        this.productArray = productArray;
    }

    @Override
    public ShoppingCart getnewinstance() {
        return new ShoppingCart();
    }
}
