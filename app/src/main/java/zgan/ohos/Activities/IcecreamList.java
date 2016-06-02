package zgan.ohos.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import zgan.ohos.ConstomControls.MySelectCount;
import zgan.ohos.Dals.VegetableDal;
import zgan.ohos.Models.BaseGoods;
import zgan.ohos.Models.MyOrder;
import zgan.ohos.Models.Vegetable;
import zgan.ohos.R;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.generalhelper;

public class IcecreamList extends myBaseActivity implements View.OnClickListener {

    int pageindex = 0;
    boolean isLoadingMore = false;
    LinearLayoutManager mLayoutManager;
    SwipeRefreshLayout refreshview;
    myAdapter adapter;
    List<Vegetable> list;
    List<BaseGoods> buylist;
    ImageLoader imageLoader;
    VegetableDal dal;
    RecyclerView rv_vegetable;
    Button btncheck;
    TextView gdcount, totalpay;
    //商品数量
    int goodscount = 0;
    //商品价格
    double goodssum = 0;
    DecimalFormat decimalFormat = new DecimalFormat("###.0");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("goodscount")) {
                goodscount = savedInstanceState.getInt("goodscount", 0);
            }
            if (savedInstanceState.containsKey("goodssum")) {
                goodssum = savedInstanceState.getInt("goodssum", 0);
            }
        } else {
            goodscount = 0;
            goodssum = 0;
        }
        gdcount.setText("商品：" + String.valueOf(goodscount));
        totalpay.setText("合计：" + String.valueOf(goodssum));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("goodscount", goodscount);
        outState.putDouble("goodssum", goodssum);
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_icecream_list);
        rv_vegetable = (RecyclerView) findViewById(R.id.rv_vegetable);
        mLayoutManager = new LinearLayoutManager(IcecreamList.this);
        dal = new VegetableDal();
        buylist = new ArrayList<>();
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
        rv_vegetable.setOnScrollListener(new RecyclerView.OnScrollListener() {
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
        gdcount = (TextView) findViewById(R.id.gdcount);
        totalpay = (TextView) findViewById(R.id.totalpay);
        btncheck = (Button) findViewById(R.id.btncheck);
        btncheck.setText("购买");
        btncheck.setOnClickListener(this);
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
        ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1021, "@id=22,@page=0", "22"), handler);
    }

    public void loadMoreData() {
        try {
            pageindex++;
            //isLoadingMore = true;
            refreshview.setRefreshing(true);
            ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1021, String.format("@id=22,@page=%d", pageindex), "22"), handler);
        } catch (Exception ex) {
            generalhelper.ToastShow(this, ex.getMessage());
        }
    }

    void bindData() {
        if (adapter == null) {
            adapter = new myAdapter();
            rv_vegetable.setAdapter(adapter);
            rv_vegetable.setLayoutManager(mLayoutManager);
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
                    if (results[0].equals("0") && results[1].equals("1021")&&results.length>2) {
                        try {
                            if (pageindex == 0) {
                                list = new ArrayList<>();
                            }
                            if (frame.platform != 0) {
                                addCache("40" + String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1021, String.format("@id=22,@page=%d", pageindex), "22"), frame.strData);
                            }
                            List<Vegetable> vegetables = dal.getList(results[2]);
                            list.addAll(vegetables);
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
        switch (v.getId()) {
            case R.id.btncheck:
                if (goodscount < 1) {
                    generalhelper.ToastShow(this, "还没有选择任何蔬菜哟~");
                    return;
                }
                Calendar calendar = Calendar.getInstance();
                Calendar bestshippingdate = calendar;
                bestshippingdate.add(Calendar.MINUTE, 20);

                Intent intent = new Intent(this, CommitOrder.class);
                MyOrder m1 = new MyOrder();
//                m1.setOrder_sn("2016041500245");
//                m1.setHouse_holder("徐鹏");
//                m1.setHouser_name("金易伯爵世家4栋3单元6-2");
//                m1.setOrder_status("已确认");
//                m1.setShipping_status(0);
//                m1.setPay_status(0);
//                m1.setBest_time(generalhelper.getStringFromDate(bestshippingdate.getTime()));
//                m1.setOrder_type("订购");
//                m1.setShipping_id("送货上门");
//                m1.setGoods_amount(goodssum);
//                m1.setPay_fee(goodssum);
//                m1.setMoney_paid(0);
//                m1.setOrder_amount(goodssum);
//                m1.setAdd_time(generalhelper.getStringFromDate(calendar.getTime()));
                m1.setorder_id(m1.generateOrderId());
                m1.setaccount(PreferenceUtil.getUserName());
                m1.setdiliver_time("0");//(generalhelper.getStringFromDate(bestshippingdate.getTime()));
                //m1.setpay_type(3);
                m1.settotal(goodssum);
                m1.SetGoods(buylist);
                StringBuilder builder = new StringBuilder();
                String bstr = "";
                builder.append("'");
                for (BaseGoods g : buylist) {
                    //'商品id_t数量_t单价_t''属性''_t''商品名称''_p'
                    builder.append(g.getproduct_id() + "_t" + g.getSelectedcount() + "_t" + g.getprice() + "_t''" + g.getspecs() + "''_t''" + g.gettitle() + "''_p");
                }
                if (builder.length() > 1)
                    bstr = builder.substring(0, builder.length() - 2);
                bstr += "'";
                m1.setorder_details(bstr);
                Bundle bundle = new Bundle();
                bundle.putSerializable("order", m1);
                intent.putExtras(bundle);
                startActivityWithAnim(intent);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        ViewClick(v);
    }

    class myAdapter extends RecyclerView.Adapter<myAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.lo_vegetable_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final Vegetable vegetable = list.get(position);
            ImageLoader.bindBitmap(vegetable.getpic_url(), holder.iv_preview, 200, 200);
            holder.name.setText(vegetable.gettitle());
            holder.price.setText("￥" + String.valueOf(vegetable.getprice()));
            holder.size.setText(vegetable.getitemSize());
            holder.selectCount.setOnchangeListener(new MySelectCount.IonChanged() {
                @Override
                public void onAddition(int count) {
                    boolean contained = false;
                    for (int i = 0; i < buylist.size(); i++) {
                        if (buylist.get(i).getproduct_id().equals(vegetable.getproduct_id())) {
                            contained = true;
                            int selectedcount = vegetable.getSelectedcount();
                            if (selectedcount > 0)
                                vegetable.setSelectedcount(selectedcount + 1);
                            break;
                        }
                    }
                    if (!contained) {
                        buylist.add(vegetable);
                    }
                    goodscount++;
                    goodssum += vegetable.getprice();
                    gdcount.setText("商品：" + String.valueOf(goodscount));
                    totalpay.setText("合计：" + decimalFormat.format(goodssum));
                }

                @Override
                public void onReduction(int count) {
                    //是否移除
                    boolean canremove = false;
                    //移除索引
                    int removeIndex = 0;
                    for (int i = 0; i < buylist.size(); i++) {
                        if (buylist.get(i).getproduct_id().equals(vegetable.getproduct_id())) {
                            int selectedcount = vegetable.getSelectedcount();
                            if (selectedcount > 0)
                                vegetable.setSelectedcount(selectedcount - 1);
                            if (vegetable.getSelectedcount() == 0) {
                                canremove = true;
                                removeIndex = i;
                            }
                            break;
                        }
                    }
                    if (canremove) {
                        buylist.remove(removeIndex);
                    }
                    goodssum -= vegetable.getprice();
                    goodscount--;
                    gdcount.setText("商品：" + String.valueOf(goodscount));
                    totalpay.setText("合计：" + decimalFormat.format(goodssum));
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView iv_preview;
            TextView name, price, size;
            MySelectCount selectCount;

            public ViewHolder(View itemView) {
                super(itemView);
                iv_preview = (ImageView) itemView.findViewById(R.id.iv_preview);
                name = (TextView) itemView.findViewById(R.id.txt_name);
                price = (TextView) itemView.findViewById(R.id.txt_price);
                size = (TextView) itemView.findViewById(R.id.txt_size);
                selectCount = (MySelectCount) itemView.findViewById(R.id.selectcount);
            }
        }
    }
}
