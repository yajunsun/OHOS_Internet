package zgan.ohos.Models;

import com.tencent.mm.sdk.modelbase.BaseResp;

import java.io.Serializable;

/**
 * Created by Administrator on 16-5-2.
 */
public class WXResp implements Serializable {
    public WXResp() {
    }

    private int errcode;

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public String getErrstr() {
        return errstr;
    }

    public void setErrstr(String errstr) {
        this.errstr = errstr;
    }

    public String get_transaction() {
        return _transaction;
    }

    public void set_transaction(String _transaction) {
        this._transaction = _transaction;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    private String errstr;
    public String _transaction;
    public String openid;

    public WXResp(BaseResp resp) {
        setErrcode(resp.errCode);
        setErrstr(resp.errStr);
        set_transaction(resp.transaction);
        setOpenid(resp.openId);
    }
}
