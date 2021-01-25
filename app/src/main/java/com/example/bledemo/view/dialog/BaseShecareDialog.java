package com.example.bledemo.view.dialog;

import android.app.Dialog;
import android.os.Build;
import android.view.WindowManager;

public class BaseShecareDialog {
    protected Dialog dialog;

    public void dissmiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public boolean showing() {
        if (dialog != null && dialog.isShowing()) {
            return true;
        }

        return false;
    }


    public BaseShecareDialog withOverLay() {
        if (Build.VERSION.SDK_INT >= 26) {
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY - 1);
        }
        return this;
    }
}
