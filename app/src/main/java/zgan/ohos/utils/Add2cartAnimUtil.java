package zgan.ohos.utils;

import android.animation.Animator;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.CycleInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import zgan.ohos.Activities.ShoppingCart;
import zgan.ohos.Contracts.IAddShopListener;
import zgan.ohos.R;

/**
 * Created by yajunsun on 16/10/16.
 */
public class Add2cartAnimUtil {

//    private Context mContext;
//
//    /**
//     * 添加物品按钮
//     */
//    private View mButtonView;
//
//    /**
//     * 购物车小图标
//     */
//    private View mShopCartView;
//    Point addP;
//
//    /**
//     * 所购买商品的图标
//     */
//    private View mShopView;
//
//    Point shopP;
//
//    private LinearLayout animLayout;
//
//    float a = -1f / 75f;
//    float v=0;//平抛X方向速度
//    float g=0;//平抛重力参数
//
//
//    private ObjectAnimator mObjectAnimator;
//
//    /**
//     * @param context
//     * @param btnView      添加物品按钮
//     * @param shopCartView 购物车小图标（你要添加的购物车容器）
//     * @param result       你购买商品的图标
//     */
//    public Add2cartAnimUtil(Context context, View btnView, View shopCartView,
//                            View result) {
//        this.mContext = context;
//        this.mButtonView = btnView;
//        this.mShopCartView = shopCartView;
//        this.mShopView = result;
//        init();
//    }
//
//    /**
//     * 初始化
//     */
//    private void init() {
//        ViewGroup rootView = (ViewGroup) ((Activity) mContext).getWindow()
//                .getDecorView();
//        animLayout = new LinearLayout(mContext);
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.MATCH_PARENT);
//        animLayout.setLayoutParams(lp);
//        //animLayout.setId();
//        animLayout.setBackgroundResource(android.R.color.transparent);
//        rootView.addView(animLayout);
//        animLayout.addView(mShopView);
//    }
//
//    /**
//     * 这里是根据三个坐标点{（0,0），（300,0），（150,300）}计算出来的抛物线方程
//     *
//     * @param x
//     * @return
//     */
//    private float getY(float x) {
//        return a * x * x + 4 * x;
//        //return 0-(x*addP.y/shopP.x);
//        //return (g/(2*v*v))*x*x;
////        x=x-shopP.x;
////        float y=(g/(2*v*v))*x*x+shopP.y;
////        Log.i("suntest x",String.valueOf(y));
////        Log.i("suntest y",String.valueOf(y));
//      //  return y;
//    }
//
//    public void addShopCart(final IAddShopListener listener) {
//        int[] start_location = new int[2];
//        //mButtonView.getLocationInWindow(start_location);
//        mButtonView.getLocationInWindow(start_location);
//
//        addViewToAnimLayout(animLayout, mShopView, start_location);
//        shopP=new Point(Math.round( mShopView.getX()),Math.round(mShopView.getY()));
//
//        int[] end_location = new int[2];
//        //end_location[0]=Math.round( mShopCartView.getX());
//        //end_location[1]=Math.round(mShopCartView.getY());
//        //mShopCartView.getLocationInWindow(end_location);
//        mShopCartView.getLocationInWindow(end_location);
//        addP=new Point(Math.round(mShopCartView.getX()),Math.round(mShopCartView.getY()));
//
//        setV(start_location[0] ,end_location[0]);
//        setG(end_location[1],start_location[1]);
//        Log.i("suntest",String.valueOf(v));
//        Log.i("suntest",String.valueOf(g));
//        float count = 300;
//        Keyframe[] xkeyframes = new Keyframe[(int) count];//x帧集合
//        Keyframe[] ykeyframes = new Keyframe[(int) count];//y帧集合
//        final float keyStep = 1f / (float) count;
//        float f = (float) (start_location[0] - end_location[0]) / count;
//        //X
//        float key = keyStep;
//        for (int i = 0; i < count; ++i) {
//            xkeyframes[i] = Keyframe.ofFloat(key, -i * f);
//            key += keyStep;
//        }
//
//        PropertyValuesHolder pvhX = PropertyValuesHolder.ofKeyframe(
//                "translationX", xkeyframes);
//        //y
//        key = keyStep;
//
//        for (int i = 0; i < count; ++i) {
//            //keyframes[i] = Keyframe.ofFloat(key, -getY(i + 1));
//            ykeyframes[i] = Keyframe.ofFloat(key, -getY(i+1));
//            key += keyStep;
//        }
//
//        PropertyValuesHolder pvhY = PropertyValuesHolder.ofKeyframe(
//                "translationY", ykeyframes);
//        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX",
//                1f, 0.5f);
//        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY",
//                1f, 0.5f);
//
//        if (mObjectAnimator == null) {
//
//            mObjectAnimator = ObjectAnimator.ofPropertyValuesHolder(mShopView,
//                    pvhX, pvhY, scaleX, scaleY).setDuration(1500);
//            mObjectAnimator.setInterpolator(new AccelerateInterpolator());
//            mObjectAnimator.addListener(new Animator.AnimatorListener() {
//
//                @Override
//                public void onAnimationStart(Animator animation) {
//                    // TODO Auto-generated method stub
//                    mShopView.setVisibility(View.VISIBLE);
//                }
//
//                @Override
//                public void onAnimationRepeat(Animator animation) {
//                    // TODO Auto-generated method stub
//
//                }
//
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    // TODO Auto-generated method stub
//                    mShopView.setVisibility(View.GONE);
//                    listener.addSucess();
//                }
//
//                @Override
//                public void onAnimationCancel(Animator animation) {
//                    // TODO Auto-generated method stub
//
//                }
//            });
//
//        }
//        if (com.tencent.mm.sdk.constants.Build.SDK_INT > 13)
//            if (!mObjectAnimator.isStarted()) {
//
//                mObjectAnimator.start();
//            }
//    }
//
//    private View addViewToAnimLayout(final ViewGroup vg, final View view,
//                                     int[] location) {
//        int x = location[0];
//        int y = location[1];
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.WRAP_CONTENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT);
//        lp.leftMargin = x;
//        lp.topMargin = y;
//        view.setLayoutParams(lp);
//        return view;
//    }
//    private void setV(int xs,int xe){
//        v=Math.round((xe-xs)/1.5);
//    }
//    private void setG(int ys,int ye){
//        g=Math.round((ye-ys)*2/(2.25));
//    }
private ImageView buyImg;//播放动画的参照imageview
    private int[] start_location = new int[2];// 这是用来存储动画开始位置的X、Y坐标;
    private int[] end_location = new int[2];// 这是用来存储动画结束位置的X、Y坐标;
    FrameLayout.LayoutParams lp;
    float density;
    public ViewGroup root;//动画层
    private  Thread //数据操作的非ui线程

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //非ui线程
                //这里执行动画完成后的数据处理，例如将商品加入购物车


                //发送消息给ui线程
                mThreadHandler.sendEmptyMessage(0);
            }
        });
    private  Handler mThreadHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        //线程操作完以后要及时关闭
                        thread.interrupt();
                        //ui操作
                        //这里做动画结束后的处理，例如购物车抖动动画
                        break;
                }
            }
        };
        public Add2cartAnimUtil(Activity activity, Drawable drawable) {
            density=AppUtils.getDensity(activity);
            lp=new FrameLayout.LayoutParams(Math.round(30*density),Math.round(30*density));
            buyImg = new ImageView(activity);//buyImg是动画的图片
            buyImg.setImageDrawable(drawable);// 设置buyImg的图片
            //buyImg.setImageBitmap(bitmap);//也可以设置bitmap，可以用商品缩略图来播放动画
            root = (ViewGroup) activity.getWindow().getDecorView();//创建一个动画层
            root.addView(buyImg);//将动画参照imageview放入
        }
    /**
     * 将image图片添加到动画层并放在起始坐标位置
     *
     * @param view     播放动画的view
     * @param location 起始位置
     * @return
     */
    private View addViewFromAnimLayout(View view, int[] location) {
        int x = location[0];
        int y = location[1];
//        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
//                FrameLayout.LayoutParams.WRAP_CONTENT,
//                FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = x;
        lp.topMargin = y;
        view.setLayoutParams(lp);
        return view;
    }
    public void startAnim(View startView, final View endView) {
        // 这是获取起始目标view在屏幕的X、Y坐标（这也是动画开始的坐标）
        startView.getLocationInWindow(start_location);
        // 购物车结束位置
        endView.getLocationInWindow(end_location);
        //将动画图片和起始坐标绘制成新的view，用于播放动画
        //将image图片添加到动画层
        /**这里为什么不直接传一个图片而是传一个imageview呢？
         * 因为我这样做的目的是clone动画播放控件，为什么要clone呢？
         * 因为如果用户连续点击添加购物车的话，如果只用一个imageview去播放动画的话，这个动画就会成还没播放完就回到原点重新播放。
         * 而如果clone一个imageview去播放，那么这个动画还没播放完，用户再点击添加购物车以后我们还是clone一个新的imageview去播放。
         * 这样动画就会出现好几个点而不是一个点还没播放完又缩回去。
         * 说的通俗点，就是依靠这个方法，把参照对象和起始位置穿进去，得到一个clone的对象来播放动画
         */View run_view = addViewFromAnimLayout(buyImg, start_location);

        // 计算位移
        int endX = end_location[0] - start_location[0];
        int endY = end_location[1] - start_location[1];

        //平移动画 绘制X轴 0到结束的x轴
        TranslateAnimation translateAnimationX = new TranslateAnimation(0,
                endX, 0, 0);
        //设置线性插值器
        translateAnimationX.setInterpolator(new LinearInterpolator());
        // 动画重复执行的次数
        translateAnimationX.setRepeatCount(0);
        //设置动画播放完以后消失，终止填充
        translateAnimationX.setFillAfter(true);

        //平移动画 绘制Y轴
        TranslateAnimation translateAnimationY = new TranslateAnimation(0, 0,
                0, endY);
        translateAnimationY.setInterpolator(new AccelerateInterpolator());
        translateAnimationY.setRepeatCount(0);
        translateAnimationX.setFillAfter(true);
        //ScaleAnimation scale = new ScaleAnimation(1f,0.9f,1f,0.9f);


        //将两个动画放在动画播放集合里
        // 设置false使每个子动画都使用自己的插值器
        AnimationSet set = new AnimationSet(false);
        //设置动画播放完以后消失，终止填充
        set.setFillAfter(false);
        set.addAnimation(translateAnimationY);
        set.addAnimation(translateAnimationX);
        //set.addAnimation(scale);
        set.setDuration(800);// 动画的执行时间
        /**
         * 动画开始播放的时候，参照对象要显示出来，如果不显示的话这个动画会看不到任何东西。
         * 因为不管用户点击几次动画，播放的imageview都是从参照对象buyImg中clone来的
         * */
        buyImg.setVisibility(View.VISIBLE);
        run_view.startAnimation(set);
        // 动画监听事件
        set.setAnimationListener(new Animation.AnimationListener() {
            // 动画的开始
            @Override
            public void onAnimationStart(Animation animation) {

            }

            //动画重复中
            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
            }

            // 动画的结束
            @Override
            public void onAnimationEnd(Animation animation) {
                //动画播放完以后，参照对象要隐藏
                buyImg.setVisibility(View.GONE);
                Animation translateAnimation = new TranslateAnimation(0, 5, 0, 5);
                translateAnimation.setInterpolator(new CycleInterpolator(4));
                translateAnimation.setDuration(200);
                endView.startAnimation(translateAnimation);
                //结束后访问数据
                thread.start();
            }
        });
    }
}