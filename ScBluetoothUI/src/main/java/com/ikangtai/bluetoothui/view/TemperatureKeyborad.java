package com.ikangtai.bluetoothui.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.ikangtai.bluetoothui.R;


public class TemperatureKeyborad extends LinearLayout implements View.OnClickListener {

    private IEvent event;

    public TemperatureKeyborad(Context context) {
        super(context);
    }

    public TemperatureKeyborad(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TemperatureKeyborad(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        findViewById(R.id.number1Btn).setOnClickListener(this);
        findViewById(R.id.number2Btn).setOnClickListener(this);
        findViewById(R.id.number3Btn).setOnClickListener(this);
        findViewById(R.id.number4Btn).setOnClickListener(this);
        findViewById(R.id.number5Btn).setOnClickListener(this);
        findViewById(R.id.number6Btn).setOnClickListener(this);
        findViewById(R.id.number7Btn).setOnClickListener(this);
        findViewById(R.id.number8Btn).setOnClickListener(this);
        findViewById(R.id.number9Btn).setOnClickListener(this);
        findViewById(R.id.number0Btn).setOnClickListener(this);
        findViewById(R.id.numberDelBtn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.numberDelBtn) {
            if (event != null) {
                event.delete();
            }
            return;
        }


        if (v.getId() == R.id.number1Btn) {
            if (event != null) {
                event.input(1);
            }
        }
        if (v.getId() == R.id.number2Btn) {
            if (event != null) {
                event.input(2);
            }
        }
        if (v.getId() == R.id.number3Btn) {
            if (event != null) {
                event.input(3);
            }
        }
        if (v.getId() == R.id.number4Btn) {
            if (event != null) {
                event.input(4);
            }
        }
        if (v.getId() == R.id.number5Btn) {
            if (event != null) {
                event.input(5);
            }
        }
        if (v.getId() == R.id.number6Btn) {
            if (event != null) {
                event.input(6);
            }
        }
        if (v.getId() == R.id.number7Btn) {
            if (event != null) {
                event.input(7);
            }
        }
        if (v.getId() == R.id.number8Btn) {
            if (event != null) {
                event.input(8);
            }
        }
        if (v.getId() == R.id.number9Btn) {
            if (event != null) {
                event.input(9);
            }
        }
        if (v.getId() == R.id.number0Btn) {
            if (event != null) {
                event.input(0);
            }
        }

    }


    public void setEvent(IEvent event) {
        this.event = event;
    }

    public interface IEvent {
        void delete();

        void input(int value);
    }
}
