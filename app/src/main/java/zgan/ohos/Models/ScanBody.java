package zgan.ohos.Models;

import java.io.Serializable;

/**
 * Created by yajunsun on 2016/11/27.
 */
public class ScanBody extends BaseModel implements Serializable{
    private ScanTitle title;
    private ScanDetail detail;
    public ScanTitle gettitle(){return title;}
    public void settitle(ScanTitle _title){title=_title;}
    public ScanDetail getdetail(){return detail;}
    public void setdetail(ScanDetail _detail){detail=_detail;}

    @Override
    public ScanBody getnewinstance() {
        return new ScanBody();
    }
}
