package zgan.ohos.ConstomControls;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Scroller;

/**
 * Created by yajunsun on 2015/12/17.
 */
public class MyHorizentalScrollView extends ViewGroup {

    public MyHorizentalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

        IniView();
    }

    public MyHorizentalScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyHorizentalScrollView(Context context) {
        this(context, null);
    }

    private void IniView() {
        scroller = new Scroller(context);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        DisplayMetrics dm1 = context.getResources().getDisplayMetrics();
        int sh = dm1.heightPixels;
        int sw = dm1.widthPixels;
        float density = dm1.density;

        wm.getDefaultDisplay().getMetrics(dm);
        mScreenHeight = dm.heightPixels;
        mScreenWidth = dm.widthPixels;
    }

    Context context;
    Scroller scroller;
    int mStart;
    int mEnd;
    int mScreenWidth;
    int mScreenHeight;
    int mLastX;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) getLayoutParams();
        // 设置ViewGroup的宽度
        marginLayoutParams.width = count * mScreenWidth;
        setLayoutParams(marginLayoutParams);
        for (int i = 0; i < count; i++) {
            View v = getChildAt(i);
            if (v.getVisibility() != View.GONE) {
                v.layout(i * mScreenWidth, t, (i + 1) * mScreenWidth, b);
            }
        }
    }


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
    public boolean onTouchEvent(MotionEvent e) {
        //当前触摸点的X位置
        int x = (int) e.getX();
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //上次触摸点的X位置
                mLastX = x;
                //如果手指有滑动，则这是滑动的起点（相对于view的坐标，不是相对于屏幕的坐标）
                mStart = getScrollX();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!scroller.isFinished())
                    scroller.abortAnimation();
                int dx = mLastX - x;
                //如果view处于第一个子view的状态下像右划，左边是没有内容的
                if (getScrollX() < 0)
                    //所以划不动
                    dx = 0;
                //如果view处于最后一个子view的状态下向左划，右边也没有内容
                if (getScrollX() > getWidth() - mScreenWidth)
                    //所以划不动
                    dx = 0;
                //滑动dx的距离dx>0是像左划，dx<0是向右划
                scrollBy(dx, 0);
                //更新上次触摸的X点
                mLastX = x;
                break;
            case MotionEvent.ACTION_UP:
                //手指抬起来的这个X点，相对于view
                mEnd = getScrollX();
                //手指按下和手指抬起来相对滑动的距离
                int dScrollx = mEnd - mStart;
                //如果view的滑动方向是像左
                if (dScrollx > 0) {
                    //滑动的距离小于屏幕的1/3时
                    if (dScrollx < mScreenWidth / 3)
                        //view像右回dScrollX的距离
                        scroller.startScroll(getScrollX(), 0, -dScrollx, 0);
                    else
                        //view想左移动，手动滑动和自动滑动的总距离是一个屏幕的宽度
                        scroller.startScroll(getScrollX(), 0, mScreenWidth - dScrollx, 0);
                }
                //向右滑动
                else {
                    //滑动距离小于屏幕的1/3
                    if (-dScrollx < mScreenWidth / 3)
                        scroller.startScroll(getScrollX(), 0, -dScrollx, 0);
                    else
                        scroller.startScroll(getScrollX(), 0, -mScreenWidth - dScrollx, 0);
                }
                break;
        }
        postInvalidate();
        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), 0);
            postInvalidate();
        }
    }

}
