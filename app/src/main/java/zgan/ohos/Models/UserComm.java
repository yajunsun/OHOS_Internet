package zgan.ohos.Models;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.util.Hashtable;

/**
 * Created by yajunsun on 2015/12/10.
 */
public class UserComm extends BaseObject {

    private int Comm_Id;
    private String Comm_Name;

    public int getComm_Id() {
        return Comm_Id;
    }

    public void setComm_Id(Object value) {
        if (value != null)
            Comm_Id = Integer.valueOf(value.toString());
    }

    public String getComm_Name() {
        return Comm_Name;
    }

    public void setComm_Name(Object value) {
        if (value != null)
            Comm_Name = value.toString();
    }

    public int getFComm_Id() {
        return FComm_Id;
    }

    public void setFComm_Id(Object value) {
        if (value != null)
            this.FComm_Id = Integer.valueOf(value.toString());
    }

    public int getHasChild() {
        return HasChild;
    }

    public void setHasChild(Object value) {
        if (value != null)
            HasChild = Integer.valueOf(value.toString());
    }

    private int FComm_Id;
    private int HasChild;

    public UserComm() {
    }

    public UserComm(SoapObject soapObject) {
        if (soapObject != null) {
            setComm_Id(soapObject.getProperty("Comm_Id"));
            setComm_Name(soapObject.getProperty("Comm_Name"));
            setFComm_Id(soapObject.getProperty("FComm_Id"));
            setHasChild(soapObject.getProperty("HasChild"));
        }
    }

    @Override
    public String gettablename() {
        return this.getClass().getCanonicalName();
    }

    @Override
    public UserComm getnewinstance(SoapObject soapObject) {
        return soapObject == null ? new UserComm() : new UserComm(soapObject);
    }

    @Override
    public Object getProperty(int i) {
        switch (i) {
            case 0:
                return Comm_Id;
            case 1:
                return Comm_Name;
            case 2:
                return FComm_Id;
            case 3:
                return HasChild;
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
        if (o != null) {
            switch (i) {
                case 0:
                    Comm_Id = Integer.valueOf(o.toString());
                    break;
                case 1:
                    Comm_Name = o.toString();
                    break;
                case 2:
                    FComm_Id = Integer.valueOf(o.toString());
                    break;
                case 3:
                    HasChild = Integer.valueOf(o.toString());
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void getPropertyInfo(int i, Hashtable hashtable, PropertyInfo propertyInfo) {
        propertyInfo.namespace = super.NAMESPACE;
        switch (i) {
            case 0:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "Comm_Id";
                break;
            case 1:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "Comm_Name";
                break;
            case 2:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "FComm_Id";
                break;
            case 3:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "HasChild";
                break;
            default:
                break;
        }
    }
}
