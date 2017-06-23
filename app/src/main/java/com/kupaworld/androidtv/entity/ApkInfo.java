package com.kupaworld.androidtv.entity;

/**
 * Created by admin on 2016/12/19.
 */

public class ApkInfo {

    private int versionCode;
    private String versionName;
    private String versionDesc;
    private String url;
    private String updateTime;

    public ApkInfo(int versionCode, String versionName, String versionDesc, String url, String updateTime) {
        super();
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.versionDesc = versionDesc;
        this.url = url;
        this.updateTime = updateTime;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionDesc() {
        return versionDesc;
    }

    public void setVersionDesc(String versionDesc) {
        this.versionDesc = versionDesc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
