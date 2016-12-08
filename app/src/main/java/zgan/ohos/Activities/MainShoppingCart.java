package zgan.ohos.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import zgan.ohos.Contracts.UpdateCartListner;
import zgan.ohos.Dals.MainShoppingCartDal;
import zgan.ohos.Models.MainShoppingCartM;
import zgan.ohos.Models.SM_Payway;
import zgan.ohos.R;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;
import zgan.ohos.utils.generalhelper;
import zgan.ohos.utils.resultCodes;

public class MainShoppingCart extends myBaseActivity {

    RecyclerView rvbussiness;
    RecyclerView.LayoutManager layoutManager;
    MainShoppingCartDal cartDal;
    MainShoppingCartM list;
    @Override
    protected void onStart() {
        super.onStart();
        cartDal=new MainShoppingCartDal();
        loadData();
    }
    @Override
    protected void initView() {
        setContentView(R.layout.activity_main_shopping_cart);
        rvbussiness=(RecyclerView)findViewById(R.id.rvbussiness);
        layoutManager=new LinearLayoutManager(MainShoppingCart.this);
        rvbussiness.setLayoutManager(layoutManager);

    }

    void loadData()
    {
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
    void bindData()
    {
        if (list.getbusiness_flag()==0)
        {
            Intent intent=new Intent(MainShoppingCart.this,ShoppingCart.class);
            startActivityForResult(intent, resultCodes.TOSHOPPINGCART);

        }
        else if (list.getbusiness_flag()==1)
        {

        }

    }

    private Handler handler=new Handler(){
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
                            //bindData();
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
}
