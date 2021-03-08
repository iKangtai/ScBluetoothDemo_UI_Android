package com.ikangtai.bluetoothui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ikangtai.bluetoothui.AppInfo;
import com.ikangtai.bluetoothui.R;
import com.ikangtai.bluetoothui.event.AutoUploadTemperatureEvent;
import com.ikangtai.bluetoothui.event.BleStateEventBus;
import com.ikangtai.bluetoothui.event.BluetoothStateEventBus;
import com.ikangtai.bluetoothui.event.TemperatureBleScanEventBus;
import com.ikangtai.bluetoothui.info.HardwareInfo;
import com.ikangtai.bluetoothui.model.HardwareModel;
import com.ikangtai.bluetoothui.util.CheckBleFeaturesUtil;
import com.ikangtai.bluetoothui.view.ThermometerHelper;
import com.ikangtai.bluetoothui.view.TopBar;
import com.ikangtai.bluetoothui.view.dialog.BleAlertDialog;
import com.ikangtai.bluetoothsdk.util.BleTools;
import com.ikangtai.bluetoothsdk.util.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Connect a thermometer
 *
 * @author xiongyl 2021/1/21 21:11
 */
public class DeviceConnectActivity extends AppCompatActivity {
    private Disposable connDisposable;
    private Disposable waitDisposable;
    private Disposable waitUploadDisposable;
    /**
     * Device is connecting
     */
    public static final int CONN_ING = 0;
    /**
     * Device connection is slow for 15 seconds
     */
    public static final int CONN_SLOW = 1;

    /**
     * Device connection complete
     */
    public static final int CONN_COMPLETE = 2;
    /**
     * Device connected successfully
     */
    public static final int CONN_SUCCESS = 3;

    /**
     * Device connection failed
     */
    public static final int CONN_FAIL = 4;

    /**
     * Device connection data transmission is successful
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
                        //rescan
                        loadData();
                        operatorBtn.setText(getResources().getString(R.string.cancel));
                        EventBus.getDefault().post(new TemperatureBleScanEventBus());
                    } else if (content.equals(getResources().getString(R.string.cancel)) ||
                            content.equals(getResources().getString(R.string.upload_complete))) {
                        //cancel
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
            //Mobile phone bluetooth is turned on/pregnant orange thermometer is turned on
            //First notify the user that the device is connected successfully
            notifyUserConnected();
        } else {
            disposables.clear();
            disposables.add(taskDisposable(10 * 1000, new IEvent() {
                @Override
                public void firstTask() {
                    //Notify the user that the device is connecting
                    showConnState(CONN_ING);
                }

                @Override
                public void nextTask() {
                    //After 10 seconds, the phone and the thermometer have not established a connection
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
     * Notify the user that the connection is successful
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
     * Notify the user to continue waiting for 50s
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
     * Wait 30 seconds for data upload
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
                    //No new body temperature found
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
     * Receive thermometer status
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
     * Bluetooth status of receiving device
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
     * Notify the user of the temperature result information transmitted by the clinical thermometer
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
     * Do the first task first, how long is the delay, do the second task
     *
     * @param delay millisecond
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
     * Change the bottom button text
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
