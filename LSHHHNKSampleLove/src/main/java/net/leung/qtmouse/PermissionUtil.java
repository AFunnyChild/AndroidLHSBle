package net.leung.qtmouse;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;


import android.text.TextUtils;


/**
 * -|-
 * @author majh
 */
public class PermissionUtil {
    private static final String TAG = "PermissionUtil";
    public static boolean isAccessibilityServiceEnable(Context mContext) {
          int accessibilityEnabled = 0;
          final String service = "net.leung.qtmouse/net.leung.qtmouse.MouseAccessibilityService";
          boolean accessibilityFound = false;
          try {
              accessibilityEnabled = Settings.Secure.getInt(
                      mContext.getApplicationContext().getContentResolver(),
                      android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
              Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
          } catch (Settings.SettingNotFoundException e) {
              Log.e(TAG, "Error finding setting, default accessibility to not found: "
                      + e.getMessage());
          }
          TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

          if (accessibilityEnabled == 1) {
              Log.v(TAG, "***ACCESSIBILIY IS ENABLED*** -----------------");
              String settingValue = Settings.Secure.getString(
                      mContext.getApplicationContext().getContentResolver(),
                      Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
              if (settingValue != null) {
                  TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
                  splitter.setString(settingValue);
                  while (splitter.hasNext()) {
                      String accessabilityService = splitter.next();

                      Log.v(TAG, "-------------- > accessabilityService :: " + accessabilityService);
                      if (accessabilityService.equalsIgnoreCase(service)) {
                          Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
                          return true;
                      }
                  }
              }
          } else {
              Log.v(TAG, "***ACCESSIBILIY IS DISABLED***");
          }

          return accessibilityFound;
      }
//    @RequiresApi(api = Build.VERSION_CODES.M)
//    public static boolean isSettingsCanWrite(Context context) {
//        return Settings.System.canWrite(context);
//    }

//    @RequiresApi(api = Build.VERSION_CODES.M)
//    public static boolean isCanDrawOverlays(Context context) {
//        return Settings.canDrawOverlays(context);
//    }
}
