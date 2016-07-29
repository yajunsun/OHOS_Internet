
package zgan.ohos.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import zgan.ohos.Dals.NewUserCommDal;
import zgan.ohos.Dals.UserCommDal;
import zgan.ohos.Models.NewUserComm;
import zgan.ohos.Models.UserComm;
import zgan.ohos.R;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.generalhelper;
import zgan.ohos.utils.resultCodes;

public class UserCommSelect extends myBaseActivity {

    Toolbar toolbar;
    ListView lst_ucselect;
    private String FCommId = "0", Phone, FCommName = "";
    List<NewUserComm> list;
    private String fComms[] = new String[]{};
    ;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == resultCodes.COMMSELECTED) {
            if (data != null && data.hasExtra("commid")) {
                setResult(resultCodes.COMMSELECTED, data);
                finish();
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void initView() {
        setContentView(R.layout.lo_user_comm_select);
        Intent requestIntent = getIntent();
        FCommId = requestIntent.getStringExtra("fcommid");
        if (requestIntent.hasExtra("fcommname"))
            FCommName = requestIntent.getStringExtra("fcommname");
        fComms = FCommId.split(",");
        Phone = requestIntent.getStringExtra("username");
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        lst_ucselect = (ListView) findViewById(R.id.lst_ucselect);
        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //getData();
        loadData();
    }

    @Override
    public void ViewClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
        }
    }

    private void loadData() {
        ZganCommunityService.toGetServerData(8, String.format("%s\t%s", Phone, fComms[fComms.length - 1]), handler);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    Frame f = (Frame) msg.obj;
                    //String result = generalhelper.getSocketeStringResult(f.strData);
                    String[] results = f.strData.split("\t");
                    System.out.print(f.strData);
                    if (f.subCmd == 8) {
                        if (results[0].equals("0")) {
                            //list=
                            if (results.length > 2) {
                                list = new NewUserCommDal().getCommDetailListfromString(results[2]);
                                bindData();
                            } else {
                                Intent intent = new Intent();
                                intent.putExtra("commid", FCommId);
                                intent.putExtra("commname", FCommName);
                                setResult(resultCodes.COMMSELECTED, intent);
                                finish();
                            }
                        }
                        break;
                    }
            }
        }
    };

    private void bindData() {
        try {
            lst_ucselect.setAdapter(new UserCommListAdapter());
            lst_ucselect.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    NewUserComm uc = list.get(position);
//                        if (uc.getHasChild() == 1) {
                    Intent intent = new Intent(UserCommSelect.this, UserCommSelect.class);
                    intent.putExtra("fcommid", FCommId + "," + uc.getCommId());
                    intent.putExtra("fcommname", FCommName + uc.getCommName().trim());
                    intent.putExtra("username", Phone);
                    startActivityWithAnimForResult(intent, resultCodes.COMMSELECTED);
//                        } else {
//                            generalhelper.ToastShow(UserCommSelect.this, "当前选中户号:" + uc.getComm_Id() + "  " + uc.getComm_Name());
//                            //Intent intent = new Intent(UserCommSelect.this, UserHostNameAndPhone.class);
////                        intent.putExtra("commid", uc.getComm_Id());
////                        intent.putExtra("commname", uc.getComm_Name());
//                            Intent intent = new Intent();
//                            intent.putExtra("commid", uc.getComm_Id());
//                            intent.putExtra("commname", uc.getComm_Name());
//                            setResult(resultCodes.COMMSELECTED, intent);
//                            finish();
//                            //startActivity(intent);
//                        }
                }
            });
        } catch (Exception e) {
            generalhelper.ToastShow(UserCommSelect.this, e.getMessage());
        }
    }

    class UserCommListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null, false);
            }
            TextView text = (TextView) convertView.findViewById(android.R.id.text1);
            text.setText(list.get(position).getCommName());
            return convertView;
        }
    }
}
