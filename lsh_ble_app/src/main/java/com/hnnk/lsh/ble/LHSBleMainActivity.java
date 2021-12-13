package com.hnnk.lsh.ble;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.feng.mydemo.Model.BluetoothLeServiceModel;
import com.feng.mydemo.activity.BleScanActivity;
import com.iflytek.VoiceWakeuperHelper;
import com.ryan.socketwebrtc.MainActivity;

import net.leung.qtmouse.FloatWindowManager;
import  net.leung.qtmouse.LoadingDialog;
import net.leung.qtmouse.JniEvent;
import net.leung.qtmouse.LoveApplication;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
public class LHSBleMainActivity extends Activity implements View.OnClickListener {
    static LHSBleMainActivity activity;
    private static final int REQUEST_CODE_CHOOSE = 23;
    private EditText et_index;
    private VoiceWakeuperHelper mVoiceWakeuperHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS );
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_start).setOnClickListener(this);
        findViewById(R.id.btn_state).setOnClickListener(this);
        findViewById(R.id.btn_0).setOnClickListener(this);
        findViewById(R.id.btn_1).setOnClickListener(this);
        findViewById(R.id.btn_2).setOnClickListener(this);
        findViewById(R.id.btn_3).setOnClickListener(this);
        findViewById(R.id.btn_4).setOnClickListener(this);
        et_index = findViewById(R.id.et_index);
        LoveApplication.getInstance().initActivity(this);
        EventBus.getDefault().register(this);
        activity=this;
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN );
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams
                (FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        //设置顶部,左边布局
        params.gravity= Gravity.TOP|Gravity.LEFT;
        requestPermissions();
       initWake();
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getId());

                while (true){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //UI操作
                             if (isSoftShowing()==false){

                                 mVoiceWakeuperHelper.startListening();
                             }
                            //
                        }
                    });

                }
            }
        }).start();

    }

    private void initWake() {
        mVoiceWakeuperHelper = new VoiceWakeuperHelper();
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
    @SuppressLint("CheckResult")
    @Override
    public void onClick(final View v) {
    if (v.getId()==R.id.btn_start) {
     //   FloatWindowManager.getInstance().applyOrShowFloatWindow(this,true);
        BleScanActivity scanActivity=new BleScanActivity(this);
        scanActivity.showBleWindow();
    }
    if (v.getId()==R.id.btn_0) {
        mVoiceWakeuperHelper.startListening();

    }  if (v.getId()==R.id.btn_1) {
            mVoiceWakeuperHelper.stopListening();
    }  if (v.getId()==R.id.btn_2) {
        Log.e("BleScanActivity", "onClick: " + 2);
        BluetoothLeServiceModel.offsetDirection(2);
    }  if (v.getId()==R.id.btn_3) {
        Log.e("BleScanActivity", "onClick: " + 3);
        BluetoothLeServiceModel.offsetDirection(3);
    }  if (v.getId()==R.id.btn_4) {
        Log.e("BleScanActivity", "onClick: " + 4);
        BluetoothLeServiceModel.offsetDirection(4);
    }

    if(v.getId()==R.id.btn_state){
       // FloatWindowManager.getInstance().applyOrShowFloatWindow(this,true);
        BleScanActivity  bleScanActivity=new BleScanActivity(this);
        bleScanActivity.showBleWindow();
    }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMouseMove(JniEvent event) {
        switch (event.eventType) {
            case JniEvent.ON_VOICE_PASTE:
                break;
                case JniEvent.ON_WINDOW_CHANGE:
                 //  Toast.makeText(activity, "sada", Toast.LENGTH_SHORT).show();
                    //Log.e("ss", "onMouseMove: "+isSoftShowing() );
                break;
                case JniEvent.SOFTINPUT_SHOW:
                    mVoiceWakeuperHelper.stopListening();

                    Log.d( "voicewake", "SOFTINPUT_SHOW: startListening start" );
                break;
                case JniEvent.SOFTINPUT_CAN_CLOSE:
                    mVoiceWakeuperHelper.startListening();
                    Log.d( "voicewake", "SOFTINPUT_CAN_CLOSE: startListening stop" );
                break;
            default:
                break;
        }
    }
    @Override
    public void onResume() {
        super.onResume();
//              if ( FloatWindowManager.getInstance().checkAndApplyPermission(this)==false){
//                  execRootCmd("adb  shell");
//                  execRootCmd("settings put secure enabled_accessibility_services net.leung.qtmouse/net.leung.qtmouse.MouseAccessibilityService");
//                  execRootCmd("settings put secure accessibility_enabled 1");
//
//              }
//
//
//        FloatWindowManager.getInstance().applyOrShowFloatWindow(this,true);
    }
    private boolean isSoftShowing() {
        //获取当屏幕内容的高度
        int screenHeight = this.getWindow().getDecorView().getHeight();
        //获取View可见区域的bottom
        Rect rect = new Rect();
        //DecorView即为activity的顶级view
        this.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        //考虑到虚拟导航栏的情况（虚拟导航栏情况下：screenHeight = rect.bottom + 虚拟导航栏高度）
        //选取screenHeight*2/3进行判断
       // Log.e("test", "isSoftShowing: "+(screenHeight*2/3 +"--"+rect.bottom)+(screenHeight*2/3 >rect.bottom) );
        return screenHeight*2/3 > rect.bottom;

    }




    /**
     * 执行命令并且输出结果
     */
    public static String execRootCmd(String cmd) {
        String result = "";
        DataOutputStream dos = null;
        DataInputStream dis = null;

        try {
            Process p = Runtime.getRuntime().exec("su");// 经过Root处理的android系统即有su命令
            dos = new DataOutputStream(p.getOutputStream());
            dis = new DataInputStream(p.getInputStream());


            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            String line = null;
            while ((line = dis.readLine()) != null) {
                Log.d("result", line);
                result += line;
            }
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }


}
