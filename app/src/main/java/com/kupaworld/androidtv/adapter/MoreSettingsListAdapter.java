package com.kupaworld.androidtv.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kupaworld.androidtv.R;

/**
 * Created by admin on 2016/12/5.
 */

public class MoreSettingsListAdapter extends BaseAdapter {

    private Context context;
    private String[] settings;
    private LayoutInflater inflater;
    private TextView mTvName;
    private int len;

    public MoreSettingsListAdapter(Context context, String[] settings) {
        this.context = context;
        this.settings = settings;
        this.len = settings.length;
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
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_more_settings, null);
        }
        mTvName = (TextView) convertView.findViewById(R.id.more_settings_item_name);
        mTvName.setText(settings[position]);
        //设置ListView Item点击后的效果，使用Selector
        if (position == 0)
            convertView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.listview_focus_top_bg));
        else if (position == (len - 1))
            convertView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.listview_focus_bottom_bg));
        else
            convertView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.listview_focus_bg));
        return convertView;
    }
}
