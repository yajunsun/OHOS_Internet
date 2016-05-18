package zgan.ohos.Activities;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

import java.util.ArrayList;
import java.util.List;

import zgan.ohos.R;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.generalhelper;

//import com.mikepenz.actionitembadge.library.ActionItemBadge;
//import com.mikepenz.actionitembadge.library.utils.BadgeStyle;

/**
 * Created by yajunsun on 2015/11/18.
 */
public class GoodsItemDetail extends AppCompatActivity {

    IconicsImageView iv_custom_service, iv_attention, iv_shopping_car;
    View ll_custom_service, ll_attention, ll_shopping_car;
    TextView txt_custom_serviec, txt_attention, txt_shopping_car;
    String imgName;
    ImageLoader imageLoader;
    ViewPager advPager;
    LinearLayout viewGoup;
    boolean isContinue = true;
    List<ImageView> imageViews = new ArrayList<>();
    String ads[] = new String[]{
            "http://img0.wgimg.com/qqbuy/421602620/item-0000000000000000000000721921253C.0.jpg/600?564ACA22",
            "http://img1.wgimg.com/qqbuy/421602620/item-0000000000000000000000721921253C.1.jpg/600?564ACA22",
            "http://img2.wgimg.com/qqbuy/421602620/item-0000000000000000000000721921253C.2.jpg/600?564ACA22",
            "http://img3.wgimg.com/qqbuy/421602620/item-0000000000000000000000721921253C.3.jpg/600?564ACA22",
            "http://img0.wgimg.com/qqbuy/421602620/item-0000000000000000000000721921253C.4.jpg/600?564ACA22",
            "http://img1.wgimg.com/qqbuy/421602620/item-0000000000000000000000721921253C.5.jpg/600?562F3A1B"
    };
    int badgeCount = 9;
    //private BadgeStyle style = ActionItemBadge.BadgeStyles.RED.getStyle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lo_goods_item_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        imageLoader = new ImageLoader();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initailView();
        //style.setColor(Color.RED);
        //style.setTextColor(Color.WHITE);

        //ActionItemBadge.update(this, ll_shopping_car, style, badgeCount);
        imgName = getIntent().getStringExtra("imgname");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    private void initailView() {
        iv_custom_service = (IconicsImageView) findViewById(R.id.iv_custom_service);
        iv_shopping_car = (IconicsImageView) findViewById(R.id.iv_shopping_car);
        iv_attention = (IconicsImageView) findViewById(R.id.iv_attention);
        ll_custom_service = findViewById(R.id.ll_custom_service);
        ll_attention = findViewById(R.id.ll_attention);
        ll_shopping_car = findViewById(R.id.ll_shopping_car);
        txt_custom_serviec = (TextView) findViewById(R.id.txt_custom_service);
        txt_attention = (TextView) findViewById(R.id.txt_attention);
        txt_shopping_car = (TextView) findViewById(R.id.txt_shopping_car);
        advPager = (ViewPager) findViewById(R.id.adv_pager);
        viewGoup = (LinearLayout) findViewById(R.id.viewGroup);
        initViewPager();
    }

    private void initViewPager() {

        List<View> advPics = new ArrayList<>();
        int i = 0;
        Point p = AppUtils.getWindowSize(this);
        int window_width = p.x;
        for (String drawable : ads) {
            ImageView img = new ImageView(this);
//            imageLoader.loadImage(drawable, img, new IImageloader() {
//                @Override
//                public void onDownloadSucc(Bitmap bitmap, String c_url, View imageView) {
//                    ((ImageView)imageView).setImageBitmap(bitmap);
//                }
//            }, window_width, 240);
            ImageLoader.bindBitmap(drawable,img,window_width,240);

            //img.setImageDrawable(getResources().getDrawable(drawable));
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
                imageViews.get(arg0).setImageDrawable(new IconicsDrawable(GoodsItemDetail.this, GoogleMaterial.Icon.gmd_brightness_1).color(Color.RED).sizeDp(20));
                if (arg0 != i) {
                    imageViews.get(i)
                            .setImageDrawable(new IconicsDrawable(GoodsItemDetail.this, GoogleMaterial.Icon.gmd_brightness_1).color(Color.WHITE).sizeDp(20));
                }
            }
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

    public void viewClick(View view) {
        generalhelper.ToastShow(this, view.getId());
    }
}