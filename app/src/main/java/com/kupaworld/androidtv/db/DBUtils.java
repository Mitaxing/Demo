package com.kupaworld.androidtv.db;

import com.kupaworld.androidtv.application.SysApplication;
import com.kupaworld.androidtv.entity.App;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.DbException;

/**
 * Created by admin on 2017/5/16.
 */

public class DBUtils {

    private static DbUtils dbUtils = SysApplication.dbUtils;

    public static App getOtherPackageName(String type) {
        App app = null;
        try {
            app = dbUtils.findById(App.class, type);
        } catch (DbException e) {
            e.printStackTrace();
        }
        return app;
    }

    /**
     * 没有并保存
     * @param app
     */
    public static void noSoSave(App app) {
        try {
            App a = dbUtils.findById(App.class, app.getType());
            if (null == a) {
                dbUtils.save(app);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }
}
