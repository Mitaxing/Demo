package com.kupaworld.androidtv.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

import com.kupaworld.androidtv.R;
import com.kupaworld.androidtv.adapter.AppsAdapter;
import com.kupaworld.androidtv.entity.AppInfo;
import com.kupaworld.androidtv.util.Contacts;
import com.kupaworld.androidtv.util.Toastor;
import com.kupaworld.androidtv.util.Utils;
import com.kupaworld.androidtvwidget.bridge.EffectNoDrawBridge;
import com.kupaworld.androidtvwidget.view.GridViewTV;
import com.kupaworld.androidtvwidget.view.MainUpView;

import java.util.ArrayList;
import java.util.List;

/**
 * MyApp
 */
public class MyAppActivity extends BaseActivity implements AdapterView.OnItemLongClickListener, OnItemSelectedListener, OnItemClickListener {
    private final static String TAG = "MyAppActivity";
    private List<AppInfo> mlistAppInfo = new ArrayList<>();
    private MainUpView mainUpView1;
    private View mOldView;
    private TextView mNoApp;
    private GridViewTV gridView, mGvCover;
    private AppsAdapter mAdapter;
    private boolean isCover, isFirst;

    private int mSavePos = 0, mAppListSize;
    private Bitmap bm;

    private int[] appIcons = {R.mipmap.icon_yingshikuaisou, R.mipmap.icon_wps, R.mipmap.icon_lebo, R.mipmap.icon_qipo, R.mipmap.icon_music,
            R.mipmap.icon_headnews, R.mipmap.icon_kyx, R.mipmap.icon_sougou, R.mipmap.icon_feishi};
    private String[] appNames = {"影视快搜", "WPS Office", "乐播投屏", "奇珀市场", "QQ音乐", "头条资讯", "快游戏", "搜狗输入法TV版", "飞视浏览器"};
    private String[] appPackages = {"com.tv.kuaisou", "cn.wps.moffice_eng", "com.hpplay.happyplay.aw", "com.guozi.appstore", "com.tencent.qqmusictv",
            "com.tv.topnews", "com.kuaiyouxi.tv.market", "com.sohu.inputmethod.sogouoem", "com.android.letv.browser"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myapp);
        getViews();
        updateTime(this);
        registerInstalledReceiver();
        initUpView();
        initFalseApps();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.sendEmptyMessageDelayed(1, 100);
    }

    private void initFalseApps() {
        SharedPreferences sp = getSharedPreferences("kupaTV", 0);
        isFirst = sp.getBoolean("isFirst", true);
        if (isFirst) {
            int len = appIcons.length;
            for (int i = 0; i < len; i++) {
                AppInfo info = new AppInfo();
                info.setAppName(appNames[i]);
                info.setPackageName(appPackages[i]);
                bm = BitmapFactory.decodeResource(getResources(), appIcons[i]);
                BitmapDrawable bd = new BitmapDrawable(bm);
                info.setAppIcon(bd);
                mlistAppInfo.add(info);
            }
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("isFirst", false).apply();
            initGridView(gridView);
        } else {
            // 加载数据.
            getAllApps();
            initGridView(gridView);
            showNoTips();
            handler.sendEmptyMessageDelayed(1, 100);
        }
    }

    private void getAllApps() {
        PackageManager packageManager = getApplication().getPackageManager();
        List<PackageInfo> paklist = packageManager.getInstalledPackages(0);
        if (mlistAppInfo != null) {
            mlistAppInfo.clear();
            for (int i = 0; i < paklist.size(); i++) {
                PackageInfo pak = paklist.get(i);
                if ((pak.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0 && !pak.packageName.equals(getPackageName()) && !pak.packageName.equals(Contacts.SETTING_PACKAGE)) {
                    addAppInfo(pak, packageManager);
                }
            }
            mAppListSize = mlistAppInfo.size();
        }
    }

    /**
     * 添加应用信息
     *
     * @param pak
     * @param packageManager
     */
    private void addAppInfo(PackageInfo pak, PackageManager packageManager) {
        AppInfo appInfo = new AppInfo();
        appInfo.setAppName((String) pak.applicationInfo.loadLabel(packageManager));
        appInfo.setAppIcon(pak.applicationInfo.loadIcon(packageManager));
        appInfo.setPackageName(pak.packageName);
        mlistAppInfo.add(appInfo);
    }

    /**
     * 初始化移动边框
     */
    private void initUpView() {
        // 建议使用 NoDraw.
        mainUpView1.setEffectBridge(new EffectNoDrawBridge());
        EffectNoDrawBridge bridget = (EffectNoDrawBridge) mainUpView1.getEffectBridge();
        bridget.setTranDurAnimTime(200);
        // 设置移动边框的图片.
        mainUpView1.setUpRectResource(R.drawable.white_light_10);
        mainUpView1.setDrawUpRectPadding(new RectF(6, 7, 6, 8));
    }

    /**
     * 初始化已安装应用列表
     */
    private void initGridView(GridViewTV gridView) {
        mAdapter = new AppsAdapter(this, mlistAppInfo);
        gridView.setAdapter(mAdapter);
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        gridView.setOnItemLongClickListener(this);
        gridView.setOnItemSelectedListener(this);
        gridView.setOnItemClickListener(this);
    }

    @Override
    protected void onDestroy() {
        if (installedReceiver != null)
            unregisterReceiver(installedReceiver);
        if (null != bm && !bm.isRecycled()) {
            bm.recycle();
            System.gc();
            bm = null;
        }
        super.onDestroy();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    //延时请求选择的位置
                    gridView.setDefualtSelect(mSavePos);
                    break;

                case 2:
                    mGvCover.setDefualtSelect(0);
                    break;
            }
        }
    };

    /**
     * 刷新聚焦
     */
    private void refreshFocus() {
        if (mSavePos <= mAppListSize) {
            gridView.setDefualtSelect(mSavePos);
        }
        if (mAppListSize == 1) {
            isCover = true;
            mGvCover.setVisibility(View.VISIBLE);
            gridView.setVisibility(View.GONE);
            initGridView(mGvCover);
            handler.sendEmptyMessageDelayed(2, 100);
        }
        showNoTips();
    }

    /**
     * 显示无应用提示
     */
    private void showNoTips() {
        if (mAppListSize == 0) {
            gridView.setVisibility(View.GONE);
            mGvCover.setVisibility(View.GONE);
            mNoApp.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        mSavePos = position;
        Utils.uninstallApk(this, mlistAppInfo.get(position).getPackageName());
        return true;
    }

    private MyInstalledReceiver installedReceiver;

    private void registerInstalledReceiver() {
        installedReceiver = new MyInstalledReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addDataScheme("package");
        registerReceiver(installedReceiver, filter);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        /**
         * 这里注意要加判断是否为NULL.
         * 因为在重新加载数据以后会出问题.
         */
        if (view != null) {
            mainUpView1.setFocusView(view, mOldView, 1.1f);
            if (mOldView != null)
                mOldView.setBackgroundResource(R.drawable.layout_setting_bg);
            view.setBackgroundResource(R.drawable.layout_setting_focus);
        }
        mOldView = view;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        mSavePos = position;
        try {
            PackageManager packageManager = getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(mlistAppInfo.get(position).getPackageName());
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            showToast("正在安装，请等待");
        }
    }

    public class MyInstalledReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
                isFirst = false;
            }
            if (!isFirst) {
                getAllApps();
                mAppListSize = mlistAppInfo.size();
                if (isCover)
                    mGvCover.setAdapter(new AppsAdapter(MyAppActivity.this, mlistAppInfo));
                else
                    gridView.setAdapter(new AppsAdapter(MyAppActivity.this, mlistAppInfo));
                refreshFocus();
            }
        }
    }

    private void getViews() {
        gridView = (GridViewTV) findViewById(R.id.gridView_myApp);
        mGvCover = (GridViewTV) findViewById(R.id.gridView_cover);
        mainUpView1 = (MainUpView) findViewById(R.id.mainUpView1);
        mNoApp = (TextView) findViewById(R.id.tv_noapp);
    }
}