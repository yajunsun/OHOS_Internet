package zgan.ohos.Models;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.util.Hashtable;


public abstract class BaseObject implements KvmSerializable {
    public static final String NAMESPACE = "http://schemas.datacontract.org/2004/07/sun.model";

    // public static final String NAMESPACE =
    // "http://schemas.datacontract.org/2004/07/myhealth.healthModel";
    // public static final String NAMESPACE =
    // "http://schemas.datacontract.org/2004/07/HL7.Base.Struct";
    public BaseObject() {
        super();
    }

    public abstract String gettablename();

    private String errorString;

    public String geterror() {
        return errorString;
    }

    public void seterror(Object _error) {
        if (_error != null)
            errorString = _error.toString();
    }


    public abstract <T> T getnewinstance(SoapObject soapObject);


    public static long getserialVersionUID(String modelname) {
        if (modelname.equals("MessageType"))
            return 6L;
        if (modelname.equals("Message"))
            return 7L;
        if (modelname.equals("Product"))
            return 8L;
        if (modelname.equals("Event"))
            return 9L;
        if (modelname.equals("Event_Product"))
            return 10L;
        if (modelname.equals("ProductType"))
            return 11L;

        return 0;
    }
}
