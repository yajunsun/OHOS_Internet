package zgan.ohos.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import zgan.ohos.Models.BaseModel;

/**
 * Created by Administrator on 16-4-21.
 */
public class JsonParser<T extends BaseModel> {
    //单个实例
    T model;
    //xml解析后的实例集合
    public List<T> list;
    //类的临时实例
    T modelInstance;
    //类中的方法集合
    Method[] methods;
    private String jsonstr;

    public JsonParser(T _instance) {
        modelInstance = _instance;
        Method[]superMs=null;
        Method[]myMs;
        if (modelInstance.getClass().getSuperclass().toString().equals("class zgan.ohos.Models.BaseGoods"))
        {
            superMs=modelInstance.getClass().getSuperclass().getDeclaredMethods();
        }
        myMs = modelInstance.getClass().getDeclaredMethods();
        if (superMs!=null) {
            methods = new Method[superMs.length + myMs.length];
            for(int i=0;i<superMs.length;i++)
            {
                methods[i]=superMs[i];
            }
            for(int i=0;i<myMs.length;i++)
            {
                methods[superMs.length+i]=myMs[i];
            }

        }
        else
        {
            methods=myMs;
        }
    }

    public void setJosnString(String _jsonstr) {
        jsonstr = _jsonstr;
    }

    public boolean DeSerialize() {
        if (jsonstr.length() > 0) {
            list = new ArrayList<>();
            try {
                JSONArray jsonArray = new JSONObject(jsonstr)
                        .getJSONArray("data");
//                Log.i("suntest", jsonstr);
                for (int i = 0; i < jsonArray.length(); i++) {
                    model = modelInstance.getnewinstance();
                    JSONObject obj = (JSONObject) jsonArray.opt(i);
                    for (Method m : methods) {
                        String mName = m.getName();
                        if (mName.startsWith("set")) {
                            Type[] types = m.getParameterTypes();
                            if (types.length > 0) {
                                try {
                                    String value = obj.get(mName.substring(3)).toString().trim();
                                    m.invoke(model, value);
                                }
                                catch (JSONException jse)
                                {
                                    continue;
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                    continue;
                                }
                            }
                        }
                    }
                    list.add(model);
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
}
