package zgan.ohos.Models;

import java.io.Serializable;

/**
 * Created by yajunsun on 16-5-6.
 */
public class HouseHolderServiceM extends BaseGoods implements Serializable{
    @Override
    public HouseHolderServiceM getnewinstance() {
        return new HouseHolderServiceM();
    }

    public String getdetails_url() {
        return details_url;
    }

    public void setdetails_url(String details_url) {
        this.details_url = details_url;
    }

    private String  details_url;

}
