package com.example.bledemo.activity;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bledemo.AppInfo;
import com.example.bledemo.BaseAppActivity;
import com.example.bledemo.R;
import com.example.bledemo.ui.HomeFragment;
import com.example.bledemo.view.TopBar;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import androidx.annotation.Nullable;

/**
 * desc
 *
 * @author xiongyl 2021/1/21 21:11
 */
public class BindDeviceActivity  extends BaseAppActivity {
    public static final String TAG = BindDeviceActivity.class.getSimpleName();
    private TopBar topBar;
    public ImageView stepFirstImage;
    public ImageView stepSecondImage;
    private ImageView stepThirdState3;

    private Dialog progressNumDialog;
    public String firmwareVersion = null;
    public int hardwareType;

    private static final int CHECKBIND_SUCCESS = 0;
    private static final int CHECKBIND_FAILURE = 1;
    private static final int BIND_REPETE = 2;
    private static final int BIND_SUCCESS = 3;
    private static final int BIND_FAILURE = 4;
    public static final int SEL_WRONG_GEN = 5;
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
    private BroadcastReceiver bleReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.i(TAG,"STATE_OFF 手机蓝牙关闭");
                        stepFirstImage.setImageResource(R.drawable.personal_my_device_bind_false);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.i(TAG,"STATE_TURNING_OFF 手机蓝牙正在关闭");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.i(TAG,"STATE_ON 手机蓝牙开启");
                        stepFirstImage.setImageResource(R.drawable.personal_my_device_bind_ok);

                        // 开启后，需要一定的时间差再扫描
                        // 温度计扫描操作打开
                        (new Handler()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                scanLeDevice(true);
                            }
                        }, 500);

                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.i(TAG,"STATE_TURNING_ON 手机蓝牙正在开启");
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

        stepFirstImage = findViewById(R.id.stepFirstState3);
        stepSecondImage = findViewById(R.id.stepSecondState3);
        stepThirdState3 = findViewById(R.id.stepThirdState3);
    }
    private void initData(){

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
                        commandData.setParam1(TempConfig.isTempUnitC() ? 1 : 2);
                        scPeripheralManager.sendPeripheralCommand(macAddress, BleCommand.SYNC_THERMOMETER_UNIT, commandData);
                        scPeripheralManager.sendPeripheralCommand(macAddress, BleCommand.SYNC_TIME);
                        BLEThermometerParameters.FW_VERSION = value;
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
                            BLEThermometerParameters.FW_VERSION = "1.0";
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
                    Log.i("The device is connected " + macAddress);
                    AppInfo.getInstance().setThermometerState(true);
                    updateConnectInfo(BLEThermometerParameters.STATE_CONNECTED);
                    isWaitBinding = true;
                } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i("Device disconnected " + macAddress);
                    AppInfo.getInstance().setThermometerState(false);
                    updateConnectInfo(BLEThermometerParameters.STATE_DISCONNECTED);
                    isWaitBinding = false;
                    // 连接断开之后，紧接着继续扫描
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("连接断开后继续扫描!");
                            scanLeDevice(true);
                        }
                    });
                }
            }
        };
        scPeripheralManager.addReceiveDataListener(receiveDataListenerAdapter);
    }

    private void handleFirmwareInfo() {
        firmwareVersion = BLEThermometerParameters.FW_VERSION;
        AppInfo.getInstance().saveUserPreference(AppInfo.SHARED_PREF_FMW_VERSION, BLEThermometerParameters.FW_VERSION);
        FirmwareVersionMsg firmwareVersionMsg = new FirmwareVersionMsg();
        firmwareVersionMsg.setFirmwareVersion(BLEThermometerParameters.FW_VERSION);
        EventBus.getDefault().post(firmwareVersionMsg);

        Log.i(BLETAG + "固件版本 = " + BLEThermometerParameters.FW_VERSION);
        if (TextUtils.equals(deviceName, BLEThermometerParameters.BLE_AKY3_NAME)) {
            BLEThermometerParameters.HW_GENERATION = BLEThermometerParameters.HW_GENERATION_AKY3;
        } else if (TextUtils.equals(deviceName, BLEThermometerParameters.BLE_AKY4_NAME)) {
            BLEThermometerParameters.HW_GENERATION = BLEThermometerParameters.HW_GENERATION_AKY4;
        } else if (TextUtils.equals(deviceName, BleParam.BLE_TXY_LJ_NAME)) {
            BLEThermometerParameters.HW_GENERATION = BLEThermometerParameters.HW_GENERATION_LAIJIA_TXY;
        } else if (BleTools.getDeviceType(deviceName) == BleTools.TYPE_EWQ) {
            BLEThermometerParameters.HW_GENERATION = BLEThermometerParameters.HW_GENERATION_EWQ;
        } else if (!BLEThermometerParameters.FW_VERSION.equals("")) {
            int intPart = Integer.valueOf(BLEThermometerParameters.FW_VERSION.split("\\.")[0]);
            Log.i(BLETAG + "这是第-----" + intPart);

            if (intPart > 3) {
                BLEThermometerParameters.HW_GENERATION = BLEThermometerParameters.HW_GENERATION_3;
            } else {
                switch (intPart) {
                    case BLEThermometerParameters.HW_GENERATION_1:
                        BLEThermometerParameters.HW_GENERATION = BLEThermometerParameters.HW_GENERATION_1;
                        break;
                    case BLEThermometerParameters.HW_GENERATION_2:
                        BLEThermometerParameters.HW_GENERATION = BLEThermometerParameters.HW_GENERATION_2;
                        break;
                    case BLEThermometerParameters.HW_GENERATION_3:
                        BLEThermometerParameters.HW_GENERATION = BLEThermometerParameters.HW_GENERATION_3;
                        break;
                }
            }
        }
        if (BLEThermometerParameters.HW_GENERATION == BLEThermometerParameters.HW_GENERATION_AKY3) {
            Log.i(BLETAG + "这是安康源第3代硬件!");
        } else if (BLEThermometerParameters.HW_GENERATION == BLEThermometerParameters.HW_GENERATION_AKY4) {
            Log.i(BLETAG + "这是安康源第4代硬件!");
        } else if (BLEThermometerParameters.HW_GENERATION == BLEThermometerParameters.HW_GENERATION_LAIJIA_TXY) {
            Log.i(BLETAG + "这是莱佳胎心仪硬件!");
        } else if (BLEThermometerParameters.HW_GENERATION == BLEThermometerParameters.HW_GENERATION_EWQ) {
            Log.i(BLETAG + "这是额温枪硬件!");
        } else {
            Log.i(BLETAG + "这是第" + BLEThermometerParameters.HW_GENERATION + "代硬件!");
        }
        AppInfo.getInstance().setHardwareType(BLEThermometerParameters.HW_GENERATION);
        hardwareType = BLEThermometerParameters.HW_GENERATION;
        AppInfo.getInstance().saveUserPreference("hardwareRevision", AppInfo.getInstance().getHardwareType());
        //if(activity.typeBind.equals("bindThermometer"))
        syncMACAddrAndBind();
    }

    private void updateConnectInfo(int status) {
        switch (status) {
            case BLEThermometerParameters.STATE_CONNECTED:
                saveConnectTime();
                stepSecondImage.setImageResource(R.drawable.personal_my_device_bind_ok);
                if (null != deviceAddr) {
                    ToastUtils.show(this, getString(R.string.binding));
                }
                break;

            case BLEThermometerParameters.STATE_DISCONNECTED:
                stepSecondImage.setImageResource(R.drawable.personal_my_device_bind_false);
                dismissProgressNumDialog();//用于关闭更新进度条
                break;
        }
    }

    /**
     * 同步Mac地址并绑定
     */
    public void syncMACAddrAndBind() {
        if (InternetUtil.hasInternet()) {
            SyncMACAddressForBindMsg syncdeviceAddrForBindMsg = new SyncMACAddressForBindMsg();
            syncdeviceAddrForBindMsg.setMacAddress(deviceAddr);
            syncdeviceAddrForBindMsg.setOSType(hardwareType);
            syncdeviceAddrForBindMsg.setVersion(firmwareVersion);

            HardwareInfo hardwareInfo = new HardwareInfo();
            hardwareInfo.setHardMacId(deviceAddr);
            hardwareInfo.setHardHardwareUuid("");
            hardwareInfo.setHardBindingPlatftom(DeviceUtils.getDevicesInfo());
            hardwareInfo.setHardBindingLocation("china");
            hardwareInfo.setHardBindingDate(System.currentTimeMillis() / 1000);
            if (hardwareType == BLEThermometerParameters.HW_GENERATION_LAIJIA_TXY) {
                hardwareInfo.setHardType(HardwareInfo.HARD_TYPE_TXY);
                hardwareInfo.setHardHardwareType(1);
            } else if (hardwareType == BLEThermometerParameters.HW_GENERATION_EWQ) {
                hardwareInfo.setHardType(HardwareInfo.HARD_TYPE_EWQ);
                hardwareInfo.setHardHardwareType(1);
            } else {
                hardwareInfo.setHardType(HardwareInfo.HARD_TYPE_THERMOMETER);
                hardwareInfo.setHardHardwareType(hardwareType);
            }
            hardwareInfo.setHardHardwareVersion(firmwareVersion);
            hardwareInfo.setDeleted(0);
            hardwareInfo.setIsSynced(0);

            SyncMACAddressForBind syncdeviceAddr = new SyncMACAddressForBind(this, syncdeviceAddrForBindMsg);
            syncdeviceAddr.syncMACAddressForBindWithNetwork(hardwareInfo);
        } else {
            bindMsg.setRespCode(BIND_FAILURE);
            EventBus.getDefault().post(bindMsg);
            ToastUtils.show(this, getString(R.string.network_anomalies));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void syncMACAddressForBind(SyncMACAddressForBindMsg syncdeviceAddrForBindMsg) {
        checkAndBinding(deviceAddr);
    }

    private HardwareInfo hardwareInfo;

    public void checkAndBinding(final String deviceAddr) {
        AppLog.i("MyDeviceVersion3Activity 准备 bindFromServer！");
        long time = System.currentTimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date1 = new Date(time);
        String dateStr = format.format(date1);
        hardwareInfo = new HardwareInfo();
        hardwareInfo.setHardMacId(deviceAddr);
        hardwareInfo.setHardHardwareUuid(deviceAddr);
        hardwareInfo.setHardBindingPlatftom(DeviceUtils.getDevicesInfo());
        hardwareInfo.setHardBindingLocation("china");
        hardwareInfo.setHardBindingDate(time / 1000);
        hardwareInfo.setHardHardwareVersion(firmwareVersion);
        hardwareInfo.setDeleted(0);
        hardwareInfo.setIsSynced(0);
        if (hardwareType == BLEThermometerParameters.HW_GENERATION_LAIJIA_TXY) {
            hardwareInfo.setHardType(HardwareInfo.HARD_TYPE_TXY);
            hardwareInfo.setHardHardwareType(1);
            HardwareModel.saveHardwareInfo(hardwareInfo);
            onSuccess();
        } else if (hardwareType == BLEThermometerParameters.HW_GENERATION_EWQ) {
            hardwareInfo.setHardType(HardwareInfo.HARD_TYPE_EWQ);
            hardwareInfo.setHardHardwareType(1);
            HardwareModel.saveHardwareInfo(hardwareInfo);
            onSuccess();
        } else {
            BindingHardwareReq bindingHardwareReq = new BindingHardwareReq();
            bindingHardwareReq.setMacId(deviceAddr);
            bindingHardwareReq.setBindingPlatform(DeviceUtils.getDevicesInfo());
            bindingHardwareReq.setBindingLocation("china");
            bindingHardwareReq.setBindingDate(dateStr);
            if (hardwareType == BLEThermometerParameters.HW_GENERATION_AKY3) {
                bindingHardwareReq.setType(BLEThermometerParameters.HW_GENERATION_3);
            } else if (hardwareType == BLEThermometerParameters.HW_GENERATION_AKY4) {
                bindingHardwareReq.setType(BLEThermometerParameters.HW_GENERATION_2);
            } else {
                bindingHardwareReq.setType(hardwareType);
            }
            bindingHardwareReq.setVersion(firmwareVersion);
            bindingHardwareReq.setBound(1);

            hardwareInfo.setIsSynced(1);
            hardwareInfo.setHardType(HardwareInfo.HARD_TYPE_THERMOMETER);
            hardwareInfo.setHardHardwareType(bindingHardwareReq.getType());
            HardwareModel.updateHardwareInfo(hardwareInfo);

            BindingHardwarePresenter presenter = new BindingHardwarePresenter(this);
            presenter.onBindingHardware(bindingHardwareReq);
        }

    }

    private void saveData() {
        AppInfo.getInstance().saveUserPreference("bindState", "binded");
        dbManager.updateUserPreference(AppInfo.getInstance().getUserName(),
                "deviceType", hardwareType);
        dbManager.updateUserPreference(AppInfo.getInstance().getUserName(),
                "deviceType", firmwareVersion);
        dbManager.updateUserPreference(AppInfo.getInstance().getUserName(),
                "macAddress", deviceAddr, "isMACAddressSynced", 1);
        AppInfo.getInstance().addMacAddressList(deviceAddr);

        if (hardwareType == BLEThermometerParameters.HW_GENERATION_LAIJIA_TXY) {
            RemindResolve.getInstance(MyDeviceVersion3Activity.this).bindThermometerRemind(WXResolve.WECHAT_PUSH_BIND_TXY);
        } else {
            RemindResolve.getInstance(MyDeviceVersion3Activity.this).bindThermometerRemind(WXResolve.WECHAT_PUSH_BIND_THERMOMTER);
        }
        StatisticResolve.appUserTag(StatisticResolve.APP_USER_TAG_BIND_THERMOMETER);

        if (bindingHardwareInfo != null && bindingHardwareInfo.getData() != null) {
            AppInfo.getInstance().setVipEndTime(bindingHardwareInfo.getData().getEndTime());
            AppInfo.getInstance().setVipStatus(bindingHardwareInfo.getData().getVipStatus());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void dealRegisterMsg(BindMsg bindMsg) {
        int eventBusCode = bindMsg.getRespCode();
        switch (eventBusCode) {
            case CHECKBIND_SUCCESS:
                AppLog.i("MyDeviceVersion3Activity CHECKBIND_SUCCESS");
                break;
            case CHECKBIND_FAILURE:
                AppLog.i("MyDeviceVersion3Activity CHECKBIND_FAILURE");
                break;
            case BIND_REPETE:
                AppLog.i("MyDeviceVersion3Activity BIND_REPETE");
                ToastUtils.show(getApplicationContext(), getString(R.string.hardware_attached_unattach));
                // 继续扫描
                scanLeDevice(true);
                break;

            case BIND_FAILURE:
            case BIND_SUCCESS:
                AppLog.i("MyDeviceVersion3Activity BIND_STATE:" + (eventBusCode == BIND_SUCCESS ? "BIND_SUCCESS" : "BIND_FAILURE"));
                ToastUtils.show(getApplicationContext(), getString(R.string.attach_success));
                saveData();
                if (bindingHardwareInfo != null && bindingHardwareInfo.getData() != null) {
                    int presentMonth = bindingHardwareInfo.getData().getPresentMonth();
                    int vipStatus = bindingHardwareInfo.getData().getVipStatus();

                    if (bindingHardwareInfo.getData().isShowThermometerCourse()) {
                        AppInfo.getInstance().setCourseThermometer(1);
                        dbManager.updateUserPreference(AppInfo.getInstance().getUserName(),
                                "shecare_course_thermometer", 1);
                        RouteUtils.go(RouteUtils.ROUTE_DEVICE_BIND_RESULT);
                        if (vipStatus != 1) {
                            if (presentMonth > 0 || presentMonth == -3) {
                                EventBus.getDefault().post(new VipResultEventBus(DeviceBindVipResultActivity.RESULT_SUCCESS));
                            } else if (presentMonth == 0 || presentMonth == -1) {
                                EventBus.getDefault().post(new VipResultEventBus(DeviceBindVipResultActivity.RESULT_FAIL));
                            }
                        }
                        finish();
                        return;
                    } else {
                        if (vipStatus != 1) {
                            if (presentMonth > 0 || presentMonth == -3) {
                                EventBus.getDefault().post(new VipResultEventBus(DeviceBindVipResultActivity.RESULT_SUCCESS));
                            } else if (presentMonth == 0 || presentMonth == -1) {
                                EventBus.getDefault().post(new VipResultEventBus(DeviceBindVipResultActivity.RESULT_FAIL));
                            }
                        }
                    }
                }
                if (targetDeviceType == BleTools.TYPE_LJ_TXY) {
                    RouteUtils.go(RouteUtils.ROUTE_DEVICE_TXY_BIND_RESULT);
                    finish();
                } else {
                    RouteUtils.go(RouteUtils.ROUTE_APP_MAIN);
                }
                break;
            case SEL_WRONG_GEN:
                ToastUtils.show(getApplicationContext(), getString(R.string.attach_fail)
                        + "/n" + getString(R.string.sel_wrong_gen3));
                break;
            default:
                break;
        }
    }


    public void saveConnectTime() {
        long time = System.currentTimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm");
        Date date1 = new Date(time);
        String dateStr = format.format(date1);
        dbManager.updateUserPreference(AppInfo.getInstance().getUserName(), "lastConnectTime", dateStr);
        AppInfo.getInstance().setLastConnectTime(dateStr);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }


    public void showProgressNumDialog() {
        progressNumDialog = com.ikangtai.shecare.common.dialog.ProgressNumDialog.createLoadingDialog(this, getResources().getString(R.string.oading));
        progressNumDialog.setCancelable(false);
        progressNumDialog.show();
    }

    public void setProgressNumText(String msg) {
        com.ikangtai.shecare.common.dialog.ProgressNumDialog.setTipNumTextView(msg);
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
        AppLog.i("onResume");
//        if (!isReceiverRegistered) {
//            registerReceiver(mBleReceiver, BleService.getIntentFilter());
//            isReceiverRegistered = true;
//        }
        AppInfo.getInstance().setBindActivityActive(true);
        handleScan(null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleScan(BindLocationEventBus bindLocationEventBus) {
        if (BleTools.checkBleEnable()) {
            stepFirstImage.setImageResource(R.drawable.personal_my_device_bind_ok);
            AppLog.i("mScanning = " + mScanning);
            if (!mScanning) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scanLeDevice(true);
                    }
                }, 1000);
            }
        } else {
            stepFirstImage.setImageResource(R.drawable.personal_my_device_bind_false);
        }
    }

    private boolean checkBleFeatures() {
        //Check Bluetooth Location Service
        if (!BleTools.isLocationEnable(this)) {
            EventBus.getDefault().post(new LocationEventBus(LocationEventBus.HOME_PAGE));
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
                                new AlertDialog(MyDeviceVersion3Activity.this).builder().setTitle(getString(R.string.title_tip)).setMsg(getString(R.string.request_location_premisson)).setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                }).setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        XXPermissions.gotoPermissionSettings(MyDeviceVersion3Activity.this);
                                    }
                                }).show();

                            } else {
                                ToastUtils.show(MyDeviceVersion3Activity.this, getString(R.string.request_location_premisson));
                            }
                        }
                    });
            return false;
        }
        //Check the Bluetooth switch
        if (!BleTools.checkBleEnable()) {
//            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(intent, REQUEST_BLE_SETTINGS_CODE);
            AppLog.i(BLETAG + "不可用");
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
                        Log.i("MyDeviceVersion3Activity mBle != null, timeout! stop scanning device!");
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
                                if (deviceFoundMap.get(scPeripheral.getMacAddress()) == null && (targetDeviceType == -1 || targetDeviceType == BleTools.TYPE_LJ_TXY && TextUtils.equals(scPeripheral.getDeviceName(), BleParam.BLE_TXY_LJ_NAME))) {
                                    deviceAddr = scPeripheral.getMacAddress();
                                    deviceName = scPeripheral.getDeviceName();
                                    deviceFoundMap.put(deviceAddr, true);
                                    scanLeDevice(false);
                                    Log.i("已扫描到 device! 停止扫描!");
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
                AppLog.i("MyDeviceVersion3Activity mBle != null, V3 stop scanning ...");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppLog.i("onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
        AppLog.i("onDestroy");
        AppInfo.getInstance().setBindActivityActive(false);
        unRegisterBleReceiver();
        if (scPeripheralManager != null) {
            scPeripheralManager.removeReceiveDataListener(receiveDataListenerAdapter);
        }
        if (mScanning) {
            scanLeDevice(false);
        }
        if (AppInfo.getInstance().getStatus() != AppInfo.STATUS_HUAIYUN) {
            scPeripheralManager.disconnectPeripheral();
        }
//        if (isReceiverRegistered) {
//            unregisterReceiver(mBleReceiver);
//            isReceiverRegistered = false;
//        }

    }

    @Override
    public void showError(int errorCode) {

    }

    @Override
    public void showError() {
        AppLog.i("用户绑定体温计失败");
        bindMsg.setRespCode(BIND_FAILURE);
        EventBus.getDefault().post(bindMsg);
    }

    @Override
    public void onSuccess(BindingHardwareInfo bean) {
        bindingHardwareInfo = bean;
        AppLog.i("用户绑定体温计成功");
        bindMsg.setRespCode(BIND_SUCCESS);
        if (hardwareType == BLEThermometerParameters.HW_GENERATION_AKY3 || hardwareType == BLEThermometerParameters.HW_GENERATION_AKY4 || hardwareType == BLEThermometerParameters.HW_GENERATION_1 || hardwareType == BLEThermometerParameters.HW_GENERATION_2 || hardwareType == BLEThermometerParameters.HW_GENERATION_3) {
            stepThirdState3.setImageResource(R.drawable.personal_my_device_bind_false);
            FirmwareVersionReq firmwareVersionReq = new FirmwareVersionReq();
            firmwareVersionReq.setMacAddress(deviceAddr);
            if (hardwareType == BLEThermometerParameters.HW_GENERATION_AKY3) {
                firmwareVersionReq.setFactory(2);
                firmwareVersionReq.setFirmwareGeneration(3);
            } else if (hardwareType == BLEThermometerParameters.HW_GENERATION_AKY4) {
                firmwareVersionReq.setFactory(2);
                firmwareVersionReq.setFirmwareGeneration(4);
            } else if (hardwareType == BLEThermometerParameters.HW_GENERATION_1 || hardwareType == BLEThermometerParameters.HW_GENERATION_2 || hardwareType == BLEThermometerParameters.HW_GENERATION_3) {
                firmwareVersionReq.setFactory(1);
                firmwareVersionReq.setFirmwareGeneration(hardwareType);
            }
            CheckFirmwareVersionPresenter presenter = new CheckFirmwareVersionPresenter(new CheckFirmwareVersionContract.IView() {
                @Override
                public void showVersionError(int errorCode) {
                    stepThirdState3.setImageResource(R.drawable.personal_my_device_bind_ok);
                    EventBus.getDefault().post(bindMsg);
                }

                @Override
                public void showVersionError() {
                    stepThirdState3.setImageResource(R.drawable.personal_my_device_bind_ok);
                    EventBus.getDefault().post(bindMsg);
                }

                @Override
                public void onVersionSuccess(final FirmwareVersionResp checkInfo) {
                    stepThirdState3.setImageResource(R.drawable.personal_my_device_bind_ok);
                    if (checkInfo != null && checkInfo.getData() != null) {
                        try {
                            if (Double.parseDouble(checkInfo.getData().getVersion()) > Double.parseDouble(BLEThermometerParameters.FW_VERSION)) {
                                saveData();
                                new BasicSingleOptionDialog(MyDeviceVersion3Activity.this)
                                        .builder()
                                        .title(getString(R.string.warm_prompt))
                                        .buttonText(getString(R.string.ok))
                                        .content(getString(R.string.device_upate_tips))
                                        .setCancelable(false)
                                        .setCanceledOnTouchOutside(false)
                                        .initEvent(new BasicSingleOptionDialog.IEvent() {
                                            @Override
                                            public void clickButton() {
                                                new FirmwareUpdateDialog(MyDeviceVersion3Activity.this, hardwareInfo, checkInfo.getData()).builder().initEvent(new FirmwareUpdateDialog.IEvent() {
                                                    @Override
                                                    public void onDismiss() {
                                                        EventBus.getDefault().post(bindMsg);
                                                    }
                                                }).show();
                                            }
                                        })
                                        .show();
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    EventBus.getDefault().post(bindMsg);
                }
            });
            presenter.onCheckAppVersion(firmwareVersionReq);
        } else {
            stepThirdState3.setImageResource(R.drawable.personal_my_device_bind_ok);
            EventBus.getDefault().post(bindMsg);
        }
    }

    @Override
    public void onSuccess() {
        bindingHardwareInfo = null;
        onSuccess(null);
    }
}
