package com.ikangtai.bluetoothui.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ikangtai.bluetoothui.R;
import com.ikangtai.bluetoothui.util.DateUtil;


/**
 * 添加温度
 */
public class AndTemperatureDialog extends BaseShecareDialog {

    private Context context;
    private Display display;
    private TextView titleTv;
    private ImageView closeView;
    private Button temp_add;
    private Button temp_auto_upload;


    public AndTemperatureDialog(Context context) {
        this.context = context;
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();
    }

    public AndTemperatureDialog builder() {
        View view = LayoutInflater.from(context).inflate(
                R.layout.layout_add_temperature_dialog, null);
        titleTv = view.findViewById(R.id.txt_title);
        closeView = view.findViewById(R.id.iv_dialog_close);
        temp_add = view.findViewById(R.id.temp_add);
        temp_auto_upload = view.findViewById(R.id.temp_auto_upload);
        titleTv.setText(DateUtil.getDateFormatMD(System.currentTimeMillis() / 1000));
        closeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        temp_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (event != null) {
                    event.clickManualAddTemperature();
                }
            }
        });


        temp_auto_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (event != null) {
                    event.clickAutoSyncTemperature();
                }
            }
        });

        // 定义Dialog布局和参数
        dialog = new Dialog(context, R.style.BleAlertDialogStyle);
        dialog.setContentView(view);

        return this;
    }

    public AndTemperatureDialog show() {

        if (dialog != null) {
            dialog.show();
        }

        return this;
    }

    private AndTemperatureDialog.IEvent event;

    public AndTemperatureDialog initEvent(AndTemperatureDialog.IEvent event) {
        this.event = event;
        return this;
    }

    public interface IEvent {
        void clickAutoSyncTemperature();

        void clickManualAddTemperature();
    }


}
