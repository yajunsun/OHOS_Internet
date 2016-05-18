package zgan.ohos.Dals;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;

import java.util.List;

import zgan.ohos.Models.HouseHoldingMsg;

/**
 * Created by yajunsun on 2016/1/13.
 */
public class HouseHoldingMsgDal extends baseDal<HouseHoldingMsg> {
    public String SendHouseHoldingMsg(HouseHoldingMsg msg) throws Exception {
//        String SOAP_ACTION = "http://tempuri.org/IEventsContract/SendHouseHoldingMsg";
//        String MethodName = "SendHouseHoldingMsg";
//        SoapObject request = new SoapObject(NameSpace, MethodName);
//        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
//                SoapEnvelope.VER11);
//        envelope.addMapping(HouseHoldingMsg.NAMESPACE, "HouseHoldingMsg", HouseHoldingMsg.class);
//        request.addProperty("msg", msg);
//        return GetExecuteStatus(request, URL, SOAP_ACTION, envelope);
        return "failure";
    }

    public List<HouseHoldingMsg> GetHouseHoldingMsgs(String userId, int pagesize, int pageindex) throws Exception {
//        String SOAP_ACTION = "http://tempuri.org/IEventsContract/GetHouseHoldingMsgs";
//        String MethodName = "GetHouseHoldingMsgs";
//        SoapObject request = new SoapObject(NameSpace, MethodName);
//        request.addProperty("userId", userId);
//        request.addProperty("pagesize", pagesize);
//        request.addProperty("pageindex", pageindex);
//        return getnetobjectlist(new HouseHoldingMsg(), request, URL, SOAP_ACTION);
        return null;
    }
}
