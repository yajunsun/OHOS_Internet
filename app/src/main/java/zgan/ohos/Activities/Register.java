package zgan.ohos.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;

import zgan.ohos.R;
import zgan.ohos.services.login.ZganLoginService;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;
import zgan.ohos.utils.generalhelper;
import zgan.ohos.utils.resultCodes;

public class Register extends myBaseActivity {
    TextInputLayout til_Phone;
    TextInputLayout til_pwd;
    TextInputLayout til_repwd;
    EditText et_Phone;
    EditText et_pwd;
    EditText et_repwd;
    RadioButton rb_access;
    //int communityId = 4;
    String Phone;
    String Pwd;
    String rePwd;
    Button btnRegister;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void initView() {
        setContentView(R.layout.lo_activity_register);
        Intent thisIntent = getIntent();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //communityId = thisIntent.getIntExtra("communityid", 4);
        til_Phone = (TextInputLayout) findViewById(R.id.til_phone);
        til_pwd = (TextInputLayout) findViewById(R.id.til_pwd);
        til_repwd = (TextInputLayout) findViewById(R.id.til_repwd);
        et_Phone = (EditText) findViewById(R.id.et_Phone);
        et_pwd = (EditText) findViewById(R.id.et_pwd);
        et_repwd = (EditText) findViewById(R.id.et_repwd);
        rb_access = (RadioButton) findViewById(R.id.rb_access);
        btnRegister = (Button) findViewById(R.id.btn_register);
        btnRegister.setEnabled(rb_access.isChecked());
        rb_access.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                btnRegister.setEnabled(isChecked);
            }
        });
        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void ViewClick(View view) {
        switch (view.getId()) {
            case R.id.btn_register:
                boolean CheckName = false;
                boolean CheckPwd = false;
                Phone = et_Phone.getText().toString().trim();
                Pwd = et_pwd.getText().toString().trim();
                rePwd = et_repwd.getText().toString().trim();
                if (Phone.length() == 0) {
                    til_Phone.setError("电话号码不能为空");
                    til_Phone.setErrorEnabled(true);
                } else if (Phone.toCharArray().length != 11) {
                    til_Phone.setError("电话号码填写错误");
                    til_Phone.setErrorEnabled(true);
                } else {
                    CheckName = true;
                    til_Phone.setErrorEnabled(false);
                }
                if (Pwd.length() == 0) {
                    til_pwd.setError("密码不能为空");
                    til_pwd.setErrorEnabled(true);
                } else if (Pwd.length() > 20) {
                    til_pwd.setError("密码长度不能超过20位");
                    til_pwd.setErrorEnabled(true);
                } else {
                    CheckPwd = true;
                    til_pwd.setErrorEnabled(false);
                }
                if (rePwd.length() == 0) {
                    CheckPwd = false;
                    til_repwd.setError("重复密码不能为空");
                    til_repwd.setErrorEnabled(true);
                } else if (!rePwd.equals(Pwd)) {
                    CheckPwd = false;
                    til_repwd.setError("两次输入密码不一样");
                    til_repwd.setErrorEnabled(true);
                } else {
                    CheckPwd = true;
                    til_repwd.setErrorEnabled(false);
                }
                try {
                    if (CheckName && CheckPwd) {
                        toSetProgressText("请稍后...");
                        toShowProgress();
                        //ZganLoginService.toGetServerData(2, communityId + "\t" + Phone + "\t" + Pwd + "\t0", handler);
                        ZganLoginService.toGetServerData(2, Phone + "\t" + Pwd + "\t0", handler);
                    }
                } catch (Exception ex) {
                    generalhelper.ToastShow(Register.this, ex.getMessage());
                }
                break;
            case R.id.back:
                finish();
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
                if (frame.subCmd == 2 && result.equals("0")) {
                    setResult(resultCodes.LOGIN);
                    SystemUtils.setIsLogin(true);
                    PreferenceUtil.setUserName(Phone);
                    PreferenceUtil.setPassWord(Pwd);
                    //PreferenceUtil.setCommunityId(String.valueOf(communityId));
                    Log.v(TAG, "注册成功");
                    Intent intent = new Intent(Register.this, BindDevice.class);
                    intent.putExtra("showcancel", true);
                    startActivityWithAnim(intent);
                    toCloseProgress();
                    finish();
                } else if (frame.subCmd == 2 && result.equals("24")) {
                    generalhelper.ToastShow(Register.this, "该号码已被注册");
                    toCloseProgress();
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
