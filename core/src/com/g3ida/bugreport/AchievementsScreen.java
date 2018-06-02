package com.g3ida.bugreport;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.Vector;

/**
 * Created by g3ida on 1/16/2018.
 */

public class AchievementsScreen implements Screen, Observer {

    private TextureAtlas _atlas;
    private Vector<MenuButton> _buttons = new Vector<MenuButton>();

    private TextureAtlas _achievmentsAtlas;

    private Appear _appear;
    private Appear _appearText;

    private OrthographicCamera _menuCam;
    private Viewport _viewport;

    private TextureAtlas.AtlasRegion _bugImg;
    private float _startBug;

    private BugReport _game;
    private Event.Type _currentEvent = Event.Type.NONE;

    private BitmapFont _font;
    private String _text = new String("");

    private int[] space = new int[130];

    private String s1;
    private String s2;
    private String s3;
    private String s4;
    private String s5;
    private String s6;
    private String s7;
    private String s8;
    private String s9;

    private int _currentPage = 0;
    private final int ACHIEVEMENTS_PER_PAGE = 4;
    private int _numPages = (int)Math.ceil(((float)Settings.NUM_ACHIEVEMENTS / ACHIEVEMENTS_PER_PAGE)) + 1;
    private TextureAtlas.AtlasRegion[] _achievementsRegions = new TextureAtlas.AtlasRegion[Settings.NUM_ACHIEVEMENTS];

    private Appear[] _anim = new Appear[ACHIEVEMENTS_PER_PAGE];

    public AchievementsScreen(BugReport game, TextureAtlas atlas, Viewport viewport) {

        _viewport = viewport;
        _menuCam = (OrthographicCamera) _viewport.getCamera();
        _game = game;

        //_achievmentsAtlas = new TextureAtlas(Gdx.files.internal("data/graphics/achievements.pack"));
         _achievmentsAtlas = AchievementSystem.get().getAtlas();

        _atlas = atlas;
        _buttons.add(new MenuButton(_achievmentsAtlas.findRegion("achiev"), Event.Type.NONE, _viewport));
        _buttons.add(new MenuButton( _atlas.findRegion("back"), Event.Type.BACK_BUTTON_TRIGGERED, _viewport));
        _buttons.add(new MenuButton( _atlas.findRegion(((Settings.isSilent) ? "soundoff" : "sound")), Event.Type.SOUND_BUTTON_TRIGGERED, _viewport));
        _buttons.add(new MenuButton( _achievmentsAtlas.findRegion("next"), Event.Type.MENU_RIGHT_BUTTON_TRIGGERED, _viewport));

        _bugImg =  _atlas.findRegion("bug");
        _startBug = -_bugImg.getRegionHeight() * 1.3f;

        _appear = new Appear();
        _appear.start();
        _appearText = new Appear();
        _appearText.start();

        for(int i=0; i<Settings.NUM_ACHIEVEMENTS; i++) {
            _achievementsRegions[i] = _achievmentsAtlas.findRegion("" + (i+ 1));
        }

        AudioManager.get().attachSound(Settings.shakeSFX, Event.Type.MENU_BUTTON_SHAKE);
        AudioManager.get().attachSound(Settings.cancelSFX, Event.Type.BACK_BUTTON_TRIGGERED);
        AudioManager.get().attachSound(Settings.shakeSFX, Event.Type.DRAG_GESTURE);

        _font = new BitmapFont(Gdx.files.internal("data/fonts/achievmentspatrick.fnt"),Gdx.files.internal("data/fonts/achievmentspatrick.png"),false);

        EventHandler.get().addObserver(this);

        //this mess is done to see how much points to add :3
        initCharSpace();

        s1 = fillWithCharUntilValue('.', "total time played", 1000);
        s2 = fillWithCharUntilValue('.', "game progress", 1000);
        s3 = fillWithCharUntilValue('.', "levels cleared", 1000);
        s4 = fillWithCharUntilValue('.', "diamonds collected", 1000);
        s5 = fillWithCharUntilValue('.', "players unlocked", 1000);
        s6 = fillWithCharUntilValue('.', "total jumps", 1000);
        s7 = fillWithCharUntilValue('.', "total deaths", 1000);
        s8 = fillWithCharUntilValue('.', "total smilies triggered", 1000);
        s9 = fillWithCharUntilValue('.', "restarts performed", 1000);

        for(int i=0; i<ACHIEVEMENTS_PER_PAGE; i++) {
            _anim[i] = new Appear();
        }
    }

    @Override
    public void show() {}

    private void startAnimation(float time) {
        for(int i=0; i<ACHIEVEMENTS_PER_PAGE; i++) {
            _anim[i].start(-i*0.03f+time);
        }
    }

    private void startAnimation() {
        startAnimation(0);
    }

    private void reverseAnim() {
        for(int i=0; i< ACHIEVEMENTS_PER_PAGE; i++)
            _anim[i].reverse();
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
        _appearText.update(delta);

        _buttons.get(0).setPosition((viewWidth - _buttons.get(0).getWidth())*0.5f, viewHeight - 1.5f*_buttons.get(0).getHeight()*(_appear.get()));
        _buttons.get(1).setPosition(viewWidth * 0.02f - (viewWidth * 0.02f + _buttons.get(0).getWidth()) * (1 - _appear.get()), viewHeight * 0.05f);
        _buttons.get(2).setPosition(viewWidth*0.98f - _buttons.get(2).getWidth()+(viewWidth*0.02f+_buttons.get(2).getWidth())*(1-_appear.get()), viewHeight*0.98f - _buttons.get(2).getHeight());
        _buttons.get(3).setPosition(viewWidth*0.98f - _buttons.get(3).getWidth()+(viewWidth*0.02f+_buttons.get(3).getWidth())*(1-_appear.get()), viewHeight*0.05f);

        if(_currentPage != 0) {
            _buttons.get(1).setEvent(Event.Type.MENU_LEFT_BUTTON_TRIGGERED);
            if(_currentPage == _numPages-1) {
                _buttons.get(3).ignore();
            } else {
                _buttons.get(3).reconsider();
            }
        } else {
            _buttons.get(1).setEvent(Event.Type.BACK_BUTTON_TRIGGERED);
        }

        if(!_appear.isRunning() &&!_appearText.isRunning()) {
            switch (_currentEvent) {
                case BACK:
                case BACK_BUTTON_TRIGGERED:
                    dispose();
                    SettingsScreen tmp = new SettingsScreen(_game, _atlas, _viewport);
                    tmp.setStartBug(_startBug);
                    _game.setScreen(tmp);
                    break;
                case MENU_LEFT_BUTTON_TRIGGERED:
                    if(_currentPage == 0) break;
                    if(!_anim[ACHIEVEMENTS_PER_PAGE-1].isRunning()) {
                        _currentPage--;
                        if(_currentPage == 0) {
                            _appearText.reverse();
                            _appearText.start(-0.1f);
                        }
                            reverseAnim();
                            startAnimation();

                        _currentEvent = Event.Type.NONE;
                    }
                    break;
                case MENU_RIGHT_BUTTON_TRIGGERED:
                    if(!_anim[ACHIEVEMENTS_PER_PAGE-1].isRunning() && ! _appearText.isRunning() &&  _currentPage+1 < _numPages) {
                        _currentPage++;
                        reverseAnim();
                        startAnimation();
                        _currentEvent = Event.Type.NONE;
                    }
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

        for (int i = 0; i < ACHIEVEMENTS_PER_PAGE; i++) {
            _anim[i].update(delta);
        }

        if(_currentPage == 0) {

            drawText();

        } else {
            int k = ((_currentPage-1)*ACHIEVEMENTS_PER_PAGE);
            float yspace = (_buttons.get(0).gety() - 4* _achievementsRegions[0].getRegionHeight() -  _buttons.get(0).getHeight())*0.2f;
            float posy = _buttons.get(0).gety() - yspace -_achievementsRegions[0].getRegionHeight() ;
            for(int i = k; i < k+4 && i < Settings.NUM_ACHIEVEMENTS; i++) {
                Color tmp = batch.getColor();
                if(Settings.isAchievementsUnlocked[i] == false)
                    batch.setColor(Color.DARK_GRAY);

                batch.draw(_achievementsRegions[i], (viewWidth - _achievementsRegions[i].getRegionWidth())*0.5f - (1-_anim[i-k].get())*1000,
                        posy
                );
                posy =posy - yspace - _achievementsRegions[0].getRegionHeight();
                batch.setColor(tmp);

            }
        }


        //write

        batch.end();
    }

    private void drawText() {
        _font.setColor(0.48627f, 0.1764f, 0.1764f, 1);

        int numDiamondsInLevels = 0;
        for (int i=0; i<Settings.NUM_LEVELS; i++) {
            if(Settings.isLevelDiamondTaken[i])
                numDiamondsInLevels++;
        }

        float progress = (float)(((float)Settings.levelsWon/ (float)Settings.NUM_LEVELS) * 75f +
                (Settings.achievementsUnlocked/ (float)Settings.NUM_ACHIEVEMENTS ) * 15f) +
                (numDiamondsInLevels/(float)(Settings.NUM_LEVELS)) * 10f;
        progress = (float)((int)(progress*100))/100.f;
        int playersUnlocked = 0;

        int ets = Settings.getEllapsedTimeInSeconds();
        int hours = ets / 3600;
        int minutes = (ets - hours*3600) / 60;
        int seconds = (ets % 60);
        String timeStr = ((hours !=0) ? "" + hours + "h" : "") + ((minutes !=0) ? "" + minutes + "min"  : "") + seconds +  "s";

        for(int i=0; i< Settings.NUM_PLAYERS; i++) {if(Settings.isUnlockedPlayer[i]) playersUnlocked++;}


        _text = "GAME STATS\n" +
                 s1 +
                fillWithCharUntilValueFromBegin('.', timeStr, 600 + 1000 - (getStringWidth(s1))) +"\n"+
                s2+
                fillWithCharUntilValueFromBegin('.', ""+progress + "%", 600+ 1000 - (getStringWidth(s2))) + "\n"+
                s3+
                fillWithCharUntilValueFromBegin('.', "" + Settings.levelsWon + "/" + Settings.NUM_LEVELS, 600+ 1000 - (getStringWidth(s3))) +"\n"+
                s4+
                fillWithCharUntilValueFromBegin('.', "" + Settings.numDiamonds, 600+ 1000 - (getStringWidth(s4))) +"\n"+
                s5+
                fillWithCharUntilValueFromBegin('.', "" + playersUnlocked + "/" + Settings.NUM_PLAYERS, 600+ 1000 - (getStringWidth(s5))) +"\n"+
                s6+
                fillWithCharUntilValueFromBegin('.', "" + Settings.numJumps, 600+ 1000 - (getStringWidth(s6))) +"\n"+
                s7+
                fillWithCharUntilValueFromBegin('.', "" + Settings.numDeaths, 600+ 1000 - (getStringWidth(s7))) +"\n"+
                 s8+
                fillWithCharUntilValueFromBegin('.', "" + Settings.numSmiles, 600+ 1000 - (getStringWidth(s8))) +"\n"+
                s9+
                fillWithCharUntilValueFromBegin('.', "" + Settings.numRestarts, 600+ 1000 - (getStringWidth(s9)));

        _font.draw(_game.getBatch(), _text,
                (_viewport.getWorldWidth()-850)*0.5f,
                (float)_viewport.getWorldHeight()*0.43f+((float)Math.ceil(_text.length()/70.f)+2)*_font.getLineHeight()*0.5f

                        - 600f*(1-_appearText.get())
                ,
                860, Align.left, false);

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
        AudioManager.get().detatch(Event.Type.DRAG_GESTURE);

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
                reverseAnim();
                startAnimation();

                if(!_appearText.reversed()) {
                    _appearText.reverse();
                    _appearText.start();
                }
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
            case MENU_LEFT_BUTTON_TRIGGERED:
                if(_buttons.get(1).getEvent() == Event.Type.BACK_BUTTON_TRIGGERED)
                    break;
                if(_anim[ACHIEVEMENTS_PER_PAGE-1].isRunning()) break;
                _currentEvent = e.type;
                if(_currentPage == 0) {
                    _appearText.reverse();
                    _appearText.start();
                }
                reverseAnim();
                startAnimation();
                break;
            case MENU_RIGHT_BUTTON_TRIGGERED:
                if(_buttons.get(3).isIgnored()) break;
                if(_anim[ACHIEVEMENTS_PER_PAGE-1].isRunning()) break;
                _currentEvent = e.type;
                if(_currentPage == 0) {
                    _appearText.reverse();
                    _appearText.start();
                }
                reverseAnim();
                startAnimation();
                break;

        }
    }

    public void setStartBug(float start) {_startBug = start;}

    public void initCharSpace() {
        space[32] = 10;
        space[41] = 12 + 2 + 16;
        space[93] = 11 + 1 + 13;
        space[91] = 11 + 2 + 13;
        space[40] = 12 + 2 + 14;
        space[36] = 20 + 2 + 22;
        space[92] = 17 + 1 + 18;
        space[124] = 6 + 1 + 8;
        space[125] = 13 + 1 + 14;
        space[123] = 13 + 2 + 15;
        space[47] = 18 + 1 + 18 ;
        space[68] = 21 + 1 + 20 ;
        space[81] = 26 + 2 + 28 ;
        space[38] = 21 + 1 + 23 ;
        space[63] = 16 + 1 + 16 ;
        space[48] = 18 + 1 + 19 ;
        space[57] = 15 + 1 + 16 ;
        space[56] = 18 + 1 + 19 ;
        space[53] = 18 + 1 + 18 ;
        space[51] = 18 + 1 + 19 ;
        space[50] = 20 + 1 + 20 ;
        space[90] = 21 + 1 + 22 ;
        space[89] = 20 + 0 + 19 ;
        space[88] = 25 + 1 + 26 ;
        space[87] = 26 + 1 + 26 ;
        space[86] = 24 + 2 + 24 ;
        space[84] = 22 + 0 + 20 ;
        space[83] = 19 + 1 + 20 ;
        space[82] = 19 + 1 + 20 ;
        space[80] = 18 + 2 + 21 ;
        space[79] = 23 + 1 + 24 ;
        space[77] = 23 + 2 + 26 ;
        space[76] = 15 + 1 + 16 ;
        space[75] = 21 + 2 + 22 ;
        space[71] = 21 + 1 + 21 ;
        space[70] = 19 + -1 + 16;
        space[69] = 19 + 1 + 19 ;
        space[67] = 22 + 1 + 24 ;
        space[66] = 20 + 2 + 22 ;
        space[65] = 21 + 1 + 21 ;
        space[37] = 27 + 1 + 28 ;
        space[33] = 6 + 3 + 11  ;
        space[55] = 21 + 0 + 21 ;
        space[54] = 16 + 1 + 17 ;
        space[52] = 15 + 1 + 16 ;
        space[49] = 14 + 1 + 16 ;
        space[85] = 22 + 1 + 24 ;
        space[78] = 22 + 1 + 23 ;
        space[74] = 15 + 1 + 16 ;
        space[73] = 6 + 3 + 10  ;
        space[72] = 18 + 2 + 20 ;
        space[59] = 7 + 2 + 10  ;
        space[109] = 20 + 1 + 21;
        space[35] = 19 + 1 + 20 ;
        space[64] = 22 + 1 + 24 ;
        space[122] = 17 + 1 + 19;
        space[119] = 21 + 1 + 22;
        space[116] = 18 + 1 + 18;
        space[115] = 16 + 1 + 17;
        space[114] = 15 + 1 + 17;
        space[113] = 21 + 1 + 22;
        space[112] = 14 + 1 + 16;
        space[111] = 18 + 1 + 19;
        space[110] = 17 + 1 + 19;
        space[108] = 12 + 1 + 13;
        space[106] = 12 + 1 + 13;
        space[104] = 15 + 1 + 16;
        space[103] = 17 + 1 + 18;
        space[102] = 14 + 1 + 15;
        space[101] = 15 + 1 + 16;
        space[100] = 17 + 1 + 19;
        space[99] = 18 + 1 + 19 ;
        space[98] = 16 + 1 + 17 ;
        space[97] = 17 + 1 + 19 ;
        space[43] = 19 + 1 + 21 ;
        space[62] = 17 + 1 + 17 ;
        space[60] = 16 + 1 + 17 ;
        space[121] = 15 + 1 + 16;
        space[120] = 20 + 1 + 22;
        space[118] = 18 + 1 + 20;
        space[117] = 18 + 1 + 19;
        space[107] = 18 + 1 + 19;
        space[105] = 5 + 1 + 6  ;
        space[58] = 7 + 2 + 10  ;
        space[42] = 16 + 1 + 17 ;
        space[61] = 18 + 4 + 25 ;
        space[44] = 7 + 1 + 9   ;
        space[39] = 6 + 1 + 7   ;
        space[96] = 9 + 1 + 9   ;
        space[34] = 11 + 1 + 13 ;
        space[126] = 22 + 1 + 23;
        space[94] = 13 + 2 + 16 ;
        space[46] = 7 + 2 + 9   ;
        space[95] = 24 + 1 + 25 ;
        space[45] = 16 + 1 + 17 ;
    }

    public int getStringWidth(String s) {
        int count = 0;
        for(int i=0; i<s.length(); i++) {
            count += space[s.charAt(i)];
        }
        return count;
    }

    public int getCharWidth(char c) {
        return space[c];
    }

    public String fillWithCharUntilValue(char c, String s, int value) {
        int count = getStringWidth(s);
        int numadds = Math.round((float)(value - count) / space[c]);
        for(int i=0; i<numadds; ++i) {
            s += c;
        }
        return s;

    }

    public String fillWithCharUntilValueFromBegin(char c, String s, int value) {
        int count = getStringWidth(s);
        String s2 = new String("");
        int numadds = Math.round((float)(value - count) / space[c]);
        for(int i=0; i<numadds; ++i) {
            s2 += c;
        }
        s2 += s;
        return s2;
    }
}