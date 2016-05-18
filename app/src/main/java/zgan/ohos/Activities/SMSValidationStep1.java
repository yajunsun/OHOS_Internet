package zgan.ohos.Activities;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.EditText;

import zgan.ohos.R;
import zgan.ohos.utils.PreferenceUtil;

public class SMSValidationStep1 extends myBaseActivity {

    TextInputLayout til_phone;
    EditText et_phone;
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
        til_phone=(TextInputLayout)findViewById(R.id.til_phone);
        et_phone=(EditText)findViewById(R.id.et_phone);
    }

    @Override
    public void ViewClick(View v) {
       if (v.getId()==R.id.btn_ensure)
       {
           if (et_phone.getText().toString().trim().equals(""))
           {
               til_phone.setError("电话号码不能为空");
               til_phone.setErrorEnabled(true);
           }
//           else if (et_phone.getText().toString().trim().length()!=11)
//           {
//               til_phone.setError("电话号码格式不正确");
//               til_phone.setErrorEnabled(true);
//           }
           else if (!et_phone.getText().toString().trim().equals(PreferenceUtil.getUserName()))
           {
               til_phone.setError("电话号码与注册号码不同");
               til_phone.setErrorEnabled(true);
           }
           else {
               til_phone.setErrorEnabled(false);
               Intent intent=new Intent(this,SMSValidationStep2.class);
               intent.putExtra("phone",et_phone.getText().toString());
               startActivityWithAnim(intent);
           }
       }
    }
}
