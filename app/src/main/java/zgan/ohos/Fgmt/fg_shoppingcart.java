package zgan.ohos.Fgmt;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.squareup.okhttp.OkHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import zgan.ohos.Activities.CommitCartOrder;
import zgan.ohos.ConstomControls.MySelectCount;
import zgan.ohos.Contracts.UpdateCartListner;
import zgan.ohos.Dals.RequstResultDal;
import zgan.ohos.Dals.SM_OrderPayDal;
import zgan.ohos.Dals.ShoppingCartDal;
import zgan.ohos.Models.RequstResultM;
import zgan.ohos.Models.SM_GoodsM;
import zgan.ohos.Models.SM_Payway;
import zgan.ohos.Models.ShoppingCartM;
import zgan.ohos.Models.ShoppingCartSummary;
import zgan.ohos.R;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;
import zgan.ohos.utils.generalhelper;

/**
 * Created by yajunsun on 16/10/3.
 */
public class fg_shoppingcart extends myBaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    @Override
    public void onStart() {
        super.onStart();
        opGoods = new ArrayList<>();
        loadData();
    }

    ProgressDialog progressDialog;
    private String processText = "正在加载中，请稍等...";
    ToggleButton tgedit;
    LinearLayout rvcarts;
    ShoppingCartDal cartDal;
    SM_OrderPayDal orderDal;
    List<ShoppingCartM> list;
    List<SM_GoodsM> opGoods;
    ShoppingCartSummary summary;
    //cartAdapter cAdapter;
    SwipeRefreshLayout refreshview;
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
    int lbpxWidth = 0, lbpxHeight = 0;
    View llselectall;
    boolean ModifyChild = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        return initView();
    }

    protected View initView() {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(true);
        progressDialog.setMessage(processText);
        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_fg_shopping_cart, null, false);
        cartDal = new ShoppingCartDal();
        orderDal = new SM_OrderPayDal();
        View back = v.findViewById(R.id.back);
        back.setVisibility(View.GONE);
        refreshview = (SwipeRefreshLayout) v.findViewById(R.id.refreshview);
        refreshview.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                pageIndex = 1;
//                isLoadingMore = false;
                //isFirstload = true;
                opGoods = new ArrayList<>();
                loadData();
                //adapter.notifyDataSetChanged();

            }
        });
        rvcarts = (LinearLayout) v.findViewById(R.id.rv_carts);
        tgedit = (ToggleButton) v.findViewById(R.id.tg_edit);
        tgedit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {//编辑模式
                    isEdit = true;
                    refreshview.setEnabled(false);
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
                    refreshview.setEnabled(true);
                    llcheck.setVisibility(View.VISIBLE);
                    lloption.setVisibility(View.GONE);
                    delItems = null;
                    //isFirstload = true;
                    opGoods = new ArrayList<>();
                    loadData();//重新加载
                }
            }
        });


        llcheck = v.findViewById(R.id.ll_check);
        llselectall = v.findViewById(R.id.llselectall);
        selectall = (CheckBox) v.findViewById(R.id.selectall);
        txttotalprice = (TextView) v.findViewById(R.id.txt_totalprice);
        txtoldtotalprice = (TextView) v.findViewById(R.id.txt_oldtotalprice);
        rloldprice = v.findViewById(R.id.rl_oldprice);
        btncheck = (TextView) v.findViewById(R.id.btn_check);
        btncheck.setOnClickListener(this);
        selectall.setOnCheckedChangeListener(this);
        llselectall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ModifyChild=true;
                selectall.setChecked(!selectall.isChecked());
            }
        });
        lloption = v.findViewById(R.id.ll_option);
        selectall1 = (CheckBox) v.findViewById(R.id.selectall1);
        btndelete = (TextView) v.findViewById(R.id.btn_delete);
        selectall1.setOnCheckedChangeListener(this);
        btndelete.setOnClickListener(this);
        density = AppUtils.getDensity(getActivity());
        lbpxWidth = Math.round(40 * density);
        lbpxHeight = Math.round(20 * density);
        opGoods = new ArrayList<>();
        return v;
    }

    protected void toShowProgress() {
        progressDialog.show();

    }

    protected void toCloseProgress() {
        progressDialog.dismiss();

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
        boolean checkallC = true;
        if (list == null || list.size() == 0) {
            btncheck.setEnabled(false);
            btndelete.setEnabled(false);
            btncheck.setBackgroundColor(getResources().getColor(R.color.color_sm_normal_txt));
            btndelete.setBackgroundColor(getResources().getColor(R.color.color_sm_normal_txt));
        } else {
            btncheck.setEnabled(true);
            btndelete.setEnabled(true);
            btncheck.setBackgroundColor(getResources().getColor(R.color.primary));
            btndelete.setBackgroundColor(getResources().getColor(R.color.primary));
        }
        rvcarts.removeAllViews();
        for (final ShoppingCartM cartM : list) {
            boolean checkallP = true;
            //boolean isAll = true;
            final ViewGroup cv = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.lo_scart_item, null, false);
            final CheckBox rballproduct = (CheckBox) cv.findViewById(R.id.rb_allproduct);
            rballproduct.setText(cartM.getdistributionType());
            View allproduct = cv.findViewById(R.id.ll_allproduct);
            final LinearLayout pView = (LinearLayout) cv.findViewById(R.id.pView);
            for (final SM_GoodsM goodsM : cartM.getproductArray()) {
                final ViewGroup v = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.lo_scart_subitem, pView, false);
                final CheckBox rbproduct = (CheckBox) v.findViewById(R.id.rb_product);
                ImageView imgproduct = (ImageView) v.findViewById(R.id.img_product);
                final TextView txtname = (TextView) v.findViewById(R.id.txt_name);
                TextView txtspec = (TextView) v.findViewById(R.id.txt_spec);
                TextView txtprice = (TextView) v.findViewById(R.id.txt_price);
                LinearLayout lltypes = (LinearLayout) v.findViewById(R.id.ll_types);
                MySelectCount selectcount = (MySelectCount) v.findViewById(R.id.selectcount);
                View flouter = v.findViewById(R.id.fl_outer);
                ImageLoader.bindBitmap(goodsM.getpic_url(), imgproduct);
                txtname.setText(goodsM.getname());
                txtspec.setText("规格:" + goodsM.getspecification());
                txtprice.setText("￥" + String.valueOf(goodsM.getprice()));
                selectcount.setCount(goodsM.getcount());
                lltypes.removeAllViews();
                if (goodsM.gettype_list() != null && goodsM.gettype_list().size() > 0) {
                    lltypes.setVisibility(View.VISIBLE);
                    int tcount = goodsM.gettype_list().size();
                    LinearLayout.LayoutParams vparams = new LinearLayout.LayoutParams(
                            lbpxWidth, lbpxHeight);
                    vparams.setMargins(Math.round(1 * density), 0, 0, 0);
                    for (int i = 0; i < tcount; i++) {
                        ImageView iv = new ImageView(getActivity());
                        iv.setLayoutParams(vparams);
                        ImageLoader.bindBitmap(goodsM.gettype_list().get(i), iv, lbpxWidth, lbpxHeight);
                        lltypes.addView(iv);
                    }
                }
                if (isEdit)
                    selectcount.setVisibility(View.GONE);
                rbproduct.setOnCheckedChangeListener(new productCheckListner(cv, goodsM, cartM.getproductArray().size()));
                selectcount.setOnchangeListener(new MySelectCount.IonChanged() {
                    @Override
                    public void onAddition(int count) {

                        goodsM.setcount(count);
                        if (!opGoods.contains(goodsM))
                            opGoods.add(goodsM);
                        cartDal.updateCart(ShoppingCartDal.UPDATECART, goodsM, count, new UpdateCartListner() {
                            @Override
                            public void onFailure() {

                            }

                            @Override
                            public void onResponse(String response) {
                                if (!rbproduct.isChecked())
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            rbproduct.setChecked(true);
                                        }
                                    });
                            }
                        });
                        summaryCart();
                    }

                    @Override
                    public void onReduction(int count) {
                        if (count == 0) {
                            opGoods.remove(goodsM);
                        } else {
                            goodsM.setcount(count);
                        }
                        cartDal.updateCart(ShoppingCartDal.UPDATECART, goodsM, count, new UpdateCartListner() {
                            @Override
                            public void onFailure() {

                            }

                            @Override
                            public void onResponse(String response) {
                                if (!rbproduct.isChecked())
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            rbproduct.setChecked(true);
                                        }
                                    });
                            }
                        });
                        summaryCart();
                    }
                });
                if (goodsM.getSelect()) {
                    rbproduct.setChecked(true);
                } else {
                    rbproduct.setChecked(false);
                    checkallP = false;
                }
                flouter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        rbproduct.setChecked(!rbproduct.isChecked());
                    }
                });
                pView.addView(v);
            }
            allproduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ModifyChild = true;
                    rballproduct.setChecked(!rballproduct.isChecked());
                }
            });
            rballproduct.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (!ModifyChild) {
                        return;
                    }
                    ModifyChild = true;
                    int c = pView.getChildCount();
                    for (int i = 0; i < c; i++) {
                        CheckBox checkBox = (CheckBox) pView.getChildAt(i).findViewById(R.id.rb_product);
                        checkBox.setChecked(isChecked);
                    }
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
                }
            });
            //rballproduct.setChecked(cartM.getSelect());
            //rballproduct.setChecked(isAll);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, Math.round((cartM.getproductArray().size() * 120 * density) + 50 * density));
            cv.setLayoutParams(params);
            if (!checkallP)
                checkallC = false;
            rballproduct.setChecked(checkallP);
            rvcarts.addView(cv);
        }
        selectall.setChecked(checkallC);
        if (!isEdit) {
            summaryCart();
        }
        refreshview.setRefreshing(false);
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
        txttotalprice.setText("合计 ￥" + summary.getTotalprice());
        if (!summary.getOldtotalprice().equals("0")) {
            txtoldtotalprice.setText("￥" + summary.getOldtotalprice());
            rloldprice.setVisibility(View.VISIBLE);
        } else {
            rloldprice.setVisibility(View.GONE);
        }
        setViewChecked();
    }

    void setViewChecked() {
        boolean allcartChecked = true;
        int c = rvcarts.getChildCount();
        for (int i = 0; i < c; i++) {
            ViewGroup vg = (ViewGroup) rvcarts.getChildAt(i);
            //这里写死了,布局不能变
            CheckBox b = (CheckBox) vg.findViewById(R.id.rb_allproduct); //(CheckBox) ((ViewGroup) vg.getChildAt(0)).getChildAt(0);

            if (!b.isChecked()) {
                allcartChecked = false;
            }
        }
        ModifyChild = false;
        selectall.setChecked(allcartChecked);
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
                            cartDal.syncCart(list);
                            bindData();
                            //selectall.setChecked(true);
                        } else if (!errmsg.isEmpty()) {
                            generalhelper.ToastShow(getActivity(), "服务器错误:" + errmsg);
                            if (errmsg.contains("时间戳")) {
                                ZganCommunityService.toGetServerData(43, PreferenceUtil.getUserName(), tokenHandler);
                            }
                        }
                    } catch (JSONException jse) {

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (msg.what == 2) {
                toCloseProgress();
                SM_Payway payway = orderDal.getPayWays(msg.obj.toString());
                Intent intent = new Intent(getActivity(), CommitCartOrder.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("payways", payway);
                intent.putExtras(bundle);
                startActivityWithAnim(getActivity(), intent);
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

    public void ViewClick(View v) {
        switch (v.getId()) {
            case R.id.btn_check:
                if (opGoods == null || opGoods.size() == 0) {
                    generalhelper.ToastShow(getActivity(), "还没有选择商品哦！");
                    break;
                }
                toShowProgress();
                orderDal.ComfirmOrder(opGoods, new UpdateCartListner() {
                    @Override
                    public void onFailure() {

                    }

                    @Override
                    public void onResponse(String response) {
                        final RequstResultM result = new RequstResultDal().getItem("{data:[" + response + "]}");
                        if (result.getresult().equals("0")) {
                            Message msg = handler.obtainMessage();
                            msg.what = 2;
                            msg.obj = response;
                            msg.sendToTarget();
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    generalhelper.ToastShow(getActivity(), result.getmsg());
                                    toCloseProgress();
                                }
                            });
                        }
                    }
                });
                //验证
                break;
            case R.id.btn_delete://删除
                if (delItems == null || delItems.size() == 0) {
                    generalhelper.ToastShow(getActivity(), "还没有选择商品哦！");
                    break;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

    class productCheckListner implements CompoundButton.OnCheckedChangeListener {
        SM_GoodsM goodsM;
        ViewGroup parentV;
        CheckBox pcb;
        int c = 0;

        public productCheckListner(ViewGroup _parent, SM_GoodsM _goodsM, int _c) {
            parentV = _parent;
            goodsM = _goodsM;
            c = _c;
            pcb = (CheckBox) parentV.findViewById(R.id.rb_allproduct);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            //选中
            if (isChecked) {
                if (!isEdit) {//非编辑状态
                    if (!opGoods.contains(goodsM))
                        opGoods.add(goodsM);
                    if (goodsM.getcan_handsel() != 1)//&& isFirstload == false
                    {
                        cartDal.updateCart(ShoppingCartDal.SELECTCART, goodsM, 1, null);//更新服务器选中状态
                    }
                } else {//编辑状态
                    if (!delItems.contains(goodsM))
                        delItems.add(goodsM);//加入删除列表
                }

                int count = 0;
                for (int i = 0; i < c; i++) {
                    View vg = ((ViewGroup) parentV.getChildAt(1)).getChildAt(i);
                    if (vg == null)
                        return;
                    CheckBox checkBox = (CheckBox) vg.findViewById(R.id.rb_product);
                    if (checkBox != null && checkBox.isChecked())
                        count++;
                }
                if (count == c) {
                    ModifyChild = false;
                    pcb.setChecked(true);
                } else {
                    ModifyChild = false;
                    pcb.setChecked(false);
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
                ModifyChild = false;
                pcb.setChecked(false);
                selectall.setChecked(false);

            }
            summaryCart();//更新商品总量和价格
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //        for (ShoppingCartM m : list) {
//            if (isChecked) {
//                opGoods.addAll(m.getproductArray());
//            }
//            m.setSelect(isChecked);
//        }
        if (!ModifyChild) {
            return;
        }

        int c = rvcarts.getChildCount();
        for (int i = 0; i < c; i++) {
            ModifyChild = true;
            ViewGroup vg = (ViewGroup) rvcarts.getChildAt(i);
            //这里写死了,布局不能变
            CheckBox b = (CheckBox) vg.findViewById(R.id.rb_allproduct); //(CheckBox) ((ViewGroup) vg.getChildAt(0)).getChildAt(0);
            b.setChecked(isChecked);
        }
        if (isEdit) {
            if (!isChecked) {
                delItems = new ArrayList<>();
            }
        } else {
            //opGoods = new ArrayList<>();
            summaryCart();
        }
    }
}
