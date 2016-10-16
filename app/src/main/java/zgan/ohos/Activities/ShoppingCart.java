package zgan.ohos.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import zgan.ohos.ConstomControls.MySelectCount;
import zgan.ohos.Contracts.UpdateCartListner;
import zgan.ohos.Dals.SM_OrderPayDal;
import zgan.ohos.Dals.ShoppingCartDal;
import zgan.ohos.Models.SM_GoodsM;
import zgan.ohos.Models.SM_Payway;
import zgan.ohos.Models.ShoppingCartM;
import zgan.ohos.Models.ShoppingCartSummary;
import zgan.ohos.R;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.generalhelper;
import zgan.ohos.utils.resultCodes;

/**
 * Created by yajunsun on 16/10/3.
 */
public class ShoppingCart extends myBaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    ToggleButton tgedit;
    RecyclerView rvcarts;
    ShoppingCartDal cartDal;
    SM_OrderPayDal orderDal;
    List<ShoppingCartM> list;
    List<SM_GoodsM> opGoods;
    ShoppingCartSummary summary;
    cartAdapter cAdapter;
    LinearLayoutManager cartLayoutManager;
    float density;
    DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
    //结算
    View llcheck;
    CheckBox selectall;
    TextView txttotalprice, txtoldtotalprice, btncheck;
    View rloldprice;
    //删除
    View lloption;
    CheckBox selectall1;
    TextView btndelete;

    //是否编辑模式 true是 false否
    boolean isEdit = false;
    List<SM_GoodsM> delItems;
    Dialog delDialog;
    //商品列表数据notify的次数
    //boolean isFirstload = true;

    @Override
    protected void initView() {
        setContentView(R.layout.fragment_fg_shopping_cart);
        cartDal = new ShoppingCartDal();
        orderDal = new SM_OrderPayDal();
        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        rvcarts = (RecyclerView) findViewById(R.id.rv_carts);
        tgedit = (ToggleButton) findViewById(R.id.tg_edit);
        tgedit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {//编辑模式
                    isEdit = true;
                    llcheck.setVisibility(View.GONE);
                    lloption.setVisibility(View.VISIBLE);
                    //编辑模式下全部默认未选中
                    //selectall1.setChecked(false);
                    for (ShoppingCartM m : list) {
                        m.setSelect(false);
                        for (SM_GoodsM goodsM : m.getproductArray()) {
                            goodsM.setSelect(false);
                        }
                    }
                    delItems = new ArrayList<>();
                    bindData();
                } else {//非编辑模式
                    isEdit = false;
                    llcheck.setVisibility(View.VISIBLE);
                    lloption.setVisibility(View.GONE);
                    delItems = null;
                    //isFirstload = true;
                    opGoods = new ArrayList<>();
                    loadData();//重新加载
                }
            }
        });
        cartLayoutManager = new LinearLayoutManager(this);
        llcheck = findViewById(R.id.ll_check);
        selectall = (CheckBox) findViewById(R.id.selectall);
        txttotalprice = (TextView) findViewById(R.id.txt_totalprice);
        txtoldtotalprice = (TextView) findViewById(R.id.txt_oldtotalprice);
        rloldprice = findViewById(R.id.rl_oldprice);
        btncheck = (TextView) findViewById(R.id.btn_check);
        btncheck.setOnClickListener(this);
        selectall.setOnCheckedChangeListener(this);
        lloption = findViewById(R.id.ll_option);
        selectall1 = (CheckBox) findViewById(R.id.selectall1);
        btndelete = (TextView) findViewById(R.id.btn_delete);
        selectall1.setOnCheckedChangeListener(this);
        btndelete.setOnClickListener(this);
        density = AppUtils.getDensity(ShoppingCart.this);
        opGoods = new ArrayList<>();
        loadData();
        setResult(resultCodes.TOSHOPPINGCART);
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
        if(list==null||list.size()==0) {
            btncheck.setEnabled(false);
            btndelete.setEnabled(false);
            btncheck.setBackgroundColor(getResources().getColor(R.color.color_sm_normal_txt));
            btndelete.setBackgroundColor(getResources().getColor(R.color.color_sm_normal_txt));
        }
        else
        {
            btncheck.setEnabled(true);
            btndelete.setEnabled(true);
            btncheck.setBackgroundColor(getResources().getColor(R.color.primary));
            btndelete.setBackgroundColor(getResources().getColor(R.color.primary));
        }
        if (cAdapter == null) {
            rvcarts.setLayoutManager(cartLayoutManager);
            cAdapter = new cartAdapter();
            rvcarts.setAdapter(cAdapter);
        } else {
            cAdapter.notifyDataSetChanged();
        }
        if (!isEdit) {
            summaryCart();
        }
        toCloseProgress();
    }

    void summaryCart() {
        summary = new ShoppingCartSummary();
        int i = 0;
        int tcount = 0;
        double totalprice = 0.0;
        double oldtotalprice = 0.0;
        for (SM_GoodsM m : opGoods) {
            i++;
            tcount += m.getcount();
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
                            //selectall.setChecked(true);
                        } else if (!errmsg.isEmpty()) {
                            generalhelper.ToastShow(ShoppingCart.this, "服务器错误:" + errmsg);
                        }
                    } catch (JSONException jse) {

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (msg.what == 2) {
                toCloseProgress();
                SM_Payway payway = orderDal.getPayWays(msg.obj.toString());
                Intent intent = new Intent(ShoppingCart.this, CommitCartOrder.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("payways", payway);
                intent.putExtras(bundle);
                startActivityWithAnim(intent);
            }
        }
    };

    @Override
    public void ViewClick(View v) {
        switch (v.getId()) {
            case R.id.btn_check:
                toShowProgress();
                orderDal.ComfirmOrder(opGoods, new UpdateCartListner() {
                    @Override
                    public void onFailure() {

                    }

                    @Override
                    public void onResponse(String response) {
                        Message msg = handler.obtainMessage();
                        msg.what =2;
                        msg.obj=response;
                        msg.sendToTarget();
                    }
                });
               //验证
                break;
            case R.id.btn_delete://删除
                if (delItems == null || delItems.size() == 0) {
                    generalhelper.ToastShow(ShoppingCart.this, "还没有选择商品哦！");
                    break;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(ShoppingCart.this);
                builder.setMessage(String.format("确认要删除这%s中商品吗?", delItems.size())).setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        cartDal.updateCart(ShoppingCartDal.DELETECART, delItems, 0, new UpdateCartListner() {
                                    @Override
                                    public void onFailure() {

                                    }

                                    @Override
                                    public void onResponse(String response) {
                                        opGoods.removeAll(delItems);
                                        while (delItems.size() > 0) {
                                            SM_GoodsM m = delItems.get(0);
                                            cart:
                                            for (int c = 0; c < list.size(); c++) {
                                                List<SM_GoodsM> tempgoods = list.get(c).getproductArray();
                                                product:
                                                for (int g = 0; g < tempgoods.size(); g++) {
                                                    if (tempgoods.size() == 0)
                                                        break;
                                                    if (m.getproduct_id().equals(tempgoods.get(g).getproduct_id())) {
                                                        list.get(c).getproductArray().remove(g);
                                                        delItems.remove(m);
                                                        break product;
                                                    }
                                                }
                                                if (list.get(c).getproductArray().size() == 0) {
                                                    list.remove(c);
                                                    break cart;
                                                }
                                            }
                                        }
                                        //delItems = new ArrayList<>();//清空删除列表
                                        handler.post(new

                                                             Runnable() {
                                                                 @Override
                                                                 public void run() {
                                                                     bindData();
                                                                 }
                                                             }

                                        );
                                    }
                                }

                        );
                    }
                }).setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        delDialog.dismiss();
                    }
                });
                delDialog = builder.create();
                delDialog.show();
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
        if (isEdit) {
            if (!isChecked) {
                delItems = new ArrayList<>();
            }
        } else {
            opGoods = new ArrayList<>();
            summaryCart();
        }
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
            holder.rballproduct.setText(cartM.getdistributionType());
            holder.rballproduct.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //if (!isReload)//编辑过后重新加载时不将所有数据选中，选中状态来自服务器
                    for (SM_GoodsM goodsM : cartM.getproductArray()) {
                        goodsM.setSelect(isChecked);
                    }//不需要默认修改选中状态
                    if (!isChecked) {//取消选中
                        if (isEdit)//编辑状态
                        {
                            delItems.removeAll(cartM.getproductArray());
                        } else {
                            opGoods.removeAll(cartM.getproductArray());
                            cartDal.updateCart(ShoppingCartDal.SELECTCART, cartM.getproductArray(), 0, null);
                            summaryCart();
                        }
                    }
                    pAdapter.notifyDataSetChanged();
                }
            });
            holder.rballproduct.setChecked(cartM.getSelect());
            /*//数据加载完成后更新isFirstload = false,之后商品列表上的选中状态就会更新到服务器（商品在加载的时候选中状态是不需要更新到服务器上的）
            if(position+1==list.size())
            {
            isFirstload = false;
            }*/
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
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final SM_GoodsM goodsM = goodsMs.get(position);
            ImageLoader.bindBitmap(goodsM.getpic_url(), holder.imgproduct);
            holder.txtname.setText(goodsM.getname());
            holder.txtspec.setText("规格:" + goodsM.getspecification());
            holder.txtprice.setText("￥" + String.valueOf(goodsM.getprice()));
            holder.selectcount.setCount(goodsM.getcount());
            if (isEdit)
                holder.selectcount.setVisibility(View.GONE);
            holder.rbproduct.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //选中
                    if (isChecked) {
                        if (!isEdit) {//非编辑状态
//                            holder.selectcount.setVisibility(View.VISIBLE);//显示数量操作
                            if (!opGoods.contains(goodsM))
                                opGoods.add(goodsM);
                            if (goodsM.getcan_handsel() != 1)//&& isFirstload == false
                            {
                                cartDal.updateCart(ShoppingCartDal.SELECTCART, goodsM, 1, null);//更新服务器选中状态
                            }
//                            else {
//                                isFirstload = false;
//                            }
                        } else {//编辑状态
                            //holder.selectcount.setVisibility(View.GONE);//隐藏数量操作
                            if (!delItems.contains(goodsM))
                                delItems.add(goodsM);//加入删除列表
                        }
                    } else {//取消选中
                        if (!isEdit) {//非编辑状态
                            //holder.selectcount.setVisibility(View.VISIBLE);
                            opGoods.remove(goodsM);
                            if (goodsM.getcan_handsel() == 1)
                                cartDal.updateCart(ShoppingCartDal.SELECTCART, goodsM, 0, null);
                        } else {//编辑状态
                            //holder.selectcount.setVisibility(View.GONE);//隐藏数量操作
                            if (delItems.contains(goodsM))//删除列表包含当前商品
                                delItems.remove(goodsM);//从删除列表移除
                        }
                    }
                    summaryCart();//更新商品总量和价格
                }
            });
            holder.selectcount.setOnchangeListener(new MySelectCount.IonChanged() {
                @Override
                public void onAddition(int count) {

                    goodsM.setcount(count);
                    if (!opGoods.contains(goodsM))
                        opGoods.add(goodsM);
                    cartDal.updateCart(ShoppingCartDal.UPDATECART, goodsM, count, null);
                    summaryCart();
                }

                @Override
                public void onReduction(int count) {
                    if (count == 0) {
                        opGoods.remove(goodsM);
                    } else {
                        goodsM.setcount(count);
                    }
                    cartDal.updateCart(ShoppingCartDal.UPDATECART, goodsM, count, null);
                    summaryCart();
                }
            });
            holder.rbproduct.setChecked(goodsM.getSelect());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.rbproduct.setChecked(!holder.rbproduct.isChecked());
                }
            });
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
