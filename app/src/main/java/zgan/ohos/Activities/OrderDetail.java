package zgan.ohos.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pay.alipay.AliPay;
import com.pay.wxpay.Constants;
import com.pay.wxpay.WXPay;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import zgan.ohos.ConstomControls.PayPwdEditText;
import zgan.ohos.Contracts.IImageloader;
import zgan.ohos.Dals.MyOrderDal;
import zgan.ohos.Dals.VegetableDal;
import zgan.ohos.Models.BaseGoods;
import zgan.ohos.Models.MyOrder;
import zgan.ohos.Models.Vegetable;
import zgan.ohos.Models.WXResp;
import zgan.ohos.R;
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
 *
 * 订单详情界面
 * */
public class OrderDetail extends myBaseActivity implements View.OnClickListener {
    private IWXAPI api;
    String[] mPaytypeNames = new String[]{"现金支付", "刷卡支付", "支付宝支付", "微信支付"};
    //int[] mPaytypeDraws = new int[]{R.drawable.pay_weixin, R.drawable.pay_zhifubao, R.drawable.pay_qianbao, R.drawable.pay_huodaofukuan};

    TextView txt_ordernum /**订单号**/
            , txt_shipingstatus/**订单状态**/
            //, txt_householder /**收货人**/
            ;
    TextView txt_addr /**收货地址**/
            , txt_phone /**收货人电话**/
            ;
    TextView txt_shippingid, txt_shippinger/**配送人**/
            , txt_shippingtime/**配送时间**/
            , txt_addtime/**下单时间**/
            , txt_payfee/**付款金额**/
            ;

    Button btn_deleteorder, btn_checkshipping, btn_payimmediatly;

    RecyclerView rv_goods;
    LinearLayout lpaytypes;
    ImageLoader imageLoader;
    int mOrder_type = 1;
    MyOrder order;
    MyOrderDal dal;
    List<BaseGoods> list;
    Dialog paymentSelectDialog;
    Dialog dialog;
    Dialog paypwdInputDialog;
    double fee = 0;
    DecimalFormat decimalFormat=new DecimalFormat("#,###.##");
    String orderStatus = "配送中";
    String addTime;

    @Override
    protected void initView() {
        Intent data = getIntent();
        order = (MyOrder) data.getSerializableExtra("order");
        mOrder_type = data.getIntExtra("ordertype", 1);
        orderStatus = data.getStringExtra("orderstatus");
        addTime = data.getStringExtra("addtime");
        fee = data.getDoubleExtra("fee", 0);
        setContentView(R.layout.activity_order_detail);
        imageLoader = new ImageLoader();
        txt_ordernum = (TextView) findViewById(R.id.txt_ordernum);
        txt_shipingstatus = (TextView) findViewById(R.id.txt_shipingstatus);
        //txt_householder = (TextView) findViewById(R.id.txt_householder);
        txt_addr = (TextView) findViewById(R.id.txt_addr);
        txt_phone = (TextView) findViewById(R.id.txt_phone);
        txt_shippingid = (TextView) findViewById(R.id.txt_shippingid);
        txt_shippinger = (TextView) findViewById(R.id.txt_shippinger);
        txt_shippingtime = (TextView) findViewById(R.id.txt_shippingtime);
        txt_addtime = (TextView) findViewById(R.id.txt_addtime);
        txt_payfee = (TextView) findViewById(R.id.txt_payfee);
        rv_goods = (RecyclerView) findViewById(R.id.rv_goods);
        lpaytypes = (LinearLayout) findViewById(R.id.lpaytypes);
        btn_deleteorder = (Button) findViewById(R.id.btn_deleteorder);
        btn_checkshipping = (Button) findViewById(R.id.btn_checkshipping);
        btn_payimmediatly = (Button) findViewById(R.id.btn_payimmediatly);
        btn_deleteorder.setOnClickListener(this);
        btn_checkshipping.setOnClickListener(this);
        btn_payimmediatly.setOnClickListener(this);
//        if (mOrder_type == 1 || mOrder_type == 3) {
//            btn_deleteorder.setVisibility(View.VISIBLE);
//            btn_checkshipping.setVisibility(View.VISIBLE);
//            btn_payimmediatly.setVisibility(View.GONE);
//        } else if (mOrder_type == 2) {
//            btn_deleteorder.setVisibility(View.VISIBLE);
//            btn_checkshipping.setVisibility(View.GONE);
//            btn_payimmediatly.setVisibility(View.VISIBLE);
//        }
        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (order != null) {
            //list = order.GetGoods();
            loadData();
            initialPage();
        }
        txt_addr.setText("收货地址：" + SystemUtils.getVillage()+SystemUtils.getAddress());
        //ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1020, String.format("@id=22,@account=%s", PreferenceUtil.getUserName()), "22"), handler);
    }

    private void initialPage() {
        txt_ordernum.setText("订单号：" + order.getorder_id());
        txt_shipingstatus.setText(orderStatus);
        //txt_householder.setText("收货人：" + order.getHouse_holder());
        //txt_addr.setText("收货地址：" + order.getHouser_name());
        txt_phone.setText("收货人："+PreferenceUtil.getUserName());
        txt_shippingid.setText("配送方式：送货上门");
        txt_shippinger.setText("配送人员：" + "A");
        if (order.getdiliver_time().equals("0"))
            txt_shippingtime.setText("配送时间：立即配送");
        else
            txt_shippingtime.setText("配送时间：" + order.getdiliver_time());
        txt_addtime.setText("下单时间：" + addTime);
//        for (BaseGoods g : order.GetGoods()) {
//            fee += g.getprice() * g.getSelectedcount();
//        }
        txt_payfee.setText("￥" + decimalFormat.format(fee));

        buildPayView(order.getpay_type());
    }

    private void  loadData()
    {
        ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1026, String.format("@id=22,@account=%s,@order_id=%s", PreferenceUtil.getUserName(), order.getorder_id()),"22"), handler);
    }
    private void dataBind()
    {
        int h = (int) (AppUtils.getDensity(this) * 120 * list.size());
        ViewGroup.LayoutParams params = rv_goods.getLayoutParams();
        params.height = h;
        rv_goods.setAdapter(new myAdapter());
        rv_goods.setLayoutParams(params);
        rv_goods.setLayoutManager(new LinearLayoutManager(this));
    }
    private void buildPayView(int statu) {
        float density = AppUtils.getDensity(this);
        int i = 0;
        for (String p : mPaytypeNames
                ) {
            LinearLayout l = new LinearLayout(this);
            ImageView v = new ImageView(this);
            TextView t = new TextView(this);
            ViewGroup.MarginLayoutParams lparams = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            l.setLayoutParams(lparams);
            l.setOrientation(LinearLayout.HORIZONTAL);
            l.setPadding(10, 0, 10, 0);
            v.setLayoutParams(new LinearLayout.LayoutParams((int) (10 * density), ViewGroup.LayoutParams.MATCH_PARENT));
            v.setScaleType(ImageView.ScaleType.FIT_CENTER);
            l.setGravity(Gravity.CENTER_VERTICAL);
            if (statu == 0 || statu - 1 != i) {
                imageLoader.loadDrawableRS(this, R.drawable.status_unselected, v, new IImageloader() {
                    @Override
                    public void onDownloadSucc(Bitmap bitmap, String c_url, View imageView, int w, int h) {
                        ((ImageView) imageView).setImageBitmap(bitmap);
                    }
                }, (int) (10 * density), (int) (10 * density));
            } else {
                imageLoader.loadDrawableRS(this, R.drawable.status_selected, v, new IImageloader() {
                    @Override
                    public void onDownloadSucc(Bitmap bitmap, String c_url, View imageView, int w, int h) {
                        ((ImageView) imageView).setImageBitmap(bitmap);
                    }
                }, (int) (10 * density), (int) (10 * density));
            }
            l.addView(v);
            ViewGroup.MarginLayoutParams tparams = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            t.setLayoutParams(tparams);
            t.setGravity(Gravity.CENTER_VERTICAL);
            t.setText(p);
            //t.setTextSize(getResources().getDimension(R.dimen.front_text_size));
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            l.addView(t);
            lpaytypes.addView(l);
            i++;
        }
    }

    private void buildPaySelection() {
        if (paymentSelectDialog == null) {
            ImageView iv_hdfk, iv_alipay, iv_wallite, iv_wxpay;
            TextView txt_paymount;
            View view = getLayoutInflater().inflate(R.layout.lo_paytype_choose_dialog,
                    null);

            iv_hdfk = (ImageView) view.findViewById(R.id.iv_hdfk);
            iv_alipay = (ImageView) view.findViewById(R.id.iv_alipay);
            iv_wallite = (ImageView) view.findViewById(R.id.iv_wallite);
            iv_wxpay = (ImageView) view.findViewById(R.id.iv_wxpay);
            txt_paymount = (TextView) view.findViewById(R.id.txt_paymount);
            txt_paymount.setText("￥" + fee);
            iv_hdfk.setOnClickListener(this);
            iv_alipay.setOnClickListener(this);
            iv_wallite.setOnClickListener(this);
            iv_wxpay.setOnClickListener(this);
            paymentSelectDialog = new Dialog(this, R.style.transparentFrameWindowStyle);
            paymentSelectDialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            Window window = paymentSelectDialog.getWindow();
            // 设置显示动画
            window.setWindowAnimations(R.style.main_menu_animstyle);
            WindowManager.LayoutParams wl = window.getAttributes();
            wl.x = 0;
            wl.y = getWindowManager().getDefaultDisplay().getHeight();
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

    private void buildDialog(boolean result, String payid) {
        TextView txt_payresult, txt_payid, txt_payfee, txt_toorder, btn_complete;
        ImageView iv_result;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.lo_commitorder_msg_dialog, null, false);
        txt_payresult = (TextView) v.findViewById(R.id.txt_payresult);
        txt_payid = (TextView) v.findViewById(R.id.txt_payid);
        txt_payfee = (TextView) v.findViewById(R.id.txt_payfee);
        iv_result = (ImageView) v.findViewById(R.id.iv_result);
        txt_toorder = (TextView) v.findViewById(R.id.txt_toorder);
        btn_complete = (TextView) v.findViewById(R.id.btn_complete);
        txt_toorder.setOnClickListener(this);
        btn_complete.setOnClickListener(this);
        builder.setView(v);
        dialog = builder.create();

        txt_payresult.setText(result ? "支付订单成功" : "支付订单失败");
        txt_payid.setText("支付方式：" + payid);
        txt_payfee.setText("订单金额：￥" + decimalFormat.format(fee));
        if (result) {
            imageLoader.loadDrawableRS(this, R.drawable.order_success, iv_result, new IImageloader() {
                @Override
                public void onDownloadSucc(Bitmap bitmap, String c_url, View imageView, int w, int h) {
                    ((ImageView) imageView).setImageBitmap(bitmap);
                }
            }, 30, 30);
        } else {
            imageLoader.loadDrawableRS(this, R.drawable.order_error, iv_result, new IImageloader() {
                @Override
                public void onDownloadSucc(Bitmap bitmap, String c_url, View imageView, int w, int h) {
                    ((ImageView) imageView).setImageBitmap(bitmap);
                }
            }, 30, 30);
        }
        dialog.show();
    }

    private void buildPaypwdDialog() {
        final PayPwdEditText input_pwd;
        TextView btn_dismiss, btn_ok;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.lo_paypwd_dialog, null, false);
        input_pwd = (PayPwdEditText) v.findViewById(R.id.input_pwd);
        btn_dismiss = (TextView) v.findViewById(R.id.btn_dismiss);
        btn_ok = (TextView) v.findViewById(R.id.btn_ok);
        builder.setView(v);
        btn_dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paypwdInputDialog.dismiss();
            }
        });
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd = input_pwd.getText().toString().trim();
//                order.setConfirm_time(generalhelper.getStringFromDate(new Date()));
//                order.setPay_time(generalhelper.getStringFromDate(new Date()));
                order.setpay_type(2);
                //order.setPay_status(1);
                //dal.mConfirmedOrders.add(order);
                buildDialog(true, "钱包支付");
                generalhelper.ToastShow(OrderDetail.this, pwd);
                paypwdInputDialog.dismiss();
            }
        });
        paypwdInputDialog = builder.create();
        paypwdInputDialog.show();
    }

    private void commit() {
        //这里需要修改订单的支付状态
//        ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1015,
//                String.format("@id=22,@order_id=%s,@state=%s,@goods_type=%s,@account=%s,@diliver_time=%s,@pay_type=%s,@total=%s,@order_details=%s",
//                        order.getorder_id(), order.getstate(), order.getgoods_type(), order.getaccount(), order.getdiliver_time(), order.getpay_type(),
//                        order.gettotal(), order.getorder_details())
//                , "22"), handler);
    }

    @Override
    public void ViewClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btn_payimmediatly:
                buildPaySelection();
                break;
            case R.id.btn_deleteorder:
                //删除订单
                break;
            case R.id.btn_checkshipping:
                //查看物流
                break;
            case R.id.iv_hdfk:
                generalhelper.ToastShow(this, "选择了货到付款");
                //order.setConfirm_time(generalhelper.getStringFromDate(new Date()));
                //dal.mConfirmedOrders.add(order);
                order.setpay_type(1);
                //dal.mConfirmedOrders.add(order);
                order.setstate(0);
                buildDialog(true, "货到付款");
                commit();
                break;
            case R.id.iv_alipay:
                //generalhelper.ToastShow(this, "选择了支付宝支付");
                //order.setConfirm_time(generalhelper.getStringFromDate(new Date()));
                //order.setPay_time(generalhelper.getStringFromDate(new Date()));
                AliPay aliPay = new AliPay(this);
                aliPay.setOnPayListner(new AliPay.OnAliPayListner() {
                    @Override
                    public void done(String stat) {
                        if (stat.equals("9000")) {
                            order.setpay_type(3);
                            //order.setPay_status(1);
                            order.setstate(1);
                            //dal.mConfirmedOrders.add(order);
                            buildDialog(true, "支付宝支付");
                        } else if (stat.equals("8000")) {
                            order.setstate(0);
                            generalhelper.ToastShow(OrderDetail.this, "支付结果确认中");
                        } else {
                            order.setstate(0);
                            buildDialog(false, "支付宝支付-结果确认中");
                        }
                        commit();
                    }

                    @Override
                    public void predo() {

                    }
                });
                aliPay.Pay(order);
                break;
            case R.id.iv_wallite:
                //generalhelper.ToastShow(this, "选择了钱包支付");
                buildPaypwdDialog();
                break;
            case R.id.iv_wxpay:
                //generalhelper.ToastShow(this, "选择了微信支付");
//                order.setConfirm_time(generalhelper.getStringFromDate(new Date()));
//                order.setPay_time(generalhelper.getStringFromDate(new Date()));

                IntentFilter filter = new IntentFilter();
                filter.addAction(WXPay.payresultAction);
                registerReceiver(new wxpayreceiver(), filter);
                api = WXAPIFactory.createWXAPI(this, Constants.APP_ID, false);
                api.registerApp(Constants.APP_ID);
                WXPay wxPay = new WXPay(api);
                if (!wxPay.checkSupport()) {
                    generalhelper.ToastShow(this, "此版本的微信不支持支付功能");
                    return;
                }
                wxPay.setOrder(order);
                wxPay.Pay();
                break;
            case R.id.btn_complete:
                dialog.dismiss();
                paymentSelectDialog.dismiss();
                handler.obtainMessage(resultCodes.PAYCOMPLETE).sendToTarget();
                finish();
                break;
            case R.id.txt_toorder:
                intent = new Intent(this, OrderList.class);
                startActivityWithAnim(intent);
                finish();
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
            return new ViewHolder(getLayoutInflater().inflate(R.layout.lo_ordergoods_detail_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            BaseGoods g = list.get(position);
//            imageLoader.loadDrawableRS(CommitOrder.this, g.getDraw(), holder.iv_preview, new IImageloader() {
//                @Override
//                public void onDownloadSucc(Bitmap bitmap, String c_url, View imageView, int w, int h) {
//                    ((ImageView) imageView).setImageBitmap(bitmap);
//                }
//            }, 100, 100);
            ImageLoader.bindBitmap(g.getpic_url(), holder.iv_preview, 100, 100);
            holder.txt_name.setText(g.gettitle());
            holder.txt_price.setText("￥" +decimalFormat.format( g.getprice()));
            holder.txt_count.setText("*" + g.getcount());
        }

        @Override
        public int getItemCount() {
            return list.size();
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

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == resultCodes.PAYCOMPLETE) {
                setResult(resultCodes.PAYCOMPLETE);
                unregisterReceiver(new wxpayreceiver());
                finish();
            } else if (msg.what == 1) {
                Frame frame = (Frame) msg.obj;
                String ret = generalhelper.getSocketeStringResult(frame.strData);
                Log.i(TAG, frame.subCmd + "  " + ret);
                if (frame.subCmd == 40) {
                    String[] results = frame.strData.split("\t");
                    if (results[0].equals("0") && results[1].equals("1026")) {
                        try {
                            list=new ArrayList<>();
                            List<Vegetable> lst = new VegetableDal().getList(results[2]);
                            List<BaseGoods> goodslst = new ArrayList<>();
                            for (BaseGoods v : lst) {
                                list.add(v);
                            }
                                    dataBind();
                            //goodsMap.put(SystemUtils.getIntValue(results[3]), goodslst);
                        } catch (Exception ex) {
                            android.os.Message msg1 = new android.os.Message();
                            msg1.what = 0;
                            msg1.obj = ex.getMessage();
                            handler.sendMessage(msg1);
                        }
                    }
                }
            }
        }
    };

    class wxpayreceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("data")) {
                WXResp resp = (WXResp) intent.getSerializableExtra("data");
                switch (resp.getErrcode()) {
                    case BaseResp.ErrCode.ERR_OK:
                        generalhelper.ToastShow(OrderDetail.this, "支付成功");
                        handler.obtainMessage(resultCodes.PAYCOMPLETE).sendToTarget();
                        break;
                    case BaseResp.ErrCode.ERR_USER_CANCEL:
                        generalhelper.ToastShow(OrderDetail.this, "已取消支付");
                        break;
                    case BaseResp.ErrCode.ERR_AUTH_DENIED:
                        generalhelper.ToastShow(OrderDetail.this, "未授权");
                        break;
                    default:
                        generalhelper.ToastShow(OrderDetail.this, "未知错误");
                        break;
                }
            }
        }
    }
}
