package net.leung.qtmouse;

import android.accessibilityservice.GestureDescription;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.core.app.NotificationCompat;

import com.android.sidebar.views.SideBarContent;
import com.feng.mydemo.activity.BleScanActivity;
import com.ryan.socketwebrtc.MainActivity;

import net.leung.qtmouse.tools.Screen;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;


public class MouseAccessibilityService extends BaseAccessibilityService {

    private static boolean serviceRunning = false;
    /**
     * 设置滑动的起点是否从光标位置开始
     *
     * @param value
     */
    public static void setScrollStartWithCursor(int value) {
        SCROLL_START_WITH_CURSOR = value != 0;
    }

    private static boolean SCROLL_START_WITH_CURSOR = true;

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
    //创建前台通知，可写成方法体，也可单独写成一个类
    private Notification createForegroundNotification(){
        //前台通知的id名，任意
        String channelId = "ForegroundService";
        //前台通知的名称，任意
        String channelName = "Service";
        //发送通知的等级，此处为高，根据业务情况而定
        int importance = NotificationManager.IMPORTANCE_HIGH;
        //判断Android版本，不同的Android版本请求不一样，以下代码为官方写法
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(channelId,channelName,importance);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        //点击通知时可进入的Activity
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0);
         //.setContentIntent(pendingIntent)//点击通知进入Activity
        //最终创建的通知，以下代码为官方写法
        //注释部分是可扩展的参数，根据自己的功能需求添加
        return new NotificationCompat.Builder(this,channelId)
                .setContentTitle("脑机AI鼠标")
                .setContentText("脑机AI鼠标的服务")
                .setSmallIcon(R.mipmap.mouse_pointer)//通知显示的图标
                .setTicker("脑机AI鼠标的服务")
                .build();
        //.setOngoing(true)
        //.setPriority(NotificationCompat.PRIORITY_MAX)
        //.setCategory(Notification.CATEGORY_TRANSPORT)
        //.setLargeIcon(Icon)
        //.setWhen(System.currentTimeMillis())
    }
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();


        SideBarContent.getInstance().createToucher(MouseAccessibilityService.this);
        SideBarContent.getInstance().setiSideEventListener(new SideBarContent.ISideEventListener() {
            @Override
            public void onEvent(int eventIndex) {
                if (eventIndex==0){
                    performScrollBackward();
                   // longClick();
                }else  if(eventIndex==1){
                    performScrollForward();
                }else  if(eventIndex==2){
                    performScrollLeft();
                }else  if(eventIndex==3){
                    performScrollRight();
                }
            }

            @Override
            public void onIsClicked() {
                EventBus.getDefault().post(new JniEvent(JniEvent.ON_WINDOW_CHANGE));

            }
        });
        CursorView.getInstance(MouseAccessibilityService.this);
        CursorView.getInstance().setMoveSpeed(UserSettings.CursorMoveSpeed);
        CursorView.getInstance().setIsShowing(MouseAccessibilityService.this,true);
        CursorView.getInstance().setCursorDrop(1);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        super.onAccessibilityEvent(event);

   //  Log.e(TAG, "onAccessibilityEvent: "+getRootInActiveWindow().toString() );
        if (event.getEventType()==TYPE_WINDOW_STATE_CHANGED){
           // Log.e(TAG, "onAccessibilityEvent:TYPE_WINDOW_STATE_CHANGED "+event.toString()+event.getContentChangeTypes() );
            if(event.toString().contains("SoftInput")||event.toString().contains("input")){
            //    Log.e(TAG, "onAccessibilityEvent: SOFTINPUT_SHOW"+event.toString()+event.getContentChangeTypes() );
                EventBus.getDefault().post(new JniEvent(JniEvent.SOFTINPUT_SHOW));
            }else{

                EventBus.getDefault().post(new JniEvent(JniEvent.SOFTINPUT_CAN_CLOSE));
            }
        }

    }

    @Override
    public void onInterrupt() {
        super.onInterrupt();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LoveApplication application = LoveApplication.getInstance();
        if (application!=null){
            application.initService(this);
        }

        EventBus.getDefault().register(this);
   //   FloatWindowManager.getInstance().applyOrShowFloatWindowResume(this);
//服务创建时创建前台通知
        Notification notification = createForegroundNotification();
        //启动前台服务
        startForeground(1,notification);
        Log.d(TAG, "onCreate");
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {

            //    Log.e(TAG,"event="+ event.toString());
        return super.onKeyEvent(event);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
        //在服务被销毁时，关闭前台服务
        stopForeground(true);
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        //更新屏幕大小
        Screen.update(newConfig.orientation);

        //更新光标位置
        CursorView cursorView = CursorView.getInstance();
        if (cursorView != null) cursorView.updatePosition();
    }

    interface ActionHandler {
        void exe(AccessibilityNodeInfo nodeInfo);
    }

    private void doActionUnderMouse(ActionHandler handler) {
        CursorView cursorView = CursorView.getInstance();

        if (handler == null || cursorView == null) return;

        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();

        if (nodeInfo == null) return;

      //  final LayoutParams cursorLayout = cursorView.layoutParams;
       final LayoutParams cursorLayout = new LayoutParams();
        cursorLayout.x=Screen.getWidth()/2;
        cursorLayout.y=Screen.getHeight()/2;
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
    int[] mLocation = new  int[2] ;
    private void click() {
        CursorView cursorView = CursorView.getInstance();

        if (Build.VERSION.SDK_INT >= 24 && cursorView != null) {
            final LayoutParams cursorLayout = cursorView.layoutParams;
            performGestureClick(
                    mLocation[0]+10,
                    mLocation[1]+10,
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
            //  final LayoutParams cursorLayout = cursorView.layoutParams;
            final LayoutParams cursorLayout = new LayoutParams();
            cursorLayout.x=Screen.getWidth()/2;
            cursorLayout.y=Screen.getHeight()/2;

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
            //  final LayoutParams cursorLayout = cursorView.layoutParams;
            final LayoutParams cursorLayout = new LayoutParams();
            cursorLayout.x=Screen.getWidth()/2;
            cursorLayout.y=Screen.getHeight()/2;

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
        Log.d(TAG, "performScrollRight: left");
        if (Build.VERSION.SDK_INT >= 24 && CursorView.getInstance() != null) {
            //  final LayoutParams cursorLayout = cursorView.layoutParams;
            final LayoutParams cursorLayout = new LayoutParams();
            cursorLayout.x=Screen.getWidth()/2;
            cursorLayout.y=Screen.getHeight()/2;
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
        Log.d(TAG, "performScrollRight: right");
        if (Build.VERSION.SDK_INT >= 24 && CursorView.getInstance() != null) {
            //  final LayoutParams cursorLayout = cursorView.layoutParams;\

            final LayoutParams cursorLayout = new LayoutParams();
            cursorLayout.x=Screen.getWidth()/2;
            cursorLayout.y=Screen.getHeight()/2;

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

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onStopService(StopServiceEvent event) {
//        stopSelf();
//    }

AccessibilityNodeInfo focus1;
String  voice_text="";
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMouseMove(MouseEvent event) {


        AccessibilityNodeInfo       focus_input = findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
      //  Log.d(TAG, "event:" + event.action+"focus_input="+(focus_input==null));
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
                if (SideBarContent.getInstance().tvLock!=null){

//                    if (AVCallFloatView.getInstance(LoveApplication.getInstance()).mCb_one.isChecked()==false){
//                        AVCallFloatView.getInstance(LoveApplication.getInstance()).mCb_one.setChecked(true);
//                        EventBus.getDefault().post(new JniEvent(JniEvent.ON_RESET_MOUSE));
//                        break;
//                    }
                  if(SideBarContent.getInstance().tvLongClick.isChecked()){
                      longClick();
                      SideBarContent.getInstance().tvLongClick.setChecked(false);
                      SideBarContent.getInstance().longClick(false);
                      break;
                  }
                    CursorView cursorView = CursorView.getInstance();
                    cursorView.getLocationOnScreen(mLocation);//获取在整个屏幕内的绝对坐标
                    //simulateClick(1000,600);
                    if (SideBarContent.getInstance().isMouseInView(mLocation[0]+10,mLocation[1]+10)){

                        click();
                    }else{

                        if (SideBarContent.getInstance().tvLock.isChecked()==false){
                            click();
                        }
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
            case MouseEvent.LONG_CLICK://长按
                longClick();
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
public void simulateClick(int x, int y) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
       // LogUtils.d(TAG, "simulateClick ", "X: " + x, "Y: " + y);
        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 100));
        dispatchGesture(builder.build(), null, null);
    }
}

/**
 * 响应开始按钮点击
 */
public static void onStartClicked(int showMenu) {

    Handler handler = new Handler(LoveApplication.getInstance().getMainLooper());
    handler.post(new Runnable() {
        @Override
        public void run() {
            if(showMenu<2){
                serviceRunning = FloatWindowManager.getInstance().applyOrShowFloatWindow(LoveApplication.getInstance().getMainActivity(), showMenu == 1 ? true : false);
            }
            if (showMenu>=2){
                FloatWindowManager.getInstance().setSideBarVisible(LoveApplication.getInstance().getMainActivity(), showMenu == 3 ? true : false);
            }

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
public static void sendLongClick() {
        EventBus.getDefault().post(new MouseEvent(MouseEvent.LONG_CLICK));
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
 * 供C++设置光标移动速度
 *
 * @param speed 移动速度（单位：像素/秒）必须大于0
 */
public static void setCursorMoveSpeed(int speed) {
//    UserSettings.CursorMoveSpeed = speed;
//
//    if (CursorView.getInstance() != null)
//        CursorView.getInstance().setMoveSpeed(UserSettings.CursorMoveSpeed);
}

/**
 * 供C++设置光标到指定位置
 *
 * @param x
 * @param y
 */
public static void setCursorPosition(int x, int y) {
    if (CursorView.getInstance() != null){
        if(SideBarContent.getInstance().tvLock.isChecked()){
            int[] location = new  int[2] ;
            SideBarContent.getInstance().tvLock.getLocationOnScreen(location);
            int  lockHeight=location[1]+SideBarContent.getInstance().tvLock.getHeight()/3;
            y=lockHeight;
        }
        CursorView.getInstance().setPosition(x, y);
    }

    //CursorView.getInstance().setVisibility(View.INVISIBLE);
   // CursorView.getInstance().setPermission();
 //EventBus.getDefault().post(new MouseEvent(0));
}

public static void setCursorDrop(int  isDrop) {
    if (CursorView.getInstance() != null)
        CursorView.getInstance().setCursorDrop(isDrop);
 //EventBus.getDefault().post(new MouseEvent(0));
}
public static void setCursorSize(int  size) {
    if (CursorView.getInstance() != null){
        Handler handler = new Handler( CursorView.getInstance().getContext().getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                CursorView.getInstance().setCursorSize(size);
            }
        });

    }

 //EventBus.getDefault().post(new MouseEvent(0));
}
public  static  void setLock(Boolean  isCheck){
    SideBarContent.getInstance().lockClick(isCheck);
}
public  static  void setLongClick(Boolean  isCheck){
    SideBarContent.getInstance().longClick(isCheck);
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
    Log.e(TAG, "set text"+reply+"--"+nodeInfo);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

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
