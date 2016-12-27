package zgan.ohos.Activities;

import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import zgan.ohos.Contracts.UpdateCartListner;
import zgan.ohos.Dals.ShippingAddressDal;
import zgan.ohos.Models.ShippingAddressModel;
import zgan.ohos.R;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.generalhelper;

/**
 * Created by yajunsun on 2016/12/27.
 */
public class ShippingAddressEditList extends myBaseActivity implements View.OnClickListener {
    SwipeRefreshLayout refreshview;
    RecyclerView rv_address;
    Button btn_new;
    ShippingAddressDal dal;
    myAdapter adapter;
    RecyclerView.LayoutManager loyoutManager;
    List<ShippingAddressModel> list;

    @Override
    public void initView() {
        setContentView(R.layout.activity_list_shipping_address);
        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }

        });

        btn_new = (Button) findViewById(R.id.btn_new);
        btn_new.setOnClickListener(this);
        refreshview = (SwipeRefreshLayout) findViewById(R.id.refreshview);
        rv_address = (RecyclerView) findViewById(R.id.rv_address);
        dal = new ShippingAddressDal();
        loyoutManager = new LinearLayoutManager(ShippingAddressEditList.this);
        rv_address.setLayoutManager(loyoutManager);
    }

    @Override
    public void ViewClick(View v) {

    }

    private void loadData() {
        UpdateCartListner listner = new UpdateCartListner() {
            @Override
            public void onFailure() {
                generalhelper.ToastShow(ShippingAddressEditList.this, "网络连接错误");
            }

            @Override
            public void onResponse(String response) {
                Message msg = handler.obtainMessage();
                msg.what = 1;
                msg.obj = response;
                msg.sendToTarget();
            }
        };
        dal.getAddress(listner);
    }

    private void bindData() {
        if (adapter == null) {
            adapter = new myAdapter();
            rv_address.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
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
                            //selectall.setChecked(true);
                        } else if (!errmsg.isEmpty()) {
                            generalhelper.ToastShow(ShippingAddressEditList.this, "服务器错误:" + errmsg);
                            if (errmsg.contains("时间戳")) {
                                //ZganCommunityService.toGetServerData(43, PreferenceUtil.getUserName(), tokenHandler);
                            }
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
    public void onClick(View v) {
        {
            if (v.getId() == R.id.btn_manage) {
            }
        }
    }


    class myAdapter extends RecyclerView.Adapter<myAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.lo_shipping_edit_address_item, null, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ShippingAddressModel m = list.get(position);
            holder.txt_username.setText(m.getUserName());
            holder.txt_userphone.setText(m.getUserPhone());
            if (m.getIsUse() == 0) {

                holder.cb_isuse.setChecked(false);
            } else {
                holder.cb_isuse.setChecked(true);
            }
            holder.txt_detail.setText(m.getUserAdress());
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txt_username, txt_userphone, txt_detail, btn_delete, btn_edit;
            CheckBox cb_isuse;

            public ViewHolder(View itemView) {
                super(itemView);
                txt_username = (TextView) itemView.findViewById(R.id.txt_username);
                txt_userphone = (TextView) itemView.findViewById(R.id.txt_userphone);
                txt_detail = (TextView) itemView.findViewById(R.id.txt_detail);
                btn_delete = (TextView) itemView.findViewById(R.id.btn_delete);
                btn_edit = (TextView) itemView.findViewById(R.id.btn_edit);
                cb_isuse = (CheckBox) itemView.findViewById(R.id.cb_isuse);
            }

        }
    }
}