package zgan.ohos.Models;

import java.io.Serializable;

import zgan.ohos.utils.SystemUtils;

/**
 * Created by Administrator on 16-3-7.
 */
public class LeaveMessage extends BaseModel implements Serializable {

    private int Id;
    private String Content;
    private String Date;

    public int getId() {
        return Id;
    }

    public String getContent() {
        return Content;
    }

    public String getDate() {
        return Date;
    }

    public void setq_id(Object value) {
        if (value != null) {
            Id = SystemUtils.getIntValue(value.toString());
        }
    }

    public void setq_content(Object value) {
        if (value != null) {
            Content = value.toString();
        }
    }

    public void setdate(Object value) {
        if (value != null) {
            Date = value.toString();
        }
    }
    @Override
    public LeaveMessage getnewinstance() {
        return new LeaveMessage();
    }
}
