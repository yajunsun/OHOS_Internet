package zgan.ohos.Dals;

import java.util.List;

import zgan.ohos.Models.FuncPage;

/**
 * Created by Administrator on 16-6-23.
 */
public class FuncPageDal extends ZGbaseDal<FuncPage> {
    public List<FuncPage> getList(String xmlString) {
        return getModelList(xmlString, new FuncPage());
    }

    public FuncPage getItem(String xmlString) {
        return GetSingleModel(xmlString, new FuncPage());
    }
}
