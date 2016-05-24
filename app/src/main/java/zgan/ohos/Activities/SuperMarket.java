package zgan.ohos.Activities;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Calendar;
import java.util.List;

import zgan.ohos.Dals.HightQualityDal;
import zgan.ohos.Dals.SuperMarketDal;
import zgan.ohos.Models.SuperMarketM;
import zgan.ohos.R;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.generalhelper;

public class SuperMarket extends myBaseActivity {

    List<SuperMarketM> list;
    SuperMarketDal dal;
    LinearLayout llcontent;
    int pageindex = 0;
    boolean isLoadingMore = false;
    GridLayoutManager mLayoutManager;
    Calendar lastCall;
    Calendar thisCall;
    int width=0;
    //myAdapter adapter;
    @Override
    protected void initView() {
        setContentView(R.layout.activity_super_market);
        dal=new SuperMarketDal();
        llcontent=(LinearLayout)findViewById(R.id.ll_content);
        width= AppUtils.getWindowSize(this).x;
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
        ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1009, "@id=22", "@22"), handler);
    }

    public void loadMoreData() {
        try {
            pageindex++;
            isLoadingMore = true;
            ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1009, "@id int", "@22"), handler);
        } catch (Exception ex) {
            generalhelper.ToastShow(this, ex.getMessage());
        }
    }

//    void bindData() {
////        if (!isLoadingMore) {
////            adapter = new myAdapter();
////            rv_cakes.setAdapter(adapter);
////            rv_cakes.setLayoutManager(mLayoutManager);
////        } else
////            adapter.notifyDataSetChanged();
////        if (m!=null)
////        {
////            ImageLoader.bindBitmap(m.getpic_url(),ivpreview,500,500);
////        }
//        ViewGroup.MarginLayoutParams params=new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,getResources().getInteger(R.integer.supermarket_img_height));
//        for (int i=0;i<list.size();i++)
//        {
//            ImageView iv=new ImageView(this);
//            iv.setLayoutParams(params);
//            iv.setScaleType(ImageView.ScaleType.FIT_XY);
//            ImageLoader.bindBitmap(list.get(i).getpic_url(), iv, 800, getResources().getInteger(R.integer.supermarket_img_height));
//            llcontent.addView(iv);
//        }
//    }

    void bindData() {
        // ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getResources().getInteger(R.integer.supermarket_img_height));
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int maxheight=width*5;
        params.setMargins(0, 0, 0, 0);
        for (int i = 0; i < list.size(); i++) {
            ImageView iv = new ImageView(this);
            iv.setLayoutParams(params);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            iv.setAdjustViewBounds(true);
            iv.setMaxWidth(width);
            iv.setMaxHeight(maxheight);
            ImageLoader.bindBitmap(list.get(i).getpic_url(), iv, width, width);
            llcontent.addView(iv);
        }
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
                    if (results[0].equals("0")&& results[1].equals("1009")) {
                        try {
                            if (!isLoadingMore) {
                                list = dal.getList(results[2]);
                                if (frame.platform != 0) {
                                    addCache("40" +  String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1009, "@id=22", "@22"), frame.strData);
                                }
                            } else
                            {}
//                                list.addAll(dal.getList(results[2]));
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
        switch (v.getId())
        {
            case R.id.l_call_mall:
                if (lastCall == null) {
                    lastCall = Calendar.getInstance();
                    Intent intent = new Intent(this, CallOut.class);
                    startActivityWithAnim(intent);
                } else {
                    thisCall = Calendar.getInstance();
                    long span = thisCall.getTimeInMillis() - lastCall.getTimeInMillis();
                    //判断上次点击开门和本次点击开门时间间隔是否大于5秒钟
                    if (span > 5000) {
                        lastCall = Calendar.getInstance();
                        //communityOpt(37, String.format("%s\t%s", PreferenceUtil.getUserName(), PreferenceUtil.getSID()));
                        Intent intent = new Intent(this, CallOut.class);
                        startActivityWithAnim(intent);
                    } else {
                        generalhelper.ToastShow(this, "请在" + ((5000 - span) / 1000 + 1) + "秒后操作");
                    }
                }
                break;
        }
    }
}
