package zgan.ohos.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import zgan.ohos.Activities.EventFrontPage;
import zgan.ohos.Dals.Product_PicsDal;
import zgan.ohos.Models.Event_Product;
import zgan.ohos.Models.Product_Pics;
import zgan.ohos.R;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.generalhelper;

/**
 * Created by Administrator on 2015/11/26.
 */
public class event_ListAdapter extends RecyclerView.Adapter<event_ListAdapter.ViewHolder> {

    Context context;
    List<Event_Product> list;
    int layout;
    ImageLoader imageLoader;
    LayoutInflater inflater;

    public event_ListAdapter(Context _context, List<Event_Product> _lst, int _layout) {
        context = _context;
        list = _lst;
        layout = _layout;
        imageLoader = new ImageLoader();
        inflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.txt_event_rule.setText(list.get(position).getProduct().getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EventFrontPage.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("currentevent", list.get(position).getEvent());
                bundle.putSerializable("currentproduct", list.get(position).getProduct());
                //bundle.putInt("productid", list.get(position).getProduct().getId());
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
        Date date = new Date();
        holder.txt_event_time.setText(
                generalhelper.getStringFromDate(
                        generalhelper.getDateFromString(
                                list.get(position).getEvent().getBtime(), date), "yyyy-MM-dd HH:mm")
                        + "到" + generalhelper.getStringFromDate(
                        generalhelper.getDateFromString(
                                list.get(position).getEvent().getEtime(), date),
                        "yyyy-MM-dd HH:mm"));
        try {
            List<Product_Pics> pics = new Product_PicsDal().getProductPics(list.get(position).getProduct().getId());
            if (pics.size() > 0)
//                imageLoader.loadImage(pics.get(0).getPicName(), holder.iv_sample, new IImageloader() {
//                    @Override
//                    public void onDownloadSucc(Bitmap bitmap, String c_url, View imageView) {
//                        ((ImageView)imageView).setImageBitmap(bitmap);
//                    }
//                }, 200, 200);
                ImageLoader.bindBitmap(pics.get(0).getPicName(), holder.iv_sample, 200, 200);
        } catch (Exception ex) {
            generalhelper.ToastShow(context, ex.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_sample;
        TextView txt_event_time, txt_event_rule;

        public ViewHolder(View itemView) {
            super(itemView);
            iv_sample = (ImageView) itemView.findViewById(R.id.iv_sample);
            txt_event_rule = (TextView) itemView.findViewById(R.id.txt_event_rule);
            txt_event_time = (TextView) itemView.findViewById(R.id.txt_event_time);
        }
    }
}