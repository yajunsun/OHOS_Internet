package zgan.ohos.Receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;

import cn.jpush.android.api.JPushInterface;
import zgan.ohos.Activities.MainActivity;
import zgan.ohos.Activities.SplashActivity;
import zgan.ohos.R;
import zgan.ohos.utils.SystemUtils;

/**
 * Created by yajunsun on 2016/8/27.
 * 接收服务器推送消息
 */
public class JReceiver extends BroadcastReceiver {
    String TAG = "SUNRECEIVE";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            Log.d(TAG, "[MyReceiver] 接收Registration Id : " + regId);
            //send the Registration Id to your server...

        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] 接收到推送下来的自定义消息: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
            //processCustomMessage(context, bundle);

        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            //标题JPushInterface.EXTRA_NOTIFICATION_TITLE
            //推送内容JPushInterface.EXTRA_ALERT
            Log.d(TAG, "[JReceiver] 接收到推送下来的通知");
            //int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
            String title = bundle.getString(JPushInterface.EXTRA_TITLE);
            String content = bundle.getString(JPushInterface.EXTRA_ALERT);
            //设置自定义布局中按钮的跳转界面
            Intent btnIntent ;
            if(SystemUtils.getIsLogin()&&SystemUtils.getIsCommunityLogin()) {
                btnIntent = new Intent(context, MainActivity.class);
            }
            else {
                btnIntent = new Intent(context, SplashActivity.class);
            }
            btnIntent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
            //如果是启动activity，那么就用PendingIntent.getActivity，如果是启动服务，那么是getService
            PendingIntent Pintent = PendingIntent.getActivity(context,
                    (int) SystemClock.uptimeMillis(), btnIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // 自定义布局
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.customer_notitfication_layout);
            remoteViews.setImageViewResource(R.id.icon, R.drawable.launcher);
            remoteViews.setTextViewText(R.id.title, title);
            remoteViews.setTextViewText(R.id.text, content);
            remoteViews.setOnClickPendingIntent(R.id.notificationview, Pintent);//定义按钮点击后的动作
            Notification notification=new Notification();
            notification.contentView=remoteViews;
            notification.contentIntent=Pintent;
            NotificationManager manager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(0,notification);
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            //Log.d(TAG, "[MyReceiver] 用户点击打开了通知");
            //打开自定义的Activity
            Intent btnIntent ;
            if(SystemUtils.getIsLogin()&&SystemUtils.getIsCommunityLogin()) {
                btnIntent = new Intent(context, MainActivity.class);
            }
            else {
                btnIntent = new Intent(context, SplashActivity.class);
            }
            btnIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(btnIntent);

        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
            Log.d(TAG, "[JReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
            //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..

        } else if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
            boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
            Log.w(TAG, "[JReceiver]" + intent.getAction() + " connected state change to " + connected);
        } else {
            Log.d(TAG, "[JReceiver] Unhandled intent - " + intent.getAction());
        }
    }
}
