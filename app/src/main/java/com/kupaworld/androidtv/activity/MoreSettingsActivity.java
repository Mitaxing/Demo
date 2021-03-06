package com.kupaworld.androidtv.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.kupaworld.androidtv.R;
import com.kupaworld.androidtv.adapter.MoreSettingsListAdapter;

/**
 * Created by admin on 2016/12/5.
 */

public class MoreSettingsActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private ListView mLvList;
    private TextView mTvTitle;

    private Intent intent;
    private MoreSettingsListAdapter adapter;
    private int[] settings = {R.string.date_and_time, R.string.language_and_inputmethod, R.string.storage,
            R.string.security, R.string.master, R.string.development};
    private String[] intents = {Settings.ACTION_DATE_SETTINGS,
            Settings.ACTION_INPUT_METHOD_SETTINGS,
            Settings.ACTION_MEMORY_CARD_SETTINGS,
            Settings.ACTION_SECURITY_SETTINGS,
            Settings.ACTION_SYNC_SETTINGS,
            Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_settings);
        getViews();
        updateTime(this);
        initListView();
    }

    private void initListView() {
        adapter = new MoreSettingsListAdapter(this, settings);
        mLvList.setAdapter(adapter);
        mLvList.setOnItemClickListener(this);
    }

    private void getViews() {
        mLvList = (ListView) findViewById(R.id.more_settings_list);
        mTvTitle = (TextView) findViewById(R.id.settings_title);

        mTvTitle.setText(R.string.setting_more);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 1) {
            openSettings("com.android.settings.InputMethodAndLanguageSettingsActivity");
        } else if (position == 4) {
            openSettings("com.android.settings.PrivacySettingsActivity");
        } else
            startSettings(intents[position]);
    }

    private void startSettings(String to) {
        intent = new Intent(to);
        startActivity(intent);
    }

    private void openSettings(String action) {
        intent = new Intent();
        ComponentName cName = new ComponentName("com.android.settings", action);
        intent.setComponent(cName);
        startActivity(intent);
    }
}
