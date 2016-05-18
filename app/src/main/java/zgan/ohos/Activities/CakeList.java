package zgan.ohos.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import zgan.ohos.Contracts.IImageloader;
import zgan.ohos.Dals.CakeDal;
import zgan.ohos.Dals.VegetableDal;
import zgan.ohos.Models.Cake;
import zgan.ohos.R;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.generalhelper;

public class CakeList extends myBaseActivity {

    int pageindex = 0;
    boolean isLoadingMore = false;
    GridLayoutManager mLayoutManager;
    myAdapter adapter;
    RecyclerView rv_cakes;
    CakeDal cakeDal;
    List<Cake> list;
    ImageLoader imageLoader;
    View lp2pservice;
    @Override
    protected void initView() {
        setContentView(R.layout.activity_cake_list);
        rv_cakes=(RecyclerView)findViewById(R.id.rv_cakes);
        mLayoutManager = new GridLayoutManager(CakeList.this,2);
        cakeDal =new CakeDal();
//        rv_cakes.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                int lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
//                int totalItemCount = mLayoutManager.getItemCount();
//                //lastVisibleItem >= totalItemCount - 4 表示剩下4个item自动加载，各位自由选择
//                // dy>0 表示向下滑动
//                if (lastVisibleItem == totalItemCount - 1 && !isLoadingMore && dy > 0) {
//
//                    loadMoreData();//这里多线程也要手动控制isLoadingMore
//                    //isLoadingMore = false;
//                }
//            }
//        });
        imageLoader=new ImageLoader();
        lp2pservice=findViewById(R.id.lp2pservice);
        list=new ArrayList<>();
        View back=findViewById(R.id.back);
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
        isLoadingMore = false;
        ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1003, "@id=22", "22"), handler);
    }

    public void loadMoreData() {
        try {
            pageindex++;
            isLoadingMore = true;
            ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1003, "@id=22", "22"), handler);
        } catch (Exception ex) {
            generalhelper.ToastShow(this, ex.getMessage());
        }
    }

    void bindData() {
        if (!isLoadingMore) {
            adapter = new myAdapter();
            rv_cakes.setAdapter(adapter);
            rv_cakes.setLayoutManager(mLayoutManager);
        } else
            adapter.notifyDataSetChanged();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Frame frame = (Frame) msg.obj;
                String[] results = frame.strData.split("\t");
                String ret = generalhelper.getSocketeStringResult(frame.strData);
                Log.v(TAG, frame.subCmd + "  " + ret);

                if (frame.subCmd == 40) {
                    if (results[0].equals("0")&& results[1].equals("1003")) {
                        try {
                            if (!isLoadingMore) {
                                list = cakeDal.getList(results[2]);
                                if (frame.platform != 0) {
                                    addCache("40, 1003", results[2]);
                                }
                            } else
                                list.addAll(cakeDal.getList(results[2]));
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
                }
                toCloseProgress();
            }
        }
    };
    @Override
    public void ViewClick(View v) {
         if (v.getId()==R.id.lp2pservice)
         {
//             Intent intent=new Intent(CakeList.this,PersonalTailor.class);
//             startActivityWithAnim(intent);
         }
    }

    class myAdapter extends RecyclerView.Adapter<myAdapter.ViewHolder>{

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.lo_cakes_item,parent,false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
           final Cake cake=list.get(position);
            ImageLoader.bindBitmap(cake.getpic_url(), holder.iv_preview, 200, 200);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CakeList.this, CakeDetail.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("cake", cake);
                    intent.putExtras(bundle);
                    startActivityWithAnim(intent);
                }
            });
            holder.name.setText(cake.gettitle());
            holder.price.setText("￥"+String.valueOf( cake.getprice()));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            ImageView iv_preview;
            TextView name,price;
            public ViewHolder(View itemView) {
                super(itemView);
                iv_preview=(ImageView)itemView.findViewById(R.id.iv_preview);
                name=(TextView)itemView.findViewById(R.id.name);
                price=(TextView)itemView.findViewById(R.id.price);
            }
        }
    }
}
