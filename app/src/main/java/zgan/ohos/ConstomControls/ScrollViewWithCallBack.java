package zgan.ohos.ConstomControls;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by Administrator on 16-4-1.
 */
public class ScrollViewWithCallBack extends ScrollView {
    public ScrollViewWithCallBack(Context context) {
        super(context);
    }

    public ScrollViewWithCallBack(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollViewWithCallBack(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private OnScrollListener scrollViewListener = null;

    public void setScrollViewListener(OnScrollListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if (scrollViewListener != null) {
            scrollViewListener.OnScroll(this, x, y, oldx, oldy);
        }
    }
    public interface OnScrollListener
    {
        void OnScroll(ScrollViewWithCallBack scrollView, int x, int y, int oldx, int oldy);
    }

}
