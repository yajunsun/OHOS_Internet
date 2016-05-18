package zgan.ohos.Models;

import java.io.Serializable;

/**
 * Created by Administrator on 16-4-6.
 */
public class Vegetable extends BaseGoods implements Serializable {
    private String size;


    public String getitemSize() {
        return size;
    }

    public void setitemSize(String size) {
        this.size = size;
    }

    @Override
    public Vegetable getnewinstance() {
        return new Vegetable();
    }
}
