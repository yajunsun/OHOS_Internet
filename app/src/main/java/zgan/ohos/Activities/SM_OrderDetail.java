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
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import zgan.ohos.ConstomControls.MySelectCount;
import zgan.ohos.ConstomControls.PayPwdEditText;
import zgan.ohos.Contracts.IImageloader;
import zgan.ohos.Contracts.UpdateCartListner;
import zgan.ohos.Dals.SM_OrderPayDal;
import zgan.ohos.Dals.ShoppingCartDal;
import zgan.ohos.Models.SM_GoodsM;
import zgan.ohos.Models.SM_OrderPayInfo;
import zgan.ohos.Models.SM_Payway;
import zgan.ohos.Models.ShoppingCartM;
import zgan.ohos.Models.WXResp;
import zgan.ohos.R;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;
import zgan.ohos.utils.generalhelper;
import zgan.ohos.utils.resultCodes;

public class SM_OrderDetail extends myBaseActivity implements View.OnClickListener {
    
    SM_Payway payways;//支付方式，收货地址
    List<ShoppingCartM> list;
    List<SM_GoodsM> productList;
    myAdapter cAdapter;
    LinearLayoutManager cartLayoutManager;
    ShoppingCartDal cartDal;
    SM_OrderPayDal payDal;
    SM_OrderPayInfo orderPayInfo;//支付接口内容
    TextView txt_totalprice //商品总额
            , txt_downprice;//商品优惠
    RecyclerView rvcarts;//订单商品
    int payType = -1;

    boolean isCommited = false;//订单是否提交
    boolean inserted = false;//订单是否新增成功
    private IWXAPI api;//微信支付接口
    String[] mPaytypeNames = new String[]{"", "货到付款", "钱包支付", "支付宝支付", "微信支付"};//所有的支付方式
    List<Integer> mVialiabelTypes;//当前商品支持的支付方式

    TextView txt_addr /**收货地址**/
            , txt_phone /**收货人电话**/
            ;
    TextView
            txt_payfee/**付款金额**/
            ;

    /***
     * 提交
     ****/
    TextView gdcount, totalpay;
    Button btn_payimmediatly,btn_checkshipping,btn_deleteorder;
    View l_option;

    ImageLoader imageLoader;
    Dialog paymentSelectDialog;
    Dialog dialog;
    Dialog paypwdInputDialog;
    DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
    int mShipping_span = 20;
    float density;
    String mTotalprice = "0";
    String OrderSN = "";
    TextView txt_ordernum,txt_paytype,txt_ordertime;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_sm__order_detail);
        payways = (SM_Payway) getIntent().getSerializableExtra("payways");
        orderPayInfo=(SM_OrderPayInfo)getIntent().getSerializableExtra("payinfo");
        list=(List<ShoppingCartM>)getIntent().getSerializableExtra("carts");
        cartDal = new ShoppingCartDal();
        payDal = new SM_OrderPayDal();
        imageLoader = new ImageLoader();
        txt_totalprice = (TextView) findViewById(R.id.txt_totalprice);//商品总额
        txt_downprice = (TextView) findViewById(R.id.txt_downprice);//商品优惠
        txt_addr = (TextView) findViewById(R.id.txt_addr);
        txt_phone = (TextView) findViewById(R.id.txt_phone);
        txt_payfee = (TextView) findViewById(R.id.txt_payfee);
        rvcarts = (RecyclerView) findViewById(R.id.rv_cart);
        cartLayoutManager = new LinearLayoutManager(SM_OrderDetail.this);
//option
        l_option=findViewById(R.id.l_option);
        l_option.setVisibility(View.GONE);
         btn_payimmediatly=(Button)findViewById(R.id.btn_payimmediatly);
     btn_deleteorder=(Button)findViewById(R.id.btn_payimmediatly);
        btn_payimmediatly.setOnClickListener(this);
        btn_deleteorder.setOnClickListener(this);

        density = AppUtils.getDensity(SM_OrderDetail.this);
        gdcount = (TextView) findViewById(R.id.gdcount);
        totalpay = (TextView) findViewById(R.id.totalpay);
        txt_addr.setText("收货地址：" + SystemUtils.getVillage() + SystemUtils.getAddress());
        txt_ordernum=(TextView)findViewById(R.id.txt_ordernum);
        txt_ordernum.setText("订单号:"+orderPayInfo.getorder_sn());
        txt_paytype=(TextView)findViewById(R.id.txt_paytype);
        txt_paytype.setText(mPaytypeNames[ orderPayInfo.getpay_way()]);
        txt_ordertime=(TextView)findViewById(R.id.txt_ordertime);
        View back = findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SM_OrderDetail.this,MainActivity.class);
                startActivityWithAnim(intent);
            }
        });

        initialPage();
        mVialiabelTypes = payways.getpay_ways();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WXPay.payresultAction);
        registerReceiver(wxpayreceiver, filter);
//        cartDal.getCartList(new UpdateCartListner() {
//            @Override
//            public void onFailure() {
//
//            }
//
//            @Override
//            public void onResponse(String response) {
//
//                Message msg = handler.obtainMessage();
//                msg.what = 1;
//                msg.obj = response;
//                msg.sendToTarget();
//            }
//        });
        bindData();
    }

    private void initialPage() {
        txt_phone.setText("收货人电话：" + PreferenceUtil.getUserName());
        txt_addr.setText(SystemUtils.getAddress());
    }

    void bindData() {
        int viewHeight = 0;//显示商品的VIEW的高度
        double totalprice = 0, oldtotalprice = 0;
        productList = new ArrayList<>();
        for (ShoppingCartM cartM : list) {
            productList.addAll(cartM.getproductArray());
            for (SM_GoodsM goodsM : cartM.getproductArray()) {
                totalprice += goodsM.getcount() * goodsM.getprice();
                if (goodsM.getoldprice().equals("") || goodsM.getoldprice().equals("0"))
                    goodsM.setoldprice(String.valueOf(goodsM.getprice()));
                try {
                    oldtotalprice += Double.parseDouble(goodsM.getoldprice()) * goodsM.getcount();
                } catch (Exception e) {
                    oldtotalprice += goodsM.getprice() * goodsM.getcount();
                    e.printStackTrace();
                }
                viewHeight += 120;
            }
            mTotalprice = decimalFormat.format(totalprice);
            txt_totalprice.setText("￥" + mTotalprice);
            txt_downprice.setText("￥" + decimalFormat.format(oldtotalprice - totalprice));
            txt_payfee.setText("￥" + mTotalprice);
            viewHeight += 50;
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Math.round(viewHeight * density));
        params.addRule(RelativeLayout.BELOW, R.id.laddress);
        rvcarts.setLayoutParams(params);
        if (cAdapter == null) {
            rvcarts.setLayoutManager(cartLayoutManager);
            cAdapter = new myAdapter();
            rvcarts.setAdapter(cAdapter);
        } else {
            cAdapter.notifyDataSetChanged();
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

            for (int tp : mVialiabelTypes
                    ) {
                if (tp == 1)
                    iv_hdfk.setVisibility(View.VISIBLE);
                if (tp == 2)
                    iv_wallite.setVisibility(View.VISIBLE);
                if (tp == 3)
                    iv_alipay.setVisibility(View.VISIBLE);
                if (tp == 4)
                    iv_wxpay.setVisibility(View.VISIBLE);
            }

            txt_paymount = (TextView) view.findViewById(R.id.txt_paymount);
            txt_paymount.setText(txt_totalprice.getText().toString());
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

    private void buildDialog(boolean result) {
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
        txt_payid.setText("支付方式：" + mPaytypeNames[payType]);
        txt_payfee.setText("订单金额：￥" + orderPayInfo.gettotal_price());
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
//                order.setpay_type(2);
                //order.setPay_status(1);
                //dal.mConfirmedOrders.add(order);
                buildDialog(true);
                generalhelper.ToastShow(SM_OrderDetail.this, pwd);
                paypwdInputDialog.dismiss();
            }
        });
        paypwdInputDialog = builder.create();
        paypwdInputDialog.show();
    }

    private void commit() {
        payDal.CommitOrder(payways.getaddress_id(), payType, mTotalprice, productList, new UpdateCartListner() {
            @Override
            public void onFailure() {
                Message msg = handler.obtainMessage();
                msg.what = 400;
                msg.obj = "服务器返回错误";
                msg.sendToTarget();
            }

            @Override
            public void onResponse(String response) {
                Message msg = handler.obtainMessage();
                msg.what = 2;
                inserted = true;
                msg.obj = response;
                msg.sendToTarget();
            }
        });
    }

    private void recommit() {
        payDal.SecondCommit(OrderSN, payType, mTotalprice, new UpdateCartListner() {
            @Override
            public void onFailure() {
                Message msg = handler.obtainMessage();
                msg.what = 400;
                msg.obj = "服务器返回错误";
                msg.sendToTarget();
            }

            @Override
            public void onResponse(String response) {
                Message msg = handler.obtainMessage();
                msg.what = 2;
                msg.obj = response;
                msg.sendToTarget();
            }
        });
    }


    private void pay() {
        if (orderPayInfo == null || orderPayInfo == null || orderPayInfo.getpay_order_sn().equals("")) {
            return;
        }
        switch (payType) {
            case 1:
                isCommited = true;
                buildDialog(true);
                break;
            case 2:
                break;
            case 3:
                AliPay aliPay = new AliPay(this);
                aliPay.setOnPayListner(new AliPay.OnAliPayListner() {
                    @Override
                    public void done(String stat) {
                        if (stat.equals("9000")) {
//                            order.setpay_type(3);
//                            order.setstate(1);
                            paymentSelectDialog.dismiss();
                            //支付成功修改订单为已支付
                            //updateCommit();
                            isCommited = true;
                            buildDialog(true);
                        } else {
                            isCommited = false;
                            generalhelper.ToastShow(SM_OrderDetail.this, "支付失败");
                        }
                    }

                    @Override
                    public void predo() {
                    }
                });
                aliPay.Pay(orderPayInfo);
                break;
            case 4:
                toSetProgressText("正在启动微信支付请稍等");
                toShowProgress();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        api = WXAPIFactory.createWXAPI(SM_OrderDetail.this, Constants.APP_ID, false);
                        api.registerApp(Constants.APP_ID);
                        WXPay wxPay = new WXPay(api);
                        if (!wxPay.checkSupport()) {
                            handler.obtainMessage(400, "此版本的微信不支持支付功能").sendToTarget();
                            return;
                        }
                        wxPay.setOrder(orderPayInfo);
                        wxPay.Pay();
                    }
                }).start();
                break;
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
//                case 1://商品列表
//                    List<ShoppingCartM> cartMs = cartDal.getList(msg.obj.toString());
//                    list = new ArrayList<>();
//                    for (ShoppingCartM m : cartMs) {
//                        List<SM_GoodsM> goodsMs = new ArrayList<>();
//                        for (SM_GoodsM goodsM : m.getproductArray()) {
//                            if (goodsM.getSelect()) {
//                                goodsMs.add(goodsM);
//                            }
//                        }
//                        if (goodsMs.size() > 0) {
//                            m.setproductArray(goodsMs);
//                            list.add(m);
//                        }
//                    }
//                    bindData();
//                    break;
                case 2:
                    String data = msg.obj.toString();
                    //RequstResultM result = new RequstResultDal().getItem(data);
                    orderPayInfo = payDal.getPayInfo(data);
                    if (payType == 3 || payType == 4) {
                        //if (result.equals("0")) {

                        if (!orderPayInfo.getorder_sn().equals(""))
                            OrderSN = orderPayInfo.getorder_sn();

                        pay();
//                        } else {
//                            generalhelper.ToastShow(SM_OrderDetail.this, result.getmsg());
//                        }
                    } else if (payType == 1) {
                        handler.sendEmptyMessage(resultCodes.PAYCOMPLETE);
                    }
                    break;
                case resultCodes.PAYCOMPLETE:
                    paymentSelectDialog.dismiss();
                    //支付成功修改订单为已支付
                    //updateCommit();
                    isCommited = true;
                    buildDialog(true);
                    break;
                case 400:
                    String notice = msg.obj.toString();
                    if (notice != null && notice.length() > 0)
                        generalhelper.ToastShow(SM_OrderDetail.this, notice);
                    break;
            }
        }
    };

    @Override
    public void ViewClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btn_check:
                buildPaySelection();
                break;
            case R.id.iv_hdfk:
                if (!isCommited) {
                    isCommited = true;
                    generalhelper.ToastShow(this, "选择了货到付款");
                    //order.setConfirm_time(generalhelper.getStringFromDate(new Date()));
                    //dal.mConfirmedOrders.add(order);
                    //buildDialog(true);
                    payType = 1;
                    commit();
                }
                break;
            case R.id.iv_alipay:
                if (!isCommited) {
                    if (!inserted) {
                        payType = 3;
                        //先插入未支付的订单
                        commit();
                    } else {
                        payType = 3;
                        recommit();
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
                    if (!inserted) {
                        payType = 4;
                        //先插入未支付的订单
                        commit();
                    } else {
                        payType = 4;
                        recommit();
                    }
                }
                break;
            case R.id.btn_complete:
                dialog.dismiss();
                paymentSelectDialog.dismiss();
               l_option.setVisibility(View.GONE);
                break;
            case R.id.txt_toorder:
                dialog.dismiss();
                paymentSelectDialog.dismiss();
                l_option.setVisibility(View.GONE);
                break;
            case R.id.btn_payimmediatly:

                break;
            case R.id.btn_deleteorder:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        ViewClick(v);
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
            return new ViewHolder(getLayoutInflater().inflate(R.layout.lo_order_cartitem, null, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final ShoppingCartM cartM = list.get(position);
            final productAdapter pAdapter = new productAdapter(cartM.getproductArray());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    Math.round(pAdapter.getItemCount() * 120 * density));

            LinearLayoutManager layoutManager = new LinearLayoutManager(SM_OrderDetail.this);
            holder.rvproducts.setLayoutManager(layoutManager);
            holder.rvproducts.setAdapter(pAdapter);
            holder.rvproducts.setLayoutParams(params);
            holder.txtshipping.setText(cartM.getdistributionType());
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            RecyclerView rvproducts;
            TextView txtshipping;

            public ViewHolder(View itemView) {
                super(itemView);
                rvproducts = (RecyclerView) itemView.findViewById(R.id.rv_products);
                txtshipping = (TextView) itemView.findViewById(R.id.txt_shipping);
            }
        }
    }

    class productAdapter extends RecyclerView.Adapter<productAdapter.ViewHolder> {

        List<SM_GoodsM> goodsMs;

        public productAdapter(List<SM_GoodsM> _list) {
            goodsMs = _list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.lo_scart_subitem, null, false));
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final SM_GoodsM goodsM = goodsMs.get(position);
            ImageLoader.bindBitmap(goodsM.getpic_url(), holder.imgproduct);
            holder.txtname.setText(goodsM.getname());
            holder.txtspec.setText("规格:" + goodsM.getspecification());
            holder.txtprice.setText("￥" + String.valueOf(goodsM.getprice()));
            holder.selectcount.setCount(goodsM.getcount());
            holder.selectcount.setVisibility(View.GONE);
            holder.rbproduct.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return goodsMs.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox rbproduct;
            ImageView imgproduct;
            TextView txtname, txtspec, txtprice;
            LinearLayout lltypes;
            MySelectCount selectcount;

            public ViewHolder(View itemView) {
                super(itemView);
                rbproduct = (CheckBox) itemView.findViewById(R.id.rb_product);
                imgproduct = (ImageView) itemView.findViewById(R.id.img_product);
                txtname = (TextView) itemView.findViewById(R.id.txt_name);
                txtspec = (TextView) itemView.findViewById(R.id.txt_spec);
                txtprice = (TextView) itemView.findViewById(R.id.txt_price);
                lltypes = (LinearLayout) itemView.findViewById(R.id.ll_types);
                selectcount = (MySelectCount) itemView.findViewById(R.id.selectcount);
            }
        }
    }

    public BroadcastReceiver wxpayreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("data")) {
                WXResp resp = (WXResp) intent.getSerializableExtra("data");
                switch (resp.getErrcode()) {
                    case BaseResp.ErrCode.ERR_OK:
                        generalhelper.ToastShow(SM_OrderDetail.this, "支付成功");
                        handler.obtainMessage(resultCodes.PAYCOMPLETE).sendToTarget();
                        break;
                    case BaseResp.ErrCode.ERR_USER_CANCEL:
                        isCommited = false;
                        generalhelper.ToastShow(SM_OrderDetail.this, "已取消支付");
                        break;
                    case BaseResp.ErrCode.ERR_AUTH_DENIED:
                        isCommited = false;
                        generalhelper.ToastShow(SM_OrderDetail.this, "未授权");
                        break;
                    default:
                        isCommited = false;
                        generalhelper.ToastShow(SM_OrderDetail.this, "未知错误");
                        break;
                }
            }
            toCloseProgress();
        }
    };

}
