package zgan.ohos.Activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import zgan.ohos.Dals.HouseHolderServiceDal;
import zgan.ohos.Models.BaseGoods;
import zgan.ohos.Models.FuncBase;
import zgan.ohos.Models.HouseHolderServiceM;
import zgan.ohos.Models.MyOrder;
import zgan.ohos.R;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.generalhelper;

/**
 * create by yajunsun
 *
 * 服务预约界面
 * */
public class HouseHolderService extends myBaseActivity implements View.OnClickListener {
    ImageView ivpreview;
    HouseHolderServiceM m;
    HouseHolderServiceDal dal;
    boolean isLoadingMore = false;
    GridLayoutManager mLayoutManager;
    //myAdapter adapter;

    TextView txt_time, btn_immediate, txt_title;
    View btn_time_select;
    Button btncheck;
    private static final int DATE_PICKER_ID = 1;// 日期静态常量
    private static final int TIME_PICKER_ID = 2;// 时间
    String scheduldate;
    MyOrder order;
    Dialog paymentSelectDialog;
    FuncBase item;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_house_holder_service);
        item = (FuncBase) getIntent().getSerializableExtra("item");
        ivpreview = (ImageView) findViewById(R.id.iv_preview);
        dal = new HouseHolderServiceDal();

        txt_title = (TextView) findViewById(R.id.txt_title);
        txt_title.setText(item.getview_title());
        txt_time = (TextView) findViewById(R.id.txt_time);
        btn_immediate = (TextView) findViewById(R.id.btn_immediate);
        btn_immediate.setOnClickListener(this);
        btn_time_select = findViewById(R.id.btn_time_select);
        btn_time_select.setOnClickListener(this);
        btncheck = (Button) findViewById(R.id.btncheck);
        btncheck.setOnClickListener(this);

        order = new MyOrder();
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
        isLoadingMore = false;
        ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), item.gettype_id(), String.format("@id=22,@page_id=%s", item.getpage_id()), "@22"), handler);
    }

    public void loadMoreData() {
        try {
            //pageindex++;
            isLoadingMore = true;
            ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), item.gettype_id(), String.format("@id=22,@page_id=%s", item.getpage_id()), "@22"), handler);
        } catch (Exception ex) {
            generalhelper.ToastShow(this, ex.getMessage());
        }
    }

    void bindData() {
//        if (!isLoadingMore) {
//            adapter = new myAdapter();
//            rv_cakes.setAdapter(adapter);
//            rv_cakes.setLayoutManager(mLayoutManager);
//        } else
//            adapter.notifyDataSetChanged();
        if (m != null) {
            int maxwidth = AppUtils.getWindowSize(this).x;
            int maxheight = 10 * maxwidth;
            ivpreview.setMaxWidth(maxwidth);
            ivpreview.setMaxHeight(maxheight);
            //ImageLoader.bindBitmap(m.getpic_url(), ivpreview, 800, 1000);
            ImageLoader.bindBitmap(m.getdetails_url(), ivpreview, 800, 1000);
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
                Log.i(TAG, frame.subCmd + "  " + ret);

                if (frame.subCmd == 40) {
                    if (results[0].equals("0") && results[1].equals(item.gettype_id())) {
                        try {
                            if (!isLoadingMore) {
                                m = dal.getItem(results[2]);
                                if (frame.platform != 0) {
                                    addCache("40" + String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), item.gettype_id(), String.format("@id=22,@page_id=%s", item.getpage_id()),"@22"), frame.strData);
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
                        Toast.makeText(HouseHolderService.this, String.format("订单已提交，工作人员将在%s上门服务~", order.getTimeticked()), Toast.LENGTH_LONG).show();
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
                generalhelper.ToastShow(HouseHolderService.this, "选择的上门时间不得小于当前时间");
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
        txt_servicetype.setText("服务类型："+item.getview_title());
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
                List<BaseGoods> goodess = new ArrayList<>();
                goodess.add(m);
                order.SetGoods(goodess);
                order.setorder_id(order.generateOrderId());
                order.setaccount(PreferenceUtil.getUserName());
                order.settotal(m.getprice());
                order.setgoods_type(m.getgoods_type());
                order.setpay_type(1);
                order.setstate(1);
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

    @Override
    public void onClick(View v) {
        ViewClick(v);
    }
}
