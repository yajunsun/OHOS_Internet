package com.pay.alipay;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import zgan.ohos.Models.MyOrder;
import zgan.ohos.Models.SM_OrderPayInfo;
import zgan.ohos.utils.SystemUtils;

/**
 * Created by yajunsun on 16-4-23.
 */
public class AliPay {


    // 商户PID
    public static final String PARTNER = "2088221444141074";
    // 商户收款账号
    public static final String SELLER = "heyoujun@1home1shop.com";// "2088221444141074";
    // 商户私钥，pkcs8格式
    public static final String RSA_PRIVATE = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBANuPSRewzWm3bE01DMBW3PJLw7k2zPKI9pMwRrra1NZYIfo/WIHyPPowA/cymp30LuG20xl3nQ1baXMoX0ZR1CKALcH0GMZ7XgDoTp/yoqV+XlEcgvRpg5LJNeXNp6s88cE+X4md7P26te5Z1wEaqIi61SVXDokBgDwlUPOS1aHxAgMBAAECgYEAm9OA68h2sU4WFmHNUAEWRPzSx3QGVygv4F0GBf3jytC4JLSqq2dyMZq8ZchLhklUsKuh/VQwaddz6uA/ZlgTNqw33SeyUA2j/QNA7SAcb3RSj5siBJ4neA8GAn7t1vcYfNqaCUeMjNNGmM8mECGD6tLK8pYteh8nG868cJ3BY3ECQQD6pQp8jveiE0W/EixqNXLyeCAkGVdEZNvpCUiKFUskvphy5Nnsc6Tkw5R1sahwA2RNVCYIl7ju5vDbgtKHMQLVAkEA4EA30wl7XemDYkRpy+JOE/hxQcI+Vyemw2OKLU6RlHO1aXILwhuIM+UWH7QgGE8MpbMH1M0qldYn3zjV5ofYrQJBALQzoFx5NVTTYlvDJyedEe19rC4IAhPsJ6ddw0dzk7jxRw2jt4ImirDmZIBRoHYYGi72hvm6i31HkHdhkRcdh5kCQDJHc7zHdXea8bBIsofaF7N2kr4xtRSJeWR5nvOFmDJ2twgLYAOHdMRd6tX05vVMVAOa3nih/5hUyd/MuHjVoIkCQFT5ssWljjQ0yPnhgQX3oBgLFOBiLNTMreToXS8kcKHT6cdaRdGz4v0AW8CwBDtzK3b6Ihes0wCyLrCLGoFhQQI=";
    // 支付宝公钥
    public static final String RSA_PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDI6d306Q8fIfCOaTXyiUeJHkrIvYISRcc73s3vF1ZT7XN8RNPwJxo8pWaJMmvyTn9N4HQ632qJBVHf8sxHi/fEsraprwCtzvzQETrNRwVxLO5jVmRGi60j8Ue1efIlzPXV9je9mkjzOmdssymZkh2QhUrCmZYI/FCEa3/cNMW0QIDAQAB";
    private static final int SDK_PAY_FLAG = 1;

    Dialog alertDialog;
    private Context context;
    private OnAliPayListner onPayListner;

    public AliPay(Context _context) {
        this.context = _context;
    }

    public void setOnPayListner(OnAliPayListner _listner) {
        this.onPayListner = _listner;
    }

    /**
     * call alipay sdk pay. 调用SDK支付
     */
    public void Pay(MyOrder order) {
        if (TextUtils.isEmpty(PARTNER) || TextUtils.isEmpty(RSA_PRIVATE) || TextUtils.isEmpty(SELLER)) {
            alertDialog = new AlertDialog.Builder(context).setTitle("警告").setMessage("需要配置PARTNER | RSA_PRIVATE| SELLER")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface, int i) {
                            alertDialog.dismiss();
                        }
                    }).create();//.show();
            alertDialog.show();
            return;
        }
        String orderInfo = getOrderInfo(order);

        /**
         * 特别注意，这里的签名逻辑需要放在服务端，切勿将私钥泄露在代码中！
         */
        String sign = sign(orderInfo);
        try {
            /**
             * 仅需对sign 做URL编码
             */
            sign = URLEncoder.encode(sign, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        /**
         * 完整的符合支付宝参数规范的订单信息
         */
        final String payInfo = orderInfo + "&sign=\"" + sign + "\"&" + getSignType();

        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask alipay = new PayTask((Activity) context);
                // 调用支付接口，获取支付结果
                String result = alipay.pay(payInfo, true);
                PayResult payResult = new PayResult(result);
                /**
                 * 同步返回的结果必须放置到服务端进行验证（验证的规则请看https://doc.open.alipay.com/doc2/
                 * detail.htm?spm=0.0.0.0.xdvAU6&treeId=59&articleId=103665&
                 * docType=1) 建议商户依赖异步通知
                 */
                String resultInfo = payResult.getResult();// 同步返回需要验证的信息

                String resultStatus = payResult.getResultStatus();
                handler.obtainMessage(1, resultStatus).sendToTarget();
//                if(onPayListner!=null)
//                    onPayListner.done(resultStatus);
                // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                // if (TextUtils.equals(resultStatus, "9000")) {
                //    Toast.makeText(context, "支付成功", Toast.LENGTH_SHORT).show();
//                } else {
//                    // 判断resultStatus 为非"9000"则代表可能支付失败
//                    // "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
//                    if (TextUtils.equals(resultStatus, "8000")) {
//                        Toast.makeText(context, "支付结果确认中", Toast.LENGTH_SHORT).show();
//
//                    } else {
//                        // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
//                        Toast.makeText(context, "支付失败", Toast.LENGTH_SHORT).show();
//
//                    }
//                }
            }
        };
        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    public void Pay(SM_OrderPayInfo smpayInfo)
    {
        if (TextUtils.isEmpty(PARTNER) || TextUtils.isEmpty(RSA_PRIVATE) || TextUtils.isEmpty(SELLER)) {
        alertDialog = new AlertDialog.Builder(context).setTitle("警告").setMessage("需要配置PARTNER | RSA_PRIVATE| SELLER")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        alertDialog.dismiss();
                    }
                }).create();//.show();
        alertDialog.show();
        return;
    }
        String orderInfo = getOrderInfo(smpayInfo);

        /**
         * 特别注意，这里的签名逻辑需要放在服务端，切勿将私钥泄露在代码中！
         */
        String sign = sign(orderInfo);
        try {
            /**
             * 仅需对sign 做URL编码
             */
            sign = URLEncoder.encode(sign, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        /**
         * 完整的符合支付宝参数规范的订单信息
         */
        final String payInfo = orderInfo + "&sign=\"" + sign + "\"&" + getSignType();

        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask alipay = new PayTask((Activity) context);
                // 调用支付接口，获取支付结果
                String result = alipay.pay(payInfo, true);
                PayResult payResult = new PayResult(result);
                /**
                 * 同步返回的结果必须放置到服务端进行验证（验证的规则请看https://doc.open.alipay.com/doc2/
                 * detail.htm?spm=0.0.0.0.xdvAU6&treeId=59&articleId=103665&
                 * docType=1) 建议商户依赖异步通知
                 */
                String resultInfo = payResult.getResult();// 同步返回需要验证的信息

                String resultStatus = payResult.getResultStatus();
                handler.obtainMessage(1, resultStatus).sendToTarget();
//                if(onPayListner!=null)
//                    onPayListner.done(resultStatus);
                // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                // if (TextUtils.equals(resultStatus, "9000")) {
                //    Toast.makeText(context, "支付成功", Toast.LENGTH_SHORT).show();
//                } else {
//                    // 判断resultStatus 为非"9000"则代表可能支付失败
//                    // "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
//                    if (TextUtils.equals(resultStatus, "8000")) {
//                        Toast.makeText(context, "支付结果确认中", Toast.LENGTH_SHORT).show();
//
//                    } else {
//                        // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
//                        Toast.makeText(context, "支付失败", Toast.LENGTH_SHORT).show();
//
//                    }
//                }
            }
        };
        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    public void h5Pay(View v) {
        Intent intent = new Intent(context, H5PayDemoActivity.class);
        Bundle extras = new Bundle();
        /**
         * url是测试的网站，在app内部打开页面是基于webview打开的，demo中的webview是H5PayDemoActivity，
         * demo中拦截url进行支付的逻辑是在H5PayDemoActivity中shouldOverrideUrlLoading方法实现，
         * 商户可以根据自己的需求来实现
         */
        String url = "http://m.meituan.com";
        // url可以是一号店或者美团等第三方的购物wap站点，在该网站的支付过程中，支付宝sdk完成拦截支付
        extras.putString("url", url);
        intent.putExtras(extras);
        context.startActivity(intent);

    }

    /**
     * create the order info. 创建订单信息
     */
    private String getOrderInfo(MyOrder order) {

        // 签约合作者身份ID
        String orderInfo = "partner=" + "\"" + PARTNER + "\"";

        // 签约卖家支付宝账号
        orderInfo += "&seller_id=" + "\"" + SELLER + "\"";

        // 商户网站唯一订单号
        //orderInfo += "&out_trade_no=" + "\"" + getOutTradeNo() + "\"";
        orderInfo += "&out_trade_no=" + "\"" + order.getorder_id() + "\"";

        // 商品名称
        orderInfo += "&subject=" + "\"一家一店在线支付-支付宝支付\"";

        // 商品详情
        orderInfo += "&body=" + "\"一家一店在线支付-支付宝支付\"";

        // 商品金额
        orderInfo += "&total_fee=" + "\""+order.gettotal()+"\"";

//        // 服务器异步通知页面路径
        orderInfo += "&notify_url=" + "\"" + SystemUtils.getALIPAYurl() + "\"";

        // 服务接口名称， 固定值
        orderInfo += "&service=\"mobile.securitypay.pay\"";

        // 支付类型， 固定值
        orderInfo += "&payment_type=\"1\"";

        // 参数编码， 固定值
        orderInfo += "&_input_charset=\"utf-8\"";

        // 设置未付款交易的超时时间
        // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
        // 取值范围：1m～15d。
        // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
        // 该参数数值不接受小数点，如1.5h，可转换为90m。
        orderInfo += "&it_b_pay=\"5m\"";

        // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
        // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

        // 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
        orderInfo += "&return_url=\"m.alipay.com\"";

        // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
        // orderInfo += "&paymethod=\"expressGateway\"";

        return orderInfo;
    }

    private String getOrderInfo(SM_OrderPayInfo  order) {

        // 签约合作者身份ID
        String orderInfo = "partner=" + "\"" + order.getpay_ways().getpay_user() + "\"";

        // 签约卖家支付宝账号
        orderInfo += "&seller_id=" + "\"" + order.getpay_ways().getpay_account() + "\"";

        // 商户网站唯一订单号
        //orderInfo += "&out_trade_no=" + "\"" + getOutTradeNo() + "\"";
        orderInfo += "&out_trade_no=" + "\"" + order.getpay_order_sn() + "\"";

        // 商品名称
        orderInfo += "&subject=" + "\"一家一店在线支付-支付宝支付\"";

        // 商品详情
        orderInfo += "&body=" + "\"一家一店在线支付-支付宝支付\"";

        // 商品金额
        orderInfo += "&total_fee=" + "\""+order.gettotal_price()+"\"";

//        // 服务器异步通知页面路径
        orderInfo += "&notify_url=" + "\"" + order.getpay_ways().getpusiness_notice_url()+ "\"";

        // 服务接口名称， 固定值
        orderInfo += "&service=\"mobile.securitypay.pay\"";

        // 支付类型， 固定值
        orderInfo += "&payment_type=\"1\"";

        // 参数编码， 固定值
        orderInfo += "&_input_charset=\"utf-8\"";

        // 设置未付款交易的超时时间
        // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
        // 取值范围：1m～15d。
        // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
        // 该参数数值不接受小数点，如1.5h，可转换为90m。
        orderInfo += "&it_b_pay=\"5m\"";

        // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
        // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

        // 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
        orderInfo += "&return_url=\"m.alipay.com\"";

        // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
        // orderInfo += "&paymethod=\"expressGateway\"";

        return orderInfo;
    }


    /**
     * get the out_trade_no for an order. 生成商户订单号，该值在商户端应保持唯一（可自定义格式规范）
     */
    private String getOutTradeNo() {
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss", Locale.getDefault());
        Date date = new Date();
        String key = format.format(date);

        Random r = new Random();
        key = key + r.nextInt();
        key = key.substring(0, 15);
        return key;
    }

    /**
     * sign the order info. 对订单信息进行签名
     *
     * @param content 待签名订单信息
     */
    private String sign(String content) {
        return SignUtils.sign(content, RSA_PRIVATE);
    }

    /**
     * get the sign type we use. 获取签名方式
     */
    private String getSignType() {
        return "sign_type=\"RSA\"";
    }

    public interface OnAliPayListner {
        void done(String stat);

        void predo();
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (onPayListner != null)
                onPayListner.done(msg.obj.toString());
        }
    };
}
