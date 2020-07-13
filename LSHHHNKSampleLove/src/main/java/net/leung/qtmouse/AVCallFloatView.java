package net.leung.qtmouse;


import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.VoiceWakeuperHelper;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.RequestListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.cloud.util.ResourceUtil;
import com.process.keepalive.daemon.MainActivity;


import net.leung.qtmouse.tools.Screen;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 悬浮球
 */
public class AVCallFloatView extends BaseFloatView implements View.OnTouchListener, View.OnClickListener {

    private volatile static AVCallFloatView mAVCallFloatView= null;
    public CheckBox mCb_one;
    public CheckBox mCb_twe;

    public static AVCallFloatView getInstance(Context context) {
        if (mAVCallFloatView == null) {
            synchronized (AVCallFloatView.class) {
                mAVCallFloatView=new AVCallFloatView(context);
            }
        }
        return mAVCallFloatView;
    }


        private static final String TAG = "AVCallFloatView";

    /**
     * 记录手指按下时在小悬浮窗的View上的横坐标的值
     */
    private float xInView;

    /**
     * 记录手指按下时在小悬浮窗的View上的纵坐标的值
     */
    private float yInView;
    /**
     * 记录当前手指位置在屏幕上的横坐标值
     */
    private float xInScreen;

    /**
     * 记录当前手指位置在屏幕上的纵坐标值
     */
    private float yInScreen;

    /**
     * 记录手指按下时在屏幕上的横坐标的值
     */
    private float xDownInScreen;

    /**
     * 记录手指按下时在屏幕上的纵坐标的值
     */
    private float yDownInScreen;

    private boolean needAnchorToSide = false;

    private boolean isAnchoring = false;
    private View menuButton;


    // 语音听写对象
    private SpeechRecognizer mIat;
    // 语音听写UI
    private RecognizerDialog mIatDialog;

    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    private String resultType = "json";
    private VoiceWakeuper mIvw;
    public Context mContext;
    private int curThresh = 1450;
    private String threshStr = "门限值：";
    private String keep_alive = "1";
    private String ivwNetMode = "0";
    private final VoiceWakeuperHelper mVoiceWakeuperHelper;

    public AVCallFloatView(Context context) {
        super(context);
        this.mContext=context;
        initView();

        mVoiceWakeuperHelper = new VoiceWakeuperHelper();
        mVoiceWakeuperHelper.initWake(mContext, new VoiceWakeuperHelper.IReceivedEvent() {
            @Override
            public void onEvent(int id) {
               // Toast.makeText(mContext, event+"", Toast.LENGTH_SHORT).show();

                if (id==0){
                    EventBus.getDefault().post(new MouseEvent(7));
                }
                if (id==1){
                    EventBus.getDefault().post(new MouseEvent(6));
                }
                if (id==2){
                    EventBus.getDefault().post(new MouseEvent(9));
                }
                if (id==3){
                    EventBus.getDefault().post(new MouseEvent(8));
                }
                if (id==4){
                    EventBus.getDefault().post(new MouseEvent(4));
                }

            if (id==5){
            //  MainActivity.ResetMouse();
                   EventBus.getDefault().post(new JniEvent(JniEvent.ON_RESET_MOUSE));
             }

            }
        });
    }

    // 查询资源请求回调监听
    private RequestListener requestListener = new RequestListener() {
        @Override
        public void onEvent(int eventType, Bundle params) {
            // 以下代码用于获取查询会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            //if(SpeechEvent.EVENT_SESSION_ID == eventType) {
            // 	Log.d(TAG, "sid:"+params.getString(SpeechEvent.KEY_EVENT_SESSION_ID));
            //}
        }

        @Override
        public void onCompleted(SpeechError error) {
            if(error != null) {
                Log.d(TAG, "error:"+error.getErrorCode());
                //  showTip(error.getPlainDescription(true));
            }
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            try {
                String resultInfo = new String(buffer, "utf-8");
                Log.d(TAG, "resultInfo:"+resultInfo);

                JSONTokener tokener = new JSONTokener(resultInfo);
                JSONObject object = new JSONObject(tokener);

                int ret = object.getInt("ret");
                if(ret == 0) {
                    String uri = object.getString("dlurl");
                    String md5 = object.getString("md5");
                    Log.d(TAG,"uri:"+uri);
                    Log.d(TAG,"md5:"+md5);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

   boolean  mCbOneCheck=true;
    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View floatView = inflater.inflate(R.layout.view_accessibility_view, null);

        addView(floatView);

        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(getContext(), mInitListener);

        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(LoveApplication.getInstance(), mInitListener);
        menuButton = findViewById(R.id.rl_accessibility);
        mCb_twe=  findViewById(R.id.cb_twe);

        menuButton.setOnTouchListener(this);
        findViewById(R.id.cb_four).setOnTouchListener(this);
        findViewById(R.id.cb_three).setOnClickListener(this);
        mCb_one = findViewById(R.id.cb_one);
        mCb_one.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                  mCbOneCheck=b;
            }
        });
        layoutParams.x = Screen.getWidth() - dp2px(getContext(), 100);
        layoutParams.y = Screen.getHeight() - dp2px(getContext(), 171);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true){

                        Thread.sleep(1000);//延时1s
                        EventBus.getDefault().post(new MouseEvent(-1));
                    }
                    //do something
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private String getResource() {
        final String resPath = ResourceUtil.generateResourcePath(mContext, ResourceUtil.RESOURCE_TYPE.assets, "ivw/"+mContext.getString(R.string.app_id)+".jet");
        Log.d( TAG, "resPath: "+resPath );
        return resPath;
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (isAnchoring) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xInView = event.getX() + v.getX();
                yInView = event.getY() + v.getY();
                xDownInScreen = event.getRawX();
                yDownInScreen = event.getRawY();
                xInScreen = event.getRawX();
                yInScreen = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                xInScreen = event.getRawX();
                yInScreen = event.getRawY();
                // 手指移动的时候更新小悬浮窗的位置
                updateViewPosition();
                break;
            case MotionEvent.ACTION_UP:
                if (Math.abs(xDownInScreen - xInScreen) <= ViewConfiguration.get(getContext()).getScaledTouchSlop()
                        && Math.abs(yDownInScreen - yInScreen) <= ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
                    // 点击效果
                    v.performClick();
                } else if (isNeedAnchorToSide()) {
                    //吸附效果
                    anchorToSide();
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void anchorToSide() {
        isAnchoring = true;
        final int screenWidth = Screen.getWidth();
        final int screenHeight = Screen.getHeight();
        int middleX = layoutParams.x + getWidth() / 2;

        int animTime = 0;
        int xDistance = 0;
        int yDistance = 0;

        int dp_25 = dp2px(5);

        //left
        if (middleX <= screenWidth / 2) {
            xDistance = dp_25 - layoutParams.x;
        }
        //right
        else {
            xDistance = screenWidth - layoutParams.x - getWidth() - dp_25;
        }

        //1
        if (layoutParams.y < dp_25) {
            yDistance = dp_25 - layoutParams.y;
        }
        //2
        else if (layoutParams.y + getHeight() + dp_25 >= screenHeight) {
            yDistance = screenHeight - dp_25 - layoutParams.y - getHeight();
        }
        Log.e(TAG, "xDistance  " + xDistance + "   yDistance" + yDistance);

        animTime = Math.abs(xDistance) > Math.abs(yDistance) ? (int) (((float) xDistance / (float) screenWidth) * 600f)
                : (int) (((float) yDistance / (float) screenHeight) * 900f);
        this.post(new AnchorAnimRunnable(Math.abs(animTime), xDistance, yDistance, System.currentTimeMillis()));
    }

    public int dp2px(float dp) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * 悬浮球是否需要靠边停靠
     */
    public boolean isNeedAnchorToSide() {
        return needAnchorToSide;
    }

    public void setNeedAnchorToSide(boolean needAnchorToSide) {
        this.needAnchorToSide = needAnchorToSide;
    }

    @Override
    public void onClick(View view) {
        if (R.id.cb_three==view.getId()){
            mVoiceWakeuperHelper.stopListening();
            mIatResults.clear();
            // 设置参数
            setParam();
            // 显示听写对话框
            mIatDialog.setListener(mRecognizerDialogListener);
            mIatDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

            mIatDialog.show();
            List<View> allChildViews = getAllChildViews(mIatDialog.getWindow().getDecorView());
            for (View allChildView : allChildViews) {
                if (allChildView instanceof TextView){
                    if (((TextView) allChildView).getText().toString().contains("识别")){
                        ((TextView) allChildView).setText("");
                        allChildView.setClickable(false);
                    }
                }
            }

        }
    }

    private class AnchorAnimRunnable implements Runnable {

        private int animTime;
        private long currentStartTime;
        private Interpolator interpolator;
        private int xDistance;
        private int yDistance;
        private int startX;
        private int startY;

        public AnchorAnimRunnable(int animTime, int xDistance, int yDistance, long currentStartTime) {
            this.animTime = animTime;
            this.currentStartTime = currentStartTime;
            interpolator = new AccelerateDecelerateInterpolator();
            this.xDistance = xDistance;
            this.yDistance = yDistance;
            startX = layoutParams.x;
            startY = layoutParams.y;
        }

        @Override
        public void run() {
            if (System.currentTimeMillis() >= currentStartTime + animTime) {
                if (layoutParams.x != (startX + xDistance) || layoutParams.y != (startY + yDistance)) {
                    layoutParams.x = startX + xDistance;
                    layoutParams.y = startY + yDistance;
                    getWindowManager().updateViewLayout(AVCallFloatView.this, layoutParams);
                }
                isAnchoring = false;
                return;
            }
            float delta = interpolator.getInterpolation((System.currentTimeMillis() - currentStartTime) / (float) animTime);
            int xMoveDistance = (int) (xDistance * delta);
            int yMoveDistance = (int) (yDistance * delta);
            Log.e(TAG, "delta:  " + delta + "  xMoveDistance  " + xMoveDistance + "   yMoveDistance  " + yMoveDistance);
            layoutParams.x = startX + xMoveDistance;
            layoutParams.y = startY + yMoveDistance;

            if (!getIsShowing()) {
                return;
            }

            getWindowManager().updateViewLayout(AVCallFloatView.this, layoutParams);
            AVCallFloatView.this.postDelayed(this, 16);
        }
    }

    private void updateViewPosition() {
        //增加移动误差\

        layoutParams.x = (int) (xInScreen - xInView);
        layoutParams.y = (int) (yInScreen - yInView);
        Log.e(TAG, "x  " + layoutParams.x + "   y  " + layoutParams.y);
        getWindowManager().updateViewLayout(this, layoutParams);
    }
    public void updateViewPosition(int x,int y) {
        //增加移动误差\
        if ((mCb_one!=null)&&(mCb_one.isChecked()==false)){
            xInScreen=x;
            yInScreen=y;
            xInView=xInScreen+mCb_one.getX();
            yInScreen=yInScreen+mCb_one.getY();
            layoutParams.x = (int) (xInScreen - xInView);
            layoutParams.y = (int) (yInScreen - yInView);
            Log.e(TAG, "x  " + layoutParams.x + "   y  " + layoutParams.y);
            getWindowManager().updateViewLayout(this, layoutParams);
        }

    }
    /**
     * 参数设置
     *
     * @return
     */
    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, resultType);

        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");



        //此处用于设置dialog中不显示错误码信息
        //mIat.setParameter("view_tips_plain","false");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, 4000+"");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS,1000+"");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, 1+"");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
//		mIat.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
//		mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");
    }
    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Toast.makeText(getContext(), "语音初始化失败", Toast.LENGTH_SHORT).show();
            }
        }
    };
    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.d(TAG, "printResult: "+isLast);
            printResult(results,isLast);

        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            Toast.makeText(getContext(), error.getPlainDescription(true), Toast.LENGTH_SHORT).show();

        }

    };
    private void printResult(RecognizerResult results,boolean islast) {
        String text = parseIatResult(results.getResultString());

        String sn = null;
        //  mIatResults.clear();
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
            Log.d(TAG, "printResult: "+key+"-"+mIatResults.get(key));
        }
        if (islast){
            new Handler().postDelayed(new Runnable(){
                public void run() {
                    EventBus.getDefault().post(new MouseEvent(MouseEvent.VOICE, resultBuffer.toString()));
                    mVoiceWakeuperHelper.startListening();
                }
            }, 1000);
        }
    }
    public static String parseIatResult(String json) {
        StringBuffer ret = new StringBuffer();
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);

            JSONArray words = joResult.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++) {
                // 转写结果词，默认使用第一个结果
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                JSONObject obj = items.getJSONObject(0);
                ret.append(obj.getString("w"));
//				如果需要多候选结果，解析数组其他字段
//				for(int j = 0; j < items.length(); j++)
//				{
//					JSONObject obj = items.getJSONObject(j);
//					ret.append(obj.getString("w"));
//				}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret.toString();
    }
    private List<View> getAllChildViews(View view) {
        List<View> allchildren = new ArrayList<View>();
        if (view instanceof ViewGroup) {
            ViewGroup vp = (ViewGroup) view;
            for (int i = 0; i < vp.getChildCount(); i++) {
                View viewchild = vp.getChildAt(i);
                allchildren.add(viewchild);
                //再次 调用本身（递归）
                allchildren.addAll(getAllChildViews(viewchild));
            }
        }
        return allchildren;
    }
}
