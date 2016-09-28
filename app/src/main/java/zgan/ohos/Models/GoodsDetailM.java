package zgan.ohos.Models;

import java.io.Serializable;

/**
 * Created by yajunsun on 2016/9/28.
 *     详情展示内容
 */
public class GoodsDetailM implements Serializable {
    //商品详情类型 1网页 2图片
    private int detailtype;
    //网页url
    private String detail_url;
    //图片url
    private String detail_pic_url;

    public int getdetailtype() {
        return detailtype;
    }

    public void setdetailtype(int detailtype) {
        this.detailtype = detailtype;
    }

    public String getdetail_url() {
        return detail_url;
    }

    public void setdetail_url(String detail_url) {
        this.detail_url = detail_url;
    }

    public String getdetail_pic_url() {
        return detail_pic_url;
    }

    public void setdetail_pic_url(String detail_pic_url) {
        this.detail_pic_url = detail_pic_url;
    }
}