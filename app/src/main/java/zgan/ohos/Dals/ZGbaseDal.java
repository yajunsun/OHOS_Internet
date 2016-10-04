package zgan.ohos.Dals;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import zgan.ohos.Models.BaseModel;
import zgan.ohos.utils.JsonParser;
import zgan.ohos.utils.NetUtils;
import zgan.ohos.utils.XmlParser;
import zgan.ohos.utils.XmlParser_model;

/**
 * Created by Administrator on 16-3-7.
 */
public class ZGbaseDal<T extends BaseModel> {
        /****************
     * sunyajun
     ****************/
    /***
     * 解析XML
     * @param xmlString XML字符串
     * @param Modelinstance XML对应的实体类
     * @param parentTag XML对象开始标记
     * @return
     */
    public List<T> getModelListfromXML(String xmlString,T Modelinstance,String parentTag)
    {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            XmlParser<T> handler = new XmlParser<>(Modelinstance,parentTag);
            reader.setContentHandler(handler);
            StringReader read = new StringReader(NetUtils.buildXMLfromNetData(xmlString));
            InputSource is = new InputSource(read);
            reader.parse(is);
            return handler.list;
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /***
     * 解析XML
     * @param xmlString XML字符串
     * @param Modelinstance XML对应的实体类
     * @return
     */
    public List<T> getModelListfromXML(String xmlString,T Modelinstance)
    {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            XmlParser<T> handler = new XmlParser<>(Modelinstance);
            reader.setContentHandler(handler);
            StringReader read = new StringReader(NetUtils.buildXMLfromNetData(xmlString));
            InputSource is = new InputSource(read);
            reader.parse(is);
            return handler.list;
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /***
     * 解析XML成单个实体对象
     * @param xmlString
     * @param modelInstance
     * @return
     */
    public T GetSingleModelfromXML(String xmlString,T modelInstance) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            XmlParser_model<T> handler = new XmlParser_model<>(modelInstance);
            reader.setContentHandler(handler);
            StringReader read = new StringReader(NetUtils.buildXMLfromNetData(xmlString));
            InputSource is = new InputSource(read);
            reader.parse(is);
            return handler.modelInstance;
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /***
     * 解析XML 成单个对象
     * @param xmlString
     * @param modelInstance
     * @param parentTag
     * @return
     */
    public T GetSingleModelfromXML(String xmlString,T modelInstance,String parentTag) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            XmlParser_model<T> handler = new XmlParser_model<>(modelInstance,parentTag);
            reader.setContentHandler(handler);
            StringReader read = new StringReader(NetUtils.buildXMLfromNetData(xmlString));
            InputSource is = new InputSource(read);
            reader.parse(is);
            return handler.modelInstance;
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    public List<T> getModelList(String jsonString, T modelInstance) {
        try {
            JsonParser<T> jsonParser = new JsonParser<>(modelInstance);
            jsonParser.setJosnString(jsonString);
            jsonParser.DeSerialize();
            return jsonParser.list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public T GetSingleModel(String xmlString, T modelInstance) {
        List<T> list = getModelList(xmlString, modelInstance);
        if (list != null && list.size() > 0)
            return list.get(0);
        return null;
    }
    public String getNullableString(JSONObject obj, String name, String nullValue)
    {
        String result=nullValue;
        try
        {
            result=obj.getString(name);
        }
        catch (JSONException jse)
        {
            jse.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }
    public int getNullableInt(JSONObject obj, String name, int nullValue)
    {
        int result=nullValue;
        try
        {
            result=obj.getInt(name);
        }
        catch (JSONException jse)
        {
            jse.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }
    public JSONArray getNullableArr(JSONObject obj, String name)
    {
        JSONArray jsonArray=new JSONArray();
        try
        {
            jsonArray=obj.getJSONArray(name);
        }
        catch (JSONException jse)
        {
            jse.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return jsonArray;
    }
    public JSONObject getNullableObj(JSONObject obj, String name)
    {
        JSONObject jobj=new JSONObject();
        try
        {
            jobj=obj.getJSONObject(name);
        }
        catch (JSONException jse)
        {
            jse.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return jobj;
    }
}
