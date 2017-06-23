package com.kupaworld.androidtvwidget.leanback.recycle.Helper;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.KeyEvent;

import com.kupaworld.androidtvwidget.leanback.recycle.RecyclerViewTV;

/**
 * 按键拖动item.
 */
public class ItemKeyHelper extends ItemTouchHelper {

    public ItemKeyHelper(Callback callback) {
        super(callback);
    }

    @Override
    public void attachToRecyclerView(RecyclerView recyclerView) {
        super.attachToRecyclerView(recyclerView);
        if (recyclerView instanceof RecyclerViewTV) {
            RecyclerViewTV recyclerViewTV = (RecyclerViewTV) recyclerView;
            recyclerViewTV.addOnItemKeyListener(mOnItemKeyListener);
        }
    }

    private final RecyclerViewTV.OnItemKeyListener mOnItemKeyListener
            = new RecyclerViewTV.OnItemKeyListener() {
        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            return false;
        }
    };

}
