package com.g3ida.bugreport;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

/**
 * Created by g3ida on 1/7/2018.
 */

public class CreditsScreen implements Screen, Observer {

    private TextureAtlas _atlas;
    private Vector<MenuButton> _buttons = new Vector<MenuButton>();

    private Appear _appear;

    private OrthographicCamera _menuCam;
    private Viewport _viewport;

    private TextureAtlas.AtlasRegion _bugImg;
    private float _startBug;

    private BugReport _game;
    private Event.Type _currentEvent = Event.Type.NONE;

    private BitmapFont _font;
    private String _text = new String("created by :\nMohamed Alaa Gaida\n" +
            "tilesets by :\nKenny, www.kenney.nl");

    public CreditsScreen(BugReport game, Viewport viewport) {
        this(game, new TextureAtlas(Gdx.files.internal("data/graphics/Menu.pack")), viewport);
    }

    public CreditsScreen(BugReport game, TextureAtlas atlas, Viewport viewport) {

        _viewport = viewport;
        _menuCam = (OrthographicCamera) _viewport.getCamera();
        _game = game;

        _atlas = atlas;
        _buttons.add(new MenuButton( _atlas.findRegion("credits2"), Event.Type.NONE, _viewport));
        _buttons.add(new MenuButton( _atlas.findRegion("back"), Event.Type.BACK_BUTTON_TRIGGERED, _viewport));
        _buttons.add(new MenuButton( _atlas.findRegion(((Settings.isSilent) ? "soundoff" : "sound")), Event.Type.SOUND_BUTTON_TRIGGERED, _viewport));

        _bugImg =  _atlas.findRegion("bug");
        _startBug = -_bugImg.getRegionHeight() * 1.3f;

        _appear = new Appear();
        _appear.start();

        AudioManager.get().attachSound(Settings.shakeSFX, Event.Type.MENU_BUTTON_SHAKE);
        AudioManager.get().attachSound(Settings.cancelSFX, Event.Type.BACK_BUTTON_TRIGGERED);

        _font = new BitmapFont(Gdx.files.internal("data/fonts/achievmentspatrick.fnt"),Gdx.files.internal("data/fonts/achievmentspatrick.png"),false);

        EventHandler.get().addObserver(this);
    }

    public CreditsScreen(BugReport game, TextureAtlas atlas) {
        this(game, atlas, new ExtendViewport(Settings.SCREEN_WIDTH, Settings.SCREEN_HEIGHT, new OrthographicCamera()));
    }

    public CreditsScreen(BugReport game) {
        this(game, new TextureAtlas(Gdx.files.internal("data/graphics/Menu.pack")));
    }

    @Override
    public void show() {}

    private void drawText() {
        _font.setColor(0.48627f, 0.1764f, 0.1764f, 1);
        _font.draw(_game.getBatch(),
                _text,
                (_viewport.getWorldWidth()-850)*0.5f,
                (float)_viewport.getWorldHeight()*0.5f+((float)Math.ceil(_text.length()/70.f)+2)*_font.getLineHeight()*0.5f

                        - 500f*(1-_appear.get())
                ,
                860, 1, true);
    }

    @Override
    public void render(float delta) {
        Settings.cuurentDelay+=delta;
        SpriteBatch batch = _game.getBatch();

        Gdx.gl.glClearColor(1, 0.8627f, 0.7725f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(_menuCam.combined);

        float viewWidth =_viewport.getWorldWidth();
        float viewHeight = _viewport.getWorldHeight();

        _appear.update(delta);

        _buttons.get(0).setPosition((viewWidth - _buttons.get(0).getWidth())*0.5f, viewHeight - 1.5f*_buttons.get(0).getHeight()*(_appear.get()));
        _buttons.get(1).setPosition(viewWidth*0.02f-(viewWidth*0.02f+_buttons.get(0).getWidth())*(1-_appear.get()), viewHeight*0.05f);
        _buttons.get(2).setPosition(viewWidth*0.98f - _buttons.get(2).getWidth()+(viewWidth*0.02f+_buttons.get(2).getWidth())*(1-_appear.get()), viewHeight*0.98f - _buttons.get(2).getHeight());

        if(!_appear.isRunning()) {
            switch (_currentEvent) {
                case BACK_BUTTON_TRIGGERED:
                    dispose();
                    SettingsScreen tmp = new SettingsScreen(_game, _atlas, _viewport);
                    tmp.setStartBug(_startBug);
                    _game.setScreen(tmp);
                    break;
            }
        }

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

        drawText();

        //write

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        _viewport.update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        AudioManager.get().detatch(Event.Type.MENU_BUTTON_SHAKE);
        AudioManager.get().detatch(Event.Type.BACK_BUTTON_TRIGGERED);

        for(MenuButton b : _buttons)
            b.dispose();
        EventHandler.get().deleteObserver(this);
    }

    @Override
    public void update(Observable observable, Object o) {
        if(o == null) return;
        if (!(o instanceof Event)) return;

        Event e = (Event) o;
        switch (e.type) {
            case BACK:
            case BACK_BUTTON_TRIGGERED:
                _currentEvent = Event.Type.BACK_BUTTON_TRIGGERED;
                _appear.reverse();
                _appear.start(-0.6f);
                break;
            case SOUND_BUTTON_TRIGGERED:
                Settings.isSilent = !Settings.isSilent;
                if(Settings.isSilent)
                    _buttons.get(2).changeRegion(_atlas.findRegion("soundoff"));
                else {
                    _buttons.get(2).changeRegion(_atlas.findRegion("sound"));
                    Settings.button2SFX.play();
                }
                break;
        }
    }

    public void setStartBug(float start) {_startBug = start;}
}
