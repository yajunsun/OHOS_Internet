package zgan.ohos.Models;

import java.io.Serializable;

import zgan.ohos.utils.SystemUtils;

/**
 * Created by Administrator on 16-4-30.
 */
public class QueryOrderM extends BaseModel  implements Serializable {
    private String order_id;//	订单id
    private int order_state;//	订单状态
    private String title;//	商品标题
    private int count;//	商品数量
    private double price;//	商品单价
    private double priceTotal;//	订单总金额
    private String sub_time;//	下单时间
    private int pay_type;//	支付方式
    private String diliver_time;//	预计完成时间
    private String pic_url;//商品图片
    //private String over_time;//	配送完成时间

//    public String getover_time() {
//        return over_time;
//    }
//
//    public void setover_time(Object value) {
//        if (value != null)
//            this.over_time = value.toString();
//    }

    public String getpic_url() {
        return pic_url;
    }

    public void setpic_url(Object value) {
        if (value != null)
            this.pic_url = value.toString();
    }

    public String getorder_id() {
        return order_id;
    }

    public void setorder_id(Object value) {
        if (value != null)
            this.order_id = value.toString();
    }

    public int getorder_state() {
        return order_state;
    }

    public void setorder_state(Object value) {
        if (value != null)
            this.order_state = SystemUtils.getIntValue(value.toString());
    }

    public String gettitle() {
        return title;
    }

    public void settitle(Object value) {
        if (value != null)
            this.title = value.toString();
    }

    public int getcount() {
        return count;
    }

    public void setcount(Object value) {
        if (value != null)
            this.count = SystemUtils.getIntValue(value.toString());
    }

    public double getprice() {
        return price;
    }

    public void setprice(Object value) {
        if (value != null)
            this.price = Double.parseDouble(value.toString());
    }

    public double getpriceTotal() {
        return priceTotal;
    }

    public void setpriceTotal(Object value) {
        if (value != null)
            this.priceTotal = Double.parseDouble(value.toString());
    }

    public String getsub_time() {
        return sub_time;
    }

    public void setsub_time(Object value) {
        if (value != null)
            this.sub_time = value.toString();
    }

    public int getpay_type() {
        return pay_type;
    }

    public void setpay_type(Object value) {
        if (value != null)
            this.pay_type = SystemUtils.getIntValue(value.toString());
    }

    public String getdiliver_time() {
        return diliver_time;
    }

    public void setdiliver_time(Object value) {
        if (value != null)
            this.diliver_time = value.toString();
    }

    @Override
    public QueryOrderM getnewinstance() {
        return new QueryOrderM();
    }


    public String getStatusText() {
        String result = "配送中";
        switch (order_state) {
            case 0:
            case 1:
                result = "新订单";
                break;
            case 2:
                result = "已取消";
                break;
            case 3:
                result = "已无效";
                break;
            case 4:
                result = "已退货";
                break;
            case 5:
                result = "已完成";
                break;
            case 6:
                result = "配送中";
                break;
        }
        return result;
    }
}
