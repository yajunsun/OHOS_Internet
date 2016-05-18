package zgan.ohos.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import zgan.ohos.Dals.MessageDal;
import zgan.ohos.R;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.services.login.ZganLoginService;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.PreferenceUtil;

public class MessageDetailActivity extends myBaseActivity {

    int msg_id;
    TextView txt_msg_type, txt_pub_time, txt_title, txt_content;
    zgan.ohos.Models.Message message;
    MessageDal msgdal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        msg_id = intent.getIntExtra("msg_id", 0);
        msgdal = new MessageDal();
        toSetProgressText(getResources().getString(R.string.loading));
        toShowProgress();
        ZganCommunityService.toGetServerData(25, 0,2, String.format("%s\t%s", PreferenceUtil.getUserName(), msg_id), handler);
//        message=(zgan.ohos.Models.Message)intent.getSerializableExtra("message");
//        bindData();
    }

    @Override
    protected void initView() {
        setContentView(R.layout.lo_message_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        txt_msg_type = (TextView) findViewById(R.id.txt_msg_type);
        txt_pub_time = (TextView) findViewById(R.id.txt_pub_time);
        txt_title = (TextView) findViewById(R.id.txt_title);
        txt_content = (TextView) findViewById(R.id.txt_content);
        txt_content.setVisibility(View.VISIBLE);
        txt_title.setMaxLines(20);
        txt_content.setMaxLines(1000);
    }

    @Override
    public void ViewClick(View v) {

    }

    private void bindData() {
        txt_msg_type.setText(message.getMsgType());
        txt_pub_time.setText(message.getMsgPublishTime());
        txt_title.setText(message.getMsgTitle());
        txt_content.setText(message.getMsgContent());
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    Frame f = (Frame) msg.obj;
                    String result = f.strData;
                    if (f.subCmd == 25) {
                        String[] results = result.split("\t");
                        if (results.length == 2 && results[0].equals("0")) {
                            try {
                                message = msgdal.GetMessage(results[1]);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        bindData();
                                        toCloseProgress();
                                    }
                                });
                            } catch (Exception ex) {
                                android.os.Message msg1 = new android.os.Message();
                                msg1.what = 0;
                                msg1.obj = ex.getMessage();
                                handler.sendMessage(msg1);
                            }
                        }
                    }
                    break;
            }
        }
    };
}
