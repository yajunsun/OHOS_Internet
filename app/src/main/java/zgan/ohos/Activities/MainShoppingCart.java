package zgan.ohos.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import zgan.ohos.Contracts.UpdateCartListner;
import zgan.ohos.Dals.MainShoppingCartDal;
import zgan.ohos.Models.BussinessShoppingCartM;
import zgan.ohos.Models.MainShoppingCartM;
import zgan.ohos.Models.Product;
import zgan.ohos.Models.SM_GoodsM;
import zgan.ohos.Models.SM_Payway;
import zgan.ohos.Models.ShoppingCartM;
import zgan.ohos.R;
import zgan.ohos.adapters.RectViewItemSpace;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;
import zgan.ohos.utils.generalhelper;
import zgan.ohos.utils.resultCodes;

public class MainShoppingCart extends myBaseActivity {

    RecyclerView rvbussiness;
    RecyclerView.LayoutManager layoutManager;
    MainShoppingCartDal cartDal;
    MainShoppingCartM msm;
    List<BussinessShoppingCartM> list;
    myAdapter adapter;
    float density;

    @Override
    protected void onStart() {
        super.onStart();
        cartDal = new MainShoppingCartDal();
        loadData();
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_main_shopping_cart);
        rvbussiness = (RecyclerView) findViewById(R.id.rvbussiness);
        layoutManager = new LinearLayoutManager(MainShoppingCart.this);
        rvbussiness.setLayoutManager(layoutManager);

        density = AppUtils.getDensity(MainShoppingCart.this);
        //rvbussiness.addItemDecoration(new RectViewItemSpace(0,0,0,Math.round(10 * density)));
    }

    void loadData() {
        toShowProgress();
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
        if (msm != null && msm.getbussiness_goodsArray() != null && msm.getbussiness_goodsArray().size() > 0) {
            if (msm.getbusiness_flag() == 0) {
                Intent intent = new Intent(MainShoppingCart.this, ShoppingCart.class);
                startActivityForResult(intent, resultCodes.TOSHOPPINGCART);

            } else if (msm.getbusiness_flag() == 1) {
                list = msm.getbussiness_goodsArray();
                if (adapter == null) {
                    adapter = new myAdapter();
                    rvbussiness.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                }
//            isLoadingMore = false;
//            refreshview.setRefreshing(false);
//            llkeys.setVisibility(View.GONE);

            }
        } else {
            Intent intent = new Intent(MainShoppingCart.this, ShoppingCart.class);
            startActivityForResult(intent, resultCodes.TOSHOPPINGCART);
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
                            msm = cartDal.getList(data);
                            bindData();
                            //selectall.setChecked(true);
                        } else if (!errmsg.isEmpty()) {
                            generalhelper.ToastShow(MainShoppingCart.this, "服务器错误:" + errmsg);
                            if (errmsg.contains("时间戳")) {
                                ZganCommunityService.toGetServerData(43, PreferenceUtil.getUserName(), tokenHandler);
                            }
                        }
                    } catch (JSONException jse) {

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            //else if (msg.what == 2) {
//                toCloseProgress();
//                SM_Payway payway = orderDal.getPayWays(msg.obj.toString());
//                Intent intent = new Intent(ShoppingCart.this, CommitCartOrder.class);
//                Bundle bundle = new Bundle();
//                bundle.putSerializable("payways", payway);
//                intent.putExtras(bundle);
//                startActivityWithAnim(intent);
//            }
            toCloseProgress();
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
    public void ViewClick(View v) {

    }

    class myAdapter extends RecyclerView.Adapter<myAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.lo_bussiness_shopping_cart_item, null, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            BussinessShoppingCartM bsc = list.get(position);
            holder.txt_name.setText(bsc.getbussinessname());
            //buildGoodsView(bsc, holder.llcontent);
            List<SM_GoodsM> goodsMs=new ArrayList<>();
            for(ShoppingCartM sc: bsc.getgoodsarray())
            {
                for (SM_GoodsM g:sc.getproductArray())
                {
                    goodsMs.add(g);
                }
            }
            ProductAdapter productAdapter=new ProductAdapter(goodsMs);
            holder.rvgoods.setAdapter(productAdapter);
            holder.rvgoods.setLayoutManager(new GridLayoutManager(MainShoppingCart.this,10000));
            holder.btn_check.setOnClickListener(new onClick(bsc));
            holder.iv_detail.setOnClickListener(new onClick(bsc));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class onClick implements View.OnClickListener {
            BussinessShoppingCartM Bsc;

            public onClick(BussinessShoppingCartM _bsc) {
                Bsc = _bsc;
            }

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainShoppingCart.this, SecondShoppingCart.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("bussinessCid", Bsc.getbussinesscid());
                intent.putExtras(bundle);
                startActivityWithAnimForResult(intent, resultCodes.TOSHOPPINGCART);
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txt_name, txt_totalprice, btn_check;
            IconicsImageView iv_detail;
            //LinearLayout llcontent;
            RecyclerView rvgoods;

            public ViewHolder(View itemView) {
                super(itemView);
                txt_name = (TextView) itemView.findViewById(R.id.txt_name);
                txt_totalprice = (TextView) itemView.findViewById(R.id.txt_totalprice);
                btn_check = (TextView) itemView.findViewById(R.id.btn_check);
                iv_detail = (IconicsImageView) itemView.findViewById(R.id.iv_detail);
                //llcontent = (LinearLayout) itemView.findViewById(R.id.llcontent);
                rvgoods=(RecyclerView)itemView.findViewById(R.id.rv_goods);
            }
        }
    }

    void buildGoodsView(BussinessShoppingCartM bsc, LinearLayout layout) {
        List<ShoppingCartM> sc = bsc.getgoodsarray();
        LinearLayout.MarginLayoutParams params = new LinearLayout.LayoutParams((int) (100 * density), (int) (100 * density));
        //List<Product>ps=new ArrayList<>();
        //LinearLayout layout = new LinearLayout(MainShoppingCart.this);
        for (ShoppingCartM m : sc) {
            List<SM_GoodsM> goodsMs = m.getproductArray();
            for (SM_GoodsM g : goodsMs) {
                ImageView img = new ImageView(MainShoppingCart.this);
                params.setMargins(10, 10, 0, 10);
                //layout.setLayoutParams(params);

                img.setLayoutParams(params);
                img.setScaleType(ImageView.ScaleType.FIT_XY);
                ImageLoader.bindBitmap(g.getpic_url(), img);
                layout.addView(img);
            }
        }
    }

    class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
        List<SM_GoodsM> goodsMs;

        public ProductAdapter(List<SM_GoodsM> sm_goodsMs) {
            goodsMs = sm_goodsMs;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.lo_product_item,null,false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ImageLoader.bindBitmap(goodsMs.get(position).getpic_url(), holder.iv_preview);
            holder.txt_count.setText(String.valueOf(goodsMs.get(position).getcount()));
        }

        @Override
        public int getItemCount() {
            return goodsMs.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView iv_preview;
            TextView txt_count;

            public ViewHolder(View itemView) {
                super(itemView);
                iv_preview=(ImageView)itemView.findViewById(R.id.iv_preview);
                txt_count=(TextView)itemView.findViewById(R.id.txt_count);
            }
        }
    }
}
