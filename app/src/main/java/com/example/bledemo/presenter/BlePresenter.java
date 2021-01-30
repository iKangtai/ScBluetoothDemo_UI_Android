package com.example.bledemo.presenter;

import android.app.Activity;

import com.example.bledemo.contract.BleContract;
import com.example.bledemo.info.TemperatureInfo;
import com.example.bledemo.model.BleModel;

import java.util.List;

import androidx.fragment.app.Fragment;

/**
 * desc
 *
 * @author xiongyl 2021/1/30 20:16
 */
public class BlePresenter implements BleContract.IPresenter {
    private BleModel bleModel;
    private BleContract.IView bleView;

    public BlePresenter(Activity activity, BleContract.IView bleView) {
        this.bleView = bleView;
        this.bleModel = new BleModel();
        this.bleModel.init(this,activity);
    }

    public BlePresenter(Fragment fragment, BleContract.IView bleView) {
        this.bleView = bleView;
        this.bleModel = new BleModel();
        this.bleModel.init(this,fragment);
    }

    @Override
    public void showAddTemperatureView() {
        this.bleModel.showAddTemperatureView();
    }

    @Override
    public void onReceiveTemperatureData(List<TemperatureInfo> temperatureInfoList) {
        this.bleView.onReceiveTemperatureData(temperatureInfoList);
    }

    @Override
    public void onSaveTemperatureData(TemperatureInfo temperatureInfo) {
        this.bleView.onSaveTemperatureData(temperatureInfo);
    }

    @Override
    public void refreshDeviceList() {
        this.bleModel.refreshDeviceList();
    }

    @Override
    public void startScan() {
        this.bleModel.startScan();
    }

    @Override
    public void stopScan() {
        this.bleModel.stopScan();
    }

    @Override
    public void destroy() {
        this.bleModel.destroy();
    }
}
