package com.ikangtai.bluetoothui.event;

import com.ikangtai.bluetoothsdk.model.ScPeripheral;

/**
 * 获取连接设备信息
 *
 * @author xiongyl 2021/1/30 22:41
 */
public class BleDeviceInfoEvent {
    private ScPeripheral connectScPeripheral;
    private String version;

    public BleDeviceInfoEvent(ScPeripheral connectScPeripheral, String version) {
        this.connectScPeripheral = connectScPeripheral;
        this.version = version;
    }

    public ScPeripheral getConnectScPeripheral() {
        return connectScPeripheral;
    }

    public void setConnectScPeripheral(ScPeripheral connectScPeripheral) {
        this.connectScPeripheral = connectScPeripheral;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
