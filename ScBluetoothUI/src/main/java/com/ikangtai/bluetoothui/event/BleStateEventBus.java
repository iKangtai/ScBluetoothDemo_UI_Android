package com.ikangtai.bluetoothui.event;

/**
 * 体温计连接状态事件
 */
public class BleStateEventBus {
    private boolean connect;
    private String deviceAddress;

    public BleStateEventBus(String deviceAddress, boolean connect) {
        this.connect = connect;
        this.deviceAddress = deviceAddress;
    }


    public boolean isConnect() {
        return connect;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }
}
