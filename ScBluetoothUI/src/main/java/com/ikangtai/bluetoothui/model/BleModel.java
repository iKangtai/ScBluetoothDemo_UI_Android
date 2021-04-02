package com.ikangtai.bluetoothui.model;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.app.Fragment;

import com.ikangtai.bluetoothsdk.BleCommand;
import com.ikangtai.bluetoothsdk.Config;
import com.ikangtai.bluetoothsdk.ScPeripheralManager;
import com.ikangtai.bluetoothsdk.listener.ReceiveDataListenerAdapter;
import com.ikangtai.bluetoothsdk.listener.ScanResultListener;
import com.ikangtai.bluetoothsdk.model.BleCommandData;
import com.ikangtai.bluetoothsdk.model.ScPeripheral;
import com.ikangtai.bluetoothsdk.model.ScPeripheralData;
import com.ikangtai.bluetoothsdk.util.BleTools;
import com.ikangtai.bluetoothsdk.util.FileUtil;
import com.ikangtai.bluetoothsdk.util.LogUtils;
import com.ikangtai.bluetoothsdk.util.ToastUtils;
import com.ikangtai.bluetoothui.AppInfo;
import com.ikangtai.bluetoothui.Keys;
import com.ikangtai.bluetoothui.R;
import com.ikangtai.bluetoothui.activity.DeviceConnectActivity;
import com.ikangtai.bluetoothui.contract.BleContract;
import com.ikangtai.bluetoothui.event.AutoUploadTemperatureEvent;
import com.ikangtai.bluetoothui.event.BleBindEvent;
import com.ikangtai.bluetoothui.event.BleDeviceInfoEvent;
import com.ikangtai.bluetoothui.event.BleStateEventBus;
import com.ikangtai.bluetoothui.event.BluetoothStateEventBus;
import com.ikangtai.bluetoothui.event.TemperatureBleScanEventBus;
import com.ikangtai.bluetoothui.info.HardwareInfo;
import com.ikangtai.bluetoothui.info.TemperatureInfo;
import com.ikangtai.bluetoothui.util.CheckBleFeaturesUtil;
import com.ikangtai.bluetoothui.util.DateUtil;
import com.ikangtai.bluetoothui.view.dialog.AndTemperatureDialog;
import com.ikangtai.bluetoothui.view.dialog.BuyAndBindThermometerDialog;
import com.ikangtai.bluetoothui.view.dialog.TemperatureAddDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;



/**
 * Handle the synchronized temperature of the connected thermometer
 *
 * @author xiongyl 2021/1/30 20:16
 */
public class BleModel {
    private Context context;
    private Activity activity;
    private Fragment fragment;
    private ScPeripheralManager scPeripheralManager;
    private ReceiveDataListenerAdapter receiveDataListenerAdapter;
    private boolean mScanning;
    private List<HardwareInfo> hardwareInfoList;
    private BleContract.IPresenter blePresenter;
    private ScPeripheral connectScPeripheral;
    private Handler handler;
    private Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            scanLeDevice();
        }
    };
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_OFF) {
                    LogUtils.e("Phone Bluetooth is turned off");
                    refreshBluetoothState(false);
                } else if (state == BluetoothAdapter.STATE_ON) {
                    LogUtils.e("Phone Bluetooth is turned on");
                    refreshBluetoothState(true);
                    if (AppInfo.getInstance().isOADConnectActive()) {
                        return;
                    }
                    restartScan(new TemperatureBleScanEventBus());
                }
            }
        }
    };

    /**
     * Restart scanning
     *
     * @param temperatureBleScanEventBus
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void restartScan(TemperatureBleScanEventBus temperatureBleScanEventBus) {
        stopScan();
        startScan();
    }


    /**
     * Bind or unbind trigger
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void restartScan(BleBindEvent bleBindEvent) {
        refreshDeviceList();
    }


    public void init(BleContract.IPresenter blePresenter, Activity activity) {
        init(blePresenter, activity, null);
    }

    public void init(BleContract.IPresenter blePresenter, Fragment fragment) {
        init(blePresenter, null, fragment);
    }

    private void init(BleContract.IPresenter blePresenter, Activity activity, Fragment fragment) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        this.blePresenter = blePresenter;
        this.fragment = fragment;
        this.activity = activity;
        if (activity != null) {
            context = activity;
        } else if (fragment != null) {
            context = fragment.getContext();
        }
        handler = new Handler(context.getMainLooper());
        this.initBleSdk();
        this.registerBleReceiver();
        this.refreshDeviceList();
    }

    private void registerBleReceiver() {
        //Register to receive Bluetooth switch broadcast
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        if (activity != null) {
            activity.registerReceiver(receiver, filter);
        } else if (fragment != null) {
            fragment.getActivity().registerReceiver(receiver, filter);
        }
    }

    private void initBleSdk() {
        scPeripheralManager = ScPeripheralManager.getInstance();
        String logFilePath = new File(FileUtil.createRootPath(context), "bleSdkLog.txt").getAbsolutePath();
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
        Config config = new Config.Builder().logWriter(logWriter).build();
        //sdk init
        scPeripheralManager.init(context, config);
        receiveDataListenerAdapter = new ReceiveDataListenerAdapter() {
            private long endConnBLETime;
            private long distanceTime = 5 * 60 * 1000;

            @Override
            public void onReceiveData(String macAddress, List<ScPeripheralData> scPeripheralDataList) {
                if (scPeripheralDataList != null && scPeripheralDataList.size() > 0) {
                    CopyOnWriteArrayList temperatureInfoList = new CopyOnWriteArrayList<>();
                    //处理离线温度
                    for (int i = 0; i < scPeripheralDataList.size(); i++) {
                        TemperatureInfo temperatureInfo = new TemperatureInfo();
                        temperatureInfo.setMeasureTime(DateUtil.getStringToDate(scPeripheralDataList.get(i).getDate()));
                        temperatureInfo.setTemperature(scPeripheralDataList.get(i).getTemp());
                        temperatureInfoList.add(temperatureInfo);
                    }

                    LogUtils.i("Prepare to return body temperature data>>>");
                    filterTempWithValidTime(temperatureInfoList);
                    notifyUserTemperature(temperatureInfoList);
                    if (temperatureInfoList != null
                            && temperatureInfoList.size() > 0) {
                        blePresenter.onReceiveTemperatureData(temperatureInfoList);
                        LogUtils.i("End of processing body temperature data");
                    } else {
                        LogUtils.i("End of processing body temperature data ---> The body temperature has been received but the body temperature data does not meet the specifications");
                    }
                }
            }

            @Override
            public void onReceiveError(String macAddress, int code, String msg) {
                super.onReceiveError(macAddress, code, msg);
                LogUtils.d("onReceiveError:" + code + "  " + msg);
            }

            @Override
            public void onReceiveCommandData(String macAddress, int type, int resultCode, String value) {
                super.onReceiveCommandData(macAddress, type, resultCode, value);
                LogUtils.d("onReceiveCommandData:" + type + "  " + resultCode + " " + value);
                if (resultCode == BleCommand.ResultCode.RESULT_FAIL) {
                    return;
                }
                if (AppInfo.getInstance().isOADConnectActive()) {
                    return;
                }
                switch (type) {
                    case BleCommand.GET_FIRMWARE_VERSION:
                        BleCommandData commandData = new BleCommandData();
                        commandData.setParam1(AppInfo.getInstance().isTempUnitC() ? 1 : 2);
                        scPeripheralManager.sendPeripheralCommand(macAddress, BleCommand.SYNC_THERMOMETER_UNIT, commandData);
                        if (connectScPeripheral!=null){
                            connectScPeripheral.setVersion(value);
                            EventBus.getDefault().post(new BleDeviceInfoEvent(connectScPeripheral));
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
                    LogUtils.i("The device is connected " + macAddress);
                    endConnBLETime = System.currentTimeMillis();
                    LogUtils.i("connected!");
                    refreshBleState(macAddress, true);
                } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                    LogUtils.i("Device disconnected " + macAddress);
                    long currentTime = System.currentTimeMillis();
                    if (endConnBLETime == 0 || (currentTime - endConnBLETime) > distanceTime) {
                        LogUtils.i("disconnect!");
                        endConnBLETime = currentTime;
                    }
                    refreshBleState(macAddress, false);
                    if (AppInfo.getInstance().isBindActivityActive() || !HardwareModel.hardwareList(context).isEmpty()) {
                        restartScan(new TemperatureBleScanEventBus());
                    }
                }
            }
        };
        scPeripheralManager.addReceiveDataListener(receiveDataListenerAdapter);
    }

    /**
     * Refresh Bluetooth connection status
     *
     * @param state
     */
    private void refreshBluetoothState(boolean state) {
        AppInfo.getInstance().setBluetoothState(state);
        EventBus.getDefault().post(new BluetoothStateEventBus(state));
    }

    /**
     * Refresh the thermometer connection status
     *
     * @param state
     */
    private void refreshBleState(String macAddress, boolean state) {
        AppInfo.getInstance().setThermometerState(state);
        EventBus.getDefault().post(new BleStateEventBus(macAddress, state));
    }


    private void notifyUserTemperature(CopyOnWriteArrayList temperatureInfoList) {
        if (AppInfo.getInstance().isDeviceConnectActive()) {
            if (temperatureInfoList == null || temperatureInfoList.size() == 0) {
                //No new body temperature found
                String content = context.getString(R.string.temperature_alert_1);
                String subContent = String.format(context.getString(R.string.format_font_ff7568), context.getString(R.string.warm_prompt) + ":") + context.getString(R.string.temperature_alert_2);
                EventBus.getDefault().post(new AutoUploadTemperatureEvent(content, subContent));
            } else {
                EventBus.getDefault().post(new AutoUploadTemperatureEvent(null, null));
            }
        }
    }

    public void filterTempWithValidTime(CopyOnWriteArrayList<TemperatureInfo> temperatureInfoList) {
        if (temperatureInfoList != null) {
            LogUtils.i("Start - filter invalid body temperature data");

            //Filter out data that deviates from the temperature value
            if (temperatureInfoList.size() != 0) {
                for (int i = temperatureInfoList.size() - 1; i >= 0; i--) {
                    if (temperatureInfoList.get(i).getTemperature() >= Keys.C_MAX
                            || temperatureInfoList.get(i).getTemperature() < Keys.C_MIN) {
                        temperatureInfoList.remove(i);
                    }
                }
                LogUtils.i("Filter out the data other than the temperature 32-43, and the number of remaining items: " + temperatureInfoList.size());
                if (temperatureInfoList.size() == 0) {
                    if (!AppInfo.getInstance().isBindActivityActive()) {
                        ToastUtils.show(context, context.getString(R.string.bbt_valid_value_post_fail));
                    }
                }
            }
            LogUtils.i("End - filter invalid body temperature data");
        }
    }

    public void startScan() {
        if (!mScanning) {
            handler.removeCallbacks(scanRunnable);
            handler.postDelayed(scanRunnable, 1500);
        }
    }

    public void stopScan() {
        mScanning = false;
        LogUtils.i("Stop scanning");
        handler.removeCallbacks(scanRunnable);
        try {
            scPeripheralManager.stopScan();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void scanLeDevice() {
        if (activity != null && !CheckBleFeaturesUtil.checkBleFeatures(activity)) {
            return;
        } else if (fragment != null && !CheckBleFeaturesUtil.checkBleFeatures(fragment)) {
            return;
        }
        mScanning = true;
        scPeripheralManager.startScan(new ScanResultListener() {
            @Override
            public void onScannerResult(List<ScPeripheral> deviceList) {
                if (deviceList != null && mScanning) {
                    for (int i = 0; i < deviceList.size(); i++) {
                        ScPeripheral scBluetoothDevice = deviceList.get(i);
                        if (scBluetoothDevice.getDeviceType() == BleTools.TYPE_UNKNOWN) {
                            continue;
                        }
                        //Currently only supports thermometer connection
                        if (scBluetoothDevice.getDeviceType() != BleTools.TYPE_SMART_THERMOMETER && scBluetoothDevice.getDeviceType() != BleTools.TYPE_AKY_3 && scBluetoothDevice.getDeviceType() != BleTools.TYPE_AKY_4) {
                            return;
                        }
                        if (AppInfo.getInstance().isBindActivityActive() || hardwareInfoList != null && !hardwareInfoList.isEmpty() && hardwareInfoList.get(0).getHardMacId().contains(scBluetoothDevice.getMacAddress())) {
                            connectScPeripheral = scBluetoothDevice;
                            String deviceAddr = connectScPeripheral.getMacAddress();
                            LogUtils.i("Device has been scanned! Stop scanning! " + deviceAddr);
                            stopScan();
                            LogUtils.i("Start requesting to connect to the device:" + scBluetoothDevice.macAddress);
                            scPeripheralManager.connectPeripheral(scBluetoothDevice.macAddress);
                            break;
                        }
                    }

                }
            }
        });

    }

    public void refreshDeviceList() {
        hardwareInfoList = HardwareModel.hardwareList(context);
    }

    public void showAddTemperatureView() {
        new AndTemperatureDialog(context).initEvent(new AndTemperatureDialog.IEvent() {
            @Override
            public void clickAutoSyncTemperature() {
                //Automatic upload temperature
                autoSyncTemperature();
            }

            @Override
            public void clickManualAddTemperature() {
                //Manually add temperature
                manualAddTemperature();
            }
        }).builder().show();
    }

    public void autoSyncTemperature() {
        List<HardwareInfo> hardwareInfoList = HardwareModel.hardwareList(context);
        if (hardwareInfoList.isEmpty()) {
            //The pop-up box prompts the user to buy or bind a thermometer
            new BuyAndBindThermometerDialog(context).builder().show();
        } else {
            //The thermometer has been bound, and the thermometer is connected
            context.startActivity(new Intent(context, DeviceConnectActivity.class));
        }
    }

    public void manualAddTemperature() {
        //Manually add temperature
        new TemperatureAddDialog(context).builder().initEvent(new TemperatureAddDialog.IEvent() {
            @Override
            public void onSave(TemperatureInfo temperatureInfo) {
                blePresenter.onSaveTemperatureData(temperatureInfo);
            }
        }).show();
    }

    public void destroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (activity != null) {
            activity.unregisterReceiver(receiver);
        } else if (fragment != null) {
            fragment.getActivity().unregisterReceiver(receiver);
        }
        if (mScanning) {
            stopScan();
        }
        scPeripheralManager.disconnectPeripheral();
        scPeripheralManager.removeReceiveDataListener(receiveDataListenerAdapter);
    }
}
