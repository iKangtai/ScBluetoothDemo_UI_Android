package com.ikangtai.bluetoothui.view;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.ikangtai.bluetoothui.AppInfo;
import com.ikangtai.bluetoothui.R;

import androidx.annotation.Nullable;

public class InputTemperatureLayout extends RelativeLayout {
    private TextView tempUnitTv;
    private AutoEditText autoEditText;
    private String type_flag_0 = " ℃";
    private String type_flag_1 = " ℉";
    private TextView temperatureContent;

    private StringBuilder inputBuilder = new StringBuilder();


    public InputTemperatureLayout(Context context) {
        super(context);
    }

    public InputTemperatureLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public InputTemperatureLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        temperatureContent = findViewById(R.id.temperatureContent);
        tempUnitTv = findViewById(R.id.temp_unit_tv);
        autoEditText = findViewById(R.id.et_temp);
        if (temperatureContent != null) {
            String divided = "-";
            String format = String.format("%s %s . %s %s", divided, divided, divided, divided);
            if (AppInfo.getInstance().isTempUnitC()) {
                format = format + type_flag_0;
            } else {
                format = format + type_flag_1;
            }
            temperatureContent.setText(format);
            temperatureContent.setTextColor(Color.parseColor("#FF8E8E93"));
        }
        if (AppInfo.getInstance().isTempUnitC()) {
            tempUnitTv.setText(type_flag_0);
        } else {
            tempUnitTv.setText(type_flag_1);
        }
    }

    /**
     * 新输入/编辑体温
     *
     * @param result
     * @param update
     */
    public void inputTemperature(String result, boolean update) {
        autoEditText.setText(result);
        if (temperatureContent != null) {
            String divided = "-";
            if (update) {
                divided = "0";
            }
            if (TextUtils.isEmpty(result)) {
                temperatureContent.setTextColor(Color.parseColor("#FF8E8E93"));
            } else {
                temperatureContent.setTextColor(Color.parseColor("#FF444444"));
            }
            int len = result.length();
            if (AppInfo.getInstance().isTempUnitC()) {
                String[] arrays = new String[]{divided, divided, divided, divided};
                for (int i = 0; i < len; i++) {
                    String each = String.valueOf(result.charAt(i));
                    arrays[i] = each;
                }
                String format = String.format("%s %s . %s %s", arrays[0], arrays[1], arrays[2], arrays[3]);
                temperatureContent.setText(format + type_flag_0);
                temperatureContent.setTag(format.replace(divided, "0"));

            } else {
                if (!TextUtils.isEmpty(result)) {
                    int firstNum = Integer.valueOf(result.substring(0, 1));
                    if (firstNum == 1) {
                        String[] arrays = new String[]{divided, divided, divided, divided, divided};
                        for (int i = 0; i < len; i++) {
                            String each = String.valueOf(result.charAt(i));
                            arrays[i] = each;
                        }
                        String format = String.format("%s %s %s . %s %s", arrays[0], arrays[1], arrays[2], arrays[3], arrays[4]);
                        temperatureContent.setText(format + type_flag_1);
                        temperatureContent.setTag(format.replace(divided, "0"));
                    } else {
                        String[] arrays = new String[]{divided, divided, divided, divided};
                        for (int i = 0; i < len; i++) {
                            String each = String.valueOf(result.charAt(i));
                            arrays[i] = each;
                        }
                        String format = String.format("%s %s . %s %s", arrays[0], arrays[1], arrays[2], arrays[3]);
                        temperatureContent.setText(format + type_flag_1);
                        temperatureContent.setTag(format.replace(divided, "0"));
                    }
                } else {
                    String[] arrays = new String[]{divided, divided, divided, divided};
                    String format = String.format("%s %s . %s %s", arrays[0], arrays[1], arrays[2], arrays[3]);
                    temperatureContent.setText(format + type_flag_1);
                    temperatureContent.setTag(format.replace(divided, "0"));
                }

            }
            if (update) {
                String tag = temperatureContent.getTag().toString()
                        .replace(" ", "").replace(".", "");
                if (!TextUtils.isEmpty(inputBuilder.toString())) {
                    inputBuilder.delete(0, inputBuilder.length());
                }
                inputBuilder.append(tag);
            }

        }
    }


    public double getValue() {
        if (temperatureContent != null) {
            //tag里存放真实的值
            String tag = temperatureContent.getTag().toString().replace(" ", "").trim();
            return Double.parseDouble(tag);
        }

        return 0;
    }

    public void delete(IEvent event) {
        int len = inputBuilder.toString().length();
        if (len > 0) {
            inputBuilder.deleteCharAt(len - 1);
        }

        if (event != null) {
            event.clear(inputBuilder.toString().length() == 0);
        }

        inputTemperature(inputBuilder.toString(), false);
    }

    public void input(int value) {

        if (AppInfo.getInstance().isTempUnitC()) {
            if (inputBuilder.length() == 4) {
                //长度等于4不在添加
                return;
            }
            inputBuilder.append(value);
        } else {
            if (!TextUtils.isEmpty(inputBuilder.toString())) {
                int firstNum = Integer.valueOf(inputBuilder.toString().substring(0, 1));
                if (firstNum == 1) {
                    if (inputBuilder.length() == 5) {
                        //长度等于5不在添加
                        return;
                    }
                    inputBuilder.append(value);
                } else {
                    if (inputBuilder.length() == 4) {
                        //长度等于4不在添加
                        return;
                    }
                    inputBuilder.append(value);
                }
            } else {
                inputBuilder.append(value);
            }
        }

        inputTemperature(inputBuilder.toString(), false);
    }

    public void setError() {
        if (temperatureContent != null) {
            temperatureContent.setTextColor(Color.parseColor("#FFFF7486"));
        }
    }

    public interface IEvent {
        /**
         * 是否已经清空
         *
         * @param result
         */
        void clear(boolean result);
    }
}
