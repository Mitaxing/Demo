package com.kupaworld.androidtv.db;

import com.kupaworld.androidtv.application.SysApplication;
import com.kupaworld.androidtv.entity.App;
import com.kupaworld.androidtv.util.Utils;
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
            Utils.log("查询的包名：" + app.getPackageName());
        } catch (DbException e) {
            e.printStackTrace();
            Utils.log("查询失败：" + e.getMessage());
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
//    private static DbManager db;
//
//    private static DbManager getManager() {
//        DbManager.DaoConfig daoConfig = DBConfig.getDaoConfig();
//        return x.getDb(daoConfig);
//    }
//
//    /**
//     * 保存内置应用信息
//     *
//     * @param app
//     */
//    public static void saveApp(App app) {
//        try {
//            db = getManager();
//            db.saveOrUpdate(app);
//        } catch (DbException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 查询第三方应用信息
//     *
//     * @return
//     */
//    public static App getOtherPackageName(String type) {
//        App app = null;
//        try {
//            db = getManager();
//            app = db.findById(App.class, type);
//        } catch (DbException e) {
//            e.printStackTrace();
//        }
//        return app;
//    }
}
