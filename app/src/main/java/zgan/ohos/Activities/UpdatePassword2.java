package zgan.ohos.Activities;

import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.EditText;

import zgan.ohos.R;
import zgan.ohos.utils.generalhelper;

public class UpdatePassword2 extends myBaseActivity {

    TextInputLayout tilpwd1,tilpwd2;
    EditText etpwd1,etpwd2;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_update_password2);
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
                    generalhelper.ToastShow(this,"密码修改成功");
                    finish();
                }

            }
        }
    }
}
