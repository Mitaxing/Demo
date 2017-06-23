package com.kupaworld.androidtvwidget.utils;

import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

/**
 * 动画集合.
 */
public class AnimateUtils {
    public static Animation zoomAnimation(float startScale, float endScale, long duration) {
        ScaleAnimation anim = new ScaleAnimation(startScale, endScale, startScale, endScale, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setFillAfter(true);
        anim.setDuration(duration);
        return anim;
    }
}
