package net.leung.qtmouse.tools;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.view.WindowManager;

import androidx.core.math.MathUtils;

public final class Screen {

    private static int width;
    private static int height;
    private static WindowManager windowManager;
     static Context   mContext;
    public static void init(Context context) {
        mContext=context;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        initSize();
    }

    public static void update(int orientation) {
        initSize();

        if ((orientation == Configuration.ORIENTATION_LANDSCAPE && width < height) //横屏
                || (orientation == Configuration.ORIENTATION_PORTRAIT && width > height)) {//竖屏
            int tmp = width;
            width = height;
            height = tmp;
        }
    }

    /**
     * 获取屏幕宽度
     *
     * @return
     */
    public static int getWidth() {
        return width;
    }

    /**
     * 获取屏幕高度
     *
     * @return
     */
    public static int getHeight() {
        return height;
    }

    private static void initSize() {

        Point size = new Point();
        windowManager.getDefaultDisplay().getSize(size);
        width      = size.x;
        height     = size.y;
    }

    /**
     * 获取屏幕内的横向点位置
     *
     * @param x
     * @return
     */
    public static int x(int x) {
        return MathUtils.clamp(x, 0, getWidth());
    }

    /**
     * 获取屏幕内的纵向点位置
     *
     * @param y
     * @return
     */
    public static int y(int y) {
        return MathUtils.clamp(y, 0, getHeight());
    }
}
