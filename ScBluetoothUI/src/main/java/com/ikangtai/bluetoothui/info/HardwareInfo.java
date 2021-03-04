package com.ikangtai.bluetoothui.info;

import android.content.Context;
import android.text.TextUtils;

import com.ikangtai.bluetoothsdk.model.ScPeripheral;
import com.ikangtai.bluetoothsdk.util.BleTools;
import com.ikangtai.bluetoothsdk.util.LogUtils;
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
     * 硬件类型 1表示智能体温计，2表示额温枪，3表示胎心仪,4体温贴
     */
    public static final int HARD_TYPE_THERMOMETER = 1;
    public static final int HARD_TYPE_EWQ = 2;
    public static final int HARD_TYPE_TXY = 3;
    public static final int HARD_TYPE_TEM_TICK = 4;
    /**
     * 固件类型
     */
    public static final int HW_GENERATION_DEFAULT = 1;
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

    public static ScPeripheral toScPeripheral(HardwareInfo hardwareInfo) {
        ScPeripheral scPeripheral = new ScPeripheral();
        scPeripheral.setVersion(hardwareInfo.getHardwareVersion());
        scPeripheral.setMacAddress(hardwareInfo.getHardMacId());
        int hardType = hardwareInfo.getHardType();
        if (hardType == HardwareInfo.HARD_TYPE_THERMOMETER) {
            if (hardType == HardwareInfo.HW_GENERATION_1 || hardType == HardwareInfo.HW_GENERATION_2 || hardType == HardwareInfo.HW_GENERATION_3) {
                scPeripheral.setDeviceType(BleTools.TYPE_SMART_THERMOMETER);
            } else if (hardType == HardwareInfo.HW_GENERATION_AKY3) {
                scPeripheral.setDeviceType(BleTools.TYPE_AKY_3);
            } else if (hardType == HardwareInfo.HW_GENERATION_AKY4) {
                scPeripheral.setDeviceType(BleTools.TYPE_AKY_4);
            }
        } else if (hardType == HardwareInfo.HARD_TYPE_EWQ) {
            scPeripheral.setDeviceType(BleTools.TYPE_EWQ);
        } else if (hardType == HardwareInfo.HARD_TYPE_TXY) {
            scPeripheral.setDeviceType(BleTools.TYPE_LJ_TXY);
        } else if (hardType == HardwareInfo.HARD_TYPE_TEM_TICK) {
            scPeripheral.setDeviceType(BleTools.TYPE_IFEVER_TEM_TICK);
        }
        return scPeripheral;
    }


    public static HardwareInfo toHardwareInfo(ScPeripheral scPeripheral) {
        String firmwareVersion = scPeripheral.getVersion();
        int hardType = HardwareInfo.HARD_TYPE_THERMOMETER;
        int hardHardwareType = HardwareInfo.HW_GENERATION_DEFAULT;
        if (scPeripheral.getDeviceType() == BleTools.TYPE_AKY_3) {
            hardHardwareType = HardwareInfo.HW_GENERATION_AKY3;
        } else if (scPeripheral.getDeviceType() == BleTools.TYPE_AKY_4) {
            hardHardwareType = HardwareInfo.HW_GENERATION_AKY4;
        } else if (scPeripheral.getDeviceType() == BleTools.TYPE_SMART_THERMOMETER) {
            int intPart = BleTools.getDeviceHardVersion(scPeripheral.getDeviceType(), firmwareVersion);
            switch (intPart) {
                case BleTools.HW_GENERATION_1:
                    hardHardwareType = HardwareInfo.HW_GENERATION_1;
                    break;
                case BleTools.HW_GENERATION_2:
                    hardHardwareType = HardwareInfo.HW_GENERATION_2;
                    break;
                case BleTools.HW_GENERATION_3:
                    hardHardwareType = HardwareInfo.HW_GENERATION_3;
                    break;
            }
        } else if (scPeripheral.getDeviceType() == BleTools.TYPE_EWQ) {
            hardType = HardwareInfo.HARD_TYPE_EWQ;
        } else if (scPeripheral.getDeviceType() == BleTools.TYPE_IFEVER_TEM_TICK) {
            hardType = HardwareInfo.HARD_TYPE_TEM_TICK;
        } else if (scPeripheral.getDeviceType() == BleTools.TYPE_LJ_TXY) {
            hardType = HardwareInfo.HARD_TYPE_TXY;
        }
        if (hardHardwareType == HardwareInfo.HW_GENERATION_AKY3) {
            LogUtils.i("这是新款第3代硬件!");
        } else if (hardHardwareType == HardwareInfo.HW_GENERATION_AKY4) {
            LogUtils.i("这是新款第4代硬件!");
        } else {
            LogUtils.i("这是旧款硬件! :" + hardHardwareType);
        }

        long time = System.currentTimeMillis();
        HardwareInfo hardwareInfo = new HardwareInfo();
        hardwareInfo.setHardMacId(scPeripheral.getMacAddress());
        hardwareInfo.setHardBindingDate(time / 1000);
        hardwareInfo.setHardwareVersion(firmwareVersion);
        hardwareInfo.setHardType(hardType);
        hardwareInfo.setHardHardwareType(hardHardwareType);

        return hardwareInfo;
    }

}
