package com.example.bledemo.event;

/**
 * BLE连接状态事件
 */
public class BleStateEventBus {
    private boolean connect;

    public BleStateEventBus(boolean connect) {
        this.connect = connect;
    }


    public boolean isConnect() {
        return connect;
    }
}
