package zgan.ohos.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import zgan.ohos.Dals.LeaveMessageDal;
import zgan.ohos.Models.FuncPage;
import zgan.ohos.Models.LeaveMessage;
import zgan.ohos.R;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.services.login.ZganLoginService;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;
import zgan.ohos.utils.generalhelper;

public class LeaveMessages extends myBaseActivity {

    Toolbar toolbar;
    TextView txt_title;
    RecyclerView rvmsg;
    SwipeRefreshLayout refreshview;
    List<LeaveMessage> msglst;
    LeaveMessageDal leavemsgDal;
    int pageindex = 0;
    myAdapter adapter;
    LinearLayoutManager mLayoutManager;
    boolean isLoadingMore = false;
    Dialog addDialog;
    Date date;
    EditText et_input;
    Button btn_commit;
    Button btn_cancel;
    FuncPage funcPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toSetProgressText(getResources().getString(R.string.loading));
        toShowProgress();
        loadData();
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_leave_messages);
        funcPage=(FuncPage)getIntent().getSerializableExtra("func");
        leavemsgDal = new LeaveMessageDal();
        mLayoutManager = new LinearLayoutManager(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        txt_title = (TextView) findViewById(R.id.txt_title);
        txt_title.setText(funcPage.getview_title());
        rvmsg = (RecyclerView) findViewById(R.id.rv_msg);
        refreshview = (SwipeRefreshLayout) findViewById(R.id.refreshview);
        refreshview.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageindex = 0;
                isLoadingMore = false;
                loadData();
                //adapter.notifyDataSetChanged();
                //refreshview.setRefreshing(false);
            }
        });
        rvmsg.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    int lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
                    int totalItemCount = mLayoutManager.getItemCount();
                    //lastVisibleItem >= totalItemCount - 4 表示剩下4个item自动加载，各位自由选择
                    // dy>0 表示向下滑动
                    if (lastVisibleItem == totalItemCount - 1 && isLoadingMore == false) {
                        loadMoreData();//这里多线程也要手动控制isLoadingMore
                        isLoadingMore = true;
                    }
                }
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(LeaveMessages.this);
        View dialog = View.inflate(this, R.layout.lo_commit_message, null);
        et_input = (EditText) dialog.findViewById(R.id.et_input);
        btn_commit = (Button) dialog.findViewById(R.id.btn_commit);
        btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel);
        btn_commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = et_input.getText().toString().trim();
                if (input.equals("")) {
                    generalhelper.ToastShow(LeaveMessages.this, "留言不能为空~");
                    return;
                } else {
                    btn_commit.setEnabled(false);
                    PreferenceUtil.getSID();
                    //ZganCommunityService.toGetServerData(29, 0, String.format("%s\t%s\t%s", PreferenceUtil.getUserName(), PreferenceUtil.getSID(), input), handler);
                    ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), P_LEAVEMSG, String.format("@id=22,@account=%s,@q_type=%s,@q_content=\"%s\"",PreferenceUtil.getUserName(),funcPage.getpage_id(),input), "22"), handler);
                }
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_input.setText("");
                addDialog.dismiss();
            }
        });
        builder.setView(dialog);
        addDialog = builder.create();
        View add = findViewById(R.id.ll_add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ZganLoginService.toGetServerData(29,254,String.format(""),handler);
                addDialog.show();
            }
        });
    }

    public void loadMoreData() {
        try {
//            toSetProgressText(getResources().getString(R.string.loading));
//            toShowProgress();
            refreshview.setRefreshing(true);
            et_input.setText("");
            pageindex++;
            //ZganCommunityService.toGetServerData(31, 0, String.format("%s\t%d", PreferenceUtil.getUserName(), pageindex), handler);
            ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), funcPage.gettype_id(), String.format("@id=22,@page_id=%s,@account,@page=%s",funcPage.getpage_id(),PreferenceUtil.getUserName(), pageindex), "22"), handler);
        } catch (Exception ex) {
            generalhelper.ToastShow(this, ex.getMessage());
        }
    }

    public void loadData() {
        refreshview.setRefreshing(true);
        //isLoadingMore = false;
        //小区ID\t帐号\t消息类型ID\t开始时间\t结束时间
        //ZganCommunityService.toGetServerData(31, 0, String.format("%s\t%d", PreferenceUtil.getUserName(), pageindex), handler);
        ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), funcPage.gettype_id(), String.format("@id=22,@page_id=%s,@account=%s,@page=%s",funcPage.getpage_id(),PreferenceUtil.getUserName(),pageindex), "22"), handler);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    generalhelper.ToastShow(LeaveMessages.this, msg.obj);
                    break;
                case 1:
                    Frame f = (Frame) msg.obj;
                    //String result = f.strData;
                    Log.i(TAG, f.strData);
                    String[] results = f.strData.split("\t");
                    if (f.subCmd == 40) {
                        if (results[0].equals("0") && results[1].equals(P_LEAVEMSG)&&results.length>2) {
                            addDialog.dismiss();
                            loadData();
                            btn_commit.setEnabled(true);
                        }
                        else if ( results[0].equals("0") && results[1].equals(funcPage.gettype_id())&&results.length>2) {
                            try {
                                //String xmlstr = results[1].substring(results[1].indexOf("<li>"), results[1].length());
                                if (pageindex == 0) {
                                    msglst = new ArrayList<>();
                                }
                                if (f.platform != 0) {
                                    //addCache("31" + String.format("%s\t%d", PreferenceUtil.getUserName(), pageindex), f.strData);
                                    addCache("40"+ String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), funcPage.gettype_id(), String.format("@id=22,@page_id=%s,@account,@page=%s",funcPage.getpage_id(),PreferenceUtil.getUserName(),pageindex), "22"),f.strData);
                                }
                                List<LeaveMessage> msgs=leavemsgDal.getLeaveMessages(results[2]);
                                msglst.addAll(msgs);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        bindData();
                                        //toCloseProgress();
                                        refreshview.setRefreshing(false);
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
            toCloseProgress();
            refreshview.setRefreshing(false);
        }
    };

    void bindData() {
        date = new Date();
        if (adapter==null) {
            adapter = new myAdapter();
            rvmsg.setAdapter(adapter);
            rvmsg.setLayoutManager(mLayoutManager);
        } else {
            adapter.notifyDataSetChanged();
        }
        isLoadingMore=false;
//        Animation animation = AnimationUtils.loadAnimation(this, R.anim.enter);
//        //得到一个LayoutAnimationController对象；
//        LayoutAnimationController lac = new LayoutAnimationController(animation);
//        //设置控件显示的顺序；
//        lac.setOrder(LayoutAnimationController.ORDER_NORMAL);
//        //设置控件显示间隔时间；
//        lac.setDelay(1);
//        //为ListView设置LayoutAnimationController属性；
//        rvmsg.setLayoutAnimation(lac);
    }

    class myAdapter extends RecyclerView.Adapter<myAdapter.ViewHolder> {
        LeaveMessage msg;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.lo_leave_messages_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            msg = msglst.get(position);
            String content = msg.getContent();
            int contentIndex = content.indexOf("$$");
            if (contentIndex > -1) {
                content = content.substring(contentIndex + 2);
            }
            holder.txt_houser.setText(SystemUtils.getVillage()+" "+SystemUtils.getAddress());
            holder.txt_content.setText(content);
            holder.txt_pub_time.setText(msg.getDate());
            holder.sessionid = msg.getId();
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LeaveMessages.this, ReplyMessages.class);
                    intent.putExtra("sessionid", holder.sessionid);
                    intent.putExtra("content", holder.txt_content.getText().toString().trim());
                    intent.putExtra("date", holder.txt_pub_time.getText().toString().trim());
                    startActivityWithAnim(intent);
                }
            });
        }


        @Override
        public int getItemCount() {
            return msglst.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView txt_pub_time, txt_content, txt_houser;
            int sessionid;

            public ViewHolder(View itemView) {
                super(itemView);
                txt_pub_time = (TextView) itemView.findViewById(R.id.txt_pub_time);
                txt_content = (TextView) itemView.findViewById(R.id.txt_content);
                txt_houser = (TextView) itemView.findViewById(R.id.txt_houser);
                sessionid = 0;
            }
        }
    }

    @Override
    public void ViewClick(View v) {

    }
}
