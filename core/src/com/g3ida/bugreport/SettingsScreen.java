package com.g3ida.bugreport;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

/**
 * Created by g3ida on 1/15/2018.
 */

public class SettingsScreen implements Screen, Observer {

    private TextureAtlas _atlas;
    private TextureAtlas _atlasAds;

    private Vector<MenuButton> _buttons = new Vector<MenuButton>();

    private Appear _appear;
    private ToggleAngle _toggle;

    Vector2 _adsAvailableDotPos = new Vector2();
    TextureAtlas.AtlasRegion _adsAvailableDot;
    boolean _adsAvailable = false;
    float _lastAdsCheck = 0.0f;

    private boolean _popUpShown = false;
    private boolean _popUpAdsShown = false;
    private TextureAtlas.AtlasRegion _popupRegion;
    private TextureAtlas.AtlasRegion _rectPopup;
    private MenuButton _confirmButton;
    private MenuButton _rejectButton;
    private Appear _popupAppear = new Appear();
    private Appear _popupAdsAppear = new Appear();
    private TextureAtlas.AtlasRegion _popupAdsRegion;
    private MenuButton _okButton;

    private OrthographicCamera _menuCam;
    private Viewport _viewport;

    private TextureAtlas.AtlasRegion _bugImg;
    private float _startBug;

    private BugReport _game;
    private Event.Type _currentEvent = Event.Type.NONE;

    private TextureAtlas.AtlasRegion _playerBoxRegion;
    private TextureAtlas.AtlasRegion _unlockPlayer;
    private TextureAtlas.AtlasRegion[] _numsRegion = new  TextureAtlas.AtlasRegion[10];
    private TextureAtlas.AtlasRegion _xRegion;
    private TextureAtlas.AtlasRegion _diamondRegin;

    private Vector<TextureRegion> _playerSprites = new Vector<TextureRegion>();

    private int _currentPlayerSelected = Settings.currentPlayer;
    private float _playerSelectX = 0f;
    private float _playerSelectY = 0f;

    private Sound _changePlayerSound;

    public SettingsScreen(BugReport game, TextureAtlas atlas, Viewport viewport) {

        _viewport = viewport;
        _menuCam = (OrthographicCamera) _viewport.getCamera();
        _game = game;

        _atlas = atlas;

        _buttons.add(new MenuButton(_atlas.findRegion("options2"), Event.Type.NONE, _viewport));
        _buttons.add(new MenuButton( _atlas.findRegion(((Settings.isSilent) ? "soundoff" : "sound")), Event.Type.SOUND_BUTTON_TRIGGERED, _viewport));

        _buttons.add(new MenuButton(_atlas.findRegion("watchad"), Event.Type.WATCH_AD, _viewport));
        _buttons.add(new MenuButton(_atlas.findRegion("buy"), Event.Type.NONE, _viewport));

        _buttons.add(new MenuButton( _atlas.findRegion("donate2"), Event.Type.NONE, _viewport));

        _buttons.add(new MenuButton( _atlas.findRegion("credits"), Event.Type.CREDITS_BUTTON_TRIGGERED, _viewport));
        _buttons.add(new MenuButton(_atlas.findRegion("achievments"), Event.Type.ACHIEVEMENTS_BUTTON_TRIGGERED, _viewport));
        _buttons.add(new MenuButton(_atlas.findRegion("rate"), Event.Type.NONE, _viewport));
        _buttons.add(new MenuButton(_atlas.findRegion("left2"), Event.Type.PLAYER_CHANGE_LEFT, _viewport));
        _buttons.add(new MenuButton(_atlas.findRegion("right2"), Event.Type.PLAYER_CHANGE_RIGHT, _viewport));

        _buttons.add(new MenuButton(_atlas.findRegion("back"), Event.Type.BACK_BUTTON_TRIGGERED, _viewport));

        _buttons.get(8).setShake(false);
        _buttons.get(9).setShake(false);

        for(int i=0; i<Settings.NUM_PLAYERS; i++) {
            _playerSprites.add(_atlas.findRegion("p"+i));
        }

        _playerBoxRegion = _atlas.findRegion("boxPlayer");
        _unlockPlayer = _atlas.findRegion("unlock");

        //diamonds print stuff
        for(int i=0; i<10; i++) {
            _numsRegion[i] = _atlas.findRegion("d"+i);
        }
        _xRegion = _atlas.findRegion("dx");
        _diamondRegin = _atlas.findRegion("diamond");

        //popup stuff
        _popupRegion = _atlas.findRegion("confirm");
        _confirmButton = new MenuButton(_atlas.findRegion("yes"), Event.Type.CONFIRM, _viewport);
        _rejectButton = new MenuButton(_atlas.findRegion("no"), Event.Type.REJECT, _viewport);
        _rectPopup = atlas.findRegion("rect");
        _confirmButton.ignore();
        _rejectButton.ignore();

        _atlasAds = new TextureAtlas("data/graphics/ads.pack");
        _popupAdsRegion = _atlasAds.findRegion("noads");
        _okButton = new MenuButton(_atlasAds.findRegion("ok"), Event.Type.OK, _viewport);
        _okButton.ignore();
        _adsAvailableDot = _atlasAds.findRegion("pt");

        _bugImg =  _atlas.findRegion("bug");
        _startBug = -_bugImg.getRegionHeight() * 1.3f;

        _appear = new Appear();
        _appear.start();
        _toggle = new ToggleAngle();

        AudioManager.get().attachSound(Settings.shakeSFX, Event.Type.MENU_BUTTON_SHAKE);
        AudioManager.get().attachSound(Settings.cancelSFX, Event.Type.BACK_BUTTON_TRIGGERED);
        AudioManager.get().attachSound(Settings.cancelSFX, Event.Type.REJECT);

        AudioManager.get().attachSound(Settings.button2SFX, Event.Type.ACHIEVEMENTS_BUTTON_TRIGGERED);
        AudioManager.get().attachSound(Settings.button2SFX, Event.Type.CREDITS_BUTTON_TRIGGERED);


        _changePlayerSound = Gdx.audio.newSound(Gdx.files.internal("data/sounds/button3.ogg"));
        AudioManager.get().attachSound(_changePlayerSound, Event.Type.PLAYER_CHANGE_LEFT);
        AudioManager.get().attachSound(_changePlayerSound, Event.Type.PLAYER_CHANGE_RIGHT);
        AudioManager.get().attachSound(Settings.buttonSFX, Event.Type.POPUP);
        AudioManager.get().attachSound(_changePlayerSound, Event.Type.CONFIRM);
        AudioManager.get().attachSound(_changePlayerSound, Event.Type.OK);

        EventHandler.get().addObserver(this);
    }

    @Override
    public void show() {
        _adsAvailable = _game.hasVideoReward();
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
        _toggle.update(delta);

        if(!_appear.isRunning()) {
            switch (_currentEvent) {
                case BACK:
                case BACK_BUTTON_TRIGGERED:
                    dispose();
                    MenuScreen tmp = new MenuScreen(_game, _atlas, _viewport);
                    tmp.setStartBug(_startBug);
                    _game.setScreen(tmp);
                    break;

                case CREDITS_BUTTON_TRIGGERED:
                    dispose();
                    CreditsScreen cs = new CreditsScreen(_game, _atlas, _viewport);
                    cs.setStartBug(_startBug);
                    _game.setScreen(cs);
                    break;

                case ACHIEVEMENTS_BUTTON_TRIGGERED:
                    dispose();
                    AchievementsScreen as = new AchievementsScreen(_game, _atlas, _viewport);
                    as.setStartBug(_startBug);
                    _game.setScreen(as);
                    break;
            }
        }

        _buttons.get(0).setPosition((viewWidth - _buttons.get(0).getWidth())*0.5f, viewHeight - 1.5f*_buttons.get(0).getHeight()*(_appear.get()));
        _buttons.get(1).setPosition(viewWidth*0.98f - _buttons.get(1).getWidth()+(viewWidth*0.02f+_buttons.get(1).getWidth())*(1-_appear.get()), viewHeight*0.98f - _buttons.get(1).getHeight());
        _buttons.get(10).setPosition(viewWidth*0.02f-(viewWidth*0.02f+_buttons.get(0).getWidth())*(1-_appear.get()), viewHeight*0.05f);


        float offsetX, offsetY, spaceX, spaceY, x, y;

        spaceX = spaceY = _buttons.get(2).getWidth() - _buttons.get(5).getWidth() - _playerBoxRegion.getRegionWidth();
        offsetX = (viewWidth - _buttons.get(2).getWidth() - _buttons.get(3).getWidth() - spaceX)*0.5f;
        offsetY = (viewHeight*0.5f + _buttons.get(2).getHeight()*0.5f);

        spaceY += (1-_appear.get())*viewHeight*0.5f;
        x = offsetX;
        y = offsetY - (1-_appear.get())*viewHeight;
        _buttons.get(2).setPosition(x, y);

        x +=_buttons.get(2).getWidth() + spaceX;
        _buttons.get(3).setPosition(x, y);

        _adsAvailableDotPos.x = x - spaceX - _adsAvailableDot.getRegionWidth()*0.5f;
        _adsAvailableDotPos.y = y + _buttons.get(2).getHeight()-_adsAvailableDot.getRegionHeight()*0.5f;
        x -= _buttons.get(2).getWidth() + spaceX;
        y -= _buttons.get(2).getHeight() + spaceY;

        //credits
        _buttons.get(5).setPosition(x, y);

        y -= _buttons.get(2).getHeight() + spaceY;
        //back
        _buttons.get(4).setPosition(x, y);

        x += _buttons.get(2).getWidth() + spaceX;
        //rate button
        _buttons.get(7).setPosition(x, y);

        y += _buttons.get(7).getHeight()+spaceY;
        //achievments button
        _buttons.get(6).setPosition(x, y);


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


        y -= _buttons.get(7).getHeight()+spaceY;
        x -= _buttons.get(2).getWidth();
        x += _buttons.get(4).getWidth();
        //playerSpace
        float tmpx, tmpy;
        tmpx = x; tmpy = y;
        _playerSelectX = x;
        _playerSelectY = y;
        batch.draw(_playerBoxRegion, x, y,
                _playerBoxRegion.getRegionWidth()*0.5f, _playerBoxRegion.getRegionHeight()*0.5f,
                (float)_playerBoxRegion.getRegionWidth(), (float)_playerBoxRegion.getRegionHeight(),
                1f, 1f,
                (1-_toggle.get())*20.f);

        y += (_playerBoxRegion.getRegionHeight() - _buttons.get(8).getHeight())*0.5f;
        float off = (_playerBoxRegion.getRegionWidth() - _buttons.get(8).getWidth()*2 -
                _playerSprites.get(_currentPlayerSelected).getRegionWidth())/4.f;
        x+= off;
        _buttons.get(8).setPosition(x, y);
        x+= _buttons.get(8).getWidth() + 2*off + _playerSprites.get(_currentPlayerSelected).getRegionWidth();
        _buttons.get(9).setPosition(x, y);

        y += _buttons.get(8).getHeight()*0.5f - _playerSprites.get(_currentPlayerSelected).getRegionHeight()*0.5f;
        x = x - _playerSprites.get(_currentPlayerSelected).getRegionWidth() - off;
        batch.draw(_playerSprites.get(_currentPlayerSelected), x, y);

        if(Settings.isUnlockedPlayer[_currentPlayerSelected] == false) {

            x = tmpx + (_playerBoxRegion.getRegionWidth() - _unlockPlayer.getRegionWidth()) * 0.5f;
            y = tmpy + _playerBoxRegion.getRegionHeight() * 0.5f - _unlockPlayer.getRegionHeight();
            batch.draw(_unlockPlayer, x, y);

        }

        for (MenuButton b : _buttons) {
            b.update(delta);
            b.draw(batch);
        }

        _lastAdsCheck += delta;
        if(_lastAdsCheck > 1.2f) {
            _adsAvailable = _game.hasVideoReward();
            _lastAdsCheck = 0.f;
        }

        if(_adsAvailable) {
            batch.draw(_adsAvailableDot, _adsAvailableDotPos.x, _adsAvailableDotPos.y);
        }

        drawHUD(batch);

        if(_popUpShown) {
            drawPopup(batch, delta);
        }

        if(_popUpAdsShown) {
            drawPopupAds(batch, delta);
        }


        batch.end();
    }

    public void drawPopup(SpriteBatch batch, float delta) {
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
            _popUpShown = false;
            _confirmButton.ignore();
            _rejectButton.ignore();
            _popupAppear.reverse();
        }


    }

    public void drawPopupAds(SpriteBatch batch, float delta) {
        float x, y;
        x = (_viewport.getWorldWidth()-_popupRegion.getRegionWidth())*0.5f;
        y = (_viewport.getWorldHeight() - _popupRegion.getRegionHeight())*0.5f - (1-_popupAdsAppear.get())* 700f;

        _okButton.setPosition( x + _popupRegion.getRegionWidth()*0.5f - _okButton.getWidth()*0.5f, y-_okButton.getHeight()*0.5f);

        _popupAdsAppear.update(2*delta);
        _okButton.update(delta);

        batch.draw(_rectPopup, 0, 0, 0, 0, _rectPopup.getRegionWidth(), _rectPopup.getRegionHeight(),
                _viewport.getWorldWidth(), ((float)_viewport.getWorldHeight()/_rectPopup.getRegionHeight())* _popupAdsAppear.get(), 0);

        batch.draw(_popupAdsRegion, x, y);

        _okButton.draw(batch);

        if(_popupAdsAppear.reversed() && !_popupAdsAppear.isRunning()) {
            for(int i=0; i< _buttons.size(); i++) {
                _buttons.get(i).unfreeze();
            }
            _popUpAdsShown = false;
            _okButton.ignore();
            _popupAdsAppear.reverse();
        }
    }

    public void drawHUD(SpriteBatch batch) {

        float diamondsx, diamondsy;
        diamondsx = 15 - (1-_appear.get())*200f;
        diamondsy = _viewport.getWorldHeight() - _diamondRegin.getRegionHeight();
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

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        AudioManager.get().detatch(Event.Type.MENU_BUTTON_SHAKE);
        AudioManager.get().detatch(Event.Type.BACK_BUTTON_TRIGGERED);
        AudioManager.get().detatch(Event.Type.PLAYER_CHANGE_RIGHT);
        AudioManager.get().detatch(Event.Type.PLAYER_CHANGE_LEFT);
        AudioManager.get().detatch(Event.Type.REJECT);
        AudioManager.get().detatch(Event.Type.POPUP);
        AudioManager.get().detatch(Event.Type.CONFIRM);
        AudioManager.get().detatch(Event.Type.OK);
        AudioManager.get().detatch(Event.Type.ACHIEVEMENTS_BUTTON_TRIGGERED);
        AudioManager.get().detatch(Event.Type.CREDITS_BUTTON_TRIGGERED);

        _changePlayerSound.dispose();

        _confirmButton.dispose();
        _rejectButton.dispose();
        _okButton.dispose();
        _atlasAds.dispose();

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
            case OK:
                if(_popUpAdsShown) {
                    _popupAdsAppear.reverse();
                    _popupAdsAppear.start();
                }
                break;
            case CONFIRM:
                Settings.isUnlockedPlayer[_currentPlayerSelected] = true;
                Settings.numDiamonds -= 15;
                if(Settings.isUnlockedPlayer[_currentPlayerSelected]) {
                    Settings.currentPlayer = _currentPlayerSelected;
                }
                Settings.save();
                EventHandler.get().notifyObservers(new Event(Event.Type.PLAYER_PURSHASE));
            case REJECT:
            case BACK:
                if(_popUpAdsShown) {
                    _popupAdsAppear.reverse();
                    _popupAdsAppear.start();
                    break;
                }
                if(_popUpShown) {
                    _popupAppear.reverse();
                    _popupAppear.start();
                    break;
                }
            case BACK_BUTTON_TRIGGERED:
                if(Settings.isUnlockedPlayer[_currentPlayerSelected] == false) {
                    _toggle.start();
                    break;
                }
            case CREDITS_BUTTON_TRIGGERED:
            case ACHIEVEMENTS_BUTTON_TRIGGERED:
                _currentEvent = e.type;
                _appear.reverse();
                _appear.start(-0.6f);
                break;
            case SOUND_BUTTON_TRIGGERED:
                Settings.isSilent = !Settings.isSilent;
                if(Settings.isSilent)
                    _buttons.get(1).changeRegion(_atlas.findRegion("soundoff"));
                else {
                    _buttons.get(1).changeRegion(_atlas.findRegion("sound"));
                    Settings.button2SFX.play();
                }
                break;
            case PLAYER_CHANGE_LEFT:
                _currentPlayerSelected--;
                if(_currentPlayerSelected<0) _currentPlayerSelected = Settings.NUM_PLAYERS - 1;
                if(Settings.isUnlockedPlayer[_currentPlayerSelected]) {
                    Settings.currentPlayer = _currentPlayerSelected;
                }
                break;
            case PLAYER_CHANGE_RIGHT:
                _currentPlayerSelected++;
                if(_currentPlayerSelected>=Settings.NUM_PLAYERS) _currentPlayerSelected = 0;
                if(Settings.isUnlockedPlayer[_currentPlayerSelected]) {
                    Settings.currentPlayer = _currentPlayerSelected;
                }
                break;
            case WATCH_AD:
                if(!_game.hasVideoReward()) {
                    _adsAvailable = false;
                    _popUpAdsShown = true;
                    _popupAdsAppear.start(-0.1f);
                    for(int i=0; i<_buttons.size(); i++) {
                        _buttons.get(i).freeze();
                    }
                    EventHandler.get().notifyObservers(new Event(Event.Type.POPUP));
                    _okButton.reconsider();
                    _game.showVideoAd();
                } else {
                    Settings.button2SFX.play();
                    _game.showVideoAd();
                }
                break;
            case TOUCH:
                if(e.info.touchState.isDown) {
                    Vector3 tp = new Vector3();
                    _viewport.getCamera().unproject(tp.set(e.info.touchState.screenX , e.info.touchState.screenY, 0));
                    if((tp.x >= _playerSelectX) &&
                            (tp.x<= _playerSelectX + _playerBoxRegion.getRegionWidth()) &&
                            (tp.y >= _playerSelectY) &&
                            (tp.y <= _playerSelectY + _playerBoxRegion.getRegionHeight()))
                    {
                        if(_popUpAdsShown) break;
                        if(!_buttons.get(8).isClicInside(tp.x, tp.y)
                                && !_buttons.get(9).isClicInside(tp.x, tp.y)
                                && !Settings.isUnlockedPlayer[_currentPlayerSelected]
                                && Settings.numDiamonds >= 15
                                && ! _popUpShown) {
                            EventHandler.get().notifyObservers(new Event(Event.Type.POPUP));
                            _toggle.start();
                            _popUpShown = true;
                            _confirmButton.reconsider();
                            _rejectButton.reconsider();
                            _popupAppear.start();
                            for(int i=0; i<_buttons.size(); i++) {
                                _buttons.get(i).freeze();
                            }
                        } else if (!_buttons.get(8).isClicInside(tp.x, tp.y)
                                && !_buttons.get(9).isClicInside(tp.x, tp.y)
                                && !Settings.isUnlockedPlayer[_currentPlayerSelected]
                                && Settings.numDiamonds < 15
                                && ! _popUpShown){
                            Settings.buttonErrorSFX.play();

                        }
                    }
                }
        }
    }

    public void setStartBug(float start) {_startBug = start;}
}
