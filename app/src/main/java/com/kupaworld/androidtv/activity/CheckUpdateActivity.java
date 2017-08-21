package com.kupaworld.androidtv.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.kupaworld.androidtv.R;
import com.kupaworld.androidtv.download.DownloadInfo;
import com.kupaworld.androidtv.download.DownloadManager;
import com.kupaworld.androidtv.download.DownloadService;
import com.kupaworld.androidtv.entity.SystemInfo;
import com.kupaworld.androidtv.util.Contacts;
import com.kupaworld.androidtv.util.JsonUtils;
import com.kupaworld.androidtv.util.Network;
import com.kupaworld.androidtv.util.Toastor;
import com.kupaworld.androidtv.util.UpdateUtils;
import com.kupaworld.androidtv.util.Utils;
import com.kupaworld.androidtv.view.DynamicWave;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 系统升级页面
 * Created by MiTa on 2016/12/11.
 */

public class CheckUpdateActivity extends BaseActivity implements View.OnClickListener {

    private TextView mTvNoVersion;
    private FrameLayout mFlUpdate;
    private TextView mTvVersion, mTvNewVersion, mTvDate, mTvDesc, mTvTitle, mTvWatchAll, mTvUpdate, mTvLocalUpdate;
    private DynamicWave mDwProgress;
    private FrameLayout mFlProgress;

    private String url;
    private boolean isDownload, isClick;

    private DownloadManager downloadManager;
    private TextView mTvProgress;

    private SystemInfo systemInfo;

    private final int STARTED = 1;
    private final int LOADING = 2;
    private final int FAILURE = 3;
    private final int SUCCESS = 5;

    private AlphaAnimation alphaAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_update);
        updateTime(this);
        getViews();
        downloadManager = DownloadService.getDownloadManager(getApplicationContext());
        handler.sendEmptyMessage(STARTED);
        checkSystemUpdate();
    }

    /**
     * 检测系统升级
     */
    private void checkSystemUpdate() {
        if (Network.isConnected(this)) {
            mTvUpdate.setClickable(false);
            mTvUpdate.setText(R.string.checking);
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
                        Utils.log("检查系统更新结果：" + responseInfo.result);
                        systemInfo = JsonUtils.resolveResult(responseInfo.result);
                        if (null != systemInfo) {
                            resolveResult();
                        } else {
                            mTvUpdate.setClickable(true);
                            mTvUpdate.setText(R.string.check_for_update);
                            showToast(R.string.now_is_new_version);
                        }
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        Utils.log("检测失败：" + s);
                        mTvUpdate.setClickable(true);
                        mTvUpdate.setText(R.string.check_for_update);
                        showToast(R.string.check_fail_try_again);
                    }
                });
            }
        } else
            showToast(R.string.check_network);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case STARTED:
                case LOADING:
                    refreshState();
                    break;
            }
        }
    };

    private void refreshState() {
        List<DownloadInfo> list = downloadManager.getDownloadInfoList();
        for (int i = 0; i < list.size(); i++) {
            DownloadInfo info = list.get(i);
            String fileName = info.getFileName();
            int state = info.getState().value();
            int progress = 0;
            if (state == LOADING) {
                progress = (int) (info.getProgress() * 100 / info.getFileLength());
                if (fileName.equals(Contacts.TYPE_LAUNCHER)) {
                    setDownloadProgress(progress);
                    mFlProgress.setVisibility(View.VISIBLE);
                    mTvUpdate.setFocusable(false);
                }
            }
            switch (fileName) {
                case Contacts.TYPE_LAUNCHER:
                    updateState(state);
                    if (state == SUCCESS) {
                        try {
                            UpdateUtils.installPackage(CheckUpdateActivity.this, new File(Contacts.PATH_UPDATE));
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
     * 设置下载进度
     *
     * @param progress
     */
    private void setDownloadProgress(int progress) {
        mTvProgress.setText(getString(R.string.downloading) + progress + " %");
        mDwProgress.setProgress(progress);
    }

    /**
     * 更新下载进度
     *
     * @param state
     */
    private void updateState(int state) {
        switch (state) {
            case STARTED:
                mTvUpdate.setText("");
                isDownload = true;
                mFlProgress.setVisibility(View.VISIBLE);
                mTvUpdate.setFocusable(false);
                if (!isClick)
                    handler.sendEmptyMessageDelayed(STARTED, 500);
                break;

            case LOADING:
                mTvUpdate.setText("");
                isDownload = true;
                if (!isClick)
                    handler.sendEmptyMessageDelayed(LOADING, 500);
                break;

            case SUCCESS:
                setDownloadProgress(100);
                break;

            case FAILURE:
                if (Network.isConnected(this))
                    showToast(R.string.download_fail_try_again);
                else
                    showToast(R.string.download_fail_check_network);

                mFlProgress.setVisibility(View.GONE);
                mTvUpdate.setText(R.string.upgrade_immediately);
                mTvUpdate.setFocusable(true);
                mTvUpdate.requestFocus();
                isDownload = false;
                break;
        }
    }

    private void resolveResult() {
        if (systemInfo.getResult().equals("ok")) {
            url = systemInfo.getVersionDownloadUrl();
            mTvNewVersion.setText(getString(R.string.latest_version) +" "+ systemInfo.getVersionName());
            String describe = systemInfo.getVersionInformation();
            describe = describe.replace("\\n", ",");
            String[] splits = describe.split(",");
            int len = splits.length;
            String info = "";
            for (int i = 0; i < len; i++) {
                info += splits[i];
            }
            mTvDesc.setText(getString(R.string.update_content) + info);
            mTvDate.setText(getString(R.string.release_date) + Utils.formatDate(systemInfo.getUpdateTime()));
            mFlUpdate.setVisibility(View.VISIBLE);
            mTvWatchAll.setVisibility(View.VISIBLE);

            alphaAnimation = (AlphaAnimation) AnimationUtils.loadAnimation(CheckUpdateActivity.this, R.anim.alpha);
            mFlUpdate.startAnimation(alphaAnimation);
            mTvWatchAll.startAnimation(alphaAnimation);


            alphaAnimation = (AlphaAnimation) AnimationUtils.loadAnimation(CheckUpdateActivity.this, R.anim.alphaout);
            mTvNoVersion.startAnimation(alphaAnimation);

            mTvNoVersion.setVisibility(View.GONE);
            if (!isDownload) {
                mTvUpdate.setClickable(true);
                mTvUpdate.setText(R.string.upgrade_immediately);
                mTvUpdate.setBackgroundResource(R.drawable.btn_update_selector);
            }
        } else {
            mTvNoVersion.setVisibility(View.VISIBLE);
            mFlUpdate.setVisibility(View.GONE);
            mTvWatchAll.setVisibility(View.GONE);
            mTvUpdate.setBackgroundResource(R.drawable.btn_update_normal);
            mTvNoVersion.setVisibility(View.VISIBLE);
            mTvUpdate.setText(R.string.check_for_update);
        }
    }

    /**
     * 添加下载任务
     *
     * @param type
     * @param url
     */
    private void addDownload(String type, String url) {
        try {
            downloadManager.addNewDownload(url, type, Contacts.PATH_UPDATE, true, true, new RequestCallBack<File>() {

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

    private void getViews() {
        mTvVersion = (TextView) findViewById(R.id.update_version);
        mTvNewVersion = (TextView) findViewById(R.id.update_new_version);
        mTvDate = (TextView) findViewById(R.id.update_date);
        mTvDesc = (TextView) findViewById(R.id.update_describe);
        mTvUpdate = (TextView) findViewById(R.id.update_btn);
        mTvTitle = (TextView) findViewById(R.id.settings_title);
        mTvProgress = (TextView) findViewById(R.id.update_progress);
        mTvNoVersion = (TextView) findViewById(R.id.update_no_version);
        mFlUpdate = (FrameLayout) findViewById(R.id.update_info_layout);
        mTvWatchAll = (TextView) findViewById(R.id.update_watch_all);
        mDwProgress = (DynamicWave) findViewById(R.id.update_wave);
        mFlProgress = (FrameLayout) findViewById(R.id.update_progress_layout);
        mTvLocalUpdate = (TextView) findViewById(R.id.local_update_btn);

        mTvTitle.setText(R.string.setting_update);
        mTvVersion.setText("v" + Utils.getVersionName(this));

        mTvWatchAll.setOnClickListener(this);
        mTvUpdate.setOnClickListener(this);
        mTvLocalUpdate.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.update_btn:
                if (!isDownload) {
                    if (TextUtils.isEmpty(url)) {
                        checkSystemUpdate();
                    } else {
                        isClick = true;
                        addDownload(Contacts.TYPE_LAUNCHER, Contacts.BASE_URI + url);
                        mTvWatchAll.requestFocus();
                    }
                }
                break;

            case R.id.update_watch_all:
                Intent intent = new Intent(this, UpdateInfoActivity.class);
                intent.putExtra("version", systemInfo.getVersionName());
                intent.putExtra("date", Utils.formatDate(systemInfo.getUpdateTime()));
                intent.putExtra("describe", systemInfo.getVersionInformation());
                startActivity(intent);
                break;

            case R.id.local_update_btn:
                startLocalRecovery();
                break;
        }
    }

    private boolean checkPackage(String packageName) {
        if (packageName == null || "".equals(packageName))
            return false;
        try {
            getPackageManager().getApplicationInfo(
                    packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void startLocalRecovery() {
        if (checkPackage("com.softwinner.settingsassist") && checkPackage("com.softwinner.TvdFileManager")) {
            Utils.log("apk exist!");
            Intent intent = new Intent("softwinner.intent.action.RECOVREY");
            startActivity(intent);
        } else {
            showToast(R.string.not_support_local_update);
        }
    }

}
