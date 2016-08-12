package zgan.ohos.Activities;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Timer;
import java.util.TimerTask;

import zgan.ohos.R;
import zgan.ohos.services.login.ZganLoginService;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.generalhelper;

public class SMSValidationStep2 extends myBaseActivity {


    Button btnredo;
    EditText etcode;
    String phone,pwd;
    int mTimeout = 60;
    Timer mTimer;
    final static int TIME_TICK = 9;
    boolean mIsregister=false;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_smsvalidation_step2);
        btnredo = (Button) findViewById(R.id.btn_redo);
        etcode = (EditText) findViewById(R.id.et_code);
        Intent intent=getIntent();
        phone = intent.getStringExtra("phone");
        mIsregister=intent.getBooleanExtra("register",false);
        if(intent.hasExtra("pwd"))
        {
            pwd=intent.getStringExtra("pwd");
        }
        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        iniTimer();
    }

    private void iniTimer() {
        mTimeout = 60;
        btnredo.setEnabled(false);
        mTimer = new Timer(true);
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mTimeout--;
                handler.sendEmptyMessage(TIME_TICK);
            }
        }, 0, 1000);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TIME_TICK:
                    if (mTimeout == 0) {
                        btnredo.setEnabled(true);
                        btnredo.setText("重发");
                        break;
                    } else if (mTimeout > 0)
                        btnredo.setText(mTimeout + "秒");
                    else
                        mTimer.cancel();
                    break;
                case 1:
                    Frame frame = (Frame) msg.obj;
                    String ret = generalhelper.getSocketeStringResult(frame.strData);
                    Log.i(TAG, frame.subCmd + "  " + ret);
                    if (frame.subCmd == 8) {
                        if (ret.equals("0")) {
                            iniTimer();
                        } else {
                            generalhelper.ToastShow(SMSValidationStep2.this, "短信发送失败~");
                        }
                    }
                     if (frame.subCmd == 2 && ret.equals("0")) {
                        Intent intent=new Intent(SMSValidationStep2.this,BindCommunity.class);
                         intent.putExtra("username",phone);
                         intent.putExtra("pwd",pwd);
                         startActivityWithAnim(intent);
                         finish();
//
                    } else if (frame.subCmd == 2 && ret.equals("24")) {
                        generalhelper.ToastShow(SMSValidationStep2.this, "该号码已被注册");
                        toCloseProgress();
                         //Intent intent=new Intent(SMSValidationStep2.this,Register.class);
                         Intent intent=new Intent(SMSValidationStep2.this,Login.class);
                         intent.putExtra("phone",phone);
                         startActivityWithAnim(intent);
                         finish();
                    }
                    break;
            }
        }
    };

    @Override
    public void ViewClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ensure:
                if (etcode.getText().toString().trim().equals("")) {
                    generalhelper.ToastShow(this, "短信验证码不能为空");
                    return;
                } else {
                    //判断验证码是否输入正确
                    if(mIsregister)
                    {
                        toSetProgressText("请稍后...");
                        toShowProgress();
                        ZganLoginService.toGetServerData(2, phone + "\t" + pwd + "\t"+etcode.getText().toString().trim(), handler);
                        //intent.putExtra("code", etcode.getText().toString().trim());
                    }
                    else {
                        Intent  intent= new Intent(this, UpdatePassword2.class);
                        intent.putExtra("code", etcode.getText().toString().trim());
                        intent.putExtra("phone", phone);
                        startActivityWithAnim(intent);
                        finish();
                    }

                }
                break;
            case R.id.btn_redo:
                ZganLoginService.toGetServerData(8, 0, phone, handler);
                break;
        }
    }
}
