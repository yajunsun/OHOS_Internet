package zgan.ohos.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;

import zgan.ohos.Dals.Event_ProductDal;
import zgan.ohos.Models.Event_Product;
import zgan.ohos.adapters.event_ListAdapter;
import zgan.ohos.R;
import zgan.ohos.utils.generalhelper;

//import zgan.ohos.adapters.category_RightContent;

public class EventList extends myBaseActivity {

    Toolbar toolbar;
    RecyclerView rv_event_list;
    ImageView ivdisplay;
    private final static int listType = 1;//列表方式
    private final static int gridType = 2;//表格方式
    public final static int PRE = 1;
    public final static int CURRENT = 2;
    int EventId = 0;
    private int dataModel = CURRENT;
    private int showType = gridType;//当前表现方式
    event_ListAdapter adapter;
    List<Event_Product> list;
    Event_ProductDal event_productDal;
    boolean showback = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        EventId = intent.getIntExtra("EventId", 0);
        dataModel = intent.getIntExtra("data", CURRENT);
        showback = intent.getBooleanExtra("showback", false);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(showback);
        loadData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void initView() {
        setContentView(R.layout.lo_event_list);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        ivdisplay = (ImageView) findViewById(R.id.iv_display);
        rv_event_list = (RecyclerView) findViewById(R.id.rv_event_list);
        event_productDal = new Event_ProductDal();
        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void loadData() {
        toSetProgressText(getResources().getString(R.string.loading));
        toShowProgress();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try {
                    list = event_productDal.getEventProductByEvent(EventId);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            BindData();
                            toCloseProgress();
                        }
                    });
                } catch (Exception ex) {
                    //generalhelper.ToastShow(this, ex.getMessage());
                    Message msg = handler.obtainMessage();
                    msg.what = 0;
                    msg.obj = ex.getMessage();
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    private void BindData() {
        switch (showType) {
            case listType:
                adapter = new event_ListAdapter(this, list, R.layout.lo_event_grd_item);
                rv_event_list.setLayoutManager(new GridLayoutManager(this, 2));
                ivdisplay.setImageDrawable(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_view_list).sizeDp(24).color(Color.WHITE));
                showType = gridType;
                break;
            case gridType:
                adapter = new event_ListAdapter(this, list, R.layout.lo_event_lst_item);
                rv_event_list.setLayoutManager(new LinearLayoutManager(this));
                ivdisplay.setImageDrawable(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_view_comfy).sizeDp(24).color(Color.WHITE));
                showType = listType;
                break;
        }
        rv_event_list.setAdapter(adapter);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.enter);

        //得到一个LayoutAnimationController对象；

        LayoutAnimationController lac = new LayoutAnimationController(animation);

        //设置控件显示的顺序；

        lac.setOrder(LayoutAnimationController.ORDER_NORMAL);

        //设置控件显示间隔时间；

        lac.setDelay(1);

        //为ListView设置LayoutAnimationController属性；

        rv_event_list.setLayoutAnimation(lac);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    generalhelper.ToastShow(EventList.this, msg.obj);
                    toCloseProgress();
                    break;
            }
        }
    };

    @Override
    public void ViewClick(View v) {
        switch (v.getId()) {
            case R.id.iv_display:
                BindData();
                break;
            case R.id.back:
                finish();
                break;
        }
    }
}