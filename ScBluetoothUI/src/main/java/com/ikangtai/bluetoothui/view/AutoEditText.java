package com.ikangtai.bluetoothui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;


import com.ikangtai.bluetoothui.AppInfo;
import com.ikangtai.bluetoothsdk.util.PxDxUtil;

import androidx.appcompat.widget.AppCompatEditText;


/**
 * Created by IkangTai on 2017/7/1.
 */

public class AutoEditText extends AppCompatEditText implements View.OnClickListener {

    private float mNumHeight;
    private int mLength = 4;
    private int mPointWidth = 0;
    private int mNumWidth = 0;
    private int mWidth = 0;
    private Paint paint;
    private Paint unitPaint;
    private Paint linePaint;
    private Paint pointPaint;
    private int spacWidth;
    private float height;
    private float descent;
    private float ascent;

    public String[] chars = new String[]{"0", "0", "0", "0", "0"};

    public AutoEditText(Context context) {
        this(context, null);
    }

    public AutoEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(getTextSize());
        paint.setColor(getCurrentTextColor());

        pointPaint = new Paint();
        pointPaint.setAntiAlias(true);
        pointPaint.setTextSize(getTextSize());
        pointPaint.setColor(getCurrentTextColor());

        unitPaint = new Paint();
        unitPaint.setAntiAlias(true);
        unitPaint.setTextSize(getTextSize() / 2);
        unitPaint.setColor(getCurrentTextColor());

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(getCurrentTextColor());
        linePaint.setStrokeWidth(PxDxUtil.dip2px(context, 1));
        linePaint.setStyle(Paint.Style.FILL);


        String text = "9";
        String point = ".";

        mNumWidth = (int) paint.measureText(text, 0, text.length());
        mPointWidth = (int) paint.measureText(point, 0, point.length());
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, 1, rect);
        mNumHeight = rect.height();

        Paint.FontMetrics metrics = paint.getFontMetrics();
        descent = metrics.descent;
        ascent = metrics.ascent;
        height = Math.abs(ascent);

        spacWidth = mNumWidth / 2;

        setOnClickListener(this);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        mWidth = (mNumWidth + spacWidth) * mLength + 2 * spacWidth;
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(mWidth, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (height + descent), MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (!TextUtils.isEmpty(text)) {
            mLength = text.toString().startsWith("1") && !AppInfo.getInstance().isTempUnitC() ? 5 : 4;
            setFilters(new InputFilter[]{new InputFilter.LengthFilter(mLength)});
            invalidate();
        }
    }

    public void setmLength(String text) {
        if (text != null) {
            this.mLength = text.length();
            setFilters(new InputFilter[]{new InputFilter.LengthFilter(mLength)});
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        float h = height + descent;
        if (mLength == 4) {
            float[] pts = {0, h, mNumWidth, h,
                    mNumWidth + spacWidth, h, mNumWidth * 2 + spacWidth, h,
                    mNumWidth * 2 + spacWidth * 5, h, mNumWidth * 3 + spacWidth * 5, h,
                    mNumWidth * 3 + spacWidth * 6, h, mNumWidth * 4 + spacWidth * 6, h};
            canvas.drawLines(pts, linePaint);

        } else {
            float[] pts = {0, h, mNumWidth, h,
                    mNumWidth + spacWidth, h, mNumWidth * 2 + spacWidth, h,
                    mNumWidth * 2 + spacWidth * 2, h, mNumWidth * 3 + spacWidth * 2, h,
                    mNumWidth * 3 + spacWidth * 6, h, mNumWidth * 4 + spacWidth * 6, h,
                    mNumWidth * 4 + spacWidth * 7, h, mNumWidth * 5 + spacWidth * 7, h};
            canvas.drawLines(pts, linePaint);
        }

        if (mLength == 4) {
            canvas.drawRect(mNumWidth * 2 + spacWidth * 2.75f, h - spacWidth , mNumWidth * 2 + spacWidth * 3.25f, h - spacWidth*0.5f, pointPaint);
        } else {
            canvas.drawRect(mNumWidth * 3 + spacWidth * 3.75f, h - spacWidth, mNumWidth * 3 + spacWidth * 4.25f, h - spacWidth*0.5f, pointPaint);
        }

        if (mLength == 4) {
            char[] chars = getText().toString().toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (i < 2) {
                    canvas.drawText(chars, i, 1, (mNumWidth + spacWidth) * i, h - descent / 2, paint);
                } else {
                    canvas.drawText(chars, i, 1, (mNumWidth + spacWidth) * i + 3 * spacWidth, h - descent / 2, paint);
                }
            }
        } else {
            char[] chars = getText().toString().toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (i < 3) {
                    canvas.drawText(chars, i, 1, (mNumWidth + spacWidth) * i, h - descent / 2, paint);
                } else {
                    canvas.drawText(chars, i, 1, (mNumWidth + spacWidth) * i + 3 * spacWidth, h - descent / 2, paint);
                }
            }
        }

        setSelection(getText().toString().length());
    }


    public int getAutoAddZero() {
        int[] ints = null;
        int res = 0;
        char[] chars = getText().toString().toCharArray();
        if (chars[0] == '1' && !AppInfo.getInstance().isTempUnitC()) {
            ints = new int[]{0, 0, 0, 0, 0};
        } else {
            ints = new int[]{0, 0, 0, 0};
        }
        for (int i = 0; i < chars.length; i++) {
            if (i > ints.length - 1) {
                break;
            }
            ints[i] = Integer.valueOf(String.valueOf(chars[i]));
        }
        for (int i = 0; i < ints.length; i++) {
            res = (int) (ints[i] * Math.pow(10, ints.length - 1 - i) + res);
        }
        return res;
    }

    public void setHintLine(boolean isHint) {
        if (isHint) {
            linePaint.setColor(Color.TRANSPARENT);
        } else {
            linePaint.setColor(getCurrentTextColor());
        }
        invalidate();
    }


    @Override
    public void onClick(View v) {
        setSelection(getText().length());
        setHintLine(false);
    }

}
