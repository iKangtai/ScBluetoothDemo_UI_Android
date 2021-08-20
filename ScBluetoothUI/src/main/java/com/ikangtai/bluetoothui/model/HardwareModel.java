package com.ikangtai.bluetoothui.model;


import android.content.Context;
import android.text.TextUtils;

import com.ikangtai.bluetoothui.info.HardwareInfo;
import com.google.gson.Gson;

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
    public static List<HardwareInfo> hardwareList(Context context) {
        List<HardwareInfo> hardwareInfoList = new ArrayList<>();
        String jsonDataStr = context.getSharedPreferences("HardwareDataPref", Context.MODE_PRIVATE).getString("HardwareData", "");
        if (!TextUtils.isEmpty(jsonDataStr)) {
            try{
                HardwareInfo hardwareInfo = new Gson().fromJson(jsonDataStr, HardwareInfo.class);
                hardwareInfoList.add(hardwareInfo);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
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
     * Bound list
     *
     * @return
     */
    public static Observable<List<HardwareInfo>> obtainDeviceObservable(final Context context) {

        return Observable.create(new ObservableOnSubscribe<List<HardwareInfo>>() {
            @Override
            public void subscribe(final ObservableEmitter<List<HardwareInfo>> emitter) {
                final List<HardwareInfo> thermometerInfoList = hardwareList(context);
                if (!emitter.isDisposed()) {
                    emitter.onNext(thermometerInfoList);
                }
            }
        });
    }

    public static void updateHardwareInfo(Context context, HardwareInfo hardwareInfo) {
        context.getSharedPreferences("HardwareDataPref", Context.MODE_PRIVATE).edit().putString("HardwareData", new Gson().toJson(hardwareInfo)).commit();
    }

    public static void saveHardwareInfo(Context context, final HardwareInfo hardwareInfo) {
        HardwareModel.updateHardwareInfo(context, hardwareInfo);
    }

    public static void deleteHardwareInfo(Context context, final HardwareInfo hardwareInfo) {
        context.getSharedPreferences("HardwareDataPref", Context.MODE_PRIVATE).edit().remove("HardwareData").commit();
    }
}
