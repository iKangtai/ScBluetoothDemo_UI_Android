package com.example.bledemo.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bledemo.App;
import com.example.bledemo.AppInfo;
import com.example.bledemo.Keys;
import com.example.bledemo.MainActivity;
import com.example.bledemo.R;
import com.example.bledemo.activity.DeviceConnectActivity;
import com.example.bledemo.event.AutoUploadTemperatureEvent;
import com.example.bledemo.event.BleStateEventBus;
import com.example.bledemo.event.BluetoothStateEventBus;
import com.example.bledemo.event.TemperatureBleScanEventBus;
import com.example.bledemo.info.HardwareInfo;
import com.example.bledemo.info.TemperatureInfo;
import com.example.bledemo.model.HardwareModel;
import com.example.bledemo.util.CheckBleFeaturesUtil;
import com.example.bledemo.util.DateUtil;
import com.example.bledemo.util.NotificationUtil;
import com.example.bledemo.view.ActionSheetDialog;
import com.example.bledemo.view.dialog.BleAlertDialog;
import com.example.bledemo.view.dialog.BuyAndBindThermometerDialog;
import com.example.bledemo.view.dialog.TemperatureAddDialog;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {
    public static final String TAG = HomeFragment.class.getSimpleName();
    private ScPeripheralManager scPeripheralManager;
    private ReceiveDataListenerAdapter receiveDataListenerAdapter;
    private String deviceName;
    private String deviceAddr;
    private boolean mScanning;
    private List<HardwareInfo> hardwareInfoList;
    public List<TemperatureInfo> temperatureInfoList = new CopyOnWriteArrayList<>();

    private void refreshBluetoothState(boolean state) {
        AppInfo.getInstance().setBluetoothState(state);
        EventBus.getDefault().post(new BluetoothStateEventBus(state));
    }

    /**
     * 刷新BLE状态
     *
     * @param state
     */
    private void refreshBleState(boolean state) {
        if (state) {
            ToastUtils.show(getContext(), getString(R.string.thermometer_conn_success));
        } else {
            ToastUtils.show(getContext(), getString(R.string.thermometer_conn_fail));
        }
        AppInfo.getInstance().setThermometerState(state);
        EventBus.getDefault().post(new BleStateEventBus(state));
    }

    private void sendNotification(String notificationContent) {
        LogUtils.i("发送通知栏通知消息>>>");
        String title = getString(R.string.ble_temp_bg_notif_title);
        String content = notificationContent;
        Intent intent = new Intent(getContext(), MainActivity.class);
        NotificationUtil.pushMessage(getContext(), false, title, content, intent);
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

    private void notifyUserTemperature() {
        if (AppInfo.getInstance().isDeviceConnectActive()) {
            if (temperatureInfoList != null) {
                int len = temperatureInfoList.size();
                if (len > 0) {
                    EventBus.getDefault().post(new AutoUploadTemperatureEvent(null, null));
                } else {
                    //未发现新增体温
                    String content = getString(R.string.temperature_alert_1);
                    String subContent = String.format(getString(R.string.format_font_ff7568), getString(R.string.warm_prompt) + ":") + getString(R.string.temperature_alert_2);
                    EventBus.getDefault().post(new AutoUploadTemperatureEvent(content, subContent));
                }
            } else {
                //未发现新增体温
                String content = getString(R.string.temperature_alert_1);
                String subContent = String.format(getString(R.string.format_font_ff7568), getString(R.string.warm_prompt) + ":") + getString(R.string.temperature_alert_2);
                EventBus.getDefault().post(new AutoUploadTemperatureEvent(content, subContent));
            }
        }
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        root.findViewById(R.id.add_temp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ActionSheetDialog(getContext())
                        .builder()
                        .setCancelable(false)
                        .setCanceledOnTouchOutside(false)
                        .addSheetItem(getString(R.string.temp_auto_upload), ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        List<HardwareInfo> hardwareInfoList = HardwareModel.hardwareList(getContext());
                                        if (hardwareInfoList.isEmpty()) {
                                            //弹框提示用户购买或者绑定体温计
                                            new BuyAndBindThermometerDialog(getContext()).builder().show();
                                        } else {
                                            startActivity(new Intent(getContext(), DeviceConnectActivity.class));
                                        }
                                    }
                                })
                        .addSheetItem(getString(R.string.temp_add), ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        new TemperatureAddDialog(getContext()).builder().initEvent(new TemperatureAddDialog.IEvent() {
                                            @Override
                                            public void onSave(TemperatureInfo temperatureInfo) {
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
                                        }).show();
                                    }
                                }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                    }
                }).show();
            }
        });
        //Register to receive Bluetooth switch broadcast
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        getActivity().registerReceiver(receiver, filter);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initBleSdk();
    }

    private void initBleSdk() {
        scPeripheralManager = ScPeripheralManager.getInstance();
        String logFilePath = new File(FileUtil.createRootPath(getContext()), "log.txt").getAbsolutePath();
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
        scPeripheralManager.init(getContext(), config);
        receiveDataListenerAdapter = new ReceiveDataListenerAdapter() {
            private long endConnBLETime;
            private long distanceTime = 5 * 60 * 1000;

            @Override
            public void onReceiveData(String macAddress, List<ScPeripheralData> scPeripheralDataList) {
                if (scPeripheralDataList != null && scPeripheralDataList.size() > 0) {
                    if (temperatureInfoList == null) {
                        temperatureInfoList = new CopyOnWriteArrayList<>();
                    }
                    //处理离线温度
                    for (int i = 0; i < scPeripheralDataList.size(); i++) {
                        TemperatureInfo temperatureInfo = new TemperatureInfo();
                        temperatureInfo.setMeasureTime(DateUtil.getStringToDate(scPeripheralDataList.get(i).getDate()));
                        temperatureInfo.setTemperature(scPeripheralDataList.get(i).getTemp());
                        temperatureInfoList.add(temperatureInfo);
                    }

                    LogUtils.i("准备上传体温数据>>>");
                    filterTempWithValidTime();

                    if (temperatureInfoList != null
                            && temperatureInfoList.size() > 0) {
                        notifyUserTemperature();
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
                            sendNotification(getNotificationContent(notifyTempNum, notifyTempValue));
                        }
                        LogUtils.i("处理体温数据结束");
                    } else {
                        LogUtils.i("处理体温数据结束--->收到过体温但体温数据不符合规范");
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
                switch (type) {
                    case BleCommand.GET_FIRMWARE_VERSION:
                        BleCommandData commandData = new BleCommandData();
                        commandData.setParam1(AppInfo.getInstance().isTempUnitC() ? 1 : 2);
                        scPeripheralManager.sendPeripheralCommand(macAddress, BleCommand.SYNC_THERMOMETER_UNIT, commandData);
                        scPeripheralManager.sendPeripheralCommand(macAddress, BleCommand.GET_POWER);
                        break;
                }
            }

            @Override
            public void onConnectionStateChange(String macAddress, int state) {
                super.onConnectionStateChange(macAddress, state);
                if (state == BluetoothProfile.STATE_CONNECTED) {
                    LogUtils.i("The device is connected " + macAddress);
                    deviceAddr = macAddress;
                    endConnBLETime = System.currentTimeMillis();
                    LogUtils.i("已连接!");
                    refreshBleState(true);
                } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                    LogUtils.i("Device disconnected " + macAddress);
                    long currentTime = System.currentTimeMillis();
                    if (endConnBLETime == 0 || (currentTime - endConnBLETime) > distanceTime) {
                        LogUtils.i("断开连接!");
                        endConnBLETime = currentTime;
                    }
                    refreshBleState(false);
                    if (!HardwareModel.hardwareList(getContext()).isEmpty()) {
                        EventBus.getDefault().post(new TemperatureBleScanEventBus());
                    }
                    deviceName = null;
                    deviceAddr = null;
                }
            }
        };
        scPeripheralManager.addReceiveDataListener(receiveDataListenerAdapter);
    }

    public void filterTempWithValidTime() {
        if (temperatureInfoList != null) {
            LogUtils.i("开始--过滤无效体温数据");

            //过滤掉温度值偏离的数据
            if (temperatureInfoList.size() != 0) {
                for (int i = temperatureInfoList.size() - 1; i >= 0; i--) {
                    if (temperatureInfoList.get(i).getTemperature() >= Keys.C_MAX
                            || temperatureInfoList.get(i).getTemperature() < Keys.C_MIN) {
                        temperatureInfoList.remove(i);
                    }
                }
                LogUtils.i("过滤掉温度32-43以外的数据,剩下条数: " + temperatureInfoList.size());
                if (temperatureInfoList.size() == 0) {
                    if (!AppInfo.getInstance().isBindActivityActive()) {
                        ToastUtils.show(getContext(), getString(R.string.bbt_valid_value_post_fail));
                    }
                }
            }
            LogUtils.i("结束--过滤无效体温数据");
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_OFF) {
                    LogUtils.e("手机蓝牙开启");
                    refreshBluetoothState(false);
                } else if (state == BluetoothAdapter.STATE_ON) {
                    LogUtils.e("手机蓝牙开启");
                    refreshBluetoothState(true);
                    EventBus.getDefault().post(new TemperatureBleScanEventBus());
                }
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void restartScan(TemperatureBleScanEventBus temperatureBleScanEventBus) {
        stopScan();
        startScan();
    }

    private void startScan() {
        if (!mScanning) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanLeDevice();
                }
            }, 1500);
        }
    }

    public void stopScan() {
        mScanning = false;
        LogUtils.i("停止扫描");
        try {
            scPeripheralManager.stopScan();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void scanLeDevice() {
        if (!CheckBleFeaturesUtil.checkBleFeatures(this)) {
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
                        if (hardwareInfoList != null && !hardwareInfoList.isEmpty() && hardwareInfoList.get(0).getHardMacId().contains(scBluetoothDevice.getMacAddress())) {
                            deviceAddr = scBluetoothDevice.getMacAddress();
                            deviceName = scBluetoothDevice.getDeviceName();
                            LogUtils.i("已扫描到 device! 停止扫描! " + deviceAddr);
                            stopScan();
                            LogUtils.i("开始请求连接设备:" + scBluetoothDevice.macAddress);
                            scPeripheralManager.connectPeripheral(scBluetoothDevice.macAddress);
                            break;
                        }
                    }

                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CheckBleFeaturesUtil.handBleFeaturesResult(getContext(), requestCode, resultCode);
    }

    @Override
    public void onResume() {
        super.onResume();
        hardwareInfoList = HardwareModel.hardwareList(getContext());
        if (!AppInfo.getInstance().isThermometerState()) {
            startScan();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        getActivity().unregisterReceiver(receiver);
        if (mScanning) {
            stopScan();
        }
        scPeripheralManager.disconnectPeripheral(deviceAddr);
    }
}