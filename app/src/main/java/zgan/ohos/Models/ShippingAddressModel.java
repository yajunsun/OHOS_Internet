package zgan.ohos.Models;

import java.io.Serializable;

/**
 * Created by yajunsun on 2016/12/27.
 */
public class ShippingAddressModel extends BaseModel implements Serializable {
    private String method;
    private String address_id;
    private String UserName;
    private String UserPhone;
    private String UserAdress;
    private int IsUse;

    public String getmethod() {
        return method;
    }

    public void setmethod(String value) {
        if (value != null && !value.isEmpty()) method = value;
    }

    public String getaddress_id() {
        return address_id;
    }

    public void setaddress_id(String value) {
        if (value != null && !value.isEmpty()) address_id = value;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String value) {
        if (value != null && !value.isEmpty()) UserName = value;
    }

    public String getUserPhone() {
        return UserPhone;
    }

    public void setUserPhone(String value) {
        if (value != null && !value.isEmpty()) UserPhone = value;
    }

    public String getUserAdress() {
        return UserAdress;
    }

    public void setUserAdress(String value) {
        if (value != null && !value.isEmpty()) UserAdress = value;
    }

    public int getIsUse() {
        return IsUse;
    }

    public void setIsUse(int value) {
        IsUse = value;
    }

    @Override
    public ShippingAddressModel getnewinstance() {
        return new ShippingAddressModel();
    }
}