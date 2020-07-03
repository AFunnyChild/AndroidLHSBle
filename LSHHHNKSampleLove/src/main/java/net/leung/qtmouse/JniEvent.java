package net.leung.qtmouse;

public class JniEvent {
     public static final int ON_VOICE_PASTE=0;
     public static final int ON_RESET_MOUSE=1;
    int eventType;
    public JniEvent(int eventType) {
        this.eventType = eventType;
    }


}
