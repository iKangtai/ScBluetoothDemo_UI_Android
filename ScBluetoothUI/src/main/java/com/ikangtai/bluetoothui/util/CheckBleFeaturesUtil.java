package com.ikangtai.bluetoothui.util;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.View;

import com.ikangtai.bluetoothui.R;
import com.ikangtai.bluetoothui.view.dialog.BleAlertDialog;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.ikangtai.bluetoothsdk.util.BleTools;
import com.ikangtai.bluetoothsdk.util.LogUtils;
import com.ikangtai.bluetoothsdk.util.ToastUtils;

import java.util.List;

import androidx.fragment.app.Fragment;

/**
 * Before the scan starts, you need to check the location service switch above 6.0, the location permission of the system above 6.0, and the Bluetooth switch
 *
 * @author xiongyl 2021/1/30 14:36
 */
public class CheckBleFeaturesUtil {
    public final static int REQUEST_LOCATION_SETTINGS = 1000;
    public final static int REQUEST_BLE_SETTINGS_CODE = 1001;

    public static boolean checkBleFeatures(Activity activity) {
        return checkBleFeatures(activity, null);
    }

    public static boolean checkBleFeatures(Fragment fragment) {
        return checkBleFeatures(null, fragment);
    }

    private static boolean checkBleFeatures(final Activity activity, final Fragment fragment) {
        final Context context;
        if (activity != null) {
            context = activity;
        } else if (fragment != null) {
            context = fragment.getContext();
        } else {
            return false;
        }
        //Check system location service
        if (!BleTools.isLocationEnable(context)) {
            new BleAlertDialog(context).builder().setTitle(context.getString(R.string.open_location_hint)).setMsg(context.getString(R.string.locaiton_server_hint)).setPositiveButton(context.getString(R.string.authorize), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    if (activity != null) {
                        activity.startActivityForResult(locationIntent, REQUEST_LOCATION_SETTINGS);
                    } else if (fragment != null) {
                        fragment.startActivityForResult(locationIntent, REQUEST_LOCATION_SETTINGS);
                    }
                }
            }).setNegativeButton(context.getString(R.string.cancel), new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            }).show();
            return false;
        }
        //Check the location permissions required to scan nearby devices
        if (!BleTools.checkBlePermission(context)) {
            XXPermissions.with(activity != null ? activity : fragment.getActivity())
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
                                new BleAlertDialog(context).builder().setTitle(context.getString(R.string.warm_prompt)).setMsg(context.getString(R.string.request_location_premisson)).setNegativeButton(context.getString(R.string.cancel), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                }).setPositiveButton(context.getString(R.string.ok), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        XXPermissions.gotoPermissionSettings(context);
                                    }
                                }).show();

                            } else {
                                ToastUtils.show(context, context.getString(R.string.request_location_premisson));
                            }
                        }
                    });
            return false;
        }
        //Check the Bluetooth switch
        if (!BleTools.checkBleEnable()) {
            LogUtils.i("Bluetooth is not available");
            new BleAlertDialog(context).builder().setTitle(context.getString(R.string.warm_prompt)).setMsg(context.getString(R.string.request_location_premisson_tips)).setNegativeButton(context.getString(R.string.cancel), new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            }).setPositiveButton(context.getString(R.string.ok), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    if (activity != null) {
                        activity.startActivityForResult(intent, REQUEST_BLE_SETTINGS_CODE);
                    } else if (fragment != null) {
                        fragment.startActivityForResult(intent, REQUEST_BLE_SETTINGS_CODE);
                    }
                }
            }).show();
            return false;
        }
        return true;
    }

    public static void handBleFeaturesResult(Context context, int requestCode, int resultCode) {
        if (requestCode == REQUEST_LOCATION_SETTINGS) {
            boolean openLocationServer = BleTools.isLocationEnable(context);
            if (openLocationServer) {
                LogUtils.i("Location service: User manually set to enable location service");
                ToastUtils.show(context,
                        context.getString(R.string.open_location_server_success));
            } else {
                LogUtils.i("Location service: The user manually sets the location service to be disabled");
                ToastUtils.show(context,
                        context.getString(R.string.open_locaiton_service_fail));
            }
        } else if (requestCode == REQUEST_BLE_SETTINGS_CODE) {
            boolean enable = BleTools.isLocationEnable(context);
            if (!enable) {
                LogUtils.i("Bluetooth is not turned on");
                ToastUtils.show(context, context.getString(R.string.request_location_premisson_tips));
            } else {
                LogUtils.i("Bluetooth is on");
            }
        }
    }
}
