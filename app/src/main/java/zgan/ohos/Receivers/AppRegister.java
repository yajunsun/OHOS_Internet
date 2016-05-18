package zgan.ohos.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pay.wxpay.Constants;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class AppRegister extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		//final IWXAPI msgApi = WXAPIFactory.createWXAPI(context, null);
		final IWXAPI msgApi = WXAPIFactory.createWXAPI(context,Constants.APP_ID, false);

		// 将该app注册到微信
		msgApi.registerApp(Constants.APP_ID);
	}
}
