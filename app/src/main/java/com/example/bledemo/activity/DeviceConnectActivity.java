package com.example.bledemo.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.bledemo.AppInfo;
import com.example.bledemo.R;
import com.example.bledemo.event.AutoUploadTemperatureEvent;
import com.example.bledemo.event.BleStateEventBus;
import com.example.bledemo.event.BluetoothStateEventBus;
import com.example.bledemo.event.TemperatureBleScanEventBus;
import com.example.bledemo.info.HardwareInfo;
import com.example.bledemo.model.HardwareModel;
import com.example.bledemo.util.CheckBleFeaturesUtil;
import com.example.bledemo.view.ThermometerHelper;
import com.example.bledemo.view.TopBar;
import com.example.bledemo.view.dialog.BleAlertDialog;
import com.ikangtai.bluetoothsdk.util.BleTools;
import com.ikangtai.bluetoothsdk.util.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 连接体温计
 *
 * @author xiongyl 2021/1/21 21:11
 */
public class DeviceConnectActivity extends AppCompatActivity {
    private Disposable connDisposable;
    private Disposable waitDisposable;
    private Disposable waitUploadDisposable;
    /**
     * 设备正在连接
     */
    public static final int CONN_ING = 0;
    /**
     * 设备连接缓慢已过15秒
     */
    public static final int CONN_SLOW = 1;

    /**
     * 设备连接完成
     */
    public static final int CONN_COMPLETE = 2;
    /**
     * 设备连接成功
     */
    public static final int CONN_SUCCESS = 3;

    /**
     * 设备连接失败
     */
    public static final int CONN_FAIL = 4;

    /**
     * 设备连接数据传输成功
     */
    public static final int CONN_SEND_DATA_SUCCESS = 5;


    private TopBar topBar;

    private TextView openBluetoothHint;
    private TextView openThermometerhHint;
    private TextView manualThermomter;
    private ThermometerHelper thermomterHelper;
    private Button operatorBtn;
    private boolean uploadSuccess;
    protected CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        LogUtils.i("DeviceConnectActivity onCreate>>>");
        setContentView(R.layout.activity_thermometer_device);
        AppInfo.getInstance().setDeviceConnectActive(true);
        initView();
        CheckBleFeaturesUtil.checkBleFeatures(this);
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
                startActivity(new Intent(DeviceConnectActivity.this, MyDeviceActivity.class));
            }
        });

        openBluetoothHint = findViewById(R.id.openBluetoothHint);
        openThermometerhHint = findViewById(R.id.openThermometerhHint);
        manualThermomter = findViewById(R.id.manualThermomter);
        thermomterHelper = findViewById(R.id.thermomterHelper);
        operatorBtn = findViewById(R.id.operatorBtn);
        if (manualThermomter != null) {
            manualThermomter.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
            manualThermomter.getPaint().setAntiAlias(true);
            manualThermomter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
        if (operatorBtn != null) {
            operatorBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String content = operatorBtn.getText().toString();
                    if (content.equals(getResources().getString(R.string.repeat_search))) {
                        //重新搜索
                        loadData();
                        operatorBtn.setText(getResources().getString(R.string.cancel));
                        EventBus.getDefault().post(new TemperatureBleScanEventBus());
                    } else if (content.equals(getResources().getString(R.string.cancel)) ||
                            content.equals(getResources().getString(R.string.upload_complete))) {
                        //取消
                        finish();
                    }
                }
            });
        }

    }

    private void loadData() {
        if (operatorBtn != null) {
            operatorBtn.setText(getResources().getString(R.string.cancel));
        }
        boolean thermometerState = obtainThermometerState();
        boolean bluetoothState = obtainBluetoothState();
        showBleState(thermometerState);
        showBluetoothState(bluetoothState);

        if (bluetoothState && thermometerState) {
            //手机蓝牙打开/孕橙体温计开关打开
            //先告知用户设备已连接成功
            notifyUserConnected();
        } else {
            disposables.clear();
            disposables.add(taskDisposable(10 * 1000, new IEvent() {
                @Override
                public void firstTask() {
                    //告知用户设备正在连接
                    showConnState(CONN_ING);
                }

                @Override
                public void nextTask() {
                    //10秒后，手机与体温计还未建立连接
                    if (!(obtainThermometerState() && obtainBluetoothState())) {
                        notifyUserWait();
                    } else {
                        notifyUserConnected();
                    }
                }
            }));

        }
        List<HardwareInfo> hardwareInfoList = HardwareModel.hardwareList(this);
        if (hardwareInfoList.isEmpty()) {
            finish();
        }
    }

    private boolean obtainBluetoothState() {
        boolean bluetoothState = AppInfo.getInstance().isBluetoothState();
        if (BleTools.checkBleEnable()) {
            bluetoothState = true;
        }
        return bluetoothState;
    }

    private boolean obtainThermometerState() {
        return AppInfo.getInstance().isThermometerState();
    }

    /**
     * 通知用户已经连接成功
     */
    private void notifyUserConnected() {
        if (connDisposable != null) {
            disposables.remove(connDisposable);
        }
        if (waitUploadDisposable != null) {
            disposables.remove(waitUploadDisposable);
        }
        if (thermomterHelper != null) {
            boolean thermometerState = obtainThermometerState();
            boolean bluetoothState = obtainBluetoothState();
            boolean isConnected = thermometerState && bluetoothState;
            if (isConnected) {
                connDisposable = taskDisposable(1500, new IEvent() {
                    @Override
                    public void firstTask() {
                        showConnState(CONN_COMPLETE);
                    }

                    @Override
                    public void nextTask() {
                        waitDataUpload();
                    }
                });
                disposables.add(connDisposable);
            } else {
                showConnState(CONN_FAIL);
            }
        }

    }

    /**
     * 通知用户继续等待50s
     */
    private void notifyUserWait() {
        if (waitDisposable != null) {
            disposables.remove(waitDisposable);
        }
        waitDisposable = taskDisposable(50 * 1000, new IEvent() {
            @Override
            public void firstTask() {
                showConnState(CONN_SLOW);
            }

            @Override
            public void nextTask() {
                if (!(obtainThermometerState() && obtainBluetoothState())) {
                    showConnState(CONN_FAIL);
                } else {
                    notifyUserConnected();
                }
            }
        });
        disposables.add(waitDisposable);
    }

    /**
     * 等待30秒数据上传
     */
    private void waitDataUpload() {
        if (waitUploadDisposable != null) {
            disposables.remove(waitUploadDisposable);
        }
        waitUploadDisposable = taskDisposable(30 * 1000, new IEvent() {
            @Override
            public void firstTask() {
                showConnState(CONN_SUCCESS);
            }

            @Override
            public void nextTask() {
                if (!uploadSuccess) {
                    //未发现新增体温
                    String content = getString(R.string.temperature_alert_1);
                    String subContent = String.format(getString(R.string.format_font_ff7568), getString(R.string.warm_prompt) + ":") + getString(R.string.temperature_alert_2);
                    showTemperatureInfo(new AutoUploadTemperatureEvent(content, subContent));
                }
            }
        });
        disposables.add(waitUploadDisposable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.i("onDestroy");
        if (disposables != null) {
            disposables.clear();
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        AppInfo.getInstance().setDeviceConnectActive(false);
    }


    /**
     * 显示体温计状态
     *
     * @param eventBus
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void syncBLeState(BleStateEventBus eventBus) {
        if (eventBus != null) {
            boolean isConn = eventBus.isConnect();
            showBleState(isConn);
            notifyUserConnected();
        }

    }

    /**
     * 显示设备蓝牙状态
     *
     * @param eventBus
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void synBluetoothState(BluetoothStateEventBus eventBus) {
        if (eventBus != null) {
            boolean isConn = eventBus.isOpen();
            showBluetoothState(isConn);
            if (!isConn) {
                showConnState(CONN_FAIL);
            } else {
                loadData();
            }
        }
    }

    /**
     * 将体温计传输的温度结果信息告知用户
     *
     * @param autoUploadTemperatureEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showTemperatureInfo(AutoUploadTemperatureEvent autoUploadTemperatureEvent) {
        uploadSuccess = true;
        showConnState(CONN_SEND_DATA_SUCCESS);

        if (autoUploadTemperatureEvent != null) {
            String content = autoUploadTemperatureEvent.getContent();
            String subContent = autoUploadTemperatureEvent.getSubContent();
            if (TextUtils.isEmpty(content) && TextUtils.isEmpty(subContent)) {
                return;
            }
            new BleAlertDialog(DeviceConnectActivity.this).builder()
                    .setMsg(Html.fromHtml(content + "<br><br>" + subContent), Gravity.LEFT)
                    .setCancelable(false)
                    .setCanceledOnTouchOutside(false)
                    .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    }).show();
        }

    }

    private void showBleState(boolean isConn) {
        if (openThermometerhHint != null) {
            openThermometerhHint.setTextColor(isConn ? Color.parseColor("#444444")
                    : Color.parseColor("#B2B2B2"));

            int resId = isConn ? R.drawable.binding_icon_selected : R.drawable.binding_icon_normal;
            Drawable drawable = getResources().getDrawable(resId);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            openThermometerhHint.setCompoundDrawables(drawable, null, null, null);

        }
    }

    private void showBluetoothState(boolean isOpen) {

        if (openBluetoothHint != null) {
            openBluetoothHint.setTextColor(isOpen ? Color.parseColor("#444444")
                    : Color.parseColor("#B2B2B2"));

            int resId = isOpen ? R.drawable.binding_icon_selected : R.drawable.binding_icon_normal;
            Drawable drawable = getResources().getDrawable(resId);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            openBluetoothHint.setCompoundDrawables(drawable, null, null, null);
        }
    }

    private void showConnState(int state) {
        if (thermomterHelper != null) {
            if (state == CONN_ING) {
                thermomterHelper.connect();
            }

            if (state == CONN_SLOW) {
                thermomterHelper.connectHelp();
            }

            if (state == CONN_COMPLETE) {
                thermomterHelper.connectComplete();
            }

            if (state == CONN_SUCCESS) {
                thermomterHelper.connected(uploadSuccess);
                setOperateContent(getResources().getString(R.string.data_uploading));
            }

            if (state == CONN_FAIL) {
                thermomterHelper.connectFail();
                setOperateContent(getResources().getString(R.string.repeat_search));
            }

            if (state == CONN_SEND_DATA_SUCCESS) {
                setOperateContent(getResources().getString(R.string.upload_complete));
                thermomterHelper.sendDataSuccess();
            }

        }

    }

    /**
     * 先做第一任务，延时多久，做第二任务
     *
     * @param delay 毫秒
     * @param event
     * @return
     */
    private Disposable taskDisposable(long delay, final IEvent event) {
        return Observable.just(0L).doOnNext(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {

                if (event != null) {
                    event.firstTask();
                }

            }
        }).delay(delay, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (event != null) {
                            event.nextTask();
                        }
                    }
                });
    }

    /**
     * 更改底部按钮文案
     *
     * @param content
     */
    private void setOperateContent(String content) {
        if (operatorBtn != null) {
            if (getResources().getString(R.string.upload_complete).equals(content) ||
                    getResources().getString(R.string.repeat_search).equals(content)) {
                operatorBtn.setBackgroundResource(R.drawable.app_button_corner);
                operatorBtn.setText(content);
            } else if (getResources().getString(R.string.data_uploading).equals(content)) {
                operatorBtn.setBackgroundResource(R.drawable.app_button_login_unenable);
                operatorBtn.setText(content);
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CheckBleFeaturesUtil.handBleFeaturesResult(this, requestCode, resultCode);
    }

    private interface IEvent {
        void firstTask();

        void nextTask();
    }

}
