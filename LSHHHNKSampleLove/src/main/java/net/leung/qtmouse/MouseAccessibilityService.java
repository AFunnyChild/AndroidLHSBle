package net.leung.qtmouse;

import android.accessibilityservice.GestureDescription;
import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import net.leung.qtmouse.tools.Screen;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import android.os.Bundle;
import android.os.Handler;

import com.android.sidebar.views.SideBarContent;
import com.feng.mydemo.activity.BleScanActivity;
import com.process.keepalive.daemon.MainActivity;

import android.content.Context;
import android.content.ClipData;
import android.content.ClipboardManager;



public class MouseAccessibilityService extends BaseAccessibilityService {
    private static MouseAccessibilityService activity;
    private static boolean serviceRunning = false;
    /**
     * 设置滑动的起点是否从光标位置开始
     *
     * @param value
     */
    public static void setScrollStartWithCursor(int value) {
        SCROLL_START_WITH_CURSOR = value != 0;
    }

    private static boolean SCROLL_START_WITH_CURSOR = false;

    /**
     * 左右滑动一次的距离
     */
    public static void setLeftRightScrollDistance(int value) {
        LEFT_RIGHT_SCROLL_DISTANCE = value;
    }

    private static int LEFT_RIGHT_SCROLL_DISTANCE = 500;

    /**
     * 左右滑动一次消耗的时间
     */
    public static void setLeftRightScrollDuration(int value) {
        LEFT_RIGHT_SCROLL_DURATION = value;
    }

    private static int LEFT_RIGHT_SCROLL_DURATION = 220;

    /**
     * 上下滑动一次的距离
     */
    public static void setUpDownScrollDistance(int value) {
        UP_DOWN_SCROLL_DISTANCE = value;
    }

    private static int UP_DOWN_SCROLL_DISTANCE = 500;

    /**
     * 上下滑动一次消耗的时间
     */
    public static void setUpDownScrollDuration(int value) {
        UP_DOWN_SCROLL_DURATION = value;
    }

    private static int UP_DOWN_SCROLL_DURATION = 500;

    private static final String TAG = MouseAccessibilityService.class.getName();

    private static void logNodeHierachy(AccessibilityNodeInfo nodeInfo, int depth) {
        if (nodeInfo == null) return;

        Rect bounds = new Rect();
        nodeInfo.getBoundsInScreen(bounds);

        StringBuilder sb = new StringBuilder();
        if (depth > 0) {
            for (int i = 0; i < depth; i++) {
                sb.append("  ");
            }
            sb.append("\u2514 ");
        }
        sb.append(nodeInfo.getClassName());
        sb.append(" (" + nodeInfo.getChildCount() + ")");
        sb.append(" " + bounds.toString());
        if (nodeInfo.getText() != null) {
            sb.append(" - \"" + nodeInfo.getText() + "\"");
        }
        Log.v(TAG, sb.toString());

        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo childNode = nodeInfo.getChild(i);
            if (childNode != null) {
                logNodeHierachy(childNode, depth + 1);
            }
        }
    }

    private static AccessibilityNodeInfo findSmallestNodeAtPoint(AccessibilityNodeInfo sourceNode, int x, int y) {
        if (sourceNode == null) return sourceNode;

        Rect bounds = new Rect();
        sourceNode.getBoundsInScreen(bounds);

        if (!bounds.contains(x, y)) {
            return null;
        }

        for (int i = 0; i < sourceNode.getChildCount(); i++) {
            AccessibilityNodeInfo nearestSmaller = findSmallestNodeAtPoint(sourceNode.getChild(i), x, y);
            if (nearestSmaller != null) {
                return nearestSmaller;
            }
        }
        return sourceNode;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        super.onAccessibilityEvent(event);
    }

    @Override
    public void onInterrupt() {
        super.onInterrupt();
    }

    @Override
    public void onCreate() {
        super.onCreate();
               LoveApplication.getInstance().initService(this);
      activity = this;
        EventBus.getDefault().register(this);
      FloatWindowManager.getInstance().applyOrShowFloatWindowResume(this);


        Log.d(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);

        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        //更新屏幕大小
        Screen.update(newConfig.orientation);

        //更新光标位置
        CursorView cursorView = CursorView.getInstance(this);
        if (cursorView != null) cursorView.updatePosition();
    }

    interface ActionHandler {
        void exe(AccessibilityNodeInfo nodeInfo);
    }

    private void doActionUnderMouse(ActionHandler handler) {
        CursorView cursorView = CursorView.getInstance(this);

        if (handler == null || cursorView == null) return;

        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();

        if (nodeInfo == null) return;

        final LayoutParams cursorLayout = cursorView.layoutParams;
        AccessibilityNodeInfo nearestNodeToMouse = findSmallestNodeAtPoint(
                nodeInfo,
                cursorLayout.x+10,
                cursorLayout.y+10);

        if (nearestNodeToMouse != null) {
            logNodeHierachy(nearestNodeToMouse, 0);
            handler.exe(nearestNodeToMouse);
        }

        nodeInfo.recycle();
    }

    private void click() {
        CursorView cursorView = CursorView.getInstance();
        if (Build.VERSION.SDK_INT >= 24 && cursorView != null) {
            final LayoutParams cursorLayout = cursorView.layoutParams;
            performGestureClick(
                    cursorLayout.x+10,
                    cursorLayout.y+10,
                    new GestureMoveCallback("performGestureClick") {
                        @Override
                        public void onCancelled(GestureDescription gestureDescription) {
                            doActionUnderMouse(nodeInfo -> {
                                performViewClick(nodeInfo);
                            });

                            super.onCancelled(gestureDescription);
                        }
                    });
            return;
        }

        doActionUnderMouse(nodeInfo -> {
            performViewClick(nodeInfo);
        });
    }

    private void longClick() {
        CursorView cursorView = CursorView.getInstance();

        if (Build.VERSION.SDK_INT >= 24 && cursorView != null) {
            final LayoutParams cursorLayout = cursorView.layoutParams;
            performGestureLongClick(
                    cursorLayout.x,
                    cursorLayout.y,
                    new GestureMoveCallback("performGestureLongClick") {
                        @Override
                        public void onCancelled(GestureDescription gestureDescription) {
                            doActionUnderMouse(nodeInfo -> {
                                performViewLongClick(nodeInfo);
                            });

                            super.onCancelled(gestureDescription);
                        }
                    });
            return;
        }

        doActionUnderMouse(nodeInfo -> {
            performViewLongClick(nodeInfo);
        });
    }

    //以下成员变量声明为了避免每次操作重新申请内存
    /**
     * 滑动起点和终点
     */
    private Point startPoint = new Point();
    private Point endPoint = new Point();
    /**
     * 滑动结果回调
     */
    private GestureMoveCallback performScrollBackwardCB = new GestureMoveCallback("performScrollBackward") {
        @Override
        public void onCancelled(GestureDescription gestureDescription) {
            doActionUnderMouse(nodeInfo -> {
                performScrollBackward(nodeInfo);
            });

            super.onCancelled(gestureDescription);
        }
    };
    private GestureMoveCallback performScrollForwardCB = new GestureMoveCallback("performScrollForward") {
        @Override
        public void onCancelled(GestureDescription gestureDescription) {
            doActionUnderMouse(nodeInfo -> {
                performScrollForward(nodeInfo);
            });

            super.onCancelled(gestureDescription);
        }
    };
    @TargetApi(23)
    private GestureMoveCallback performScrollLeftCB = new GestureMoveCallback("performScrollLeft") {
        @Override
        public void onCancelled(GestureDescription gestureDescription) {
            doActionUnderMouse(nodeInfo -> {
                performScroll(nodeInfo, AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_LEFT.getId());
            });

            super.onCancelled(gestureDescription);
        }
    };
    @TargetApi(23)
    private GestureMoveCallback performScrollRightCB = new GestureMoveCallback("performScrollRight") {
        @Override
        public void onCancelled(GestureDescription gestureDescription) {
            doActionUnderMouse(nodeInfo -> {
                performScroll(nodeInfo, AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_RIGHT.getId());
            });

            super.onCancelled(gestureDescription);
        }
    };

    /**
     * 上下滑动起点距离中心点的距离
     */
    private final int scrollUpDownDistance = 320;

    /**
     * scroll from down to up
     */
    public void performScrollBackward() {
        if (Build.VERSION.SDK_INT < 24) {
            doActionUnderMouse(nodeInfo -> {
                performScrollBackward(nodeInfo);
            });
        } else if (CursorView.getInstance() != null) {
            final LayoutParams cursorLayout = CursorView.getInstance().layoutParams;

            if (SCROLL_START_WITH_CURSOR) {
                startPoint.set(cursorLayout.x-150, cursorLayout.y);
                endPoint.set(cursorLayout.x-150, Screen.y(cursorLayout.y + UP_DOWN_SCROLL_DISTANCE));
            } else {
                int centerY = Screen.getHeight() / 2;
                startPoint.set(cursorLayout.x-150, centerY - scrollUpDownDistance);
                endPoint.set(cursorLayout.x-150, centerY + scrollUpDownDistance);
            }

            performGestureMove(startPoint, endPoint, UP_DOWN_SCROLL_DURATION, performScrollBackwardCB);
        }
    }

    /**
     * scroll from up to down
     */
    public void performScrollForward() {
        if (Build.VERSION.SDK_INT < 24) {
            doActionUnderMouse(nodeInfo -> {
                performScrollForward(nodeInfo);
            });
        } else if (CursorView.getInstance() != null) {
            final LayoutParams cursorLayout = CursorView.getInstance().layoutParams;

            if (SCROLL_START_WITH_CURSOR) {
                startPoint.set(cursorLayout.x-150, cursorLayout.y);
                endPoint.set(cursorLayout.x-150, Screen.y(cursorLayout.y - UP_DOWN_SCROLL_DISTANCE));
            } else {
                int centerY = Screen.getHeight() / 2;
                startPoint.set(cursorLayout.x-150, centerY + scrollUpDownDistance);
                endPoint.set(cursorLayout.x-150, centerY - scrollUpDownDistance);
            }

            performGestureMove(startPoint, endPoint, UP_DOWN_SCROLL_DURATION, performScrollForwardCB);
        }
    }

    /**
     * 模拟左划
     */
    private void performScrollLeft() {
        if (Build.VERSION.SDK_INT >= 24 && CursorView.getInstance() != null) {
            final LayoutParams cursorLayout = CursorView.getInstance().layoutParams;

            if (SCROLL_START_WITH_CURSOR) {
                startPoint.set(cursorLayout.x, cursorLayout.y);
                endPoint.set(Screen.x(cursorLayout.x - LEFT_RIGHT_SCROLL_DISTANCE), cursorLayout.y);
            } else {
                startPoint.set(Screen.getWidth() - 40, cursorLayout.y);
                endPoint.set(0, cursorLayout.y);
            }

            performGestureMove(startPoint, endPoint, LEFT_RIGHT_SCROLL_DURATION, performScrollLeftCB);
        } else if (Build.VERSION.SDK_INT >= 23) {
            doActionUnderMouse(nodeInfo -> {
                performScroll(nodeInfo, AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_LEFT.getId());
            });
        }
    }

    /**
     * 模拟右划
     */
    private void performScrollRight() {
        if (Build.VERSION.SDK_INT >= 24 && CursorView.getInstance() != null) {
            final LayoutParams cursorLayout = CursorView.getInstance().layoutParams;

            if (SCROLL_START_WITH_CURSOR) {
                startPoint.set(cursorLayout.x, cursorLayout.y);
                endPoint.set(Screen.x(cursorLayout.x + LEFT_RIGHT_SCROLL_DISTANCE), cursorLayout.y);
            } else {
                startPoint.set(40, cursorLayout.y);
                endPoint.set(Screen.getWidth(), cursorLayout.y);
            }

            performGestureMove(startPoint, endPoint, LEFT_RIGHT_SCROLL_DURATION, performScrollRightCB);
        } else if (Build.VERSION.SDK_INT >= 23) {
            doActionUnderMouse(nodeInfo -> {
                performScroll(nodeInfo, AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_RIGHT.getId());
            });
        }
    }

    /**
     * 放大缩小手势划动的距离
     */
    private static final int zoomDistance = 300;

    /**
     * 放大缩小手势划动的时间
     */
    private static final int zoomDuration = 300;

    /**
     * 模拟放大手势
     */
    @TargetApi(24)
    private void performZoomIn() {
        int centerX = Screen.getWidth() / 2;
        int centerY = Screen.getHeight() / 2;
        int xOffset = centerX + 30;

        //move from center to up
        Path path1 = new Path();
        path1.moveTo(centerX, centerY - 200);
        path1.lineTo(xOffset, Screen.y(centerY - zoomDistance));

        //move from center to down
        Path path2 = new Path();
        path2.moveTo(xOffset, centerY + 200);
        path2.lineTo(centerX, Screen.y(centerY + zoomDistance));

        GestureDescription.StrokeDescription sd1 = new GestureDescription.StrokeDescription(path1, 50, zoomDuration);
        GestureDescription.StrokeDescription sd2 = new GestureDescription.StrokeDescription(path2, 50, zoomDuration);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        GestureDescription gestureDescription = builder.addStroke(sd1).addStroke(sd2).build();

        dispatchGesture(gestureDescription, new GestureMoveCallback("performZoomIn").start(), null);
    }

    /**
     * 模拟缩小手势
     */
    @TargetApi(24)
    private void performZoomOut() {
        int centerX = Screen.getWidth() / 2;
        int centerY = Screen.getHeight() / 2;
        int xOffset = centerX + 30;

        //move from up to center
        Path path1 = new Path();
        path1.moveTo(xOffset, Screen.y(centerY - zoomDistance));
        path1.lineTo(centerX, centerY - 200);

        //move from down to center
        Path path2 = new Path();
        path2.moveTo(centerX, Screen.y(centerY + zoomDistance));
        path2.lineTo(xOffset, centerY + 200);

        GestureDescription.StrokeDescription sd1 = new GestureDescription.StrokeDescription(path1, 50, zoomDuration);
        GestureDescription.StrokeDescription sd2 = new GestureDescription.StrokeDescription(path2, 50, zoomDuration);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        GestureDescription gestureDescription = builder.addStroke(sd1).addStroke(sd2).build();

        dispatchGesture(gestureDescription, new GestureMoveCallback("performZoomOut").start(), null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStopService(StopServiceEvent event) {
        stopSelf();
    }

AccessibilityNodeInfo focus1;
String  voice_text="";
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMouseMove(MouseEvent event) {
        Log.d(TAG, "event:" + event.action);

        AccessibilityNodeInfo       focus_input = findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
               if (focus_input!=null){
                   focus1=focus_input;
               }
               if (event.voice!=null&&(event.action==MouseEvent.VOICE)){
                   voice_text=event.voice;
                   fillText(focus1,voice_text);
                  // MainActivityJni.VoicePaste();
                   EventBus.getDefault().post(new JniEvent(JniEvent.ON_VOICE_PASTE));
               }
        switch (event.action) {
            case MouseEvent.CLICK:
                if (AVCallFloatView.getInstance(LoveApplication.getInstance()).mCb_twe!=null){
                    if (AVCallFloatView.getInstance(LoveApplication.getInstance()).mCb_twe.isChecked()==false){
                        click();
                    }
                }

                break;
            case MouseEvent.RETURN:
                performBackClick();
                break;
                case MouseEvent.HOME:
                    performHomeClick();
                    break;
            case MouseEvent.SCROLL_UP:
                performScrollForward();
                break;
            case MouseEvent.SCROLL_DOWN:
                performScrollBackward();
                break;
            case MouseEvent.SCROLL_LEFT:
                performScrollLeft();
                break;
            case MouseEvent.SCROLL_RIGHT:
                performScrollRight();
                break;
            case MouseEvent.LOCATION://长按
               // longClick();
                break;
            case MouseEvent.ZOOM_IN://放大
                if (Build.VERSION.SDK_INT >= 24) performZoomIn();
                break;
            case MouseEvent.ZOOM_OUT://缩小
                if (Build.VERSION.SDK_INT >= 24) performZoomOut();
                break;
            default:
                break;
        }
    }

//public static native void onServiceStateChanged(boolean running);

/**
 * 响应开始按钮点击
 */
public static void onStartClicked(int showMenu) {

    Handler handler = new Handler(LoveApplication.getInstance().getMainLooper());
    handler.post(new Runnable() {
        @Override
        public void run() {
            serviceRunning = FloatWindowManager.getInstance().applyOrShowFloatWindow(LoveApplication.getInstance(), showMenu == 1 ? true : false);
        }
    });
}
public static void onStartBlueTooth(int isStart) {
    Handler handler = new Handler(LoveApplication.getInstance().getMainLooper());
    handler.post(new Runnable() {
        @Override
        public void run() {
            BleScanActivity  bleScanActivity=new BleScanActivity(LoveApplication.getInstance().getMainActivity());
                 bleScanActivity.showBleWindow();
        }
    });
}

/**
 * 提供接口给C++模拟操作按钮点击消息
 *
 * @param action 消息ID
 * @param cancel 是否是按钮释放消息（用于持续移动光标）
 */
public static void sendMouseEvent(int action, int cancel) {
EventBus.getDefault().post(new MouseEvent(4));
}

/**
 * 提供接口给C++模拟操作按钮点击消息
 *
 * @param action 消息ID
 */
public static void sendMouseLocationEvent(int action, int x,int y) {
    EventBus.getDefault().post(new MouseEvent(action, x,y));
}
/**
 * 供C++设置悬浮球吸边效果开关
 * @param open true吸附false反之
 */
public static void setFloatViewAnchorToSide(int open) {
    UserSettings.FloatViewAnchorToSide = open != 0;
    AVCallFloatView floatView = FloatWindowManager.getInstance().getFloatView();
    if (floatView != null)
        floatView.setNeedAnchorToSide(UserSettings.FloatViewAnchorToSide );
}

/**
 * 供C++设置光标移动速度
 *
 * @param speed 移动速度（单位：像素/秒）必须大于0
 */
public static void setCursorMoveSpeed(int speed) {
    UserSettings.CursorMoveSpeed = speed;

    if (CursorView.getInstance() != null)
        CursorView.getInstance().setMoveSpeed(UserSettings.CursorMoveSpeed);
}

/**
 * 供C++设置光标到指定位置
 *
 * @param x
 * @param y
 */
public static void setCursorPosition(int x, int y) {
    if (CursorView.getInstance() != null)
        CursorView.getInstance().setPosition(x, y);
 //EventBus.getDefault().post(new MouseEvent(0));
}


/**
 * 填充文本
 */
private void fillText(AccessibilityNodeInfo nodeInfo, String reply) {
    ClipData data = ClipData.newPlainText("reply", reply);
    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    clipboardManager.setPrimaryClip(data);
    if(nodeInfo==null){
        return;
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        Log.e(TAG, "set text"+reply+"--"+nodeInfo);
//            Bundle bundle = new Bundle();
//            bundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
//                    nodeInfo.getText()+ reply);
//            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundle);


        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS); // 获取焦点
        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE); // 执行粘贴
    } else {

        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS); // 获取焦点
        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE); // 执行粘贴
    }
}
}
