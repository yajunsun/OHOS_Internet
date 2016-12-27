package zgan.ohos.Models;

import java.io.Serializable;

/**
 * Created by yajunsun on 2016/12/27.
 */
public class ProvinceModel extends BaseModel implements Serializable
{
    private int num=0;
    private String name;
    private String data;
    public int getnum(){return num;}
    public void setnum(int value){num=value;}
    public String getname(){return name;}
    public void setname(String value){if (value!=null&&!value.isEmpty()) name=value;}
    public String getdata(){return data;}
    public void setdata(String value){if (value!=null&&!value.isEmpty()) data=value;}

    @Override
    public ProvinceModel getnewinstance() {
        return new ProvinceModel();
    }
}