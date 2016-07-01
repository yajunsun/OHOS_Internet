package zgan.ohos.Models;

import java.io.Serializable;

/**
 * Created by Administrator on 16-6-30.
 */
public class ExpressIn extends BaseModel implements Serializable{
    public String getkuaidi() {
        return kuaidi;
    }

    public void setkuaidi(String kuaidi) {
        this.kuaidi = kuaidi;
    }

    public String getorder_num() {
        return order_num;
    }

    public void setorder_num(String order_num) {
        this.order_num = order_num;
    }

    public String gettime() {
        return time;
    }

    public void settime(String time) {
        this.time = time;
    }

    private String kuaidi;
    private String order_num;
    private String time;

    @Override
    public ExpressIn getnewinstance() {
        return new ExpressIn();
    }
}
