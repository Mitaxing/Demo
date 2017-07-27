package com.kupaworld.androidtv.util;

import android.text.TextUtils;

import com.kupaworld.androidtv.entity.App;
import com.kupaworld.androidtv.entity.BgImage;
import com.kupaworld.androidtv.entity.SystemInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by admin on 2017/6/2.
 */

public class JsonUtils {

    private static List<App> apkList = new ArrayList<>();
    private static List<BgImage> imgList = new ArrayList<>();

    public static SystemInfo resolveResult(String result) {
        SystemInfo info = null;
        JSONTokener parse = new JSONTokener(result);
        try {
            JSONObject object = (JSONObject) parse.nextValue();
            String res = object.optString("res", "no-update-version");
            if (res.equals("ok")) {
                info = new SystemInfo();
                info.setResult(res);
                JSONObject infoObj = object.optJSONObject("info");
                info.setVersionId(infoObj.optInt("ktpsvNum"));
                info.setVersionName(infoObj.optString("ktpsvName"));
                info.setVersionInformation(infoObj.optString("ktpsvDescribe"));
                info.setVersionDownloadUrl(infoObj.optString("ktpsvDownload"));
                info.setUpdateTime(infoObj.optLong("ktpsvUpdateTime"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return info;
    }

    public static boolean resolveProductResult(String result) {
        boolean hasImg = false;
        try {
            JSONTokener parse = new JSONTokener(result);
            JSONObject object = (JSONObject) parse.nextValue();
            String res = object.optString("res", "error");
            if (res.equals("ok")) {
                JSONObject info = object.optJSONObject("info");
                if (info != null) {

                    //apk信息
                    JSONObject apkObj = info.optJSONObject("mKupaTvProductSystemApk");
                    apkList.clear();
                    resolveApkInfo(apkObj);
                    AppUtils.saveApps(apkList);

                    //背景图信息
                    JSONObject bgObj = info.optJSONObject("mKupaTvProductSystemBackground");
                    imgList.clear();
                    hasImg = resolveImgInfo(bgObj);
                    BgImageUtil.saveImages(imgList);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return hasImg;
    }

    private static void resolveApkInfo(JSONObject object) {
        JSONObject apkObj = object.optJSONObject("mKupaTvProductSystemApkBrowser");
        App app = new App(
                apkObj.optInt("ktpsabNum"),
                apkObj.optString("ktpsabName"),
                apkObj.optString("ktpsabDownload"),
                apkObj.optString("ktpsabPackage"),
                Contacts.TYPE_BROWSER
        );
        apkList.add(app);

        apkObj = object.optJSONObject("mKupaTvProductSystemApkFilm");
        app = new App(
                apkObj.optInt("ktpsafNum"),
                apkObj.optString("ktpsafName"),
                apkObj.optString("ktpsafDownload"),
                apkObj.optString("ktpsafPackage"),
                Contacts.TYPE_MOVIE
        );
        apkList.add(app);

        apkObj = object.optJSONObject("mKupaTvProductSystemApkGame");
        app = new App(
                apkObj.optInt("ktpsagNum"),
                apkObj.optString("ktpsagName"),
                apkObj.optString("ktpsagDownload"),
                apkObj.optString("ktpsagPackage"),
                Contacts.TYPE_GAME
        );
        apkList.add(app);

        apkObj = object.optJSONObject("mKupaTvProductSystemApkMusic");
        app = new App(
                apkObj.optInt("ktpsamNum"),
                apkObj.optString("ktpsamName"),
                apkObj.optString("ktpsamDownload"),
                apkObj.optString("ktpsamPackage"),
                Contacts.TYPE_MUSIC
        );
        apkList.add(app);

        apkObj = object.optJSONObject("mKupaTvProductSystemApkNews");
        app = new App(
                apkObj.optInt("ktpsanNum"),
                apkObj.optString("ktpsanName"),
                apkObj.optString("ktpsanDownload"),
                apkObj.optString("ktpsanPackage"),
                Contacts.TYPE_NEWS
        );
        apkList.add(app);

        apkObj = object.optJSONObject("mKupaTvProductSystemApkStore");
        app = new App(
                apkObj.optInt("ktpsasNum"),
                apkObj.optString("ktpsasName"),
                apkObj.optString("ktpsasDownload"),
                apkObj.optString("ktpsasPackage"),
                Contacts.TYPE_MARKET
        );
        apkList.add(app);

        apkObj = object.optJSONObject("mkupaTvProductSystemApkDocument");
        app = new App(
                apkObj.optInt("ktpsadNum"),
                apkObj.optString("ktpsadName"),
                apkObj.optString("ktpsadDownload"),
                apkObj.optString("ktpsadPackage"),
                Contacts.TYPE_DOCUMENT
        );
        apkList.add(app);
    }

    private static boolean resolveImgInfo(JSONObject object) {
        boolean hasImg = false;
        JSONObject imgObj = object.optJSONObject("mKupaTvProductSystemBackgroundFilm");
        int id = imgObj.optInt("ktpsbfNum");
        String url = imgObj.optString("ktpsbfUrlFirst");
        if (!TextUtils.isEmpty(url))
            hasImg = true;
        BgImage image = new BgImage(
                id, Contacts.TYPE_MOVIE, imgObj.optString("ktpsbfUrlFirst")
        );
        imgList.add(image);

        image = new BgImage(
                id, Contacts.TYPE_MOVIE, imgObj.optString("ktpsbfUrlSecond")
        );
        imgList.add(image);

        image = new BgImage(
                id, Contacts.TYPE_MOVIE, imgObj.optString("ktpsbfUrlThird")
        );
        imgList.add(image);

        imgObj = object.optJSONObject("mKupaTvProductSystemBackgroundMusic");
        image = new BgImage(
                id, Contacts.TYPE_MUSIC, imgObj.optString("ktpsbmUrlFirst")
        );
        imgList.add(image);

        image = new BgImage(
                id, Contacts.TYPE_MUSIC, imgObj.optString("ktpsbmUrlSecond")
        );
        imgList.add(image);

        image = new BgImage(
                id, Contacts.TYPE_MUSIC, imgObj.optString("ktpsbmUrlThird")
        );
        imgList.add(image);

        imgObj = object.optJSONObject("mKupaTvProductSystemBackgroundGame");

        image = new BgImage(
                id, Contacts.TYPE_GAME, imgObj.optString("ktpsbgUrlFirst")
        );
        imgList.add(image);

        image = new BgImage(
                id, Contacts.TYPE_GAME, imgObj.optString("ktpsbgUrlSecond")
        );
        imgList.add(image);

        image = new BgImage(
                id, Contacts.TYPE_GAME, imgObj.optString("ktpsbgUrlThird")
        );
        imgList.add(image);
        return hasImg;
    }
}
