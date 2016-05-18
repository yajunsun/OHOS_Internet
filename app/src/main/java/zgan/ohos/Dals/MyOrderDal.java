package zgan.ohos.Dals;

import java.util.ArrayList;
import java.util.List;

import zgan.ohos.Models.Cake;
import zgan.ohos.Models.MyOrder;

/**
 * Created by Administrator on 16-4-8.
 */
public class MyOrderDal extends ZGbaseDal<MyOrder> {

    public List<MyOrder> getList(String xmlString) {
        return getModelList(xmlString, new MyOrder());
    }

    public MyOrder getItem(String xmlString) {
        return GetSingleModel(xmlString, new MyOrder());
    }

    // public static List<MyOrder>mConfirmedOrders=new ArrayList<>();

//    public List<MyOrder> getList() {
//        List<MyOrder> list = new ArrayList<>();
//        MyOrder m1 = new MyOrder();
//        m1.setOrder_sn("2016041500245");
//        m1.setHouse_holder("徐鹏");
//        m1.setHouser_name("金易伯爵世家4栋3单元6-2");
//        m1.setOrder_status("已确认");
//        m1.setShipping_status(0);
//        m1.setPay_status(0);
//        m1.setBest_time("2016-04-15 18:00:00");
//        m1.setOrder_type("订购");
//        m1.setShipping_id("送货上门");
//        m1.setPay_id(3);
//        m1.setGoods_amount(300);
//        m1.setPay_fee(300);
//        m1.setMoney_paid(0);
//        m1.setOrder_amount(300);
//        m1.setAdd_time("2016-04-15 09:45:23");
//        m1.setConfirm_time("2016-04-15 09:50:00");
//
//        list.add(m1);
//        return list;
//    }
}
