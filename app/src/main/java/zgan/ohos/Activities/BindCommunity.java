package zgan.ohos.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import zgan.ohos.Models.NewUserComm;
import zgan.ohos.R;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.services.login.ZganLoginService;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.NetUtils;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;
import zgan.ohos.utils.generalhelper;
import zgan.ohos.utils.resultCodes;

public class BindCommunity extends myBaseActivity implements View.OnClickListener {

    View llselectComm, llselectDetail;
    TextView txtcomm, txtdetail;
    Button btn_bind;
    String Phone, Pwd;
    NewUserComm Comm;
    String CommId;
    String SID;
    //String SMScode;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case resultCodes.BINDDEVICE:
                Comm = (NewUserComm) data.getSerializableExtra("UserComm");
                if (Comm != null) {
                    txtcomm.setText(Comm.getCommName());
                    ZganCommunityService.CommunityIp = NetUtils.getIp(Comm.getCommIp());
                    ZganCommunityService.CommunityPort = Comm.getCommPort();
                    llselectDetail.setVisibility(View.VISIBLE);
                }
                break;
            case resultCodes.COMMSELECTED:
                CommId = data.getStringExtra("commid");
                txtdetail.setText(data.getStringExtra("commname"));
                btn_bind.setEnabled(true);
                break;
            default:
                break;
        }
    }

    Handler handlerL = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    Frame f = (Frame) msg.obj;
                    String result = generalhelper.getSocketeStringResult(f.strData);
                    String[] results = result.split(",");
                    System.out.print(f.strData);
                    if (f.subCmd == 1) {
                        if (results[0].equals("0")) {
                            //确认了登陆云服务器成功后绑定室内机SID
                            PreferenceUtil.setUserName(Phone);
                            PreferenceUtil.setPassWord(Pwd);
                            SystemUtils.setIsLogin(true);
                            ZganLoginService.toGetServerData(4, 0, String.format("%s\t%s", Phone, SID), handler);
                            finish();
                        }
                    }
                    break;
            }
        }
    };
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    Frame f = (Frame) msg.obj;
                    String result = generalhelper.getSocketeStringResult(f.strData);
                    String[] results = result.split(",");
                    System.out.print(f.strData);
                    if (f.subCmd == 1) {
                        if (results[0].equals("0")) {
                            SystemUtils.setIsCommunityLogin(true);
                            generalhelper.ToastShow(BindCommunity.this, "绑定成功");
                            //确定了小区服务器登陆成功后跳转到MainActivity
                            startActivityWithAnim(new Intent(BindCommunity.this, MainActivity.class));
                            finish();
                        }
                    } else if (f.subCmd == 27) {
                        if (results[0].equals("0") && results.length == 2) {
                            SID=results[1];
                            //获取到SID后确认登陆了云服务器
                            ZganLoginService.toUserLogin(Phone, Pwd, "", handlerL);
                        } else {
                            generalhelper.ToastShow(BindCommunity.this, "绑定失败~");
                        }
                    }
                    if (f.subCmd == 4) {
                        if (result.equals("0")) {
                            PreferenceUtil.setSID(SID);
                            //室内机绑定成功后再确认登陆小区服务器
                            ZganCommunityService.toUserLogin(Phone,Pwd,handler);
                        } else {
                            generalhelper.ToastShow(BindCommunity.this, "绑定室内机失败");
                            toCloseProgress();
                        }
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_bind_community);
        Intent requestIntent = getIntent();
        if (requestIntent.hasExtra("username")) {
            Phone = requestIntent.getStringExtra("username");
            Pwd = requestIntent.getStringExtra("pwd");
            //SMScode=requestIntent.getStringExtra("code");
        }
        llselectComm = findViewById(R.id.llselectComm);
        llselectDetail = findViewById(R.id.llselectDetail);
        btn_bind = (Button) findViewById(R.id.btn_bind);
        llselectComm.setOnClickListener(this);
        llselectDetail.setOnClickListener(this);
        btn_bind.setOnClickListener(this);
        txtcomm = (TextView) findViewById(R.id.txtcomm);
        txtdetail = (TextView) findViewById(R.id.txtdetail);
        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void ViewClick(View v) {
        Intent intent;
        btn_bind.setEnabled(false);
        switch (v.getId()) {
            case R.id.llselectComm:
                intent = new Intent(BindCommunity.this, SortCommunityList.class);
                intent.putExtra("username", Phone);
                startActivityWithAnimForResult(intent, resultCodes.BINDDEVICE);
                break;
            case R.id.llselectDetail:
                intent = new Intent(BindCommunity.this, UserCommSelect.class);
                intent.putExtra("fcommid", "0");
                intent.putExtra("username", Phone);
                startActivityWithAnimForResult(intent, resultCodes.COMMSELECTED);
                break;
            case R.id.btn_bind:
               /* 手机->服务器: 账号\t楼层信息
                （楼层信息：幢号,单元号,层号,房间号,设备号)
                返回: 状态码\t室内机SID*/
                Log.i(TAG, CommId);
                if (CommId.indexOf(",") > 0) {
                    String comm = CommId.substring(CommId.indexOf(",") + 1);
                    if (comm.length() > 0) {
                        //获取SID
                        ZganCommunityService.toGetServerData(27, Phone + "\t" + comm + ",0", handler);
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        ViewClick(v);
    }
}
