package zgan.ohos.Models;

import java.io.Serializable;

/**
 * Created by Administrator on 16-4-7.
 */
public class ServeTraceM  implements Serializable {
    private String detail;

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getOptime() {
        return optime;
    }

    public void setOptime(String optime) {
        this.optime = optime;
    }

    private String optime;
    private int sequence;
}
