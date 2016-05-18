package zgan.ohos.Dals;

import java.util.ArrayList;
import java.util.List;

import zgan.ohos.Models.ServeTraceM;

/**
 * Created by Administrator on 16-4-7.
 */
public class ServeTraceDal {
    public List<ServeTraceM> getList()
    {
        List<ServeTraceM> list=new ArrayList<>();
        ServeTraceM m1=new ServeTraceM();
        m1.setDetail("【配送完成】 衣服已签收，感谢您使用一家一店！");
        m1.setOptime("2016-4-10 8:20:32");
        m1.setSequence(1);
        list.add(m1);

        ServeTraceM m2=new ServeTraceM();
        m2.setDetail("【上门送衣】 配送人员正在送货当中");
        m2.setOptime("2016-4-9 18:20:32");
        m2.setSequence(2);
        list.add(m2);

        ServeTraceM m3=new ServeTraceM();
        m3.setDetail("【抵达一家一店】 正在通知配送人员");
        m3.setOptime("2016-4-9 15:20:32");
        m3.setSequence(3);
        list.add(m3);

        ServeTraceM m4=new ServeTraceM();
        m4.setDetail("【清洗中】");
        m4.setOptime("2016-4-9 10:20:32");
        m4.setSequence(4);
        list.add(m4);

        ServeTraceM m5=new ServeTraceM();
        m5.setDetail("【上门收衣】 配送人员 李四");
        m5.setOptime("2016-4-9 8:20:32");
        m5.setSequence(5);
        list.add(m5);
        return list;
    }
}
