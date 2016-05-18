package zgan.ohos.ConstomControls;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 * Created by yajunsun on 2016/1/6.
 */
public class MyCircleMenuLayout extends ViewGroup {
    public MyCircleMenuLayout(Context context) {
        this(context, null);
    }

    public MyCircleMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyCircleMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context=context;
        IniView();
    }
    private void IniView() {

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        DisplayMetrics dm1 = context.getResources().getDisplayMetrics();
        density = (int)dm1.density;

        wm.getDefaultDisplay().getMetrics(dm);
        mScreenHeight = dm.heightPixels;
        mScreenWidth = dm.widthPixels;
    }
    Context context;
    int mScreenWidth;
    int mScreenHeight;
     Point ab, be, ec, cd, Ptop, Pcenter;
    int k;
    int density=1;

    @Override
    protected void onMeasure(int WidthMeasureSpec, int HeightMeasureSpec) {
        super.onMeasure(WidthMeasureSpec, HeightMeasureSpec);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);
            measureChild(view, WidthMeasureSpec, HeightMeasureSpec);
        }
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) getLayoutParams();
//        // 设置ViewGroup的宽度
//        marginLayoutParams.width = mScreenWidth;
//        marginLayoutParams.height=mScreenHeight;
//        setLayoutParams(marginLayoutParams);
        int mWidth = getMeasuredWidth(), mHeight = getMeasuredHeight();
        r = mWidth / 2;
        k = (int) Math.sqrt(Math.pow(r, 2) / 2);
        ab = new Point(k/2, k/2+mHeight / 2);
        be = new Point(k , k + (mHeight / 2));
        ec = new Point(r + k/2 , k + (mHeight / 2));
        cd = new Point(k + r, k/2+mHeight / 2);
        Ptop = new Point(0, r / 2);
        Pcenter = new Point(r, mHeight / 2);
        int offset=30*density;
        if (getChildAt(0) instanceof MyCirlcleMenu) {
            MyCirlcleMenu myCirlcleMenu = (MyCirlcleMenu) getChildAt(0);
            if (getChildCount() == 6) ;
            {
                //0
                int w, h;
                w = myCirlcleMenu.getMeasuredWidth();
                h = myCirlcleMenu.getMeasuredHeight();
                myCirlcleMenu.layout(0, mHeight / 2 - h / 2, mWidth, mHeight / 2 + h / 2);
                //1
                View top = getChildAt(1);
                w = top.getMeasuredWidth();
                h = top.getMeasuredHeight();
                top.layout(mWidth / 2 - Ptop.x - w / 2, mHeight / 2 - Ptop.y - h / 2, mWidth / 2 - Ptop.x + w / 2, mHeight / 2 - Ptop.y + h / 2);
                //2
                View center = getChildAt(2);
                w = center.getMeasuredWidth();
                h = center.getMeasuredHeight();
                center.layout(mWidth / 2 - w / 2, mHeight / 2  - h / 2, mWidth / 2  + w / 2, getHeight() / 2 + h / 2);
                //3
                View vab = getChildAt(3);
                w = vab.getMeasuredWidth();
                h = vab.getMeasuredHeight();
                vab.layout(ab.x - w / 2+offset, ab.y - h / 2-offset, ab.x + w / 2+offset,  ab.y + h/2-offset);
                //4
                View vbe = getChildAt(4);
                w = vbe.getMeasuredWidth();
                h = vbe.getMeasuredHeight();
                vbe.layout(be.x - w / 2+offset/2, be.y - h / 2-offset-10*density, be.x + w / 2+offset/2,  be.y + h/2-offset-10*density);
                //5
                View vec = getChildAt(5);
                w = vec.getMeasuredWidth();
                h = vec.getMeasuredHeight();
                vec.layout(ec.x - w / 2-offset, ec.y - h / 2-offset-10*density, ec.x + w / 2-offset,  ec.y + h/2-offset-10*density);
                //6
                View vcd = getChildAt(6);
                w = vcd.getMeasuredWidth();
                h = vcd.getMeasuredHeight();
                vcd.layout(cd.x - w / 2-offset, cd.y - h / 2-offset, cd.x + w / 2-offset,  cd.y + h/2-offset);
            }
        }
    }
}
