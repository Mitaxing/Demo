package com.kupaworld.androidtv.util;

import com.kupaworld.androidtv.application.SysApplication;
import com.kupaworld.androidtv.entity.App;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.DbException;

import java.util.List;

/**
 * Created by admin on 2017/7/26.
 */

public class AppUtils {

    private static DbUtils dbUtils = SysApplication.dbUtils;

    /**
     * 保存app信息
     *
     * @param list
     */
    public static void saveApps(List<App> list) {
        try {
            dbUtils.deleteAll(App.class);
            dbUtils.saveAll(list);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询app信息
     *
     * @return
     */
    public static App queryAppInfo(String type) {
        try {
            return dbUtils.findById(App.class, type);
        } catch (DbException e) {
            e.printStackTrace();
        }
        return null;
    }
}
