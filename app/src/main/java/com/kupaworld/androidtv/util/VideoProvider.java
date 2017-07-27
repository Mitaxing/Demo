package com.kupaworld.androidtv.util;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.kupaworld.androidtv.entity.Music;
import com.kupaworld.androidtv.entity.Video;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MiTa on 2016/11/15.
 */

public class VideoProvider implements AbstructProvider {
    private Context context;

    public VideoProvider(Context context) {
        this.context = context;
    }

    @Override
    public List<Video> getList() {
        List<Video> list = null;
        if (context != null) {
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null,
                    null, null);
            if (cursor != null) {
                list = new ArrayList<Video>();
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor
                            .getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                    String title = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                    String album = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM));
                    String artist = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST));
                    String displayName = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                    String mimeType = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
                    String path = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    long duration = cursor
                            .getInt(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                    long size = cursor
                            .getLong(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                    Video video = new Video(id, title, album, artist, displayName, mimeType, path, size, duration);
                    list.add(video);
                }
                cursor.close();
            }
        }
        return list;
    }

    public static List<Music> getAllMediaList(Context context) {
        Cursor cursor = null;
        List<Music> mediaList = new ArrayList<>();
        try {
            cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            if (cursor == null) {
                return mediaList;
            }
            int count = cursor.getCount();
            if (count <= 0) {
                return mediaList;
            }
            mediaList = new ArrayList<>();
            Music mediaEntity;
//          String[] columns = cursor.getColumnNames();
            while (cursor.moveToNext()) {
                mediaEntity = new Music();
                mediaEntity.id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                mediaEntity.title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                mediaEntity.display_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                mediaEntity.duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                mediaEntity.size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
//                mediaEntity.durationStr = longToStrTime(mediaEntity.duration);

                if (!checkIsMusic(mediaEntity.duration, mediaEntity.size)) {
                    continue;
                }
                mediaEntity.artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                mediaEntity.path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                mediaList.add(mediaEntity);
            }
        } catch (Exception e) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return mediaList;
    }

    /**
     * 根据时间和大小，来判断所筛选的media 是否为音乐文件，具体规则为筛选小于30秒和1m一下的
     */
    public static boolean checkIsMusic(int time, long size) {
        if (time <= 0 || size <= 0) {
            return false;
        }

        time /= 1000;
        int minute = time / 60;
//  int hour = minute / 60;
        int second = time % 60;
        minute %= 60;
        if (minute <= 0 && second <= 30) {
            return false;
        }
        if (size <= 1024 * 1024) {
            return false;
        }
        return true;
    }

    public static List<Music> getAllImageList(Context context) {
        Cursor cursor = null;
        List<Music> mediaList = new ArrayList<>();
        try {
            cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, null, null, MediaStore.Images.Media.DEFAULT_SORT_ORDER);
            if (cursor == null) {
                return mediaList;
            }
            int count = cursor.getCount();
            if (count <= 0) {
                return mediaList;
            }
            mediaList = new ArrayList<>();
            Music mediaEntity;
//          String[] columns = cursor.getColumnNames();
            while (cursor.moveToNext()) {
                mediaEntity = new Music();
                mediaEntity.id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                mediaEntity.title = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.TITLE));
                mediaEntity.display_name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                mediaEntity.duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE));
                mediaEntity.size = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE));
//                mediaEntity.durationStr = longToStrTime(mediaEntity.duration);
//                if (!checkIsMusic(mediaEntity.duration, mediaEntity.size)) {
//                    continue;
//                }
                mediaEntity.artist = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DESCRIPTION));
                mediaEntity.path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                mediaList.add(mediaEntity);
            }
        } catch (Exception e) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return mediaList;
    }
}
