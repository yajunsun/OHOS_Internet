package zgan.ohos.ConstomControls;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import zgan.ohos.utils.generalhelper;

/**
 * Created by yajunsun on 2015/12/24.
 */
public class MyWatch extends View {
    public MyWatch(Context context) {
        this(context, null);
    }

    public MyWatch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyWatch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
    }

    Paint paint;
    Bitmap basebmp;
    Bitmap numbmp;
    Date nowDate;
    boolean isFirstDraw = true;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int mWidth = getMeasuredWidth();
        int mHeight = getMeasuredHeight();
        if (isFirstDraw)
            baseDraw();
        canvas.drawBitmap(basebmp, 0, 0, null);
        canvas.saveLayerAlpha(0, 0, mWidth, mHeight, 254, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
        if (numbmp != null) {
            canvas.drawBitmap(numbmp, 0, 50, null);
            canvas.saveLayerAlpha(0, 0, mWidth, 50, 254, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
        }
        if (nowDate != null) {
            int houre = nowDate.getHours();
            int minute = nowDate.getMinutes();
            int second = nowDate.getSeconds();
            //时针
            paint.setStrokeWidth(6);

            canvas.translate(mWidth / 2, mHeight / 2);
            canvas.rotate(180);
            canvas.rotate(30 * houre);
            canvas.drawLine(0, 0, 0, mWidth / 4, paint);
            //canvas.save();
            //分针
            paint.setStrokeWidth(3);
            //canvas.translate(mWidth / 2, mHeight / 2);
            canvas.rotate(-30 * houre);
            canvas.rotate(6 * minute);
            canvas.drawLine(0, 0, 0, mWidth / 2 - 80, paint);
            //canvas.restore();
            //秒针
            paint.setStrokeWidth(1);
            canvas.rotate(-6 * minute);
            canvas.rotate(6 * second);
            canvas.drawLine(0, 0, 0, mWidth / 2 - 30, paint);
        }
    }

    private void baseDraw() {
        int mWidth = getMeasuredWidth();
        int mHeight = getMeasuredHeight();
        basebmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(basebmp);
        /********画表盘************/
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        //圆心
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(20);
        canvas.drawCircle(mWidth / 2, mHeight / 2, 10, paint);
        //圆盘
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(mWidth / 2, mHeight / 2, mWidth / 2, paint);
        //刻度
        for (int i = 0; i < 60; i++) {
            //int time = 12;
            if (i == 0 || i == 15 || i == 30 || i == 45) {
                paint.setStrokeWidth(4);
                paint.setTextSize(15);
                canvas.drawLine(mWidth / 2, mHeight / 2 - mWidth / 2, mWidth / 2, mHeight / 2 - mWidth / 2 + 30, paint);
                paint.setStrokeWidth(2);
            } else {
                paint.setStrokeWidth(1);
                paint.setTextSize(10);
                if (i == 5 || i == 10 || i == 20 || i == 25 || i == 35 || i == 40 || i == 50 || i == 55) {
                    canvas.drawLine(mWidth / 2, mHeight / 2 - mWidth / 2, mWidth / 2, mHeight / 2 - mWidth / 2 + 20, paint);
                } else {
                    canvas.drawLine(mWidth / 2, mHeight / 2 - mWidth / 2, mWidth / 2, mHeight / 2 - mWidth / 2 + 10, paint);
                }
            }
            canvas.rotate(6, mWidth / 2, mHeight / 2);
        }
        canvas.save();

        int a = mWidth / 4-20;
        int b = (int) (Math.sqrt(3) * mWidth) / 4-20;
        int c = mWidth / 2-40;
        canvas.translate(mWidth / 2, mHeight / 2);
        //canvas.rotate(-180);
        for (int i = 0; i < 12; i++) {
            switch (i) {
                case 0:
                    canvas.drawText("6", 0, c, paint);
                    break;
                case 1:
                    canvas.drawText("5", a, b, paint);
                    break;
                case 2:
                    canvas.drawText("4", b, a, paint);
                    break;
                case 3:
                    canvas.drawText("3", c, 0, paint);
                    break;
                case 4:
                    canvas.drawText("2", b, -a, paint);
                    break;
                case 5:
                    canvas.drawText("1", a, -b, paint);
                    break;
                case 6:
                    canvas.drawText("12", 0, -c, paint);
                    break;
                case 7:
                    canvas.drawText("11", -a, -b, paint);
                    break;
                case 8:
                    canvas.drawText("10", -b, -a, paint);
                    break;
                case 9:
                    canvas.drawText("9", -c, 0, paint);
                    break;
                case 10:
                    canvas.drawText("8", -b, a, paint);
                    break;
                case 11:
                    canvas.drawText("7", -a, b, paint);
                    break;

//                case 12:
//                    canvas.drawText("12", 0, c, paint);
//                    break;
//                case 1:
//                    canvas.drawText("1", a, b, paint);
//                    break;
//                case 2:
//                    canvas.drawText("2", b, a, paint);
//                    break;
//                case 3:
//                    canvas.drawText("3", c, 0, paint);
//                    break;
//                case 4:
//                    canvas.drawText("4", b, -a, paint);
//                    break;
//                case 5:
//                    canvas.drawText("5", a, -b, paint);
//                    break;
//                case 6:
//                    canvas.drawText("6", 0, -c, paint);
//                    break;
//                case 7:
//                    canvas.drawText("7", -a, -b, paint);
//                    break;
//                case 8:
//                    canvas.drawText("8", -b, -a, paint);
//                    break;
//                case 9:
//                    canvas.drawText("9", -c, 0, paint);
//                    break;
//                case 10:
//                    canvas.drawText("10", -b, a, paint);
//                    break;
//                case 11:
//                    canvas.drawText("11", -a, b, paint);
//                    break;
//
            }
        }

        canvas.restore();
        timer.schedule(task, 0, 1000);
        isFirstDraw = false;
    }

    private void numdateDraw() {
        nowDate = new Date();
        numbmp = Bitmap.createBitmap(getMeasuredWidth(), 50, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(numbmp);
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(20);
        paint.setTextSize(40);
        canvas.drawText(generalhelper.getStringFromDate(nowDate), 0, 50, paint);
    }

    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            handler.sendEmptyMessage(1);
        }
    };
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                numdateDraw();
                invalidate();
            }
        }
    };
}
