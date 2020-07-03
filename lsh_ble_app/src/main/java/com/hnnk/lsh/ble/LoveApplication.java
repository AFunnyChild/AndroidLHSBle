package com.hnnk.lsh.ble;

import android.app.Application;
import android.content.Context;

import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeechUtility;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;


public class LoveApplication extends Application {


    @Override
    public void onCreate() {
    //    DaemonEnv.initialize(this, DemoService.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
       // DemoService.sShouldStopService = false;
        Bugly.init(getApplicationContext(), "8e1eee2cd5", true);

        SpeechUtility.createUtility(LoveApplication.this, "appid=" + "5ef16797");
        Setting.setLogLevel(Setting.LOG_LEVEL.none);
        super.onCreate();

      //  Beta.checkUpgrade();

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);


    }

}
