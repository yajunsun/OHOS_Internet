package zgan.ohos.utils;

import android.animation.Animator;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;

import zgan.ohos.Activities.ShoppingCart;
import zgan.ohos.Contracts.IAddShopListener;

/**
 * Created by yajunsun on 16/10/16.
 */
public class Add2cartAnimUtil {

    private Context mContext;

    /**
     * 添加物品按钮
     */
    private View mButtonView;

    /**
     * 购物车小图标
     */
    private View mShopCartView;
    Point addP;

    /**
     * 所购买商品的图标
     */
    private View mShopView;

    Point shopP;

    private LinearLayout animLayout;

    float a = -1f / 75f;
    float v=0;//平抛X方向速度
    float g=0;//平抛重力参数


    private ObjectAnimator mObjectAnimator;

    /**
     * @param context
     * @param btnView      添加物品按钮
     * @param shopCartView 购物车小图标（你要添加的购物车容器）
     * @param result       你购买商品的图标
     */
    public Add2cartAnimUtil(Context context, View btnView, View shopCartView,
                            View result) {
        this.mContext = context;
        this.mButtonView = btnView;
        this.mShopCartView = shopCartView;
        this.mShopView = result;
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        ViewGroup rootView = (ViewGroup) ((Activity) mContext).getWindow()
                .getDecorView();
        animLayout = new LinearLayout(mContext);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        animLayout.setLayoutParams(lp);
        //animLayout.setId();
        animLayout.setBackgroundResource(android.R.color.transparent);
        rootView.addView(animLayout);
        animLayout.addView(mShopView);
    }

    /**
     * 这里是根据三个坐标点{（0,0），（300,0），（150,300）}计算出来的抛物线方程
     *
     * @param x
     * @return
     */
    private float getY(float x) {
        //return a * x * x + 4 * x;
        //return 0-(x*addP.y/shopP.x);
        //return (g/(2*v*v))*x*x;
        x=x-shopP.x;
        return (g/(2*v*v))*x*x+shopP.y;
    }

    public void addShopCart(final IAddShopListener listener) {
        int[] start_location = new int[2];
        mButtonView.getLocationInWindow(start_location);

        addViewToAnimLayout(animLayout, mShopView, start_location);
        shopP=new Point(Math.round( mShopView.getX()),Math.round(mShopView.getY()));

        int[] end_location = new int[2];
        //end_location[0]=Math.round( mShopCartView.getX());
        //end_location[1]=Math.round(mShopCartView.getY());
        mShopCartView.getLocationInWindow(end_location);
        addP=new Point(Math.round(mShopCartView.getX()),Math.round(mShopCartView.getY()));

        setV(start_location[0] ,end_location[0]);
        setG(end_location[1],start_location[1]);
        float count = 300;
        Keyframe[] xkeyframes = new Keyframe[(int) count];//x帧集合
        Keyframe[] ykeyframes = new Keyframe[(int) count];//y帧集合
        final float keyStep = 1f / (float) count;
        float f = (float) (start_location[0] - end_location[0]) / count;
        //X
        float key = keyStep;
        for (int i = 0; i < count; ++i) {
            xkeyframes[i] = Keyframe.ofFloat(key, -i * f);
            key += keyStep;
        }

        PropertyValuesHolder pvhX = PropertyValuesHolder.ofKeyframe(
                "translationX", xkeyframes);
        //y
        key = keyStep;

        for (int i = 0; i < count; ++i) {
            //keyframes[i] = Keyframe.ofFloat(key, -getY(i + 1));
            ykeyframes[i] = Keyframe.ofFloat(key, -getY((float)xkeyframes[i].ofFloat(key).getValue()));
            key += keyStep;
        }

        PropertyValuesHolder pvhY = PropertyValuesHolder.ofKeyframe(
                "translationY", ykeyframes);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX",
                1f, 0.5f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY",
                1f, 0.5f);

        if (mObjectAnimator == null) {

            mObjectAnimator = ObjectAnimator.ofPropertyValuesHolder(mShopView,
                    pvhX, pvhY, scaleX, scaleY).setDuration(1500);
            mObjectAnimator.setInterpolator(new AccelerateInterpolator());
            mObjectAnimator.addListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {
                    // TODO Auto-generated method stub
                    mShopView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    // TODO Auto-generated method stub
                    mShopView.setVisibility(View.GONE);
                    listener.addSucess();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    // TODO Auto-generated method stub

                }
            });

        }
        if (com.tencent.mm.sdk.constants.Build.SDK_INT > 13)
            if (!mObjectAnimator.isStarted()) {

                mObjectAnimator.start();
            }
    }

    private View addViewToAnimLayout(final ViewGroup vg, final View view,
                                     int[] location) {
        int x = location[0];
        int y = location[1];
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = x;
        lp.topMargin = y;
        view.setLayoutParams(lp);
        return view;
    }
    private void setV(int xs,int xe){
        v=Math.round((xe-xs)/1.5);
    }
    private void setG(int ys,int ye){
        g=Math.round((ye-ys)*2/(2.25));
    }

}