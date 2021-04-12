package com.example.bledemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.bledemo.App;
import com.example.bledemo.MainActivity;
import com.example.bledemo.R;
import com.ikangtai.bluetoothui.AppInfo;
import com.ikangtai.bluetoothui.Keys;
import com.ikangtai.bluetoothui.contract.BleContract;
import com.ikangtai.bluetoothui.event.AutoUploadTemperatureEvent;
import com.ikangtai.bluetoothui.event.BleStateEventBus;
import com.ikangtai.bluetoothui.event.BluetoothStateEventBus;
import com.ikangtai.bluetoothui.info.TemperatureInfo;
import com.ikangtai.bluetoothui.presenter.BlePresenter;
import com.ikangtai.bluetoothui.util.CheckBleFeaturesUtil;
import com.ikangtai.bluetoothui.util.DateUtil;
import com.ikangtai.bluetoothui.util.NotificationUtil;
import com.ikangtai.bluetoothui.view.dialog.BleAlertDialog;

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
                //Show the added temperature View
                presenter.showAddTemperatureView();
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter = new BlePresenter(this, this);
    }

    @Override
    public void onStop() {
        super.onStop();
        setUserVisibleHint(false);
    }

    /**
     * Receive thermometer status
     *
     * @param eventBus
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void syncBLeState(BleStateEventBus eventBus) {
        if (eventBus != null) {
            boolean isConn = eventBus.isConnect();
            if (isConn) {
                Toast.makeText(getContext(), getString(R.string.thermometer_conn_success),Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), getString(R.string.thermometer_conn_fail),Toast.LENGTH_LONG).show();
            }
        }

    }

    /**
     * Receive device Bluetooth status
     *
     * @param eventBus
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void synBluetoothState(BluetoothStateEventBus eventBus) {
        if (eventBus != null) {
            boolean isOpen = eventBus.isOpen();
            if (isOpen) {
                Toast.makeText(getContext(), getString(R.string.turn_on_ble),Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), getString(R.string.turn_off_ble),Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Receive thermometer temperature data
     *
     * @param temperatureInfoList
     */
    @Override
    public void onReceiveTemperatureData(List<TemperatureInfo> temperatureInfoList) {
        /**
         * @TODO Need App to save the temperature data
         */
        if (App.getInstance().isForeground()) {
            //App front pop-up display
            StringBuffer message = new StringBuffer();
            for (TemperatureInfo temperatureInfo :
                    temperatureInfoList) {
                message.append(DateUtil.getDateFormatYMDHM(temperatureInfo.getMeasureTime()) + "\n" + AppInfo.getInstance().getTemp((float) temperatureInfo.getTemperature()) + AppInfo.getInstance().getTempUnit());
                message.append("\n");
            }
            if (getUserVisibleHint()) {
                new BleAlertDialog(getContext()).builder()
                        .setTitle(getString(R.string.temp_auto_upload_success))
                        .setMsg(message.toString())
                        .setCancelable(false)
                        .setCanceledOnTouchOutside(false)
                        .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }).show();
            } else {
                EventBus.getDefault().post(new AutoUploadTemperatureEvent(getString(R.string.temp_auto_upload_success), message.toString()));
            }
        } else {
            //App background to send notifications
            int notifyTempNum = temperatureInfoList.size();
            double notifyTempValue = temperatureInfoList.get(0).getTemperature();
            String notificationContent = getNotificationContent(notifyTempNum, notifyTempValue);
            String title = getString(R.string.ble_temp_bg_notif_title);
            Intent intent = new Intent(getContext(), MainActivity.class);
            NotificationUtil.pushMessage(getContext(), false, title, notificationContent, intent);
        }
    }

    /**
     * Receive manually added body temperature data
     *
     * @param temperatureInfo
     */
    public void onSaveTemperatureData(TemperatureInfo temperatureInfo) {
        /**
         * @TODO Need App to save the temperature data
         */
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

    /**
     * Splicing notification content
     *
     * @param notifyTempNum
     * @param notifyTempValue
     * @return
     */
    private String getNotificationContent(int notifyTempNum, double notifyTempValue) {
        String notificationContent = getString(R.string.ble_temp_bg_notif_def_content);
        if (notifyTempNum == 1) {
            notificationContent = String.format(getString(R.string.ble_temp_bg_notif_content)
                    , AppInfo.getInstance().getTemp((float) notifyTempValue) + AppInfo.getInstance().getTempUnit());
        }

        notificationContent += getString(R.string.ble_temp_bg_notif_content_last);

        return notificationContent;
    }

    @Override
    public void onResume() {
        super.onResume();
        setUserVisibleHint(true);
        //Restart scanning for nearby devices when the device is not connected
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
        //Release resources to disconnect Bluetooth
        presenter.destroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Processing request to turn on the Bluetooth switch and positioning switch results
        CheckBleFeaturesUtil.handBleFeaturesResult(getContext(), requestCode, resultCode);
    }
}