package zgan.ohos.Models;

import java.io.Serializable;

/**
 * Created by Administrator on 16-4-21.
 */
public class Advertise extends BaseModel implements Serializable {

    public int getad_type() {
        return ad_type;
    }

    public void setad_type(Object value) {
        if (value != null)
            this.ad_type = Integer.parseInt(value.toString());
    }

    public String getad_url() {
        return ad_url;
    }

    public void setad_url(Object value) {
        if (value != null)
            this.ad_url = value.toString();
    }

    public String getpic_url() {
        return pic_url;
    }

    public void setpic_url(Object value) {
        if (value != null)
            this.pic_url = value.toString();
    }

    public String gettitle() {
        return title;
    }

    public void settitle(Object value) {
        if (value != null)
            this.title = value.toString();
    }

    public String getad_content() {
        return ad_content;
    }

    public void setad_content(Object value) {
        if (value != null)
        this.ad_content = value.toString();
    }

    public String getweb_url() {
        return web_url;
    }

    public void setweb_url(Object value) {
        if (value != null)this.web_url = value.toString();
    }

    public String getview_title() {
        return view_title;
    }

    public void setview_title(Object value) {
        if (value != null)this.view_title = value.toString();
    }

    /***
     * 广告类型	文字：0，图片：1，web：6
     */
    private int ad_type;

    /***
     * 首页广告图片地址	任何类型都必须具有
     */
    private String ad_url;
    /***
     * 广告图片内容	类型1有内容，其他为空
     */
    private String pic_url;
    /***
     * 广告标题	类型0有内容，其他为空
     */
    private String title;
    /***
     * 广告内容	类型0有内容，其他为空
     */
    private String ad_content;
    /***
     * 广告web地址	类型6有内容，其他为空
     */
    private String web_url;
    /***
     * 视图标题	显示在广告页面的状态栏
     */
    private String view_title;

    @Override
    public Advertise getnewinstance() {
        return new Advertise();
    }
}
