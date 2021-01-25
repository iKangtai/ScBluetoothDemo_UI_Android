package com.example.bledemo;

/**
 * desc
 *
 * @author xiongyl 2021/1/21 22:10
 */
public class AppInfo {
    public static final String kTempUnitC = "ºC";
    public static final String kTempUnitF = "ºF";
    /**
     * 设备连接界面
     */
    private boolean isDeviceConnectActive = false;

    /**
     * OAD连接界面
     */
    private boolean isOADConnectActive = false;
    /**
     * 蓝牙状态开/闭
     */
    private boolean bluetoothState;
    /**
     * 体温计状态开/闭
     */
    private boolean thermometerState;
    /**
     * 是否处于绑定页面
     */
    private boolean isBindActivityActive = false;
    private static volatile AppInfo instance;

    private AppInfo() {
    }

    public static AppInfo getInstance() {
        if (instance == null) {
            synchronized (AppInfo.class) {
                if (instance == null) {
                    instance = new AppInfo();
                }
            }
        }

        return instance;
    }

    public boolean isBindActivityActive() {
        return isBindActivityActive;
    }

    public void setBindActivityActive(boolean bindActivityActive) {
        isBindActivityActive = bindActivityActive;
    }

    public void setDeviceConnectActive(boolean deviceConnectActive) {
        isDeviceConnectActive = deviceConnectActive;
    }

    public boolean isDeviceConnectActive() {
        return isDeviceConnectActive;
    }

    public void setOADConnectActive(boolean OADConnectActive) {
        isOADConnectActive = OADConnectActive;
    }

    public boolean isOADConnectActive() {
        return isOADConnectActive;
    }

    public boolean isBluetoothState() {
        return bluetoothState;
    }

    public void setBluetoothState(boolean bluetoothState) {
        this.bluetoothState = bluetoothState;
    }

    public boolean isThermometerState() {
        return thermometerState;
    }

    public void setThermometerState(boolean thermometerState) {
        this.thermometerState = thermometerState;
    }

    public boolean isTempUnitC() {
        return true;
    }
}
