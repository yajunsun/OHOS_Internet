package zgan.ohos.Dals;

import zgan.ohos.Models.RequstResultM;

/**
 * Created by yajunsun on 16/10/17.
 */
public class RequstResultDal extends ZGbaseDal<RequstResultM>{
    public RequstResultM getItem(String xmlString) {
        return  GetSingleModel(xmlString,new RequstResultM());
    }
}
