package com.example.bledemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.bledemo.AppInfo;
import com.example.bledemo.R;
import com.example.bledemo.event.BleDeviceInfoEvent;
import com.example.bledemo.event.BleStateEventBus;
import com.example.bledemo.event.BluetoothStateEventBus;
import com.example.bledemo.event.TemperatureBleScanEventBus;
import com.example.bledemo.info.FirmwareVersionResp;
import com.example.bledemo.info.HardwareInfo;
import com.example.bledemo.model.HardwareModel;
import com.example.bledemo.util.CheckBleFeaturesUtil;
import com.example.bledemo.view.TopBar;
import com.example.bledemo.view.dialog.BleAlertDialog;
import com.example.bledemo.view.dialog.FirmwareUpdateDialog;
import com.example.bledemo.view.loading.LoadingView;
import com.google.gson.Gson;
import com.ikangtai.bluetoothsdk.model.ScPeripheral;
import com.ikangtai.bluetoothsdk.util.BleTools;
import com.ikangtai.bluetoothsdk.util.LogUtils;
import com.ikangtai.bluetoothsdk.util.ToastUtils;

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
            handleFirmwareInfo(eventBus.getConnectScPeripheral(), eventBus.getVersion());
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
     * @param firmwareVersion
     */
    private void handleFirmwareInfo(ScPeripheral scPeripheral, String firmwareVersion) {
        LogUtils.i("固件版本 = " + firmwareVersion);
        int hardwareType = 1;
        if (scPeripheral.getDeviceType() == BleTools.TYPE_AKY_3) {
            hardwareType = HardwareInfo.HW_GENERATION_AKY3;
        } else if (scPeripheral.getDeviceType() == BleTools.TYPE_AKY_4) {
            hardwareType = HardwareInfo.HW_GENERATION_AKY4;
        } else if (scPeripheral.getDeviceType() == BleTools.TYPE_SMART_THERMOMETER) {
            int intPart = BleTools.getDeviceHardVersion(scPeripheral.getDeviceType(), firmwareVersion);
            switch (intPart) {
                case BleTools.HW_GENERATION_1:
                    hardwareType = HardwareInfo.HW_GENERATION_1;
                    break;
                case BleTools.HW_GENERATION_2:
                    hardwareType = HardwareInfo.HW_GENERATION_2;
                    break;
                case BleTools.HW_GENERATION_3:
                    hardwareType = HardwareInfo.HW_GENERATION_3;
                    break;
            }
        }
        if (hardwareType == HardwareInfo.HW_GENERATION_AKY3) {
            LogUtils.i("这是新款第3代硬件!");
        } else if (hardwareType == HardwareInfo.HW_GENERATION_AKY4) {
            LogUtils.i("这是新款第4代硬件!");
        } else {
            LogUtils.i("这是旧款硬件! :" + hardwareType);
        }
        LogUtils.i("准备绑定设备");
        long time = System.currentTimeMillis();
        hardwareInfo = new HardwareInfo();
        hardwareInfo.setHardMacId(scPeripheral.getMacAddress());
        hardwareInfo.setHardBindingDate(time / 1000);
        hardwareInfo.setHardwareVersion(firmwareVersion);
        hardwareInfo.setHardType(HardwareInfo.HARD_TYPE_THERMOMETER);
        hardwareInfo.setHardHardwareType(hardwareType);
        HardwareModel.saveHardwareInfo(BindDeviceActivity.this, hardwareInfo);
        checkFirmwareVersion();
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
    public void checkFirmwareVersion() {
        Log.i(TAG, "用户绑定体温计成功");
        stepThirdLoading.finishLoading();
        stepThirdState.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.device_binding_page_pic_check_selected, 0);
        if (hardwareInfo.getHardType() == HardwareInfo.HARD_TYPE_THERMOMETER && hardwareInfo.getHardHardwareType() == HardwareInfo.HW_GENERATION_3 && AppInfo.getInstance().getServerHardwareVersion() > Double.parseDouble(hardwareInfo.getHardwareVersion())) {
            //模拟需要固件升级
            //旧三代 {"code":200,"message":"Success","data":{"fileUrl":"{\"A\":\"http://yunchengfile.oss-cn-beijing.aliyuncs.com/firmware/A31/Athermometer.bin\",\"B\":\"http://yunchengfile.oss-cn-beijing.aliyuncs.com/firmware/A31/Bthermometer.bin\"}\r\n","version":"3.68","type":1}}
            //新三代四代  {"code":200,"message":"Success","data":{"fileUrl":"http://yunchengfile.oss-cn-beijing.aliyuncs.com/firmware/A31/Bthermometer.bin","version":"6.1","type":2}}
            String jsonData = "{\"code\":200,\"message\":\"Success\",\"data\":{\"fileUrl\":\"{\\\"A\\\":\\\"http://yunchengfile.oss-cn-beijing.aliyuncs.com/firmware/A31/Athermometer.bin\\\",\\\"B\\\":\\\"http://yunchengfile.oss-cn-beijing.aliyuncs.com/firmware/A31/Bthermometer.bin\\\"}\\r\\n\",\"version\":\"3.68\",\"type\":1}}";
            final FirmwareVersionResp firmwareVersionResp = new Gson().fromJson(jsonData, FirmwareVersionResp.class);
            new BleAlertDialog(BindDeviceActivity.this).builder()
                    .setTitle(getString(R.string.warm_prompt))
                    .setMsg(getString(R.string.device_upate_tips))
                    .setCancelable(false)
                    .setCanceledOnTouchOutside(false)
                    .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new FirmwareUpdateDialog(BindDeviceActivity.this, hardwareInfo, firmwareVersionResp.getData()).builder().initEvent(new FirmwareUpdateDialog.IEvent() {
                                @Override
                                public void onDismiss() {
                                    bindSuccess();
                                }
                            }).show();
                        }
                    }).show();
            return;
        }
        bindSuccess();
    }

    private void bindSuccess() {
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
