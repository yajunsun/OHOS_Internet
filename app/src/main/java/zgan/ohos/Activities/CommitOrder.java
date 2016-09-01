package zgan.ohos.Activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Build;
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
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import zgan.ohos.ConstomControls.PayPwdEditText;
import zgan.ohos.Contracts.IImageloader;
import zgan.ohos.Dals.MyOrderDal;
import zgan.ohos.Models.BaseGoods;
import zgan.ohos.Models.MyOrder;
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

public class CommitOrder extends myBaseActivity implements View.OnClickListener {

    boolean isCommited = false;
    boolean inserted=false;
    private IWXAPI api;
    String[] mPaytypeNames = new String[]{"", "货到付款", "钱包支付", "支付宝支付", "微信支付"};
    String[] mVialiabelTypes;// order.GetGoods().get(0).getpayment().split(",");
    //int[] mPaytypeDraws = new int[]{R.drawable.pay_weixin, R.drawable.pay_zhifubao, R.drawable.pay_qianbao, R.drawable.pay_huodaofukuan};

    TextView txt_ordernum /**订单号**/
            , txt_shipingstatus/**订单状态**/
            //, txt_householder /**收货人**/
            ;
    TextView txt_addr /**收货地址**/
            , txt_phone /**收货人电话**/
            , btnshippingimediatly/**立即送货**/
            ;
    TextView btnshippingdelay/**选择送货时间**/
            , txt_besttime/**预期送货时间**/
            , txt_shippingid/**配送方式**/
            , txt_besttime2;
    TextView
// txt_shippinger/**配送人**/
//            , txt_shippingtime/**配送时间**/
//            , txt_addtime/**下单时间**/
//            ,
            txt_payfee/**付款金额**/
            ;

    RecyclerView rv_goods;
    LinearLayout lpaytypes;
    View lshippingtime /**配送时间选择**/
            , lshippingtime2, lshipping/**配送过程**/
            , check/**提交行**/
            ;
    /***
     * 提交
     ****/
    TextView gdcount, totalpay;
    Button btncheck;

    ImageLoader imageLoader;
    MyOrder order;
    MyOrderDal dal;
    List<BaseGoods> list;
    Dialog paymentSelectDialog;
    Dialog dialog;
    Dialog paypwdInputDialog;
    double fee = 0;
    int count = 0;
    private static final int DATE_PICKER_ID = 1;// 日期静态常量
    private static final int TIME_PICKER_ID = 2;// 时间
    String scheduldate;
    DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
    int mShipping_span = 20;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_commit_order);
        imageLoader = new ImageLoader();
        txt_ordernum = (TextView) findViewById(R.id.txt_ordernum);
        txt_shipingstatus = (TextView) findViewById(R.id.txt_shipingstatus);
        //txt_householder = (TextView) findViewById(R.id.txt_householder);
        txt_addr = (TextView) findViewById(R.id.txt_addr);
        txt_phone = (TextView) findViewById(R.id.txt_phone);
        btnshippingimediatly = (TextView) findViewById(R.id.btnshippingimediatly);
        btnshippingdelay = (TextView) findViewById(R.id.btnshippingdelay);
        txt_besttime = (TextView) findViewById(R.id.txt_besttime);
        txt_besttime2 = (TextView) findViewById(R.id.txt_besttime2);
        txt_shippingid = (TextView) findViewById(R.id.txt_shippingid);
        txt_payfee = (TextView) findViewById(R.id.txt_payfee);
        rv_goods = (RecyclerView) findViewById(R.id.rv_goods);
        lpaytypes = (LinearLayout) findViewById(R.id.lpaytypes);
        lshippingtime = findViewById(R.id.lshippingtime);
        lshippingtime2 = findViewById(R.id.lshippingtime2);
        lshipping = findViewById(R.id.lshipping);

        check = findViewById(R.id.check);
        gdcount = (TextView) findViewById(R.id.gdcount);
        totalpay = (TextView) findViewById(R.id.totalpay);
        btncheck = (Button) findViewById(R.id.btncheck);
        txt_addr.setText("收货地址：" + SystemUtils.getVillage() + SystemUtils.getAddress());
        View back = findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        View ivshippingdelay = findViewById(R.id.ivshippingdelay);
        ivshippingdelay.setOnClickListener(this);
        btncheck.setText("提交订单");
        btncheck.setOnClickListener(this);
        btnshippingimediatly.setOnClickListener(this);
        btnshippingdelay.setOnClickListener(this);
        order = (MyOrder) getIntent().getSerializableExtra("order");
        if (order != null) {
            list = order.GetGoods();
            initialPage();
        }
        mVialiabelTypes = list.get(0).getpayment().split(",");
        initalShipping();
        btnshippingimediatly.setTextColor(getResources().getColor(R.color.primary));
        if (Build.VERSION.SDK_INT >= 16) {
            btnshippingimediatly.setBackground(getResources().getDrawable(R.drawable.bg_shippingtime_selected));
        }
        //ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1020, String.format("@id=22,@account=%s", PreferenceUtil.getUserName()), "22"), handler);
        buildPayView(0);
        IntentFilter filter = new IntentFilter();
        filter.addAction(WXPay.payresultAction);
        registerReceiver(wxpayreceiver, filter);
    }

    private void initialPage() {
        txt_ordernum.setText("订单号：" + order.getorder_id());
        //txt_shipingstatus.setText(getShippingStatus(order.getShipping_status()));
        //txt_householder.setText("收货人：" + order.getHouse_holder());
        txt_phone.setText("收货人：" + PreferenceUtil.getUserName());

        for (BaseGoods g : order.GetGoods()) {
            fee += g.getprice() * g.getSelectedcount();
            count += g.getSelectedcount();

            if (g.gettime() != null && !g.gettime().equals("")) {
                int span = 0;
                try {
                    span = Integer.valueOf(g.gettime());
                    if (span > mShipping_span) {
                        mShipping_span = span;
                    }
                } catch (Exception e) {
                    Log.i(TAG, "CommitOrder  Line 607 exception:" + e.getMessage());
                }
            }
        }

        Calendar c = Calendar.getInstance();
        Date now = new Date();


        //mShipping_span如果大于20分钟，用户可以选择送货上门时间
        if (mShipping_span > 20) {
            lshippingtime.setVisibility(View.GONE);
            lshippingtime2.setVisibility(View.VISIBLE);
            //当下单时间在8点到16点之间f
            if (now.getHours() > 8 && now.getHours() <= 16) {
                c.add(Calendar.MINUTE, mShipping_span);
            }
            //当下单时间在16点到24点之间
            if (now.getHours() > 16 && now.getHours() <= 24) {
                c.add(Calendar.DATE, 1);
                c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 8, 0);
                c.add(Calendar.MINUTE, mShipping_span);
            }
            //当下单时间在0点到8点之间
            if (now.getHours() > 0 && now.getHours() <= 8) {
                c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 8, 0);
                c.add(Calendar.MINUTE, mShipping_span);
            }
            txt_besttime2.setText("预计" + generalhelper.getStringFromDate(c.getTime(), "yyyy-MM-dd") + "送达");
            order.setdiliver_time(generalhelper.getStringFromDate(c.getTime(), "yyyyMMddHHmm"));
        } else {
            lshippingtime2.setVisibility(View.GONE);
            lshippingtime.setVisibility(View.VISIBLE);

            c.add(Calendar.MINUTE, mShipping_span);
            Date d = c.getTime();
            txt_besttime.setText(generalhelper.getStringFromDate(d, "yyyy-MM-dd HH:mm"));
            order.setdiliver_time(generalhelper.getStringFromDate(d, "yyyyMMddHHmm"));
        }


        gdcount.setText("商品：" + count);
        totalpay.setText("合计：￥" + decimalFormat.format(fee));
        txt_payfee.setText("￥" + decimalFormat.format(fee));
        int h = (int) (AppUtils.getDensity(this) * 120 * list.size());
        ViewGroup.LayoutParams params = rv_goods.getLayoutParams();
        params.height = h;
        rv_goods.setAdapter(new myAdapter());
        rv_goods.setLayoutManager(new LinearLayoutManager(this));
//        if (order.getpay_type() == 0) {
//            check.setVisibility(View.VISIBLE);
//            buildPayView(0);
//        } else {
//            check.setVisibility(View.GONE);
//            buildPayView(order.getpay_type());
//        }
//        if (order.getShipping_status() == 0) {
//            lshipping.setVisibility(View.GONE);
//            lshippingtime.setVisibility(View.VISIBLE);
//        } else {
//            lshipping.setVisibility(View.VISIBLE);
//            lshippingtime.setVisibility(View.GONE);
//        }
    }

    private void buildPayView(int statu) {
        float density = AppUtils.getDensity(this);
        int i = 0;
        for (String p : mVialiabelTypes
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
            t.setText(mPaytypeNames[Integer.valueOf(p)]);
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
            iv_wallite = (ImageView) view.findViewById(R.id.iv_wallite);
            iv_alipay = (ImageView) view.findViewById(R.id.iv_alipay);
            iv_wxpay = (ImageView) view.findViewById(R.id.iv_wxpay);

            for (String tp : mVialiabelTypes
                    ) {
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
            txt_paymount.setText("￥" + decimalFormat.format(fee));
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
                generalhelper.ToastShow(CommitOrder.this, pwd);
                paypwdInputDialog.dismiss();
            }
        });
        paypwdInputDialog = builder.create();
        paypwdInputDialog.show();
    }

    private void initalShipping() {
        if (Build.VERSION.SDK_INT >= 16) {
            btnshippingimediatly.setBackground(getResources().getDrawable(R.drawable.bg_rect_border));
            btnshippingdelay.setBackground(getResources().getDrawable(R.drawable.bg_rect_border));
        }
        btnshippingimediatly.setTextColor(getResources().getColor(R.color.solid_black));
        btnshippingdelay.setTextColor(getResources().getColor(R.color.solid_black));
    }

    private void commit() {
        ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1015,
                String.format("@id=22,@order_id=%s,@state=%s,@goods_type=%s,@account=%s,@diliver_time=%s,@pay_type=%s,@total=%s,@order_details=%s",
                        order.getorder_id(), order.getstate(), order.getgoods_type(), order.getaccount(), order.getdiliver_time(), order.getpay_type(),
                        order.gettotal(), order.getorder_details())
                , "22"), handler);
    }

    private void updateCommit() {
        ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1025,
                String.format("@id=22,@order_id=%s,@state=%s,@pay_type=%s",
                        order.getorder_id(), order.getstate(),order.getpay_type())
                , "22"), handler);
    }

    private void pay() {
        switch (order.getpay_type()) {
            case 1:
                break;
            case 2:
                break;
            case 3:
                AliPay aliPay = new AliPay(this);
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
                            generalhelper.ToastShow(CommitOrder.this, "支付失败");
                        }
                    }

                    @Override
                    public void predo() {
                    }
                });
                aliPay.Pay(order);
                break;
            case 4:
                toSetProgressText("正在启动微信支付请稍等");
                toShowProgress();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        api = WXAPIFactory.createWXAPI(CommitOrder.this, Constants.APP_ID, false);
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

    @Override
    public void ViewClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btncheck:
                buildPaySelection();
                break;
            case R.id.btnshippingimediatly:
                initalShipping();
                btnshippingimediatly.setTextColor(getResources().getColor(R.color.primary));
                if (Build.VERSION.SDK_INT >= 16) {
                    btnshippingimediatly.setBackground(getResources().getDrawable(R.drawable.bg_shippingtime_selected));
                }
                Calendar calendar = Calendar.getInstance();
                Calendar bestshippingdate = calendar;
                bestshippingdate.add(Calendar.MINUTE, 20);
                txt_besttime.setText(generalhelper.getStringFromDate(bestshippingdate.getTime(), "yyyy-MM-dd HH:mm"));
                order.setdiliver_time(generalhelper.getStringFromDate(bestshippingdate.getTime(), "yyyyMMddHHmm"));
                break;
            case R.id.btnshippingdelay:
            case R.id.ivshippingdelay:
                initalShipping();
                btnshippingdelay.setTextColor(getResources().getColor(R.color.primary));
                if (Build.VERSION.SDK_INT >= 16) {
                    btnshippingdelay.setBackground(getResources().getDrawable(R.drawable.bg_shippingtime_selected));
                }
                showDialog(DATE_PICKER_ID);
                break;
            case R.id.iv_hdfk:
                if (!isCommited) {
                    isCommited = true;
                    generalhelper.ToastShow(this, "选择了货到付款");
                    //order.setConfirm_time(generalhelper.getStringFromDate(new Date()));
                    //dal.mConfirmedOrders.add(order);
                    order.setpay_type(1);
                    //dal.mConfirmedOrders.add(order);
                    order.setstate(0);
                    buildDialog(true, "货到付款");
                    commit();
                }
                break;
            case R.id.iv_alipay:
                if (!isCommited) {
                    if(order.getpay_type()==0||!inserted) {
                        order.setstate(0);
                        order.setpay_type(3);
                        //先插入未支付的订单
                        commit();
                    }
                    else
                    {
                        order.setpay_type(3);
                        pay();
                    }
                }
                break;
            case R.id.iv_wallite:
                if (!isCommited) {
                    buildPaypwdDialog();
                }
                break;
            case R.id.iv_wxpay:
                if (!isCommited) {
                    isCommited = true;
                    if (order.getpay_type()==0||!inserted) {
                        order.setpay_type(4);
                        order.setstate(0);
                        //先插入未支付的订单
                        commit();
                    }
                    else
                    {
                        order.setpay_type(4);
                        pay();
                    }
                }
                break;
            case R.id.btn_complete:
                dialog.dismiss();
                paymentSelectDialog.dismiss();
                intent = new Intent(this, MainActivity.class);
                startActivityWithAnim(intent);
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

    // 监听器，用户点下set后设置日期
    DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            int month = Integer.parseInt(String.valueOf(monthOfYear)) + 1;
            // System.out.println(year + "," + monthOfYear + "," + dayOfMonth);
            // txtschedultime.setText(year + "-" + month + "-" + dayOfMonth);
            scheduldate = year + "-" + month + "-" + dayOfMonth;
            showDialog(TIME_PICKER_ID);
        }
    };
    TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

        @Override
        public void onTimeSet(TimePicker view, int hour, int minute) {
            // TODO Auto-generated method stub
            String timestr = scheduldate + " " + hour + ":" + minute + ":00";
            Date time = generalhelper.getDateFromString(timestr, new Date());
            Date nowtime = new Date();
            if (time.compareTo(nowtime) < 0) {
                generalhelper.ToastShow(CommitOrder.this, "选择的配送时间不得小于当前时间");
            } else {
                txt_besttime.setText(generalhelper.getStringFromDate(time, "yyyy-MM-dd HH:mm"));
                order.setdiliver_time(generalhelper.getStringFromDate(time, "yyyyMMddHHmm"));
            }
        }
    };

    @Override
    protected Dialog onCreateDialog(int id) {
        Calendar calendar = Calendar.getInstance();
        switch (id) {
            case DATE_PICKER_ID:
                // return new DatePickerDialog(this, onDateSetListener,
                // date.getYear(), date.getMonth(), date.getDay());
                return new DatePickerDialog(this, onDateSetListener,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
            case TIME_PICKER_ID:
                return new TimePickerDialog(this, onTimeSetListener,
                        calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE),
                        false);
        }

        return null;

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(wxpayreceiver);
        Log.i(TAG, "unregisterReceiver:wxpayreceiver");
        super.onDestroy();
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
            holder.txt_price.setText("￥" + decimalFormat.format(g.getprice()));
            holder.txt_count.setText("*" + g.getSelectedcount());
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
                order.setpay_type(4);
                order.setstate(1);
                paymentSelectDialog.dismiss();
                updateCommit();
                //dal.mConfirmedOrders.add(order);
            } else if (msg.what == 1) {
                Frame frame = (Frame) msg.obj;
                String ret = generalhelper.getSocketeStringResult(frame.strData);
                Log.i(TAG, frame.subCmd + "  " + ret);
                if (frame.subCmd == 40) {
                    String[] results = frame.strData.split("\t");
                    if (results[0].equals("0") && results[1].equals("1015")) {
                        String datastr = results[2];
                        try {
                            JSONArray jsonArray = new JSONObject(datastr)
                                    .getJSONArray("data");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject obj = (JSONObject) jsonArray.opt(i);
                                if (obj.getString("result").equals("0")) {
                                    inserted=true;
                                    pay();
                                    AppUtils.NEED_REFRESH_ORDER=true;
                                } else {
                                    isCommited = false;
                                    inserted=false;
                                    generalhelper.ToastShow(CommitOrder.this, "订单新增错误~");
                                }
                            }
                        } catch (Exception e) {
                            inserted=false;
                            isCommited=false;
                            e.printStackTrace();
                        }
                    }
                    if (results[0].equals("0") && results[1].equals("1025")) {
//                        String datastr = results[2];
//                        try {
//                            JSONArray jsonArray = new JSONObject(datastr)
//                                    .getJSONArray("data");
//                            for (int i = 0; i < jsonArray.length(); i++) {
//                                JSONObject obj = (JSONObject) jsonArray.opt(i);
//                                if (obj.getString("result").equals("0")) {
//                                    buildDialog(true, mPaytypeNames[order.getpay_type()]);
//                                } else {
//                                    isCommited = false;
//                                }
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                        buildDialog(true, mPaytypeNames[order.getpay_type()]);
                    }
                }
            } else {
                generalhelper.ToastShow(CommitOrder.this, msg.obj.toString());
            }
        }
    };

    public BroadcastReceiver wxpayreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("data")) {
                WXResp resp = (WXResp) intent.getSerializableExtra("data");
                switch (resp.getErrcode()) {
                    case BaseResp.ErrCode.ERR_OK:
                        generalhelper.ToastShow(CommitOrder.this, "支付成功");
                        handler.obtainMessage(resultCodes.PAYCOMPLETE).sendToTarget();
                        break;
                    case BaseResp.ErrCode.ERR_USER_CANCEL:
                        isCommited = false;
                        generalhelper.ToastShow(CommitOrder.this, "已取消支付");
                        break;
                    case BaseResp.ErrCode.ERR_AUTH_DENIED:
                        isCommited = false;
                        generalhelper.ToastShow(CommitOrder.this, "未授权");
                        break;
                    default:
                        isCommited = false;
                        generalhelper.ToastShow(CommitOrder.this, "未知错误");
                        break;
                }
            }
            toCloseProgress();
        }
    };
}
