package com.example.bledemo;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import com.example.bledemo.view.ProgressDialog;

import org.greenrobot.eventbus.EventBus;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * desc
 *
 * @author xiongyl 2021/1/21 21:25
 */
public class BaseAppActivity extends AppCompatActivity {
    protected Dialog progressDialog;

    public void showProgressDialog() {
        progressDialog = ProgressDialog.createLoadingDialog(this, null, null);
        if (progressDialog != null && !progressDialog.isShowing() && !isFinishing()) {
            progressDialog.setCancelable(true);
            progressDialog.show();
        }
    }

    public void showProgressDialog(String msg) {
        progressDialog = ProgressDialog.createLoadingDialog(this, msg, null);
        if (progressDialog != null && !progressDialog.isShowing() && !isFinishing()) {
            progressDialog.setCancelable(true);
            progressDialog.show();
        }
    }

    public void showProgressDialog(String msg, View.OnClickListener onClickListener) {
        progressDialog = ProgressDialog.createLoadingDialog(this, msg, onClickListener);
        if (progressDialog != null && !progressDialog.isShowing() && !isFinishing()) {
            progressDialog.setCancelable(true);
            progressDialog.show();
        }
    }

    public void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
