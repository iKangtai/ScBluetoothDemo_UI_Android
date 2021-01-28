package com.example.bledemo;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * desc
 *
 * @author xiongyl 2021/1/21 22:10
 */
public class AppInfo {
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

    /**
     * temp 输入源必须是摄氏度单位, 如果是华摄氏度源数据，则不能使用此方法
     *
     * @param temp
     * @return
     */
    public float getTemp(float temp) {

        if (isTempUnitC()) {
            return temp;
        } else {
            DecimalFormat decimalFormat = new DecimalFormat(".000");
            DecimalFormatSymbols dfs = new DecimalFormatSymbols();
            dfs.setDecimalSeparator('.');
            decimalFormat.setDecimalFormatSymbols(dfs);
            String tempF = decimalFormat.format(1.8 * temp + 32);
            return Float.valueOf(tempF.substring(0, tempF.length() - 1));
        }
    }

    public String getTempUnit() {
        return isTempUnitC() ? Keys.kTempUnitC : Keys.kTempUnitF;
    }
}
