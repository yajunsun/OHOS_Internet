package zgan.ohos.Dals;

import java.util.List;

import zgan.ohos.Models.Advertise;

/**
 * Created by Administrator on 16-4-21.
 */
public class AdvertiseDal extends ZGbaseDal<Advertise> {
    public List<Advertise> getList(String xmlString) {
        return getModelList(xmlString,new Advertise());
    }

    public Advertise getItem(String xmlString) {
        return  GetSingleModel(xmlString,new Advertise());
    }
}
