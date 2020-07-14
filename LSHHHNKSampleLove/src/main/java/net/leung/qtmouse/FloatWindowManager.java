package net.leung.qtmouse;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Toast;

import net.leung.qtmouse.rom.HuaweiUtils;
import net.leung.qtmouse.rom.MeizuUtils;
import net.leung.qtmouse.rom.MiuiUtils;
import net.leung.qtmouse.rom.OppoUtils;
import net.leung.qtmouse.rom.QikuUtils;
import net.leung.qtmouse.rom.RomUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.android.sidebar.views.SideBarContent;
import com.process.keepalive.daemon.DemoService;
import com.process.keepalive.daemon.guard.DaemonEnv;

public class FloatWindowManager {
    private static final String TAG = "FloatWindowManager";

    private static volatile FloatWindowManager instance;

    private boolean isWindowDismiss = true;

    private AVCallFloatView floatView = null;
    public CursorView cursorView = null;
    private Dialog dialog;

    public static FloatWindowManager getInstance() {
        if (instance == null) {
            synchronized (FloatWindowManager.class) {
                if (instance == null) {
                    instance = new FloatWindowManager();
                }
            }
        }
        return instance;
    }

    public AVCallFloatView getFloatView() {
        return floatView;
    }
    public  boolean  mIsShowMenu=false;
    public boolean applyOrShowFloatWindow(Context context,boolean showMenu) {
         this.mIsShowMenu=showMenu;
        if (!checkAndApplyPermission(context)) {
            return false;
        }

        showWindow(context,showMenu);
        return true;
    }
    public void applyOrShowFloatWindowResume(Context context){
        applyOrShowFloatWindow(context,mIsShowMenu);
    }

    public boolean checkAndApplyPermission(Context context) {
        if (!checkPermission(context)) {

            applyPermission(context);
            return false;
        }

        //检查辅助权限
        final String serviceStr = context.getPackageName() + "/." + MouseAccessibilityService.class.getSimpleName();
        if (!PermissionUtil.isAccessibilityServiceEnable(context)) {
            showConfirmDialog(context, "您的手机没有授予辅助服务（" + context.getString(R.string.app_name) + "）权限，请开启后再试", confirm -> {
                if (confirm) {
                    BaseAccessibilityService.goAccess(context);
                } else {
                    Log.e(TAG, "user manually refuse ACCESSIBILITY_SERVICE");
                }
            });
        Intent intent1 = new Intent(context, MouseAccessibilityService.class);
          context.startService(intent1);
            return false;
        }

        return true;
    }

    private boolean checkPermission(Context context) {
        //6.0 版本之后由于 google 增加了对悬浮窗权限的统一管理
        if (Build.VERSION.SDK_INT < 23) {
            if (RomUtils.checkIsMiuiRom()) {
                return miuiPermissionCheck(context);
            } else if (RomUtils.checkIsMeizuRom()) {
                return meizuPermissionCheck(context);
            } else if (RomUtils.checkIsHuaweiRom()) {
                return huaweiPermissionCheck(context);
            } else if (RomUtils.checkIs360Rom()) {
                return qikuPermissionCheck(context);
            } else if (RomUtils.checkIsOppoRom()) {
                return oppoROMPermissionCheck(context);
            }
        }
        return commonROMPermissionCheck(context);
    }

    private boolean huaweiPermissionCheck(Context context) {
        return HuaweiUtils.checkFloatWindowPermission(context);
    }

    private boolean miuiPermissionCheck(Context context) {
        return MiuiUtils.checkFloatWindowPermission(context);
    }

    private boolean meizuPermissionCheck(Context context) {
        return MeizuUtils.checkFloatWindowPermission(context);
    }

    private boolean qikuPermissionCheck(Context context) {
        return QikuUtils.checkFloatWindowPermission(context);
    }

    private boolean oppoROMPermissionCheck(Context context) {
        return OppoUtils.checkFloatWindowPermission(context);
    }

    private boolean commonROMPermissionCheck(Context context) {
        //最新发现魅族6.0的系统这种方式不好用，没办法，只能单独适配一下
        if (RomUtils.checkIsMeizuRom()) {
            return meizuPermissionCheck(context);
        } else {
            Boolean result = true;
            if (Build.VERSION.SDK_INT >= 23) {
                try {
                    Class clazz = Settings.class;
                    Method canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context.class);
                    result = (Boolean) canDrawOverlays.invoke(null, context);
                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            }
            return result;
        }
    }

    private void applyPermission(Context context) {

        if (Build.VERSION.SDK_INT < 23) {
            if (RomUtils.checkIsMiuiRom()) {
                miuiROMPermissionApply(context);
            } else if (RomUtils.checkIsMeizuRom()) {
                meizuROMPermissionApply(context);
            } else if (RomUtils.checkIsHuaweiRom()) {
                huaweiROMPermissionApply(context);
            } else if (RomUtils.checkIs360Rom()) {
                ROM360PermissionApply(context);
            } else if (RomUtils.checkIsOppoRom()) {
                oppoROMPermissionApply(context);
            }
        } else {
            commonROMPermissionApply(context);
        }
    }

    private void ROM360PermissionApply(final Context context) {
        showConfirmDialog(context, confirm -> {
            if (confirm) {
                QikuUtils.applyPermission(context);
            } else {
                Log.e(TAG, "ROM:360, user manually refuse OVERLAY_PERMISSION");
            }
        });
    }

    private void huaweiROMPermissionApply(final Context context) {
        showConfirmDialog(context, confirm -> {
            if (confirm) {
                HuaweiUtils.applyPermission(context);
            } else {
                Log.e(TAG, "ROM:huawei, user manually refuse OVERLAY_PERMISSION");
            }
        });
    }

    private void meizuROMPermissionApply(final Context context) {
        showConfirmDialog(context, confirm -> {
            if (confirm) {
                MeizuUtils.applyPermission(context);
            } else {
                Log.e(TAG, "ROM:meizu, user manually refuse OVERLAY_PERMISSION");
            }
        });
    }

    private void miuiROMPermissionApply(final Context context) {
        showConfirmDialog(context, confirm -> {
            if (confirm) {
                MiuiUtils.applyMiuiPermission(context);
            } else {
                Log.e(TAG, "ROM:miui, user manually refuse OVERLAY_PERMISSION");
            }
        });
    }

    private void oppoROMPermissionApply(final Context context) {
        showConfirmDialog(context, confirm -> {
            if (confirm) {
                OppoUtils.applyOppoPermission(context);
            } else {
                Log.e(TAG, "ROM:miui, user manually refuse OVERLAY_PERMISSION");
            }
        });
    }

    //通用 rom 权限申请
    private void commonROMPermissionApply(final Context context) {
        //这里也一样，魅族系统需要单独适配
        if (RomUtils.checkIsMeizuRom()) {
            meizuROMPermissionApply(context);
        } else {
            if (Build.VERSION.SDK_INT >= 23) {
                showConfirmDialog(context, confirm -> {
                    if (confirm) {
                        try {
                            commonROMPermissionApplyInternal(context);
                        } catch (Exception e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                    } else {
                        Log.d(TAG, "user manually refuse OVERLAY_PERMISSION");
                    }
                });
            }
        }
    }

    public static void commonROMPermissionApplyInternal(Context context) throws NoSuchFieldException, IllegalAccessException {
        Class clazz = Settings.class;
        Field field = clazz.getDeclaredField("ACTION_MANAGE_OVERLAY_PERMISSION");
        Intent intent = new Intent(field.get(null).toString());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }

    private void showConfirmDialog(Context context, OnConfirmResult result) {
        showConfirmDialog(context, "您的手机没有授予悬浮窗权限，请开启后再试", result);
    }
      String  mMessage="";
     Context mContext=null;
    private void showConfirmDialog(Context context, String message, final OnConfirmResult result) {
        //
        if (dialog ==null||(mMessage.equals(message)==false)||(mContext==null)||(mContext!=context)) {
            if (dialog!=null){
                dialog.dismiss();
                dialog=null;
            }
            mMessage=message;
            mContext=context;
            dialog = new AlertDialog.Builder(LoveApplication.getInstance().getMainActivity()).setCancelable(false).setTitle("")
                    .setMessage(message)
                    .setPositiveButton("现在去开启",
                            (dialog, which) -> {
                                result.confirmResult(true);
                                dialog.dismiss();
                                dialog=null;
                            }).create();
        }
//.setNegativeButton("暂不开启",
//                (dialog, which) -> {
//                    result.confirmResult(false);
//                    dialog.dismiss();
//                    dialog=null;
//                })


        if (dialog.isShowing()==false){

            dialog.show();
        }

    }

    private interface OnConfirmResult {
        void confirmResult(boolean confirm);
    }

    public static WindowManager.LayoutParams createLayoutParams(Context context) {
        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
        mParams.packageName = context.getPackageName();
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        int mType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mType = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT ;
        }
        mParams.type = mType;
        mParams.format = PixelFormat.RGBA_8888;
        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        return mParams;
    }

    public void showWindow(Context context,boolean showMenu) {

        MouseAccessibilityService service = LoveApplication.getInstance().getService();
       if (service==null){
           return;
       }
        floatView=AVCallFloatView.getInstance(context);
        floatView.setNeedAnchorToSide(UserSettings.FloatViewAnchorToSide);
        floatView.setIsShowing(showMenu);



            SideBarContent.getInstance().createToucher(service);
            SideBarContent.getInstance().setiSideEventListener(new SideBarContent.ISideEventListener() {
                @Override
                public void onEvent(int eventIndex) {
                    if (eventIndex==0){
                        service.performScrollBackward();

                    }else{
                        service.performScrollForward();
                    }
                }
            });
            SideBarContent.getInstance().setIsShowing(showMenu);


        cursorView = CursorView.getInstance(context);
        cursorView.setMoveSpeed(UserSettings.CursorMoveSpeed);
        cursorView.setIsShowing(showMenu);


        DaemonEnv.initialize(LoveApplication.getInstance(), DemoService.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
        DemoService.sShouldStopService = false;
        DaemonEnv.startServiceMayBind(DemoService.class);
        DaemonEnv.startServiceMayBind(MouseAccessibilityService.class);
    }

    public void dismissWindow() {
        if (isWindowDismiss) {
            Log.e(TAG, "window can not be dismiss cause it has not been added");
            return;
        }

        isWindowDismiss = true;
        if(floatView != null){
            floatView.setIsShowing(false);
        }
        cursorView.setIsShowing(false);
    }
}
