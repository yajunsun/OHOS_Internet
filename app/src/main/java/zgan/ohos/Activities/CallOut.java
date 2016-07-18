package zgan.ohos.Activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.IRegisterIOTCListener;
import com.tutk.IOTC.Monitor;

import zgan.ohos.Models.FuncBase;
import zgan.ohos.R;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.MyCamera;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.generalhelper;

/**
 * create by yajunsun
 *
 * 音视频通话界面
 * */
public class CallOut extends myBaseActivity implements IRegisterIOTCListener, View.OnClickListener, ZganCommunityService.ICallOutListner {


    //摄像头"Y7YXVMNF6CYB9NBXA7EJ"
    // (api测试)"W7KXTWNR6SU3UNBXE7Z1";
    private String mDevUID;// = "Y7YXVMNF6CYB9NBXA7EJ";

    private Monitor monitor = null;
    private Camera mCamera = null;
    private BitmapDrawable bg;
    private BitmapDrawable bgSplit;
    ImageView btn_hangup;

    private String mConnStatus = "";
    private int mVideoWidth;
    private int mVideoHeight;
    private int mSelectedChannel;

    View l_connected, l_waite;
    Dialog dialog;
    FuncBase item;

    @Override
    protected void initView() {

        setContentView(R.layout.activity_call_out);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        item = (FuncBase) getIntent().getSerializableExtra("item");
        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                communityService.hangup();
                toQuitVideo();
                iniDialog("提示", "确定退出视频通话");
            }
        });
        if (item != null) {
            TextView txt_title = (TextView) findViewById(R.id.txt_title);
            txt_title.setText(item.getview_title());
        }
        btn_hangup = (ImageView) findViewById(R.id.btn_hangup);
        btn_hangup.setOnClickListener(this);
        l_connected = findViewById(R.id.l_connected);
        l_waite = findViewById(R.id.l_waite);
//        toSetProgressText("正在接通中，请稍后。。。");
//        toShowProgress();
        //ZganCommunityService.toGetServerData(37, 0, String.format("%s\t3", PreferenceUtil.getUserName()), serviceHandler);
        //connect();
        bindService(new Intent(CallOut.this, ZganCommunityService.class), mconnection, Context.BIND_AUTO_CREATE);
    }

    private void connect() {
        MyCamera.init();

        mSelectedChannel = 0;
        mCamera = new MyCamera("admin["
                + mDevUID
                + "]",
                mDevUID,
                "admin", "admin");
        mCamera.registerIOTCListener(this);

        mCamera.start(
                MyCamera.DEFAULT_AV_CHANNEL,
                "admin", "admin");
        mCamera.connect(mDevUID, mSelectedChannel);
        //mCamera.startShow(mSelectedChannel);
        if (monitor != null)
            monitor.deattachCamera();

        monitor = null;
        monitor = (Monitor) findViewById(R.id.monitor);
        monitor.setMaxZoom(3.0f);
        //monitor.mEnableDither = mCamera.mEnableDither;
        monitor.attachCamera(mCamera, mSelectedChannel);
        //toCloseProgress();
    }

    // 退出视频
    private synchronized void toQuitVideo() {
        if (monitor != null) {
            monitor.deattachCamera();
            monitor = null;
        }

        if (mCamera != null) {
            mCamera.unregisterIOTCListener(this);
//            mCamera.stopSpeaking(mSelectedChannel);
//            mCamera.stopListening(mSelectedChannel);
            mCamera.stopShow(mSelectedChannel);

            mCamera.disconnect();

            //MyCamera.uninit();
            communityService.uninstallCamera();
            mCamera = null;
        }
        if (handler != null) {
            handler.removeCallbacks(null);
            handler = null;
        }
    }

    @Override
    public void ViewClick(View v) {

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_hangup) {
            communityService.hangup();
            toQuitVideo();
            iniDialog("提示", "确定关闭视频通话");
        }
    }

    @Override
    public void receiveFrameData(final Camera camera, int avChannel, Bitmap bmp) {

        if (mCamera == camera && avChannel == mSelectedChannel) {
            if (bmp.getWidth() != mVideoWidth || bmp.getHeight() != mVideoHeight) {
                mVideoWidth = bmp.getWidth();
                mVideoHeight = bmp.getHeight();
            }
        }
    }

    @Override
    public void receiveFrameInfo(final Camera camera, int avChannel, long bitRate, int frameRate, int onlineNm, int frameCount, int incompleteFrameCount) {
    }

    @Override
    public void receiveChannelInfo(final Camera camera, int avChannel, int resultCode) {

        if (mCamera == camera && avChannel == mSelectedChannel) {
            if (handler == null)
                return;
            Bundle bundle = new Bundle();
            bundle.putInt("avChannel", avChannel);

            Message msg = handler.obtainMessage();
            msg.what = resultCode;
            msg.setData(bundle);
            handler.sendMessage(msg);
        }
    }

    @Override
    public void receiveSessionInfo(final Camera camera, int resultCode) {

        if (mCamera == camera) {
            if (handler == null)
                return;
            Bundle bundle = new Bundle();
            Message msg = handler.obtainMessage();
            msg.what = resultCode;
            msg.setData(bundle);
            handler.sendMessage(msg);
        }
    }

    @Override
    public void receiveIOCtrlData(final Camera camera, int avChannel, int avIOCtrlMsgType, byte[] data) {

        if (mCamera == camera) {
            if (handler == null)
                return;
            Bundle bundle = new Bundle();
            bundle.putInt("avChannel", avChannel);
            bundle.putByteArray("data", data);
            Message msg = handler.obtainMessage();
            msg.what = avIOCtrlMsgType;
            handler.sendMessage(msg);
        }
    }

    private Handler handler = new Handler() {

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @SuppressLint("NewApi")
        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            int avChannel = bundle.getInt("avChannel");

            switch (msg.what) {

                case Camera.CONNECTION_STATE_CONNECTING:

                    if (!mCamera.isSessionConnected() || !mCamera.isChannelConnected(mSelectedChannel)) {
                        mConnStatus = "连接中";
                    }

                    break;

                case Camera.CONNECTION_STATE_CONNECTED:

                    //toCloseProgress();
                    l_waite.setVisibility(View.GONE);
                    l_connected.setVisibility(View.VISIBLE);
                    break;

                case Camera.CONNECTION_STATE_DISCONNECTED:
                    mConnStatus = "连接已断开";
                    communityService.hangup();
                    toQuitVideo();
                    iniDialog("提示", "连接已断开");
                    Log.i(TAG,"CONNECTION_STATE_DISCONNECTED");
                    break;

                case Camera.CONNECTION_STATE_UNKNOWN_DEVICE:
                    communityService.hangup();
                    toQuitVideo();
                    iniDialog("提示", "连接失败");
                    Log.i(TAG,"CONNECTION_STATE_UNKNOWN_DEVICE");
                    break;

                case Camera.CONNECTION_STATE_TIMEOUT:
                    if (mCamera != null) {
                        communityService.hangup();
                        toQuitVideo();
                        iniDialog("提示", "连接超时");
                        Log.i(TAG,"CONNECTION_STATE_TIMEOUT");
                    }
                    break;

                case Camera.CONNECTION_STATE_CONNECT_FAILED:
                    communityService.hangup();
                    toQuitVideo();
                    iniDialog("提示", "连接失败");
                    Log.i(TAG,"CONNECTION_STATE_CONNECT_FAILED");
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        toQuitVideo();
        unbindService(mconnection);
        Log.i(TAG, "CallOut onDestroy");
    }

    ZganCommunityService.communityService communityService;
    private ServiceConnection mconnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            communityService = ((ZganCommunityService.communityService) iBinder);
            communityService.setCallOutListner(CallOut.this);
            communityService.reqquestCall();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            communityService.setCallOutListner(null);
            communityService = null;
        }
    };

    @Override
    public void Connect(String devUid) {
        mDevUID = devUid;
        connect();
    }

    @Override
    public void CanntConnect() {
        //generalhelper.ToastShow(CallOut.this, "忙碌中，请稍后再拨");
        //unbindService(mconnection);
        iniDialog("提示", "忙碌中，请稍后再拨");
    }

    @Override
    public void NotOnLine() {
        //设备不在线
        iniDialog("提示", "设备不在线");
    }

    @Override
    public void InBusy() {
        //设备不在线
        iniDialog("提示", "忙碌中，请稍后再拨");
    }

    void iniDialog(String title, String msg) {
        View v = getLayoutInflater().inflate(R.layout.dialog_call_not_sucecess, null, false);
        TextView dialog_title, dialog_msg, dialog_button;
        dialog_title = (TextView) v.findViewById(R.id.dialog_title);
        dialog_msg = (TextView) v.findViewById(R.id.dialog_msg);
        dialog_button = (TextView) v.findViewById(R.id.dialog_button);
        dialog_button.setText("关闭");
        dialog_title.setText(title);
        dialog_msg.setText(msg);
        dialog_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(CallOut.this);
        builder.setCancelable(false);
        builder.setView(v);
        dialog = builder.create();
        dialog.show();
    }
}




