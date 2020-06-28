package com.hnnk.lsh.ble;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.feng.mydemo.activity.BleScanActivity;
import com.iflytek.speech.setting.IatSettings;


public class LHSBleMainActivity extends AppCompatActivity implements View.OnClickListener {
    static LHSBleMainActivity activity;
    private static final int REQUEST_CODE_CHOOSE = 23;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_start).setOnClickListener(this);
        findViewById(R.id.btn_state).setOnClickListener(this);
        activity=this;
     //   startActivity(new Intent(this, BleTestMainActivity.class));

//        Intent dialogIntent = new Intent(getBaseContext(), BleScanActivity.class);
//        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        getApplication().startActivity(dialogIntent);


        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN );

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams
                (FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        //设置顶部,左边布局
        params.gravity= Gravity.TOP|Gravity.LEFT;

        initWake();

    }

    private void initWake() {
     //   mIvw = VoiceWakeuper.createWakeuper(this, null);
    }


    public static void onStartBlueTooth(int isStart) {
        BleScanActivity  bleScanActivity=new BleScanActivity(activity);
        bleScanActivity.show();

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {

        }
    }

    // <editor-fold defaultstate="collapsed" desc="onClick">
    @SuppressLint("CheckResult")
    @Override
    public void onClick(final View v) {
//        select_photo();
//        JniSelectPhoto();
        onStartBlueTooth(1);
    }

    public static void JniSelectPhoto() {

    }



}
