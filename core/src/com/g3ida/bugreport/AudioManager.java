package com.g3ida.bugreport;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class AudioManager implements Observer, Disposable {

    private static AudioManager _instance;

    private Map<Event.Type, Sound> _map;

    public static final AudioManager get() {
        if (_instance == null) {
            _instance = new AudioManager();
        }
        return _instance;
    }

    private AudioManager() {
        EventHandler.get().addObserver(this);
        _map = new HashMap<Event.Type, Sound>();
    }


    void detatch(Event.Type e) {
        _map.remove(e);
    }

    void attachSound(Sound s, Event.Type e) {
        _map.put(e, s);
    }

    @Override
    public void update(Observable obs, Object o) {
        if(Settings.isSilent) return;
        if(o == null) return;
        if (!(o instanceof Event)) return;
        Event obj = (Event) o;
        try{
            _map.get(obj.type).play();
        } catch(Exception e) {

        }
    }

    @Override
    public void dispose() {}
}
