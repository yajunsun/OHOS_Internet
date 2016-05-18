package zgan.ohos.Dals;

import java.util.List;

import zgan.ohos.Models.FrontItem;

/**
 * Created by Administrator on 16-4-21.
 */
public class FrontItemDal extends  ZGbaseDal<FrontItem> {
    public List<FrontItem> getList(String xmlString) {
        return getModelList(xmlString,new FrontItem());
    }

    public FrontItem getItem(String xmlString) {
        return  GetSingleModel(xmlString,new FrontItem());
    }
}
