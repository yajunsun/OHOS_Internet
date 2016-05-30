package zgan.ohos.Models;

import java.io.Serializable;
import java.util.Objects;

import zgan.ohos.utils.SystemUtils;

/**
 * Created by Administrator on 16-4-8.
 */
public class MyPakage extends BaseGoods implements Serializable {

    public int getptotal() {
        return ptotal;
    }

    public void setptotal(Object value) {
        if (value!=null)
        this.ptotal = Integer.parseInt(value.toString());
    }

    public int getpused() {
        return pused;
    }

    public void setpused(Object value) {
        if (value!=null)this.pused = SystemUtils.getIntValue(value.toString());
    }

    public int getpleft() {
        return pleft;
    }

    public void setpleft(Object value) {
        if (value!=null)this.pleft = SystemUtils.getIntValue(value.toString());
    }

    public int getimgdesc() {
        return imgdesc;
    }

    public void setimgdesc(Object value) {
        if (value!=null)
        this.imgdesc = SystemUtils.getIntValue(value.toString());
    }

    private int imgdesc;
    private int ptotal;
    private int pused;
    private int pleft;

    public String getdetails_url() {
        return details_url;
    }

    public void setdetails_url(Object value) {
        if (value!=null)
            this.details_url = value.toString();
    }

    private String details_url;

    @Override
    public MyPakage getnewinstance() {
        return new MyPakage();
    }
}
