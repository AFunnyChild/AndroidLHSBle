package com.process.keepalive.daemon.guard.pixel;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.process.keepalive.daemon.guard.DaemonEnv;
import com.process.keepalive.daemon.guard.LogUtils;
import com.process.keepalive.daemon.guard.R;
import com.process.keepalive.daemon.guard.WatchDogService;

public class SinglePixelActivity extends Activity {

    public static void launch(Context context) {
        Intent intent = new Intent(context, SinglePixelActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();

        // 设置左上角
        window.setGravity(Gravity.TOP | Gravity.START);
        WindowManager.LayoutParams params = window.getAttributes();
        // 坐标
        params.x = 0;
        params.y = 0;
        // 设置 1 像素
        params.width = 1;
        params.height = 1;
        window.setAttributes(params);

        LogUtils.i("Keep","SinglePixelActivity onCreate");
        ScreenManager.getInstance().setKeepLiveActivity(this);
    }

    @Override
    protected void onDestroy() {
        DaemonEnv.startServiceMayBind(WatchDogService.class);
        LogUtils.i("Keep","SinglePixelActivity onDestroy");
        super.onDestroy();
    }

}
