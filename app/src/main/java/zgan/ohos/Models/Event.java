package zgan.ohos.Models;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Objects;

/**
 * Created by yajunsun on 2015/11/24.
 */
public class Event extends BaseObject implements Serializable{
    private String ePlace;
    private String eRules;
    private int Id;
    private String bTime;
    private String eTime;
    private int restrict_Members;
    private float pre_Money;
    private String eImage;

    private static final long serialVersionUID = getserialVersionUID("Event");
    public Event() {
    }

    public Event(SoapObject soapObject) {
        if (soapObject != null) {
            setId(soapObject.getProperty("Id"));
            setBtime(soapObject.getProperty("bTime"));
            setEtime(soapObject.getProperty("eTime"));
            setEplace(soapObject.getProperty("ePlace"));
            setErules(soapObject.getProperty("eRules"));
            setRestrict_members(soapObject.getProperty("restrict_Members"));
            setPre_money(soapObject.getProperty("pre_Money"));
            setEimage(soapObject.getProperty("eImage"));
        }
    }

    public int getId() {
        return Id;
    }

    public void setId(Object id) {
        if (id != null)
            Id = Integer.valueOf(id.toString());
    }

    /**
     * @return 返回 btime。
     */
    public String getBtime() {
        return bTime;
    }

    /**
     * @param value 要设置的 btime。
     */
    public void setBtime(Object value) {
        if (value != null)
            this.bTime = value.toString();
    }

    /**
     * @return 返回 etime。
     */
    public String getEtime() {
        return eTime;
    }

    /**
     * @param value 要设置的 etime。
     */
    public void setEtime(Object value) {
        if (value != null)
            this.eTime = value.toString();
    }

    /**
     * @return 返回 eplace。
     */
    public String getEplace() {
        return ePlace;
    }

    /**
     * @param value 要设置的 eplace。
     */
    public void setEplace(Object value) {
        if (value != null)
            this.ePlace = value.toString();
    }

    /**
     * @return 返回 restrict_members。
     */
    public int getRestrict_members() {
        return restrict_Members;
    }

    /**
     * @param value 要设置的 restrict_members。
     */
    public void setRestrict_members(Object value) {
        if (value != null)
            this.restrict_Members = Integer.valueOf(value.toString());
    }

    /**
     * @return 返回 pre_money。
     */
    public float getPre_money() {
        return pre_Money;
    }

    /**
     * @param value 要设置的 pre_money。
     */
    public void setPre_money(Object value) {

        if (value != null)
            this.pre_Money = Float.valueOf(value.toString());
    }

    /**
     * @return 返回 erules。
     */
    public String getErules() {
        return eRules;
    }

    /**
     * @param value 要设置的 erules。
     */
    public void setErules(Object value) {
        if (value != null)
            this.eRules = value.toString();
    }

    public String getEimage()
    {
        return eImage;
    }

    public void setEimage(Object value)
    {
        if (value!=null)
            this.eImage=value.toString();
    }
    @Override
    public String gettablename() {
        return this.getClass().getCanonicalName();
    }

    @Override
    public Event getnewinstance(SoapObject soapObject) {
        return soapObject == null ? new Event() : new Event(soapObject);
    }

    @Override
    public Object getProperty(int i) {
        switch (i) {
            case 0:
                return Id;
            case 1:
                return bTime;
            case 2:
                return eTime;
            case 3:
                return ePlace;
            case 4:
                return restrict_Members;
            case 5:
                return pre_Money;
            case 6:
                return eRules;
            case 7:
                return eImage;
            default:
                return null;
        }
    }

    @Override
    public int getPropertyCount() {
        return 8;
    }

    @Override
    public void setProperty(int i, Object o) {
        if (o != null) {
            switch (i) {
                case 0:
                    Id = Integer.valueOf(o.toString());
                    break;
                case 1:
                    bTime = o.toString();
                    break;
                case 2:
                    eTime = o.toString();
                    break;
                case 3:
                    ePlace = o.toString();
                    break;
                case 4:
                    restrict_Members = Integer.valueOf(o.toString());
                    break;
                case 5:
                    pre_Money = Float.valueOf(o.toString());
                    break;
                case 6:
                    eRules = o.toString();
                    break;
                case 7:
                    eImage=o.toString();
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
                propertyInfo.name = "Id";
                break;
            case 1:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "bTime";
                break;
            case 2:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "eTime";
                break;
            case 3:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "ePlace";
                break;
            case 4:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "restrict_Members";
                break;
            case 5:
                propertyInfo.type = Float.class;
                propertyInfo.name = "pre_Money";
                break;
            case 6:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "eRules";
                break;
            case 7:
                propertyInfo.type=PropertyInfo.STRING_CLASS;
                propertyInfo.name="eImage";
                break;
            default:
                break;
        }
    }
}