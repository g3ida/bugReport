package com.g3ida.bugreport;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.Vector;

public class Player implements Observer, Disposable, Collidable{

    private Vector2 _position = new Vector2();
    private Vector2 _velocity = new Vector2();
    private Vector2 _defaultPos = new Vector2();
    private  final float SPEED = 8f* Settings.PIXELS_PER_METER;
    private static final float GRAVITY = 6*9.8f * Settings.PIXELS_PER_METER;
    private static final float FPS = 60.f;
    private static final float FRAME_DURATION = 1.f/FPS*3.f;
    private float _animationTime = 0.f;
    private boolean _avoidMultipleJumpFlag = false;

    private boolean _canJump;
    private int _direction = 1;
    private float _scale = 1.f;

    private Animation _stand, _walk, _jump, _die, _currentAnim;
    private TextureAtlas.AtlasRegion _currentRegion;
    private TiledMapTileLayer _collisionLayer;

    private static Sound _jumpSFX = null;
    private static Sound _walkSFX = null;
    private static Sound _dieSFX = null;

    private static int _numInstances = 0;

    private int _instanceId = 0;

    public ParticleEffect dustParticles = new ParticleEffect();

    private boolean _isDead = false;
    private boolean _isRecentlyDead = true;
    private boolean _isRecentlyWalk = true;
    private boolean _isFrozen = false;
    private boolean _reverseDirections = false;

    //Variable jumping stuff
    private boolean _canPushJump = false;
    private final float _jumpTimeLimit = 0.4f;
    private float _jumpTime = 0.f;

    private boolean _isShown = true;
    private float _walkTime = 0.f;
    private Vector<Collidable> _collidables;

    private boolean _blocked = false;

    Random _randomNumberGenerator = new Random();

    public void changePlayerSprite(int currentPlayer) {
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("data/players/" + Settings.playerNames[currentPlayer] +".pack"));
        Array<TextureAtlas.AtlasRegion> regions = new Array<TextureAtlas.AtlasRegion>();
        for(int i=0; i<8; i++)
            regions.add(atlas.findRegion(Integer.toString(i)));
        _walk = new Animation(FRAME_DURATION, regions, Animation.PlayMode.LOOP);

        _stand = new Animation(FRAME_DURATION, atlas.findRegions("stand"));
        _die = new Animation(FRAME_DURATION, atlas.findRegions("die"));
        _jump = new Animation(FRAME_DURATION, atlas.findRegions("jump"));
    }

    public void block() {
        _blocked = true;
    }

    public Player(TiledMapTileLayer collisionLayer, Vector<Collidable> coll) {
        _numInstances++;
        _instanceId = _numInstances;
        _collidables = coll;


        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("data/players/" + Settings.playerNames[Settings.currentPlayer] +".pack"));
        Array<TextureAtlas.AtlasRegion> regions = new Array<TextureAtlas.AtlasRegion>();

        for(int i=0; i<8; i++)
            regions.add(atlas.findRegion(Integer.toString(i)));
        _walk = new Animation(FRAME_DURATION, regions, Animation.PlayMode.LOOP);

        _stand = new Animation(FRAME_DURATION, atlas.findRegions("stand"));
        _die = new Animation(FRAME_DURATION, atlas.findRegions("die"));
        _jump = new Animation(FRAME_DURATION, atlas.findRegions("jump"));

        _collisionLayer = collisionLayer;

        _currentAnim = _stand;
        _currentRegion = (TextureAtlas.AtlasRegion)_currentAnim.getKeyFrame(_animationTime);


        if(_numInstances == 1 || _jumpSFX == null) {
            _jumpSFX = Gdx.audio.newSound(Gdx.files.internal("data/sounds/jump.ogg"));
            AudioManager.get().attachSound(_jumpSFX, Event.Type.PLAYER_JUMPED);
        }

        if(_numInstances == 1 ||_dieSFX == null) {
            _dieSFX = Gdx.audio.newSound(Gdx.files.internal("data/sounds/die.ogg"));
            AudioManager.get().attachSound(_dieSFX, Event.Type.PLAYER_DYING);
        }

        if(_numInstances == 1 ||_walkSFX == null) {
            //AudioManager.get().attachSound(_dieSFX, Event.Type.PLAYER_MOVING);
            _walkSFX = Gdx.audio.newSound((Gdx.files.internal("data/sounds/walk.ogg")));
        }

        dustParticles.load(Gdx.files.internal("dust.particle"),
                Gdx.files.internal(""));
        InputHandler.get().reset();
        EventHandler.get().addObserver(this);

        _randomNumberGenerator.setSeed(0);
    }

    public void reset() {
        _isDead = false;
        _isRecentlyDead = true;
        _isRecentlyWalk = true;
        _isFrozen = false;
        _canJump = false;
        _direction = 1;
        _scale = 1.f;
        _velocity.x = 0.f;
        _velocity.y = 0.f;
        _currentAnim = _stand;
        _animationTime = 0.f;
        resetDefaultPosition();
    }

    public void freeze() {_isFrozen=true;}
    public void unfreeze() {_isFrozen=false;}
    public boolean isFrozen() {return _isFrozen;}

    public TextureAtlas.AtlasRegion getCurrentRegion() {
        return _currentRegion;
    }

    @Override
    public void dispose() {
        _numInstances--;
        if(_numInstances == 0) {
            EventHandler.get().deleteObserver(this);
            AudioManager.get().detatch(Event.Type.PLAYER_JUMPED);
            AudioManager.get().detatch(Event.Type.PLAYER_DYING);
            //AudioManager.get().detatch(Event.Type.PLAYER_MOVING);

            _jumpSFX.dispose();
            _walkSFX.stop();
            _walkSFX.dispose();
            _dieSFX.dispose();

            _jumpSFX = null;
            _walkSFX = null;
            _dieSFX = null;
        }
        _currentAnim = _stand;
        _velocity.x = 0;
    }

    public void draw(SpriteBatch batch) {

        if(_currentAnim == _walk) {
            dustParticles.draw(batch);
        }

        if(_isShown) {

            //test if the player is at the edge of the right side. So it should be clipped to the left side.
            //note that the condition is wrong but it works somehow.
            if(_position.x + _position.x + LevelSettings.config[LevelSettings.currentLevel].playerOffsetX >
                    _collisionLayer.getTileWidth() * _collisionLayer.getWidth() && _direction==1) {
                batch.draw(_currentRegion,
                        //x
                        0 - _collisionLayer.getTileWidth() * _collisionLayer.getWidth() +
                                _position.x + LevelSettings.config[LevelSettings.currentLevel].playerOffsetX,
                        //y
                        _position.y + LevelSettings.config[LevelSettings.currentLevel].playerOffsetY,
                        //originX, originY
                        _currentRegion.getRegionWidth() * 0.5f, _currentRegion.getRegionHeight() * 0.5f,
                        //width, height
                        _currentRegion.getRegionWidth(), _currentRegion.getRegionHeight(),
                        //scaleX, scaleY, rotation
                         _scale, _scale, 0.f);

            }

            //Draw the player normally
            batch.draw(_currentRegion, _position.x + LevelSettings.config[LevelSettings.currentLevel].playerOffsetX,
                    _position.y + LevelSettings.config[LevelSettings.currentLevel].playerOffsetY,
                    _currentRegion.getRegionWidth() * 0.5f, _currentRegion.getRegionHeight() * 0.5f,
                    _currentRegion.getRegionWidth(), _currentRegion.getRegionHeight(),
                    _direction * _scale, _scale, 0.f);
        } else {
            drawDust(batch);
        }
    }

    public void drawDust(SpriteBatch b) {
        if(_currentAnim == _walk) {
            dustParticles.draw(b);
        }
    }

    public void update(float delta) {
        _avoidMultipleJumpFlag = false;
        _currentRegion = (TextureAtlas.AtlasRegion)_currentAnim.getKeyFrame(_animationTime);

        if(LevelSettings.config[LevelSettings.currentLevel].gravityReversed)
            _velocity.y = MathUtils.clamp(_velocity.y+GRAVITY*delta, -30*SPEED, 30*SPEED);
        else
            _velocity.y = MathUtils.clamp(_velocity.y-GRAVITY*delta, -30*SPEED, 30*SPEED);

        Vector2 oldCoords = new Vector2(_position.x, _position.y);

        if(!_isDead) {
            boolean collideX = false, collideY = false;
            if (InputHandler.get().leftDown()) {
                _walkTime += delta;
                if (_walkTime >= 0.5f) _walkTime = 0.5f;
                _velocity.x = -(_walkTime+0.5f)*SPEED;
            }
            if (InputHandler.get().rightDown()) {
                _walkTime += delta;
                if (_walkTime >= 0.5f) _walkTime = 0.5f;
                _velocity.x = (_walkTime+0.5f)*SPEED;
            }
            // this is done to make the player walk slowly at first.
            if(_velocity.x == 0) _walkTime = 0;

            if(LevelSettings.config[LevelSettings.currentLevel].allwaysJump) {
                if(_canJump) {
                    _velocity.y += 1.9f*SPEED;
                    _canPushJump=false;
                    _canJump = false;
                }
            } else {

                if (InputHandler.get().jumpDown()) {
                    if (_canPushJump && _jumpTime < _jumpTimeLimit) {
                        if (LevelSettings.config[LevelSettings.currentLevel].buggyJump) {
                            _jumpTime += delta;
                            _velocity.y += SPEED * 1.3f * (_jumpTimeLimit - _jumpTime);
                            if (_velocity.y > 2 * SPEED) _velocity.y = 2 * SPEED;
                        } else {
                            _velocity.y += 1.3f * SPEED;
                            _canPushJump = false;

                        }
                    }
                } else {
                    _canPushJump = false;
                    _jumpTime = 0.f;
                }

            }

            if(_reverseDirections) _velocity.x = -_velocity.x;
            if(LevelSettings.config[LevelSettings.currentLevel].infiniteWalk) _velocity.x = _direction*SPEED;

            if(isFrozen()) {_velocity.x = 0;  if(_velocity.y>0) _velocity.y=0;}

            if (delta < 0.08f) {
                _position.x += _velocity.x * delta;

                for(int i=0; i<_collidables.size(); i++) {
                    if(_collidables.get(i) instanceof Box) {
                        if (getCollisionMask().overlaps(_collidables.get(i).getCollisionMask())) {

                            _position.x = oldCoords.x;
                            float amount = _velocity.x * delta;
                            float pushed = ((Box)(_collidables.get(i))).push(amount);
                            _position.x = oldCoords.x + pushed;
                        }
                    } else if (_collidables.get(i) instanceof Player) {
                        if(_collidables.get(i) == this) continue;
                        if(((Player) _collidables.get(i)).isDead()) continue;
                        if(((Player) _collidables.get(i)).isFrozen()) continue;

                        if (getCollisionMask().overlaps(_collidables.get(i).getCollisionMask())) {
                            _position.x = oldCoords.x;
                        }
                    }
                }
            }

            if (_velocity.x < 0) collideX = collidesLeft();
            else if (_velocity.x > 0) collideX = collidesRight();
            if (collideX) {
                _position.x = oldCoords.x;
                _velocity.x = 0;
            }

            int n=1;
            if(_velocity.y*delta > _collisionLayer.getTileHeight()) {

                n = (int)(_velocity.y*delta / _collisionLayer.getTileHeight())+1;
            }

            for(int k=0; k<n; k++) {

                // Dirty and quick fix to the bullet behaviour
                if (delta < 0.08f) {
                    _position.y += _velocity.y / n * delta;
                }

                for (int i = 0; i < _collidables.size(); i++) {
                    if(_collidables.get(i) == this) continue;

                    if(_collidables.get(i) instanceof Player && (((Player) _collidables.get(i)).isFrozen() ||
                        ((Player) _collidables.get(i)).isFrozen())) continue;

                    if (getCollisionMask().overlaps(_collidables.get(i).getCollisionMask())) {
                        _position.y = oldCoords.y;
                        _velocity.y = 0;
                        _canJump = true;
                    }
                }

                if (_velocity.y < 0) _canJump = collideY = collidesBottom();
                else if (_velocity.y > 0) collideY = collidesTop();

                if (collideY) {
                    _position.y = oldCoords.y;
                    _velocity.y = 0;
                    break;
                }

            }

            _direction = ((_velocity.x > 0) ? 1 : ((_velocity.x < 0) ? -1 : _direction));

            // Update the sprite animation.
            if (_velocity.x == 0 && _canJump) _currentAnim = _stand;
            if (_velocity.y > 0) _currentAnim = _jump;
            if (_velocity.x != 0 && _canJump) _currentAnim = _walk;

            //for clipping (the player will appear from the other side)
            if (_direction == 1) {
                _position.x = (_position.x + _collisionLayer.getTileWidth() * _collisionLayer.getWidth()) %
                        (_collisionLayer.getTileWidth() * _collisionLayer.getWidth());
            } else {
                _position.x = (_position.x + _currentRegion.getRegionWidth() + _collisionLayer.getTileWidth()
                        * _collisionLayer.getWidth()) %
                        (_collisionLayer.getTileWidth() * _collisionLayer.getWidth()) - _currentRegion.getRegionWidth();
            }

            //vertical clipping not really used so I have commented i as it causes some problems
            // like if you jump too high you touch the floor.
            /*_position.y = (_position.y + _collisionLayer.getTileHeight() * _collisionLayer.getHeight()) %
                    (_collisionLayer.getTileHeight() * _collisionLayer.getHeight());*/

            _animationTime += delta;

            if (_currentAnim == _walk) {
                if (dustParticles.isComplete()) dustParticles.start();
                if (_direction == 1)
                    dustParticles.setPosition(_position.x + 5, _position.y + 5);
                else
                    dustParticles.setPosition(_position.x - 5 + _currentRegion.getRegionWidth(), _position.y + 5);
                dustParticles.update(delta);
                if(_isRecentlyWalk) {
                    if(!Settings.isSilent)
                        _walkSFX.loop();
                    _isRecentlyWalk = false;
                }
            } else {
                _isRecentlyWalk = true;
                if(!Settings.isSilent)
                    _walkSFX.stop();
            }
        } else {
            if(!Settings.isSilent)
                _walkSFX.stop();
            if(_isRecentlyDead) {
                EventHandler.get().notifyObservers(new Event(Event.Type.PLAYER_DYING));
                _velocity.y = 2 * SPEED;
                _velocity.x = SPEED*0.3f;
                _isRecentlyDead = false;
            }

            // Dirty and quick fix to the bullet behaviour
            if (delta < 0.2f) {
                _position.y += _velocity.y * delta;
                _position.x += _velocity.x * delta;
            }
        }


        if(isDead() && !isDying()) {
            EventHandler.get().notifyObservers(new Event(Event.Type.PLAYER_DEAD));
        }

        if(_blocked) {
            _position.x = oldCoords.x;
            if(_position.y > oldCoords.y)
                _position.y = oldCoords.y;
            _currentAnim = _stand;
            _direction = -1;
        }
    }

    public boolean isCellBlocked(float x, float y) {
        int xx = ((int)(x / _collisionLayer.getTileWidth()))%_collisionLayer.getWidth();
        int yy = ((int)(y / _collisionLayer.getTileHeight()))%_collisionLayer.getHeight();
        if(xx < 0) xx += _collisionLayer.getWidth();
        TiledMapTileLayer.Cell cell = _collisionLayer.getCell(xx, yy);
        return (cell != null);
    }

    public boolean isCellBlocked(int x, int y) {
        if(x < 0) x += _collisionLayer.getWidth();
        TiledMapTileLayer.Cell cell = _collisionLayer.getCell(x, y);
        return (cell != null);
    }

    public boolean collidesLeft() {
        Rectangle r = getCollisionMask();
        for(float step = 0; step < r.height*_scale; step += _collisionLayer.getTileHeight()*0.5f)
            if(isCellBlocked(r.x, r.y + step))
                return true;
        return false;
    }

    public boolean collidesRight() {
        Rectangle r = getCollisionMask();
        for(float step = 0; step < r.height*_scale; step += _collisionLayer.getTileHeight()*0.5f)
            if(isCellBlocked(r.x+r.width *_scale, r.y + step))
                return true;
        return false;
    }

    public boolean collidesTop() {
        Rectangle r = getCollisionMask();
        for(float step = 0; step < r.width*_scale; step += _collisionLayer.getTileWidth()*0.5f) {

            float x = r.x + step;
            float y = r.y + r.height * _scale;
            int xx = ((int)(x / _collisionLayer.getTileWidth()))%_collisionLayer.getWidth();
            int yy = ((int)(y / _collisionLayer.getTileHeight()))%_collisionLayer.getHeight();

            if (_collisionLayer.getCell(xx, yy) != null && _collisionLayer.getCell(xx, yy).getTile().getProperties().containsKey("collide")) {
                String s = (String) _collisionLayer.getCell(xx, yy).getTile().getProperties().get("collide");
                if(s.equals("buttom"))
                    return false;
            }
            if (isCellBlocked(xx, yy))
                return true;
        }
        return false;
    }

    public boolean collidesBottom() {
        Rectangle r = getCollisionMask();
        for(float step = 0; step < r.width*_scale; step += _collisionLayer.getTileWidth()*0.5f)
            if(isCellBlocked(r.x + step, r.y))
                return true;
        return false;
    }

    public Rectangle getBoundingRectangle() {
        return new Rectangle(_position.x, _position.y, _currentRegion.getRegionWidth(), _currentRegion.getRegionHeight());
    }

    @Override
    public Rectangle getCollisionMask() {
        //if(_currentRegion == null )return new Rectangle();
        float w = _currentRegion.getRegionWidth();
        float h = _currentRegion.getRegionHeight();

        return new Rectangle(_position.x + w*0.3f, _position.y, w*0.4f, h*0.95f);
    }

    public float getY() {return _position.y;}
    public float getX() {return _position.x;}


    public void setPosition(float x, float y) {
        _position.x = x; _position.y = y;
    }

    public void kill() {
        _currentAnim = _die;
        _isDead = true;
        _walkSFX.stop();
    }

    public boolean isDead() {
        return (_currentAnim == _die);
    }

    public boolean isDying() {
        return (_currentAnim == _die && _position.y+_currentRegion.getRegionHeight()*1.5f > 0);
    }

    public void stand() {
        _currentAnim = _stand;
    }

    public void setShown(boolean b){
        _isShown = b;
    }

    public boolean isShown() {
        return _isShown;
    }

    public void setReverseDirections(boolean b) {
        _reverseDirections = b;
    }

    public boolean isDirectionReversed() {
        return _reverseDirections;
    }

    public void setDefaultPos(float x, float y)
    {
        _defaultPos.x = x; _defaultPos.y = y;
    }

    public void resetDefaultPosition() {

        if(LevelSettings.config[LevelSettings.currentLevel].randomPositionX) {
            _defaultPos.x = _randomNumberGenerator.nextInt(900) + 50;
        }

        _position.x = _defaultPos.x; _position.y = _defaultPos.y;
    }

    @Override
    public void update(Observable o, Object ev)
    {
        if(_blocked) return;
        if(_isDead) return;
        if(ev == null) return;
        Event e = (Event) ev;
        switch (e.type)
        {
            case BUTTON_LEFT:
                if(e.info.buttonState.pressed == true) {
                    //_velocity.x = -SPEED;
                    _animationTime = 0;
                    _direction = -1;
                    break;
                }  else {
                    _velocity.x = 0;
                    break;
                }

            case BUTTON_JUMP:
                if(e.info.buttonState.pressed == true) {
                    if (_canJump) {
                        //_velocity.y = 2 * SPEED;
                        _velocity.y = 0.8f * SPEED;
                        _canJump = false;

                        _canPushJump = true;
                        _jumpTime = 0.f;

                        Settings.numJumps++;
                        if (!_avoidMultipleJumpFlag) {
                            EventHandler.get().notifyObservers(new Event(Event.Type.PLAYER_JUMPED));
                            _avoidMultipleJumpFlag = true;
                        }
                    }
                }
                break;
            case BUTTON_RIGHT:
                if(e.info.buttonState.pressed == true) {
                    _animationTime = 0;
                    _direction = 1;
                } else {
                    _velocity.x = 0;
                }
                break;
        }
    }
}
