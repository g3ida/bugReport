package com.g3ida.bugreport;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LevelScreen implements Screen, Observer {

    private static final float TILE_SIZE = 42;
    private static final float TILES_W = 24;
    private static final float TILES_H = 16;
    private static final float VIRTUAL_WIDTH = TILES_W * TILE_SIZE;
    private static final float VIRTUAL_HEIGHT = TILES_H * TILE_SIZE;

    private BugReport _game;

    private Box _doorBox;

    private ToggleAngle _randomGameSpeedToggle = new ToggleAngle();
    private float _randomSpeed = 1.f;

    private OrthographicCamera camera;
    private Viewport viewport;
    private Viewport _otherView;

    private TiledMap map;
    private TiledMap collisionMap;

    private TmxMapLoader loader;

    private Color _batchOldColor;

    private OrthogonalTiledMapRenderer renderer;
    //private OrthogonalTiledMapRenderer renderer2;

    private int _level;

    private Button _jumpButton;
    private Button _leftButton;
    private Button _rightButton;
    private Button _restartButton;
    private TextureAtlas _buttonsAtlas;

    private Sound _winSFX;
    private boolean _isLevelClear = false;
    private float _waitWin = 0.f;
    private static final float WAIT_WIN = 0.5f;

    //Lvel items.
    private Vector<Rectangle> _winningDoors = new Vector<Rectangle>();
    private Vector<Rectangle> _sharpObjects = new Vector<Rectangle>();
    private Vector<Rectangle> _smilies = new Vector<Rectangle>();

    private class Area{public Rectangle r; public boolean flipH, flipV;}
    private Vector<Area> _frameBufferAreas = new Vector<Area>();

    //private Player player2;
    private Vector<Player> _players = new Vector<Player>();
    private Vector<Player> _winners = new Vector<Player>();

    private Vector2 _diamondPos = new Vector2();
    private Rectangle _rdiamond = new Rectangle();
    private Animation<TextureRegion> _diamondAnim;
    private float _diamondAnimTime = 0.f;
    private boolean _diamondAlreadyExist = true;
    private boolean _isDiamondCaptured = false;

    Random rand = new Random(0);

    private  float _timeLag = 0.f;

    private FrameBuffer _frameBuffer;

    private Vector<Box> _boxes = new Vector<Box>();
    private Vector<Collidable> _collidables = new Vector<Collidable>();

    private float _time = 0.f;
    private int[] _numTable = new int[10];
    private int[] _numTable2 = new int[11];

    LevelScreen(BugReport game, int level, Viewport view) {
        _game = game;
        camera = new OrthographicCamera();
        viewport = new FillViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        _level = level;
        _otherView = view;

        InputHandler.get().launch();
        EventHandler.get().addObserver(this);

        loader = new TmxMapLoader();

        //Fixing the glitches on the screen
        TmxMapLoader.Parameters params = new TmxMapLoader.Parameters();
        params.textureMinFilter = Texture.TextureFilter.Linear.Linear;
        params.textureMagFilter = Texture.TextureFilter.Nearest;

        map = loader.load("data/Levels/map"+ _level +".tmx");
        collisionMap = loader.load("data/Levels/cmap"+ _level +".tmx");


        renderer = new OrthogonalTiledMapRenderer(map);
        //renderer2 = new OrthogonalTiledMapRenderer(map, 1.002f);

        TiledMapTileLayer layer =  (TiledMapTileLayer)(collisionMap.getLayers().get("trigger"));

        for(int i=0; i<layer.getWidth(); i++)
            for(int j=0; j<layer.getHeight(); j++) {
                if (layer.getCell(i, j) != null && layer.getCell(i, j).getTile().getProperties().containsKey("trigger")) {
                    String s = (String) layer.getCell(i, j).getTile().getProperties().get("trigger");
                    if (s.equals("win")) {
                        Rectangle winningDoor = new Rectangle();
                        winningDoor.x = i * layer.getTileWidth();
                        winningDoor.y = j * layer.getTileHeight();
                        winningDoor.setWidth(layer.getTileWidth());
                        winningDoor.setHeight(layer.getHeight());
                        _winningDoors.add(winningDoor);
                    } else if (s.equals("die")) {
                        _sharpObjects.add(new Rectangle(i * layer.getTileWidth(), j * layer.getTileHeight()
                                , layer.getTileWidth(), layer.getHeight()));
                    } else if (s.equals("smile")) {
                        _smilies.add(new Rectangle(i * layer.getTileWidth(), j * layer.getTileHeight()
                                , layer.getTileWidth(), layer.getHeight()));
                    }
                }
            }

        _winSFX = Gdx.audio.newSound(Gdx.files.internal("data/sounds/level_clear.ogg"));
        AudioManager.get().attachSound(_winSFX, Event.Type.PLAYER_WIN);
        AudioManager.get().attachSound(Settings.diamondCaptureSFX, Event.Type.DIAMOND_CAPTURED);

        _buttonsAtlas = new TextureAtlas("data/graphics/controls.pack");

        _leftButton = new Button(_buttonsAtlas.findRegion("left"), _buttonsAtlas.findRegion("lefts"), Event.Type.BUTTON_LEFT, Input.Keys.LEFT, camera);
        _rightButton = new Button(_buttonsAtlas.findRegion("right"), _buttonsAtlas.findRegion("rights"), Event.Type.BUTTON_RIGHT, Input.Keys.RIGHT, camera);
        if(LevelSettings.config[LevelSettings.currentLevel].restartAndJumpReversed) {
            _jumpButton = new Button(_buttonsAtlas.findRegion("jump"), _buttonsAtlas.findRegion("jumps"), Event.Type.RESTART, Input.Keys.UP, camera);
            _restartButton = new Button(_buttonsAtlas.findRegion("restart"), _buttonsAtlas.findRegion("restarts"),  Event.Type.BUTTON_JUMP, Input.Keys.R, camera);

        } else {
            _jumpButton = new Button(_buttonsAtlas.findRegion("jump"), _buttonsAtlas.findRegion("jumps"), Event.Type.BUTTON_JUMP, Input.Keys.UP, camera);
            _restartButton = new Button(_buttonsAtlas.findRegion("restart"), _buttonsAtlas.findRegion("restarts"),  Event.Type.RESTART, Input.Keys.R, camera);

        }

        _restartButton.setOffset(0,
                ((((viewport.getWorldHeight() - viewport.getWorldWidth() *
                        Gdx.graphics.getHeight() / Gdx.graphics.getWidth()) - TILE_SIZE) / 2) + TILE_SIZE));


        _jumpButton.setPosition(
                viewport.getWorldWidth() - _jumpButton.getWidth(),
                (VIRTUAL_HEIGHT - VIRTUAL_WIDTH *  (float)Gdx.graphics.getHeight()/Gdx.graphics.getWidth())*0.5f);
        _leftButton.setPosition(0,
                (VIRTUAL_HEIGHT - VIRTUAL_WIDTH *  (float)Gdx.graphics.getHeight()/Gdx.graphics.getWidth())*0.5f);
        _rightButton.setPosition(_leftButton.getWidth(),
                (VIRTUAL_HEIGHT - VIRTUAL_WIDTH *  (float)Gdx.graphics.getHeight()/Gdx.graphics.getWidth())*0.5f);

        float restartx = viewport.getWorldWidth() - _restartButton.getWidth() - TILE_SIZE * 0.5f;
        float restarty = viewport.getWorldHeight() - ((((viewport.getWorldHeight() - viewport.getWorldWidth() *
                Gdx.graphics.getHeight() / (float)Gdx.graphics.getWidth()) - TILE_SIZE) / 2) + TILE_SIZE) - _restartButton.getHeight();

        _restartButton.setPosition(restartx, restarty);

        AchievementSystem._appearAchievement = new Appear();

        Iterator<MapObject> it = map.getLayers().get("objects").getObjects().iterator();
        while (it.hasNext()) {
            MapObject mo = it.next();
            String  s = (String) mo.getName();
            if(s != null && s.equals("area")) {
                Area a = new Area();
                a.r = ((RectangleMapObject) mo).getRectangle();
                a.flipH =(Boolean) (mo.getProperties().get("flipH"));
                a.flipV = (Boolean) mo.getProperties().get("flipV");
                _frameBufferAreas.add(a);
            }  else if(s != null && s.equals("box")) {
                Box b = new Box((TiledMapTileLayer)collisionMap.getLayers().get("collision"),
                        map.getTileSets().getTile(192).getTextureRegion(), _collidables);
                Rectangle rc = ((RectangleMapObject) mo).getRectangle();
                b.setPosition(rc.x, rc.y);
                b.setDefaultPos(rc.x, rc.y);
                _boxes.add(b);
            }
        }

        if(LevelSettings.config[LevelSettings.currentLevel].flippedWorldW || LevelSettings.config[LevelSettings.currentLevel].flippedWorldH) {
            _frameBuffer = new FrameBuffer(Pixmap.Format.RGB888,
                    Gdx.graphics.getWidth(),
                    (int)(Gdx.graphics.getWidth()*((float)(TILES_H+1)/(TILES_W+1))), false);
        }
        if(LevelSettings.config[LevelSettings.currentLevel].drawError) {
            _frameBuffer = new FrameBuffer(Pixmap.Format.RGB888,
                    Gdx.graphics.getWidth(),
                    (int)(Gdx.graphics.getWidth()*((float)(TILES_H+1)/(TILES_W+1))), false);
            _frameBuffer.bind();
            renderer.getBatch().begin();
            Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            renderer.getBatch().end();
            _frameBuffer.unbind();
        }

        _numTable[0] = 880;
        _numTable[1] = 881;
        _numTable[2] = 882;
        _numTable[3] = 883;
        _numTable[4] = 884;
        _numTable[5] = 910;
        _numTable[6] = 911;
        _numTable[7] = 912;
        _numTable[8] = 913;
        _numTable[9] = 914;

        //setup the diamond Animation
        Array<TextureRegion> regions = new Array<TextureRegion>();
        if(LevelSettings.config[LevelSettings.currentLevel].drawX) {
            regions.add(map.getTileSets().getTile(229).getTextureRegion());
        } else {
            for (int i = 0; i < 4; i++) {
                regions.add(map.getTileSets().getTile(147 + i).getTextureRegion());
            }
        }

        _diamondAnim = new Animation<TextureRegion>(0.28f, regions, Animation.PlayMode.LOOP);
        _diamondAlreadyExist = !Settings.isLevelDiamondTaken[_level-1];
        _isDiamondCaptured = Settings.isLevelDiamondTaken[_level-1];

        ///////////////////////////////

        for(int i = 201; i<=210; i++) {
            _numTable2[i-201] = i;
        }
        _numTable2[10] = 200;

        Gdx.gl.glClearColor(1.f, 1.f, 1.f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void dispose() {

        map.dispose();
        collisionMap.dispose();

        renderer.dispose();
        //renderer2.dispose();

        for(int i=0; i<_players.size(); ++i)
            _players.get(i).dispose();

        for(int i=0; i<_winners.size(); ++i)
            _winners.get(i).dispose();

        _otherView.apply();

        AudioManager.get().detatch(Event.Type.PLAYER_WIN);
        AudioManager.get().detatch(Event.Type.DIAMOND_CAPTURED);

        _winSFX.dispose();

        EventHandler.get().deleteObserver(this);
        if(_frameBuffer != null)
            _frameBuffer.dispose();
    }

    @Override
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resize(int width, int height) {
        _otherView.update(width, height, true);
        viewport.update(width, height, true);
        _jumpButton.setPosition(
                viewport.getWorldWidth() - _jumpButton.getWidth(),
                (VIRTUAL_HEIGHT - VIRTUAL_WIDTH *  (float)Gdx.graphics.getHeight()/Gdx.graphics.getWidth())*0.5f);
        _leftButton.setPosition(0,
                (VIRTUAL_HEIGHT - VIRTUAL_WIDTH *  (float)Gdx.graphics.getHeight()/Gdx.graphics.getWidth())*0.5f);
        _rightButton.setPosition(_leftButton.getWidth(),
                (VIRTUAL_HEIGHT - VIRTUAL_WIDTH *  (float)Gdx.graphics.getHeight()/Gdx.graphics.getWidth())*0.5f);

        float restartx = viewport.getWorldWidth() - _restartButton.getWidth() - TILE_SIZE * 0.5f;
        float restarty = viewport.getWorldHeight() - ((((viewport.getWorldHeight() - viewport.getWorldWidth() *
                Gdx.graphics.getHeight() / (float)Gdx.graphics.getWidth()) - TILE_SIZE) / 2) + TILE_SIZE) - _restartButton.getHeight()*0.72f;

        _restartButton.setPosition(restartx, restarty);
    }

    @Override
    public void resume() {}

    @Override
    public void show() {
        //Set the player.
        try {
            Iterator<MapObject> it = map.getLayers().get("objects").getObjects().iterator();
            while (it.hasNext())
            {
                MapObject mo = it.next();

                String  s = (String) mo.getName();
                if(s != null && s.equals("player"))
                {
                    Rectangle rc = ((RectangleMapObject) mo).getRectangle();
                    Player p = new Player((TiledMapTileLayer)collisionMap.getLayers().get("collision"), _collidables);
                    p.setPosition(rc.x, rc.y);
                    p.setDefaultPos(rc.x, rc.y);
                    if(mo.getProperties().containsKey("rev_dir")) {
                        p.setReverseDirections((Boolean) mo.getProperties().get("rev_dir"));
                    }
                    if(mo.getProperties().containsKey("is_shown")) {
                        p.setShown((Boolean) mo.getProperties().get("is_shown"));
                    }
                    if(mo.getProperties().containsKey("player2")) {

                        if(Settings.level29FirstTimeEntred) {
                            Settings.level29SecondPlayer = (Settings.currentPlayer+1) % Settings.NUM_PLAYERS;
                            //Settings.level29FirstPlayer = Settings.currentPlayer;
                            Settings.level29FirstTimeEntred = false;
                        }
                        p.changePlayerSprite(Settings.level29SecondPlayer);

                        if(Settings.level29SecondPlayer != Settings.currentPlayer) {
                            p.block();
                        }
                    }
                    _players.add(p);
                } else if(s != null && s.equals("diamond")) {
                    Rectangle rc = ((RectangleMapObject) mo).getRectangle();
                    _diamondPos.x = rc.x;
                    _diamondPos.y = rc.y;
                    _rdiamond.x = _diamondPos.x+5;
                    _rdiamond.y = _diamondPos.y+5;
                    _rdiamond.width = 37;
                    _rdiamond.height = 37;
                } else if(s != null && s.equals("doorBox")) {
                    _doorBox = new Box((TiledMapTileLayer)collisionMap.getLayers().get("collision"),
                            map.getTileSets().getTile(168).getTextureRegion(), _collidables);
                    Rectangle rc = ((RectangleMapObject) mo).getRectangle();
                    _doorBox.setPosition(rc.x, rc.y);
                    _doorBox.setDefaultPos(rc.x, rc.y);
                    _collidables.add(_doorBox);
                }
            }
        } catch (NullPointerException e) {
            Player p = new Player((TiledMapTileLayer)collisionMap.getLayers().get("collision"), _collidables);
            p.setPosition(500, 500);
            _players.add(p);
        }
        for(int i=0; i<_boxes.size(); i++) {
            _collidables.add(_boxes.get(i));
        }
        for(int i=0; i<_players.size(); i++) {
            _collidables.add(_players.get(i));
        }
    }

    public void reset() {
        _isLevelClear = false;
        _waitWin = 0.f;
        _time = 0.f;
        _isDiamondCaptured = Settings.isLevelDiamondTaken[LevelSettings.currentLevel];

        for(int i=0; i<_winners.size(); i++)
            _players.add(_winners.get(i));

        _winners.clear();

        if(!LevelSettings.config[LevelSettings.currentLevel].restartFixedBox) {
            for (int i = 0; i < _boxes.size(); i++)
                _boxes.get(i).resetDefaultPosition();

        }

        if(_doorBox != null) {
            _doorBox.resetDefaultPosition();
        }

        for(int i=0; i<_players.size(); ++i) {
            _players.get(i).reset();
        }

        //Smilies stuff
        _smilies.clear();
        TiledMapTileLayer layer =  (TiledMapTileLayer)(collisionMap.getLayers().get("trigger"));
        for(int i=0; i<layer.getWidth(); i++)
            for(int j=0; j<layer.getHeight(); j++) {
                if (layer.getCell(i, j) != null && layer.getCell(i, j).getTile().getProperties().containsKey("trigger")) {
                    String s = (String) layer.getCell(i, j).getTile().getProperties().get("trigger");
                    if (s.equals("smile")) {
                        _smilies.add(new Rectangle(i * layer.getTileWidth(), j * layer.getTileHeight()
                                , layer.getTileWidth(), layer.getHeight()));
                    }
                }
            }
                TiledMapTileLayer foreground = (TiledMapTileLayer) map.getLayers().get("foreground");
                for(int i=0; i< foreground.getWidth(); i++) {
                    for (int j = 0; j < foreground.getHeight(); j++) {
                        if (foreground.getCell(i, j) != null && foreground.getCell(i, j).getTile().getProperties().containsKey("platform")) {
                            TiledMapTileLayer.Cell cell = foreground.getCell(i, j);
                            String s = (String) cell.getTile().getProperties().get("platform");
                            if (s.equals("happy")) {
                                foreground.setCell(i, j, cell.setTile(map.getTileSets().getTile(407)));
                            }
                        }
                    }
                }
    }

    @Override
    public void render(float delta) {
        Settings.cuurentDelay+=delta;
        _time += delta;
        if(LevelSettings.config[LevelSettings.currentLevel].doTimeout &&
                _time > LevelSettings.config[LevelSettings.currentLevel].timeout && _waitWin == 0)
        {
            dispose();
            _game.setScreen(new CrashScreen(_game, _otherView, "Unexpected crash occured after 3.5sec ellapsed!", true, _level));
            return;
        }

        if(delta > 0.05f) return;
        delta = LevelSettings.config[LevelSettings.currentLevel].gameSpeed * delta;

        if(LevelSettings.config[LevelSettings.currentLevel].randomGameSpeed) {
            if(!_randomGameSpeedToggle.isRunning()) {
                _randomGameSpeedToggle.start();
            }
            _randomGameSpeedToggle.update(delta);
            System.out.println(_randomGameSpeedToggle.get());
            delta = delta * (((_randomGameSpeedToggle.get()-1f)*2f)+1f);
        }

        _jumpButton.update(camera);
        _leftButton.update(camera);
        _rightButton.update(camera);
        _restartButton.update(camera);

        for (int i = 0; i < _players.size(); i++) {
            _players.get(i).update(delta);
        }

        for (int i = 0; i < _boxes.size(); i++) {
            _boxes.get(i).update(delta);
        }

        if(_doorBox != null) {
            _doorBox.update(delta);
        }

        if(LevelSettings.config[LevelSettings.currentLevel].drawLag) {
            if(_timeLag < LevelSettings.config[LevelSettings.currentLevel].timelag) {
                _timeLag += delta;
                testForEvents(delta);
                return;
            } else {
                _timeLag = 0;
            }
        }

        if (_frameBuffer != null) {
            _frameBuffer.bind();
        }

        SpriteBatch b = (SpriteBatch) renderer.getBatch();
        //SpriteBatch b2 = (SpriteBatch) renderer2.getBatch();


        if(LevelSettings.config[LevelSettings.currentLevel].randombatchColor) {
            _batchOldColor = b.getColor();
            b.setColor( rand.nextFloat(), rand.nextFloat(),rand.nextFloat(), 1.f);
            //b2.setColor( rand.nextFloat(), rand.nextFloat(),rand.nextFloat(), 1.f);

        }

        if(!LevelSettings.config[LevelSettings.currentLevel].drawError) {

            Gdx.gl.glClearColor(1.f, 1.f, 1.f, 1.0f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        }
            renderer.setView(camera);

        /*if(!LevelSettings.config[LevelSettings.currentLevel].drawError)
            //renderer2.render();
            renderer.setView(camera);*/

        if(!LevelSettings.config[LevelSettings.currentLevel].drawError)
            renderer.render();


        if(LevelSettings.config[LevelSettings.currentLevel].randombatchColor) {
            b.setColor(_batchOldColor);
            //b2.setColor(_batchOldColor);

        }

        b.begin();


        if (_frameBuffer == null || LevelSettings.config[LevelSettings.currentLevel].flippedButtonsWithPlatform) {
            _jumpButton.draw(b);
            _rightButton.draw(b);
            _leftButton.draw(b);
            _restartButton.draw(b);
        }

        //draw the diamond
        if(!Settings.isLevelDiamondTaken[_level-1] && !_isDiamondCaptured) {
            _diamondAnimTime+=delta;
            b.draw(_diamondAnim.getKeyFrame(_diamondAnimTime), _diamondPos.x, _diamondPos.y);

        } else if(!_diamondAlreadyExist && !LevelSettings.config[LevelSettings.currentLevel].drawX) {
            Color tmpCol = b.getColor();
            b.setColor(0.4f, 0.4f, 0.4f, 0.1f);
            //b.draw(map.getTileSets().getTile(147).getTextureRegion(), _diamondPos.x, _diamondPos.y);
            _diamondAnimTime+=delta;
            b.draw(_diamondAnim.getKeyFrame(_diamondAnimTime), _diamondPos.x, _diamondPos.y);
            b.setColor(tmpCol);
        }

        for(int i=0; i< _boxes.size(); i++) {
            _boxes.get(i).draw(b);
        }

        if(_doorBox != null) {
            b.draw(map.getTileSets().getTile(167).getTextureRegion(), _doorBox.getX(), _doorBox.getY()+TILE_SIZE);
            _doorBox.draw(b);
        }

        for (int i = 0; i < _players.size(); i++) {
            _players.get(i).draw(b);
        }

        //TODO
// /*

        b.end();

        if (_frameBuffer != null) {
            _frameBuffer.unbind();

            Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            b.begin();
            Texture texture = _frameBuffer.getColorBufferTexture();

            float w = renderer.getViewBounds().width;
            float h = (w / texture.getWidth()) * texture.getHeight();
            float x = 0;
            float y = 0;

            b.draw(texture, x, y, 0, 0, w, h, 1, 1, 0, 0, 0, texture.getWidth(), texture.getHeight(), false, true);

            Iterator<Area> it = _frameBufferAreas.iterator();
            while (it.hasNext()) {
                Area tmp = it.next();
                float a = ((float)texture.getWidth() / (25*42));
                TextureRegion region = new TextureRegion(texture,(int)(a*tmp.r.x), (int)(a*tmp.r.y),(int)(a*tmp.r.width), (int)(a*tmp.r.height));
                int s1 = ((tmp.flipH == true) ? -1 : 1);
                int s2 = ((tmp.flipV == true) ? 1 : -1);

                b.draw(region, tmp.r.x, tmp.r.y, tmp.r.width*0.5f, tmp.r.height*0.5f, tmp.r.width, tmp.r.height, s1, s2, 0f);
            }

            if (!LevelSettings.config[LevelSettings.currentLevel].flippedButtonsWithPlatform) {
                _jumpButton.draw(b);
                _rightButton.draw(b);
                _leftButton.draw(b);
                _restartButton.draw(b);
            }
            b.end();
        }

        b.begin();
        drawHUD(b);
        _drawAchievement(b);
        b.end();

        testForEvents(delta);
    }

    private void drawHUD(SpriteBatch b) {
        float xx = 30;
        float yy = viewport.getWorldHeight() - ((((viewport.getWorldHeight() - viewport.getWorldWidth() *
                Gdx.graphics.getHeight() / (float)Gdx.graphics.getWidth()) - TILE_SIZE) / 2) + TILE_SIZE) - 42;
        b.draw(map.getTileSets().getTile(146).getTextureRegion(), xx, yy+5);
        xx += 30;
        b.draw(map.getTileSets().getTile(_numTable2[10]).getTextureRegion(), xx, yy);
        int numdiamonds = Settings.numDiamonds + ((_isDiamondCaptured && !Settings.isLevelDiamondTaken[LevelSettings.currentLevel])? 1 : 0);
        if(numdiamonds < 10) {
            xx += 25;
            b.draw(map.getTileSets().getTile(_numTable2[numdiamonds]).getTextureRegion(), xx, yy);
        } else if(Settings.numDiamonds < 100){
            xx += 25;
            b.draw(map.getTileSets().getTile(_numTable2[numdiamonds/10]).getTextureRegion(), xx, yy);
            xx += 18;
            b.draw(map.getTileSets().getTile(_numTable2[numdiamonds%10]).getTextureRegion(), xx, yy);
        } else {
            xx += 25;
            b.draw(map.getTileSets().getTile(_numTable2[numdiamonds/100]).getTextureRegion(), xx, yy);
            xx += 18;
            b.draw(map.getTileSets().getTile(_numTable2[(numdiamonds%100)/10]).getTextureRegion(), xx, yy);
            xx += 18;
            b.draw(map.getTileSets().getTile(_numTable2[numdiamonds%100]).getTextureRegion(), xx, yy);
        }

        if(_level < 10) {
            float px = (viewport.getWorldWidth()-42)*0.5f;
            float py = viewport.getWorldHeight() - ((((viewport.getWorldHeight() - viewport.getWorldWidth() *
                    Gdx.graphics.getHeight() / (float)Gdx.graphics.getWidth()) - TILE_SIZE) / 2) + TILE_SIZE) - TILE_SIZE;
            b.draw(map.getTileSets().getTile(_numTable[_level]).getTextureRegion(), px, py);
        } else {
            float px = (viewport.getWorldWidth()-72)*0.5f;
            float py = viewport.getWorldHeight() - ((((viewport.getWorldHeight() - viewport.getWorldWidth() *
                    Gdx.graphics.getHeight() / (float)Gdx.graphics.getWidth()) - TILE_SIZE) / 2) + TILE_SIZE) - TILE_SIZE;
            b.draw(map.getTileSets().getTile(_numTable[_level / 10]).getTextureRegion(), px, py);
            px +=30;
            b.draw(map.getTileSets().getTile(_numTable[_level % 10]).getTextureRegion(), px, py);
        }
    }

    private void _drawAchievement(SpriteBatch b) {

        if(AchievementSystem._drawAchievement) {
            float px = (viewport.getWorldWidth() - AchievementSystem._achievementRegion.getRegionWidth())*0.5f;
            float m = AchievementSystem._appearAchievement.get();

            float py = viewport.getWorldHeight() - ((((viewport.getWorldHeight() - viewport.getWorldWidth() *
                    Gdx.graphics.getHeight() / (float)Gdx.graphics.getWidth()) - TILE_SIZE) / 2) + TILE_SIZE) + 20 -
                    AchievementSystem._achievementRegion.getRegionHeight()*1.1f + (1-m) *AchievementSystem._achievementRegion.getRegionHeight()*1.1f ;

            b.draw(AchievementSystem._plusDiamondRegion,
                    px + AchievementSystem._achievementRegion.getRegionWidth()*0.5f - AchievementSystem._plusDiamondRegion.getRegionWidth()*0.5f

                    , py-AchievementSystem._plusDiamondRegion.getRegionHeight()*(AchievementSystem._appearAchievement.get()),
                    0, 0, AchievementSystem._plusDiamondRegion.getRegionWidth(), AchievementSystem._plusDiamondRegion.getRegionHeight(), 1.f
                    , 1.f, 0.f);
            b.draw(AchievementSystem._achievementRegion, px, py, 0, 0, AchievementSystem._achievementRegion.getRegionWidth(), AchievementSystem._achievementRegion.getRegionHeight(), 1.f
                    , 1.f, 0.f);
        }

    }

    public void testForEvents(float delta) {

        for (int i = 0; i < _players.size(); i++) {
            boolean abort = false;
            if (!_players.get(i).isDead() && !_isLevelClear) {
                if(_doorBox != null) {
                    _winningDoors.add(new Rectangle(_doorBox.getX(), _doorBox.getY(), TILE_SIZE, TILE_SIZE * 1.5f));
                }
                for (Rectangle r : _winningDoors) {
                    if (!_players.get(i).getCollisionMask().overlaps(r)) continue;
                    if (LevelSettings.config[LevelSettings.currentLevel].winCrash && !Settings.isSilent && _game.getSoundVolume() != 0) {
                        dispose();
                        _game.setScreen(new CrashScreen(_game, _otherView, "the file \"winSound.mp3\" is corrupt and cannot be played!"));
                        return;
                    } else {
                        Event e = new Event();
                        e.type = Event.Type.PLAYER_WIN;
                        e.info.player = i;
                        EventHandler.get().notifyObservers(e);
                        if(_players.size() == 1) {
                            if(!Settings.isWonLevel[LevelSettings.currentLevel]) {
                                Settings.levelsWon++;
                            }
                            Settings.isWonLevel[_level-1] = true;
                            if(_level != Settings.NUM_LEVELS)
                                Settings.menuCurrentPage = (_level) / 8;
                            //Unlock level.
                            if(_level == Settings.levelsUnlocked) {
                                if( _level != Settings.NUM_LEVELS) {
                                    Settings.levelsUnlocked++;
                                    Settings.isUnlockedLevel[_level] = true;
                                }
                            }
                            if(_isDiamondCaptured && !Settings.isLevelDiamondTaken[_level-1]) {
                                Settings.isLevelDiamondTaken[_level-1] = true;
                                Settings.numDiamonds++;
                                //Settings.overallDiamonds++;
                            }

                            _isLevelClear = true;
                            _players.get(0).freeze();
                            Event e2 = new Event();
                            e2.type = Event.Type.LEVEL_CLEAR;
                            e2.info.player = i;
                            EventHandler.get().notifyObservers(e2);
                        } else {
                            _winners.add(_players.get(i));
                            _players.get(i).freeze();
                            _players.remove(i);
                            abort = true;
                            break;
                        }
                    }
                }
                if(_doorBox != null) {
                    _winningDoors.removeElementAt(_winningDoors.size() - 1);
                }
            }

            if(abort) break;

            for (Rectangle r : _sharpObjects) {
                if (_players.get(i).getCollisionMask().overlaps(r)) {
                    _players.get(i).kill();
                    break;
                }
            }

            for (Rectangle r : _smilies) {
                if (_players.get(i).getCollisionMask().overlaps(r)) {
                    if (!Settings.isSilent)
                        Settings.smileSFX.play();

                    TiledMapTileLayer foreground = (TiledMapTileLayer) map.getLayers().get("foreground");
                    for (int k = 0; k < foreground.getWidth(); k++) {
                        for (int j = 0; j < foreground.getHeight(); j++) {
                            if (foreground.getCell(k, j) != null && foreground.getCell(k, j).getTile().getProperties().containsKey("platform")) {
                                TiledMapTileLayer.Cell cell = foreground.getCell(k, j);
                                String s = (String) cell.getTile().getProperties().get("platform");
                                if (s.equals("sad")) {
                                    foreground.setCell(k, j, cell.setTile(map.getTileSets().getTile(197)));
                                }
                            }
                        }
                    }
                    _smilies.clear();
                    Settings.numSmiles++;
                    EventHandler.get().notifyObservers(new Event(Event.Type.SMILING_PLATFORM_TRGGERED));
                    break;
                }
            }
            if(!_isDiamondCaptured) {
                if((_players.get(i).getCollisionMask().overlaps(_rdiamond) && ! _players.get(i).isDead())) {
                    _isDiamondCaptured = true;
                    Event e = new Event();
                    e.type = Event.Type.DIAMOND_CAPTURED;
                    EventHandler.get().notifyObservers(e);

                }
            }
        }

        if(_isLevelClear) {
            for(int i=0; i<_players.size(); ++i)
                _players.get(i).freeze();

            //TODO here we must make the case of ending the game happy ending yaaah !
            _waitWin += delta;
            if(_waitWin >= WAIT_WIN) {
                Settings.save();
                dispose();
                if(_level+1 <= Settings.NUM_LEVELS) {
                    _game.setScreen(new TransitionScreen(_game, _otherView, _level+1));
                }
                else {
                    _game.setScreen(new LevelMenuScreen(_game, _otherView));
                }
            }
        }
    }

    @Override
    public void update(Observable observable, Object o) {
        if(o == null) return;
        if (!(o instanceof Event)) return;

        Event e = (Event) o;

        if(e.type == Event.Type.RESTART && e.info.buttonState.pressed == true) {
            if(e.info.buttonState.pressed && !Settings.isSilent) {
                Settings.buttonSFX.play();
                Settings.numRestarts++;
            }
        }

        if(e.type == Event.Type.PLAYER_DEAD) {Settings.numDeaths++;}

        if(e.type == Event.Type.BACK) {
            dispose();
            _game.setScreen(new LevelMenuScreen(_game, _otherView));
        }
        else if(e.type == Event.Type.PLAYER_DEAD || (e.type == Event.Type.RESTART && e.info.buttonState.pressed == true)) {
            //dispose();
            reset();
            //_game.setScreen(new LevelScreen(_game, _level, _otherView));
        }
    }
}
