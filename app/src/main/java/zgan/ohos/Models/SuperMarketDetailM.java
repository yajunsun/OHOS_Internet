package zgan.ohos.Models;

import java.io.Serializable;
import java.util.List;

/**
 * Created by yajunsun on 2016/9/28.
 * 超市购商品详情
 */
public class SuperMarketDetailM extends SM_GoodsM implements Serializable {
    //倒计时
    private int countdown;
    //商品图片集合
    private List<String>pic_urls_list;
    //商品详情内容
    private GoodsDetailM goodsdetail;
    //评价图片
    private String comment_pic;

    public String getcomment_pic() {
        return comment_pic;
    }

    public void setcomment_pic(String comment_pic) {
        this.comment_pic = comment_pic;
    }

    public String getphone() {
        return phone;
    }

    public void setphone(String phone) {
        this.phone = phone;
    }

    //咨询电话
    private String phone;

    public int getcountdown() {
        return countdown;
    }

    public void setcountdown(int countdown) {
        this.countdown = countdown;
    }

    public List<String> getpic_urls_list() {
        return pic_urls_list;
    }

    public void setpic_urls_list(List<String> pic_urls_list) {
        this.pic_urls_list = pic_urls_list;
    }

    public GoodsDetailM getgoodsdetail() {
        return goodsdetail;
    }

    public void setgoodsdetail(GoodsDetailM goodsdetail) {
        this.goodsdetail = goodsdetail;
    }
    //详情展示内容

}