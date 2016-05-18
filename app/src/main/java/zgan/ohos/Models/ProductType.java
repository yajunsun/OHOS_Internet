package zgan.ohos.Models;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.io.Serializable;
import java.util.Hashtable;

/**
 * Created by Administrator on 2016/1/11.
 */
public class ProductType extends BaseObject implements Serializable {

    private static final long serialVersionUID = getserialVersionUID("ProductType");
    public ProductType(){}
    public ProductType(SoapObject soapObject)
    {
        if(soapObject!=null)
        {
            setId(soapObject.getProperty("Id"));
            setName(soapObject.getProperty("Name"));
            setParentId(soapObject.getProperty("ParentId"));
        }
    }
    private int Id;
    private String Name;
    private int ParentId;

    public int getId() {
        return Id;
    }

    public void setId(Object value) {
        if (value != null)
            Id = Integer.valueOf(value.toString());
    }

    public String getName() {
        return Name;
    }

    public void setName(Object value) {
        if (value != null)
            Name = value.toString();
    }

    public int getParentId() {
        return ParentId;
    }

    public void setParentId(Object value) {
        if (value != null)
            ParentId =Integer.valueOf(value.toString());
    }

    @Override
    public String gettablename() {
        return "ProductType";
    }

    @Override
    public ProductType getnewinstance(SoapObject soapObject) {
        return (soapObject==null)?new ProductType():new ProductType(soapObject);
    }

    @Override
    public Object getProperty(int i) {
        switch (i)
        {
            case 0:
                return Id;
            case 1:
                return Name;
            case 2:
                return ParentId;
        }
        return null;
    }

    @Override
    public int getPropertyCount() {
        return 3;
    }

    @Override
    public void setProperty(int i, Object o) {
        switch (i)
        {
            case 0:
                setId(o);
                break;
            case 1:
                setName(o);
                break;
            case 2:
                setParentId(o);
                break;
        }
    }

    @Override
    public void getPropertyInfo(int i, Hashtable hashtable, PropertyInfo propertyInfo) {
        propertyInfo.namespace=super.NAMESPACE;
        switch (i)
        {
            case 0:
                propertyInfo.name= "Id";
                propertyInfo.type=PropertyInfo.INTEGER_CLASS;
                break;
            case 1:
                propertyInfo.name= "Name";
                propertyInfo.type=PropertyInfo.STRING_CLASS;
                break;
            case 2:
                propertyInfo.name= "ParentId";
                propertyInfo.type=PropertyInfo.INTEGER_CLASS;
                break;
        }
    }
}