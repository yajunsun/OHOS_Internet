package zgan.ohos.Models;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import zgan.ohos.utils.SystemUtils;
import zgan.ohos.utils.generalhelper;

/**
 * Created by Administrator on 16-4-8.
 */
public class MyOrder extends BaseModel implements Serializable {


    private List<BaseGoods> goods;

    //    private String order_sn;//订单号
//    private String house_holder;//收货人
//    private String houser_name;//收货地址
//    private String order_status;//订单状态 [0未确认\1确认\2已取消\3无效\4退货] 服务状态: 根据自定义程序流程确定状态
//    private int shipping_status=0;//商品配送情况[0未发货\1已发货\2已收货\4退货]
//    private int pay_status=0;//支付状态[0未付款\1付款中\2已付款]
//    private String best_time;//预约配送时间
//    private String order_type;//下订单方式[0订购\1预约]
//    private String shipping_id;//用户选择的配送方式ID[0自提\2送货上门]
//    private int pay_id;// 用户选择的支付方式的ID[1现金\2刷卡3.支付宝 4.微信]
//    private int is_return=0;//是否退货
//    private double return_amount=0;//退货金额
//    private double goods_amount;//商品的总金额
//    private double shipping_fee=0;//配送费用
//    private double pay_fee;//支付总费用
//    private double money_paid;//实际支付金额
//    private int integral=0;//使用积分数量
//    private double integral_money=0;//积分对应的金额
//    private double order_amount;//应付款金额
//    private String add_time;//订单时间
//    private String confirm_time;//订单确认时间
//    private String pay_time;//支付时间
//    private String shipping_time;//配送时间
//    private int extension_id=0;//通过活动购买的物品ID,如果是正常普通商品,该处为0
//    private String to_buyer;//商家给客户的留言
//    private String pay_note;//付款备注
//    private int is_inv=0;//是否开发票
//    private String inv_title;//发票抬头
//    private String inv_type;//发票类型
//    private String inv_nub;//发票号
//    private String urgent_time;//加急时间
//    private double urgent_price;//加急金额
//    private int is_urgent=0;//是否加急
//    private int are_package=0;//是否套餐
//    private int stype;//0.商品 >.0服务类型
//    public String getOrder_sn() {
//        return order_sn;
//    }
//
//    public void setOrder_sn(String order_sn) {
//        this.order_sn = order_sn;
//    }
//
//    public String getHouse_holder() {
//        return house_holder;
//    }
//
//    public void setHouse_holder(String house_holder) {
//        this.house_holder = house_holder;
//    }
//
//    public String getHouser_name() {
//        return houser_name;
//    }
//
//    public void setHouser_name(String houser_name) {
//        this.houser_name = houser_name;
//    }
//
//    public String getOrder_status() {
//        return order_status;
//    }
//
//    public void setOrder_status(String order_status) {
//        this.order_status = order_status;
//    }
//
//    public int getShipping_status() {
//        return shipping_status;
//    }
//
//    public void setShipping_status(int shipping_status) {
//        this.shipping_status = shipping_status;
//    }
//
//    public int getPay_status() {
//        return pay_status;
//    }
//
//    public void setPay_status(int pay_status) {
//        this.pay_status = pay_status;
//    }
//
//    public String getBest_time() {
//        return best_time;
//    }
//
//    public void setBest_time(String best_time) {
//        this.best_time = best_time;
//    }
//
//    public String getOrder_type() {
//        return order_type;
//    }
//
//    public void setOrder_type(String order_type) {
//        this.order_type = order_type;
//    }
//
//    public String getShipping_id() {
//        return shipping_id;
//    }
//
//    public void setShipping_id(String shipping_id) {
//        this.shipping_id = shipping_id;
//    }
//
//    public int getPay_id() {
//        return pay_id;
//    }
//
//    public void setPay_id(int pay_id) {
//        this.pay_id = pay_id;
//    }
//
//    public int getIs_return() {
//        return is_return;
//    }
//
//    public void setIs_return(int is_return) {
//        this.is_return = is_return;
//    }
//
//    public double getReturn_amount() {
//        return return_amount;
//    }
//
//    public void setReturn_amount(double return_amount) {
//        this.return_amount = return_amount;
//    }
//
//    public double getGoods_amount() {
//        return goods_amount;
//    }
//
//    public void setGoods_amount(double goods_amount) {
//        this.goods_amount = goods_amount;
//    }
//
//    public double getShipping_fee() {
//        return shipping_fee;
//    }
//
//    public void setShipping_fee(double shipping_fee) {
//        this.shipping_fee = shipping_fee;
//    }
//
//    public double getPay_fee() {
//        return pay_fee;
//    }
//
//    public void setPay_fee(double pay_fee) {
//        this.pay_fee = pay_fee;
//    }
//
//    public double getMoney_paid() {
//        return money_paid;
//    }
//
//    public void setMoney_paid(double money_paid) {
//        this.money_paid = money_paid;
//    }
//
//    public int getIntegral() {
//        return integral;
//    }
//
//    public void setIntegral(int integral) {
//        this.integral = integral;
//    }
//
//    public double getIntegral_money() {
//        return integral_money;
//    }
//
//    public void setIntegral_money(double integral_money) {
//        this.integral_money = integral_money;
//    }
//
//    public double getOrder_amount() {
//        return order_amount;
//    }
//
//    public void setOrder_amount(double order_amount) {
//        this.order_amount = order_amount;
//    }
//
//    public String getAdd_time() {
//        return add_time;
//    }
//
//    public void setAdd_time(String add_time) {
//        this.add_time = add_time;
//    }
//
//    public String getConfirm_time() {
//        return confirm_time;
//    }
//
//    public void setConfirm_time(String confirm_teim) {
//        this.confirm_time = confirm_teim;
//    }
//
//    public String getPay_time() {
//        return pay_time;
//    }
//
//    public void setPay_time(String pay_time) {
//        this.pay_time = pay_time;
//    }
//
//    public String getShipping_time() {
//        return shipping_time;
//    }
//
//    public void setShipping_time(String shipping_time) {
//        this.shipping_time = shipping_time;
//    }
//
//    public int getExtension_id() {
//        return extension_id;
//    }
//
//    public void setExtension_id(int extension_id) {
//        this.extension_id = extension_id;
//    }
//
//    public String getTo_buyer() {
//        return to_buyer;
//    }
//
//    public void setTo_buyer(String to_buyer) {
//        this.to_buyer = to_buyer;
//    }
//
//    public String getPay_note() {
//        return pay_note;
//    }
//
//    public void setPay_note(String pay_note) {
//        this.pay_note = pay_note;
//    }
//
//    public int getIs_inv() {
//        return is_inv;
//    }
//
//    public void setIs_inv(int is_inv) {
//        this.is_inv = is_inv;
//    }
//
//    public String getInv_title() {
//        return inv_title;
//    }
//
//    public void setInv_title(String inv_title) {
//        this.inv_title = inv_title;
//    }
//
//    public String getInv_type() {
//        return inv_type;
//    }
//
//    public void setInv_type(String inv_type) {
//        this.inv_type = inv_type;
//    }
//
//    public String getInv_nub() {
//        return inv_nub;
//    }
//
//    public void setInv_nub(String inv_nub) {
//        this.inv_nub = inv_nub;
//    }
//
//    public String getUrgent_time() {
//        return urgent_time;
//    }
//
//    public void setUrgent_time(String urgent_time) {
//        this.urgent_time = urgent_time;
//    }
//
//    public double getUrgent_price() {
//        return urgent_price;
//    }
//
//    public void setUrgent_price(double urgent_price) {
//        this.urgent_price = urgent_price;
//    }
//
//    public int getIs_urgent() {
//        return is_urgent;
//    }
//
//    public void setIs_urgent(int is_urgent) {
//        this.is_urgent = is_urgent;
//    }
//
//    public int getAre_package() {
//        return are_package;
//    }
//
//    public void setAre_package(int are_package) {
//        this.are_package = are_package;
//    }
//
//    public int getStype() {
//        return stype;
//    }
//
//    public void setStype(int stype) {
//        this.stype = stype;
//    }
//
    public List<BaseGoods> GetGoods() {
        return goods;
    }

    public void SetGoods(List<BaseGoods> goods) {
        this.goods = goods;
    }

    public String getorder_details() {
        return order_details;
    }

    public void setorder_details(String order_details) {
        this.order_details = order_details;
    }

    public String getorder_id() {
        return order_id;
    }

    public void setorder_id(String order_id) {
        this.order_id = order_id;
    }

    public int getstate() {
        return state;
    }

    public void setstate(int state) {
        this.state = state;
    }

    public String getaccount() {
        return account;
    }

    public void setaccount(String account) {
        this.account = account;
    }

    public String getdiliver_time() {
        return diliver_time;
    }

    public void setdiliver_time(String diliver_time) {
        this.diliver_time = diliver_time;
    }

    public int getpay_type() {
        return pay_type;
    }

    public void setpay_type(int pay_type) {
        this.pay_type = pay_type;
    }

    public double gettotal() {
        return total;
    }

    public void settotal(double total) {
        this.total = total;
    }

    public int getgoods_type() {
        return goods_type;
    }

    public void setgoods_type(Object value) {
        if (value!=null)
            this.goods_type = SystemUtils.getIntValue(value.toString());
    }

    private String order_id;//	订单号码
    private int state;//	支付状态
    private String account;//	户ID(手机号码)
    private String diliver_time;//	送货时间
    private int pay_type;//	支付方式
    private double total;//	订单总金额
    private String order_details;//	订单详情
    private int goods_type=0;//商品类型 0普通商品 >0服务商品编号

    public String toString() {
        StringBuilder builder=new StringBuilder();
        for (BaseGoods g:goods) {
            //'商品id_t数量_t单价_t''属性''_t''商品名称''_p'
            builder.append("'"+g.getproduct_id()+"_t"+g.getSelectedcount()+"_t"+g.getprice()+"_t"+g.getdesc()+"_p");
        }
        return builder.toString();
    }

    public String generateOrderId() {
        StringBuilder builder = new StringBuilder(20);
        builder.append("A0000");
        String time= generalhelper.getStringFromDate(new Date(), "yyMMddHHmmssSSS");
        builder.append(time);
        //I0000112255334466555   I 苹果 A 安卓  年月日时分秒毫秒
        return builder.toString();
    }

    public String getTimeticked()
    {
        if (diliver_time.equals("0"))
            return "20分钟内";
        else
        {
            return  diliver_time;
        }
    }

    public static final int GOODS=0;
    public static final int PROLAUNDRY=1;
    public static final int CAKE=2;
    public static final int ELECMAINTENANCE=3;
    public static final int HOUSEHOLDERSERVICE=4;
    public static final int COOKIES=5;
    public static final int EXPRESSIN=80;
    public static final int EXPRESSOUT=81;
    @Override
    public MyOrder getnewinstance() {
        return new MyOrder();
    }
}
