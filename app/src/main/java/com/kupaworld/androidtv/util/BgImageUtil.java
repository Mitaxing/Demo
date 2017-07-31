package com.kupaworld.androidtv.util;

import com.kupaworld.androidtv.application.SysApplication;
import com.kupaworld.androidtv.entity.BgImage;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.DbException;
import java.util.List;

/**
 * Created by Mita on 2017/6/21.
 */

public class BgImageUtil {

    private static DbUtils dbUtils = SysApplication.dbUtils;

    /**
     * 保存背景图片信息
     *
     * @param list
     */
    public static void saveImages(List<BgImage> list) {
        try {
            dbUtils.deleteAll(BgImage.class);
            dbUtils.saveAll(list);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询所以图片信息
     *
     * @return
     */
    public static List<BgImage> queryImageInfo() {
        try {
            return dbUtils.findAll(BgImage.class);
        } catch (DbException e) {
            e.printStackTrace();
        }
        return null;
    }
}
