package com.android.sidebar.views;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;


import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatCheckedTextView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.percentlayout.widget.PercentRelativeLayout;

import com.android.sidebar.R;
import com.android.sidebar.utils.PermissionUtil;
import com.android.sidebar.utils.SystemVolume;

/**
 * Sidebar left & right
 *
 * @author majh
 */
public class SideBarContent implements View.OnClickListener {
    private volatile static SideBarContent mSideBarContent= null;
    public AppCompatCheckedTextView tvLock;
    public AppCompatCheckedTextView tvLongClick;
    private AppCompatTextView mTvVolume;
    public View mLLHideBar;
    public View mLLRoot;

    public static SideBarContent getInstance() {
        if (mSideBarContent == null) {
            synchronized (SideBarContent.class) {
                mSideBarContent=new SideBarContent();
            }
        }
        return mSideBarContent;
    }



    private Context mContext;
    private boolean mLeft;
    private RelativeLayout mContentView;
    private WindowManager mWindowManager;
    private AccessibilityService mSideBarService;
    private ControlBar mControlBar;
    private LinearLayout mSeekBarView;

    private int mTagTemp = -1;
    public static WindowManager.LayoutParams mParams;


    public RelativeLayout getView(Context context,
                         boolean left,
                         WindowManager windowManager,
                         WindowManager.LayoutParams params,

                               AccessibilityService sideBarService
                        ) {
        mContext = context;
        mLeft = left;
        mWindowManager = windowManager;

        mSideBarService = sideBarService;

        // get layout
        LayoutInflater inflater = LayoutInflater.from(context);
        mContentView = (RelativeLayout) inflater.inflate(R.layout.layout_content, null);
        // init click
        mContentView.findViewById(R.id.tv_left).  setOnClickListener(this);
        mContentView.findViewById(R.id.tv_back).  setOnClickListener(this);
        mContentView.findViewById(R.id.tv_home).  setOnClickListener(this);
        mContentView.findViewById(R.id.tv_upward).setOnClickListener(this);
        mContentView.findViewById(R.id.tv_down).  setOnClickListener(this);
        mTvVolume = mContentView.findViewById(R.id.tv_volume);
        mTvVolume.setOnClickListener(this);
        mContentView.findViewById(R.id.tv_right).setOnClickListener(this);
        tvLock = mContentView.findViewById(R.id.tv_lock);
        tvLock.setOnClickListener(this);
        tvLongClick = mContentView.findViewById(R.id.tv_long_click);
        tvLongClick.setOnClickListener(this);
        mLLRoot = mContentView.findViewById(R.id.root);
        mLLHideBar = mContentView.findViewById(R.id.ll_hide_bar);
        if(left) {
            mLLRoot.setPadding(15,0,0,0);
        }else {
            mLLRoot.setPadding(0,0,15,0);
        }
        mContentView.setVisibility(View.GONE);
        mWindowManager.addView(mContentView,params);


        return mContentView;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (iSideEventListener != null) {
            iSideEventListener.onIsClicked();
        }
        if (id == R.id.tv_left) {
            if (iSideEventListener != null) {
                iSideEventListener.onEvent(3);
            }
        } else if (id == R.id.tv_back) {
            mSideBarService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
        } else if (id == R.id.tv_home) {
            mSideBarService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
        } else if (id == R.id.tv_upward) {
            if (iSideEventListener != null) {
                iSideEventListener.onEvent(0);
            }
        } else if (id == R.id.tv_down) {
            if (iSideEventListener != null) {
                iSideEventListener.onEvent(1);
            }
        } else if (id == R.id.tv_volume) {
           // brightnessOrVolume(1);
            adjustVolume();
        } else if (id == R.id.tv_right) {
            if (iSideEventListener != null) {
                iSideEventListener.onEvent(2);
            }
        } else if (id == R.id.tv_lock) {
             tvLock.setChecked(!tvLock.isChecked());
             lockClick(tvLock.isChecked());

        }else if (id == R.id.tv_long_click) {
            tvLongClick.setChecked(!tvLongClick.isChecked());
            longClick(tvLongClick.isChecked());
        }

    }
     Thread  mVolumeThread;
     boolean mVolumeThreadRunning=false;
    volatile int  mVolumeThreadWaitTime=0;
    private void adjustVolume() {
         if (mVolumeThreadRunning==false){
              if (mVolumeThread!=null){
                  mVolumeThread.interrupt();
                  mVolumeThread=null;
              }
             mVolumeThreadRunning=true;
             mVolumeThread=new Thread(){
                 @Override
                 public void run() {
                     super.run();
                     while (mVolumeThreadRunning){

                        SystemClock.sleep(1000);
                         mVolumeThreadWaitTime+=1000;
                         //Log.d("mVolumeThreadRunning", "run: "+ mVolumeThreadRunning +"--"+mVolumeThreadWaitTime);
                         if (mVolumeThreadWaitTime>=15000){
                             mVolumeThreadRunning=false;
                             handler.sendEmptyMessage(0);
                         }
                     }
                 }
             };
             mVolumeThread.start();
         }
        mVolumeThreadWaitTime=0;
        int volume = SystemVolume.get3Volume(mContext);

        if (mTvVolume.getText().toString().trim().equals("音量")){
            double volume_text=((volume)/15d)*100d;
            mTvVolume.setText((int)volume_text+"%");
        }else{
            if (volume!=15){
                SystemVolume.setVolume(mContext,volume+3);
                double volume_text=((volume+3)/15d)*100d;
                mTvVolume.setText((int)volume_text+"%");
              //  Log.d("mVolumeThreadRunning", "run: volume"+volume+"--"+volume_text);
            }else{
                SystemVolume.setVolume(mContext,0);
                mTvVolume.setText(0+"%");
            }
        }
    }
    int[] location = new  int[2] ;
    public boolean isMouseInView(int x ,int y){
        tvLock.getLocationOnScreen(location);
        if (x>location[0]&&x-location[0]<tvLock.getWidth()){
            if (y>location[1]&&y-location[1]<tvLock.getHeight()){
                return true;
            }
        }
        return false;
    }
    int[] location1 = new  int[2] ;
    public boolean isMouseInSide(int x ,int y){
        mContentView.getLocationOnScreen(location1);
        if (x>location1[0]&&x-location1[0]<mContentView.getWidth()){
            if (y>location1[1]&&y-location1[1]<mContentView.getHeight()){
                return true;
            }
        }
        return false;
    }
    int[] location2 = new  int[2] ;
    public boolean isMouseInHideSide(int x ,int y){
        mLLHideBar.getLocationOnScreen(location2);
        //Log.e("test", "isMouseInHideSide: "+);
        if (x>location2[0]&&x-location2[0]<mLLHideBar.getWidth()){
            if (y>location2[1]&&y-location2[1]<mLLHideBar.getHeight()){
                return true;
            }
        }
        return false;
    }
    private void brightnessOrVolume(int tag) {
        if(mTagTemp == tag) {
            if(null != mSeekBarView) {
                removeSeekBarView();
            }else {
                addSeekBarView(tag);
            }
            return;
        }
        mTagTemp = tag;
        if(null == mControlBar) {
            mControlBar = new ControlBar();
        }
        if(null == mSeekBarView) {
            addSeekBarView(tag);
        }else {
            removeSeekBarView();
            addSeekBarView(tag);
        }
    }
    private void addSeekBarView(int tag) {
        mSeekBarView = mControlBar.getView(mContext,mLeft,tag,this);
        mWindowManager.addView(mSeekBarView, mControlBar.mParams);
    }

    private void removeSeekBarView() {
        if(null != mSeekBarView) {
            mWindowManager.removeView(mSeekBarView);
            mSeekBarView = null;
        }
    }
    private void brightnessPermissionCheck() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!PermissionUtil.isSettingsCanWrite(mContext)) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + mContext.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                Toast.makeText(mContext,mContext.getString(R.string.setting_modify_toast),Toast.LENGTH_LONG).show();
            }else {
                brightnessOrVolume(0);
            }
        }else {
            brightnessOrVolume(0);
        }
    }

     RelativeLayout mContentBarView=null;
    @SuppressLint({"RtlHardcoded", "InflateParams"})
    public  void createToucher(  AccessibilityService sideBarService) {
        // get window manager
        WindowManager windowManager = (WindowManager)sideBarService.getSystemService(Context.WINDOW_SERVICE);
        // right arrow


        mParams = new WindowManager.LayoutParams();
        // compatible

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY ;
        } else {
            mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT ;
        }
        // set bg transparent
        mParams.format = PixelFormat.RGBA_8888;
        // can not focusable
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mParams.x = 0;
        mParams.y = 0;
        // window size
        final float scale = sideBarService.getResources().getDisplayMetrics().density;
        int sideWidth= (int) (650 * scale + 0.5f);
        DisplayMetrics displayMetrics = sideBarService.getResources().getDisplayMetrics();
        sideWidth = displayMetrics.widthPixels;
        mParams.width = sideWidth;
        mParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;


        mParams.gravity = Gravity.BOTTOM | Gravity.CENTER_VERTICAL;
        mParams.windowAnimations = R.style.RightSeekBarAnim;



        if(null == mSideBarContent || null == mContentBarView) {
            mSideBarContent =  SideBarContent.getInstance();
            mContentBarView = mSideBarContent.getView(sideBarService,false,windowManager, mParams,sideBarService);
        }

    }

    public void setIsShowing( boolean isShowing) {
        if (mContentView==null){
            return;
        }
        if (isShowing) {
            mContentView.setVisibility(View.VISIBLE);
        } else {
            mContentView.setVisibility(View.GONE);
        }


    }


    ISideEventListener  iSideEventListener;

    public void setiSideEventListener(ISideEventListener iSideEventListener) {
        this.iSideEventListener = iSideEventListener;
    }

    public  interface ISideEventListener{
      void onEvent(int eventIndex);
      void onIsClicked();

}
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
        mTvVolume.setText("音量");
        }
    };
   public void lockClick(Boolean isCheck){
        Drawable drawTop= null;
        if (isCheck){
            mContentView.findViewById(R.id.tv_left).setVisibility(View.INVISIBLE);
            mContentView.findViewById(R.id.tv_back).setVisibility(View.INVISIBLE);
            mContentView.findViewById(R.id.tv_home).setVisibility(View.INVISIBLE);
            mContentView.findViewById(R.id.tv_upward).setVisibility(View.INVISIBLE);
            mContentView.findViewById(R.id.tv_down).setVisibility(View.INVISIBLE);
            mContentView.findViewById(R.id.tv_volume).setVisibility(View.INVISIBLE);
            mContentView.findViewById(R.id.tv_right).setVisibility(View.INVISIBLE);
            mContentView.findViewById(R.id.tv_long_click).setVisibility(View.INVISIBLE);

            tvLock.setTextColor(ContextCompat.getColor(mContext,R.color.color_lock_red));
            drawTop=  mContext.getResources().getDrawable(R.drawable.ic_lock_open_red);
        }else{

            mContentView.findViewById(R.id.tv_left).setVisibility(View.VISIBLE);
            mContentView.findViewById(R.id.tv_back).setVisibility(View.VISIBLE);
            mContentView.findViewById(R.id.tv_home).setVisibility(View.VISIBLE);
            mContentView.findViewById(R.id.tv_upward).setVisibility(View.VISIBLE);
            mContentView.findViewById(R.id.tv_down).setVisibility(View.VISIBLE);
            mContentView.findViewById(R.id.tv_volume).setVisibility(View.VISIBLE);
            mContentView.findViewById(R.id.tv_right).setVisibility(View.VISIBLE);
            mContentView.findViewById(R.id.tv_long_click).setVisibility(View.VISIBLE);
            tvLock.setTextColor(ContextCompat.getColor(mContext,R.color.color_main));
            drawTop=  mContext.getResources().getDrawable(R.drawable.ic_lock_open_);
        }
        drawTop.setBounds(0, 0, drawTop.getMinimumWidth(),drawTop.getMinimumHeight());
        tvLock.setCompoundDrawables(null,drawTop,null,null);
    }
    public void  longClick(Boolean  isCheck){
        Drawable drawTop= null;
        if (isCheck){


            tvLongClick.setTextColor(ContextCompat.getColor(mContext,R.color.color_lock_red));
            drawTop=  mContext.getResources().getDrawable(R.drawable.ic_lock_long_click_red);
        }else{


            tvLongClick.setTextColor(ContextCompat.getColor(mContext,R.color.color_main));
            drawTop=  mContext.getResources().getDrawable(R.drawable.ic_long_click);
        }
        drawTop.setBounds(0, 0, drawTop.getMinimumWidth(),drawTop.getMinimumHeight());
        tvLongClick.setCompoundDrawables(null,drawTop,null,null);

    }
}
