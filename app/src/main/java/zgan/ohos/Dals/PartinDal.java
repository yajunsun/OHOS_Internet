package zgan.ohos.Dals;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;

import java.util.List;

import zgan.ohos.Models.Partin;

/**
 * Created by yajunsun on 2015/11/25.
 */
public class PartinDal extends baseDal<Partin> {
    public String PartIn(int eventId, String PhoneNum, String HomeAdress) throws Exception {
        String SOAP_ACTION = "http://download1.zgantech.com/IEventsContract/PartIn";
        String MethodName = "PartIn";
        SoapObject request = new SoapObject(NameSpace, MethodName);
        request.addProperty("eventId", eventId);
        request.addProperty("PhoneNum",PhoneNum);
        request.addProperty("HomeAdress", HomeAdress);
        return GetExecuteStatus(request,URL,SOAP_ACTION);
    }

    public String SavePartIn(Partin p) throws Exception {
        String SOAP_ACTION = "http://service.zgantech.com/IEventsContract/SavePartIn";
        String MethodName = "SavePartIn";
        SoapObject request = new SoapObject(NameSpace, MethodName);
        request.addProperty("p", p);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);

        envelope.addMapping(Partin.NAMESPACE, "Partin", Partin.class);

        return GetExecuteStatus(request, URL, SOAP_ACTION, envelope);
    }

    public int PartedinMembers(int eventId) throws Exception {
        String SOAP_ACTION = "http://service.zgantech.com/IEventsContract/PartedinMembers";
        String MethodName = "PartedinMembers";
        SoapObject request = new SoapObject(NameSpace, MethodName);
        request.addProperty("eventId", eventId);
        return Integer.parseInt(GetExecuteStatus(request,URL,SOAP_ACTION));
    }

    public List<Partin> getPartinMembers(int eventId) throws Exception {
        String SOAP_ACTION = "http://service.zgantech.com/IEventsContract/getPartinMembers";
        String MethodName = "getPartinMembers";
        SoapObject request = new SoapObject(NameSpace, MethodName);
        request.addProperty("eventId", eventId);
        return getnetobjectlist(new Partin(), request,URL,SOAP_ACTION);
    }
}