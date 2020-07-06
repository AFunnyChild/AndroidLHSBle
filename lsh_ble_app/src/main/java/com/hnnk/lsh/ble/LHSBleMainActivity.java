package com.hnnk.lsh.ble;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.feng.mydemo.activity.BleScanActivity;
import com.iflytek.VoiceWakeuperHelper;

import net.leung.qtmouse.JniEvent;
import net.leung.qtmouse.MouseEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class LHSBleMainActivity extends AppCompatActivity implements View.OnClickListener {
    static LHSBleMainActivity activity;
    private static final int REQUEST_CODE_CHOOSE = 23;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_start).setOnClickListener(this);
        findViewById(R.id.btn_state).setOnClickListener(this);

       EventBus.getDefault().register(this);
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
        requestPermissions();

        initWake();

    }

    private void initWake() {
        VoiceWakeuperHelper mVoiceWakeuperHelper = new VoiceWakeuperHelper();
        mVoiceWakeuperHelper.initWake(this, new VoiceWakeuperHelper.IReceivedEvent() {
            @Override
            public void onEvent(final int id) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LHSBleMainActivity.this, id+"", Toast.LENGTH_SHORT).show();
                    }
                });
                //

            }
        });
    }
    private void requestPermissions(){
//        mayRequestLocation();
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if(!Settings.System.canWrite(this)){
//                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
//                        Uri.parse("package:" + getPackageName()));
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivityForResult(intent, 108);
//            }else {
//                //有了权限，你要做什么呢？具体的动作
//            }
//
//        }


        try {
            if (Build.VERSION.SDK_INT >= 23) {
                int permission = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if(permission!= PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,new String[]
                            {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.LOCATION_HARDWARE,Manifest.permission.READ_PHONE_STATE,
                                    Manifest.permission.WRITE_SETTINGS,Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_CONTACTS},0x0010);
                }

                if(permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,new String[] {
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},0x0010);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMouseMove(MouseEvent event) {

        switch (event.action) {
            case JniEvent.ON_VOICE_PASTE:
                break;
                case JniEvent.ON_RESET_MOUSE:
                break;
            default:
                break;
        }
    }



}
