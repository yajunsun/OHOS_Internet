package com.pay.wxpay;

import android.os.AsyncTask;

import com.tencent.mm.sdk.constants.Build;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import zgan.ohos.Models.MyOrder;
import zgan.ohos.Models.SM_OrderPayInfo;
import zgan.ohos.Models.WXPayM;
import zgan.ohos.utils.NetUtils;
import zgan.ohos.utils.XmlParser_model;
import zgan.ohos.utils.generalhelper;

/**
 * Created by Administrator on 16-4-23.
 */
public class WXPay {
    public static final String payresultAction = "action.pay.wxpay.result";
    private IWXAPI api;
    private String appid, attach, body, mch_id, nonce_str, device_info,
            out_trade_no, notify_url, total_fee, trade_type, key, sign,
            detail, fee_type, spbill_create_ip, time_start, time_expire, goods_tag, limit_pay;
    // private String prepay_id;

    WXPayM preResult;


    private OnWXPayListner onPayListner;

    public void setOnPayListner(OnWXPayListner _listner) {
        this.onPayListner = _listner;
    }

    public WXPay(IWXAPI _api) {
        api = _api;
    }

    public void setOrder(MyOrder order) {
        Calendar c = Calendar.getInstance();
        //订单时间
        final Date d = c.getTime();
        c.add(Calendar.MINUTE, 30);
        final Date e = c.getTime();

        appid = Constants.APP_ID;
        attach = "一家一店在线支付-微信支付";
        body = "一家一店在线支付-微信支付"; //order.toString();
        mch_id = Constants.MCH_ID;
        nonce_str = getRandomString(32).toUpperCase();
        device_info = "WEB";
        out_trade_no = order.getorder_id();
        total_fee = String.valueOf((int) (order.gettotal() * 100));
        trade_type = "APP";
        notify_url = Constants.notify_url;
        detail = "一家一店在线支付-微信支付";
        fee_type = "CNY";
        spbill_create_ip = "192.168.0.155";
        time_start = generalhelper.getStringFromDate(d, "yyyyMMddHHmmss");
        time_expire = generalhelper.getStringFromDate(e, "yyyyMMddHHmmss");
        goods_tag = "WXG";
        limit_pay = "no_credit";


        key = Constants.Key;
        sign = getpreSIGN();
    }

    public void setOrder(SM_OrderPayInfo order) {
        Calendar c = Calendar.getInstance();
        //订单时间
        final Date d = c.getTime();
        c.add(Calendar.MINUTE, 30);
        final Date e = c.getTime();

        appid = Constants.APP_ID;
        attach = "一家一店在线支付-微信支付";
        body = "一家一店在线支付-微信支付"; //order.toString();
        mch_id = order.getpay_ways().getpay_account();
        nonce_str = getRandomString(32).toUpperCase();
        device_info = "WEB";
        out_trade_no = order.getpay_order_sn();
        total_fee = String.valueOf( Math.round(Double.parseDouble( order.gettotal_price()) * 100));
        trade_type = "APP";
        notify_url =order.getpay_ways().getpusiness_notice_url();
        detail = "一家一店在线支付-微信支付";
        fee_type = "CNY";
        spbill_create_ip = "192.168.0.155";
        time_start = generalhelper.getStringFromDate(d, "yyyyMMddHHmmss");
        time_expire = generalhelper.getStringFromDate(e, "yyyyMMddHHmmss");
        goods_tag = "WXG";
        limit_pay = "no_credit";


        key = order.getpay_ways().getPay_key();
        sign = getpreSIGN();
    }

    public String prePay() {

        /************统一下单************/
        String urlString = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        HttpURLConnection connection = null;
        BufferedInputStream in = null;
        DataOutputStream out = null;
        byte[] bytes = new byte[1024];
        try {
            final URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            // Read from the connection. Default is true.
            connection.setDoInput(true);
            // Set the post method. Default is GET
            connection.setRequestMethod("POST");

            // Post 请求不能使用缓存
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Content-Type",
                    "application/xml;charset=utf-8");
            StringBuilder dreqest = new StringBuilder();
            dreqest.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            dreqest.append("<xml>");
            dreqest.append("<appid>" + appid + "</appid>");
            dreqest.append("<attach>" + attach + "</attach>");
            dreqest.append("<body>" + body + "</body>");
            dreqest.append("<detail>" + detail + "</detail>");
            dreqest.append("<device_info>" + device_info + "</device_info>");
            dreqest.append("<fee_type>" + fee_type + "</fee_type>");
            dreqest.append("<goods_tag>" + goods_tag + "</goods_tag>");
            dreqest.append("<limit_pay>" + limit_pay + "</limit_pay>");
            dreqest.append("<mch_id>" + mch_id + "</mch_id>");
            dreqest.append("<nonce_str>" + nonce_str + "</nonce_str>");
            dreqest.append("<notify_url>" + notify_url + "</notify_url>");
            dreqest.append("<out_trade_no>" + out_trade_no + "</out_trade_no>");
            dreqest.append("<spbill_create_ip>" + spbill_create_ip + "</spbill_create_ip>");
            dreqest.append("<time_expire>" + time_expire + "</time_expire>");
            dreqest.append("<time_start>" + time_start + "</time_start>");
            dreqest.append("<total_fee>" + total_fee + "</total_fee>");
            dreqest.append("<trade_type>" + trade_type + "</trade_type>");
            dreqest.append("<sign>" + sign + "</sign>");
            dreqest.append("</xml>");
            out = new DataOutputStream(connection
                    .getOutputStream());
            out.write(dreqest.toString().getBytes());
            in = new BufferedInputStream(connection.getInputStream(), 1024);
            StringBuffer result = new StringBuffer();
            while ((in.read(bytes)) != -1) {
                result.append(new String(bytes));
            }
            return result.toString();
        } catch (IOException e2) {
            e2.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                in.close();
                out.flush();
                out.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return "";
    }

    public void Pay() {
        new doPayTask().execute(new String[]{"0"});
    }

    class doPayTask extends AsyncTask<String, String[], String> {
        @Override
        protected String doInBackground(String... params) {
            String result = "";
            String exeType = params[0];
            if (exeType.equals("0")) {
                result = prePay().trim();

            } else if (exeType.equals("1")) {
                preResult = GetSingleModel(params[1], new WXPayM(), "xml");
                if (preResult.getreturn_code().equals("SUCCESS")) {
                    PayReq req = new PayReq();
                    req.appId = preResult.getappid();// appid;
                    req.partnerId = preResult.getmch_id();// mch_id;
                    req.prepayId = preResult.getprepay_id();
                    req.nonceStr = preResult.getnonce_str();// nonce_str;
                    req.timeStamp = String.valueOf(System.currentTimeMillis()).substring(0, 10);
                    req.packageValue = "Sign=WXPay";
                    getpaySIGN(req);
                    // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
                    api.registerApp(Constants.APP_ID);
                    api.sendReq(req);
                    // }
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.length() > 0) {
                StringBuilder builder = new StringBuilder();
                String[] lins = s.split("\n");
                for (String line : lins
                        ) {
                    builder.append(line);
                }
                new doPayTask().execute(new String[]{"1", builder.toString().replace("<![CDATA[", "").replace("]]>", "")});
            }
        }

    }

    public boolean checkSupport() {
        boolean isPaySupported = api.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
        return isPaySupported;
    }

    private WXPayM GetSingleModel(String xmlString, WXPayM modelInstance, String parentTag) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            XmlParser_model<WXPayM> handler = new XmlParser_model<>(modelInstance, parentTag);
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

    private String getRandomString(int length) { //length表示生成字符串的长度
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    private String getpreSIGN() {
        //String A = String.format("appid=%s&body=%s&mch_id=%s&nonce_str=%s&notify_url=%s&out_trade_no=%s&device_info=%s&total_fee=%s&trade_type=%s", appid, body, mch_id, nonce_str,notify_url,out_trade_no,device_info,total_fee,trade_type);
        String A = String.format("appid=%s&attach=%s&body=%s&detail=%s&device_info=%s&fee_type=%s&goods_tag=%s&limit_pay=%s&mch_id=%s&nonce_str=%s&notify_url=%s&out_trade_no=%s&spbill_create_ip=%s&time_expire=%s&time_start=%s&total_fee=%s&trade_type=%s",
                appid, attach, body, detail, device_info, fee_type, goods_tag, limit_pay, mch_id, nonce_str, notify_url, out_trade_no, spbill_create_ip, time_expire, time_start, total_fee, trade_type);
        String SignTemp = String.format("%s&key=%s", A, key);
        String sign = MD5.getMessageDigest(SignTemp.getBytes()).toUpperCase();
        return sign;
    }

    private String getpaySIGN(PayReq req) {
        String A = String.format("appid=%s&noncestr=%s&package=Sign=WXPay&partnerid=%s&prepayid=%s&timestamp=%s", req.appId, req.nonceStr, req.partnerId, req.prepayId, req.timeStamp);
        String SignTemp = String.format("%s&key=%s", A, key);
        String sign = MD5.getMessageDigest(SignTemp.getBytes()).toUpperCase();
        req.sign = sign;
        return sign;
    }

    public interface OnWXPayListner {
        void done(BaseResp req);

        void predo();
    }
}
