package com.example.bledemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bledemo.App;
import com.example.bledemo.AppInfo;
import com.example.bledemo.Keys;
import com.example.bledemo.MainActivity;
import com.example.bledemo.R;
import com.example.bledemo.contract.BleContract;
import com.example.bledemo.event.BleStateEventBus;
import com.example.bledemo.event.BluetoothStateEventBus;
import com.example.bledemo.event.TemperatureBleScanEventBus;
import com.example.bledemo.info.TemperatureInfo;
import com.example.bledemo.presenter.BlePresenter;
import com.example.bledemo.util.CheckBleFeaturesUtil;
import com.example.bledemo.util.DateUtil;
import com.example.bledemo.util.NotificationUtil;
import com.example.bledemo.view.dialog.BleAlertDialog;
import com.ikangtai.bluetoothsdk.util.LogUtils;
import com.ikangtai.bluetoothsdk.util.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment implements BleContract.IView {
    private BleContract.IPresenter presenter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        initView(root);
        return root;
    }

    private void initView(View view) {
        view.findViewById(R.id.add_temp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //显示添加温度View
                presenter.showAddTemperatureView();
            }
        });
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter = new BlePresenter(this, this);
    }

    /**
     * 显示体温计状态
     *
     * @param eventBus
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void syncBLeState(BleStateEventBus eventBus) {
        if (eventBus != null) {
            boolean isConn = eventBus.isConnect();
            if (isConn) {
                ToastUtils.show(getContext(), getString(R.string.thermometer_conn_success));
            } else {
                ToastUtils.show(getContext(), getString(R.string.thermometer_conn_fail));
            }
        }

    }

    /**
     * 显示设备蓝牙状态
     *
     * @param eventBus
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void synBluetoothState(BluetoothStateEventBus eventBus) {
        if (eventBus != null) {
            boolean isOpen = eventBus.isOpen();
            if (isOpen) {
                ToastUtils.show(getContext(), getString(R.string.turn_on_ble));
            } else {
                ToastUtils.show(getContext(), getString(R.string.turn_off_ble));
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //处理蓝牙开关、定位开关回调
        CheckBleFeaturesUtil.handBleFeaturesResult(getContext(), requestCode, resultCode);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.refreshDeviceList();
        if (!AppInfo.getInstance().isThermometerState()) {
            presenter.startScan();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        presenter.destroy();

    }

    /**
     * 处理接收到体温计温度
     *
     * @param temperatureInfoList
     */
    @Override
    public void onReceiveTemperatureData(List<TemperatureInfo> temperatureInfoList) {
        if (App.getInstance().isForeground()) {
            //App前台弹框显示
            StringBuffer message = new StringBuffer();
            for (TemperatureInfo temperatureInfo :
                    temperatureInfoList) {
                message.append(DateUtil.getDateFormatYMDHM(temperatureInfo.getMeasureTime()) + "\n" + AppInfo.getInstance().getTemp((float) temperatureInfo.getTemperature()) + AppInfo.getInstance().getTempUnit());
                message.append("\n");
            }
            new BleAlertDialog(getContext()).builder()
                    .setTitle(getString(R.string.temp_auto_upload_success))
                    .setMsg(message.toString())
                    .setCancelable(false)
                    .setCanceledOnTouchOutside(false)
                    .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).withOverLay().show();
        } else {
            //App后台发送通知
            int notifyTempNum = temperatureInfoList.size();
            double notifyTempValue = temperatureInfoList.get(0).getTemperature();
            String notificationContent = getNotificationContent(notifyTempNum, notifyTempValue);
            LogUtils.i("发送通知栏通知消息>>>");
            String title = getString(R.string.ble_temp_bg_notif_title);
            Intent intent = new Intent(getContext(), MainActivity.class);
            NotificationUtil.pushMessage(getContext(), false, title, notificationContent, intent);
        }
    }

    /**
     * 处理手动保存体温计温度
     *
     * @param temperatureInfo
     */
    public void onSaveTemperatureData(TemperatureInfo temperatureInfo) {
        //处理保存温度
        new BleAlertDialog(getContext()).builder()
                .setTitle(getString(R.string.temp_add_success))
                .setMsg(DateUtil.getDateFormatYMDHM(temperatureInfo.getMeasureTime()) + "\n" + temperatureInfo.getTemperature() + Keys.kTempUnitC)
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
    }

    private String getNotificationContent(int notifyTempNum, double notifyTempValue) {
        String notificationContent = getString(R.string.ble_temp_bg_notif_def_content);
        if (notifyTempNum == 1) {
            notificationContent = String.format(getString(R.string.ble_temp_bg_notif_content)
                    , AppInfo.getInstance().getTemp((float) notifyTempValue) + AppInfo.getInstance().getTempUnit());
        }

        notificationContent += getString(R.string.ble_temp_bg_notif_content_last);

        return notificationContent;
    }
}