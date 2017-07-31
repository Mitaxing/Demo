package com.kupaworld.androidtv.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kupaworld.androidtv.R;
import com.kupaworld.androidtv.entity.AppInfo;

import java.util.List;

/**
 * Created by admin on 2017/5/22.
 */

public class AppsAdapter extends BaseAdapter {

    private Context context;
    private List<AppInfo> list;
    private ViewHolder holder;

    public AppsAdapter(Context context, List<AppInfo> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return (list == null) ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = LayoutInflater.from(context).inflate(R.layout.myapp_item, null);
            holder = new ViewHolder();
            holder.appIcon = (ImageView) convertView.findViewById(R.id.imageView_appIcon);
            holder.appName = (TextView) convertView.findViewById(R.id.textView_appName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.appName.setText(list.get(position).getAppName());
        holder.appIcon.setImageDrawable(list.get(position).getAppIcon());
        return convertView;
    }

    private final class ViewHolder {
        ImageView appIcon;
        TextView appName;
    }
}
