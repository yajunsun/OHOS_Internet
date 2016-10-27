
package zgan.ohos.Activities;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsImageView;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import zgan.ohos.Contracts.UpdateCartListner;
import zgan.ohos.Dals.ShoppingCartDal;
import zgan.ohos.Dals.SuperMarketDal;
import zgan.ohos.Models.SM_GoodsM;
import zgan.ohos.Models.ShoppingCartSummary;
import zgan.ohos.R;
import zgan.ohos.adapters.RecyclerViewItemSpace;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.utils.Add2cartAnimUtil;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;
import zgan.ohos.utils.generalhelper;
import zgan.ohos.utils.resultCodes;

public class SMSearchResult extends myBaseActivity implements View.OnClickListener {

    boolean isLoadingMore = false;
    //商品列表页码
    int pageIndex = 1;
    //网络请求api
    OkHttpClient mOkHttpClient;
    EditText txtsearch;
    FloatingActionButton fab;

    RecyclerView rvproducts;
    GridLayoutManager product_layoutManager;
    SwipeRefreshLayout refreshview;
    List<SM_GoodsM> list;
    SuperMarketDal dal;
    ShoppingCartDal cartDal;
    productAdapter adapter;
    float density;
    int keyLayoutH;
    /***
     * 购物车部分
     **/
    TextView txtcount, btncheck, txtoldtotalprice, txttotalprice;
    View rloldprice1;

    LinearLayout llkeys;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_smsearch_result);
        txtsearch = (EditText) findViewById(R.id.txt_search);
        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        final View clear = findViewById(R.id.btn_clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtsearch.setText("");
            }
        });
        dal = new SuperMarketDal();
        cartDal = new ShoppingCartDal();
        density = AppUtils.getDensity(SMSearchResult.this);
        keyLayoutH = Math.round(density * 30);
        rvproducts = (RecyclerView) findViewById(R.id.rv_products);
        product_layoutManager = new GridLayoutManager(SMSearchResult.this, 2);
        refreshview = (SwipeRefreshLayout) findViewById(R.id.refreshview);
        refreshview.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageIndex = 1;
                isLoadingMore = false;
                loadData();
                //adapter.notifyDataSetChanged();

            }
        });
        rvproducts.setLayoutManager(product_layoutManager);
        rvproducts.addItemDecoration(new RecyclerViewItemSpace(20));
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
        View btncheck = findViewById(R.id.btn_check);
        btncheck.setOnClickListener(this);
        //TextView txtcount,txtoldtotalprice,txt_totalprice;
        //View rl_oldprice;
        txtcount = (TextView) findViewById(R.id.txt_count);
        txtoldtotalprice = (TextView) findViewById(R.id.txt_oldtotalprice);
        txttotalprice = (TextView) findViewById(R.id.txt_totalprice);
        rloldprice1 = findViewById(R.id.rl_oldprice);
        fab = (FloatingActionButton) findViewById(R.id.img_icon);
        fab.setOnClickListener(this);
        if (ShoppingCartDal.mOrderIDs == null)
            loadShoppingCart();
        else {
            final ShoppingCartSummary summary = cartDal.getSCSummary();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    bindShoppingCard(summary);
                }
            });
        }
        llkeys = (LinearLayout) findViewById(R.id.ll_keys);
        txtsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cartDal.getSearchKeys(String.valueOf(s), new UpdateCartListner() {
                    @Override
                    public void onFailure() {

                    }

                    @Override
                    public void onResponse(String response) {
                        //RequstResultM m=new RequstResultDal().getItem(response);
                        if (response.isEmpty())
                            return;
                        List<String> keys = cartDal.getStringList(response);
                        Message msg = handler.obtainMessage();
                        msg.what = 2;
                        msg.obj = keys;
                        msg.sendToTarget();
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

                llkeys.setVisibility(View.GONE);
            }
        });
        setResult(resultCodes.TOSHOPPINGCART);
    }

    //从网络获取数据
    protected void loadData() {
        toSetProgressText();
        toShowProgress();

        mOkHttpClient = new OkHttpClient();
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"search_str\":");
        sb.append("\"" + txtsearch.getText().toString().trim() + "\"");
        sb.append("}");
        //创建一个Request
        FormEncodingBuilder builder = new FormEncodingBuilder();
        builder.add("account", PreferenceUtil.getUserName());
        builder.add("token", SystemUtils.getNetToken());
        builder.add("data", sb.toString());
        final Request request = new Request.Builder()
                .url(String.format("%s/V1_0/searchgoodslist.aspx", SystemUtils.getAppurl())).post(builder.build())
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
        if (adapter == null) {
            adapter = new productAdapter();
            rvproducts.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
        isLoadingMore = false;
        refreshview.setRefreshing(false);
        llkeys.setVisibility(View.GONE);
    }

    void loadMoreData() {
    }

    //加载购物车数据
    void loadShoppingCart() {
        UpdateCartListner lstner = new UpdateCartListner() {
            @Override
            public void onFailure() {
                generalhelper.ToastShow(SMSearchResult.this, "服务器错误!");
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
                            generalhelper.ToastShow(SMSearchResult.this, "服务器错误:" + errmsg);
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
            rloldprice1.setVisibility(View.VISIBLE);
        } else {
            rloldprice1.setVisibility(View.GONE);
        }
    }

    void bindKeys(List<String> keys) {
        llkeys.removeAllViews();
        if (keys != null && keys.size() > 0) {
            int leftm=Math.round(10*density);
            RelativeLayout.LayoutParams keysPa = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, keys.size() * (keyLayoutH+10));
            LinearLayout.LayoutParams keyPa = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, keyLayoutH);
            keyPa.setMargins(leftm,0,0,0);
            keysPa.addRule(RelativeLayout.BELOW,R.id.toolbar);
            llkeys.setLayoutParams(keysPa);
            llkeys.setVisibility(View.VISIBLE);
            for (int i = 0; i < keys.size(); i++) {
                TextView tv = new TextView(SMSearchResult.this);
                tv.setLayoutParams(keyPa);
                tv.setText(keys.get(i));
                tv.setClickable(true);
                tv.setOnClickListener(new keyClick(keys.get(i)));
                llkeys.addView(tv);
            }
        }
    }

    @Override
    public void onClick(View v) {
        ViewClick(v);
    }

    class keyClick implements View.OnClickListener {
        String key;

        public keyClick(String _key) {
            key = _key;
        }

        @Override
        public void onClick(View v) {
            txtsearch.setText(key);
            llkeys.setVisibility(View.GONE);
            loadData();
        }
    }

    @Override
    public void ViewClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btn_search:
                if (txtsearch.getText().toString().trim().equals(""))
                    return;
                loadData();
                break;
            case R.id.btn_check:
                 intent = new Intent(SMSearchResult.this, ShoppingCart.class);
                startActivityWithAnimForResult(intent, resultCodes.TOSHOPPINGCART);
                break;
            case R.id.img_icon:
                 intent = new Intent(SMSearchResult.this, ShoppingCart.class);
                startActivityWithAnimForResult(intent, resultCodes.TOSHOPPINGCART);
                break;
        }
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
                            list = dal.getGoodsList(data);
                            if (list == null || list.size() == 0) {
                                generalhelper.ToastShow(SMSearchResult.this, String.format("没有找到\"%s\"相关的商品", txtsearch.getText()));
                            }
                            bindData();
                        } else if (!errmsg.isEmpty()) {
                            generalhelper.ToastShow(SMSearchResult.this, "服务器错误:" + errmsg);
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
            } else if (msg.what == 2) {
                List<String> keys = (List<String>) msg.obj;
                bindKeys(keys);
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
    UpdateCartListner cartChanged = new UpdateCartListner() {
        @Override
        public void onFailure() {
            generalhelper.ToastShow(SMSearchResult.this, "加入购物车失败!");
        }

        @Override
        public void onResponse(String response) {
            ShoppingCartSummary summary = cartDal.getSCSummary();
            Message msg = handler.obtainMessage();
            msg.what = 3;
            msg.obj = summary;
            msg.sendToTarget();
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

    class productAdapter extends RecyclerView.Adapter<productAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.sm_rearch_itme, parent, false));
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final SM_GoodsM goodsM = list.get(position);
            ImageLoader.bindBitmap(goodsM.getpic_url(), holder.ivproduct);
            holder.txtname.setText(goodsM.getname());
            holder.txtprice.setText("￥" + goodsM.getprice());
            if (goodsM.getoldprice().equals("") || goodsM.getoldprice().equals("0")) {
                holder.rloldprice.setVisibility(View.GONE);
            } else {
                holder.rloldprice.setVisibility(View.VISIBLE);
                holder.txtoldprice.setText(goodsM.getoldprice());
            }
            holder.txtspec.setText(goodsM.getspecification());
            holder.btnadd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (android.os.Build.VERSION.SDK_INT > 13) {
                        final ImageView imageView = new ImageView(SMSearchResult.this);
                        imageView.setLayoutParams(new LinearLayout.LayoutParams(30, 60));
                        imageView.setImageDrawable(holder.ivproduct.getDrawable());
                        Add2cartAnimUtil mAnimUtils = new Add2cartAnimUtil(SMSearchResult.this, holder.ivproduct.getDrawable());

                        mAnimUtils.startAnim(holder.ivproduct, fab);
                    }
                    cartDal.updateCart(ShoppingCartDal.ADDCART, goodsM, 1, cartChanged);
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SMSearchResult.this, SuperMarketDetail.class);
                    intent.putExtra("product", goodsM);
                    startActivityWithAnimForResult(intent,resultCodes.TOSHOPPINGCART);
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivproduct;
            TextView txtname, txtprice, txtoldprice, txtspec;
            View rloldprice;
            IconicsImageView btnadd;

            public ViewHolder(View itemView) {
                super(itemView);
                ivproduct = (ImageView) itemView.findViewById(R.id.iv_product);
                txtname = (TextView) itemView.findViewById(R.id.txt_name);
                txtprice = (TextView) itemView.findViewById(R.id.txt_price);
                txtoldprice = (TextView) itemView.findViewById(R.id.txt_oldprice);
                txtspec = (TextView) itemView.findViewById(R.id.txt_spec);
                rloldprice = itemView.findViewById(R.id.rl_oldprice);
                btnadd = (IconicsImageView) itemView.findViewById(R.id.btn_add);
            }
        }
    }
}

