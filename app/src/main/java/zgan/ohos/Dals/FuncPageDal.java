package zgan.ohos.Dals;

import java.util.List;

import zgan.ohos.Models.FuncPage1;

/**
 * Created by Administrator on 16-6-23.
 */
public class FuncPageDal extends ZGbaseDal<FuncPage1> {
    public List<FuncPage1> getList(String xmlString) {
        return getModelList(xmlString, new FuncPage1());
    }

    public FuncPage1 getItem(String xmlString) {
        return GetSingleModel(xmlString, new FuncPage1());
    }
}
