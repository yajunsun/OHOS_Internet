package zgan.ohos.utils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import zgan.ohos.Models.BaseModel;

/**
 * Created by sunyajun on 16-3-3.
 * 用于获取实体列表
 */
public class XmlParser<T extends BaseModel> extends DefaultHandler {
    //单个实例
    T model;
    //xml解析后的实例集合
    public List<T> list;
    //类的临时实例
    T modelInstance;
    //类中的方法集合
    Method[] methods;
    //此时解析类中存在的方法
    Method method;

    String ParentTag = "li";
    public XmlParser(T _instance, String _parentTag) {
        this(_instance);
        ParentTag = _parentTag;
    }

    public XmlParser(T _instance) {
        modelInstance = _instance;
        methods = modelInstance.getClass().getDeclaredMethods();
    }

    @Override
    public void startDocument() throws SAXException {
        // TODO Auto-generated method stub
        list = new ArrayList<>();
    }

    @Override
    public void endDocument() throws SAXException {
        // TODO Auto-generated method stub

    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        // TODO Auto-generated method stub
        if (localName.equals(ParentTag)) {
            model = modelInstance.getnewinstance();
        }
        if (!(localName.equals("root") || localName.equals(ParentTag)))
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
//        if (localName.equals("item")) {
//            RssFeed.addItem(RssItem);
        // return;
        // }
        if (localName.equals(ParentTag))
            list.add(model);
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        // TODO Auto-generated method stub
        String theString = new String(ch, start, length);
        try {
            method.invoke(model, theString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

