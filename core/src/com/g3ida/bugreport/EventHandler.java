package com.g3ida.bugreport;

import com.badlogic.gdx.Gdx;

import java.util.Observable;

/**
 * Created by g3ida on 8/15/2017.
 */

public class EventHandler extends Observable {

    private static EventHandler _instance;

    public static final EventHandler get() {
        if (_instance == null) {
            _instance = new EventHandler();
        }
        return _instance;
    }

    private EventHandler() {}

    public void notifyObservers(Object arg){

        setChanged();
        super.notifyObservers(arg);
    }
}
