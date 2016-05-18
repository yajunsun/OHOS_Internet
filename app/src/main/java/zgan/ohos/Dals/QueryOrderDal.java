package zgan.ohos.Dals;

import java.util.List;

import zgan.ohos.Models.QueryOrderM;
import zgan.ohos.Models.Vegetable;

/**
 * Created by Administrator on 16-4-30.
 */
public class QueryOrderDal extends ZGbaseDal<QueryOrderM> {

    public List<QueryOrderM> getList(String xmlString) {
        return getModelList(xmlString, new QueryOrderM());
    }

    public QueryOrderM getItem(String xmlString) {
        return GetSingleModel(xmlString, new QueryOrderM());
    }
}
