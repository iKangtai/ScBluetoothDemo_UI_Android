package com.ikangtai.bluetoothui.activity;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ikangtai.bluetoothsdk.ScPeripheralManager;
import com.ikangtai.bluetoothsdk.http.respmodel.CheckFirmwareVersionResp;
import com.ikangtai.bluetoothsdk.listener.CheckFirmwareVersionListener;
import com.ikangtai.bluetoothsdk.model.ScPeripheral;
import com.ikangtai.bluetoothsdk.util.LogUtils;
import com.ikangtai.bluetoothsdk.util.ToastUtils;
import com.ikangtai.bluetoothui.R;
import com.ikangtai.bluetoothui.event.BleBindEvent;
import com.ikangtai.bluetoothui.info.HardwareInfo;
import com.ikangtai.bluetoothui.model.HardwareModel;
import com.ikangtai.bluetoothui.view.TopBar;
import com.ikangtai.bluetoothui.view.dialog.BleAlertDialog;
import com.ikangtai.bluetoothui.view.dialog.FirmwareUpdateDialog;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

/**
 * My device
 *
 * @author xiongyl 2021/1/21 21:11
 */
public class MyDeviceActivity extends AppCompatActivity implements View.OnClickListener {

    private TopBar topBar;
    private Button unbindDevice;
    private TextView deviceUpgrade;
    private HardwareInfo hardwareInfo;
    public ImageView deviceLogo;
    public TextView deviceName;
    public TextView deviceMacAddress;
    public TextView deviceOadVesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.i("Go to my device page");
        setContentView(R.layout.activity_my_device);
        initView();
    }

    private void initView() {
        topBar = findViewById(R.id.topBar);
        topBar.setOnTopBarClickListener(new TopBar.OnTopBarClickListener() {
            @Override
            public void leftClick() {
                finish();
            }

            @Override
            public void midLeftClick() {

            }

            @Override
            public void midRightClick() {

            }

            @Override
            public void rightClick() {

            }
        });

        deviceName = findViewById(R.id.deviceName);
        deviceLogo = findViewById(R.id.deviceLogo);
        deviceMacAddress = findViewById(R.id.deviceMacAddress);
        deviceOadVesion = findViewById(R.id.deviceOadVesion);
        unbindDevice = findViewById(R.id.unbind_device);
        unbindDevice.setOnClickListener(this);
        deviceUpgrade = findViewById(R.id.device_upgrade);
        deviceUpgrade.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        deviceUpgrade.getPaint().setAntiAlias(true);
        deviceUpgrade.setOnClickListener(this);

        loadData();
    }


    private void loadData() {
        List<HardwareInfo> hardwareInfoList = HardwareModel.hardwareList(this);
        if (!hardwareInfoList.isEmpty()) {
            hardwareInfo = hardwareInfoList.get(0);
            deviceLogo.setImageResource(hardwareInfo.getDeviceLogo());
            deviceName.setText(hardwareInfo.getDeviceName(MyDeviceActivity.this));
            deviceMacAddress.setText(getString(R.string.macAddress)
                    + hardwareInfo.getHardMacId());
            deviceOadVesion.setText(getString(R.string.deviceVersion)
                    + hardwareInfo.getHardwareVersion());
            if (hardwareInfo.getHardType() != HardwareInfo.HARD_TYPE_THERMOMETER) {
                deviceUpgrade.setVisibility(View.GONE);
            } else {
                deviceUpgrade.setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.device_upgrade) {
            ScPeripheral scPeripheral = HardwareInfo.toScPeripheral(hardwareInfo);
            ScPeripheralManager.getInstance().checkFirmwareVersion(scPeripheral, new CheckFirmwareVersionListener() {
                @Override
                public void checkSuccess(final CheckFirmwareVersionResp.Data data) {
                    if (Double.parseDouble(data.getVersion()) > Double.parseDouble(hardwareInfo.getHardwareVersion())) {
                        new BleAlertDialog(MyDeviceActivity.this).builder()
                                .setTitle(getString(R.string.warm_prompt))
                                .setMsg(getString(R.string.device_upate_tips))
                                .setCancelable(false)
                                .setCanceledOnTouchOutside(false)
                                .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        new FirmwareUpdateDialog(MyDeviceActivity.this, hardwareInfo, data).builder().initEvent(new FirmwareUpdateDialog.IEvent() {
                                            @Override
                                            public void onDismiss() {

                                            }
                                        }).show();
                                    }
                                }).show();
                    } else {
                        this.checkFail();
                    }
                }

                @Override
                public void checkFail() {
                    new BleAlertDialog(MyDeviceActivity.this).builder()
                            .setTitle(getString(R.string.warm_prompt))
                            .setMsg(getString(R.string.already_latest_ver))
                            .setCancelable(false)
                            .setCanceledOnTouchOutside(false)
                            .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).show();
                }
            });
        } else if (v.getId() == R.id.unbind_device) {
            if (ClickFilter.filter()) {
                return;
            }
            HardwareModel.deleteHardwareInfo(MyDeviceActivity.this, hardwareInfo);
            EventBus.getDefault().post(new BleBindEvent());
            ScPeripheralManager.getInstance().disconnectPeripheral();
            ToastUtils.show(MyDeviceActivity.this, getString(R.string.unbind_device));
            finish();
        }
    }

    public static class ClickFilter {
        //防止连续点击的时间间隔
        public static final long INTERVAL = 500L;
        //上一次点击的时间
        private static long lastClickTime = 0L;

        public static boolean filter() {
            long time = System.currentTimeMillis();

            if ((time - lastClickTime) > INTERVAL) {
                lastClickTime = time;
                return false;
            }
            return true;
        }
    }
}
