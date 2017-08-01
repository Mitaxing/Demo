package com.kupaworld.androidtv.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.RecoverySystem;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import java.io.File;
import java.io.IOException;

/**
 * Created by admin on 2017/6/5.
 */

public class UpdateUtils {

    //刷机
    public static void installPackage(Context context, File packageFile)
            throws IOException {
        getFilePath("/cache/recovery", "command");
        RecoverySystem.installPackage(context, packageFile);
    }

    //获取系统升级包文件路径
    private static File getFilePath(String filePath,
                                    String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    //生成系统镜像目录
    private static void makeRootDirectory(String filePath) {
        File file;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {

        }
    }

    /**
     * 保存系统版本
     *
     * @param context
     * @return
     */
    public static void saveVersion(Context context) {
        SharedPreferences sp = context.getSharedPreferences("kupaTv", 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("oldVersion", Utils.getVersionCode(context)).apply();
    }

    /**
     * 更新系统结果
     *
     * @param context
     */
    public static void updateSystemResult(final Context context) {
        SharedPreferences sp = context.getSharedPreferences("kupaTv", 0);
        int oldVersion = sp.getInt("oldVersion", -1);
        int currentVersion = Utils.getVersionCode(context);
        if (oldVersion == -1) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("oldVersion", currentVersion).apply();
        } else if (currentVersion > oldVersion) {
            HttpUtils httpUtils = new HttpUtils(10 * 1000);
            RequestParams params = new RequestParams();
            params.addBodyParameter("mac", Utils.getLocalMacAddress(context));
            params.addBodyParameter("oldVersion", String.valueOf(oldVersion));
            params.addBodyParameter("newVersion", String.valueOf(currentVersion));
            params.addBodyParameter("result", String.valueOf(1));
            httpUtils.send(HttpRequest.HttpMethod.POST, Contacts.URI_UPDATE_RESULT, params, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    Utils.log("系统更新结果成功：" + responseInfo.result);
                    if (responseInfo.result.contains("ok"))
                        saveVersion(context);
                }

                @Override
                public void onFailure(HttpException e, String s) {
                    Utils.log("系统更新失败：" + s);
                }
            });
        }
    }

}
