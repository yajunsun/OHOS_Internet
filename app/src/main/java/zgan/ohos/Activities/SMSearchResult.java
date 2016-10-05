
package zgan.ohos.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
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
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;
import zgan.ohos.utils.generalhelper;

public class SMSearchResult extends myBaseActivity {

    boolean isLoadingMore = false;
    //商品列表页码
    int pageIndex = 1;
    //网络请求api
    OkHttpClient mOkHttpClient;
    EditText txtsearch;

    RecyclerView rvproducts;
    GridLayoutManager product_layoutManager;
     List<SM_GoodsM> list;
    SuperMarketDal dal;
    ShoppingCartDal cartDal;
    productAdapter adapter;
    /***
     * 购物车部分
     **/
    TextView txtcount, btncheck, txtoldtotalprice, txttotalprice;
    View rloldprice;

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
        final View clear=findViewById(R.id.btn_clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtsearch.setText("");
            }
        });
        dal=new SuperMarketDal();
        cartDal=new ShoppingCartDal();
        rvproducts = (RecyclerView) findViewById(R.id.rv_products);
        product_layoutManager = new GridLayoutManager(SMSearchResult.this, 2);
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
        btncheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SMSearchResult.this, ShoppingCart.class);
                startActivityWithAnim(intent);
            }
        });
        //TextView txtcount,txtoldtotalprice,txt_totalprice;
        //View rl_oldprice;
        txtcount = (TextView) findViewById(R.id.txt_count);
        txtoldtotalprice = (TextView) findViewById(R.id.txt_oldtotalprice);
        txttotalprice = (TextView) findViewById(R.id.txt_totalprice);
        rloldprice = findViewById(R.id.rl_oldprice);
        ShoppingCartSummary summary=cartDal.getSCSummary();
        bindShoppingCard(summary);
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
                .url("http://app.yumanc.1home1shop.com/V1_0/searchgoodslist.aspx").post(builder.build())
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
        if(adapter==null)
        {
            adapter=new productAdapter();
            rvproducts.setAdapter(adapter);
        }
        else
        {
            adapter.notifyDataSetChanged();
        }
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
        txtcount.setText(summary.getCount());
        txttotalprice.setText("￥" + summary.getTotalprice());
        if (!summary.getOldtotalprice().equals("0")) {
            txtoldtotalprice.setText("￥" + summary.getOldtotalprice());
            rloldprice.setVisibility(View.VISIBLE);
        } else {
            rloldprice.setVisibility(View.GONE);
        }
    }
    @Override
    public void ViewClick(View v) {
      switch (v.getId())
      {
          case R.id.btn_search:
              if(txtsearch.getText().toString().trim().equals(""))
                  return;
              loadData();
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
                            bindData();
                        } else if (!errmsg.isEmpty()) {
                            generalhelper.ToastShow(SMSearchResult.this, "服务器错误:" + errmsg);
                        }
                    } catch (JSONException jse) {

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                toCloseProgress();
            }
            else  if(msg.what==3)
            {
                ShoppingCartSummary summary=(ShoppingCartSummary)msg.obj;
                bindShoppingCard(summary);
            }
        }
    };

    UpdateCartListner cartChanged =new UpdateCartListner() {
        @Override
        public void onFailure() {
            generalhelper.ToastShow(SMSearchResult.this, "加入购物车失败!");
        }

        @Override
        public void onResponse(String response) {
            ShoppingCartSummary summary = cartDal.getSCSummary();
            Message msg=handler.obtainMessage();
            msg.what=3;
            msg.obj=summary;
            msg.sendToTarget();
        }
    };
    class productAdapter extends RecyclerView.Adapter<productAdapter.ViewHolder>
    {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.sm_rearch_itme,parent,false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final SM_GoodsM goodsM=list.get(position);
            ImageLoader.bindBitmap(goodsM.getpic_url(),holder.ivproduct);
            holder.txtname.setText(goodsM.getname());
            holder.txtprice.setText("￥"+goodsM.getprice());
            if(goodsM.getoldprice().equals("")||goodsM.getoldprice().equals("0"))
            {
                holder.rloldprice.setVisibility(View.GONE);
            }
            else
            {
                holder.rloldprice.setVisibility(View.VISIBLE);
                holder.txtoldprice.setText(goodsM.getoldprice());
            }
            holder.txtspec.setText(goodsM.getspecification());
            holder.btnadd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cartDal.updateCart(ShoppingCartDal.ADDCART, goodsM, 1, cartChanged);
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(SMSearchResult.this,SuperMarketDetail.class);
                    intent.putExtra("productid",goodsM.getproduct_id());
                    startActivityWithAnim(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            ImageView ivproduct;
            TextView txtname,txtprice,txtoldprice,txtspec;
            View rloldprice;
            IconicsImageView btnadd;
            public ViewHolder(View itemView) {
                super(itemView);
                ivproduct=(ImageView)itemView.findViewById(R.id.iv_product);
                txtname=(TextView)itemView.findViewById(R.id.txt_name);
                txtprice=(TextView)itemView.findViewById(R.id.txt_price);
                txtoldprice=(TextView)itemView.findViewById(R.id.txt_oldprice);
                txtspec=(TextView)itemView.findViewById(R.id.txt_spec);
                rloldprice=itemView.findViewById(R.id.rl_oldprice);
                btnadd=(IconicsImageView)itemView.findViewById(R.id.btn_add);
            }
        }
    }
}

