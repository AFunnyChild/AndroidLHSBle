package net.leung.qtmouse;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

import androidx.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;

public class BaseAccessibilityService extends AccessibilityService {

    private static String TAG = BaseAccessibilityService.class.getName();

    /**
     * Check当前辅助服务是否启用
     *
     * @param serviceName serviceName
     * @return 是否启用
     */
    public static boolean checkAccessibilityEnabled(Context context, final String serviceName) {
        AccessibilityManager accessibilityManager =
                (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        assert accessibilityManager != null;
        List<AccessibilityServiceInfo> accessibilityServices =
                accessibilityManager.getEnabledAccessibilityServiceList(
                        AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo info : accessibilityServices) {
            Log.e("AccessibilityService", context.getPackageName());
            if (info.getId().contains(context.getPackageName())) {
                Log.e("AccessibilityService", "AccessibilityService permission open");
                return true;
            }
        }

        Log.e("AccessibilityService", "AccessibilityService permission close");
        return false;
    }

/**
  * @param context
  * @return
  * AccessibilityService permission check
  */
 public static boolean isAccessibilityServiceEnable(Context context) {
     AccessibilityManager accessibilityManager =
             (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
     assert accessibilityManager != null;
     List<AccessibilityServiceInfo> accessibilityServices =
             accessibilityManager.getEnabledAccessibilityServiceList(
                     AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
     for (AccessibilityServiceInfo info : accessibilityServices) {
         if (info.getId().contains(context.getPackageName())) {
             Log.e("AccessibilityService", "AccessibilityService permission open");

             return true;
         }
     }

     Log.e("AccessibilityService", "AccessibilityService permission close");
     return false;
 }
    /**
     * 前往开启辅助服务界面
     */
    public static void goAccess(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
//        Intent accessibleIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
//        startActivityForResult(accessibleIntent,ACCESSIBILITY_REQUEST_CODE);
    }

    /**
     * 模拟点击事件
     *
     * @param nodeInfo nodeInfo
     */
    public void performViewClick(AccessibilityNodeInfo nodeInfo) {
        while (nodeInfo != null) {
            if (nodeInfo.isClickable()) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }

            nodeInfo = nodeInfo.getParent();
        }
    }

    /**
     * 模拟长按事件
     *
     * @param nodeInfo nodeInfo
     */
    public void performViewLongClick(AccessibilityNodeInfo nodeInfo) {
        while (nodeInfo != null) {
            if (nodeInfo.isLongClickable()) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
                break;
            }

            nodeInfo = nodeInfo.getParent();
        }
    }

    /**
     * 发送一个点击事件
     *
     * @param x
     * @param y
     * @param callback
     * @return
     */
    @TargetApi(24)
    public boolean performGestureClick(int x, int y, @Nullable GestureResultCallback callback) {
        Path path = new Path();
        path.moveTo(x, y);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(path, 0, 50);
        GestureDescription gestureDescription = builder.addStroke(sd).build();
        return dispatchGesture(gestureDescription, callback, null);
    }

    /**
     * 发送一个长点击事件
     *
     * @param x
     * @param y
     * @param callback
     * @return
     */
    @TargetApi(24)
    public boolean performGestureLongClick(int x, int y, @Nullable GestureResultCallback callback) {
        Path path = new Path();
        path.moveTo(x, y);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(path, 0, 1000);
        GestureDescription gestureDescription = builder.addStroke(sd).build();
        return dispatchGesture(gestureDescription, callback, null);
    }

    @TargetApi(24)
    public class GestureMoveCallback extends GestureResultCallback {

        private final String name;

        GestureMoveCallback(String name) {
            this.name = name;
        }

        public GestureMoveCallback start() {

            return this;
        }

        @Override
        public void onCompleted(GestureDescription gestureDescription) {
            super.onCompleted(gestureDescription);
            Log.d(TAG, name + " onCompleted");


        }

        @Override
        public void onCancelled(GestureDescription gestureDescription) {
            super.onCancelled(gestureDescription);
            Log.d(TAG, name + " onCancelled");

        }
    }

    private Path movePath = new Path();

    /**
     * 发送一个滑动事件
     *
     * @param start    滑动起点
     * @param end      滑动终点
     * @param duration 滑动时间
     * @return
     */
    @TargetApi(24)
    public boolean performGestureMove(Point start, Point end, final int duration, GestureMoveCallback callback) {
        movePath.rewind();
        movePath.moveTo(start.x, start.y);
        movePath.lineTo(end.x, end.y);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(movePath, 50, duration);
        GestureDescription gestureDescription = builder.addStroke(sd).build();
        Log.d(TAG, "start:" + start + " end:" + end + " time:" + duration);
        return dispatchGesture(gestureDescription, callback.start(), null);
    }

    /**
     * 模拟返回操作
     */
    public void performBackClick() {
        performGlobalAction(GLOBAL_ACTION_BACK);
    }
/**
 * 模拟主页操作
 */

public void performHomeClick() {

    performGlobalAction( GLOBAL_ACTION_HOME);
}
    /**
     * 模拟下滑操作
     */
    public static boolean performScrollBackward(AccessibilityNodeInfo nodeInfo) {
        return performScroll(nodeInfo, AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
    }

    /**
     * 模拟上滑操作
     */
    public static boolean performScrollForward(AccessibilityNodeInfo nodeInfo) {
        return performScroll(nodeInfo, AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
    }

    /**
     * 模拟滑动操作
     *
     * @param nodeInfo 要滑动的节点
     * @param action   滑动Action
     * @return 是否执行成功
     */
    protected static boolean performScroll(AccessibilityNodeInfo nodeInfo, int action) {
        while (nodeInfo != null) {
            if (nodeInfo.isScrollable())
                return nodeInfo.performAction(action);

            nodeInfo = nodeInfo.getParent();
        }

        return false;
    }

    /**
     * 查找对应文本的View
     *
     * @param text text
     * @return View
     */
    public AccessibilityNodeInfo findViewByText(String text) {
        return findViewByText(text, false);
    }

    /**
     * 查找对应文本的View
     *
     * @param text      text
     * @param clickable 该View是否可以点击
     * @return View
     */
    public AccessibilityNodeInfo findViewByText(String text, boolean clickable) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return null;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText(text);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null && (nodeInfo.isClickable() == clickable)) {
                    return nodeInfo;
                }
            }
        }
        return null;
    }

    /**
     * 查找对应ID的View
     *
     * @param id id
     * @return View
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public AccessibilityNodeInfo findViewByID(String id) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return null;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null) {
                    return nodeInfo;
                }
            }
        }
        return null;
    }

    public void clickTextViewByText(String text) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText(text);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null) {
                    performViewClick(nodeInfo);
                    break;
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void clickTextViewByID(String id) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null) {
                    performViewClick(nodeInfo);
                    break;
                }
            }
        }
    }

    /**
     * 模拟输入
     *
     * @param nodeInfo nodeInfo
     * @param text     text
     */
    public void inputText(AccessibilityNodeInfo nodeInfo, String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", text);
            clipboard.setPrimaryClip(clip);
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
        Log.d("bac", "onInterrupt");
    }
}
