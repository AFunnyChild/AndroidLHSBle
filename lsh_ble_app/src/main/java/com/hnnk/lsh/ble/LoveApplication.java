package com.hnnk.lsh.ble;

import android.app.Application;
import android.content.Context;

import com.iflytek.VoiceWakeuperHelper;
import com.iflytek.cloud.SpeechUtility;


public class LoveApplication extends Application {


    @Override
    public void onCreate() {
        SpeechUtility.createUtility(LoveApplication.this, "appid=" + "5ef16797");
  //      VoiceWakeuperHelper
        super.onCreate();

        SpeechUtility utility = SpeechUtility.getUtility();

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);


    }

}
