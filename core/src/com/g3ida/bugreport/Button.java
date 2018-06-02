package com.g3ida.bugreport;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;

public class Button
{
    private TextureRegion _r, _s;
    private float _x, _y, _w, _h;
    private boolean _selected = false;
    private Event.Type _event;
    private Camera _cam;
    private int _key;

    private float _ox, _oy;

    public Button(TextureRegion r, TextureRegion s, Event.Type e, int key, Camera c) {
        _r = r;
        _s = s;
        _x = 0;
        _y = 0;
        _w = _r.getRegionWidth();
        _h = _r.getRegionHeight();
        _event = e;
        _cam = c;
        _key = key;
    }

    public float getWidth(){return _w;}
    public float getHeight(){return _h;}

    public boolean selected(){return _selected;}

    public void setPosition(float x, float y) {
        _x = x;
        _y = y;
    }

    public void setOffset(float x, float y) {
        _ox = x; _oy = y;
    }

    public void update(Camera c) {
        boolean _selectedOld = _selected;
        _selected = false;
        for (int i = 0; i < 20; i++) {

            if (Gdx.input.isTouched(i)) {
                int touchx = Gdx.input.getX(i);
                int touchy = Gdx.input.getY(i);

                Vector3 tp = new Vector3();

                c.unproject(tp.set(touchx, touchy, 0));

                //just a dirty fix
                if (tp.x >= _x+_ox && tp.x <= _x+_ox + _w &&
                        tp.y >= _y+_oy-100 && tp.y <= _y+_oy + _h) {
                    _selected = true;
                    break;
                }
            }
        }

         if(Gdx.input.isKeyPressed(_key)) {

            _selected = true;
        }


        if(_selectedOld == true && _selected == false) {
            Event e = new Event();
            e.type = _event;
            e.info.buttonState.pressed = false;
            EventHandler.get().notifyObservers(e);
        }
        else if(_selectedOld == false && _selected == true) {
            Event e = new Event();
            e.type = _event;
            e.info.buttonState.pressed = true;
            EventHandler.get().notifyObservers(e);
        }
    }

    public void draw(SpriteBatch batch) {
        if(!_selected)
            batch.draw(_r, _x, _y);
        else
            batch.draw(_s, _x, _y);
    }

    public void setEvent(Event.Type type) {
        _event = type;
    }
}