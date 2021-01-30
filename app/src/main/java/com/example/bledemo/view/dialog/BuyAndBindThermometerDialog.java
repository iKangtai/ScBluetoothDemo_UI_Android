package com.example.bledemo.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.bledemo.R;
import com.example.bledemo.activity.BindDeviceActivity;
import com.example.bledemo.util.ThirdAppUtils;


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
                //跳转到体温计绑定界面
                context.startActivity(new Intent(context, BindDeviceActivity.class));
            }
        });


        gotoBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                //进入购买体温计
                String url = "https://s.click.taobao.com/t?e=m%3D2%26s%3DfaVd4i2mdTccQipKwQzePOeEDrYVVa64K7Vc7tFgwiFRAdhuF14FMXD0IuTjzjxDMMgx22UI05YG50TC%2BOEKRV2PCaw8s0I5%2B4XeV4Br1rRhqXPujuVgf9JduXIAgIVPa7G8XfSvkCk8eRex8LTEM9CkUAHLmx%2B1xg5p7bh%2BFbQ%3D&pvid=53_115.239.25.5_624_1611125519095";
                ThirdAppUtils.handleShop(context, url);
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
