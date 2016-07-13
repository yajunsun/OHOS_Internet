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
import zgan.ohos.Models.FuncBase;
import zgan.ohos.Models.HightQualityServiceM;
import zgan.ohos.Models.MyOrder;
import zgan.ohos.R;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.generalhelper;

/**
 * create by yajunsun
 *
 * 特供类商品详细页面，包括提交订单
 * */
public class HightQualityDetail extends myBaseActivity implements View.OnClickListener {


    ImageView ivpreview, ivimgdesc;
    TextView txtdesc, txtprice, txtstock;
    HightQualityServiceM hqs;
    ImageLoader imageLoader;
    TextView gdcount, totalpay;
    Button btncheck;
    FuncBase func;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_hight_quality_detail);
        hqs = (HightQualityServiceM) getIntent().getSerializableExtra("hqs");
        func = (FuncBase) getIntent().getSerializableExtra("func");
        TextView t = (TextView) findViewById(R.id.txt_title);
        t.setText(func.getview_title());
        ivpreview = (ImageView) findViewById(R.id.iv_preview);
        ivimgdesc = (ImageView) findViewById(R.id.iv_imgdesc);
        txtdesc = (TextView) findViewById(R.id.txt_desc);
        txtprice = (TextView) findViewById(R.id.txt_price);
        txtstock = (TextView) findViewById(R.id.txt_stock);

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
        txtstock.setText("库存：" + hqs.getstock());
        try {
            if (hqs.getstock() == 0) {
                btncheck.setEnabled(false);
            }
        }
        catch (Exception e){
            btncheck.setEnabled(false);
            Log.i(TAG,"库存解析错误："+e.getMessage());
        }
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
                m1.setgoods_type(hqs.getgoods_type());
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
