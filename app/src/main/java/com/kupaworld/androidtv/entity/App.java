package com.kupaworld.androidtv.entity;


import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.NoAutoIncrement;
import com.lidroid.xutils.db.annotation.Table;

/**
 * Created by admin on 2017/5/16.
 */
@Table(name = "App")
public class App {

    @Column
    private int id;
    @Column()
    private String name;
    @Column()
    private String url;
    @Column()
    private String packageName;
    @Id()
    @NoAutoIncrement
    private String type;

    //默认的构造方法必须写出，如果没有，这张表是创建不成功的
    public App() {
    }

    public App(int id, String name, String url, String packageName, String type) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.packageName = packageName;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
