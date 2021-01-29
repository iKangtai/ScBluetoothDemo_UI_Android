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
}
