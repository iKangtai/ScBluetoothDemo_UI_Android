package com.example.bledemo.view;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;


import com.example.bledemo.basic.R;

import androidx.appcompat.app.AppCompatDialog;


/**
 * 基本操作
 * 卡型试纸Dialog
 */
public class PaperHintDialog {

    private Context context;

    private TextView contentView;
    private TextView title;
    private TextView operate;

    private IEvent event;
    private Dialog dialog;

    public PaperHintDialog(Context context) {
        this.context = context;
    }

    public PaperHintDialog builder() {
        View view = LayoutInflater.from(context).inflate(
                R.layout.layout_dialog_paper_hint, null);

        contentView = view.findViewById(R.id.content);
        title = view.findViewById(R.id.title);
        operate = view.findViewById(R.id.operate);

        operate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                if (event != null) {
                    event.clickButton();
                }

            }
        });

        // 定义Dialog布局和参数
        dialog = new AppCompatDialog(context, R.style.CardPaperDialogStyle);
        dialog.setContentView(view);

        return this;
    }

    public PaperHintDialog setCancelable(boolean cancel) {
        dialog.setCancelable(cancel);
        return this;
    }

    public PaperHintDialog setCanceledOnTouchOutside(boolean cancel) {
        dialog.setCanceledOnTouchOutside(cancel);
        return this;
    }

    public PaperHintDialog content(String content) {
        if (contentView != null && !TextUtils.isEmpty(content)) {
            this.contentView.setText(content);
        }
        return this;
    }


    public PaperHintDialog title(String title) {
        if (this.title != null && !TextUtils.isEmpty(title)) {
            this.title.setVisibility(View.VISIBLE);
            this.title.setText(title);
        }
        return this;
    }


    public PaperHintDialog buttonText(String content) {
        if (operate != null && !TextUtils.isEmpty(content)) {
            this.operate.setText(content);
        }
        return this;
    }

    public PaperHintDialog show() {

        if (dialog != null) {
            dialog.show();
        }

        return this;
    }


    public PaperHintDialog initEvent(IEvent event) {
        this.event = event;
        return this;
    }

    public interface IEvent {
        void clickButton();
    }

}
