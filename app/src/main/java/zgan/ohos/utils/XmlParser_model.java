package zgan.ohos.utils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.lang.reflect.Method;

import zgan.ohos.Models.BaseModel;

/**
 * Created by Administrator on 16-3-4.
 */
public class XmlParser_model<T extends BaseModel> extends DefaultHandler {

    //Model类的实例
    public T modelInstance;
    //类中的方法集合
    Method[] methods;
    //此时解析类中存在的方法
    Method method;
    String parentTag="li";

    public XmlParser_model(T _instance,String _parentTag)
    {
        this(_instance);
        parentTag=_parentTag;
    }
    public XmlParser_model(T _instance) {
        modelInstance = _instance;
        methods = modelInstance.getClass().getDeclaredMethods();
    }

    @Override
    public void startDocument() throws SAXException {
        // TODO Auto-generated method stub
    }

    @Override
    public void endDocument() throws SAXException {
        // TODO Auto-generated method stub

    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        // TODO Auto-generated method stub
        if (!(localName.equals("root") || localName.equals(parentTag)))
            try {
                String tempMethod = "set" + localName;
                for (Method m : methods) {
                    if (m.getName().equals(tempMethod))
                        method = m;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        // TODO Auto-generated method stub
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        // TODO Auto-generated method stub
        String theString = new String(ch, start, length);
        try {
            method.invoke(modelInstance, theString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
