package com.ikangtai.bluetoothui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ikangtai.bluetoothsdk.ScPeripheralManager;
import com.ikangtai.bluetoothsdk.http.respmodel.CheckFirmwareVersionResp;
import com.ikangtai.bluetoothsdk.listener.CheckFirmwareVersionListener;
import com.ikangtai.bluetoothsdk.model.ScPeripheral;
import com.ikangtai.bluetoothsdk.util.BleTools;
import com.ikangtai.bluetoothsdk.util.LogUtils;
import com.ikangtai.bluetoothsdk.util.ToastUtils;
import com.ikangtai.bluetoothui.AppInfo;
import com.ikangtai.bluetoothui.R;
import com.ikangtai.bluetoothui.event.BleBindEvent;
import com.ikangtai.bluetoothui.event.BleDeviceInfoEvent;
import com.ikangtai.bluetoothui.event.BleStateEventBus;
import com.ikangtai.bluetoothui.event.BluetoothStateEventBus;
import com.ikangtai.bluetoothui.event.TemperatureBleScanEventBus;
import com.ikangtai.bluetoothui.info.HardwareInfo;
import com.ikangtai.bluetoothui.model.HardwareModel;
import com.ikangtai.bluetoothui.util.CheckBleFeaturesUtil;
import com.ikangtai.bluetoothui.view.TopBar;
import com.ikangtai.bluetoothui.view.dialog.BleAlertDialog;
import com.ikangtai.bluetoothui.view.dialog.FirmwareUpdateDialog;
import com.ikangtai.bluetoothui.view.loading.LoadingView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Thermometer binding
 *
 * @author xiongyl 2021/1/21 21:11
 */
public class BindDeviceActivity extends AppCompatActivity {
    public static final String TAG = BindDeviceActivity.class.getSimpleName();
    private TopBar topBar;
    public TextView stepFirstState;
    public TextView stepSecondState;
    private TextView stepThirdState;
    private LoadingView stepFirstLoading, stepSecondLoading, stepThirdLoading;
    private HardwareInfo hardwareInfo;

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
                stepSecondLoading.finishLoading();
                stepSecondState.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.device_binding_page_pic_device_selected, 0);
                stepThirdLoading.startLoading();
                ToastUtils.show(this, getString(R.string.binding));
            } else {
                stepSecondLoading.initLoading();
                stepSecondState.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.device_binding_page_pic_device_unselected, 0);
                handleScanSate();
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
                Log.i(TAG, "STATE_OFF");
                handleScanSate();
            } else {
                Log.i(TAG, "STATE_OFF");
                stepFirstLoading.initLoading();
                stepSecondLoading.initLoading();
                stepFirstState.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.device_binding_page_pic_bluetooth_unselected, 0);
            }
        }
    }

    /**
     * Receive device info
     *
     * @param eventBus
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void synBleDeviceInfo(BleDeviceInfoEvent eventBus) {
        if (eventBus != null) {
            handleFirmwareInfo(eventBus.getConnectScPeripheral());
            checkFirmwareVersion(eventBus.getConnectScPeripheral());
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        AppInfo.getInstance().setBindActivityActive(true);
        setContentView(R.layout.activity_bind_device);
        initView();
        CheckBleFeaturesUtil.checkBleFeatures(this);
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
        stepFirstLoading = findViewById(R.id.stepFirstLoading3);
        stepSecondLoading = findViewById(R.id.stepSecondLoading3);
        stepThirdLoading = findViewById(R.id.stepThirdLoading3);
        stepFirstState = findViewById(R.id.stepFirstState3);
        stepSecondState = findViewById(R.id.stepSecondState3);
        stepThirdState = findViewById(R.id.stepThirdState3);
    }

    public void handleScanSate() {
        if (BleTools.checkBleEnable()) {
            stepFirstLoading.finishLoading();
            stepFirstState.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.device_binding_page_pic_bluetooth_selected, 0);
            stepSecondLoading.startLoading();
        } else {
            stepFirstLoading.initLoading();
            stepSecondLoading.initLoading();
            stepFirstState.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.device_binding_page_pic_bluetooth_unselected, 0);
        }
    }

    /**
     * Bind the device and save the scanned device information
     *
     * @param scPeripheral
     */
    private void handleFirmwareInfo(ScPeripheral scPeripheral) {
        LogUtils.i("Prepare to bind the device");
        hardwareInfo = HardwareInfo.toHardwareInfo(scPeripheral);
        HardwareModel.saveHardwareInfo(BindDeviceActivity.this, hardwareInfo);
        EventBus.getDefault().post(new BleBindEvent());
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        if (!AppInfo.getInstance().isThermometerState() && !AppInfo.getInstance().isOADConnectActive()) {
            EventBus.getDefault().post(new TemperatureBleScanEventBus());
            handleScanSate();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        EventBus.getDefault().unregister(this);
        AppInfo.getInstance().setBindActivityActive(false);
    }

    /**
     * Check whether the firmware version needs to be upgraded
     */
    public void checkFirmwareVersion(ScPeripheral scPeripheral) {
        Log.i(TAG, "check firmware version");
        boolean mockData = false;
        if (mockData) {
            checkFirmwareVersionMock(scPeripheral);
            return;
        }
        ScPeripheralManager.getInstance().checkFirmwareVersion(scPeripheral, new CheckFirmwareVersionListener() {
            @Override
            public void checkSuccess(final CheckFirmwareVersionResp.Data data) {
                if (Double.parseDouble(data.getVersion()) > Double.parseDouble(hardwareInfo.getHardwareVersion())) {
                    new BleAlertDialog(BindDeviceActivity.this).builder()
                            .setTitle(getString(R.string.warm_prompt))
                            .setMsg(getString(R.string.device_upate_tips))
                            .setCancelable(false)
                            .setCanceledOnTouchOutside(false)
                            .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new FirmwareUpdateDialog(BindDeviceActivity.this, hardwareInfo, data).builder().initEvent(new FirmwareUpdateDialog.IEvent() {
                                        @Override
                                        public void onDismiss() {
                                            bindSuccess();
                                        }
                                    }).show();
                                }
                            }).show();
                } else {
                    bindSuccess();
                }
            }

            @Override
            public void checkFail() {
                bindSuccess();
            }
        });

    }

    /**
     * Mock Check whether the firmware version needs to be upgraded
     */
    public void checkFirmwareVersionMock(ScPeripheral scPeripheral) {
        int deviceType = scPeripheral.getDeviceType();
        CheckFirmwareVersionResp firmwareVersionResp = null;
        if (deviceType == BleTools.TYPE_SMART_THERMOMETER) {
            //旧三代体温计
            String jsonData = "{\"code\":200,\"message\":\"Success\",\"data\":{\"fileUrl\":\"{\\\"A\\\":\\\"http://yunchengfile.oss-cn-beijing.aliyuncs.com/firmware/A31/Athermometer.bin\\\",\\\"B\\\":\\\"http://yunchengfile.oss-cn-beijing.aliyuncs.com/firmware/A31/Bthermometer.bin\\\"}\\r\\n\",\"version\":\"3.68\",\"type\":1}}";
            firmwareVersionResp = new Gson().fromJson(jsonData, CheckFirmwareVersionResp.class);
        } else if (deviceType == BleTools.TYPE_AKY_3 || deviceType == BleTools.TYPE_AKY_4) {
            //新三代四代体温计
            String jsonData = "{\"code\":200,\"message\":\"Success\",\"data\":{\"fileUrl\":\"https://yunchengfile.oss-cn-beijing.aliyuncs.com/firmware/A32/thermometer_6.01.img\",\"version\":\"6.01\",\"type\":2}}";
            firmwareVersionResp = new Gson().fromJson(jsonData, CheckFirmwareVersionResp.class);
        } else if (deviceType == BleTools.TYPE_LJ_TXY) {
            //胎心仪FD120A
            String jsonData = "{\"code\":200,\"message\":\"Success\",\"data\":{\"fileUrl\":\"https://yunchengfile.oss-cn-beijing.aliyuncs.com/firmware/FD120A/txy_1.0.1.img\",\"version\":\"1.01\",\"type\":3}}";
            firmwareVersionResp = new Gson().fromJson(jsonData, CheckFirmwareVersionResp.class);
        }

        if (firmwareVersionResp != null && firmwareVersionResp.getData() != null) {
            final CheckFirmwareVersionResp.Data data = firmwareVersionResp.getData();
            data.setMockData(true);
            new BleAlertDialog(BindDeviceActivity.this).builder()
                    .setTitle(getString(R.string.warm_prompt))
                    .setMsg(getString(R.string.device_upate_tips))
                    .setCancelable(false)
                    .setCanceledOnTouchOutside(false)
                    .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new FirmwareUpdateDialog(BindDeviceActivity.this, hardwareInfo, data).builder().initEvent(new FirmwareUpdateDialog.IEvent() {
                                @Override
                                public void onDismiss() {
                                    bindSuccess();
                                }
                            }).show();
                        }
                    }).show();
        } else {
            bindSuccess();
        }

    }

    private void bindSuccess() {
        Log.i(TAG, "The user successfully binds the thermometer");
        stepThirdLoading.finishLoading();
        stepThirdState.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.device_binding_page_pic_check_selected, 0);
        ToastUtils.show(getApplicationContext(), getString(R.string.bind_success));
        startActivity(new Intent(BindDeviceActivity.this, BindResultActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CheckBleFeaturesUtil.handBleFeaturesResult(this, requestCode, resultCode);
    }
}
