package com.example.bledemo.ui;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bledemo.Keys;
import com.example.bledemo.R;
import com.example.bledemo.activity.BindDeviceActivity;
import com.example.bledemo.activity.DeviceConnectActivity;
import com.example.bledemo.info.HardwareInfo;
import com.example.bledemo.info.TemperatureInfo;
import com.example.bledemo.model.HardwareModel;
import com.example.bledemo.util.DateUtil;
import com.example.bledemo.view.ActionSheetDialog;
import com.example.bledemo.view.dialog.BleAlertDialog;
import com.example.bledemo.view.dialog.TemperatureAddDialog;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.ikangtai.bluetoothsdk.Config;
import com.ikangtai.bluetoothsdk.ScPeripheralManager;
import com.ikangtai.bluetoothsdk.util.BleTools;
import com.ikangtai.bluetoothsdk.util.FileUtil;
import com.ikangtai.bluetoothsdk.util.ToastUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {
    public static final String TAG = HomeFragment.class.getSimpleName();
    public final static int REQUEST_LOCATION_SETTINGS = 1000;
    public final static int REQUEST_BLE_SETTINGS_CODE = 1001;
    private ScPeripheralManager scPeripheralManager;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        root.findViewById(R.id.image_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ActionSheetDialog(getContext())
                        .builder()
                        .setCancelable(false)
                        .setCanceledOnTouchOutside(false)
                        .addSheetItem("体温计自动上传", ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        if (!checkBleFeatures()) {
                                            return;
                                        }
                                        List<HardwareInfo> hardwareInfoList=HardwareModel.hardwareList(getContext());
                                        if (hardwareInfoList.isEmpty()){
                                            startActivity(new Intent(getContext(), BindDeviceActivity.class));
                                        }else {
                                            startActivity(new Intent(getContext(), DeviceConnectActivity.class));
                                        }
                                    }
                                })
                        .addSheetItem("手动输入", ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        new TemperatureAddDialog(getContext()).builder().initEvent(new TemperatureAddDialog.IEvent() {
                                            @Override
                                            public void onSave(TemperatureInfo temperatureInfo) {
                                                new BleAlertDialog(getContext()).builder()
                                                        .setTitle("温度保存成功")
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
    }

    /**
     * Before the scan starts, you need to check the positioning service switch above 6.0, the positioning authority of the system above 6.0, and the Bluetooth switch
     *
     * @return
     */
    private boolean checkBleFeatures() {
        //Check Bluetooth Location Service
        if (!BleTools.isLocationEnable(getContext())) {
            Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(locationIntent, REQUEST_LOCATION_SETTINGS);
            return false;
        }
        //Check Bluetooth location permission
        if (!BleTools.checkBlePermission(getContext())) {
            XXPermissions.with(getActivity())
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
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setTitle(R.string.warm_prompt)
                                        .setMessage(R.string.request_location_premisson).setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                XXPermissions.gotoPermissionSettings(getContext());
                                            }
                                        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        });
                                builder.create().show();

                            } else {
                                showMessage(getString(R.string.request_location_premisson));
                            }
                        }
                    });
            return false;
        }
        //Check the Bluetooth switch
        if (!BleTools.checkBleEnable()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_BLE_SETTINGS_CODE);
            return false;
        }
        return true;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_OFF) {
                    Log.e(TAG, "Bluetooth is off");
                    showMessage("Bluetooth off");
                } else if (state == BluetoothAdapter.STATE_ON) {
                    Log.e(TAG, "Bluetooth is on");
                    showMessage("Bluetooth is on");
                }
            }
        }

    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOCATION_SETTINGS) {
            boolean openLocationServer = BleTools.isLocationEnable(getContext());
            if (openLocationServer) {
                Log.e(TAG, "Location service: The user manually sets the location service");
                showMessage(getString(R.string.location_service_turn_on));
            } else {
                Log.e(TAG, "Location service: The user manually set the location service is not enabled");
                showMessage(getString(R.string.location_service_turn_off));
            }
        } else if (requestCode == REQUEST_BLE_SETTINGS_CODE) {
            boolean enable = BleTools.isLocationEnable(getContext());
            if (!enable) {
                showMessage(getString(R.string.request_location_premisson_tips));
            }
        }
    }

    private void showMessage(String massage) {
        ToastUtils.show(getContext(), massage);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(receiver);
    }
}