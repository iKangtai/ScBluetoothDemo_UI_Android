package com.example.bledemo.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.bledemo.R;


/**
 * 绑定/购买体温计
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
                //RouteUtils.go(RouteUtils.ROUTE_APP_DEVICE_CHOOSE, Keys.KEY_TYPE, Keys.KEY_BIND_THERMOMETER);
            }
        });


        gotoBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                //进入购买体温计
                //EventBus.getDefault().post(new ChangeTabEvent(JPType.EXTRA_MARKET, Urls.getThermomterProductUrl()));
            }
        });

        // 定义Dialog布局和参数
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
