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
         * Receive thermometer temperature data
         *
         * @param temperatureInfoList
         */
        void onReceiveTemperatureData(List<TemperatureInfo> temperatureInfoList);
        /**
         * Save manually added temperature data
         * @param temperatureInfo
         */
        void onSaveTemperatureData(TemperatureInfo temperatureInfo);
    }

    public interface IPresenter {

        /**
         * Add temperature
         */
        void showAddTemperatureView();

        /**
         * Receive thermometer temperature data
         *
         * @param temperatureInfoList
         */
        void onReceiveTemperatureData(List<TemperatureInfo> temperatureInfoList);

        /**
         * Save manually added temperature
         * @param temperatureInfo
         */
        void onSaveTemperatureData(TemperatureInfo temperatureInfo);

        /**
         * Refresh the list of bound devices
         */
        void refreshDeviceList();

        /**
         * Start scanning for nearby devices
         */
        void startScan();

        /**
         * Stop scanning
         */
        void stopScan();

        /**
         * Release resources
         */
        void destroy();
    }
}
