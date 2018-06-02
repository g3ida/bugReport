package com.g3ida.bugreport;

/**
 * Created by g3ida on 8/15/2017.
 */

public class Event {
    public enum Type {BUTTON_LEFT, BUTTON_RIGHT, BUTTON_JUMP, MENU_BUTTON_SUBMITTED, MENU_BUTTON_SHAKE,
        TOUCH, TITLE_SHAKED,

        PLAY_BUTTON_TRIGGERED, BACK_BUTTON_TRIGGERED, CREDITS_BUTTON_TRIGGERED, ACHIEVEMENTS_BUTTON_TRIGGERED,
        MENU_LEFT_BUTTON_TRIGGERED, MENU_RIGHT_BUTTON_TRIGGERED,
        LEVEL_TRIGGERED, SOUND_BUTTON_TRIGGERED, RESTART,

        DIAMOND_CAPTURED, DIAMOND_WON, SMILING_PLATFORM_TRGGERED,

        WATCH_AD,

        LEVEL_CLEAR, LEVEL_LOST, PAUSE_TRIGGERED,

        CONFIRM, REJECT, POPUP, PLAYER_PURSHASE, OK,

        PLAYER_CHANGE_LEFT, PLAYER_CHANGE_RIGHT, DRAG_GESTURE,

        PLAY_STATE, MENU_STATE, NONE, BACK,
        PLAYER_JUMPED, PLAYER_MOVING, PLAYER_DEAD, PLAYER_DYING, PLAYER_WIN,
        ACHIVEMENT_UNLOCKED, PLAYER_UNLOCKED, ONE_HOUR_ELLAPSED, ACHIEVEMENT_SHOWN
    }

    Event() {

    }

    Event(Event.Type e) {
        this.type = e;
    }

    public Type type;

    public class Info{
        public ButtonState buttonState = new ButtonState();
        public TouchState touchState = new TouchState();
        public int level = 0;
        public int player = 0;
        public int achievementNum = 0;

        public class ButtonState{public boolean pressed = false;}
        public class TouchState{
            public boolean isDown;
            public int screenX;
            public int screenY;
            public int pointer;
            public int button;
        }
    }

    public Info info = new  Info();
}
