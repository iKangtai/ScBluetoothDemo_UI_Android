package com.example.bledemo.activity;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.bledemo.AppInfo;
import com.example.bledemo.R;
import com.example.bledemo.ThermometerParameters;
import com.example.bledemo.info.FirmwareVersionResp;
import com.example.bledemo.info.HardwareInfo;
import com.example.bledemo.model.HardwareModel;
import com.example.bledemo.view.TopBar;
import com.example.bledemo.view.dialog.BleAlertDialog;
import com.example.bledemo.view.dialog.FirmwareUpdateDialog;
import com.example.bledemo.view.loading.LoadingView;
import com.google.gson.Gson;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.ikangtai.bluetoothsdk.BleCommand;
import com.ikangtai.bluetoothsdk.Config;
import com.ikangtai.bluetoothsdk.ScPeripheralManager;
import com.ikangtai.bluetoothsdk.listener.ReceiveDataListenerAdapter;
import com.ikangtai.bluetoothsdk.listener.ScanResultListener;
import com.ikangtai.bluetoothsdk.model.BleCommandData;
import com.ikangtai.bluetoothsdk.model.ScPeripheral;
import com.ikangtai.bluetoothsdk.model.ScPeripheralData;
import com.ikangtai.bluetoothsdk.util.BleParam;
import com.ikangtai.bluetoothsdk.util.BleTools;
import com.ikangtai.bluetoothsdk.util.FileUtil;
import com.ikangtai.bluetoothsdk.util.LogUtils;
import com.ikangtai.bluetoothsdk.util.ToastUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * desc
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
    private Dialog progressNumDialog;
    public String firmwareVersion = null;
    public int hardwareType;
    /**
     * BLE 相关
     */
    private Handler mHandler = new Handler();
    private boolean mScanning = false;
    private static final long SCAN_PERIOD = 30 * 60 * 1000;
    private Runnable stopScanRunnable = null;

    public String deviceAddr;
    /**
     * 用于处理同一个设备多次发广播的问题
     */
    public Map<String, Boolean> deviceFoundMap = new HashMap<>();
    private String deviceName;

    private ScPeripheralManager scPeripheralManager;
    private ReceiveDataListenerAdapter receiveDataListenerAdapter;
    private boolean isWaitBinding;
    protected final int REQUEST_CODE_LOCATION_SETTINGS = 1000;
    private BroadcastReceiver bleReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.i(TAG, "STATE_OFF 手机蓝牙关闭");
                        stepFirstLoading.initLoading();
                        stepSecondLoading.initLoading();
                        stepFirstState.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.device_binding_page_pic_bluetooth_unselected, 0);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.i(TAG, "STATE_TURNING_OFF 手机蓝牙正在关闭");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.i(TAG, "STATE_ON 手机蓝牙开启");
                        // 开启后，需要一定的时间差再扫描
                        // 温度计扫描操作打开
                        handleScan();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.i(TAG, "STATE_TURNING_ON 手机蓝牙正在开启");
                        stepFirstLoading.startLoading();
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_device);
        initView();
        initData();
        initBleSdk();
        registerBleReceiver();
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

    private void initData() {

    }

    public void registerBleReceiver() {
        //Register to receive Bluetooth switch broadcast
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bleReceiver, filter);
    }

    public void unRegisterBleReceiver() {
        unregisterReceiver(bleReceiver);
    }

    private void initBleSdk() {
        String logFilePath = new File(FileUtil.createRootPath(this), "log.txt").getAbsolutePath();
        BufferedWriter logWriter = null;
        try {
            logWriter = new BufferedWriter(new FileWriter(logFilePath, true), 2048);
        } catch (IOException e) {
            e.printStackTrace();
        }
        /**
         * There are two ways to configure log
         * 1. {@link Config.Builder#logWriter(Writer)}
         * 2. {@link Config.Builder#logFilePath(String)}
         */
        scPeripheralManager = ScPeripheralManager.getInstance();
        Config config = new Config.Builder().logWriter(logWriter).build();
        //sdk init
        scPeripheralManager.init(this, config);
        receiveDataListenerAdapter = new ReceiveDataListenerAdapter() {
            @Override
            public void onReceiveData(String macAddress, List<ScPeripheralData> scPeripheralDataList) {

            }

            @Override
            public void onReceiveError(String macAddress, int code, String msg) {
                super.onReceiveError(macAddress, code, msg);
                LogUtils.d("onReceiveError:" + code + "  " + msg);
            }

            @Override
            public void onReceiveCommandData(String macAddress, int type, int resultCode, String value) {
                super.onReceiveCommandData(macAddress, type, resultCode, value);
                if (AppInfo.getInstance().isOADConnectActive()) {
                    return;
                }
                switch (type) {
                    case BleCommand.GET_FIRMWARE_VERSION:
                        BleCommandData commandData = new BleCommandData();
                        commandData.setParam1(AppInfo.getInstance().isTempUnitC() ? 1 : 2);
                        scPeripheralManager.sendPeripheralCommand(macAddress, BleCommand.SYNC_THERMOMETER_UNIT, commandData);
                        scPeripheralManager.sendPeripheralCommand(macAddress, BleCommand.SYNC_TIME);
                        ThermometerParameters.FW_VERSION = value;
                        handleFirmwareInfo();
                        break;
                }
            }

            @Override
            public void onReceiveCommandData(String macAddress, int type, int resultCode, byte[] value) {
                if (AppInfo.getInstance().isOADConnectActive()) {
                    return;
                }
                switch (type) {
                    case BleCommand.GET_DEVICE_DATA:
                        if (isWaitBinding && TextUtils.equals(deviceName, BleParam.BLE_TXY_LJ_NAME)) {
                            isWaitBinding = false;
                            ThermometerParameters.FW_VERSION = "1.0";
                            handleFirmwareInfo();
                        }
                        break;
                }

            }

            @Override
            public void onConnectionStateChange(String macAddress, int state) {
                super.onConnectionStateChange(macAddress, state);
                if (AppInfo.getInstance().isOADConnectActive()) {
                    return;
                }
                if (state == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "The device is connected " + macAddress);
                    AppInfo.getInstance().setThermometerState(true);
                    updateConnectInfo(ThermometerParameters.STATE_CONNECTED);
                    isWaitBinding = true;
                } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i(TAG, "Device disconnected " + macAddress);
                    AppInfo.getInstance().setThermometerState(false);
                    updateConnectInfo(ThermometerParameters.STATE_DISCONNECTED);
                    isWaitBinding = false;
                    // 连接断开之后，紧接着继续扫描
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "连接断开后继续扫描!");
                            handleScan();
                        }
                    });
                }
            }
        };
        scPeripheralManager.addReceiveDataListener(receiveDataListenerAdapter);
    }

    private void handleFirmwareInfo() {
        firmwareVersion = ThermometerParameters.FW_VERSION;
        Log.i(TAG, "固件版本 = " + ThermometerParameters.FW_VERSION);
        if (TextUtils.equals(deviceName, ThermometerParameters.BLE_AKY3_NAME)) {
            ThermometerParameters.HW_GENERATION = ThermometerParameters.HW_GENERATION_AKY3;
        } else if (TextUtils.equals(deviceName, ThermometerParameters.BLE_AKY4_NAME)) {
            ThermometerParameters.HW_GENERATION = ThermometerParameters.HW_GENERATION_AKY4;
        } else if (TextUtils.equals(deviceName, BleParam.BLE_TXY_LJ_NAME)) {
            ThermometerParameters.HW_GENERATION = ThermometerParameters.HW_GENERATION_LAIJIA_TXY;
        } else if (BleTools.getDeviceType(deviceName) == BleTools.TYPE_EWQ) {
            ThermometerParameters.HW_GENERATION = ThermometerParameters.HW_GENERATION_EWQ;
        } else if (!ThermometerParameters.FW_VERSION.equals("")) {
            int intPart = Integer.valueOf(ThermometerParameters.FW_VERSION.split("\\.")[0]);
            Log.i(TAG, "这是第-----" + intPart);

            if (intPart > 3) {
                ThermometerParameters.HW_GENERATION = ThermometerParameters.HW_GENERATION_3;
            } else {
                switch (intPart) {
                    case ThermometerParameters.HW_GENERATION_1:
                        ThermometerParameters.HW_GENERATION = ThermometerParameters.HW_GENERATION_1;
                        break;
                    case ThermometerParameters.HW_GENERATION_2:
                        ThermometerParameters.HW_GENERATION = ThermometerParameters.HW_GENERATION_2;
                        break;
                    case ThermometerParameters.HW_GENERATION_3:
                        ThermometerParameters.HW_GENERATION = ThermometerParameters.HW_GENERATION_3;
                        break;
                }
            }
        }
        if (ThermometerParameters.HW_GENERATION == ThermometerParameters.HW_GENERATION_AKY3) {
            Log.i(TAG, "这是安康源第3代硬件!");
        } else if (ThermometerParameters.HW_GENERATION == ThermometerParameters.HW_GENERATION_AKY4) {
            Log.i(TAG, "这是安康源第4代硬件!");
        } else if (ThermometerParameters.HW_GENERATION == ThermometerParameters.HW_GENERATION_LAIJIA_TXY) {
            Log.i(TAG, "这是莱佳胎心仪硬件!");
        } else if (ThermometerParameters.HW_GENERATION == ThermometerParameters.HW_GENERATION_EWQ) {
            Log.i(TAG, "这是额温枪硬件!");
        } else {
            Log.i(TAG, "这是第" + ThermometerParameters.HW_GENERATION + "代硬件!");
        }
        hardwareType = ThermometerParameters.HW_GENERATION;
        syncMACAddrAndBind();
    }

    private void updateConnectInfo(int status) {
        switch (status) {
            case ThermometerParameters.STATE_CONNECTED:
                stepSecondLoading.finishLoading();
                stepSecondState.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.device_binding_page_pic_device_selected, 0);
                stepThirdLoading.startLoading();
                if (null != deviceAddr) {
                    ToastUtils.show(this, getString(R.string.binding));
                }
                break;

            case ThermometerParameters.STATE_DISCONNECTED:
                stepSecondLoading.initLoading();
                stepSecondState.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.device_binding_page_pic_device_unselected, 0);
                dismissProgressNumDialog();//用于关闭更新进度条
                break;
        }
    }

    /**
     * 同步Mac地址并绑定
     */
    public void syncMACAddrAndBind() {
        checkAndBinding(deviceAddr);
    }

    private HardwareInfo hardwareInfo;

    public void checkAndBinding(final String deviceAddr) {
        Log.i(TAG, "MyDeviceVersion3Activity 准备 bindFromServer！");
        long time = System.currentTimeMillis();
        hardwareInfo = new HardwareInfo();
        hardwareInfo.setHardMacId(deviceAddr);
        hardwareInfo.setHardHardwareUuid(deviceAddr);
        hardwareInfo.setHardBindingDate(time / 1000);
        hardwareInfo.setHardHardwareVersion(firmwareVersion);
        if (hardwareType == ThermometerParameters.HW_GENERATION_LAIJIA_TXY) {
            hardwareInfo.setHardType(HardwareInfo.HARD_TYPE_TXY);
            hardwareInfo.setHardHardwareType(1);
        } else if (hardwareType == ThermometerParameters.HW_GENERATION_EWQ) {
            hardwareInfo.setHardType(HardwareInfo.HARD_TYPE_EWQ);
            hardwareInfo.setHardHardwareType(1);
        } else {
            hardwareInfo.setHardType(HardwareInfo.HARD_TYPE_THERMOMETER);
            hardwareInfo.setHardHardwareType(hardwareType);
        }
        HardwareModel.saveHardwareInfo(BindDeviceActivity.this, hardwareInfo);
        onSuccess();

    }


    public void dismissProgressNumDialog() {
        if (progressNumDialog != null && progressNumDialog.isShowing()) {
            progressNumDialog.dismiss();
            progressNumDialog = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        AppInfo.getInstance().setBindActivityActive(true);
        handleScan();
    }

    public void handleScan() {
        if (BleTools.checkBleEnable()) {
            stepFirstLoading.finishLoading();
            stepSecondLoading.startLoading();
            stepFirstState.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.device_binding_page_pic_bluetooth_selected, 0);
            Log.i(TAG, "mScanning = " + mScanning);
            if (!mScanning) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scanLeDevice(true);
                    }
                }, 1000);
            }
        } else {
            stepFirstLoading.initLoading();
            stepSecondLoading.initLoading();
            stepFirstState.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.device_binding_page_pic_bluetooth_unselected, 0);
        }
    }

    public void openLocationServer() {
        new BleAlertDialog(this).builder().setTitle(getString(R.string.open_location_hint)).setMsg(getString(R.string.locaiton_server_hint)).setPositiveButton(getString(R.string.authorize), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(locationIntent, REQUEST_CODE_LOCATION_SETTINGS);
            }
        }).show();
    }

    private boolean checkBleFeatures() {
        //Check Bluetooth Location Service
        if (!BleTools.isLocationEnable(this)) {
            openLocationServer();
            return false;
        }
        //Check Bluetooth location permission
        if (!BleTools.checkBlePermission(this)) {
            XXPermissions.with(this)
                    .permission(Permission.Group.LOCATION)
                    .request(new OnPermission() {
                        @Override
                        public void hasPermission(List<String> granted, boolean isAll) {
                            if (isAll) {
                                //do something
                            }
                        }

                        @Override
                        public void noPermission(List<String> denied, boolean quick) {
                            if (quick) {
                                new BleAlertDialog(BindDeviceActivity.this).builder().setTitle(getString(R.string.warm_prompt)).setMsg(getString(R.string.request_location_premisson)).setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                }).setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        XXPermissions.gotoPermissionSettings(BindDeviceActivity.this);
                                    }
                                }).show();

                            } else {
                                ToastUtils.show(BindDeviceActivity.this, getString(R.string.request_location_premisson));
                            }
                        }
                    });
            return false;
        }
        //Check the Bluetooth switch
        if (!BleTools.checkBleEnable()) {
//            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(intent, REQUEST_BLE_SETTINGS_CODE);
            Log.i(TAG, "不可用");
            return false;
        }
        return true;
    }

    public void scanLeDevice(final boolean enable) {
        deviceFoundMap.clear();

        // 每次重新计时, 不然每次扫描都有一个计时器加入导致混乱
        if (stopScanRunnable != null) {
            mHandler.removeCallbacks(stopScanRunnable);
        }

        if (enable) {
            if (!checkBleFeatures()) {
                return;
            }
            stopScanRunnable = new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    if (scPeripheralManager != null) {
                        scPeripheralManager.stopScan();
                        Log.i(TAG, "MyDeviceVersion3Activity mBle != null, timeout! stop scanning device!");
                    }
                }
            };

            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(stopScanRunnable, SCAN_PERIOD);

            mScanning = true;
            if (scPeripheralManager != null) {
                scPeripheralManager.startScan(new ScanResultListener() {
                    @Override
                    public void onScannerResult(List<ScPeripheral> deviceList) {
                        if (deviceList != null && !deviceList.isEmpty()) {
                            for (int i = 0; i < deviceList.size(); i++) {
                                ScPeripheral scPeripheral = deviceList.get(i);
                                if (scPeripheral.getDeviceType() == BleTools.TYPE_UNKNOWN) {
                                    continue;
                                }
                                // 防止同一个设备多次广播
                                if (deviceFoundMap.get(scPeripheral.getMacAddress()) == null) {
                                    deviceAddr = scPeripheral.getMacAddress();
                                    deviceName = scPeripheral.getDeviceName();
                                    deviceFoundMap.put(deviceAddr, true);
                                    scanLeDevice(false);
                                    Log.i(TAG, "已扫描到 device! 停止扫描!");
                                    scPeripheralManager.connectPeripheral(deviceAddr);
                                    break;
                                }
                            }
                        }
                    }
                });
            }
        } else {
            mScanning = false;
            if (scPeripheralManager != null) {
                scPeripheralManager.stopScan();
                Log.i(TAG, "MyDeviceVersion3Activity mBle != null, V3 stop scanning ...");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        AppInfo.getInstance().setBindActivityActive(false);
        unRegisterBleReceiver();
        if (scPeripheralManager != null) {
            scPeripheralManager.removeReceiveDataListener(receiveDataListenerAdapter);
        }
        if (mScanning) {
            scanLeDevice(false);
        }
        scPeripheralManager.disconnectPeripheral();
    }

    public void onSuccess() {
        Log.i(TAG, "用户绑定体温计成功");
        stepThirdLoading.finishLoading();
        stepThirdState.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.device_binding_page_pic_check_selected, 0);
        if (hardwareType == ThermometerParameters.HW_GENERATION_AKY3 || hardwareType == ThermometerParameters.HW_GENERATION_AKY4 || hardwareType == ThermometerParameters.HW_GENERATION_1 || hardwareType == ThermometerParameters.HW_GENERATION_2 || hardwareType == ThermometerParameters.HW_GENERATION_3) {
            if (hardwareType == ThermometerParameters.HW_GENERATION_3 && 3.68 > Double.parseDouble(ThermometerParameters.FW_VERSION)) {
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
        if (requestCode == REQUEST_CODE_LOCATION_SETTINGS) {
            boolean openLocationServer = BleTools.isLocationEnable(getApplicationContext());
            if (openLocationServer) {
                Log.i(TAG, "定位服务: 用户手动设置开启了定位服务");
                ToastUtils.show(getApplicationContext(),
                        getString(R.string.open_location_server_success));
            } else {
                Log.i(TAG, "定位服务: 用户手动设置未开启定位服务");
                ToastUtils.show(getApplicationContext(),
                        getString(R.string.open_locaiton_service_fail));
            }
        }
    }
}
