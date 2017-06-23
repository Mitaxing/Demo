package com.kupaworld.androidtvwidget.leanback.mode;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.kupaworld.androidtvwidget.R;
import com.kupaworld.androidtvwidget.menu.OpenMenuItemView;

/**
 * 标题头的 Presenter. (test)
 */
public class ItemHeaderPresenter extends OpenPresenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View headview = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_menu_item_layout, parent, false);
        return new ItemHeadViewHolder(headview);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) viewHolder.view.getLayoutParams();
        lp.weight = LinearLayout.LayoutParams.MATCH_PARENT;
        ((OpenMenuItemView) viewHolder.view).setTitle((String) item);
    }

    static class ItemHeadViewHolder extends OpenPresenter.ViewHolder {
        public ItemHeadViewHolder(View view) {
            super(view);
        }
    }

}
