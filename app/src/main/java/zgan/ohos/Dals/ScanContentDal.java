package zgan.ohos.Dals;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import zgan.ohos.Models.ScanBody;
import zgan.ohos.Models.ScanContent;
import zgan.ohos.Models.ScanDetail;
import zgan.ohos.Models.ScanTitle;

/**
 * Created by yajunsun on 2016/11/27.
 */
public class ScanContentDal extends ZGbaseDal {
    public ScanContent getItem(String jstr) {
        ScanContent content = new ScanContent();
        try {
            JSONObject obj = new JSONObject(jstr);
            String scan_pageID = getNullableString(obj, "scan_pageID", "");
            JSONArray jsversion = getNullableArr(obj, "version");
            int verLen = jsversion.length();
            content.setscan_pageID(scan_pageID);
            if (verLen > 0) {
                ScanBody body = new ScanBody();
                JSONObject jstitle = (JSONObject) jsversion.opt(0);
                String version_id = getNullableString(jstitle, "version_id", "");
                String ID = getNullableString(jstitle, "ID", "");
                ScanTitle title = new ScanTitle();
                title.setversion_id(version_id);
                title.setID(ID);
                body.settitle(title);
                if (verLen == 2) {
                    JSONObject jsdetail = (JSONObject) jsversion.opt(1);
                    ScanDetail detail = new ScanDetail();
                    String version_id1 = getNullableString(jsdetail, "version_id", "");
                    String page_id = getNullableString(jsdetail, "page_id", "");
                    String sub_category_id = getNullableString(jsdetail, "sub_category_id", "");
                    String category_id = getNullableString(jsdetail, "category_id", "");
                    detail.setversion_id(version_id1);
                    detail.setpage_id(page_id);
                    detail.setsub_category_id(sub_category_id);
                    detail.setcategory_id(category_id);
                    body.setdetail(detail);
                }
                content.setversion(body);
            }
        } catch (JSONException jse) {
            jse.printStackTrace();

        }
        return content;
    }
}
