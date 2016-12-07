package zgan.ohos.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import zgan.ohos.MyApplication;
import zgan.ohos.R;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.services.login.ZganLoginService;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.LocationUtil;
import zgan.ohos.utils.NetUtils;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;
import zgan.ohos.utils.generalhelper;
import zgan.ohos.utils.resultCodes;

/**
 * create by yajunsun
 * <p/>
 * 登陆界面
 */
public class Login extends myBaseActivity {

    EditText et_Phone;
    EditText et_pwd;
    String PhoneNum;
    Button btnlogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtils.iniLoginActivity(this);
    }


    public void initView() {
        setContentView(R.layout.lo_activity_login);
        et_Phone = (EditText) findViewById(R.id.et_Phone);
        et_pwd = (EditText) findViewById(R.id.et_pwd);
        btnlogin=(Button)findViewById(R.id.btn_login);
        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewClick(v);
            }
        });
        //phone参数从注册验证那边传过来
        Intent requestIntent = getIntent();
        if (requestIntent.hasExtra("phone")) {
            et_Phone.setText(requestIntent.getStringExtra("phone"));
        }
    }

    public void ViewClick(View view) {
        boolean CheckName = false;
        boolean CheckPwd = false;
        switch (view.getId()) {
            case R.id.btn_login:
                String Phone = et_Phone.getText().toString().trim();
                String Pwd = et_pwd.getText().toString().trim();
                if (Phone.length() == 0) {
                   generalhelper.ToastShow(Login.this,"电话号码不能为空");

                } else if (Phone.toCharArray().length != 11) {
                    generalhelper.ToastShow(Login.this,"电话号码填写错误");

                } else {
                    CheckName = true;

                }
                if (Pwd.length() == 0) {
                    generalhelper.ToastShow(Login.this,"密码不能为空");

                } else if (Pwd.length() > 20) {
                    generalhelper.ToastShow(Login.this,"密码长度不能超过20位");

                } else {
                    CheckPwd = true;

                }
                try {
                    if (CheckName && CheckPwd) {
                        toSetProgressText("请稍等...");
                        toShowProgress();
                        this.PhoneNum = Phone;
                        //SystemUtils.login(Phone, Pwd, handler);
                        String imei = LocationUtil.getDrivenToken(MyApplication.context, Phone);
                        ZganLoginService.toUserLogin(Phone, Pwd, imei, handler);
                        Log.v(TAG, "log click");
                    }

                } catch (Exception ex) {
                    generalhelper.ToastShow(Login.this, ex.getMessage());
                }
                break;
            case R.id.btn_register:

                Intent intent = new Intent(Login.this, Register.class);
                startActivityWithAnim(intent);
//                Intent intent=new Intent(Login.this,BindCommunity.class);
//                intent.putExtra("username",et_Phone.getText().toString());
//                intent.putExtra("pwd",et_pwd.getText().toString());
//                startActivityWithAnim(intent);
                break;
            case R.id.btn_forgetpwd:
                Intent pwdintent = new Intent(Login.this, SMSValidationStep1.class);
                startActivityWithAnim(pwdintent);
                break;
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Frame frame = (Frame) msg.obj;
                String result = generalhelper.getSocketeStringResult(frame.strData);
                String[] results = result.split(",");
                if (frame.subCmd == 1) {
                    if (result.equals("0")) {
                        ZganLoginService.toGetServerData(3, 0, PhoneNum, handler);

                    } else if (result.equals("6") || result.equals("8")) {
                        generalhelper.ToastShow(Login.this,"不存在此用户，请先注册");

                    } else if (result.equals("26")) {
                        generalhelper.ToastShow(Login.this,"密码错误");

                    }
                } else if (frame.subCmd == 3) {
                    String communityIP = PreferenceUtil.getCommunityIP();
                    int communityPort = PreferenceUtil.getCommunityPORT();
                    if (results.length == 3 && results[0].equals("0")) {
                        Log.v("TAG", "ZganLoginService小区ID：" + results[1]);
                        //String[] ipport = results[1].split(":");
                        if (!communityIP.equals(NetUtils.getIp(results[1])) || communityPort != Integer.parseInt(results[2])) {
                            PreferenceUtil.setCommunityIP(NetUtils.getIp(results[1]));
                            PreferenceUtil.setCommunityPORT(Integer.parseInt(results[2]));
                            communityIP = NetUtils.getIp(results[1]);
                            communityPort = Integer.parseInt(results[2]);
                        }
                        ZganCommunityService.CommunityIp = communityIP;
                        ZganCommunityService.CommunityPort = communityPort;
                        ZganCommunityService.toUserLogin(PhoneNum, et_pwd.getText().toString().trim(), communityHandler);
                    } else {
                        //Intent intent = new Intent(Login.this, BindDevice.class);
                        Intent intent = new Intent(Login.this, BindCommunity.class);
                        intent.putExtra("username", PhoneNum);
                        intent.putExtra("pwd", et_pwd.getText().toString().trim());
                        //intent.putExtra("showcancel", true);
                        startActivityWithAnim(intent);
                        //finish();
                    }
                }
                toCloseProgress();
            }
        }
    };

    private Handler communityHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Frame frame = (Frame) msg.obj;
                String result = generalhelper.getSocketeStringResult(frame.strData);
                String[] results = result.split(",");
                if (frame.subCmd == 1) {
                    if (result.equals("0")) {
                        SystemUtils.setIsLogin(true);
                        SystemUtils.setIsCommunityLogin(true);
                        ZganLoginService.toGetServerData(28, 0, PhoneNum, communityHandler);
                        //获取联网令牌
                        ZganCommunityService.toGetServerData(43, PhoneNum, communityHandler);
                    } else {
//                        Intent intent = new Intent(Login.this, BindDevice.class);
//                        intent.putExtra("username", PhoneNum);
//                        intent.putExtra("pwd", et_pwd.getText().toString().trim());
//                        intent.putExtra("showcancel", true);
                        Intent intent = new Intent(Login.this, BindCommunity.class);
                        intent.putExtra("username", et_Phone.getText().toString());
                        intent.putExtra("pwd", et_pwd.getText().toString());
                        startActivityWithAnim(intent);
                        //startActivityWithAnim(intent);
                    }
                } else if (frame.subCmd == 43 && results[0].equals("0")) {
                    SystemUtils.setNetToken(results[1]);
                } else if (frame.subCmd == 28 && results[0].equals("0")) {
                    if (results.length == 2) {
                        PreferenceUtil.setSID(results[1]);
                        logined();
                    }
                    finish();
                }
            }
        }
    };

    private void logined() {
        SystemUtils.setIsLogin(true);
        Intent intent = getIntent();
        boolean forresult = false;
        toCloseProgress();
        Log.v(TAG, "logined");
        PreferenceUtil.setUserName(PhoneNum);
        PreferenceUtil.setPassWord(et_pwd.getText().toString());
        if (intent.hasExtra(SystemUtils.FORRESULT)) {
            forresult = intent.getBooleanExtra(SystemUtils.FORRESULT, false);
            setResult(resultCodes.LOGIN);
        }
        if (!forresult) {
            Intent returnintent = new Intent(Login.this, MainActivity.class);
            startActivityWithAnim(returnintent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(null);
    }
}
