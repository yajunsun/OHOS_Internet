package zgan.ohos.ConstomControls;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import zgan.ohos.R;

/**
 * Created by yajunsun on 16/10/16.
 */
public class SM_CartCountDown extends LinearLayout {

    TextView txthour, txtminutes, txtseconds;
    long mTotalSenconds;
    Timer timer;
    String mhourStr, mMimutes, mSecondes;

    public SM_CartCountDown(Context context) {
        this(context, null);
    }

    public SM_CartCountDown(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SM_CartCountDown(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View v = LayoutInflater.from(context).inflate(R.layout.lo_cart_count_down, this,true);
        txthour = (TextView) v.findViewById(R.id.txt_hour);
        txtminutes = (TextView) v.findViewById(R.id.txt_minutes);
        txtseconds = (TextView) v.findViewById(R.id.txt_seconds);
    }

    public void StartCount(long totalSeconds) {
        mTotalSenconds = totalSeconds;
        //long day = mTotalSenconds / (24 * 60 * 60 );
        //long hour = (mTotalSenconds / (60 * 60 ) - day * 24);
        //long hour = (mTotalSenconds / (60 * 60));
        //long min = ((mTotalSenconds / (60)) - hour * 60);
        //long s = (mTotalSenconds - hour * 60 * 60 - min * 60);
        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //holder.txttimer.setText(""+day+"天"+hour+"小时"+min+"分"+s+"秒");
                int hour =Math.round (mTotalSenconds / (60 * 60));
                int min =Math.round ((mTotalSenconds / 60) - hour * 60);
                int s =Math.round (mTotalSenconds - hour * 60 * 60 - min * 60);
                Message msg = handler.obtainMessage();
                msg.what = 3;
                //msg.obj = min + "分" + s + "秒";
                mhourStr = hour < 10 ? "0" + String.valueOf(hour) : String.valueOf(hour);
                mMimutes = min < 10 ? "0" + String.valueOf(min) : String.valueOf(min);
                mSecondes = s < 10 ? "0" + String.valueOf(s) : String.valueOf(s);
                handler.sendMessage(msg);
                if (hour == 0 && min == 0 && s == 0) {
                    handler.sendEmptyMessage(0);
                }
            }
        }, 0, 1000);
    }

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 3) {
                //计数中
                txthour.setText(mhourStr);
                txtminutes.setText(mMimutes);
                txtseconds.setText(mSecondes);
            } else if (msg.what == 0) {
                //计数完成do something
               
            }

        }
    };
}
