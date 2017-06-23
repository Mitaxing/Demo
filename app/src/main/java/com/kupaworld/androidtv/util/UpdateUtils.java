package com.kupaworld.androidtv.util;

import android.content.Context;
import android.os.PowerManager;
import android.os.RecoverySystem;
import android.text.TextUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by admin on 2017/6/5.
 */

public class UpdateUtils {

    private static String COMMAND_FILE = "/cache/recovery/command";

    public static void installPackage(Context context, File packageFile)
            throws IOException {
//        File file = new File("/cache/recovery");
//        if (!file.exists())
//            file.mkdirs();
//        File file1 = new File(COMMAND_FILE);
//        if (!file1.exists())
//            file1.createNewFile();
        getFilePath("/cache/recovery","command");
        RecoverySystem.installPackage(context,packageFile);
//        String filename = packageFile.getCanonicalPath();
//        Utils.log("!!! REBOOTING TO INSTALL " + filename + " !!!");
//
//        final String filenameArg = "--update_package=" + filename;
//        final String localeArg = "--locale=" + Locale.getDefault().toString();
//        bootCommand(context, filenameArg, localeArg);
    }

    private static void bootCommand(Context context, String... args) throws IOException {
//        RECOVERY_DIR.mkdirs();  // In case we need it
//        COMMAND_FILE.delete();  // In case it's not writable
//        LOG_FILE.delete();
        File file = new File("/cache/recovery");
        if (!file.exists())
            file.mkdirs();
        FileWriter command = new FileWriter(COMMAND_FILE);
        try {
            for (String arg : args) {
                if (!TextUtils.isEmpty(arg)) {
                    command.write(arg);
                    command.write("\n");
                }
            }
        } finally {
            command.close();
        }

        // Having written the command file, go ahead and reboot
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        pm.reboot("recovery");

        throw new IOException("Reboot failed (no permissions?)");
    }

    public static File getFilePath(String filePath,
                                   String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return file;
    }

    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {

        }
    }
}
