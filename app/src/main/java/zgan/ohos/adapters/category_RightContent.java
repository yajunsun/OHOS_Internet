package zgan.ohos.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import zgan.ohos.Activities.GoodsItemDetail;
import zgan.ohos.Contracts.IImageloader;
import zgan.ohos.R;
import zgan.ohos.utils.ImageLoader;

/**
 * Created by yajunsun on 2015/11/17.
 */
public class category_RightContent extends RecyclerView.Adapter<category_RightContent.ViewHolder> {

    String[] picurls;
    Context context;
    ImageLoader imageLoader;
    int layout;

    public category_RightContent(String[] _picurls, Context _context, int _layout) {
        this.picurls = _picurls;
        this.context = _context;
        this.layout = _layout;
        imageLoader = new ImageLoader();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
//        imageLoader.loadImage(picurls[position], holder.ivsample, new OnImageDownload(), 200, 200);
        ImageLoader.bindBitmap(picurls[position],holder.ivsample,200,200);
        final Intent intent = new Intent(context, GoodsItemDetail.class);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("imgname", picurls[position]);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return picurls.length;
    }

    public class OnImageDownload implements IImageloader {
        @Override
        public void onDownloadSucc(Bitmap bitmap, String c_url, View imageView,  int w,int h) {
            ((ImageView)imageView).setImageBitmap(bitmap);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
            ivsample = (ImageView) itemView.findViewById(R.id.iv_sample);
            txtcontent = (TextView) itemView.findViewById(R.id.txt_content);
//            txtprice = (TextView) itemView.findViewById(R.id.txt_price);
//            txt_goodev_ratio = (TextView) itemView.findViewById(R.id.txt_good_ev_ratio);
//            txt_ev_count = (TextView) itemView.findViewById(R.id.txt_ev_count);
        }

        ImageView ivsample;
        TextView txtcontent;
        TextView txtprice;
        TextView txt_goodev_ratio;
        TextView txt_ev_count;
    }
}