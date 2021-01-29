package com.example.bledemo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.bledemo.BaseAppActivity;
import com.example.bledemo.R;
import com.example.bledemo.view.TopBar;
import com.ikangtai.bluetoothsdk.util.LogUtils;

/**
 * 我的设备
 */
public class BindResultActivity extends BaseAppActivity {

    private TopBar topBar;
    private Button finishBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.i("进入设备绑定结果");
        setContentView(R.layout.activity_bind_result);
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
        finishBtn = findViewById(R.id.btn_finish);
        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

}
