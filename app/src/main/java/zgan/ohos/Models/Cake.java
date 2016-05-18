package zgan.ohos.Models;

import java.io.Serializable;

/**
 * Created by Administrator on 16-4-5.
 */
public class Cake extends BaseGoods implements Serializable {

    public float getsize() {
        return size;
    }

    public void setsize(float size) {
        this.size = size;
    }

    private float size = 6;

    public String getmsg() {
        return msg;
    }

    public void setmsg(String msg) {
        this.msg = msg;
    }

    private String msg;

    @Override
    public Cake getnewinstance() {
        return new Cake();
    }
}
