package zgan.ohos.Models;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.io.Serializable;
import java.util.Hashtable;

import zgan.ohos.utils.SystemUtils;

/**
 * Created by yajunsun on 2015/11/24.
 */
public class Partin extends BaseObject implements Serializable{
    private static final long serialVersionUID = getserialVersionUID("Partin");
    public Partin() {
    }

    @Override
    public String gettablename() {
        return this.getClass().getCanonicalName();
    }

    @Override
    public Partin getnewinstance(SoapObject soapObject) {
        return soapObject == null ? new Partin() : new Partin(soapObject);
    }

    public Partin(SoapObject soapObject) {
        if (soapObject != null) {
            setId(soapObject.getProperty("Id"));
            setEventId(soapObject.getProperty("EventId"));
            setPhone(soapObject.getProperty("Phone"));
            setAdress(soapObject.getProperty("Adress"));
        }
    }

    public int Id;

    public String getAdress() {
        return Adress;
    }

    public void setAdress(Object value) {
        if (value != null)
            Adress = value.toString();
    }

    public int getEventId() {
        return EventId;
    }

    public void setEventId(Object value) {
        if (value != null)
            EventId = SystemUtils.getIntValue(value.toString());
    }

    public int getId() {
        return Id;
    }

    public void setId(Object id) {
        if (id != null)
            Id = SystemUtils.getIntValue(id.toString());
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(Object value) {
        if (value != null)
            Phone = value.toString();
    }

    public int EventId;

    public String Phone;

    public String Adress;

    @Override
    public Object getProperty(int i) {
        switch (i) {
            case 0:
                return Id;
            case 1:
                return EventId;
            case 2:
                return Phone;
            case 3:
                return Adress;
            default:
                return null;
        }
    }

    @Override
    public int getPropertyCount() {
        return 4;
    }

    @Override
    public void setProperty(int i, Object o) {
        if (o != null)
            switch (i) {
                case 0:
                    this.Id = SystemUtils.getIntValue(o.toString());
                    break;
                case 1:
                    this.EventId =SystemUtils.getIntValue(o.toString());
                    break;
                case 2:
                    this.Phone = o.toString();
                    break;
                case 3:
                    this.Adress = o.toString();
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
                propertyInfo.name = "EventId";
                break;
            case 2:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "Phone";
                break;
            case 3:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "Adress";
                break;
            default:
                break;
        }
    }
}