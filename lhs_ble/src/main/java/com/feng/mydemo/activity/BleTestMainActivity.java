package com.feng.mydemo.activity;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class BleTestMainActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BleScanActivity  bleScanActivity=new BleScanActivity(this);
        bleScanActivity.show();
    }
}
