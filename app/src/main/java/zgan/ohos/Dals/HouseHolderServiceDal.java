package zgan.ohos.Dals;

import java.util.List;

import zgan.ohos.Models.HouseHolderServiceM;

/**
 * Created by Administrator on 16-5-6.
 */
public class HouseHolderServiceDal extends ZGbaseDal<HouseHolderServiceM> {
    public List<HouseHolderServiceM> getList(String xmlString) {
        return getModelList(xmlString, new HouseHolderServiceM());
    }

    public HouseHolderServiceM getItem(String xmlString) {
        return GetSingleModel(xmlString, new HouseHolderServiceM());
    }
}
