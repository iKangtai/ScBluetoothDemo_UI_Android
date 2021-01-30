package com.ikangtai.bluetoothui.event;

/**
 * 蓝牙状态事件
 */
public class BluetoothStateEventBus {
    private boolean open;

    public BluetoothStateEventBus(boolean open) {
        this.open = open;
    }

    public boolean isOpen() {
        return open;
    }
}
