package com.kupaworld.androidtv.util;

import android.content.Context;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

/**
 * Created by admin on 2016/12/13.
 */

public class PhotoChange {

    private AlphaAnimation anim;
    private ImageView iv;
    private Context context;

    private int[] res;
    private int len, currentRes;
    private int status;

    public PhotoChange(Context context, ImageView iv, int[] res) {
        this.context = context;
        this.iv = iv;
        this.res = res;
        this.len = res.length - 1;
        initAnimation();
        anim.start();
    }

    private void initAnimation() {
        if (status == 0) {
            anim = new AlphaAnimation(1f, 0.3f);
            anim.setDuration(3000);
        } else if (status == 1) {
            anim = new AlphaAnimation(0.3f, 1f);
            anim.setDuration(3000);
        } else {
            anim = new AlphaAnimation(1f, 1f);
            anim.setDuration(3000);
        }
        anim.setAnimationListener(listener);
        anim.setInterpolator(new LinearInterpolator());
        iv.setAnimation(anim);
    }

    private void startAnim() {
        anim.start();
    }

    public void stopAnim(){
        anim.cancel();
        anim.reset();
        iv.clearAnimation();
    }

    public int getCurrentRes(){
        return currentRes;
    }

    Animation.AnimationListener listener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (status == 0) {
                status = 1;
            } else if (status == 1) {
                status = 2;
            } else {
                status = 0;
            }
            if (status == 1) {
                if (currentRes < len)
                    currentRes++;
                else
                    currentRes = 0;
                iv.setImageBitmap(ImageDeal.readBitMap(context, res[currentRes]));
            }
            initAnimation();
            startAnim();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            Log.d("kumi", "z这是啥----------");
        }
    };
}
