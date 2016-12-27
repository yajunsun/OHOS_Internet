package zgan.ohos.Activities;

import android.os.Handler;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;

import zgan.ohos.Contracts.UpdateCartListner;
import zgan.ohos.Dals.ShippingAddressDal;
import zgan.ohos.Models.Message;
import zgan.ohos.Models.ShippingAddressModel;
import zgan.ohos.R;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.generalhelper;
import zgan.ohos.utils.resultCodes;

/**
 * Created by yajunsun on 2016/12/27.
 */
public class AddressEdit extends myBaseActivity implements View.OnClickListener {
    TextView btn_save, select_major;
    EditText input_username, input_userphone, input_detail;
    ToggleButton tg_isuse;
    ShippingAddressModel shippingAddress;
    boolean isUse = true;
    boolean isEdit = false;
    ShippingAddressDal dal;

    @Override
    public void initView() {


        setContentView(R.layout.activity_edit_address);
        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btn_save = (TextView) findViewById(R.id.btn_save);
        select_major = (TextView) findViewById(R.id.select_major);
        input_username = (EditText) findViewById(R.id.input_username);
        input_userphone = (EditText) findViewById(R.id.input_userphone);
        input_detail = (EditText) findViewById(R.id.input_detail);
        tg_isuse = (ToggleButton) findViewById(R.id.tg_isuse);
        dal = new ShippingAddressDal();
        if (getIntent().hasExtra("addressmodel")) {
            shippingAddress = (ShippingAddressModel) getIntent().getSerializableExtra("addressmodel");
            isEdit = true;
        } else {
            shippingAddress = new ShippingAddressModel();
        }
        tg_isuse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //if(b) isUse=true;
            }
        });

    }

    @Override
    public void ViewClick(View v) {

    }


    UpdateCartListner listner = new UpdateCartListner() {
        @Override
        public void onFailure() {
            generalhelper.ToastShow(AddressEdit.this, "ÍøÂçÁ¬½Ó´íÎó");
        }

        @Override
        public void onResponse(String response) {
            android.os.Message msg = handler.obtainMessage();
            msg.what = 1;
            msg.obj = response;
            msg.sendToTarget();
        }
    };
    Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);


            super.handleMessage(msg);
            if (msg.what == 1) {
                String data = msg.obj.toString();
                if (!data.isEmpty()) {
                    try {
                        String result = new JSONObject(data).get("result").toString();
                        String errmsg = new JSONObject(data).get("msg").toString();
                        //获取数据并绑定数据
                        if (result.equals("0")) {
                            generalhelper.ToastShow(AddressEdit.this, "保存成功");
                            setResult(resultCodes.SHIPPINGADDRESS);
                            finish();
                        } else if (!errmsg.isEmpty()) {
                            generalhelper.ToastShow(AddressEdit.this, "服务器错误:" + errmsg);
                            if (errmsg.contains("时间戳")) {
                                //ZganCommunityService.toGetServerData(43, PreferenceUtil.getUserName(), tokenHandler);
                            }
                        }
                    } catch (JSONException jse) {

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    };


    private String invalidateInput() {
        String result = "";
        if (input_username.getText().toString().trim() == "") {
            result = "收货人不能为空";
        }
        if (input_userphone.getText().toString().trim() == "") {
            result = "联系电话不能为空";
        }
        if (select_major.getText().toString().trim() == "") {
            result = "所在地区不能为空";
        }
        if (input_detail.getText().toString().trim() == "") {
            result = "详细地址不能为空";
        }
        if (input_detail.getText().toString().trim().length() < 5) {
            result = "详细地址不能少于5个字";
        }
        if (input_detail.getText().toString().trim().length() > 200) {
            result = "详细地址不能大于200个字";
        }
        return result;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save:
                String invalidateResult = invalidateInput();
                if (invalidateResult != "") {
                    generalhelper.ToastShow(AddressEdit.this, invalidateResult);
                    return;
                }
                shippingAddress.setUserName(input_username.getText().toString().trim());
                shippingAddress.setUserPhone(input_userphone.getText().toString().trim());
                shippingAddress.setUserAdress(input_detail.getText().toString().trim());
                shippingAddress.setIsUse(isUse ? 1 : 0);
                if (isEdit)
                    dal.updateAddress(shippingAddress, listner);
                else
                    dal.addAddress(shippingAddress, listner);

                break;
            case R.id.select_major:
                break;
        }
    }
}
