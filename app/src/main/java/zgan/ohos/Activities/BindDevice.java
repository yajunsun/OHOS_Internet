package zgan.ohos.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import zgan.ohos.R;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.services.login.ZganLoginService;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.NetUtils;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;
import zgan.ohos.utils.generalhelper;

/**
 * create by yajunsun
 *
 * 绑定室内机操作页面
 * */
public class BindDevice extends myBaseActivity {

    TextInputLayout til_input;
    EditText et_input;
    String SID;
    Button btn_cancel;
    ViewStub bindedshow;
    ViewStub unbindshow;
    boolean showcancel = false;
    TextView txtsid;
    String Phone = "", Pwd = "";

    @Override
    protected void initView() {
        Intent intent = getIntent();
        showcancel = intent.getBooleanExtra("showcancel", false);
        //获取从登陆或注册页面传过来的用户名和密码
        if (intent.hasExtra("username")) {
            Phone = intent.getStringExtra("username");
            Pwd = intent.getStringExtra("pwd");

            setContentView(R.layout.activity_bind_device);
            //if (PreferenceUtil.getSID().equals("0")) {
            unbindshow = (ViewStub) findViewById(R.id.unbindshow);
            unbindshow.inflate();
            til_input = (TextInputLayout) findViewById(R.id.til_input);
            et_input = (EditText) findViewById(R.id.et_input);
            btn_cancel = (Button) findViewById(R.id.btn_cancel);
            //btn_cancel.setVisibility(showcancel ? View.VISIBLE : View.GONE);
//            } else {
//                bindedshow = (ViewStub) findViewById(R.id.bindedshow);
//                bindedshow.inflate();
//                txtsid = (TextView) findViewById(R.id.txt_deviceSid);
//                txtsid.setText(PreferenceUtil.getSID());
//            }

            View back = findViewById(R.id.back);
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    @Override
    public void ViewClick(View v) {
        switch (v.getId()) {
            case R.id.btn_bind:
                SID = et_input.getText().toString().trim();
                if (SID.equals("")) {
                    til_input.setError("SID不能为空");
                    til_input.setErrorEnabled(true);
                } else if (SID.length() > 40) {
                    til_input.setError("SID不正确");
                    til_input.setErrorEnabled(true);
                } else {
                    til_input.setErrorEnabled(false);
                    toSetProgressText("请稍等，正在绑定中");
                    toShowProgress();
                    ZganLoginService.toGetServerData(4, 0, String.format("%s\t%s", Phone, SID), handler);
                }
                break;
            case R.id.btn_cancel:
                Intent intent = new Intent(BindDevice.this, MainActivity.class);
                startActivityWithAnim(intent);
                finish();
                break;
        }

    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Frame f = (Frame) msg.obj;
                String result = generalhelper.getSocketeStringResult(f.strData);
                String[] results = result.split(",");
                System.out.print(f.strData);
                if (f.subCmd == 1) {
                    if (results[0].equals("0")) {
                        SystemUtils.setIsLogin(true);
                        SystemUtils.setIsCommunityLogin(true);
                        PreferenceUtil.setUserName(Phone);
                        PreferenceUtil.setPassWord(Pwd);
                        generalhelper.ToastShow(BindDevice.this, "绑定室内机成功");
                        startActivityWithAnim(new Intent(BindDevice.this, MainActivity.class));
                        finish();
                    }
                }
                if (f.subCmd == 4) {
                    if (result.equals("0")) {
                        ZganLoginService.toGetServerData(3, 0, String.format("%s", Phone), handler);
                        PreferenceUtil.setSID(SID);
                    } else {
                        generalhelper.ToastShow(BindDevice.this, "绑定室内机失败");
                        toCloseProgress();
                    }
                }
                if (f.subCmd == 3) {
                    if (results.length == 3 && results[0].equals("0")) {
                        PreferenceUtil.setCommunityIP(NetUtils.getIp(results[1]));
                        PreferenceUtil.setCommunityPORT(Integer.parseInt(results[2]));
                        ZganCommunityService.CommunityIp = NetUtils.getIp(results[1]);
                        ZganCommunityService.CommunityPort = Integer.parseInt(results[2]);
                        ZganCommunityService.toUserLogin(Phone, Pwd, handler);//.toGetServerData(1,"",handler);
                        //ZganCommunityService.startCommunityService(BindDevice.this);
                        //}
                    }
                }
            }
        }
    };
}
