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
import zgan.ohos.utils.generalhelper;

public class SMSValidationStep1 extends myBaseActivity {

    EditText et_phone;
    boolean mIsregiter=false;
    String mPhone,mPwd;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_smsvalidation_step1);
        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        et_phone = (EditText) findViewById(R.id.et_phone);
        mIsregiter=getIntent().getBooleanExtra("register",false);
        //获取传递过来的参数
        Intent intent=getIntent();
        if (intent.hasExtra("phone"))
        {
            mPhone=intent.getStringExtra("phone");
            et_phone.setText(mPhone);
            et_phone.setEnabled(false);
            mPwd=intent.getStringExtra("pwd");

        }
    }

    @Override
    public void ViewClick(View v) {
        if (v.getId() == R.id.btn_ensure) {
            if (et_phone.getText().toString().trim().equals("")) {
                generalhelper.ToastShow(this, "电话号码不能为空~");
            } else if (et_phone.getText().toString().trim().length() != 11) {
                generalhelper.ToastShow(this, "电话号码格式出错误~");
            }
//            else if (!et_phone.getText().toString().trim().equals(PreferenceUtil.getUserName())) {
//                til_phone.setError("电话号码与注册号码不同");
//                til_phone.setErrorEnabled(true);
//            }
            else {
                ZganLoginService.toGetServerData(8, 0, et_phone.getText().toString().trim(), handler);
            }
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    Frame frame = (Frame) msg.obj;
                    String ret = generalhelper.getSocketeStringResult(frame.strData);
                    Log.i(TAG, frame.subCmd + "  " + ret);
                    if (frame.subCmd == 8) {
                        if (ret.equals("0")) {
                            Intent intent = new Intent(SMSValidationStep1.this, SMSValidationStep2.class);
                            intent.putExtra("phone", et_phone.getText().toString());
                            if(mPwd!=null&&!mPwd.equals(""))
                                intent.putExtra("pwd",mPwd);
                            intent.putExtra("register",mIsregiter);
                            startActivityWithAnim(intent);
                            finish();
                        } else {
                            generalhelper.ToastShow(SMSValidationStep1.this, "短信发送失败~");
                        }
                    }
                    break;
            }
        }
    };
}
