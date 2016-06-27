package zgan.ohos.Dals;

import java.util.List;

import zgan.ohos.Models.ReplyMessage;

/**
 * Created by Administrator on 16-3-7.
 */
public class ReplyMessageDal extends ZGbaseDal<ReplyMessage> {
    public List<ReplyMessage> getReplyMessages(String xmlString)
    {
        return getModelList(xmlString,new ReplyMessage());
    }
    public  ReplyMessage getSingleReplyMessage(String xmlString) {
        return  GetSingleModel(xmlString, new ReplyMessage());
    }
}
