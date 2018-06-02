package com.g3ida.bugreport;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

public class MenuScreen implements Screen, Observer {

    private TextureAtlas _atlas;
    private Vector<MenuButton> _buttons = new Vector<MenuButton>();

    private Appear _appearPlay;
    private Appear _appearOthers;
    private Appear _appearTitle;

    private OrthographicCamera _menuCam;
    private Viewport _viewport;

    private TextureAtlas.AtlasRegion _bugImg;
    private float _startBug;

    private Event.Type _currentEvent = Event.Type.NONE;

    private BugReport _game;

    private float _menuTime = 0.f;
    private boolean _elasticSoundPlayed = false;


    public MenuScreen(BugReport game, TextureAtlas atlas, Viewport viewport) {

        _viewport = viewport;
        _menuCam = (OrthographicCamera) _viewport.getCamera();
        _game = game;

        _atlas = atlas;
        _buttons.add(new MenuButton( _atlas.findRegion("title"), Event.Type.TITLE_SHAKED, _viewport));
        _buttons.add(new MenuButton( _atlas.findRegion("play"), Event.Type.PLAY_BUTTON_TRIGGERED, _viewport));
        _buttons.add(new MenuButton( _atlas.findRegion("options"), Event.Type.CREDITS_BUTTON_TRIGGERED, _viewport));
        _buttons.add(new MenuButton( _atlas.findRegion("donate"), Event.Type.NONE, _viewport));
        _buttons.add(new MenuButton( _atlas.findRegion(((Settings.isSilent) ? "soundoff" : "sound")), Event.Type.SOUND_BUTTON_TRIGGERED, _viewport));
        _buttons.add(new MenuButton( _atlas.findRegion("fb"), Event.Type.NONE, _viewport));
        _buttons.add(new MenuButton( _atlas.findRegion("tweet"), Event.Type.NONE, _viewport));

        _buttons.get(4).setShakeEvent(false);

        _bugImg =  _atlas.findRegion("bug");
        _startBug = -_bugImg.getRegionHeight() * 1.3f;

        _appearPlay = new Appear();
        _appearPlay.start(-0.4f);
        _appearOthers = new Appear();
        _appearOthers.start(-0.1f);
        _appearTitle = new Appear(Interpolation.elasticOut);
        _appearTitle.lifetime(1.8f);
        _appearTitle.start(-0.2f);

        //initialize achievement system
        AchievementSystem.get();

        AudioManager.get().attachSound(Settings.shakeSFX, Event.Type.MENU_BUTTON_SHAKE);
        AudioManager.get().attachSound(Settings.button2SFX, Event.Type.PLAY_BUTTON_TRIGGERED);
        AudioManager.get().attachSound(Settings.button2SFX, Event.Type.CREDITS_BUTTON_TRIGGERED);
        EventHandler.get().addObserver(this);
    }

    public MenuScreen(BugReport game, TextureAtlas atlas) {
        this(game, atlas, new ExtendViewport(Settings.SCREEN_WIDTH, Settings.SCREEN_HEIGHT, new OrthographicCamera()));
    }

    public MenuScreen(BugReport game) {
        this(game, new TextureAtlas(Gdx.files.internal("data/graphics/menus.pack")));
    }

    @Override
    public void update(Observable observable, Object o) {
        if(o == null) return;
        if (!(o instanceof Event)) return;

        Event e = (Event) o;
        switch (e.type) {
            case PLAY_BUTTON_TRIGGERED:
                if(!_appearPlay.reversed()) {
                    _appearPlay.reverse();
                    _appearPlay.start(-0.1f);
                    _appearOthers.reverse();
                    _appearOthers.start(-0.4f);
                    _appearTitle.reverse();
                    _appearTitle.start();
                    if(_currentEvent== Event.Type.NONE) _currentEvent = Event.Type.PLAY_BUTTON_TRIGGERED;
                }
                break;
            case CREDITS_BUTTON_TRIGGERED:
                if(!_appearPlay.reversed()) {
                    _appearPlay.reverse();
                    _appearPlay.start(-0.1f);
                    _appearOthers.reverse();
                    _appearOthers.start(-0.4f);
                    _appearTitle.reverse();
                    _appearTitle.start();
                    if(_currentEvent== Event.Type.NONE) _currentEvent = Event.Type.CREDITS_BUTTON_TRIGGERED;
                }
                break;
            case SOUND_BUTTON_TRIGGERED:
                Settings.isSilent = !Settings.isSilent;
                if(Settings.isSilent)
                    _buttons.get(4).changeRegion(_atlas.findRegion("soundoff"));
                else {
                    _buttons.get(4).changeRegion(_atlas.findRegion("sound"));
                    Settings.button2SFX.play();
                }
                break;
        }
    }

    @Override
    public void show() {
        _appearPlay.start();
        _appearOthers.start();
    }

    @Override
    public void render(float delta) {
        Settings.cuurentDelay+=delta;
        Gdx.gl.glClearColor(1, 0.8627f, 0.7725f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        SpriteBatch batch = _game.getBatch();

        batch.setProjectionMatrix(_menuCam.combined);


        float viewWidth =_viewport.getWorldWidth();
        float viewHeight = _viewport.getWorldHeight();

        _appearPlay.update(delta);
        _appearOthers.update(delta);
        _appearTitle.update(delta);


        float x = _appearTitle.get();
        if(!_appearTitle.reversed()) {
            _buttons.get(0).setPosition((viewWidth - _buttons.get(0).getWidth()) * 0.5f, viewHeight * 0.67f * x - _buttons.get(0).getHeight() * 0.5f);
            _buttons.get(0).setScale(1.f/x, x);
        } else {
            _buttons.get(0).setPosition((viewWidth - _buttons.get(0).getWidth()) * 0.5f, (viewHeight*0.67f-_buttons.get(0).getHeight()*0.5f)+viewHeight*(1-_appearPlay.get()));
            _buttons.get(0).setScale(1.f, 1.f);
        }
        _buttons.get(0).setAngle(_appearOthers.get()*5.f);
        _buttons.get(1).setPosition((viewWidth - _buttons.get(1).getWidth())*0.5f, viewHeight*0.05f - _buttons.get(1).getWidth()*(1-_appearPlay.get()));
        _buttons.get(2).setPosition(viewWidth*0.02f-(viewWidth*0.02f+_buttons.get(2).getWidth())*(1-_appearOthers.get()), viewHeight*0.05f);
        _buttons.get(3).setPosition(viewWidth*0.98f - _buttons.get(3).getWidth()+(viewWidth*0.02f+_buttons.get(3).getWidth())*(1-_appearOthers.get()), viewHeight*0.05f);
        _buttons.get(4).setPosition(viewWidth*0.98f - _buttons.get(4).getWidth()+(viewWidth*0.02f+_buttons.get(4).getWidth())*(1-_appearOthers.get()), viewHeight*0.98f - _buttons.get(4).getHeight());
        _buttons.get(5).setPosition(viewWidth*0.02f-(viewWidth*0.02f+_buttons.get(5).getHeight())*(1-_appearOthers.get()), viewHeight*0.98f - _buttons.get(5).getHeight());
        _buttons.get(6).setPosition(viewWidth*0.03f + _buttons.get(5).getWidth() -(viewWidth*0.03f+_buttons.get(5).getWidth()+_buttons.get(6).getWidth())*(1-_appearOthers.get()), viewHeight*0.98f - _buttons.get(6).getHeight());

        batch.begin();

        //Background animation.
        float bugWidth = _bugImg.getRegionWidth();
        float bugHeight = _bugImg.getRegionHeight();
        _startBug = (_startBug + delta*250);
        if(_startBug>0) _startBug -=bugHeight*1.3f;
        for(float j=0; j < viewWidth; j += bugWidth*2.6f) {
            for (float i = _startBug - bugHeight * 1.3f; i < viewHeight ; i += bugHeight*1.3f) {

                batch.draw(_bugImg, j, i);
                batch.draw(_bugImg, j + bugWidth*1.3f, i - 2* _startBug + bugHeight*0.65f);
            }
        }

        for (MenuButton b : _buttons) {
            b.update(delta);
            b.draw(batch);
        }

        batch.end();

        if(!_appearOthers.isRunning() &&!_appearPlay.isRunning() && !_appearTitle.isRunning())
        {
            switch (_currentEvent)
            {
                case PLAY_BUTTON_TRIGGERED:
                    dispose();
                    LevelMenuScreen tmp = new LevelMenuScreen(_game, _atlas, _viewport);
                    tmp.setStartBug(_startBug);
                    _game.setScreen(tmp);
                    break;
                case CREDITS_BUTTON_TRIGGERED:
                    dispose();/*
                    CreditsScreen cs = new CreditsScreen(_game, _atlas, _viewport);
                    cs.setStartBug(_startBug);*/
                    SettingsScreen cs = new SettingsScreen(_game, _atlas, _viewport);
                    cs.setStartBug(_startBug);
                    _game.setScreen(cs);
                    break;
            }
        }

        _menuTime += delta;
        if(_menuTime > 0.4f && !_elasticSoundPlayed) {
            if(!Settings.isSilent)
                Settings.elasticSFX.play();
            _elasticSoundPlayed = true;
        }

    }

    @Override
    public void resize(int width, int height) {
        _viewport.update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {
        _appearPlay.start();
        _appearOthers.start();
    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        AudioManager.get().detatch(Event.Type.PLAY_BUTTON_TRIGGERED);
        AudioManager.get().detatch(Event.Type.SOUND_BUTTON_TRIGGERED);
        AudioManager.get().detatch(Event.Type.MENU_BUTTON_SHAKE);
        AudioManager.get().detatch(Event.Type.CREDITS_BUTTON_TRIGGERED);

        for(MenuButton b : _buttons)
            b.dispose();

        EventHandler.get().deleteObserver(this);
    }

    public void setStartBug(float start) {_startBug = start;}

}


class ToggleAngle
{
    private float _timePlay = 2.f;
    private float _lifetime = 1.f;
    private Interpolation _interpolation = Interpolation.elasticOut;
    private float _playAngle = 0.f;

    public ToggleAngle(){}
    public void start(){
        if(_timePlay > 0.6f)
            _timePlay = 0.f;
    }
    public void update(float dt){
        _timePlay += dt;
        float pro = Math.min(1f, _timePlay/_lifetime);
        _playAngle = _interpolation.apply(pro);
    }

    public float get(){
        return _playAngle;
    }

    public boolean isRunning(){return _timePlay < _lifetime;}
}

class Appear
{
    private float _timePlay = 2.f;
    private float _lifetime = 0.6f;
    private Interpolation _interpolation = Interpolation.pow2;
    private float _pos;
    boolean _rev = false;

    public Appear() {}
    public Appear(Interpolation i) {_interpolation = i;}

    public void lifetime(float lf){_lifetime = lf;}

    public void start() {
        if(_timePlay > _lifetime) _timePlay = 0;
    }

    public void start(float timestart) {
        if(_timePlay > _lifetime) _timePlay = timestart;
    }
    public void update(float dt) {
        _timePlay += dt;
        float pro = Math.min(1f, _timePlay/_lifetime);
        _pos = _interpolation.apply(pro);
    }

    public float get() {
        if(_timePlay < 0) if(_rev) return 1; else return 0;
        if(_rev)
            return 1-_pos;
        return _pos;
    }

    public boolean isRunning(float x){return _timePlay < _lifetime+x;}

    public boolean isRunning(){return _timePlay < _lifetime;}
    public boolean reversed() {return _rev;}

    public void reverse(){_rev = !_rev;}
}
