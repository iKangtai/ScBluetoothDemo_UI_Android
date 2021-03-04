package com.ikangtai.bluetoothui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
 * 体温计绑定
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
     * 显示体温计状态
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
     * 显示设备蓝牙状态
     *
     * @param eventBus
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void synBluetoothState(BluetoothStateEventBus eventBus) {
        if (eventBus != null) {
            boolean isOpen = eventBus.isOpen();
            if (isOpen) {
                Log.i(TAG, "STATE_OFF 手机蓝牙已打开");
                handleScanSate();
            } else {
                Log.i(TAG, "STATE_OFF 手机蓝牙关闭");
                stepFirstLoading.initLoading();
                stepSecondLoading.initLoading();
                stepFirstState.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.device_binding_page_pic_bluetooth_unselected, 0);
            }
        }
    }

    /**
     * 显示设备信息
     *
     * @param eventBus
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void synBleDeviceInfo(BleDeviceInfoEvent eventBus) {
        if (eventBus != null) {
            handleFirmwareInfo(eventBus.getConnectScPeripheral());
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
     * 固件类型版本号映射
     *
     * @param scPeripheral
     */
    private void handleFirmwareInfo(ScPeripheral scPeripheral) {
        LogUtils.i("准备绑定设备");
        hardwareInfo = HardwareInfo.toHardwareInfo(scPeripheral);
        HardwareModel.saveHardwareInfo(BindDeviceActivity.this, hardwareInfo);
        EventBus.getDefault().post(new BleBindEvent());
        checkFirmwareVersion(scPeripheral);
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
     * 检查固件版本是否需要升级
     */
    public void checkFirmwareVersion(ScPeripheral scPeripheral) {
        Log.i(TAG, "用户绑定体温计成功");
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

    private void bindSuccess() {
        stepThirdLoading.finishLoading();
        stepThirdState.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.device_binding_page_pic_check_selected, 0);
        ToastUtils.show(getApplicationContext(), getString(R.string.attach_success));
        startActivity(new Intent(BindDeviceActivity.this, BindResultActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CheckBleFeaturesUtil.handBleFeaturesResult(this, requestCode, resultCode);
    }
}
