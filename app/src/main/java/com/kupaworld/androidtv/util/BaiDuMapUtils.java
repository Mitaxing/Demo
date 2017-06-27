package com.kupaworld.androidtv.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClientOption;

/**
 * Created by admin on 2017/5/27.
 */

public class BaiDuMapUtils {

    public static boolean isLocatSuccess(BDLocation location) {
        if (location.getLocType() == BDLocation.TypeGpsLocation) {
            // GPS定位结果
            return true;
        } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
            // 网络定位结果
            return true;
        } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {
            // 离线定位结果
            return true;
        } else if (location.getLocType() == BDLocation.TypeServerError) {
            Utils.log("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
        } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
            Utils.log("网络不同导致定位失败，请检查网络是否通畅");
        } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
            Utils.log("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
        }
        return false;
    }

    /**
     * 配置百度地图定位参数
     */
    public static LocationClientOption getOption() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1;
        option.setScanSpan(span);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps
        option.setLocationNotify(false);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        return option;
    }

    /**
     * 保存地址信息
     *
     * @param context
     * @param address
     */
    public static void saveAddress(Context context, String address) {
        SharedPreferences sp = context.getSharedPreferences("kupaTV", 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("address", address).apply();
    }

    /**
     * 获取位置信息
     *
     * @param context
     * @return
     */
    public static String getAddress(Context context) {
        SharedPreferences sp = context.getSharedPreferences("kupaTV", 0);
        return sp.getString("address", "暂无位置信息");
    }
}
