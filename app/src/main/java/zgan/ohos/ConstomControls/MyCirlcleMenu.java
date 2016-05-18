package zgan.ohos.ConstomControls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by yajunsun on 2016/1/6.
 */
public class MyCirlcleMenu extends View {


    public MyCirlcleMenu(Context context) {
        super(context);
    }

    public MyCirlcleMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyCirlcleMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    String firstBtnColor = "#FFFFA924";
    String secondBtnColor = "#FFA9A9DB";
    //分割线端点
    Point p1, p2, p3, p4, p5;
    //圆的半径
    int r;
    //非直角边的端点位置
    int k;
    //视图的宽度和长度
    int mWidth, mHeight;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        r = mWidth / 2 - 50;
        k = (int) Math.sqrt(Math.pow(r, 2) / 2);
        p1 = new Point(-r, 0);
        p2 = new Point(-k, k);
        p3 = new Point(k, k);
        p4 = new Point(r, 0);
        p5 = new Point(0, r);


        Paint mpaint = new Paint();
        mpaint.setAntiAlias(true);
        mpaint.setStyle(Paint.Style.STROKE);
        mpaint.setStrokeWidth(50);
        mpaint.setAlpha(30);
        mpaint.setColor(Color.parseColor("#80EAA435"));

        canvas.drawCircle(mWidth / 2, mHeight / 2, r, mpaint);
        mpaint.setAlpha(100);
        mpaint.setStyle(Paint.Style.FILL);
        mpaint.setColor(Color.parseColor("#F1F1F1"));
        canvas.drawCircle(mWidth / 2, mHeight / 2, r, mpaint);

        mpaint.setStrokeWidth(5);
        mpaint.setColor(Color.parseColor("#FFF7F7F7"));


        canvas.drawLine(mWidth / 2, mHeight / 2, (mWidth / 2) + p1.x, (mHeight / 2) + p1.y, mpaint);

        canvas.drawLine(mWidth / 2, mHeight / 2, (mWidth / 2) + p2.x, (mHeight / 2) + p2.y, mpaint);
        canvas.drawLine(mWidth / 2, mHeight / 2, (mWidth / 2) + p5.x, (mHeight / 2) + p5.y, mpaint);
        canvas.drawLine(mWidth / 2, mHeight / 2, (mWidth / 2) + p3.x, (mHeight / 2) + p3.y, mpaint);
        canvas.drawLine(mWidth / 2, mHeight / 2, (mWidth / 2) + p4.x, (mHeight / 2) + p4.y, mpaint);


        mpaint.setColor(Color.parseColor("#FFFFA924"));
        canvas.drawCircle(mWidth / 2, mHeight / 2, r / 3, mpaint);

        //gmd_call一键呼叫 gmd_store一家一店 gmd_lock_open远程开门 gmd_business物业服务 oct_repo政务信息

    }
}
