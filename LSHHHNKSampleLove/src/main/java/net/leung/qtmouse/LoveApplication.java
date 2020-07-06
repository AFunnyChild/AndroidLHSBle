package net.leung.qtmouse;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeechUtility;
import com.process.keepalive.daemon.DemoService;
import com.process.keepalive.daemon.guard.DaemonEnv;
import com.process.keepalive.daemon.guard.pixel.ScreenManager;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.UpgradeInfo;

import net.leung.qtmouse.tools.Screen;

import java.util.Locale;

public class LoveApplication extends Application {
   Activity mActivity;
   MouseAccessibilityService mService;
    @Override
    public void onCreate() {
        DaemonEnv.initialize(getApplicationContext(), DemoService.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
        DemoService.sShouldStopService = false;
        DaemonEnv.startServiceMayBind(DemoService.class);
        DaemonEnv.startServiceMayBind(MouseAccessibilityService.class);

        SpeechUtility.createUtility(LoveApplication.this, "appid=" + "5ef16797");
        Setting.setLogLevel(Setting.LOG_LEVEL.none);
        super.onCreate();
        mApplication=this;
        ScreenManager.getInstance().register(this);
//        Beta.smallIconId = getResources().getIdentifier("ic_launcher", "id", getPackageName());
//        Bugly.init(getApplicationContext(), "8e1eee2cd5", true);
//
//        UpgradeInfo upgradeInfo = Beta.getUpgradeInfo();
//
//        //String title, int upgradeType, String newFeature, long publishTime, int buildNo, int versioncode, String versionName, String downloadUrl, long fileSize, String fileMd5, String bannerUrl, int dialogStyle, DownloadListener listener, Runnable upgradeRunnable, Runnable cancelRunnable, boolean isManual
//
//        StringBuilder info = new StringBuilder();
//        if (upgradeInfo!=null){
//            info.append("id: ").append(upgradeInfo.id).append("\n");
//            info.append("标题: ").append(upgradeInfo.title).append("\n");
//            info.append("升级说明: ").append(upgradeInfo.newFeature).append("\n");
//            info.append("versionCode: ").append(upgradeInfo.versionCode).append("\n");
//            info.append("versionName: ").append(upgradeInfo.versionName).append("\n");
//            info.append("发布时间: ").append(upgradeInfo.publishTime).append("\n");
//            info.append("安装包Md5: ").append(upgradeInfo.apkMd5).append("\n");
//            info.append("安装包下载地址: ").append(upgradeInfo.apkUrl).append("\n");
//            info.append("安装包大小: ").append(upgradeInfo.fileSize).append("\n");
//            info.append("弹窗间隔（ms）: ").append(upgradeInfo.popInterval).append("\n");
//            info.append("弹窗次数: ").append(upgradeInfo.popTimes).append("\n");
//            info.append("发布类型（0:测试 1:正式）: ").append(upgradeInfo.publishType).append("\n");
//            info.append("弹窗类型（1:建议 2:强制 3:手工）: ").append(upgradeInfo.upgradeType).append("\n");
//            info.append("图片地址：").append(upgradeInfo.imageUrl);
//            Log.d("Application", "upgradeInfo: "+(info.toString()));
//
//            //  Beta.showUpgradeDialog(upgradeInfo.title,upgradeInfo.upgradeType,upgradeInfo.newFeature,upgradeInfo.publishTime,1,upgradeInfo.versionCode,upgradeInfo.versionName,upgradeInfo.apkUrl,upgradeInfo.fileSize,upgradeInfo.apkMd5,upgradeInfo.imageUrl,0,null,null,null,true);
//        }
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


}
