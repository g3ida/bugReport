package com.g3ida.bugreport;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.Vector;

public class Box implements Collidable {

    private boolean _alreadyPushed = false;

    private Vector2 _position = new Vector2();
    private Vector2 _velocity = new Vector2();
    private Vector2 _defaultPos = new Vector2();

    private TiledMapTileLayer _collisionLayer;
    private TextureRegion _region;
    private Vector<Collidable> _collidables;

    private  final float SPEED = 8f* Settings.PIXELS_PER_METER;
    private static final float GRAVITY = 6*9.8f * Settings.PIXELS_PER_METER;

    public void setPosition(float x, float y) {
        _position.x = x; _position.y = y;
    }

    public void setDefaultPos(float x, float y)
    {
        _defaultPos.x = x; _defaultPos.y = y;
    }

    public void resetDefaultPosition() {
        _position.x = _defaultPos.x; _position.y = _defaultPos.y;
    }

    public Box(TiledMapTileLayer collisionLayer, TextureRegion region, Vector<Collidable> collidables) {
        _region = region;
        _collisionLayer = collisionLayer;
        _collidables = collidables;
    }

    public float push(float amount) {
        _alreadyPushed=true;
        float oldX = _position.x;

        float damount = amount * 0.05f;
        int i;

        //lastx used to backup the previous position before detecting collision.
        float lastx = _position.x;
        for(i=0; i<20; i++) {
            _position.x += damount;
            if (damount > 0) {
                if (collidesRight()) {
                    _position.x = lastx;
                    break;
                }
            } else if (damount < 0) {
                if (collidesLeft()) {
                    _position.x = lastx;
                    break;
                }
            }
            lastx = _position.x;
        }

        float  pushing = _position.x - oldX;

        for(int k=0; k<_collidables.size(); k++) {
            if(_collidables.get(k) == this) continue;
            if(_collidables.get(k) instanceof Player &&
                    (((Player) _collidables.get(k)).isFrozen() || ((Player) _collidables.get(k)).isDead())) {
                continue;
            }
            else if(_collidables.get(k) instanceof Box) {
                Box b = (Box) _collidables.get(k);

                //this is done to avoid infinite recursive calls.
                if(b._alreadyPushed == true) {
                    continue;
                }

                Rectangle r = b.getCollisionMask();
                //horizental collision
                if(r.overlaps(getCollisionMask())) {
                    b._alreadyPushed = true;
                    float tmp = b.push(pushing);
                    if(Math.abs(tmp) < Math.abs(pushing)) {
                        pushing = tmp;
                    }
                    //vertical collision
                } else {
                    //the -1 is a fix of the behaviour when boxes are prefectly on the same height.
                    r.y -= _region.getRegionHeight() - 1.f;
                    if (r.overlaps(getCollisionMask())) {
                        b.push(pushing);
                    }
                }
            }
        }

        _position.x = oldX;
        _position.x += pushing;
        float res = _position.x - oldX;
        _velocity.x = 0;
        return pushing;
    }

    @Override
    public Rectangle getCollisionMask() {
        return new Rectangle(_position.x, _position.y, _region.getRegionWidth(), _region.getRegionHeight());
    }

    public void draw(SpriteBatch b) {
        b.draw(_region, _position.x, _position.y);
    }

    public void update(float delta) {

        this._alreadyPushed = false;

        if(LevelSettings.config[LevelSettings.currentLevel].gravityReversed)
            _velocity.y = - _velocity.y;

        _velocity.y = MathUtils.clamp(_velocity.y-GRAVITY*delta, -30*SPEED, 30*SPEED);

        if(LevelSettings.config[LevelSettings.currentLevel].gravityReversed)
            _velocity.y = - _velocity.y;

            Vector2 oldCoords = new Vector2(_position.x, _position.y);
        boolean collideX = false, collideY = false;
        if (delta < 0.08f)
            _position.x += _velocity.x * delta;
        if (_velocity.x < 0) collideX = collidesLeft();
        else if (_velocity.x > 0) collideX = collidesRight();
        if (collideX) {
            _position.x = oldCoords.x;
            _velocity.x = 0;
        }
        // Dirty and quick fix to the bullet behaviour
        if (delta < 0.08f) {
            _position.y += _velocity.y * delta;
        }

        if (_velocity.y < 0) collideY = collidesBottom();
        else if (_velocity.y > 0) collideY = collidesTop();

        for(int k=0; k<_collidables.size(); k++) {
            if (_collidables.get(k) == this) continue;
            if (getCollisionMask().overlaps(_collidables.get(k).getCollisionMask())) {
                collideY = true;
                break;
            }
        }

        if (collideY) {
            _position.y = oldCoords.y;
            _velocity.y = 0;
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
        for(float step = 0; step < r.height; step += _collisionLayer.getTileHeight()*0.5f)
            if(isCellBlocked(r.x, r.y + step))
                return true;
        return false;
    }

    public boolean collidesRight() {
        Rectangle r = getCollisionMask();
        for(float step = 0; step < r.height; step += _collisionLayer.getTileHeight()*0.5f)
            if(isCellBlocked(r.x+r.width, r.y + step))
                return true;
        return false;
    }

    public boolean collidesTop() {
        Rectangle r = getCollisionMask();
        for(float step = 0; step < r.width; step += _collisionLayer.getTileWidth()*0.5f)
            if(isCellBlocked(r.x + step, r.y + r.height))
                return true;
        return false;
    }

    public boolean collidesBottom() {
        Rectangle r = getCollisionMask();
        for(float step = 0; step < r.width; step += _collisionLayer.getTileWidth()*0.5f)
            if(isCellBlocked(r.x + step, r.y))
                return true;
        return false;
    }

    public float getY() {return _position.y;}
    public float getX() {return _position.x;}
}
