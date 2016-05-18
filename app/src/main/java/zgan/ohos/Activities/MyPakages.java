package zgan.ohos.Activities;

import android.graphics.Bitmap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import zgan.ohos.Contracts.IImageloader;
import zgan.ohos.Dals.MyPakageDal;
import zgan.ohos.Models.MyOrder;
import zgan.ohos.Models.MyPakage;
import zgan.ohos.R;
import zgan.ohos.utils.ImageLoader;

public class MyPakages extends myBaseActivity {

    RecyclerView rvpakages;
    //List<MyPakage> list;
    MyPakageDal dal;
    ImageLoader imageLoader;
    List<MyOrder> orderList;
    MyOrder order;


    @Override
    protected void initView() {
        setContentView(R.layout.activity_my_pakages);
        //order=(MyOrder)getIntent().getSerializableExtra("order");
        rvpakages = (RecyclerView) findViewById(R.id.rv_pakages);
        dal = new MyPakageDal();
        //list = dal.getList();
        orderList = dal.mPakageOrders;
        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        imageLoader = new ImageLoader();
        rvpakages.setAdapter(new myAdapter());
        rvpakages.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void ViewClick(View v) {

    }

    class myAdapter extends RecyclerView.Adapter<myAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.lo_mpakages_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            MyOrder m = orderList.get(position);
            MyPakage p = (MyPakage) m.GetGoods().get(0);
//            imageLoader.loadDrawableRS(MyPakages.this, p.getDraw(), holder.ivpreview, new IImageloader() {
//                @Override
//                public void onDownloadSucc(Bitmap bitmap, String c_url, View imageView, int w, int h) {
//                    ((ImageView) imageView).setImageBitmap(bitmap);
//                }
//            }, 100, 100);
            ImageLoader.bindBitmap(p.getpic_url(),holder.ivpreview,100,100);
            holder.txtpname.setText(p.gettitle());
            holder.txtporder.setText("订单号：" + m.getorder_id());
            //holder.txtpordertime.setText("下单时间：" + m.getAdd_time());
            holder.txtptotal.setText("总计：" + p.getptotal());
            holder.txtpused.setText("已使用：" + p.getpused());
            holder.txtpleft.setText("剩余：" + p.getpleft());
        }

        @Override
        public int getItemCount() {
            return orderList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivpreview;
            TextView txtpname, txtporder, txtpordertime, txtptotal, txtpused, txtpleft;

            public ViewHolder(View itemView) {
                super(itemView);
                ivpreview = (ImageView) itemView.findViewById(R.id.iv_preview);
                txtpname = (TextView) itemView.findViewById(R.id.txt_pname);
                txtporder = (TextView) itemView.findViewById(R.id.txt_porder);
                txtpordertime = (TextView) itemView.findViewById(R.id.txt_pordertime);
                txtptotal = (TextView) itemView.findViewById(R.id.txt_ptotal);
                txtpused = (TextView) itemView.findViewById(R.id.txt_pused);
                txtpleft = (TextView) itemView.findViewById(R.id.txt_pleft);
            }
        }
    }
}
