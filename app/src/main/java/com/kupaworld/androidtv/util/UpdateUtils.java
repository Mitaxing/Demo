package com.kupaworld.androidtv.util;

import android.content.Context;
import android.os.RecoverySystem;

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
}
