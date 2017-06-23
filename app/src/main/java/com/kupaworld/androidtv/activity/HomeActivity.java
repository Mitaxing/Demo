package com.kupaworld.androidtv.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.IPackageManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.kupaworld.androidtv.R;
import com.kupaworld.androidtv.application.SysApplication;
import com.kupaworld.androidtv.db.DBUtils;
import com.kupaworld.androidtv.download.DownloadInfo;
import com.kupaworld.androidtv.download.DownloadManager;
import com.kupaworld.androidtv.download.DownloadService;
import com.kupaworld.androidtv.entity.App;
import com.kupaworld.androidtv.entity.BgImage;
import com.kupaworld.androidtv.entity.SystemInfo;
import com.kupaworld.androidtv.util.BgImageUtil;
import com.kupaworld.androidtv.util.Contacts;
import com.kupaworld.androidtv.util.JsonUtils;
import com.kupaworld.androidtv.util.UpdateUtils;
import com.kupaworld.androidtv.util.Utils;
import com.kupaworld.androidtv.view.FreshDownloadView;
import com.kupaworld.androidtv.view.TipDialog;
import com.kupaworld.androidtvwidget.view.FrameMainLayout;
import com.kupaworld.androidtvwidget.view.MainUpView;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 1280*720
 * Created by MiTa on 2016/11/30.
 */

public class HomeActivity extends BaseActivity implements View.OnClickListener {

    private MainUpView mainUpView1;
    private View mOldFocus; // 4.3以下版本需要自己保存.

    private ImageView mMovieIcon, mMusicIcon, mGameIcon;
    private ImageView currentView, currentOverView;
    private ImageView mIvMovie, mIvMusic, mIvGame, mIvOverMovie, mIvOverMusic, mIvOverGame;

    private TextView mMovieLife, mMusicArea, mGameCenter, mTvNews, mTvIe, mTvStore, mTvMovieTip, mTvMusicTip, mTvGameTip;
    private View coverMovie, coverMusic, coverGame, coverNews, coverIe, coverStore;

    private int[] resMovies = {R.mipmap.kupatv_mainpage_movie_one, R.mipmap.kupatv_mainpage_movie_two, R.mipmap.kupatv_mainpage_movie_three};
    private int[] resMusics = {R.mipmap.kupatv_mainpage_music_one, R.mipmap.kupatv_mainpage_music_two, R.mipmap.kupatv_mainpage_music_three};
    private int[] resGames = {R.mipmap.game_two, R.mipmap.kupatv_mainpage_game_two, R.mipmap.game_three};

    private List<String> movieUrl = new ArrayList<>();
    private List<String> musicUrl = new ArrayList<>();
    private List<String> gameUrl = new ArrayList<>();

    private boolean showOverMovie, showOverMusic, showOverGame, showMovieTip, showMusicTip, showGameTip;
    private int currentRes, currentModule, currentMovie, currentMusic, currentGame;
    private String currentUrl;
    private int REQUEST_TIMES;
    private int focusViewId;
    private SystemInfo systemInfo;

    private DisplayImageOptions options;
    private final String ALPHA = "alpha";
    private String imageUri = "drawable://"; // from drawables (only images, non-9patch)
    private ImageLoader imageLoader;
    private static boolean isChange;
    private ValueAnimator valueAnimator;

    private FreshDownloadView fdv_movie, fdv_music, fdv_game, fdv_news, fdv_ie, fdv_store;
    private DownloadManager downloadManager;

    private String[] apps = {Contacts.TYPE_MOVIE, Contacts.TYPE_MUSIC, Contacts.TYPE_GAME, Contacts.TYPE_NEWS, Contacts.TYPE_BROWSER, Contacts.TYPE_MARKET};

    private final int DEFAULT = -1;
    private final int STARTED = 1;
    private final int LOADING = 2;
    private final int FAILURE = 3;
    private final int INSTALL = 4;
    private final int SUCCESS = 5;

    //下载状态
    private int MOVIE_STATE = DEFAULT;
    private int MUSIC_STATE = DEFAULT;
    private int GAME_STATE = DEFAULT;
    private int IE_STATE = DEFAULT;
    private int NEWS_STATE = DEFAULT;
    private int STORE_STATE = DEFAULT;

    private DbUtils dbUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getViews();
        updateTime(this);
        initUpView();
        initImageLoader();
        downloadManager = DownloadService.getDownloadManager(getApplicationContext());
        downloadManager.getDownloadInfoList().clear();
        dbUtils = SysApplication.dbUtils;
        saveDefaultInfo();
        checkSystemUpdate();
        deleteUpdate();
    }

    /**
     * 删除下载的安装包
     */
    private void deleteUpdate() {
        File file = new File(Contacts.PATH_UPDATE);
        if (file.exists()) {
            boolean isDelete = file.delete();
            Utils.log("安装包删除：" + isDelete);
        }
    }

    /**
     * 检测系统升级
     */
    private void checkSystemUpdate() {
        String mac = Utils.getMac();
        if (!TextUtils.isEmpty(mac)) {
            int version = Utils.getVersionCode(this);
            HttpUtils http = new HttpUtils(10 * 1000);
            RequestParams params = new RequestParams();
            params.addBodyParameter("mac", mac);
            params.addBodyParameter("version", String.valueOf(version));
            http.send(HttpRequest.HttpMethod.POST, Contacts.URL_SYSTEM_UPDATE, params, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    systemInfo = JsonUtils.resolveResult(HomeActivity.this, responseInfo.result);
                    if (null != systemInfo) {
                        String result = systemInfo.getResult();
                        if ("ok".equals(result)) {
                            showUpdateDialog(systemInfo.getVersionName() + "：" + systemInfo.getVersionInformation());
                        }
                    }
                }

                @Override
                public void onFailure(HttpException e, String s) {
                    Utils.log("查询失败：" + s);
                }
            });
        }
    }

    private void checkAppInfoUpdate() {
        if (Utils.isNetworkAvailable(HomeActivity.this)) {
            int len = apps.length;
            for (int i = 0; i < len; i++) {
                requestApkInfo(apps[i], false);
            }
        }
    }

    /**
     * 添加下载任务
     *
     * @param type
     * @param url
     */
    private void addDownload(String type, String url, boolean isSystem) {
        try {
            String target = Contacts.BASE_PATH + type + ".apk";
            if (isSystem) {
                target = Contacts.PATH_UPDATE;
            }
            downloadManager.addNewDownload(url, type, target, isSystem, true, new RequestCallBack<File>() {

                @Override
                public void onSuccess(ResponseInfo<File> responseInfo) {
                    refreshState();
                }

                @Override
                public void onFailure(HttpException e, String s) {
                    refreshState();
                }

                @Override
                public void onLoading(long total, long current, boolean isUploading) {
                    super.onLoading(total, current, isUploading);
                    refreshState();
                }

                @Override
                public void onStart() {
                    super.onStart();
                    refreshState();
                }

            });
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析预安装第三方应用信息
     *
     * @param result
     */
    private void resolveAppInfo(String result, boolean onlyCheck) {
        try {
            JSONTokener parse = new JSONTokener(result);
            JSONObject object = (JSONObject) parse.nextValue();
            if (object.optInt("code") == 200) {
                JSONArray values = object.optJSONArray("apkinfos");
                if (values.length() > 0) {
                    JSONObject info = values.optJSONObject(0);
                    String type = info.optString("apktype");
                    App app = new App(info.optInt("apkId"),
                            info.optString("apkname"),
                            info.optString("apkdownloadurl"),
                            info.optString("apkpackagename"),
                            type);
                    try {
                        dbUtils.update(app);
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                    if (onlyCheck)
                        openOrDownload(type, true);
                }
            } else {
                Utils.toast(HomeActivity.this, "服务器异常");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 存储默认内置应用信息
     */
    private void saveDefaultInfo() {
        String[] types = {Contacts.TYPE_MOVIE, Contacts.TYPE_MUSIC, Contacts.TYPE_BROWSER, Contacts.TYPE_GAME,
                Contacts.TYPE_NEWS, Contacts.TYPE_MARKET};
        String[] packages = {Contacts.PACKAGE_MOVIE, Contacts.PACKAGE_MUSIC, Contacts.PACKAGE_BROWSER, Contacts.PACKAGE_GAME,
                Contacts.PACKAGE_NEWS, Contacts.PACKAGE_MARKET,};
        App app = new App();
        int len = types.length;
        for (int i = 0; i < len; i++) {
            app.setType(types[i]);
            app.setPackageName(packages[i]);
            DBUtils.noSoSave(app);
        }
    }

    /**
     * 查询需要安装的应用的信息
     *
     * @param appType
     */
    private void requestApkInfo(final String appType, final boolean onlyCheck) {
        if (!Utils.isNetworkAvailable(HomeActivity.this) && !onlyCheck) {//没网
            openOrDownload(appType, false);
        } else {
            RequestParams params = new RequestParams();
            params.addBodyParameter("appType", appType);
            HttpUtils httpUtils = new HttpUtils();
            httpUtils.send(HttpRequest.HttpMethod.POST, Contacts.GET_TYPE_CHILD_URL, params, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    resolveAppInfo(responseInfo.result, onlyCheck);
                }

                @Override
                public void onFailure(HttpException e, String s) {
                    Utils.toast(HomeActivity.this, "请检查网络连接~");
                }
            });
        }
    }

    private void initUpView() {
        mainUpView1 = (MainUpView) findViewById(R.id.mainUpView2);
        mainUpView1.setUpRectResource(R.drawable.white_light_10); // 设置移动边框的图片.
        mainUpView1.setDrawUpRectPadding(new RectF(-11, -9, -13, -11));
        mainUpView1.setDrawShadowPadding(-2); // 阴影图片设置距离.
        FrameMainLayout main_lay12 = (FrameMainLayout) findViewById(R.id.main_lay2);
        main_lay12.getViewTreeObserver().addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
            @Override
            public void onGlobalFocusChanged(final View oldFocus, View newFocus) {
                if (newFocus != null)
                    newFocus.bringToFront(); // 防止放大的view被压在下面. (建议使用MainLayout)
                float scale = 1.2f;
                hideOver();
                if (null != newFocus) {
                    focusViewId = newFocus.getId();
                    if (focusViewId == R.id.main_movie) {
                        if (MOVIE_STATE == DEFAULT) {
                            mMovieIcon.setVisibility(View.VISIBLE);
                            mMovieLife.setVisibility(View.GONE);
                            coverMovie.setVisibility(View.VISIBLE);
                            mTvMovieTip.setVisibility(View.VISIBLE);
                        }
                        showMovieTip = true;
                        scale = 1.05f;
                    } else if (focusViewId == R.id.main_music) {
                        if (MUSIC_STATE == DEFAULT) {
                            mMusicIcon.setVisibility(View.VISIBLE);
                            mMusicArea.setVisibility(View.GONE);
                            coverMusic.setVisibility(View.VISIBLE);
                            mTvMusicTip.setVisibility(View.VISIBLE);
                        }
                        showMusicTip = true;
                        scale = 1.05f;
                    } else if (focusViewId == R.id.main_game) {
                        if (GAME_STATE == DEFAULT) {
                            mGameIcon.setVisibility(View.VISIBLE);
                            mGameCenter.setVisibility(View.GONE);
                            coverGame.setVisibility(View.VISIBLE);
                            mTvGameTip.setVisibility(View.VISIBLE);
                        }
                        showGameTip = true;
                        scale = 1.05f;
                    }
                    mainUpView1.setFocusView(newFocus, mOldFocus, scale);
                    mOldFocus = newFocus; // 4.3以下需要自己保存.
                }
            }
        });
    }

    private void hideOver() {
        if (showMovieTip) {
            mMovieIcon.setVisibility(View.GONE);
            mMovieLife.setVisibility(View.VISIBLE);
            coverMovie.setVisibility(View.GONE);
            mTvMovieTip.setVisibility(View.GONE);
            showMovieTip = false;
        }
        if (showMusicTip) {
            mMusicIcon.setVisibility(View.GONE);
            mMusicArea.setVisibility(View.VISIBLE);
            coverMusic.setVisibility(View.GONE);
            mTvMusicTip.setVisibility(View.GONE);
            showMusicTip = false;
        }
        if (showGameTip) {
            mGameIcon.setVisibility(View.GONE);
            mGameCenter.setVisibility(View.VISIBLE);
            coverGame.setVisibility(View.GONE);
            mTvGameTip.setVisibility(View.GONE);
            showGameTip = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentModule = 0;
//        handler.sendEmptyMessage(1);
        checkAppInfoUpdate();
        Utils.killAll(this);
        REQUEST_TIMES = 0;
        requestBgInfo();
    }

    /**
     * 初始化图片链接
     */
    private void initImageUrl() {
        List<BgImage> list = BgImageUtil.queryImageInfo();
        if (null != list && list.size() > 0) {
            movieUrl.clear();
            musicUrl.clear();
            gameUrl.clear();
            for (BgImage image : list) {
                switch (image.getType()) {
                    case "movie":
                        movieUrl.add(image.getUrl());
                        break;

                    case "music":
                        musicUrl.add(image.getUrl());
                        break;

                    case "game":
                        gameUrl.add(image.getUrl());
                        break;
                }
            }
            handler.sendEmptyMessage(1);
        } else {
            if (REQUEST_TIMES < 3) {
                requestBgInfo();
                REQUEST_TIMES++;
            }
        }
    }

    /**
     * 查询数据库图片是否更新
     */
    private void requestBgInfo() {
        HttpUtils httpUtils = new HttpUtils(10 * 1000);
        httpUtils.send(HttpRequest.HttpMethod.GET, Contacts.URL_IMAGE_UPDATE, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                BgImageUtil.resolveImg(responseInfo.result);
                initImageUrl();
                handler.sendEmptyMessage(1);
            }

            @Override
            public void onFailure(HttpException e, String s) {
                Utils.log("背景图片查询失败：" + s);
            }
        });
    }

    @Override
    protected void onPause() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator.end();
        }
        mIvMovie.clearAnimation();
        mIvMusic.clearAnimation();
        mIvGame.clearAnimation();
        mIvOverMovie.clearAnimation();
        mIvOverMusic.clearAnimation();
        mIvOverGame.clearAnimation();
        handler.removeMessages(1);
        handler.removeMessages(2);
        super.onPause();
    }

    private void initAnimation() {
        switch (currentModule) {
            case 0:
                if (showOverMovie) {
                    currentView = mIvOverMovie;
                    currentOverView = mIvMovie;
                    showOverMovie = false;
                } else {
                    currentView = mIvMovie;
                    currentOverView = mIvOverMovie;
                    showOverMovie = true;
                }
                currentMovie++;
//                currentRes = resMovies[currentMovie % resMovies.length];
                currentUrl = movieUrl.get(currentMovie % movieUrl.size());
                currentModule = 1;
                break;

            case 1:
                if (showOverMusic) {
                    currentView = mIvOverMusic;
                    currentOverView = mIvMusic;
                    showOverMusic = false;
                } else {
                    currentView = mIvMusic;
                    currentOverView = mIvOverMusic;
                    showOverMusic = true;
                }
                currentMusic++;
//                currentRes = resMusics[currentMusic % resMusics.length];
                currentUrl = musicUrl.get(currentMusic % musicUrl.size());
                currentModule = 2;
                break;

            case 2:
                if (showOverGame) {
                    currentView = mIvOverGame;
                    currentOverView = mIvGame;
                    showOverGame = false;
                } else {
                    currentView = mIvGame;
                    currentOverView = mIvOverGame;
                    showOverGame = true;
                }
                currentGame++;
//                currentRes = resGames[currentGame % resGames.length];
                currentUrl = gameUrl.get(currentGame % gameUrl.size());
                currentModule = 0;
                break;
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    initAnimation();
                    isChange = true;
                    valueAnimator = ObjectAnimator.ofFloat(currentView, ALPHA, 1f, 0f);
                    valueAnimator.setDuration(4000);
                    valueAnimator.start();
                    handler.sendEmptyMessageDelayed(2, 2000);
                    break;

                case 2:
                    imageLoader.displayImage(Contacts.KUPA_DOMAIN_NAME + currentUrl, currentOverView, options, new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String s, View view) {

                        }

                        @Override
                        public void onLoadingFailed(String s, View view, FailReason failReason) {

                        }

                        @Override
                        public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                            currentOverView.setVisibility(View.VISIBLE);
                            valueAnimator = ObjectAnimator.ofFloat(currentOverView, ALPHA, 0.3f, 1f);
                            valueAnimator.setDuration(3000);
                            valueAnimator.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animator) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    if (isChange) {
                                        handler.sendEmptyMessage(1);
                                        isChange = false;
                                    }
                                }

                                @Override
                                public void onAnimationCancel(Animator animator) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animator) {

                                }
                            });
                            valueAnimator.start();
                        }

                        @Override
                        public void onLoadingCancelled(String s, View view) {

                        }
                    });
                    break;

                case 3:
                    hideDownloadState(msg.obj.toString(), true);
                    break;

                case 4:
                    DownloadInfo info = (DownloadInfo) msg.obj;
                    try {
                        downloadManager.removeDownload(info);
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                    hideDownloadState(info.getFileName(), false);
                    break;
            }
        }
    };

    /**
     * 刷新下载进度
     */
    private void refreshState() {
        List<DownloadInfo> list = downloadManager.getDownloadInfoList();
        for (int i = 0; i < list.size(); i++) {
            DownloadInfo info = list.get(i);
            String fileName = info.getFileName();
            int state = info.getState().value();
            int progress = 0;
            if (state == LOADING) {
                progress = (int) (info.getProgress() * 100 / info.getFileLength());
            }
            switch (fileName) {
                case Contacts.TYPE_MOVIE:
                    showState(fdv_movie, state, progress, info, mMovieLife, coverMovie);
                    MOVIE_STATE = state;
                    mMovieIcon.setVisibility(View.GONE);
                    break;

                case Contacts.TYPE_MUSIC:
                    showState(fdv_music, state, progress, info, mMusicArea, coverMusic);
                    MUSIC_STATE = state;
                    mMusicIcon.setVisibility(View.GONE);
                    break;

                case Contacts.TYPE_GAME:
                    showState(fdv_game, state, progress, info, mGameCenter, coverGame);
                    GAME_STATE = state;
                    mGameIcon.setVisibility(View.GONE);
                    break;

                case Contacts.TYPE_NEWS:
                    showState(fdv_news, state, progress, info, mTvNews, coverNews);
                    NEWS_STATE = state;
                    break;

                case Contacts.TYPE_BROWSER:
                    showState(fdv_ie, state, progress, info, mTvIe, coverIe);
                    IE_STATE = state;
                    break;

                case Contacts.TYPE_MARKET:
                    showState(fdv_store, state, progress, info, mTvStore, coverStore);
                    STORE_STATE = state;
                    break;

                case Contacts.TYPE_LAUNCHER:
                    if (state == SUCCESS) {
                        try {
                            UpdateUtils.installPackage(HomeActivity.this, new File(Contacts.PATH_UPDATE));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (state == FAILURE) {
                        try {
                            downloadManager.removeDownload(info);
                        } catch (DbException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }

    /**
     * 显示下载状态
     *
     * @param view
     * @param state
     * @param progress
     */
    private void showState(FreshDownloadView view, int state, int progress, DownloadInfo info, TextView mTvTip, View cover) {
        if (state == STARTED) {
            view.setVisibility(View.VISIBLE);
            view.startDownload();
            mTvTip.setText("开始下载");
            mTvTip.setVisibility(View.VISIBLE);
            cover.setVisibility(View.VISIBLE);
        } else if (state == LOADING) {
            view.upDateProgress(progress);
            mTvTip.setText("下载中...");
        } else if (state == SUCCESS) {
            try {
                mTvTip.setText("正在安装");
                view.showDownloadOk();
                downloadManager.removeDownload(info);
                backgroundInstall(info.getFileSavePath(), info.getFileName());
            } catch (DbException e) {
                e.printStackTrace();
            }
        } else {
            mTvTip.setText("下载失败");
            view.showDownloadError();
            view.reset();
            Message msg = new Message();
            msg.obj = info;
            msg.what = 4;
            handler.sendMessageDelayed(msg, 1500);
        }
    }

    /**
     * 初始化下载状态
     *
     * @param type
     */
    private void initState(String type) {
        Utils.log("初始化状态：" + type);
        switch (type) {
            case Contacts.TYPE_MOVIE:
                MOVIE_STATE = DEFAULT;
                break;

            case Contacts.TYPE_MUSIC:
                MUSIC_STATE = DEFAULT;
                break;

            case Contacts.TYPE_GAME:
                GAME_STATE = DEFAULT;
                break;

            case Contacts.TYPE_NEWS:
                NEWS_STATE = DEFAULT;
                break;

            case Contacts.TYPE_BROWSER:
                IE_STATE = DEFAULT;
                break;

            case Contacts.TYPE_MARKET:
                STORE_STATE = DEFAULT;
                break;
        }
    }

    /**
     * 静默安装
     */
    private void backgroundInstall(String path, final String name) {
        try {
            File apk = new File(path);
            Class<?> clazz = Class.forName("android.os.ServiceManager");
            Method method_getService = clazz.getMethod("getService",
                    String.class);
            IBinder bind = (IBinder) method_getService.invoke(null, "package");

            IPackageManager iPm = IPackageManager.Stub.asInterface(bind);
            iPm.installPackage(Uri.fromFile(apk), new IPackageInstallObserver.Stub() {
                        @Override
                        public void packageInstalled(String packageName, int returnCode) throws RemoteException {
                            Looper.prepare();
                            Message msg = new Message();
                            msg.obj = name;
                            msg.what = 3;
                            handler.sendMessage(msg);
                            Looper.loop();
                        }
                    }, 2,
                    apk.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideDownloadState(String type, boolean isSuccess) {
        initState(type);
        switch (type) {
            case Contacts.TYPE_MOVIE:
                type = "电影";
                hideOption(fdv_movie, mMovieLife, R.string.movie, coverMovie);
                if (focusViewId == R.id.main_movie) {
                    mMovieIcon.setVisibility(View.VISIBLE);
                    mMovieLife.setVisibility(View.GONE);
                    coverMovie.setVisibility(View.VISIBLE);
                    mTvMovieTip.setVisibility(View.VISIBLE);
                }
                break;

            case Contacts.TYPE_MUSIC:
                type = "音乐";
                hideOption(fdv_music, mMusicArea, R.string.music, coverMusic);
                if (focusViewId == R.id.main_music) {
                    mMusicIcon.setVisibility(View.VISIBLE);
                    mMusicArea.setVisibility(View.GONE);
                    coverMusic.setVisibility(View.VISIBLE);
                    mTvMusicTip.setVisibility(View.VISIBLE);
                }
                break;

            case Contacts.TYPE_GAME:
                type = "游戏";
                hideOption(fdv_game, mGameCenter, R.string.game, coverGame);
                if (focusViewId == R.id.main_game) {
                    mGameIcon.setVisibility(View.VISIBLE);
                    mGameCenter.setVisibility(View.GONE);
                    coverGame.setVisibility(View.VISIBLE);
                    mTvGameTip.setVisibility(View.VISIBLE);
                }
                break;

            case Contacts.TYPE_NEWS:
                type = "新闻";
                hideOption(fdv_news, mTvNews, R.string.news, coverNews);
                break;

            case Contacts.TYPE_BROWSER:
                type = "浏览器";
                hideOption(fdv_ie, mTvIe, R.string.browser, coverIe);
                break;

            case Contacts.TYPE_MARKET:
                type = "应用市场";
                hideOption(fdv_store, mTvStore, R.string.market, coverStore);
                break;

            default:
                type = "系统";
                break;
        }
        if (isSuccess)
            Utils.toast(HomeActivity.this, type + "应用安装完成");
    }

    private void hideOption(FreshDownloadView fdv, TextView textView, int res, View cover) {
        fdv.setVisibility(View.GONE);
        textView.setText(res);
        cover.setVisibility(View.GONE);
    }

    /**
     * 显示系统更新弹出框
     */
    private void showUpdateDialog(String msg) {
        final TipDialog dialog = new TipDialog(this, R.style.MyDialog);
        dialog.setEdit(false);
        dialog.setTitle("Kupa TV发现新版本");
        dialog.setMessage(msg);
        dialog.setYesText("升级");
        dialog.setCancleText("取消");
        dialog.setNoClickListener(new TipDialog.OnNoClickListener() {
            @Override
            public void onNoClick() {
                dialog.dismiss();
            }
        });
        dialog.setYesClickListener(new TipDialog.OnYesClickListener() {
            @Override
            public void onYesClick() {
                dialog.dismiss();
                showUpdateTips();
                addDownload(Contacts.TYPE_LAUNCHER, Contacts.KUPA_DOMAIN_NAME + systemInfo.getVersionDownloadUrl(), true);
            }
        });
        dialog.show();
    }

    /**
     * 显示升级进度提醒对话框
     */
    private void showUpdateTips() {
        final TipDialog dialog = new TipDialog(this, R.style.MyDialog);
        dialog.setEdit(false);
        dialog.setTitle("升级提示");
        dialog.setMessage("正在更新系统，可至\"设置 - 系统升级\"中查看进度");
        dialog.setYesText("好的");
        dialog.setYesClickListener(new TipDialog.OnYesClickListener() {
            @Override
            public void onYesClick() {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void initImageLoader() {
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)//设置下载的图片是否缓存在内存中
                .considerExifParams(true)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)//设置图片以如何的编码方式显示
                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型//
                .build();//构建完成
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.main_movie:
                if (hasClickTip(MOVIE_STATE)) {
                    openOrDownload(Contacts.TYPE_MOVIE, true);
                }
                break;

            case R.id.main_app:
                startActivity(new Intent(this, MyAppActivity.class));
                overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
                break;

            case R.id.main_ie:
                if (hasClickTip(IE_STATE)) {
                    openOrDownload(Contacts.TYPE_BROWSER, true);
                }
                break;

            case R.id.main_game:
                if (hasClickTip(GAME_STATE)) {
                    openOrDownload(Contacts.TYPE_GAME, true);
                }
                break;

            case R.id.main_music:
                if (hasClickTip(MUSIC_STATE)) {
                    openOrDownload(Contacts.TYPE_MUSIC, true);
                }
                break;

            case R.id.main_app_store:
                if (hasClickTip(STORE_STATE)) {
                    openOrDownload(Contacts.TYPE_MARKET, true);
                }
                break;

            //设置
            case R.id.main_setting:
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
                break;

            //文件管理
            case R.id.main_file:
                //P1系统自带文件管理器
                Intent intent = new Intent();
                ComponentName cm = new ComponentName("com.softwinner.TvdFileManager",
                        "com.softwinner.TvdFileManager.MainUI");
                intent.setComponent(cm);
                startActivity(intent);
                break;

            case R.id.main_news:
                if (hasClickTip(NEWS_STATE)) {
                    openOrDownload(Contacts.TYPE_NEWS, true);
                }
                break;
        }
    }

    /**
     * 点击提示
     *
     * @param state
     * @return
     */
    private boolean hasClickTip(int state) {
        boolean flag = false;
        if (state != DEFAULT) {
            switch (state) {
                case STARTED:
                case LOADING:
                    Utils.toast(HomeActivity.this, "正在下载，请稍后");
                    break;

                case INSTALL:
                    Utils.toast(HomeActivity.this, "正在安装，请稍后");
                    break;

                case FAILURE:
                    flag = true;
                    break;
            }
        } else {
            flag = true;
        }
        return flag;
    }

    /**
     * 打开还是下载第三方应用
     *
     * @param type
     */
    private void openOrDownload(String type, boolean hasNetwork) {
        App app = DBUtils.getOtherPackageName(type);
        if (TextUtils.isEmpty(app.getPackageName())) {//查询应用信息
            if (hasNetwork) {
                requestApkInfo(type, false);
            } else {
                Utils.toast(HomeActivity.this, "请检查网络连接");
            }
        } else {
            String packageName = app.getPackageName();
            Utils.log("packageName：" + app.getPackageName());
            boolean hasInstalled = Utils.isAppInstalled(this, packageName);
            if (hasInstalled) {//打开
                Utils.openOtherApp(this, packageName);
            } else if (hasNetwork) {//下载
                addDownload(app.getType(), Contacts.KUPA_DOMAIN_NAME + app.getUrl(), false);
                Utils.log("下载链接：" + app.getUrl());
            } else {
                Utils.toast(HomeActivity.this, "请检查网络连接");
            }
        }
    }

    /**
     * 监听Back键按下事件,方法1:
     * 注意:
     * super.onBackPressed()会自动调用finish()方法,关闭
     * 当前Activity.
     * 若要屏蔽Back键盘,注释该行代码即可
     */
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    private void getViews() {
        mMovieIcon = (ImageView) findViewById(R.id.iv_movie);
        mMusicIcon = (ImageView) findViewById(R.id.iv_music);
        mGameIcon = (ImageView) findViewById(R.id.iv_game);

        mMovieLife = (TextView) findViewById(R.id.tv_movie_life);
        mMusicArea = (TextView) findViewById(R.id.tv_music_area);
        mGameCenter = (TextView) findViewById(R.id.tv_game_center);
        mTvMovieTip = (TextView) findViewById(R.id.tv_movie_tip);
        mTvMusicTip = (TextView) findViewById(R.id.tv_music_tip);
        mTvGameTip = (TextView) findViewById(R.id.tv_game_tip);


        mIvMovie = (ImageView) findViewById(R.id.tip_movie_img);
        mIvMusic = (ImageView) findViewById(R.id.tip_music_img);
        mIvGame = (ImageView) findViewById(R.id.tip_game_img);
        mIvOverMovie = (ImageView) findViewById(R.id.tip_movie_over);
        mIvOverMusic = (ImageView) findViewById(R.id.tip_music_over);
        mIvOverGame = (ImageView) findViewById(R.id.tip_game_over);

        fdv_movie = (FreshDownloadView) findViewById(R.id.fdv_movie);
        fdv_music = (FreshDownloadView) findViewById(R.id.fdv_music);
        fdv_game = (FreshDownloadView) findViewById(R.id.fdv_game);
        fdv_ie = (FreshDownloadView) findViewById(R.id.fdv_ie);
        fdv_news = (FreshDownloadView) findViewById(R.id.fdv_news);
        fdv_store = (FreshDownloadView) findViewById(R.id.fdv_store);

        mTvIe = (TextView) findViewById(R.id.tv_ie);
        mTvNews = (TextView) findViewById(R.id.tv_news);
        mTvStore = (TextView) findViewById(R.id.tv_store);

        coverMovie = findViewById(R.id.cover_movie);
        coverMusic = findViewById(R.id.cover_music);
        coverGame = findViewById(R.id.cover_game);
        coverNews = findViewById(R.id.cover_news);
        coverIe = findViewById(R.id.cover_ie);
        coverStore = findViewById(R.id.cover_store);

        findViewById(R.id.main_movie).setOnClickListener(this);
        findViewById(R.id.main_music).setOnClickListener(this);
        findViewById(R.id.main_ie).setOnClickListener(this);
        findViewById(R.id.main_app_store).setOnClickListener(this);
        findViewById(R.id.main_game).setOnClickListener(this);
        findViewById(R.id.main_file).setOnClickListener(this);
        findViewById(R.id.main_setting).setOnClickListener(this);
        findViewById(R.id.main_app).setOnClickListener(this);
        findViewById(R.id.main_news).setOnClickListener(this);
    }

}
