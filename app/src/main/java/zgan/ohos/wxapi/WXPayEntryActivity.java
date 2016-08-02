package zgan.ohos.wxapi;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.pay.wxpay.Constants;
import com.pay.wxpay.WXPay;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import zgan.ohos.Models.WXReq;
import zgan.ohos.Models.WXResp;
import zgan.ohos.R;


public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

    private static final String TAG = "MicroMsg.SDKSample.WXPayEntryActivity";

    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_result);

        api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
        Toast.makeText(this, "openid = " + req.openId, Toast.LENGTH_SHORT).show();

//        switch (req.getType()) {
//            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
//                //goToGetMsg();
//                break;
//            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
//                //goToShowMsg((ShowMessageFromWX.Req) req);
//                break;
//            case ConstantsAPI.COMMAND_LAUNCH_BY_WX:
//                //Toast.makeText(this, R.string.launch_from_wx, Toast.LENGTH_SHORT).show();
//                break;
//            default:
//                break;
//        }
//        WXReq wxreq = new WXReq(req);
//        Intent data = new Intent();
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("data", wxreq);
//        setResult(WXPay.reqcode, data);
    }

    @Override
    public void onResp(BaseResp resp) {

        //Toast.makeText(this, String.format("openid =%s,errcode =%s,type=%s ", resp.openId,resp.errCode,resp.getType()) , Toast.LENGTH_LONG).show();
        Log.i("suntest", String.format("openid =%s,errcode =%s,type=%s ", resp.openId, resp.errCode, resp.getType()));

//        if (resp.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
//            Toast.makeText(this, "code = " + ((SendAuth.Resp) resp).code, Toast.LENGTH_SHORT).show();
//        }
//        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
//            Log.d("suntest", "onPayFinish,errCode=" + resp.errCode);
//        int result = 0;
//        switch (resp.errCode) {
//            case BaseResp.ErrCode.ERR_OK:
//                result = R.string.errcode_success;
//                break;
//            case BaseResp.ErrCode.ERR_USER_CANCEL:
//                result = R.string.errcode_cancel;
//                break;
//            case BaseResp.ErrCode.ERR_AUTH_DENIED:
//                result = R.string.errcode_deny;
//                break;
//            default:
//                result = R.string.errcode_unknown;
//                break;
//        }
//        }

        WXResp wxresp = new WXResp(resp);
        Intent data = new Intent(WXPay.payresultAction);
        Bundle bundle = new Bundle();
        bundle.putSerializable("data", wxresp);
        data.putExtras(bundle);
        sendOrderedBroadcast(data,null);
        Log.i("suntest", "complete pay and sendbroadcast");
        finish();
    }
}