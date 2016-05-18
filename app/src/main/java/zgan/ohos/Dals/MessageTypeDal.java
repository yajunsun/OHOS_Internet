package zgan.ohos.Dals;

import org.ksoap2.serialization.SoapObject;

import java.util.List;

import zgan.ohos.Models.MessageType;

/**
 * Created by yajunsun on 2015/12/28.
 */
public class MessageTypeDal extends baseDal<MessageType> {

    public List<MessageType> GetMessagetTypes()throws  Exception
    {
//        String SOAP_ACTION = "http://tempuri.org/IEventsContract/GetMessagetTypes";
//        String MethodName = "GetMessagetTypes";
//        SoapObject request = new SoapObject(NameSpace, MethodName);
//        return getnetobjectlist(new MessageType(), request, URL, SOAP_ACTION);
        return null;
    }
}
