package com.ikangtai.bluetoothui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ikangtai.bluetoothui.R;
import com.ikangtai.bluetoothui.view.TopBar;
import com.ikangtai.bluetoothsdk.util.LogUtils;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 绑定结果
 *
 * @author xiongyl 2021/1/21 21:11
 */
public class BindResultActivity extends AppCompatActivity {

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