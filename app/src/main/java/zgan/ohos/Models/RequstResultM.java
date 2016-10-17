package zgan.ohos.Models;

import java.io.Serializable;

/**
 * Created by yajunsun on 16/10/17.
 */
public class RequstResultM extends BaseModel implements Serializable {
    private String result;

    public String getresult() {
        return result;
    }

    public void setresult(String result) {
        this.result = result;
    }

    public String getdata() {
        return data;
    }

    public void setdata(String data) {
        this.data = data;
    }

    public String getmsg() {
        return msg;
    }

    public void setmsg(String msg) {
        this.msg = msg;
    }

    private String data;
    private String msg;
    @Override
    public RequstResultM getnewinstance() {
        return new RequstResultM();
    }
}
