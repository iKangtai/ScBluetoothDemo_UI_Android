package com.ikangtai.bluetoothui.activity;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ikangtai.bluetoothsdk.ScPeripheralManager;
import com.ikangtai.bluetoothsdk.util.LogUtils;
import com.ikangtai.bluetoothsdk.util.ToastUtils;
import com.ikangtai.bluetoothui.AppInfo;
import com.ikangtai.bluetoothui.R;
import com.ikangtai.bluetoothui.event.BleBindEvent;
import com.ikangtai.bluetoothui.info.FirmwareVersionResp;
import com.ikangtai.bluetoothui.info.HardwareInfo;
import com.ikangtai.bluetoothui.model.HardwareModel;
import com.ikangtai.bluetoothui.view.TopBar;
import com.ikangtai.bluetoothui.view.dialog.BleAlertDialog;
import com.ikangtai.bluetoothui.view.dialog.FirmwareUpdateDialog;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 我的设备
 *
 * @author xiongyl 2021/1/21 21:11
 */
public class MyDeviceActivity extends AppCompatActivity implements View.OnClickListener {

    private TopBar topBar;
    private Button unbindDevice;
    private TextView deviceUpgrade;
    private HardwareInfo hardwareInfo;
    public ImageView deviceLogo;
    public TextView deviceName;
    public TextView deviceMacAddress;
    public TextView deviceOadVesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.i("进入我的设备");
        setContentView(R.layout.activity_my_device);
        initView();
    }

    private void initView() {
        topBar = findViewById(R.id.topBar);
        topBar.setOnTopBarClickListener(new TopBar.OnTopBarClickListener() {
            @Override
            public void leftClick() {
                finish();
            }

            @Override
            public void midLeftClick() {

            }

            @Override
            public void midRightClick() {

            }

            @Override
            public void rightClick() {

            }
        });

        deviceName = findViewById(R.id.deviceName);
        deviceLogo = findViewById(R.id.deviceLogo);
        deviceMacAddress = findViewById(R.id.deviceMacAddress);
        deviceOadVesion = findViewById(R.id.deviceOadVesion);
        unbindDevice = findViewById(R.id.unbind_device);
        unbindDevice.setOnClickListener(this);
        deviceUpgrade = findViewById(R.id.device_upgrade);
        deviceUpgrade.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        deviceUpgrade.getPaint().setAntiAlias(true);//抗锯齿
        deviceUpgrade.setOnClickListener(this);

        loadData();
    }


    private void loadData() {
        List<HardwareInfo> hardwareInfoList = HardwareModel.hardwareList(this);
        if (!hardwareInfoList.isEmpty()) {
            hardwareInfo = hardwareInfoList.get(0);
            deviceLogo.setImageResource(hardwareInfo.getDeviceLogo());
            deviceName.setText(hardwareInfo.getDeviceName(MyDeviceActivity.this));
            deviceMacAddress.setText(getString(R.string.macAddress)
                    + hardwareInfo.getHardMacId());
            deviceOadVesion.setText(getString(R.string.deviceVersion)
                    + hardwareInfo.getHardwareVersion());
            if (hardwareInfo.getHardType() != HardwareInfo.HARD_TYPE_THERMOMETER) {
                deviceUpgrade.setVisibility(View.GONE);
            } else {
                deviceUpgrade.setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.device_upgrade) {
            //模拟需要固件升级
            //傅达康三代 {"code":200,"message":"Success","data":{"fileUrl":"{\"A\":\"http://yunchengfile.oss-cn-beijing.aliyuncs.com/firmware/A31/Athermometer.bin\",\"B\":\"http://yunchengfile.oss-cn-beijing.aliyuncs.com/firmware/A31/Bthermometer.bin\"}\r\n","version":"3.68","type":1}}
            //安康源三代四代  {"code":200,"message":"Success","data":{"fileUrl":"http://yunchengfile.oss-cn-beijing.aliyuncs.com/firmware/A31/Bthermometer.bin","version":"6.1","type":2}}
            String jsonData = "{\"code\":200,\"message\":\"Success\",\"data\":{\"fileUrl\":\"{\\\"A\\\":\\\"http://yunchengfile.oss-cn-beijing.aliyuncs.com/firmware/A31/Athermometer.bin\\\",\\\"B\\\":\\\"http://yunchengfile.oss-cn-beijing.aliyuncs.com/firmware/A31/Bthermometer.bin\\\"}\\r\\n\",\"version\":\"3.68\",\"type\":1}}";
            final FirmwareVersionResp firmwareVersionResp = new Gson().fromJson(jsonData, FirmwareVersionResp.class);
            if (hardwareInfo != null && hardwareInfo.getHardType() == HardwareInfo.HARD_TYPE_THERMOMETER && AppInfo.getInstance().getServerHardwareVersion() > Double.parseDouble(hardwareInfo.getHardwareVersion())) {
                new BleAlertDialog(MyDeviceActivity.this).builder()
                        .setTitle(getString(R.string.warm_prompt))
                        .setMsg(getString(R.string.device_upate_tips))
                        .setCancelable(false)
                        .setCanceledOnTouchOutside(false)
                        .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new FirmwareUpdateDialog(MyDeviceActivity.this, hardwareInfo, firmwareVersionResp.getData()).builder().initEvent(new FirmwareUpdateDialog.IEvent() {
                                    @Override
                                    public void onDismiss() {

                                    }
                                }).show();
                            }
                        }).show();
            } else {
                new BleAlertDialog(MyDeviceActivity.this).builder()
                        .setTitle(getString(R.string.warm_prompt))
                        .setMsg(String.format(getString(R.string.already_latest_ver),
                                hardwareInfo.getHardwareVersion()))
                        .setCancelable(false)
                        .setCanceledOnTouchOutside(false)
                        .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }).show();
            }
        } else if (v.getId() == R.id.unbind_device) {
            if (ClickFilter.filter()) {
                return;
            }
            HardwareModel.deleteHardwareInfo(MyDeviceActivity.this, hardwareInfo);
            EventBus.getDefault().post(new BleBindEvent());
            ScPeripheralManager.getInstance().disconnectPeripheral();
            ToastUtils.show(MyDeviceActivity.this, "解绑成功");
            finish();
        }
    }

    public static class ClickFilter {
        //防止连续点击的时间间隔
        public static final long INTERVAL = 500L;
        //上一次点击的时间
        private static long lastClickTime = 0L;

        public static boolean filter() {
            long time = System.currentTimeMillis();

            if ((time - lastClickTime) > INTERVAL) {
                lastClickTime = time;
                return false;
            }
            return true;
        }
    }
}