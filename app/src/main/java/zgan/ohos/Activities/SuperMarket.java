package zgan.ohos.Activities;

import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
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
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;

import okio.BufferedSink;
import zgan.ohos.ConstomControls.SortView.SortModel;
import zgan.ohos.Dals.SuperMarketDal;
import zgan.ohos.Models.SM_GoodsM;
import zgan.ohos.Models.SM_SecondaryM;
import zgan.ohos.Models.SuperMarketM;
import zgan.ohos.R;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;
import zgan.ohos.utils.generalhelper;

/**
 * create by yajunsun
 * <p/>
 * 超市购界面
 */
public class SuperMarket extends myBaseActivity {
    SuperMarketDal dal;
    ListView lstclass;
    RecyclerView rvproducts;
    LinearLayout llcategray1;
    LinearLayout llcategray2;
    sm_class_Adapter classAdapter;
    sm_product_Adapter productAdapter;
    RecyclerView.LayoutManager product_layoutManager = new LinearLayoutManager(SuperMarket.this);
    List<SuperMarketM> list;
    List<SM_SecondaryM> secondarylst;
    List<SM_GoodsM> goodslst;
    //分类选择索引
    int lastClassIndex = 0;
    int catParentWidth = 0;


    @Override
    protected void initView() {
        setContentView(R.layout.activity_super_market);
        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        catParentWidth = (AppUtils.getWindowSize(SuperMarket.this).x / 3) * 2;
        dal = new SuperMarketDal();
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
                    bindData();
                }
            }
        });
        rvproducts = (RecyclerView) findViewById(R.id.rv_products);
        llcategray1 = (LinearLayout) findViewById(R.id.ll_categray1);
        llcategray2 = (LinearLayout) findViewById(R.id.ll_categray2);
        loadData();
    }

    //从网络获取数据
    protected void loadData() {
        toSetProgressText("正在加载...");
        toShowProgress();

        OkHttpClient mOkHttpClient = new OkHttpClient();
//创建一个Request
        FormEncodingBuilder builder = new FormEncodingBuilder();
        builder.add("ID", "2016");
        builder.add("account", PreferenceUtil.getUserName());
        builder.add("token", SystemUtils.getNetToken());
        final Request request = new Request.Builder()
                .url("http://app.yumanc.1home1shop.com/V1_0/marketlist.aspx").post(builder.build())
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

    //绑定数据
    void bindData() {
        if (list != null && list.size() > 0) {
            bindClass();
            secondarylst = list.get(lastClassIndex).getcategory();
        }
        if (secondarylst != null && secondarylst.size() > 0) {
            bindSecodary();
            goodslst = secondarylst.get(0).getlist();
        }
        if (goodslst != null && goodslst.size() > 0) {
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
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(50, 50, 0, 50);
        int usedWidth=50;
        LinearLayout parent=llcategray1;
        for (SM_SecondaryM cat : secondarylst) {
            TextView txt = new TextView(SuperMarket.this);
            txt.setLayoutParams(params);
            txt.setText(cat.getname());
            txt.setClickable(true);
            txt.setOnClickListener(new catOnclick(cat));
            usedWidth+=50+(cat.getname().length()*50);
            if (catParentWidth-usedWidth<50)
            {
                if(parent.getId()==llcategray2.getId())
                    break;
                parent=llcategray2;
                usedWidth=50+(cat.getname().length()*50);
            }
            parent.addView(txt);
        }
    }

    //绑定商品列表
    void bindProduct() {
        if (productAdapter == null) {
            productAdapter = new sm_product_Adapter(goodslst);
            rvproducts.setLayoutManager(product_layoutManager);
            rvproducts.setAdapter(productAdapter);
        } else {
            productAdapter.notifyDataSetChanged();
        }
    }

    private Handler handler = new Handler() {
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
                        } else if (!errmsg.isEmpty()) {
                            generalhelper.ToastShow(SuperMarket.this, "服务器错误:" + errmsg);
                        }
                    } catch (JSONException jse) {

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                toCloseProgress();
            }
        }
    };

    @Override
    public void ViewClick(View v) {

    }

    class catOnclick implements View.OnClickListener {
        SM_SecondaryM cat;

        public catOnclick(SM_SecondaryM _cat) {
            cat = _cat;
        }

        @Override
        public void onClick(View view) {
            generalhelper.ToastShow(SuperMarket.this, cat.getname());
        }
    }

    //商品列表适配器
    class sm_product_Adapter extends RecyclerView.Adapter<sm_product_Adapter.ViewHoler> {
        List<SM_GoodsM> goodsMs;

        public sm_product_Adapter(List<SM_GoodsM> _list) {
            goodsMs = _list;
        }

        @Override
        public ViewHoler onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHoler(getLayoutInflater().inflate(R.layout.lo_sm_product_item, null));
        }

        @Override
        public void onBindViewHolder(ViewHoler holder, int position) {
            SM_GoodsM goodsM = goodsMs.get(position);
            ImageLoader.bindBitmap(goodsM.getpic_url(), holder.img_product);
            holder.txt_name.setText(goodsM.getname());
            holder.txt_price.setText(String.valueOf(goodsM.getprice()));
            holder.txt_oldprice1.setText(goodsM.getoldprice());
            holder.txt_oldprice2.setText(goodsM.getoldprice());
            if (!goodsM.getoldprice().equals("") && !goodsM.getoldprice().equals("0")) {
                if (goodsM.gettype_list() != null && goodsM.gettype_list().size() > 0) {
                    holder.ll_oldprice1.setVisibility(View.GONE);
                    holder.ll_oldprice2.setVisibility(View.VISIBLE);
                } else {
                    holder.ll_oldprice1.setVisibility(View.VISIBLE);
                    holder.ll_oldprice2.setVisibility(View.GONE);
                }

            }
        }

        @Override
        public int getItemCount() {
            return goodsMs.size();
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
            viewHolder.llselected.setVisibility(superMarketM.getIsSelected() == 1 ? View.VISIBLE : View.GONE);
            return view;
        }

        class ViewHolder {
            LinearLayout llselected;
            TextView txtname;
            ImageView imgrecommand;
        }
    }
}