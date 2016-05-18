package zgan.ohos.Dals;

import java.util.ArrayList;
import java.util.List;

import zgan.ohos.Models.HightQualityServiceM;
import zgan.ohos.Models.MyOrder;
import zgan.ohos.Models.MyPakage;
import zgan.ohos.R;

/**
 * Created by Administrator on 16-4-8.
 */
public class MyPakageDal extends ZGbaseDal<MyPakage> {
    public static List<MyOrder> mPakageOrders=new ArrayList<>();
    public List<MyPakage> getList(String xmlString) {
        return getModelList(xmlString,new MyPakage());
    }

    public MyPakage getItem(String xmlString) {
        return  GetSingleModel(xmlString,new MyPakage());
    }
}
