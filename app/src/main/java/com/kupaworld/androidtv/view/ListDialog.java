package com.kupaworld.androidtv.view;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.kupaworld.androidtv.R;
import com.kupaworld.androidtv.adapter.MyDialogListAdapter;

/**
 * Created by MiTa on 2016/11/16.
 */

public class ListDialog extends AlertDialog {

    private Context context;
    private String[] item;
    private onOptionClickListener itemClickListener;
    private onCancelListener clickListener;
    private TextView mTvCancel;
    private ListView listView;

    public ListDialog(Context context, int theme, String[] items) {
        super(context, theme);
        this.context = context;
        this.item = items;
    }

    public void setOnCancelClickLisnter(onCancelListener lisnter){
        this.clickListener = lisnter;
    }

    public void setOnOptionClickListener(onOptionClickListener listener){
        this.itemClickListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_my_dialog);
        initView();
        initEvent();
    }

    private void initView() {
        mTvCancel = (TextView) findViewById(R.id.file_option_cancel);

        listView = (ListView) findViewById(R.id.myDialog_list);
        listView.setAdapter(new MyDialogListAdapter(context, item));
    }

    private void initEvent() {
        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null)
                    clickListener.onCancelClick();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (itemClickListener!=null)
                    itemClickListener.onOptionClick(parent,view,position,id);
            }
        });
    }

    public interface onCancelListener {
        void onCancelClick();
    }

    public interface onOptionClickListener{
        void onOptionClick(AdapterView<?> parent, View view, int position, long id);
    }
}
