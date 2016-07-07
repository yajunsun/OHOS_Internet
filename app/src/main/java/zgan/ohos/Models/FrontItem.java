package zgan.ohos.Models;

import java.io.Serializable;

/**
 * Created by Administrator on 16-4-21.
 */
public class FrontItem extends BaseModel implements Serializable {
    private String image_url;
    private int width = 0;

    public String getview_title() {
        return view_title;
    }

    public void setview_title(String view_title) {
        this.view_title = view_title;
    }

    public String getimage_url() {
        return image_url;
    }

    public void setimage_url(String image_url) {
        this.image_url = image_url;
    }

    public int getwidth() {
        return width;
    }

    public void setwidth(int width) {
        this.width = width;
    }

    public int getheight() {
        return height;
    }

    public void setheight(int height) {
        this.height = height;
    }

    public String gettype_id() {
        return type_id;
    }

    public void settype_id(String type_id) {
        this.type_id = type_id;
    }

    public String getpage_id() {
        return page_id;
    }

    public void setpage_id(String page_id) {
        this.page_id = page_id;
    }

    private int height = 0;
    private String type_id;
    private String page_id;
    private String view_title;


    @Override
    public FrontItem getnewinstance() {
        return new FrontItem();
    }
}
