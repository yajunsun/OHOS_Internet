package zgan.ohos.Dals;

import org.ksoap2.serialization.SoapObject;

import java.util.List;

import zgan.ohos.Models.UserComm;

/**
 * Created by yajunsun on 2015/12/10.
 */
public class UserCommDal extends baseDal<UserComm> {
    public List<UserComm> GetComm(int FCommId) throws Exception {
//        String SOAP_ACTION = "http://tempuri.org/IEventsContract/GetComm";
//        String MethodName = "GetComm";
//        SoapObject request = new SoapObject(NameSpace, MethodName);
//        request.addProperty("FCommId", FCommId);
//        return getnetobjectlist(new UserComm(), request, URL, SOAP_ACTION);
        return null;
    }

    public String GetHostNameAndPhone(int CommId) throws Exception {
//        String SOAP_ACTION = "http://tempuri.org/IEventsContract/GetHostNameAndPhone";
//        String MethodName = "GetHostNameAndPhone";
//        SoapObject request = new SoapObject(NameSpace, MethodName);
//        request.addProperty("CommId", CommId);
//        return GetExecuteStatus(request, URL, SOAP_ACTION);
        return "failure";
    }

    public String GetUnitId(int commId) throws Exception {
//        String SOAP_ACTION = "http://tempuri.org/IEventsContract/GetUnitId";
//        String MethodName = "GetUnitId";
//        SoapObject request = new SoapObject(NameSpace, MethodName);
//        request.addProperty("commId", commId);
//        return GetExecuteStatus(request, URL, SOAP_ACTION);
        return "failure";
    }
}
