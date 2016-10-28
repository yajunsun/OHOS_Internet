package zgan.ohos.Models;

/**
 * Created by yajunsun on 16/10/28.
 */
public class BigAdvertise extends BaseModel {


    private String image_url;
    private int width;
    private int height;
    private String type_id;

    public String getimage_url() {
        return image_url;
    }

    public void setimage_url(String image_url) {
        this.image_url = image_url;
    }

    public int getwidth() {
        return width;
    }

    public void setwidth(String width) {
        if (!width.isEmpty())
            this.width = Integer.valueOf(width);
    }

    public int getheight() {
        return height;
    }

    public void setheight(String height) {
        if (!height.isEmpty())
            this.height = Integer.valueOf(height);
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

    public String getview_title() {
        return view_title;
    }

    public void setview_title(String view_title) {
        this.view_title = view_title;
    }

    public int gettimer() {
        return timer;
    }

    public void settimer(String timer) {
        if (!timer.isEmpty())
            this.timer = Integer.valueOf(timer);
    }

    private String page_id;
    private String view_title;
    private int timer;

    @Override
    public BigAdvertise getnewinstance() {
        return new BigAdvertise();
    }
}
