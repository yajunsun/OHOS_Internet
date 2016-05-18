package zgan.ohos.Activities;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import zgan.ohos.R;
import zgan.ohos.utils.generalhelper;

public class SMSValidationStep2 extends myBaseActivity {

    TextView txtjishu, txtphone;
    TextInputLayout tilcode;
    EditText etcode;
    String phone;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_smsvalidation_step2);
        txtjishu = (TextView) findViewById(R.id.txt_jishu);
        txtphone = (TextView) findViewById(R.id.txt_phone);
        tilcode = (TextInputLayout) findViewById(R.id.til_code);
        etcode = (EditText) findViewById(R.id.et_code);
        phone = getIntent().getStringExtra("phone");
        txtphone.setText("我们已经发送验证短信至号码：+86 " + phone);
        txtjishu.setText("60秒后可以重新获取");
    }

    @Override
    public void ViewClick(View v) {
        if (v.getId() == R.id.btn_ensure) {
            if (etcode.getText().toString().trim().equals("")) {
                tilcode.setError("请输入验证码");
                tilcode.setErrorEnabled(true);
            } else {
                //判断验证码是否输入正确
                tilcode.setErrorEnabled(false);
                Intent intent = new Intent(this,UpdatePassword2.class);
                startActivityWithAnim(intent);
            }
        }
    }
}
