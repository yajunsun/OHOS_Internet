package zgan.ohos.Models;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/7/8 0008.
 */
public class FuncBase extends BaseModel implements Serializable {
    protected String type_id;
    protected String page_id;
    protected String view_title;

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

    @Override
    public <T> T getnewinstance() {
        return null;
    }
}
