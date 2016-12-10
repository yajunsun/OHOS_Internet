package zgan.ohos.adapters;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by yajunsun on 2016/12/10.
 */
public class RectViewItemSpace extends RecyclerView.ItemDecoration {
    private int space;
    private int l=0,t=0,r=0,b=0;
    public RectViewItemSpace(int space)
    {
        this.space = space;
    }
    public RectViewItemSpace(int l,int t,int r,int b)
    {
        this.l=l;
        this.t=t;
        this.r=r;
        this.b=b;
    }
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

//        if(parent.getChildPosition(view) != 0) {
//            outRect.top = space;
//            outRect.left=space;
//            outRect.left=space;
//            outRect.right=space;
//        }
        outRect.set(l,t,r,b);
    }
}
