package zgan.ohos.Activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.CompoundButtonCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import zgan.ohos.Contracts.IImageloader;
import zgan.ohos.Dals.HouseHolderServiceDal;
import zgan.ohos.Models.BaseGoods;
import zgan.ohos.Models.FuncPage;
import zgan.ohos.Models.HouseHolderServiceM;
import zgan.ohos.Models.MyOrder;
import zgan.ohos.R;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.generalhelper;

public class Express_out extends myBaseActivity implements View.OnClickListener {

    ImageView iv_preview, iv_prebook;
    ImageView ivpreview;
    HouseHolderServiceM m;
    HouseHolderServiceDal dal;
    boolean isLoadingMore = false;
//    GridLayoutManager mLayoutManager;
//    myAdapter adapter;

    TextView txt_time, btn_immediate, txt_title;
    View btn_time_select, llexpresstype;
    Button btncheck;
    private static final int DATE_PICKER_ID = 1;// 日期静态常量
    private static final int TIME_PICKER_ID = 2;// 时间
    String scheduldate;
    MyOrder order;

    Dialog bookSelectDialog;
    Dialog paymentSelectDialog;
    FuncPage funcPage;
    ToggleButton tbnormal, tboverweight, tboversize;

    final static String TYPE_NORMAL = "普通", TYPE_OVERWEIGHT = "超重", TYPE_OVERSIZE = "超大";
    ArrayList<String> mExpressType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_express_out);
        funcPage = (FuncPage) getIntent().getSerializableExtra("item");
        iv_prebook = (ImageView) findViewById(R.id.iv_prebook);
        iv_prebook.setOnClickListener(this);
        iv_preview = (ImageView) findViewById(R.id.iv_preview);
        txt_title = (TextView) findViewById(R.id.txt_title);
        txt_title.setText(funcPage.getview_title());
        int maxw = AppUtils.getWindowSize(this).x;
        int maxh = 2 * maxw;
        iv_preview.setMaxWidth(maxw);
        iv_preview.setMaxHeight(maxh);
        new ImageLoader().loadDrawableRS(this, R.drawable.img_expressout_bg, iv_preview, new IImageloader() {
            @Override
            public void onDownloadSucc(Bitmap bitmap, String c_url, View imageView, int w, int h) {
                ((ImageView) imageView).setImageBitmap(bitmap);
            }
        }, 500, 800);
        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

//    protected void loadData() {
//        //isLoadingMore = false;
//        ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1011, "@id=22", "@22"), handler);
//    }
//
//    public void loadMoreData() {
//        try {
//            //pageindex++;
//            //isLoadingMore = true;
//            ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1011, "@id int", "@22"), handler);
//        } catch (Exception ex) {
//            generalhelper.ToastShow(this, ex.getMessage());
//        }
//    }

    void bindData() {
//        if (!isLoadingMore) {
//            adapter = new myAdapter();
//            rv_cakes.setAdapter(adapter);
//            rv_cakes.setLayoutManager(mLayoutManager);
//        } else
//            adapter.notifyDataSetChanged();
//        if (m != null) {
//            int maxwidth = AppUtils.getWindowSize(this).x;
//            int maxheight = 5 * maxwidth;
//            ivpreview.setMaxWidth(maxwidth);
//            ivpreview.setMaxHeight(maxheight);
//            ImageLoader.bindBitmap(m.getpic_url(), ivpreview, 800, 1000);
//        }
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
                    if (results[0].equals("0") && results[1].equals(funcPage.gettype_id())) {
                        try {
                            if (!isLoadingMore) {
                                m = dal.getItem(results[2]);
                                if (frame.platform != 0) {
                                    addCache("40" + String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), funcPage.gettype_id(), String.format("@id=22,@page_id=%s",funcPage.getpage_id()), "@22"), frame.strData);
                                }
                            } else {
                            }
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
                    } else if (results[0].equals("0") && results[1].equals("1015")) {
                        Toast.makeText(Express_out.this, String.format("订单已提交，工作人员将在%s上门取件~", order.getTimeticked()), Toast.LENGTH_LONG).show();
                        finish();
                    }
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
                generalhelper.ToastShow(Express_out.this, "选择的上门时间不得小于当前时间");
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

    private void buildBookSelection() {
        bookSelectDialog = new Dialog(this, R.style.transparentFrameWindowStyle);
        View view = getLayoutInflater().inflate(R.layout.lo_prebook, null, false);
        llexpresstype = view.findViewById(R.id.llexpresstype);
        llexpresstype.setVisibility(View.VISIBLE);
        tbnormal = (ToggleButton) view.findViewById(R.id.tb_normal);
        tboverweight = (ToggleButton) view.findViewById(R.id.tb_overweight);
        tboversize = (ToggleButton) view.findViewById(R.id.tb_oversize);

        tbnormal.setOnCheckedChangeListener(new typeToggle());
        tboverweight.setOnCheckedChangeListener(new typeToggle());
        tboversize.setOnCheckedChangeListener(new typeToggle());
        txt_time = (TextView) view.findViewById(R.id.txt_time);
        btn_immediate = (TextView) view.findViewById(R.id.btn_immediate);
        btn_immediate.setOnClickListener(this);
        btn_time_select = view.findViewById(R.id.btn_time_select);
        btn_time_select.setOnClickListener(this);
        btncheck = (Button) view.findViewById(R.id.btncheck);
        btncheck.setOnClickListener(this);
        bookSelectDialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        Window window = bookSelectDialog.getWindow();
        // 设置显示动画
        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.x = 0;
        wl.y = getWindowManager().getDefaultDisplay().getHeight();
        // 以下这两句是为了保证按钮可以水平满屏
        wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;

        // bookSelectDialog
        bookSelectDialog.onWindowAttributesChanged(wl);
        // 设置点击外围解散
        bookSelectDialog.setCanceledOnTouchOutside(true);
        bookSelectDialog.show();
    }

    private void buildPaySelection() {
        ImageView ivprebookok, ivprebookno;
        TextView txt_servicetype, txt_servicetime;
        View view = getLayoutInflater().inflate(R.layout.dialog_prebook_layout,
                null);
        ivprebookok = (ImageView) view.findViewById(R.id.ivprebook_ok);
        ivprebookno = (ImageView) view.findViewById(R.id.ivprebook_no);
        txt_servicetype = (TextView) view.findViewById(R.id.txt_servicetype);
        txt_servicetime = (TextView) view.findViewById(R.id.txt_servicetime);
        txt_servicetype.setText("服务类型："+funcPage.getview_title());
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

    class typeToggle implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.tb_normal:
                    if (tbnormal.isChecked()) {
                        tboverweight.setChecked(false);
                        tboversize.setChecked(false);
                        mExpressType.add(TYPE_NORMAL);
                    } else
                        mExpressType.remove(TYPE_NORMAL);
                    break;
                case R.id.tb_overweight:
                    if (tboverweight.isChecked()) {
                        tbnormal.setChecked(false);
                        mExpressType.add(TYPE_OVERWEIGHT);
                    } else
                        mExpressType.remove(TYPE_OVERWEIGHT);
                    break;
                case R.id.tb_oversize:
                    if (tboversize.isChecked()) {
                        tbnormal.setChecked(false);
                        mExpressType.add(TYPE_OVERSIZE);
                    } else
                        mExpressType.remove(TYPE_OVERWEIGHT);
                    break;
            }
        }
    }

    @Override
    public void ViewClick(View v) {
        switch (v.getId()) {
            case R.id.btncheck:
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
                if (mExpressType.size() == 0) {
                    generalhelper.ToastShow(Express_out.this, "请选择货物类型~");
                    break;
                }
                List<BaseGoods> goodess = new ArrayList<>();
                m = new HouseHolderServiceM();
                m.setproduct_id(MyOrder.EXPRESSOUT);
                String spec = "";
                for (String str : mExpressType
                        ) {
                    spec += str + ",";
                }
                m.setspecs(spec.substring(0, spec.length() - 1));
                m.settitle(funcPage.getview_title());
                m.setdesc(funcPage.getview_title());
                goodess.add(m);
                order.SetGoods(goodess);
                order.setorder_id(order.generateOrderId());
                order.setaccount(PreferenceUtil.getUserName());
                order.settotal(m.getprice());
                order.setgoods_type(MyOrder.GTYPEEXPOUT);
                order.setpay_type(1);
                order.setstate(1);


                StringBuilder builder = new StringBuilder();
                String bstr = "";
                builder.append("'");
                for (BaseGoods g : goodess) {
                    //'商品id_t数量_t单价_t''属性''_t''商品名称''_p'
                    if (g.getspecs() == null)
                        g.setspecs("无");
                    builder.append(g.getproduct_id() + "_t" + g.getSelectedcount() + "_t" + g.getprice() + "_t''" + g.getspecs() + "''_t''" + g.gettitle() + "''_p");
                }
                if (builder.length() > 1)
                    bstr = builder.substring(0, builder.length() - 2);
                bstr += "'";
                order.setorder_details(bstr);
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
            case R.id.iv_prebook:
                order = new MyOrder();
                buildBookSelection();
                mExpressType = new ArrayList<>();
                mExpressType.add(TYPE_NORMAL);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        ViewClick(v);
    }
}
