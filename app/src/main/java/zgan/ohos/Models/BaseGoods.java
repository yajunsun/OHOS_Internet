package zgan.ohos.Models;

import java.io.Serializable;

/**
 * Created by Administrator on 16-4-8.
 */
public abstract class BaseGoods extends BaseModel implements Serializable {

    public String getspecs() {
        return specs;
    }

    public void setspecs(Object value) {
        if (value != null) this.specs = value.toString();
    }

    public String gettitle() {
        return title;
    }

    public void settitle(Object value) {
        if (value != null)
            this.title = value.toString();
    }

    public String getpic_url() {
        return pic_url;
    }

    public void setpic_url(Object value) {
        if (value != null) this.pic_url = value.toString();
    }

    public String getproduct_id() {
        return product_id;
    }

    public void setproduct_id(Object value) {
        if (value != null)
            this.product_id = value.toString();
    }

    public String gettime() {
        return time;
    }

    public void settime(Object value) {
        if (value != null)this.time = value.toString();
    }

    public int getstock() {
        return stock;
    }

    public void setstock(Object value) {
        if (value != null)
            this.stock = Integer.parseInt(value.toString());
    }

    public double getprice() {
        return price;
    }

    public void setprice(Object value) {
        if (value != null)this.price = Double.parseDouble(value.toString());
    }

    public int getSelectedcount() {
        return selectedcount;
    }

    public void setSelectedcount(int selectedcount) {
        this.selectedcount = selectedcount;
    }

    public String getdesc() {
        return desc;
    }

    public void setdesc(Object value) {
        if (value != null)this.desc = value.toString();
    }

    /***
     * 商品名称
     */
    private String title;
    /***
     * 图片uri
     */
    private String pic_url;
    /***
     * 商品编号
     */
    private String product_id;
    /***
     * 配送时间
     */
    private String time;
    /***
     * 库存
     */
    private int stock;
    /***
     * 规格
     */
    private String specs;
    /***
     * 商品描述
     */
    private String desc;
    /***
     * 商品价格
     */
    private double price;
    /***
     * 购买量
     */
    private int selectedcount = 1;
}
