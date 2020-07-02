package com.hnnk.lsh.ble;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.iflytek.VoiceWakeuperHelper;
import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeechUtility;
import com.process.keepalive.daemon.DemoService;
import com.process.keepalive.daemon.guard.DaemonEnv;


public class LoveApplication extends Application {


    @Override
    public void onCreate() {
        DaemonEnv.initialize(this, DemoService.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
        DemoService.sShouldStopService = false;

        SpeechUtility.createUtility(LoveApplication.this, "appid=" + "5ef16797");
        Setting.setLogLevel(Setting.LOG_LEVEL.none);
        super.onCreate();



    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);


    }

}
