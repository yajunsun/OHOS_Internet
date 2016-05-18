package zgan.ohos.Models;

import java.io.Serializable;

/**
 * Created by Administrator on 16-4-5.
 */
public class HightQualityServiceM extends BaseGoods implements Serializable{

    public int getServiceType() {
        return serviceType;
    }

    public void setServiceType(int serviceType) {
        this.serviceType = serviceType;
    }

    /***
     * 1土特产 2高端特供
     */
    private int serviceType=0;

    public String getdetails_url() {
        return details_url;
    }

    public void setdetails_url(String imgdesc) {
        this.details_url = imgdesc;
    }

    private String details_url;

    @Override
    public HightQualityServiceM getnewinstance() {
        return new HightQualityServiceM();
    }
}
