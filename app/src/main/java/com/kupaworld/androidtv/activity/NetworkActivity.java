package com.kupaworld.androidtv.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import com.kupaworld.androidtv.R;
import com.kupaworld.androidtv.util.Contacts;

/**
 * Created by admin on 2016/12/15.
 */

public class NetworkActivity extends BaseActivity implements View.OnClickListener {

    private TextView mTvTitle, mTvWifi, mTvBlue, mTvLine;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);
        getViews();
        updateTime(this);
        registerEth();
    }

    private void getViews() {
        mTvTitle = (TextView) findViewById(R.id.settings_title);
        mTvBlue = (TextView) findViewById(R.id.network_blue_state);
        mTvLine = (TextView) findViewById(R.id.network_line_state);
        mTvWifi = (TextView) findViewById(R.id.network_wifi_state);


        findViewById(R.id.network_wifi).setOnClickListener(this);
        findViewById(R.id.network_blue).setOnClickListener(this);
        findViewById(R.id.network_line).setOnClickListener(this);

        mTvTitle.setText("设置  |  网络设置");
    }

    private void startSettings(String to) {
        intent = new Intent(to);
        startActivity(intent);
        overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id) {
            case R.id.network_wifi:
                Intent intent = new Intent();
                ComponentName cm = new ComponentName("com.android.setting",
                        "com.android.settings.WifiSettingsActivity");
                intent.setComponent(cm);
                startActivity(intent);
                break;

            case R.id.network_blue:
                startSettings(Settings.ACTION_BLUETOOTH_SETTINGS);
                break;

            case R.id.network_line:
                Intent ethIntent = new Intent();
                ComponentName componentName = new ComponentName("com.android.settings",
                        "com.android.settings.EthernetSettingsActivity");
                ethIntent.setComponent(componentName);
                startActivity(ethIntent);
                break;
        }
    }

    private void accessWifiInfo() {
        // Wifi的连接速度及信号强度：
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            mTvWifi.setText("未开启");
        } else {
            // WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            WifiInfo info = wifiManager.getConnectionInfo();
            int strength = -1;
            if (info.getBSSID() != null) {
                // 链接信号强度
                strength = WifiManager.calculateSignalLevel(info.getRssi(), 5);
                // 链接速度
                int speed = info.getLinkSpeed();
                // 链接速度单位
                String units = WifiInfo.LINK_SPEED_UNITS;
                // Wifi源名称
                String ssid = info.getSSID();
                if (strength == 0)
                    mTvWifi.setText("未连接");
                else
                    mTvWifi.setText(ssid);
            }
        }
    }

    private void checkBlueState() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                mTvBlue.setText("已开启");
            } else {
                mTvBlue.setText("未开启");
            }
        }
    }

    private boolean isEthernetOn() {
        ConnectivityManager connectivity = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivity.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        if (info == null) return false;

        if (info.isConnected()) {
            mTvLine.setText("已连接");
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
                case Contacts.ETHERNET_STATE_ENABLED:
                    mTvLine.setText("已连接");
                    break;

                case 3:
                    mTvLine.setText("未连接");
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        accessWifiInfo();
        checkBlueState();
        isEthernetOn();
    }

    @Override
    protected void onDestroy() {
        if (mEthStateReceiver != null)
            unregisterReceiver(mEthStateReceiver);
        super.onDestroy();
    }
}