package zgan.ohos.Activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import zgan.ohos.Dals.PartinDal;
import zgan.ohos.R;
import zgan.ohos.utils.generalhelper;
import zgan.ohos.utils.resultCodes;
import zgan.ohos.utils.viewhelper;

/**
 * Created by yajunsun on 2015/11/26.
 */
public class EventPreCheck extends myBaseActivity {
    EditText input_phone, input_address;
    TextView txt_error_msg;
    int eventId;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventId = getIntent().getIntExtra("eventid", 0);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void initView() {
        setContentView(R.layout.lo_event_precheck);
        input_phone = (EditText) findViewById(R.id.input_phone);
        input_address = (EditText) findViewById(R.id.input_address);
        txt_error_msg = (TextView) findViewById(R.id.txt_error_msg);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        input_phone.setText(generalhelper.getmyPhoneNumber(EventPreCheck.this));
        View back=findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void ViewClick(View v) {
        String phone = input_phone.getText().toString().trim();
        String address = input_address.getText().toString().trim();
        if (phone.length() == 0) {
            viewhelper.tada(input_phone, 1f).start();
            return;
        }
        if (address.length() == 0) {
            viewhelper.tada(input_address, 1f).start();
            return;
        }
        try {
            String result = new PartinDal().PartIn(eventId, phone, address);
            if (result.equals(generalhelper.returnsta.success)) {
                generalhelper.ToastShow(this, "预定成功");
                setResult(resultCodes.EVENT_CHECK);
                finish();
            } else {
                txt_error_msg.setText(result);
                txt_error_msg.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            generalhelper.ToastShow(this, "程序异常:" + e.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}