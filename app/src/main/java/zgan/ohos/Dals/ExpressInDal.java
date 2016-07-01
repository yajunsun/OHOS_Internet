package zgan.ohos.Dals;

import java.util.List;

import zgan.ohos.Models.ExpressIn;
import zgan.ohos.Models.Message;

/**
 * Created by Administrator on 16-6-30.
 */
public class ExpressInDal extends ZGbaseDal<ExpressIn> {
    public List<ExpressIn> Getlist(String xmlString) {
        return getModelList(xmlString,new ExpressIn());
    }

    public ExpressIn GetModel(String xmlString) {
        return  GetSingleModel(xmlString,new ExpressIn());
    }
}
