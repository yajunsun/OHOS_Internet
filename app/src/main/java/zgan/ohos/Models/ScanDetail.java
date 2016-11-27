package zgan.ohos.Models;

import java.io.Serializable;

/**
 * Created by yajunsun on 2016/11/27.
 */
public class ScanDetail extends BaseModel implements Serializable {

    private String version_id;
    private String page_id;
    private String sub_category_id;
    private String category_id;
    public String getversion_id()
    {
        return version_id;
    }
    public void setversion_id(String _version_id)
    {
        version_id=_version_id;
    }
    public String getpage_id(){
        return page_id;
    }
    public void setpage_id(String _page_id)
    {
        page_id=_page_id;
    }
    public String getsub_category_id()
    {
        return sub_category_id;
    }
    public void setsub_category_id(String _sub_category_id)
    {
        sub_category_id=_sub_category_id;
    }
    public String getcategory_id(){
        return category_id;
    }
    public void setcategory_id(String _category_id){
        category_id=_category_id;
    }
    public String getgetversion_id(){
        return version_id;
    }
    public void setgetversion_id(String _getversion_id){
        version_id=_getversion_id;
    }
    @Override
    public ScanDetail getnewinstance() {
        return new ScanDetail();
    }
}
