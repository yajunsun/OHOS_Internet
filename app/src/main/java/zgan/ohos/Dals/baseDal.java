package zgan.ohos.Dals;


import android.accounts.NetworkErrorException;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.kxml2.io.KXmlParser;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import zgan.ohos.Models.BaseObject;
import zgan.ohos.utils.DataCacheHelper;
import zgan.ohos.utils.Databasehelper;

public class baseDal<T extends BaseObject> {

    /****************
     * 本地數據
     ****************/
    SQLiteDatabase db;
    String NameSpace = "http://service.zgantech.com";
    String URL ="http://115.28.202.130:10001/EventService";//"http://192.168.1.108:10001/EventService"; //"http://115.28.202.130:10001/EventService";

    public boolean istableExists(String tbname) {

        boolean exits = false;
        String sql = "select * from sqlite_master where name=" + "'" + tbname
                + "'";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.getCount() != 0) {
            exits = true;
        }
        return exits;
    }

    public void creatable(T record) {
        if (!istableExists(record.gettablename())) {
            int count = record.getPropertyCount();
            String columns = "";
            for (int i = 0; i < count; i++) {
                PropertyInfo info = new PropertyInfo();
                record.getPropertyInfo(i, null, info);
                if (info.getType() == String.class)
                    columns += "," + info.getName() + " TEXT";
                else if (info.getType() == Integer.class)
                    columns += "," + info.getName() + " INTEGER";
                else if (info.getType() == byte[].class)
                    columns += "," + info.getName() + " BLOB";
                else if (info.getType() == Float.class)
                    columns += "," + info.getName() + " REAL";
                else {
                    columns += "," + info.getName() + " text";
                }
                System.out.println(info.getName());
            }
            db.execSQL(String.format("create table %s (%s)",
                    record.gettablename(),
                    columns.substring(1, columns.length())));
        }
    }

    // 打开数据库
    public void open() {
        db = (new Databasehelper()).getReadableDatabase();
    }

    // 关闭数据库
    public void close() {
        if (db != null)
            if (db.isOpen())
                db.close();
    }

    // 新增
    public Boolean insert(T record) throws Exception {
        // 数据库操作时用到的contentvalues
        open();
        ContentValues cv = new ContentValues();
        // 字段的属性（类型，字段名）
        PropertyInfo pInfo;
        // 字段数量
        int count = record.getPropertyCount();
        // 若表不存在则创建
        creatable(record);
        // 遍历所有字段
        for (int i = 0; i < count; i++) {
            // 实例化字段属性
            pInfo = new PropertyInfo();
            // 给字段属性赋值
            record.getPropertyInfo(i, null, pInfo);
            // 如果字段类型是BaseObject，则是嵌套类型
            // 获得当前类型
            Class<?> typeClass = (Class<?>) pInfo.type;
            // 判断当前类型是否继承于BaseObject.class
            if (record.getProperty(i) != null)
                if (((Class) BaseObject.class).isAssignableFrom(typeClass)) {
                    // pInfo.type == BaseObject.class
                    PropertyInfo pi = new PropertyInfo();
                    BaseObject bo = (BaseObject) record.getProperty(i);
                    bo.getPropertyInfo(0, null, pi);
                    // 取Id字段名和字段值
                    cv.put(pInfo.getName(), bo.getProperty(0).toString());
                } else {
                    if (pInfo.getType() == byte[].class)
                        cv.put(pInfo.getName(), (byte[]) record.getProperty(i));
                    else {
                        cv.put(pInfo.getName(), record.getProperty(i)
                                .toString());
                    }
                }
        }
        Boolean result = db.insert(record.gettablename(), null, cv) > 0;
        close();
        return result;
    }

    // 删除
    public Boolean delete(T record, String whereClause, String[] whereArgs)
            throws Exception {
        open();
        int result = db.delete(record.gettablename(), whereClause, whereArgs);
        close();
        return result > 0;
    }

    // 修改
    public Boolean update(T record, String whereClause, String[] whereArgs) {
        open();
        // 数据库操作时用到的contentvalues
        ContentValues cv = new ContentValues();
        // 字段的属性（类型，字段名）
        PropertyInfo pInfo;
        // 字段数量
        int count = record.getPropertyCount();
        // 遍历所有字段
        for (int i = 0; i < count; i++) {
            // 实例化字段属性
            pInfo = new PropertyInfo();
            // 给字段属性赋值
            record.getPropertyInfo(i, null, pInfo);
            // 获得当前类型
            Class<?> typeClass = (Class<?>) pInfo.type;
            // 如果字段类型是BaseObject，则是嵌套类型
            if (record.getProperty(i) != null)
                if (((Class) BaseObject.class).isAssignableFrom(typeClass)) {
                    PropertyInfo pi = new PropertyInfo();
                    BaseObject bo = (BaseObject) record.getProperty(i);
                    bo.getPropertyInfo(0, null, pi);
                    // 取Id字段名和字段值
                    cv.put(pInfo.getName(), bo.getProperty(0).toString());
                } else {
                    if (pInfo.getType() == byte[].class)
                        cv.put(pInfo.getName(), (byte[]) record.getProperty(i));
                    else {
                        cv.put(pInfo.getName(), record.getProperty(i)
                                .toString());
                    }
                }
        }

        int result = db.update(record.gettablename(), cv, whereClause,
                whereArgs);
        close();
        return result > 0;
    }

    public T getbaseobject(T record, String selection, String... args) {
        int count = record.getPropertyCount();
        String[] columns = new String[count];
        String[] selectionArgs = args;

        // 若表不存在则创建
        creatable(record);
        for (int i = 0; i < count; i++) {
            PropertyInfo info = new PropertyInfo();
            record.getPropertyInfo(i, null, info);
            columns[i] = info.getName();
        }
        Cursor result = db.query(record.gettablename(), columns, selection,
                selectionArgs, null, null, null);

        if (result.getCount() > 0) {
            // 取第一条记录
            result.moveToFirst();
            for (int i = 0; i < count; i++) {
                PropertyInfo info = new PropertyInfo();
                record.getPropertyInfo(i, null, info);
                if (info.getType() == String.class)
                    record.setProperty(i, result.getString(i));
                else if (info.getType() == Integer.class)
                    record.setProperty(i, result.getInt(i));
                else if (info.getType() == byte[].class)
                    record.setProperty(i, result.getBlob(i));
                else if (info.getType() == Float.class)
                    record.setProperty(i, result.getFloat(i));
                else {
                    record.setProperty(i, result.getString(i));
                }
            }
            return record;
        }
        return null;
    }

    public List<T> getbaseobjectlist(T record, String selection, String... args) {
        List<T> list = new ArrayList<T>();

        String[] selectionArgs = args;
        int count = record.getPropertyCount();
        String[] columns = new String[count];
        // 若表不存在则创建
        creatable(record);
        for (int i = 0; i < count; i++) {
            PropertyInfo info = new PropertyInfo();
            record.getPropertyInfo(i, null, info);
            columns[i] = info.getName();
        }
        Cursor result = db.query(record.gettablename(), columns, selection,
                selectionArgs, null, null, null);
        while (result.moveToNext()) {
            T newrecord = record.getnewinstance(null);
            for (int i = 0; i < count; i++) {
                PropertyInfo info = new PropertyInfo();
                newrecord.getPropertyInfo(i, null, info);
                if (info.getType() == String.class)
                    newrecord.setProperty(i, result.getString(i));
                else if (info.getType() == Integer.class)
                    newrecord.setProperty(i, result.getInt(i));
                else if (info.getType() == byte[].class)
                    newrecord.setProperty(i, result.getBlob(i));
                else if (info.getType() == Float.class)
                    newrecord.setProperty(i, result.getFloat(i));
                else {
                    newrecord.setProperty(i, result.getString(i));
                }
            }
            list.add(newrecord);
        }
        return list;
    }

    /****************
     * 網絡數據
     *****************/

    private List<T> list;
    private SoapSerializationEnvelope envelope;
    private HttpTransportSE transport;
    private SoapObject response;

    private static Boolean wsstatus = false;

    public static boolean getwsstatus() {
        return wsstatus;
    }

    public void setwsstatus() {
        try {
            String SOAP_ACTION = "http://tempuri.org/IEventsContract/TryConnect";
            String MethodName = "TryConnect";
            SoapObject request = new SoapObject(NameSpace, MethodName);

            String resultString;

            resultString = GetExecuteStatus(request, URL, SOAP_ACTION);
            wsstatus = resultString.equals("success");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            wsstatus = false;
        }

    }

    public List<T> getnetobjectlist(T record, SoapObject request, String URL,
                                    String SOAP_ACTION) throws Exception {
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        int count = record.getPropertyCount();
        envelope.setOutputSoapObject(request);
        envelope.bodyOut = request;
        envelope.dotNet = true;
        transport = new HttpTransportSE(URL, 60000);
        transport.debug=true;
        int resultCount = 0;
        SoapObject result=null;
//		try {
//			transport.call(SOAP_ACTION, envelope);
//			response = (SoapObject) envelope.bodyIn;
//		} catch (Exception e) {
//			throw new Exception("网络连接错误");
//		}
        AsyncTask<String, String[], SoapObject> task;
        try {
            task = new GetNetAsyncTask().execute(SOAP_ACTION);
        } catch (Exception e) {
            // TODO: handle exception
            throw e;

        }

        if (task.get() == null) {
            //throw new Exception("网络连接错误");
            String soapstr=DataCacheHelper.loadData(SOAP_ACTION);
            if (soapstr.length()>0)
            {
                KXmlParser xp = new KXmlParser();
                xp.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", true);
                InputStream is=new ByteArrayInputStream(soapstr.getBytes());
                xp.setInput(is, (String) null);
                envelope.parse(xp);
                result=(SoapObject)((SoapObject)envelope.bodyIn).getProperty(0);
            }
        }
        else {
            result = (SoapObject) task.get().getProperty(0);
        }
        list = new ArrayList<T>();
        resultCount = result.getPropertyCount();
        for (int i = 0; i < resultCount; i++) {
            SoapObject item = (SoapObject) result.getProperty(i);
            T newrecord = record.getnewinstance(item);
            list.add(newrecord);
        }
        return list;
    }

    public T getnetobject(T record, SoapObject request, String URL,
                          String SOAP_ACTION) throws Exception {
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);
        envelope.bodyOut = request;
        envelope.dotNet = true;
        transport = new HttpTransportSE(URL, 60000);
        AsyncTask<String, String[], SoapObject> task;
        try {
            task = new GetNetAsyncTask().execute(SOAP_ACTION);
        } catch (Exception e) {
            // TODO: handle exception
            throw e;
        }

        if (task.get() == null)
            throw new NetworkErrorException();//("网络连接错误");
        SoapObject result;
        T newrecord;
        result = (SoapObject) task.get().getProperty(0);
        try {
            newrecord = record.getnewinstance(result);
        } catch (Exception e) {
            // TODO: handle exception
            newrecord = record.getnewinstance(null);
            newrecord.seterror(result);
        }
        return newrecord;
    }

    public T getnetobjectdirect(T record, SoapObject request, String URL,
                                String SOAP_ACTION) throws Exception {
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);
        envelope.bodyOut = request;
        envelope.dotNet = true;
        transport = new HttpTransportSE(URL, 60000);

        transport.call(SOAP_ACTION, envelope);
        response = (SoapObject) envelope.bodyIn;

        SoapObject result;
        T newrecord;
        result = (SoapObject) response.getProperty(0);
        try {
            newrecord = record.getnewinstance(result);
        } catch (Exception e) {
            // TODO: handle exception
            newrecord = record.getnewinstance(null);
            newrecord.seterror(result);
        }
        return newrecord;
    }

    public String GetExecuteStatus(SoapObject request, String URL,
                                   String SOAP_ACTION) throws Exception {
        SoapSerializationEnvelope _envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        return GetExecuteStatus(request, URL, SOAP_ACTION, _envelope);
    }

    public String GetExecuteStatus(SoapObject request, String URL,
                                   String SOAP_ACTION, SoapSerializationEnvelope _envelope)
            throws Exception {
        envelope = _envelope;
        envelope.setOutputSoapObject(request);
        envelope.bodyOut = request;
        envelope.dotNet = true;

        transport = new HttpTransportSE(URL, 600000);
        AsyncTask<String, String[], SoapObject> task;
        try {
            task = new GetNetAsyncTask().execute(SOAP_ACTION);
        } catch (Exception e) {
            // TODO: handle exception
            throw e;
        }

        if (task.get() == null)
            throw new Exception("网络连接错误");
        SoapObject result = (SoapObject) task.get();

        if (result != null) {
            String str = result.getProperty(0).toString();
            return str;
        }
        return "failure";
    }

    public void backgraoupwork(SoapObject _request, String _URL,
                               String _SOAP_ACTION) throws Exception {
        SoapSerializationEnvelope _envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        _envelope.setOutputSoapObject(_request);
        _envelope.bodyOut = _request;
        _envelope.dotNet = true;
        HttpTransportSE _transport = new HttpTransportSE(_URL, 3000000);
        _transport.call(_SOAP_ACTION, _envelope);

    }

    private class GetNetAsyncTask extends
            AsyncTask<String, String[], SoapObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // TODO 执行请求前的准备工作可以在这里做
        }

        @Override
        protected SoapObject doInBackground(String... params) {
            // TODO 接收参数params，并发送网络请求
            try {
                transport.call(params[0], envelope);
                response = (SoapObject) envelope.bodyIn;
            } catch (Exception e) {
                return null;
            }
            return response;
        }

        @Override
        protected void onPostExecute(SoapObject result) {
            super.onPostExecute(result);
            // TODO 处理网络请求返回的数据，并刷新UI
        }
    }
}
