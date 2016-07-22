package zgan.ohos.ConstomControls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsButton;
import com.mikepenz.iconics.view.IconicsImageView;

import zgan.ohos.R;

/**
 * Created by yajunsun on 16-4-5.
 */
public class MySelectCount extends LinearLayout implements View.OnClickListener {

    IonChanged ionChanged;
    IconicsImageView ivadd, ivremove;
    EditText edinput;
    int mCount = 0;
    int mRestrict = 200;
    int size = 20;
    int minValue = 0;
    boolean caninput = false;

    public MySelectCount(Context context) {
        this(context, null);
    }

    public MySelectCount(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MySelectCount(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MySelectCount);
        size = ta.getInt(R.styleable.MySelectCount_size, 20);
        minValue = ta.getInt(R.styleable.MySelectCount_minValue, 0);
        mCount = minValue;
        caninput = ta.getBoolean(R.styleable.MySelectCount_canInput, false);
        //int color=ta.getColor(R.styleable.MySelectCount_icon_color, Color.parseColor("#000"));
        ta.recycle();

        View v = LayoutInflater.from(context).inflate(R.layout.control_selectcount, this, true);
        ivadd = (IconicsImageView) v.findViewById(R.id.iv_add);
        ivadd.setImageDrawable(new IconicsDrawable(context, GoogleMaterial.Icon.gmd_add).sizePx(size));
        ivremove = (IconicsImageView) v.findViewById(R.id.iv_remove);
        ivremove.setImageDrawable(new IconicsDrawable(context, GoogleMaterial.Icon.gmd_remove).sizePx(size));
        edinput = (EditText) v.findViewById(R.id.ed_input);
        edinput.setEnabled(caninput);

        ivremove.setEnabled(false);
        ivremove.setColor(getResources().getColor(R.color.myaccount_icon));

        setSize(size);

        ivadd.setOnClickListener(this);
        ivremove.setOnClickListener(this);
        edinput.setText(String.valueOf(minValue));
        edinput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (start == 0 && Integer.parseInt(s.toString())<minValue) {
//                    edinput.setText(String.valueOf(minValue));
//                    ivremove.setEnabled(false);
//                    mCount=minValue;
//                }
//                else if (start == 0 && s.equals(String.valueOf(minValue)))
//                    ivremove.setEnabled(false);
//                else
//                    ivremove.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals("") || Integer.parseInt(s.toString()) < minValue) {
                    ivremove.setEnabled(false);
                    edinput.setText(String.valueOf(minValue));
                    mCount = minValue;
                } else {
                    mCount = Integer.parseInt(s.toString());
//                    if (ionChanged != null)
//                        ionChanged.onAddition(mCount);
                    ivremove.setEnabled(true);
                }
            }
        });
    }

    public void setSize(int s) {
        size = s;
        LayoutParams lp1 = new LayoutParams((int) (size * 1.5), (int) (size * 1.5));
        LayoutParams lp2 = new LayoutParams(size * 2, (int) (size * 1.5));
        ivadd.setLayoutParams(lp1);
        ivremove.setLayoutParams(lp1);
        edinput.setLayoutParams(lp2);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_remove:
                if (ivremove.isEnabled()) {
                    mCount--;
                    edinput.setText(String.valueOf(mCount));
                    if (ionChanged != null)
                        ionChanged.onReduction(mCount);
                }
                if (mCount < mRestrict - 1) {
                    ivadd.setEnabled(true);
                    ivadd.setColor(getResources().getColor(R.color.solid_black));
                }
                if (Integer.parseInt(edinput.getText().toString().trim()) <= minValue) {
                    ivremove.setEnabled(false);
                    ivremove.setColor(getResources().getColor(R.color.myaccount_icon));
                } else if (Integer.parseInt(edinput.getText().toString().trim()) > minValue) {
                    ivremove.setEnabled(true);
                    ivremove.setColor(getResources().getColor(R.color.solid_black));
                }
                break;
            case R.id.iv_add:

                if (mCount == mRestrict) {
                    ivadd.setEnabled(false);
                    ivadd.setColor(getResources().getColor(R.color.myaccount_icon));
                }
                if (ivadd.isEnabled()) {
                    mCount++;
                    edinput.setText(String.valueOf(mCount));
                    ivremove.setEnabled(true);
                    ivremove.setColor(getResources().getColor(R.color.solid_black));
                    if (ionChanged != null)
                        ionChanged.onAddition(mCount);
                }
                break;
        }
    }

    public int getCount() {
        return mCount;
    }

    public void setCount(int count) {
        mCount = count;
        edinput.setText(String.valueOf(mCount));
        if (mCount > minValue) {
            ivremove.setEnabled(true);
            ivremove.setColor(getResources().getColor(R.color.solid_black));
        }
    }

    public int getMinValue() {
        return minValue;
    }

    public int getRestrict() {
        return mRestrict;
    }

    public void restore() {
        mCount = minValue;
        edinput.setText(String.valueOf(minValue));
        ivremove.setEnabled(false);
        ivremove.setColor(getResources().getColor(R.color.myaccount_icon));
    }

    public void increace() {
        mCount = mCount + 1;
        edinput.setText(String.valueOf(mCount));
        ivremove.setEnabled(true);
        ivremove.setColor(getResources().getColor(R.color.solid_black));
    }

    public void reduce() {
        mCount = mCount - 1;
        edinput.setText(String.valueOf(mCount));
        if(mCount<=minValue)
        {
            ivremove.setEnabled(false);
            ivremove.setColor(getResources().getColor(R.color.myaccount_icon));
        }
    }

    public void setOnchangeListener(IonChanged onchangeListener) {
        this.ionChanged = onchangeListener;
    }

    public void setMaxValue(int maxValue) {
        this.mRestrict = maxValue;
    }

    public interface IonChanged {
        void onAddition(int count);

        void onReduction(int count);
    }
}
