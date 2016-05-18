package zgan.ohos.Dals;

import java.util.ArrayList;
import java.util.List;

import zgan.ohos.Activities.HightQualityService;
import zgan.ohos.Models.HightQualityServiceM;
import zgan.ohos.R;

/**
 * Created by Administrator on 16-4-5.
 */
public class HightQualityDal extends ZGbaseDal<HightQualityServiceM> {

    public List<HightQualityServiceM> getList(String xmlString) {
        return getModelList(xmlString,new HightQualityServiceM());
    }

    public HightQualityServiceM getItem(String xmlString) {
        return  GetSingleModel(xmlString,new HightQualityServiceM());
    }
}
