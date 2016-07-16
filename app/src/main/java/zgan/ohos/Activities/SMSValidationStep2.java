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
    String phone;
    int mTimeout = 60;
    Timer mTimer;
    final static int TIME_TICK = 9;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_smsvalidation_step2);
        btnredo = (Button) findViewById(R.id.btn_redo);
        etcode = (EditText) findViewById(R.id.et_code);
        phone = getIntent().getStringExtra("phone");
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
                    Intent intent = new Intent(this, UpdatePassword2.class);
                    intent.putExtra("code", etcode.getText().toString().trim());
                    intent.putExtra("phone", phone);
                    startActivityWithAnim(intent);
                    finish();
                }
                break;
            case R.id.btn_redo:
                ZganLoginService.toGetServerData(8, 0, phone, handler);
                break;
        }
    }
}
