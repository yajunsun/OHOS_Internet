package zgan.ohos.Dals;

import java.util.List;

import zgan.ohos.Models.LeaveMessage;
import zgan.ohos.Models.Message;

/**
 * Created by Administrator on 16-3-7.
 */
public class LeaveMessageDal extends ZGbaseDal<LeaveMessage>{
     public List<LeaveMessage>getLeaveMessages(String xmlString)
     {
         return getModelList(xmlString,new LeaveMessage());
     }
    public  LeaveMessage getSingleLeaveMessage(String xmlString) {
        return  GetSingleModel(xmlString, new LeaveMessage());
    }
}
