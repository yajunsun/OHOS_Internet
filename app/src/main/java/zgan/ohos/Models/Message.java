package zgan.ohos.Models;

import java.io.Serializable;

/**
 * Created by yajunsun on 2015/12/28.
 */
public class Message extends BaseModel implements Serializable {

    /****
     * <msg_id>消息id</msg_id>
     * <title>标题</title>
     * <firsttime>信息发布时间</firsttime>
     * <updatetime>信息更新时间</updatetime>
     * <offtime>消息下架时间</offtime>
     * <author>发布人</author>
     * <msgtype>消息类型</msgtype>
     ****/
    //消息ID
    private int MsgId;
    //标题
    private String MsgTitle;
    //发布时间
    private String MsgAddTime;
    //更新时间
    private String MsgPublishTime;
    //下架时间
    private String MsgOffTime;
    //内容
    private String MsgContent;
    //类型
    private String MsgType;

    public Message() {
    }

    public String getMsgAddTime() {
        return MsgAddTime;
    }

    public void setfirsttime(Object value) {
        if (value != null)
            MsgAddTime = value.toString();
    }

    public String getMsgContent() {
        return MsgContent;
    }

    public void setcontent(Object value) {
        if (value != null)
            MsgContent = value.toString();
    }

    public int getMsgId() {
        return MsgId;
    }

    public void setmsg_id(Object value) {
        if (value != null)
            MsgId = Integer.valueOf(value.toString());
    }

    public String getMsgOffTime() {
        return MsgOffTime;
    }

    public void setofftime(Object value) {
        if (value != null)
            MsgOffTime = value.toString();
    }

    public String getMsgPublishTime() {
        return MsgPublishTime;
    }

    public void setupdatetime(Object value) {
        if (value != null)
            MsgPublishTime = value.toString();
    }

    public String getMsgTitle() {
        return MsgTitle;
    }

    public void settitle(Object value) {
        if (value != null)
            MsgTitle = value.toString();
    }

    public String getMsgType() {
        return MsgType;
    }

    public void setmsgtype(Object value) {
        if (value != null)
            MsgType = value.toString();
    }

    //    @Override
//    public String gettablename() {
//        return getClass().getCanonicalName();
//    }
//
    @Override
    public Message getnewinstance() {
        return new Message();
    }
}
