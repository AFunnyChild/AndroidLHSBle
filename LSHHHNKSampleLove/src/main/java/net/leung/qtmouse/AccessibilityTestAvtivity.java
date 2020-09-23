package net.leung.qtmouse;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.feng.mydemo.activity.BleScanActivity;
import com.iflytek.VoiceWakeuperHelper;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class AccessibilityTestAvtivity extends AppCompatActivity implements View.OnClickListener {
    static AccessibilityTestAvtivity activity;
    private static final int REQUEST_CODE_CHOOSE = 23;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS );
        //setContentView(R.layout.activity_accessibility_test);
        findViewById(R.id.btn_start).setOnClickListener(this);
       // findViewById(R.id.btn_state).setOnClickListener(this);
        LoveApplication.getInstance().initActivity(this);

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
    int i=0;
    @SuppressLint("CheckResult")
    @Override
    public void onClick(final View v) {
        if (v.getId()==R.id.btn_start){
            FloatWindowManager.getInstance().applyOrShowFloatWindow(LoveApplication.getInstance(),true);
        }else{
            //    FloatWindowManager.getInstance().applyOrShowFloatWindow(LoveApplication.getInstance(),false);
            if (Build.VERSION.SDK_INT >= 23) {
                if (!Settings.canDrawOverlays(this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityForResult(intent, 1);
                } else {
                    Toast.makeText(activity, "suce", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        FloatWindowManager.getInstance().applyOrShowFloatWindowResume(this);
    }
}
