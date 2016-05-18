package zgan.ohos.Dals;

import org.ksoap2.serialization.SoapObject;

import java.util.List;

import zgan.ohos.Models.Message;
import zgan.ohos.Models.Product;

/**
 * Created by yajunsun on 2015/11/24.
 */
public class ProductDal extends baseDal<Product> {
    String URL="http://192.168.1.108:10002/MallService";
    public List<Product> GetProductsInSale(int pagesize, int pageindex , int type , String key ) throws Exception
    {
        String SOAP_ACTION = "http://tempuri.org/IMallContract/GetProductsInSale";
        String MethodName = "GetProductsInSale";
        SoapObject request = new SoapObject(NameSpace, MethodName);
        request.addProperty("pagesize", pagesize);
        request.addProperty("pageindex", pageindex);
        request.addProperty("type", type);
        request.addProperty("key",key);
        return getnetobjectlist(new Product(), request, URL, SOAP_ACTION);
    }
}