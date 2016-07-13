package zgan.ohos.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import zgan.ohos.R;
import zgan.ohos.services.login.ZganLoginService;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.generalhelper;

/**
 * create by yajunsun
 *
 * 密码修改界面
 * */
public class UpdatePassword extends myBaseActivity {

    Toolbar toolbar;
    TextInputLayout til_oldpwd, til_newpwd;
    EditText et_oldpwd, et_newpwd;
    String oldpwd,newpwd;


    @Override
    protected void initView() {
        setContentView(R.layout.lo_update_password);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        til_oldpwd = (TextInputLayout) findViewById(R.id.til_oldpwd);
        til_newpwd = (TextInputLayout) findViewById(R.id.til_newpwd);
        et_oldpwd = (EditText) findViewById(R.id.et_oldpwd);
        et_newpwd = (EditText) findViewById(R.id.et_newpwd);
        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    Frame f = (Frame) msg.obj;
                    String result = generalhelper.getSocketeStringResult(f.strData);
                    if (f.subCmd == 27) {
                        if (result.equals("0")) {
                            PreferenceUtil.setPassWord(newpwd);
                            generalhelper.ToastShow(UpdatePassword.this, "更新密码成功~");

                            postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            }, 500);
                        }
                    }
                    break;
            }
        }
    };

    @Override
    public void ViewClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ensure:
                 oldpwd=et_oldpwd.getText().toString();
                 newpwd=et_newpwd.getText().toString();
                if (oldpwd.equals("")) {
                    til_oldpwd.setError("旧密码不能为空");
                    til_oldpwd.setErrorEnabled(true);
                }
                else if(newpwd.equals(""))
                {
                    til_newpwd.setError("新密码不能为空");
                    til_newpwd.setErrorEnabled(true);
                }
                else if (!oldpwd.equals(PreferenceUtil.getPassWord()))
                {
                    til_oldpwd.setError("旧密码输入错误");
                    til_oldpwd.setErrorEnabled(true);
                }
                else
                {
                    til_oldpwd.setErrorEnabled(false);
                    til_newpwd.setErrorEnabled(false);
                    ZganLoginService.toGetServerData(27, 0, String.format("%s\t%s\t%s", PreferenceUtil.getUserName(), newpwd, 0), handler);
                }

                break;
            case R.id.btn_forgetpwd:
                Intent intent=new Intent(this,SMSValidationStep1.class);
                startActivityWithAnim(intent);
                break;
            case R.id.back:
                finish();
                break;
        }
    }
}
