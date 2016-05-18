package zgan.ohos.Dals;

import org.ksoap2.serialization.SoapObject;

import java.util.List;

import zgan.ohos.Models.Event_Product;
import zgan.ohos.Models.Product_Pics;

/**
 * Created by Administrator on 2015/11/25.
 */
public class Product_PicsDal extends baseDal<Product_Pics> {
    public List<Product_Pics> getProductPics(int ProductId) throws Exception {
        String SOAP_ACTION = "http://service.zgantech.com/IEventsContract/getProductPics";
        String MethodName = "getProductPics";
        SoapObject request = new SoapObject(NameSpace, MethodName);
        request.addProperty("ProductId", ProductId);
        return getnetobjectlist(new Product_Pics(), request, URL, SOAP_ACTION);
    }
}