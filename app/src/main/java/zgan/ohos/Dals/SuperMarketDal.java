package zgan.ohos.Dals;

import java.util.List;

import zgan.ohos.Models.SuperMarketM;
import zgan.ohos.Models.Vegetable;

/**
 * Created by Administrator on 16-5-6.
 */
public class SuperMarketDal extends ZGbaseDal<SuperMarketM> {
    public List<SuperMarketM> getList(String xmlString) {
        return getModelList(xmlString, new SuperMarketM());
    }

    public SuperMarketM getItem(String xmlString) {
        return GetSingleModel(xmlString, new SuperMarketM());
    }
}
