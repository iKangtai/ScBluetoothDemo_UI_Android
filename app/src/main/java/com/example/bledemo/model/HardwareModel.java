package com.example.bledemo.model;


import android.content.Context;
import android.text.TextUtils;

import com.example.bledemo.info.HardwareInfo;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;


/**
 * desc
 *
 * @author xiongyl 2020/11/16 22:44
 */
public class HardwareModel {
    public static HardwareInfo findHardwareInfo(String macAddress) {
        if (TextUtils.isEmpty(macAddress)) {
            return null;
        }
        return new HardwareInfo();
    }

    public static List<HardwareInfo> hardwareList(Context context) {
        List<HardwareInfo> hardwareInfoList = new ArrayList<>();
        HardwareInfo hardwareInfo = new HardwareInfo();
        hardwareInfo.setHardMacId("");
        hardwareInfo.setHardHardwareUuid("");
        //hardwareInfo.setHardBindingPlatftom(DeviceUtils.getDevicesInfo());
        hardwareInfo.setHardBindingLocation("china");
        hardwareInfo.setHardBindingDate(System.currentTimeMillis() / 1000);
        hardwareInfo.setHardType(HardwareInfo.HARD_TYPE_THERMOMETER);
        //hardwareInfo.setHardHardwareType(AppInfo.getInstance().getHardwareType());
        //hardwareInfo.setHardHardwareVersion(AppInfo.getInstance().getHardwareVersion());
        hardwareInfo.setDeleted(0);
        hardwareInfo.setIsSynced(1);
        HardwareModel.updateHardwareInfo(hardwareInfo);
        hardwareInfoList.add(hardwareInfo);
        return hardwareInfoList;
    }

    public static List<HardwareInfo> thermometerList(Context context) {
        List<HardwareInfo> hardwareInfoList = hardwareList(context);
        List<HardwareInfo> thermometerInfoList = new ArrayList<>();
        for (int i = 0; i < hardwareInfoList.size(); i++) {
            HardwareInfo hardwareInfo = hardwareInfoList.get(i);
            if (hardwareInfo.getHardType() == HardwareInfo.HARD_TYPE_THERMOMETER) {
                thermometerInfoList.add(hardwareInfo);
            }
        }
        return thermometerInfoList;
    }

    /**
     * 体温计列表
     *
     * @return
     */
    public static Observable<List<HardwareInfo>> obtainThermometerObservable(final Context context) {

        return Observable.create(new ObservableOnSubscribe<List<HardwareInfo>>() {
            @Override
            public void subscribe(final ObservableEmitter<List<HardwareInfo>> emitter) {
                final List<HardwareInfo> thermometerInfoList = thermometerList(context);
                if (!emitter.isDisposed()) {
                    emitter.onNext(thermometerInfoList);
                }
            }
        });
    }

    /**
     * 设备列表
     *
     * @return
     */
    public static Observable<List<HardwareInfo>> obtainHardwareObservable(final Context context) {

        return Observable.create(new ObservableOnSubscribe<List<HardwareInfo>>() {
            @Override
            public void subscribe(final ObservableEmitter<List<HardwareInfo>> emitter) {
                final List<HardwareInfo> hardwareInfoList = hardwareList(context);
                if (!emitter.isDisposed()) {
                    emitter.onNext(hardwareInfoList);
                }
            }
        });
    }

    public static void updateHardwareInfo(HardwareInfo hardwareInfo) {

    }

    public static void saveHardwareInfo(final HardwareInfo hardwareInfo) {
        HardwareModel.updateHardwareInfo(hardwareInfo);

    }
}
