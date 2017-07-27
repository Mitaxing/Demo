package com.kupaworld.androidtv.entity;

import java.io.Serializable;

/**
 * Created by MiTa on 2016/11/15.
 */

public class Music implements Serializable {

    private static final long serialVersionUID = 1L;

    public int id; //id标识
    public String title; // 显示名称
    public String display_name; // 文件名称
    public String path; // 音乐文件的路径
    public int duration; // 媒体播放总时间
    public String albums; // 专辑
    public String artist; // 艺术家
    public String singer; //歌手
    public long size;

}