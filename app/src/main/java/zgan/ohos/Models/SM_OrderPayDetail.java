package zgan.ohos.Models;

import java.io.Serializable;

/**
 * Created by yajunsun on 2016/10/15.
 */
public class SM_OrderPayDetail extends BaseModel implements Serializable {
    private String pay_account;//支付帐号(商户号)
    private String pay_key;//支付密钥
    private String pay_user;//用户id(应用ID)
    private String business_notice_url;// 业务通知地址

    public String getpay_account() {
        return pay_account;
    }

    public void setpay_account(String pay_account) {
        this.pay_account = pay_account;
    }

    public String getPay_key() {
        return pay_key;
    }

    public void setpay_key(String pay_key) {
        this.pay_key = pay_key;
    }

    public String getpay_user() {
        return pay_user;
    }

    public void setPay_user(String pay_user) {
        this.pay_user = pay_user;
    }

    public String getpusiness_notice_url() {
        return business_notice_url;
    }

    public void setbusiness_notice_url(String business_notice_url) {
        this.business_notice_url = business_notice_url;
    }

    @Override
    public SM_OrderPayDetail getnewinstance() {
        return new SM_OrderPayDetail();
    }
}
