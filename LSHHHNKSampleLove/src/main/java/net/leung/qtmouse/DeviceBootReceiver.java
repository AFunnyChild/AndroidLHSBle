package net.leung.qtmouse;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.process.keepalive.daemon.DemoService;
import com.process.keepalive.daemon.guard.DaemonEnv;

import java.util.ArrayList;


/**
 * device boot receiver
 *
 * @author majh
 */
public class DeviceBootReceiver extends BroadcastReceiver {

    private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_BOOT)) {

            DaemonEnv.initialize(context.getApplicationContext(), DemoService.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
            DemoService.sShouldStopService = false;
            DaemonEnv.startServiceMayBind(DemoService.class);
            if(isRunning(context,"net.leung.qtmouse.MouseAccessibilityService")==true){
                DaemonEnv.startServiceMayBind(MouseAccessibilityService.class);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(MouseAccessibilityService.isAccessibilityServiceEnable(context)) {
                    Intent intent1 = new Intent(context, MouseAccessibilityService.class);
                      context.startService(intent1);

                }else {
                    MainPageGo(context);
                }
            }else {
                if(MouseAccessibilityService.isAccessibilityServiceEnable(context)) {
                    Intent intent1 = new Intent(context, MouseAccessibilityService.class);
                      context.startService(intent1);

                }else {
                    MainPageGo(context);
                }
            }

            if (LoveApplication.getInstance().getMainActivity()==null){
                MainPageGo(context);
            }
        }
    }

    private void MainPageGo(Context context) {
        Class<?> activity_class=null;
        try {
            activity_class   = Class.forName("net.leung.qtmouse.MainActivity");
        } catch (ClassNotFoundException e) {
            //e.printStackTrace();
            Log.d("Application", "MainPageGo: "+e.getMessage());
        }
        if (activity_class!=null){
            Intent launch = new Intent(context, activity_class);
            launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launch);
        }

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
