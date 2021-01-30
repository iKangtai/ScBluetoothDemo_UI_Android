package com.ikangtai.bluetoothui.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import com.ikangtai.bluetoothui.AppInfo;
import com.ikangtai.bluetoothui.Keys;
import com.ikangtai.bluetoothui.R;
import com.ikangtai.bluetoothui.activity.DeviceConnectActivity;
import com.ikangtai.bluetoothui.info.HardwareInfo;
import com.ikangtai.bluetoothui.info.TemperatureInfo;
import com.ikangtai.bluetoothui.model.HardwareModel;
import com.ikangtai.bluetoothui.util.DateUtil;
import com.ikangtai.bluetoothui.view.InputTemperatureLayout;
import com.ikangtai.bluetoothui.view.TemperatureKeyborad;
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
    private TextView errorHint;
    private ImageView closeBtn;
    private Button operator;
    private long measureTime;
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
        errorHint = view.findViewById(R.id.errorHint);
        keyBoardLayout = view.findViewById(R.id.keyBoardLayout);
        inputTemperatureLayout = view.findViewById(R.id.inputTemperatureLayout);
        temperatureDateContent = view.findViewById(R.id.temperatureDateContent);
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
                    final String content = operator.getText().toString();
                    if (context.getResources().getString(R.string.save).equals(content)) {
                        double temperatureValue = inputTemperatureLayout.getValue();
                        //用户保存当天体温
                        String temperature = String.valueOf(temperatureValue);
                        if (!TextUtils.isEmpty(temperature)) {
                            if (AppInfo.getInstance().isTempUnitC()) {
                                if (temperatureValue < Keys.C_MIN || temperatureValue > Keys.C_MAX) {
                                    if (errorHint != null) {
                                        String errorContent = String.format(context.getResources().getString(R.string.add_valid_temperature_hint),
                                                temperature, Keys.kTempUnitC, Keys.C_MIN + "", Keys.C_MAX + "", Keys.kTempUnitC);
                                        errorHint.setText(errorContent);
                                        if (inputTemperatureLayout != null) {
                                            inputTemperatureLayout.setError();
                                        }
                                    }

                                    return;
                                }
                            } else {
                                if (temperatureValue < Keys.F_MIN || temperatureValue > Keys.F_MAX) {
                                    if (errorHint != null) {
                                        String errorContent = String.format(context.getResources().getString(R.string.add_valid_temperature_hint),
                                                temperature, Keys.kTempUnitF, Keys.F_MIN + "", Keys.F_MAX + "", Keys.kTempUnitF);
                                        errorHint.setText(errorContent);
                                        if (inputTemperatureLayout != null) {
                                            inputTemperatureLayout.setError();
                                        }
                                    }

                                    return;
                                }
                            }
                            if (event != null) {
                                TemperatureInfo temperatureInfo = new TemperatureInfo();
                                temperatureInfo.setMeasureTime(measureTime);
                                temperatureInfo.setTemperature(temperatureValue);
                                event.onSave(temperatureInfo);
                            }
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                        }
                    } else {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        LogUtils.i("添加温度");
                        //判断用户是否已经绑定
                        List<HardwareInfo> hardwareInfoList=HardwareModel.hardwareList(context);
                        if (hardwareInfoList.isEmpty()) {
                            //弹框提示用户购买体温计
                            new BuyAndBindThermometerDialog(context).builder().show();
                        } else {
                            //跳转到数据上传界面
                            context.startActivity(new Intent(context, DeviceConnectActivity.class));
                        }
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
                        measureTime = DateUtil.getYMDHMDate(DateUtil.getDateFormatYMDHM(date.getTime() / 1000));
                        temperatureDateContent.setText(DateUtil.getDateFormatYMDHM(measureTime));

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


    public TemperatureAddDialog withTitle(String title) {

        if (titleContent != null) {
            titleContent.setText(title);
        }

        return this;
    }

    private TemperatureAddDialog withMeasureTime(long measureTime) {
        this.measureTime = measureTime;
        if (measureTime > 0) {
            temperatureDateContent.setText(DateUtil.getDateFormatYMDHM(measureTime));
        } else {
            this.measureTime = DateUtil.getYMDHMDate(DateUtil.getDateFormatYMDHM(System.currentTimeMillis() / 1000));
            temperatureDateContent.setText(DateUtil.getDateFormatYMDHM(this.measureTime));
        }
        return this;
    }

    public TemperatureAddDialog show() {

        if (dialog != null) {
            dialog.show();
        }

        return this;
    }

    private IEvent event;

    public TemperatureAddDialog initEvent(IEvent event) {
        this.event = event;
        return this;
    }

    public interface IEvent {
        void onSave(TemperatureInfo temperatureInfo);
    }

}
