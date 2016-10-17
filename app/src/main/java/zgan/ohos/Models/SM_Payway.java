package zgan.ohos.Models;

import java.io.Serializable;
import java.util.List;

/**
 * Created by yajunsun on 16/10/16.
 */
public class SM_Payway extends BaseModel implements Serializable {
    private String address_name;

    public String getaddress_name() {
        return address_name;
    }

    public void setaddress_name(String address_name) {
        this.address_name = address_name;
    }

    public String getaddress_id() {
        return address_id;
    }

    public void setaddress_id(String address_id) {
        this.address_id = address_id;
    }

    public List<Integer> getpay_ways() {
        return pay_ways;
    }

    public void setpay_ways(List<Integer> pay_ways) {
        this.pay_ways = pay_ways;
    }

    private String        address_id;
    private List<Integer> pay_ways;

    @Override
    public <T> T getnewinstance() {
        return null;
    }
}
