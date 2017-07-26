package com.kupaworld.androidtv.entity;

/**
 * Created by admin on 2017/6/2.
 */

public class SystemInfo {

    private Integer versionId;
    private String versionNumber;
    private String versionName;
    private String versionInformation;
    private String versionDownloadUrl;
    private Integer versionClassId;
    private long updateTime;
    private String result;

    public SystemInfo() {
    }

    public SystemInfo(Integer versionId, String versionName, String versionInformation, String versionDownloadUrl, long updateTime) {
        this.versionId = versionId;
        this.versionName = versionName;
        this.versionInformation = versionInformation;
        this.versionDownloadUrl = versionDownloadUrl;
        this.updateTime = updateTime;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Integer getVersionId() {
        return versionId;
    }

    public void setVersionId(Integer versionId) {
        this.versionId = versionId;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionInformation() {
        return versionInformation;
    }

    public void setVersionInformation(String versionInformation) {
        this.versionInformation = versionInformation;
    }

    public String getVersionDownloadUrl() {
        return versionDownloadUrl;
    }

    public void setVersionDownloadUrl(String versionDownloadUrl) {
        this.versionDownloadUrl = versionDownloadUrl;
    }

    public Integer getVersionClassId() {
        return versionClassId;
    }

    public void setVersionClassId(Integer versionClassId) {
        this.versionClassId = versionClassId;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
}
