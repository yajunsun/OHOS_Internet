package zgan.ohos.Models;

import zgan.ohos.utils.SystemUtils;

/**
 * Created by Administrator on 16-3-7.
 */
public class ReplyMessage extends BaseModel {

    private int msgType;
    private String msgContent;
    public String msgDate;

    public int getMsgType()
    {
        return msgType;
    }
    public String getMsgContent()
    {
        return msgContent;
    }
    public String getMsgDate()
    {
        return msgDate;
    }
    public void setq_type(Object value)
    {
        if (value!=null)
        {
            msgType= SystemUtils.getIntValue(value.toString());
        }
    }
    public void setq_content(Object value)
    {
        if (value!=null)
        {
            msgContent=value.toString();
        }
    }
    public void setdate(Object value)
    {
        if (value!=null)
            msgDate=value.toString();
    }

    @Override
    public ReplyMessage getnewinstance() {
        return new ReplyMessage();
    }
}
