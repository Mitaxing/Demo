package com.kupaworld.androidtv.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.kupaworld.androidtv.R;

/**
 * Created by Mita on 2017/6/22.
 */

public class UpdateInfoActivity extends Activity {

    private TextView mTvVersion, mTvDate, mTvDescribe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_info);
        getViews();
        initViews();
    }

    private void initViews() {
        Intent intent = getIntent();
        mTvVersion.setText(intent.getStringExtra("version"));
        mTvDate.setText("发布日期：" + intent.getStringExtra("date"));
        mTvDescribe.setText(intent.getStringExtra("describe"));
    }

    private void getViews() {
        mTvVersion = (TextView) findViewById(R.id.update_info_version);
        mTvDate = (TextView) findViewById(R.id.update_info_date);
        mTvDescribe = (TextView) findViewById(R.id.update_info_describe);
    }
}
