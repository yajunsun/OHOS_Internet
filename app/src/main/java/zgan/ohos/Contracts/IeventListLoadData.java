package zgan.ohos.Contracts;

import java.io.Serializable;
import java.util.List;

import zgan.ohos.Models.Event_Product;

/**
 * Created by yajunsun on 2015/11/27.
 */
public interface IeventListLoadData extends Serializable {
    void loadData(List<Event_Product>_eplst) throws Exception;
}