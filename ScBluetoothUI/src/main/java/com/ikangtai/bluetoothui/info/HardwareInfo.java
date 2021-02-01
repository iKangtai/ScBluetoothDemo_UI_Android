package com.ikangtai.bluetoothui.info;

import android.content.Context;
import android.text.TextUtils;

import com.ikangtai.bluetoothui.R;

/**
 * desc
 *
 * @author xiongyl 2021/1/21 21:37
 */
public class HardwareInfo {
    /*"hardMacId": "18:93:D7:08:3F:67",
    "hardUserId": 79410,
    "hardBindingLocation": "上海",
    "hardBindingPlatftom": "iPhone 7 (A1660/A1779/A1780) 14.0.1",
    "hardBindingDate": 12321312321,
    "hardHardwareType": 1,
    "hardHardwareVersion": "10.2",
    "hardHardwareUuid": "",
    "hardType": 3,
    "gmtCreateTime": 1605063990,
    "gmtUpdateTime": 1605064004*/
    /**
     * 硬件类型 1表示体温计，4表示额温枪，5表示胎心仪
     */
    public static final int HARD_TYPE_THERMOMETER = 1;
    public static final int HARD_TYPE_EWQ = 2;
    public static final int HARD_TYPE_TXY = 3;
    /**
     * 固件类型
     */
    /**
     * 旧版1、2、3代体温计
     */
    public static final int HW_GENERATION_1 = 1001;
    public static final int HW_GENERATION_2 = 1002;
    public static final int HW_GENERATION_3 = 1003;
    /**
     * 新款三四代体温计
     */
    public static final int HW_GENERATION_AKY3 = 2003;
    public static final int HW_GENERATION_AKY4 = 2004;

    private int deviceLogo;
    private String deviceName;
    private String hardMacId;
    private long hardBindingDate;
    private int hardHardwareType;
    private String hardwareVersion;
    private int hardType;
    private long gmtCreateTime = System.currentTimeMillis() / 1000;
    private long gmtUpdateTime = System.currentTimeMillis() / 1000;

    public String getHardMacId() {
        return hardMacId;
    }

    public void setHardMacId(String hardMacId) {
        this.hardMacId = hardMacId;
    }

    public long getHardBindingDate() {
        return hardBindingDate;
    }

    public void setHardBindingDate(long hardBindingDate) {
        this.hardBindingDate = hardBindingDate;
    }

    public int getHardHardwareType() {
        return hardHardwareType;
    }

    public void setHardHardwareType(int hardHardwareType) {
        this.hardHardwareType = hardHardwareType;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    public long getGmtCreateTime() {
        return gmtCreateTime;
    }

    public void setGmtCreateTime(long gmtCreateTime) {
        this.gmtCreateTime = gmtCreateTime;
    }

    public long getGmtUpdateTime() {
        return gmtUpdateTime;
    }

    public void setGmtUpdateTime(long gmtUpdateTime) {
        this.gmtUpdateTime = gmtUpdateTime;
    }

    public int getHardType() {
        return hardType;
    }

    public void setHardType(int hardType) {
        this.hardType = hardType;
    }

    public String getDeviceName(Context context) {
        if (TextUtils.isEmpty(deviceName)) {
            switch (hardType) {
                case HARD_TYPE_THERMOMETER:
                    deviceName = context.getString(R.string.shecare_thermometer);
                    break;
                case HARD_TYPE_EWQ:
                    deviceName = context.getString(R.string.shecare_ewq);
                    break;
                case HARD_TYPE_TXY:
                    deviceName = context.getString(R.string.shecare_txy);
                    break;
            }
        }
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setDeviceLogo(int deviceLogo) {
        this.deviceLogo = deviceLogo;
    }

    public int getDeviceLogo() {
        if (deviceLogo == 0) {
            switch (hardType) {
                case HARD_TYPE_THERMOMETER:
                    if (hardHardwareType == HardwareInfo.HW_GENERATION_2 || hardHardwareType == HardwareInfo.HW_GENERATION_AKY4) {
                        deviceLogo = R.drawable.a21;
                    } else if (hardHardwareType == HardwareInfo.HW_GENERATION_3 || hardHardwareType == HardwareInfo.HW_GENERATION_AKY3) {
                        deviceLogo = R.drawable.a31;
                    }
                    break;
                case HARD_TYPE_EWQ:
                    deviceLogo = R.drawable.a31;
                    break;
                case HARD_TYPE_TXY:
                    deviceLogo = R.drawable.device_txy_fd100a;
                    break;
            }
        }
        return deviceLogo;
    }
}