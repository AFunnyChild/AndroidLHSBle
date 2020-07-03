package net.leung.qtmouse;

import android.content.Context;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

class BaseFloatView extends FrameLayout {

    protected WindowManager.LayoutParams layoutParams;
    private WindowManager windowManager = null;
    private boolean isShowing = false;

    public BaseFloatView(@NonNull Context context) {
        super(context);

        layoutParams = FloatWindowManager.createLayoutParams(context);
    }

    public void setIsShowing(boolean isShowing) {
        if (isShowing == this.isShowing) return;

        if (isShowing) {
            getWindowManager().addView(this, layoutParams);
        } else {
            getWindowManager().removeViewImmediate(this);
        }

        this.isShowing = isShowing;
    }

    public boolean getIsShowing() {
        return this.isShowing;
    }

    /**
     * 设置是否忽略手势触摸
     *
     * @param ignore
     */
    public void ignoreTouch(boolean ignore) {
        if (ignore) layoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        else layoutParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

        getWindowManager().updateViewLayout(this, layoutParams);
    }

    protected WindowManager getWindowManager() {
        if (windowManager == null) {
            windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        }

        return windowManager;
    }

    protected static int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
