package net.leung.qtmouse;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.os.Message;
import android.widget.ImageView;

import net.leung.qtmouse.tools.Screen;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.NonNull;
import androidx.core.math.MathUtils;

import static android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;

public class CursorView extends BaseFloatView {



    private volatile static CursorView mCursorView= null;
    public static CursorView getInstance(Context context) {
        if (mCursorView == null) {
            synchronized (CursorView.class) {
                mCursorView=new CursorView(context);

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
     boolean  mIsDrop=true;
    ImageView mIvCursor=null;
    public CursorView(@NonNull Context context) {
        super(context);

        View view = View.inflate(context, R.layout.cursor, null);
        mIvCursor=  view.findViewById(R.id.imageView);
        addView(view);

        layoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE//光标不会遮挡操作
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;//光标可以移动到屏幕边缘
        layoutParams.x = 250;
        layoutParams.y = 250;
        mIsDrop=true;
    }
   public   void  setCursorDrop(boolean  isDrop){
        if(mIsDrop!=isDrop){
            mIsDrop=isDrop;
            if (mIsDrop){
                mIvCursor.setImageResource(R.mipmap.mouse_pointer_drop);
            }else{
                mIvCursor.setImageResource(R.mipmap.mouse_pointer);
            }
        }

   }
   public   void  setCursorSize(int  size){
      if (mIvCursor!=null){
          ViewGroup.LayoutParams layoutParams = mIvCursor.getLayoutParams();
          layoutParams.width = dp2px(getContext(),size);
          layoutParams.height = dp2px(getContext(),size);
          mIvCursor.setLayoutParams(layoutParams);
          System.out.println("mIvCursor size="+size);
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
    public void setIsShowing(boolean isShowing) {
        super.setIsShowing(isShowing);
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
        final int screenWidth = Screen.getWidth() - getWidth() ;
        final int screenHeight = Screen.getHeight() - getHeight() ;
        layoutParams.x = MathUtils.clamp(layoutParams.x, 0, screenWidth);
        layoutParams.y = MathUtils.clamp(layoutParams.y, 0, screenHeight);

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
                 // 直接移除，定时器停止
                     layoutParams.x = msg.arg1;
                     layoutParams.y = msg.arg2;
//                        AVCallFloatView.getInstance(mCursorView.getContext()).updateViewPosition(msg.arg1,msg.arg2);
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
