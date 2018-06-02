package com.g3ida.bugreport;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by g3ida on 8/24/2017.
 */

public class CrashScreen implements Screen, Observer {

    private Viewport _view;
    private BugReport _game;
    private String _text;
    private BitmapFont _font;
    private float _time = 0.f;
    private boolean _restartEnabled = false;
    private int _level = 1;

    public CrashScreen(BugReport game, Viewport view, String splashMsg, boolean restartEnabled, int level) {
        this(game, view, splashMsg);
        _restartEnabled = restartEnabled;
        _level = level;
    }


    public CrashScreen(BugReport game, Viewport view, String splashMsg) {
        _game = game;
        _view = view;
        _text = splashMsg;


        _game = game;
        _view = view;
        EventHandler.get().addObserver(this);
        _font = new BitmapFont(Gdx.files.internal("data/fonts/Courier.fnt"),Gdx.files.internal("data/fonts/Courier.png"),false);
        if(!Settings.isSilent)
            Settings.buttonErrorSFX.play();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Settings.cuurentDelay+=delta;
        Gdx.gl.glClearColor(0.f, 0.f, 1.f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        SpriteBatch batch = _game.getBatch();
        batch.begin();
        _font.setColor(255, 255, 255, 255);
        _font.draw(batch,  "FATAL ERROR !\n" + _text ,97,
                Settings.SCREEN_HEIGHT*0.5f +(_text.length()/(850/64)*(_font.getLineHeight()))*0.3f, 850, 3, true);


        if(_restartEnabled) {
            batch.end();

            ShapeRenderer s = new ShapeRenderer();
            s.begin(ShapeRenderer.ShapeType.Filled);
            s.setColor(255, 255, 255, 0);
            s.rect(0, 40, _view.getScreenWidth(), 60);
            s.end();

            batch.begin();

            _font.setColor(0, 0, 255, 255);
            _font.draw(_game.getBatch(),
                    "RESTART LEVEL",  (float) (_view.getWorldWidth()-400)*0.5f, 100, 400, 1, true);

        }
        batch.end();

        _time += delta;
    }

    @Override
    public void resize(int width, int height) {
        _view.update(width, height, true);

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        _font.dispose();
        EventHandler.get().deleteObserver(this);
    }

    @Override
    public void update(Observable observable, Object o) {
        if(_time < 1.f) return;
        if(o == null) return;
        if (!(o instanceof Event)) return;

        Event e = (Event) o;
        if(e.type == Event.Type.BACK)
        {
            dispose();
            _game.setScreen(new LevelMenuScreen(_game, _view));
        }

        if(e.type == Event.Type.TOUCH)
        {
            Vector3 tp = new Vector3();
            _view.getCamera().unproject(tp.set(e.info.touchState.screenX, e.info.touchState.screenY, 0));
            if(tp.y < 120 && tp.y > 40) {
                dispose();
                _game.setScreen(new TransitionScreen(_game, _view, _level));

            } else {
                dispose();
                _game.setScreen(new LevelMenuScreen(_game, _view));
            }
        }
    }
}
