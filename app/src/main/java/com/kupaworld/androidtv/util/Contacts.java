package com.kupaworld.androidtv.util;

import android.os.Environment;

/**
 * Created by admin on 2016/12/19.
 */

public class Contacts {

    public static String KUPA_DOMAIN_NAME = "http://www.kupaworld.cn:1226";

    public static String GET_TYPE_CHILD_URL = KUPA_DOMAIN_NAME + "/Tv/GetTypeChildServlet";
    public static String URL_SYSTEM_UPDATE = KUPA_DOMAIN_NAME + "/Tv/VersionUpdateHandleServlet";
    //查询影视、音乐、游戏图片更新信息
    public static String URL_IMAGE_UPDATE = KUPA_DOMAIN_NAME + "/Tv/CheckImgUpdateServlet";

    public static final String SETTING_PACKAGE = "com.android.setting";
    public static final String BASE_PATH = Environment.getExternalStorageDirectory().toString() + "/";
    public static final String PATH_UPDATE = Contacts.BASE_PATH + "update.zip";

    public static final String TYPE_LAUNCHER = "launcher";
    public static final String TYPE_SETTING = "setting";
    public static final String TYPE_MOVIE = "movie";
    public static final String TYPE_MUSIC = "music";
    public static final String TYPE_GAME = "game";
    public static final String TYPE_NEWS = "news";
    public static final String TYPE_BROWSER = "browser";
    public static final String TYPE_MARKET = "appmarket";

    public static final String PACKAGE_MOVIE = "com.tv.kuaisou";
    public static final String PACKAGE_MUSIC = "com.tencent.qqmusictv";
    public static final String PACKAGE_GAME = "com.kuaiyouxi.tv.market";
    public static final String PACKAGE_NEWS = "com.tv.topnews";
    public static final String PACKAGE_BROWSER = "com.android.letv.browser";
    public static final String PACKAGE_MARKET = "com.guozi.appstore";
    public static final String PACKAGE_LEBO = "com.hpplay.happyplay.aw";
    public static final String PACKAGE_WPS = "cn.wps.moffice_eng";
    public static final String PACKAGE_SOGOU = "com.sohu.inputmethod.sogouoem";
    public static final String PACKAGE_SHOW = "com.amlogic.projector";

    public static final String NETWORK_STATE_CHANGED_ACTION = "android.net.ethernet.STATE_CHANGE";
    public static final String ETHERNET_STATE_CHANGED_ACTION = "android.net.ethernet.ETHERNET_STATE_CHANGE";
    public static final String EXTRA_ETHERNET_STATE = "ethernet_state";
    public static final int ETHERNET_STATE_DISABLED = 0;
    public static final int ETHERNET_STATE_ENABLED = 1;


}