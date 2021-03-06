package com.kupaworld.androidtv.activity;

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

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.kupaworld.androidtv.R;
import com.kupaworld.androidtv.db.DBUtils;
import com.kupaworld.androidtv.download.DownloadInfo;
import com.kupaworld.androidtv.download.DownloadManager;
import com.kupaworld.androidtv.download.DownloadService;
import com.kupaworld.androidtv.entity.App;
import com.kupaworld.androidtv.entity.BgImage;
import com.kupaworld.androidtv.entity.SystemInfo;
import com.kupaworld.androidtv.util.BaiDuMapUtils;
import com.kupaworld.androidtv.util.BgImageUtil;
import com.kupaworld.androidtv.util.Contacts;
import com.kupaworld.androidtv.util.JsonUtils;
import com.kupaworld.androidtv.util.Network;
import com.kupaworld.androidtv.util.Toastor;
import com.kupaworld.androidtv.util.UpdateUtils;
import com.kupaworld.androidtv.util.Utils;
import com.kupaworld.androidtv.view.FreshDownloadView;
import com.kupaworld.androidtv.view.TipDialog;
import com.kupaworld.androidtvwidget.view.FrameMainLayout;
import com.kupaworld.androidtvwidget.view.MainUpView;
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

    private TextView mMovieLife, mMusicArea, mGameCenter, mTvNews, mTvIe, mTvStore, mTvFile, mTvMovieTip, mTvMusicTip, mTvGameTip;
    private View coverMovie, coverMusic, coverGame, coverNews, coverIe, coverStore, coverFile;

    private List<String> movieUrl = new ArrayList<>();
    private List<String> musicUrl = new ArrayList<>();
    private List<String> gameUrl = new ArrayList<>();

    private boolean showOverMovie, showOverMusic, showOverGame, showMovieTip, showMusicTip, showGameTip;
    private int currentModule, currentMovie, currentMusic, currentGame;
    private String currentUrl;
    private int focusViewId;
    private SystemInfo systemInfo;

    private DisplayImageOptions options;
    private final String ALPHA = "alpha";
    private ImageLoader imageLoader;
    private static boolean isChange;
    private ValueAnimator valueAnimator;

    private FreshDownloadView fdv_movie, fdv_music, fdv_game, fdv_news, fdv_ie, fdv_store, fdv_file;
    private DownloadManager downloadManager;

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
    private int DOCUMENT_STATE = DEFAULT;

    private LocationClient mLocationClient = null;
    private BDLocationListener myListener = new MyLocationListener();
    private String address;

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
        //保存默认内置应用信息
        saveDefaultInfo();
        //删除系统升级包
        deleteUpdate();
        //开始定位
        startLocation();
        //检测系统升级
        checkSystemUpdate();
        //更新系统结果
        UpdateUtils.updateSystemResult(this);
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
        String mac = Utils.getLocalMacAddress(this);
        if (!TextUtils.isEmpty(mac)) {
            int version = Utils.getVersionCode(this);
            HttpUtils http = new HttpUtils(10 * 1000);
            RequestParams params = new RequestParams();
            params.addBodyParameter("mac", mac);
            params.addBodyParameter("version", String.valueOf(version));
            http.send(HttpRequest.HttpMethod.POST, Contacts.URI_SYSTEM_UPDATE, params, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    systemInfo = JsonUtils.resolveResult(responseInfo.result);
                    if (null != systemInfo) {
                        String result = systemInfo.getResult();
                        if ("ok".equals(result)) {
                            showUpdateDialog(systemInfo.getVersionName() + "：" + systemInfo.getVersionInformation().replace("\\n","\n"));
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

    /**
     * 添加下载任务
     *
     * @param type
     * @param url
     */
    private void addDownload(String type, String url, boolean isSystem) {
        try {
            //apk下载路径
            String target = Contacts.BASE_PATH + type + ".apk";
            if (isSystem) {
                //系统镜像下载路径
                target = Contacts.PATH_UPDATE;
            }
            Utils.log("下载链接：" + url);
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
     * 存储默认内置应用信息
     */
    private void saveDefaultInfo() {
        //内置应用类型
        String[] types = {Contacts.TYPE_MOVIE, Contacts.TYPE_MUSIC, Contacts.TYPE_BROWSER, Contacts.TYPE_GAME,
                Contacts.TYPE_NEWS, Contacts.TYPE_MARKET, Contacts.TYPE_DOCUMENT};
        //内置应用包名
        String[] packages = {Contacts.PACKAGE_MOVIE, Contacts.PACKAGE_MUSIC, Contacts.PACKAGE_BROWSER, Contacts.PACKAGE_GAME,
                Contacts.PACKAGE_NEWS, Contacts.PACKAGE_MARKET, Contacts.PACKAGE_DOCUMENT};
        App app = new App();
        int len = types.length;
        for (int i = 0; i < len; i++) {
            app.setType(types[i]);
            app.setPackageName(packages[i]);
            DBUtils.noSoSave(app);
        }
    }

    /**
     * 初始化UpView
     */
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

    /**
     * 去除电影、音乐、游戏模块聚焦时的遮盖层
     */
    private void hideOver() {
        //电影
        if (showMovieTip) {
            mMovieIcon.setVisibility(View.GONE);
            mMovieLife.setVisibility(View.VISIBLE);
            coverMovie.setVisibility(View.GONE);
            mTvMovieTip.setVisibility(View.GONE);
            showMovieTip = false;
        }
        //音乐
        if (showMusicTip) {
            mMusicIcon.setVisibility(View.GONE);
            mMusicArea.setVisibility(View.VISIBLE);
            coverMusic.setVisibility(View.GONE);
            mTvMusicTip.setVisibility(View.GONE);
            showMusicTip = false;
        }
        //游戏
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
        //请求内置应用和模块背景图信息
        requestAppAndImgInfo();
        //杀死后台进程
        Utils.killAll(this);
    }

    /**
     * 请求内置应用和模块背景图信息
     */
    private void requestAppAndImgInfo() {
        HttpUtils httpUtils = new HttpUtils(10 * 1000);
        RequestParams params = new RequestParams();
        params.addBodyParameter("mac", Utils.getLocalMacAddress(this));
        httpUtils.send(HttpRequest.HttpMethod.POST, Contacts.URI_SYSTEM, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Utils.log("系统信息：" + responseInfo.result);
                JsonUtils.resolveProductResult(responseInfo.result);
                initImageUrl();
            }

            @Override
            public void onFailure(HttpException e, String s) {

            }
        });
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
                    case Contacts.TYPE_MOVIE://电影背景图
                        movieUrl.add(image.getUrl());
                        break;

                    case Contacts.TYPE_MUSIC://音乐背景图
                        musicUrl.add(image.getUrl());
                        break;

                    case Contacts.TYPE_GAME://游戏背景图
                        gameUrl.add(image.getUrl());
                        break;
                }
            }
            handler.sendEmptyMessage(1);
        }
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

    /**
     * 初始化动画
     */
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
                    imageLoader.displayImage(Contacts.BASE_URI + currentUrl, currentOverView, options, new ImageLoadingListener() {
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
                case Contacts.TYPE_MOVIE://电影模块
                    showState(fdv_movie, state, progress, info, mMovieLife, coverMovie);
                    MOVIE_STATE = state;
                    mMovieIcon.setVisibility(View.GONE);
                    break;

                case Contacts.TYPE_MUSIC://音乐模块
                    showState(fdv_music, state, progress, info, mMusicArea, coverMusic);
                    MUSIC_STATE = state;
                    mMusicIcon.setVisibility(View.GONE);
                    break;

                case Contacts.TYPE_GAME://游戏模块
                    showState(fdv_game, state, progress, info, mGameCenter, coverGame);
                    GAME_STATE = state;
                    mGameIcon.setVisibility(View.GONE);
                    break;

                case Contacts.TYPE_NEWS://新闻模块
                    showState(fdv_news, state, progress, info, mTvNews, coverNews);
                    NEWS_STATE = state;
                    break;

                case Contacts.TYPE_BROWSER://浏览器模块
                    showState(fdv_ie, state, progress, info, mTvIe, coverIe);
                    IE_STATE = state;
                    break;

                case Contacts.TYPE_MARKET://应用市场模块
                    showState(fdv_store, state, progress, info, mTvStore, coverStore);
                    STORE_STATE = state;
                    break;

                case Contacts.TYPE_DOCUMENT://文件管理器模块
                    showState(fdv_file, state, progress, info, mTvFile, coverFile);
                    DOCUMENT_STATE = state;
                    break;

                case Contacts.TYPE_LAUNCHER://系统更新
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
     * @param view     自定义下载动画
     * @param state    下载状态
     * @param progress 下载进度
     * @param info     下载实体
     * @param mTvTip   模块TextView
     * @param cover    模块遮罩层
     */
    private void showState(FreshDownloadView view, int state, int progress, DownloadInfo info, TextView mTvTip, View cover) {
        if (state == STARTED) {
            view.setVisibility(View.VISIBLE);
            view.startDownload();
            mTvTip.setText(R.string.installing);
            mTvTip.setVisibility(View.VISIBLE);
            cover.setVisibility(View.VISIBLE);
        } else if (state == LOADING) {
            view.upDateProgress(progress);
            mTvTip.setText(R.string.downloading);
        } else if (state == SUCCESS) {
            try {
                mTvTip.setText(R.string.installing);
                view.showDownloadOk();
                downloadManager.removeDownload(info);
                backgroundInstall(info.getFileSavePath(), info.getFileName());
            } catch (DbException e) {
                e.printStackTrace();
            }
        } else {
            mTvTip.setText(R.string.download_failed);
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
        switch (type) {
            case Contacts.TYPE_MOVIE://电影模块
                MOVIE_STATE = DEFAULT;
                break;

            case Contacts.TYPE_MUSIC://音乐模块
                MUSIC_STATE = DEFAULT;
                break;

            case Contacts.TYPE_GAME://游戏模块
                GAME_STATE = DEFAULT;
                break;

            case Contacts.TYPE_NEWS://新闻模块
                NEWS_STATE = DEFAULT;
                break;

            case Contacts.TYPE_BROWSER://浏览器模块
                IE_STATE = DEFAULT;
                break;

            case Contacts.TYPE_MARKET://应用市场模块
                STORE_STATE = DEFAULT;
                break;

            case Contacts.TYPE_DOCUMENT://文件管理模块
                DOCUMENT_STATE = DEFAULT;
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

    /**
     * 下载完成，显示正常状态
     *
     * @param type      模块类型
     * @param isSuccess 是否下载成功
     */
    private void hideDownloadState(String type, boolean isSuccess) {
        initState(type);
        switch (type) {
            case Contacts.TYPE_MOVIE:
                type = getString(R.string.type_movie);
                hideOption(fdv_movie, mMovieLife, R.string.movie, coverMovie);
                if (focusViewId == R.id.main_movie) {
                    mMovieIcon.setVisibility(View.VISIBLE);
                    mMovieLife.setVisibility(View.GONE);
                    coverMovie.setVisibility(View.VISIBLE);
                    mTvMovieTip.setVisibility(View.VISIBLE);
                }
                break;

            case Contacts.TYPE_MUSIC:
                type = getString(R.string.type_music);
                hideOption(fdv_music, mMusicArea, R.string.music, coverMusic);
                if (focusViewId == R.id.main_music) {
                    mMusicIcon.setVisibility(View.VISIBLE);
                    mMusicArea.setVisibility(View.GONE);
                    coverMusic.setVisibility(View.VISIBLE);
                    mTvMusicTip.setVisibility(View.VISIBLE);
                }
                break;

            case Contacts.TYPE_GAME:
                type = getString(R.string.type_game);
                hideOption(fdv_game, mGameCenter, R.string.game, coverGame);
                if (focusViewId == R.id.main_game) {
                    mGameIcon.setVisibility(View.VISIBLE);
                    mGameCenter.setVisibility(View.GONE);
                    coverGame.setVisibility(View.VISIBLE);
                    mTvGameTip.setVisibility(View.VISIBLE);
                }
                break;

            case Contacts.TYPE_NEWS:
                type = getString(R.string.type_news);
                hideOption(fdv_news, mTvNews, R.string.news, coverNews);
                break;

            case Contacts.TYPE_BROWSER:
                type = getString(R.string.type_browser);
                hideOption(fdv_ie, mTvIe, R.string.browser, coverIe);
                break;

            case Contacts.TYPE_MARKET:
                type = getString(R.string.type_market);
                hideOption(fdv_store, mTvStore, R.string.market, coverStore);
                break;

            case Contacts.TYPE_DOCUMENT:
                type = getString(R.string.type_file);
                hideOption(fdv_file, mTvFile, R.string.document, coverFile);
                break;

            default:
                type = getString(R.string.type_system);
                break;
        }
        if (isSuccess)
            showStringToast(type + getString(R.string.app_install_complete));
    }

    /**
     * 去除下载状态
     *
     * @param fdv      下载动画
     * @param textView 模块textView
     * @param res      模块名
     * @param cover    遮盖层
     */
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
        dialog.setTitle(R.string.release_new_version);
        dialog.setMessage("Kupa TV " + msg);
        dialog.setYesText(getString(R.string.upgrade_immediately));
        dialog.setCancleText(getString(R.string.cancel));
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
                addDownload(Contacts.TYPE_LAUNCHER, Contacts.BASE_URI + systemInfo.getVersionDownloadUrl(), true);
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
        dialog.setTitle(R.string.upgrade_prompt);
        dialog.setMessage(getString(R.string.updating_see_progress));
        dialog.setYesText(getString(R.string.ok));
        dialog.setYesClickListener(new TipDialog.OnYesClickListener() {
            @Override
            public void onYesClick() {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 初始化imageLoader
     */
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
            case R.id.main_movie://电影模块
                if (hasClickTip(MOVIE_STATE)) {
                    openOrDownload(Contacts.TYPE_MOVIE);
                }
                break;

            case R.id.main_app://我的应用模块
                startActivity(new Intent(this, MyAppActivity.class));
                overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
                break;

            case R.id.main_ie://浏览器模块
                if (hasClickTip(IE_STATE)) {
                    openOrDownload(Contacts.TYPE_BROWSER);
                }
                break;

            case R.id.main_game://游戏模块
                if (hasClickTip(GAME_STATE)) {
                    openOrDownload(Contacts.TYPE_GAME);
                }
                break;

            case R.id.main_music://音乐模块
                if (hasClickTip(MUSIC_STATE)) {
                    openOrDownload(Contacts.TYPE_MUSIC);
                }
                break;

            case R.id.main_app_store://应用市场模块
                if (hasClickTip(STORE_STATE)) {
                    openOrDownload(Contacts.TYPE_MARKET);
                }
                break;

            case R.id.main_setting://设置模块
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
                break;

            case R.id.main_file://文件管理模块
                if (hasClickTip(DOCUMENT_STATE)) {
                    openOrDownload(Contacts.TYPE_DOCUMENT);
                }
                break;

            case R.id.main_news://新闻模块
                if (hasClickTip(NEWS_STATE)) {
                    openOrDownload(Contacts.TYPE_NEWS);
                }
                break;
        }
    }

    /**
     * 点击提示下载状态
     *
     * @param state 下载状态
     * @return 是否下载完成
     */
    private boolean hasClickTip(int state) {
        boolean flag = false;
        if (state != DEFAULT) {
            switch (state) {
                case STARTED:
                case LOADING:
                    showToast(R.string.downloading_wait);
                    break;

                case SUCCESS:
                    showToast(R.string.install_wait);
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
     * @param type 下载类型
     */
    private void openOrDownload(String type) {
        App app = DBUtils.getOtherPackageName(type);
        if (app != null) {
            String packageName = app.getPackageName();
            //检查是否安装
            boolean hasInstalled = Utils.isAppInstalled(this, packageName);
            if (hasInstalled) {//打开
                Utils.openOtherApp(this, packageName);
            } else {//下载
                String url = app.getUrl();
                if (TextUtils.isEmpty(url) || url.equals("null"))
                    showToast(R.string.miss_info_contact);
                else {
                    if (Network.isConnected(this)) {
                        addDownload(app.getType(), Contacts.BASE_URI + app.getUrl(), false);
                    } else
                        showToast(R.string.check_network);
                }
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
    }

    /**
     * 启动定位
     */
    private void startLocation() {
        mLocationClient = new LocationClient(this);
        mLocationClient.registerLocationListener(myListener);
        mLocationClient.setLocOption(BaiDuMapUtils.getOption());
        mLocationClient.start();
    }

    /**
     * 百度地图定位监听
     */
    private class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            boolean localSuccess = BaiDuMapUtils.isLocatSuccess(location);
            if (localSuccess) {
                Utils.log("定位成功");
                address = location.getAddrStr();
                if (!TextUtils.isEmpty(address)) {
                    mLocationClient.stop();
                    Utils.log("定位的地址：" + address);
                    //保存定位地址
                    BaiDuMapUtils.saveAddress(HomeActivity.this, address);
                }
            } else {
                Utils.log("定位失败");
            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
        // your stuff or nothing
    }

    //初始化组件
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
        fdv_file = (FreshDownloadView) findViewById(R.id.fdv_file);

        mTvIe = (TextView) findViewById(R.id.tv_ie);
        mTvNews = (TextView) findViewById(R.id.tv_news);
        mTvStore = (TextView) findViewById(R.id.tv_store);
        mTvFile = (TextView) findViewById(R.id.tv_file);

        coverMovie = findViewById(R.id.cover_movie);
        coverMusic = findViewById(R.id.cover_music);
        coverGame = findViewById(R.id.cover_game);
        coverNews = findViewById(R.id.cover_news);
        coverIe = findViewById(R.id.cover_ie);
        coverStore = findViewById(R.id.cover_store);
        coverFile = findViewById(R.id.cover_file);

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
