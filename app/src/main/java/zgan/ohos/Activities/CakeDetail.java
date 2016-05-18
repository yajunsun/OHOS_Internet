package zgan.ohos.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import zgan.ohos.ConstomControls.MySelectCount;
import zgan.ohos.Contracts.IImageloader;
import zgan.ohos.Models.BaseGoods;
import zgan.ohos.Models.Cake;
import zgan.ohos.Models.MyOrder;
import zgan.ohos.R;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.generalhelper;

public class CakeDetail extends myBaseActivity implements View.OnClickListener{

    Cake cake;
    TextView txt_title, inch6, inch8, inch10, inch12, txt_desc, txt_price;
    ImageView iv_preview;
    EditText et_msg;
    ImageLoader imageLoader;
   MySelectCount input_count;
    final int INCH6 = 6, INCH8 = 8, INCH10 = 10, INCH12 = 12;
    float SELECTED_SIZE = INCH6;
    TextView gdcount, totalpay;
    Button btncheck;
    float density=1;
    int selectedcount=1;
    double totalpayamount=0;
    @Override
    protected void initView() {
        setContentView(R.layout.activity_cake_detail);
        cake = (Cake) getIntent().getSerializableExtra("cake");
        density=(int)(AppUtils.getDensity(this));
        txt_title = (TextView) findViewById(R.id.txt_title);
        inch6 = (TextView) findViewById(R.id.inch6);
        inch10 = (TextView) findViewById(R.id.inch10);
        inch8 = (TextView) findViewById(R.id.inch8);
        inch12 = (TextView) findViewById(R.id.inch12);
        iv_preview = (ImageView) findViewById(R.id.iv_preview);
        et_msg = (EditText) findViewById(R.id.et_msg);
        txt_desc = (TextView) findViewById(R.id.txt_desc);
        txt_price = (TextView) findViewById(R.id.txt_price);
        btncheck=(Button)findViewById(R.id.btncheck);
        gdcount = (TextView) findViewById(R.id.gdcount);
        totalpay = (TextView) findViewById(R.id.totalpay);
        input_count=(MySelectCount)findViewById(R.id.input_count);
        input_count.setOnchangeListener(new MySelectCount.IonChanged() {
            @Override
            public void onAddition(int count) {
                selectedcount=count;
                totalpayamount=cake.getprice()*count;
                gdcount.setText("商品："+count);
                totalpay.setText("合计："+totalpayamount);
            }

            @Override
            public void onReduction(int count) {
                selectedcount=count;
                totalpayamount=cake.getprice()*count;
                gdcount.setText("商品："+count);
                totalpay.setText("合计："+totalpayamount);
            }
        });
        btncheck.setText("购买");
        btncheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
            }
        });
        imageLoader = new ImageLoader();
        ImageLoader.bindBitmap(cake.getpic_url(),iv_preview,400,400);
        txt_desc.setText(cake.gettitle());
        txt_price.setText("￥" + String.valueOf(cake.getprice()));
        gdcount.setText("商品：" + input_count.getCount());
        totalpay.setText("合计：" + cake.getprice());
        btncheck.setOnClickListener(this);
        View back=findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        initialSIZE();
        inch6.setBackgroundColor(getResources().getColor(R.color.primary));
        SELECTED_SIZE = INCH6;
    }

    @Override
    public void ViewClick(View v) {
        switch (v.getId()) {
            case R.id.inch6:
                initialSIZE();
                inch6.setBackgroundColor(getResources().getColor(R.color.primary));
                SELECTED_SIZE = INCH6;
                break;
            case R.id.inch8:
                initialSIZE();
                inch8.setBackgroundColor(getResources().getColor(R.color.primary));
                SELECTED_SIZE = INCH8;
                break;
            case R.id.inch10:
                initialSIZE();
                inch10.setBackgroundColor(getResources().getColor(R.color.primary));
                SELECTED_SIZE = INCH10;
                break;
            case R.id.inch12:
                initialSIZE();
                inch12.setBackgroundColor(getResources().getColor(R.color.primary));
                SELECTED_SIZE = INCH12;
                break;
            case R.id.btncheck:
                Calendar calendar=Calendar.getInstance();
                Calendar bestshippingdate=calendar;
                bestshippingdate.add(Calendar.MINUTE, 20);

                Intent intent=new Intent(this,CommitOrder.class);
                MyOrder m1=new MyOrder();
                m1.setorder_id(m1.generateOrderId());
                m1.setaccount(PreferenceUtil.getUserName());
                m1.setdiliver_time("0");
                m1.settotal(totalpayamount);
//                m1.setHouse_holder("徐鹏");
//                m1.setHouser_name("金易伯爵世家4栋3单元6-2");

//                m1.setOrder_status("已确认");
//                m1.setShipping_status(0);
//                m1.setPay_status(0);
                //m1.setBest_time(generalhelper.getStringFromDate(bestshippingdate.getTime()));

//                m1.setOrder_type("订购");
//                m1.setShipping_id("送货上门");
               // m1.setPay_id(3);

                //m1.setGoods_amount(totalpayamount);
                //m1.setPay_fee(totalpayamount);

                //m1.setMoney_paid(0);
                //m1.setOrder_amount(totalpayamount);
                //m1.setAdd_time(generalhelper.getStringFromDate(calendar.getTime()));
                //m1.setConfirm_time("2016-04-15 09:50:00");
                List<BaseGoods> goods=new ArrayList<>();
                cake.setsize(SELECTED_SIZE);
                cake.setspecs(SELECTED_SIZE);
                cake.setmsg(et_msg.getText().toString().trim());
                cake.setSelectedcount(input_count.getCount());
                goods.add(cake);
                StringBuilder builder=new StringBuilder();
                String bstr="";
                builder.append("'");
                for (BaseGoods g:goods) {
                    //'商品id_t数量_t单价_t''属性''_t''商品名称''_p'
                    builder.append(g.getproduct_id()+"_t"+g.getSelectedcount()+"_t"+g.getprice()+"_t''"+g.getspecs()+"''_t''"+g.gettitle()+"''_p");
                }
                if (builder.length()>1)
                    bstr= builder.substring(0,builder.length()-2);
                bstr+="'";
                m1.setorder_details(bstr);
                m1.SetGoods(goods);
                Bundle bundle=new Bundle();
                bundle.putSerializable("order",m1);
                intent.putExtras(bundle);
                startActivityWithAnim(intent);
                break;
        }
    }

    void initialSIZE() {
        inch6.setBackgroundColor(getResources().getColor(R.color.transparent80));
        inch8.setBackgroundColor(getResources().getColor(R.color.transparent80));
        inch10.setBackgroundColor(getResources().getColor(R.color.transparent80));
        inch12.setBackgroundColor(getResources().getColor(R.color.transparent80));
    }

    @Override
    public void onClick(View v) {
        ViewClick(v);
    }
}
