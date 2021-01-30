package com.ikangtai.bluetoothui.contract;

import com.ikangtai.bluetoothui.info.TemperatureInfo;

import java.util.List;

/**
 * desc
 *
 * @author xiongyl 2021/1/30 20:14
 */
public class BleContract {
    public interface IView {
        /**
         * 接收体温计温度
         *
         * @param temperatureInfoList
         */
        void onReceiveTemperatureData(List<TemperatureInfo> temperatureInfoList);
        /**
         * 保存手动添加温度
         * @param temperatureInfo
         */
        void onSaveTemperatureData(TemperatureInfo temperatureInfo);
    }

    public interface IPresenter {

        /**
         * 添加温度
         */
        void showAddTemperatureView();

        /**
         * 接收到体温计温度
         *
         * @param temperatureInfoList
         */
        void onReceiveTemperatureData(List<TemperatureInfo> temperatureInfoList);

        /**
         * 保存手动添加温度
         * @param temperatureInfo
         */
        void onSaveTemperatureData(TemperatureInfo temperatureInfo);

        /**
         * 刷新已绑定设备列表
         */
        void refreshDeviceList();

        /**
         * 开始扫描附近设备
         */
        void startScan();

        /**
         * 停止扫描
         */
        void stopScan();

        /**
         * 释放资源
         */
        void destroy();
    }
}
