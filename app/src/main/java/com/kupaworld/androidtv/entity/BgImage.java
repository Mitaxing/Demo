package com.kupaworld.androidtv.entity;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;

/**
 * Created by Mita on 2017/6/21.
 */
@Table(name = "BgImage")
public class BgImage {

    @Id()
    private int id;
    @Column()
    private String type;
    @Column()
    private String url;

    public BgImage() {
    }

    public BgImage(int id, String type, String url) {
        this.id = id;
        this.type = type;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
