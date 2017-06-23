package com.kupaworld.androidtvwidget.leanback.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * 优化过的LinerLayout. --> hasOverlappingRendering (false)
 */
public class NORLinearLayout extends LinearLayout {
    public NORLinearLayout(Context context) {
        this(context, null);
    }

    public NORLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NORLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }
}
