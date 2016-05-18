package zgan.ohos.Dals;

import java.util.List;

import zgan.ohos.Models.Cake;

/**
 * Created by Administrator on 16-4-5.
 */
public class CakeDal extends ZGbaseDal<Cake>{
    public List<Cake> getList(String xmlString) {
        return getModelList(xmlString,new Cake());
    }

    public Cake getItem(String xmlString) {
        return  GetSingleModel(xmlString,new Cake());
    }
}
