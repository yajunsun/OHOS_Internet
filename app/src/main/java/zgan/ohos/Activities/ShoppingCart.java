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
public class ShoppingCart extends myBaseActivity {
    OkHttpClient mOkHttpClient;
    TextView txtcontent;
    RecyclerView rvcarts;
    ShoppingCartDal cartDal;
    List<ShoppingCartM> list;
    cartAdapter cAdapter;
    LinearLayoutManager cartLayoutManager;
    float density;

    @Override
    protected void initView() {
        setContentView(R.layout.fragment_fg_shopping_cart);
        cartDal = new ShoppingCartDal();
        View back =findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        rvcarts=(RecyclerView)findViewById(R.id.rv_carts);
        cartLayoutManager=new LinearLayoutManager(this);

        density= AppUtils.getDensity(ShoppingCart.this);
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
        if(cAdapter==null)
        {
            rvcarts.setLayoutManager(cartLayoutManager);
            cAdapter=new cartAdapter();
            rvcarts.setAdapter(cAdapter);
        }
        else
        {
            cAdapter.notifyDataSetChanged();
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

    //    UpdateCartListner cartChanged =new UpdateCartListner() {
//        @Override
//        public void onFailure() {
//            generalhelper.ToastShow(SuperMarket.this, "加入购物车失败!");
//        }
//
//        @Override
//        public void onResponse(String response) {
//            ShoppingCartSummary summary=cartDal.getSCSummary();
//            bindShoppingCard(summary);
//        }
//    };
    @Override
    public void ViewClick(View v) {

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
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT,
                   Math.round(pAdapter.getItemCount()*120*density));

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
                    pAdapter.notifyDataSetChanged();
                }
            });
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
            return new ViewHolder(getLayoutInflater().inflate(R.layout.lo_scart_subitem, null,false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            SM_GoodsM goodsM = goodsMs.get(position);
            ImageLoader.bindBitmap(goodsM.getpic_url(), holder.imgproduct);
            holder.txtname.setText(goodsM.getname());
            holder.txtspec.setText("规格:" + goodsM.getspecification());
            holder.txtprice.setText("￥" + String.valueOf(goodsM.getprice()));
            holder.selectcount.setCount(goodsM.getcount());
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
