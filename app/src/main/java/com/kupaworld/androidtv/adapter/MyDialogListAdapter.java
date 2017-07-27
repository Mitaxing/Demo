package com.kupaworld.androidtv.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kupaworld.androidtv.R;

/**
 * Created by MiTa on 2016/11/17.
 */

public class MyDialogListAdapter extends BaseAdapter {

    private String[] items;
    private int len;
    private LayoutInflater inflater;

    public MyDialogListAdapter(Context context, String[] items) {
        this.items = items;
        this.len = items.length;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return len;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.item_my_dialog, null);
        }
        TextView tv = (TextView) convertView.findViewById(R.id.menu_name);
        tv.setText(items[position]);
        return convertView;
    }
}
