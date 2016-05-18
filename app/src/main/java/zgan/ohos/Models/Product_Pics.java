package zgan.ohos.Models;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.io.Serializable;
import java.util.Hashtable;

/**
 * Created by yajunsun on 2015/11/24.
 */
public class Product_Pics extends BaseObject implements Serializable {
    private static final long serialVersionUID = getserialVersionUID("Product_Pics");

    public Product_Pics() {
    }

    @Override
    public String gettablename() {
        return this.getClass().getCanonicalName();
    }

    @Override
    public Product_Pics getnewinstance(SoapObject soapObject) {
        return soapObject == null ? new Product_Pics() : new Product_Pics(soapObject);
    }

    public Product_Pics(SoapObject soapObject) {
        if (soapObject != null) {
            setId(soapObject.getProperty("Id"));
            setProductId(soapObject.getProperty("ProductId"));
            setPicName(soapObject.getProperty("PicName"));
        }
    }

    private int Id;

    private int ProductId;

    public int getId() {
        return Id;
    }

    public void setId(Object id) {
        if (id != null)
            Id = Integer.valueOf(id.toString());
    }

    public String getPicName() {
        return PicName;
    }

    public void setPicName(Object
                                   value) {
        if (value != null)
            PicName = value.toString();
    }

    public int getProductId() {
        return ProductId;
    }

    public void setProductId(Object value) {
        if (value != null)
            ProductId = Integer.valueOf(value.toString());
    }

    private String PicName;

    @Override
    public Object getProperty(int i) {
        switch (i) {
            case 0:
                return Id;
            case 1:
                return ProductId;
            case 2:
                return PicName;
            default:
                return null;
        }
    }

    @Override
    public int getPropertyCount() {
        return 3;
    }

    @Override
    public void setProperty(int i, Object o) {
        if (o != null)
            switch (i) {
                case 0:
                    this.Id = Integer.valueOf(o.toString());
                    break;
                case 1:
                    this.ProductId = Integer.valueOf(o.toString());
                    break;
                case 2:
                    this.PicName = o.toString();
                    break;
                default:
                    break;
            }
    }

    @Override
    public void getPropertyInfo(int i, Hashtable hashtable, PropertyInfo propertyInfo) {
        propertyInfo.namespace = super.NAMESPACE;
        switch (i) {
            case 0:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "Id";
                break;
            case 1:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "ProductId";
                break;
            case 2:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "PicName";
                break;
            default:
                break;
        }
    }
}