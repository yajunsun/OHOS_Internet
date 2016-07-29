package zgan.ohos.Models;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by Administrator on 2016/7/29 0029.
 */
public class NewUserComm extends BaseModel implements Serializable {
    @Override
    public NewUserComm getnewinstance() {
        return new NewUserComm();
    }

    public String getCommId() {
        return commId;
    }

    public void setCommId(String commId) {
        this.commId = commId;
    }

    public String getCommName() {
        return commName;
    }

    public void setCommName(String commName) {
        this.commName = commName;
    }

    public String getCommIp() {
        return commIp;
    }

    public void setCommIp(String commIp) {
        this.commIp = commIp;
    }

    public int getCommPort() {
        return commPort;
    }

    public void setCommPort(Object value) {
        if (value != null)
            this.commPort = Integer.valueOf(value.toString());
    }


    private String commId;
    private String commName;
    private String commIp;
    private int commPort;
}
