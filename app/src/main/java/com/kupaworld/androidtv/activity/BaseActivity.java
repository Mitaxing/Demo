package com.kupaworld.androidtv.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kupaworld.androidtv.R;
import com.kupaworld.androidtv.util.Contacts;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by admin on 2016/12/13.
 */

public class BaseActivity extends Activity {

    private TextView mTvTime;
    private ImageView mIvWifi, mIvBluetooth;
    private SimpleDateFormat sdf;
    private String time;
    public Bitmap bm;

    public ImageLoader imageLoader;
    public DisplayImageOptions options;

    public int[] wifiIcons = {R.mipmap.wifi_off, R.mipmap.wifi_one, R.mipmap.wifi_two, R.mipmap.wifi_three,
            R.mipmap.wifi_four, R.mipmap.wifi_no, R.mipmap.ethernet};
    public int ETHERNET_POSITION = 6;
    public String imageUri = "drawable://";

    private boolean isLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getViews();
        initImageLoader();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeMessages(10);
        if (rssiReceiver != null)
            unregisterReceiver(rssiReceiver);
        if (wifiReceiver != null)
            unregisterReceiver(wifiReceiver);
        if (bluetoothReceiver != null)
            unregisterReceiver(bluetoothReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTvTime != null)
            updateTime();
        checkBlueState();
        registerReceiver(rssiReceiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        registerEth();
        isEthernetOn();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 10:
                    updateTime();
                    break;

                case 11:
                    if (!isEthernetOn())
                        changeWifiIcon(msg.arg1);
                    break;

                case 12:
                    showHideWifi(msg.arg1);
                    break;

                case 13:
                    showHideBluetooth(msg.arg1);
                    break;
            }
        }
    };

    private void showHideWifi(int wifiState) {
        if (wifiState == WifiManager.WIFI_STATE_DISABLING) {//正在关闭
        } else if (wifiState == WifiManager.WIFI_STATE_ENABLING) {//正在打开
        } else if (wifiState == WifiManager.WIFI_STATE_DISABLED) {//已经关闭
            if (!isLine)
                mIvWifi.setVisibility(View.GONE);
        } else if (wifiState == WifiManager.WIFI_STATE_ENABLED) {//已经打开
            mIvWifi.setVisibility(View.VISIBLE);
        } else {//未知状态
        }
    }

    private void changeWifiIcon(int strength) {
        if (mIvWifi.getVisibility() == View.GONE)
            mIvWifi.setVisibility(View.VISIBLE);
        int res = wifiIcons[strength];
        imageLoader.displayImage(imageUri + res, mIvWifi, options);
    }

    private void checkBlueState() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                mIvBluetooth.setVisibility(View.VISIBLE);
                imageLoader.displayImage(imageUri + R.mipmap.bluetooth, mIvBluetooth, options);
            } else
                mIvBluetooth.setVisibility(View.GONE);
        }
    }

    private void showHideBluetooth(int blueState) {
        switch (blueState) {
            case BluetoothAdapter.STATE_TURNING_ON:
                break;

            case BluetoothAdapter.STATE_ON:
                mIvBluetooth.setVisibility(View.VISIBLE);
                break;

            case BluetoothAdapter.STATE_TURNING_OFF:
                break;

            case BluetoothAdapter.STATE_OFF:
                mIvBluetooth.setVisibility(View.GONE);
                break;
        }
    }

    private void updateTime() {
        time = sdf.format(new Date());
        mTvTime.setText(time);
        handler.sendEmptyMessageDelayed(10, 1000);
    }

    public void updateTime(Activity activity) {
        this.mTvTime = (TextView) activity.findViewById(R.id.mainPage_time);
        this.mIvBluetooth = (ImageView) findViewById(R.id.mainPage_bluetooth);
        this.mIvWifi = (ImageView) findViewById(R.id.mainPage_wifi);
        updateTime();
    }

    private void getViews() {
        sdf = new SimpleDateFormat("HH:mm");
    }

    @Override
    protected void onDestroy() {
        if (bm != null && !bm.isRecycled()) {
            bm.recycle();
            bm = null;
        }
        if (mEthStateReceiver != null)
            unregisterReceiver(mEthStateReceiver);
        super.onDestroy();
    }

    public BroadcastReceiver wifiReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
            Message msg = new Message();
            msg.arg1 = wifiState;
            msg.what = 12;
            handler.sendMessage(msg);
        }
    };

    public BroadcastReceiver rssiReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int strength = obtainWifiInfo();
            Message msg = new Message();
            msg.arg1 = strength;
            msg.what = 11;
            handler.sendMessage(msg);
        }
    };

    public BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
            Message msg = new Message();
            msg.arg1 = blueState;
            msg.what = 13;
            handler.sendMessage(msg);
        }
    };

    public int obtainWifiInfo() {
        // Wifi的连接速度及信号强度：
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        // WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        WifiInfo info = wifiManager.getConnectionInfo();
        int strength = 5;
        if (info.getBSSID() != null) {
            // 链接信号强度
            strength = WifiManager.calculateSignalLevel(info.getRssi(), 5);
            // 链接速度
            int speed = info.getLinkSpeed();
            // 链接速度单位
            String units = WifiInfo.LINK_SPEED_UNITS;
            // Wifi源名称
            String ssid = info.getSSID();
        }
        return strength;
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
        registerReceiver(mEthStateReceiver, filter);
    }

    BroadcastReceiver mEthStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleEvent(intent);
        }
    };


    private void handleEvent(Intent intent) {
        String action = intent.getAction();
        if (Contacts.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            final int event = intent.getIntExtra(Contacts.EXTRA_ETHERNET_STATE,
                    0);
            switch (event) {
                case Contacts.ETHERNET_STATE_DISABLED:
//                    changeWifiIcon(obtainWifiInfo());
                    break;

                case Contacts.ETHERNET_STATE_ENABLED:
                    changeWifiIcon(ETHERNET_POSITION);
                    isLine = true;
                    break;

                case 3:
                    isLine = false;
                    changeWifiIcon(obtainWifiInfo());
                    break;
            }
        } else if (Contacts.ETHERNET_STATE_CHANGED_ACTION.equals(action)) {

        }
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


}
