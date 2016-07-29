package zgan.ohos.Dals;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import zgan.ohos.Models.NewUserComm;
import zgan.ohos.Models.UserComm;

/**
 * Created by Administrator on 2016/7/29 0029.
 */
public class NewUserCommDal {
    /**
     * （小区信息： 小区id,小区名称，小区IP，小区端口;...  以分号间隔各个小区）
     *
     * @param str
     * @return 实体列表
     */
    public List<NewUserComm> getCommListfromString(String str) {
        try {
            String[] comms = str.split(";");
            if (comms.length > 0) {
                List<NewUserComm> list = new ArrayList<>();
                for (String comm : comms) {
                    NewUserComm model = new NewUserComm();
                    String[] items = comm.split(",");
                    model.setCommId(items[0]);
                    model.setCommName(items[1]);
                    model.setCommIp(items[2]);
                    model.setCommPort(items[3]);
                    list.add(model);
                }
                return list;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * （楼栋信息： （数据格式，id，名称; id2，名称））
     *
     * @param str
     * @return 实体列表
     */
    public List<NewUserComm>getCommDetailListfromString(String str)
    {
        try {
            String[] comms = str.split(";");
            if (comms.length > 0) {
                List<NewUserComm> list = new ArrayList<>();
                for (String comm : comms) {
                    NewUserComm model = new NewUserComm();
                    String[] items = comm.split(",");
                    model.setCommId(items[0]);
                    model.setCommName(items[1]);
                    list.add(model);
                }
                return list;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
