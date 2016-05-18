package zgan.ohos.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import zgan.ohos.Dals.EventDal;
import zgan.ohos.Dals.Event_ProductDal;
import zgan.ohos.Dals.PartinDal;
import zgan.ohos.Dals.Product_PicsDal;
import zgan.ohos.Models.Event;
import zgan.ohos.Models.Product;
import zgan.ohos.Models.Product_Pics;
import zgan.ohos.R;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.generalhelper;
import zgan.ohos.utils.resultCodes;

/**
 * Created by yajunsun on 2015/11/24.
 */
public class EventFrontPage extends myBaseActivity {
    TextView txt_product_name, txt_product_detail, txt_event_rule,
            txt_event_time, txt_product_price, txt_event_restrict_member,
            txt_product_unit, txt_event_preMoney, txt_rest_member;
    Button btn_order, btn_call;
    ViewPager advPager;
    LinearLayout viewGoup;
    View ll_pre_check_money;
    boolean isContinue = true;
    List<Event> events = null;
    Product_PicsDal product_picsDal = null;
    EventDal eventDal = null;
    Event_ProductDal event_productDal = null;
    PartinDal partinDal;
    List<ImageView> imageViews = new ArrayList<>();
    private AtomicInteger what = new AtomicInteger(0);
    private ImageLoader imageLoader;
    Toolbar toolbar;
    Event currentevent;
    int productId = 0;
    Product currentproduct;
    int restcount = 0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == resultCodes.EVENT_CHECK) {
            getRestCount();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meventfront, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.m_more) {
            Intent intent = new Intent(this, EventList.class);
            intent.putExtra("data", EventList.CURRENT);
            intent.putExtra("showback", true);
            startActivity(intent);
        } else if (item.getItemId() == R.id.m_pre) {
            Intent intent = new Intent(this, EventList.class);
            intent.putExtra("data", EventList.PRE);
            intent.putExtra("showback", true);
            startActivity(intent);
        }
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void initView() {
        setContentView(R.layout.lo_event_frontpage);
        Intent intent = getIntent();
        if (intent.hasExtra("currentevent"))
            currentevent = (Event) intent.getSerializableExtra("currentevent");
        if (intent.hasExtra("currentproduct"))
            currentproduct = (Product) intent.getSerializableExtra("currentproduct");
        //productId=intent.getIntExtra("productId",0);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        btn_order = (Button) findViewById(R.id.btn_order);
        txt_product_name = (TextView) findViewById(R.id.txt_product_name);
        txt_product_detail = (TextView) findViewById(R.id.txt_product_detail);
        txt_event_rule = (TextView) findViewById(R.id.txt_event_rule);
        txt_event_time = (TextView) findViewById(R.id.txt_event_time);
        txt_product_price = (TextView) findViewById(R.id.txt_product_price);
        txt_event_restrict_member = (TextView) findViewById(R.id.txt_event_restrict_member);
        txt_product_unit = (TextView) findViewById(R.id.txt_product_unit);
        txt_event_preMoney = (TextView) findViewById(R.id.txt_event_preMoney);
        txt_rest_member = (TextView) findViewById(R.id.txt_rest_member);
        advPager = (ViewPager) findViewById(R.id.adv_pager);
        viewGoup = (LinearLayout) findViewById(R.id.viewGroup);
        ll_pre_check_money = findViewById(R.id.ll_pre_check_money);
        imageLoader = new ImageLoader();
        eventDal = new EventDal();
        product_picsDal = new Product_PicsDal();
        event_productDal = new Event_ProductDal();
        partinDal = new PartinDal();
        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
//        ImageView iv_clickable1 = (ImageView) findViewById(R.id.iv_clickable1);
//        iv_clickable1.setImageDrawable(new IconicsDrawable(EventFrontPage.this, GoogleMaterial.Icon.gmd_chevron_right).sizeDp(12).color(Color.WHITE));
    }

    void loadData() {
        try {
            getRestCount();
            //List<Event_Product> event_products = event_productDal.getEventProductByEvent(currentevent.getId());
            List<Product_Pics> pics = product_picsDal.getProductPics(currentproduct.getId());

            txt_product_name.setText(currentproduct.getName());
            txt_product_detail.setText(currentproduct.getDescription());
            txt_event_rule.setText(currentevent.getErules());
            Date date = new Date();
            txt_event_time.setText(generalhelper.getStringFromDate(
                    generalhelper.getDateFromString(
                            currentevent.getBtime(), date), "yyyy-MM-dd HH:mm")
                    + "到" + generalhelper.getStringFromDate(
                    generalhelper.getDateFromString(
                            currentevent.getEtime(), date),
                    "yyyy-MM-dd HH:mm"));
            if (generalhelper.getDateFromString(currentevent.getBtime(), date).compareTo(date) > 0) {
                btn_order.setEnabled(false);
                btn_order.setText(R.string.event_frontpage_inivalied_event);
            } else if (generalhelper.getDateFromString(currentevent.getEtime(), date).compareTo(date) < 0) {
                btn_order.setEnabled(false);
                btn_order.setText(R.string.event_frontpage_overivalied_event);
            }
            txt_event_restrict_member.setText(String.valueOf(currentevent.getRestrict_members()));
            txt_product_price.setText(String.valueOf(currentproduct.getPrice()));
            txt_product_unit.setText(currentproduct.getUnit());
            txt_event_preMoney.setText(String.valueOf(currentevent.getPre_money()));
            if (currentevent.getPre_money() > 0)
                ll_pre_check_money.setVisibility(View.VISIBLE);
            else
                ll_pre_check_money.setVisibility(View.GONE);


            loadSampleImage(pics);
        } catch (Exception e) {
            generalhelper.ToastShow(this, "程序异常:" + e.getMessage());
        }
    }

    private void getRestCount() {
        try {
            restcount = partinDal.PartedinMembers(currentevent.getId());
            txt_rest_member.setText(String.valueOf(restcount));
            if (restcount >= currentevent.getRestrict_members()) {
                btn_order.setEnabled(false);
                btn_order.setText("名额已满");
            }
        } catch (Exception ex) {
            generalhelper.ToastShow(this, ex.getMessage());
        }
    }

    void loadSampleImage(List<Product_Pics> pics) {
        List<View> advPics = new ArrayList<>();
        int i = 0;
        Point p = AppUtils.getWindowSize(this);
        int window_width = p.x;
        for (Product_Pics pic : pics) {
            ImageView img = new ImageView(this);
//            imageLoader.loadImage(pic.getPicName(), img, new IImageloader() {
//                @Override
//                public void onDownloadSucc(Bitmap bitmap, String c_url, View imageView) {
//                    ((ImageView) imageView).setImageBitmap(bitmap);
//                }
//            }, window_width, 240);
            ImageLoader.bindBitmap(pic.getPicName(), img, window_width, 240);

            advPics.add(img);
            ImageView simg = new ImageView(this);
            simg.setLayoutParams(new ViewGroup.LayoutParams(20, 20));
            simg.setPadding(5, 5, 5, 5);
            if (i == 0)
                simg.setImageDrawable(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_brightness_1).color(Color.RED).sizeDp(20));
            else
                simg.setImageDrawable(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_brightness_1).color(Color.WHITE).sizeDp(20));
            imageViews.add(simg);
            viewGoup.addView(simg);
            i++;
        }
        advPager.setAdapter(new AdvAdapter(advPics));
        advPager.setOnPageChangeListener(new GuidePageChangeListener());
        advPager.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        isContinue = false;
                        break;
                    case MotionEvent.ACTION_UP:
                        isContinue = true;
                        break;
                    default:
                        isContinue = true;
                        break;
                }
                return false;
            }
        });
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    if (isContinue) {
                        viewHandler.sendEmptyMessage(what.get());
                        whatOption();
                    }
                }
            }

        }).start();
    }

    @Override
    public void ViewClick(View v) {
        switch (v.getId()) {
            case R.id.btn_order:
                Intent intent = new Intent(this, EventPreCheck.class);
                intent.putExtra("eventid", currentevent.getId());
                startActivityForResult(intent, resultCodes.EVENT_CHECK);
                break;
            case R.id.back:
                finish();
                break;
        }
    }

    private Handler viewHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            advPager.setCurrentItem(msg.what);
        }

    };

    private void whatOption() {
        what.incrementAndGet();
        if (what.get() > imageViews.size() - 1) {
            what.getAndAdd(0 - imageViews.size());
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {

        }
    }

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
                imageViews.get(arg0).setImageDrawable(new IconicsDrawable(EventFrontPage.this, GoogleMaterial.Icon.gmd_brightness_1).color(Color.RED).sizeDp(20));
                if (arg0 != i) {
                    imageViews.get(i)
                            .setImageDrawable(new IconicsDrawable(EventFrontPage.this, GoogleMaterial.Icon.gmd_brightness_1).color(Color.WHITE).sizeDp(20));
                }
            }
            what.getAndSet(arg0);

        }

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
}