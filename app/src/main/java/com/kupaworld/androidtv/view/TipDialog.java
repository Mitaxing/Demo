package com.kupaworld.androidtv.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.kupaworld.androidtv.R;

/**
 * Created by HM on 2016/11/18 16:36
 */

public class TipDialog extends Dialog {

    private TextView mTvYes;
    private TextView mTvNo;
    private EditText mEtName;
    private FrameLayout mFlNo, mFlYes;

    private OnYesClickListener yesClickListener;
    private OnNoClickListener noClickListener;

    private Context context;

    private String message, title, sure, cancel;
    private boolean isEdit;

    public TipDialog(Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setYesText(String str) {
        this.sure = str;
    }

    public void setCancleText(String str) {
        this.cancel = str;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setEdit(boolean isEdit) {
        this.isEdit = isEdit;
    }

    public void setEditName(String name) {
        mEtName.setText(name);
    }

    public String getEditName() {
        return mEtName.getText().toString();
    }

    public void setYesClickListener(OnYesClickListener yesClickListener) {
        this.yesClickListener = yesClickListener;
    }

    public void setNoClickListener(OnNoClickListener noClickListener) {
        this.noClickListener = noClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_tip_dialog);
        initViews();
        initEvent();
    }

    private void initViews() {
        TextView mTvMessage = (TextView) findViewById(R.id.tip_dialog_message);
        mTvNo = (TextView) findViewById(R.id.tip_dialog_no);
        TextView mTvTitle = (TextView) findViewById(R.id.tip_dialog_title);
        mTvYes = (TextView) findViewById(R.id.tip_dialog_yes);
        mFlNo = (FrameLayout) findViewById(R.id.tip_dialog_no_layout);
        mFlYes = (FrameLayout) findViewById(R.id.tip_dialog_yes_layout);
        mEtName = (EditText) findViewById(R.id.tip_dialog_edit_name);

        if (null != message) {
            mTvMessage.setText(message);
        }
        if (null != title) {
            mTvTitle.setText(title);
        }

        if (null != sure) {
            mTvYes.setText(sure);
            mFlYes.requestFocus();
        }
        if (null != cancel) {
            mTvNo.setText(cancel);
        }

        if (isEdit) {
            mTvMessage.setVisibility(View.GONE);
            mEtName.setVisibility(View.VISIBLE);
        }

        if (noClickListener == null) {
            mFlNo.setVisibility(View.GONE);
            mFlYes.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.tv_focus_full_bg));
        }

        mFlNo.setOnFocusChangeListener(listener);
        mFlYes.setOnFocusChangeListener(listener);
    }

    View.OnFocusChangeListener listener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            int id = v.getId();
            switch (id) {
                case R.id.tip_dialog_yes_layout:
                    if (hasFocus)
                        mTvYes.setTextColor(Color.WHITE);
                    else
                        mTvYes.setTextColor(Color.BLACK);
                    break;

                case R.id.tip_dialog_no_layout:
                    if (hasFocus)
                        mTvNo.setTextColor(Color.WHITE);
                    else
                        mTvNo.setTextColor(Color.BLACK);
                    break;
            }
        }
    };

    private void initEvent() {
        mFlNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noClickListener != null)
                    noClickListener.onNoClick();
            }
        });
        mFlYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (yesClickListener != null)
                    yesClickListener.onYesClick();
            }
        });
    }

    public interface OnYesClickListener {
        void onYesClick();
    }

    public interface OnNoClickListener {
        void onNoClick();
    }
}
