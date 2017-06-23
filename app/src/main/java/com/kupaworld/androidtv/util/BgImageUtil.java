package com.kupaworld.androidtv.util;

import com.kupaworld.androidtv.application.SysApplication;
import com.kupaworld.androidtv.entity.BgImage;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mita on 2017/6/21.
 */

public class BgImageUtil {

    private static DbUtils dbUtils = SysApplication.dbUtils;

    /**
     * 查询数据库图片是否更新
     */
    public static void requestBgInfo() {
        HttpUtils httpUtils = new HttpUtils();
        httpUtils.send(HttpRequest.HttpMethod.POST, Contacts.KUPA_DOMAIN_NAME + Contacts.URL_IMAGE_UPDATE, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Utils.log("查询成功：" + responseInfo.result);
                resolveImg(responseInfo.result);
            }

            @Override
            public void onFailure(HttpException e, String s) {
                Utils.log("查询失败：" + s);
            }
        });
    }

    /**
     * 解析返回信息
     *
     * @param result
     */
    public static void resolveImg(String result) {
        List<BgImage> list = new ArrayList<>();
        JSONTokener parse = new JSONTokener(result);
        try {
            JSONObject object = (JSONObject) parse.nextValue();
            if (object.optString("res", "error").equals("ok")) {
                JSONArray images = object.optJSONArray("imgs");
                int len = images.length();
                for (int i = 0; i < len; i++) {
                    JSONObject imgObj = images.optJSONObject(i);
                    BgImage image = new BgImage(imgObj.optInt("id"), imgObj.optString("type"), imgObj.optString("url"));
                    list.add(image);
                }
                saveImages(list);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存背景图片信息
     *
     * @param list
     */
    private static void saveImages(List<BgImage> list) {
        try {
            dbUtils.deleteAll(BgImage.class);
            dbUtils.saveAll(list);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询所以图片信息
     * @return
     */
    public static List<BgImage> queryImageInfo(){
        try {
            return dbUtils.findAll(BgImage.class);
        } catch (DbException e) {
            e.printStackTrace();
        }
        return null;
    }
}
