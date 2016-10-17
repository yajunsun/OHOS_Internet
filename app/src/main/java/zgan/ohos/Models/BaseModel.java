package zgan.ohos.Models;

import java.io.Serializable;

/**
 * Created by Administrator on 16-3-3.
 */
public abstract class BaseModel implements Serializable {
    public abstract <T> T getnewinstance();

}
