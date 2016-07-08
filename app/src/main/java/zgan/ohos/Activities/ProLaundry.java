package zgan.ohos.Activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
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
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import zgan.ohos.Dals.MyPakageDal;
import zgan.ohos.Models.BaseGoods;
import zgan.ohos.Models.FuncBase;
import zgan.ohos.Models.MyOrder;
import zgan.ohos.Models.MyPakage;
import zgan.ohos.R;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.generalhelper;

public class ProLaundry extends myBaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    int current_option_index = -1;
    SwipeRefreshLayout refreshview;
    ImageView iv_preview, iv_detail;
    ImageLoader imageLoader;
    ScrollView imgdetail;
    LinearLayout llprename;
    List<MyPakage> list;
    MyPakageDal dal;
    Toolbar toolbar;
    View llpreview;
    Button btncheck;
    TextView totalpay;
    View rcheck;
    View lcheck;
    TextView txt_time, btn_immediate;
    View btn_time_select;
    Button btnprecheck;
    private static final int DATE_PICKER_ID = 1;// 日期静态常量
    private static final int TIME_PICKER_ID = 2;// 时间
    String scheduldate;

    Dialog paymentSelectDialog;
    //    Dialog dialog;
//    Dialog paypwdInputDialog;
    double fee = 0;
    MyOrder order;
    int index = 0;

    float density;
     FuncBase item;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_pro_laundry);
        item=(FuncBase)getIntent().getSerializableExtra("item");
        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        dal = new MyPakageDal();
        order = new MyOrder();
        density = AppUtils.getDensity(this);
        //list = dal.getList();
        imageLoader = new ImageLoader();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        llpreview = findViewById(R.id.llpreview);
        iv_preview = (ImageView) findViewById(R.id.iv_preview);
        iv_detail = (ImageView) findViewById(R.id.iv_detail);
        imgdetail = (ScrollView) findViewById(R.id.imgdetail);
        llprename = (LinearLayout) findViewById(R.id.llprename);
        refreshview = (SwipeRefreshLayout) findViewById(R.id.refreshview);
        refreshview.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
                //adapter.notifyDataSetChanged();
            }
        });

        txt_time = (TextView) findViewById(R.id.txt_time);
        btn_immediate = (TextView) findViewById(R.id.btn_immediate);
        btn_immediate.setOnClickListener(this);
        btn_time_select = findViewById(R.id.btn_time_select);
        btn_time_select.setOnClickListener(this);
        btnprecheck = (Button) findViewById(R.id.btnprecheck);
        btnprecheck.setOnClickListener(this);
        lcheck = findViewById(R.id.l_check);
        rcheck = findViewById(R.id.check);
        btncheck = (Button) findViewById(R.id.btncheck);
        btncheck.setOnClickListener(this);
        totalpay = (TextView) findViewById(R.id.totalpay);

        toSetProgressText(getResources().getString(R.string.loading));
        toShowProgress();
        loadData();
    }

    protected void loadData() {
        refreshview.setRefreshing(true);
        ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), item.gettype_id(), String.format("@id=22,@page_id=%s",item.getpage_id()), "22"), handler);
    }

    void bindData() {
        int h = (int) (250 * density / list.size());
        for (int i = 0; i < list.size(); i++) {
            TextView t = new TextView(this);
            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h);
            params.leftMargin = 10;
            params.rightMargin = 10;
            params.topMargin = 10;
            t.setLayoutParams(params);
            t.setClickable(true);
            t.setGravity(Gravity.CENTER);
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            t.setOnClickListener(new pakageSelected(i));
            t.setText(list.get(i).gettitle());
            t.setTextColor(getResources().getColor(R.color.solid_black));
            llprename.addView(t);
        }
        setTabSelection(0);
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
                    if (results[0].equals("0") && results[1].equals("1008")) {
                        try {
                            list = dal.getList(results[2]);
                            if (frame.platform != 0) {
                                addCache("40" +  String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), item.gettype_id(),String.format("@id=22,@page_id=%s",item.getpage_id()), "22"), frame.strData);
                            }
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
                    } else if (results[0].equals("0") && results[1].equals("1015")) {
                        Toast.makeText(ProLaundry.this, String.format("订单已提交，工作人员将在%s上门取衣~",order.getTimeticked()), Toast.LENGTH_LONG).show();
                        finish();
                    }
                    refreshview.setRefreshing(false);
                }
                toCloseProgress();
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
                generalhelper.ToastShow(ProLaundry.this, "选择的上门时间不得小于当前时间");
            } else {
                //txt_time.setText(scheduldate + " " + hour + ":" + minute);
                txt_time.setText(generalhelper.getStringFromDate(time, "yyyy-MM-dd HH:mm"));
                order.setdiliver_time(generalhelper.getStringFromDate(time, "yyyyMMddHHmm"));
                btn_immediate.setTextColor(getResources().getColor(R.color.solid_black));
                txt_time.setTextColor(getResources().getColor(R.color.primary));
            }
//            order.setdiliver_time(scheduldate + " " + hour + ":" + minute
//                    + ":00");
        }
    };

    private void buildPaySelection() {
        ImageView ivprebookok, ivprebookno;
        TextView txt_servicetype, txt_servicetime;
        View view = getLayoutInflater().inflate(R.layout.dialog_prebook_layout,
                null);
        ivprebookok = (ImageView) view.findViewById(R.id.ivprebook_ok);
        ivprebookno = (ImageView) view.findViewById(R.id.ivprebook_no);
        txt_servicetype = (TextView) view.findViewById(R.id.txt_servicetype);
        txt_servicetime = (TextView) view.findViewById(R.id.txt_servicetime);
        BaseGoods goods = list.get(index);
        txt_servicetype.setText("服务类型："+goods.gettitle());
        if (order.getdiliver_time() == null || order.getdiliver_time().equals(""))
            txt_servicetime.setText("上门时间：即时上门");
        else
            txt_servicetime.setText("上门时间：" + txt_time.getText());
        ivprebookok.setOnClickListener(this);
        ivprebookno.setOnClickListener(this);
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
        paymentSelectDialog.show();
    }

    @Override
    public void onClick(View v) {
        ViewClick(v);
    }

    class pakageSelected implements View.OnClickListener {

        int myindex = 0;

        public pakageSelected(int _index) {
            myindex = _index;
        }

        @Override
        public void onClick(View v) {
            index = myindex;
            setTabSelection(myindex);
        }
    }

    @Override
    public void ViewClick(View v) {
        Intent intent = null;
        refreshview.setRefreshing(false);
        switch (v.getId()) {
            case R.id.btncheck:
                BaseGoods goods = list.get(index);
                order = new MyOrder();
                intent = new Intent(this, CommitOrder.class);
                List<BaseGoods> goodess = new ArrayList<>();
                goodess.add(goods);
                order.SetGoods(goodess);
                order.setorder_id(order.generateOrderId());
                order.setaccount(PreferenceUtil.getUserName());
                order.setdiliver_time("0");//(generalhelper.getStringFromDate(bestshippingdate.getTime()));
                order.settotal(goods.getprice());
                if (goods.getprice() > 0) {
                    order.setgoods_type(0);
                } else {
                    order.setgoods_type(MyOrder.PROLAUNDRY);
                }
                StringBuilder builder = new StringBuilder();
                String bstr = "";
                builder.append("'");
                for (BaseGoods g : goodess) {
                    //'商品id_t数量_t单价_t''属性''_t''商品名称''_p'
                    if (g.getspecs() == null)
                        g.setspecs("无");
                    builder.append(g.getproduct_id() + "_t" + g.getSelectedcount() + "_t" + g.getprice() + "_t''" + g.getspecs().trim() + "''_t''" + g.gettitle() + "''_p");
                }
                if (builder.length() > 1)
                    bstr = builder.substring(0, builder.length() - 2);
                bstr += "'";
                order.setorder_details(bstr);
                Bundle bundle = new Bundle();
                bundle.putSerializable("order", order);
                intent.putExtras(bundle);
                startActivityWithAnim(intent);
                break;
            case R.id.btnprecheck:
                buildPaySelection();
                break;
            case R.id.btn_time_select:
                showDialog(DATE_PICKER_ID);
                break;
            case R.id.btn_immediate:
                btn_immediate.setTextColor(getResources().getColor(R.color.primary));
                order.setdiliver_time("0");
                txt_time.setTextColor(getResources().getColor(R.color.solid_black));
                txt_time.setText("选择时间");
                break;
            case R.id.ivprebook_ok:
                List<BaseGoods> goodess1 = new ArrayList<>();
                BaseGoods goods1 = list.get(index);
                goodess1.add(goods1);
                order.SetGoods(goodess1);
                order.setorder_id(order.generateOrderId());
                order.setaccount(PreferenceUtil.getUserName());
                order.settotal(goods1.getprice());
                order.setgoods_type(MyOrder.PROLAUNDRY);
                order.setpay_type(1);
                order.setstate(1);
                StringBuilder builder1 = new StringBuilder();
                String bstr1 = "";
                builder1.append("'");
                for (BaseGoods g : goodess1) {
                    //'商品id_t数量_t单价_t''属性''_t''商品名称''_p'
                    if (g.getspecs() == null)
                        g.setspecs("无");
                    builder1.append(g.getproduct_id() + "_t" + g.getSelectedcount() + "_t" + g.getprice() + "_t''" + g.getspecs().trim() + "''_t''" + g.gettitle() + "''_p");
                }
                if (builder1.length() > 1)
                    bstr1 = builder1.substring(0, builder1.length() - 2);
                bstr1 += "'";
                order.setorder_details(bstr1);
                if (order.getdiliver_time() == null || order.getdiliver_time().equals("")) {
                    order.setdiliver_time("0");
                }
                ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1015,
                        String.format("@id=22,@order_id=%s,@state=%s,@goods_type=%s,@account=%s,@diliver_time=%s,@pay_type=%s,@total=%s,@order_details=%s",
                                order.getorder_id(), order.getstate(), order.getgoods_type(), order.getaccount(), order.getdiliver_time(), order.getpay_type(),
                                order.gettotal(), order.getorder_details())
                        , "22"), handler);
                break;
            case R.id.ivprebook_no:
                paymentSelectDialog.dismiss();
                break;
        }
    }

    void initialOptions() {
        for (int i = 0; i < llprename.getChildCount(); i++) {
            View child = llprename.getChildAt(i);
            if (Build.VERSION.SDK_INT >= 16) {
                child.setBackground(getResources().getDrawable(R.drawable.bg_rect_border));
                child.setBackground(getResources().getDrawable(R.drawable.bg_rect_border));
                child.setBackground(getResources().getDrawable(R.drawable.bg_rect_border));
                child.setBackground(getResources().getDrawable(R.drawable.bg_rect_border));
                child.setBackground(getResources().getDrawable(R.drawable.bg_rect_border));
            } else {
                child.setBackgroundColor(getResources().getColor(R.color.solid_white));
                child.setBackgroundColor(getResources().getColor(R.color.solid_white));
                child.setBackgroundColor(getResources().getColor(R.color.solid_white));
                child.setBackgroundColor(getResources().getColor(R.color.solid_white));
                child.setBackgroundColor(getResources().getColor(R.color.solid_white));
            }
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(getResources().getColor(R.color.solid_black));
            }
        }

    }

    private void setTabSelection(int _index) {
        Log.v(TAG, String.valueOf(_index));
        if (current_option_index != _index) {
            initialOptions();
            TextView t;
            View v = llprename.getChildAt(_index);
            if (v instanceof TextView) {
                t = (TextView) v;

                t.setTextColor(getResources().getColor(R.color.primary_light));

                if (Build.VERSION.SDK_INT >= 16)
                    v.setBackground(getResources().getDrawable(R.drawable.bg_rect_border));
                else
                    v.setBackgroundColor(getResources().getColor(R.color.transparent80));

//                if (list.get(_index).getdesc() != null && !list.get(_index).getdesc().equals("")) {
//                    show_txt();
//                    pkg_name.setText(t.getText());
//                    pkg_desc.setText(list.get(_index).getdesc());
//                    pkg_price.setText("￥" + list.get(_index).getprice() + "/月");
//                } else
                if (!list.get(_index).getdetails_url().equals("")) {
                    int maxwidth = AppUtils.getWindowSize(this).x;
                    int maxheight = 5 * maxwidth;
                    iv_detail.setMaxWidth(maxwidth);
                    iv_detail.setMaxHeight(maxheight);
                    ImageLoader.bindBitmap(list.get(_index).getdetails_url(), iv_detail, 800, 1200);
                }
                ImageLoader.bindBitmap(list.get(_index).getpic_url(), iv_preview, 300, 300);

                fee = list.get(_index).getprice();
                totalpay.setText("单价：￥" + list.get(_index).getprice());
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                if (fee > 0) {
                    lcheck.setVisibility(View.GONE);
                    rcheck.setVisibility(View.VISIBLE);
                    params.setMargins(0, toolbar.getHeight() + llpreview.getHeight(), 0, (int)(60*density));
                } else {
                    lcheck.setVisibility(View.VISIBLE);
                    rcheck.setVisibility(View.GONE);
                    params.setMargins(0, toolbar.getHeight() + llpreview.getHeight(), 0, (int)(100*density));
                }
                imgdetail.setLayoutParams(params);
                current_option_index = _index;
            }
        }
    }

}
