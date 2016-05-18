package zgan.ohos.Models;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import zgan.ohos.utils.NetUtils;
import zgan.ohos.utils.XmlParser_model;

/**
 * Created by Administrator on 16-4-27.
 */
public class WXPayM extends BaseModel {

    private String return_code;
    private String return_msg;
    private String appid;
    private String mch_id;
    private String device_info;
    private String nonce_str;
    private String sign;
    private String result_code;
    private String prepay_id;
    private String trade_type;

    public String getreturn_code() {
        return return_code;
    }

    public void setreturn_code(String return_code) {
        this.return_code = return_code;
    }

    public String getreturn_msg() {
        return return_msg;
    }

    public void setreturn_msg(String return_msg) {
        this.return_msg = return_msg;
    }

    public String getappid() {
        return appid;
    }

    public void setappid(String appid) {
        this.appid = appid;
    }

    public String getmch_id() {
        return mch_id;
    }

    public void setmch_id(String mch_id) {
        this.mch_id = mch_id;
    }

    public String getdevice_info() {
        return device_info;
    }

    public void setdevice_info(String device_info) {
        this.device_info = device_info;
    }

    public String getnonce_str() {
        return nonce_str;
    }

    public void setnonce_str(String nonce_str) {
        this.nonce_str = nonce_str;
    }

    public String getsign() {
        return sign;
    }

    public void setsign(String sign) {
        this.sign = sign;
    }

    public String getresult_code() {
        return result_code;
    }

    public void setresult_code(String result_code) {
        this.result_code = result_code;
    }

    public String getprepay_id() {
        return prepay_id;
    }

    public void setprepay_id(String prepay_id) {
        this.prepay_id = prepay_id;
    }

    public String gettrade_type() {
        return trade_type;
    }

    public void settrade_type(String trade_type) {
        this.trade_type = trade_type;
    }

    @Override
    public WXPayM getnewinstance() {
        return new WXPayM();
    }
}
