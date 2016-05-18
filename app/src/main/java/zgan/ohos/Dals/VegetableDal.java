package zgan.ohos.Dals;

import java.util.ArrayList;
import java.util.List;

import zgan.ohos.Models.Vegetable;
import zgan.ohos.R;

/**
 * Created by Administrator on 16-4-6.
 */
public class VegetableDal  extends ZGbaseDal<Vegetable>{
    public List<Vegetable> getList(String xmlString) {
        return getModelList(xmlString,new Vegetable());
    }

    public Vegetable getItem(String xmlString) {
        return  GetSingleModel(xmlString,new Vegetable());
    }
}
