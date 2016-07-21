package zgan.ohos.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.tutk.IOTC.Camera;
import com.tutk.IOTC.HightRDTCamera;
import com.tutk.IOTC.IRegisterIOTCListener;
import com.tutk.IOTC.Monitor;
import com.tutk.IOTC.NormalRDTCamera;
import com.tutk.IOTC.RDTCamera;

import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;

import zgan.ohos.Models.FuncBase;
import zgan.ohos.MyApplication;
import zgan.ohos.R;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.utils.SystemUtils;

/**
 * create by yajunsun
 * <p/>
 * 音视频通话界面
 */
public class CallOut extends myBaseActivity implements IRegisterIOTCListener, View.OnClickListener, ZganCommunityService.ICallOutListner {


    //摄像头"Y7YXVMNF6CYB9NBXA7EJ"
    // (api测试)"W7KXTWNR6SU3UNBXE7Z1";
    private String mDevUID;// = "Y7YXVMNF6CYB9NBXA7EJ";
    private final static int REQUESTCODE = 25;
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
        Log.i(TAG, "my PhoneYear is " + MyApplication.PhoneYear);
        if (MyApplication.PhoneYear < 2013) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CALL_PHONE)) {

                    new AlertDialog.Builder(CallOut.this)
                            .setMessage("app需要开启权限才能使用此功能")
                            .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton("取消", null)
                            .create()
                            .show();
                } else {

                    //申请权限
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            REQUESTCODE);
                }

            } else {
                //已经拥有权限进行拨打
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + SystemUtils.getShop()));
                startActivityForResult(intent, 0);
            }
        } else {
            setContentView(R.layout.activity_call_out);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            item = (FuncBase) getIntent().getSerializableExtra("item");
            View back = findViewById(R.id.back);
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    communityService.hangup();
                    toQuitVideo();
                    iniDialog("提示", "确定退出通话");
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
    }

    private void connect() {
        RDTCamera.init();
        mSelectedChannel = 0;
        if (MyApplication.PhoneYear >= 2013) {
            mCamera = new HightRDTCamera("admin["
                    + mDevUID
                    + "]",
                    mDevUID,
                    "admin", "admin");
            Log.i(TAG, "use HightRDTCamera");
        } else {
            mCamera = new NormalRDTCamera("admin["
                    + mDevUID
                    + "]",
                    mDevUID,
                    "admin", "admin");
            Log.i(TAG, "use NormalRDTCamera");
        }
        mCamera.registerIOTCListener(this);

        mCamera.start(
                RDTCamera.DEFAULT_AV_CHANNEL,
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
                    Log.i(TAG, "CONNECTION_STATE_DISCONNECTED");
                    break;

                case Camera.CONNECTION_STATE_UNKNOWN_DEVICE:
                    communityService.hangup();
                    toQuitVideo();
                    iniDialog("提示", "连接失败");
                    Log.i(TAG, "CONNECTION_STATE_UNKNOWN_DEVICE");
                    break;

                case Camera.CONNECTION_STATE_TIMEOUT:
                    if (mCamera != null) {
                        communityService.hangup();
                        toQuitVideo();
                        iniDialog("提示", "连接超时");
                        Log.i(TAG, "CONNECTION_STATE_TIMEOUT");
                    }
                    break;

                case Camera.CONNECTION_STATE_CONNECT_FAILED:
                    communityService.hangup();
                    toQuitVideo();
                    iniDialog("提示", "连接失败");
                    Log.i(TAG, "CONNECTION_STATE_CONNECT_FAILED");
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, String.valueOf(resultCode));
        if (requestCode == 0)
            finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        toQuitVideo();
        if (communityService != null)
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUESTCODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //用户同意了授权
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel://" + SystemUtils.getShop()));
                    if (getPackageManager().checkPermission("android.permission.CALL_PHONE", getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                        startActivityForResult(intent, 0);
                    } else {
                        iniDialog("提示", "呼叫受限，请修改应用权限~");
                    }

                } else {
                    //用户拒绝了授权
                    // Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }

    }
}




