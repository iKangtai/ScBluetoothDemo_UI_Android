package com.example.bledemo.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.example.bledemo.AppInfo;
import com.example.bledemo.Keys;
import com.example.bledemo.R;
import com.example.bledemo.info.HardwareInfo;
import com.example.bledemo.model.HardwareModel;
import com.example.bledemo.view.InputTemperatureLayout;
import com.example.bledemo.view.TemperatureKeyborad;
import com.ikangtai.bluetoothsdk.util.LogUtils;
import com.ikangtai.bluetoothsdk.util.ToastUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 体温添加对话框
 */
public class TemperatureAddDialog extends BaseShecareDialog {
    private Context context;
    private Display display;

    private TemperatureKeyborad keyBoardLayout;
    private InputTemperatureLayout inputTemperatureLayout;
    private TextView titleContent;
    private TextView addMoreData;
    private TextView errorHint;
    private ImageView closeBtn;
    private Button operator;
    private long measureTime;
    private String temperatureId;
    private TextView temperatureDateContent;


    public TemperatureAddDialog(Context context) {
        this.context = context;
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();
    }

    public TemperatureAddDialog builder() {
        final View view = LayoutInflater.from(context).inflate(
                R.layout.layout_add_temperature, null);
        // 设置Dialog最小宽度为屏幕宽度
        view.setMinimumWidth(display.getWidth());
        operator = view.findViewById(R.id.operator);
        closeBtn = view.findViewById(R.id.closeBtn);
        titleContent = view.findViewById(R.id.titleContent);
        addMoreData = view.findViewById(R.id.addMoreData);
        errorHint = view.findViewById(R.id.errorHint);
        keyBoardLayout = view.findViewById(R.id.keyBoardLayout);
        inputTemperatureLayout = view.findViewById(R.id.inputTemperatureLayout);
        temperatureDateContent = view.findViewById(R.id.temperatureDateContent);
        if (addMoreData != null) {
            addMoreData.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
            addMoreData.getPaint().setAntiAlias(true);
            addMoreData.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    //RouteUtils.go(RouteUtils.ROUTE_APP_BBT, Keys.KEY_DATE_RECORD, DateUtil.getSimpleDate());
                }
            });
        }

        closeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        operator.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (operator instanceof Button) {
                    String content = operator.getText().toString();
                    if (context.getResources().getString(R.string.save).equals(content)) {
                        //用户保存当天体温
                        String temperature = String.valueOf(inputTemperatureLayout.getValue());
                        if (!TextUtils.isEmpty(temperature)) {
                            if (AppInfo.getInstance().isTempUnitC()) {
                                if (Double.parseDouble(temperature) < Keys.C_MIN || Double.parseDouble(temperature) > Keys.C_MAX) {
                                    if (errorHint != null) {
                                        String errorContent = String.format(context.getResources().getString(R.string.add_valid_temperature_hint),
                                                temperature, "℃", Keys.C_MIN + "", Keys.C_MAX + "", "℃");
                                        errorHint.setText(errorContent);
                                        if (inputTemperatureLayout != null) {
                                            inputTemperatureLayout.setError();
                                        }
                                    }

                                    return;
                                }
                            } else {
                                if (Double.parseDouble(temperature) < Keys.F_MIN || Double.parseDouble(temperature) > Keys.F_MAX) {
                                    if (errorHint != null) {
                                        String errorContent = String.format(context.getResources().getString(R.string.add_valid_temperature_hint),
                                                temperature, "℉", Keys.F_MIN + "", Keys.F_MAX + "", "℉");
                                        errorHint.setText(errorContent);
                                        if (inputTemperatureLayout != null) {
                                            inputTemperatureLayout.setError();
                                        }
                                    }

                                    return;
                                }
                            }
                            if (TextUtils.isEmpty(temperatureId)) {
                                if (measureTime > 0) {
                                    //新增体温从体温列表添加
                                    //EventBus.getDefault().post(new TemperatureEventBus(temperature, temperatureId, measureTime));
                                } else {
                                    //新增体温从首页添加
                                    //TemperatureListActivity activity = new TemperatureListActivity();
                                    //activity.addOrEditTemperature(new TemperatureEventBus(temperature,temperatureId, System.currentTimeMillis() / 1000));
                                }
                            } else {
                                //编辑
                                //EventBus.getDefault().post(new TemperatureEventBus(temperature, temperatureId, measureTime));
                            }
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                        }
                    } else {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        LogUtils.i("添加温度，用户已绑定设备");
                        //判断用户是否已经绑定
                        HardwareModel.obtainThermometerObservable(context).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<List<HardwareInfo>>() {
                                    @Override
                                    public void accept(List<HardwareInfo> hardwareInfoList) throws Exception {
                                        if (hardwareInfoList.isEmpty()) {
                                            //弹框提示用户购买体温计
                                            new BuyAndBindThermometerDialog(context).builder().show();
                                        } else {
                                            //跳转到数据上传界面
                                            //RouteUtils.go(RouteUtils.ROUTE_APP_CONNECT_THERMOMETER);
                                        }
                                    }
                                }, new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        LogUtils.i("绑定设备列表" + throwable.getMessage());
                                    }
                                });
                    }
                }


            }
        });

        keyBoardLayout.setEvent(new TemperatureKeyborad.IEvent() {
            @Override
            public void delete() {
                if (inputTemperatureLayout != null) {
                    inputTemperatureLayout.delete(new InputTemperatureLayout.IEvent() {
                        @Override
                        public void clear(boolean result) {
                            if (operator != null) {
                                if (result) {
                                    operator.setText(context.getResources().getString(R.string.auto_upload_temperature));
                                    if (errorHint != null) {
                                        errorHint.setText("");
                                    }
                                } else {
                                    operator.setText(context.getResources().getString(R.string.save));
                                }
                            }
                        }
                    });
                }

            }

            @Override
            public void input(int value) {
                if (inputTemperatureLayout != null) {
                    inputTemperatureLayout.input(value);
                }
                if (operator != null) {
                    operator.setText(context.getResources().getString(R.string.save));
                }
            }
        });
        temperatureDateContent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                if (measureTime > 0) {
                    calendar.setTime(new Date(measureTime * 1000));
                }
                new TimePickerBuilder(context, new OnTimeSelectListener() {
                    @Override
                    public void onTimeSelect(Date date, View v) {
                        if (date.getTime() > System.currentTimeMillis()) {
                            ToastUtils.show(context, context.getString(R.string.not_edit_future_time));
                            return;
                        }
                        measureTime = date.getTime() / 1000;
                        temperatureDateContent.setText(getDateFormatYMDHM(measureTime));

                    }
                })
                        .setDate(calendar)
                        .setTitleText(context.getString(R.string.add_temperature_select_time).substring(0, 4))
                        .setType(new boolean[]{true, true, true, true, true, false})
                        .setCancelColor(context.getResources().getColor(R.color.app_primary_dark_color))
                        .setSubmitText(context.getString(R.string.save))
                        .setSubmitColor(context.getResources().getColor(R.color.app_primary_dark_color))
                        .isDialog(true)
                        .build()
                        .show();
            }
        });


        // 定义Dialog布局和参数
        dialog = new Dialog(context, R.style.ActionSheetDialogStyle);
        dialog.setContentView(view);
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setGravity(Gravity.LEFT | Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.x = 0;
        lp.y = 0;
        dialogWindow.setAttributes(lp);
        withMeasureTime(measureTime);
        return this;
    }

    /**
     * 获取月日 时分
     *
     * @param seconds
     * @return
     */
    public static String getDateFormatYMDHM(long seconds) {
        Date date = new Date(seconds * 1000);
        SimpleDateFormat simpleDateFormat;
        if (TextUtils.equals(Locale.getDefault().getLanguage(), "zh")) {
            simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        } else {
            simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        }
        return simpleDateFormat.format(date);
    }

    public TemperatureAddDialog withTitle(String title) {

        if (titleContent != null) {
            titleContent.setText(title);
        }

        return this;
    }

    public TemperatureAddDialog withMeasureTime(long measureTime) {
        this.measureTime = measureTime;
        if (measureTime > 0) {
            temperatureDateContent.setText(getDateFormatYMDHM(measureTime));
        } else {
            temperatureDateContent.setText(getDateFormatYMDHM(System.currentTimeMillis() / 1000));
        }
        return this;
    }


    public TemperatureAddDialog withAddMoreTemperature(boolean isVisible) {
        if (addMoreData != null) {
            addMoreData.setVisibility(!isVisible ? View.GONE : View.VISIBLE);
        }

        return this;
    }

    /**
     * 含有体温 代表修改体温数据
     *
     * @param temperatureId
     * @return
     */
    public TemperatureAddDialog withTemperature(String temperatureId, String temperature) {
        this.temperatureId = temperatureId;
        if (!TextUtils.isEmpty(temperature)) {
            temperature = temperature.replace(".", "");
            if (inputTemperatureLayout != null) {
                inputTemperatureLayout.inputTemperature(temperature, true);
            }
            if (operator != null) {
                operator.setText(context.getResources().getString(R.string.save));
            }
        }
        return this;
    }

    public TemperatureAddDialog show() {

        if (dialog != null) {
            dialog.show();
        }

        return this;
    }

}
