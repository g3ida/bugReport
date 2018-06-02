package com.g3ida.bugreport;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Observable;
import java.util.Observer;

public class BugReport extends Game implements Observer{

    private SpriteBatch _batch;
    private SystemVolume _vol;
    private Viewport _view;
    private float _time = 0.f;

    public BugReport() {
        this(null);
    }

    public BugReport(SystemVolume v) {
        _vol = v;

    }

    public float getSoundVolume() {
        if(_vol == null) {
            return -1.f;
        }
        return _vol.getMusicVolume();

    }

    public void loadInterstitialAd() {
        if(_vol != null)
             _vol.loadInterstitialAd();
    }


    public void showVideoAd() {
        if(_vol != null) {
            _vol.showVideoAd();
        }
    }

    public boolean hasVideoReward(){
        if(_vol == null) return false;
        return  _vol.hasVideoReward();
    }

    public SpriteBatch getBatch()
    {
        return _batch;
    }

	@Override
	public void create ()
    {
        InputHandler.get().launch();
        Settings.init();
        Settings.load();
        AchievementSystem._appearAchievement = new Appear();
        _batch = new SpriteBatch();
        setScreen(new MenuScreen(this));
        _view = new ExtendViewport(Settings.SCREEN_WIDTH, Settings.SCREEN_HEIGHT, new OrthographicCamera());
        EventHandler.get().addObserver(this);
        AchievementSystem.get();
	}

	@Override
	public void render ()
    {
        float delta = Gdx.graphics.getDeltaTime();
        AchievementSystem._appearAchievement.update(delta);
        _time += delta;

        //for the time achievement
        if(_time > 5f) {
            _time = 0;
            Settings.getEllapsedTimeInSeconds();
        }

        super.render();

        if(AchievementSystem._drawAchievement == false && !AchievementSystem._achievementRegions.isEmpty()) {
            AchievementSystem._achievementRegion = AchievementSystem._achievementRegions.poll();
            EventHandler.get().notifyObservers(new Event(Event.Type.ACHIEVEMENT_SHOWN));
            AchievementSystem._drawAchievement = true;
            AchievementSystem._appearAchievement.start(-0.5f);
        }

        if(AchievementSystem._drawAchievement) {
            if (!(getScreen() instanceof LevelScreen)) {
                float px = (_view.getWorldWidth() - AchievementSystem._achievementRegion.getRegionWidth()) * 0.5f;
                float m = AchievementSystem._appearAchievement.get();
                float py = _view.getWorldHeight() - AchievementSystem._achievementRegion.getRegionHeight()
                        * 1.1f + (1 - m) * AchievementSystem._achievementRegion.getRegionHeight() * 1.1f;
                _batch.begin();

                _batch.draw(AchievementSystem._plusDiamondRegion,
                        px + AchievementSystem._achievementRegion.getRegionWidth()*0.5f - AchievementSystem._plusDiamondRegion.getRegionWidth()*0.5f,
                        py-AchievementSystem._plusDiamondRegion.getRegionHeight()*(AchievementSystem._appearAchievement.get()));

                _batch.draw(AchievementSystem._achievementRegion, px, py);
                _batch.end();
            }
            //if it has fully appeared
            if(!AchievementSystem._appearAchievement.isRunning(1.3f) && !AchievementSystem._appearAchievement.reversed()) {
                AchievementSystem._appearAchievement.reverse();
                AchievementSystem._appearAchievement.start();
            //if it has fully disappeared
            }else if (!AchievementSystem._appearAchievement.isRunning(0.1f) && AchievementSystem._appearAchievement.reversed()) {
                Settings.numDiamonds++;
                EventHandler.get().notifyObservers(new Event(Event.Type.DIAMOND_WON));
                //if there are achievements in the queue pop the next one
                if(!AchievementSystem._achievementRegions.isEmpty()) {
                    AchievementSystem._achievementRegion = AchievementSystem._achievementRegions.poll();
                    EventHandler.get().notifyObservers(new Event(Event.Type.ACHIEVEMENT_SHOWN));
                    //undo the reversing of the animation
                    AchievementSystem._appearAchievement.reverse();
                    AchievementSystem._appearAchievement.start();

                } else {
                    AchievementSystem._drawAchievement = false;
                    //undo the reversing of the animation
                    AchievementSystem._appearAchievement.reverse();
                }
            }
        }
    }

	@Override
	public void dispose ()
    {
        EventHandler.get().deleteObservers();
        InputHandler.get().kill();
        Settings.save();
        Settings.dispose();
        _batch.dispose();
        super.dispose();
    }

	@Override
	public void resize(int width, int height)
	{
        _view.update(width, height);
        super.resize(width, height);

    }

	@Override
	public void pause()
	{
        Settings.save();
        super.pause();
	}

	@Override
	public void resume()
	{
        super.resume();
	}

    @Override
    public void update(Observable observable, Object o) {

        if(o == null) return;
        if (!(o instanceof Event)) return;

        Event e = (Event) o;

        if(e.type == Event.Type.ACHIVEMENT_UNLOCKED) {
            /*if(AchievementSystem._achievementRegions.isEmpty()) {
                AchievementSystem._achievementRegions.offer(AchievementSystem.get().getAtlas().findRegion("0"));
            }*/
            AchievementSystem._achievementRegions.offer(AchievementSystem.get().getAtlas().findRegion(""+e.info.achievementNum));
        }
    }
}
