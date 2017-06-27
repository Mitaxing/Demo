package com.kupaworld.androidtv.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.kupaworld.androidtv.R;
import com.kupaworld.androidtv.util.Contacts;
import com.kupaworld.androidtvwidget.bridge.OpenEffectBridge;
import com.kupaworld.androidtvwidget.view.FrameMainLayout;
import com.kupaworld.androidtvwidget.view.MainUpView;
import com.kupaworld.androidtvwidget.view.ReflectItemView;

/**
 * Created by MiTa on 2016/12/1.
 */

public class SettingsActivity extends BaseActivity implements View.OnClickListener {

    private MainUpView mainUpView1;
    private OpenEffectBridge mOpenEffectBridge;
    private View mOldFocus; // 4.3以下版本需要自己保存.
    private Intent intent;
    private ReflectItemView mRiMore, mRiNetwork, mRiShow, mRiUpdate, mRiApps, mRiAbout;
    private ImageView mIvWifi;

    private int[] wifiImg = {R.mipmap.kupatv_wifi_off, R.mipmap.kupatv_wifi_one, R.mipmap.kupatv_wifi_two, R.mipmap.kupatv_wifi_three,
            R.mipmap.kupatv_wifi_four, R.mipmap.kupatv_wifi_no, R.mipmap.ethernet};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getViews();
        updateTime(this);
        initUpView();
        registerEth();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(rssReceiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
        isEthernetOn();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rssReceiver != null)
            unregisterReceiver(rssReceiver);
    }

    @Override
    protected void onDestroy() {
        if (mEthReceiver != null)
            unregisterReceiver(mEthReceiver);
        super.onDestroy();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (!isEthernetOn())
                        changeWifiIcon(msg.arg1);
                    break;
            }
        }
    };

    private void changeWifiIcon(int strength) {
        int res = wifiImg[strength];
        imageLoader.displayImage(imageUri + res, mIvWifi, options);
    }

    public BroadcastReceiver rssReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int strength = obtainWifiInfo();
            Message msg = new Message();
            msg.arg1 = strength;
            msg.what = 0;
            handler.sendMessage(msg);
        }
    };

    private void initUpView() {
        mainUpView1 = (MainUpView) findViewById(R.id.setting_mainUpView);
        mOpenEffectBridge = (OpenEffectBridge) mainUpView1.getEffectBridge();
        mainUpView1.setUpRectResource(R.drawable.white_light_10); // 设置移动边框的图片.
        mainUpView1.setDrawUpRectPadding(new RectF(-15, -15, -15, -15)); //
//         设置移动边框的距离.
        mainUpView1.setDrawShadowPadding(-2); // 阴影图片设置距离.

        FrameMainLayout main_lay12 = (FrameMainLayout) findViewById(R.id.setting_lay);
        main_lay12.getViewTreeObserver().addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
            @Override
            public void onGlobalFocusChanged(final View oldFocus, final View newFocus) {
                if (newFocus != null)
                    newFocus.bringToFront(); // 防止放大的view被压在下面. (建议使用MainLayout)
                //设置获得焦点图片放大的倍数
                float scale = 1.05f;
                mainUpView1.setFocusView(newFocus, mOldFocus, scale);
                mOldFocus = newFocus; // 4.3以下需要自己保存.
                if (oldFocus != null)
                    oldFocus.setBackgroundResource(R.drawable.layout_setting_bg);
                newFocus.setBackgroundResource(R.drawable.layout_setting_focus);
            }
        });
    }

    private void getViews() {
        mRiMore = (ReflectItemView) findViewById(R.id.settings_more);
        mRiNetwork = (ReflectItemView) findViewById(R.id.settings_network);
        mRiShow = (ReflectItemView) findViewById(R.id.settings_show);
        mRiUpdate = (ReflectItemView) findViewById(R.id.settings_update);
        mRiApps = (ReflectItemView) findViewById(R.id.settings_apps);
        mRiAbout = (ReflectItemView) findViewById(R.id.settings_about);
        mIvWifi = (ImageView) findViewById(R.id.settings_wifi);

        mRiMore.setOnClickListener(this);
        mRiNetwork.setOnClickListener(this);
        mRiShow.setOnClickListener(this);
        mRiUpdate.setOnClickListener(this);
        mRiApps.setOnClickListener(this);
        mRiAbout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
//        boolean hasIntent = true;
        int id = v.getId();
        switch (id) {
            case R.id.settings_more:
                intent = new Intent(this, MoreSettingsActivity.class);
                break;

            case R.id.settings_network:
                intent = new Intent(this, NetworkActivity.class);
                break;

            case R.id.settings_show:
                intent = new Intent();
                ComponentName cm = new ComponentName("com.amlogic.projector",
                        "com.amlogic.projector.ShowActivity");
                intent.setComponent(cm);
                break;

            case R.id.settings_update:
                intent = new Intent(this, CheckUpdateActivity.class);
                break;

            case R.id.settings_apps:
                intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                break;

            case R.id.settings_about:
                intent = new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS);
                break;
        }
        startActivity(intent);
        overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
    }

    private boolean isEthernetOn() {
        ConnectivityManager connectivity = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivity.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        if (info == null) return false;

        if (info.isConnected()) {
            changeWifiIcon(ETHERNET_POSITION);
            return true;
        } else {
            return false;
        }
    }

    private void registerEth() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Contacts.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mEthReceiver, filter);
    }

    BroadcastReceiver mEthReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleEthEvent(intent);
        }
    };


    private void handleEthEvent(Intent intent) {
        String action = intent.getAction();
        if (Contacts.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            final int event = intent.getIntExtra(Contacts.EXTRA_ETHERNET_STATE,
                    0);
            switch (event) {
                case Contacts.ETHERNET_STATE_ENABLED:
                    changeWifiIcon(ETHERNET_POSITION);
                    break;

                case 3:
                    changeWifiIcon(obtainWifiInfo());
                    break;
            }
        }
    }
}
