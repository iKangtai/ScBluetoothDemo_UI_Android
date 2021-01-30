package com.example.bledemo.util;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.View;

import com.example.bledemo.R;
import com.example.bledemo.view.dialog.BleAlertDialog;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.ikangtai.bluetoothsdk.util.BleTools;
import com.ikangtai.bluetoothsdk.util.LogUtils;
import com.ikangtai.bluetoothsdk.util.ToastUtils;

import java.util.List;

import androidx.fragment.app.Fragment;

/**
 * desc
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

    /**
     * 在扫描开始之前，您需要检查6.0以上的定位服务开关、6.0以上系统的定位权限以及蓝牙开关
     */
    private static boolean checkBleFeatures(final Activity activity, final Fragment fragment) {
        final Context context;
        if (activity != null) {
            context = activity;
        } else if (fragment != null) {
            context = fragment.getContext();
        } else {
            return false;
        }
        //Check Bluetooth Location Service
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
        //Check Bluetooth location permission
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
            LogUtils.i("蓝牙不可用");
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
                LogUtils.i("定位服务: 用户手动设置开启了定位服务");
                ToastUtils.show(context,
                        context.getString(R.string.open_location_server_success));
            } else {
                LogUtils.i("定位服务: 用户手动设置未开启定位服务");
                ToastUtils.show(context,
                        context.getString(R.string.open_locaiton_service_fail));
            }
        } else if (requestCode == REQUEST_BLE_SETTINGS_CODE) {
            boolean enable = BleTools.isLocationEnable(context);
            if (!enable) {
                LogUtils.i("蓝牙未开启");
                ToastUtils.show(context, context.getString(R.string.request_location_premisson_tips));
            } else {
                LogUtils.i("蓝牙已开启");
            }
        }
    }
}
