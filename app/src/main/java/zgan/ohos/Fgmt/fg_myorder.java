package zgan.ohos.Fgmt;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.sql.Time;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import zgan.ohos.Activities.OrderDetail;
import zgan.ohos.Dals.QueryOrderDal;
import zgan.ohos.Models.BaseGoods;
import zgan.ohos.Models.MyOrder;
import zgan.ohos.Models.QueryOrderM;
import zgan.ohos.Models.Vegetable;
import zgan.ohos.R;
import zgan.ohos.adapters.RecyclerViewItemSpace;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.generalhelper;
import zgan.ohos.utils.resultCodes;

/**
 * create by yajunsun
 * <p/>
 * 首页订单fragment
 */
public class fg_myorder extends myBaseFragment implements View.OnClickListener {
    int pageindex = 1;
    boolean isLoadingMore = false;
    LinearLayoutManager mLayoutManager;
    myAdapter adapter;
    int mOrder_type = 1;
    RecyclerView rv_orders;
    QueryOrderDal dal;
    ImageLoader imageLoader;
    List<QueryOrderM> list;
    LayoutInflater myInflater;
    SwipeRefreshLayout refreshview;
    float density = 1;
    DecimalFormat decimalFormat = new DecimalFormat("#,###.##");

    TextView tall, tunpay, tunget;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == resultCodes.PAYCOMPLETE) {
            //有订单支付成功
            loadData();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myInflater = inflater;
        View v = myInflater.inflate(R.layout.activity_order_list, container, false);
        dal = new QueryOrderDal();
        density = AppUtils.getDensity(getActivity());
        mLayoutManager = new LinearLayoutManager(getActivity());
        imageLoader = new ImageLoader();
        //list = MyOrderDal.mConfirmedOrders;
        rv_orders = (RecyclerView) v.findViewById(R.id.rv_orders);
        rv_orders.setLayoutManager(mLayoutManager);
        rv_orders.addItemDecoration(new RecyclerViewItemSpace(20));
        refreshview = (SwipeRefreshLayout) v.findViewById(R.id.refreshview);
        refreshview.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageindex = 1;
                isLoadingMore = false;
                loadData();
                //adapter.notifyDataSetChanged();

            }
        });
        rv_orders.setOnScrollListener(new RecyclerView.OnScrollListener() {
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

        tall = (TextView) v.findViewById(R.id.t_all);
        tunpay = (TextView) v.findViewById(R.id.t_unpay);
        tunget = (TextView) v.findViewById(R.id.t_unget);
        tall.setOnClickListener(this);
        tunpay.setOnClickListener(this);
        tunget.setOnClickListener(this);
        tall.setTextColor(getResources().getColor(R.color.primary));
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter = null;
        pageindex = 1;
        loadData();
    }

    void initialOptions() {
        tall.setTextColor(getResources().getColor(R.color.solid_black));
        tunpay.setTextColor(getResources().getColor(R.color.solid_black));
        tunget.setTextColor(getResources().getColor(R.color.solid_black));
    }

    protected void loadData() {
        refreshview.setRefreshing(true);
        ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1016, String.format("@id=22,@account=%s,@order_type=%s,@page=%s", PreferenceUtil.getUserName(), mOrder_type, pageindex), "22"), handler);
    }

    public void loadMoreData() {
        try {
            pageindex++;
            refreshview.setRefreshing(true);
            ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1016, String.format("@id=22,@account=%s,@order_type=%s,@page=%s", PreferenceUtil.getUserName(), mOrder_type, pageindex), "22"), handler);
        } catch (Exception ex) {
            generalhelper.ToastShow(getActivity(), ex.getMessage());
        }
    }

    void bindData() {
        if (adapter == null) {
            adapter = new myAdapter();
            rv_orders.setAdapter(adapter);
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
                Log.i("suntest", frame.subCmd + "  " + ret);

                if (frame.subCmd == 40) {
                    if (results[0].equals("0") && results[1].equals("1016")) {
                        try {
                            if (pageindex == 1) {
                                list = new ArrayList<>();
                            }
                            if (frame.platform != 0) {
                                String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1016, String.format("@id=22,@account=%s,@order_type=%s,@page=%s", PreferenceUtil.getUserName(), mOrder_type, pageindex), frame.strData);
                            }
                            List<QueryOrderM> orders = dal.getList(results[2]);
                            list.addAll(orders);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    bindData();
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
                    refreshview.setRefreshing(false);
                }
                //toCloseProgress();
            }
        }
    };

    @Override
    public void onClick(View v) {
        refreshview.setRefreshing(false);
        switch (v.getId()) {
            case R.id.t_all:
                initialOptions();
                mOrder_type = 1;
                tall.setTextColor(getResources().getColor(R.color.primary));
                loadData();
                break;
            case R.id.t_unpay:
                initialOptions();
                mOrder_type = 2;
                tunpay.setTextColor(getResources().getColor(R.color.primary));
                loadData();
                break;
            case R.id.t_unget:
                initialOptions();
                mOrder_type = 3;
                tunget.setTextColor(getResources().getColor(R.color.primary));
                loadData();
                break;
        }
    }

    class myAdapter extends RecyclerView.Adapter<myAdapter.ViewHolder> {

        HashMap<Integer, Handler> handlerHashMap = new HashMap<>();

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(myInflater.inflate(R.layout.lo_order_item, parent, false));
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            QueryOrderM m = list.get(position);
            MyOrder o = new MyOrder();
            //设置recycleviewer的高度
            //int h = 0;//(int) (density * 120 * getItemCount());
            int h = (int) (density * 120);
            double fee = m.getpriceTotal();
            int count = m.getcount();
            List<BaseGoods> goodses = new ArrayList<>();
            BaseGoods goods = new Vegetable();
            goods.settitle(m.gettitle());
            goods.setprice(m.getprice());
            goods.setSelectedcount(m.getcount());
            goods.setpic_url(m.getpic_url());
            goodses.add(goods);
//            for (BaseGoods g : m.GetGoods()) {
//                fee += g.getprice()*g.getSelectedcount();
//                count+=g.getSelectedcount();
//                h +=  (int) (density * 120);
//            }
            if (m.gettitle().equals(""))
                h = 0;
            o.setorder_id(m.getorder_id());
            o.setdiliver_time(m.getdiliver_time());
            o.SetGoods(goodses);
            o.settotal(m.getpriceTotal());
            o.setstate(m.getorder_state());
            o.setpay_type(m.getpay_type());
            ViewGroup.LayoutParams params = holder.rv_goods.getLayoutParams();
            params.height = h;
            holder.rv_goods.setLayoutParams(params);
            holder.rv_goods.setAdapter(new mySubAdapter(goodses));
            holder.txt_ordernum.setText("订单号：" + m.getorder_id());
            holder.txt_shippingstatus.setText(m.getStatusText());
            // holder.txt_shippingstatus.setText(getShippingStatus(m.getShipping_status()));
            holder.txt_count.setText(String.format("共%d件商品", count));
            holder.txt_payfee.setText("合计：￥" + decimalFormat.format(fee));
            holder.index = position;
            holder.txttimer.setText("");
            final Date deliverTime = generalhelper.getDateFromString(o.getdiliver_time(), new Date());
            Date now = new Date();
            if (deliverTime.compareTo(now) > 0) {
                long l = deliverTime.getTime() - now.getTime();
                long day = l / (24 * 60 * 60 * 1000);
                long hour = (l / (60 * 60 * 1000) - day * 24);
                long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
                long s = (l / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);

                if (day > 0) {
                    holder.txttimer.setText(String.format("预计%s送达", generalhelper.getStringFromDate(deliverTime, "yyyy-MM-dd")));
                } else if (hour > 0) {
                    holder.txttimer.setText(String.format("预计%s送达", generalhelper.getStringFromDate(deliverTime, "yyyy-MM-dd")));
                } else {
                    holder.timer = new Timer(true);

                    holder.timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            //holder.txttimer.setText(""+day+"天"+hour+"小时"+min+"分"+s+"秒");
                            Date now = new Date();
                            long l = deliverTime.getTime() - now.getTime();
                            long day = l / (24 * 60 * 60 * 1000);
                            long hour = (l / (60 * 60 * 1000) - day * 24);
                            long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
                            long s = (l / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
                            Message msg = holder.handler.obtainMessage();
                            msg.what = 1;
                            msg.obj = min + "分" + s + "秒";
                            msg.arg1 = position;
                            holder.handler.sendMessage(msg);
                            if (deliverTime.compareTo(now) <= 0) {
                                holder.handler.sendEmptyMessage(0);
                            }
                        }
                    }, 0, 1000);
                }
            }


//            if (mOrder_type == 1 || mOrder_type == 3) {
//                holder.btn_deleteorder.setVisibility(View.VISIBLE);
//                holder.btn_checkshipping.setVisibility(View.VISIBLE);
//                holder.btn_payimmediatly.setVisibility(View.GONE);
//            }
//            if (mOrder_type == 2) {
//                holder.btn_deleteorder.setVisibility(View.VISIBLE);
//                holder.btn_checkshipping.setVisibility(View.GONE);
//                holder.btn_payimmediatly.setVisibility(View.VISIBLE);
//            }
            holder.btn_deleteorder.setOnClickListener(new ItemButtonOnclickListner(o, m.getStatusText(), m.getsub_time()));
            holder.btn_checkshipping.setOnClickListener(new ItemButtonOnclickListner(o, m.getStatusText(), m.getsub_time()));
            holder.btn_payimmediatly.setOnClickListener(new ItemButtonOnclickListner(o, m.getStatusText(), m.getsub_time()));
            holder.itemView.setOnClickListener(new ItemButtonOnclickListner(o, m.getStatusText(), m.getsub_time()));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ItemButtonOnclickListner implements View.OnClickListener {
            MyOrder o;
            String statusText = "配送中";
            String addTime;

            public ItemButtonOnclickListner(MyOrder _o, String _sText, String _addTime) {
                o = _o;
                statusText = _sText;
                addTime = _addTime;
            }

            @Override
            public void onClick(View v) {
                refreshview.setRefreshing(false);
                Intent intent;
                switch (v.getId()) {
                    case R.id.btn_deleteorder:
                        //删除订单
                        break;
                    case R.id.btn_payimmediatly:
                        //支付
                        break;
                    case R.id.btn_checkshipping:
                        //查看物流
                    default:
                        intent = new Intent(getActivity(), OrderDetail.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("order", o);
                        intent.putExtra("ordertype", mOrder_type);
                        intent.putExtra("orderstatus", statusText);
                        intent.putExtra("addtime", addTime);
                        intent.putExtra("fee", o.gettotal());
                        intent.putExtras(bundle);
                        startActivityForResult(intent, 0);
                        break;
                }
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txt_ordernum, txt_shippingstatus, txt_count, txt_payfee, txttimer;
            Button btn_deleteorder, btn_checkshipping, btn_payimmediatly;
            RecyclerView rv_goods;
            Timer timer;
            int index = 0;
            final Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (index == msg.arg1)
                        switch (msg.what) {
                            case 1:
                                settxtTimer(msg.obj.toString());
                                break;
                            case 0:
                                timer.cancel();
                                settxtTimer("");
                                break;
                        }
                }
            };

            public ViewHolder(View itemView) {
                super(itemView);
                txt_ordernum = (TextView) itemView.findViewById(R.id.txt_ordernum);
                txt_shippingstatus = (TextView) itemView.findViewById(R.id.txt_shippingstatus);
                txt_count = (TextView) itemView.findViewById(R.id.txt_count);
                txt_payfee = (TextView) itemView.findViewById(R.id.txt_payfee);
                txttimer = (TextView) itemView.findViewById(R.id.txttimer);
                rv_goods = (RecyclerView) itemView.findViewById(R.id.rv_goods);
                rv_goods.setLayoutManager(new LinearLayoutManager(getActivity()));
                btn_deleteorder = (Button) itemView.findViewById(R.id.btn_deleteorder);
                btn_checkshipping = (Button) itemView.findViewById(R.id.btn_checkshipping);
                btn_payimmediatly = (Button) itemView.findViewById(R.id.btn_payimmediatly);
            }

            public void settxtTimer(String str) {
                txttimer.setText(str);
            }
        }
    }

    class mySubAdapter extends RecyclerView.Adapter<mySubAdapter.ViewHolder> {

        List<BaseGoods> goodslist;

        public mySubAdapter(List<BaseGoods> _goodslist) {
            goodslist = _goodslist;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(myInflater.inflate(R.layout.lo_ordergoods_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            BaseGoods g = goodslist.get(position);
            ImageLoader.bindBitmap(g.getpic_url(), holder.iv_preview, 100, 100);
            holder.txt_name.setText(g.gettitle());
            holder.txt_price.setText("￥" + decimalFormat.format(g.getprice()));
            holder.txt_count.setText("*" + g.getSelectedcount());
        }

        @Override
        public int getItemCount() {
            return goodslist.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView iv_preview;
            TextView txt_price, txt_name, txt_count;

            public ViewHolder(View itemView) {
                super(itemView);
                iv_preview = (ImageView) itemView.findViewById(R.id.iv_preview);
                txt_price = (TextView) itemView.findViewById(R.id.txt_price);
                txt_name = (TextView) itemView.findViewById(R.id.txt_name);
                txt_count = (TextView) itemView.findViewById(R.id.txt_count);
            }
        }
    }
}
