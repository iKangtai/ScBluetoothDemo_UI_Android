package com.example.bledemo.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;


import com.example.bledemo.R;
import com.ikangtai.bluetoothsdk.util.LogUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class ThermometerHelper extends FrameLayout {
    protected CompositeDisposable disposables = new CompositeDisposable();
    /**
     * 帮助
     */
    protected Disposable helpDisposable;
    /**
     * 上传
     */
    protected Disposable uploadDisposable;
    /**
     * 当前状态
     */
    private TextView thermomterState;

    /**
     * 提示用户该如何操作体温计
     */
    private TextView thermomterHintTitle;
    private TextSwitcher thermomterHint;

    /**
     * 加载等待框
     */
    private ProgressBar thermomterProgressBar;
    /**
     * 设备图标
     */
    private ImageView thermomterIcon;

    /**
     * 上传中...
     */
    private TextView thermomterUploading;

    private int pos;

    private String[] hints;


    public ThermometerHelper(Context context) {
        super(context);
        init();
    }

    public ThermometerHelper(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ThermometerHelper(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        hints = new String[]{
                getResources().getString(R.string.connect_hint_2),
                getResources().getString(R.string.connect_hint_3),
                getResources().getString(R.string.connect_hint_4)
        };
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        thermomterState = findViewById(R.id.thermomterState);
        thermomterHintTitle = findViewById(R.id.thermomterHintTitle);
        thermomterHint = findViewById(R.id.thermomterHint);
        thermomterProgressBar = findViewById(R.id.thermomterProgressBar);
        thermomterIcon = findViewById(R.id.thermomterIcon);
        thermomterUploading = findViewById(R.id.thermomterUploading);

        if (thermomterHintTitle != null) {
            thermomterHintTitle.setText(getResources().getString(R.string.connect_hint_1));
        }

        if (thermomterHint != null) {
            thermomterHint.setFactory(new ViewSwitcher.ViewFactory() {
                @Override
                public View makeView() {
                    final TextView tv = new TextView(getContext());
                    tv.setTextSize(12);
                    tv.setTextColor(Color.parseColor("#FFB2B2B2"));
                    tv.setGravity(Gravity.CENTER);
                    return tv;
                }
            });
        }


    }

    /**
     * 设备连接中
     */
    public void connect() {
        LogUtils.i("体温计设备连接中");
        if (thermomterProgressBar != null) {
            if (thermomterProgressBar != null) {
                Drawable drawable = getResources().getDrawable(R.drawable.ble_loading);
                Rect bounds = thermomterProgressBar.getIndeterminateDrawable().getBounds();
                thermomterProgressBar.setIndeterminateDrawable(drawable);
                thermomterProgressBar.getIndeterminateDrawable().setBounds(bounds);
            }
        }

        if (thermomterHint != null) {
            thermomterHint.setVisibility(View.GONE);
        }
        if (thermomterHintTitle != null) {
            thermomterHintTitle.setVisibility(GONE);
        }

        if (thermomterIcon != null) {
            thermomterIcon.setVisibility(GONE);
        }
        if (thermomterState != null) {
            thermomterState.setVisibility(View.VISIBLE);
            thermomterState.setText(getResources().getString(R.string.connect_device));
        }
        if (thermomterUploading != null) {
            thermomterUploading.setVisibility(GONE);
        }

    }

    /**
     * 设备连接完成
     */
    public void connectComplete() {
        LogUtils.i("体温计设备连接完成");
        if (thermomterProgressBar != null) {
            if (thermomterProgressBar != null) {
                Drawable drawable = getResources().getDrawable(R.drawable.ble_loading);
                Rect bounds = thermomterProgressBar.getIndeterminateDrawable().getBounds();
                thermomterProgressBar.setIndeterminateDrawable(drawable);
                thermomterProgressBar.getIndeterminateDrawable().setBounds(bounds);
            }
        }

        if (thermomterHint != null) {
            thermomterHint.setVisibility(View.GONE);
        }
        if (thermomterHintTitle != null) {
            thermomterHintTitle.setVisibility(GONE);
        }

        if (thermomterIcon != null) {
            thermomterIcon.setVisibility(GONE);
        }
        if (thermomterState != null) {
            thermomterState.setVisibility(View.VISIBLE);
            thermomterState.setText(getResources().getString(R.string.connect_device_success));
        }
        if (thermomterUploading != null) {
            thermomterUploading.setVisibility(GONE);
        }

    }

    /**
     * 设备连接成功
     */
    public void connected(final boolean sendDataSuccess) {
        disposables.add(Observable.just(0L).doOnNext(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                LogUtils.i("显示设备图像:" + sendDataSuccess);

                if (thermomterHint != null) {
                    thermomterHint.setVisibility(View.GONE);
                }
                if (thermomterHintTitle != null) {
                    thermomterHintTitle.setVisibility(GONE);
                }

                if (thermomterProgressBar != null) {
                    if (thermomterProgressBar != null) {
                        Drawable drawable = getResources().getDrawable(R.drawable.ble_loading_complete);
                        Rect bounds = thermomterProgressBar.getIndeterminateDrawable().getBounds();
                        thermomterProgressBar.setIndeterminateDrawable(drawable);
                        thermomterProgressBar.getIndeterminateDrawable().setBounds(bounds);
                    }
                }

                if (thermomterIcon != null) {
                    thermomterIcon.setVisibility(VISIBLE);
                    thermomterIcon.setImageResource(R.drawable.a31);
                }

                if (thermomterState != null) {
                    thermomterState.setVisibility(View.GONE);
                }

                if (thermomterUploading != null) {
                    thermomterUploading.setVisibility(GONE);
                }

            }
        }).delay(1500, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {

                        if (!sendDataSuccess) {

                            if (thermomterIcon != null) {
                                thermomterIcon.setVisibility(GONE);
                            }

                            if (thermomterUploading != null) {
                                thermomterUploading.setVisibility(View.VISIBLE);
                                if (uploadDisposable != null) {
                                    disposables.remove(uploadDisposable);
                                }

                                uploadDisposable = Observable.interval(0, 1500, TimeUnit.MILLISECONDS)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Consumer<Long>() {
                                            @Override
                                            public void accept(Long aLong) throws Exception {
                                                int len = (int) (aLong % 3) + 1;
                                                StringBuilder stringBuilder = new StringBuilder();
                                                stringBuilder.append(getResources().getString(R.string.wait_thermometer_data_uploading));
                                                for (int i = 0; i < len; i++) {
                                                    stringBuilder.append(".");
                                                }
                                                thermomterUploading.setText(stringBuilder.toString());
                                            }
                                        });

                                disposables.add(uploadDisposable);
                            }
                        }
                    }
                }));

    }

    /**
     * 发送数据成功 已通知用户
     */
    public void sendDataSuccess() {
        LogUtils.i("体温计发送数据成功");
        disposables.clear();
        if (thermomterProgressBar != null) {
            if (thermomterProgressBar != null) {
                Drawable drawable = getResources().getDrawable(R.drawable.ble_loading_complete);
                Rect bounds = thermomterProgressBar.getIndeterminateDrawable().getBounds();
                thermomterProgressBar.setIndeterminateDrawable(drawable);
                thermomterProgressBar.getIndeterminateDrawable().setBounds(bounds);
            }
        }

        if (thermomterHint != null) {
            thermomterHint.setVisibility(View.GONE);
        }
        if (thermomterHintTitle != null) {
            thermomterHintTitle.setVisibility(GONE);
        }
        if (thermomterIcon != null) {
            thermomterIcon.setVisibility(View.VISIBLE);
            thermomterIcon.setImageResource(R.drawable.a31);
        }
        if (thermomterState != null) {
            thermomterState.setVisibility(GONE);
        }
        if (thermomterUploading != null) {
            thermomterUploading.setVisibility(View.GONE);
        }


    }

    /**
     * 连接失败
     */
    public void connectFail() {
        LogUtils.i("体温计连接失败");
        disposables.clear();
        if (thermomterProgressBar != null) {
            if (thermomterProgressBar != null) {
                Drawable drawable = getResources().getDrawable(R.drawable.ble_loading_wait);
                Rect bounds = thermomterProgressBar.getIndeterminateDrawable().getBounds();
                thermomterProgressBar.setIndeterminateDrawable(drawable);
                thermomterProgressBar.getIndeterminateDrawable().setBounds(bounds);
            }
        }

        if (thermomterHint != null) {
            thermomterHint.setVisibility(View.GONE);
        }
        if (thermomterHintTitle != null) {
            thermomterHintTitle.setVisibility(GONE);
        }

        if (thermomterIcon != null) {
            thermomterIcon.setVisibility(GONE);
        }
        if (thermomterState != null) {
            thermomterState.setVisibility(View.VISIBLE);
            thermomterState.setText(getResources().getString(R.string.unconnect_device));
        }
        if (thermomterUploading != null) {
            thermomterUploading.setVisibility(GONE);
        }


    }

    /**
     * 显示帮助提示
     */
    public void connectHelp() {
        if (thermomterProgressBar != null) {
            if (thermomterProgressBar != null) {
                Drawable drawable = getResources().getDrawable(R.drawable.ble_loading);
                Rect bounds = thermomterProgressBar.getIndeterminateDrawable().getBounds();
                thermomterProgressBar.setIndeterminateDrawable(drawable);
                thermomterProgressBar.getIndeterminateDrawable().setBounds(bounds);
            }
        }

        if (thermomterHint != null) {
            thermomterHint.setVisibility(View.VISIBLE);
            if (helpDisposable != null) {
                disposables.remove(helpDisposable);
            }
            helpDisposable = Observable.interval(0, 3, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            if (thermomterHint != null) {
                                if (pos >= hints.length) {
                                    pos = 0;
                                }
                                thermomterHint.setText(hints[pos]);
                                pos++;
                            }
                        }
                    });
            disposables.add(helpDisposable);
        }
        if (thermomterHintTitle != null) {
            thermomterHintTitle.setVisibility(VISIBLE);
        }

        if (thermomterIcon != null) {
            thermomterIcon.setVisibility(GONE);
        }
        if (thermomterState != null) {
            thermomterState.setVisibility(View.GONE);
        }
        if (thermomterUploading != null) {
            thermomterUploading.setVisibility(GONE);
        }


    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        disposables.clear();
    }


}
