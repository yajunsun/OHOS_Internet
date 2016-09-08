package zgan.ohos.Fgmt;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pay.alipay.AliPay;
import com.pay.wxpay.Constants;
import com.pay.wxpay.WXPay;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.sql.Time;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import zgan.ohos.Activities.MainActivity;
import zgan.ohos.Activities.OrderDetail;
import zgan.ohos.Activities.OrderList;
import zgan.ohos.Dals.QueryOrderDal;
import zgan.ohos.Dals.VegetableDal;
import zgan.ohos.Models.BaseGoods;
import zgan.ohos.Models.MyOrder;
import zgan.ohos.Models.QueryOrderM;
import zgan.ohos.Models.Vegetable;
import zgan.ohos.Models.WXResp;
import zgan.ohos.R;
import zgan.ohos.adapters.RecyclerViewItemSpace;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;
import zgan.ohos.utils.generalhelper;
import zgan.ohos.utils.resultCodes;

/**
 * create by yajunsun
 * <p>
 * 首页订单fragment
 */
public class fg_myorder extends myBaseFragment implements View.OnClickListener {
    int pageindex = 1;
    boolean isLoadingMore = false;
    LinearLayoutManager mLayoutManager;
    myAdapter adapter;
    //int mOrder_type = 1;
    int mOrder_type = 4;
    RecyclerView rv_orders;
    QueryOrderDal dal;
    VegetableDal goodsdal;
    ImageLoader imageLoader;
    List<QueryOrderM> list;
    //数据是否已经在本地的标志存档
    List<String>mOids;
    LayoutInflater myInflater;
    SwipeRefreshLayout refreshview;
    float density = 1;
    DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
    Dialog paymentSelectDialog;
    TextView tall, tunpay, tunget, tinprogress;
    LinearLayout llall, llunpay, llunget, llinprogress;
    String[] mPaytypeNames = new String[]{"", "货到付款", "钱包支付", "支付宝支付", "微信支付"};
    String[] mVialiabelTypes = new String[]{"3", "4"};
    boolean isCommited = false;
    private IWXAPI api;
    MyOrder order;
    private ProgressDialog progressDialog;
    public static final int ORDER_GOODS_LIST = 10000;
    boolean mNeedReload = true;

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
        goodsdal = new VegetableDal();
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
        tinprogress = (TextView) v.findViewById(R.id.t_inprogress);
        llall = (LinearLayout) v.findViewById(R.id.ll_all);
        llunpay = (LinearLayout) v.findViewById(R.id.ll_unpay);
        llunget = (LinearLayout) v.findViewById(R.id.ll_unget);
        llinprogress = (LinearLayout) v.findViewById(R.id.ll_inprogress);
        tall.setOnClickListener(this);
        tunpay.setOnClickListener(this);
        tunget.setOnClickListener(this);
        tinprogress.setOnClickListener(this);
        loadData();
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (AppUtils.NEED_REFRESH_ORDER)
            reload();
        AppUtils.NEED_REFRESH_ORDER=false;
    }

    private void reload() {
        adapter = null;
        pageindex = 1;
        loadData();
    }

    void initialOptions() {
        tall.setTextColor(getResources().getColor(R.color.solid_black));
        tunpay.setTextColor(getResources().getColor(R.color.solid_black));
        tunget.setTextColor(getResources().getColor(R.color.solid_black));
        tinprogress.setTextColor(getResources().getColor(R.color.solid_black));
        if (Build.VERSION.SDK_INT > 15) {
            llall.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            llunpay.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            llunget.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            llinprogress.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }
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
                                mOids=new ArrayList<>();
                            }
                            if (frame.platform != 0) {
                                String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1016, String.format("@id=22,@account=%s,@order_type=%s,@page=%s", PreferenceUtil.getUserName(), mOrder_type, pageindex), frame.strData);
                            }
                            int ini_count = list.size();
                            List<QueryOrderM> orders = dal.getList(results[2]);
                            //如果从服务器获取到了数据
                            if(orders.size()>0) {
                                //判断本地是否已经有了本次获取的数据
                                if (mOids.contains(orders.get(0).getorder_id()))
                                {
                                    //如果存在就直接忽略此次加载,避免重复
                                    pageindex--;
                                    refreshview.setRefreshing(false);
                                    return;
                                }
                                else
                                {
                                    //如果本地没有已有标志,则保存次页数据已存在的标志并加载显示出来
                                    mOids.add(orders.get(0).getorder_id());
                                }
                            }
                            list.addAll(orders);
                            for (QueryOrderM m : orders) {
                                ZganCommunityService.toGetServerData(40, 0, 3, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1026, String.format("@id=22,@account=%s,@order_id=%s", PreferenceUtil.getUserName(), m.getorder_id()), ini_count++), handler);
                            }
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
                    } else if (results[1].equals("1025")) {//results[0].equals("0") &&
                        if (results[0].equals("0")) {
                            generalhelper.ToastShow(getActivity(), "支付成功~");
                            paymentSelectDialog.dismiss();
                            loadData();
                        } else
                            generalhelper.ToastShow(getActivity(), "支付失败~");
                    } else if (results[0].equals("0") && results[1].equals("1026")) {
                        try {
                            List<Vegetable> lst = goodsdal.getList(results[2]);
                            final List<BaseGoods> goodslst = new ArrayList<>();
                            for (BaseGoods v : lst) {
                                goodslst.add(v);
                            }
                            list.get(SystemUtils.getIntValue(results[3])).setgoogsitems(goodslst);
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
            } else if (msg.what == resultCodes.PAYCOMPLETE) {
                updateCommit();
                try {
                    getActivity().unregisterReceiver(wxpayreceiver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
                if (Build.VERSION.SDK_INT > 15) {
                    llall.setBackgroundColor(getResources().getColor(R.color.primary));
                }
                loadData();
                break;
            case R.id.t_unpay:
                initialOptions();
                mOrder_type = 2;
                tunpay.setTextColor(getResources().getColor(R.color.primary));
                if (Build.VERSION.SDK_INT > 15) {
                    llunpay.setBackgroundColor(getResources().getColor(R.color.primary));
                }
                loadData();
                break;
            case R.id.t_unget:
                initialOptions();
                mOrder_type = 3;
                tunget.setTextColor(getResources().getColor(R.color.primary));
                if (Build.VERSION.SDK_INT > 15) {
                    llunget.setBackgroundColor(getResources().getColor(R.color.primary));
                }
                loadData();
                break;
            case R.id.t_inprogress:
                initialOptions();
                mOrder_type = 4;
                tinprogress.setTextColor(getResources().getColor(R.color.primary));
                if (Build.VERSION.SDK_INT > 15) {
                    llinprogress.setBackgroundColor(getResources().getColor(R.color.primary));
                }
                loadData();
                break;
            case R.id.iv_alipay:
                if (!isCommited) {
                    order.setpay_type(3);
                    pay();
                }
                break;
            case R.id.iv_wxpay:
                if (!isCommited) {
                    order.setpay_type(4);
                    pay();
                }
                break;
            case R.id.btn_complete:
            case R.id.txt_toorder:
                paymentSelectDialog.dismiss();
                break;
        }
    }

    class myAdapter extends RecyclerView.Adapter<myAdapter.ViewHolder> {
        HashMap<Integer, Boolean> isLoadDetail = new HashMap<>();

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(myInflater.inflate(R.layout.lo_order_item, parent, false));
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final QueryOrderM m = list.get(position);
            MyOrder o = new MyOrder();
            //设置recycleviewer的高度
            //int h = 0;//(int) (density * 120 * getItemCount());
            //int w = (int) (density * 120);
            double fee = m.getpriceTotal();
            int count = m.getcount();
            o.setorder_id(m.getorder_id());
            o.setdiliver_time(m.getdiliver_time());
            // o.SetGoods(goodses);
            o.settotal(m.getpriceTotal());
            o.setstate(m.getorder_state());
            o.setpay_type(m.getpay_type());
            //ViewGroup.LayoutParams params = holder.rv_goods.getLayoutParams();
            //params.height = h;
            if (m.getpay_state() == 0)
                holder.btn_payimmediatly.setVisibility(View.VISIBLE);
            //holder.rv_goods.setLayoutParams(params);
            //holder.rv_goods.setAdapter(new mySubAdapter(goodses));
            holder.txt_ordernum.setText(m.getorder_id());
            holder.txt_shippingstatus.setText(m.getStatusText());
            if (m.getStatusText().equals("已完成")) {
                holder.imgywc.setVisibility(View.VISIBLE);
                holder.txt_shippingstatus.setVisibility(View.GONE);
            } else {
                holder.imgywc.setVisibility(View.GONE);
                holder.txt_shippingstatus.setVisibility(View.VISIBLE);
            }
            // holder.txt_shippingstatus.setText(getShippingStatus(m.getShipping_status()));
            holder.txt_count.setText(String.format("共%d件商品", count));
            holder.txt_payfee.setText("合计：￥" + decimalFormat.format(fee));
            holder.index = position;
            holder.txttimer.setText("");
            holder.rv_goods.removeAllViews();
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
                            msg.what = 3;
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
            if (m.getgoodsitems().size() > 0) {
                List<BaseGoods> goodses = m.getgoodsitems();
                isLoadDetail.put(position, true);
                for (BaseGoods g : goodses) {
                    LinearLayout layout = buildGoodsView(g, goodses.size());
                    if (layout != null)
                        holder.rv_goods.addView(layout);
                }

            } else
                holder.rv_goods.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (m.getgoodsitems().size() > 0) {
                            if (isLoadDetail.get(position) == null || !isLoadDetail.get(position)) {
                                List<BaseGoods> goodses = m.getgoodsitems();
                                for (BaseGoods g : goodses) {
                                    LinearLayout layout = buildGoodsView(g, goodses.size());
                                    if (layout != null)
                                        holder.rv_goods.addView(layout);
                                }
                            }
                        }
                    }
                }, 1000);
            holder.btn_deleteorder.setOnClickListener(new ItemButtonOnclickListner(o, m.getStatusText(), m.getsub_time()));
            holder.btn_checkshipping.setOnClickListener(new ItemButtonOnclickListner(o, m.getStatusText(), m.getsub_time()));
            holder.btn_payimmediatly.setOnClickListener(new ItemButtonOnclickListner(o, m.getStatusText(), m.getsub_time()));
            holder.itemView.setOnClickListener(new ItemButtonOnclickListner(o, m.getStatusText(), m.getsub_time()));
        }

        private LinearLayout buildGoodsView(BaseGoods g, int count) {
            LinearLayout layout = new LinearLayout(getActivity());
            if (count == 1) {
                ImageView iv_preview;
                TextView txt_price, txt_name, txt_count;
                RelativeLayout v = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.lo_ordergoods_detail_item, null, false);
                iv_preview = (ImageView) v.findViewById(R.id.iv_preview);
                txt_price = (TextView) v.findViewById(R.id.txt_price);
                txt_name = (TextView) v.findViewById(R.id.txt_name);
                txt_count = (TextView) v.findViewById(R.id.txt_count);
                ImageLoader.bindBitmap(g.getpic_url(), iv_preview, 100, 100);
                txt_name.setText(g.gettitle());
                txt_count.setText("*" + String.valueOf(g.getcount()));
                txt_price.setText("￥" + decimalFormat.format(g.getprice()));
                layout.addView(v);
            } else {
                LinearLayout.MarginLayoutParams params = new LinearLayout.LayoutParams((int) (100 * density), (int) (100 * density));
                layout.setOrientation(LinearLayout.VERTICAL);
                ImageView img = new ImageView(getActivity());
                params.setMargins(10, 10, 0, 10);
                layout.setLayoutParams(params);
                img.setLayoutParams(params);
                img.setLayoutParams(params);
                img.setScaleType(ImageView.ScaleType.FIT_XY);
                img.setAdjustViewBounds(true);
                img.setMaxWidth((int) (300 * density));
                ImageLoader.bindBitmap(g.getpic_url(), img, 100, 100);
                layout.addView(img);
            }
            return layout;
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
                        order = o;
                        buildPaySelection();
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
                        startActivityWithAnimForResult(getActivity(), intent, resultCodes.DETAIL);
                        break;
                }
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txt_ordernum, txt_shippingstatus, txt_count, txt_payfee, txttimer;
            Button btn_deleteorder, btn_checkshipping, btn_payimmediatly;
            //RecyclerView rv_goods;
            LinearLayout rv_goods;
            Timer timer;
            ImageView imgywc;
            int index = 0;
            final Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (index == msg.arg1)
                        switch (msg.what) {
                            case 3:
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
                //rv_goods = (RecyclerView) itemView.findViewById(R.id.rv_goods);
                rv_goods = (LinearLayout) itemView.findViewById(R.id.rv_goods);
                //rv_goods.setLayoutManager(new LinearLayoutManager(getActivity()));
                imgywc = (ImageView) itemView.findViewById(R.id.img_ywc);
                btn_deleteorder = (Button) itemView.findViewById(R.id.btn_deleteorder);
                btn_checkshipping = (Button) itemView.findViewById(R.id.btn_checkshipping);
                btn_payimmediatly = (Button) itemView.findViewById(R.id.btn_payimmediatly);
            }

            public void settxtTimer(String str) {
                txttimer.setText(str);
            }
        }
    }

    private void buildPaySelection() {
        if (paymentSelectDialog == null) {
            ImageView iv_hdfk, iv_alipay, iv_wallite, iv_wxpay;
            TextView txt_paymount;
            View view = getActivity().getLayoutInflater().inflate(R.layout.lo_paytype_choose_dialog,
                    null);
            iv_hdfk = (ImageView) view.findViewById(R.id.iv_hdfk);
            iv_wallite = (ImageView) view.findViewById(R.id.iv_wallite);
            iv_alipay = (ImageView) view.findViewById(R.id.iv_alipay);
            iv_wxpay = (ImageView) view.findViewById(R.id.iv_wxpay);

            for (String tp : mVialiabelTypes) {
                if (tp.equals("1"))
                    iv_hdfk.setVisibility(View.VISIBLE);
                if (tp.equals("2"))
                    iv_wallite.setVisibility(View.VISIBLE);
                if (tp.equals("3"))
                    iv_alipay.setVisibility(View.VISIBLE);
                if (tp.equals("4"))
                    iv_wxpay.setVisibility(View.VISIBLE);
            }

            txt_paymount = (TextView) view.findViewById(R.id.txt_paymount);
            txt_paymount.setText("￥" + decimalFormat.format(order.gettotal()));
            iv_hdfk.setOnClickListener(this);
            iv_alipay.setOnClickListener(this);
            iv_wallite.setOnClickListener(this);
            iv_wxpay.setOnClickListener(this);
            paymentSelectDialog = new Dialog(getActivity(), R.style.transparentFrameWindowStyle);
            paymentSelectDialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            Window window = paymentSelectDialog.getWindow();
            // 设置显示动画
            window.setWindowAnimations(R.style.main_menu_animstyle);
            WindowManager.LayoutParams wl = window.getAttributes();
            wl.x = 0;
            wl.y = getActivity().getWindowManager().getDefaultDisplay().getHeight();
            // 以下这两句是为了保证按钮可以水平满屏
            wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
            wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;

            // 设置显示位置
            paymentSelectDialog.onWindowAttributesChanged(wl);
            // 设置点击外围解散
            paymentSelectDialog.setCanceledOnTouchOutside(true);
        }
        paymentSelectDialog.show();
    }

    private void updateCommit() {
        ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1025,
                String.format("@id=22,@order_id=%s,@state=%s,@pay_type=%s",
                        order.getorder_id(), order.getstate(), order.getpay_type())
                , "22"), handler);
    }

    private void pay() {
        switch (order.getpay_type()) {
            case 1:
                break;
            case 2:
                break;
            case 3:
                AliPay aliPay = new AliPay(getActivity());
                aliPay.setOnPayListner(new AliPay.OnAliPayListner() {
                    @Override
                    public void done(String stat) {
                        if (stat.equals("9000")) {
                            order.setpay_type(3);
                            order.setstate(1);
                            paymentSelectDialog.dismiss();
                            //支付成功修改订单为已支付
                            updateCommit();
                            isCommited = true;
                        } else {
                            isCommited = false;
                            generalhelper.ToastShow(getActivity(), "支付失败");
                        }
                    }

                    @Override
                    public void predo() {
                    }
                });
                aliPay.Pay(order);
                break;
            case 4:
//                toSetProgressText("正在启动微信支付请稍等");
//                toShowProgress();
                IntentFilter filter = new IntentFilter();
                filter.addAction(WXPay.payresultAction);
                getActivity().registerReceiver(wxpayreceiver, filter);
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setCancelable(true);
                progressDialog.setMessage("正在启动微信支付。。");
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        api = WXAPIFactory.createWXAPI(getActivity(), Constants.APP_ID, false);
                        api.registerApp(Constants.APP_ID);
                        WXPay wxPay = new WXPay(api);
                        if (!wxPay.checkSupport()) {
                            handler.obtainMessage(400, "此版本的微信不支持支付功能").sendToTarget();
                            return;
                        }
                        wxPay.setOrder(order);
                        wxPay.Pay();
                    }
                }).start();
                break;
        }
    }

    public BroadcastReceiver wxpayreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("data")) {
                WXResp resp = (WXResp) intent.getSerializableExtra("data");
                switch (resp.getErrcode()) {
                    case BaseResp.ErrCode.ERR_OK:
                        generalhelper.ToastShow(getActivity(), "支付成功");
                        handler.obtainMessage(resultCodes.PAYCOMPLETE).sendToTarget();
                        break;
                    case BaseResp.ErrCode.ERR_USER_CANCEL:
                        isCommited = false;
                        generalhelper.ToastShow(getActivity(), "已取消支付");
                        break;
                    case BaseResp.ErrCode.ERR_AUTH_DENIED:
                        isCommited = false;
                        generalhelper.ToastShow(getActivity(), "未授权");
                        break;
                    default:
                        isCommited = false;
                        generalhelper.ToastShow(getActivity(), "未知错误");
                        break;
                }
            }
            progressDialog.dismiss();
        }
    };
}
