package zgan.ohos.Models;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.util.Hashtable;

/**
 * Created by yajunsun on 2016/1/13.
 */
public class HouseHoldingMsg extends BaseObject {

    private int Id;
    private String Ffrom;
    private String Fto;
    private String Fcontent;
    private String Fimage;
    private String Ftime;

    public HouseHoldingMsg() {
    }

    public HouseHoldingMsg(SoapObject soapObject) {
        if (soapObject != null) {
            setId(soapObject.getProperty("Id"));
            setFfrom(soapObject.getProperty("Ffrom"));
            setFto(soapObject.getProperty("Fto"));
            setFcontent(soapObject.getProperty("Fcontent"));
            setFimage(soapObject.getProperty("Fimage"));
            setFtime(soapObject.getProperty("Ftime"));
        }
    }

    public int getId() {
        return Id;
    }

    public void setId(Object value) {
        if (value != null)
            Id = Integer.valueOf(value.toString());
    }

    public String getFcontent() {
        return Fcontent;
    }

    public void setFcontent(Object value) {
        if (value != null)
            Fcontent = value.toString();
    }

    public String getFfrom() {
        return Ffrom;
    }

    public void setFfrom(Object value) {
        if (value != null)
            Ffrom = value.toString();
    }

    public String getFimage() {
        return Fimage;
    }

    public void setFimage(Object value) {
        if (value != null)
            Fimage = value.toString();
    }

    public String getFtime() {
        return Ftime;
    }

    public void setFtime(Object value) {
        if (value != null)
            Ftime = value.toString();
    }

    public String getFto() {
        return Fto;
    }

    public void setFto(Object value) {
        if (value != null)
            Fto = value.toString();
    }

    @Override
    public String gettablename() {
        return "HouseHoldingMsg";
    }

    @Override
    public HouseHoldingMsg getnewinstance(SoapObject soapObject) {
        return soapObject == null ? new HouseHoldingMsg() : new HouseHoldingMsg(soapObject);
    }

    @Override
    public Object getProperty(int i) {
        switch (i) {
            case 0:
                return Id;
            case 1:
                return Ffrom;
            case 2:
                return Fto;
            case 3:
                return Fcontent;
            case 4:
                return Fimage;
            case 5:
                return Ftime;
        }
        return null;
    }

    @Override
    public int getPropertyCount() {
        return 6;
    }

    @Override
    public void setProperty(int i, Object o) {
        switch (i) {
            case 0:
                setId(o);
                break;
            case 1:
                setFfrom(o);
                break;
            case 2:
                setFto(o);
                break;
            case 3:
                setFcontent(o);
                break;
            case 4:
                setFimage(o);
                break;
            case 5:
                setFtime(o);
                break;
        }
    }

    @Override
    public void getPropertyInfo(int i, Hashtable hashtable, PropertyInfo propertyInfo) {
        propertyInfo.namespace = super.NAMESPACE;
        switch (i) {
            case 0:
                propertyInfo.name = "Id";
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                break;
            case 1:
                propertyInfo.name = "Ffrom";
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                break;
            case 2:
                propertyInfo.name = "Fto";
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                break;
            case 3:
                propertyInfo.name = "Fcontent";
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                break;
            case 4:
                propertyInfo.name = "Fimage";
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                break;
            case 5:
                propertyInfo.name = "Ftime";
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                break;
        }
    }
}
