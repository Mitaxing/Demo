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

    /**
     * 通过类型获取包名
     *
     * @param type 应用类型
     * @return 应用包名
     */
    public static App getOtherPackageName(String type) {
        App app = null;
        try {
            app = dbUtils.findById(App.class, type);
        } catch (DbException e) {
            e.printStackTrace();
            Utils.log("查询失败：" + e.getMessage());
        }
        return app;
    }

    /**
     * 没有并保存
     *
     * @param app 应用
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
