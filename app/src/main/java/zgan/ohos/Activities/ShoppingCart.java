package zgan.ohos.Activities;

import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
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

import zgan.ohos.ConstomControls.MySelectCount;
import zgan.ohos.Contracts.UpdateCartListner;
import zgan.ohos.Dals.ShoppingCartDal;
import zgan.ohos.Models.SM_GoodsM;
import zgan.ohos.Models.ShoppingCartM;
import zgan.ohos.Models.ShoppingCartSummary;
import zgan.ohos.R;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;
import zgan.ohos.utils.generalhelper;

/**
 * Created by yajunsun on 16/10/3.
 */
public class ShoppingCart extends myBaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    OkHttpClient mOkHttpClient;
    TextView txtcontent;
    RecyclerView rvcarts;
    ShoppingCartDal cartDal;
    List<ShoppingCartM> list;
    List<SM_GoodsM> opGoods;
    ShoppingCartSummary summary;
    cartAdapter cAdapter;
    LinearLayoutManager cartLayoutManager;
    float density;
    DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
    //结算
    CheckBox selectall;
    TextView txttotalprice, txtoldtotalprice, btncheck;
    View rloldprice;

    @Override
    protected void initView() {
        setContentView(R.layout.fragment_fg_shopping_cart);
        cartDal = new ShoppingCartDal();
        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        rvcarts = (RecyclerView) findViewById(R.id.rv_carts);
        cartLayoutManager = new LinearLayoutManager(this);
        selectall = (CheckBox) findViewById(R.id.selectall);
        txttotalprice = (TextView) findViewById(R.id.txt_totalprice);
        txtoldtotalprice = (TextView) findViewById(R.id.txt_oldtotalprice);
        rloldprice = findViewById(R.id.rl_oldprice);
        btncheck = (TextView) findViewById(R.id.btn_check);
        btncheck.setOnClickListener(this);
        selectall.setOnCheckedChangeListener(this);
        density = AppUtils.getDensity(ShoppingCart.this);
        opGoods = new ArrayList<>();
        loadData();
    }

    void loadData() {
        UpdateCartListner listner = new UpdateCartListner() {
            @Override
            public void onFailure() {

            }

            @Override
            public void onResponse(String response) {
                Message msg = handler.obtainMessage();
                msg.what = 1;
                msg.obj = response;
                msg.sendToTarget();
            }
        };
        cartDal.getCartList(listner);
    }

    void bindData() {
        if (cAdapter == null) {
            rvcarts.setLayoutManager(cartLayoutManager);
            cAdapter = new cartAdapter();
            rvcarts.setAdapter(cAdapter);
        } else {
            cAdapter.notifyDataSetChanged();
        }
    }

    void summaryCart() {
        summary = new ShoppingCartSummary();
        int i = 0;
        int tcount=0;
        double totalprice = 0.0;
        double oldtotalprice = 0.0;
        for (SM_GoodsM m : opGoods) {
            i++;
            tcount+=m.getcount();
            totalprice += m.getprice() * m.getcount();
            if (!m.getoldprice().equals("") && !m.getoldprice().equals("0"))
                oldtotalprice += Double.parseDouble(m.getoldprice()) * m.getcount();
        }
        //if (i > 0)
        summary.setTotalcount(String.valueOf(tcount));
        summary.setCount(String.valueOf(i));
        summary.setTotalprice(decimalFormat.format(totalprice));
        if (oldtotalprice == 0.0)
            summary.setOldtotalprice("0");
        else
            summary.setOldtotalprice(decimalFormat.format(oldtotalprice));
        bindShoppingCard(summary);
    }

    //绑定购物车数据
    void bindShoppingCard(ShoppingCartSummary summary) {
        btncheck.setText("去结算(" + summary.getCount() + ")");
        txttotalprice.setText("￥" + summary.getTotalprice());
        if (!summary.getOldtotalprice().equals("0")) {
            txtoldtotalprice.setText("￥" + summary.getOldtotalprice());
            rloldprice.setVisibility(View.VISIBLE);
        } else {
            rloldprice.setVisibility(View.GONE);
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
                            list = cartDal.getList(data);
                            bindData();
                            selectall.setChecked(true);
                        } else if (!errmsg.isEmpty()) {
                            generalhelper.ToastShow(ShoppingCart.this, "服务器错误:" + errmsg);
                        }
                    } catch (JSONException jse) {

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    @Override
    public void ViewClick(View v) {
        switch (v.getId()) {
            case R.id.btn_check:
//               cartDal.commitCart(opGoods, summary,"0", new UpdateCartListner() {
//                   @Override
//                   public void onFailure() {
//
//                   }
//
//                   @Override
//                   public void onResponse(String response) {
//
//                   }
//               });
                cartDal.verifyGoods(opGoods);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        ViewClick(v);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        for (ShoppingCartM m : list) {
            m.setSelect(isChecked);
        }
        cAdapter.notifyDataSetChanged();
        opGoods = new ArrayList<>();
        summaryCart();
    }

    class cartAdapter extends RecyclerView.Adapter<cartAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.lo_scart_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final ShoppingCartM cartM = list.get(position);
            final productAdapter pAdapter = new productAdapter(cartM.getproductArray());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    Math.round(pAdapter.getItemCount() * 120 * density));

            LinearLayoutManager layoutManager = new LinearLayoutManager(ShoppingCart.this);
            holder.rvproducts.setLayoutManager(layoutManager);
            holder.rvproducts.setAdapter(pAdapter);
            holder.rvproducts.setLayoutParams(params);
            holder.rballproduct.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    for (SM_GoodsM goodsM : cartM.getproductArray()) {
                        goodsM.setSelect(isChecked);
                    }
                    if (!isChecked) {
                        opGoods.removeAll(cartM.getproductArray());
                        summaryCart();
                    }
                    pAdapter.notifyDataSetChanged();
                }
            });
            holder.rballproduct.setChecked(cartM.getSelect());
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox rballproduct;
            RecyclerView rvproducts;

            public ViewHolder(View itemView) {
                super(itemView);
                rballproduct = (CheckBox) itemView.findViewById(R.id.rb_allproduct);
                rvproducts = (RecyclerView) itemView.findViewById(R.id.rv_products);
            }
        }
    }

    class productAdapter extends RecyclerView.Adapter<productAdapter.ViewHolder> {

        List<SM_GoodsM> goodsMs;

        public productAdapter(List<SM_GoodsM> _list) {
            goodsMs = _list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.lo_scart_subitem, null, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final SM_GoodsM goodsM = goodsMs.get(position);
            ImageLoader.bindBitmap(goodsM.getpic_url(), holder.imgproduct);
            holder.txtname.setText(goodsM.getname());
            holder.txtspec.setText("规格:" + goodsM.getspecification());
            holder.txtprice.setText("￥" + String.valueOf(goodsM.getprice()));
            holder.selectcount.setCount(goodsM.getcount());

            holder.rbproduct.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        opGoods.add(goodsM);
                    } else {
                        opGoods.remove(goodsM);
                    }
                    summaryCart();
                }
            });
            holder.selectcount.setOnchangeListener(new MySelectCount.IonChanged() {
                @Override
                public void onAddition(int count) {
                    goodsM.setcount(count);
                    if (!opGoods.contains(goodsM))
                        opGoods.add(goodsM);
                    summaryCart();
                }

                @Override
                public void onReduction(int count) {
                    if (count == 0) {
                        opGoods.remove(goodsM);
                    } else {
                        goodsM.setcount(count);
                    }
                    summaryCart();
                }
            });
            holder.rbproduct.setChecked(goodsM.getSelect());
        }

        @Override
        public int getItemCount() {
            return goodsMs.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox rbproduct;
            ImageView imgproduct;
            TextView txtname, txtspec, txtprice;
            LinearLayout lltypes;
            MySelectCount selectcount;

            public ViewHolder(View itemView) {
                super(itemView);
                rbproduct = (CheckBox) itemView.findViewById(R.id.rb_product);
                imgproduct = (ImageView) itemView.findViewById(R.id.img_product);
                txtname = (TextView) itemView.findViewById(R.id.txt_name);
                txtspec = (TextView) itemView.findViewById(R.id.txt_spec);
                txtprice = (TextView) itemView.findViewById(R.id.txt_price);
                lltypes = (LinearLayout) itemView.findViewById(R.id.ll_types);
                selectcount = (MySelectCount) itemView.findViewById(R.id.selectcount);
            }
        }
    }
}
