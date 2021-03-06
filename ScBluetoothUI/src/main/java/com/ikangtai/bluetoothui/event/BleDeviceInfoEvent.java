package com.ikangtai.bluetoothui.event;

import com.ikangtai.bluetoothsdk.model.ScPeripheral;

/**
 * Get connected device information
 *
 * @author xiongyl 2021/1/30 22:41
 */
public class BleDeviceInfoEvent {
    private ScPeripheral connectScPeripheral;

    public BleDeviceInfoEvent(ScPeripheral connectScPeripheral) {
        this.connectScPeripheral = connectScPeripheral;
    }

    public ScPeripheral getConnectScPeripheral() {
        return connectScPeripheral;
    }

    public void setConnectScPeripheral(ScPeripheral connectScPeripheral) {
        this.connectScPeripheral = connectScPeripheral;
    }
}
