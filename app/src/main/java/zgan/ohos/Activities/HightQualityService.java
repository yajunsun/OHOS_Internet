package zgan.ohos.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import zgan.ohos.Contracts.IImageloader;
import zgan.ohos.Dals.HightQualityDal;
import zgan.ohos.Models.FrontItem;
import zgan.ohos.Models.FuncBase;
import zgan.ohos.Models.HightQualityServiceM;
import zgan.ohos.R;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.generalhelper;

public class HightQualityService extends myBaseActivity {

    int pageindex = 0;
    boolean isLoadingMore = false;
    SwipeRefreshLayout refreshview;
    LinearLayoutManager mLayoutManager;
    myAdapter adapter;
    RecyclerView rc_items;
    List<HightQualityServiceM> list;
    HightQualityDal dal;
    ImageLoader imageLoader;
    FuncBase item;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_hight_quality_service);
        item =(FuncBase) getIntent().getSerializableExtra("item");
//        if (item.getpage_id().equals("2")) {
//            TextView t = (TextView) findViewById(R.id.txt_title);
//            t.setText("土特产");
//        }
//        if (item.getpage_id().equals("3")) {
//            TextView t = (TextView) findViewById(R.id.txt_title);
//            t.setText("高端特供");
//        }
            TextView t = (TextView) findViewById(R.id.txt_title);
            t.setText(item.getview_title());
        mLayoutManager = new LinearLayoutManager(HightQualityService.this);
        rc_items = (RecyclerView) findViewById(R.id.rv_items);
        dal = new HightQualityDal();
        imageLoader = new ImageLoader();
        refreshview = (SwipeRefreshLayout) findViewById(R.id.refreshview);
        refreshview.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageindex = 0;
                isLoadingMore = false;
                loadData();
                //adapter.notifyDataSetChanged();

            }
        });
        rc_items.setOnScrollListener(new RecyclerView.OnScrollListener() {
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
        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        toSetProgressText(getResources().getString(R.string.loading));
        toShowProgress();
        loadData();
    }

    protected void loadData() {
        //isLoadingMore = false;
        refreshview.setRefreshing(true);
        ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), item.gettype_id(),String.format("@id=22,@page_id=%s,@page=0",item.getpage_id()), "22"), handler);
    }

    public void loadMoreData() {
        try {
            pageindex++;
            //isLoadingMore = true;
            refreshview.setRefreshing(true);
            ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), item.gettype_id(), String.format("@id=22,@page_id=%s,@page=%s",item.getpage_id(), pageindex), "22"), handler);
        } catch (Exception ex) {
            generalhelper.ToastShow(this, ex.getMessage());
        }
    }

    void bindData() {
        if (adapter == null) {
            adapter = new myAdapter();
            rc_items.setAdapter(adapter);
            rc_items.setLayoutManager(mLayoutManager);
        } else
            adapter.notifyDataSetChanged();
        isLoadingMore = false;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Frame frame = (Frame) msg.obj;
                String[] results = frame.strData.split("\t");
                String ret = generalhelper.getSocketeStringResult(frame.strData);
                Log.i(TAG, frame.subCmd + "  " + ret);

                if (frame.subCmd == 40) {
                    if (results[0].equals("0") && results[1].equals(item.gettype_id())&&results.length>2) {
                        try {
                            if (pageindex == 0) {
                                list = new ArrayList<>();
                            }
                            if (frame.platform != 0) {

                                addCache("40" + String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), item.gettype_id(), String.format("@id=22,@page_id=%s,@page=%s",item.getpage_id(), pageindex), "22"), frame.strData);
                            }
                            List<HightQualityServiceM>hightQualityServiceMs = dal.getList(results[2]);
                            list.addAll(hightQualityServiceMs);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    bindData();
                                }
                            });
                        } catch (Exception ex) {
                            android.os.Message msg1 = new android.os.Message();
                            msg1.what = 0;
                            msg1.obj = ex.getMessage();
                            handler.sendMessage(msg1);
                        }
                    }
                    refreshview.setRefreshing(false);
                }
                toCloseProgress();
            }
        }
    };

    @Override
    public void ViewClick(View v) {

    }

    class myAdapter extends RecyclerView.Adapter<myAdapter.ViewHoler> {

        int width = 200, height = 200;

        public myAdapter() {
            width = AppUtils.getWindowSize(HightQualityService.this).x;
            height = 5 * width;
        }

        @Override
        public ViewHoler onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHoler(getLayoutInflater().inflate(R.layout.lo_hightq_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHoler holder, int position) {
            final HightQualityServiceM m = list.get(position);
            ImageLoader.bindBitmap(m.getpic_url(), holder.ivpreview, 600, 600);
            holder.txtdesc.setText(m.gettitle());
            holder.txtprice.setText("￥" + m.getprice());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshview.setRefreshing(false);
                    Intent intent = new Intent(HightQualityService.this, HightQualityDetail.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("hqs", m);
                    intent.putExtras(bundle);
                    intent.putExtra("pageid", item.gettype_id());
                    startActivityWithAnim(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHoler extends RecyclerView.ViewHolder {
            ImageView ivpreview;
            TextView txtdesc, txtprice;

            public ViewHoler(View itemView) {
                super(itemView);
                ivpreview = (ImageView) itemView.findViewById(R.id.iv_preview);
                txtdesc = (TextView) itemView.findViewById(R.id.txt_desc);
                txtprice = (TextView) itemView.findViewById(R.id.txt_price);
                ivpreview.setMaxWidth(width);
                ivpreview.setMaxHeight(height);
            }
        }
    }
}
