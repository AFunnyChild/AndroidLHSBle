package com.android.sidebar.views;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;


import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.sidebar.R;
import com.android.sidebar.utils.PermissionUtil;

/**
 * Sidebar left & right
 *
 * @author majh
 */
public class SideBarContent implements View.OnClickListener {
    private volatile static SideBarContent mSideBarContent= null;
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
    private LinearLayout mContentView;
    private WindowManager mWindowManager;
    private AccessibilityService mSideBarService;
    private ControlBar mControlBar;
    private LinearLayout mSeekBarView;

    private int mTagTemp = -1;
    public static WindowManager.LayoutParams mParams;


    public LinearLayout getView(Context context,
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
        mContentView = (LinearLayout) inflater.inflate(R.layout.layout_content, null);
        // init click
        mContentView.findViewById(R.id.tv_brightness).setOnClickListener(this);
        mContentView.findViewById(R.id.tv_back).setOnClickListener(this);
        mContentView.findViewById(R.id.tv_home).setOnClickListener(this);
        mContentView.findViewById(R.id.tv_upward).setOnClickListener(this);
        mContentView.findViewById(R.id.tv_down).setOnClickListener(this);
        mContentView.findViewById(R.id.tv_volume).setOnClickListener(this);
        mContentView.findViewById(R.id.tv_backstage).setOnClickListener(this);
        LinearLayout root = mContentView.findViewById(R.id.root);
        if(left) {
            root.setPadding(15,0,0,0);
        }else {
            root.setPadding(0,0,15,0);
        }
        mWindowManager.addView(mContentView,params);
        return mContentView;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_brightness) {
            brightnessPermissionCheck();
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
            brightnessOrVolume(1);
        } else if (id == R.id.tv_backstage) {
            mSideBarService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
        }
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

     LinearLayout mContentBarView=null;
    @SuppressLint({"RtlHardcoded", "InflateParams"})
    public  void createToucher(  AccessibilityService sideBarService) {
        // get window manager
        WindowManager windowManager = (WindowManager)sideBarService.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        // right arrow


        mParams = new WindowManager.LayoutParams();
        // compatible

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
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
        mParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        mParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;


        mParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
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

}


}
