package zgan.ohos.Models;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Objects;

import zgan.ohos.utils.SystemUtils;

/**
 * Created by yajunsun on 2015/12/28.
 */
public class MessageType extends BaseObject  implements Serializable {

    private static final long serialVersionUID = getserialVersionUID("MessageType");

    public MessageType() {
    }

    public MessageType(SoapObject soapObject) {
        if (soapObject != null) {
            setMsgTypeId(soapObject.getProperty("MsgTypeId"));
            setMsgTypeName(soapObject.getProperty("MsgTypeName"));
            setMsgTypePId(soapObject.getProperty("MsgTypePId"));
        }
    }

    public int getMsgTypeId() {
        return MsgTypeId;
    }

    public void setMsgTypeId(Object value) {
        if (value != null)
            MsgTypeId = SystemUtils.getIntValue(value.toString());
    }

    public String getMsgTypeName() {
        return MsgTypeName;
    }

    public void setMsgTypeName(Object value) {
        if (value != null) {
            if (value.toString().length() > 50)
                throw new IndexOutOfBoundsException("消息类型必须在50字内");
            MsgTypeName = value.toString();
        }
    }

    public int getMsgTypePId() {
        return MsgTypePId;
    }

    public void setMsgTypePId(Object value) {
        if (value != null)
            MsgTypePId = SystemUtils.getIntValue(value.toString());
    }

    private int MsgTypeId;

    private String MsgTypeName;
    //    {
//        get { return _msgTypeName; }
//        set
//        {
//            if (value != null && value.Length > 50)
//                throw new ArgumentOutOfRangeException("Invalid value for MsgTypeName", value, value.ToString());
//            _msgTypeName = value;
//        }
//    }
    private int MsgTypePId;


    @Override
    public String gettablename() {
        return this.getClass().getCanonicalName();
    }

    @Override
    public MessageType getnewinstance(SoapObject soapObject) {
        return soapObject == null ? new MessageType() : new MessageType(soapObject);
    }

    @Override
    public Object getProperty(int i) {
        switch (i) {
            case 0:
                return MsgTypeId;
            case 1:
                return MsgTypeName;
            case 2:
                return MsgTypePId;
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
        switch (i) {
            case 0:
                setMsgTypeId(o);
                break;
            case 1:
                setMsgTypeName(o);
                break;
            case 2:
                setMsgTypePId(o);
                break;
        }
    }

    @Override
    public void getPropertyInfo(int i, Hashtable hashtable, PropertyInfo propertyInfo) {
        propertyInfo.namespace = super.NAMESPACE;
        switch (i) {
            case 0:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "MsgTypeId";
                break;
            case 1:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "MsgTypeName";
                break;
            case 2:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "MsgTypePId";
                break;
        }
    }
}
