package com.g3ida.bugreport;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

public class LevelMenuScreen implements Screen, Observer {

    private TextureAtlas _atlas;
    private Vector<MenuButton> _buttons = new Vector<MenuButton>();

    private Appear _appear;
    private Appear _appearLeft;
    private Appear _appearRight;

    private Sound _confirmSound;

    private OrthographicCamera _menuCam;
    private Viewport _viewport;

    private TextureAtlas.AtlasRegion _bugImg;
    private float _startBug;

    private Levels _levels;

    private boolean _popUpShown = false;
    private TextureAtlas.AtlasRegion _popupRegion;
    private TextureAtlas.AtlasRegion _rectPopup;
    private MenuButton _confirmButton;
    private MenuButton _rejectButton;
    private Appear _popupAppear = new Appear();

    private BugReport _game;
    private Event.Type _currentEvent = Event.Type.NONE;

    private TextureAtlas.AtlasRegion[] _numsRegion = new  TextureAtlas.AtlasRegion[10];
    private TextureAtlas.AtlasRegion _xRegion;
    private TextureAtlas.AtlasRegion _diamondRegin;

    public LevelMenuScreen(BugReport game, Viewport viewport) {
        this(game, new TextureAtlas(Gdx.files.internal("data/graphics/menus.pack")), viewport);
    }

    public LevelMenuScreen(BugReport game, TextureAtlas atlas, Viewport viewport) {

        _viewport = viewport;
        _menuCam = (OrthographicCamera) _viewport.getCamera();
        _game = game;


        _atlas = atlas;
        _buttons.add(new MenuButton( _atlas.findRegion("levels"), Event.Type.NONE, _viewport));
        _buttons.add(new MenuButton( _atlas.findRegion("back"), Event.Type.BACK_BUTTON_TRIGGERED, _viewport));
        _buttons.add(new MenuButton( _atlas.findRegion("right"), Event.Type.MENU_RIGHT_BUTTON_TRIGGERED, _viewport));
        _buttons.add(new MenuButton( _atlas.findRegion("left"), Event.Type.MENU_LEFT_BUTTON_TRIGGERED, _viewport));
        _buttons.add(new MenuButton( _atlas.findRegion(((Settings.isSilent) ? "soundoff" : "sound")), Event.Type.SOUND_BUTTON_TRIGGERED, _viewport));

        _bugImg =  _atlas.findRegion("bug");
        _startBug = -_bugImg.getRegionHeight() * 1.3f;


        for(int i=0; i<10; i++) {
            _numsRegion[i] = _atlas.findRegion("d"+i);
        }
        _xRegion = _atlas.findRegion("dx");
        _diamondRegin = _atlas.findRegion("diamond");

        _appear = new Appear();
        _appear.start();
        _appearLeft = new Appear();
        _appearRight = new Appear();
        _appearLeft.start();
        _appearRight.start();

        _levels = new Levels(_game.getBatch(), _atlas, _viewport);
        _levels.startAnimation();

        //popup stuff
        _popupRegion = _atlas.findRegion("confirm");
        _confirmButton = new MenuButton(_atlas.findRegion("yes"), Event.Type.CONFIRM, _viewport);
        _rejectButton = new MenuButton(_atlas.findRegion("no"), Event.Type.REJECT, _viewport);
        _rectPopup = atlas.findRegion("rect");
        _confirmButton.ignore();
        _rejectButton.ignore();

        _confirmSound = Gdx.audio.newSound(Gdx.files.internal("data/sounds/button3.ogg"));

        AudioManager.get().attachSound(Settings.shakeSFX, Event.Type.MENU_BUTTON_SHAKE);
        AudioManager.get().attachSound(Settings.cancelSFX, Event.Type.BACK_BUTTON_TRIGGERED);
        AudioManager.get().attachSound(Settings.cancelSFX, Event.Type.REJECT);
        AudioManager.get().attachSound(_confirmSound, Event.Type.CONFIRM);
        AudioManager.get().attachSound(Settings.buttonSFX, Event.Type.POPUP);
        AudioManager.get().attachSound(Settings.shakeSFX, Event.Type.DRAG_GESTURE);


        EventHandler.get().addObserver(this);
    }

    public LevelMenuScreen(BugReport game, TextureAtlas atlas) {
        this(game, atlas, new ExtendViewport(Settings.SCREEN_WIDTH, Settings.SCREEN_HEIGHT, new OrthographicCamera()));
    }

    public LevelMenuScreen(BugReport game) {
        this(game, new TextureAtlas(Gdx.files.internal("data/graphics/Menu.pack")));
    }

    @Override
    public void show() {}

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
        _appearRight.update(delta);
        _appearLeft.update(delta);

        _buttons.get(0).setPosition((viewWidth - _buttons.get(0).getWidth())*0.5f, viewHeight - 1.5f*_buttons.get(0).getHeight()*(_appear.get()));
        _buttons.get(1).setPosition(viewWidth*0.02f-(viewWidth*0.02f+_buttons.get(0).getWidth())*(1-_appear.get()), viewHeight*0.05f);
        _buttons.get(2).setPosition(viewWidth*0.98f - _buttons.get(2).getWidth()+(viewWidth*0.02f+_buttons.get(2).getWidth())*(1-_appearRight.get()), viewHeight*0.5f - _buttons.get(0).getHeight()*0.8f);
        _buttons.get(3).setPosition(viewWidth*0.02f-(viewWidth*0.02f+_buttons.get(2).getWidth())*(1-_appearLeft.get()), viewHeight*0.5f - _buttons.get(3).getHeight()*0.8f);
        _buttons.get(4).setPosition(viewWidth*0.98f - _buttons.get(4).getWidth()+(viewWidth*0.02f+_buttons.get(4).getWidth())*(1-_appear.get()), viewHeight*0.98f - _buttons.get(4).getHeight());

        if(_levels.getCurrentPage() == _levels.numPages()-1) {
            if(!_appearRight.reversed() && !_appear.isRunning()) {
                _appearRight.reverse();
                _appearRight.start();
            }
        } else {
            if(_appearRight.reversed() && !_appear.reversed()) {
                _appearRight.reverse();
                _appearRight.start();
            }
        }

        if(_levels.getCurrentPage() == 0) {
            if(!_appearLeft.reversed() && !_appear.isRunning()) {
                _appearLeft.reverse();
                _appearLeft.start();
            }
        } else {
            if(_appearLeft.reversed() && !_appear.reversed())
            {
                _appearLeft.reverse();
                _appearLeft.start();
            }
        }

        if(!_appear.isRunning() &&!_appearLeft.isRunning() && !_appearRight.isRunning() &&!_levels.isRunning()) {
            switch (_currentEvent) {
                case BACK_BUTTON_TRIGGERED:
                    dispose();
                    MenuScreen tmp = new MenuScreen(_game, _atlas, _viewport);
                    tmp.setStartBug(_startBug);
                    _game.setScreen(tmp);
                    break;
                case LEVEL_TRIGGERED:
                    dispose();
                    //_game.setScreen(new LevelScreen(_game));
                    _game.setScreen(new TransitionScreen(_game, _viewport, _levels.getLastTriggered()));
                    break;
            }
        }

        //Level list navigation stuff.
        if(!_appear.reversed() && _levels.reversed() && !_levels.isRunning()){
            _levels.reverse();
            if(_currentEvent == Event.Type.MENU_LEFT_BUTTON_TRIGGERED)
                _levels.previousPage();
            else if (_currentEvent == Event.Type.MENU_RIGHT_BUTTON_TRIGGERED)
                _levels.nextPage();

            _currentEvent = Event.Type.NONE;
            _levels.startAnimation();
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


        float diamondsx, diamondsy;
        diamondsx = 15 - (1-_appear.get())*200f;
        diamondsy = viewHeight - _diamondRegin.getRegionHeight();
        batch.draw(_diamondRegin, diamondsx, diamondsy);
        diamondsx += _diamondRegin.getRegionWidth()-4;
        diamondsy = diamondsy + (_diamondRegin.getRegionHeight() - _numsRegion[0].getRegionHeight())*0.5f;
        batch.draw(_xRegion, diamondsx, diamondsy);
        diamondsx += _xRegion.getRegionWidth()+2;
        if(Settings.numDiamonds < 10) {
            batch.draw(_numsRegion[Settings.numDiamonds], diamondsx, diamondsy);
        } else if (Settings.numDiamonds < 100){
            batch.draw(_numsRegion[Settings.numDiamonds/10], diamondsx, diamondsy);
            diamondsx += _numsRegion[Settings.numDiamonds/10].getRegionWidth()+2;
            batch.draw(_numsRegion[Settings.numDiamonds%10], diamondsx, diamondsy);
        } else {
            batch.draw(_numsRegion[Settings.numDiamonds/100], diamondsx, diamondsy);
            diamondsx += _numsRegion[Settings.numDiamonds/100].getRegionWidth()+2;
            batch.draw(_numsRegion[(Settings.numDiamonds/10)%10], diamondsx, diamondsy);
            diamondsx += _numsRegion[(Settings.numDiamonds/10)%10].getRegionWidth()+2;
            batch.draw(_numsRegion[Settings.numDiamonds%10], diamondsx, diamondsy);
        }

        //The levels.
        _levels.update(delta);
        _levels.draw(viewWidth, viewHeight);

        if(_popUpShown) {
            drawPopup(batch, delta);
        }

        batch.end();

        if(!LevelSettings.isConfigurationLoaded()) {
            LevelSettings.loadConfiguration();
        }
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
        AudioManager.get().detatch(Event.Type.POPUP);
        AudioManager.get().detatch(Event.Type.CONFIRM);
        AudioManager.get().detatch(Event.Type.REJECT);
        AudioManager.get().detatch(Event.Type.DRAG_GESTURE);


        for(MenuButton b : _buttons)
            b.dispose();
        EventHandler.get().deleteObserver(this);
        _levels.dispose();

        _confirmSound.dispose();
        _confirmButton.dispose();
        _rejectButton.dispose();
    }

    @Override
    public void update(Observable observable, Object o) {
        if(o == null) return;
        if (!(o instanceof Event)) return;

        Event e = (Event) o;
        switch (e.type) {
            case CONFIRM:
                /*Settings.isUnlockedPlayer[_currentPlayerSelected] = true;
                Settings.numDiamonds -= 15;
                EventHandler.get().notifyObservers(new Event(Event.Type.PLAYER_PURSHASE));*/
                Settings.isUnlockedLevel[Settings.levelsUnlocked] = true;
                Settings.levelsUnlocked++;
                Settings.numDiamonds-=10;

                //TODO
            case REJECT:
            case BACK:
                if(_popUpShown) {
                    _popupAppear.reverse();
                    _popupAppear.start();
                    break;
                }
            case BACK_BUTTON_TRIGGERED:
                _currentEvent = Event.Type.BACK_BUTTON_TRIGGERED;
                if(_levels.isRunning()) break;
                _appear.reverse();
                _appear.start(-0.6f);
                _levels.reverse();
                _levels.startAnimation();
                if(!_appearLeft.reversed()) {
                    _appearLeft.reverse();
                    _appearLeft.start();
                }
                if(!_appearRight.reversed()){
                    _appearRight.reverse();
                    _appearRight.start();
                }
                break;
            case MENU_LEFT_BUTTON_TRIGGERED:
                if(_levels.isRunning()) break;
                if(_levels.getCurrentPage() == 0) {
                    break;
                }
                _currentEvent = Event.Type.MENU_LEFT_BUTTON_TRIGGERED;
                _levels.reverse();
                _levels.startAnimation();
                break;
            case MENU_RIGHT_BUTTON_TRIGGERED:
                if(_levels.isRunning()) break;
                if(_levels.getCurrentPage() == _levels.gerNumPages()-1) {
                    break;
                }
                _currentEvent = Event.Type.MENU_RIGHT_BUTTON_TRIGGERED;
                _levels.reverse();
                _levels.startAnimation();
                break;
            case LEVEL_TRIGGERED:
                if(_levels.isRunning()) break;
                _levels.lock();
                _currentEvent = Event.Type.LEVEL_TRIGGERED;
                _appear.reverse();
                _appear.start(-0.6f);
                _levels.reverse();
                _levels.startAnimation(-0.25f);
                if(!_appearLeft.reversed()) {
                    _appearLeft.reverse();
                    _appearLeft.start();
                }
                if(!_appearRight.reversed()){
                    _appearRight.reverse();
                    _appearRight.start();
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
            case POPUP:
                _popUpShown = true;
                _confirmButton.reconsider();
                _rejectButton.reconsider();
                _popupAppear.start();
                for(int i=0; i<_buttons.size(); i++) {
                    _buttons.get(i).freeze();
                }
                _levels.lock();
        }
    }

    private void drawPopup(SpriteBatch batch, float delta) {
        float x, y;
        x = (_viewport.getWorldWidth()-_popupRegion.getRegionWidth())*0.5f;
        y = (_viewport.getWorldHeight() - _popupRegion.getRegionHeight())*0.5f - (1-_popupAppear.get())* 700f;

        _confirmButton.setPosition( x + _confirmButton.getWidth()*0.5f, y-_confirmButton.getHeight()*0.5f);
        _rejectButton.setPosition( x + _popupRegion.getRegionWidth() - _confirmButton.getWidth()*1.5f, y-_confirmButton.getHeight()*0.5f);

        _popupAppear.update(delta);
        _confirmButton.update(delta);
        _rejectButton.update(delta);
        _popupAppear.update(delta);

        batch.draw(_rectPopup, 0, 0, 0, 0, _rectPopup.getRegionWidth(), _rectPopup.getRegionHeight(),
                _viewport.getWorldWidth(), ((float)_viewport.getWorldHeight()/_rectPopup.getRegionHeight())* _popupAppear.get(), 0);

        batch.draw(_popupRegion, x, y);

        _confirmButton.draw(batch);
        _rejectButton.draw(batch);

        if(_popupAppear.reversed() && !_popupAppear.isRunning()) {
            for(int i=0; i< _buttons.size(); i++) {
                _buttons.get(i).unfreeze();
            }
            _levels.unlock();
            _popUpShown = false;
            _confirmButton.ignore();
            _rejectButton.ignore();
            _popupAppear.reverse();
        }
    }

    public void setStartBug(float start) {_startBug = start;}
}


class Levels implements Observer, Disposable
{
    private int _currentPage = Settings.menuCurrentPage;
    private final int LEVELS_PER_PAGES = 8;
    private int _numPages;
    private SpriteBatch _batch;
    private TextureAtlas _atlas;
    private TextureAtlas.AtlasRegion _region;
    private TextureAtlas.AtlasRegion[] _nums = new TextureAtlas.AtlasRegion[10];
    private int _lastTriggered = 0;

    private TextureAtlas.AtlasRegion _diamondRegin;
    private TextureAtlas.AtlasRegion[] _numsRegion = new  TextureAtlas.AtlasRegion[2];
    private TextureAtlas.AtlasRegion _xRegion;
    private TextureAtlas.AtlasRegion _preunlockRegion;

    private ToggleAngle _toggle = new ToggleAngle();
    private int _toggled = 0;

    public class Rect {public float x, y, w, h;}

    private Rect[] _levels = new Rect[LEVELS_PER_PAGES];
    private Appear[] _anim = new Appear[LEVELS_PER_PAGES];

    private Viewport _viewport;

    private boolean _locked = false;

    private TextureAtlas.AtlasRegion _checkMarkRegion;
    private TextureAtlas.AtlasRegion _diamondMarkRegion;
    private TextureAtlas.AtlasRegion _lockedRegion;

    public int gerNumPages() {return _numPages;}


    void lock() {
        _locked = true;
    }

    void unlock() {
        _locked = false;
    }

    boolean isLocked() {
        return _locked;
    }

    public Levels(SpriteBatch batch, TextureAtlas a, Viewport view) {
        _viewport = view;
        _numPages = (int)Math.ceil((double)Settings.NUM_LEVELS / (double)LEVELS_PER_PAGES);
        _batch = batch;
        _atlas = a;
        _region = a.findRegion("insect");
        for(int i=0; i<LEVELS_PER_PAGES; i++) {
            _levels[i] = new Rect();
            _anim[i] = new Appear();
        }
        for(int i=0; i<10; i++) {
            _nums[i] = _atlas.findRegion(Integer.toString(i));
        }

        _checkMarkRegion = _atlas.findRegion("clearIcon");
        _diamondMarkRegion = _atlas.findRegion("diamondIcon");
        _lockedRegion = _atlas.findRegion("lockedIcon");

        _diamondRegin = _atlas.findRegion("diamondlittle");
        for(int i=0; i<2; i++) {
            _numsRegion[i] = _atlas.findRegion("d"+i + "White");
        }
        _xRegion = _atlas.findRegion("dxWhite");
        _preunlockRegion = _atlas.findRegion("preunlock");


        EventHandler.get().addObserver(this);
    }

    @Override
    public void dispose() {
        EventHandler.get().deleteObserver(this);
    }

    public void toggle(int i) {
        _toggled = i;
        _toggle.start();
    }

    @Override
    public void update(Observable observable, Object o) {

        if(_locked) return;

        if(o == null) return;
        if (!(o instanceof Event)) return;

        Event e = (Event) o;

        Vector3 tp = new Vector3();

        if(e.type == Event.Type.TOUCH && e.info.touchState.button == Input.Buttons.LEFT) {
            _viewport.getCamera().unproject(tp.set(e.info.touchState.screenX , e.info.touchState.screenY, 0));

            int p = GetClic(tp);
            if(p != 0)
            {
                if(e.info.touchState.isDown == false)
                {
                    //added recently to avoid entering menu while dragging not sure if it works.
                    if(isRunning()) return;
                    if(InputHandler.get().recentlyDragged()) return;

                    Event ev = new Event();
                    ev.type = Event.Type.LEVEL_TRIGGERED;
                    toggle(p);
                    if(Settings.isUnlockedLevel[p-1]) {
                        _lastTriggered = ev.info.level = p;
                        if(!Settings.isSilent)
                            Settings.button2SFX.play();
                        EventHandler.get().notifyObservers(ev);
                    } else {

                        if(!Settings.isUnlockedLevel[p-1]
                                && (p-2) >= 0
                                && Settings.isUnlockedLevel[p-2]
                                && !Settings.isWonLevel[p-2]
                                && Settings.numDiamonds >= 10
                                ) {
                            ev.type = Event.Type.POPUP;
                            EventHandler.get().notifyObservers(ev);
                        } else {

                            if (!Settings.isSilent)
                                Settings.buttonErrorSFX.play();
                        }
                    }
                }
            }
        }
    }


    int GetClic(Vector3 v) {
        for(int i=0; i<LEVELS_PER_PAGES; i++) {
            float offset = _levels[i].w * 0.18f;
            if( (v.x >= _levels[i].x + offset) &&
                    (v.x <= _levels[i].x + _levels[i].w - offset) &&
                    (v.y >= _levels[i].y) &&
                    (v.y <= _levels[i].y + _levels[i].h))
            {
                return i+_currentPage*8+1;
            }
        }
        return 0;
    }

    public void startAnimation(float time) {
        for(int i=0; i<LEVELS_PER_PAGES; i++) {
            _anim[i].start(-i*0.03f+time);
        }
    }


    public void startAnimation() {
        startAnimation(0);
    }

    private void updateRects(float w, float h) {
        for(int i= 0; i < LEVELS_PER_PAGES; i++) {
            _levels[i].x=-1;
            _levels[i].y=-1;
            _levels[i].w=-1;
            _levels[i].h=-1;
        }
        float padding = (w - (LEVELS_PER_PAGES/2) * _region.getRegionWidth()) / (LEVELS_PER_PAGES-1);

        for(int i= 0; _currentPage*8+i < Settings.NUM_LEVELS && i<LEVELS_PER_PAGES; i++) {
            _levels[i].x = 2*padding + (i%4)*padding + (i%4)*_region.getRegionWidth();
            _levels[i].y = h * 0.5f -(i/4)* (padding + _region.getRegionHeight())
                    -((_anim[i].reversed())?-1:1)*(1-_anim[i].get())*h;
            _levels[i].w = _region.getRegionWidth();
            _levels[i].h = _region.getRegionHeight();
        }
    }

    public int getLastTriggered() {
        return _lastTriggered;
    }

    public boolean isRunning() {
        for(int i=0; i<LEVELS_PER_PAGES; i++)
            if(_anim[i].isRunning()) return true;
        return false;
    }
    public int getCurrentPage() {return _currentPage;}
    public void nextPage() {if(_currentPage+1 < _numPages) {_currentPage++; Settings.menuCurrentPage=_currentPage;}}
    public void previousPage(){if(_currentPage > 0) {_currentPage--;Settings.menuCurrentPage=_currentPage;}}
    public Rect get(int i) {
        return _levels[i];
    }
    public void update(float delta) {
        for (int i = 0; i < LEVELS_PER_PAGES; i++) {
            _anim[i].update(delta);
        }
        _toggle.update(delta);
    }
    public int numPages(){return _numPages;}
    public void reverse() {
        for(int i=0; i< LEVELS_PER_PAGES; i++)
            _anim[i].reverse();
    }
    public boolean reversed(){return _anim[0].reversed();}
    public void draw(float w, float h) {

        updateRects(w, h);
        for(int i= 0; _currentPage*8+i < Settings.NUM_LEVELS && i<8; i++) {
            Color c = _batch.getColor();

            if(!Settings.isUnlockedLevel[i+_currentPage*8]) {
                _batch.setColor(Color.DARK_GRAY);
            }
            float angle = 1;
            if(_toggled == i+_currentPage*8+1 && Settings.isUnlockedLevel[_toggled-1]) angle = _toggle.get();
            _batch.draw(_region, _levels[i].x, _levels[i].y,_region.getRegionWidth()*0.5f, _region.getRegionHeight()*0.5f,
                    _region.getRegionWidth(), _region.getRegionHeight(), 1.f, 1.f, angle*20-20);
            if(i+_currentPage*8+1 <= 9) {
                int j = i+_currentPage*8+1;
                _batch.draw(_nums[j],
                        _levels[i].x + _levels[i].w * 0.5f - _nums[j].getRegionWidth() * 0.5f,
                        _levels[i].y + _levels[i].h * 0.5f - _nums[j].getRegionHeight() * 0.5f,
                        _nums[j].getRegionWidth()*0.5f, _nums[j].getRegionHeight()*0.5f, _nums[j].getRegionWidth(),
                        _nums[j].getRegionHeight(), 1.f, 1.f, angle*20-20
                );
            }
            else {
                int j = i+_currentPage*8+1;
                int k = j % 10;
                int l = j / 10;
                float width = _nums[k].getRegionWidth()*0.5f+_nums[l].getRegionWidth()*0.5f+5;
                _batch.draw(_nums[k],
                        _levels[i].x+_levels[i].w*0.5f-width+_nums[l].getRegionWidth()+5,
                        _levels[i].y+_levels[i].h*0.5f-_nums[k].getRegionHeight()*0.5f,
                        _nums[k].getRegionWidth()*0.5f, _nums[k].getRegionHeight()*0.5f, _nums[k].getRegionWidth(),
                        _nums[k].getRegionHeight(), 1.f, 1.f, angle*20-20
                );
                _batch.draw(_nums[l],
                        _levels[i].x +_levels[i].w*0.5f-width,
                        _levels[i].y +_levels[i].h*0.5f-_nums[l].getRegionHeight()*0.5f,
                        _nums[l].getRegionWidth()*0.5f, _nums[l].getRegionHeight()*0.5f, _nums[l].getRegionWidth(),
                        _nums[l].getRegionHeight(), 1.f, 1.f, angle*20-20
                );
            }

            if(Settings.isWonLevel[i+_currentPage*8]) {
                _batch.draw(_checkMarkRegion, _levels[i].x+_region.getRegionWidth()-_checkMarkRegion.getRegionWidth()*1.5f, _levels[i].y);
            }

            if(Settings.isLevelDiamondTaken[i+_currentPage*8]) {
                _batch.draw(_diamondMarkRegion, _levels[i].x+_diamondMarkRegion.getRegionWidth()*0.5f, _levels[i].y);
            }

            _batch.setColor(c);

            if(!Settings.isUnlockedLevel[i+_currentPage*8]) {
                _batch.draw(_lockedRegion, _levels[i].x+_region.getRegionWidth()-_lockedRegion.getRegionWidth()*1.5f, _levels[i].y);
            }

            if(!Settings.isUnlockedLevel[i+_currentPage*8]
                    && (i+_currentPage*8-1) >= 0
                    && Settings.isUnlockedLevel[i+_currentPage*8-1]
                    && !Settings.isWonLevel[i+_currentPage*8-1]) {

                float diamondsx, diamondsy;
                diamondsx = _levels[i].x + (_levels[i].w - _preunlockRegion.getRegionWidth())*0.5f ;
                diamondsy = _levels[i].y + _diamondRegin.getRegionHeight() + _preunlockRegion.getRegionHeight();
                _batch.draw(_preunlockRegion, diamondsx, diamondsy);
                diamondsx = _levels[i].x + (_levels[i].w - _diamondRegin.getRegionWidth() - _xRegion.getRegionWidth()*3)*0.5f ;
                diamondsy -= _diamondRegin.getRegionHeight();
                _batch.draw(_diamondRegin, diamondsx, diamondsy);
                diamondsx += _diamondRegin.getRegionWidth()-4;
                diamondsy = diamondsy + (_diamondRegin.getRegionHeight() - _numsRegion[0].getRegionHeight())*0.5f;
                _batch.draw(_xRegion, diamondsx, diamondsy);
                diamondsx += _xRegion.getRegionWidth()+2;
                _batch.draw(_numsRegion[1], diamondsx, diamondsy);
                diamondsx += _numsRegion[1].getRegionWidth()+2;
                _batch.draw(_numsRegion[0], diamondsx, diamondsy);
                //diamondsx += _numsRegion[0].getRegionWidth()+2;

            }

        }
    }
}
