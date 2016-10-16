package zgan.ohos.Models;

import java.io.Serializable;
import java.util.List;

/**
 * Created by yajunsun on 2016/10/15.
 */
public class SM_OrderPayInfo extends BaseModel implements Serializable {
    private String order_sn;// 主订单号
    private int order_type;//  订单类型
    private String pay_order_sn;// 支付订单号
    private int pay_way;// 支付方式
    private String total_price;//  商品总价
    private SM_OrderPayDetail pay_ways;//  支付返回对象

    public String getorder_sn() {
        return order_sn;
    }

    public void setorder_sn(String order_sn) {
        this.order_sn = order_sn;
    }

    public int getorder_type() {
        return order_type;
    }

    public void setorder_type(int order_type) {
        this.order_type = order_type;
    }

    public String getpay_order_sn() {
        return pay_order_sn;
    }

    public void setpay_order_sn(String pay_order_sn) {
        this.pay_order_sn = pay_order_sn;
    }

    public int getpay_way() {
        return pay_way;
    }

    public void setpay_way(int pay_way) {
        this.pay_way = pay_way;
    }

    public String gettotal_price() {
        return total_price;
    }

    public void settotal_price(String total_price) {
        this.total_price = total_price;
    }

    public SM_OrderPayDetail getpay_ways() {
        return pay_ways;
    }

    public void setpay_ways(SM_OrderPayDetail pay_ways) {
        this.pay_ways = pay_ways;
    }


    @Override
    public SM_OrderPayInfo getnewinstance() {
        return new SM_OrderPayInfo();
    }
}
