package net.leung.qtmouse;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.math.MathUtils;

import com.android.sidebar.views.SideBarContent;

import net.leung.qtmouse.tools.Screen;

public class CursorView extends BaseFloatView {



    private volatile static CursorView mCursorView= null;
    private final View mView;
    private final View mRlContent;
    public static   MouseAccessibilityService mService;

    public static CursorView getInstance(Context context) {
        if (mCursorView == null) {
            synchronized (CursorView.class) {
                mCursorView=new CursorView(context);
                mService= (MouseAccessibilityService) context;
            }
        }
        return mCursorView;
    }
    public  static CursorView getInstance(){
        return  mCursorView;
    }
    /**
     * 横向移动方向：-1为向左，1为向右，0不动
     */
    private int xDir = 0;

    /**
     * 垂直移动方向：-1为向上，1为向下，0不动
     */
    private int yDir = 0;

    /**
     * 每帧移动距离（单位：像素）
     */
    private int moveSpeed = 10;

    /**
     * 每帧逝去的时间（单位：毫秒）
     */
    private final int frameTime = 50;
     int  mIsDrop=-1;
    ImageView mIvCursor=null;
    Context  mContext=null;
    public CursorView(@NonNull Context context) {
        super(context);

        mView = View.inflate(context, R.layout.cursor, null);
        mIvCursor=  mView.findViewById(R.id.imageView);
        mRlContent = mView.findViewById(R.id.rl_cursor);
        // setPermission();
        addView(mView);

        layoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE//光标不会遮挡操作
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;//光标可以移动到屏幕边缘
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
       int width = metrics.widthPixels;
       int height = metrics.heightPixels;
        layoutParams.x = width/2;
        layoutParams.y = height/2;
        mContext=context;
    }
   public   void  setCursorDrop(int  cursorDrop){
        if(mIsDrop!=cursorDrop){
            mIsDrop=cursorDrop;
            if (mIsDrop==1){
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mIvCursor.setImageResource(R.mipmap.mouse_pointer_drop);
                        CursorView.this.setVisibility(View.GONE);
                        SideBarContent.getInstance().setIsShowing(false);
                    }
                });

            }
            if(mIsDrop==0){
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mIvCursor.setImageResource(R.mipmap.mouse_pointer);
                        CursorView.this.setVisibility(View.VISIBLE);
                        SideBarContent.getInstance().setIsShowing(true);
                    }
                });

            }
            if(mIsDrop==2){
                mIvCursor.setImageResource(R.mipmap.mouse_pointer_green);

            }
        }

   }
   public   void  setCursorSize(int  size){
      if (mIvCursor!=null){
          ViewGroup.LayoutParams layoutParams = mIvCursor.getLayoutParams();
          int dp2px = dp2px(getContext(), size);
          layoutParams.width =dp2px;
          layoutParams.height = dp2px;
          mIvCursor.setMaxHeight(dp2px);
          mIvCursor.setMaxWidth(dp2px);
          mIvCursor.setMinimumWidth(dp2px);
          mIvCursor.setMinimumHeight(dp2px);
          mIvCursor.setLayoutParams(layoutParams);
          System.out.println("mIvCursor size="+ layoutParams.height+"-"+layoutParams.width);
      }

   }
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    @Override
    public void setIsShowing(Context context,boolean isShowing) {
        super.setIsShowing(context,isShowing);
        if (isShowing == this.isShowing) return;
//        if (isShowing) {
//            EventBus.getDefault().register(this);
//
//        } else {
//            EventBus.getDefault().unregister(this);
//        }
    }

    /**
     * 设置光标移动速度
     *
     * @param speed 移动速度（单位：像素/秒）必须大于0
     */
    public void setMoveSpeed(int speed) {
        if (speed > 0)
            this.moveSpeed = speed * frameTime / 1000;
    }
    int m_pre_X=-1;
    int m_pre_y=-1;
    /**
     * 直接移动光标到指定位置
     *
     * @param x
     * @param y
     */
    public void setPosition(int x, int y) {
        Message msg = Message.obtain();
       msg.what=2;
       msg.arg1=x;
       msg.arg2=y;

        handler.sendMessage(msg);

    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onMouseMove(MouseEvent event) {
//        switch (event.action) {
//            case MouseEvent.MOVE_LEFT:
//                xDir = event.cancel ? 0 : -1;
//                break;
//            case MouseEvent.MOVE_RIGHT:
//                xDir = event.cancel ? 0 : 1;
//                break;
//            case MouseEvent.MOVE_UP:
//                yDir = event.cancel ? 0 : -1;
//                break;
//            case MouseEvent.MOVE_DOWN:
//                yDir = event.cancel ? 0 : 1;
//                break;
//            default:
//                break;
//        }
//    }

    public void updatePosition() {
        //final int screenWidth = Screen.getWidth() - getWidth() ;
       // final int screenHeight = Screen.getHeight() - getHeight() ;
       // layoutParams.x = MathUtils.clamp(layoutParams.x, 0, screenWidth);
        //layoutParams.y = MathUtils.clamp(layoutParams.y, 0, screenHeight);

        try {
               getWindowManager().updateViewLayout(this, layoutParams);
        } catch (Exception e) {

        }

    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    // 移除所有的msg.what为0等消息，保证只有一个循环消息队列再跑
//                    handler.removeMessages(0);
//
//                    // app的功能逻辑处理
//                    if (xDir != 0 || yDir != 0) {
//                        layoutParams.x += moveSpeed * xDir;
//                        layoutParams.y += moveSpeed * yDir;
//                        updatePosition();
//                    }

                    // 再次发出msg，循环更新
                 //   handler.sendEmptyMessageDelayed(0, frameTime);
                    break;

                case 1:
                    // 直接移除，定时器停止
                  //  handler.removeMessages(0);
                    break;
                    case 2:

                        layoutParams.x = msg.arg1;
                        layoutParams.y = msg.arg2;
                        updatePosition();
                 break;
                default:
                    break;
            }
        }
    };
    public void  updatePosition(int x ,int y){
          layoutParams.x=x;
          layoutParams.y=y;
          updatePosition();
    }

 
}
