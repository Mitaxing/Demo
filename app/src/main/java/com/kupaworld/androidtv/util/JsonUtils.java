package com.kupaworld.androidtv.util;

import android.content.Context;

import com.kupaworld.androidtv.entity.SystemInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Created by admin on 2017/6/2.
 */

public class JsonUtils {

    public static SystemInfo resolveResult(Context context, String result) {
        SystemInfo info = null;
        JSONTokener parse = new JSONTokener(result);
        try {
            JSONObject object = (JSONObject) parse.nextValue();
            if (object.optInt("code") == 200) {
                String res = object.optString("res","no-update-version");
                info = new SystemInfo();
                info.setResult(res);
                if (res.equals("ok")){
                    JSONObject infoObj = object.optJSONObject("info");
                    info.setUpdateTime(infoObj.optLong("updateTime"));
                    info.setVersionClassId(infoObj.optInt("versionClassId"));
                    info.setVersionDownloadUrl(infoObj.optString("versionDownloadUrl"));
                    info.setVersionId(infoObj.optInt("versionId"));
                    info.setVersionInformation(infoObj.optString("versionInformation"));
                    info.setVersionName(infoObj.optString("versionName"));
                    info.setVersionNumber(infoObj.optString("versionNumber"));
                }
            } else {
                Utils.toast(context, "服务器开小差了~");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return info;
    }
}
