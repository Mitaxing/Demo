package com.kupaworld.androidtv.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by admin on 2017/5/16.
 */

public class Utils {

    /**
     * 静默安装
     *
     * @param apk
     */
    public static void backgroundInstall(final Context context, File apk) {
        try {
            Class<?> clazz = Class.forName("android.os.ServiceManager");
            Method method_getService = clazz.getMethod("getService",
                    String.class);
            IBinder bind = (IBinder) method_getService.invoke(null, "package");

            IPackageManager iPm = IPackageManager.Stub.asInterface(bind);
            iPm.installPackage(Uri.fromFile(apk), new IPackageInstallObserver.Stub() {
                        @Override
                        public void packageInstalled(String packageName, int returnCode) throws RemoteException {
                            Looper.prepare();
                            Toast.makeText(context.getApplicationContext(), "安装完成", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }, 2,
                    apk.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    * check the app is installed
    */
    public static boolean isAppInstalled(Context context, String packageName) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        if (packageInfo == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 卸载应用
     *
     * @param packageName
     */
    public static void uninstallApk(Context context, String packageName) {
        Uri uri = Uri.parse("package:" + packageName);//获取删除包名的URI
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DELETE);//设置我们要执行的卸载动作
        intent.setData(uri);//设置获取到的URI
        context.startActivity(intent);
    }

    /**
     * 打开第三方应用
     *
     * @param context
     * @param packageName
     */
    public static void openOtherApp(Context context, String packageName) {
        try {
            PackageManager packageManager = context.getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(packageName);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            toast(context, "请稍后再试~~~");
        }
    }

    /**
     * 检查当前网络是否可用
     *
     * @param context
     * @return
     */

    public static boolean isNetworkAvailable(Context context) {
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        } else {
            // 获取NetworkInfo对象
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

            if (networkInfo != null && networkInfo.length > 0) {
                for (int i = 0; i < networkInfo.length; i++) {
                    System.out.println(i + "===状态===" + networkInfo[i].getState());
                    System.out.println(i + "===类型===" + networkInfo[i].getTypeName());
                    // 判断当前网络状态是否为连接状态
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    //打开APK程序代码
    public static void openFile(Context context, File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * Toast
     *
     * @param context
     * @param msg
     */
    public static void toast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void log(String msg) {
        Log.i("mita", msg);
    }

    /*
     * 杀死后台进程
     */
    public static void killAll(Context context) {

        //获取一个ActivityManager 对象
        ActivityManager activityManager = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);
        //获取系统中所有正在运行的进程
        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager
                .getRunningAppProcesses();
//        int count = 0;//被杀进程计数
//        String nameList = "";//记录被杀死进程的包名
//        long beforeMem = getAvailMemory(context);//清理前的可用内存
//        Log.i(TAG, "清理前可用内存为 : " + beforeMem);

        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfos) {
//            nameList = "";
            if (appProcessInfo.processName.contains("com.android.system")
                    || appProcessInfo.pid == android.os.Process.myPid())//跳过系统 及当前进程
                continue;
            String[] pkNameList = appProcessInfo.pkgList;//进程下的所有包名
            for (int i = 0; i < pkNameList.length; i++) {
                String pkName = pkNameList[i];
                if (!pkName.equals(Contacts.PACKAGE_SHOW)) {
                    activityManager.killBackgroundProcesses(pkName);//杀死该进程
//                    count++;//杀死进程的计数+1
//                    nameList += "  " + pkName;
                }
            }
//            Log.i(TAG, nameList+"---------------------");
        }

//        long afterMem = getAvailMemory(context);//清理后的内存占用
//        Utils.toast(context, "杀死 " + (count + 1) + " 个进程, 释放"
//                + formatFileSize(context,afterMem - beforeMem) + "内存");
//        Log.i(TAG, "清理后可用内存为 : " + afterMem);
//        Log.i(TAG, "清理进程数量为 : " + count+1);

    }

    /*
   * *获取可用内存大小
   */
    private static long getAvailMemory(Context context) {
        // 获取android当前可用内存大小
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem;
    }

    /*
         * *字符串转换 long-string KB/MB
         */
    private static String formatFileSize(Context context, long number) {
        return Formatter.formatFileSize(context, number);
    }

    /**
     * 获取当前系统连接网络的网卡的mac地址
     * @return
     */
    public static String getCurrentMac() {
        byte[] mac = null;
        StringBuffer sb = new StringBuffer();
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> address = ni.getInetAddresses();
                while (address.hasMoreElements()) {
                    InetAddress ip = address.nextElement();
                    if (ip.isAnyLocalAddress() || !(ip instanceof Inet4Address) || ip.isLoopbackAddress())
                        continue;
                    if (ip.isSiteLocalAddress())
                        mac = ni.getHardwareAddress();
                    else if (!ip.isLinkLocalAddress()) {
                        mac = ni.getHardwareAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        if(mac != null){
            for(int i=0 ;i<mac.length ;i++){
                sb.append(parseByte(mac[i]));
            }
            return sb.substring(0, sb.length()-1);
        }
        return null;
    }

    private static String parseByte(byte b) {
        String s = "00" + Integer.toHexString(b)+":";
        return s.substring(s.length() - 3);
    }

    /**
     * 获取设备Mac地址
     *
     * @return
     */
    public static String getMac() {
        String macSerial = null;
        String str = "";

        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        return macSerial;
    }

    // 获取本机的物理地址
    public static String getLocalMacAddress(Context context) {
        WifiManager wifi = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }

    /**
     * 获取应用版本号
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            return pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取应用版本名
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String formatDate(long time){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return sdf.format(time);
    }
}
