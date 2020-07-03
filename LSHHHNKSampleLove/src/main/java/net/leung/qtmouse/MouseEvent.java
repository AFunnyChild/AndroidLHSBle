package net.leung.qtmouse;

//鼠标操作消息
public class MouseEvent {

    public static final int
            MOVE_UP = 0,//上移
            MOVE_DOWN = 1,//下移
            MOVE_LEFT = 2,//左移
            MOVE_RIGHT = 3,//右移
            CLICK = 4,//点击
            RETURN = 5,//返回
            SCROLL_UP = 6,//上滑
            SCROLL_DOWN = 7,//下滑
            SCROLL_LEFT = 8,//左滑
            SCROLL_RIGHT = 9,//右滑
            ZOOM_IN = 10,//放大
            ZOOM_OUT = 11,//缩小
            LONG_CLICK = 12,//长按
            HOME = 12,//HOME
            LOCATION = 14;//HOME

    public  int action=0;
    public  boolean cancel=false;
     public int x,y;

     public  String voice;

      public MouseEvent(int action, String voice) {
          this.action = action;
          this.voice = voice;
      }
    public MouseEvent(int action) {
        this.action = action;
        this.cancel = false;
    }

    public MouseEvent(int action, boolean cancel) {
        this.action = action;
        this.cancel = cancel;
    }
public MouseEvent(int action, int x,int y) {
    this.x = x;
    this.y = y;
}
}
