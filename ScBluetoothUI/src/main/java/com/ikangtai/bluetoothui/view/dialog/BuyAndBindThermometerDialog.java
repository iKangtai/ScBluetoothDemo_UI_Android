package com.ikangtai.bluetoothui.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.ikangtai.bluetoothui.Keys;
import com.ikangtai.bluetoothui.R;
import com.ikangtai.bluetoothui.activity.BindDeviceActivity;
import com.ikangtai.bluetoothui.util.ThirdAppUtils;


/**
 * Bind/Buy Thermometer
 */
public class BuyAndBindThermometerDialog extends BaseShecareDialog {

    private Context context;
    private Display display;

    private TextView gotoBind;
    private TextView gotoBuy;


    public BuyAndBindThermometerDialog(Context context) {
        this.context = context;
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();
    }

    public BuyAndBindThermometerDialog builder() {
        View view = LayoutInflater.from(context).inflate(
                R.layout.layout_bind_buy_thermometer, null);

        gotoBind = view.findViewById(R.id.gotoBind);
        gotoBuy = view.findViewById(R.id.gotoBuy);


        gotoBind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                //Jump to the thermometer binding interface
                context.startActivity(new Intent(context, BindDeviceActivity.class));
            }
        });


        gotoBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                //Enter to buy a thermometer
                String url = Keys.SHOP_URL;
                ThirdAppUtils.handleShop(context, url);
            }
        });
        dialog = new Dialog(context, R.style.BleAlertDialogStyle);
        dialog.setContentView(view);

        return this;
    }

    public BuyAndBindThermometerDialog show() {

        if (dialog != null) {
            dialog.show();
        }

        return this;
    }


}
