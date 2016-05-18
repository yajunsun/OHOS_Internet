package zgan.ohos.Models;

/**
 * Created by Administrator on 16-4-21.
 */
public class FrontItem extends BaseModel{
    private String pic_url;
    private String view_id;
    private int order;
    public String getpic_url() {
        return pic_url;
    }

    public void setpic_url(Object value) {
        if (value != null)this.pic_url = value.toString();
    }

    public String getview_id() {
        return view_id;
    }

    public void setview_id(Object value) {
        if (value != null)this.view_id = value.toString();
    }

    public int getorder() {
        return order;
    }

    public void setorder(Object value) {
        if (value != null)this.order = Integer.parseInt(value.toString());
    }


    @Override
    public FrontItem getnewinstance() {
        return new FrontItem();
    }
}
