package zgan.ohos.Activities;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import zgan.ohos.Contracts.UpdateCartListner;
import zgan.ohos.Dals.ShoppingCartDal;
import zgan.ohos.Dals.SuperMarketDal;
import zgan.ohos.Models.FrontItem;
import zgan.ohos.Models.SM_GoodsM;
import zgan.ohos.Models.SM_SecondaryM;
import zgan.ohos.Models.ShoppingCartSummary;
import zgan.ohos.Models.SuperMarketM;
import zgan.ohos.R;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.utils.Add2cartAnimUtil;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;
import zgan.ohos.utils.generalhelper;
import zgan.ohos.utils.resultCodes;

/**
 * create by yajunsun
 * <p/>
 * 超市购界面
 */
public class SuperMarket extends myBaseActivity implements View.OnClickListener {
    SuperMarketDal dal;
    ShoppingCartDal cartDal;
    ListView lstclass;
    RecyclerView rvproducts;
    LinearLayout llcategray1;
    LinearLayout llcategray2;
    LinearLayout llcate;
    SwipeRefreshLayout refreshview;
    sm_class_Adapter classAdapter;
    sm_product_Adapter productAdapter;
    LinearLayoutManager product_layoutManager;
    boolean isLoadingMore = false;
    List<SuperMarketM> list;
    List<SM_SecondaryM> secondarylst;
    List<SM_GoodsM> goodslst;
    List<String> mOids;
    //商品列表页码
    int pageIndex = 1;
    //一级分类选择索引
    int lastClassIndex = 0;
    //一级分类当前id
    String mCurrentClassId;
    //二级分类当前id
    String mCurrentCatId = "-1";
    //二级分类的宽度
    int catParentWidth = 0;
    //屏幕密度
    float density = 1;
    //网络请求api
    OkHttpClient mOkHttpClient;
    /***
     * 购物车部分
     **/
    TextView txtcount, btncheck, txtoldtotalprice, txttotalprice;
    View rloldprice;

    int lbpxWidth = 0, lbpxHeight = 0;
    FloatingActionButton fab;
    Point MlcartIcon;
    String PageId;
    DecimalFormat decimalFormat=new DecimalFormat("0.00");

    @Override
    protected void initView() {
        setContentView(R.layout.activity_super_market);
        product_layoutManager = new LinearLayoutManager(SuperMarket.this);
        PageId = ((FrontItem) getIntent().getSerializableExtra("item")).getpage_id().replace("'", "");
        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        View rlsearch = findViewById(R.id.rl_search);
        rlsearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SuperMarket.this, SMSearchResult.class);
                startActivityWithAnim(intent);
            }
        });
        View btncheck = findViewById(R.id.btn_check);
        btncheck.setOnClickListener(this);
        //TextView txtcount,txtoldtotalprice,txt_totalprice;
        //View rl_oldprice;
        txtcount = (TextView) findViewById(R.id.txt_count);
        txtoldtotalprice = (TextView) findViewById(R.id.txt_oldtotalprice);
        txttotalprice = (TextView) findViewById(R.id.txt_totalprice);
        rloldprice = findViewById(R.id.rl_oldprice);

        fab = (FloatingActionButton) findViewById(R.id.img_icon);
        fab.setOnClickListener(this);
        catParentWidth = (AppUtils.getWindowSize(SuperMarket.this).x / 4) * 3;
        dal = new SuperMarketDal();
        cartDal = new ShoppingCartDal();
        density = AppUtils.getDensity(SuperMarket.this);
        lbpxWidth = Math.round(40 * density);
        lbpxHeight = Math.round(20 * density);
        lstclass = (ListView) findViewById(R.id.lst_class);
        lstclass.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != lastClassIndex) {
                    for (SuperMarketM m : list) {
                        m.setIsSelected(0);
                    }
                    llcategray1.removeAllViews();
                    llcategray2.removeAllViews();
                    list.get(i).setIsSelected(1);
                    lastClassIndex = i;
                    mCurrentClassId = list.get(i).getid();
                    mCurrentCatId = "-1";
                    bindData();
                    pageIndex = 1;
                    getCatProducts();
                }
            }
        });

        rvproducts = (RecyclerView) findViewById(R.id.rv_products);
        refreshview = (SwipeRefreshLayout) findViewById(R.id.refreshview);

        llcate = (LinearLayout) findViewById(R.id.ll_cate);
        llcategray1 = (LinearLayout) findViewById(R.id.ll_categray1);
        llcategray2 = (LinearLayout) findViewById(R.id.ll_categray2);
        loadData();
    }

    //从网络获取数据
    protected void loadData() {
        toSetProgressText();
        toShowProgress();

        mOkHttpClient = new OkHttpClient();
        //创建一个Request
        FormEncodingBuilder builder = new FormEncodingBuilder();
        builder.add("ID", PageId);
        builder.add("account", PreferenceUtil.getUserName());
        builder.add("token", SystemUtils.getNetToken());
        final Request request = new Request.Builder()
                .url(String.format("%s/V1_0/marketlist.aspx", SystemUtils.getAppurl())).post(builder.build())
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

    //加载更多
    void loadMoreData() {
        pageIndex++;
        getCatProducts();
    }

    //绑定数据
    void bindData() {
        if (list != null && list.size() > 0) {

            mCurrentClassId = list.get(lastClassIndex).getid();
            secondarylst = list.get(lastClassIndex).getcategory();
        } else {
            list = new ArrayList<>();
        }
        bindClass();
        if (secondarylst != null && secondarylst.size() > 0) {

            goodslst = secondarylst.get(0).getlist();
        } else {
            secondarylst = new ArrayList<>();
        }
        bindSecodary();
        mCurrentCatId = "-1";
        if (goodslst != null && goodslst.size() > 0) {
            MlcartIcon = new Point();
            MlcartIcon.set(Math.round(fab.getX()), Math.round(fab.getY()));
            mOids = new ArrayList<>();
            mOids.add(goodslst.get(0).getproduct_id());
            bindProduct();
        } else {

            goodslst = new ArrayList<>();
            mOids = new ArrayList<>();
            bindProduct();
        }

    }

    //绑定一级分类
    void bindClass() {
        if (classAdapter == null) {
            list.get(0).setIsSelected(1);
            classAdapter = new sm_class_Adapter(list);
            lstclass.setAdapter(classAdapter);
        } else {
            classAdapter.notifyDataSetChanged();
        }
    }

    //绑定二级分类
    void bindSecodary() {
        Paint paint;
        Rect rect = new Rect();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //params.setMargins(50, 30, 0, 30);
        int marginl = getResources().getInteger(R.integer.margin_left);
        int margint = getResources().getInteger(R.integer.margin_top);
        params.setMargins(marginl, margint, 0, margint);
        int usedWidth = marginl;
        //float txtparm=Float.parseFloat(getResources().getString(R.string.sd_txt_parm));
        LinearLayout parent = llcategray1;
        int txtHeight = Math.round(marginl * density);
        //float textsize=getResources().getInteger(R.integer.sd_txt_size)*density;
        for (SM_SecondaryM cat : secondarylst) {
            TextView txt = new TextView(SuperMarket.this);
            txt.setLayoutParams(params);
            txt.setText(cat.getname());
            txt.setPadding(5, 0, 5, 0);
            //txt.setTextSize(textsize);
            txt.setMinHeight(txtHeight);
            txt.setGravity(Gravity.CENTER_VERTICAL);
            txt.setTag(cat.getid());
            paint = txt.getPaint();
            paint.getTextBounds(cat.getname(), 0, cat.getname().length(), rect);
            float strw = paint.measureText(cat.getname());
            Log.i("suntest", String.valueOf(strw));
            txt.setClickable(true);
            if (cat.getid().equals("-1")) {
                if (android.os.Build.VERSION.SDK_INT > 15)
                    txt.setBackground(getResources().getDrawable(R.drawable.bg_primary_rectangle_border));
                txt.setTextColor(getResources().getColor(R.color.primary));
            } else {
                if (android.os.Build.VERSION.SDK_INT > 15)
                    txt.setBackground(getResources().getDrawable(R.drawable.bg_normal_rectangle_border));
                txt.setTextColor(getResources().getColor(R.color.color_sm_normal_txt));
            }
            txt.setOnClickListener(new catOnclick(cat));
            //usedWidth += marginl + (cat.getname().length() * textsize*txtparm);
            usedWidth += marginl + (strw);
            if (catParentWidth - usedWidth < marginl) {
                if (parent.getId() == llcategray2.getId())
                    break;
                parent = llcategray2;
                usedWidth = marginl + (cat.getname().length() * marginl);
            }
            parent.addView(txt);
        }
    }

    //绑定商品列表
    void bindProduct() {

        if (productAdapter == null) {
            if (goodslst == null || goodslst.size() == 0) {
                generalhelper.ToastShow(SuperMarket.this, "没有更多商品啦~");
            }
            productAdapter = new sm_product_Adapter();
            rvproducts.setLayoutManager(product_layoutManager);
            rvproducts.setAdapter(productAdapter);
            rvproducts.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (dy > 0) {
                        int lastVisibleItem = product_layoutManager.findLastVisibleItemPosition();
                        int totalItemCount = product_layoutManager.getItemCount();
                        //lastVisibleItem >= totalItemCount - 4 表示剩下4个item自动加载，各位自由选择
                        // dy>0 表示向下滑动
                        if (lastVisibleItem == totalItemCount - 1 && isLoadingMore == false) {
                            loadMoreData();//这里多线程也要手动控制isLoadingMore
                            isLoadingMore = true;
                        }
                    }
                }
            });

            refreshview.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    pageIndex = 1;
                    isLoadingMore = false;
                    getCatProducts();
                    //adapter.notifyDataSetChanged();

                }
            });
        } else {
            productAdapter.notifyDataSetChanged();
        }
        isLoadingMore = false;
        refreshview.setRefreshing(false);
    }

    //加载购物车数据
    void loadShoppingCart() {
        UpdateCartListner lstner = new UpdateCartListner() {
            @Override
            public void onFailure() {
                generalhelper.ToastShow(SuperMarket.this, "服务器错误!");
            }

            @Override
            public void onResponse(String data) {
                if (!data.isEmpty()) {
                    try {
                        String result = new JSONObject(data).get("result").toString();
                        String errmsg = new JSONObject(data).get("msg").toString();
                        //获取数据并绑定数据
                        if (result.equals("0")) {
                            //list = dal.getGoodsList(data);
                            List<zgan.ohos.Models.ShoppingCartM> lst = cartDal.getList(data);
                            cartDal.syncCart(lst);
                            ShoppingCartSummary summary = cartDal.getSCSummary();
                            Message msg = handler.obtainMessage();
                            msg.what = 3;
                            msg.obj = summary;
                            msg.sendToTarget();

                        } else if (!errmsg.isEmpty()) {
                            generalhelper.ToastShow(SuperMarket.this, "服务器错误:" + errmsg);
                        }
                    } catch (JSONException jse) {

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        cartDal.getCartList(lstner);
    }

    //绑定购物车数据
    void bindShoppingCard(ShoppingCartSummary summary) {
        txtcount.setText(summary.getTotalcount());
        txttotalprice.setText("合计 ￥" + summary.getTotalprice());
        if (!summary.getOldtotalprice().equals("0")) {
            txtoldtotalprice.setText("￥" + summary.getOldtotalprice());
            rloldprice.setVisibility(View.VISIBLE);
        } else {
            rloldprice.setVisibility(View.GONE);
        }
    }

    //清除二级分类的选中样式
    void clearCatStyle() {
        int count = llcate.getChildCount();
        for (int i = 0; i < count; i++) {
            LinearLayout v = (LinearLayout) llcate.getChildAt(i);
            int c = v.getChildCount();
            for (int j = 0; j < c; j++) {
                View t = v.getChildAt(j);
                if (t instanceof TextView) {
                    TextView tv = (TextView) t;
                    if (android.os.Build.VERSION.SDK_INT > 15)
                        tv.setBackground(getResources().getDrawable(R.drawable.bg_normal_rectangle_border));
                    tv.setTextColor(getResources().getColor(R.color.color_sm_normal_txt));
                }
            }
        }
    }

    private Handler handler = new Handler() {
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
                            list = dal.getList(data);
                            bindData();
                            loadShoppingCart();
                        } else if (!errmsg.isEmpty()) {
                            generalhelper.ToastShow(SuperMarket.this, "服务器错误:" + errmsg);
                            if (errmsg.contains("时间戳")) {
                                ZganCommunityService.toGetServerData(43, PreferenceUtil.getUserName(), tokenHandler);
                            }
                        }
                    } catch (JSONException jse) {

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                toCloseProgress();
            }
            //单纯商品列表数据
            else if (msg.what == 2) {
                String data = msg.obj.toString();
                if (!data.isEmpty()) {
                    try {
                        String result = new JSONObject(data).get("result").toString();
                        String errmsg = new JSONObject(data).get("msg").toString();
                        //获取数据并绑定数据
                        if (result.equals("0")) {
                            if (pageIndex == 1) {
                                goodslst = new ArrayList<>();
                                mOids = new ArrayList<>();
                            }
                            List<SM_GoodsM> templst = dal.getGoodsList(data);
                            //goodslst = dal.getGoodsList(data);
                            if (templst.size() > 0) {
                                //判断本地是否已经有了本次获取的数据
                                if (mOids.contains(templst.get(0).getproduct_id())) {
                                    //如果存在就直接忽略此次加载,避免重复
                                    pageIndex--;
                                    return;
                                } else {
                                    //如果本地没有已有标志,则保存次页数据已存在的标志并加载显示出来
                                    mOids.add(templst.get(0).getproduct_id());
                                }
                            } else {
                                generalhelper.ToastShow(SuperMarket.this, "没有更多商品啦~");
                            }
                            goodslst.addAll(templst);
                            bindProduct();
                        } else if (!errmsg.isEmpty()) {
                            generalhelper.ToastShow(SuperMarket.this, "服务器错误:" + errmsg);
                        }
                    } catch (JSONException jse) {

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                toCloseProgress();
            } else if (msg.what == 3) {
                ShoppingCartSummary summary = (ShoppingCartSummary) msg.obj;
                bindShoppingCard(summary);
            }

        }
    };

    Handler tokenHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Frame frame = (Frame) msg.obj;
                String result = generalhelper.getSocketeStringResult(frame.strData);
                String[] results = result.split(",");
                if (frame.subCmd == 43 && results[0].equals("0")) {
                    SystemUtils.setNetToken(results[1]);
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == resultCodes.TOSHOPPINGCART) {
            ShoppingCartSummary summary = cartDal.getSCSummary();
            Message msg = handler.obtainMessage();
            msg.what = 3;
            msg.obj = summary;
            msg.sendToTarget();
        }
    }

    @Override
    public void ViewClick(View v) {
        if (v.getId() == R.id.img_icon||v.getId()==R.id.btn_check) {
            Intent intent = new Intent(SuperMarket.this, ShoppingCart.class);
            startActivityWithAnimForResult(intent, resultCodes.TOSHOPPINGCART);
        }
    }

    @Override
    public void onClick(View v) {
        ViewClick(v);
    }

    class catOnclick implements View.OnClickListener {
        SM_SecondaryM cat;

        public catOnclick(SM_SecondaryM _cat) {
            cat = _cat;
        }

        @Override
        public void onClick(View view) {
            toShowProgress();
            clearCatStyle();
            TextView v = (TextView) view;
            if (android.os.Build.VERSION.SDK_INT > 15)
                v.setBackground(getResources().getDrawable(R.drawable.bg_primary_rectangle_border));
            ((TextView) view).setTextColor(getResources().getColor(R.color.primary));
            //请求商品数据
            pageIndex = 1;
            mCurrentCatId = cat.getid();
            getCatProducts();
        }
    }

    //获取商品列表
    void getCatProducts() {
        FormEncodingBuilder builder = new FormEncodingBuilder();
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"page_id\":");
        sb.append("\"" + pageIndex + "\"");
        sb.append(",\"sub_category_id\":");
        sb.append("\"" + mCurrentClassId + "\"");
        sb.append(",\"category_id\":");
        sb.append("\"" + mCurrentCatId + "\"");
        sb.append("}");
        builder.add("account", PreferenceUtil.getUserName());
        builder.add("token", SystemUtils.getNetToken());
        builder.add("data", sb.toString());
        final Request request = new Request.Builder()
                .url(String.format("%s/V1_0/goodslist.aspx", SystemUtils.getAppurl())).post(builder.build())
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
                msg.what = 2;
                msg.obj = htmlStr;
                msg.sendToTarget();
            }
        });
    }

    UpdateCartListner cartChanged = new UpdateCartListner() {
        @Override
        public void onFailure() {
            generalhelper.ToastShow(SuperMarket.this, "加入购物车失败!");
        }

        @Override
        public void onResponse(String response) {
            ShoppingCartSummary summary = cartDal.getSCSummary();
            Message msg = handler.obtainMessage();
            msg.what = 3;
            msg.obj = summary;
            msg.sendToTarget();
            //bindShoppingCard(summary);
        }
    };

    //商品列表适配器
    class sm_product_Adapter extends RecyclerView.Adapter<sm_product_Adapter.ViewHoler> {


        @Override
        public ViewHoler onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHoler(getLayoutInflater().inflate(R.layout.lo_sm_product_item, null));
        }

        @Override
        public void onBindViewHolder(final ViewHoler holder, int position) {
            final SM_GoodsM goodsM = goodslst.get(position);
            ImageLoader.bindBitmap(goodsM.getpic_url(), holder.img_product);
            holder.txt_name.setText(goodsM.getname());
            holder.txt_price.setText("￥" + decimalFormat.format(goodsM.getprice()));
            holder.txt_oldprice1.setText("￥" + goodsM.getoldprice());
            holder.txt_oldprice2.setText("￥" + goodsM.getoldprice());
            holder.ll_oldprice1.setVisibility(View.GONE);
            holder.ll_oldprice2.setVisibility(View.GONE);
            if (!goodsM.getoldprice().equals("") && !goodsM.getoldprice().equals("0")) {
                if (goodsM.gettype_list() != null && goodsM.gettype_list().size() > 0) {
                    holder.ll_oldprice1.setVisibility(View.GONE);
                    holder.ll_oldprice2.setVisibility(View.VISIBLE);
                } else {
                    holder.ll_oldprice1.setVisibility(View.VISIBLE);
                    holder.ll_oldprice2.setVisibility(View.GONE);
                }
            }
            holder.ll_types.removeAllViews();
            if (goodsM.gettype_list() != null && goodsM.gettype_list().size() > 0) {
                int tcount = goodsM.gettype_list().size();
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        lbpxWidth, lbpxHeight);
                params.setMargins(Math.round(1 * density), 0, 0, 0);
                for (int i = 0; i < tcount; i++) {
                    ImageView iv = new ImageView(SuperMarket.this);
                    iv.setLayoutParams(params);
                    ImageLoader.bindBitmap(goodsM.gettype_list().get(i), iv, lbpxWidth, lbpxHeight);
                    holder.ll_types.addView(iv);
                }
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(SuperMarket.this, SuperMarketDetail.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("product", goodsM);
                    intent.putExtras(bundle);
                    startActivityWithAnimForResult(intent,resultCodes.TOSHOPPINGCART);
                }
            });
            //添加到购物车
            holder.btn_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (android.os.Build.VERSION.SDK_INT > 13) {
                        final ImageView imageView = new ImageView(SuperMarket.this);
                        imageView.setLayoutParams(new LinearLayout.LayoutParams(30, 60));
                        imageView.setImageDrawable(holder.img_product.getDrawable());
                        Add2cartAnimUtil mAnimUtils = new Add2cartAnimUtil(SuperMarket.this, holder.img_product.getDrawable());

                        mAnimUtils.startAnim(holder.itemView, fab);
                    }
                    cartDal.updateCart(ShoppingCartDal.ADDCART, goodsM, 1, cartChanged);

                }
            });
        }

        @Override
        public int getItemCount() {
            return goodslst.size();
        }

        class ViewHoler extends RecyclerView.ViewHolder {
            ImageView img_product, btn_add;
            TextView txt_name, txt_price, txt_oldprice1, txt_oldprice2;
            LinearLayout ll_types;
            View ll_oldprice1, ll_oldprice2;

            public ViewHoler(View itemView) {
                super(itemView);
                img_product = (ImageView) itemView.findViewById(R.id.img_product);
                btn_add = (ImageView) itemView.findViewById(R.id.btn_add);
                txt_name = (TextView) itemView.findViewById(R.id.txt_name);
                txt_price = (TextView) itemView.findViewById(R.id.txt_price);
                txt_oldprice1 = (TextView) itemView.findViewById(R.id.txt_oldprice1);
                txt_oldprice2 = (TextView) itemView.findViewById(R.id.txt_oldprice2);
                ll_types = (LinearLayout) itemView.findViewById(R.id.ll_types);
                ll_oldprice1 = itemView.findViewById(R.id.ll_oldprice1);
                ll_oldprice2 = itemView.findViewById(R.id.ll_oldprice2);
            }
        }
    }

    class sm_class_Adapter extends BaseAdapter {
        List<SuperMarketM> superMarketMs;

        public sm_class_Adapter(List<SuperMarketM> _list) {
            superMarketMs = _list;
        }

        @Override
        public int getCount() {
            return superMarketMs.size();
        }

        @Override
        public Object getItem(int i) {
            return superMarketMs.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder = null;
            SuperMarketM superMarketM = superMarketMs.get(i);
            if (view == null) {
                viewHolder = new ViewHolder();
                view = getLayoutInflater().inflate(R.layout.lo_sm_product_class, null);
                viewHolder.llselected = (LinearLayout) view.findViewById(R.id.ll_selected);
                viewHolder.txtname = (TextView) view.findViewById(R.id.txt_name);
                viewHolder.imgrecommand = (ImageView) view.findViewById(R.id.img_recommand);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.txtname.setText(superMarketM.getname());
            if (superMarketM.getrecommend().equals("0")) {
                viewHolder.imgrecommand.setVisibility(View.VISIBLE);
            } else {
                viewHolder.imgrecommand.setVisibility(View.GONE);
            }
            if (superMarketM.getIsSelected() == 1) {
                viewHolder.llselected.setVisibility(View.VISIBLE);
                view.setBackgroundColor(getResources().getColor(R.color.solid_white));
            } else {
                viewHolder.llselected.setVisibility(View.GONE);
                view.setBackgroundColor(getResources().getColor(R.color.color_sm_class_bg));
            }
            return view;
        }

        class ViewHolder {
            LinearLayout llselected;
            TextView txtname;
            ImageView imgrecommand;
        }
    }
}
