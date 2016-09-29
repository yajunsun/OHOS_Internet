package zgan.ohos.Activities;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import zgan.ohos.Dals.SuperMarketDetalDal;
import zgan.ohos.Models.Advertise;
import zgan.ohos.Models.SuperMarketDetailM;
import zgan.ohos.R;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;
import zgan.ohos.utils.generalhelper;

public class SuperMarketDetail extends myBaseActivity {

    ViewPager adv_pager;
    LinearLayout pager_ind;
    List<ImageView> imageViews = new ArrayList<>();//显示图片的imageview集合
    OkHttpClient mOkHttpClient;
    String product_id;
    SuperMarketDetalDal dal;
    SuperMarketDetailM model;
    float density;
    TextView txtname,txtprice,txtoldprice,txtcountdown;
    LinearLayout lltypes;
    View lloldprice,rldetail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_super_market_detail);
        product_id = getIntent().getStringExtra("productid");
        adv_pager=(ViewPager)findViewById(R.id.adv_pager);
        pager_ind=(LinearLayout)findViewById(R.id.pager_ind);
        txtname=(TextView)findViewById(R.id.txt_name);
        txtprice=(TextView)findViewById(R.id.txt_price);
        txtoldprice=(TextView)findViewById(R.id.txt_oldprice);
        txtcountdown=(TextView)findViewById(R.id.txt_count);
        lltypes=(LinearLayout)findViewById(R.id.ll_types);
        lloldprice=findViewById(R.id.ll_oldprice2);
        rldetail=findViewById(R.id.rl_detail);
        dal=new SuperMarketDetalDal();
        density= AppUtils.getDensity(SuperMarketDetail.this);
        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        loadData();
    }

    void loadData() {
        toSetProgressText();
        toShowProgress();
        mOkHttpClient = new OkHttpClient();
        //创建一个Request
        FormEncodingBuilder builder = new FormEncodingBuilder();
        builder.add("data", "{\"product_id\":\"" + product_id + "\"}");
        builder.add("account", PreferenceUtil.getUserName());
        builder.add("token", SystemUtils.getNetToken());
        final Request request = new Request.Builder()
                .url("http://app.yumanc.1home1shop.com/V1_0/goodsinfo.aspx").post(builder.build())
                .build();
        //new call
        Call call = mOkHttpClient.newCall(request);
        //请求加入调度
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                final String htmlStr = response.body().string().replace("\\", "");
                Message msg = handler.obtainMessage();
                msg.what = 1;
                msg.obj = htmlStr;
                msg.sendToTarget();
            }
        });
    }

    void bindData() {
        //加载图片列表
        if (model.getpic_urls_list() != null) {
            List<View> advPics = new ArrayList<>();
            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            for (int i = 0; i < model.getpic_urls_list().size(); i++) {
                ImageView img = new ImageView(SuperMarketDetail.this);
                img.setLayoutParams(params);
                img.setScaleType(ImageView.ScaleType.FIT_XY);
                //取消点击功能
                //img.setOnClickListener(new adverClick(advertises.get(i)));
                ImageLoader.bindBitmap(model.getpic_urls_list().get(i), img, 500, 500);
                advPics.add(img);
                ImageView simg = new ImageView(SuperMarketDetail.this);
                simg.setLayoutParams(new ViewGroup.LayoutParams(20, 20));
                simg.setPadding(5, 5, 5, 5);
                if (i == 0)
                    simg.setImageDrawable(new IconicsDrawable(SuperMarketDetail.this, GoogleMaterial.Icon.gmd_brightness_1).color(Color.RED).sizeDp(30));
                else
                    simg.setImageDrawable(new IconicsDrawable(SuperMarketDetail.this, GoogleMaterial.Icon.gmd_brightness_1).color(Color.LTGRAY).sizeDp(30));
                imageViews.add(simg);
                pager_ind.addView(simg);
            }
            adv_pager.setAdapter(new AdvAdapter(advPics));
            adv_pager.setOnPageChangeListener(new GuidePageChangeListener());
        }
        //商品名和价格
        txtname.setText(model.getname());
        if (model.gettype_list() != null && model.gettype_list().size() > 0) {
            lltypes.setVisibility(View.VISIBLE);
            int tcount = model.gettype_list().size();
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    Math.round(40 * density), Math.round(20 * density));
            params.setMargins(Math.round(8 * density), 0, 0, 0);
            for (int i = 0; i < tcount; i++) {
                ImageView iv = new ImageView(SuperMarketDetail.this);
                iv.setLayoutParams(params);
                ImageLoader.bindBitmap(model.gettype_list().get(i), iv);
            }
        }
        txtprice.setText(String.valueOf(model.getprice()));
        if(!model.getoldprice().equals("")&&!model.getoldprice().equals("0"))
        {
            lloldprice.setVisibility(View.VISIBLE);
            txtoldprice.setText("￥"+model.getoldprice());
        }
        else
        {
            lloldprice.setVisibility(View.GONE);
        }
        txtcountdown.setText(model.getcountdown());
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //全部数据 包括一级二级分类和第一页商品数据
            if (msg.what == 1) {
                String data = msg.obj.toString();
                if (!data.isEmpty()) {
                    try {
                        String result = new JSONObject(data).get("result").toString();
                        String errmsg = new JSONObject(data).get("msg").toString();
                        //获取数据并绑定数据
                        if (result.equals("0")) {
                            model = dal.Get(data);
                            bindData();
                        } else if (!errmsg.isEmpty()) {
                            generalhelper.ToastShow(SuperMarketDetail.this, "服务器错误:" + errmsg);
                        }
                    } catch (JSONException jse) {

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                toCloseProgress();
            }

        }
    };

    @Override
    public void ViewClick(View v) {
    }

    private final class AdvAdapter extends PagerAdapter {
        private List<View> views = null;

        public AdvAdapter(List<View> views) {
            this.views = views;
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(views.get(arg1));
        }

        @Override
        public void finishUpdate(View arg0) {
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(views.get(arg1), 0);
            return views.get(arg1);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
        }
    }

    //图片滑动监听
    private final class GuidePageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int arg0) {

            for (int i = 0; i < imageViews.size(); i++) {
                imageViews.get(arg0).setImageDrawable(new IconicsDrawable(SuperMarketDetail.this, GoogleMaterial.Icon.gmd_brightness_1).color(Color.RED).sizeDp(20));
                if (arg0 != i) {
                    imageViews.get(i)
                            .setImageDrawable(new IconicsDrawable(SuperMarketDetail.this, GoogleMaterial.Icon.gmd_brightness_1).color(Color.LTGRAY).sizeDp(20));
                }
            }

        }

    }
}