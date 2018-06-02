package com.g3ida.bugreport;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Observable;
import java.util.Observer;

public class TransitionScreen implements Screen, Observer {

    private BugReport _game;
    private BitmapFont _font;
    private Viewport _viewport;
    private int _level;

    private String _quote = new String();
    private String _autor = new String();

    //this variables serves to make a blink effect for the 'tap to skip" message.
    private boolean _showTapToSkip = true;
    private float _timeToSkip = 0.f;
    private final float TIME_BLINK_DURATION = 1.0f;

    private FrameBuffer _buffer;

    private float _time = 0.f;
    private boolean _closing = false;

    TransitionScreen(BugReport game, Viewport view, int level) {
        _game = game;
        _viewport = view;
        _level = level;

        EventHandler.get().addObserver(this);

        _font = new BitmapFont(Gdx.files.internal("data/fonts/PatrickHandSC.fnt"),Gdx.files.internal("data/fonts/PatrickHandSC.png"),false);

        if(!LevelSettings.isQuotationsLoaded()) {
            LevelSettings.loadQuotations();
        }

        if(LevelSettings.quotations != null && LevelSettings.quotations[_level-1] != null) {
            _quote =  LevelSettings.quotations[_level-1];
            _autor = LevelSettings.aurhors[_level-1];
        }

        LevelSettings.currentLevel = level-1;

    }

    @Override
    public void show() {
        _buffer = new FrameBuffer(Pixmap.Format.RGB888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),false);
        _buffer.bind();

        Gdx.gl.glClearColor(0.f, 0.f, 0.f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        SpriteBatch batch = _game.getBatch();
        batch.begin();
        drawText();
        batch.end();

        _buffer.unbind();
        _game.loadInterstitialAd();
    }

    private void drawText() {
        _font.draw(_game.getBatch(),
                "bug #" + _level +
                        ((!_quote.isEmpty())? "\n" + "\"" + _quote + "\"" + "\n" : "\n") +
                        ((!_autor.isEmpty())? "-" + _autor + "-" : _autor),
                (_viewport.getWorldWidth()-850)*0.5f,
                (float)_viewport.getWorldHeight()*0.5f+((float)Math.ceil(_quote.length()/70.f)+2)*_font.getLineHeight()*0.5f,
                850, 1, true);

        _font.draw(_game.getBatch(),
                "tap to skip", (float) (_viewport.getWorldWidth() - 400) * 0.5f, 50, 400, 1, true);
    }

    @Override
    public void render(float delta) {

        Settings.cuurentDelay+=delta;
        Gdx.gl.glClearColor(0.f, 0.f, 0.f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        SpriteBatch batch = _game.getBatch();

        batch.begin();
        if(!_closing) {
            batch.setColor(1.f, 1.f, 1.f, Math.min(1.f, _time*2f));
        } else {
            batch.setColor(1.f, 1.f, 1.f, Math.max(0.f, 1-_time*2f));
        }
        Texture texture = _buffer.getColorBufferTexture();

        float w = _viewport.getWorldWidth();
        float h = ((float)texture.getHeight()/(float)texture.getWidth()) * _viewport.getWorldWidth();

        float x = _viewport.getWorldWidth()*0.5f - w*0.5f;
        float y = _viewport.getWorldHeight()*0.5f - h*0.5f;

        batch.draw(texture,
                x, y,
                w, h,
                0, 0, 1, 1);


        batch.end();

        //blink effect for the tap to skip message.
        _timeToSkip += delta;
        if(_timeToSkip > TIME_BLINK_DURATION) {
            _timeToSkip = 0.f;
            _showTapToSkip = !_showTapToSkip;
        }
        batch.setColor(1.f, 1.f, 1.f, 1.f);

        //Blink effect.
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        ShapeRenderer shapeRenderer = new ShapeRenderer();
        shapeRenderer.setProjectionMatrix(_viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if(_showTapToSkip) {
            shapeRenderer.setColor(0.0f, 0.0f, 0.0f, _timeToSkip / TIME_BLINK_DURATION);
        } else {
            shapeRenderer.setColor(0.0f, 0.0f, 0.0f, 1 - ( _timeToSkip / TIME_BLINK_DURATION));
        }
        shapeRenderer.rect(x, y, w, h*0.12f);
        shapeRenderer.end();


        _time += delta;

        if(_closing && _time >= 0.5f) {
            dispose();
            _game.setScreen(new LevelScreen(_game, _level, _viewport));
        }
    }

    @Override
    public void resize(int width, int height) {
        _viewport.update(width, height, true);
        _game.getBatch().setProjectionMatrix(_viewport.getCamera().combined);
        _viewport.apply();

        _buffer.dispose();
        _buffer = new FrameBuffer(Pixmap.Format.RGB888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),false);
        _buffer.bind();

        Gdx.gl.glClearColor(0.f, 0.f, 0.f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        SpriteBatch batch = _game.getBatch();
        batch.begin();
        drawText();
        batch.end();

        _buffer.unbind();
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
        _buffer.dispose();
    }

    @Override
    public void update(Observable observable, Object o) {
        if(_time < 0.5f) return;
        if(o == null) return;
        if (!(o instanceof Event)) return;

        Event e = (Event) o;
        if(e.type == Event.Type.BACK)
        {
            dispose();
            _game.setScreen(new LevelMenuScreen(_game, _viewport));
        }

        if(e.type == Event.Type.TOUCH)
        {
            _closing = true;
            _time = 0.f;
        }
    }
}
