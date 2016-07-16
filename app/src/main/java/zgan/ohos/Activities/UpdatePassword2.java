package zgan.ohos.Activities;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import zgan.ohos.R;
import zgan.ohos.services.login.ZganLoginService;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;
import zgan.ohos.utils.generalhelper;

public class UpdatePassword2 extends myBaseActivity {

    TextInputLayout tilpwd1,tilpwd2;
    EditText etpwd1,etpwd2;
    String code,phone;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_update_password2);
        Intent requestintent=getIntent();
        code=requestintent.getStringExtra("code");
        phone=requestintent.getStringExtra("phone");
        tilpwd1=(TextInputLayout)findViewById(R.id.til_pwd1);
        tilpwd2=(TextInputLayout)findViewById(R.id.til_pwd2);
        etpwd1=(EditText)findViewById(R.id.et_pwd1);
        etpwd2=(EditText)findViewById(R.id.et_pwd2);
    }

    @Override
    public void ViewClick(View v) {
        if (v.getId()==R.id.btn_ensure)
        {
            if (etpwd1.getText().toString().trim().equals(""))
            {
                tilpwd1.setError("不能为空");
                tilpwd1.setErrorEnabled(true);
            }
            else
            {
                if (etpwd2.getText().toString().trim().equals(""))
                {
                    tilpwd2.setError("不能为空");
                    tilpwd2.setErrorEnabled(true);
                }
                else if (!etpwd1.getText().toString().equals(etpwd2.getText().toString()))
                {
                    tilpwd1.setError("两次输入密码不同");
                    tilpwd2.setError("两次输入密码不同");
                    tilpwd1.setErrorEnabled(true);
                    tilpwd2.setErrorEnabled(true);
                }
                else
                {
                    tilpwd1.setErrorEnabled(false);
                    tilpwd2.setErrorEnabled(false);
                    //修改密码doupdate
                    ZganLoginService.toGetServerData(5, 0, String.format("%s\t%s\t%s\t%s",phone,0,etpwd1.getText().toString(),code), handler);
                }

            }
        }
    }

    Handler handler=new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Frame frame = (Frame) msg.obj;
                String ret = generalhelper.getSocketeStringResult(frame.strData);
                Log.i(TAG, frame.subCmd + "  " + ret);
                if (frame.subCmd == 8) {
                    if (ret.equals("0")) {
                        generalhelper.ToastShow(UpdatePassword2.this,"密码修改成功~");
                        if (SystemUtils.getIsLogin())
                        {
                            PreferenceUtil.setPassWord(etpwd1.getText().toString());
                            Intent intent=new Intent(UpdatePassword2.this,MainActivity.class);
                            startActivityWithAnim(intent);
                        }
                        finish();
                    }
                    else
                    {

                        generalhelper.ToastShow(UpdatePassword2.this,"修改密码失败~");
                    }
                }
            }
        }
    };
}
