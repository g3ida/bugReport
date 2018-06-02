package com.g3ida.bugreport;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Observable;
import java.util.Observer;

public class MenuButton implements Observer, Disposable{
    private TextureAtlas.AtlasRegion _region;
    private float _x, _y, _sx = 1f, _sy = 1f, _angle=0f, _ox, _oy, _w, _h, _angleExtra=0f;
    private Event.Type _event;
    private ToggleAngle _toggle;
    private boolean _doShake = true;
    private boolean _doShakeEvent = true;
    private Viewport _view;
    private boolean _ignored = false;
    private boolean _frozen = false;
    private boolean _doNotifyShaking = true;
    private boolean _triggered = false;

    public MenuButton(TextureAtlas.AtlasRegion region, Event.Type event, Viewport view)
    {
        _region = region;
        _x = 0;
        _y = 0;
        _event = event;
        _w = region.getRegionWidth();
        _h = region.getRegionHeight();
        _ox = _w * 0.5f;
        _oy = _y * 0.5f;
        _toggle = new ToggleAngle();
        _view = view;
        EventHandler.get().addObserver(this);

    }

    @Override
    public void dispose() {
        EventHandler.get().deleteObserver(this);
    }

    public float getWidth() {
        return _w;
    }

    public float getHeight() {
        return _h;
    }
    public void setPosition(float x, float y) {
        _x = x;
        _y = y;
    }

    public void doNotNotifyShaking() {
        _doNotifyShaking = false;
    }

    public void setScale(float scaleX, float scaleY) {
        _sx = scaleX;
        _sy = scaleY;
    }

    public float getx() {return _x;}
    public float gety() {return _y;}

    public void setOrigin(float originX, float originY) {
        _ox = originX;
        _oy = originY;
    }

    public void setAngle(float angle) {
        _angleExtra = angle;
    }

    public float getAngle() {
      return _angleExtra;
    }

    public void update(float delta) {
        _toggle.update(delta);
        _angle = _toggle.get()*20-20;
    }

    public void changeRegion(TextureAtlas.AtlasRegion region) {
        _region = region;
        _w = region.getRegionWidth();
        _h = region.getRegionHeight();
        _ox = _w * 0.5f;
        _oy = _y * 0.5f;
    }

    public void setShake(boolean doshake) {
        _doShake = doshake;
    }

    public void setShakeEvent(boolean doshake) {
        _doShakeEvent = doshake;
    }

    public boolean isClicInside(float x, float y) {
        return ((x >= _x) && (x<= _x + _w) && (y >= _y) && (y <= _y + _h));
    }

    @Override
    public void update(Observable obs, Object o) {
        if(_ignored || _frozen) return;
        if(o == null) return;
        if (!(o instanceof Event)) return;

        Event e = (Event) o;

        Vector3 tp = new Vector3();

        if(e.type == Event.Type.TOUCH && e.info.touchState.button == Input.Buttons.LEFT) {
            _view.getCamera().unproject(tp.set(e.info.touchState.screenX , e.info.touchState.screenY, 0));
            if((tp.x >= _x) &&
                    (tp.x<= _x + _w) &&
                    (tp.y >= _y) &&
                    (tp.y <= _y + _h))
            {

                if(e.info.touchState.isDown == true) {
                    if(!_toggle.isRunning())
                    {
                        if(_doShake) {
                            if(_doShakeEvent && _doNotifyShaking)
                                EventHandler.get().notifyObservers(new Event(Event.Type.MENU_BUTTON_SHAKE));
                            _toggle.start();
                            _triggered = false;
                        }
                    }
                } else {
                    if(!_triggered) {
                        EventHandler.get().notifyObservers(new Event(_event));
                        _triggered = true;
                    }

                }
            }
        }
    }

    public void draw(SpriteBatch batch) {
        if(_ignored) return;
        batch.draw(_region, _x, _y, _ox, _oy, _w, _h, _sx, _sy, _angle+_angleExtra);
    }

    public void setEvent(Event.Type type) {
        _event = type;
    }

    public void ignore() {
        _ignored = true;
    }

    public boolean isIgnored() { return _ignored; }
    public Event.Type getEvent() { return _event; }

    public void freeze() {
        _frozen = true;
    }
    public void unfreeze() {
        _frozen = false;
    }

    public void reconsider() {
        _ignored = false;
    }
}
