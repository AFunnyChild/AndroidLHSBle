package com.process.keepalive.daemon;


import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.process.keepalive.daemon.guard.DaemonEnv;
import com.process.keepalive.daemon.guard.IntentWrapper;
import com.process.keepalive.daemon.guard.R;
import com.process.keepalive.daemon.guard.pixel.ScreenManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        ScreenManager.getInstance().register(this);
    }



    @Override
    public void onBackPressed() {
        IntentWrapper.onBackPressed(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ScreenManager.getInstance().unRegister(this);
    }

}
