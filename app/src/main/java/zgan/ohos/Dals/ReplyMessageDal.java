package zgan.ohos.Dals;

import java.util.List;

import zgan.ohos.Models.ReplyMessage;

/**
 * Created by Administrator on 16-3-7.
 */
public class ReplyMessageDal extends ZGbaseDal<ReplyMessage> {
    public List<ReplyMessage> getReplyMessages(String xmlString)
    {
        return getModelListfromXML(xmlString,new ReplyMessage());
    }
    public  ReplyMessage getSingleReplyMessage(String xmlString) {
        return  GetSingleModelfromXML(xmlString, new ReplyMessage());
    }
}
