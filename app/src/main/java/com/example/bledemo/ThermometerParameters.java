package com.example.bledemo;

/**
 * 蓝牙常量
 *
 * @author xiongyl 2021/1/21 21:48
 */
public class ThermometerParameters {
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTED = 1;

    public static String FW_VERSION;
    public static int HW_GENERATION = 2;
    public static final int HW_GENERATION_1 = 1;
    public static final int HW_GENERATION_2 = 2;
    public static final int HW_GENERATION_3 = 3;
    public static final int HW_GENERATION_4 = 4;

    public static final int HW_GENERATION_EWQ = 1001;
    public static final int HW_GENERATION_AKY3 = 2003;
    public static final int HW_GENERATION_AKY4 = 2004;
    public static final int HW_GENERATION_LAIJIA_TXY = 3001;

    //安康源相关
    public static final String BLE_AKY3_NAME = "yuncheng_a33";
    public static final String BLE_AKY4_NAME = "YC-K399B";
}
