package zgan.ohos.Dals;

import java.util.List;

import zgan.ohos.Models.Message;

/**
 * Created by yajunsun on 2015/12/28.
 */
public class MessageDal extends ZGbaseDal<Message>{

    public List<Message> GetMessages(String xmlString) {
        return getModelList(xmlString,new Message());
    }

    public Message GetMessage(String xmlString) {
        return  GetSingleModel(xmlString,new Message());
    }
}
