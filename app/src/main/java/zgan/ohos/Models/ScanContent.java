package zgan.ohos.Models;

import java.io.Serializable;

/**
 * Created by yajunsun on 2016/11/27.
 */
public class ScanContent extends BaseModel implements Serializable{
    private String scan_pageID;
    private ScanBody version;
    public String getscan_pageID(){return scan_pageID;}
    public void setscan_pageID(String _scan_pageID){ scan_pageID=_scan_pageID;}
    public ScanBody getversion(){return version;}
    public void setversion(ScanBody _body){version=_body;}

    @Override
    public ScanContent getnewinstance() {
        return new ScanContent();
    }
}
