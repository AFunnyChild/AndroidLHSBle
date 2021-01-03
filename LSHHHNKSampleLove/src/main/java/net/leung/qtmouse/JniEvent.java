package net.leung.qtmouse;

public class JniEvent {
     public static final int ON_VOICE_PASTE=0;
     public static final int ON_RESET_MOUSE=1;
     public static final int ON_WINDOW_CHANGE=2;
    public int eventType;
    public JniEvent(int eventType) {
        this.eventType = eventType;

    }
}
