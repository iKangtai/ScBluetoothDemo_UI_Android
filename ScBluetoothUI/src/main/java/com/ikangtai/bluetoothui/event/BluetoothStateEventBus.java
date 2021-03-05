package com.ikangtai.bluetoothui.event;

/**
 * Bluetooth status event
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
