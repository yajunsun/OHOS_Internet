package zgan.ohos.ConstomControls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by Administrator on 2016/1/6.
 */
public class MyArcButton extends Button {
    public MyArcButton(Context context) {
        this(context, null);
    }

    public MyArcButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyArcButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        mpaint=new Paint();
        mpaint.setAntiAlias(true);
        mpaint.setStyle(Paint.Style.FILL);
        mpaint.setColor(Color.parseColor(BtnColor));
    }
    String BtnColor = "#FF000000";
    int angle=45;
    Paint mpaint;
    int index=0;
    int mWidth;
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mWidth=getWidth();
        canvas.drawArc(new RectF(mWidth*0.1f,mWidth*0.1f,mWidth*.5f,mWidth*.5f),0,angle,true,mpaint);
//        if (index==0)
//            canvas.rotate();
    }
}
