package zgan.ohos.Models;

import java.io.Serializable;

/**
 * Created by yajunsun on 2016/11/27.
 */
public class ScanTitle extends BaseModel implements Serializable {
    private String version_id;
    private String ID;
    public String getversion_id(){
        return version_id;
    }
    public void setversion_id(String _version_id)
    {
        version_id=_version_id;
    }
    public String getID(){return ID;}
    public void setID(String _ID){
        ID=_ID;
    }

    @Override
    public ScanTitle getnewinstance() {
        return new ScanTitle();
    }
}
