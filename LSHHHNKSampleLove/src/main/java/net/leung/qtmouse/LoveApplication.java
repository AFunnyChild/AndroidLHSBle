package net.leung.qtmouse;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeechUtility;

import com.tencent.bugly.Bugly;

import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.UpgradeInfo;
import com.tencent.bugly.crashreport.CrashReport;

import net.leung.qtmouse.tools.Screen;

import java.util.ArrayList;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


public class LoveApplication extends Application {
   Activity mActivity;
   MouseAccessibilityService mService;
    @Override
    public void onCreate() {
        super.onCreate();
//        ScreenManager.getInstance().register(this);
//        DaemonEnv.initialize(this, DemoService.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
//        DemoService.sShouldStopService = false;
//        DaemonEnv.startServiceMayBind(DemoService.class);
//        if(isRunning(this,"net.leung.qtmouse.MouseAccessibilityService")==true){
//           DaemonEnv.startServiceMayBind(MouseAccessibilityService.class);
//        }
        Log.d("Application", "service name: "+this.getPackageName());
        SpeechUtility.createUtility(LoveApplication.this, "appid=" + "5f5acfb0");
        Setting.setLogLevel(Setting.LOG_LEVEL.none);
        mApplication=this;
        Beta.enableHotfix=false;
        Beta.initDelay=4000;
        Beta.smallIconId = getResources().getIdentifier("ic_launcher", "id", getPackageName());
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
        Bugly.init(getApplicationContext(), "5a7644633a", true,strategy);


        Log.d("Application", "upgradeInfo: "+(Bugly.getAppChannel()));
        Log.d("Application", "init_Info: "+(Beta.getInstance().id+"-"+  Beta.getInstance().moduleName+"-"+  Beta.getInstance().version+"-"+  Beta.getInstance().versionKey));
         UpgradeInfo upgradeInfo = Beta.getUpgradeInfo();
        //String title, int upgradeType, String newFeature, long publishTime, int buildNo, int versioncode, String versionName, String downloadUrl, long fileSize, String fileMd5, String bannerUrl, int dialogStyle, DownloadListener listener, Runnable upgradeRunnable, Runnable cancelRunnable, boolean isManual

        StringBuilder info = new StringBuilder();
        if (upgradeInfo!=null){
            info.append("id: ").append(upgradeInfo.id).append("\n");
            info.append("标题: ").append(upgradeInfo.title).append("\n");
            info.append("升级说明: ").append(upgradeInfo.newFeature).append("\n");
            info.append("versionCode: ").append(upgradeInfo.versionCode).append("\n");
            info.append("versionName: ").append(upgradeInfo.versionName).append("\n");
            info.append("发布时间: ").append(upgradeInfo.publishTime).append("\n");
            info.append("安装包Md5: ").append(upgradeInfo.apkMd5).append("\n");
            info.append("安装包下载地址: ").append(upgradeInfo.apkUrl).append("\n");
            info.append("安装包大小: ").append(upgradeInfo.fileSize).append("\n");
            info.append("弹窗间隔（ms）: ").append(upgradeInfo.popInterval).append("\n");
            info.append("弹窗次数: ").append(upgradeInfo.popTimes).append("\n");
            info.append("发布类型（0:测试 1:正式）: ").append(upgradeInfo.publishType).append("\n");
            info.append("弹窗类型（1:建议 2:强制 3:手工）: ").append(upgradeInfo.upgradeType).append("\n");
            info.append("图片地址：").append(upgradeInfo.imageUrl);
            Log.d("Application", "upgradeInfo: "+(info.toString()));
            //Beta.showUpgradeDialog(upgradeInfo.title,upgradeInfo.upgradeType,upgradeInfo.newFeature,upgradeInfo.publishTime,1,upgradeInfo.versionCode,upgradeInfo.versionName,upgradeInfo.apkUrl,upgradeInfo.fileSize,upgradeInfo.apkMd5,upgradeInfo.imageUrl,0,null,null,null,true);
        }
        ignoreBatteryOptimization(this);
//        AVCallFloatView.getInstance(this);
//        CountDownTimer timer = new CountDownTimer(5000, 1000) {
//            public void onTick(long millisUntilFinished) {
//            }
//            public void onFinish() {
//                AVCallFloatView.getInstance(LoveApplication.this).initWake(LoveApplication.this);
//            }
//        };
//        timer.start();
    }

    /**
     * 忽略电池优化
     */
    private void ignoreBatteryOptimization(Context activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);

            boolean hasIgnored = powerManager.isIgnoringBatteryOptimizations(activity.getPackageName());
            //  判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。
            if (!hasIgnored) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }

    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // 初始化工具
        Screen.init(base);
    }
   public void initActivity(Activity activity){
      this.mActivity=activity;
       }
   public Activity getMainActivity(){
       return mActivity;
       }
   public void initService(MouseAccessibilityService service){
       this.mService=service;
       }
   public MouseAccessibilityService getService(){
       return mService;
       }

private volatile static LoveApplication mApplication = null;
public static LoveApplication getInstance() {
    if (mApplication == null) {
        synchronized (LoveApplication.class) {

        }
    }
    return mApplication;

}
    public  boolean isRunning(Context c,String serviceName)
    {
        ActivityManager myAM=(ActivityManager)c.getSystemService(Context.ACTIVITY_SERVICE);

        ArrayList<ActivityManager.RunningServiceInfo> runningServices = (ArrayList<ActivityManager.RunningServiceInfo>) myAM.getRunningServices(100);
        //获取最多40个当前正在运行的服务，放进ArrList里,以现在手机的处理能力，要是超过40个服务，估计已经卡死，所以不用考虑超过40个该怎么办
        for(int i = 0 ; i<runningServices.size();i++)//循环枚举对比
        {
            if(runningServices.get(i).service.getClassName().toString().equals(serviceName))
            {
                return true;
            }
        }
        return false;
    }

}
