package zgan.ohos.Models;

import com.tencent.mm.sdk.modelbase.BaseReq;

import java.io.Serializable;

/**
 * Created by Administrator on 16-5-2.
 */
public class WXReq extends BaseReq implements Serializable {
    public WXReq() {
    }

    public WXReq(BaseReq req) {
        transaction = req.transaction;
        openId = req.openId;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public boolean checkArgs() {
        return false;
    }
}
