package net.leung.qtmouse;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;


import com.process.keepalive.daemon.DemoService;
import com.process.keepalive.daemon.MainActivity;
import com.process.keepalive.daemon.guard.DaemonEnv;
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
            DaemonEnv.initialize(context.getApplicationContext(), DemoService.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
            DemoService.sShouldStopService = false;
            DaemonEnv.startServiceMayBind(DemoService.class);
         //   DaemonEnv.startServiceMayBind(MouseAccessibilityService.class);
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

}
