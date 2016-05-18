package zgan.ohos.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import zgan.ohos.Contracts.IImageloader;
import zgan.ohos.Models.BaseGoods;
import zgan.ohos.Models.HightQualityServiceM;
import zgan.ohos.Models.MyOrder;
import zgan.ohos.R;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.generalhelper;

public class HightQualityDetail extends myBaseActivity implements View.OnClickListener {


    ImageView ivpreview, ivimgdesc;
    TextView txtdesc, txtprice;
    HightQualityServiceM hqs;
    ImageLoader imageLoader;
    TextView gdcount, totalpay;
    Button btncheck;
    String pageid = "1006";

    @Override
    protected void initView() {
        setContentView(R.layout.activity_hight_quality_detail);
        hqs = (HightQualityServiceM) getIntent().getSerializableExtra("hqs");
        pageid = getIntent().getStringExtra("pageid");
        if (pageid.equals("1006")) {
            TextView t = (TextView) findViewById(R.id.txt_title);
            t.setText("土特产");
        }
        if (pageid.equals("1007")) {
            TextView t = (TextView) findViewById(R.id.txt_title);
            t.setText("高端特供");
        }
        ivpreview = (ImageView) findViewById(R.id.iv_preview);
        ivimgdesc = (ImageView) findViewById(R.id.iv_imgdesc);
        txtdesc = (TextView) findViewById(R.id.txt_desc);
        txtprice = (TextView) findViewById(R.id.txt_price);

        gdcount = (TextView) findViewById(R.id.gdcount);
        totalpay = (TextView) findViewById(R.id.totalpay);
        btncheck = (Button) findViewById(R.id.btncheck);
        int maxwidth = AppUtils.getWindowSize(this).x;
        int maxheight = 5 * maxwidth;
        imageLoader = new ImageLoader();
        ivpreview.setMaxWidth(maxwidth);
        ivpreview.setMaxHeight(maxheight);
        ImageLoader.bindBitmap(hqs.getpic_url(), ivpreview, 800, 1000);
        ivimgdesc.setMaxWidth(maxwidth);
        ivimgdesc.setMaxHeight(maxheight);
        ImageLoader.bindBitmap(hqs.getdetails_url(), ivimgdesc, 800, 1000);
        txtdesc.setText(hqs.gettitle());
        txtprice.setText("￥" + String.valueOf(hqs.getprice()));

        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //gdcount.setText("商品：" + hqs.getPrice());
        totalpay.setText("合计：" + hqs.getprice());
        btncheck.setOnClickListener(this);
    }

    @Override
    public void ViewClick(View v) {
        switch (v.getId()) {
            case R.id.btncheck:
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
//                m1.setOrder_type("预定");
//                m1.setShipping_id("送货上门");
//                m1.setGoods_amount(hqs.getprice());
//                m1.setPay_fee(hqs.getprice());
//                m1.setMoney_paid(0);
//                m1.setOrder_amount(hqs.getprice());
//                m1.setAdd_time(generalhelper.getStringFromDate(calendar.getTime()));
                m1.setorder_id(m1.generateOrderId());
                m1.setaccount(PreferenceUtil.getUserName());
                m1.setdiliver_time("0");
                m1.settotal(hqs.getprice());
                //m1.setConfirm_time("2016-04-15 09:50:00");
                List<BaseGoods> goods = new ArrayList<>();
                goods.add(hqs);
                StringBuilder builder = new StringBuilder();
                String bstr = "";
                builder.append("'");
                for (BaseGoods g : goods) {
                    //'商品id_t数量_t单价_t''属性''_t''商品名称''_p'
                    builder.append(g.getproduct_id() + "_t" + g.getSelectedcount() + "_t" + g.getprice() + "_t''" + g.getspecs() + "''_t''" + g.gettitle() + "''_p");
                }
                if (builder.length() > 1)
                    bstr = builder.substring(0, builder.length() - 2);
                bstr += "'";
                m1.setorder_details(bstr);
                m1.SetGoods(goods);
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
}
