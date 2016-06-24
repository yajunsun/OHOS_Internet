package zgan.ohos.Models;

import java.io.Serializable;

/**
 * Created by Administrator on 16-6-23.
 * 功能页面实体
 */
public class FuncPage extends BaseModel implements Serializable {

    public static final String funPage = "100102";
    private String icon_url;
    private String type_id;
    private String page_id;
    private String view_title;

    public String geticon_url() {
        return icon_url;
    }

    public void seticon_url(String icon_url) {
        this.icon_url = icon_url;
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


    @Override
    public FuncPage getnewinstance() {
        return new FuncPage();
    }
}
