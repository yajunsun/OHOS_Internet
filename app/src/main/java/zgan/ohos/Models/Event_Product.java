package zgan.ohos.Models;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.io.Serializable;
import java.util.Hashtable;

/**
 * Created by yajunsun on 2015/11/24.
 */
public class Event_Product extends BaseObject implements Serializable{
    private static final long serialVersionUID = getserialVersionUID("Event_Product");
    public Event_Product() {
    }

    public Event_Product(SoapObject soapObject) {
        if (soapObject != null) {
            setId(soapObject.getProperty("Id"));
            if (soapObject.getProperty("Event") != null) {
                zgan.ohos.Models.Event event = new Event((SoapObject) soapObject.getProperty("Event"));
                setEvent(event);
            }
            if (soapObject.getProperty("Product") != null) {
                zgan.ohos.Models.Product product = new Product((SoapObject) soapObject.getProperty("Product"));
                setProduct(product);
            }
        }
    }

    public Event getEvent() {
        return Event;
    }

    public void setEvent(Object value) {
        if (value != null)
            this.Event = (Event) value;
    }

    public int getId() {
        return Id;
    }

    public void setId(Object value) {
        if (value != null)
            Id = Integer.valueOf(value.toString());
    }

    public Product getProduct() {
        return Product;
    }

    public void setProduct(Object value) {
        if (value != null)
            this.Product = (Product) value;
    }

    private int Id;
    private Product Product;
    private Event Event;


    @Override
    public String gettablename() {
        return this.getClass().getCanonicalName();
    }

    @Override
    public Event_Product getnewinstance(SoapObject soapObject) {
        return soapObject == null ? new Event_Product() : new Event_Product(soapObject);
    }

    @Override
    public Object getProperty(int i) {
        switch (i) {
            case 0:
                return Id;
            case 1:
                return Product;
            case 2:
                return Event;
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
                    Id = Integer.valueOf(o.toString());
                    break;
                case 1:
                    Product = (Product) o;
                    break;
                case 2:
                    Event = (Event) o;
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
                propertyInfo.type = zgan.ohos.Models.Product.class;
                propertyInfo.name = "Product";
                break;
            case 2:
                propertyInfo.type = zgan.ohos.Models.Event.class;
                propertyInfo.name = "Event";
                break;
            default:
                break;
        }
    }
}