package zgan.ohos.Models;

import java.io.Serializable;

import zgan.ohos.utils.SystemUtils;

/**
 * Created by Administrator on 16-5-6.
 */
public class SuperMarketM extends BaseModel  implements Serializable {

    private int order;
    private String pic_url;

    public int getorder() {
        return order;
    }

    public void setorder(Object value) {
        if (value != null)
            this.order = SystemUtils.getIntValue(value.toString());
    }

    public String getpic_url() {
        return pic_url;
    }

    public void setpic_url(Object value) {
        if (value != null)
            this.pic_url = value.toString();
    }

    @Override
    public SuperMarketM getnewinstance() {
        return new SuperMarketM();
    }
}
