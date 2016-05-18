package zgan.ohos.ConstomControls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.view.View;

import zgan.ohos.R;

/**
 * Created by Administrator on 16-2-25.
 */
public class MyGifView extends View {
    private long movieStart;
    private Movie movie;

    public MyGifView(Context context) {
        this(context, null);
    }

    public MyGifView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyGifView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        movie = Movie.decodeStream(getResources().openRawResource(R.raw.monkey001));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long curTime = android.os.SystemClock.uptimeMillis();
        //第一次播放
        if (movieStart == 0) {
            movieStart = curTime;
        }
        if (movie != null) {
            int duraction = movie.duration();
            if (duraction == 0) {
                duraction = 1000;
            }
            int relTime = (int) ((curTime - movieStart) % duraction);
            movie.setTime(relTime);
            movie.draw(canvas,(getWidth()-movie.width())/2, (getHeight()-movie.height())/2);
            //强制重绘
            invalidate();
        }
    }
}
