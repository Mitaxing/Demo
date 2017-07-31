package com.kupaworld.androidtv.entity;

import android.content.Intent;
import android.graphics.drawable.Drawable;

/**
 * Created by admin on 2016/10/15.
 */

public class AppInfo {
    private Drawable appIcon;
    private String AppName;
    private String PackageName;
    private Intent intent;

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public String getAppName() {
        return AppName;
    }

    public void setAppName(String appName) {
        AppName = appName;
    }

    public String getPackageName() {
        return PackageName;
    }

    public void setPackageName(String packageName) {
        PackageName = packageName;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }
}
