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
        setContentView(R.layout.activity_main);

        ScreenManager.getInstance().register(this);
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_start) {
            DemoService.sShouldStopService = false;
            DaemonEnv.startServiceMayBind(DemoService.class);
        } else if (id == R.id.btn_white) {
            IntentWrapper.whiteListMatters(this, "轨迹跟踪服务的持续运行");
        } else if (id == R.id.btn_stop) {
            DemoService.stopService();
        }
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
