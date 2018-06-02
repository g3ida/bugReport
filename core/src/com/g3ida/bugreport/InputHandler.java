package com.g3ida.bugreport;

import java.util.Observable;
import java.util.Observer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;


/**
 * Created by g3ida on 8/15/2017.
 */

public class InputHandler extends Observable implements InputProcessor, Observer {

    private boolean _leftDown = false;
    private boolean _rightDown = false;
    private boolean _jumpDown = false;

    //to avoid menu click when dragged.
    private boolean _recentlyDragged = true;

    //for the singleton pattern
    private static InputHandler _instance;

    //this is set for the touch drag event to calculate informations about the drag direction.
    private Vector2 _lastTouch = new Vector2();


    public boolean recentlyDragged() {return _recentlyDragged;}

    public static final InputHandler get() {
        if (_instance == null) {
            _instance = new InputHandler();
        }
        return _instance;
    }

    public void launch() {
        EventHandler.get().addObserver(this);
        Gdx.input.setInputProcessor(this);
        Gdx.input.setCatchBackKey(true);
    }

    public void reset(){_leftDown=false; _rightDown=false;}

    public void kill() {
        EventHandler.get().deleteObserver(this);
        Gdx.input.setInputProcessor(null);}

    private InputHandler()
    {
        //Gdx.input.setInputProcessor(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        if(arg == null) return;
        Event e = (Event) arg;
        if(e.type == Event.Type.BUTTON_LEFT) {
            _leftDown = (e.info.buttonState.pressed == true);
        } else if(e.type == Event.Type.BUTTON_RIGHT) {
            _rightDown = (e.info.buttonState.pressed == true);
        } else if(e.type == Event.Type.BUTTON_JUMP) {
            _jumpDown = (e.info.buttonState.pressed == true);
        }
    }

    @Override
    public boolean keyUp(int keycode) {_recentlyDragged=false; return false;}


    @Override
    public boolean keyTyped(char character) {_recentlyDragged=false; return false;}


    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        _recentlyDragged=false;
        //for the drag event.
        _lastTouch.set(screenX, screenY);

        //creating a new touch event and sending it to the EventHandler.
        Event e = new Event();
        e.type = Event.Type.TOUCH;
        e.info.touchState.button = button;
        e.info.touchState.isDown = true;
        e.info.touchState.pointer = pointer;
        e.info.touchState.screenX = screenX;
        e.info.touchState.screenY = screenY;
        EventHandler.get().notifyObservers(e);
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {

        Event e = new Event();
        e.type = Event.Type.TOUCH;
        e.info.touchState.button = button;
        e.info.touchState.isDown = false;
        e.info.touchState.pointer = pointer;
        e.info.touchState.screenX = screenX;
        e.info.touchState.screenY = screenY;
        EventHandler.get().notifyObservers(e);
        return true;
    }

    @Override
    public boolean keyDown(int keycode) {
        _recentlyDragged=false;
        if(keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE){
            Event e = new Event();
            e.type = Event.Type.BACK;
            EventHandler.get().notifyObservers(e);
            if(!Settings.isSilent)
                Settings.cancelSFX.play();
        }
        return true;
    }

    public boolean leftDown(){return _leftDown;}
    public boolean rightDown(){return _rightDown;}
    public boolean jumpDown() {return _jumpDown;}

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Vector2 newTouch = new Vector2(screenX, screenY);
        // delta will now hold the difference between the last and the current touch positions
        // delta.x > 0 means the touch moved to the right, delta.x < 0 means a move to the left
        Vector2 delta = newTouch.cpy().sub(_lastTouch);

        if(delta.x > 0) {
            //touch moved to the right.
            if (delta.x > 100.f) {
                //a good enough drag.
                if(!recentlyDragged()) {
                    EventHandler.get().notifyObservers(new Event(Event.Type.DRAG_GESTURE));
                    EventHandler.get().notifyObservers(new Event(Event.Type.MENU_LEFT_BUTTON_TRIGGERED));
                }
                _recentlyDragged = true;
                return true;

            }
        } else {
            //touch moved to the left.
            if (delta.x < -100.f) {
                //a good enough drag.
                if(!recentlyDragged()) {
                    EventHandler.get().notifyObservers(new Event(Event.Type.DRAG_GESTURE));
                    EventHandler.get().notifyObservers(new Event(Event.Type.MENU_RIGHT_BUTTON_TRIGGERED));
                    _recentlyDragged = true;
                }
                return true;
            }
        }

        //Gdx.app.log("XXX", "x = " + delta.x);

        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        _recentlyDragged=false;
        return false;
    }
    @Override
    public boolean scrolled(int amount) {
        _recentlyDragged=false;
        return false;
    }
}
